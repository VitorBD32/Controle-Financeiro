package controle.tools;

import controle.Conexao;
import controle.config.DBConfig;

public class CheckDB {
    public static void main(String[] args) {
        System.out.println("=== CHECK DB INFO ===");

        try {
            // Print JDBC URL (note: may include flags)
            String url = DBConfig.getUrl();
            System.out.println("JDBC URL: " + url);

            // Print user but never the password
            String user = DBConfig.getUser();
            System.out.println("DB User: " + (user == null || user.isEmpty() ? "<not set>" : user));

            // Indicate if the DB password is present in the environment/file (do NOT print it)
            String pwd = DBConfig.getPassword();
            System.out.println("DB Password configured? " + (pwd != null && !pwd.isEmpty() ? "YES" : "NO"));

            // Try a quick connection test
            System.out.println("Testing DB connection...");
            boolean ok = false;
            try (java.sql.Connection c = Conexao.getConnection()) {
                ok = (c != null && !c.isClosed());
            }
            System.out.println("Connection OK: " + ok);

            if (!ok) {
                System.out.println("If this is false, check your config/db.properties or DB_* environment variables and credentials.");
            } else {
                System.out.println("Connection successful. If you still can't log in, check the user row's password format (bcrypt vs MD5/legacy) and whether the user is authorized.");
            }
        } catch (Exception e) {
            System.err.println("Error while checking DB: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}
