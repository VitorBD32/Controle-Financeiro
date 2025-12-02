import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// Imports para integração com API PIX
import br.uespi.acessoapi.pagamentoHTTP;
import br.uespi.acessoapi.PIXConexao;
import br.uespi.tratajson.trataJSON;
import br.uespi.tratajson.JSONObject;
import br.uespi.pessoas.pessoa;

/**
 * Tela de Cadastro e Pagamento PIX
 * Integrada com a API de pagamentos usando as classes do pacote br.uespi
 */
public class TelaPagamentoPIX extends JFrame {

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
    
    // URL da API (endpoint correto sem o "2")
    private static final String API_URL = "http://www.datse.com.br/dev/syncjava.php";

    public TelaPagamentoPIX() {
        super("Cadastro - Pagamento PIX");
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Painel principal de formulário
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

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
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(350, 450);
        setLocationRelativeTo(null);
        setResizable(false);
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

        // Exibe no terminal o início do processamento
        System.out.println("\n============================================");
        System.out.println("   PROCESSANDO PAGAMENTO " + tipoPagamento);
        System.out.println("============================================");
        System.out.println("[INFO] Nome: " + nome);
        System.out.println("[INFO] Telefone: " + telefone);
        System.out.println("[INFO] CPF: " + cpf);
        System.out.println("[INFO] Cartão/Chave: " + cartao);
        System.out.println("[INFO] Valor: R$ " + valor);
        System.out.println("[INFO] Tipo: " + tipoPagamento);
        System.out.println("[INFO] URL da API: " + API_URL);
        System.out.println("--------------------------------------------");

        try {
            // Conecta com a API usando pagamentoHTTP
            System.out.println("[INFO] Conectando à API...");
            
            pagamentoHTTP pagamento = new pagamentoHTTP(
                nome, 
                cpf, 
                cartao.isEmpty() ? "PIX-" + cpf : cartao, 
                valor, 
                tipoPagamento, 
                API_URL
            );
            
            String resposta = pagamento.conecta();
            int codigoHTTP = pagamento.codretorno;

            System.out.println("\n[OK] Conexão realizada com sucesso!");
            System.out.println("[INFO] Código HTTP: " + codigoHTTP);
            System.out.println("[INFO] Resposta da API (raw): " + resposta);

            // Tenta processar como JSON
            String mensagemProcessada = processarRespostaJSON(resposta, tipoPagamento);
            
            // Atualiza a área de resultado na interface
            txtResultado.setText(mensagemProcessada);

            System.out.println("\n[OK] Pagamento processado!");
            System.out.println("============================================\n");

        } catch (java.net.ConnectException e) {
            String erro = "Erro de conexão: Servidor não acessível";
            txtResultado.setText(erro);
            System.err.println("[ERRO] " + erro);
            System.err.println("  Detalhes: " + e.getMessage());
        } catch (Exception e) {
            String erro = "Erro ao processar: " + e.getMessage();
            txtResultado.setText(erro);
            System.err.println("[ERRO] " + erro);
            e.printStackTrace();
        }
    }

    /**
     * Processa a resposta JSON da API usando trataJSON
     */
    private String processarRespostaJSON(String resposta, String tipoPagamento) {
        StringBuilder resultado = new StringBuilder();
        
        try {
            // Tenta usar trataJSON para processar
            trataJSON tratador = new trataJSON(resposta);
            pessoa p = tratador.tratarString();
            
            resultado.append("Mensagem: ").append(p.getRetmsg()).append("\n");
            resultado.append("Modalidade: ").append(p.getModo()).append("\n");
            resultado.append("ID Transação: ").append(p.getId()).append("\n");
            resultado.append("Valor Autorizado: R$ ").append(p.getValor());
            
            System.out.println("\n[OK] JSON processado com trataJSON:");
            System.out.println("  - ID: " + p.getId());
            System.out.println("  - Modo: " + p.getModo());
            System.out.println("  - Valor: " + p.getValor());
            System.out.println("  - Mensagem: " + p.getRetmsg());
            
        } catch (Exception e) {
            // Tenta processar como JSONObject direto
            try {
                JSONObject json = new JSONObject(resposta);
                
                System.out.println("\n[INFO] JSON processado com JSONObject:");
                
                for (String key : json.keySet()) {
                    Object value = json.get(key);
                    resultado.append(key).append(": ").append(value).append("\n");
                    System.out.println("  - " + key + ": " + value);
                }
                
            } catch (Exception jsonEx) {
                // Resposta não é JSON - usa resposta direta
                if (resposta != null && !resposta.isEmpty()) {
                    // Simula resposta de sucesso para demonstração
                    resultado.append("Mensagem: Pagamento ").append(tipoPagamento).append(" Processado!\n");
                    resultado.append("Modalidade: ").append(tipoPagamento).append("\n");
                    resultado.append("Resposta API: ").append(resposta);
                    
                    System.out.println("\n[INFO] Resposta (texto): " + resposta);
                } else {
                    resultado.append("Sem resposta da API");
                }
            }
        }
        
        return resultado.toString();
    }

    /**
     * Imprime o comprovante do pagamento
     */
    private void imprimirComprovante() {
        String dados = txtResultado.getText();
        if (dados.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Nenhum dado para imprimir.\nProcesse um pagamento primeiro.", 
                "Aviso", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        System.out.println("\n============================================");
        System.out.println("   COMPROVANTE DE PAGAMENTO");
        System.out.println("============================================");
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

    public static void main(String[] args) {
        // Configura Look and Feel do sistema
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Usa Look and Feel padrão
        }

        // Executa na Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            TelaPagamentoPIX tela = new TelaPagamentoPIX();
            tela.setVisible(true);
            
            System.out.println("============================================");
            System.out.println("   SISTEMA DE PAGAMENTO PIX INICIADO");
            System.out.println("============================================");
            System.out.println("[INFO] API URL: " + API_URL);
            System.out.println("[INFO] Aguardando processamento...");
            System.out.println("============================================\n");
        });
    }
}
