
import controle.Conexao;
import java.sql.Connection;

public class TestConnection {

    public static void main(String[] args) {
        try {
            System.out.println("Testando conexão com o banco...");
            Connection conn = Conexao.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("✓ Conexão estabelecida com sucesso!");
                System.out.println("  Database: " + conn.getCatalog());
                System.out.println("  URL: " + conn.getMetaData().getURL());
                conn.close();
            } else {
                System.out.println("✗ Falha na conexão");
            }
        } catch (Exception e) {
            System.out.println("✗ Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
