package controle.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DBConfig {

    private static final String CONFIG_PATH = "config/db.properties";
    // single final Properties instance to avoid initialization warnings
    private static final Properties props = new Properties();

    static {
        // try working-directory file first
        try (FileInputStream in = new FileInputStream(CONFIG_PATH)) {
            props.load(in);
        } catch (IOException e) {
            // fallback to classpath resource (e.g., packaged defaults)
            try (var inStream = DBConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (inStream != null) {
                    props.load(inStream);
                }
            } catch (IOException ignored) {
                // leave properties empty
            }
        }
    }

    public static String getUrl() {
        String host = props.getProperty("host", "127.0.0.1");
        String port = props.getProperty("port", "3306");
        String db = props.getProperty("database", "controle_financeiro");
        // Build optional query parameters from properties
        StringBuilder params = new StringBuilder();
        String useSSL = props.getProperty("useSSL");
        if (useSSL != null) {
            params.append("useSSL=").append(useSSL);
        } else {
            params.append("useSSL=false");
        }
        String allowPKR = props.getProperty("allowPublicKeyRetrieval");
        if (allowPKR != null) {
            params.append("&allowPublicKeyRetrieval=").append(allowPKR);
        }
        String serverTZ = props.getProperty("serverTimezone");
        if (serverTZ != null) {
            params.append("&serverTimezone=").append(serverTZ);
        } else {
            params.append("&serverTimezone=UTC");
        }
        return String.format("jdbc:mysql://%s:%s/%s?%s", host, port, db, params.toString());
    }

    public static String getUser() {
        return props.getProperty("user", "root");
    }

    public static String getPassword() {
        return props.getProperty("password", "");
    }
}
