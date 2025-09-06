package controle.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import controle.Conexao;
import controle.model.Categoria;

public class CategoriaDAOImpl implements CategoriaDAO {

    @Override
    public Categoria insert(Categoria c) throws Exception {
        String sql = "INSERT INTO categorias (nome, valor, descricao) VALUES (?,?,?)";
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getNome());
            ps.setBigDecimal(2, c.getValor() != null ? c.getValor() : java.math.BigDecimal.ZERO);
            ps.setString(3, c.getDescricao());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    c.setId(rs.getInt(1));
                }
            }
            return c;
        }
    }

    @Override
    public Categoria findById(int id) throws Exception {
        String sql = "SELECT id_categoria, nome, valor, descricao FROM categorias WHERE id_categoria = ?";
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Categoria c = new Categoria();
                    c.setId(rs.getInt("id_categoria"));
                    c.setNome(rs.getString("nome"));
                    c.setValor(rs.getBigDecimal("valor"));
                    c.setDescricao(rs.getString("descricao"));
                    return c;
                }
            }
        }
        return null;
    }

    @Override
    public List<Categoria> findAll() throws Exception {
        String sql = "SELECT id_categoria, nome, valor, descricao FROM categorias";
        List<Categoria> list = new ArrayList<>();
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Categoria c = new Categoria();
                c.setId(rs.getInt("id_categoria"));
                c.setNome(rs.getString("nome"));
                c.setValor(rs.getBigDecimal("valor"));
                c.setDescricao(rs.getString("descricao"));
                list.add(c);
            }
        }
        return list;
    }

    @Override
    public boolean update(Categoria c) throws Exception {
        String sql = "UPDATE categorias SET nome = ?, valor = ?, descricao = ? WHERE id_categoria = ?";
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getNome());
            ps.setBigDecimal(2, c.getValor() != null ? c.getValor() : java.math.BigDecimal.ZERO);
            ps.setString(3, c.getDescricao());
            ps.setInt(4, c.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws Exception {
        String sql = "DELETE FROM categorias WHERE id_categoria = ?";
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
