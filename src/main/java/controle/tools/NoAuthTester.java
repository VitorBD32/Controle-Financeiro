package controle.tools;

import controle.config.APIConfig;
import controle.util.HttpSyncUtil;

/**
 * Testa enviar dados SEM autenticação (somente os dados de transação)
 */
public class NoAuthTester {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Teste SEM Autenticação ===\n");

        String url = APIConfig.getSyncUrl();
        String secret = APIConfig.getSyncSecret();

        // JSON sem login/senha
        String jsonData = "{\"id\":999,\"tipo\":\"C\",\"valor\":100.00,\"data\":\"2025-10-28T18:00:00\",\"descricao\":\"Teste sem auth\"}";

        System.out.println("URL: " + url);
        System.out.println("JSON (sem credenciais): " + jsonData);
        System.out.println();

        String payload = HttpSyncUtil.buildEncryptedPayload(jsonData, secret);
        String response = HttpSyncUtil.sendPost(url, payload);

        System.out.println("Resposta do servidor: " + response);

        if (response.toLowerCase().contains("sucesso") || response.toLowerCase().contains("success")
                || response.toLowerCase().contains("ok") || !response.toLowerCase().contains("invalido")) {
            System.out.println("\n>>> POSSÍVEL SUCESSO! O servidor pode não exigir autenticação. <<<");
        } else {
            System.out.println("\n>>> Servidor exige autenticação. Use Opção 1 ou 2. <<<");
        }
    }
}
