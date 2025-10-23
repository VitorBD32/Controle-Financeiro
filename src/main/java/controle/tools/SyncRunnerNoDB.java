package controle.tools;

import controle.config.APIConfig;
import controle.util.HttpSyncUtil;

public class SyncRunnerNoDB {

    public static void main(String[] args) {
        try {
            // monta um JSON de exemplo com uma transacao
            String json = "{\"id\":999,\"tipo\":\"D\",\"valor\":123.45,\"data\":\"2025-10-23T00:00:00\",\"descricao\":\"TesteNoDB\"}";
            String secret = APIConfig.getSyncSecret();
            String payload = HttpSyncUtil.buildEncryptedPayload(json, secret);
            String url = APIConfig.getSyncUrl();
            // Debug info to help diagnose mode/URL selection
            System.out.println("Env API_MODE=" + System.getenv("API_MODE"));
            System.out.println("APIConfig.getMode()=" + APIConfig.getMode());
            System.out.println("APIConfig.getMockUrl()=" + APIConfig.getMockUrl());
            System.out.println("APIConfig.getSyncUrl()=" + url);
            System.out.println("Sending to: " + url);
            String resp = HttpSyncUtil.sendPost(url, payload);
            System.out.println("Response: " + resp);
        } catch (Exception ex) {
            System.err.println("SyncRunnerNoDB failed: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
            ex.printStackTrace(System.err);
            System.exit(1);
        }
    }
}
