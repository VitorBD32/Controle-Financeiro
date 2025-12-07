package controle.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import controle.Conexao;

public class ConfigDAOImpl implements ConfigDAO {

    @Override
    public String get(String key) throws Exception {
        String sql = "SELECT valor FROM sistema_config WHERE chave = ?";
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        }
        return null;
    }

    @Override
    public boolean set(String key, String value) throws Exception {
        String up = "UPDATE sistema_config SET valor = ? WHERE chave = ?";
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(up)) {
            ps.setString(1, value);
            ps.setString(2, key);
            if (ps.executeUpdate() > 0) return true;
        }
        // insert if not exists
        String ins = "INSERT INTO sistema_config (chave, valor) VALUES (?, ?)";
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(ins)) {
            ps.setString(1, key);
            ps.setString(2, value);
            return ps.executeUpdate() > 0;
        }
    }
}
