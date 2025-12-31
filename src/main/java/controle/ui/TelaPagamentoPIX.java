package controle.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import controle.dao.CartaoDAO;
import controle.dao.CartaoDAOImpl;
import controle.dao.CategoriaDAO;
import controle.dao.CategoriaDAOImpl;
import controle.dao.TransacaoDAO;
import controle.dao.TransacaoDAOImpl;
import controle.dao.UsuarioDAO;
import controle.dao.UsuarioDAOImpl;
import controle.model.Cartao;
import controle.model.Categoria;
import controle.model.Transacao;
import controle.model.Usuario;
import controle.security.SecurityManager;
import controle.util.QRCodePIXGenerator;

/**
 * Tela de Pagamento PIX integrada com o sistema de Controle Financeiro
 * Conectada com Usuários, Categorias e Transações
 * Suporte a Cartões de Crédito/Débito com dados criptografados
 */
public class TelaPagamentoPIX extends JFrame {

    // Campos de entrada
    private JComboBox<Usuario> cmbUsuario;
    private JComboBox<Categoria> cmbCategoria;
    private JTextField txtValor;
    private JTextField txtDescricao;
    private JComboBox<String> cmbTipoPagamento;
    
    // Painel de seleção de cartão (para Crédito/Débito)
    private JPanel panelCartao;
    private JComboBox<Cartao> cmbCartao;
    private JButton btnNovoCartao;
    private JLabel lblCartaoInfo;
    
    // Área de resultado e QR Code
    private JTextArea txtResultado;
    private JLabel lblQRCode;
    private JPanel panelQRCode;
    
    // Botões
    private JButton btnEmitir;
    private JButton btnSalvarTransacao;
    private JButton btnLimpar;
    
    // DAOs
    private UsuarioDAO usuarioDAO;
    private CategoriaDAO categoriaDAO;
    private TransacaoDAO transacaoDAO;
    private CartaoDAO cartaoDAO;
    
    // Usuário logado atual
    private Usuario usuarioLogado;
    // Security helper
    private final SecurityManager securityManager = SecurityManager.getInstance();
    
    // URL da API
    private static final String API_URL = "http://www.datse.com.br/dev/syncjava.php";
    private static final String QR_CODE_PATH = "resources/images/qrcode_pix.png";

    public TelaPagamentoPIX() {
        this(null);
    }
    
    public TelaPagamentoPIX(Usuario usuarioLogado) {
        super("💳 Pagamento - Controle Financeiro Premium");
        this.usuarioLogado = usuarioLogado;
        initDAOs();
        initComponents();
        carregarDados();
    }

    private void initDAOs() {
        try {
            usuarioDAO = new UsuarioDAOImpl();
            categoriaDAO = new CategoriaDAOImpl();
            transacaoDAO = new TransacaoDAOImpl();
            cartaoDAO = new CartaoDAOImpl();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao conectar com banco de dados:\n" + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Painel principal
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // === PAINEL DE SELEÇÃO ===
        JPanel selecaoPanel = new JPanel(new GridBagLayout());
        selecaoPanel.setBorder(BorderFactory.createTitledBorder("📋 Dados do Pagamento"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Usuário
        gbc.gridx = 0; gbc.gridy = 0;
        selecaoPanel.add(new JLabel("👤 Usuário:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        cmbUsuario = new JComboBox<>();
        cmbUsuario.setPreferredSize(new Dimension(250, 25));
        selecaoPanel.add(cmbUsuario, gbc);

        // Categoria
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        selecaoPanel.add(new JLabel("📁 Categoria:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        cmbCategoria = new JComboBox<>();
        selecaoPanel.add(cmbCategoria, gbc);

        // Tipo de Pagamento
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        selecaoPanel.add(new JLabel("💳 Tipo:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        cmbTipoPagamento = new JComboBox<>(new String[]{"PIX", "Crédito", "Débito", "Boleto"});
        cmbTipoPagamento.addActionListener(e -> atualizarTipoPagamento());
        selecaoPanel.add(cmbTipoPagamento, gbc);

        // === PAINEL DE CARTÃO (visível apenas para Crédito/Débito) ===
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panelCartao = new JPanel(new GridBagLayout());
        panelCartao.setBorder(BorderFactory.createTitledBorder("🔒 Cartão Seguro"));
        panelCartao.setBackground(new Color(240, 248, 255));
        GridBagConstraints gbcCard = new GridBagConstraints();
        gbcCard.insets = new Insets(3, 5, 3, 5);
        
        // Combo de cartões
        gbcCard.gridx = 0; gbcCard.gridy = 0;
        panelCartao.add(new JLabel("Cartão:"), gbcCard);
        gbcCard.gridx = 1; gbcCard.fill = GridBagConstraints.HORIZONTAL; gbcCard.weightx = 1;
        cmbCartao = new JComboBox<>();
        cmbCartao.setRenderer(new CartaoComboRenderer());
        cmbCartao.addActionListener(e -> atualizarInfoCartao());
        panelCartao.add(cmbCartao, gbcCard);
        
        // Botão novo cartão
        gbcCard.gridx = 2; gbcCard.fill = GridBagConstraints.NONE; gbcCard.weightx = 0;
        btnNovoCartao = new JButton("➕ Novo");
        btnNovoCartao.setToolTipText("Cadastrar novo cartão");
        btnNovoCartao.addActionListener(e -> abrirCadastroCartao());
        panelCartao.add(btnNovoCartao, gbcCard);
        
        // Info do cartão selecionado
        gbcCard.gridx = 0; gbcCard.gridy = 1; gbcCard.gridwidth = 3;
        gbcCard.fill = GridBagConstraints.HORIZONTAL;
        lblCartaoInfo = new JLabel(" ");
        lblCartaoInfo.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lblCartaoInfo.setForeground(new Color(0, 100, 0));
        panelCartao.add(lblCartaoInfo, gbcCard);
        
        panelCartao.setVisible(false); // Começa oculto
        selecaoPanel.add(panelCartao, gbc);
        gbc.gridwidth = 1;

        // Valor
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        selecaoPanel.add(new JLabel("💰 Valor (R$):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        txtValor = new JTextField(15);
        // Atualiza QR Code quando valor mudar
        txtValor.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent e) {
                if ("PIX".equals(cmbTipoPagamento.getSelectedItem())) {
                    gerarQRCodeDinamico();
                }
            }
        });
        selecaoPanel.add(txtValor, gbc);

        // Descrição
        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        selecaoPanel.add(new JLabel("📝 Descrição:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        txtDescricao = new JTextField(25);
        // Atualiza QR Code quando descrição mudar
        txtDescricao.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent e) {
                if ("PIX".equals(cmbTipoPagamento.getSelectedItem())) {
                    gerarQRCodeDinamico();
                }
            }
        });
        selecaoPanel.add(txtDescricao, gbc);
        
        // Botão para regenerar QR Code
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        JButton btnGerarQR = new JButton("🔄 Atualizar QR Code");
        btnGerarQR.addActionListener(e -> gerarQRCodeDinamico());
        selecaoPanel.add(btnGerarQR, gbc);
        gbc.gridwidth = 1;

        mainPanel.add(selecaoPanel);
        mainPanel.add(Box.createVerticalStrut(10));

        // === PAINEL DO QR CODE PIX ===
        panelQRCode = new JPanel(new BorderLayout(5, 5));
        panelQRCode.setBorder(BorderFactory.createTitledBorder("📱 QR Code PIX - Escaneie para Pagar"));
        panelQRCode.setBackground(Color.WHITE);
        panelQRCode.setPreferredSize(new Dimension(300, 250));
        
        lblQRCode = new JLabel("", SwingConstants.CENTER);
        lblQRCode.setPreferredSize(new Dimension(200, 200));
        panelQRCode.add(lblQRCode, BorderLayout.CENTER);
        
        JLabel lblInstrucao = new JLabel("<html><center>Abra o app do seu banco<br>e escaneie o QR Code acima</center></html>", SwingConstants.CENTER);
        lblInstrucao.setForeground(new Color(0, 100, 0));
        lblInstrucao.setFont(new Font("SansSerif", Font.ITALIC, 11));
        panelQRCode.add(lblInstrucao, BorderLayout.SOUTH);
        
        mainPanel.add(panelQRCode);
        mainPanel.add(Box.createVerticalStrut(10));

        // === PAINEL DE BOTÕES ===
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        
        btnEmitir = new JButton("✅ Processar Pagamento");
        btnEmitir.setBackground(new Color(40, 167, 69));
        btnEmitir.setForeground(Color.WHITE);
        btnEmitir.addActionListener(this::processarPagamento);
        buttonPanel.add(btnEmitir);
        
        btnSalvarTransacao = new JButton("💾 Salvar como Transação");
        btnSalvarTransacao.addActionListener(this::salvarComoTransacao);
        buttonPanel.add(btnSalvarTransacao);
        
        btnLimpar = new JButton("🗑️ Limpar");
        btnLimpar.addActionListener(e -> limparCampos());
        buttonPanel.add(btnLimpar);
        
        mainPanel.add(buttonPanel);
        mainPanel.add(Box.createVerticalStrut(10));

        // === ÁREA DE RESULTADO ===
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder("📊 Resultado do Processamento"));
        txtResultado = new JTextArea(6, 40);
        txtResultado.setEditable(false);
        txtResultado.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultPanel.add(new JScrollPane(txtResultado), BorderLayout.CENTER);
        
        mainPanel.add(resultPanel);

        add(mainPanel, BorderLayout.CENTER);

        // Configurações da janela
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(450, 650);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Carrega QR Code inicialmente
        atualizarQRCode();
    }

    private void carregarDados() {
        try {
            // Carrega usuários
            List<Usuario> usuarios = usuarioDAO.findAll();
            // Ordena usuários por nome para melhor usabilidade (O(n log n))
            usuarios.sort((a, b) -> {
                String na = a.getNome() == null ? "" : a.getNome();
                String nb = b.getNome() == null ? "" : b.getNome();
                return na.compareToIgnoreCase(nb);
            });
            cmbUsuario.removeAllItems();
            for (Usuario u : usuarios) {
                cmbUsuario.addItem(u);
                // Log minimal: id + masked email (não expor dados sensíveis)
                String masked = u.getEmail() != null && !u.getEmail().isEmpty() ?
                        securityManager.maskSensitiveData(u.getEmail(), "EMAIL") : securityManager.maskSensitiveData(u.getNome(), "GENERIC");
                System.out.println("[USERS] ID:" + u.getId() + " - " + masked + " - autorizado=" + u.isAutorizado());
            }
            
            // Carrega categorias (apenas tipo D - Despesa para pagamentos)
            List<Categoria> categorias = categorySort(categoriaDAO.findAll());
            cmbCategoria.removeAllItems();
            for (Categoria c : categorias) {
                cmbCategoria.addItem(c);
            }
            
            System.out.println("✅ Dados carregados: " + usuarios.size() + " usuários, " + categorias.size() + " categorias");
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao carregar dados: " + e.getMessage());
            txtResultado.setText("Erro ao carregar dados do banco:\n" + e.getMessage());
        }
    }

    // Ordena categorias por nome (O(n log n))
    private List<Categoria> categorySort(List<Categoria> list) {
        if (list == null) return java.util.Collections.emptyList();
        list.sort((a, b) -> {
            String na = a.getNome() == null ? "" : a.getNome();
            String nb = b.getNome() == null ? "" : b.getNome();
            return na.compareToIgnoreCase(nb);
        });
        return list;
    }
    private void appendLog(String msg) {
        String time = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        if (txtResultado != null) {
            txtResultado.append("[" + time + "] " + msg + "\n");
            txtResultado.setCaretPosition(txtResultado.getDocument().getLength());
        }
        System.out.println(msg);
    }

    // Compatibilidade com Java 8: repete um caractere N vezes
    private static String repeatChar(char ch, int count) {
        char[] arr = new char[count];
        Arrays.fill(arr, ch);
        return new String(arr);
    }
    

    private void atualizarQRCode() {
        String tipo = (String) cmbTipoPagamento.getSelectedItem();
        boolean isPix = "PIX".equals(tipo);
        
        panelQRCode.setVisible(isPix);
        
        if (isPix) {
            gerarQRCodeDinamico();
        }
        
        pack();
        setSize(450, isPix ? 650 : 450);
    }

    /**
     * Gera QR Code PIX dinamicamente usando ZXing
     */
    private void gerarQRCodeDinamico() {
        try {
            // Pega o valor digitado (ou usa 0 para QR sem valor fixo)
            BigDecimal valor = BigDecimal.ZERO;
            String valorStr = txtValor.getText().trim();
            if (!valorStr.isEmpty()) {
                try {
                    valor = new BigDecimal(valorStr.replace(",", "."));
                } catch (NumberFormatException e) {
                    // Ignora valor inválido
                }
            }
            
            // Pega descrição
            String descricao = txtDescricao.getText().trim();
            if (descricao.length() > 25) {
                descricao = descricao.substring(0, 25);
            }
            
            // Gera QR Code usando ZXing
            BufferedImage qrImage = QRCodePIXGenerator.gerarQRCodePIX(valor, descricao);
            
            if (qrImage != null) {
                Image scaledImage = qrImage.getScaledInstance(180, 180, Image.SCALE_SMOOTH);
                lblQRCode.setIcon(new ImageIcon(scaledImage));
                lblQRCode.setText("");
                System.out.println("✅ QR Code PIX gerado dinamicamente!");
            } else {
                lblQRCode.setIcon(null);
                lblQRCode.setText("<html><center>Erro ao gerar QR Code</center></html>");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao gerar QR Code: " + e.getMessage());
            lblQRCode.setIcon(null);
            lblQRCode.setText("<html><center>Erro: " + e.getMessage() + "</center></html>");
        }
    }

    private void carregarImagemQRCode() {
        // Usa geração dinâmica em vez de arquivo estático
        gerarQRCodeDinamico();
    }

    private void processarPagamento(ActionEvent e) {
        Usuario usuario = (Usuario) cmbUsuario.getSelectedItem();
        Categoria categoria = (Categoria) cmbCategoria.getSelectedItem();
        String valorStr = txtValor.getText().trim();
        String descricao = txtDescricao.getText().trim();
        String tipoPagamento = (String) cmbTipoPagamento.getSelectedItem();

        // Validação
        if (usuario == null || categoria == null || valorStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Preencha todos os campos obrigatórios:\nUsuário, Categoria e Valor",
                "Campos Obrigatórios", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal valor;
        try {
            valor = new BigDecimal(valorStr.replace(",", "."));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Valor inválido!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Log no terminal
        String sepEq = repeatChar('=', 60);
        String sepDash = repeatChar('-', 60);
        System.out.println("\n" + sepEq);
        System.out.println("💳 PROCESSANDO PAGAMENTO " + tipoPagamento);
        System.out.println(sepEq);
        System.out.println("👤 Usuário: " + usuario.getNome() + " (ID: " + usuario.getId() + ")");
        System.out.println("📁 Categoria: " + categoria.getNome());
        System.out.println("💰 Valor: R$ " + valor);
        System.out.println("📝 Descrição: " + descricao);
        System.out.println(sepDash);
        System.out.println("\n" + "=".repeat(60));
        System.out.println("💳 PROCESSANDO PAGAMENTO " + tipoPagamento);
        System.out.println("=".repeat(60));
        System.out.println("👤 Usuário: " + usuario.getNome() + " (ID: " + usuario.getId() + ")");
        System.out.println("📁 Categoria: " + categoria.getNome());
        System.out.println("💰 Valor: R$ " + valor);
        System.out.println("📝 Descrição: " + descricao);
        System.out.println("-".repeat(60));

        StringBuilder resultado = new StringBuilder();
        resultado.append("=== PAGAMENTO ").append(tipoPagamento).append(" ===\n\n");
        resultado.append("👤 Usuário ID: ").append(usuario.getId()).append("\n");

        // Desabilita botões antes da operação em background
        btnEmitir.setEnabled(false);
        btnSalvarTransacao.setEnabled(false);
        btnLimpar.setEnabled(false);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private String resposta = null;
            private int codigoHTTP = -1;
            private Exception erro = null;

            @Override
            protected Void doInBackground() {
                try {
                    String cpf = usuario.getEmail() != null ? usuario.getEmail() : String.valueOf(usuario.getId());
                    String ncartao = ""; // not used for PIX
                    pagamentoHTTP pagamento = new pagamentoHTTP(
                            usuario.getNome(),
                            cpf,
                            ncartao,
                            valor.toString(),
                            tipoPagamento,
                            API_URL
                    );
                    resposta = pagamento.conecta();
                    codigoHTTP = pagamento.codretorno;
                } catch (Exception ex) {
                    erro = ex;
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    if (erro != null) {
                        appendLog("❌ Erro no processamento do pagamento: " + erro.getMessage());
                        JOptionPane.showMessageDialog(TelaPagamentoPIX.this, "Erro ao processar pagamento: " + erro.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    appendLog("✅ Pagamento processado! Código HTTP: " + codigoHTTP);
                    if (resposta != null && !resposta.isEmpty()) {
                        String frag = resposta.length() > 200 ? resposta.substring(0, 200) + "..." : resposta;
                        System.out.println("[API RESPONSE FRAGMENT] " + frag);
                    }

                        try {
                            // Sanitiza descrição antes de salvar para evitar injeções
                            String safeDescricao = descricao;
                            try {
                                safeDescricao = securityManager.validateAndSanitize(descricao, "descricao");
                            } catch (Exception se) {
                                appendLog("⚠️ Descrição contém caracteres inválidos, será filtrada.");
                                safeDescricao = securityManager.sanitizeSqlInput(descricao);
                            }

                            Transacao t = new Transacao();
                            t.setIdUsuario(usuario.getId());
                            t.setIdCategoria(categoria.getId());
                            t.setTipo("D");
                            t.setValor(valor);
                            t.setData(LocalDateTime.now());
                            t.setDescricao(safeDescricao == null || safeDescricao.isEmpty() ? "Pagamento " + tipoPagamento : safeDescricao);
                            transacaoDAO.insert(t);
                        appendLog("✅ Transação salva com sucesso (ID: " + t.getId() + ")");
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(TelaPagamentoPIX.this, "Pagamento e transação salvos com sucesso (ID: " + t.getId() + ")", "Sucesso", JOptionPane.INFORMATION_MESSAGE));
                    } catch (Exception se) {
                        appendLog("⚠️ Pagamento processado, mas falha ao salvar transação: " + se.getMessage());
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(TelaPagamentoPIX.this, "Pagamento processado, mas falha ao salvar transação: " + se.getMessage(), "Aviso", JOptionPane.WARNING_MESSAGE));
                    }
                } finally {
                    btnEmitir.setEnabled(true);
                    btnSalvarTransacao.setEnabled(true);
                    btnLimpar.setEnabled(true);
                }
            }
        };
        worker.execute();

        txtResultado.setText(resultado.toString());
    }

    private void salvarComoTransacao(ActionEvent e) {
        Usuario usuario = (Usuario) cmbUsuario.getSelectedItem();
        Categoria categoria = (Categoria) cmbCategoria.getSelectedItem();
        String valorStr = txtValor.getText().trim();
        String descricao = txtDescricao.getText().trim();
        String tipoPagamento = (String) cmbTipoPagamento.getSelectedItem();

        if (usuario == null || categoria == null || valorStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Preencha todos os campos!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            BigDecimal valor = new BigDecimal(valorStr.replace(",", "."));
            
            Transacao t = new Transacao();
            t.setIdUsuario(usuario.getId());
            t.setIdCategoria(categoria.getId());
            t.setTipo("D"); // Despesa (pagamento)
            t.setValor(valor);
            t.setData(LocalDateTime.now());
            // Sanitiza descrição
            String safeDescricao = descricao;
            try {
                safeDescricao = securityManager.validateAndSanitize(descricao, "descricao");
            } catch (Exception se) {
                safeDescricao = securityManager.sanitizeSqlInput(descricao);
            }
            t.setDescricao(safeDescricao.isEmpty() ? "Pagamento " + tipoPagamento : safeDescricao);
            
            transacaoDAO.insert(t);
            
            JOptionPane.showMessageDialog(this, 
                "✅ Transação salva com sucesso!\nID: " + t.getId(),
                "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            
            System.out.println("✅ Transação salva - ID: " + t.getId());
            
            txtResultado.append("\n✅ Transação salva no banco (ID: " + t.getId() + ")");
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao salvar transação:\n" + ex.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limparCampos() {
        txtValor.setText("");
        txtDescricao.setText("");
        txtResultado.setText("");
        cmbTipoPagamento.setSelectedIndex(0);
        if (cmbCartao != null && cmbCartao.getItemCount() > 0) {
            cmbCartao.setSelectedIndex(0);
        }
    }
    
    /**
     * Atualiza a interface baseado no tipo de pagamento selecionado
     */
    private void atualizarTipoPagamento() {
        String tipo = (String) cmbTipoPagamento.getSelectedItem();
        boolean isPix = "PIX".equals(tipo);
        boolean isCartao = "Crédito".equals(tipo) || "Débito".equals(tipo);
        
        // Mostra/oculta QR Code PIX
        panelQRCode.setVisible(isPix);
        
        // Mostra/oculta painel de cartão
        panelCartao.setVisible(isCartao);
        
        if (isPix) {
            gerarQRCodeDinamico();
        }
        
        if (isCartao) {
            carregarCartoes();
        }
        
        pack();
        int altura = isPix ? 700 : (isCartao ? 550 : 450);
        setSize(480, altura);
    }
    
    /**
     * Carrega os cartões do usuário selecionado
     */
    private void carregarCartoes() {
        cmbCartao.removeAllItems();
        Usuario usuario = (Usuario) cmbUsuario.getSelectedItem();
        
        if (usuario == null && usuarioLogado != null) {
            usuario = usuarioLogado;
        }
        
        if (usuario == null) {
            lblCartaoInfo.setText("⚠️ Selecione um usuário primeiro");
            return;
        }
        
        try {
            String tipo = (String) cmbTipoPagamento.getSelectedItem();
            String tipoCartao = "Crédito".equals(tipo) ? "CREDITO" : "DEBITO";
            
            List<Cartao> cartoes = cartaoDAO.findByUsuario(usuario.getId());
            // Ordena por último uso desc (cartões mais recentemente usados primeiro) - O(n log n)
            cartoes.sort((a, b) -> {
                java.time.LocalDateTime ua = a.getUltimoUso();
                java.time.LocalDateTime ub = b.getUltimoUso();
                if (ua == null && ub == null) return 0;
                if (ua == null) return 1;
                if (ub == null) return -1;
                return ub.compareTo(ua);
            });
            int count = 0;
            for (Cartao c : cartoes) {
                if (c.isAtivo() && c.getTipo() != null && c.getTipo().equalsIgnoreCase(tipoCartao)) {
                    cmbCartao.addItem(c);
                    count++;
                }
            }
            
            if (count == 0) {
                lblCartaoInfo.setText("⚠️ Nenhum cartão de " + tipo.toLowerCase() + " cadastrado");
            } else {
                lblCartaoInfo.setText("✅ " + count + " cartão(ões) disponível(is)");
            }
            
            System.out.println("✅ Carregados " + count + " cartões de " + tipo + " para usuário ID:" + usuario.getId() + " - " + securityManager.maskSensitiveData(usuario.getNome(), "GENERIC"));
            
        } catch (Exception e) {
            lblCartaoInfo.setText("❌ Erro ao carregar cartões");
            System.err.println("❌ Erro ao carregar cartões: " + e.getMessage());
        }
    }
    
    /**
     * Atualiza info do cartão selecionado
     */
    private void atualizarInfoCartao() {
        Cartao cartao = (Cartao) cmbCartao.getSelectedItem();
        if (cartao != null) {
            String mascarado = cartao.getNumeroMascarado();
            lblCartaoInfo.setText("🔒 " + cartao.getBandeira() + " - " + mascarado + " (Val: " + cartao.getValidade() + ")");
        }
    }
    
    /**
     * Abre tela de cadastro de novo cartão
     */
    private void abrirCadastroCartao() {
        Usuario usuario = (Usuario) cmbUsuario.getSelectedItem();
        if (usuario == null && usuarioLogado != null) {
            usuario = usuarioLogado;
        }
        
        if (usuario == null) {
            JOptionPane.showMessageDialog(this, 
                "Selecione um usuário primeiro!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String tipo = (String) cmbTipoPagamento.getSelectedItem();
        String tipoCartao = "Crédito".equals(tipo) ? "CREDITO" : "DEBITO";
        
        TelaCadastroCartao tela = new TelaCadastroCartao(this, usuario, tipoCartao);
        tela.setVisible(true);
        
        // Recarrega cartões após fechar o cadastro
        carregarCartoes();
    }
    
    /**
     * Renderer customizado para exibir cartões no combobox
     */
    private class CartaoComboRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, 
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof Cartao) {
                Cartao c = (Cartao) value;
                String mascarado = c.getNumeroMascarado();
                String holder = c.getNomeTitular() != null ? securityManager.maskSensitiveData(c.getNomeTitular(), "GENERIC") : "";
                setText(c.getBandeira() + " " + mascarado + (holder.isEmpty() ? "" : " - " + holder));
            }
            return this;
        }
    }

    /**
     * Stub local simples para substituir a dependência externa de pagamento
     * quando a biblioteca não estiver disponível em tempo de compilação.
     * Substitua por implementação real de integração HTTP/PIX em produção.
     */
    private static class pagamentoHTTP {
        public int codretorno = -1;
        private final String nome;
        private final String cpf;
        private final String ncartao;
        private final String valor;
        private final String tipoPagamento;
        private final String apiUrl;

        public pagamentoHTTP(String nome, String cpf, String ncartao, String valor, String tipoPagamento, String apiUrl) {
            this.nome = nome;
            this.cpf = cpf;
            this.ncartao = ncartao;
            this.valor = valor;
            this.tipoPagamento = tipoPagamento;
            this.apiUrl = apiUrl;
        }

        public String conecta() throws Exception {
            // Simula uma chamada externa; altera codretorno conforme necessário
            this.codretorno = 200;
            return "{\"status\":\"APROVADO\",\"valor\":\"" + valor + "\"}";
        }
        }
        
        public static void main(String[] args) {
            SwingUtilities.invokeLater(() -> {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException e) {
                    System.err.println("Erro ao aplicar look and feel: " + e.getMessage());
                }
                
                new TelaPagamentoPIX().setVisible(true);
            });
        }
    }
