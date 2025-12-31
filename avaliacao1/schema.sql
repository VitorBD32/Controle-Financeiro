-- Script para criar o banco de dados e tabelas do Controle Financeiro
-- Execute este script no MySQL Workbench

-- Criar banco de dados
CREATE DATABASE IF NOT EXISTS PROVA1 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE PROVA1;

-- Tabela de usuários
CREATE TABLE IF NOT EXISTS usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    is_admin BOOLEAN DEFAULT FALSE,
    autorizado BOOLEAN DEFAULT TRUE,
    criado_em DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabela de categorias
CREATE TABLE IF NOT EXISTS categorias (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    tipo VARCHAR(10) DEFAULT 'D',
    descricao VARCHAR(255),
    criado_em DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabela de transações
CREATE TABLE IF NOT EXISTS transacoes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    id_categoria INT NOT NULL,
    descricao VARCHAR(255),
    valor DECIMAL(13,2) NOT NULL,
    tipo VARCHAR(10) NOT NULL,
    data DATETIME NOT NULL,
    criado_em DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (id_categoria) REFERENCES categorias(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabela de cartões (dados sensíveis criptografados com AES-256)
CREATE TABLE IF NOT EXISTS cartoes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    token VARCHAR(64) NOT NULL UNIQUE,
    numero_mascarado VARCHAR(20) NOT NULL,
    numero_cripto TEXT NOT NULL,
    nome_titular VARCHAR(100) NOT NULL,
    validade VARCHAR(7) NOT NULL,
    cvv_cripto TEXT NOT NULL,
    bandeira VARCHAR(30) NOT NULL,
    tipo ENUM('CREDITO', 'DEBITO', 'AMBOS') NOT NULL DEFAULT 'CREDITO',
    apelido VARCHAR(50),
    ativo BOOLEAN DEFAULT TRUE,
    data_cadastro DATETIME DEFAULT CURRENT_TIMESTAMP,
    ultimo_uso DATETIME,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id) ON DELETE CASCADE,
    INDEX idx_cartoes_usuario (id_usuario),
    INDEX idx_cartoes_token (token),
    INDEX idx_cartoes_ativo (ativo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabela de configurações do sistema
CREATE TABLE IF NOT EXISTS sistema_config (
    chave VARCHAR(50) PRIMARY KEY,
    valor TEXT NOT NULL,
    descricao VARCHAR(255),
    atualizado_em DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Índices para performance
CREATE INDEX idx_transacoes_usuario ON transacoes(id_usuario);
CREATE INDEX idx_transacoes_categoria ON transacoes(id_categoria);
CREATE INDEX idx_transacoes_data ON transacoes(data);

-- Configurações padrão do sistema
INSERT INTO sistema_config (chave, valor, descricao) VALUES
    ('taxa_pix', '0', 'Taxa para pagamentos PIX (%)'),
    ('taxa_credito', '2.99', 'Taxa para pagamentos com cartão de crédito (%)'),
    ('taxa_debito', '1.49', 'Taxa para pagamentos com cartão de débito (%)'),
    ('taxa_boleto', '1.00', 'Taxa fixa para boletos (R$)'),
    ('limite_transacao', '5000.00', 'Limite máximo por transação (R$)'),
    ('criptografia_algoritmo', 'AES-256-GCM', 'Algoritmo de criptografia para dados sensíveis'),
    ('versao_sistema', '2.0.0', 'Versão atual do sistema')
ON DUPLICATE KEY UPDATE valor = VALUES(valor);

-- Dados de exemplo
INSERT INTO usuarios (nome, email, senha, is_admin) VALUES
    ('Admin', 'admin@controle.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIHgYqBbAZ5sFzKqW3JCgpLCzNsxKkDi', TRUE),
    ('User Example', 'user@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIHgYqBbAZ5sFzKqW3JCgpLCzNsxKkDi', FALSE),
    ('Maria Santos', 'maria@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIHgYqBbAZ5sFzKqW3JCgpLCzNsxKkDi', FALSE)
ON DUPLICATE KEY UPDATE nome = VALUES(nome);

INSERT INTO categorias (nome, tipo, descricao) VALUES
    ('Salário', 'R', 'Receita mensal'),
    ('Alimentação', 'D', 'Gastos com comida'),
    ('Transporte', 'D', 'Gastos com transporte'),
    ('Pagamentos', 'D', 'Pagamentos diversos')
ON DUPLICATE KEY UPDATE descricao = VALUES(descricao);

INSERT INTO transacoes (id_usuario, id_categoria, descricao, valor, tipo, data) VALUES
    (1, 1, 'Salário mensal', 3500.00, 'R', NOW()),
    (1, 2, 'Supermercado', 250.50, 'D', NOW()),
    (1, 3, 'Uber', 45.00, 'D', NOW())
ON DUPLICATE KEY UPDATE descricao = VALUES(descricao);
