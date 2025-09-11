import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class Tela01 extends JFrame {

    private JTextField cpf;
    private JTextField nome;
    private JTextField cidade;
    private JButton entrar;

    public Tela01() {
        super("Login");
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        this.addComponentes();
    }

    public void addComponentes() {
        JLabel cpfLabel = new JLabel("CPF:");
        cpf = new JTextField();
        cpf.setMaximumSize(new Dimension(200, 25));

        JLabel nomeLabel = new JLabel("Nome:");
        nome = new JTextField();
        nome.setMaximumSize(new Dimension(200, 25));

        JLabel cidadeLabel = new JLabel("Cidade:");
        cidade = new JTextField();
        cidade.setMaximumSize(new Dimension(200, 25));

        entrar = new JButton("Entrar");

        add(cpfLabel);
        add(cpf);
        add(nomeLabel);
        add(nome);
        add(cidadeLabel);
        add(cidade);
        add(entrar);
    }

    public static void main(String[] args) {
        Tela01 tela = new Tela01();
        tela.setSize(300, 200);
        tela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        tela.setVisible(true);
    }
}