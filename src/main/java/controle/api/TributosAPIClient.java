package controle.api;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import controle.dao.AliquotaCacheDAO;
import controle.model.Localizacao;
import controle.model.TributosCalculados;
import controle.service.GeoLocationService;

/**
 * Cliente para consulta de alíquotas de tributos (CBS, IBS, IS)
 * por município e estado via API externa ou base local
 * 
 * Integrado com a Reforma Tributária 2026 (EC 132/2023)
 * Suporta detecção automática de localização via IP
 * 
 * @author Sistema de Controle Financeiro
 * @version 1.1 (com geolocalização)
 */
public class TributosAPIClient {
    
    private static final String BRASILAPI_BASE = "https://brasilapi.com.br/api";
    private final HttpClient httpClient;
    private final AliquotaCacheDAO cacheDAO;
    
    // Alíquotas padrão (fallback se API não responder)
    private static final BigDecimal CBS_PADRAO = new BigDecimal("0.088");  // 8.8%
    private static final BigDecimal IBS_PADRAO = new BigDecimal("0.15");   // 15%
    private static final BigDecimal IS_PADRAO = new BigDecimal("0.0");     // 0% (seletivo)
    
    // Cache de alíquotas por UF/município (evita chamadas excessivas)
    private final Map<String, Map<String, BigDecimal>> cacheMemoria;
    
    public TributosAPIClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.cacheDAO = new AliquotaCacheDAO();
        this.cacheMemoria = new HashMap<>();
    }
    
    /**
     * Calcula impostos (CBS, IBS, IS) com DETECÇÃO AUTOMÁTICA de localização
     * Detecta UF e município via IP do usuário
     * 
     * @param valorBase Valor sobre o qual calcular tributos
     * @param tipoServico Tipo de serviço/produto (para identificar IS)
     * @return TributosCalculados com valores detalhados
     */
    public TributosCalculados calcularImpostosComLocalizacaoAutomatica(BigDecimal valorBase, String tipoServico) {
        System.out.println("[TRIBUTOS] 🌍 Detectando localização automática para cálculo de impostos...");
        
        Localizacao loc = GeoLocationService.obterLocalizacaoAtual();
        
        if (loc != null && loc.isValida()) {
            System.out.println("[TRIBUTOS] ✅ Localização detectada: " + loc.getDescricaoResumida());
            return calcularImpostos(valorBase, loc.getUf(), loc.getCidade(), tipoServico);
        } else {
            System.err.println("[TRIBUTOS] ⚠️ Não foi possível detectar localização. Usando alíquotas padrão.");
            return calcularImpostosComAliquotasPadrao(valorBase, tipoServico);
        }
    }
    
    /**
     * Calcula impostos usando alíquotas padrão (fallback)
     */
    private TributosCalculados calcularImpostosComAliquotasPadrao(BigDecimal valorBase, String tipoServico) {
        BigDecimal aliqIS = isSujeitoIS(tipoServico) ? IS_PADRAO : BigDecimal.ZERO;
        
        BigDecimal valorCBS = valorBase.multiply(CBS_PADRAO).setScale(2, RoundingMode.HALF_UP);
        BigDecimal valorIBS = valorBase.multiply(IBS_PADRAO).setScale(2, RoundingMode.HALF_UP);
        BigDecimal valorIS = valorBase.multiply(aliqIS).setScale(2, RoundingMode.HALF_UP);
        
        TributosCalculados resultado = new TributosCalculados(valorBase, valorCBS, valorIBS, valorIS, "BR", "Padrão");
        resultado.setAliquotaCBS(CBS_PADRAO);
        resultado.setAliquotaIBS(IBS_PADRAO);
        resultado.setAliquotaIS(aliqIS);
        
        return resultado;
    }
    
    /**
     * Calcula impostos (CBS, IBS, IS) sobre valor base
     * Consulta alíquotas por UF e município
     * 
     * @param valorBase Valor sobre o qual calcular tributos
     * @param uf Sigla do estado (ex: "PI", "SP")
     * @param codigoMunicipio Código IBGE do município (7 dígitos) ou nome
     * @param tipoServico Tipo de serviço/produto (para identificar IS)
     * @return TributosCalculados com valores detalhados
     */
    public TributosCalculados calcularImpostos(BigDecimal valorBase, String uf, String codigoMunicipio, String tipoServico) {
        if (valorBase == null || valorBase.compareTo(BigDecimal.ZERO) <= 0) {
            return new TributosCalculados(valorBase, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, uf, codigoMunicipio);
        }
        
        // Busca alíquotas (cache -> DB -> API -> padrão)
        Map<String, BigDecimal> aliquotas = getAliquotas(uf, codigoMunicipio);
        
        BigDecimal aliqCBS = aliquotas.getOrDefault("CBS", CBS_PADRAO);
        BigDecimal aliqIBS = aliquotas.getOrDefault("IBS", IBS_PADRAO);
        BigDecimal aliqIS = aliquotas.getOrDefault("IS", IS_PADRAO);
        
        // Aplica IS seletivo apenas para produtos específicos
        if (tipoServico != null && !isSujeitoIS(tipoServico)) {
            aliqIS = BigDecimal.ZERO;
        }
        
        // Calcula valores
        BigDecimal valorCBS = valorBase.multiply(aliqCBS).setScale(2, RoundingMode.HALF_UP);
        BigDecimal valorIBS = valorBase.multiply(aliqIBS).setScale(2, RoundingMode.HALF_UP);
        BigDecimal valorIS = valorBase.multiply(aliqIS).setScale(2, RoundingMode.HALF_UP);
        
        TributosCalculados resultado = new TributosCalculados(valorBase, valorCBS, valorIBS, valorIS, uf, codigoMunicipio);
        resultado.setAliquotaCBS(aliqCBS);
        resultado.setAliquotaIBS(aliqIBS);
        resultado.setAliquotaIS(aliqIS);
        
        System.out.println("[TRIBUTOS] Cálculo realizado: Base=" + valorBase + 
                          " | CBS=" + valorCBS + " | IBS=" + valorIBS + 
                          " | IS=" + valorIS + " | Total=" + resultado.getTotalTributos());
        
        return resultado;
    }
    
    /**
     * Obtém alíquotas de CBS, IBS e IS para UF/município
     * Tenta cache em memória -> DB local -> API -> valores padrão
     */
    private Map<String, BigDecimal> getAliquotas(String uf, String codigoMunicipio) {
        String chave = uf + "_" + (codigoMunicipio != null ? codigoMunicipio : "DEFAULT");
        
        // 1. Cache em memória (rápido)
        if (cacheMemoria.containsKey(chave)) {
            return cacheMemoria.get(chave);
        }
        
        // 2. Cache no banco (persistente)
        try {
            Map<String, BigDecimal> fromDB = cacheDAO.getAliquotas(uf, codigoMunicipio);
            if (fromDB != null && !fromDB.isEmpty()) {
                cacheMemoria.put(chave, fromDB);
                return fromDB;
            }
        } catch (Exception e) {
            System.err.println("[WARN] Erro ao consultar cache DB de alíquotas: " + e.getMessage());
        }
        
        // 3. Consulta API externa (BrasilAPI ou mock)
        try {
            Map<String, BigDecimal> fromAPI = consultarAPIExterna(uf, codigoMunicipio);
            if (fromAPI != null && !fromAPI.isEmpty()) {
                // Salva no cache
                cacheMemoria.put(chave, fromAPI);
                try {
                    cacheDAO.salvarAliquotas(uf, codigoMunicipio, fromAPI);
                } catch (Exception e) {
                    System.err.println("[WARN] Erro ao salvar cache de alíquotas: " + e.getMessage());
                }
                return fromAPI;
            }
        } catch (Exception e) {
            System.err.println("[WARN] Erro ao consultar API de tributos: " + e.getMessage());
        }
        
        // 4. Fallback: alíquotas padrão
        Map<String, BigDecimal> padrao = new HashMap<>();
        padrao.put("CBS", CBS_PADRAO);
        padrao.put("IBS", IBS_PADRAO);
        padrao.put("IS", IS_PADRAO);
        
        cacheMemoria.put(chave, padrao);
        return padrao;
    }
    
    /**
     * Consulta API externa (mock/simulado - adapte para API real)
     * TODO: Integrar com API real de consulta de alíquotas (Sefaz, BrasilAPI, etc)
     */
    private Map<String, BigDecimal> consultarAPIExterna(String uf, String codigoMunicipio) throws IOException, InterruptedException {
        // MOCK: retorna alíquotas padrão (substituir por chamada real)
        // Exemplo de endpoint fictício: GET https://api.tributos.gov.br/aliquotas?uf=PI&municipio=2211001
        
        Map<String, BigDecimal> resultado = new HashMap<>();
        
        // Variação por UF (simplificado - ajustar conforme legislação real)
        BigDecimal ibsUF = IBS_PADRAO;
        switch (uf != null ? uf.toUpperCase() : "") {
            case "SP":
            case "RJ":
                ibsUF = new BigDecimal("0.17");  // Estados com IBS mais alto
                break;
            case "PI":
            case "MA":
            case "CE":
                ibsUF = new BigDecimal("0.14");  // Nordeste
                break;
            default:
                ibsUF = IBS_PADRAO;
        }
        
        resultado.put("CBS", CBS_PADRAO);  // Federal, uniforme
        resultado.put("IBS", ibsUF);       // Estadual/Municipal, varia
        resultado.put("IS", IS_PADRAO);    // Seletivo, depende do produto
        
        System.out.println("[API-MOCK] Alíquotas consultadas para " + uf + "/" + codigoMunicipio + 
                          ": CBS=" + CBS_PADRAO + ", IBS=" + ibsUF);
        
        return resultado;
    }
    
    /**
     * Verifica se produto/serviço está sujeito ao Imposto Seletivo (IS)
     * IS aplica-se a: bebidas alcoólicas, cigarros, veículos, etc.
     */
    private boolean isSujeitoIS(String tipoServico) {
        if (tipoServico == null) return false;
        String tipo = tipoServico.toLowerCase();
        
        return tipo.contains("bebida") || tipo.contains("alcool") || 
               tipo.contains("cigarro") || tipo.contains("veiculo") ||
               tipo.contains("combustivel") || tipo.contains("luxo");
    }
    
    /**
     * Valida município via BrasilAPI (opcional - para validar código IBGE)
     */
    public String validarMunicipio(String codigoIBGE) {
        try {
            String url = BRASILAPI_BASE + "/ibge/municipios/v1/" + codigoIBGE;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                // Parse JSON simples (pode usar biblioteca ou regex)
                String body = response.body();
                if (body.contains("\"nome\"")) {
                    return body;  // Retorna JSON com dados do município
                }
            }
        } catch (Exception e) {
            System.err.println("[WARN] Erro ao validar município: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Limpa cache em memória (útil para testes)
     */
    public void limparCache() {
        cacheMemoria.clear();
        System.out.println("[TRIBUTOS] Cache de alíquotas limpo");
    }
}
