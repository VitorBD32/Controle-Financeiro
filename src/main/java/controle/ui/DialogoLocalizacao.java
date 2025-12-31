package controle.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

import controle.model.Localizacao;
import controle.service.GeoLocationService;

/**
 * Diálogo para solicitar permissão e detectar localização do usuário
 * Usado para cálculo automático de tributos baseado em UF/Município
 * 
 * @author Sistema de Controle Financeiro
 * @version 1.0
 */
public class DialogoLocalizacao extends JDialog {
    
    private static final Color PRIMARY_COLOR = new Color(25, 118, 210);
    private static final Color SUCCESS_COLOR = new Color(76, 175, 80);
    private static final Color BACKGROUND = new Color(250, 250, 250);
    
    private Localizacao localizacaoDetectada;
    private boolean permissaoConcedida = false;
    
    private JLabel lblStatus;
    private JLabel lblCidade;
    private JLabel lblUF;
    private JLabel lblCoordenadas;
    private JProgressBar progressBar;
    private JButton btnPermitir;
    private JButton btnNegar;
    private JButton btnFechar;

    public DialogoLocalizacao(Frame parent) {
        super(parent, "📍 Detecção de Localização", true);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(BACKGROUND);
        
        // Painel principal
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        
        // Ícone e título
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        headerPanel.setBackground(Color.WHITE);
        
        JLabel lblIcone = new JLabel("📍");
        lblIcone.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        headerPanel.add(lblIcone);
        
        mainPanel.add(headerPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        
        // Título
        JLabel lblTitulo = new JLabel("Detectar sua localização?");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(PRIMARY_COLOR);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblTitulo);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Descrição
        JTextArea txtDescricao = new JTextArea(
            "O sistema precisa da sua localização para calcular automaticamente " +
            "os tributos (CBS, IBS, IS) de acordo com as alíquotas do seu " +
            "município e estado.\n\n" +
            "A detecção é feita via endereço IP e não utiliza GPS. " +
            "Suas informações são usadas apenas para cálculo de impostos."
        );
        txtDescricao.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtDescricao.setForeground(new Color(100, 100, 100));
        txtDescricao.setLineWrap(true);
        txtDescricao.setWrapStyleWord(true);
        txtDescricao.setEditable(false);
        txtDescricao.setOpaque(false);
        txtDescricao.setAlignmentX(Component.CENTER_ALIGNMENT);
        txtDescricao.setMaximumSize(new Dimension(450, 120));
        mainPanel.add(txtDescricao);
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Status
        lblStatus = new JLabel("Aguardando permissão...");
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblStatus.setForeground(new Color(150, 150, 150));
        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblStatus);
        mainPanel.add(Box.createVerticalStrut(10));
        
        // Progress bar (inicialmente invisível)
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setMaximumSize(new Dimension(400, 20));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(progressBar);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Painel de informações (inicialmente invisível)
        JPanel infoPanel = createInfoPanel();
        mainPanel.add(infoPanel);
        
        // Botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        btnNegar = createButton("❌ Não Permitir", new Color(244, 67, 54), Color.WHITE);
        btnNegar.addActionListener(e -> {
            permissaoConcedida = false;
            dispose();
        });
        
        btnPermitir = createButton("✅ Permitir Localização", SUCCESS_COLOR, Color.WHITE);
        btnPermitir.addActionListener(e -> detectarLocalizacao());
        
        btnFechar = createButton("Fechar", PRIMARY_COLOR, Color.WHITE);
        btnFechar.setVisible(false);
        btnFechar.addActionListener(e -> dispose());
        
        buttonPanel.add(btnNegar);
        buttonPanel.add(btnPermitir);
        buttonPanel.add(btnFechar);
        
        mainPanel.add(buttonPanel);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Configurações da janela
        setSize(550, 500);
        setResizable(false);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(240, 248, 255));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 220, 240), 1),
            new EmptyBorder(15, 20, 15, 20)
        ));
        panel.setMaximumSize(new Dimension(450, 150));
        panel.setVisible(false);
        panel.setName("infoPanel");
        
        lblCidade = createInfoLabel("Cidade: -");
        lblUF = createInfoLabel("Estado: -");
        lblCoordenadas = createInfoLabel("Coordenadas: -");
        
        panel.add(lblCidade);
        panel.add(Box.createVerticalStrut(8));
        panel.add(lblUF);
        panel.add(Box.createVerticalStrut(8));
        panel.add(lblCoordenadas);
        
        return panel;
    }
    
    private JLabel createInfoLabel(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
    
    private JButton createButton(String texto, Color bg, Color fg) {
        JButton button = new JButton(texto);
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(170, 38));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Efeito hover
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bg.darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(bg);
            }
        });
        
        return button;
    }
    
    private void detectarLocalizacao() {
        // Desabilita botões
        btnPermitir.setEnabled(false);
        btnNegar.setEnabled(false);
        
        // Mostra progress
        progressBar.setVisible(true);
        lblStatus.setText("🔍 Detectando sua localização...");
        lblStatus.setForeground(PRIMARY_COLOR);
        
        // Executa em background
        SwingWorker<Localizacao, Void> worker = new SwingWorker<Localizacao, Void>() {
            @Override
            protected Localizacao doInBackground() throws Exception {
                return GeoLocationService.obterLocalizacaoAtual(true);
            }
            
            @Override
            protected void done() {
                try {
                    localizacaoDetectada = get();
                    progressBar.setVisible(false);
                    
                    if (localizacaoDetectada != null && localizacaoDetectada.isValida()) {
                        permissaoConcedida = true;
                        mostrarResultado(localizacaoDetectada);
                    } else {
                        mostrarErro();
                    }
                } catch (Exception e) {
                    progressBar.setVisible(false);
                    mostrarErro();
                    e.printStackTrace();
                }
            }
        };
        
        worker.execute();
    }
    
    private void mostrarResultado(Localizacao loc) {
        lblStatus.setText("✅ Localização detectada com sucesso!");
        lblStatus.setForeground(SUCCESS_COLOR);
        
        // Atualiza informações
        lblCidade.setText("📍 Cidade: " + loc.getCidade());
        lblUF.setText("🏛️ Estado: " + loc.getRegiao() + " (" + loc.getUf() + ")");
        lblCoordenadas.setText(String.format("🌍 Coordenadas: %.4f, %.4f", 
                                            loc.getLatitude(), loc.getLongitude()));
        
        // Mostra painel de informações
        for (Component comp : ((JPanel) getContentPane().getComponent(0)).getComponents()) {
            if (comp instanceof JPanel && "infoPanel".equals(comp.getName())) {
                comp.setVisible(true);
                break;
            }
        }
        
        // Troca botões
        btnPermitir.setVisible(false);
        btnNegar.setVisible(false);
        btnFechar.setVisible(true);
        
        revalidate();
        repaint();
    }
    
    private void mostrarErro() {
        lblStatus.setText("❌ Não foi possível detectar sua localização");
        lblStatus.setForeground(new Color(244, 67, 54));
        
        btnPermitir.setEnabled(true);
        btnPermitir.setText("🔄 Tentar Novamente");
        btnNegar.setEnabled(true);
    }
    
    /**
     * Retorna true se o usuário permitiu a localização
     */
    public boolean isPermissaoConcedida() {
        return permissaoConcedida;
    }
    
    /**
     * Retorna a localização detectada (ou null se não permitido)
     */
    public Localizacao getLocalizacaoDetectada() {
        return localizacaoDetectada;
    }
    
    /**
     * Método estático para solicitar localização
     * Retorna a localização ou null se negado
     */
    public static Localizacao solicitarLocalizacao(Frame parent) {
        DialogoLocalizacao dialogo = new DialogoLocalizacao(parent);
        dialogo.setVisible(true);
        
        if (dialogo.isPermissaoConcedida()) {
            return dialogo.getLocalizacaoDetectada();
        }
        
        return null;
    }
}
