package controle;

import java.sql.Connection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import controle.dao.UsuarioDAO;
import controle.dao.UsuarioDAOImpl;
import controle.model.Usuario;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        // shutdown hook para encerrar corretamente threads internas do driver MySQL
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
            } catch (Throwable ex) {
                // ignorar
            }
        }));

        System.out.println("Teste de conexão e lista de usuários");
        // Testa a conexão primeiro
        try (Connection conn = Conexao.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Conexão com o banco: OK -> " + conn.getMetaData().getURL());
            } else {
                System.out.println("Conexão com o banco: falhou (conn nulo ou fechado)");
            }
        } catch (Exception ce) {
            LOGGER.log(Level.SEVERE, "Falha ao conectar no banco", ce);
            // se a conexão falhar, ainda tentamos executar o DAO para ver mensagens adicionais
        }

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

        // chamada explícita para encerrar a thread de limpeza do driver MySQL
        try {
            com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
        } catch (Throwable ex) {
            // ignorar erros de shutdown
        }
    }
}
