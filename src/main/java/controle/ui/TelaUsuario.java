package controle.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import controle.dao.UsuarioDAO;
import controle.dao.UsuarioDAOImpl;
import controle.model.Usuario;

public class TelaUsuario extends JFrame {

    private final UsuarioDAO dao = new UsuarioDAOImpl();
    private final DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Nome", "Email"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(model);
    private final JTextField nomeField = new JTextField(20);
    private final JTextField emailField = new JTextField(20);
    private final JPasswordField senhaField = new JPasswordField(20);

    // UI actions/buttons are fields so we can enable/disable them
    private final JButton btnNovo = new JButton("Novo");
    private final JButton btnSalvar = new JButton("Salvar");
    private final JButton btnAtualizar = new JButton("Atualizar");
    private final JButton btnExcluir = new JButton("Excluir");
    private final JButton btnTransacoes = new JButton("Transações");
    private final JLabel lblStatus = new JLabel(" ");

    // simple email validation
    private final Pattern emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public TelaUsuario() {
        super("CRUD - Usuários");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        // topo - formulário
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0;
        c.gridy = 0;
        JLabel lblNome = new JLabel("Nome:");
        lblNome.setLabelFor(nomeField);
        form.add(lblNome, c);
        c.gridx = 1;
        form.add(nomeField, c);

        c.gridx = 0;
        c.gridy = 1;
        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setLabelFor(emailField);
        form.add(lblEmail, c);
        c.gridx = 1;
        form.add(emailField, c);

        c.gridx = 0;
        c.gridy = 2;
        JLabel lblSenha = new JLabel("Senha:");
        lblSenha.setLabelFor(senhaField);
        form.add(lblSenha, c);
        c.gridx = 1;
        form.add(senhaField, c);

        nomeField.setToolTipText("Nome completo do usuário");
        emailField.setToolTipText("Email de contato (ex: user@dominio.com)");
        senhaField.setToolTipText("Senha (ficará mascarada)");

        add(form, BorderLayout.NORTH);

        // centro - tabela com sorter
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(220);
        table.getColumnModel().getColumn(2).setPreferredWidth(260);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // rodapé - botões + status
        JPanel actions = new JPanel();
        actions.add(btnNovo);
        actions.add(btnSalvar);
        actions.add(btnAtualizar);
        actions.add(btnExcluir);
        actions.add(btnTransacoes);

        JPanel south = new JPanel(new BorderLayout());
        south.add(actions, BorderLayout.NORTH);
        south.add(lblStatus, BorderLayout.SOUTH);
        lblStatus.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 6, 6, 6));

        add(south, BorderLayout.SOUTH);

        // initial button states
        btnAtualizar.setEnabled(false);
        btnExcluir.setEnabled(false);

        // listeners
        btnNovo.addActionListener(e -> onNovo());
        btnSalvar.addActionListener(e -> onSalvar());
        btnAtualizar.addActionListener(e -> onAtualizar());
        btnExcluir.addActionListener(e -> onExcluir());
        btnTransacoes.addActionListener(e -> {
            TelaTransacao tt = new TelaTransacao();
            tt.setVisible(true);
        });

        // keyboard shortcuts
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), "save");

        getRootPane().getActionMap().put("save", new AbstractAction() {

            @Override

            public void actionPerformed(ActionEvent e) {

                btnSalvar.doClick();

            }
        });
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK), "novo");
        getRootPane().getActionMap().put("novo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnNovo.doClick();
            }
        });
        // (removed F5 -> refresh mapping)

        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_DOWN_MASK), "transacoes");

        getRootPane().getActionMap().put("transacoes", new AbstractAction() {

            @Override

            public void actionPerformed(ActionEvent e) {

                btnTransacoes.doClick();

            }

        });
        // make Enter trigger save
        getRootPane().setDefaultButton(btnSalvar);

        // table selection -> populate fields and enable actions
        table.getSelectionModel().addListSelectionListener((ListSelectionEvent ev) -> {
            if (!ev.getValueIsAdjusting()) {
                updateFieldsFromSelection();
            }
        });

        // double click to edit (keeps password hidden)
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    updateFieldsFromSelection();
                }
            }
        });

        // popup menu on table
        JPopupMenu popup = new JPopupMenu();
        JMenuItem miEdit = new JMenuItem("Editar");
        miEdit.addActionListener(a -> updateFieldsFromSelection());
        JMenuItem miDelete = new JMenuItem("Excluir");
        miDelete.addActionListener(a -> btnExcluir.doClick());
        popup.add(miEdit);
        popup.add(miDelete);
        table.setComponentPopupMenu(popup);

        setSize(800, 480);
        setLocationRelativeTo(null);
        loadUsers();
    }

    private void updateFieldsFromSelection() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            try {
                // convert view index to model index when sorting is active
                int modelRow = table.convertRowIndexToModel(row);
                Object nameObj = model.getValueAt(modelRow, 1);
                Object emailObj = model.getValueAt(modelRow, 2);
                nomeField.setText(nameObj != null ? nameObj.toString() : "");
                emailField.setText(emailObj != null ? emailObj.toString() : "");
                senhaField.setText(""); // don't populate password
                btnAtualizar.setEnabled(true);
                btnExcluir.setEnabled(true);
                Object idObj = model.getValueAt(modelRow, 0);
                String idText = idObj != null ? idObj.toString() : "?";
                lblStatus.setText("Selecionado ID: " + idText);
            } catch (Exception ex) {
                // protect UI from unexpected model types
                btnAtualizar.setEnabled(false);
                btnExcluir.setEnabled(false);
                lblStatus.setText("Erro ao selecionar linha: " + ex.getMessage());
            }
        } else {
            btnAtualizar.setEnabled(false);
            btnExcluir.setEnabled(false);
            lblStatus.setText("Pronto.");
        }
    }

    private void onNovo() {
        nomeField.setText("");
        emailField.setText("");
        senhaField.setText("");
        table.clearSelection();
        nomeField.requestFocusInWindow();
    }

    private void onSalvar() {
        try {
            String nome = nomeField.getText().trim();
            String email = emailField.getText().trim();
            String senha = new String(senhaField.getPassword());
            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Preencha nome, email e senha.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!emailPattern.matcher(email).matches()) {
                JOptionPane.showMessageDialog(this, "Email inválido.", "Aviso", JOptionPane.WARNING_MESSAGE);
                emailField.requestFocusInWindow();
                return;
            }
            Usuario u = new Usuario();
            u.setNome(nome);
            u.setEmail(email);
            u.setSenha(senha);
            dao.insert(u);
            loadUsers();
            JOptionPane.showMessageDialog(this, "Usuário salvo.");
            onNovo();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onAtualizar() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um usuário para atualizar.");
            return;
        }
        try {
            int modelRow = table.convertRowIndexToModel(row);
            Object idObj = model.getValueAt(modelRow, 0);
            if (!(idObj instanceof Number)) {
                JOptionPane.showMessageDialog(this, "ID do usuário inválido.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int id = ((Number) idObj).intValue();
            Usuario u = new Usuario();
            u.setId(id);
            u.setNome(nomeField.getText().trim());
            u.setEmail(emailField.getText().trim());
            String senha = new String(senhaField.getPassword()).trim();
            // only set password when user provided a new one
            if (!senha.isEmpty()) {
                u.setSenha(senha);
            } else {
                u.setSenha(null);
            }
            if (!emailPattern.matcher(u.getEmail()).matches()) {
                JOptionPane.showMessageDialog(this, "Email inválido.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (dao.update(u)) {
                loadUsers();
                JOptionPane.showMessageDialog(this, "Usuário atualizado.");
            } else {
                JOptionPane.showMessageDialog(this, "Atualização não modificou registros.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onExcluir() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um usuário para excluir.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Confirma excluir o usuário selecionado?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            int modelRow = table.convertRowIndexToModel(row);
            Object idObj = model.getValueAt(modelRow, 0);
            if (!(idObj instanceof Number)) {
                JOptionPane.showMessageDialog(this, "ID do usuário inválido.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int id = ((Number) idObj).intValue();
            if (dao.delete(id)) {
                loadUsers();
                JOptionPane.showMessageDialog(this, "Usuário excluído.");
            } else {
                JOptionPane.showMessageDialog(this, "Exclusão não modificou registros.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao excluir: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadUsers() {
        try {
            model.setRowCount(0);
            List<Usuario> users = dao.findAll();
            for (Usuario u : users) {
                model.addRow(new Object[]{u.getId(), u.getNome(), u.getEmail()});
            }
            lblStatus.setText("Total: " + users.size() + " usuários");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar usuários: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TelaUsuario t = new TelaUsuario();
            t.setVisible(true);
        });
    }
}
