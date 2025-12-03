import controle.model.Usuario;
import controle.model.Categoria;
import controle.model.Transacao;
import java.time.LocalDateTime;
import java.math.BigDecimal;

public class DemoMain {
    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("  SISTEMA DE CONTROLE FINANCEIRO - DEMO");
        System.out.println("==============================================\n");

        // Demonstração de Usuários
        System.out.println(">>> USUÁRIOS CADASTRADOS <<<");
        System.out.println("-----------------------------");
        
        Usuario user1 = new Usuario();
        user1.setId(1);
        user1.setNome("João Silva");
        user1.setEmail("joao@email.com");
        user1.setSenha("senha123");
        
        Usuario user2 = new Usuario();
        user2.setId(2);
        user2.setNome("Maria Santos");
        user2.setEmail("maria@email.com");
        user2.setSenha("senha456");
        
        System.out.println(user1);
        System.out.println(user2);
        
        // Demonstração de Categorias
        System.out.println("\n>>> CATEGORIAS <<<");
        System.out.println("-------------------");
        
        Categoria cat1 = new Categoria();
        cat1.setId(1);
        cat1.setNome("Alimentação");
        cat1.setTipo("D");
        
        Categoria cat2 = new Categoria();
        cat2.setId(2);
        cat2.setNome("Transporte");
        cat2.setTipo("D");
        
        Categoria cat3 = new Categoria();
        cat3.setId(3);
        cat3.setNome("Salário");
        cat3.setTipo("R");
        
        System.out.println(cat1);
        System.out.println(cat2);
        System.out.println(cat3);
        
        // Demonstração de Transações
        System.out.println("\n>>> TRANSAÇÕES FINANCEIRAS <<<");
        System.out.println("-------------------------------");
        
        Transacao t1 = new Transacao();
        t1.setId(1);
        t1.setDescricao("Supermercado");
        t1.setValor(new BigDecimal("250.50"));
        t1.setTipo("D"); // Despesa
        t1.setIdCategoria(1);
        t1.setData(LocalDateTime.now().minusDays(5));
        t1.setIdUsuario(1);
        
        Transacao t2 = new Transacao();
        t2.setId(2);
        t2.setDescricao("Uber");
        t2.setValor(new BigDecimal("45.00"));
        t2.setTipo("D"); // Despesa
        t2.setIdCategoria(2);
        t2.setData(LocalDateTime.now().minusDays(3));
        t2.setIdUsuario(1);
        
        Transacao t3 = new Transacao();
        t3.setId(3);
        t3.setDescricao("Pagamento Mensal");
        t3.setValor(new BigDecimal("3500.00"));
        t3.setTipo("R"); // Receita
        t3.setIdCategoria(3);
        t3.setData(LocalDateTime.now().minusDays(1));
        t3.setIdUsuario(1);
        
        System.out.println(t1);
        System.out.println(t2);
        System.out.println(t3);
        
        // Cálculo de Saldo
        System.out.println("\n>>> RESUMO FINANCEIRO <<<");
        System.out.println("--------------------------");
        
        BigDecimal totalReceitas = new BigDecimal("3500.00");
        BigDecimal totalDespesas = new BigDecimal("295.50");
        BigDecimal saldo = totalReceitas.subtract(totalDespesas);
        
        System.out.println("Total de Receitas: R$ " + totalReceitas);
        System.out.println("Total de Despesas: R$ " + totalDespesas);
        System.out.println("Saldo Atual:       R$ " + saldo);
        
        System.out.println("\n==============================================");
        System.out.println("  FUNCIONALIDADES DISPONÍVEIS:");
        System.out.println("  - Cadastro de Usuários");
        System.out.println("  - Gerenciamento de Categorias");
        System.out.println("  - Registro de Transações (Receitas/Despesas)");
        System.out.println("  - Relatórios Financeiros");
        System.out.println("  - Integração com Banco de Dados MySQL");
        System.out.println("  - Interface Gráfica com Swing");
        System.out.println("==============================================\n");
        
        System.out.println("NOTA: Para usar todas as funcionalidades,");
        System.out.println("      configure o banco de dados MySQL conforme");
        System.out.println("      o arquivo: avaliacao1/schema.sql");
    }
}
