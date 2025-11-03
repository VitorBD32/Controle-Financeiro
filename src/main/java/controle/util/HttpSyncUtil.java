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

    /**
     * Tenta enviar em plain (form fields `nome` e `senha` extraídos do JSON)
     * primeiro. Se a tentativa plain falhar (não-2xx ou exceção), envia o
     * payload criptografado. Retorna a resposta do servidor no primeiro envio
     * bem-sucedido.
     */
    public static String sendWithPlainFallback(String url, String jsonData, String secret) throws Exception {
        // Tentativa: vários formatos plain antes do envio criptografado.
        String[] nameKeys = new String[]{"nome", "login", "user", "username", "email", "usuario"};
        String[] passKeys = new String[]{"senha", "password", "pass"};

        // tenta extrair uma senha clara do JSON (se presente) para usar nas tentativas
        String foundPassword = null;
        try {
            java.util.regex.Pattern pSenhaAny = java.util.regex.Pattern.compile("\\\"(?:senha|password|pass)\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"");
            java.util.regex.Matcher mSenhaAny = pSenhaAny.matcher(jsonData);
            if (mSenhaAny.find()) {
                foundPassword = mSenhaAny.group(1);
            }
        } catch (Throwable ignored) {
        }

        // função auxiliar para detectar resposta de falha de autenticação
        java.util.function.Predicate<String> isAuthFailure = (resp) -> {
            if (resp == null) {
                return true;
            }
            String lc = resp.toLowerCase();
            return lc.contains("login invalido") || lc.contains("login inválido") || lc.contains("invalid login")
                    || lc.contains("login failed") || lc.contains("senha incorreta") || lc.contains("credencial")
                    || lc.contains("usuario nao encontrado") || lc.contains("usuario não encontrado");
        };

        // gera MD5 hex (lowercase)
        java.util.function.Function<String, String> md5Hex = (s) -> {
            try {
                java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
                byte[] digest = md.digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                for (byte b : digest) {
                    sb.append(String.format("%02x", b));
                }
                return sb.toString();
            } catch (Exception e) {
                return null;
            }
        };

        // tenta combinações plain (plaintext)
        if (foundPassword != null) {
            for (String nk : nameKeys) {
                for (String pk : passKeys) {
                    try {
                        String nameVal = APIConfigSafe(nk, jsonData);
                        if (nameVal == null || nameVal.isEmpty()) {
                            continue;
                        }
                        String[] nameVariants = new String[]{nameVal, nameVal.toLowerCase()};
                        for (String nv : nameVariants) {
                            try {
                                StringBuilder plain = new StringBuilder();
                                plain.append(nk).append("=").append(URLEncoder.encode(nv, "UTF-8"));
                                plain.append("&").append(pk).append("=").append(URLEncoder.encode(foundPassword, "UTF-8"));
                                plain.append("&data=").append(URLEncoder.encode(jsonData, "UTF-8"));
                                System.out.println("[HttpSyncUtil] Trying plain form (" + nk + "," + pk + ") value='" + nv + "' -> " + url);
                                String resp = sendPost(url, plain.toString());
                                if (!isAuthFailure.test(resp)) {
                                    return resp;
                                } else {
                                    System.out.println("[HttpSyncUtil] Server reported auth failure for (" + nk + "," + pk + ") value='" + nv + "'.");
                                }
                            } catch (Exception e) {
                                System.out.println("[HttpSyncUtil] Plain attempt (" + nk + ") failed: " + e.getMessage());
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("[HttpSyncUtil] Plain attempt wrapper failed for key " + nk + ": " + e.getMessage());
                    }
                }
            }

            // tenta combinações com MD5 da senha (compatibilidade com sistemas legados)
            String pwdMd5 = md5Hex.apply(foundPassword);
            if (pwdMd5 != null) {
                for (String nk : nameKeys) {
                    for (String pk : passKeys) {
                        try {
                            String nameVal = APIConfigSafe(nk, jsonData);
                            if (nameVal == null || nameVal.isEmpty()) {
                                continue;
                            }
                            String[] nameVariants = new String[]{nameVal, nameVal.toLowerCase()};
                            for (String nv : nameVariants) {
                                try {
                                    StringBuilder plain = new StringBuilder();
                                    plain.append(nk).append("=").append(URLEncoder.encode(nv, "UTF-8"));
                                    plain.append("&").append(pk).append("=").append(URLEncoder.encode(pwdMd5, "UTF-8"));
                                    plain.append("&data=").append(URLEncoder.encode(jsonData, "UTF-8"));
                                    System.out.println("[HttpSyncUtil] Trying plain form with MD5 (" + nk + "," + pk + ") value='" + nv + "' -> " + url);
                                    String resp = sendPost(url, plain.toString());
                                    if (!isAuthFailure.test(resp)) {
                                        return resp;
                                    } else {
                                        System.out.println("[HttpSyncUtil] Server reported auth failure for MD5 attempt (" + nk + "," + pk + ") value='" + nv + "'.");
                                    }
                                } catch (Exception e) {
                                    System.out.println("[HttpSyncUtil] Plain MD5 attempt (" + nk + ") failed: " + e.getMessage());
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("[HttpSyncUtil] Plain MD5 attempt wrapper failed for key " + nk + ": " + e.getMessage());
                        }
                    }
                }
            }
        }

        // Se nada funcionou, envia o payload criptografado como antes
        String encrypted = buildEncryptedPayload(jsonData, secret);
        String respEnc = sendPost(url, encrypted);
        if (!isAuthFailure.test(respEnc)) {
            return respEnc;
        }
        // Se mesmo o envio encriptado retorna 'login invalido', lançar exceção para o chamador tratar.
        throw new RuntimeException("Autenticacao rejeitada pelo servidor: " + respEnc);
    }

    // Extrai um valor apropriado para o campo de nome (tenta vários campos no JSON)
    private static String APIConfigSafe(String key, String jsonData) {
        // tenta extrair 'nome', 'login', 'user', 'username' do jsonData
        try {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\\"" + key + "\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"");
            java.util.regex.Matcher m = p.matcher(jsonData);
            if (m.find()) {
                return m.group(1);
            }
        } catch (Throwable ignored) {
        }
        // fallback: tenta extrair 'nome' generico
        try {
            java.util.regex.Pattern p2 = java.util.regex.Pattern.compile("\\\"nome\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"");
            java.util.regex.Matcher m2 = p2.matcher(jsonData);
            if (m2.find()) {
                return m2.group(1);
            }
        } catch (Throwable ignored) {
        }
        return "";
    }
}
