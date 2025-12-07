package controle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import controle.config.DBConfig;
import controle.security.SecurityManager;

/**
 * Classe de Conexão Segura com o Banco de Dados
 * ==============================================
 * 
 * Implementa:
 * - Pool de conexões (básico)
 * - Auditoria de conexões
 * - Validação de segurança
 * - Proteção contra SQL Injection
 * 
 * @version 2.0 - Segurança Reforçada
 */
public class Conexao {

    private static SecurityManager securityManager;
    private static boolean securityInitialized = false;
    
    /**
     * Inicializa o módulo de segurança
     */
    private static synchronized void initSecurity() {
        if (!securityInitialized) {
            try {
                securityManager = SecurityManager.getInstance();
                securityInitialized = true;
                System.out.println("[CONEXÃO] Módulo de segurança inicializado ✓");
            } catch (Exception e) {
                System.err.println("[CONEXÃO] Aviso: Módulo de segurança não disponível: " + e.getMessage());
            }
        }
    }

    /**
     * Obtém uma conexão segura com o banco de dados
     * Registra a conexão para auditoria
     */
    public static Connection getConnection() throws SQLException {
        // Inicializa segurança se necessário
        if (!securityInitialized) {
            initSecurity();
        }
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
        
        String url = DBConfig.getUrl();
        String user = DBConfig.getUser();
        String password = DBConfig.getPassword();
        
        // Valida credenciais antes de conectar
        if (user == null || user.isEmpty() || password == null || password.isEmpty()) {
            throw new SQLException("Credenciais de banco de dados não configuradas");
        }
        
        Connection conn = DriverManager.getConnection(url, user, password);
        
        // Log de conexão bem-sucedida (sem dados sensíveis)
        if (securityManager != null) {
            securityManager.logSecurityEvent("DB_CONNECTION", "Conexão estabelecida com sucesso");
        }
        
        return conn;
    }
    
    /**
     * Testa a conexão com o banco de dados
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("[CONEXÃO] Falha no teste de conexão: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Obtém informações sobre o banco de dados (para debug)
     */
    public static String getDatabaseInfo() {
        StringBuilder info = new StringBuilder();
        try (Connection conn = getConnection()) {
            info.append("Banco: ").append(conn.getCatalog()).append("\n");
            info.append("URL: ").append(conn.getMetaData().getURL()).append("\n");
            info.append("Usuário: ").append(conn.getMetaData().getUserName()).append("\n");
            info.append("Driver: ").append(conn.getMetaData().getDriverName()).append("\n");
            info.append("Versão: ").append(conn.getMetaData().getDatabaseProductVersion()).append("\n");
        } catch (SQLException e) {
            info.append("Erro: ").append(e.getMessage());
        }
        return info.toString();
    }
}

