package controle.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;
import controle.model.Usuario;
import controle.Conexao;

public class UsuarioDAOImpl implements UsuarioDAO {

    @Override
    public Usuario insert(Usuario u) throws Exception {
        String sql = "INSERT INTO usuarios (nome, email, senha) VALUES (?, ?, ?)";
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNome());
            ps.setString(2, u.getEmail());
            // hash the password before storing
            String hashed = BCrypt.hashpw(u.getSenha(), BCrypt.gensalt(12));
            ps.setString(3, hashed);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    u.setId(rs.getInt(1));
                }
            }
            // do not keep password in memory
            u.setSenha(null);
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
                    // do not expose password; set senha null
                    return new Usuario(rs.getInt("id_usuario"), rs.getString("nome"), rs.getString("email"), null);
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
                // do not include password in results
                list.add(new Usuario(rs.getInt("id_usuario"), rs.getString("nome"), rs.getString("email"), null));
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
            // hash the password if provided; otherwise leave existing password
            if (u.getSenha() == null || u.getSenha().isEmpty()) {
                // keep current password: read current hash and reuse
                String currentSql = "SELECT senha FROM usuarios WHERE id_usuario = ?";
                try (PreparedStatement ps2 = conn.prepareStatement(currentSql)) {
                    ps2.setInt(1, u.getId());
                    try (ResultSet rs = ps2.executeQuery()) {
                        if (rs.next()) {
                            ps.setString(3, rs.getString(1));
                        } else {
                            ps.setString(3, "");
                        }
                    }
                }
            } else {
                ps.setString(3, BCrypt.hashpw(u.getSenha(), BCrypt.gensalt(12)));
            }
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
