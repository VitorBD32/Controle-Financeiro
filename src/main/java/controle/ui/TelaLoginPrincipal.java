package controle.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

import controle.dao.UsuarioDAO;
import controle.dao.UsuarioDAOImpl;
import controle.model.Usuario;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Tela de Login Principal - Ponto de entrada do sistema
 * Design premium inspirado em aplicativos bancários
 * Identifica se o usuário é normal ou admin e redireciona para a tela apropriada
 */
public class TelaLoginPrincipal extends JFrame {

    private JTextField txtEmail;
    private JPasswordField txtSenha;
    private JButton btnEntrar;
    private JButton btnCadastrar;
    private JLabel lblStatus;
    private JCheckBox chkLembrar;
    private JProgressBar progressBar;
    
    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();

    // Cores do tema premium
    private static final Color PRIMARY_COLOR = new Color(0, 82, 155);      // Azul banco
    private static final Color SECONDARY_COLOR = new Color(0, 150, 136);   // Verde sucesso
    private static final Color BACKGROUND_COLOR = new Color(245, 247, 250); // Cinza claro
    private static final Color TEXT_COLOR = new Color(33, 37, 41);          // Quase preto
    private static final Color ACCENT_COLOR = new Color(255, 193, 7);       // Amarelo destaque

    public TelaLoginPrincipal() {
        super("Controle Financeiro - Login Seguro");
        initComponents();
        setupKeyBindings();
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        // Painel principal com gradiente
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_COLOR, 0, getHeight(), new Color(0, 51, 102));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Painel central branco arredondado
        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(40, 50, 40, 50)
        ));
        cardPanel.setMaximumSize(new Dimension(420, 550));
        cardPanel.setPreferredSize(new Dimension(420, 550));

        // Logo/Título
        JLabel lblLogo = new JLabel("💰", SwingConstants.CENTER);
        lblLogo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitulo = new JLabel("Controle Financeiro", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(PRIMARY_COLOR);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitulo = new JLabel("Sistema de Gestão Financeira Segura", SwingConstants.CENTER);
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitulo.setForeground(Color.GRAY);
        lblSubtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Campos de entrada
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setBackground(Color.WHITE);
        fieldsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Email
        JLabel lblEmail = new JLabel("📧 Email");
        lblEmail.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblEmail.setForeground(TEXT_COLOR);
        lblEmail.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtEmail = new JTextField(20);
        txtEmail.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtEmail.setMaximumSize(new Dimension(320, 40));
        txtEmail.setPreferredSize(new Dimension(320, 40));
        txtEmail.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(5, 10, 5, 10)
        ));
        txtEmail.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Senha
        JLabel lblSenha = new JLabel("🔒 Senha");
        lblSenha.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSenha.setForeground(TEXT_COLOR);
        lblSenha.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtSenha = new JPasswordField(20);
        txtSenha.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSenha.setMaximumSize(new Dimension(320, 40));
        txtSenha.setPreferredSize(new Dimension(320, 40));
        txtSenha.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(5, 10, 5, 10)
        ));
        txtSenha.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Checkbox lembrar
        chkLembrar = new JCheckBox("Lembrar-me neste dispositivo");
        chkLembrar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        chkLembrar.setBackground(Color.WHITE);
        chkLembrar.setForeground(Color.GRAY);
        chkLembrar.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Botão Entrar
        btnEntrar = new JButton("ENTRAR");
        btnEntrar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnEntrar.setForeground(Color.WHITE);
        btnEntrar.setBackground(PRIMARY_COLOR);
        btnEntrar.setMaximumSize(new Dimension(320, 45));
        btnEntrar.setPreferredSize(new Dimension(320, 45));
        btnEntrar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnEntrar.setBorderPainted(false);
        btnEntrar.setFocusPainted(false);
        btnEntrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEntrar.addActionListener(this::realizarLogin);
        
        // Efeito hover
        btnEntrar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnEntrar.setBackground(new Color(0, 60, 120));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btnEntrar.setBackground(PRIMARY_COLOR);
            }
        });

        // Botão Cadastrar
        btnCadastrar = new JButton("Criar nova conta");
        btnCadastrar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCadastrar.setForeground(PRIMARY_COLOR);
        btnCadastrar.setBackground(Color.WHITE);
        btnCadastrar.setMaximumSize(new Dimension(320, 35));
        btnCadastrar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCadastrar.setBorderPainted(false);
        btnCadastrar.setFocusPainted(false);
        btnCadastrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCadastrar.addActionListener(this::abrirCadastro);

        // Status
        lblStatus = new JLabel(" ", SwingConstants.CENTER);
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStatus.setForeground(Color.RED);
        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Progress bar
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setMaximumSize(new Dimension(320, 3));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Montagem do painel de campos
        fieldsPanel.add(Box.createVerticalStrut(20));
        fieldsPanel.add(lblEmail);
        fieldsPanel.add(Box.createVerticalStrut(5));
        fieldsPanel.add(txtEmail);
        fieldsPanel.add(Box.createVerticalStrut(15));
        fieldsPanel.add(lblSenha);
        fieldsPanel.add(Box.createVerticalStrut(5));
        fieldsPanel.add(txtSenha);
        fieldsPanel.add(Box.createVerticalStrut(10));
        fieldsPanel.add(chkLembrar);

        // Montagem do card
        cardPanel.add(lblLogo);
        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(lblTitulo);
        cardPanel.add(Box.createVerticalStrut(5));
        cardPanel.add(lblSubtitulo);
        cardPanel.add(Box.createVerticalStrut(30));
        cardPanel.add(fieldsPanel);
        cardPanel.add(Box.createVerticalStrut(20));
        cardPanel.add(progressBar);
        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(btnEntrar);
        cardPanel.add(Box.createVerticalStrut(15));
        cardPanel.add(lblStatus);
        cardPanel.add(Box.createVerticalStrut(20));
        cardPanel.add(btnCadastrar);

        // Painel wrapper para centralizar o card
        JPanel wrapperPanel = new JPanel(new GridBagLayout());
        wrapperPanel.setOpaque(false);
        wrapperPanel.add(cardPanel);

        mainPanel.add(wrapperPanel, BorderLayout.CENTER);

        // Rodapé
        JLabel lblRodape = new JLabel("© 2025 Controle Financeiro - Todos os direitos reservados", SwingConstants.CENTER);
        lblRodape.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblRodape.setForeground(new Color(200, 200, 200));
        lblRodape.setBorder(new EmptyBorder(10, 0, 10, 0));
        mainPanel.add(lblRodape, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        setSize(500, 700);
        setLocationRelativeTo(null);
    }

    private void setupKeyBindings() {
        // Enter para fazer login
        txtSenha.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    realizarLogin(null);
                }
            }
        });
        
        txtEmail.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    txtSenha.requestFocus();
                }
            }
        });
    }

    private void realizarLogin(ActionEvent e) {
        String email = txtEmail.getText().trim();
        String senha = new String(txtSenha.getPassword());

        // Validação básica
        if (email.isEmpty() || senha.isEmpty()) {
            lblStatus.setText("⚠️ Preencha email e senha");
            lblStatus.setForeground(new Color(255, 152, 0));
            return;
        }

        // Desabilita controles durante autenticação
        btnEntrar.setEnabled(false);
        progressBar.setVisible(true);
        lblStatus.setText("Autenticando...");
        lblStatus.setForeground(Color.GRAY);

        // Executa autenticação em background
        SwingWorker<Usuario, Void> worker = new SwingWorker<>() {
            @Override
            protected Usuario doInBackground() throws Exception {
                Thread.sleep(500); // Simula delay de autenticação
                
                // Busca usuário por email
                Usuario usuario = usuarioDAO.findByEmail(email);
                
                if (usuario == null) {
                    // Tenta buscar por nome também
                    usuario = usuarioDAO.findByNome(email);
                }
                
                if (usuario != null) {
                    String senhaArmazenada = usuario.getSenha();
                    boolean senhaCorreta = false;
                    
                    // Verifica se a senha é hash BCrypt ou plaintext
                    if (senhaArmazenada != null && senhaArmazenada.startsWith("$2")) {
                        // Senha hash BCrypt
                        senhaCorreta = BCrypt.checkpw(senha, senhaArmazenada);
                    } else {
                        // Senha plaintext (legado)
                        senhaCorreta = senha.equals(senhaArmazenada);
                    }
                    
                    if (senhaCorreta) {
                        // Verifica se usuário está autorizado
                        if (!usuario.isAutorizado()) {
                            return null; // Usuário desativado
                        }
                        return usuario;
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                progressBar.setVisible(false);
                btnEntrar.setEnabled(true);
                
                try {
                    Usuario usuario = get();
                    
                    if (usuario != null) {
                        lblStatus.setText("✅ Login realizado com sucesso!");
                        lblStatus.setForeground(SECONDARY_COLOR);
                        
                        System.out.println("\n" + "=".repeat(60));
                        System.out.println("🔐 LOGIN REALIZADO COM SUCESSO");
                        System.out.println("=".repeat(60));
                        System.out.println("👤 Usuário: " + usuario.getNome());
                        System.out.println("📧 Email: " + usuario.getEmail());
                        System.out.println("🔑 Admin: " + (usuario.isAdmin() ? "SIM" : "NÃO"));
                        System.out.println("=".repeat(60) + "\n");
                        
                        // Aguarda um momento e abre a tela apropriada
                        Timer timer = new Timer(800, evt -> {
                            abrirSistema(usuario);
                        });
                        timer.setRepeats(false);
                        timer.start();
                        
                    } else {
                        lblStatus.setText("❌ Email ou senha incorretos");
                        lblStatus.setForeground(Color.RED);
                        txtSenha.setText("");
                        txtSenha.requestFocus();
                    }
                } catch (Exception ex) {
                    lblStatus.setText("❌ Erro ao autenticar: " + ex.getMessage());
                    lblStatus.setForeground(Color.RED);
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void abrirSistema(Usuario usuario) {
        // Fecha a tela de login
        this.dispose();
        
        // Abre a tela principal de transações com o usuário logado
        SwingUtilities.invokeLater(() -> {
            TelaTransacao telaTransacao = new TelaTransacao(usuario);
            telaTransacao.setVisible(true);
            
            // Se for admin, mostra mensagem de boas-vindas especial
            if (usuario.isAdmin()) {
                JOptionPane.showMessageDialog(telaTransacao,
                        "Bem-vindo(a), " + usuario.getNome() + "!\n\n" +
                        "Você está logado como ADMINISTRADOR.\n" +
                        "Funcionalidades extras disponíveis:\n" +
                        "• Gráficos e relatórios\n" +
                        "• Configurações do sistema\n" +
                        "• Exportação de dados\n" +
                        "• Gestão de usuários",
                        "Acesso Administrativo",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    private void abrirCadastro(ActionEvent e) {
        // Abre dialog de cadastro de novo usuário
        JDialog dialog = new JDialog(this, "Criar Nova Conta", true);
        dialog.setLayout(new BorderLayout(10, 10));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtNome = new JTextField(25);
        JTextField txtEmailCad = new JTextField(25);
        JPasswordField txtSenhaCad = new JPasswordField(25);
        JPasswordField txtConfirmaSenha = new JPasswordField(25);

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Nome completo:"), gbc);
        gbc.gridy = 1;
        formPanel.add(txtNome, gbc);
        gbc.gridy = 2;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridy = 3;
        formPanel.add(txtEmailCad, gbc);
        gbc.gridy = 4;
        formPanel.add(new JLabel("Senha:"), gbc);
        gbc.gridy = 5;
        formPanel.add(txtSenhaCad, gbc);
        gbc.gridy = 6;
        formPanel.add(new JLabel("Confirmar senha:"), gbc);
        gbc.gridy = 7;
        formPanel.add(txtConfirmaSenha, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        JButton btnCriar = new JButton("Criar Conta");
        btnCriar.setBackground(PRIMARY_COLOR);
        btnCriar.setForeground(Color.WHITE);
        JButton btnCancelar = new JButton("Cancelar");

        btnCriar.addActionListener(evt -> {
            String nome = txtNome.getText().trim();
            String emailCad = txtEmailCad.getText().trim();
            String senhaCad = new String(txtSenhaCad.getPassword());
            String confirmaSenha = new String(txtConfirmaSenha.getPassword());

            if (nome.isEmpty() || emailCad.isEmpty() || senhaCad.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Preencha todos os campos!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!senhaCad.equals(confirmaSenha)) {
                JOptionPane.showMessageDialog(dialog, "As senhas não coincidem!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (senhaCad.length() < 4) {
                JOptionPane.showMessageDialog(dialog, "A senha deve ter pelo menos 4 caracteres!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                // Verifica se email já existe
                Usuario existente = usuarioDAO.findByEmail(emailCad);
                if (existente != null) {
                    JOptionPane.showMessageDialog(dialog, "Este email já está cadastrado!", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Cria novo usuário com senha hash
                Usuario novoUsuario = new Usuario();
                novoUsuario.setNome(nome);
                novoUsuario.setEmail(emailCad);
                novoUsuario.setSenha(BCrypt.hashpw(senhaCad, BCrypt.gensalt()));
                novoUsuario.setAutorizado(true);
                novoUsuario.setAdmin(false); // Novos usuários não são admin

                usuarioDAO.insert(novoUsuario);

                JOptionPane.showMessageDialog(dialog, 
                        "Conta criada com sucesso!\nVocê já pode fazer login.", 
                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                
                dialog.dispose();
                txtEmail.setText(emailCad);
                txtSenha.requestFocus();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, 
                        "Erro ao criar conta: " + ex.getMessage(), 
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancelar.addActionListener(evt -> dialog.dispose());

        buttonPanel.add(btnCancelar);
        buttonPanel.add(btnCriar);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // usa default
        }
        
        SwingUtilities.invokeLater(() -> {
            TelaLoginPrincipal login = new TelaLoginPrincipal();
            login.setVisible(true);
        });
    }
}
