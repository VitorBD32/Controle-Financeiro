-- ============================================================
-- SCRIPT: Criação de tabela de cache de alíquotas de tributos
-- ============================================================
-- Autor: Sistema de Controle Financeiro
-- Data: 2025-12-30
-- Objetivo: Armazenar cache local de alíquotas (CBS, IBS, IS)
--           por UF e município para reduzir chamadas à API externa
-- ============================================================

USE PROVA1;

-- Tabela de cache de alíquotas
CREATE TABLE IF NOT EXISTS cache_aliquotas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    uf VARCHAR(2) NOT NULL COMMENT 'Sigla do estado (ex: PI, SP)',
    municipio VARCHAR(100) DEFAULT NULL COMMENT 'Nome ou código IBGE do município',
    sigla_imposto VARCHAR(10) NOT NULL COMMENT 'Sigla do imposto: CBS, IBS, IS',
    aliquota DECIMAL(10,4) NOT NULL COMMENT 'Alíquota em decimal (ex: 0.088 = 8.8%)',
    data_atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Data da última atualização',
    fonte VARCHAR(100) DEFAULT 'API' COMMENT 'Fonte da informação: API, MANUAL, SEFAZ',
    observacao TEXT DEFAULT NULL COMMENT 'Observações ou detalhes sobre a alíquota',
    UNIQUE KEY uk_cache (uf, municipio, sigla_imposto),
    INDEX idx_uf (uf),
    INDEX idx_atualizacao (data_atualizacao)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Cache de alíquotas de tributos (CBS, IBS, IS) por UF e município';

-- Inserir alíquotas padrão (fallback nacional)
INSERT IGNORE INTO cache_aliquotas (uf, municipio, sigla_imposto, aliquota, fonte, observacao) VALUES
-- Alíquotas nacionais padrão (CBS é federal, uniforme)
('BR', NULL, 'CBS', 0.0880, 'MANUAL', 'Contribuição sobre Bens e Serviços - Alíquota padrão nacional (EC 132/2023)'),
('BR', NULL, 'IBS', 0.1500, 'MANUAL', 'Imposto sobre Bens e Serviços - Alíquota padrão nacional estimada'),
('BR', NULL, 'IS', 0.0000, 'MANUAL', 'Imposto Seletivo - Aplicável apenas a produtos específicos');

-- Alíquotas por estado (exemplos - ajustar conforme legislação real)
INSERT IGNORE INTO cache_aliquotas (uf, municipio, sigla_imposto, aliquota, fonte, observacao) VALUES
-- Piauí
('PI', NULL, 'IBS', 0.1400, 'MANUAL', 'IBS estimado para Piauí'),
('PI', 'Teresina', 'IBS', 0.1420, 'MANUAL', 'IBS para capital Teresina'),

-- São Paulo
('SP', NULL, 'IBS', 0.1700, 'MANUAL', 'IBS estimado para São Paulo (estado mais industrializado)'),
('SP', 'São Paulo', 'IBS', 0.1750, 'MANUAL', 'IBS para capital São Paulo'),

-- Rio de Janeiro
('RJ', NULL, 'IBS', 0.1650, 'MANUAL', 'IBS estimado para Rio de Janeiro'),
('RJ', 'Rio de Janeiro', 'IBS', 0.1680, 'MANUAL', 'IBS para capital Rio de Janeiro'),

-- Maranhão
('MA', NULL, 'IBS', 0.1380, 'MANUAL', 'IBS estimado para Maranhão'),
('MA', 'São Luís', 'IBS', 0.1400, 'MANUAL', 'IBS para capital São Luís'),

-- Ceará
('CE', NULL, 'IBS', 0.1420, 'MANUAL', 'IBS estimado para Ceará'),
('CE', 'Fortaleza', 'IBS', 0.1450, 'MANUAL', 'IBS para capital Fortaleza'),

-- Minas Gerais
('MG', NULL, 'IBS', 0.1580, 'MANUAL', 'IBS estimado para Minas Gerais'),
('MG', 'Belo Horizonte', 'IBS', 0.1600, 'MANUAL', 'IBS para capital Belo Horizonte'),

-- Imposto Seletivo (IS) - produtos específicos
('BR', NULL, 'IS_BEBIDA_ALCOOLICA', 0.2500, 'MANUAL', 'IS para bebidas alcoólicas'),
('BR', NULL, 'IS_CIGARRO', 0.3000, 'MANUAL', 'IS para cigarros e derivados do tabaco'),
('BR', NULL, 'IS_VEICULO_LUXO', 0.1500, 'MANUAL', 'IS para veículos de luxo'),
('BR', NULL, 'IS_COMBUSTIVEL', 0.1000, 'MANUAL', 'IS para combustíveis fósseis');

-- Exibir resumo
SELECT 
    'Cache de alíquotas criado e populado com sucesso!' AS status,
    COUNT(*) AS total_registros
FROM cache_aliquotas;

SELECT 
    uf,
    municipio,
    sigla_imposto,
    CONCAT(ROUND(aliquota * 100, 2), '%') AS aliquota_percentual,
    fonte,
    DATE_FORMAT(data_atualizacao, '%d/%m/%Y %H:%i') AS ultima_atualizacao
FROM cache_aliquotas
ORDER BY uf, municipio, sigla_imposto;
