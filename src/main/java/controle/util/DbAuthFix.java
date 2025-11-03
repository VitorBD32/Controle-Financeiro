package controle.util;

import java.sql.Connection;
import java.sql.Statement;

import controle.Conexao;

public class DbAuthFix {

    // One-time utility to switch root user auth plugin to mysql_native_password
    public static void main(String[] args) {
        System.out.println("Running DB auth fix (will attempt ALTER USER to mysql_native_password)");
        try (Connection conn = Conexao.getConnection(); Statement st = conn.createStatement()) {
            String sql = "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'UESPI'";
            st.execute(sql);
            st.execute("FLUSH PRIVILEGES");
            System.out.println("OK: ALTER USER executed. PHP should be able to connect now.");
        } catch (Exception e) {
            System.err.println("Failed to run auth fix: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }
}
