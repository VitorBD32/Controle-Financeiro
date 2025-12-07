-- ============================================================
-- SCRIPT SQL: TABELAS DE AUDITORIA E SEGURANÇA (SIMPLIFICADO)
-- Banco de Dados: PROVA1 (MySQL 8.0)
-- ============================================================

USE PROVA1;

-- ============================================================
-- TABELA DE LOG DE AUDITORIA
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
    INDEX idx_acao (acao)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABELA DE TENTATIVAS DE LOGIN
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
    INDEX idx_data (data_hora)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABELA DE SESSÕES ATIVAS
-- ============================================================

CREATE TABLE IF NOT EXISTS sessoes_ativas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    session_token VARCHAR(100) NOT NULL UNIQUE,
    ip_address VARCHAR(45),
    user_agent TEXT,
    data_inicio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_ultimo_acesso TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    data_expiracao TIMESTAMP NULL,
    ativa BOOLEAN DEFAULT TRUE,
    
    INDEX idx_usuario (usuario_id),
    INDEX idx_token (session_token),
    INDEX idx_ativa (ativa)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABELA DE BLOQUEIOS DE SEGURANÇA
-- ============================================================

CREATE TABLE IF NOT EXISTS bloqueios_seguranca (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tipo ENUM('IP', 'USUARIO', 'EMAIL') NOT NULL,
    valor VARCHAR(255) NOT NULL,
    motivo VARCHAR(255),
    data_bloqueio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_expiracao TIMESTAMP NULL,
    bloqueado_por INT,
    ativo BOOLEAN DEFAULT TRUE,
    
    INDEX idx_tipo_valor (tipo, valor),
    INDEX idx_ativo (ativo),
    
    UNIQUE KEY unique_bloqueio (tipo, valor)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABELA DE DADOS SENSÍVEIS CRIPTOGRAFADOS
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
    INDEX idx_tipo (tipo_dado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABELA DE ALERTAS DE SEGURANÇA
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
    data_resolucao TIMESTAMP NULL,
    
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
-- VERIFICAÇÃO FINAL
-- ============================================================

SELECT '✅ Tabelas de segurança criadas com sucesso!' AS STATUS;

SELECT TABLE_NAME AS 'Tabela', TABLE_ROWS AS 'Registros'
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'PROVA1' 
  AND TABLE_NAME IN ('auditoria_log', 'login_attempts', 'sessoes_ativas', 
                     'bloqueios_seguranca', 'dados_sensiveis', 'alertas_seguranca',
                     'sistema_config', 'usuarios', 'transacoes', 'categorias')
ORDER BY TABLE_NAME;
