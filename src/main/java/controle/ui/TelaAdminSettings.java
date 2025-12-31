package controle.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import controle.dao.ConfigDAO;
import controle.dao.ConfigDAOImpl;

/**
 * Tela de Configurações do Sistema - Design Moderno Material Design
 * Reforma Tributária 2026 - EC 132/2023
 * 
 * Alíquotas Padrão IVA-Dual:
 * - CBS (Federal): 8.8%
 * - IBS (Estadual/Municipal): 17.7%
 * - Total IVA: ~26.5%
 * - IS (Imposto Seletivo): variável por categoria
 */
public class TelaAdminSettings extends JFrame {

    // Material Design Colors - Modern Palette
    private static final Color PRIMARY_COLOR = new Color(25, 118, 210);      // Blue 700
    private static final Color PRIMARY_DARK = new Color(21, 101, 192);       // Blue 800
    private static final Color PRIMARY_LIGHT = new Color(66, 165, 245);      // Blue 400
    private static final Color ACCENT_COLOR = new Color(0, 200, 83);         // Green A400
    private static final Color WARNING_COLOR = new Color(255, 152, 0);       // Orange 500
    private static final Color ERROR_COLOR = new Color(244, 67, 54);         // Red 500
    private static final Color BACKGROUND_COLOR = new Color(250, 250, 250);  // Grey 50
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(33, 33, 33);         // Grey 900
    private static final Color TEXT_SECONDARY = new Color(117, 117, 117);    // Grey 600
    private static final Color DIVIDER_COLOR = new Color(224, 224, 224);     // Grey 300
    
    // Gradients
    private static final Color GRADIENT_START = new Color(25, 118, 210);
    private static final Color GRADIENT_END = new Color(0, 172, 193);        // Cyan 600
    
    private final ConfigDAO configDao = new ConfigDAOImpl();
    
    // Campos de Impostos - Reforma Tributária 2026
    private JTextField txtCBS;          // Contribuição sobre Bens e Serviços (Federal)
    private JTextField txtIBS;          // Imposto sobre Bens e Serviços (Estadual/Municipal)
    private JTextField txtIS;           // Imposto Seletivo (produtos específicos)
    private JTextField txtISSQN;        // ISS (para serviços - transição)
    private JTextField txtICMS;         // ICMS (para produtos - transição)
    private JTextField txtPIS;          // PIS (transição)
    private JTextField txtCOFINS;       // COFINS (transição)
    
    // Campos de Configuração da Empresa/Emissor
    private JTextField txtRazaoSocial;
    private JTextField txtNomeFantasia;
    private JTextField txtCNPJ;
    private JTextField txtInscricaoEstadual;
    private JTextField txtInscricaoMunicipal;
    private JTextField txtEndereco;
    private JTextField txtCidade;
    private JTextField txtUF;
    private JTextField txtCEP;
    private JTextField txtTelefone;
    private JTextField txtEmail;
    
    // Campos Bancários para Boletos
    private JTextField txtCodigoBanco;
    private JTextField txtNomeBanco;
    private JTextField txtAgencia;
    private JTextField txtConta;
    private JTextField txtCarteira;
    private JTextField txtConvenio;
    
    // Campos de Sistema
    private JTextField txtSerieNF;
    private JTextField txtProximoNumeroNF;
    private JTextField txtAmbienteNF;
    private JCheckBox chkModoProducao;
    
    // Mapa para facilitar salvamento
    private Map<String, JTextField> camposConfig = new HashMap<>();
    
    // Usuário logado (para log de alterações)
    private String usuarioLogado = "admin";

    public TelaAdminSettings() {
        super("⚙️ Configurações do Sistema - Reforma Tributária 2026");
        initComponents();
        loadConfig();
    }
    
    public TelaAdminSettings(String usuario) {
        this();
        this.usuarioLogado = usuario;
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        getContentPane().setBackground(BACKGROUND_COLOR);
        
        // Header com gradiente
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Tabs principais
        JTabbedPane tabbedPane = createStyledTabbedPane();
        
        // Aba 1: Impostos (Reforma Tributária)
        JPanel impostosPanel = createImpostosPanel();
        tabbedPane.addTab("💰 Impostos", null, impostosPanel, "Configurações da Reforma Tributária 2026");
        
        // Aba 2: Dados da Empresa
        JPanel empresaPanel = createEmpresaPanel();
        tabbedPane.addTab("🏢 Empresa", null, empresaPanel, "Dados do Emissor de NF-e/NFS-e");
        
        // Aba 3: Configurações Bancárias
        JPanel bancoPanel = createBancoPanel();
        tabbedPane.addTab("🏦 Banco", null, bancoPanel, "Dados Bancários para Boletos");
        
        // Aba 4: Sistema
        JPanel sistemaPanel = createSistemaPanel();
        tabbedPane.addTab("⚙️ Sistema", null, sistemaPanel, "Configurações Gerais do Sistema");
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Footer com botões
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
        
        // Configurações da janela
        setSize(1000, 750);
        setMinimumSize(new Dimension(900, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Ícone da janela (se disponível)
        try {
            setIconImage(new ImageIcon(getClass().getResource("/icons/settings.png")).getImage());
        } catch (Exception e) {
            // Ícone não disponível
        }
    }
    
    /**
     * Cria o header com gradiente e título
     */
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradiente
                GradientPaint gradient = new GradientPaint(
                    0, 0, GRADIENT_START,
                    getWidth(), getHeight(), GRADIENT_END
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setLayout(new BorderLayout());
        header.setPreferredSize(new Dimension(0, 80));
        
        // Título
        JLabel lblTitulo = new JLabel("  Configurações do Sistema");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(Color.WHITE);
        header.add(lblTitulo, BorderLayout.WEST);
        
        // Subtítulo
        JLabel lblSubtitulo = new JLabel("Reforma Tributária 2026 - EC 132/2023  ");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitulo.setForeground(new Color(255, 255, 255, 200));
        header.add(lblSubtitulo, BorderLayout.EAST);
        
        return header;
    }
    
    /**
     * Cria TabbedPane com estilo moderno
     */
    private JTabbedPane createStyledTabbedPane() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setBackground(BACKGROUND_COLOR);
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabs.setForeground(PRIMARY_COLOR);
        
        // Customiza a UI das tabs
        tabs.setUI(new BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                lightHighlight = PRIMARY_LIGHT;
                shadow = DIVIDER_COLOR;
                darkShadow = DIVIDER_COLOR;
                focus = PRIMARY_COLOR;
            }
            
            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                    int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (isSelected) {
                    g2d.setColor(CARD_COLOR);
                } else {
                    g2d.setColor(BACKGROUND_COLOR);
                }
                g2d.fillRoundRect(x, y, w, h + 5, 8, 8);
                
                if (isSelected) {
                    g2d.setColor(PRIMARY_COLOR);
                    g2d.fillRect(x, y + h - 3, w, 3);
                }
            }
        });
        
        return tabs;
    }
    
    /**
     * Cria o painel de impostos - Reforma Tributária 2026
     */
    private JPanel createImpostosPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // ScrollPane para conteúdo
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BACKGROUND_COLOR);
        
        // Card 1: Novos Tributos (IVA-Dual)
        JPanel cardIVA = createCard("🆕 Novos Tributos - IVA-Dual (2026-2033)", new Color(76, 175, 80));
        JPanel gridIVA = new JPanel(new GridLayout(3, 4, 15, 10));
        gridIVA.setBackground(CARD_COLOR);
        
        txtCBS = createStyledTextField("8.8");
        txtIBS = createStyledTextField("17.7");
        txtIS = createStyledTextField("0.0");
        
        gridIVA.add(createLabelWithTooltip("CBS (Federal) %:", 
            "Contribuição sobre Bens e Serviços\nSubstitui: PIS, COFINS, IPI\nAlíquota padrão: 8.8%"));
        gridIVA.add(txtCBS);
        gridIVA.add(createLabelWithTooltip("IBS (Est./Mun.) %:", 
            "Imposto sobre Bens e Serviços\nSubstitui: ICMS, ISS\nAlíquota padrão: 17.7%"));
        gridIVA.add(txtIBS);
        
        gridIVA.add(createLabelWithTooltip("IS (Seletivo) %:", 
            "Imposto Seletivo\nProdutos prejudiciais à saúde/meio ambiente\nCigarros, bebidas, combustíveis fósseis"));
        gridIVA.add(txtIS);
        
        // Total IVA (calculado)
        JLabel lblTotalIVA = new JLabel("Total IVA: 26.5%");
        lblTotalIVA.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTotalIVA.setForeground(PRIMARY_COLOR);
        gridIVA.add(new JLabel(""));
        gridIVA.add(lblTotalIVA);
        
        // Listener para atualizar total
        DocumentListener docListener = new DocumentListener() {
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateTotal(); }
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateTotal(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateTotal(); }
            
            private void updateTotal() {
                try {
                    double cbs = parseDouble(txtCBS.getText());
                    double ibs = parseDouble(txtIBS.getText());
                    lblTotalIVA.setText(String.format("Total IVA: %.1f%%", cbs + ibs));
                } catch (Exception ex) {
                    lblTotalIVA.setText("Total IVA: --");
                }
            }
        };
        txtCBS.getDocument().addDocumentListener(docListener);
        txtIBS.getDocument().addDocumentListener(docListener);
        
        cardIVA.add(gridIVA, BorderLayout.CENTER);
        content.add(cardIVA);
        content.add(Box.createVerticalStrut(15));
        
        // Card 2: Tributos em Transição (2026-2032)
        JPanel cardTransicao = createCard("📅 Tributos em Transição (Extinção Gradual)", WARNING_COLOR);
        JPanel gridTransicao = new JPanel(new GridLayout(2, 4, 15, 10));
        gridTransicao.setBackground(CARD_COLOR);
        
        txtICMS = createStyledTextField("18.0");
        txtISSQN = createStyledTextField("5.0");
        txtPIS = createStyledTextField("1.65");
        txtCOFINS = createStyledTextField("7.6");
        
        gridTransicao.add(createLabelWithTooltip("ICMS %:", 
            "Imposto sobre Circulação de Mercadorias\nSerá extinto gradualmente até 2033\nSubstituído pelo IBS"));
        gridTransicao.add(txtICMS);
        gridTransicao.add(createLabelWithTooltip("ISSQN %:", 
            "Imposto Sobre Serviços\nSerá extinto gradualmente até 2033\nSubstituído pelo IBS"));
        gridTransicao.add(txtISSQN);
        
        gridTransicao.add(createLabelWithTooltip("PIS %:", 
            "Programa de Integração Social\nSerá extinto gradualmente até 2027\nSubstituído pelo CBS"));
        gridTransicao.add(txtPIS);
        gridTransicao.add(createLabelWithTooltip("COFINS %:", 
            "Contribuição para Financiamento da Seguridade Social\nSerá extinto gradualmente até 2027\nSubstituído pelo CBS"));
        gridTransicao.add(txtCOFINS);
        
        cardTransicao.add(gridTransicao, BorderLayout.CENTER);
        content.add(cardTransicao);
        content.add(Box.createVerticalStrut(15));
        
        // Card 3: Informações da Reforma
        JPanel cardInfo = createCard("📋 Cronograma da Reforma Tributária", PRIMARY_LIGHT);
        JTextArea txtInfo = new JTextArea();
        txtInfo.setEditable(false);
        txtInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtInfo.setBackground(CARD_COLOR);
        txtInfo.setText(
            "CRONOGRAMA EC 132/2023 - REFORMA TRIBUTÁRIA:\n\n" +
            "• 2026: Início da transição - CBS a 0.9%, IBS a 0.1%\n" +
            "• 2027: CBS substitui PIS/COFINS, IPI zerado (exceto ZFM)\n" +
            "• 2028: CBS plena 8.8%, IBS começa substituir ICMS/ISS\n" +
            "• 2029-2032: Redução gradual ICMS/ISS, aumento IBS\n" +
            "• 2033: Extinção total ICMS/ISS, IBS pleno 17.7%\n\n" +
            "ALÍQUOTA PADRÃO ESTIMADA:\n" +
            "• CBS (Federal): 8.8%\n" +
            "• IBS (Estadual/Municipal): 17.7%\n" +
            "• TOTAL IVA-DUAL: 26.5%"
        );
        txtInfo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        cardInfo.add(new JScrollPane(txtInfo), BorderLayout.CENTER);
        content.add(cardInfo);
        
        // Registra campos
        camposConfig.put("TAXA_CBS", txtCBS);
        camposConfig.put("TAXA_IBS", txtIBS);
        camposConfig.put("TAXA_IS", txtIS);
        camposConfig.put("TAXA_ICMS", txtICMS);
        camposConfig.put("TAXA_ISSQN", txtISSQN);
        camposConfig.put("TAXA_PIS", txtPIS);
        camposConfig.put("TAXA_COFINS", txtCOFINS);
        
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Cria o painel de dados da empresa
     */
    private JPanel createEmpresaPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BACKGROUND_COLOR);
        
        // Card: Dados do Emissor
        JPanel cardEmissor = createCard("🏢 Dados do Emissor (NF-e / NFS-e)", PRIMARY_COLOR);
        JPanel gridEmissor = new JPanel(new GridBagLayout());
        gridEmissor.setBackground(CARD_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        txtRazaoSocial = createStyledTextField("");
        txtNomeFantasia = createStyledTextField("");
        txtCNPJ = createStyledTextField("");
        txtInscricaoEstadual = createStyledTextField("");
        txtInscricaoMunicipal = createStyledTextField("");
        txtEndereco = createStyledTextField("");
        txtCidade = createStyledTextField("");
        txtUF = createStyledTextField("");
        txtCEP = createStyledTextField("");
        txtTelefone = createStyledTextField("");
        txtEmail = createStyledTextField("");
        
        // Linha 1: Razão Social (span completo)
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1; gbc.weightx = 0.0;
        gridEmissor.add(createLabel("Razão Social:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 1.0;
        gridEmissor.add(txtRazaoSocial, gbc);
        
        // Linha 2: Nome Fantasia (span completo)
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.0;
        gridEmissor.add(createLabel("Nome Fantasia:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 1.0;
        gridEmissor.add(txtNomeFantasia, gbc);
        
        // Linha 3: CNPJ | Inscrição Estadual
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; gbc.weightx = 0.0;
        gridEmissor.add(createLabel("CNPJ:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 1; gbc.weightx = 0.5;
        gridEmissor.add(txtCNPJ, gbc);
        gbc.gridx = 2; gbc.gridwidth = 1; gbc.weightx = 0.0;
        gridEmissor.add(createLabel("Inscrição Estadual:"), gbc);
        gbc.gridx = 3; gbc.gridwidth = 1; gbc.weightx = 0.5;
        gridEmissor.add(txtInscricaoEstadual, gbc);
        
        // Linha 4: Inscrição Municipal | UF
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1; gbc.weightx = 0.0;
        gridEmissor.add(createLabel("Inscrição Municipal:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 1; gbc.weightx = 0.5;
        gridEmissor.add(txtInscricaoMunicipal, gbc);
        gbc.gridx = 2; gbc.gridwidth = 1; gbc.weightx = 0.0;
        gridEmissor.add(createLabel("UF:"), gbc);
        gbc.gridx = 3; gbc.gridwidth = 1; gbc.weightx = 0.2;
        gridEmissor.add(txtUF, gbc);
        
        // Linha 5: Endereço (span completo)
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1; gbc.weightx = 0.0;
        gridEmissor.add(createLabel("Endereço:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 1.0;
        gridEmissor.add(txtEndereco, gbc);
        
        // Linha 6: Cidade | CEP
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1; gbc.weightx = 0.0;
        gridEmissor.add(createLabel("Cidade:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 1; gbc.weightx = 0.6;
        gridEmissor.add(txtCidade, gbc);
        gbc.gridx = 2; gbc.gridwidth = 1; gbc.weightx = 0.0;
        gridEmissor.add(createLabel("CEP:"), gbc);
        gbc.gridx = 3; gbc.gridwidth = 1; gbc.weightx = 0.4;
        gridEmissor.add(txtCEP, gbc);
        
        // Linha 7: Telefone | E-mail
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 1; gbc.weightx = 0.0;
        gridEmissor.add(createLabel("Telefone:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 1; gbc.weightx = 0.4;
        gridEmissor.add(txtTelefone, gbc);
        gbc.gridx = 2; gbc.gridwidth = 1; gbc.weightx = 0.0;
        gridEmissor.add(createLabel("E-mail:"), gbc);
        gbc.gridx = 3; gbc.gridwidth = 1; gbc.weightx = 0.6;
        gridEmissor.add(txtEmail, gbc);
        
        cardEmissor.add(gridEmissor, BorderLayout.CENTER);
        content.add(cardEmissor);
        
        // Registra campos
        camposConfig.put("EMISSOR_RAZAO_SOCIAL", txtRazaoSocial);
        camposConfig.put("EMISSOR_NOME_FANTASIA", txtNomeFantasia);
        camposConfig.put("EMISSOR_CNPJ", txtCNPJ);
        camposConfig.put("EMISSOR_IE", txtInscricaoEstadual);
        camposConfig.put("EMISSOR_IM", txtInscricaoMunicipal);
        camposConfig.put("EMISSOR_ENDERECO", txtEndereco);
        camposConfig.put("EMISSOR_CIDADE", txtCidade);
        camposConfig.put("EMISSOR_UF", txtUF);
        camposConfig.put("EMISSOR_CEP", txtCEP);
        camposConfig.put("EMISSOR_TELEFONE", txtTelefone);
        camposConfig.put("EMISSOR_EMAIL", txtEmail);
        
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Cria o painel de configurações bancárias
     */
    private JPanel createBancoPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BACKGROUND_COLOR);
        
        // Card: Dados Bancários
        JPanel cardBanco = createCard("🏦 Dados Bancários (Boletos FEBRABAN)", new Color(156, 39, 176));
        JPanel gridBanco = new JPanel(new GridLayout(3, 4, 15, 10));
        gridBanco.setBackground(CARD_COLOR);
        
        txtCodigoBanco = createStyledTextField("001");
        txtNomeBanco = createStyledTextField("Banco do Brasil");
        txtAgencia = createStyledTextField("");
        txtConta = createStyledTextField("");
        txtCarteira = createStyledTextField("17");
        txtConvenio = createStyledTextField("");
        
        gridBanco.add(createLabelWithTooltip("Código Banco:", 
            "Código FEBRABAN do banco\n001=BB, 033=Santander, 104=CEF\n237=Bradesco, 341=Itaú"));
        gridBanco.add(txtCodigoBanco);
        gridBanco.add(new JLabel("Nome Banco:"));
        gridBanco.add(txtNomeBanco);
        
        gridBanco.add(new JLabel("Agência:"));
        gridBanco.add(txtAgencia);
        gridBanco.add(new JLabel("Conta:"));
        gridBanco.add(txtConta);
        
        gridBanco.add(createLabelWithTooltip("Carteira:", 
            "Código da carteira de cobrança\nVaria conforme o banco"));
        gridBanco.add(txtCarteira);
        gridBanco.add(new JLabel("Convênio:"));
        gridBanco.add(txtConvenio);
        
        cardBanco.add(gridBanco, BorderLayout.CENTER);
        content.add(cardBanco);
        content.add(Box.createVerticalStrut(15));
        
        // Card: Informações FEBRABAN
        JPanel cardInfo = createCard("📋 Informações sobre Boletos", new Color(103, 58, 183));
        JTextArea txtInfo = new JTextArea();
        txtInfo.setEditable(false);
        txtInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtInfo.setBackground(CARD_COLOR);
        txtInfo.setText(
            "CÓDIGOS DOS PRINCIPAIS BANCOS (FEBRABAN):\n\n" +
            "001 - Banco do Brasil\n" +
            "033 - Santander\n" +
            "104 - Caixa Econômica Federal\n" +
            "237 - Bradesco\n" +
            "341 - Itaú Unibanco\n" +
            "389 - Banco Mercantil do Brasil\n" +
            "422 - Safra\n" +
            "756 - Sicoob\n\n" +
            "O sistema gera boletos conforme padrão FEBRABAN,\n" +
            "com linha digitável de 47 posições e código de barras."
        );
        txtInfo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        cardInfo.add(new JScrollPane(txtInfo), BorderLayout.CENTER);
        content.add(cardInfo);
        
        // Registra campos
        camposConfig.put("BANCO_CODIGO", txtCodigoBanco);
        camposConfig.put("BANCO_NOME", txtNomeBanco);
        camposConfig.put("BANCO_AGENCIA", txtAgencia);
        camposConfig.put("BANCO_CONTA", txtConta);
        camposConfig.put("BANCO_CARTEIRA", txtCarteira);
        camposConfig.put("BANCO_CONVENIO", txtConvenio);
        
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Cria o painel de configurações do sistema
     */
    private JPanel createSistemaPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BACKGROUND_COLOR);
        
        // Card: Configurações de NF
        JPanel cardNF = createCard("📄 Notas Fiscais Eletrônicas", new Color(0, 150, 136));
        JPanel gridNF = new JPanel(new GridLayout(2, 4, 15, 10));
        gridNF.setBackground(CARD_COLOR);
        
        txtSerieNF = createStyledTextField("1");
        txtProximoNumeroNF = createStyledTextField("1");
        txtAmbienteNF = createStyledTextField("2");
        chkModoProducao = new JCheckBox("Modo Produção");
        chkModoProducao.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chkModoProducao.setBackground(CARD_COLOR);
        
        gridNF.add(createLabelWithTooltip("Série NF:", 
            "Série da Nota Fiscal\nGeralmente 1 para NF-e principal"));
        gridNF.add(txtSerieNF);
        gridNF.add(createLabelWithTooltip("Próximo Número:", 
            "Próximo número de NF a ser emitido"));
        gridNF.add(txtProximoNumeroNF);
        
        gridNF.add(createLabelWithTooltip("Ambiente:", 
            "1 = Produção\n2 = Homologação (Testes)"));
        gridNF.add(txtAmbienteNF);
        gridNF.add(new JLabel(""));
        gridNF.add(chkModoProducao);
        
        cardNF.add(gridNF, BorderLayout.CENTER);
        content.add(cardNF);
        content.add(Box.createVerticalStrut(15));
        
        // Card: Informações do Sistema
        JPanel cardSistema = createCard("ℹ️ Informações do Sistema", TEXT_SECONDARY);
        JTextArea txtSistema = new JTextArea();
        txtSistema.setEditable(false);
        txtSistema.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSistema.setBackground(CARD_COLOR);
        txtSistema.setText(
            "SISTEMA DE CONTROLE FINANCEIRO v2.0\n" +
            "Reforma Tributária 2026 - EC 132/2023\n\n" +
            "Funcionalidades:\n" +
            "• Gestão de receitas e despesas\n" +
            "• Emissão de NF-e e NFS-e\n" +
            "• Geração de boletos FEBRABAN\n" +
            "• Pagamentos via PIX com QR Code\n" +
            "• Gráficos e relatórios financeiros\n" +
            "• Cálculo automático de tributos (IVA-Dual)\n\n" +
            "Desenvolvido para UESPI - 2024/2025\n" +
            "Java " + System.getProperty("java.version")
        );
        txtSistema.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        cardSistema.add(new JScrollPane(txtSistema), BorderLayout.CENTER);
        content.add(cardSistema);
        
        // Registra campos
        camposConfig.put("NF_SERIE", txtSerieNF);
        camposConfig.put("NF_PROXIMO_NUMERO", txtProximoNumeroNF);
        camposConfig.put("NF_AMBIENTE", txtAmbienteNF);
        
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Cria o footer com botões
     */
    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 20));
        footer.setBackground(BACKGROUND_COLOR);
        footer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, DIVIDER_COLOR),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        
        JButton btnRestaurar = createStyledButton("🔄 Restaurar Padrões", WARNING_COLOR, Color.WHITE);
        btnRestaurar.setPreferredSize(new Dimension(180, 42));
        btnRestaurar.addActionListener(e -> restaurarPadroes());
        
        JButton btnCancelar = createStyledButton("Cancelar", TEXT_SECONDARY, Color.WHITE);
        btnCancelar.setPreferredSize(new Dimension(130, 42));
        btnCancelar.addActionListener(e -> dispose());
        
        JButton btnSalvar = createStyledButton("💾 SALVAR CONFIGURAÇÕES", ACCENT_COLOR, Color.WHITE);
        btnSalvar.setPreferredSize(new Dimension(220, 45));
        btnSalvar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSalvar.addActionListener(e -> salvarConfig());
        
        footer.add(btnRestaurar);
        footer.add(btnCancelar);
        footer.add(btnSalvar);
        
        return footer;
    }
    
    /**
     * Cria um card com título e borda
     */
    private JPanel createCard(String titulo, Color corTitulo) {
        JPanel card = new JPanel(new BorderLayout(15, 15));
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(DIVIDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(20, 25, 25, 25)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 450));
        
        // Título do card
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitulo.setForeground(corTitulo);
        lblTitulo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, corTitulo),
            BorderFactory.createEmptyBorder(0, 0, 15, 0)
        ));
        card.add(lblTitulo, BorderLayout.NORTH);
        
        return card;
    }
    
    /**
     * Cria um JLabel estilizado para os campos
     */
    private JLabel createLabel(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(TEXT_PRIMARY);
        return label;
    }
    
    /**
     * Cria um JTextField estilizado
     */
    private JTextField createStyledTextField(String texto) {
        JTextField field = new JTextField(texto);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setEditable(true); // Garantir que o admin pode editar todos os campos
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(DIVIDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        
        // Efeito de foco
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(PRIMARY_COLOR, 2, true),
                    BorderFactory.createEmptyBorder(7, 9, 7, 9)
                ));
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(DIVIDER_COLOR, 1, true),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
                ));
            }
        });
        
        return field;
    }
    
    /**
     * Cria um botão estilizado
     */
    private JButton createStyledButton(String texto, Color bgColor, Color fgColor) {
        JButton button = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2d.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(bgColor.brighter());
                } else {
                    g2d.setColor(bgColor);
                }
                
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                g2d.setColor(fgColor);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);
            }
        };
        
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(180, 40));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    /**
     * Cria label com tooltip
     */
    private JLabel createLabelWithTooltip(String texto, String tooltip) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setToolTipText("<html>" + tooltip.replace("\n", "<br>") + "</html>");
        return label;
    }
    
    /**
     * Carrega as configurações do banco de dados
     */
    private void loadConfig() {
        try {
            for (Map.Entry<String, JTextField> entry : camposConfig.entrySet()) {
                String valor = configDao.get(entry.getKey());
                if (valor != null && !valor.isEmpty()) {
                    entry.getValue().setText(valor);
                }
            }
            
            // Checkbox modo produção
            String ambiente = configDao.get("NF_AMBIENTE");
            if (ambiente != null) {
                chkModoProducao.setSelected("1".equals(ambiente));
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao carregar configurações: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Salva todas as configurações no banco de dados
     */
    private void salvarConfig() {
        try {
            // Validação
            if (!validarCampos()) {
                return;
            }
            
            // Salva todos os campos
            int salvos = 0;
            for (Map.Entry<String, JTextField> entry : camposConfig.entrySet()) {
                String valor = entry.getValue().getText().trim();
                if (configDao.set(entry.getKey(), valor)) {
                    salvos++;
                }
            }
            
            // Atualiza ambiente baseado no checkbox
            if (chkModoProducao.isSelected()) {
                configDao.set("NF_AMBIENTE", "1");
            }
            
            // Mensagem de sucesso
            JOptionPane.showMessageDialog(this,
                "✅ " + salvos + " configurações salvas com sucesso!\n\n" +
                "As novas alíquotas serão aplicadas nas próximas\n" +
                "transações e documentos fiscais.",
                "Configurações Salvas",
                JOptionPane.INFORMATION_MESSAGE);
            
            dispose();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "❌ Erro ao salvar configurações:\n" + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Valida os campos antes de salvar
     */
    private boolean validarCampos() {
        // Valida campos de porcentagem
        String[] camposPorcentagem = {"TAXA_CBS", "TAXA_IBS", "TAXA_IS", "TAXA_ICMS", 
                                       "TAXA_ISSQN", "TAXA_PIS", "TAXA_COFINS"};
        
        for (String campo : camposPorcentagem) {
            JTextField tf = camposConfig.get(campo);
            if (tf != null) {
                try {
                    double valor = parseDouble(tf.getText());
                    if (valor < 0 || valor > 100) {
                        JOptionPane.showMessageDialog(this,
                            "⚠️ O campo " + campo + " deve estar entre 0 e 100%",
                            "Validação", JOptionPane.WARNING_MESSAGE);
                        tf.requestFocus();
                        return false;
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this,
                        "⚠️ O campo " + campo + " deve ser um número válido",
                        "Validação", JOptionPane.WARNING_MESSAGE);
                    tf.requestFocus();
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Restaura os valores padrão da Reforma Tributária
     */
    private void restaurarPadroes() {
        int opcao = JOptionPane.showConfirmDialog(this,
            "🔄 Deseja restaurar todas as configurações para os valores padrão?\n\n" +
            "Isso irá definir as alíquotas padrão da Reforma Tributária 2026:\n" +
            "• CBS: 8.8%\n" +
            "• IBS: 17.7%\n" +
            "• Total IVA: 26.5%",
            "Restaurar Padrões",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (opcao == JOptionPane.YES_OPTION) {
            // Impostos - Reforma 2026
            txtCBS.setText("8.8");
            txtIBS.setText("17.7");
            txtIS.setText("0.0");
            
            // Tributos em transição
            txtICMS.setText("18.0");
            txtISSQN.setText("5.0");
            txtPIS.setText("1.65");
            txtCOFINS.setText("7.6");
            
            // Sistema
            txtSerieNF.setText("1");
            txtAmbienteNF.setText("2");
            chkModoProducao.setSelected(false);
            
            JOptionPane.showMessageDialog(this,
                "✅ Valores padrão restaurados!\n\nClique em 'Salvar' para aplicar as alterações.",
                "Padrões Restaurados",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Parse double com suporte a vírgula
     */
    private double parseDouble(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return 0.0;
        }
        return Double.parseDouble(texto.trim().replace(",", "."));
    }
    
    // Inner class para DocumentListener
    private interface DocumentListener extends javax.swing.event.DocumentListener {
    }
    
    /**
     * Main para teste
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new TelaAdminSettings().setVisible(true);
        });
    }
}
