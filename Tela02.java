import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class Tela02 extends JFrame {

    private JTextField cpf;
    private JTextField nome;
    private JTextField cidade;
    private JButton enviar, limpa;
    private JLabel cpfLabel, nomeLabel, cidadeLabel;

    public Tela02() {
        super("Cadastrar");
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        // padding around content
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(12, 12, 12, 12));
        this.addComponentes();
    }

    public void addComponentes() {
        // CPF
        cpfLabel = new JLabel("CPF:");
        cpfLabel.setAlignmentX(CENTER_ALIGNMENT);
        cpfLabel.setFont(cpfLabel.getFont().deriveFont(Font.BOLD));
        add(cpfLabel);
        add(Box.createVerticalStrut(6));

        cpf = new JTextField();
        cpf.setMaximumSize(new Dimension(320, 26));
        cpf.setAlignmentX(CENTER_ALIGNMENT);
        add(cpf);
        add(Box.createVerticalStrut(12));

        // Nome
        nomeLabel = new JLabel("Nome:");
        nomeLabel.setAlignmentX(CENTER_ALIGNMENT);
        nomeLabel.setFont(nomeLabel.getFont().deriveFont(Font.BOLD));
        add(nomeLabel);
        add(Box.createVerticalStrut(6));

        nome = new JTextField();
        nome.setMaximumSize(new Dimension(320, 26));
        nome.setAlignmentX(CENTER_ALIGNMENT);
        add(nome);
        add(Box.createVerticalStrut(12));

        // Cidade
        cidadeLabel = new JLabel("Cidade:");
        cidadeLabel.setAlignmentX(CENTER_ALIGNMENT);
        cidadeLabel.setFont(cidadeLabel.getFont().deriveFont(Font.BOLD));
        add(cidadeLabel);
        add(Box.createVerticalStrut(6));

        cidade = new JTextField();
        cidade.setMaximumSize(new Dimension(320, 26));
        cidade.setAlignmentX(CENTER_ALIGNMENT);
        add(cidade);
        add(Box.createVerticalStrut(14));

        // Buttons centered in a panel
        enviar = new JButton("Cadastrar");
        limpa = new JButton("Limpar");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttonPanel.setAlignmentX(CENTER_ALIGNMENT);
        buttonPanel.add(enviar);
        buttonPanel.add(limpa);
        add(buttonPanel);

        // Ações dos botões:
        enviar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String sCpf = cpf.getText().trim();
                String sNome = nome.getText().trim();
                String sCidade = cidade.getText().trim();

                // Se todos vazios -> "Não foi adicionado nada"
                if (sCpf.isEmpty() && sNome.isEmpty() && sCidade.isEmpty()) {
                    JOptionPane.showMessageDialog(Tela02.this,
                            "Não foi adicionado nada.",
                            "Aviso",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Se algum campo vazio -> erro pedindo preenchimento
                if (sCpf.isEmpty() || sNome.isEmpty() || sCidade.isEmpty()) {
                    JOptionPane.showMessageDialog(Tela02.this,
                            "Por favor, preencha todos os campos.",
                            "Erro",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    // todos preenchidos -> sucesso
                    JOptionPane.showMessageDialog(Tela02.this,
                            "Cadastro realizado com sucesso!",
                            "Sucesso",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        limpa.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String sCpf = cpf.getText().trim();
                String sNome = nome.getText().trim();
                String sCidade = cidade.getText().trim();

                // Se todos vazios -> mostrar mensagem que não há o que limpar
                if (sCpf.isEmpty() && sNome.isEmpty() && sCidade.isEmpty()) {
                    JOptionPane.showMessageDialog(Tela02.this,
                            "Não pode limpar nada, pois campos estão vazios.",
                            "Aviso",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Caso contrário, limpa e informa sucesso
                cpf.setText("");
                nome.setText("");
                cidade.setText("");
                JOptionPane.showMessageDialog(Tela02.this,
                        "Limpeza realizada com sucesso.",
                        "Limpar",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    public static void main(String[] args) {
        Tela02 tela = new Tela02();
        tela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        tela.setSize(360, 300);
        tela.setLocationRelativeTo(null);
        tela.setVisible(true);
    }
}