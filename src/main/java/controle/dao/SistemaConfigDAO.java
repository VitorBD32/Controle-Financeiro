package controle.dao;

import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import controle.Conexao;

/**
 * DAO para acesso às configurações do sistema
 * Tabela: sistema_config
 */
public class SistemaConfigDAO {
    
    private static Map<String, String> cache = new HashMap<>();
    private static long cacheTimestamp = 0;
    private static final long CACHE_TTL = 60000; // 1 minuto
    
    /**
     * Obtém uma configuração pelo chave
     */
    public String getConfig(String chave) {
        String sql = "SELECT valor FROM sistema_config WHERE chave = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, chave);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("valor");
                }
            }
        } catch (SQLException e) {
            System.err.println("[SistemaConfigDAO] Erro ao buscar config " + chave + ": " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Obtém uma configuração como BigDecimal
     */
    public BigDecimal getConfigDecimal(String chave) {
        String valor = getConfig(chave);
        if (valor != null) {
            try {
                return new BigDecimal(valor);
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * Obtém uma configuração como Integer
     */
    public Integer getConfigInt(String chave) {
        String valor = getConfig(chave);
        if (valor != null) {
            try {
                return Integer.parseInt(valor);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
    
    /**
     * Salva ou atualiza uma configuração
     */
    public boolean saveConfig(String chave, String valor, Integer idUsuario) {
        String sql = "INSERT INTO sistema_config (chave, valor, atualizado_por) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE valor = VALUES(valor), atualizado_por = VALUES(atualizado_por)";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, chave);
            ps.setString(2, valor);
            if (idUsuario != null) {
                ps.setInt(3, idUsuario);
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.executeUpdate();
            cache.clear(); // Limpar cache
            return true;
        } catch (SQLException e) {
            System.err.println("[SistemaConfigDAO] Erro ao salvar config " + chave + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Salva ou atualiza uma configuração (sem usuário)
     */
    public boolean saveConfig(String chave, String valor) {
        return saveConfig(chave, valor, null);
    }
    
    /**
     * Obtém todas as configurações de impostos
     */
    public Map<String, BigDecimal> getConfigImpostos() {
        Map<String, BigDecimal> impostos = new HashMap<>();
        
        // Usar valores padrão se não conseguir conectar
        impostos.put("TAXA_IBS", getConfigDecimalOrDefault("TAXA_IBS", new BigDecimal("15.00")));
        impostos.put("TAXA_CBS", getConfigDecimalOrDefault("TAXA_CBS", new BigDecimal("8.80")));
        impostos.put("TAXA_IS", getConfigDecimalOrDefault("TAXA_IS", BigDecimal.ZERO));
        impostos.put("TAXA_SERVICO", getConfigDecimalOrDefault("TAXA_SERVICO", new BigDecimal("10.00")));
        impostos.put("TAXA_MULTA_ATRASO", getConfigDecimalOrDefault("TAXA_MULTA_ATRASO", new BigDecimal("2.00")));
        impostos.put("TAXA_JUROS_MES", getConfigDecimalOrDefault("TAXA_JUROS_MES", new BigDecimal("1.00")));
        
        return impostos;
    }
    
    private BigDecimal getConfigDecimalOrDefault(String chave, BigDecimal defaultValue) {
        BigDecimal valor = getConfigDecimal(chave);
        return valor.compareTo(BigDecimal.ZERO) == 0 ? defaultValue : valor;
    }
    
    /**
     * Obtém configurações do emissor
     */
    public Map<String, String> getConfigEmissor() {
        Map<String, String> emissor = new HashMap<>();
        
        emissor.put("CNPJ", getConfigOrDefault("EMISSOR_CNPJ", "00.000.000/0001-00"));
        emissor.put("RAZAO_SOCIAL", getConfigOrDefault("EMISSOR_RAZAO_SOCIAL", "CONTROLE FINANCEIRO LTDA"));
        emissor.put("NOME_FANTASIA", getConfigOrDefault("EMISSOR_NOME_FANTASIA", "Controle Financeiro"));
        emissor.put("ENDERECO", getConfigOrDefault("EMISSOR_ENDERECO", "Rua das Finanças, 123"));
        emissor.put("CIDADE", getConfigOrDefault("EMISSOR_CIDADE", "Teresina"));
        emissor.put("UF", getConfigOrDefault("EMISSOR_UF", "PI"));
        emissor.put("CEP", getConfigOrDefault("EMISSOR_CEP", "64000-000"));
        emissor.put("IE", getConfigOrDefault("EMISSOR_IE", "ISENTO"));
        emissor.put("IM", getConfigOrDefault("EMISSOR_IM", "12345678"));
        emissor.put("EMAIL", getConfigOrDefault("EMISSOR_EMAIL", "contato@controlefinanceiro.com.br"));
        emissor.put("TELEFONE", getConfigOrDefault("EMISSOR_TELEFONE", "(86) 3333-4444"));
        
        return emissor;
    }
    
    /**
     * Obtém configurações bancárias para boleto
     */
    public Map<String, String> getConfigBanco() {
        Map<String, String> banco = new HashMap<>();
        
        banco.put("CODIGO", getConfigOrDefault("BANCO_CODIGO", "001"));
        banco.put("NOME", getConfigOrDefault("BANCO_NOME", "Banco do Brasil S.A."));
        banco.put("AGENCIA", getConfigOrDefault("BANCO_AGENCIA", "1234-5"));
        banco.put("CONTA", getConfigOrDefault("BANCO_CONTA", "12345678-9"));
        banco.put("CARTEIRA", getConfigOrDefault("BANCO_CARTEIRA", "17"));
        banco.put("CONVENIO", getConfigOrDefault("BANCO_CONVENIO", "1234567"));
        
        return banco;
    }
    
    private String getConfigOrDefault(String chave, String defaultValue) {
        String valor = getConfig(chave);
        return valor != null && !valor.isEmpty() ? valor : defaultValue;
    }
    
    /**
     * Verifica se a tabela sistema_config existe
     */
    public boolean tabelaExiste() {
        String sql = "SELECT 1 FROM sistema_config LIMIT 1";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Obtém todas as configurações
     */
    public Map<String, String> getAllConfigs() {
        Map<String, String> configs = new HashMap<>();
        String sql = "SELECT chave, valor FROM sistema_config ORDER BY chave";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                configs.put(rs.getString("chave"), rs.getString("valor"));
            }
        } catch (SQLException e) {
            System.err.println("[SistemaConfigDAO] Erro ao buscar configs: " + e.getMessage());
        }
        return configs;
    }
}
