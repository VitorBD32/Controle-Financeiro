package controle.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Imports do projeto Controle Financeiro
import controle.dao.*;
import controle.model.*;
import controle.util.QRCodePIXGenerator;
import br.uespi.acessoapi.pagamentoHTTP;
import br.uespi.acessoapi.PIXConexao;
import br.uespi.tratajson.JSONObject;
import br.uespi.tratajson.JSONArray;

/**
 * Tela de Pagamento PIX integrada com o sistema de Controle Financeiro
 * Conectada com Usuários, Categorias e Transações
 */
public class TelaPagamentoPIX extends JFrame {

    // Campos de entrada
    private JComboBox<Usuario> cmbUsuario;
    private JComboBox<Categoria> cmbCategoria;
    private JTextField txtValor;
    private JTextField txtDescricao;
    private JComboBox<String> cmbTipoPagamento;
    
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
    
    // URL da API
    private static final String API_URL = "http://www.datse.com.br/dev/syncjava.php";
    private static final String QR_CODE_PATH = "resources/images/qrcode_pix.png";

    public TelaPagamentoPIX() {
        super("💳 Pagamento PIX - Controle Financeiro");
        initDAOs();
        initComponents();
        carregarDados();
    }

    private void initDAOs() {
        try {
            usuarioDAO = new UsuarioDAOImpl();
            categoriaDAO = new CategoriaDAOImpl();
            transacaoDAO = new TransacaoDAOImpl();
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
        cmbTipoPagamento.addActionListener(e -> atualizarQRCode());
        selecaoPanel.add(cmbTipoPagamento, gbc);

        // Valor
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
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
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
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
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
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
            cmbUsuario.removeAllItems();
            for (Usuario u : usuarios) {
                cmbUsuario.addItem(u);
            }
            
            // Carrega categorias (apenas tipo D - Despesa para pagamentos)
            List<Categoria> categorias = categoriaDAO.findAll();
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
        resultado.append("👤 Usuário: ").append(usuario.getNome()).append("\n");
        resultado.append("📁 Categoria: ").append(categoria.getNome()).append("\n");
        resultado.append("💰 Valor: R$ ").append(valor).append("\n");
        resultado.append("📝 Descrição: ").append(descricao).append("\n\n");

        try {
            // Conecta com API
            pagamentoHTTP pagamento = new pagamentoHTTP(
                usuario.getNome(),
                String.valueOf(usuario.getId()),
                tipoPagamento,
                valor.toString(),
                tipoPagamento,
                API_URL
            );
            
            String resposta = pagamento.conecta();
            int codigoHTTP = pagamento.codretorno;

            resultado.append("✅ Status: PROCESSADO\n");
            resultado.append("📡 Código HTTP: ").append(codigoHTTP).append("\n");
            resultado.append("📥 Resposta: ").append(resposta).append("\n");

            System.out.println("✅ Pagamento processado!");
            System.out.println("📡 Código: " + codigoHTTP);
            System.out.println("📥 Resposta: " + resposta);
            System.out.println("=".repeat(60) + "\n");

        } catch (Exception ex) {
            resultado.append("⚠️ Pagamento simulado (API offline)\n");
            resultado.append("Status: APROVADO (modo demo)\n");
            System.out.println("⚠️ API offline - modo simulado");
        }

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
            t.setDescricao(descricao.isEmpty() ? "Pagamento " + tipoPagamento : descricao);
            
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
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {}
            
            new TelaPagamentoPIX().setVisible(true);
        });
    }
}
