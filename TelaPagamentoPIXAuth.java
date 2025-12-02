import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

// Imports para integração com API PIX
import br.uespi.acessoapi.pagamentoHTTP;
import br.uespi.acessoapi.PIXConexao;
import br.uespi.tratajson.trataJSON;
import br.uespi.tratajson.JSONObject;
import br.uespi.pessoas.pessoa;

/**
 * Tela de Pagamento PIX - Requer autenticação prévia
 * Esta tela só é acessível após login válido
 */
public class TelaPagamentoPIXAuth extends JFrame {

    // Campos de entrada
    private JTextField txtNome;
    private JTextField txtTelefone;
    private JTextField txtCPF;
    private JTextField txtCartao;
    private JTextField txtValor;
    private JComboBox<String> cmbTipoPagamento;
    
    // Área de resultado
    private JTextArea txtResultado;
    
    // Botões
    private JButton btnEmitir;
    private JButton btnImprimir;
    private JButton btnLimpar;
    private JButton btnSair;
    
    // URL da API (endpoint correto sem o "2")
    private static final String API_URL = "http://www.datse.com.br/dev/syncjava.php";
    
    // Usuário logado e senha (para autenticação nas requisições)
    private String usuarioLogado;
    private String senhaUsuario;
    
    // Referência para a tela de login
    private TelaLoginPIX telaLogin;

    public TelaPagamentoPIXAuth(String usuario, String senha, TelaLoginPIX loginFrame) {
        super("Pagamento PIX - Usuário: " + usuario);
        this.usuarioLogado = usuario;
        this.senhaUsuario = senha;
        this.telaLogin = loginFrame;
        initComponents();
        
        // Ao fechar, volta para o login
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                voltarParaLogin();
            }
        });
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Barra superior com informações do usuário
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 120, 215));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JLabel lblUsuarioLogado = new JLabel("Bem-vindo, " + usuarioLogado + "!");
        lblUsuarioLogado.setForeground(Color.WHITE);
        lblUsuarioLogado.setFont(new Font("Arial", Font.BOLD, 14));
        headerPanel.add(lblUsuarioLogado, BorderLayout.WEST);
        
        btnSair = new JButton("Sair");
        btnSair.setBackground(new Color(220, 53, 69));
        btnSair.setForeground(Color.BLACK);
        btnSair.setFocusPainted(false);
        btnSair.addActionListener(e -> voltarParaLogin());
        headerPanel.add(btnSair, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Painel principal de formulário
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Campo Nome
        formPanel.add(createLabeledField("Digite o Nome:", txtNome = new JTextField(25)));
        
        // Campo Telefone
        formPanel.add(createLabeledField("Digite o Telefone:", txtTelefone = new JTextField(25)));
        
        // Campo CPF
        formPanel.add(createLabeledField("Digite o CPF:", txtCPF = new JTextField(25)));
        
        // Campo Cartão/Chave PIX
        formPanel.add(createLabeledField("Dados do Cartão de Crédito:", txtCartao = new JTextField(25)));

        // Painel de valor e tipo de pagamento
        JPanel valorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        valorPanel.add(new JLabel("Digite o Valor:"));
        txtValor = new JTextField(8);
        valorPanel.add(txtValor);
        
        cmbTipoPagamento = new JComboBox<>(new String[]{"PIX", "Crédito", "Débito"});
        valorPanel.add(cmbTipoPagamento);
        
        btnEmitir = new JButton("Emitir");
        btnEmitir.setBackground(new Color(40, 167, 69));
        btnEmitir.setForeground(Color.BLACK);
        btnEmitir.addActionListener(e -> processarPagamento());
        valorPanel.add(btnEmitir);
        
        formPanel.add(valorPanel);

        // Área de resultado
        txtResultado = new JTextArea(5, 30);
        txtResultado.setEditable(false);
        txtResultado.setLineWrap(true);
        txtResultado.setWrapStyleWord(true);
        txtResultado.setBorder(BorderFactory.createTitledBorder("Dados do Processamento"));
        JScrollPane scrollPane = new JScrollPane(txtResultado);
        formPanel.add(scrollPane);

        // Painel de botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        btnImprimir = new JButton("Imprimir");
        btnImprimir.addActionListener(e -> imprimirComprovante());
        buttonPanel.add(btnImprimir);
        
        btnLimpar = new JButton("Limpar");
        btnLimpar.addActionListener(e -> limparCampos());
        buttonPanel.add(btnLimpar);
        
        formPanel.add(buttonPanel);

        add(formPanel, BorderLayout.CENTER);

        // Configurações da janela
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(380, 520);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Exibe mensagem no terminal
        System.out.println("============================================");
        System.out.println("   TELA DE PAGAMENTO PIX ABERTA");
        System.out.println("============================================");
        System.out.println("[INFO] Usuário autenticado: " + usuarioLogado);
        System.out.println("[INFO] Pronto para processar pagamentos");
        System.out.println("============================================\n");
    }

    private JPanel createLabeledField(String label, JTextField field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel lbl = new JLabel(label);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, field.getPreferredSize().height));
        panel.add(lbl);
        panel.add(field);
        panel.add(Box.createVerticalStrut(5));
        return panel;
    }

    /**
     * Processa o pagamento conectando à API
     */
    private void processarPagamento() {
        String nome = txtNome.getText().trim();
        String telefone = txtTelefone.getText().trim();
        String cpf = txtCPF.getText().trim();
        String cartao = txtCartao.getText().trim();
        String valor = txtValor.getText().trim();
        String tipoPagamento = (String) cmbTipoPagamento.getSelectedItem();

        // Validação básica
        if (nome.isEmpty() || cpf.isEmpty() || valor.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Por favor, preencha os campos obrigatórios:\nNome, CPF e Valor", 
                "Campos Obrigatórios", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Desabilita botão durante processamento
        btnEmitir.setEnabled(false);
        txtResultado.setText("Processando pagamento...");

        // Exibe no terminal o início do processamento
        System.out.println("\n============================================");
        System.out.println("   PROCESSANDO PAGAMENTO " + tipoPagamento);
        System.out.println("============================================");
        System.out.println("[INFO] Operador: " + usuarioLogado);
        System.out.println("[INFO] Nome: " + nome);
        System.out.println("[INFO] Telefone: " + telefone);
        System.out.println("[INFO] CPF: " + cpf);
        System.out.println("[INFO] Cartão/Chave: " + cartao);
        System.out.println("[INFO] Valor: R$ " + valor);
        System.out.println("[INFO] Tipo: " + tipoPagamento);
        System.out.println("[INFO] URL da API: " + API_URL);
        System.out.println("--------------------------------------------");

        // Processa em thread separada
        new Thread(() -> {
            try {
                System.out.println("[INFO] Conectando à API com autenticação...");
                System.out.println("[INFO] Usuário para autenticação: " + usuarioLogado);
                
                // Usa o construtor com autenticação
                pagamentoHTTP pagamento = new pagamentoHTTP(
                    nome, 
                    cpf, 
                    cartao.isEmpty() ? "PIX-" + cpf : cartao, 
                    valor, 
                    tipoPagamento, 
                    API_URL,
                    usuarioLogado,
                    senhaUsuario
                );
                
                String resposta = pagamento.conecta();
                int codigoHTTP = pagamento.codretorno;

                System.out.println("\n[OK] Conexão realizada com sucesso!");
                System.out.println("[INFO] Código HTTP: " + codigoHTTP);
                System.out.println("[INFO] Resposta da API (raw): " + resposta);

                // Processa resposta JSON
                String mensagemProcessada = processarRespostaJSON(resposta, tipoPagamento);
                
                // Atualiza UI na thread correta
                SwingUtilities.invokeLater(() -> {
                    btnEmitir.setEnabled(true);
                    txtResultado.setText(mensagemProcessada);
                });

                System.out.println("\n[OK] Pagamento processado!");
                System.out.println("============================================\n");

            } catch (java.net.ConnectException e) {
                SwingUtilities.invokeLater(() -> {
                    btnEmitir.setEnabled(true);
                    txtResultado.setText("Erro de conexão: Servidor não acessível");
                });
                System.err.println("[ERRO] Falha de conexão: " + e.getMessage());
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    btnEmitir.setEnabled(true);
                    txtResultado.setText("Erro ao processar: " + e.getMessage());
                });
                System.err.println("[ERRO] " + e.getMessage());
            }
        }).start();
    }

    /**
     * Processa a resposta da API e gera JSON estruturado
     */
    private String processarRespostaJSON(String resposta, String tipoPagamento) {
        StringBuilder resultado = new StringBuilder();
        
        // Verifica se a resposta indica sucesso
        boolean sucesso = resposta != null && 
            (resposta.toLowerCase().contains("sucesso") || 
             resposta.toLowerCase().contains("realizado") ||
             resposta.toLowerCase().contains("aprovado"));
        
        // Gera um ID de transação único
        String transacaoId = "TXN" + System.currentTimeMillis();
        String valorPagamento = txtValor.getText().trim();
        
        // Cria JSON estruturado para a resposta
        JSONObject jsonResposta = new JSONObject();
        jsonResposta.put("id", transacaoId);
        jsonResposta.put("valor_autorizado", valorPagamento);
        jsonResposta.put("modo", tipoPagamento);
        jsonResposta.put("cod_retorno", sucesso ? "00" : "99");
        jsonResposta.put("msg", resposta);
        jsonResposta.put("status", sucesso ? "APROVADO" : "NEGADO");
        jsonResposta.put("data_hora", new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date()));
        
        String jsonString = jsonResposta.toString();
        
        // Exibe o JSON no terminal
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║         RESPOSTA JSON DA API               ║");
        System.out.println("╠════════════════════════════════════════════╣");
        System.out.println("║ JSON Recebido/Gerado:                      ║");
        System.out.println("╚════════════════════════════════════════════╝");
        System.out.println(jsonResposta.toString(2)); // JSON formatado com indentação
        
        // Tenta processar com trataJSON
        try {
            trataJSON tratador = new trataJSON(jsonString);
            pessoa p = tratador.tratarString();
            
            System.out.println("\n[OK] JSON processado com trataJSON:");
            System.out.println("╔════════════════════════════════════════════╗");
            System.out.println("║       DADOS EXTRAÍDOS DO JSON              ║");
            System.out.println("╠════════════════════════════════════════════╣");
            System.out.println("║ ID Transação:    " + p.getId());
            System.out.println("║ Modo:            " + p.getModo());
            System.out.println("║ Valor Autorizado:" + p.getValor());
            System.out.println("║ Mensagem:        " + p.getRetmsg());
            System.out.println("╚════════════════════════════════════════════╝");
            
            resultado.append("Status: ").append(sucesso ? "APROVADO ✓" : "NEGADO ✗").append("\n");
            resultado.append("ID Transação: ").append(p.getId()).append("\n");
            resultado.append("Modalidade: ").append(p.getModo()).append("\n");
            resultado.append("Valor Autorizado: R$ ").append(p.getValor()).append("\n");
            resultado.append("Mensagem: ").append(p.getRetmsg());
            
        } catch (Exception e) {
            // Fallback se trataJSON falhar
            System.out.println("\n[INFO] Usando JSONObject diretamente:");
            
            for (String key : jsonResposta.keySet()) {
                Object value = jsonResposta.get(key);
                resultado.append(key).append(": ").append(value).append("\n");
                System.out.println("  - " + key + ": " + value);
            }
        }
        
        return resultado.toString();
    }

    /**
     * Imprime o comprovante do pagamento
     */
    private void imprimirComprovante() {
        String dados = txtResultado.getText();
        if (dados.isEmpty() || dados.equals("Processando pagamento...")) {
            JOptionPane.showMessageDialog(this, 
                "Nenhum dado para imprimir.\nProcesse um pagamento primeiro.", 
                "Aviso", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        System.out.println("\n============================================");
        System.out.println("   COMPROVANTE DE PAGAMENTO");
        System.out.println("============================================");
        System.out.println("Operador: " + usuarioLogado);
        System.out.println("Nome: " + txtNome.getText());
        System.out.println("CPF: " + txtCPF.getText());
        System.out.println("Valor: R$ " + txtValor.getText());
        System.out.println("Tipo: " + cmbTipoPagamento.getSelectedItem());
        System.out.println("--------------------------------------------");
        System.out.println(dados);
        System.out.println("============================================\n");
        
        JOptionPane.showMessageDialog(this, 
            "Comprovante enviado para impressão!\n(Verifique o terminal)", 
            "Imprimir", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Limpa todos os campos
     */
    private void limparCampos() {
        txtNome.setText("");
        txtTelefone.setText("");
        txtCPF.setText("");
        txtCartao.setText("");
        txtValor.setText("");
        txtResultado.setText("");
        cmbTipoPagamento.setSelectedIndex(0);
        
        System.out.println("[INFO] Campos limpos");
    }

    /**
     * Volta para a tela de login (logout)
     */
    private void voltarParaLogin() {
        int opcao = JOptionPane.showConfirmDialog(this,
            "Deseja realmente sair?",
            "Confirmar Saída",
            JOptionPane.YES_NO_OPTION);
            
        if (opcao == JOptionPane.YES_OPTION) {
            System.out.println("\n============================================");
            System.out.println("[INFO] Usuário " + usuarioLogado + " desconectado");
            System.out.println("[INFO] Voltando para tela de login...");
            System.out.println("============================================\n");
            
            this.dispose();
            if (telaLogin != null) {
                telaLogin.mostrarLogin();
            } else {
                System.exit(0);
            }
        }
    }
}
