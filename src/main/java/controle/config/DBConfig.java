package controle.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DBConfig {

    private static final String CONFIG_PATH = "config/db.properties";
    // single final Properties instance
    private static final Properties props = new Properties();

    static {
        // try load from working dir first, then from classpath
        try (FileInputStream in = new FileInputStream(CONFIG_PATH)) {
            props.load(in);
        } catch (IOException e) {
            try (java.io.InputStream in2 = DBConfig.class.getClassLoader().getResourceAsStream("config/db.properties")) {
                if (in2 != null) {
                    props.load(in2);
                }
            } catch (IOException ex) {
                // leave empty
            }
        }
    }

    public static String getUrl() {
        // Prioriza variáveis de ambiente, depois arquivo de propriedades, depois valores padrão
        String host = System.getenv("DB_HOST");
        if (host == null || host.isEmpty()) {
            host = props.getProperty("host", "127.0.0.1");
        }
        String port = System.getenv("DB_PORT");
        if (port == null || port.isEmpty()) {
            port = props.getProperty("port", "3306");
        }
        String db = System.getenv("DB_DATABASE");
        if (db == null || db.isEmpty()) {
            db = props.getProperty("database", "controle_financeiro");
        }
        // build JDBC URL. For production you should enable SSL and avoid allowPublicKeyRetrieval.
        boolean allowKeyRetrieval = Boolean.parseBoolean(props.getProperty("allowPublicKeyRetrieval", "false"));
        boolean useSsl = Boolean.parseBoolean(props.getProperty("useSSL", "true"));
        String extra = String.format("useSSL=%s&serverTimezone=UTC", useSsl);
        if (allowKeyRetrieval) {
            extra = "allowPublicKeyRetrieval=true&" + extra;
        }
        return String.format("jdbc:mysql://%s:%s/%s?%s", host, port, db, extra);
    }

    public static String getUser() {
        String user = System.getenv("DB_USER");
        if (user == null || user.isEmpty()) {
            user = props.getProperty("user", "root");
        }
        return user;
    }

    public static String getPassword() {
        String pwd = System.getenv("DB_PASSWORD");
        if (pwd == null) {
            pwd = props.getProperty("password", "");
        }
        return pwd;
    }
}
