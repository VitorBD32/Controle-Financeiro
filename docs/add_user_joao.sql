-- Arquivo: docs/add_user_joao.sql
-- Finalidade: comandos SQL de exemplo para cadastrar o usuário JOAO no banco do servidor.
-- Atenção: ajuste nomes de tabela/colunas conforme o schema real do servidor antes de executar.

-- Opção A1: Inserção usando MD5 (legado) - útil para servidores antigos
-- OBS: MD5 é fraco; use apenas se o servidor espera esse formato.
INSERT INTO usuarios (login, senha, nome, email) 
VALUES ('JOAO', MD5('YOUR_PASSWORD'), 'João da Silva', 'joao@exemplo.com');

-- Gere o hash bcrypt no servidor (ex.: em PHP: password_hash('YOUR_PASSWORD', PASSWORD_BCRYPT)) e cole o valor abaixo.
-- VALUES ('JOAO', '$2y$10$.......................................', 'João da Silva', 'joao@exemplo.com');

-- Nota: se a tabela de usuários tiver campos diferentes (por exemplo id, username, password, fullname), adapte os nomes.
