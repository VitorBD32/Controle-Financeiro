package controle.util;

import java.sql.Connection;
import java.sql.Statement;

import controle.Conexao;

public class MakeAdminUser {
    public static void main(String[] args) {
        int id = 1;
        if (args.length > 0) {
            try { id = Integer.parseInt(args[0]); } catch (Exception e) { }
        }
        System.out.println("Setting admin=1 for usuario id=" + id);
        String sql = "UPDATE usuarios SET admin = 1 WHERE id = " + id;
        try (Connection conn = Conexao.getConnection(); Statement st = conn.createStatement()) {
            int updated = st.executeUpdate(sql);
            System.out.println("Updated rows: " + updated);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }
}
