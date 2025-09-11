import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Tela04 extends JFrame {

    public Tela04() {
        super("Menu Principal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Arquivo");
        JMenuItem item1 = new JMenuItem("Novo");
        JMenuItem item2 = new JMenuItem("Abrir");
        JMenuItem item3 = new JMenuItem("Sair");
        menu.add(item1);
        menu.add(item2);
        menu.addSeparator();
        menu.add(item3);
        menuBar.add(menu);
        setJMenuBar(menuBar);

        // Content panel
        JPanel content = new JPanel();
        content.add(new JLabel("Dados:"));
        content.add(new JTextField(20));
        
        add(content, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Tela04 tela = new Tela04();
            tela.setVisible(true);
        });
    }
}