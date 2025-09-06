package controle.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import controle.Conexao;
import controle.model.Transacao;

public class TransacaoDAOImpl implements TransacaoDAO {

    @Override
    public Transacao insert(Transacao t) throws Exception {
        String sql = "INSERT INTO transacoes (id_usuario, id_categoria, tipo, valor, data, descricao) VALUES (?,?,?,?,?,?)";
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, t.getIdUsuario());
            ps.setInt(2, t.getIdCategoria());
            ps.setString(3, t.getTipo());
            ps.setBigDecimal(4, t.getValor());
            // convert LocalDateTime to java.sql.Timestamp
            if (t.getData() != null) {
                ps.setTimestamp(5, java.sql.Timestamp.valueOf(t.getData()));
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
                    Transacao t = new Transacao();
                    t.setId(rs.getInt("id_transacao"));
                    t.setIdUsuario(rs.getInt("id_usuario"));
                    t.setIdCategoria(rs.getInt("id_categoria"));
                    t.setTipo(rs.getString("tipo"));
                    t.setValor(rs.getBigDecimal("valor"));
                    java.sql.Timestamp ts = rs.getTimestamp("data");
                    if (ts != null) {
                        t.setData(ts.toLocalDateTime());
                    } else {
                        t.setData(null);
                    }
                    t.setDescricao(rs.getString("descricao"));
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
                Transacao t = new Transacao();
                t.setId(rs.getInt("id_transacao"));
                t.setIdUsuario(rs.getInt("id_usuario"));
                t.setIdCategoria(rs.getInt("id_categoria"));
                t.setTipo(rs.getString("tipo"));
                t.setValor(rs.getBigDecimal("valor"));
                java.sql.Timestamp ts = rs.getTimestamp("data");
                if (ts != null) {
                    t.setData(ts.toLocalDateTime());
                } else {
                    t.setData(null);
                }
                t.setDescricao(rs.getString("descricao"));
                list.add(t);
            }
        }
        return list;
    }

    @Override
    public List<Transacao> findByPeriodo(LocalDate inicio, LocalDate fim) throws Exception {
        String sql = "SELECT id_transacao, id_usuario, id_categoria, tipo, valor, data, descricao FROM transacoes WHERE data BETWEEN ? AND ? ORDER BY data DESC";
        List<Transacao> list = new ArrayList<>();
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, inicio != null ? java.sql.Timestamp.valueOf(inicio.atStartOfDay()) : null);
            ps.setTimestamp(2, fim != null ? java.sql.Timestamp.valueOf(fim.atStartOfDay()) : null);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Transacao t = new Transacao();
                    t.setId(rs.getInt("id_transacao"));
                    t.setIdUsuario(rs.getInt("id_usuario"));
                    t.setIdCategoria(rs.getInt("id_categoria"));
                    t.setTipo(rs.getString("tipo"));
                    t.setValor(rs.getBigDecimal("valor"));
                    java.sql.Timestamp ts = rs.getTimestamp("data");
                    if (ts != null) {
                        t.setData(ts.toLocalDateTime());
                    } else {
                        t.setData(null);
                    }
                    t.setDescricao(rs.getString("descricao"));
                    list.add(t);
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
                ps.setTimestamp(5, java.sql.Timestamp.valueOf(t.getData()));
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
}
