# Instruções rápidas para o professor / DBA — Opção A (Cadastrar JOAO)

Este arquivo contém tudo pronto para o professor ou administrador do servidor aplicar a "Opção A": adicionar o usuário JOAO no banco de dados que o endpoint `syncjava.php` consulta, e verificar a autenticação via HTTP.

IMPORTANTE: eu não tenho acesso ao servidor remoto. Estas instruções devem ser executadas por alguém com acesso ao banco de dados do servidor (DBA / professor).

---

## 1) SQL pronto (opções)

Local: `docs/add_user_joao.sql` (já incluído no repositório). Copie e adapte se necessário.

Opções fornecidas:

- Variante A1 — MD5 (legado)

```sql
-- Ajuste o nome da tabela/colunas se o schema do servidor diferir
INSERT INTO usuarios (login, senha, nome, email)
VALUES ('JOAO', MD5('1234'), 'João da Silva', 'joao23@gmail.com');
```

- Variante A2 — bcrypt (recomendado se o servidor usa password_hash do PHP)

1. Gere o hash bcrypt no servidor (ex.: PHP):

```php
<?php
echo password_hash('1234', PASSWORD_BCRYPT) . "\n";
?>
```

2. Cole o hash no INSERT (substitua `$2y$...`):

```sql
INSERT INTO usuarios (login, senha, nome, email)
VALUES ('JOAO', '$2y$10$...HASH-GERADO-AQUI...', 'João da Silva', 'joao23@gmail.com');
```

Observação: verifique o nome correto da tabela e das colunas — alguns servidores usam `username`/`password` em vez de `login`/`senha`.

---

## 2) Verificação rápida no servidor (SQL)

Após executar o INSERT, confirme com:

```sql
SELECT id_usuario, login, nome, email, senha FROM usuarios WHERE login = 'JOAO' OR email = 'joao23@gmail.com';
```

Copie a saída e confirme que a coluna `senha` contém o hash esperado (MD5 hex ou `$2y$` para bcrypt).

---

## 3) Teste HTTP direto (no servidor ou em máquina que possa alcançar o endpoint)

Recomendo usar PowerShell com `Invoke-RestMethod` (Windows) ou `curl` (Linux / Git Bash / Windows com curl.exe). Exemplos:

- PowerShell (recomendado):

```powershell
#$ para senha simples
$body = @{ email = 'joao23@gmail.com'; senha = '1234' }
Invoke-RestMethod -Uri 'http://www.datse.com.br/dev/syncjava.php' -Method Post -Body $body -ContentType 'application/x-www-form-urlencoded; charset=UTF-8' -Verbose

#$ para senha MD5 (se o servidor espera hash)
$body = @{ email = 'joao23@gmail.com'; senha = '81dc9bdb52d04dc20036dbd8313ed055' }
Invoke-RestMethod -Uri 'http://www.datse.com.br/dev/syncjava.php' -Method Post -Body $body -ContentType 'application/x-www-form-urlencoded; charset=UTF-8' -Verbose
```

- curl (exemplo literal; use `curl.exe` no Windows ou rode em Linux/Git Bash):

```bash
curl -v -X POST "http://www.datse.com.br/dev/syncjava.php" \
  -H "Content-Type: application/x-www-form-urlencoded; charset=UTF-8" \
  --data-urlencode "email=joao23@gmail.com" \
  --data-urlencode "senha=1234"

# ou MD5
curl -v -X POST "http://www.datse.com.br/dev/syncjava.php" \
  -H "Content-Type: application/x-www-form-urlencoded; charset=UTF-8" \
  --data-urlencode "email=joao23@gmail.com" \
  --data-urlencode "senha=81dc9bdb52d04dc20036dbd8313ed055"
```

Resposta esperada (quando OK): algo como `Login realizado com sucesso` ou outra mensagem definida pelo servidor. Se ainda retornar `Login invalido`, verifique o hash/stored password e qual campo é usado para autenticação.

---

## 4) Mensagem pronta para o professor / DBA (copiar e colar)

Segue uma sugestão de mensagem que você pode mandar por WhatsApp/Teams/Email ao professor:

"Olá professor, preciso cadastrar o usuário JOAO no banco usado pelo endpoint de sincronização `http://www.datse.com.br/dev/syncjava.php`. No repositório (controle-financeiro) adicionei um SQL de exemplo em `docs/add_user_joao.sql`. Poderia executar o INSERT abaixo no banco do servidor e me confirmar?\n\nINSERT INTO usuarios (login, senha, nome, email) VALUES ('JOAO', MD5('1234'), 'João da Silva', 'joao23@gmail.com');\n\nApós aplicar, favor executar o teste HTTP: `curl` ou `Invoke-RestMethod` para enviar `email=joao23@gmail.com` e `senha=1234` ao endpoint e me retornar o body/status para eu validar na aplicação. Obrigado." 

---

## 5) Notas de segurança e boas práticas

- MD5 é inseguro; bcrypt é preferível. Se possível, gere o hash bcrypt no servidor e armazene-o.\n- Nunca compartilhe senhas reais; os exemplos aqui usam `1234` apenas para testes.\n- Depois do teste, remova ou troque a senha de teste por um valor seguro.

---

Se quiser, eu vejo os logs do cliente (já mostram qual formato foi aceito) e adapto o cliente para preferir o formato que o servidor aceitar automaticamente. Quer que eu gere também uma versão do cliente que se registra automaticamente (via API) caso o endpoint suporte registro? Caso o professor aplique o SQL e você confirme, eu marco a tarefa como concluída no README/docs.
