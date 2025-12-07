-- ============================================================
-- SCRIPT SQL - TABELAS DE IMPOSTOS E CONFIGURAÇÕES TRIBUTÁRIAS
-- Sistema de Controle Financeiro - Reforma Tributária 2026
-- Banco de Dados: prova1 (MySQL 8.0)
-- ============================================================

-- Usar o banco de dados
USE prova1;

-- ============================================================
-- 1. TABELA DE CONFIGURAÇÕES DO SISTEMA
-- ============================================================
CREATE TABLE IF NOT EXISTS sistema_config (
    id INT AUTO_INCREMENT PRIMARY KEY,
    chave VARCHAR(100) NOT NULL UNIQUE,
    valor VARCHAR(500) NOT NULL,
    descricao VARCHAR(255),
    tipo_dado ENUM('STRING', 'INTEGER', 'DECIMAL', 'BOOLEAN', 'JSON') DEFAULT 'STRING',
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    atualizado_por INT,
    FOREIGN KEY (atualizado_por) REFERENCES usuarios(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 2. TABELA DE ALÍQUOTAS DE IMPOSTOS (Reforma Tributária 2026)
-- ============================================================
-- IBS (Imposto sobre Bens e Serviços) - Substituirá ICMS e ISS
-- CBS (Contribuição sobre Bens e Serviços) - Substituirá PIS e COFINS
-- IS (Imposto Seletivo) - Produtos prejudiciais à saúde/meio ambiente

CREATE TABLE IF NOT EXISTS impostos_aliquotas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE COMMENT 'Código único do imposto',
    nome VARCHAR(100) NOT NULL COMMENT 'Nome do imposto',
    sigla VARCHAR(10) NOT NULL COMMENT 'Sigla (IBS, CBS, IS, etc)',
    aliquota_padrao DECIMAL(6,4) NOT NULL DEFAULT 0.0000 COMMENT 'Alíquota padrão em decimal (ex: 0.15 = 15%)',
    aliquota_reduzida DECIMAL(6,4) DEFAULT NULL COMMENT 'Alíquota reduzida para itens essenciais',
    aliquota_zero BOOLEAN DEFAULT FALSE COMMENT 'Se aplica alíquota zero',
    esfera ENUM('FEDERAL', 'ESTADUAL', 'MUNICIPAL', 'DUAL') NOT NULL COMMENT 'Esfera de competência',
    vigencia_inicio DATE NOT NULL COMMENT 'Data de início da vigência',
    vigencia_fim DATE DEFAULT NULL COMMENT 'Data de fim da vigência (NULL = indefinido)',
    base_legal VARCHAR(255) COMMENT 'Lei/norma que institui o imposto',
    ativo BOOLEAN DEFAULT TRUE,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_sigla (sigla),
    INDEX idx_esfera (esfera),
    INDEX idx_vigencia (vigencia_inicio, vigencia_fim)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 3. TABELA DE CATEGORIAS TRIBUTÁRIAS (NCM/NBS)
-- ============================================================
CREATE TABLE IF NOT EXISTS categorias_tributarias (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo_ncm VARCHAR(10) COMMENT 'Código NCM para produtos',
    codigo_nbs VARCHAR(10) COMMENT 'Código NBS para serviços',
    descricao VARCHAR(255) NOT NULL,
    tipo ENUM('PRODUTO', 'SERVICO') NOT NULL,
    regime_tributario ENUM('NORMAL', 'REDUZIDO', 'ISENTO', 'ZERO', 'SELETIVO') DEFAULT 'NORMAL',
    reducao_base DECIMAL(5,2) DEFAULT 0.00 COMMENT 'Percentual de redução da base de cálculo',
    observacao TEXT,
    ativo BOOLEAN DEFAULT TRUE,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 4. TABELA DE NOTAS FISCAIS
-- ============================================================
CREATE TABLE IF NOT EXISTS notas_fiscais (
    id INT AUTO_INCREMENT PRIMARY KEY,
    numero_nf VARCHAR(20) NOT NULL COMMENT 'Número da NF',
    serie VARCHAR(5) DEFAULT '1' COMMENT 'Série da NF',
    tipo_nf ENUM('NFE', 'NFCE', 'NFSE') NOT NULL DEFAULT 'NFE' COMMENT 'Tipo de nota fiscal',
    
    -- Dados do emitente
    emitente_cnpj VARCHAR(18) NOT NULL,
    emitente_razao_social VARCHAR(200) NOT NULL,
    emitente_nome_fantasia VARCHAR(200),
    emitente_endereco VARCHAR(255),
    emitente_cidade VARCHAR(100),
    emitente_uf CHAR(2),
    emitente_cep VARCHAR(10),
    emitente_inscricao_estadual VARCHAR(20),
    emitente_inscricao_municipal VARCHAR(20),
    
    -- Dados do destinatário
    destinatario_cpf_cnpj VARCHAR(18) NOT NULL,
    destinatario_nome VARCHAR(200) NOT NULL,
    destinatario_endereco VARCHAR(255),
    destinatario_cidade VARCHAR(100),
    destinatario_uf CHAR(2),
    destinatario_cep VARCHAR(10),
    destinatario_email VARCHAR(150),
    destinatario_telefone VARCHAR(20),
    
    -- Valores
    valor_produtos DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    valor_servicos DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    valor_desconto DECIMAL(15,2) DEFAULT 0.00,
    valor_frete DECIMAL(15,2) DEFAULT 0.00,
    valor_seguro DECIMAL(15,2) DEFAULT 0.00,
    valor_outras_despesas DECIMAL(15,2) DEFAULT 0.00,
    
    -- Impostos (Nova Reforma Tributária)
    base_calculo_ibs DECIMAL(15,2) DEFAULT 0.00 COMMENT 'Base de cálculo IBS',
    aliquota_ibs DECIMAL(6,4) DEFAULT 0.0000 COMMENT 'Alíquota IBS aplicada',
    valor_ibs DECIMAL(15,2) DEFAULT 0.00 COMMENT 'Valor do IBS',
    
    base_calculo_cbs DECIMAL(15,2) DEFAULT 0.00 COMMENT 'Base de cálculo CBS',
    aliquota_cbs DECIMAL(6,4) DEFAULT 0.0000 COMMENT 'Alíquota CBS aplicada',
    valor_cbs DECIMAL(15,2) DEFAULT 0.00 COMMENT 'Valor do CBS',
    
    base_calculo_is DECIMAL(15,2) DEFAULT 0.00 COMMENT 'Base de cálculo Imposto Seletivo',
    aliquota_is DECIMAL(6,4) DEFAULT 0.0000 COMMENT 'Alíquota IS aplicada',
    valor_is DECIMAL(15,2) DEFAULT 0.00 COMMENT 'Valor do IS',
    
    -- Impostos legados (período de transição 2026-2033)
    valor_icms DECIMAL(15,2) DEFAULT 0.00,
    valor_iss DECIMAL(15,2) DEFAULT 0.00,
    valor_pis DECIMAL(15,2) DEFAULT 0.00,
    valor_cofins DECIMAL(15,2) DEFAULT 0.00,
    
    valor_total_impostos DECIMAL(15,2) DEFAULT 0.00,
    valor_total_nf DECIMAL(15,2) NOT NULL,
    
    -- Controle
    data_emissao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_saida DATETIME,
    chave_acesso VARCHAR(44) COMMENT 'Chave de acesso da NF-e (44 dígitos)',
    protocolo_autorizacao VARCHAR(20),
    status ENUM('PENDENTE', 'AUTORIZADA', 'CANCELADA', 'DENEGADA', 'INUTILIZADA') DEFAULT 'PENDENTE',
    motivo_cancelamento TEXT,
    
    -- Referências
    id_usuario INT NOT NULL,
    id_transacao INT,
    
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id),
    FOREIGN KEY (id_transacao) REFERENCES transacoes(id) ON DELETE SET NULL,
    
    UNIQUE KEY uk_numero_serie (numero_nf, serie),
    INDEX idx_data_emissao (data_emissao),
    INDEX idx_destinatario (destinatario_cpf_cnpj),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 5. TABELA DE ITENS DA NOTA FISCAL
-- ============================================================
CREATE TABLE IF NOT EXISTS notas_fiscais_itens (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_nota_fiscal INT NOT NULL,
    numero_item INT NOT NULL COMMENT 'Sequencial do item na NF',
    
    codigo_produto VARCHAR(60),
    descricao VARCHAR(255) NOT NULL,
    ncm VARCHAR(10) COMMENT 'NCM do produto',
    cfop VARCHAR(4) COMMENT 'CFOP',
    unidade VARCHAR(6) DEFAULT 'UN',
    quantidade DECIMAL(15,4) NOT NULL DEFAULT 1.0000,
    valor_unitario DECIMAL(15,4) NOT NULL,
    valor_total DECIMAL(15,2) NOT NULL,
    valor_desconto DECIMAL(15,2) DEFAULT 0.00,
    
    -- Impostos do item
    base_ibs DECIMAL(15,2) DEFAULT 0.00,
    aliquota_ibs DECIMAL(6,4) DEFAULT 0.0000,
    valor_ibs DECIMAL(15,2) DEFAULT 0.00,
    
    base_cbs DECIMAL(15,2) DEFAULT 0.00,
    aliquota_cbs DECIMAL(6,4) DEFAULT 0.0000,
    valor_cbs DECIMAL(15,2) DEFAULT 0.00,
    
    FOREIGN KEY (id_nota_fiscal) REFERENCES notas_fiscais(id) ON DELETE CASCADE,
    INDEX idx_nota_item (id_nota_fiscal, numero_item)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 6. TABELA DE BOLETOS
-- ============================================================
CREATE TABLE IF NOT EXISTS boletos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    
    -- Dados do boleto (Padrão FEBRABAN/Banco Central)
    codigo_barras VARCHAR(44) NOT NULL COMMENT 'Código de barras (44 posições)',
    linha_digitavel VARCHAR(54) NOT NULL COMMENT 'Linha digitável (47 posições formatada)',
    nosso_numero VARCHAR(20) NOT NULL COMMENT 'Nosso número do banco',
    
    -- Banco
    codigo_banco VARCHAR(3) NOT NULL DEFAULT '001' COMMENT 'Código do banco (3 dígitos)',
    nome_banco VARCHAR(50) DEFAULT 'Banco do Brasil',
    agencia VARCHAR(10),
    conta VARCHAR(15),
    carteira VARCHAR(3) DEFAULT '17',
    
    -- Beneficiário (quem recebe)
    beneficiario_cpf_cnpj VARCHAR(18) NOT NULL,
    beneficiario_nome VARCHAR(200) NOT NULL,
    beneficiario_endereco VARCHAR(255),
    beneficiario_cidade_uf VARCHAR(100),
    
    -- Pagador (quem paga)
    pagador_cpf_cnpj VARCHAR(18) NOT NULL,
    pagador_nome VARCHAR(200) NOT NULL,
    pagador_endereco VARCHAR(255),
    pagador_cidade_uf VARCHAR(100),
    pagador_cep VARCHAR(10),
    
    -- Valores
    valor_documento DECIMAL(15,2) NOT NULL,
    valor_desconto DECIMAL(15,2) DEFAULT 0.00,
    valor_deducao DECIMAL(15,2) DEFAULT 0.00,
    valor_mora DECIMAL(15,2) DEFAULT 0.00,
    valor_acrescimo DECIMAL(15,2) DEFAULT 0.00,
    valor_cobrado DECIMAL(15,2),
    
    -- Datas
    data_documento DATE NOT NULL,
    data_vencimento DATE NOT NULL,
    data_processamento DATE DEFAULT (CURRENT_DATE),
    data_pagamento DATE,
    
    -- Instruções
    instrucao_1 VARCHAR(100) DEFAULT 'Não receber após vencimento',
    instrucao_2 VARCHAR(100) DEFAULT 'Multa de 2% após vencimento',
    instrucao_3 VARCHAR(100) DEFAULT 'Juros de 1% ao mês',
    demonstrativo TEXT COMMENT 'Demonstrativo de cobrança',
    
    -- Controle
    status ENUM('GERADO', 'ENVIADO', 'PAGO', 'VENCIDO', 'CANCELADO', 'PROTESTADO') DEFAULT 'GERADO',
    
    -- Referências
    id_usuario INT NOT NULL,
    id_nota_fiscal INT,
    id_transacao INT,
    
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id),
    FOREIGN KEY (id_nota_fiscal) REFERENCES notas_fiscais(id) ON DELETE SET NULL,
    FOREIGN KEY (id_transacao) REFERENCES transacoes(id) ON DELETE SET NULL,
    
    INDEX idx_vencimento (data_vencimento),
    INDEX idx_pagador (pagador_cpf_cnpj),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 7. TABELA DE CARTÕES (se não existir)
-- ============================================================
CREATE TABLE IF NOT EXISTS cartoes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    tipo ENUM('CREDITO', 'DEBITO', 'MULTIPLO') NOT NULL DEFAULT 'CREDITO',
    bandeira VARCHAR(20) NOT NULL COMMENT 'VISA, MASTERCARD, ELO, etc',
    numero_mascarado VARCHAR(19) NOT NULL COMMENT 'Número mascarado: **** **** **** 1234',
    numero_cripto TEXT NOT NULL COMMENT 'Número criptografado AES-256',
    cvv_cripto TEXT COMMENT 'CVV criptografado',
    nome_titular VARCHAR(100) NOT NULL,
    validade VARCHAR(7) NOT NULL COMMENT 'Formato MM/YYYY',
    token VARCHAR(64) UNIQUE COMMENT 'Token único para transações',
    limite DECIMAL(15,2) DEFAULT 0.00,
    limite_disponivel DECIMAL(15,2) DEFAULT 0.00,
    ativo BOOLEAN DEFAULT TRUE,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ultima_utilizacao TIMESTAMP,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id) ON DELETE CASCADE,
    INDEX idx_usuario_tipo (id_usuario, tipo),
    INDEX idx_token (token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 8. INSERIR DADOS INICIAIS - CONFIGURAÇÕES DO SISTEMA
-- ============================================================
INSERT INTO sistema_config (chave, valor, descricao, tipo_dado) VALUES
-- Taxas de Impostos (Reforma Tributária 2026)
('TAXA_IBS', '15.00', 'Alíquota do IBS - Imposto sobre Bens e Serviços (%)', 'DECIMAL'),
('TAXA_CBS', '8.80', 'Alíquota da CBS - Contribuição sobre Bens e Serviços (%)', 'DECIMAL'),
('TAXA_IS', '0.00', 'Alíquota do IS - Imposto Seletivo (%)', 'DECIMAL'),
('TAXA_IBS_REDUZIDA', '7.50', 'Alíquota IBS reduzida para itens essenciais (%)', 'DECIMAL'),
('TAXA_CBS_REDUZIDA', '4.40', 'Alíquota CBS reduzida para itens essenciais (%)', 'DECIMAL'),

-- Taxas de Serviço
('TAXA_SERVICO', '10.00', 'Taxa de serviço padrão (%)', 'DECIMAL'),
('TAXA_MULTA_ATRASO', '2.00', 'Multa por atraso no pagamento (%)', 'DECIMAL'),
('TAXA_JUROS_MES', '1.00', 'Juros ao mês por atraso (%)', 'DECIMAL'),

-- Dados do Emissor (Empresa)
('EMISSOR_CNPJ', '00.000.000/0001-00', 'CNPJ do emissor', 'STRING'),
('EMISSOR_RAZAO_SOCIAL', 'CONTROLE FINANCEIRO LTDA', 'Razão social do emissor', 'STRING'),
('EMISSOR_NOME_FANTASIA', 'Controle Financeiro', 'Nome fantasia', 'STRING'),
('EMISSOR_ENDERECO', 'Rua das Finanças, 123', 'Endereço do emissor', 'STRING'),
('EMISSOR_CIDADE', 'Teresina', 'Cidade do emissor', 'STRING'),
('EMISSOR_UF', 'PI', 'UF do emissor', 'STRING'),
('EMISSOR_CEP', '64000-000', 'CEP do emissor', 'STRING'),
('EMISSOR_IE', 'ISENTO', 'Inscrição Estadual', 'STRING'),
('EMISSOR_IM', '12345678', 'Inscrição Municipal', 'STRING'),
('EMISSOR_EMAIL', 'contato@controlefinanceiro.com.br', 'Email do emissor', 'STRING'),
('EMISSOR_TELEFONE', '(86) 3333-4444', 'Telefone do emissor', 'STRING'),

-- Dados Bancários para Boleto
('BANCO_CODIGO', '001', 'Código do banco (Banco do Brasil)', 'STRING'),
('BANCO_NOME', 'Banco do Brasil S.A.', 'Nome do banco', 'STRING'),
('BANCO_AGENCIA', '1234-5', 'Agência', 'STRING'),
('BANCO_CONTA', '12345678-9', 'Conta corrente', 'STRING'),
('BANCO_CARTEIRA', '17', 'Carteira de cobrança', 'STRING'),
('BANCO_CONVENIO', '1234567', 'Código de convênio', 'STRING'),

-- Configurações de NF
('NF_SERIE', '1', 'Série da Nota Fiscal', 'STRING'),
('NF_ULTIMO_NUMERO', '0', 'Último número de NF emitida', 'INTEGER'),
('NF_AMBIENTE', 'HOMOLOGACAO', 'Ambiente: PRODUCAO ou HOMOLOGACAO', 'STRING')

ON DUPLICATE KEY UPDATE valor = VALUES(valor), atualizado_em = CURRENT_TIMESTAMP;

-- ============================================================
-- 9. INSERIR ALÍQUOTAS DE IMPOSTOS (Reforma Tributária 2026-2033)
-- ============================================================
INSERT INTO impostos_aliquotas (codigo, nome, sigla, aliquota_padrao, aliquota_reduzida, esfera, vigencia_inicio, base_legal) VALUES
-- IBS - Imposto sobre Bens e Serviços (substitui ICMS + ISS)
('IBS_ESTADUAL', 'Imposto sobre Bens e Serviços - Estadual', 'IBS', 0.1500, 0.0750, 'ESTADUAL', '2026-01-01', 'EC 132/2023 - Reforma Tributária'),
('IBS_MUNICIPAL', 'Imposto sobre Bens e Serviços - Municipal', 'IBS', 0.0500, 0.0250, 'MUNICIPAL', '2026-01-01', 'EC 132/2023 - Reforma Tributária'),

-- CBS - Contribuição sobre Bens e Serviços (substitui PIS + COFINS)
('CBS_FEDERAL', 'Contribuição sobre Bens e Serviços', 'CBS', 0.0880, 0.0440, 'FEDERAL', '2026-01-01', 'EC 132/2023 - Reforma Tributária'),

-- IS - Imposto Seletivo (produtos prejudiciais)
('IS_FEDERAL', 'Imposto Seletivo', 'IS', 0.0000, NULL, 'FEDERAL', '2026-01-01', 'EC 132/2023 - Art. 153, VIII'),

-- Impostos em transição (serão gradualmente extintos até 2033)
('ICMS', 'Imposto sobre Circulação de Mercadorias', 'ICMS', 0.1800, 0.1200, 'ESTADUAL', '2000-01-01', 'CF/88 Art. 155, II'),
('ISS', 'Imposto sobre Serviços', 'ISS', 0.0500, 0.0200, 'MUNICIPAL', '2000-01-01', 'LC 116/2003'),
('PIS', 'Programa de Integração Social', 'PIS', 0.0165, 0.0065, 'FEDERAL', '2000-01-01', 'LC 7/1970'),
('COFINS', 'Contribuição para Financiamento da Seguridade Social', 'COFINS', 0.0760, 0.0300, 'FEDERAL', '2000-01-01', 'LC 70/1991')

ON DUPLICATE KEY UPDATE aliquota_padrao = VALUES(aliquota_padrao), atualizado_em = CURRENT_TIMESTAMP;

-- ============================================================
-- 10. INSERIR CATEGORIAS TRIBUTÁRIAS BÁSICAS
-- ============================================================
INSERT INTO categorias_tributarias (codigo_ncm, descricao, tipo, regime_tributario) VALUES
('00000000', 'Serviços em Geral', 'SERVICO', 'NORMAL'),
('84713012', 'Computadores e Notebooks', 'PRODUTO', 'NORMAL'),
('85171231', 'Smartphones', 'PRODUTO', 'NORMAL'),
('22021000', 'Refrigerantes e Bebidas', 'PRODUTO', 'SELETIVO'),
('24012090', 'Cigarros e Derivados do Tabaco', 'PRODUTO', 'SELETIVO'),
('02011000', 'Carne Bovina', 'PRODUTO', 'REDUZIDO'),
('10063021', 'Arroz', 'PRODUTO', 'ISENTO'),
('07132090', 'Feijão', 'PRODUTO', 'ISENTO'),
('15079090', 'Óleo de Soja', 'PRODUTO', 'REDUZIDO')

ON DUPLICATE KEY UPDATE descricao = VALUES(descricao);

-- ============================================================
-- 11. VIEWS ÚTEIS
-- ============================================================

-- View de impostos vigentes
CREATE OR REPLACE VIEW vw_impostos_vigentes AS
SELECT 
    id, codigo, nome, sigla, 
    aliquota_padrao * 100 AS aliquota_percentual,
    aliquota_reduzida * 100 AS aliquota_reduzida_percentual,
    esfera, vigencia_inicio, base_legal
FROM impostos_aliquotas
WHERE ativo = TRUE 
  AND vigencia_inicio <= CURRENT_DATE 
  AND (vigencia_fim IS NULL OR vigencia_fim >= CURRENT_DATE);

-- View de resumo de notas fiscais
CREATE OR REPLACE VIEW vw_notas_fiscais_resumo AS
SELECT 
    nf.id,
    nf.numero_nf,
    nf.data_emissao,
    nf.destinatario_nome,
    nf.valor_total_nf,
    nf.valor_ibs,
    nf.valor_cbs,
    nf.valor_total_impostos,
    nf.status,
    u.nome AS usuario_nome
FROM notas_fiscais nf
JOIN usuarios u ON nf.id_usuario = u.id
ORDER BY nf.data_emissao DESC;

-- ============================================================
-- 12. PROCEDURES AUXILIARES
-- ============================================================

DELIMITER //

-- Procedure para calcular impostos de uma transação
CREATE PROCEDURE IF NOT EXISTS sp_calcular_impostos(
    IN p_valor_base DECIMAL(15,2),
    IN p_tipo_produto VARCHAR(20),
    OUT p_valor_ibs DECIMAL(15,2),
    OUT p_valor_cbs DECIMAL(15,2),
    OUT p_valor_is DECIMAL(15,2),
    OUT p_total_impostos DECIMAL(15,2)
)
BEGIN
    DECLARE v_aliq_ibs DECIMAL(6,4);
    DECLARE v_aliq_cbs DECIMAL(6,4);
    DECLARE v_aliq_is DECIMAL(6,4) DEFAULT 0;
    
    -- Buscar alíquotas vigentes
    SELECT aliquota_padrao INTO v_aliq_ibs 
    FROM impostos_aliquotas WHERE codigo = 'IBS_ESTADUAL' AND ativo = TRUE LIMIT 1;
    
    SELECT aliquota_padrao INTO v_aliq_cbs 
    FROM impostos_aliquotas WHERE codigo = 'CBS_FEDERAL' AND ativo = TRUE LIMIT 1;
    
    -- Se for produto seletivo, adicionar IS
    IF p_tipo_produto = 'SELETIVO' THEN
        SET v_aliq_is = 0.25; -- 25% para produtos seletivos
    END IF;
    
    -- Calcular impostos
    SET p_valor_ibs = ROUND(p_valor_base * IFNULL(v_aliq_ibs, 0.15), 2);
    SET p_valor_cbs = ROUND(p_valor_base * IFNULL(v_aliq_cbs, 0.088), 2);
    SET p_valor_is = ROUND(p_valor_base * v_aliq_is, 2);
    SET p_total_impostos = p_valor_ibs + p_valor_cbs + p_valor_is;
END //

-- Procedure para gerar próximo número de NF
CREATE PROCEDURE IF NOT EXISTS sp_proximo_numero_nf(OUT p_numero VARCHAR(20))
BEGIN
    DECLARE v_ultimo INT;
    
    SELECT CAST(valor AS UNSIGNED) INTO v_ultimo 
    FROM sistema_config WHERE chave = 'NF_ULTIMO_NUMERO';
    
    SET v_ultimo = IFNULL(v_ultimo, 0) + 1;
    
    UPDATE sistema_config SET valor = v_ultimo WHERE chave = 'NF_ULTIMO_NUMERO';
    
    SET p_numero = LPAD(v_ultimo, 9, '0');
END //

DELIMITER ;

-- ============================================================
-- VERIFICAÇÃO FINAL
-- ============================================================
SELECT 'Script executado com sucesso!' AS status;
SELECT COUNT(*) AS total_configs FROM sistema_config;
SELECT COUNT(*) AS total_impostos FROM impostos_aliquotas;
SELECT COUNT(*) AS total_categorias FROM categorias_tributarias;

-- Mostrar configurações de impostos
SELECT chave, valor, descricao 
FROM sistema_config 
WHERE chave LIKE 'TAXA_%'
ORDER BY chave;
