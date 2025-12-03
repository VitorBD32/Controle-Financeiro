package controle.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Utilitário HTTP para sincronização com API externa.
 * Usa HTTP POST com Content-Type: application/x-www-form-urlencoded
 */
public class HttpSyncUtil {

    /**
     * Envia requisição POST com application/x-www-form-urlencoded
     */
    public static String sendPost(String url, String payload) throws Exception {
        int maxAttempts = 3;
        int attempt = 0;
        long backoff = 500; // ms
        Exception lastEx = null;
        
        while (attempt < maxAttempts) {
            attempt++;
            HttpURLConnection con = null;
            try {
                // Log de diagnóstico
                System.out.println("============================================");
                System.out.println("[HttpSyncUtil] POST Request - Tentativa " + attempt);
                System.out.println("[HttpSyncUtil] URL: " + url);
                System.out.println("[HttpSyncUtil] Content-Type: application/x-www-form-urlencoded");
                String preview = payload.length() > 200 ? payload.substring(0, 200) + "..." : payload;
                System.out.println("[HttpSyncUtil] Payload: " + preview);
                
                URL obj = new URL(url);
                con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("POST");
                
                // Headers - application/x-www-form-urlencoded
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                con.setRequestProperty("User-Agent", "ControleFinanceiro/1.0");
                con.setRequestProperty("Accept", "*/*");
                
                // Adicionar credenciais Basic Auth se configuradas
                try {
                    String authUser = controle.config.APIConfig.getAuthUser();
                    String authPass = controle.config.APIConfig.getAuthPassword();
                    if (authUser != null && !authUser.isEmpty() && authPass != null && !authPass.isEmpty()) {
                        String basic = java.util.Base64.getEncoder().encodeToString(
                            (authUser + ":" + authPass).getBytes(java.nio.charset.StandardCharsets.UTF_8)
                        );
                        con.setRequestProperty("Authorization", "Basic " + basic);
                    }
                } catch (Throwable ignored) {}
                
                con.setConnectTimeout(10000); // 10s
                con.setReadTimeout(15000);    // 15s
                con.setDoOutput(true);

                // Enviar dados
                try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                    wr.write(payload.getBytes("UTF-8"));
                    wr.flush();
                }

                int responseCode = con.getResponseCode();
                
                // Ler resposta
                java.io.InputStream is = (responseCode >= 400) ? con.getErrorStream() : con.getInputStream();
                StringBuilder response = new StringBuilder();
                if (is != null) {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                    }
                }
                
                // Log da resposta
                System.out.println("[HttpSyncUtil] Response Code: " + responseCode);
                String respPreview = response.length() > 500 ? response.substring(0, 500) + "..." : response.toString();
                System.out.println("[HttpSyncUtil] Response: " + respPreview);
                System.out.println("============================================");

                if (responseCode >= 200 && responseCode < 300) {
                    return response.toString();
                } else {
                    throw new RuntimeException("HTTP error code: " + responseCode + " response: " + response.toString());
                }
            } catch (java.net.ConnectException ce) {
                lastEx = ce;
                System.out.println("[HttpSyncUtil] Conexão falhou: " + ce.getMessage());
                if (attempt >= maxAttempts) {
                    throw new java.net.ConnectException("Não foi possível conectar ao servidor " + url + " (" + ce.getMessage() + ")");
                }
                Thread.sleep(backoff);
                backoff *= 2;
            } catch (Exception ex) {
                lastEx = ex;
                System.out.println("[HttpSyncUtil] Erro: " + ex.getMessage());
                if (attempt >= maxAttempts) {
                    throw ex;
                }
                Thread.sleep(backoff);
                backoff *= 2;
            } finally {
                if (con != null) {
                    con.disconnect();
                }
            }
        }
        throw lastEx != null ? lastEx : new RuntimeException("Erro desconhecido em sendPost");
    }

    /**
     * Constrói payload form-urlencoded simples com credenciais e dados JSON
     */
    public static String buildFormPayload(String jsonData) throws Exception {
        String authUser = controle.config.APIConfig.getAuthUser();
        String authPass = controle.config.APIConfig.getAuthPassword();
        
        StringBuilder sb = new StringBuilder();
        sb.append("usuario=").append(URLEncoder.encode(authUser != null ? authUser : "", "UTF-8"));
        sb.append("&senha=").append(URLEncoder.encode(authPass != null ? authPass : "", "UTF-8"));
        sb.append("&data=").append(URLEncoder.encode(jsonData, "UTF-8"));
        
        return sb.toString();
    }

    /**
     * Envia dados para a API com autenticação
     */
    public static String sendWithAuth(String url, String jsonData) throws Exception {
        String payload = buildFormPayload(jsonData);
        return sendPost(url, payload);
    }

    // Mantém compatibilidade com código existente
    public static String buildEncryptedPayload(String data, String secret) throws Exception {
        // Simplificado: usa payload simples ao invés de criptografado
        return buildFormPayload(data);
    }

    public static String sendWithPlainFallback(String url, String jsonData, String secret) throws Exception {
        return sendWithAuth(url, jsonData);
    }
}
