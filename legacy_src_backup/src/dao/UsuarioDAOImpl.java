package controle.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import controle.Conexao;
import controle.model.Usuario;

public class UsuarioDAOImpl implements UsuarioDAO {

    @Override
    public Usuario insert(Usuario u) throws Exception {
        String sql = "INSERT INTO usuarios (nome, email, senha) VALUES (?, ?, ?)";
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNome());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getSenha());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    u.setId(rs.getInt(1));
                }
            }
            return u;
        }
    }

    @Override
    public Usuario findById(int id) throws Exception {
        String sql = "SELECT id_usuario, nome, email, senha FROM usuarios WHERE id_usuario = ?";
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(rs.getInt("id_usuario"), rs.getString("nome"), rs.getString("email"), rs.getString("senha"));
                }
            }
        }
        return null;
    }

    @Override
    public List<Usuario> findAll() throws Exception {
        String sql = "SELECT id_usuario, nome, email, senha FROM usuarios";
        List<Usuario> list = new ArrayList<>();
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Usuario(rs.getInt("id_usuario"), rs.getString("nome"), rs.getString("email"), rs.getString("senha")));
            }
        }
        return list;
    }

    @Override
    public boolean update(Usuario u) throws Exception {
        String sql = "UPDATE usuarios SET nome = ?, email = ?, senha = ? WHERE id_usuario = ?";
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getNome());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getSenha());
            ps.setInt(4, u.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws Exception {
        String sql = "DELETE FROM usuarios WHERE id_usuario = ?";
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
