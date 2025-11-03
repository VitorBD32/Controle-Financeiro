package controle.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import controle.Conexao;
import controle.config.APIConfig;
import controle.model.Transacao;
import controle.util.HttpSyncUtil;

/**
 * Implementação do DAO de Transação com operação de sincronização para a API
 * externa.
 */
public class TransacaoDAOImpl implements TransacaoDAO {

    @Override
    public Transacao insert(Transacao t) throws Exception {
        String sql = "INSERT INTO transacoes (usuario_id, categoria_id, tipo, valor, data, descricao) VALUES (?,?,?,?,?,?)";
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, t.getIdUsuario());
            ps.setInt(2, t.getIdCategoria());
            ps.setString(3, t.getTipo());
            ps.setBigDecimal(4, t.getValor());
            if (t.getData() != null) {
                ps.setTimestamp(5, Timestamp.valueOf(t.getData()));
            } else {
                ps.setTimestamp(5, null);
            }
            ps.setString(6, t.getDescricao());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    t.setId(rs.getInt(1));
                }
            }
            return t;
        }
    }

    /**
     * Retorna transações que ainda não foram sincronizadas (sincronizado = 0).
     * Se a coluna não existir, lança exceção para que o chamador trate o
     * fallback.
     */
    public List<Transacao> findUnsynced() throws Exception {
        String sql = "SELECT id, usuario_id, categoria_id, tipo, valor, data, descricao FROM transacoes WHERE sincronizado = 0 ORDER BY data DESC";
        List<Transacao> list = new ArrayList<>();
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(toTransacao(rs));
            }
        }
        return list;
    }

    /**
     * Marca a transação como sincronizada (sincronizado = 1). Lança exceção se
     * falhar.
     */
    public boolean markAsSynced(int id) throws Exception {
        String sql = "UPDATE transacoes SET sincronizado = 1 WHERE id = ?";
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Transacao findById(int id) throws Exception {
        String sql = "SELECT id, usuario_id, categoria_id, tipo, valor, data, descricao FROM transacoes WHERE id = ?";
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Transacao t = toTransacao(rs);
                    return t;
                }
            }
        }
        return null;
    }

    @Override
    public List<Transacao> findAll() throws Exception {
        String sql = "SELECT id, usuario_id, categoria_id, tipo, valor, data, descricao FROM transacoes ORDER BY data DESC";
        List<Transacao> list = new ArrayList<>();
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(toTransacao(rs));
            }
        }
        return list;
    }

    @Override
    public List<Transacao> findByPeriodo(LocalDate inicio, LocalDate fim) throws Exception {
        String sql = "SELECT id, usuario_id, categoria_id, tipo, valor, data, descricao FROM transacoes WHERE data BETWEEN ? AND ? ORDER BY data DESC";
        List<Transacao> list = new ArrayList<>();
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            Timestamp tInicio = inicio != null ? Timestamp.valueOf(inicio.atStartOfDay()) : null;
            Timestamp tFim = fim != null ? Timestamp.valueOf(fim.plusDays(1).atStartOfDay().minusNanos(1)) : null;
            if (tInicio != null) {
                ps.setTimestamp(1, tInicio);
            } else {
                ps.setTimestamp(1, null);
            }
            if (tFim != null) {
                ps.setTimestamp(2, tFim);
            } else {
                ps.setTimestamp(2, null);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(toTransacao(rs));
                }
            }
        }
        return list;
    }

    @Override
    public boolean update(Transacao t) throws Exception {
        String sql = "UPDATE transacoes SET usuario_id=?, categoria_id=?, tipo=?, valor=?, data=?, descricao=? WHERE id=?";
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, t.getIdUsuario());
            ps.setInt(2, t.getIdCategoria());
            ps.setString(3, t.getTipo());
            ps.setBigDecimal(4, t.getValor());
            if (t.getData() != null) {
                ps.setTimestamp(5, Timestamp.valueOf(t.getData()));
            } else {
                ps.setTimestamp(5, null);
            }
            ps.setString(6, t.getDescricao());
            ps.setInt(7, t.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws Exception {
        String sql = "DELETE FROM transacoes WHERE id = ?";
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Transacao toTransacao(ResultSet rs) throws Exception {
        Transacao t = new Transacao();
        t.setId(rs.getInt("id"));
        t.setIdUsuario(rs.getInt("usuario_id"));
        t.setIdCategoria(rs.getInt("categoria_id"));
        t.setTipo(rs.getString("tipo"));
        t.setValor(rs.getBigDecimal("valor"));
        Timestamp ts = rs.getTimestamp("data");
        if (ts != null) {
            t.setData(ts.toLocalDateTime());
        } else {
            t.setData((LocalDateTime) null);
        }
        t.setDescricao(rs.getString("descricao"));
        return t;
    }

    /**
     * Sincroniza transações com a API externa; retorna resumo das operações.
     * Observação: a interface TransacaoDAO não declara esse método — é
     * específico da implementação.
     */
    public String syncToAPI() {
        // tenta garantir que a coluna 'sincronizado' exista no banco;
        // se não for possível criar, fallback será usado posteriormente
        try {
            ensureSyncColumnExists();
        } catch (Exception e) {
            // log e continua: findUnsynced() pode falhar e o código fará fallback
            System.out.println("[TransacaoDAOImpl] Aviso: não foi possível garantir coluna 'sincronizado': " + e.getMessage());
        }

        List<Transacao> transacoes;
        boolean canMarkSynced = true;
        try {
            // tenta obter apenas as não-sincronizadas, se a coluna existir
            transacoes = findUnsynced();
        } catch (Exception e) {
            // Se a coluna 'sincronizado' não existir, cair para comportamento antigo
            canMarkSynced = false;
            try {
                transacoes = findAll();
            } catch (Exception ex) {
                return "Erro ao obter transacoes: " + ex.getMessage();
            }
        }

        StringBuilder summary = new StringBuilder();
        int success = 0;
        int failed = 0;

        if (transacoes == null || transacoes.isEmpty()) {
            return "Nenhuma transação encontrada para sincronizar.\n" + String.format("Sucesso: %d, Falhas: %d\n", success, failed);
        }

        for (Transacao t : transacoes) {
            try {
                String dataStr = t.getData() != null ? t.getData().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "";
                String descricao = t.getDescricao() != null ? t.getDescricao().replace("\"", "\\\"") : "";
                String valorStr = t.getValor() != null ? t.getValor().toPlainString() : "0";

                // Incluir credenciais no JSON (nome e senha do config)
                // O servidor espera "nome" (não "login") conforme estrutura: id, nome, email, senha
                String nome = APIConfig.getAuthUser() != null ? APIConfig.getAuthUser() : "";
                String senha = APIConfig.getAuthPassword() != null ? APIConfig.getAuthPassword() : "";

                String jsonData = String.format("{\"nome\":\"%s\",\"senha\":\"%s\",\"id\":%d,\"tipo\":\"%s\",\"valor\":%s,\"data\":\"%s\",\"descricao\":\"%s\"}",
                        nome, senha, t.getId(), t.getTipo(), valorStr, dataStr, descricao);

                java.util.List<String> urls = APIConfig.getSyncUrls();
                String response = null;
                Exception lastEx = null;
                for (String u : urls) {
                    try {
                        System.out.println("[TransacaoDAOImpl] Trying sync URL: " + u + " for transacao id=" + t.getId());
                        // tenta plain form com fallback para encrypted internamente
                        response = HttpSyncUtil.sendWithPlainFallback(u, jsonData, APIConfig.getSyncSecret());
                        break; // success
                    } catch (Exception e) {
                        lastEx = e;
                        System.out.println("[TransacaoDAOImpl] Failed on url=" + u + " -> " + e.getMessage());
                        // try next URL
                    }
                }

                if (response != null) {
                    summary.append("OK:").append(t.getId()).append(" ").append(response).append("\n");
                    success++;
                    // tenta marcar como sincronizado se possível
                    if (canMarkSynced) {
                        try {
                            markAsSynced(t.getId());
                        } catch (Exception me) {
                            summary.append("WARN: nao foi possivel marcar transacao " + t.getId() + " como sincronizada: " + me.getMessage() + "\n");
                        }
                    }
                } else {
                    String errMsg = lastEx != null ? lastEx.getMessage() : "Unknown error";
                    summary.append("ERR:").append(t.getId()).append(" Exception: ").append(errMsg).append("\n");
                    failed++;
                }
                // TODO: Marcar como synced no DB se existir coluna apropriada (idempotência)
            } catch (Exception ex) {
                summary.append("ERR:").append(t.getId()).append(" ").append(ex.getClass().getSimpleName()).append(": ").append(ex.getMessage()).append("\n");
                failed++;
            }
        }

        summary.insert(0, String.format("Sincronizacao finalizada. Sucesso: %d, Falhas: %d\n", success, failed));
        return summary.toString();
    }

    /**
     * Garante que a coluna 'sincronizado' exista na tabela transacoes. Se a
     * coluna já existir, ignora o erro e retorna normalmente.
     */
    private void ensureSyncColumnExists() throws Exception {
        String sql = "ALTER TABLE transacoes ADD COLUMN sincronizado TINYINT(1) DEFAULT 0";
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
            System.out.println("[TransacaoDAOImpl] Coluna 'sincronizado' criada com sucesso.");
        } catch (java.sql.SQLException sqle) {
            String msg = sqle.getMessage() != null ? sqle.getMessage().toLowerCase() : "";
            // MySQL message when column exists: "Duplicate column name 'sincronizado'"
            if (msg.contains("duplicate column") || msg.contains("já existe") || msg.contains("already exists")) {
                // já existe, ignore
                return;
            }
            // outras exceções repassam para o chamador
            throw sqle;
        }
    }

}
