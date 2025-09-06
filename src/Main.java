package controle;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import controle.dao.UsuarioDAO;
import controle.dao.UsuarioDAOImpl;
import controle.model.Usuario;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        System.out.println("Teste de conexão e lista de usuários");
        UsuarioDAO dao = new UsuarioDAOImpl();
        try {
            List<Usuario> usuarios = dao.findAll();
            if (usuarios.isEmpty()) {
                System.out.println("Nenhum usuário cadastrado.");
            } else {
                usuarios.forEach(System.out::println);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao recuperar usuários", e);
        }
    }
}
