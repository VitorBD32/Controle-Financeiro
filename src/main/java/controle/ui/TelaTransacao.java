package controle.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.NumberFormatter;

import controle.dao.CategoriaDAO;
import controle.dao.CategoriaDAOImpl;
import controle.dao.TransacaoDAO;
import controle.dao.TransacaoDAOImpl;
import controle.dao.UsuarioDAO;
import controle.dao.UsuarioDAOImpl;
import controle.model.Categoria;
import controle.model.Transacao;
import controle.model.Usuario;

public class TelaTransacao extends JFrame {

    private final TransacaoDAO transDao = new TransacaoDAOImpl();
    private final CategoriaDAO catDao = new CategoriaDAOImpl();
    private final UsuarioDAO userDao = new UsuarioDAOImpl();

    private final DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Data", "Tipo", "Categoria", "Usuário", "Descrição", "Valor"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(model);

    private final JComboBox<Usuario> cbUsuario = new JComboBox<>();
    private final JComboBox<Categoria> cbCategoria = new JComboBox<>();
    private final JComboBox<String> cbTipo = new JComboBox<>(new String[]{"D", "C"});
    private final JFormattedTextField tfValor;
    private final JSpinner spData;
    private final JFormattedTextField tfDescricao = new JFormattedTextField();
    private final JButton btnSync = new JButton("Sincronizar");

    public TelaTransacao() {
        super("Transações");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        // form
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0;
        c.gridy = 0;
        form.add(new JLabel("Usuário:"), c);
        c.gridx = 1;
        form.add(cbUsuario, c);
        c.gridx = 0;
        c.gridy = 1;
        form.add(new JLabel("Categoria:"), c);
        c.gridx = 1;
        form.add(cbCategoria, c);
        // botão de nova categoria ao lado do combo
        JButton btnNovaCategoria = new JButton("Nova Categoria");
        c.gridx = 2;
        form.add(btnNovaCategoria, c);
        c.gridx = 0;
        c.gridy = 2;
        form.add(new JLabel("Tipo (D/C):"), c);
        c.gridx = 1;
        form.add(cbTipo, c);

        NumberFormatter nf = new NumberFormatter(java.text.NumberFormat.getNumberInstance());
        nf.setValueClass(BigDecimal.class);
        nf.setAllowsInvalid(false);
        tfValor = new JFormattedTextField(nf);
        tfValor.setColumns(10);

        c.gridx = 0;
        c.gridy = 3;
        form.add(new JLabel("Valor:"), c);
        c.gridx = 1;
        form.add(tfValor, c);

        spData = new JSpinner(new SpinnerDateModel());
        c.gridx = 0;
        c.gridy = 4;
        form.add(new JLabel("Data:"), c);
        c.gridx = 1;
        form.add(spData, c);

        tfDescricao.setColumns(20);
        c.gridx = 0;
        c.gridy = 5;
        form.add(new JLabel("Descrição:"), c);
        c.gridx = 1;
        form.add(tfDescricao, c);

        add(form, BorderLayout.NORTH);

        // table
        add(new JScrollPane(table), BorderLayout.CENTER);

        // buttons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnNovo = new JButton("Novo");
        JButton btnSalvar = new JButton("Salvar");
        JButton btnExcluir = new JButton("Excluir");
        JButton btnRefresh = new JButton("Refresh");
        actions.add(btnNovo);
        actions.add(btnSalvar);
        actions.add(btnExcluir);
        actions.add(btnRefresh);
        actions.add(btnSync);
        add(actions, BorderLayout.SOUTH);

        // actions
        btnRefresh.addActionListener(e -> loadData());
        btnNovo.addActionListener(e -> createUserQuick());
        btnSalvar.addActionListener(e -> save());
        btnExcluir.addActionListener(e -> deleteSelected());
        btnNovaCategoria.addActionListener(e -> createCategoria());
        btnSync.addActionListener(e -> syncData());

        loadCombos();
        loadData();

        setSize(900, 500);
        setLocationRelativeTo(null);
    }

    private void loadCombos() {
        try {
            cbUsuario.removeAllItems();
            for (Usuario u : userDao.findAll()) {
                cbUsuario.addItem(u);
            }
            cbCategoria.removeAllItems();
            for (Categoria c : catDao.findAll()) {
                cbCategoria.addItem(c);
            }
        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Erro ao carregar combos: " + ex.getMessage());
        }
    }

    private void loadData() {
        try {
            model.setRowCount(0);
            List<Transacao> list = transDao.findAll();
            BigDecimal total = BigDecimal.ZERO;
            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (Transacao t : list) {
                String tipo = t.getTipo();
                BigDecimal v = t.getValor();
                if ("D".equalsIgnoreCase(tipo)) {
                    total = total.subtract(v);
                } else {
                    total = total.add(v);
                }
                // tentar exibir o nome da categoria ao invés do id
                String nomeCat = String.valueOf(t.getIdCategoria());
                try {
                    controle.model.Categoria cat = catDao.findById(t.getIdCategoria());
                    if (cat != null) {
                        nomeCat = cat.getNome();
                    }
                } catch (Exception ex) {
                    // se falhar, exibe o id
                }
                // obter nome do usuário associado (não armazenamos nome na tabela transacoes)
                String usuarioNome = String.valueOf(t.getIdUsuario());
                try {
                    controle.model.Usuario uu = userDao.findById(t.getIdUsuario());
                    if (uu != null && uu.getNome() != null && !uu.getNome().isEmpty()) {
                        usuarioNome = uu.getNome();
                    }
                } catch (Exception ex) {
                    // se falhar, manter id como fallback
                }
                String dataDisplay = "";
                if (t.getData() != null) {
                    dataDisplay = t.getData().format(dtf);
                }
                model.addRow(new Object[]{t.getId(), dataDisplay, tipo, nomeCat, usuarioNome, t.getDescricao(), v});
            }
            // status
            setTitle("Transações — Total: " + total);
        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Erro ao carregar transações: " + ex.getMessage());
        }
    }

    private void clearForm() {
        // clear selections safely
        cbUsuario.setSelectedItem(null);
        cbCategoria.setSelectedItem(null);
        cbTipo.setSelectedIndex(0);
        tfValor.setValue(null);
        tfDescricao.setText("");
    }

    private void createCategoria() {
        // show small dialog to collect name, optional value and description
        javax.swing.JTextField nomeField = new javax.swing.JTextField();
        // trocar campo de valor por seleção de tipo (D/C) conforme schema de categorias
        javax.swing.JComboBox<String> tipoCombo = new javax.swing.JComboBox<>(new String[]{"D", "C"});
        javax.swing.JTextField descField = new javax.swing.JTextField();

        Object[] inputs = new Object[]{
            "Nome:", nomeField,
            "Tipo (D/C):", tipoCombo,
            "Descrição (opcional):", descField
        };
        int result = javax.swing.JOptionPane.showConfirmDialog(this, inputs, "Nova Categoria", javax.swing.JOptionPane.OK_CANCEL_OPTION);
        if (result != javax.swing.JOptionPane.OK_OPTION) {
            return;
        }

        String nome = nomeField.getText();
        if (nome == null || nome.trim().isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Nome inválido.");
            return;
        }
        String tipo = null;
        Object tval = tipoCombo.getSelectedItem();
        if (tval != null) {
            tipo = tval.toString().trim().toUpperCase();
        }
        String desc = descField.getText();

        try {
            controle.model.Categoria c = new controle.model.Categoria();
            c.setNome(nome.trim());
            c.setTipo(tipo != null && !tipo.isEmpty() ? tipo : "D");
            c.setDescricao(desc);
            catDao.insert(c);
            loadCombos();
            // select created category
            for (int i = 0; i < cbCategoria.getItemCount(); i++) {
                controle.model.Categoria it = cbCategoria.getItemAt(i);
                if (it != null && it.getNome().equals(nome.trim())) {
                    cbCategoria.setSelectedIndex(i);
                    break;
                }
            }
        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Erro ao criar categoria: " + ex.getMessage());
        }
    }

    private void createUserQuick() {
        // quick dialog to create a user without leaving this screen
        javax.swing.JTextField nomeField = new javax.swing.JTextField();
        javax.swing.JTextField emailField = new javax.swing.JTextField();
        javax.swing.JPasswordField senhaField = new javax.swing.JPasswordField();
        Object[] inputs = new Object[]{
            "Nome:", nomeField,
            "Email:", emailField,
            "Senha:", senhaField
        };
        int res = javax.swing.JOptionPane.showConfirmDialog(this, inputs, "Novo Usuário", javax.swing.JOptionPane.OK_CANCEL_OPTION);
        if (res != javax.swing.JOptionPane.OK_OPTION) {
            return;
        }
        String nome = nomeField.getText() != null ? nomeField.getText().trim() : "";
        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        String senha = new String(senhaField.getPassword()).trim();
        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Preencha nome, email e senha.");
            return;
        }
        try {
            controle.model.Usuario u = new controle.model.Usuario();
            u.setNome(nome);
            u.setEmail(email);
            u.setSenha(senha);
            userDao.insert(u);
            loadCombos();
            // select the created user in the combo (matching by email or id)
            for (int i = 0; i < cbUsuario.getItemCount(); i++) {
                Usuario it = cbUsuario.getItemAt(i);
                if (it != null && it.getEmail() != null && it.getEmail().equalsIgnoreCase(email)) {
                    cbUsuario.setSelectedIndex(i);
                    break;
                }
            }
            javax.swing.JOptionPane.showMessageDialog(this, "Usuário criado com sucesso.");
        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Erro ao criar usuário: " + ex.getMessage());
        }
    }

    private void save() {
        try {
            Usuario u = (Usuario) cbUsuario.getSelectedItem();
            Categoria c = (Categoria) cbCategoria.getSelectedItem();
            Object tipoObj = cbTipo.getSelectedItem();
            String tipo = tipoObj != null ? tipoObj.toString().trim() : null;
            Object val = tfValor.getValue();
            if (u == null || c == null || val == null) {
                javax.swing.JOptionPane.showMessageDialog(this, "Preencha usuário, categoria e valor.");
                return;
            }
            // normalize tipo to a single uppercase char ('D' or 'C') to match DB column
            if (tipo == null || tipo.isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(this, "Selecione o tipo (D/C).");
                return;
            }
            if (tipo.length() > 1) {
                tipo = tipo.substring(0, 1);
            }
            tipo = tipo.toUpperCase();
            if (!"D".equals(tipo) && !"C".equals(tipo)) {
                javax.swing.JOptionPane.showMessageDialog(this, "Tipo inválido. Use 'D' ou 'C'.");
                return;
            }

            BigDecimal valor = null;
            try {
                if (val instanceof BigDecimal) {
                    valor = (BigDecimal) val;
                } else if (val instanceof Number) {
                    valor = BigDecimal.valueOf(((Number) val).doubleValue());
                } else {
                    String s = val.toString().trim();
                    if (s.isEmpty()) {
                        throw new IllegalArgumentException("Valor vazio");
                    }
                    // replace comma with dot for locales that use comma
                    s = s.replace(',', '.');
                    valor = new BigDecimal(s);
                }
            } catch (Exception ex) {
                javax.swing.JOptionPane.showMessageDialog(this, "Valor inválido: " + val);
                return;
            }
            // obtain LocalDateTime from spinner value (SpinnerDateModel stores java.util.Date)
            java.time.LocalDateTime data = null;
            try {
                Object spv = spData.getValue();
                if (spv instanceof java.util.Date) {
                    java.util.Date spinnerDate = (java.util.Date) spv;
                    data = spinnerDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
                }
            } catch (Exception ex) {
                // leave data null if conversion fails
            }
            Transacao t = new Transacao();
            t.setIdUsuario(u.getId());
            t.setIdCategoria(c.getId());
            t.setTipo(tipo);
            t.setValor(valor);
            t.setData(data);
            t.setDescricao(tfDescricao.getText());
            transDao.insert(t);
            loadData();
            clearForm();
        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Erro ao salvar: " + ex.getMessage());
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Selecione uma transação para excluir.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        Integer id = (Integer) model.getValueAt(modelRow, 0);
        int confirm = javax.swing.JOptionPane.showConfirmDialog(this, "Confirma excluir?", "Confirmar", javax.swing.JOptionPane.YES_NO_OPTION);
        if (confirm != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }
        try {
            transDao.delete(id);
            loadData();
        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Erro ao excluir: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TelaTransacao t = new TelaTransacao();
            t.setVisible(true);
        });
    }

    private void syncData() {
        btnSync.setEnabled(false);
        btnSync.setText("Sincronizando...");
        javax.swing.SwingWorker<String, Void> worker = new javax.swing.SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                try {
                    return ((TransacaoDAOImpl) transDao).syncToAPI();
                } catch (Exception ex) {
                    // devolve mensagem amigável para o done()
                    return "Erro durante sincronização: " + ex.getClass().getSimpleName() + ": " + ex.getMessage();
                }
            }

            @Override
            protected void done() {
                try {
                    String result = get();
                    // Se detectar problema de conexão, mostrar instrução útil
                    if (result != null && (result.contains("ConnectException") || result.toLowerCase().contains("connection refused") || result.toLowerCase().contains("não foi possível conectar"))) {
                        String msg = "Não foi possível conectar ao servidor de sincronização.\n"
                                + "Verifique se o servidor de sincronização (http://www.datse.com.br/dev/syncjava2.php) está acessível e se a URL está correta.\n"
                                + "Detalhes: " + result;
                        javax.swing.JOptionPane.showMessageDialog(TelaTransacao.this, msg, "Erro na sincronização", javax.swing.JOptionPane.ERROR_MESSAGE);
                    } else {
                        // Exibir resumo em área rolável para boa usabilidade
                        javax.swing.JTextArea ta = new javax.swing.JTextArea(result != null ? result : "Sincronização concluída");
                        ta.setEditable(false);
                        ta.setLineWrap(true);
                        ta.setWrapStyleWord(true);
                        javax.swing.JScrollPane sp = new javax.swing.JScrollPane(ta);
                        sp.setPreferredSize(new java.awt.Dimension(600, 300));
                        javax.swing.JOptionPane.showMessageDialog(TelaTransacao.this, sp, "Resultado da sincronização", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception ex) {
                    javax.swing.JOptionPane.showMessageDialog(TelaTransacao.this, "Erro inesperado na sincronização: " + ex.getMessage(), "Erro", javax.swing.JOptionPane.ERROR_MESSAGE);
                } finally {
                    btnSync.setEnabled(true);
                    btnSync.setText("Sincronizar");
                    loadData(); // atualizar tabela caso alguma transação tenha sido marcada
                }
            }
        };
        worker.execute();
    }
}
