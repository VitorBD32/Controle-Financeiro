-- ============================================================
-- SCRIPT SQL: TABELAS DE AUDITORIA E SEGURANÇA
-- Banco de Dados: PROVA1 (MySQL 8.0)
-- Proteção contra vazamento de dados
-- ============================================================

USE PROVA1;

-- ============================================================
-- TABELA DE LOG DE AUDITORIA
-- Registra todas as ações do sistema para rastreabilidade
-- ============================================================

CREATE TABLE IF NOT EXISTS auditoria_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    usuario_id INT,
    usuario_nome VARCHAR(100),
    ip_address VARCHAR(45),
    acao VARCHAR(50) NOT NULL,
    entidade VARCHAR(100),
    entidade_id INT,
    dados_anteriores JSON,
    dados_novos JSON,
    resultado ENUM('SUCESSO', 'FALHA', 'BLOQUEADO') DEFAULT 'SUCESSO',
    detalhes TEXT,
    session_token VARCHAR(100),
    
    INDEX idx_data_hora (data_hora),
    INDEX idx_usuario (usuario_id),
    INDEX idx_acao (acao),
    INDEX idx_entidade (entidade, entidade_id),
    
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABELA DE TENTATIVAS DE LOGIN
-- Proteção contra ataques de força bruta
-- ============================================================

CREATE TABLE IF NOT EXISTS login_attempts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    ip_address VARCHAR(45),
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sucesso BOOLEAN DEFAULT FALSE,
    motivo_falha VARCHAR(255),
    user_agent TEXT,
    
    INDEX idx_username (username),
    INDEX idx_ip (ip_address),
    INDEX idx_data (data_hora),
    INDEX idx_username_data (username, data_hora)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABELA DE SESSÕES ATIVAS
-- Controle de sessões para logout forçado se necessário
-- ============================================================

CREATE TABLE IF NOT EXISTS sessoes_ativas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    session_token VARCHAR(100) NOT NULL UNIQUE,
    ip_address VARCHAR(45),
    user_agent TEXT,
    data_inicio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_ultimo_acesso TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    data_expiracao TIMESTAMP,
    ativa BOOLEAN DEFAULT TRUE,
    
    INDEX idx_usuario (usuario_id),
    INDEX idx_token (session_token),
    INDEX idx_ativa (ativa),
    
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABELA DE BLOQUEIOS DE SEGURANÇA
-- Registra IPs e usuários bloqueados
-- ============================================================

CREATE TABLE IF NOT EXISTS bloqueios_seguranca (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tipo ENUM('IP', 'USUARIO', 'EMAIL') NOT NULL,
    valor VARCHAR(255) NOT NULL,
    motivo VARCHAR(255),
    data_bloqueio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_expiracao TIMESTAMP,
    bloqueado_por INT,
    ativo BOOLEAN DEFAULT TRUE,
    
    INDEX idx_tipo_valor (tipo, valor),
    INDEX idx_ativo (ativo),
    
    UNIQUE KEY unique_bloqueio (tipo, valor)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABELA DE DADOS SENSÍVEIS CRIPTOGRAFADOS
-- Armazena dados que precisam de criptografia
-- ============================================================

CREATE TABLE IF NOT EXISTS dados_sensiveis (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    tipo_dado ENUM('CARTAO', 'CONTA_BANCARIA', 'CPF', 'DOCUMENTO') NOT NULL,
    dados_criptografados TEXT NOT NULL,
    hash_verificacao VARCHAR(64),
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_usuario (usuario_id),
    INDEX idx_tipo (tipo_dado),
    
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABELA DE ALERTAS DE SEGURANÇA
-- Registra eventos suspeitos para análise
-- ============================================================

CREATE TABLE IF NOT EXISTS alertas_seguranca (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tipo_alerta ENUM('SQL_INJECTION', 'XSS', 'BRUTE_FORCE', 'ACESSO_NAO_AUTORIZADO', 
                     'DADOS_INVALIDOS', 'SESSAO_SUSPEITA', 'OUTRO') NOT NULL,
    severidade ENUM('BAIXA', 'MEDIA', 'ALTA', 'CRITICA') DEFAULT 'MEDIA',
    descricao TEXT,
    dados_suspeitos TEXT,
    ip_origem VARCHAR(45),
    usuario_id INT,
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolvido BOOLEAN DEFAULT FALSE,
    resolvido_por INT,
    data_resolucao TIMESTAMP,
    
    INDEX idx_tipo (tipo_alerta),
    INDEX idx_severidade (severidade),
    INDEX idx_data (data_hora),
    INDEX idx_resolvido (resolvido)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- VIEW: RESUMO DE SEGURANÇA
-- ============================================================

CREATE OR REPLACE VIEW vw_resumo_seguranca AS
SELECT 
    (SELECT COUNT(*) FROM login_attempts WHERE sucesso = FALSE AND data_hora > DATE_SUB(NOW(), INTERVAL 24 HOUR)) AS tentativas_falhas_24h,
    (SELECT COUNT(*) FROM login_attempts WHERE sucesso = TRUE AND data_hora > DATE_SUB(NOW(), INTERVAL 24 HOUR)) AS logins_sucesso_24h,
    (SELECT COUNT(*) FROM bloqueios_seguranca WHERE ativo = TRUE) AS usuarios_bloqueados,
    (SELECT COUNT(*) FROM alertas_seguranca WHERE resolvido = FALSE) AS alertas_pendentes,
    (SELECT COUNT(*) FROM sessoes_ativas WHERE ativa = TRUE) AS sessoes_ativas,
    (SELECT COUNT(*) FROM alertas_seguranca WHERE severidade = 'CRITICA' AND resolvido = FALSE) AS alertas_criticos;

-- ============================================================
-- PROCEDURES DE SEGURANÇA
-- ============================================================

-- Procedure para registrar tentativa de login
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS sp_registrar_login(
    IN p_username VARCHAR(100),
    IN p_ip VARCHAR(45),
    IN p_sucesso BOOLEAN,
    IN p_motivo VARCHAR(255),
    IN p_user_agent TEXT
)
BEGIN
    INSERT INTO login_attempts (username, ip_address, sucesso, motivo_falha, user_agent)
    VALUES (p_username, p_ip, p_sucesso, p_motivo, p_user_agent);
    
    -- Verifica se deve bloquear após 5 tentativas falhas em 15 minutos
    IF NOT p_sucesso THEN
        DECLARE tentativas INT;
        
        SELECT COUNT(*) INTO tentativas
        FROM login_attempts
        WHERE username = p_username
          AND sucesso = FALSE
          AND data_hora > DATE_SUB(NOW(), INTERVAL 15 MINUTE);
        
        IF tentativas >= 5 THEN
            INSERT INTO bloqueios_seguranca (tipo, valor, motivo, data_expiracao)
            VALUES ('USUARIO', p_username, 'Múltiplas tentativas de login falhas', DATE_ADD(NOW(), INTERVAL 30 MINUTE))
            ON DUPLICATE KEY UPDATE 
                motivo = 'Múltiplas tentativas de login falhas',
                data_expiracao = DATE_ADD(NOW(), INTERVAL 30 MINUTE),
                ativo = TRUE;
            
            INSERT INTO alertas_seguranca (tipo_alerta, severidade, descricao, ip_origem)
            VALUES ('BRUTE_FORCE', 'ALTA', CONCAT('Usuário bloqueado: ', p_username), p_ip);
        END IF;
    END IF;
END //
DELIMITER ;

-- Procedure para verificar se usuário está bloqueado
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS sp_verificar_bloqueio(
    IN p_tipo VARCHAR(20),
    IN p_valor VARCHAR(255),
    OUT p_bloqueado BOOLEAN
)
BEGIN
    SELECT EXISTS(
        SELECT 1 FROM bloqueios_seguranca 
        WHERE tipo = p_tipo 
          AND valor = p_valor 
          AND ativo = TRUE 
          AND (data_expiracao IS NULL OR data_expiracao > NOW())
    ) INTO p_bloqueado;
END //
DELIMITER ;

-- Procedure para limpar dados antigos de auditoria (manter últimos 90 dias)
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS sp_limpar_auditoria_antiga()
BEGIN
    DELETE FROM login_attempts WHERE data_hora < DATE_SUB(NOW(), INTERVAL 90 DAY);
    DELETE FROM auditoria_log WHERE data_hora < DATE_SUB(NOW(), INTERVAL 90 DAY);
    DELETE FROM sessoes_ativas WHERE ativa = FALSE AND data_inicio < DATE_SUB(NOW(), INTERVAL 30 DAY);
    
    -- Remove bloqueios expirados
    UPDATE bloqueios_seguranca SET ativo = FALSE 
    WHERE data_expiracao IS NOT NULL AND data_expiracao < NOW();
END //
DELIMITER ;

-- ============================================================
-- TRIGGER: Auditoria automática de alterações em usuarios
-- ============================================================

DELIMITER //
CREATE TRIGGER IF NOT EXISTS trg_audit_usuarios_update
AFTER UPDATE ON usuarios
FOR EACH ROW
BEGIN
    INSERT INTO auditoria_log (usuario_id, usuario_nome, acao, entidade, entidade_id, dados_anteriores, dados_novos, detalhes)
    VALUES (
        OLD.id,
        OLD.nome,
        'UPDATE',
        'usuarios',
        OLD.id,
        JSON_OBJECT('nome', OLD.nome, 'email', OLD.email, 'admin', OLD.admin),
        JSON_OBJECT('nome', NEW.nome, 'email', NEW.email, 'admin', NEW.admin),
        'Alteração de dados do usuário'
    );
END //
DELIMITER ;

-- ============================================================
-- TRIGGER: Auditoria automática de transações
-- ============================================================

DELIMITER //
CREATE TRIGGER IF NOT EXISTS trg_audit_transacoes_insert
AFTER INSERT ON transacoes
FOR EACH ROW
BEGIN
    INSERT INTO auditoria_log (usuario_id, acao, entidade, entidade_id, dados_novos, detalhes)
    VALUES (
        NEW.usuario_id,
        'INSERT',
        'transacoes',
        NEW.id,
        JSON_OBJECT('valor', NEW.valor, 'tipo', NEW.tipo, 'descricao', NEW.descricao),
        'Nova transação criada'
    );
END //
DELIMITER ;

DELIMITER //
CREATE TRIGGER IF NOT EXISTS trg_audit_transacoes_delete
BEFORE DELETE ON transacoes
FOR EACH ROW
BEGIN
    INSERT INTO auditoria_log (usuario_id, acao, entidade, entidade_id, dados_anteriores, detalhes)
    VALUES (
        OLD.usuario_id,
        'DELETE',
        'transacoes',
        OLD.id,
        JSON_OBJECT('valor', OLD.valor, 'tipo', OLD.tipo, 'descricao', OLD.descricao),
        'Transação excluída'
    );
END //
DELIMITER ;

-- ============================================================
-- EVENTO: Limpeza automática semanal
-- ============================================================

CREATE EVENT IF NOT EXISTS evt_limpeza_auditoria
ON SCHEDULE EVERY 1 WEEK
STARTS CURRENT_TIMESTAMP
DO
    CALL sp_limpar_auditoria_antiga();

-- ============================================================
-- VERIFICAÇÃO FINAL
-- ============================================================

SELECT 'Tabelas de segurança criadas com sucesso!' AS STATUS;

SELECT TABLE_NAME, TABLE_ROWS 
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'PROVA1' 
  AND TABLE_NAME IN ('auditoria_log', 'login_attempts', 'sessoes_ativas', 
                     'bloqueios_seguranca', 'dados_sensiveis', 'alertas_seguranca')
ORDER BY TABLE_NAME;
