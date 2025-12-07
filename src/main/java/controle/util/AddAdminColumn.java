package controle.util;

import java.sql.Connection;
import java.sql.Statement;

import controle.Conexao;

public class AddAdminColumn {

    public static void main(String[] args) {
        System.out.println("Adding 'admin' column to usuarios table if missing...");
        try (Connection conn = Conexao.getConnection(); Statement st = conn.createStatement()) {
            String sql = "ALTER TABLE usuarios ADD COLUMN admin TINYINT(1) DEFAULT 0";
            try {
                st.execute(sql);
                System.out.println("OK: Column 'admin' added.");
            } catch (java.sql.SQLException e) {
                String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
                if (msg.contains("duplicate column") || msg.contains("already exists") || msg.contains("já existe")) {
                    System.out.println("OK: Column 'admin' already exists.");
                } else {
                    System.err.println("Failed to add column: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to run AddAdminColumn: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }
}
