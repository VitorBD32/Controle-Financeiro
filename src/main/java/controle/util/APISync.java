package controle.util;

import br.uespi.acessoapi.ClienteHTTP;
import controle.config.APIConfig;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Classe utilitária para sincronização com API externa usando HTTP POST
 * com Content-Type: application/x-www-form-urlencoded
 */
public class APISync {

    private static final String API_URL = "http://www.datse.com.br/dev/syncjava.php";

    /**
     * Verifica login do usuário na API
     * @param usuario Nome do usuário
     * @param senha Senha do usuário
     * @return Resposta da API
     */
    public static String verificarLogin(String usuario, String senha) throws Exception {
        ClienteHTTP cliente = new ClienteHTTP(usuario, senha, API_URL);
        String resposta = cliente.conecta();
        
        System.out.println("============================================");
        System.out.println("[APISync] Login Request");
        System.out.println("[APISync] URL: " + API_URL);
        System.out.println("[APISync] Usuario: " + usuario);
        System.out.println("[APISync] Response Code: " + cliente.codretorno);
        System.out.println("[APISync] Response: " + resposta);
        System.out.println("============================================");
        
        return resposta;
    }

    /**
     * Envia dados para a API usando HTTP POST com application/x-www-form-urlencoded
     * @param params Parâmetros no formato key=value&key2=value2
     * @return Resposta da API
     */
    public static String enviarDados(String params) throws Exception {
        URL obj = new URL(API_URL);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // Set request method - HTTP POST
        con.setRequestMethod("POST");

        // Set headers - application/x-www-form-urlencoded
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        con.setRequestProperty("User-Agent", "ControleFinanceiro/1.0");
        con.setRequestProperty("Accept", "*/*");
        con.setConnectTimeout(10000);
        con.setReadTimeout(15000);

        // Send post request
        con.setDoOutput(true);
        try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
            wr.write(params.getBytes("UTF-8"));
            wr.flush();
        }

        // Get return Code
        int responseCode = con.getResponseCode();

        // Read response
        StringBuilder response = new StringBuilder();
        java.io.InputStream is = (responseCode >= 400) ? con.getErrorStream() : con.getInputStream();
        if (is != null) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }
        }

        // Log para debug
        System.out.println("============================================");
        System.out.println("[APISync] POST Request");
        System.out.println("[APISync] URL: " + API_URL);
        System.out.println("[APISync] Content-Type: application/x-www-form-urlencoded");
        System.out.println("[APISync] Params: " + (params.length() > 200 ? params.substring(0, 200) + "..." : params));
        System.out.println("[APISync] Response Code: " + responseCode);
        System.out.println("[APISync] Response: " + response.toString());
        System.out.println("============================================");

        return response.toString();
    }

    /**
     * Sincroniza um usuário com a API
     * @param nome Nome do usuário
     * @param email Email do usuário
     * @param senha Senha do usuário
     * @return Resposta da API
     */
    public static String sincronizarUsuario(String nome, String email, String senha) throws Exception {
        // Obter credenciais de autenticação do config
        String authUser = APIConfig.getAuthUser();
        String authPass = APIConfig.getAuthPassword();

        StringBuilder params = new StringBuilder();
        // Credenciais de autenticação
        params.append("usuario=").append(URLEncoder.encode(authUser != null ? authUser : "", "UTF-8"));
        params.append("&senha=").append(URLEncoder.encode(authPass != null ? authPass : "", "UTF-8"));
        // Dados do usuário
        params.append("&acao=").append(URLEncoder.encode("cadastrar_usuario", "UTF-8"));
        params.append("&nome=").append(URLEncoder.encode(nome, "UTF-8"));
        params.append("&email=").append(URLEncoder.encode(email, "UTF-8"));
        params.append("&senha_usuario=").append(URLEncoder.encode(senha, "UTF-8"));

        return enviarDados(params.toString());
    }

    /**
     * Sincroniza uma transação com a API
     * @param id ID da transação
     * @param tipo Tipo (D/C)
     * @param valor Valor da transação
     * @param data Data no formato ISO
     * @param descricao Descrição
     * @return Resposta da API
     */
    public static String sincronizarTransacao(int id, String tipo, String valor, String data, String descricao) throws Exception {
        // Obter credenciais de autenticação do config
        String authUser = APIConfig.getAuthUser();
        String authPass = APIConfig.getAuthPassword();

        StringBuilder params = new StringBuilder();
        // Credenciais de autenticação
        params.append("usuario=").append(URLEncoder.encode(authUser != null ? authUser : "", "UTF-8"));
        params.append("&senha=").append(URLEncoder.encode(authPass != null ? authPass : "", "UTF-8"));
        // Dados da transação
        params.append("&acao=").append(URLEncoder.encode("sincronizar_transacao", "UTF-8"));
        params.append("&id=").append(id);
        params.append("&tipo=").append(URLEncoder.encode(tipo != null ? tipo : "", "UTF-8"));
        params.append("&valor=").append(URLEncoder.encode(valor != null ? valor : "0", "UTF-8"));
        params.append("&data=").append(URLEncoder.encode(data != null ? data : "", "UTF-8"));
        params.append("&descricao=").append(URLEncoder.encode(descricao != null ? descricao : "", "UTF-8"));

        return enviarDados(params.toString());
    }

    /**
     * Testa a conexão com a API
     * @return true se a conexão foi bem sucedida
     */
    public static boolean testarConexao() {
        try {
            String authUser = APIConfig.getAuthUser();
            String authPass = APIConfig.getAuthPassword();
            
            String resposta = verificarLogin(authUser, authPass);
            return resposta != null && !resposta.toLowerCase().contains("invalido");
        } catch (Exception e) {
            System.out.println("[APISync] Erro ao testar conexão: " + e.getMessage());
            return false;
        }
    }
}
