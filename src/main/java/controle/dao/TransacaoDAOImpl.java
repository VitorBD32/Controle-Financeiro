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
        String sql = "INSERT INTO transacoes (id_usuario, id_categoria, tipo, valor, data, descricao) VALUES (?,?,?,?,?,?)";
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

    @Override
    public Transacao findById(int id) throws Exception {
        String sql = "SELECT id_transacao, id_usuario, id_categoria, tipo, valor, data, descricao FROM transacoes WHERE id_transacao = ?";
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
        String sql = "SELECT id_transacao, id_usuario, id_categoria, tipo, valor, data, descricao FROM transacoes ORDER BY data DESC";
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
        String sql = "SELECT id_transacao, id_usuario, id_categoria, tipo, valor, data, descricao FROM transacoes WHERE data BETWEEN ? AND ? ORDER BY data DESC";
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
        String sql = "UPDATE transacoes SET id_usuario=?, id_categoria=?, tipo=?, valor=?, data=?, descricao=? WHERE id_transacao=?";
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
        String sql = "DELETE FROM transacoes WHERE id_transacao = ?";
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Transacao toTransacao(ResultSet rs) throws Exception {
        Transacao t = new Transacao();
        t.setId(rs.getInt("id_transacao"));
        t.setIdUsuario(rs.getInt("id_usuario"));
        t.setIdCategoria(rs.getInt("id_categoria"));
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
        List<Transacao> transacoes;
        try {
            transacoes = findAll(); // ideal: filtrar apenas não sincronizadas se existir campo
        } catch (Exception e) {
            return "Erro ao obter transacoes: " + e.getMessage();
        }

        StringBuilder summary = new StringBuilder();
        int success = 0;
        int failed = 0;

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

                String payload = HttpSyncUtil.buildEncryptedPayload(jsonData, APIConfig.getSyncSecret());
                java.util.List<String> urls = APIConfig.getSyncUrls();
                String response = null;
                Exception lastEx = null;
                for (String u : urls) {
                    try {
                        System.out.println("[TransacaoDAOImpl] Trying sync URL: " + u + " for transacao id=" + t.getId());
                        response = HttpSyncUtil.sendPost(u, payload);
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

}
