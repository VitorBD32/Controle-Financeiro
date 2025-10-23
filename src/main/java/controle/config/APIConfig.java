package controle.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class APIConfig {

    private static Properties props = new Properties();

    static {
        try (FileInputStream fis = new FileInputStream("config/api.properties")) {
            props.load(fis);
        } catch (IOException e) {
            // Fallback to environment variables
            props.setProperty("api.sync.url", System.getenv().getOrDefault("API_SYNC_URL", "http://www.datse.com.br/dev/syncjava2.php"));
            props.setProperty("api.sync.secret", System.getenv().getOrDefault("API_SYNC_SECRET", "default-secret"));
            props.setProperty("api.sync.salt.length", "16");
            props.setProperty("api.sync.pbkdf2.iterations", "20000");
            props.setProperty("api.sync.aes.key.length", "256");
            // mode: prod | mock
            props.setProperty("api.mode", System.getenv().getOrDefault("API_MODE", "prod"));
            // mock URL default
            props.setProperty("api.mock.url", System.getenv().getOrDefault("API_MOCK_URL", "http://127.0.0.1:8000/syncjava.php"));
        }
    }

    public static String getSyncUrl() {
        // mode can be 'prod' or 'mock' (default 'prod'). Environment variable API_MODE overrides.
        String mode = System.getenv().getOrDefault("API_MODE", props.getProperty("api.mode", "prod")).trim().toLowerCase();
        if ("mock".equals(mode)) {
            String mock = props.getProperty("api.mock.url");
            if (mock != null && !mock.isEmpty()) {
                return mock;
            }
        }
        return props.getProperty("api.sync.url");
    }

    public static String getMode() {
        return System.getenv().getOrDefault("API_MODE", props.getProperty("api.mode", "prod"));
    }

    public static String getMockUrl() {
        return props.getProperty("api.mock.url", "http://127.0.0.1:8000/syncjava.php");
    }

    public static String getSyncSecret() {
        return props.getProperty("api.sync.secret");
    }

    public static int getSaltLength() {
        return Integer.parseInt(props.getProperty("api.sync.salt.length"));
    }

    public static int getPbkdf2Iterations() {
        return Integer.parseInt(props.getProperty("api.sync.pbkdf2.iterations"));
    }

    public static int getAesKeyLength() {
        return Integer.parseInt(props.getProperty("api.sync.aes.key.length"));
    }

    public static String getAuthUser() {
        String v = props.getProperty("api.auth.user");
        if (v == null || v.isEmpty()) {
            v = System.getenv("API_AUTH_USER");
        }
        return v;
    }

    public static String getAuthPassword() {
        String v = props.getProperty("api.auth.password");
        if (v == null || v.isEmpty()) {
            v = System.getenv("API_AUTH_PASSWORD");
        }
        return v;
    }
}
