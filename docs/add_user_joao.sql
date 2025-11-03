-- Arquivo: docs/add_user_joao.sql
-- Finalidade: comandos SQL de exemplo para cadastrar o usuário JOAO no banco do servidor.
-- Atenção: ajuste nomes de tabela/colunas conforme o schema real do servidor antes de executar.

-- Opção A1: Inserção usando MD5 (legado) - útil para servidores antigos
-- OBS: MD5 é fraco; use apenas se o servidor espera esse formato.
INSERT INTO usuarios (login, senha, nome, email) 
VALUES ('JOAO', MD5('1234'), 'João da Silva', 'joao@exemplo.com');

-- Opção A2: Inserção usando hash bcrypt (recomendado se o servidor aceitar password_hash do PHP)
-- Gere o hash bcrypt no servidor (ex.: em PHP: password_hash('1234', PASSWORD_BCRYPT)) e cole o valor abaixo.
-- Exemplo (substitua '$2y$...hash...' pelo hash gerado):
-- INSERT INTO usuarios (login, senha, nome, email) 
-- VALUES ('JOAO', '$2y$10$.......................................', 'João da Silva', 'joao@exemplo.com');

-- Nota: se a tabela de usuários tiver campos diferentes (por exemplo id, username, password, fullname), adapte os nomes.
