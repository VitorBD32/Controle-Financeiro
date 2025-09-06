package controle;

import controle.config.DBConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Legacy backup Conexao helper — unified and fixed. Uses DBConfig for
 * URL/user/password and throws SQLException on failure.
 */
public class Conexao {

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
        String url = DBConfig.getUrl();
        String user = DBConfig.getUser();
        String password = DBConfig.getPassword();
        return DriverManager.getConnection(url, user, password);
    }
}
