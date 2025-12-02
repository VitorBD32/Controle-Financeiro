package controle.api;

import br.uespi.acessoapi.ClienteHTTP;
import br.uespi.acessoapi.PIXConexao;
import br.uespi.tratajson.JSONObject;
import br.uespi.tratajson.JSONArray;
import controle.model.Transacao;
import controle.model.Usuario;
import controle.config.APIConfig;
import controle.dao.TransacaoDAO;
import controle.dao.TransacaoDAOImpl;
import controle.dao.CategoriaDAO;
import controle.dao.CategoriaDAOImpl;
import controle.dao.UsuarioDAO;
import controle.dao.UsuarioDAOImpl;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.util.List;

/**
 * Classe de integração com a API externa
 * Conecta o sistema desktop com serviços REST e mostra resultados JSON
 * INCLUI: Filtro por usuário, exibição no terminal, geração de nota fiscal
 */
public class APIIntegration {

    private String baseUrl;
    private String authUser;
    private String authPassword;
    private int authUsuarioId = -1; // ID do usuário no banco de dados
    private int lastResponseCode;
    private String lastResponse;
    private String lastJsonFormatado;

    public APIIntegration() {
        this.baseUrl = APIConfig.getSyncUrls().get(0);
        this.authUser = APIConfig.getAuthUser();
        this.authPassword = APIConfig.getAuthPassword();
        buscarIdUsuario();
    }

    public APIIntegration(String url, String user, String password) {
        this.baseUrl = url;
        this.authUser = user;
        this.authPassword = password;
        buscarIdUsuario();
    }

    /**
     * Busca o ID do usuário no banco de dados pelo nome
     */
    private void buscarIdUsuario() {
        try {
            UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
            var usuarios = usuarioDAO.findAll();
            for (var u : usuarios) {
                if (u.getNome().equalsIgnoreCase(authUser)) {
                    this.authUsuarioId = u.getId();
                    System.out.println("✅ Usuário identificado: " + authUser + " (ID: " + authUsuarioId + ")");
                    return;
                }
            }
            System.out.println("⚠️ Usuário não encontrado no banco: " + authUser);
        } catch (Exception e) {
            System.out.println("⚠️ Não foi possível buscar ID do usuário: " + e.getMessage());
        }
    }

    /**
     * Define manualmente o ID do usuário (útil quando já se sabe o ID)
     */
    public void setAuthUsuarioId(int id) {
        this.authUsuarioId = id;
    }

    public int getAuthUsuarioId() {
        return authUsuarioId;
    }

    /**
     * Testa a conexão com a API - MOSTRA JSON NO TERMINAL
     */
    public String testarConexao() {
        StringBuilder result = new StringBuilder();
        
        // === LOG NO TERMINAL ===
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🔗 CONEXÃO API - CONTROLE FINANCEIRO");
        System.out.println("=".repeat(60));
        System.out.println("📡 URL: " + baseUrl);
        System.out.println("👤 Usuário: " + authUser);
        System.out.println("-".repeat(60));
        
        result.append("=== TESTE DE CONEXÃO COM API ===\n");
        result.append("URL: ").append(baseUrl).append("\n");
        result.append("Usuário: ").append(authUser).append("\n\n");

        try {
            ClienteHTTP cliente = new ClienteHTTP(authUser, authPassword, baseUrl);
            String resposta = cliente.conecta();
            lastResponseCode = cliente.codretorno;
            lastResponse = resposta;

            result.append("Código HTTP: ").append(lastResponseCode).append("\n");
            result.append("Resposta RAW: ").append(resposta).append("\n\n");

            // === LOG NO TERMINAL ===
            System.out.println("📥 RESPOSTA DO SERVIDOR:");
            System.out.println("-".repeat(60));

            // Filtra e formata JSON apenas do usuário
            try {
                lastJsonFormatado = filtrarJSONPorUsuario(resposta);
                result.append("=== JSON FILTRADO (Usuário: ").append(authUser).append(") ===\n");
                result.append(lastJsonFormatado).append("\n");
                
                // Exibe no terminal
                System.out.println(lastJsonFormatado);
            } catch (Exception e) {
                JSONObject json = new JSONObject(resposta);
                lastJsonFormatado = json.toString(2);
                System.out.println(lastJsonFormatado);
                result.append("=== JSON PARSEADO ===\n");
                result.append(lastJsonFormatado).append("\n");
            }
            
            System.out.println("-".repeat(60));
            System.out.println("✅ Conexão realizada com sucesso!");
            System.out.println("=".repeat(60) + "\n");

        } catch (Exception e) {
            String erro = "ERRO: " + e.getMessage();
            result.append(erro).append("\n");
            System.out.println("❌ " + erro);
            e.printStackTrace();
        }

        return result.toString();
    }

    /**
     * Filtra o JSON para mostrar apenas dados do usuário logado
     * Filtra por usuario_id se disponível, senão por nome
     */
    private String filtrarJSONPorUsuario(String jsonStr) {
        try {
            if (jsonStr == null || jsonStr.trim().isEmpty()) {
                return "Resposta vazia";
            }

            if (jsonStr.trim().startsWith("[")) {
                JSONArray arrayOriginal = new JSONArray(jsonStr);
                JSONArray arrayFiltrado = new JSONArray();
                
                System.out.println("🔍 Filtrando por usuario_id: " + authUsuarioId + " (usuário: " + authUser + ")");
                
                for (int i = 0; i < arrayOriginal.length(); i++) {
                    JSONObject obj = arrayOriginal.getJSONObject(i);
                    
                    // PRIORIDADE 1: Filtra por usuario_id (mais preciso)
                    if (authUsuarioId > 0 && obj.has("usuario_id")) {
                        int objUsuarioId = obj.optInt("usuario_id", -1);
                        if (objUsuarioId == authUsuarioId) {
                            arrayFiltrado.put(obj);
                            continue;
                        }
                    }
                    
                    // PRIORIDADE 2: Se não tem usuario_id, filtra por nome (menos preciso)
                    // Só usa se authUsuarioId não estiver definido
                    if (authUsuarioId <= 0) {
                        String nomeObj = obj.optString("nome", obj.optString("usuario", ""));
                        if (nomeObj.equalsIgnoreCase(authUser) || 
                            obj.optString("usuario_nome", "").equalsIgnoreCase(authUser)) {
                            arrayFiltrado.put(obj);
                        }
                    }
                }
                
                System.out.println("📊 Total original: " + arrayOriginal.length() + " | Filtrado: " + arrayFiltrado.length());
                
                if (arrayFiltrado.length() == 0) {
                    // Se não encontrou nada filtrado, informa que não há transações deste usuário
                    System.out.println("⚠️ Nenhuma transação encontrada para o usuario_id: " + authUsuarioId);
                    return "[\n  // Nenhuma transação encontrada para o usuário " + authUser + " (ID: " + authUsuarioId + ")\n]";
                }
                return arrayFiltrado.toString(2);
                
            } else if (jsonStr.trim().startsWith("{")) {
                JSONObject obj = new JSONObject(jsonStr);
                return obj.toString(2);
            }
            
            return jsonStr;
        } catch (Exception e) {
            return jsonStr;
        }
    }

    /**
     * Sincroniza uma transação com a API e retorna o JSON de resultado
     */
    public String sincronizarTransacao(Transacao t) {
        StringBuilder result = new StringBuilder();
        result.append("=== SINCRONIZANDO TRANSAÇÃO ===\n");
        result.append("ID: ").append(t.getId()).append("\n");
        result.append("Descrição: ").append(t.getDescricao()).append("\n");
        result.append("Valor: ").append(t.getValor()).append("\n\n");

        try {
            // Monta o JSON da transação
            JSONObject jsonTransacao = new JSONObject();
            jsonTransacao.put("nome", authUser);
            jsonTransacao.put("senha", authPassword);
            jsonTransacao.put("id", t.getId());
            jsonTransacao.put("tipo", t.getTipo());
            jsonTransacao.put("valor", t.getValor() != null ? t.getValor().toString() : "0");
            jsonTransacao.put("descricao", t.getDescricao() != null ? t.getDescricao() : "");
            
            if (t.getData() != null) {
                jsonTransacao.put("data", t.getData().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }

            result.append("JSON Enviado:\n").append(jsonTransacao.toString(2)).append("\n\n");

            // Envia para a API
            String resposta = enviarPost(baseUrl, jsonTransacao.toString());
            result.append("Código HTTP: ").append(lastResponseCode).append("\n");
            result.append("Resposta RAW: ").append(resposta).append("\n\n");

            // Tenta parsear resposta como JSON
            try {
                JSONObject jsonResposta = new JSONObject(resposta);
                result.append("=== JSON RESPOSTA ===\n");
                result.append(jsonResposta.toString(2)).append("\n");
            } catch (Exception e) {
                result.append("Resposta não é JSON válido\n");
            }

        } catch (Exception e) {
            result.append("ERRO: ").append(e.getMessage()).append("\n");
        }

        return result.toString();
    }

    /**
     * Sincroniza múltiplas transações - MOSTRA JSON NO TERMINAL
     * CORRIGIDO: Busca o nome real do usuário de cada transação
     */
    public String sincronizarTransacoes(List<Transacao> transacoes) {
        StringBuilder result = new StringBuilder();
        
        // === LOG NO TERMINAL ===
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🔄 SINCRONIZAÇÃO DE TRANSAÇÕES");
        System.out.println("=".repeat(60));
        System.out.println("👤 Usuário logado: " + authUser + " (ID: " + authUsuarioId + ")");
        System.out.println("📊 Total de transações: " + transacoes.size());
        System.out.println("-".repeat(60));
        
        result.append("=== SINCRONIZAÇÃO EM LOTE ===\n");
        result.append("Total de transações: ").append(transacoes.size()).append("\n\n");

        // Carrega mapa de usuários para buscar nomes
        java.util.Map<Integer, String> mapaUsuarios = new java.util.HashMap<>();
        try {
            UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
            var usuarios = usuarioDAO.findAll();
            for (var u : usuarios) {
                mapaUsuarios.put(u.getId(), u.getNome());
            }
            System.out.println("📋 Usuários carregados: " + mapaUsuarios);
        } catch (Exception e) {
            System.out.println("⚠️ Não foi possível carregar usuários: " + e.getMessage());
        }

        int sucesso = 0;
        int falha = 0;
        JSONArray jsonArrayEnviado = new JSONArray();

        for (Transacao t : transacoes) {
            try {
                // Busca o nome REAL do usuário dono desta transação
                String nomeUsuarioReal = mapaUsuarios.getOrDefault(t.getIdUsuario(), authUser);
                
                JSONObject jsonTransacao = new JSONObject();
                jsonTransacao.put("nome", nomeUsuarioReal); // USA O NOME REAL, NÃO authUser
                jsonTransacao.put("senha", authPassword);
                jsonTransacao.put("id", t.getId());
                jsonTransacao.put("tipo", t.getTipo());
                jsonTransacao.put("valor", t.getValor() != null ? t.getValor().toString() : "0");
                jsonTransacao.put("descricao", t.getDescricao() != null ? t.getDescricao() : "");
                jsonTransacao.put("usuario_id", t.getIdUsuario());
                jsonTransacao.put("categoria_id", t.getIdCategoria());
                
                if (t.getData() != null) {
                    jsonTransacao.put("data", t.getData().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }

                jsonArrayEnviado.put(jsonTransacao);

                String resposta = enviarPost(baseUrl, jsonTransacao.toString());
                
                if (lastResponseCode >= 200 && lastResponseCode < 300) {
                    sucesso++;
                    result.append("✓ Transação ").append(t.getId()).append(" (").append(nomeUsuarioReal).append(") sincronizada\n");
                    System.out.println("✅ Transação " + t.getId() + " (" + nomeUsuarioReal + ") sincronizada");
                } else {
                    falha++;
                    result.append("✗ Transação ").append(t.getId()).append(" falhou: ").append(resposta).append("\n");
                    System.out.println("❌ Transação " + t.getId() + " falhou");
                }

            } catch (Exception e) {
                falha++;
                result.append("✗ Transação ").append(t.getId()).append(" erro: ").append(e.getMessage()).append("\n");
                System.out.println("❌ Transação " + t.getId() + " erro: " + e.getMessage());
            }
        }

        lastJsonFormatado = jsonArrayEnviado.toString(2);
        
        // === LOG NO TERMINAL ===
        System.out.println("-".repeat(60));
        System.out.println("📤 JSON ENVIADO:");
        System.out.println(lastJsonFormatado);
        System.out.println("-".repeat(60));
        System.out.println("✅ Sucesso: " + sucesso + " | ❌ Falha: " + falha);
        System.out.println("=".repeat(60) + "\n");

        result.append("\n=== RESUMO ===\n");
        result.append("Sucesso: ").append(sucesso).append("\n");
        result.append("Falha: ").append(falha).append("\n");

        return result.toString();
    }

    /**
     * Consulta dados da API via GET - MOSTRA JSON NO TERMINAL
     */
    public String consultarAPI(String endpoint) {
        StringBuilder result = new StringBuilder();
        
        // === LOG NO TERMINAL ===
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🔍 CONSULTA API");
        System.out.println("=".repeat(60));
        System.out.println("📡 Endpoint: " + endpoint);
        System.out.println("👤 Usuário: " + authUser);
        System.out.println("-".repeat(60));
        
        result.append("=== CONSULTA API ===\n");
        result.append("Endpoint: ").append(endpoint).append("\n\n");

        try {
            PIXConexao pix = new PIXConexao(baseUrl);
            String resposta = pix.consultarGet(endpoint);
            lastResponseCode = pix.getCodRetorno();
            lastResponse = resposta;

            result.append("Código HTTP: ").append(lastResponseCode).append("\n");
            result.append("Resposta RAW: ").append(resposta).append("\n\n");

            // === LOG NO TERMINAL ===
            System.out.println("📥 RESPOSTA:");

            // Tenta parsear como JSON
            try {
                if (resposta.trim().startsWith("[")) {
                    lastJsonFormatado = filtrarJSONPorUsuario(resposta);
                    result.append("=== JSON ARRAY FILTRADO ===\n");
                    result.append(lastJsonFormatado).append("\n");
                } else {
                    JSONObject json = new JSONObject(resposta);
                    lastJsonFormatado = json.toString(2);
                    result.append("=== JSON OBJECT ===\n");
                    result.append(lastJsonFormatado).append("\n");
                }
                System.out.println(lastJsonFormatado);
            } catch (Exception e) {
                System.out.println(resposta);
                result.append("Resposta não é JSON válido\n");
            }
            
            System.out.println("=".repeat(60) + "\n");

        } catch (Exception e) {
            result.append("ERRO: ").append(e.getMessage()).append("\n");
            System.out.println("❌ ERRO: " + e.getMessage());
        }

        return result.toString();
    }

    /**
     * Gera JSON da Nota Fiscal com todas as transações do banco
     */
    public String gerarNotaFiscal() {
        StringBuilder result = new StringBuilder();
        
        // === LOG NO TERMINAL ===
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🧾 GERANDO NOTA FISCAL");
        System.out.println("=".repeat(60));
        System.out.println("👤 Usuário: " + authUser);
        System.out.println("-".repeat(60));

        try {
            TransacaoDAO transacaoDAO = new TransacaoDAOImpl();
            CategoriaDAO categoriaDAO = new CategoriaDAOImpl();
            
            List<Transacao> transacoes = transacaoDAO.findAll();

            JSONObject notaFiscal = new JSONObject();
            notaFiscal.put("emitente", "Controle Financeiro - Sistema Desktop");
            notaFiscal.put("usuario", authUser);
            notaFiscal.put("data_emissao", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            notaFiscal.put("numero_nota", "NF-" + System.currentTimeMillis());

            JSONArray itens = new JSONArray();
            BigDecimal totalReceitas = BigDecimal.ZERO;
            BigDecimal totalDespesas = BigDecimal.ZERO;

            for (Transacao t : transacoes) {
                JSONObject item = new JSONObject();
                item.put("id", t.getId());
                item.put("descricao", t.getDescricao() != null ? t.getDescricao() : "Sem descrição");
                
                // Busca nome da categoria
                try {
                    var cat = categoriaDAO.findById(t.getIdCategoria());
                    item.put("categoria", cat != null ? cat.getNome() : "ID: " + t.getIdCategoria());
                } catch (Exception e) {
                    item.put("categoria", "ID: " + t.getIdCategoria());
                }
                
                item.put("valor", t.getValor() != null ? t.getValor().toString() : "0.00");
                item.put("tipo", "R".equals(t.getTipo()) ? "RECEITA" : "DESPESA");
                
                if (t.getData() != null) {
                    item.put("data", t.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                }
                
                itens.put(item);

                if ("R".equals(t.getTipo()) || "C".equals(t.getTipo())) {
                    totalReceitas = totalReceitas.add(t.getValor() != null ? t.getValor() : BigDecimal.ZERO);
                } else {
                    totalDespesas = totalDespesas.add(t.getValor() != null ? t.getValor() : BigDecimal.ZERO);
                }
            }

            notaFiscal.put("itens", itens);
            notaFiscal.put("total_itens", itens.length());
            notaFiscal.put("total_receitas", "R$ " + totalReceitas.toString());
            notaFiscal.put("total_despesas", "R$ " + totalDespesas.toString());
            notaFiscal.put("saldo", "R$ " + totalReceitas.subtract(totalDespesas).toString());

            lastJsonFormatado = notaFiscal.toString(2);
            lastResponse = lastJsonFormatado;

            // === LOG NO TERMINAL ===
            System.out.println("📄 NOTA FISCAL GERADA:");
            System.out.println("-".repeat(60));
            System.out.println(lastJsonFormatado);
            System.out.println("-".repeat(60));
            System.out.println("✅ Nota fiscal gerada com sucesso!");
            System.out.println("=".repeat(60) + "\n");

            result.append("=== NOTA FISCAL ===\n\n");
            result.append(lastJsonFormatado);

        } catch (Exception e) {
            result.append("ERRO ao gerar nota fiscal: ").append(e.getMessage()).append("\n");
            System.out.println("❌ ERRO: " + e.getMessage());
            e.printStackTrace();
        }

        return result.toString();
    }

    public String getLastJsonFormatado() {
        return lastJsonFormatado;
    }

    /**
     * Envia dados via POST e retorna resposta
     */
    private String enviarPost(String urlStr, String jsonData) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "ControleFinanceiro-Desktop/1.0");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setConnectTimeout(15000);
        con.setReadTimeout(15000);
        con.setDoOutput(true);

        // Envia JSON
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(jsonData);
        wr.flush();
        wr.close();

        lastResponseCode = con.getResponseCode();

        // Lê resposta
        BufferedReader in;
        if (lastResponseCode >= 200 && lastResponseCode < 300) {
            in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        } else {
            in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        }

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        lastResponse = response.toString();
        return lastResponse;
    }

    /**
     * Envia dados via POST com form-urlencoded
     */
    public String enviarFormPost(String usuario, String senha) throws Exception {
        StringBuilder result = new StringBuilder();
        result.append("=== CONEXÃO VIA FORM POST ===\n");
        result.append("URL: ").append(baseUrl).append("\n");
        result.append("Usuário: ").append(usuario).append("\n\n");

        try {
            ClienteHTTP cliente = new ClienteHTTP(usuario, senha, baseUrl);
            String resposta = cliente.conecta();
            lastResponseCode = cliente.codretorno;
            lastResponse = resposta;

            result.append("Código HTTP: ").append(lastResponseCode).append("\n");
            result.append("Resposta: ").append(resposta).append("\n\n");

            // Parse JSON
            try {
                JSONObject json = new JSONObject(resposta);
                result.append("=== JSON FORMATADO ===\n");
                result.append(json.toString(2)).append("\n");
            } catch (Exception e) {
                // não é JSON
            }

        } catch (Exception e) {
            result.append("ERRO: ").append(e.getMessage()).append("\n");
        }

        return result.toString();
    }

    public int getLastResponseCode() {
        return lastResponseCode;
    }

    public String getLastResponse() {
        return lastResponse;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
