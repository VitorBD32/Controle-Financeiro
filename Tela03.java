import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class Tela03 extends JFrame {

    public Tela03() {
        super("Notas da Disciplina");
        setLayout(new BorderLayout());

        // Modelo não editável com 3 colunas
        DefaultTableModel model = new DefaultTableModel(new Object[]{"Aluno", "Nota", "Situação"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Dados (nomes e notas) — situação calculada
        String[] alunos = {"Aluno01", "Aluno02", "Aluno03"};
        double[] notas = {8.5, 5.5, 3.0}; // altere conforme necessário

        for (int i = 0; i < alunos.length; i++) {
            model.addRow(new Object[] {
                alunos[i],
                String.format("%.2f", notas[i]),
                getSituacao(notas[i])
            });
        }

        JTable table = new JTable(model);
        table.setPreferredScrollableViewportSize(new Dimension(360, 100));
        table.setFillsViewportHeight(true);

        // Centralizar conteúdo das colunas
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        add(new JScrollPane(table), BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
    }

    private String getSituacao(double nota) {
        if (nota >= 7.0) return "Aprovado";
        if (nota < 4.0) return "Reprovado";
        return "Prova Final";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Tela03 tela = new Tela03();
            tela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            tela.setVisible(true);
        });
    }
}