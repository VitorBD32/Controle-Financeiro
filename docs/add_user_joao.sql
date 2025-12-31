-- Arquivo: docs/add_user_joao.sql (renamed to add_user_example.sql)
-- Este arquivo remanescente foi mantido por compatibilidade, mas contém apenas exemplos neutros
-- Use preferencialmente: docs/add_user_example.sql

INSERT INTO usuarios (login, senha, nome, email) 
VALUES ('YOUR_USER', MD5('YOUR_PASSWORD'), 'User Example', 'user@example.com');


VALUES ('YOUR_USER', MD5('YOUR_PASSWORD'), 'User Example', 'user@example.com');
-- VALUES ('YOUR_USER', '$2y$10$.......................................', 'User Example', 'user@example.com');

-- Nota: se a tabela de usuários tiver campos diferentes (por exemplo id, username, password, fullname), adapte os nomes.
