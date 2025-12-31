package controle.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import controle.config.DBConfig;
import controle.model.Cartao;

/**
 * Implementação do DAO de cartões com suporte a criptografia
 */
public class CartaoDAOImpl implements CartaoDAO {

    private Connection getConnection() throws SQLException {
        return DBConfig.getConnection();
    }

    /**
     * Cria a tabela de cartões se não existir
     */
    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS cartoes (\n" +
                "    id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                "    id_usuario INT NOT NULL,\n" +
                "    token VARCHAR(64) UNIQUE NOT NULL,\n" +
                "    numero_mascarado VARCHAR(25) NOT NULL,\n" +
                "    numero_cripto TEXT NOT NULL,\n" +
                "    nome_titular VARCHAR(100) NOT NULL,\n" +
                "    validade VARCHAR(10) NOT NULL,\n" +
                "    cvv_cripto TEXT NOT NULL,\n" +
                "    bandeira VARCHAR(30) NOT NULL,\n" +
                "    tipo VARCHAR(20) NOT NULL DEFAULT 'CREDITO',\n" +
                "    apelido VARCHAR(50),\n" +
                "    ativo BOOLEAN DEFAULT TRUE,\n" +
                "    data_cadastro DATETIME DEFAULT CURRENT_TIMESTAMP,\n" +
                "    ultimo_uso DATETIME,\n" +
                "    FOREIGN KEY (id_usuario) REFERENCES usuarios(id) ON DELETE CASCADE,\n" +
                "    INDEX idx_usuario (id_usuario),\n" +
                "    INDEX idx_token (token),\n" +
                "    INDEX idx_ativo (ativo)\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("[CartaoDAO] Tabela 'cartoes' verificada/criada com sucesso");
        } catch (SQLException e) {
            System.err.println("[CartaoDAO] Erro ao criar tabela: " + e.getMessage());
        }
    }

    @Override
    public void insert(Cartao cartao) throws Exception {
        createTableIfNotExists();
        
        String sql = "INSERT INTO cartoes (id_usuario, token, numero_mascarado, numero_cripto, " +
                "nome_titular, validade, cvv_cripto, bandeira, tipo, apelido, ativo, data_cadastro) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, cartao.getIdUsuario());
            ps.setString(2, cartao.getToken());
            ps.setString(3, cartao.getNumeroMascarado());
            ps.setString(4, cartao.getNumeroCripto());
            ps.setString(5, cartao.getNomeTitular());
            ps.setString(6, cartao.getValidade());
            ps.setString(7, cartao.getCvvCripto());
            ps.setString(8, cartao.getBandeira());
            ps.setString(9, cartao.getTipo());
            ps.setString(10, cartao.getApelido());
            ps.setBoolean(11, cartao.isAtivo());
            ps.setTimestamp(12, Timestamp.valueOf(cartao.getDataCadastro() != null ? 
                    cartao.getDataCadastro() : LocalDateTime.now()));
            
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    cartao.setId(rs.getInt(1));
                }
            }
            
            System.out.println("[CartaoDAO] Cartão inserido com sucesso. ID: " + cartao.getId());
        }
    }

    @Override
    public void update(Cartao cartao) throws Exception {
        String sql = "UPDATE cartoes SET " +
                "nome_titular = ?, validade = ?, tipo = ?, apelido = ?, ativo = ? " +
                "WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cartao.getNomeTitular());
            ps.setString(2, cartao.getValidade());
            ps.setString(3, cartao.getTipo());
            ps.setString(4, cartao.getApelido());
            ps.setBoolean(5, cartao.isAtivo());
            ps.setInt(6, cartao.getId());
            
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws Exception {
        // Soft delete - apenas desativa
        String sql = "UPDATE cartoes SET ativo = FALSE WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public Cartao findById(int id) throws Exception {
        String sql = "SELECT * FROM cartoes WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Cartao findByToken(String token) throws Exception {
        String sql = "SELECT * FROM cartoes WHERE token = ? AND ativo = TRUE";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Cartao> findByUsuario(int idUsuario) throws Exception {
        createTableIfNotExists();
        
        List<Cartao> cartoes = new ArrayList<>();
        String sql = "SELECT * FROM cartoes WHERE id_usuario = ? AND ativo = TRUE ORDER BY data_cadastro DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cartoes.add(mapResultSet(rs));
                }
            }
        }
        return cartoes;
    }

    @Override
    public List<Cartao> findAll() throws Exception {
        createTableIfNotExists();
        
        List<Cartao> cartoes = new ArrayList<>();
        String sql = "SELECT * FROM cartoes ORDER BY data_cadastro DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                cartoes.add(mapResultSet(rs));
            }
        }
        return cartoes;
    }

    @Override
    public void registrarUso(int id) throws Exception {
        String sql = "UPDATE cartoes SET ultimo_uso = ? WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    private Cartao mapResultSet(ResultSet rs) throws SQLException {
        Cartao c = new Cartao();
        c.setId(rs.getInt("id"));
        c.setIdUsuario(rs.getInt("id_usuario"));
        c.setToken(rs.getString("token"));
        c.setNumeroMascarado(rs.getString("numero_mascarado"));
        c.setNumeroCripto(rs.getString("numero_cripto"));
        c.setNomeTitular(rs.getString("nome_titular"));
        c.setValidade(rs.getString("validade"));
        c.setCvvCripto(rs.getString("cvv_cripto"));
        c.setBandeira(rs.getString("bandeira"));
        c.setTipo(rs.getString("tipo"));
        c.setApelido(rs.getString("apelido"));
        c.setAtivo(rs.getBoolean("ativo"));
        
        Timestamp ts = rs.getTimestamp("data_cadastro");
        if (ts != null) c.setDataCadastro(ts.toLocalDateTime());
        
        Timestamp tsUso = rs.getTimestamp("ultimo_uso");
        if (tsUso != null) c.setUltimoUso(tsUso.toLocalDateTime());
        
        return c;
    }
}
