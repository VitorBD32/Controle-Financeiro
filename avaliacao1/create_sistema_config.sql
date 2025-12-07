-- ============================================================
-- SCRIPT SQL: CRIAÇÃO DA TABELA sistema_config
-- Banco de Dados: PROVA1 (MySQL 8.0)
-- Reforma Tributária 2026 - EC 132/2023
-- ============================================================
-- Execute este script no DBeaver para criar a tabela de configurações
-- ============================================================

USE PROVA1;

-- Tabela de Configurações do Sistema
CREATE TABLE IF NOT EXISTS sistema_config (
    id INT AUTO_INCREMENT PRIMARY KEY,
    chave VARCHAR(100) NOT NULL UNIQUE,
    valor TEXT,
    descricao VARCHAR(255),
    tipo ENUM('STRING', 'NUMBER', 'BOOLEAN', 'JSON') DEFAULT 'STRING',
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    atualizado_por VARCHAR(100),
    INDEX idx_chave (chave)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- CONFIGURAÇÕES DE IMPOSTOS - REFORMA TRIBUTÁRIA 2026
-- ============================================================

-- Novos Tributos (IVA-Dual)
INSERT INTO sistema_config (chave, valor, descricao, tipo) VALUES
('TAXA_CBS', '8.8', 'Contribuição sobre Bens e Serviços (Federal) - Substitui PIS/COFINS/IPI', 'NUMBER'),
('TAXA_IBS', '17.7', 'Imposto sobre Bens e Serviços (Estadual/Municipal) - Substitui ICMS/ISS', 'NUMBER'),
('TAXA_IS', '0.0', 'Imposto Seletivo - Produtos prejudiciais à saúde/meio ambiente', 'NUMBER')
ON DUPLICATE KEY UPDATE valor = VALUES(valor);

-- Tributos em Transição (serão extintos gradualmente até 2033)
INSERT INTO sistema_config (chave, valor, descricao, tipo) VALUES
('TAXA_ICMS', '18.0', 'ICMS - Imposto sobre Circulação de Mercadorias (em transição)', 'NUMBER'),
('TAXA_ISSQN', '5.0', 'ISS - Imposto Sobre Serviços (em transição)', 'NUMBER'),
('TAXA_PIS', '1.65', 'PIS - Programa de Integração Social (em transição)', 'NUMBER'),
('TAXA_COFINS', '7.6', 'COFINS - Contribuição para Financiamento da Seguridade Social (em transição)', 'NUMBER')
ON DUPLICATE KEY UPDATE valor = VALUES(valor);

-- ============================================================
-- CONFIGURAÇÕES DO EMISSOR (NF-e / NFS-e)
-- ============================================================

INSERT INTO sistema_config (chave, valor, descricao, tipo) VALUES
('EMISSOR_RAZAO_SOCIAL', 'UESPI - Universidade Estadual do Piauí', 'Razão Social do Emissor', 'STRING'),
('EMISSOR_NOME_FANTASIA', 'Controle Financeiro UESPI', 'Nome Fantasia', 'STRING'),
('EMISSOR_CNPJ', '06.517.387/0001-34', 'CNPJ do Emissor', 'STRING'),
('EMISSOR_IE', 'ISENTO', 'Inscrição Estadual', 'STRING'),
('EMISSOR_IM', '', 'Inscrição Municipal', 'STRING'),
('EMISSOR_ENDERECO', 'Rua João Cabral, 2231 - Pirajá', 'Endereço Completo', 'STRING'),
('EMISSOR_CIDADE', 'Teresina', 'Cidade', 'STRING'),
('EMISSOR_UF', 'PI', 'Estado (UF)', 'STRING'),
('EMISSOR_CEP', '64002-150', 'CEP', 'STRING'),
('EMISSOR_TELEFONE', '(86) 3213-5400', 'Telefone', 'STRING'),
('EMISSOR_EMAIL', 'contato@uespi.br', 'E-mail', 'STRING')
ON DUPLICATE KEY UPDATE valor = VALUES(valor);

-- ============================================================
-- CONFIGURAÇÕES BANCÁRIAS (BOLETOS)
-- ============================================================

INSERT INTO sistema_config (chave, valor, descricao, tipo) VALUES
('BANCO_CODIGO', '001', 'Código FEBRABAN do Banco', 'STRING'),
('BANCO_NOME', 'Banco do Brasil', 'Nome do Banco', 'STRING'),
('BANCO_AGENCIA', '3793-X', 'Número da Agência', 'STRING'),
('BANCO_CONTA', '12345-6', 'Número da Conta', 'STRING'),
('BANCO_CARTEIRA', '17', 'Carteira de Cobrança', 'STRING'),
('BANCO_CONVENIO', '1234567', 'Número do Convênio', 'STRING')
ON DUPLICATE KEY UPDATE valor = VALUES(valor);

-- ============================================================
-- CONFIGURAÇÕES DO SISTEMA
-- ============================================================

INSERT INTO sistema_config (chave, valor, descricao, tipo) VALUES
('NF_SERIE', '1', 'Série da Nota Fiscal', 'NUMBER'),
('NF_PROXIMO_NUMERO', '1', 'Próximo Número de NF', 'NUMBER'),
('NF_AMBIENTE', '2', 'Ambiente: 1=Produção, 2=Homologação', 'NUMBER'),
('SISTEMA_VERSAO', '2.0', 'Versão do Sistema', 'STRING'),
('SISTEMA_NOME', 'Controle Financeiro Premium', 'Nome do Sistema', 'STRING')
ON DUPLICATE KEY UPDATE valor = VALUES(valor);

-- ============================================================
-- PIX CONFIGURAÇÕES
-- ============================================================

INSERT INTO sistema_config (chave, valor, descricao, tipo) VALUES
('PIX_CHAVE_PADRAO', 'vitordebrito23@gmail.com', 'Chave PIX padrão para recebimentos', 'STRING'),
('PIX_NOME_RECEBEDOR', 'CONTROLE FINANCEIRO', 'Nome do recebedor nos pagamentos PIX', 'STRING'),
('PIX_CIDADE', 'TERESINA', 'Cidade para payload PIX', 'STRING')
ON DUPLICATE KEY UPDATE valor = VALUES(valor);

-- ============================================================
-- VERIFICAÇÃO
-- ============================================================

SELECT 'Tabela sistema_config criada e configurada com sucesso!' AS STATUS;
SELECT COUNT(*) AS TOTAL_CONFIGURACOES FROM sistema_config;

-- Exibe todas as configurações de impostos
SELECT 
    chave AS 'Configuração',
    valor AS 'Valor',
    descricao AS 'Descrição'
FROM sistema_config 
WHERE chave LIKE 'TAXA_%'
ORDER BY chave;
