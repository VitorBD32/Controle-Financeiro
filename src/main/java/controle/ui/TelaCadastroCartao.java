package controle.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.util.List;

import controle.dao.CartaoDAO;
import controle.dao.CartaoDAOImpl;
import controle.model.Cartao;
import controle.model.Usuario;
import controle.util.CryptoUtil;

/**
 * Tela para cadastro seguro de cartões de crédito/débito
 * Implementa criptografia AES-256 para dados sensíveis
 * Design premium inspirado em aplicativos bancários
 */
public class TelaCadastroCartao extends JFrame {

    private Usuario usuario;
    private CartaoDAO cartaoDAO = new CartaoDAOImpl();
    
    // Campos do formulário
    private JTextField txtNumeroCartao;
    private JTextField txtNomeTitular;
    private JFormattedTextField txtValidade;
    private JPasswordField txtCVV;
    private JComboBox<String> cmbTipoCartao;
    private JTextField txtApelido;
    private JLabel lblBandeira;
    private JLabel lblValidacao;
    
    // Lista de cartões cadastrados
    private JList<Cartao> listCartoes;
    private DefaultListModel<Cartao> listModel;
    
    // Cores do tema
    private static final Color PRIMARY_COLOR = new Color(0, 82, 155);
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private static final Color DANGER_COLOR = new Color(220, 53, 69);
    private static final Color WARNING_COLOR = new Color(255, 193, 7);
    
    // Tipo de cartão pré-selecionado
    private String tipoCartaoPreSelecionado = null;

    public TelaCadastroCartao(Usuario usuario) {
        super("💳 Gerenciar Cartões - Controle Financeiro");
        this.usuario = usuario;
        initComponents();
        carregarCartoes();
    }
    
    /**
     * Construtor alternativo com tipo de cartão pré-selecionado
     */
    public TelaCadastroCartao(JFrame parent, Usuario usuario, String tipoCartao) {
        super("💳 Adicionar Cartão - " + tipoCartao);
        this.usuario = usuario;
        this.tipoCartaoPreSelecionado = tipoCartao;
        initComponents();
        carregarCartoes();
        
        // Pré-seleciona o tipo de cartão
        if (tipoCartao != null && cmbTipoCartao != null) {
            cmbTipoCartao.setSelectedItem(tipoCartao);
            cmbTipoCartao.setEnabled(false); // Trava no tipo selecionado
        }
        
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 247, 250));

        // Painel principal dividido
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(450);
        splitPane.setBorder(null);

        // === PAINEL ESQUERDO - Formulário de cadastro ===
        JPanel formPanel = criarPainelFormulario();
        splitPane.setLeftComponent(formPanel);

        // === PAINEL DIREITO - Lista de cartões ===
        JPanel listPanel = criarPainelLista();
        splitPane.setRightComponent(listPanel);

        add(splitPane, BorderLayout.CENTER);

        // Rodapé com informações de segurança
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(new Color(233, 236, 239));
        footerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel lblSeguranca = new JLabel("🔒 Seus dados são protegidos com criptografia AES-256 de nível bancário");
        lblSeguranca.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSeguranca.setForeground(new Color(108, 117, 125));
        footerPanel.add(lblSeguranca);
        add(footerPanel, BorderLayout.SOUTH);

        setSize(850, 600);
        setLocationRelativeTo(null);
    }

    private JPanel criarPainelFormulario() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(222, 226, 230)),
                new EmptyBorder(20, 20, 20, 20)
        ));

        // Título
        JLabel lblTitulo = new JLabel("Adicionar Novo Cartão");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(PRIMARY_COLOR);
        panel.add(lblTitulo, BorderLayout.NORTH);

        // Formulário
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Número do Cartão
        gbc.gridx = 0; gbc.gridy = 0;
        fieldsPanel.add(new JLabel("Número do Cartão:"), gbc);
        gbc.gridy = 1; gbc.weightx = 1;
        txtNumeroCartao = new JTextField(20);
        txtNumeroCartao.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtNumeroCartao.putClientProperty("JTextField.placeholderText", "0000 0000 0000 0000");
        txtNumeroCartao.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                formatarNumeroCartao();
                detectarBandeira();
                validarCartao();
            }
        });
        fieldsPanel.add(txtNumeroCartao, gbc);

        // Bandeira detectada
        gbc.gridy = 2;
        lblBandeira = new JLabel(" ");
        lblBandeira.setFont(new Font("Segoe UI", Font.BOLD, 12));
        fieldsPanel.add(lblBandeira, gbc);

        // Nome do Titular
        gbc.gridy = 3;
        fieldsPanel.add(new JLabel("Nome do Titular (como no cartão):"), gbc);
        gbc.gridy = 4;
        txtNomeTitular = new JTextField(20);
        txtNomeTitular.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtNomeTitular.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                txtNomeTitular.setText(txtNomeTitular.getText().toUpperCase());
            }
        });
        fieldsPanel.add(txtNomeTitular, gbc);

        // Validade e CVV na mesma linha
        gbc.gridy = 5; gbc.gridwidth = 1; gbc.weightx = 0.5;
        fieldsPanel.add(new JLabel("Validade (MM/AA):"), gbc);
        gbc.gridx = 1;
        fieldsPanel.add(new JLabel("CVV:"), gbc);

        gbc.gridy = 6; gbc.gridx = 0;
        try {
            MaskFormatter maskValidade = new MaskFormatter("##/##");
            maskValidade.setPlaceholderCharacter('_');
            txtValidade = new JFormattedTextField(maskValidade);
        } catch (ParseException e) {
            txtValidade = new JFormattedTextField();
        }
        txtValidade.setColumns(8);
        txtValidade.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        fieldsPanel.add(txtValidade, gbc);

        gbc.gridx = 1;
        txtCVV = new JPasswordField(6);
        txtCVV.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        fieldsPanel.add(txtCVV, gbc);

        // Tipo de Cartão
        gbc.gridy = 7; gbc.gridx = 0; gbc.gridwidth = 2; gbc.weightx = 1;
        fieldsPanel.add(new JLabel("Tipo de Cartão:"), gbc);
        gbc.gridy = 8;
        cmbTipoCartao = new JComboBox<>(new String[]{"CREDITO", "DEBITO", "AMBOS"});
        cmbTipoCartao.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        fieldsPanel.add(cmbTipoCartao, gbc);

        // Apelido
        gbc.gridy = 9;
        fieldsPanel.add(new JLabel("Apelido (opcional):"), gbc);
        gbc.gridy = 10;
        txtApelido = new JTextField(20);
        txtApelido.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtApelido.putClientProperty("JTextField.placeholderText", "Ex: Cartão Principal");
        fieldsPanel.add(txtApelido, gbc);

        // Label de validação
        gbc.gridy = 11;
        lblValidacao = new JLabel(" ");
        lblValidacao.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        fieldsPanel.add(lblValidacao, gbc);

        panel.add(fieldsPanel, BorderLayout.CENTER);

        // Botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton btnLimpar = new JButton("Limpar");
        btnLimpar.addActionListener(e -> limparFormulario());

        JButton btnSalvar = new JButton("💾 Salvar Cartão");
        btnSalvar.setBackground(SUCCESS_COLOR);
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSalvar.addActionListener(e -> salvarCartao());

        buttonPanel.add(btnLimpar);
        buttonPanel.add(btnSalvar);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel criarPainelLista() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 10, 20, 20));

        // Título
        JLabel lblTitulo = new JLabel("Meus Cartões");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(PRIMARY_COLOR);
        panel.add(lblTitulo, BorderLayout.NORTH);

        // Lista de cartões
        listModel = new DefaultListModel<>();
        listCartoes = new JList<>(listModel);
        listCartoes.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        listCartoes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listCartoes.setCellRenderer(new CartaoListCellRenderer());
        
        JScrollPane scrollPane = new JScrollPane(listCartoes);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(222, 226, 230)));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Botões da lista
        JPanel listButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        listButtonPanel.setBackground(Color.WHITE);

        JButton btnRemover = new JButton("🗑️ Remover");
        btnRemover.setForeground(DANGER_COLOR);
        btnRemover.addActionListener(e -> removerCartaoSelecionado());

        JButton btnAtualizar = new JButton("🔄 Atualizar");
        btnAtualizar.addActionListener(e -> carregarCartoes());

        listButtonPanel.add(btnAtualizar);
        listButtonPanel.add(btnRemover);
        panel.add(listButtonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void formatarNumeroCartao() {
        String texto = txtNumeroCartao.getText().replaceAll("\\D", "");
        StringBuilder formatado = new StringBuilder();
        for (int i = 0; i < texto.length() && i < 16; i++) {
            if (i > 0 && i % 4 == 0) {
                formatado.append(" ");
            }
            formatado.append(texto.charAt(i));
        }
        
        int caretPos = txtNumeroCartao.getCaretPosition();
        txtNumeroCartao.setText(formatado.toString());
        try {
            txtNumeroCartao.setCaretPosition(Math.min(caretPos, formatado.length()));
        } catch (Exception e) {
            // ignora
        }
    }

    private void detectarBandeira() {
        String numero = txtNumeroCartao.getText().replaceAll("\\D", "");
        String bandeira = CryptoUtil.detectCardBrand(numero);
        
        String emoji;
        switch (bandeira) {
            case "Visa":
                emoji = "💳 Visa";
                break;
            case "Mastercard":
                emoji = "💳 Mastercard";
                break;
            case "American Express":
                emoji = "💳 Amex";
                break;
            case "Elo":
                emoji = "💳 Elo";
                break;
            case "Hipercard":
                emoji = "💳 Hipercard";
                break;
            default:
                emoji = "💳 " + bandeira;
                break;
        }
        
        lblBandeira.setText(emoji);
        lblBandeira.setForeground(PRIMARY_COLOR);
    }

    private void validarCartao() {
        String numero = txtNumeroCartao.getText().replaceAll("\\D", "");
        
        if (numero.length() < 13) {
            lblValidacao.setText(" ");
            return;
        }
        
        if (CryptoUtil.validateCardNumber(numero)) {
            lblValidacao.setText("✅ Número de cartão válido");
            lblValidacao.setForeground(SUCCESS_COLOR);
        } else {
            lblValidacao.setText("❌ Número de cartão inválido");
            lblValidacao.setForeground(DANGER_COLOR);
        }
    }

    private void salvarCartao() {
        // Validações
        String numero = txtNumeroCartao.getText().replaceAll("\\D", "");
        String nomeTitular = txtNomeTitular.getText().trim();
        String validade = txtValidade.getText().trim();
        String cvv = new String(txtCVV.getPassword()).trim();
        String tipo = (String) cmbTipoCartao.getSelectedItem();
        String apelido = txtApelido.getText().trim();

        if (numero.length() < 13) {
            JOptionPane.showMessageDialog(this, "Número de cartão inválido!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!CryptoUtil.validateCardNumber(numero)) {
            JOptionPane.showMessageDialog(this, "O número do cartão não passou na validação!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (nomeTitular.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o nome do titular!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (validade.contains("_") || validade.length() < 5) {
            JOptionPane.showMessageDialog(this, "Informe a validade completa (MM/AA)!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (cvv.length() < 3) {
            JOptionPane.showMessageDialog(this, "CVV inválido!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Criar objeto Cartão com dados criptografados
            Cartao cartao = new Cartao();
            cartao.setIdUsuario(usuario.getId());
            cartao.setToken(CryptoUtil.generateSecureToken());
            cartao.setNumeroMascarado(CryptoUtil.maskCardNumber(numero));
            cartao.setNumeroCripto(CryptoUtil.encrypt(numero)); // Criptografa número
            cartao.setNomeTitular(nomeTitular);
            cartao.setValidade(validade);
            cartao.setCvvCripto(CryptoUtil.encrypt(cvv)); // Criptografa CVV
            cartao.setBandeira(CryptoUtil.detectCardBrand(numero));
            cartao.setTipo(tipo);
            cartao.setApelido(apelido.isEmpty() ? null : apelido);

            // Salvar no banco
            cartaoDAO.insert(cartao);

            JOptionPane.showMessageDialog(this,
                    "Cartão cadastrado com sucesso!\n\n" +
                    "🔒 Os dados sensíveis foram criptografados.\n" +
                    "Token: " + cartao.getToken().substring(0, 8) + "...",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);

            limparFormulario();
            carregarCartoes();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao salvar cartão: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            System.err.println("❌ Erro ao salvar cartão: " + ex.getMessage());
        }
    }

    private void limparFormulario() {
        txtNumeroCartao.setText("");
        txtNomeTitular.setText("");
        txtValidade.setText("");
        txtCVV.setText("");
        txtApelido.setText("");
        cmbTipoCartao.setSelectedIndex(0);
        lblBandeira.setText(" ");
        lblValidacao.setText(" ");
        txtNumeroCartao.requestFocus();
    }

    private void carregarCartoes() {
        listModel.clear();
        try {
            List<Cartao> cartoes = cartaoDAO.findByUsuario(usuario.getId());
            for (Cartao c : cartoes) {
                listModel.addElement(c);
            }
            
            System.out.println("[TelaCadastroCartao] Carregados " + cartoes.size() + " cartões do usuário " + usuario.getNome());
        } catch (Exception e) {
            System.err.println("[TelaCadastroCartao] Erro ao carregar cartões: " + e.getMessage());
        }
    }

    private void removerCartaoSelecionado() {
        Cartao selecionado = listCartoes.getSelectedValue();
        if (selecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um cartão para remover!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirma = JOptionPane.showConfirmDialog(this,
                "Deseja remover o cartão:\n" + selecionado.getDescricaoCompleta() + "?",
                "Confirmar Remoção", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirma == JOptionPane.YES_OPTION) {
            try {
                cartaoDAO.delete(selecionado.getId());
                carregarCartoes();
                JOptionPane.showMessageDialog(this, "Cartão removido com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao remover: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Renderer customizado para exibição dos cartões na lista
     */
    private static class CartaoListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof Cartao) {
                Cartao cartao = (Cartao) value;
                String texto = String.format("<html><b>%s</b> %s<br><small>%s | %s</small></html>",
                        cartao.getBandeira(),
                        cartao.getNumeroMascarado(),
                        cartao.getTipo(),
                        cartao.getApelido() != null ? cartao.getApelido() : "Sem apelido");
                setText(texto);
            }
            
            setBorder(new EmptyBorder(8, 10, 8, 10));
            return this;
        }
    }

    public static void main(String[] args) {
        // Teste standalone
        Usuario u = new Usuario();
        u.setId(1);
        u.setNome("Teste");
        
        SwingUtilities.invokeLater(() -> {
            new TelaCadastroCartao(u).setVisible(true);
        });
    }
}
