package controle.ui;

import controle.api.APIIntegration;
import controle.dao.TransacaoDAO;
import controle.dao.TransacaoDAOImpl;
import controle.model.Transacao;
import br.uespi.tratajson.JSONObject;
import br.uespi.tratajson.JSONArray;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.print.*;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

/**
 * Tela para testar e visualizar conexões com a API
 * Mostra os resultados JSON das requisições
 * INCLUI: Botões de Gerar Nota Fiscal, Imprimir e Salvar JSON
 */
public class TelaAPIConexao extends JFrame {

    private JTextField txtUrl;
    private JTextField txtUsuario;
    private JPasswordField txtSenha;
    private JTextArea txtResultado;
    private JTextArea txtJsonFormatado;
    private JButton btnTestarConexao;
    private JButton btnSincronizar;
    private JButton btnConsultar;
    private JButton btnGerarNota;
    private JButton btnImprimir;
    private JButton btnSalvarJson;
    private JLabel lblStatus;
    private TransacaoDAO transacaoDAO;
    private String ultimoJsonGerado = "";

    public TelaAPIConexao() {
        super("Conexão API - Controle Financeiro");
        transacaoDAO = new TransacaoDAOImpl();
        initComponents();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Painel de configuração
        JPanel panelConfig = new JPanel(new GridBagLayout());
        panelConfig.setBorder(new TitledBorder("Configuração da API"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // URL
        gbc.gridx = 0; gbc.gridy = 0;
        panelConfig.add(new JLabel("URL da API:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        txtUrl = new JTextField("http://www.datse.com.br/dev/syncjava.php", 40);
        panelConfig.add(txtUrl, gbc);

        // Usuário
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panelConfig.add(new JLabel("Usuário:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        txtUsuario = new JTextField("JOAO", 20);
        panelConfig.add(txtUsuario, gbc);

        // Senha
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panelConfig.add(new JLabel("Senha:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        txtSenha = new JPasswordField("1234", 20);
        panelConfig.add(txtSenha, gbc);

        // Botões
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnTestarConexao = new JButton("🔗 Testar Conexão");
        btnSincronizar = new JButton("🔄 Sincronizar Transações");
        btnConsultar = new JButton("🔍 Consultar API");
        btnGerarNota = new JButton("🧾 Gerar Nota Fiscal");
        btnImprimir = new JButton("🖨️ Imprimir");
        btnSalvarJson = new JButton("💾 Salvar JSON");
        
        btnTestarConexao.addActionListener(this::testarConexao);
        btnSincronizar.addActionListener(this::sincronizarTransacoes);
        btnConsultar.addActionListener(this::consultarAPI);
        btnGerarNota.addActionListener(this::gerarNotaFiscal);
        btnImprimir.addActionListener(this::imprimirNotaFiscal);
        btnSalvarJson.addActionListener(this::salvarJsonArquivo);

        panelBotoes.add(btnTestarConexao);
        panelBotoes.add(btnSincronizar);
        panelBotoes.add(btnConsultar);
        panelBotoes.add(Box.createHorizontalStrut(20));
        panelBotoes.add(btnGerarNota);
        panelBotoes.add(btnImprimir);
        panelBotoes.add(btnSalvarJson);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panelConfig.add(panelBotoes, gbc);

        // Status
        lblStatus = new JLabel("Pronto");
        lblStatus.setForeground(Color.BLUE);
        gbc.gridy = 4;
        panelConfig.add(lblStatus, gbc);

        add(panelConfig, BorderLayout.NORTH);

        // Painel de resultados (split)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.5);

        // Resultado RAW
        JPanel panelRaw = new JPanel(new BorderLayout());
        panelRaw.setBorder(new TitledBorder("Resposta RAW"));
        txtResultado = new JTextArea();
        txtResultado.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtResultado.setEditable(false);
        panelRaw.add(new JScrollPane(txtResultado), BorderLayout.CENTER);

        // JSON Formatado
        JPanel panelJson = new JPanel(new BorderLayout());
        panelJson.setBorder(new TitledBorder("JSON Formatado"));
        txtJsonFormatado = new JTextArea();
        txtJsonFormatado.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtJsonFormatado.setEditable(false);
        txtJsonFormatado.setForeground(new Color(0, 100, 0));
        panelJson.add(new JScrollPane(txtJsonFormatado), BorderLayout.CENTER);

        splitPane.setLeftComponent(panelRaw);
        splitPane.setRightComponent(panelJson);

        add(splitPane, BorderLayout.CENTER);

        // Painel inferior com log
        JPanel panelLog = new JPanel(new BorderLayout());
        panelLog.setBorder(new TitledBorder("Log de Operações"));
        JTextArea txtLog = new JTextArea(5, 50);
        txtLog.setFont(new Font("Monospaced", Font.PLAIN, 11));
        txtLog.setEditable(false);
        panelLog.add(new JScrollPane(txtLog), BorderLayout.CENTER);

        add(panelLog, BorderLayout.SOUTH);
    }

    private void testarConexao(ActionEvent e) {
        lblStatus.setText("Conectando...");
        lblStatus.setForeground(Color.ORANGE);
        
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            String jsonFormatado = "";
            
            @Override
            protected String doInBackground() throws Exception {
                APIIntegration api = new APIIntegration(
                    txtUrl.getText(),
                    txtUsuario.getText(),
                    new String(txtSenha.getPassword())
                );
                
                String resultado = api.testarConexao();
                
                // Usa JSON filtrado da API
                jsonFormatado = api.getLastJsonFormatado();
                if (jsonFormatado == null || jsonFormatado.isEmpty()) {
                    try {
                        String resposta = api.getLastResponse();
                        if (resposta != null && !resposta.isEmpty()) {
                            if (resposta.trim().startsWith("[")) {
                                JSONArray arr = new JSONArray(resposta);
                                jsonFormatado = arr.toString(2);
                            } else if (resposta.trim().startsWith("{")) {
                                JSONObject obj = new JSONObject(resposta);
                                jsonFormatado = obj.toString(2);
                            }
                        }
                    } catch (Exception ex) {
                        jsonFormatado = "Não foi possível parsear JSON: " + ex.getMessage();
                    }
                }
                
                return resultado;
            }

            @Override
            protected void done() {
                try {
                    String resultado = get();
                    txtResultado.setText(resultado);
                    txtJsonFormatado.setText(jsonFormatado);
                    ultimoJsonGerado = jsonFormatado;
                    lblStatus.setText("Conexão concluída");
                    lblStatus.setForeground(Color.GREEN);
                } catch (Exception ex) {
                    txtResultado.setText("ERRO: " + ex.getMessage());
                    lblStatus.setText("Erro na conexão");
                    lblStatus.setForeground(Color.RED);
                }
            }
        };
        worker.execute();
    }

    private void sincronizarTransacoes(ActionEvent e) {
        lblStatus.setText("Sincronizando...");
        lblStatus.setForeground(Color.ORANGE);

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            String jsonFormatado = "";

            @Override
            protected String doInBackground() throws Exception {
                StringBuilder resultado = new StringBuilder();
                resultado.append("=== SINCRONIZAÇÃO DE TRANSAÇÕES ===\n\n");

                try {
                    String nomeUsuario = txtUsuario.getText().trim();
                    
                    // Busca o ID do usuário pelo nome no banco
                    int usuarioId = -1;
                    try {
                        controle.dao.UsuarioDAO usuarioDAO = new controle.dao.UsuarioDAOImpl();
                        var usuarios = usuarioDAO.findAll();
                        for (var u : usuarios) {
                            if (u.getNome().equalsIgnoreCase(nomeUsuario)) {
                                usuarioId = u.getId();
                                break;
                            }
                        }
                    } catch (Exception ex) {
                        resultado.append("⚠️ Erro ao buscar usuário: ").append(ex.getMessage()).append("\n");
                    }
                    
                    if (usuarioId <= 0) {
                        resultado.append("❌ Usuário '").append(nomeUsuario).append("' não encontrado no banco!\n");
                        resultado.append("Verifique se o nome está correto.\n");
                        return resultado.toString();
                    }
                    
                    resultado.append("👤 Usuário: ").append(nomeUsuario).append(" (ID: ").append(usuarioId).append(")\n\n");
                    
                    // Busca APENAS transações do usuário logado
                    List<Transacao> todasTransacoes = transacaoDAO.findAll();
                    List<Transacao> transacoesDoUsuario = new java.util.ArrayList<>();
                    
                    for (Transacao t : todasTransacoes) {
                        if (t.getIdUsuario() == usuarioId) {
                            transacoesDoUsuario.add(t);
                        }
                    }
                    
                    resultado.append("📊 Total no banco: ").append(todasTransacoes.size()).append("\n");
                    resultado.append("📋 Transações do usuário ").append(nomeUsuario).append(": ").append(transacoesDoUsuario.size()).append("\n\n");

                    if (transacoesDoUsuario.isEmpty()) {
                        resultado.append("Nenhuma transação encontrada para o usuário ").append(nomeUsuario).append(".\n");
                        return resultado.toString();
                    }

                    APIIntegration api = new APIIntegration(
                        txtUrl.getText(),
                        txtUsuario.getText(),
                        new String(txtSenha.getPassword())
                    );
                    api.setAuthUsuarioId(usuarioId);

                    // Monta JSON apenas com as transações DO USUÁRIO
                    JSONArray jsonArray = new JSONArray();
                    for (Transacao t : transacoesDoUsuario) {
                        JSONObject jsonT = new JSONObject();
                        jsonT.put("id", t.getId());
                        jsonT.put("nome", nomeUsuario);
                        jsonT.put("tipo", t.getTipo());
                        jsonT.put("valor", t.getValor() != null ? t.getValor().toString() : "0");
                        jsonT.put("descricao", t.getDescricao() != null ? t.getDescricao() : "");
                        jsonT.put("usuario_id", t.getIdUsuario());
                        jsonT.put("categoria_id", t.getIdCategoria());
                        if (t.getData() != null) {
                            jsonT.put("data", t.getData().toString());
                        }
                        jsonArray.put(jsonT);
                    }

                    jsonFormatado = jsonArray.toString(2);

                    // Sincroniza apenas as transações do usuário
                    resultado.append(api.sincronizarTransacoes(transacoesDoUsuario));

                } catch (Exception ex) {
                    resultado.append("ERRO: ").append(ex.getMessage()).append("\n");
                    ex.printStackTrace();
                }

                return resultado.toString();
            }

            @Override
            protected void done() {
                try {
                    String resultado = get();
                    txtResultado.setText(resultado);
                    txtJsonFormatado.setText(jsonFormatado);
                    ultimoJsonGerado = jsonFormatado;
                    lblStatus.setText("Sincronização concluída");
                    lblStatus.setForeground(Color.GREEN);
                } catch (Exception ex) {
                    txtResultado.setText("ERRO: " + ex.getMessage());
                    lblStatus.setText("Erro na sincronização");
                    lblStatus.setForeground(Color.RED);
                }
            }
        };
        worker.execute();
    }

    private void consultarAPI(ActionEvent e) {
        String endpoint = JOptionPane.showInputDialog(this, 
            "Digite o endpoint para consulta (deixe vazio para raiz):", 
            "Consulta API", 
            JOptionPane.QUESTION_MESSAGE);

        if (endpoint == null) return;

        lblStatus.setText("Consultando...");
        lblStatus.setForeground(Color.ORANGE);

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            String jsonFormatado = "";

            @Override
            protected String doInBackground() throws Exception {
                APIIntegration api = new APIIntegration(
                    txtUrl.getText(),
                    txtUsuario.getText(),
                    new String(txtSenha.getPassword())
                );

                String resultado = api.consultarAPI(endpoint);
                jsonFormatado = api.getLastJsonFormatado();
                if (jsonFormatado == null || jsonFormatado.isEmpty()) {
                    try {
                        String resposta = api.getLastResponse();
                        if (resposta != null && !resposta.isEmpty()) {
                            if (resposta.trim().startsWith("[")) {
                                JSONArray arr = new JSONArray(resposta);
                                jsonFormatado = arr.toString(2);
                            } else if (resposta.trim().startsWith("{")) {
                                JSONObject obj = new JSONObject(resposta);
                                jsonFormatado = obj.toString(2);
                            }
                        }
                    } catch (Exception ex) {
                        jsonFormatado = "Erro ao parsear: " + ex.getMessage();
                    }
                }

                return resultado;
            }

            @Override
            protected void done() {
                try {
                    String resultado = get();
                    txtResultado.setText(resultado);
                    txtJsonFormatado.setText(jsonFormatado);
                    ultimoJsonGerado = jsonFormatado;
                    lblStatus.setText("Consulta concluída");
                    lblStatus.setForeground(Color.GREEN);
                } catch (Exception ex) {
                    txtResultado.setText("ERRO: " + ex.getMessage());
                    lblStatus.setText("Erro na consulta");
                    lblStatus.setForeground(Color.RED);
                }
            }
        };
        worker.execute();
    }

    /**
     * Gera a Nota Fiscal JSON a partir das transações do banco
     */
    private void gerarNotaFiscal(ActionEvent e) {
        lblStatus.setText("Gerando Nota Fiscal...");
        lblStatus.setForeground(Color.ORANGE);

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            String jsonFormatado = "";

            @Override
            protected String doInBackground() throws Exception {
                APIIntegration api = new APIIntegration(
                    txtUrl.getText(),
                    txtUsuario.getText(),
                    new String(txtSenha.getPassword())
                );

                String resultado = api.gerarNotaFiscal();
                jsonFormatado = api.getLastJsonFormatado();

                return resultado;
            }

            @Override
            protected void done() {
                try {
                    String resultado = get();
                    txtResultado.setText(resultado);
                    txtJsonFormatado.setText(jsonFormatado);
                    ultimoJsonGerado = jsonFormatado;
                    lblStatus.setText("Nota Fiscal gerada!");
                    lblStatus.setForeground(new Color(0, 150, 0));
                } catch (Exception ex) {
                    txtResultado.setText("ERRO: " + ex.getMessage());
                    lblStatus.setText("Erro ao gerar nota");
                    lblStatus.setForeground(Color.RED);
                }
            }
        };
        worker.execute();
    }

    /**
     * Imprime a Nota Fiscal exibida na tela
     */
    private void imprimirNotaFiscal(ActionEvent e) {
        String conteudo = txtJsonFormatado.getText();
        if (conteudo == null || conteudo.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Nenhum conteúdo para imprimir!\nGere uma nota fiscal primeiro.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        PrinterJob printerJob = PrinterJob.getPrinterJob();
        printerJob.setJobName("Nota Fiscal - Controle Financeiro");

        printerJob.setPrintable(new Printable() {
            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                if (pageIndex > 0) {
                    return Printable.NO_SUCH_PAGE;
                }

                Graphics2D g2d = (Graphics2D) graphics;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

                // Configura fonte
                Font font = new Font("Monospaced", Font.PLAIN, 10);
                g2d.setFont(font);

                // Desenha título
                Font titleFont = new Font("SansSerif", Font.BOLD, 14);
                g2d.setFont(titleFont);
                g2d.drawString("NOTA FISCAL - CONTROLE FINANCEIRO", 50, 30);
                g2d.drawString("Usuário: " + txtUsuario.getText(), 50, 50);

                // Volta para fonte normal
                g2d.setFont(font);
                
                // Desenha linha separadora
                g2d.drawLine(50, 60, (int) pageFormat.getImageableWidth() - 50, 60);

                // Desenha conteúdo JSON
                String[] linhas = conteudo.split("\n");
                int y = 80;
                int lineHeight = 12;
                int maxY = (int) pageFormat.getImageableHeight() - 50;

                for (String linha : linhas) {
                    if (y > maxY) break;
                    g2d.drawString(linha.length() > 80 ? linha.substring(0, 80) + "..." : linha, 50, y);
                    y += lineHeight;
                }

                return Printable.PAGE_EXISTS;
            }
        });

        // Mostra diálogo de impressão
        if (printerJob.printDialog()) {
            try {
                printerJob.print();
                JOptionPane.showMessageDialog(this, 
                    "Nota fiscal enviada para impressão!",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                
                // Log no terminal
                System.out.println("\n" + "=".repeat(60));
                System.out.println("🖨️ NOTA FISCAL ENVIADA PARA IMPRESSÃO");
                System.out.println("=".repeat(60) + "\n");
                
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Erro ao imprimir: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Salva o JSON em um arquivo
     */
    private void salvarJsonArquivo(ActionEvent e) {
        String conteudo = txtJsonFormatado.getText();
        if (conteudo == null || conteudo.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Nenhum JSON para salvar!\nFaça uma consulta ou gere uma nota fiscal primeiro.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar JSON");
        fileChooser.setSelectedFile(new File("nota_fiscal_" + System.currentTimeMillis() + ".json"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivos JSON (*.json)", "json"));

        int resultado = fileChooser.showSaveDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File arquivo = fileChooser.getSelectedFile();
            
            // Adiciona extensão .json se não tiver
            if (!arquivo.getName().endsWith(".json")) {
                arquivo = new File(arquivo.getAbsolutePath() + ".json");
            }

            try (FileWriter writer = new FileWriter(arquivo)) {
                writer.write(conteudo);
                JOptionPane.showMessageDialog(this, 
                    "JSON salvo com sucesso em:\n" + arquivo.getAbsolutePath(),
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                
                // Log no terminal
                System.out.println("\n" + "=".repeat(60));
                System.out.println("💾 JSON SALVO EM: " + arquivo.getAbsolutePath());
                System.out.println("=".repeat(60) + "\n");
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Erro ao salvar arquivo: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // usa default
            }
            new TelaAPIConexao().setVisible(true);
        });
    }
}
