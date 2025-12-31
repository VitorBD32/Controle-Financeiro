package controle.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import controle.Conexao;

/**
 * DAO para cache local de alíquotas de tributos
 * Reduz chamadas à API externa e melhora performance
 * 
 * Tabela: cache_aliquotas
 * 
 * @author Sistema de Controle Financeiro
 * @version 1.0
 */
public class AliquotaCacheDAO {
    
    // TTL do cache: 7 dias (alíquotas mudam raramente)
    private static final int CACHE_TTL_DIAS = 7;
    
    /**
     * Obtém alíquotas do cache local (se válidas)
     */
    public Map<String, BigDecimal> getAliquotas(String uf, String municipio) throws Exception {
        String sql = "SELECT sigla_imposto, aliquota FROM cache_aliquotas " +
                     "WHERE uf = ? AND (municipio = ? OR municipio IS NULL) " +
                     "AND data_atualizacao >= DATE_SUB(NOW(), INTERVAL ? DAY)";
        
        Map<String, BigDecimal> resultado = new HashMap<>();
        
        try (Connection conn = Conexao.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, uf != null ? uf.toUpperCase() : "");
            ps.setString(2, municipio);
            ps.setInt(3, CACHE_TTL_DIAS);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String sigla = rs.getString("sigla_imposto");
                    BigDecimal aliquota = rs.getBigDecimal("aliquota");
                    resultado.put(sigla, aliquota);
                }
            }
        } catch (SQLException e) {
            // Tabela pode não existir ainda
            if (!e.getMessage().contains("doesn't exist")) {
                throw e;
            }
        }
        
        return resultado;
    }
    
    /**
     * Salva alíquotas no cache local
     */
    public void salvarAliquotas(String uf, String municipio, Map<String, BigDecimal> aliquotas) throws Exception {
        String sql = "INSERT INTO cache_aliquotas (uf, municipio, sigla_imposto, aliquota, data_atualizacao) " +
                     "VALUES (?, ?, ?, ?, NOW()) " +
                     "ON DUPLICATE KEY UPDATE aliquota = VALUES(aliquota), data_atualizacao = NOW()";
        
        try (Connection conn = Conexao.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            for (Map.Entry<String, BigDecimal> entry : aliquotas.entrySet()) {
                ps.setString(1, uf != null ? uf.toUpperCase() : "");
                ps.setString(2, municipio);
                ps.setString(3, entry.getKey());
                ps.setBigDecimal(4, entry.getValue());
                ps.addBatch();
            }
            
            ps.executeBatch();
            System.out.println("[CACHE] Alíquotas salvas: " + uf + "/" + municipio + " (" + aliquotas.size() + " impostos)");
            
        } catch (SQLException e) {
            // Tabela pode não existir; criar automaticamente
            if (e.getMessage().contains("doesn't exist")) {
                criarTabelaCache();
                // Retry
                salvarAliquotas(uf, municipio, aliquotas);
            } else {
                throw e;
            }
        }
    }
    
    /**
     * Cria tabela de cache se não existir
     */
    private void criarTabelaCache() throws Exception {
        String sql = "CREATE TABLE IF NOT EXISTS cache_aliquotas (" +
                     "  id INT AUTO_INCREMENT PRIMARY KEY," +
                     "  uf VARCHAR(2) NOT NULL," +
                     "  municipio VARCHAR(100)," +
                     "  sigla_imposto VARCHAR(10) NOT NULL," +
                     "  aliquota DECIMAL(10,4) NOT NULL," +
                     "  data_atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP," +
                     "  UNIQUE KEY uk_cache (uf, municipio, sigla_imposto)" +
                     ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        
        try (Connection conn = Conexao.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("[DB] Tabela cache_aliquotas criada com sucesso");
        }
    }
    
    /**
     * Limpa cache antigo (manutenção)
     */
    public int limparCacheAntigo() throws Exception {
        String sql = "DELETE FROM cache_aliquotas WHERE data_atualizacao < DATE_SUB(NOW(), INTERVAL ? DAY)";
        
        try (Connection conn = Conexao.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, CACHE_TTL_DIAS);
            int deleted = ps.executeUpdate();
            
            if (deleted > 0) {
                System.out.println("[CACHE] " + deleted + " registros antigos removidos");
            }
            
            return deleted;
        }
    }
    
    /**
     * Verifica se cache existe para UF/município
     */
    public boolean existeCache(String uf, String municipio) throws Exception {
        String sql = "SELECT 1 FROM cache_aliquotas WHERE uf = ? AND (municipio = ? OR municipio IS NULL) LIMIT 1";
        
        try (Connection conn = Conexao.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, uf != null ? uf.toUpperCase() : "");
            ps.setString(2, municipio);
            
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("doesn't exist")) {
                return false;
            }
            throw e;
        }
    }
}
