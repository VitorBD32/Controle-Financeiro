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
        // Ajustado para usar coluna 'tipo' (conforme schema atual: id, nome, tipo, descricao)
        String sql = "INSERT INTO categorias (nome, tipo, descricao) VALUES (?,?,?)";
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getNome());
            ps.setString(2, c.getTipo());
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
        String sql = "SELECT id, nome, tipo, descricao FROM categorias WHERE id = ?";
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Categoria c = new Categoria();
                    c.setId(rs.getInt("id"));
                    c.setNome(rs.getString("nome"));
                    c.setTipo(rs.getString("tipo"));
                    c.setDescricao(rs.getString("descricao"));
                    return c;
                }
            }
        }
        return null;
    }

    @Override
    public List<Categoria> findAll() throws Exception {
        String sql = "SELECT id, nome, tipo, descricao FROM categorias";
        List<Categoria> list = new ArrayList<>();
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Categoria c = new Categoria();
                c.setId(rs.getInt("id"));
                c.setNome(rs.getString("nome"));
                c.setTipo(rs.getString("tipo"));
                c.setDescricao(rs.getString("descricao"));
                list.add(c);
            }
        }
        return list;
    }

    @Override
    public boolean update(Categoria c) throws Exception {
        String sql = "UPDATE categorias SET nome = ?, tipo = ?, descricao = ? WHERE id = ?";
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getNome());
            ps.setString(2, c.getTipo());
            ps.setString(3, c.getDescricao());
            ps.setInt(4, c.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws Exception {
        String sql = "DELETE FROM categorias WHERE id = ?";
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
