import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

// Imports para integração com API
import br.uespi.acessoapi.ClienteHTTP;
import br.uespi.tratajson.JSONObject;

/**
 * Tela de Login - Autenticação do usuário
 * Após login válido, abre a tela de pagamento PIX
 */
public class TelaLoginPIX extends JFrame {

    private JTextField txtUsuario;
    private JPasswordField txtSenha;
    private JButton btnEntrar;
    private JButton btnLimpar;
    private JLabel lblStatus;
    
    // URL da API de autenticação (endpoint correto sem o "2")
    private static final String API_URL = "http://www.datse.com.br/dev/syncjava.php";
    
    // Usuário e senha logados (serão passados para a próxima tela)
    private String usuarioLogado = null;
    private String senhaLogado = null;

    public TelaLoginPIX() {
        super("Login - Sistema de Pagamento PIX");
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Painel principal
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        mainPanel.setBackground(new Color(240, 240, 240));

        // Título
        JLabel lblTitulo = new JLabel("Sistema de Pagamento PIX");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblTitulo);
        
        mainPanel.add(Box.createVerticalStrut(10));
        
        JLabel lblSubtitulo = new JLabel("Faça login para continuar");
        lblSubtitulo.setFont(new Font("Arial", Font.PLAIN, 12));
        lblSubtitulo.setForeground(Color.GRAY);
        lblSubtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblSubtitulo);
        
        mainPanel.add(Box.createVerticalStrut(30));

        // Campo Usuário
        JPanel userPanel = new JPanel(new BorderLayout(5, 5));
        userPanel.setBackground(new Color(240, 240, 240));
        userPanel.setMaximumSize(new Dimension(250, 50));
        JLabel lblUsuario = new JLabel("Usuário:");
        txtUsuario = new JTextField(20);
        txtUsuario.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    txtSenha.requestFocus();
                }
            }
        });
        userPanel.add(lblUsuario, BorderLayout.NORTH);
        userPanel.add(txtUsuario, BorderLayout.CENTER);
        mainPanel.add(userPanel);
        
        mainPanel.add(Box.createVerticalStrut(15));

        // Campo Senha
        JPanel passPanel = new JPanel(new BorderLayout(5, 5));
        passPanel.setBackground(new Color(240, 240, 240));
        passPanel.setMaximumSize(new Dimension(250, 50));
        JLabel lblSenha = new JLabel("Senha:");
        txtSenha = new JPasswordField(20);
        txtSenha.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    realizarLogin();
                }
            }
        });
        passPanel.add(lblSenha, BorderLayout.NORTH);
        passPanel.add(txtSenha, BorderLayout.CENTER);
        mainPanel.add(passPanel);
        
        mainPanel.add(Box.createVerticalStrut(10));

        // Label de Status
        lblStatus = new JLabel(" ");
        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblStatus.setFont(new Font("Arial", Font.PLAIN, 11));
        mainPanel.add(lblStatus);
        
        mainPanel.add(Box.createVerticalStrut(20));

        // Painel de Botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(new Color(240, 240, 240));
        
        btnEntrar = new JButton("Entrar");
        btnEntrar.setPreferredSize(new Dimension(100, 30));
        btnEntrar.setBackground(new Color(0, 120, 215));
        btnEntrar.setForeground(Color.BLACK);
        btnEntrar.setFocusPainted(false);
        btnEntrar.addActionListener(e -> realizarLogin());
        buttonPanel.add(btnEntrar);
        
        btnLimpar = new JButton("Limpar");
        btnLimpar.setPreferredSize(new Dimension(100, 30));
        btnLimpar.addActionListener(e -> limparCampos());
        buttonPanel.add(btnLimpar);
        
        mainPanel.add(buttonPanel);

        add(mainPanel, BorderLayout.CENTER);

        // Configurações da janela
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(320, 350);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    /**
     * Realiza o login conectando à API
     */
    private void realizarLogin() {
        String usuario = txtUsuario.getText().trim();
        String senha = new String(txtSenha.getPassword());

        // Validação básica
        if (usuario.isEmpty() || senha.isEmpty()) {
            mostrarStatus("Preencha usuário e senha!", Color.RED);
            return;
        }

        // Desabilita botão durante o login
        btnEntrar.setEnabled(false);
        mostrarStatus("Conectando...", Color.BLUE);

        // Exibe no terminal
        System.out.println("\n============================================");
        System.out.println("   TENTATIVA DE LOGIN");
        System.out.println("============================================");
        System.out.println("[INFO] Usuário: " + usuario);
        System.out.println("[INFO] URL da API: " + API_URL);
        System.out.println("--------------------------------------------");

        // Executa login em thread separada para não travar a UI
        new Thread(() -> {
            try {
                System.out.println("[INFO] Conectando à API de autenticação...");
                
                ClienteHTTP conexao = new ClienteHTTP(usuario, senha, API_URL);
                String resposta = conexao.conecta();
                int codigoHTTP = conexao.codretorno;

                System.out.println("\n[OK] Resposta recebida!");
                System.out.println("[INFO] Código HTTP: " + codigoHTTP);
                System.out.println("[INFO] Resposta: " + resposta);

                // Verifica se o login foi bem-sucedido
                boolean loginValido = verificarLogin(resposta, codigoHTTP);

                // Atualiza UI na thread correta
                final String senhaFinal = senha;
                SwingUtilities.invokeLater(() -> {
                    btnEntrar.setEnabled(true);
                    
                    if (loginValido) {
                        usuarioLogado = usuario;
                        senhaLogado = senhaFinal;
                        mostrarStatus("Login realizado com sucesso!", new Color(0, 128, 0));
                        
                        System.out.println("\n[OK] LOGIN AUTORIZADO!");
                        System.out.println("[INFO] Abrindo tela de pagamento PIX...");
                        System.out.println("============================================\n");
                        
                        // Abre a tela de pagamento PIX
                        abrirTelaPagamento();
                    } else {
                        mostrarStatus("Usuário ou senha inválidos!", Color.RED);
                        txtSenha.setText("");
                        txtSenha.requestFocus();
                        
                        System.out.println("\n[ERRO] LOGIN NEGADO!");
                        System.out.println("[INFO] Verifique suas credenciais.");
                        System.out.println("============================================\n");
                    }
                });

            } catch (java.net.ConnectException e) {
                SwingUtilities.invokeLater(() -> {
                    btnEntrar.setEnabled(true);
                    mostrarStatus("Erro: Servidor não acessível", Color.RED);
                });
                System.err.println("[ERRO] Falha de conexão: " + e.getMessage());
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    btnEntrar.setEnabled(true);
                    mostrarStatus("Erro ao conectar: " + e.getMessage(), Color.RED);
                });
                System.err.println("[ERRO] " + e.getMessage());
            }
        }).start();
    }

    /**
     * Verifica se a resposta indica login válido
     */
    private boolean verificarLogin(String resposta, int codigoHTTP) {
        if (resposta == null || resposta.isEmpty()) {
            return false;
        }
        
        // Verifica mensagens de erro conhecidas
        String respostaLower = resposta.toLowerCase();
        if (respostaLower.contains("login invalido") || 
            respostaLower.contains("invalid") ||
            respostaLower.contains("erro") ||
            respostaLower.contains("negado") ||
            respostaLower.contains("unauthorized")) {
            return false;
        }
        
        // Tenta verificar via JSON
        try {
            JSONObject json = new JSONObject(resposta);
            
            // Verifica campos comuns de sucesso
            if (json.has("cod_retorno")) {
                String codRetorno = json.getString("cod_retorno");
                if ("0".equals(codRetorno) || "00".equals(codRetorno) || "success".equalsIgnoreCase(codRetorno)) {
                    return true;
                }
            }
            
            if (json.has("status")) {
                String status = json.getString("status");
                if ("ok".equalsIgnoreCase(status) || "success".equalsIgnoreCase(status)) {
                    return true;
                }
            }
            
            if (json.has("autorizado")) {
                return json.getBoolean("autorizado");
            }
            
            // Se tem ID, considera como sucesso
            if (json.has("id") && !json.getString("id").isEmpty()) {
                return true;
            }
            
        } catch (Exception e) {
            // Não é JSON, verifica texto
        }
        
        // Se chegou aqui e tem código 200, considera sucesso se não tem erro
        if (codigoHTTP == 200 && !respostaLower.contains("invalido")) {
            return true;
        }
        
        return false;
    }

    /**
     * Abre a tela de pagamento PIX
     */
    private void abrirTelaPagamento() {
        // Fecha a tela de login
        this.setVisible(false);
        
        // Abre a tela de pagamento passando o usuário e senha logados
        TelaPagamentoPIXAuth telaPagamento = new TelaPagamentoPIXAuth(usuarioLogado, senhaLogado, this);
        telaPagamento.setVisible(true);
    }

    /**
     * Mostra mensagem de status
     */
    private void mostrarStatus(String mensagem, Color cor) {
        lblStatus.setText(mensagem);
        lblStatus.setForeground(cor);
    }

    /**
     * Limpa os campos
     */
    private void limparCampos() {
        txtUsuario.setText("");
        txtSenha.setText("");
        lblStatus.setText(" ");
        txtUsuario.requestFocus();
    }

    /**
     * Método para reexibir a tela de login (chamado ao fazer logout)
     */
    public void mostrarLogin() {
        usuarioLogado = null;
        senhaLogado = null;
        limparCampos();
        this.setVisible(true);
    }

    public static void main(String[] args) {
        // Configura Look and Feel do sistema
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Usa Look and Feel padrão
        }

        // Executa na Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            TelaLoginPIX tela = new TelaLoginPIX();
            tela.setVisible(true);
            
            System.out.println("============================================");
            System.out.println("   SISTEMA DE PAGAMENTO PIX");
            System.out.println("============================================");
            System.out.println("[INFO] Tela de login iniciada");
            System.out.println("[INFO] API URL: " + API_URL);
            System.out.println("[INFO] Aguardando autenticação...");
            System.out.println("============================================\n");
        });
    }
}
