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

-- Índices para performance
CREATE INDEX idx_transacoes_usuario ON transacoes(id_usuario);
CREATE INDEX idx_transacoes_categoria ON transacoes(id_categoria);
CREATE INDEX idx_transacoes_data ON transacoes(data);

-- Dados de exemplo
INSERT INTO usuarios (nome, email, senha) VALUES
    ('João Silva', 'joao@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye'),
    ('Maria Santos', 'maria@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye');

INSERT INTO categorias (nome, tipo, descricao) VALUES
    ('Salário', 'R', 'Receita mensal'),
    ('Alimentação', 'D', 'Gastos com comida'),
    ('Transporte', 'D', 'Gastos com transporte');

INSERT INTO transacoes (id_usuario, id_categoria, descricao, valor, tipo, data) VALUES
    (1, 1, 'Salário mensal', 3500.00, 'R', NOW()),
    (1, 2, 'Supermercado', 250.50, 'D', NOW()),
    (1, 3, 'Uber', 45.00, 'D', NOW());
