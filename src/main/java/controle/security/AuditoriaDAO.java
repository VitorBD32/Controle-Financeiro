package controle.security;

import controle.Conexao;
import java.sql.*;
import java.time.LocalDateTime;

/**
 * DAO para Auditoria de Segurança
 * ================================
 * 
 * Registra todas as ações do sistema para:
 * - Rastreabilidade de operações
 * - Detecção de ataques
 * - Compliance com LGPD
 * - Análise forense
 * 
 * @author Sistema de Controle Financeiro
 * @version 2.0
 */
public class AuditoriaDAO {
    
    private static AuditoriaDAO instance;
    private SecurityManager securityManager;
    
    private AuditoriaDAO() {
        this.securityManager = SecurityManager.getInstance();
    }
    
    public static synchronized AuditoriaDAO getInstance() {
        if (instance == null) {
            instance = new AuditoriaDAO();
        }
        return instance;
    }
    
    // =========================================================================
    // REGISTRO DE AUDITORIA
    // =========================================================================
    
    /**
     * Registra uma ação no log de auditoria
     */
    public void registrarAcao(Integer usuarioId, String usuarioNome, String ipAddress, 
                              String acao, String entidade, Integer entidadeId, 
                              String dadosAnteriores, String dadosNovos, 
                              String resultado, String detalhes, String sessionToken) {
        
        String sql = "INSERT INTO auditoria_log (usuario_id, usuario_nome, ip_address, acao, " +
                     "entidade, entidade_id, dados_anteriores, dados_novos, resultado, detalhes, session_token) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = Conexao.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, usuarioId);
            ps.setString(2, securityManager.sanitizeSqlInput(usuarioNome));
            ps.setString(3, ipAddress);
            ps.setString(4, acao);
            ps.setString(5, entidade);
            ps.setObject(6, entidadeId);
            ps.setString(7, dadosAnteriores);
            ps.setString(8, dadosNovos);
            ps.setString(9, resultado);
            ps.setString(10, securityManager.sanitizeSqlInput(detalhes));
            ps.setString(11, sessionToken);
            
            ps.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("[AUDITORIA] Erro ao registrar ação: " + e.getMessage());
        }
    }
    
    /**
     * Versão simplificada para ações comuns
     */
    public void log(String acao, String detalhes) {
        registrarAcao(null, "SISTEMA", "127.0.0.1", acao, null, null, null, null, "SUCESSO", detalhes, null);
    }
    
    /**
     * Log de ação de usuário
     */
    public void logUsuario(int usuarioId, String nome, String acao, String entidade, String detalhes) {
        registrarAcao(usuarioId, nome, getClientIp(), acao, entidade, null, null, null, "SUCESSO", detalhes, null);
    }
    
    // =========================================================================
    // REGISTRO DE LOGIN
    // =========================================================================
    
    /**
     * Registra tentativa de login
     */
    public void registrarTentativaLogin(String username, String ipAddress, boolean sucesso, 
                                        String motivoFalha, String userAgent) {
        
        String sql = "INSERT INTO login_attempts (username, ip_address, sucesso, motivo_falha, user_agent) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = Conexao.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, securityManager.sanitizeSqlInput(username));
            ps.setString(2, ipAddress);
            ps.setBoolean(3, sucesso);
            ps.setString(4, motivoFalha);
            ps.setString(5, userAgent);
            
            ps.executeUpdate();
            
            // Verifica se deve bloquear usuário
            if (!sucesso) {
                verificarBloqueioAutomatico(username, ipAddress);
            }
            
        } catch (SQLException e) {
            System.err.println("[AUDITORIA] Erro ao registrar login: " + e.getMessage());
        }
    }
    
    /**
     * Verifica e aplica bloqueio automático após múltiplas falhas
     */
    private void verificarBloqueioAutomatico(String username, String ipAddress) {
        String sql = "SELECT COUNT(*) FROM login_attempts " +
                     "WHERE username = ? AND sucesso = FALSE " +
                     "AND data_hora > DATE_SUB(NOW(), INTERVAL 15 MINUTE)";
        
        try (Connection conn = Conexao.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, username);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) >= 5) {
                    bloquearUsuario(username, "Múltiplas tentativas de login falhas", 30);
                    registrarAlerta("BRUTE_FORCE", "ALTA", 
                        "Usuário bloqueado após múltiplas tentativas: " + username, 
                        null, ipAddress, null);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("[AUDITORIA] Erro ao verificar bloqueio: " + e.getMessage());
        }
    }
    
    // =========================================================================
    // GERENCIAMENTO DE BLOQUEIOS
    // =========================================================================
    
    /**
     * Bloqueia um usuário
     */
    public void bloquearUsuario(String username, String motivo, int minutosExpiracao) {
        String sql = "INSERT INTO bloqueios_seguranca (tipo, valor, motivo, data_expiracao) " +
                     "VALUES ('USUARIO', ?, ?, DATE_ADD(NOW(), INTERVAL ? MINUTE)) " +
                     "ON DUPLICATE KEY UPDATE motivo = ?, data_expiracao = DATE_ADD(NOW(), INTERVAL ? MINUTE), ativo = TRUE";
        
        try (Connection conn = Conexao.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, username);
            ps.setString(2, motivo);
            ps.setInt(3, minutosExpiracao);
            ps.setString(4, motivo);
            ps.setInt(5, minutosExpiracao);
            
            ps.executeUpdate();
            
            System.err.println("[SEGURANÇA] Usuário bloqueado: " + username + " - " + motivo);
            
        } catch (SQLException e) {
            System.err.println("[AUDITORIA] Erro ao bloquear usuário: " + e.getMessage());
        }
    }
    
    /**
     * Bloqueia um IP
     */
    public void bloquearIP(String ip, String motivo, int minutosExpiracao) {
        String sql = "INSERT INTO bloqueios_seguranca (tipo, valor, motivo, data_expiracao) " +
                     "VALUES ('IP', ?, ?, DATE_ADD(NOW(), INTERVAL ? MINUTE)) " +
                     "ON DUPLICATE KEY UPDATE motivo = ?, data_expiracao = DATE_ADD(NOW(), INTERVAL ? MINUTE), ativo = TRUE";
        
        try (Connection conn = Conexao.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, ip);
            ps.setString(2, motivo);
            ps.setInt(3, minutosExpiracao);
            ps.setString(4, motivo);
            ps.setInt(5, minutosExpiracao);
            
            ps.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("[AUDITORIA] Erro ao bloquear IP: " + e.getMessage());
        }
    }
    
    /**
     * Verifica se usuário está bloqueado
     */
    public boolean isUsuarioBloqueado(String username) {
        String sql = "SELECT 1 FROM bloqueios_seguranca " +
                     "WHERE tipo = 'USUARIO' AND valor = ? AND ativo = TRUE " +
                     "AND (data_expiracao IS NULL OR data_expiracao > NOW())";
        
        try (Connection conn = Conexao.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, username);
            
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
            
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Verifica se IP está bloqueado
     */
    public boolean isIPBloqueado(String ip) {
        String sql = "SELECT 1 FROM bloqueios_seguranca " +
                     "WHERE tipo = 'IP' AND valor = ? AND ativo = TRUE " +
                     "AND (data_expiracao IS NULL OR data_expiracao > NOW())";
        
        try (Connection conn = Conexao.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, ip);
            
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
            
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Desbloqueia um usuário
     */
    public void desbloquearUsuario(String username) {
        String sql = "UPDATE bloqueios_seguranca SET ativo = FALSE WHERE tipo = 'USUARIO' AND valor = ?";
        
        try (Connection conn = Conexao.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, username);
            ps.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("[AUDITORIA] Erro ao desbloquear: " + e.getMessage());
        }
    }
    
    // =========================================================================
    // ALERTAS DE SEGURANÇA
    // =========================================================================
    
    /**
     * Registra um alerta de segurança
     */
    public void registrarAlerta(String tipoAlerta, String severidade, String descricao, 
                                String dadosSuspeitos, String ipOrigem, Integer usuarioId) {
        
        String sql = "INSERT INTO alertas_seguranca (tipo_alerta, severidade, descricao, " +
                     "dados_suspeitos, ip_origem, usuario_id) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = Conexao.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, tipoAlerta);
            ps.setString(2, severidade);
            ps.setString(3, descricao);
            ps.setString(4, dadosSuspeitos);
            ps.setString(5, ipOrigem);
            ps.setObject(6, usuarioId);
            
            ps.executeUpdate();
            
            // Log crítico no console
            if ("CRITICA".equals(severidade) || "ALTA".equals(severidade)) {
                System.err.println("\n⚠️ ALERTA DE SEGURANÇA [" + severidade + "] ⚠️");
                System.err.println("Tipo: " + tipoAlerta);
                System.err.println("Descrição: " + descricao);
                System.err.println("IP: " + ipOrigem);
                System.err.println();
            }
            
        } catch (SQLException e) {
            System.err.println("[AUDITORIA] Erro ao registrar alerta: " + e.getMessage());
        }
    }
    
    /**
     * Alerta de SQL Injection detectado
     */
    public void alertaSqlInjection(String campo, String valorSuspeito, String ip) {
        registrarAlerta("SQL_INJECTION", "CRITICA", 
            "Tentativa de SQL Injection detectada no campo: " + campo,
            valorSuspeito, ip, null);
    }
    
    /**
     * Alerta de XSS detectado
     */
    public void alertaXss(String campo, String valorSuspeito, String ip) {
        registrarAlerta("XSS", "ALTA",
            "Tentativa de XSS detectada no campo: " + campo,
            valorSuspeito, ip, null);
    }
    
    /**
     * Alerta de acesso não autorizado
     */
    public void alertaAcessoNaoAutorizado(String recurso, Integer usuarioId, String ip) {
        registrarAlerta("ACESSO_NAO_AUTORIZADO", "ALTA",
            "Tentativa de acesso não autorizado ao recurso: " + recurso,
            null, ip, usuarioId);
    }
    
    // =========================================================================
    // SESSÕES
    // =========================================================================
    
    /**
     * Cria uma nova sessão
     */
    public String criarSessao(int usuarioId, String ipAddress, String userAgent) {
        String token = securityManager.generateSessionToken();
        
        String sql = "INSERT INTO sessoes_ativas (usuario_id, session_token, ip_address, user_agent, " +
                     "data_expiracao) VALUES (?, ?, ?, ?, DATE_ADD(NOW(), INTERVAL 8 HOUR))";
        
        try (Connection conn = Conexao.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, usuarioId);
            ps.setString(2, token);
            ps.setString(3, ipAddress);
            ps.setString(4, userAgent);
            
            ps.executeUpdate();
            
            return token;
            
        } catch (SQLException e) {
            System.err.println("[AUDITORIA] Erro ao criar sessão: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Valida se sessão é válida
     */
    public boolean validarSessao(String token) {
        String sql = "SELECT 1 FROM sessoes_ativas " +
                     "WHERE session_token = ? AND ativa = TRUE " +
                     "AND (data_expiracao IS NULL OR data_expiracao > NOW())";
        
        try (Connection conn = Conexao.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, token);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Atualiza último acesso
                    atualizarUltimoAcesso(token);
                    return true;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("[AUDITORIA] Erro ao validar sessão: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Atualiza último acesso da sessão
     */
    private void atualizarUltimoAcesso(String token) {
        String sql = "UPDATE sessoes_ativas SET data_ultimo_acesso = NOW() WHERE session_token = ?";
        
        try (Connection conn = Conexao.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, token);
            ps.executeUpdate();
            
        } catch (SQLException e) {
            // Ignora erros de atualização de acesso
        }
    }
    
    /**
     * Encerra uma sessão
     */
    public void encerrarSessao(String token) {
        String sql = "UPDATE sessoes_ativas SET ativa = FALSE WHERE session_token = ?";
        
        try (Connection conn = Conexao.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, token);
            ps.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("[AUDITORIA] Erro ao encerrar sessão: " + e.getMessage());
        }
    }
    
    /**
     * Encerra todas as sessões de um usuário
     */
    public void encerrarTodasSessoes(int usuarioId) {
        String sql = "UPDATE sessoes_ativas SET ativa = FALSE WHERE usuario_id = ?";
        
        try (Connection conn = Conexao.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, usuarioId);
            ps.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("[AUDITORIA] Erro ao encerrar sessões: " + e.getMessage());
        }
    }
    
    // =========================================================================
    // UTILIDADES
    // =========================================================================
    
    /**
     * Obtém o IP do cliente (simulado para aplicação desktop)
     */
    private String getClientIp() {
        try {
            return java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }
    
    /**
     * Conta tentativas de login falhas nas últimas horas
     */
    public int contarTentativasFalhas(String username, int horas) {
        String sql = "SELECT COUNT(*) FROM login_attempts " +
                     "WHERE username = ? AND sucesso = FALSE " +
                     "AND data_hora > DATE_SUB(NOW(), INTERVAL ? HOUR)";
        
        try (Connection conn = Conexao.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, username);
            ps.setInt(2, horas);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
        } catch (SQLException e) {
            return 0;
        }
        
        return 0;
    }
    
    /**
     * Obtém resumo de segurança
     */
    public String getResumoSeguranca() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========== RESUMO DE SEGURANÇA ==========\n");
        
        String sql = "SELECT " +
                     "(SELECT COUNT(*) FROM login_attempts WHERE sucesso = FALSE AND data_hora > DATE_SUB(NOW(), INTERVAL 24 HOUR)) AS falhas, " +
                     "(SELECT COUNT(*) FROM bloqueios_seguranca WHERE ativo = TRUE) AS bloqueados, " +
                     "(SELECT COUNT(*) FROM alertas_seguranca WHERE resolvido = FALSE) AS alertas, " +
                     "(SELECT COUNT(*) FROM sessoes_ativas WHERE ativa = TRUE) AS sessoes";
        
        try (Connection conn = Conexao.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            if (rs.next()) {
                sb.append("Tentativas de login falhas (24h): ").append(rs.getInt("falhas")).append("\n");
                sb.append("Usuários/IPs bloqueados: ").append(rs.getInt("bloqueados")).append("\n");
                sb.append("Alertas pendentes: ").append(rs.getInt("alertas")).append("\n");
                sb.append("Sessões ativas: ").append(rs.getInt("sessoes")).append("\n");
            }
            
        } catch (SQLException e) {
            sb.append("Erro ao obter resumo: ").append(e.getMessage()).append("\n");
        }
        
        sb.append("==========================================\n");
        return sb.toString();
    }
    
    /**
     * Verifica se as tabelas de segurança existem
     */
    public boolean tabelasExistem() {
        String sql = "SELECT 1 FROM auditoria_log LIMIT 1";
        
        try (Connection conn = Conexao.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            return true;
            
        } catch (SQLException e) {
            return false;
        }
    }
}
