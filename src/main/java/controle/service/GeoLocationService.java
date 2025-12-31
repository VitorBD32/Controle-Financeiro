package controle.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import controle.model.Localizacao;

/**
 * Serviço de Geolocalização
 * Detecta automaticamente a localização do usuário via IP
 * Não requer GPS ou permissões especiais
 * 
 * APIs gratuitas suportadas:
 * - ipapi.co (1000 requests/dia grátis)
 * - ip-api.com (45 requests/minuto grátis)
 * 
 * @author Sistema de Controle Financeiro
 * @version 1.0
 */
public class GeoLocationService {
    
    private static final String API_IPAPI_CO = "https://ipapi.co/json/";
    private static final String API_IP_API = "http://ip-api.com/json/?fields=status,country,countryCode,region,regionName,city,lat,lon,timezone";
    
    // Cache da última localização (válido por 1 hora)
    private static Localizacao ultimaLocalizacao = null;
    private static long timestampCache = 0;
    private static final long CACHE_TTL_MS = 60 * 60 * 1000; // 1 hora
    
    /**
     * Obtém a localização atual do usuário via IP
     * Usa cache de 1 hora para evitar muitas requisições
     * 
     * @return Localizacao detectada ou null se falhar
     */
    public static Localizacao obterLocalizacaoAtual() {
        return obterLocalizacaoAtual(false);
    }
    
    /**
     * Obtém a localização atual do usuário via IP
     * 
     * @param forcarAtualizacao Se true, ignora cache e faz nova requisição
     * @return Localizacao detectada ou null se falhar
     */
    public static Localizacao obterLocalizacaoAtual(boolean forcarAtualizacao) {
        // Verifica cache
        if (!forcarAtualizacao && ultimaLocalizacao != null) {
            long tempoDecorrido = System.currentTimeMillis() - timestampCache;
            if (tempoDecorrido < CACHE_TTL_MS) {
                System.out.println("[GeoLocation] Usando localização em cache: " + 
                                 ultimaLocalizacao.getCidade() + ", " + ultimaLocalizacao.getUf());
                return ultimaLocalizacao;
            }
        }
        
        System.out.println("[GeoLocation] Detectando localização via IP...");
        
        // Tenta primeira API (ipapi.co)
        Localizacao loc = tentarIPApiCo();
        if (loc != null) {
            ultimaLocalizacao = loc;
            timestampCache = System.currentTimeMillis();
            System.out.println("[GeoLocation] ✅ Localização detectada: " + loc.getCidade() + ", " + loc.getUf());
            return loc;
        }
        
        // Fallback: segunda API (ip-api.com)
        loc = tentarIPApi();
        if (loc != null) {
            ultimaLocalizacao = loc;
            timestampCache = System.currentTimeMillis();
            System.out.println("[GeoLocation] ✅ Localização detectada (fallback): " + loc.getCidade() + ", " + loc.getUf());
            return loc;
        }
        
        System.err.println("[GeoLocation] ❌ Não foi possível detectar localização");
        return null;
    }
    
    /**
     * Tenta obter localização via ipapi.co
     */
    private static Localizacao tentarIPApiCo() {
        try {
            URL url = new URL(API_IPAPI_CO);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("User-Agent", "ControleFinanceiro/1.0");
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();
                
                return parseIPApiCoResponse(response.toString());
            } else {
                System.err.println("[GeoLocation] ipapi.co retornou código: " + responseCode);
            }
        } catch (Exception e) {
            System.err.println("[GeoLocation] Erro ao chamar ipapi.co: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Tenta obter localização via ip-api.com
     */
    private static Localizacao tentarIPApi() {
        try {
            URL url = new URL(API_IP_API);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("User-Agent", "ControleFinanceiro/1.0");
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();
                
                return parseIPApiResponse(response.toString());
            } else {
                System.err.println("[GeoLocation] ip-api.com retornou código: " + responseCode);
            }
        } catch (Exception e) {
            System.err.println("[GeoLocation] Erro ao chamar ip-api.com: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Parse da resposta JSON do ipapi.co (manual, sem dependências)
     */
    private static Localizacao parseIPApiCoResponse(String json) {
        try {
            Map<String, String> dados = parseSimpleJson(json);
            
            Localizacao loc = new Localizacao();
            loc.setIp(dados.getOrDefault("ip", ""));
            loc.setCidade(dados.getOrDefault("city", ""));
            loc.setRegiao(dados.getOrDefault("region", ""));
            loc.setUf(dados.getOrDefault("region_code", ""));
            loc.setPais(dados.getOrDefault("country_name", "Brasil"));
            loc.setCodigoPais(dados.getOrDefault("country_code", "BR"));
            
            String lat = dados.getOrDefault("latitude", "0");
            String lon = dados.getOrDefault("longitude", "0");
            loc.setLatitude(Double.parseDouble(lat));
            loc.setLongitude(Double.parseDouble(lon));
            
            loc.setTimezone(dados.getOrDefault("timezone", "America/Sao_Paulo"));
            loc.setFonte("ipapi.co");
            
            return loc;
        } catch (Exception e) {
            System.err.println("[GeoLocation] Erro ao parsear resposta ipapi.co: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Parse da resposta JSON do ip-api.com
     */
    private static Localizacao parseIPApiResponse(String json) {
        try {
            Map<String, String> dados = parseSimpleJson(json);
            
            if (!"success".equals(dados.get("status"))) {
                return null;
            }
            
            Localizacao loc = new Localizacao();
            loc.setCidade(dados.getOrDefault("city", ""));
            loc.setRegiao(dados.getOrDefault("regionName", ""));
            loc.setUf(dados.getOrDefault("region", ""));
            loc.setPais(dados.getOrDefault("country", "Brasil"));
            loc.setCodigoPais(dados.getOrDefault("countryCode", "BR"));
            
            String lat = dados.getOrDefault("lat", "0");
            String lon = dados.getOrDefault("lon", "0");
            loc.setLatitude(Double.parseDouble(lat));
            loc.setLongitude(Double.parseDouble(lon));
            
            loc.setTimezone(dados.getOrDefault("timezone", "America/Sao_Paulo"));
            loc.setFonte("ip-api.com");
            
            return loc;
        } catch (Exception e) {
            System.err.println("[GeoLocation] Erro ao parsear resposta ip-api.com: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Parser JSON simples (sem biblioteca externa)
     * Funciona para JSONs simples com chave:valor
     */
    private static Map<String, String> parseSimpleJson(String json) {
        Map<String, String> map = new HashMap<>();
        
        // Remove chaves e espaços
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}")) json = json.substring(0, json.length() - 1);
        
        // Divide por vírgulas
        String[] pairs = json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim().replace("\"", "");
                String value = keyValue[1].trim().replace("\"", "");
                map.put(key, value);
            }
        }
        
        return map;
    }
    
    /**
     * Limpa o cache de localização
     */
    public static void limparCache() {
        ultimaLocalizacao = null;
        timestampCache = 0;
        System.out.println("[GeoLocation] Cache limpo");
    }
    
    /**
     * Retorna informações sobre o status do cache
     */
    public static String getStatusCache() {
        if (ultimaLocalizacao == null) {
            return "Cache vazio";
        }
        
        long tempoDecorrido = System.currentTimeMillis() - timestampCache;
        long minutosRestantes = (CACHE_TTL_MS - tempoDecorrido) / 60000;
        
        return String.format("Cache válido por mais %d minutos - %s, %s", 
                           minutosRestantes, 
                           ultimaLocalizacao.getCidade(), 
                           ultimaLocalizacao.getUf());
    }
}
