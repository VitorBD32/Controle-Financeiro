package controle.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import controle.Conexao;

/**
 * Check whether the user's stored senha looks like a BCrypt hash and does not match plaintext.
 */
public class CheckPasswordStatus {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: CheckPasswordStatus --id=N|--email=EMAIL");
            System.exit(2);
        }
        String identifier = args[0];
        boolean byEmail = false;
        int id = -1;
        String email = null;
        if (identifier.startsWith("--id=")) {
            try { id = Integer.parseInt(identifier.substring(5)); } catch (Exception e) { id = -1; }
        } else if (identifier.startsWith("--email=")) {
            byEmail = true;
            email = identifier.substring(8);
        } else if (identifier.contains("@")) {
            byEmail = true; email = identifier;
        } else {
            try { id = Integer.parseInt(identifier); } catch (Exception e) { id = -1; }
        }
        if (!byEmail && id <= 0) {
            System.err.println("Invalid identifier");
            System.exit(2);
        }
        String sql = byEmail ? "SELECT senha FROM usuarios WHERE email = ?" : "SELECT senha FROM usuarios WHERE id = ?";
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (byEmail) ps.setString(1, email); else ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String senha = rs.getString(1);
                    String defaultTestPassword = System.getenv("TEST_PASSWORD");
                    boolean isPlain = defaultTestPassword != null && senha != null && senha.equals(defaultTestPassword);
                    boolean looksHashed = senha != null && (senha.startsWith("$2a$") || senha.startsWith("$2b$") || senha.startsWith("$2y$"));
                    System.out.println("User identifier: " + (byEmail ? email : id));
                    System.out.println("Senha stored as hashed? " + (looksHashed ? "Yes" : "No"));
                    System.out.println("Plaintext equals given? " + (isPlain ? "Yes" : "No"));
                } else {
                    System.out.println("No user found.");
                }
            }
        } catch (Exception e) {
            System.err.println("Error checking password: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }
}
