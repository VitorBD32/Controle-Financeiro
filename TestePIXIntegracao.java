import br.uespi.acessoapi.ClienteHTTP;
import br.uespi.acessoapi.PIXConexao;
import br.uespi.acessoapi.pagamentoHTTP;
import br.uespi.tratajson.trataJSON;
import br.uespi.tratajson.JSONObject;
import br.uespi.pessoas.pessoa;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * Classe de teste para integração com API PIX
 * Demonstra a conexão com a API e tratamento da resposta JSON
 */
public class TestePIXIntegracao {

    private static final String CONFIG_FILE = "config/api.properties";
    
    public static void main(String[] args) {
        System.out.println("============================================");
        System.out.println("   TESTE DE INTEGRAÇÃO COM API PIX");
        System.out.println("============================================\n");
        
        try {
            // Carrega configurações
            Properties props = carregarConfiguracoes();
            // Prefer values from config or environment via APIConfig
            String apiUrl = controle.config.APIConfig.getSyncUrl();
            String usuario = controle.config.APIConfig.getAuthUser();
            // Do not default to a test password; prefer empty and encourage users to set via config or env
            String senha = controle.config.APIConfig.getAuthPassword();
            
            System.out.println("[INFO] Configurações carregadas:");
            System.out.println("  - URL da API: " + apiUrl);
            System.out.println("  - Usuário: " + usuario);
            System.out.println();
            
            // Teste 1: Conexão básica com ClienteHTTP
            System.out.println("--------------------------------------------");
            System.out.println("TESTE 1: Conexão básica com ClienteHTTP");
            System.out.println("--------------------------------------------");
            testarConexaoBasica(usuario, senha, apiUrl);
            
            // Teste 2: Simulação de pagamento PIX
            System.out.println("\n--------------------------------------------");
            System.out.println("TESTE 2: Simulação de Pagamento PIX");
            System.out.println("--------------------------------------------");
            testarPagamentoPIX(apiUrl);
            
            // Teste 3: Conexão PIX específica
            System.out.println("\n--------------------------------------------");
            System.out.println("TESTE 3: Conexão PIX com autenticação");
            System.out.println("--------------------------------------------");
            testarConexaoPIX(apiUrl, usuario, senha);
            
        } catch (Exception e) {
            System.err.println("\n[ERRO] Falha na execução dos testes:");
            e.printStackTrace();
        }
        
        System.out.println("\n============================================");
        System.out.println("   FIM DOS TESTES DE INTEGRAÇÃO");
        System.out.println("============================================");
    }
    
    /**
     * Carrega as configurações do arquivo api.properties
     */
    private static Properties carregarConfiguracoes() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);
            System.out.println("[OK] Arquivo de configuração carregado: " + CONFIG_FILE);
        } catch (Exception e) {
            System.out.println("[AVISO] Não foi possível carregar " + CONFIG_FILE + ", usando valores padrão.");
        }
        return props;
    }
    
    /**
     * Teste 1: Conexão básica usando ClienteHTTP
     */
    private static void testarConexaoBasica(String usuario, String senha, String url) {
        try {
            System.out.println("[INFO] Iniciando conexão com a API...");
            System.out.println("[INFO] URL: " + url);
            System.out.println("[INFO] Usuário: " + usuario);
            
            ClienteHTTP conexao = new ClienteHTTP(usuario, senha, url);
            String resposta = conexao.conecta();
            
            System.out.println("\n[OK] Conexão realizada com sucesso!");
            System.out.println("[INFO] Código de retorno HTTP: " + conexao.codretorno);
            System.out.println("[INFO] Resposta da API (raw): " + resposta);
            
            // Tenta tratar o JSON
            if (resposta != null && !resposta.isEmpty()) {
                try {
                    tratarRespostaJSON(resposta);
                } catch (Exception e) {
                    System.out.println("[INFO] Resposta não é JSON estruturado esperado: " + e.getMessage());
                }
            }
            
        } catch (java.net.ConnectException e) {
            System.err.println("[ERRO] Falha de conexão: Servidor não acessível");
            System.err.println("  Detalhes: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[ERRO] Falha na conexão básica: " + e.getMessage());
        }
    }
    
    /**
     * Teste 2: Simulação de pagamento PIX usando pagamentoHTTP
     */
    private static void testarPagamentoPIX(String url) {
        try {
            System.out.println("[INFO] Simulando pagamento PIX...");
            
            String nome = "Teste Usuario";
            String cpf = "12345678900";
            String ncartao = "PIX-CHAVE-TESTE";
            String valor = "100.00";
            String tipopag = "PIX";
            
            System.out.println("[INFO] Dados do pagamento:");
            System.out.println("  - Nome: " + nome);
            System.out.println("  - CPF: " + cpf);
            System.out.println("  - Chave PIX: " + ncartao);
            System.out.println("  - Valor: R$ " + valor);
            System.out.println("  - Tipo: " + tipopag);
            
            pagamentoHTTP pagamento = new pagamentoHTTP(nome, cpf, ncartao, valor, tipopag, url);
            String resposta = pagamento.conecta();
            
            System.out.println("\n[OK] Requisição de pagamento enviada!");
            System.out.println("[INFO] Código de retorno HTTP: " + pagamento.codretorno);
            System.out.println("[INFO] Resposta da API: " + resposta);
            
            // Tenta tratar o JSON
            if (resposta != null && !resposta.isEmpty()) {
                try {
                    tratarRespostaJSON(resposta);
                } catch (Exception e) {
                    System.out.println("[INFO] Processando resposta alternativa...");
                    tratarRespostaAlternativa(resposta);
                }
            }
            
        } catch (java.net.ConnectException e) {
            System.err.println("[ERRO] Falha de conexão: Servidor não acessível");
        } catch (Exception e) {
            System.err.println("[ERRO] Falha no pagamento PIX: " + e.getMessage());
        }
    }
    
    /**
     * Teste 3: Conexão PIX com classe específica
     */
    private static void testarConexaoPIX(String url, String usuario, String senha) {
        try {
            System.out.println("[INFO] Testando conexão PIX específica...");
            
            PIXConexao pixConn = new PIXConexao(url);
            pixConn.setDadosPix("chave@pix.teste", "50.00", "Teste de integração PIX", "Recebedor Teste");
            pixConn.setAutenticacao(usuario, senha);
            
            System.out.println("[INFO] Dados configurados:");
            System.out.println("  - Chave PIX: " + pixConn.getChavePix());
            System.out.println("  - Valor: R$ " + pixConn.getValor());
            System.out.println("  - Descrição: " + pixConn.getDescricao());
            System.out.println("  - Recebedor: " + pixConn.getNomeRecebedor());
            
            String resposta = pixConn.conectar();
            
            System.out.println("\n[OK] Conexão PIX realizada!");
            System.out.println("[INFO] Código de retorno HTTP: " + pixConn.getCodRetorno());
            System.out.println("[INFO] Resposta da API: " + resposta);
            
            // Tratamento JSON
            if (resposta != null && !resposta.isEmpty()) {
                try {
                    tratarRespostaJSON(resposta);
                } catch (Exception e) {
                    tratarRespostaAlternativa(resposta);
                }
            }
            
        } catch (java.net.ConnectException e) {
            System.err.println("[ERRO] Falha de conexão: Servidor não acessível");
        } catch (Exception e) {
            System.err.println("[ERRO] Falha na conexão PIX: " + e.getMessage());
        }
    }
    
    /**
     * Trata a resposta JSON usando a classe trataJSON
     */
    private static void tratarRespostaJSON(String jsonString) {
        System.out.println("\n[INFO] Tratando resposta JSON com trataJSON...");
        
        trataJSON tratador = new trataJSON(jsonString);
        pessoa resultado = tratador.tratarString();
        
        System.out.println("\n[OK] JSON processado com sucesso!");
        System.out.println("╔════════════════════════════════════════════╗");
        System.out.println("║       RESULTADO DO PROCESSAMENTO JSON      ║");
        System.out.println("╠════════════════════════════════════════════╣");
        System.out.println("║ ID:              " + formatarCampo(resultado.getId()));
        System.out.println("║ Modo:            " + formatarCampo(resultado.getModo()));
        System.out.println("║ Valor Autorizado:" + formatarCampo(resultado.getValor()));
        System.out.println("║ Mensagem:        " + formatarCampo(resultado.getRetmsg()));
        System.out.println("╚════════════════════════════════════════════╝");
    }
    
    /**
     * Tratamento alternativo para respostas que não seguem o padrão esperado
     */
    private static void tratarRespostaAlternativa(String resposta) {
        System.out.println("\n[INFO] Processando resposta com JSONObject direto...");
        
        try {
            JSONObject json = new JSONObject(resposta);
            
            System.out.println("\n[OK] JSON parseado com sucesso!");
            System.out.println("╔════════════════════════════════════════════╗");
            System.out.println("║         CONTEÚDO DO JSON RECEBIDO          ║");
            System.out.println("╠════════════════════════════════════════════╣");
            
            // Itera sobre as chaves do JSON
            for (String key : json.keySet()) {
                Object value = json.get(key);
                System.out.println("║ " + key + ": " + value);
            }
            
            System.out.println("╚════════════════════════════════════════════╝");
            
        } catch (Exception e) {
            System.out.println("[INFO] Resposta (texto): " + resposta);
        }
    }
    
    /**
     * Formata campo para exibição na tabela
     */
    private static String formatarCampo(String valor) {
        if (valor == null) return "(vazio)";
        String resultado = valor.length() > 24 ? valor.substring(0, 21) + "..." : valor;
        return String.format("%-24s║", resultado);
    }
}
