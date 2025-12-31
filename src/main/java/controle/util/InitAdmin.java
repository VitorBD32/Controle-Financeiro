package controle.util;

import java.io.Console;

import controle.dao.UsuarioDAO;
import controle.dao.UsuarioDAOImpl;
import controle.model.Usuario;

/**
 * Utility to initialize an admin user in the database.
 * Usage:
 *   java controle.util.InitAdmin --user=admin --email=admin@example.com
 *   The tool will ask for a password interactively if not provided via TEST_PASSWORD env var.
 */
public class InitAdmin {
    public static void main(String[] args) {
        String user = null;
        String email = null;
        String name = null;
        String password = System.getenv("TEST_PASSWORD");

        for (String arg : args) {
            if (arg.startsWith("--user=")) user = arg.substring(7);
            else if (arg.startsWith("--email=")) email = arg.substring(8);
            else if (arg.startsWith("--name=")) name = arg.substring(7);
            else if (arg.startsWith("--password=")) password = arg.substring(11);
        }

        if (user == null || user.trim().isEmpty()) {
            System.err.println("Usage: InitAdmin --user=<username> --email=<email> [--name=<name>] [--password=<password>]\nOr set TEST_PASSWORD env var to provide password interactively.");
            System.exit(1);
        }

        if (password == null || password.isEmpty()) {
            // Try console prompt
            Console c = System.console();
            if (c != null) {
                char[] p1 = c.readPassword("Password for %s: ", user);
                if (p1 != null) password = new String(p1);
            }
        }
        if (password == null || password.isEmpty()) {
            System.err.println("No password provided or available in TEST_PASSWORD env var. Exiting.");
            System.exit(2);
        }

        try {
            UsuarioDAO dao = new UsuarioDAOImpl();
            Usuario u = new Usuario();
            u.setNome(name != null ? name : user);
            u.setEmail(email != null ? email : user + "@example.com");
            u.setSenha(password);
            u.setAutorizado(true);
            u.setAdmin(true);
            Usuario created = dao.insert(u);
            System.out.println("Admin user created with id: " + created.getId());
        } catch (Exception e) {
            System.err.println("Error creating admin user: " + e.getMessage());
            e.printStackTrace();
            System.exit(3);
        }
    }
}
