package controle.util;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.mindrot.jbcrypt.BCrypt;

import controle.Conexao;

/**
 * CLI utility: SetUserPassword
 * Usage:
 *   java controle.util.SetUserPassword --id=2 <NEW_PASSWORD>
 *   java controle.util.SetUserPassword --email=vitordebrito23@gmail.com <NEW_PASSWORD>
 * The provided password is hashed with BCrypt and stored in DB. No plaintext is logged.
 */
public class SetUserPassword {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: SetUserPassword (--id=<id>|--email=<email>) <new_password>");
            System.exit(2);
        }
        String idArg = args[0];
        String newPassword = args[1];
        boolean byEmail = false;
        int userId = -1;
        String email = null;
        if (idArg.startsWith("--id=")) {
            try { userId = Integer.parseInt(idArg.substring(5)); } catch (NumberFormatException nfe) { userId = -1; }
        } else if (idArg.startsWith("--email=")) {
            byEmail = true;
            email = idArg.substring(8);
        } else if (idArg.contains("@")) {
            byEmail = true;
            email = idArg;
        } else {
            try { userId = Integer.parseInt(idArg); } catch (NumberFormatException nfe) { userId = -1; }
        }
        if (!byEmail && userId <= 0) {
            System.err.println("Invalid identifier. Provide --id=N or --email=EMAIL or plain numeric id.");
            System.exit(2);
        }
        // Hash the password
        String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
        // Update database
        String sql = byEmail ? "UPDATE usuarios SET senha = ? WHERE email = ?" : "UPDATE usuarios SET senha = ? WHERE id = ?";
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashed);
            if (byEmail) ps.setString(2, email); else ps.setInt(2, userId);
            int updated = ps.executeUpdate();
            if (updated > 0) {
                System.out.println("Password updated (hash stored). Rows updated: " + updated);
            } else {
                System.err.println("No user matched the criteria. No rows updated.");
            }
        } catch (Exception e) {
            System.err.println("Error updating password: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }
}
