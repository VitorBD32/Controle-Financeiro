package controle.tools;

import controle.config.APIConfig;
import controle.util.HttpSyncUtil;

/**
 * Testa diferentes formatos de autenticação para descobrir o que o servidor
 * aceita
 */
public class AuthTester {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Teste de Autenticação - Descobrindo formato aceito pelo servidor ===\n");

        String url = APIConfig.getSyncUrl();
        String secret = APIConfig.getSyncSecret();
        String login = APIConfig.getAuthUser();
        String senha = APIConfig.getAuthPassword();

        System.out.println("Configuração atual:");
        System.out.println("  URL: " + url);
        System.out.println("  Login: " + login);
        System.out.println("  Senha: " + senha);
        System.out.println();

        // Teste 1: Campos originais (login/senha)
        System.out.println("--------------------------------------------------");
        System.out.println("Teste 1: Usando campos 'login' e 'senha'");
        String json1 = String.format("{\"login\":\"%s\",\"senha\":\"%s\",\"teste\":\"auth1\"}", login, senha);
        testarPayload(url, secret, json1, "login/senha");

        // Teste 2: Campos em inglês (username/password)
        System.out.println("\n--------------------------------------------------");
        System.out.println("Teste 2: Usando campos 'username' e 'password'");
        String json2 = String.format("{\"username\":\"%s\",\"password\":\"%s\",\"teste\":\"auth2\"}", login, senha);
        testarPayload(url, secret, json2, "username/password");

        // Teste 3: Campos user/pass
        System.out.println("\n--------------------------------------------------");
        System.out.println("Teste 3: Usando campos 'user' e 'pass'");
        String json3 = String.format("{\"user\":\"%s\",\"pass\":\"%s\",\"teste\":\"auth3\"}", login, senha);
        testarPayload(url, secret, json3, "user/pass");

        // Teste 4: Só dados sem auth (para ver a resposta de erro)
        System.out.println("\n--------------------------------------------------");
        System.out.println("Teste 4: Sem credenciais (para ver mensagem de erro)");
        String json4 = "{\"teste\":\"auth4\"}";
        testarPayload(url, secret, json4, "sem auth");

        // Teste 5: Payload não criptografado (plain text) - para debug
        System.out.println("\n--------------------------------------------------");
        System.out.println("Teste 5: Payload NÃO criptografado (plain text)");
        String plainPayload = String.format("login=%s&senha=%s&teste=plain", login, senha);
        try {
            String resp = HttpSyncUtil.sendPost(url, plainPayload);
            System.out.println("✓ Resposta: " + resp);
        } catch (Exception e) {
            System.out.println("✗ Erro: " + e.getMessage());
        }

        System.out.println("\n=== Conclusão ===");
        System.out.println("Verifique qual teste retornou algo diferente de 'Login invalido'.");
        System.out.println("A resposta de sucesso pode ser JSON, texto, ou código específico.");
    }

    private static void testarPayload(String url, String secret, String jsonData, String label) {
        try {
            System.out.println("JSON antes de criptografar: " + jsonData);
            String payload = HttpSyncUtil.buildEncryptedPayload(jsonData, secret);
            System.out.println("Enviando payload criptografado...");
            String response = HttpSyncUtil.sendPost(url, payload);
            System.out.println("✓ RESPOSTA (" + label + "): " + response);

            if (!response.toLowerCase().contains("invalido") && !response.toLowerCase().contains("invalid")) {
                System.out.println(">>> POSSÍVEL SUCESSO! Este formato pode estar correto. <<<");
            }
        } catch (Exception e) {
            System.out.println("✗ ERRO (" + label + "): " + e.getMessage());
        }
    }
}
