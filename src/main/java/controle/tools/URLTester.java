package controle.tools;

import java.util.List;

import controle.config.APIConfig;
import controle.util.HttpSyncUtil;

/**
 * Test runner que testa todas as URLs de sincronização configuradas e mostra
 * qual delas responde com sucesso (se alguma).
 */
public class URLTester {

    public static void main(String[] args) {
        System.out.println("=== URL Tester - Teste de conectividade com endpoints de sincronização ===\n");

        List<String> urls = APIConfig.getSyncUrls();
        if (urls.isEmpty()) {
            System.out.println("ERRO: Nenhuma URL configurada em api.sync.url ou api.sync.fallbacks.");
            return;
        }

        System.out.println("URLs a serem testadas (em ordem):");
        for (int i = 0; i < urls.size(); i++) {
            System.out.println("  [" + (i + 1) + "] " + urls.get(i));
        }
        System.out.println();

        String testPayload = "encrypted_data=TEST_BASE64&salt=TEST_SALT&client_id=url-tester";

        String workingUrl = null;
        for (String url : urls) {
            System.out.println("--------------------------------------------------");
            System.out.println("Testando: " + url);
            try {
                String response = HttpSyncUtil.sendPost(url, testPayload);
                System.out.println("✓ SUCESSO! Resposta recebida:");
                System.out.println("  " + (response.length() > 300 ? response.substring(0, 300) + "..." : response));
                workingUrl = url;
                break; // encontrou URL que funciona
            } catch (Exception e) {
                System.out.println("✗ FALHA: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            }
        }

        System.out.println("\n=== Resultado Final ===");
        if (workingUrl != null) {
            System.out.println("✓ URL FUNCIONAL ENCONTRADA:");
            System.out.println("  " + workingUrl);
            System.out.println("\nRecomendação: atualize config/api.properties para usar esta URL:");
            System.out.println("  api.sync.url=" + workingUrl);
        } else {
            System.out.println("✗ NENHUMA URL RESPONDEU COM SUCESSO.");
            System.out.println("\nPossíveis causas:");
            System.out.println("  1) O servidor não tem o script PHP publicado em nenhum desses caminhos.");
            System.out.println("  2) O servidor bloqueia requisições POST desse tipo (firewall/WAF).");
            System.out.println("  3) O caminho correto é diferente dos testados.");
            System.out.println("\nAção recomendada:");
            System.out.println("  - Confirme com o administrador do servidor o caminho correto do script PHP.");
            System.out.println("  - Teste manualmente com curl: curl -v -X POST '<URL>' --data 'encrypted_data=TEST'");
            System.out.println("  - Se necessário, implante o script PHP fornecido em tools/server_stub/syncjava.php");
        }
    }
}
