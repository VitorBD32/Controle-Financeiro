package controle.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

import controle.Conexao;
import controle.model.Usuario;

public class UsuarioDAOImpl implements UsuarioDAO {

    @Override
    public Usuario insert(Usuario u) throws Exception {
        // Try with admin column first
        String sql = "INSERT INTO usuarios (nome, email, senha, autorizado, admin) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Conexao.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, u.getNome());
                ps.setString(2, u.getEmail());
                // hash the password before storing
                String hashed = BCrypt.hashpw(u.getSenha(), BCrypt.gensalt(12));
                ps.setString(3, hashed);
                ps.setBoolean(4, u.isAutorizado());
                ps.setBoolean(5, u.isAdmin());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) u.setId(rs.getInt(1));
                }
                u.setSenha(null);
                return u;
            }
        } catch (SQLException ex) {
            // If 'admin' column doesn't exist, fallback to INSERT without admin
            String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
            if (msg.contains("unknown column") || msg.contains("no such column") ) {
                String fallback = "INSERT INTO usuarios (nome, email, senha, autorizado) VALUES (?, ?, ?, ?)";
                try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(fallback, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, u.getNome());
                    ps.setString(2, u.getEmail());
                    String hashed = BCrypt.hashpw(u.getSenha(), BCrypt.gensalt(12));
                    ps.setString(3, hashed);
                    ps.setBoolean(4, u.isAutorizado());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) u.setId(rs.getInt(1)); }
                    u.setSenha(null);
                    return u;
                }
            }
            throw ex;
        }
        
    }

    @Override
    public Usuario findById(int id) throws Exception {
        String sql = "SELECT id, nome, email, senha, autorizado, admin FROM usuarios WHERE id = ?";
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario(rs.getInt("id"), rs.getString("nome"), rs.getString("email"), null);
                    try {
                        boolean autorizado = rs.getBoolean("autorizado");
                        u.setAutorizado(autorizado);
                    } catch (SQLException e) {
                        // Column might not exist; default true
                        u.setAutorizado(true);
                    }
                    try {
                        boolean admin = rs.getBoolean("admin");
                        u.setAdmin(admin);
                    } catch (SQLException e) {
                        // Column might not exist; default false
                        u.setAdmin(false);
                    }
                    return u;
                }
            }
        }
        return null;
    }

    @Override
    public Usuario findByEmail(String email) throws Exception {
        String sql = "SELECT id, nome, email, senha, autorizado, admin FROM usuarios WHERE email = ?";
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario(rs.getInt("id"), rs.getString("nome"), rs.getString("email"), rs.getString("senha"));
                    try { u.setAutorizado(rs.getBoolean("autorizado")); } catch (SQLException e) { u.setAutorizado(true); }
                    try { u.setAdmin(rs.getBoolean("admin")); } catch (SQLException e) { u.setAdmin(false); }
                    return u;
                }
            }
        }
        return null;
    }

    @Override
    public Usuario findByNome(String nome) throws Exception {
        String sql = "SELECT id, nome, email, senha, autorizado, admin FROM usuarios WHERE nome = ?";
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nome);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario(rs.getInt("id"), rs.getString("nome"), rs.getString("email"), rs.getString("senha"));
                    try { u.setAutorizado(rs.getBoolean("autorizado")); } catch (SQLException e) { u.setAutorizado(true); }
                    try { u.setAdmin(rs.getBoolean("admin")); } catch (SQLException e) { u.setAdmin(false); }
                    return u;
                }
            }
        }
        return null;
    }

    @Override
    public List<Usuario> findAll() throws Exception {
        String sql = "SELECT id, nome, email, senha, autorizado, admin FROM usuarios";
        List<Usuario> list = new ArrayList<>();
        // Attempt to read including 'autorizado' column; fall back if column missing
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Usuario u = new Usuario(rs.getInt("id"), rs.getString("nome"), rs.getString("email"), null);
                try {
                    boolean autorizado = rs.getBoolean("autorizado");
                    u.setAutorizado(autorizado);
                } catch (SQLException ex) {
                    u.setAutorizado(true);
                }
                try {
                    boolean admin = rs.getBoolean("admin");
                    u.setAdmin(admin);
                } catch (SQLException ex) {
                    u.setAdmin(false);
                }
                list.add(u);
            }
        } catch (SQLException e) {
            // fallback: no autorizado column; select without it
            String fallback = "SELECT id, nome, email, senha FROM usuarios";
            try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(fallback); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Usuario u = new Usuario(rs.getInt("id"), rs.getString("nome"), rs.getString("email"), null);
                    u.setAutorizado(true); // default to true if column not present
                    list.add(u);
                }
            }
        }
        return list;
    }

    @Override
    public boolean update(Usuario u) throws Exception {
        String sql = "UPDATE usuarios SET nome = ?, email = ?, senha = ?, autorizado = ?, admin = ? WHERE id = ?";
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getNome());
            ps.setString(2, u.getEmail());
            // hash the password if provided; otherwise leave existing password
            if (u.getSenha() == null || u.getSenha().isEmpty()) {
                // keep current password: read current hash and reuse
                String currentSql = "SELECT senha FROM usuarios WHERE id = ?";
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
            ps.setBoolean(4, u.isAutorizado());
            ps.setBoolean(5, u.isAdmin());
            ps.setInt(6, u.getId());
                return ps.executeUpdate() > 0;
            }
        }
    
    @Override
    public boolean delete(int id) throws Exception {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean existsAdmin() throws Exception {
        String sql = "SELECT 1 FROM usuarios WHERE admin = 1 LIMIT 1";
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next();
        } catch (SQLException ex) {
            // If the 'admin' column does not exist return false
            String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
            if (msg.contains("unknown column") || msg.contains("no such column")) {
                return false;
            }
            throw ex;
        }
    }
}
