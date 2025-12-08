# Integração: Desktop ↔ PHP ↔ MySQL

## 📋 Visão Geral

Este documento descreve como o sistema desktop Java se comunica com os endpoints PHP (`syncjava.php` e `syncjava2.php`) e como esses endpoints acessam o banco de dados MySQL `prova1`.

## 🔄 Fluxo de Dados

```
┌─────────────────┐
│  Desktop Java   │
│  (controle-     │
│   financeiro)   │
└────────┬────────┘
         │ HTTP POST
         │ (username, password ou encrypted_data)
         ▼
┌─────────────────────────────────────┐
│  Apache/XAMPP                       │
│  ┌─────────────────────────────┐   │
│  │ syncjava.php / syncjava2.php│   │
│  │                             │   │
│  │ 1. Recebe credenciais       │   │
│  │ 2. Conecta ao MySQL         │◄──┼─── localhost:3306
│  │ 3. Valida contra usuarios   │   │
│  │ 4. Retorna JSON             │   │
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
         │
         ▼
┌─────────────────┐
│  MySQL          │
│  Database:      │
│  prova1         │
│                 │
│  Tabelas:       │
│  - usuarios     │
│  - categorias   │
│  - transacoes   │
└─────────────────┘
```

## 🗄️ Estrutura do Banco de Dados

### Database: `prova1`

#### Tabela: `usuarios`
```sql
CREATE TABLE usuarios (
    id INT(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Tabela: `categorias`
```sql
CREATE TABLE categorias (
    id INT(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    tipo ENUM('RECEITA', 'DESPESA') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Tabela: `transacoes`
```sql
CREATE TABLE transacoes (
    id INT(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT(11) NOT NULL,
    categoria_id INT(11) NOT NULL,
    descricao VARCHAR(255),
    valor DECIMAL(10,2) NOT NULL,
    data DATE NOT NULL,
    tipo ENUM('RECEITA', 'DESPESA') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    FOREIGN KEY (categoria_id) REFERENCES categorias(id)
);
```

## 🔐 Autenticação e Validação

Os endpoints PHP (`syncjava.php` e `syncjava2.php`) suportam múltiplos formatos de autenticação:

### 1. Form POST (campos plain)
```http
POST /syncjava.php HTTP/1.1
Content-Type: application/x-www-form-urlencoded

username=JOAO&password=YOUR_PASSWORD
```

**Variantes aceitas:**
- Campos de usuário: `username`, `user`, `login`, `email`, `nome`, `usuario`
- Campos de senha: `password`, `pass`, `senha`

### 2. Basic Authentication
```http
POST /syncjava.php HTTP/1.1
Authorization: Basic Sm9hbzoxMjM0
```

### 3. Encrypted Payload (fallback)
```http
POST /syncjava.php HTTP/1.1
Content-Type: application/x-www-form-urlencoded

encrypted_data=base64encodeddata&salt=randomsalt&client_id=desktop
```

## 🔑 Validação de Senha

A função `pw_match()` no PHP tenta três métodos:

1. **Plain text** (para testes): `$provided === $stored_hash`
2. **MD5**: `md5($provided) === $stored_hash`
3. **bcrypt**: `password_verify($provided, $stored_hash)`

**Recomendação:** Em produção, use sempre bcrypt. Para criar hash bcrypt:

```php
$hash = password_hash('YOUR_PASSWORD', PASSWORD_BCRYPT);
// Retorna: $2y$10$...
```

```sql
-- Atualizar senha de JOAO para bcrypt:
UPDATE usuarios SET senha = '$2y$10$YourBcryptHashHere' WHERE nome = 'JOAO';
```

## 📡 Respostas JSON

### Sucesso (HTTP 200)
```json
{
    "ok": true,
    "auth_ok": true,
    "endpoint": "/syncjava.php",
    "user": {
        "id": 1,
        "nome": "JOAO",
        "email": "joao23@gmail.com"
    },
    "received": {
        "encrypted_data_length": 0,
        "salt": null,
        "client_id": "desktop"
    },
    "message": "Sincronização aceita"
}
```

### Falha (HTTP 401)
```json
{
    "ok": false,
    "auth_ok": false,
    "endpoint": "/syncjava.php",
    "user": null,
    "received": {
        "encrypted_data_length": 0,
        "salt": null,
        "client_id": "unknown"
    },
    "message": "Login invalido"
}
```

### Erro de Conexão (HTTP 500)
```json
{
    "ok": false,
    "message": "Conexão perdida",
    "error": "Database connection failed"
}
```

## 🖥️ Configuração Local (XAMPP)

### 1. Credenciais de Banco (já configuradas nos stubs)

```php
// Em syncjava.php e syncjava2.php (seção CONFIGURAÇÃO):
$DB_HOST = 'localhost';
$DB_USER = 'root';
$DB_PASS = '';  // Senha vazia no XAMPP padrão
$DB_NAME = 'prova1';
```

### 2. Localização dos Arquivos

- **Stubs originais:** `tools/server_stub/syncjava.php` e `syncjava2.php`
- **Deploy local:** `C:\xampp\htdocs\syncjava.php` e `syncjava2.php`
- **URL local:** 
  - `http://localhost/syncjava.php`
  - `http://localhost/syncjava2.php`

### 3. Testar Localmente

#### Via PowerShell (Invoke-RestMethod):
```powershell
# Teste 1: username + password (plain)
Invoke-RestMethod -Uri 'http://localhost/syncjava.php' -Method Post -Body @{ username='JOAO'; password='YOUR_PASSWORD' } | ConvertTo-Json

# Teste 2: email + password
Invoke-RestMethod -Uri 'http://localhost/syncjava2.php' -Method Post -Body @{ email='joao23@gmail.com'; senha='1234' } | ConvertTo-Json

# Teste 3: Basic Auth
$b64 = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes('JOAO:YOUR_PASSWORD'))
Invoke-RestMethod -Uri 'http://localhost/syncjava.php' -Method Post -Headers @{ Authorization="Basic $b64" } | ConvertTo-Json
```

#### Via curl (Bash/WSL):
```bash
# Teste 1: Form POST
curl -X POST http://localhost/syncjava.php -d "username=JOAO&password=YOUR_PASSWORD"

# Teste 2: Basic Auth
curl -X POST http://localhost/syncjava.php -u JOAO:YOUR_PASSWORD

# Teste 3: Via email
curl -X POST http://localhost/syncjava2.php -d "email=joao23@gmail.com&senha=YOUR_PASSWORD"
```

## 🌐 Deploy no Servidor Remoto (datse.com.br)

### 1. Ajustar Credenciais do Banco

Edite as seções marcadas como "Para servidor remoto" em `syncjava.php` e `syncjava2.php`:

```php
// Para servidor remoto (production - www.datse.com.br):
// Descomentar e ajustar as credenciais do servidor:
$DB_HOST = 'localhost'; // ou IP do servidor MySQL do datse.com.br
$DB_USER = 'seu_usuario_mysql';  // ← Substituir
$DB_PASS = 'sua_senha_mysql';    // ← Substituir
$DB_NAME = 'prova1';             // ou nome do banco no servidor
```

### 2. Criar o Banco `prova1` no Servidor

Conecte ao MySQL do servidor e execute:

```sql
CREATE DATABASE IF NOT EXISTS prova1 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE prova1;

-- Criar tabelas (copiar do schema.sql ou executar via phpMyAdmin)
CREATE TABLE usuarios (...);
CREATE TABLE categorias (...);
CREATE TABLE transacoes (...);

-- Inserir usuário JOAO (com bcrypt para produção)
INSERT INTO usuarios (nome, email, senha) VALUES 
('JOAO', 'joao23@gmail.com', '$2y$10$YourBcryptHashHere');
```

### 3. Upload dos Arquivos

Via FTP/SFTP ou painel de controle (cPanel/Plesk):

```
/public_html/dev/syncjava.php   ← upload de tools/server_stub/syncjava.php
/public_html/dev/syncjava2.php  ← upload de tools/server_stub/syncjava2.php
```

### 4. Testar Endpoints Remotos

```powershell
# Teste remoto
Invoke-RestMethod -Uri 'https://www.datse.com.br/dev/syncjava.php' -Method Post -Body @{ username='JOAO'; password='YOUR_PASSWORD' } | ConvertTo-Json
```

**Resultado esperado:** JSON com `"auth_ok": true` se credenciais estiverem corretas no banco remoto.

## 🔧 Troubleshooting

### Problema: "Conexão perdida" (HTTP 500)

**Causa:** PHP não consegue conectar ao MySQL.

**Solução:**
1. Verifique se MySQL está rodando: `netstat -ano | findstr :3306`
2. Teste conexão manual:
   ```bash
   mysql -h localhost -u root -p
   ```
3. Confirme credenciais em `syncjava.php` (linhas 9-12).

### Problema: PHP warnings about caching_sha2_password in Apache error.log

Se você encontrar avisos no log do Apache/PHP semelhantes a:

```
PHP Warning: mysqli::__construct(): The server requested authentication method unknown to the client [caching_sha2_password]
```

Isso significa que o usuário MySQL que o PHP está tentando usar foi criado com o plugin de autenticação `caching_sha2_password` (padrão no MySQL 8) e a versão/driver do PHP instalado não suporta esse plugin.

Soluções rápidas:

- Criar (ou alterar) um usuário MySQL que use `mysql_native_password` e atualize o `syncjava.php`/`syncjava2.php` para utilizar esse usuário. Há um script de ajuda em `tools/mysql_fix/fix_auth.sql`.
- Alternativa (melhor a longo prazo): atualizar/instalar uma versão do PHP + mysqlnd que suporte `caching_sha2_password`.

Exemplo (executar no servidor MySQL como root):

```sql
CREATE USER 'app_user'@'localhost' IDENTIFIED WITH mysql_native_password BY 'some_secure_password';
GRANT ALL PRIVILEGES ON prova1.* TO 'app_user'@'localhost';
FLUSH PRIVILEGES;
```

Depois, atualize `$DB_USER` / `$DB_PASS` no topo dos stubs para `app_user`/`some_secure_password`.

Um helper PowerShell para rodar o SQL está em `tools/mysql_fix/run_fix.ps1`.

### Problema: "Login invalido" (HTTP 401)

**Causa:** Credenciais não encontradas ou senha incorreta.

**Solução:**
1. Verifique se usuário existe:
   ```sql
   SELECT * FROM usuarios WHERE nome='JOAO' OR email='joao23@gmail.com';
   ```
2. Confirme formato da senha (plain, MD5 ou bcrypt).
3. Teste via navegador/curl para isolar problema do desktop Java.

### Problema: Desktop Java não recebe resposta

**Causa:** URL incorreta ou Apache não está rodando.

**Solução:**
1. Inicie Apache no XAMPP Control Panel.
2. Teste URL no navegador: `http://localhost/syncjava.php` (deve retornar JSON de erro pois não enviou credenciais).
3. Confirme URL no código Java (deve ser `http://localhost/syncjava.php` ou `https://www.datse.com.br/dev/syncjava.php`).

## ✅ Checklist de Deploy

- [ ] Banco `prova1` criado (local ou remoto)
- [ ] Tabelas `usuarios`, `categorias`, `transacoes` criadas
- [ ] Usuário JOAO inserido com senha válida
- [ ] Arquivos `syncjava.php` e `syncjava2.php` copiados para htdocs (local) ou public_html/dev (remoto)
- [ ] Credenciais de banco ajustadas nos arquivos PHP
- [ ] Apache rodando (XAMPP ou servidor)
- [ ] Teste manual via curl/PowerShell retorna HTTP 200
- [ ] Desktop Java configurado com URL correta

## 📚 Referências

- **Documentação do projeto:** `docs/README.md`
- **Schema SQL:** `avaliacao1/schema.sql`
- **Configuração Java:** `config/db.properties`
- **Instruções para DBA:** `docs/INSTRUCOES_CADASTRO_PROFESSOR.md`
- **Suporte API:** `docs/SOLICITACAO_SUPORTE_API.md`

---

**Última atualização:** 2 de novembro de 2025  
**Versão:** 1.0
