package controle.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class HttpSyncUtil {

    public static String sendPost(String url, String payload) throws Exception {
        int maxAttempts = 3;
        int attempt = 0;
        long backoff = 500; // ms
        Exception lastEx = null;
        while (attempt < maxAttempts) {
            attempt++;
            HttpURLConnection con = null;
            try {
                // Diagnostic logging: print destination and a short payload preview
                try {
                    System.out.println("[HttpSyncUtil] Sending POST to: " + url);
                    String preview = payload.length() > 200 ? payload.substring(0, 200) + "..." : payload;
                    System.out.println("[HttpSyncUtil] Payload preview (first 200 chars): " + preview);
                } catch (Throwable _t) {
                    // ignore logging errors
                }
                URL obj = new URL(url);
                con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                // If credentials are configured, add Basic Authorization header
                try {
                    String authUser = controle.config.APIConfig.getAuthUser();
                    String authPass = controle.config.APIConfig.getAuthPassword();
                    if (authUser != null && !authUser.isEmpty() && authPass != null && !authPass.isEmpty()) {
                        String basic = java.util.Base64.getEncoder().encodeToString((authUser + ":" + authPass).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                        con.setRequestProperty("Authorization", "Basic " + basic);
                    }
                } catch (Throwable ignored) {
                    // ignore errors while trying to set auth header
                }
                con.setConnectTimeout(5000); // 5s
                con.setReadTimeout(10000); // 10s
                con.setDoOutput(true);

                try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                    wr.write(payload.getBytes("UTF-8"));
                    wr.flush();
                }

                int responseCode = con.getResponseCode();
                java.io.InputStream is = null;
                if (responseCode >= 400) {
                    is = con.getErrorStream();
                } else {
                    is = con.getInputStream();
                }
                StringBuilder response = new StringBuilder();
                if (is != null) {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                    }
                }
                // Diagnostic logging of response code and a short response preview
                try {
                    System.out.println("[HttpSyncUtil] Response code: " + responseCode);
                    String respPreview = response.length() > 1000 ? response.substring(0, 1000) + "..." : response.toString();
                    System.out.println("[HttpSyncUtil] Response preview: " + respPreview);
                } catch (Throwable _t) {
                    // ignore logging errors
                }

                if (responseCode >= 200 && responseCode < 300) {
                    return response.toString();
                } else {
                    throw new RuntimeException("HTTP error code: " + responseCode + " response: " + response.toString());
                }
            } catch (java.net.ConnectException ce) {
                lastEx = ce;
                // don't spam; try again after backoff
                if (attempt >= maxAttempts) {
                    throw new java.net.ConnectException("Não foi possível conectar ao servidor " + url + " (" + ce.getMessage() + ")");
                }
                Thread.sleep(backoff);
                backoff *= 2;
            } catch (Exception ex) {
                lastEx = ex;
                // for other exceptions, decide to retry only on temporary IO errors
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
        throw lastEx != null ? lastEx : new RuntimeException("Unknown error in sendPost");
    }

    public static String buildEncryptedPayload(String data, String secret) throws Exception {
        int saltLen = controle.config.APIConfig.getSaltLength();
        byte[] salt = new byte[saltLen];
        new java.security.SecureRandom().nextBytes(salt);
        javax.crypto.spec.SecretKeySpec key = AESUtil.deriveKey(secret.toCharArray(), salt, controle.config.APIConfig.getPbkdf2Iterations(), controle.config.APIConfig.getAesKeyLength());
        String encryptedData = AESUtil.encrypt(data, key);
        StringBuilder sb = new StringBuilder();
        sb.append("encrypted_data=").append(URLEncoder.encode(encryptedData, "UTF-8"));
        sb.append("&salt=").append(URLEncoder.encode(java.util.Base64.getEncoder().encodeToString(salt), "UTF-8"));
        sb.append("&client_id=").append(URLEncoder.encode(System.getProperty("user.name", "desktop-client"), "UTF-8"));
        return sb.toString();
    }
}
