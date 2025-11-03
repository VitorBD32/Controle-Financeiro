# Guia de Deploy para www.datse.com.br

## 🎯 Objetivo

Fazer upload dos arquivos `syncjava.php` e `syncjava2.php` para o servidor `www.datse.com.br` e conectá-los ao banco de dados MySQL do servidor.

## 📋 Pré-requisitos

- [ ] Acesso FTP/SFTP ou painel de controle (cPanel/Plesk) do servidor datse.com.br
- [ ] Credenciais do banco de dados MySQL do servidor
- [ ] Banco `prova1` criado no servidor (ou nome alternativo)

## 🔧 Passo a Passo

### 1. Preparar os Arquivos

Os arquivos estão em:
```
tools/server_stub/syncjava.php
tools/server_stub/syncjava2.php
```

**Antes de fazer upload**, edite as linhas 9-12 em **ambos** os arquivos:

```php
// ========== CONFIGURAÇÃO DO BANCO DE DADOS ==========
// Para servidor remoto (production - www.datse.com.br):
// Descomentar e ajustar as credenciais do servidor:
$DB_HOST = 'localhost'; // ← Geralmente 'localhost' (verificar com provedor)
$DB_USER = 'seu_usuario_mysql';  // ← Substituir pela credencial real
$DB_PASS = 'sua_senha_mysql';    // ← Substituir pela senha real
$DB_NAME = 'prova1';             // ← Nome do banco no servidor
```

**Exemplo ajustado:**
```php
$DB_HOST = 'localhost';
$DB_USER = 'datse_dbuser';
$DB_PASS = 'SenhaSegura123!';
$DB_NAME = 'datse_prova1';
```

### 2. Criar o Banco de Dados no Servidor

#### Via phpMyAdmin (cPanel):

1. Acesse o cPanel do servidor datse.com.br
2. Vá em **MySQL Databases** ou **phpMyAdmin**
3. Crie um novo banco: `prova1` (ou `datse_prova1`)
4. Crie um usuário MySQL e dê permissões ALL ao banco
5. Execute o seguinte SQL:

```sql
CREATE DATABASE IF NOT EXISTS prova1 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE prova1;

-- Tabela de usuários
CREATE TABLE usuarios (
    id INT(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de categorias
CREATE TABLE categorias (
    id INT(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    tipo ENUM('RECEITA', 'DESPESA') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de transações
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

-- Inserir usuário JOAO com senha bcrypt
-- Gerar hash bcrypt em PHP: password_hash('1234', PASSWORD_BCRYPT)
INSERT INTO usuarios (nome, email, senha) VALUES 
('JOAO', 'joao23@gmail.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi');
-- (Este hash é para a senha '1234' - substitua conforme necessário)
```

**Gerar hash bcrypt:**
```php
<?php
// Criar arquivo temp.php, fazer upload, acessar via navegador e depois deletar
echo password_hash('1234', PASSWORD_BCRYPT);
?>
```

#### Via SSH (se disponível):

```bash
mysql -u root -p
```

```sql
CREATE DATABASE prova1;
USE prova1;
-- (executar os CREATE TABLE acima)
```

### 3. Upload dos Arquivos

#### Via FTP/FileZilla:

1. Conecte ao servidor FTP:
   - Host: `ftp.datse.com.br` (ou conforme provedor)
   - Usuário: seu usuário FTP
   - Senha: sua senha FTP

2. Navegue até a pasta pública:
   ```
   /public_html/dev/
   ```
   (Se a pasta `dev` não existir, crie-a)

3. Faça upload dos arquivos:
   ```
   syncjava.php  → /public_html/dev/syncjava.php
   syncjava2.php → /public_html/dev/syncjava2.php
   ```

#### Via cPanel File Manager:

1. Acesse cPanel → **File Manager**
2. Navegue para `public_html/dev/`
3. Clique em **Upload**
4. Selecione `syncjava.php` e `syncjava2.php`
5. Aguarde o upload concluir

### 4. Definir Permissões

Ajuste as permissões dos arquivos para `644`:

```bash
chmod 644 /public_html/dev/syncjava.php
chmod 644 /public_html/dev/syncjava2.php
```

Ou via File Manager do cPanel:
- Clique com botão direito no arquivo → **Permissions**
- Marque: Owner (Read/Write), Group (Read), World (Read)
- Salvar

### 5. Testar os Endpoints

#### Via navegador:

Acesse (você verá um erro JSON, pois não enviou credenciais - isso é esperado):
```
https://www.datse.com.br/dev/syncjava.php
```

**Resultado esperado (HTTP 401):**
```json
{
    "ok": false,
    "auth_ok": false,
    "message": "Login invalido"
}
```

Se você vir **"Conexão perdida"**, as credenciais do banco estão erradas.

#### Via PowerShell:

```powershell
# Teste 1: POST com credenciais
Invoke-RestMethod -Uri 'https://www.datse.com.br/dev/syncjava.php' -Method Post -Body @{ username='JOAO'; password='1234' } | ConvertTo-Json

# Teste 2: Endpoint alternativo
Invoke-RestMethod -Uri 'https://www.datse.com.br/dev/syncjava2.php' -Method Post -Body @{ email='joao23@gmail.com'; senha='1234' } | ConvertTo-Json
```

**Resultado esperado (HTTP 200):**
```json
{
    "ok": true,
    "auth_ok": true,
    "endpoint": "/dev/syncjava.php",
    "user": {
        "id": 1,
        "nome": "JOAO",
        "email": "joao23@gmail.com"
    },
    "message": "Sincronização aceita"
}
```

#### Via curl (Linux/Mac/WSL):

```bash
curl -X POST https://www.datse.com.br/dev/syncjava.php \
  -d "username=JOAO&password=1234"
```

### 6. Configurar Desktop Java

Após confirmar que os endpoints remotos funcionam, atualize o código Java:

**Editar:** `src/main/java/controle/util/HttpSyncUtil.java` (ou onde estiver a URL)

```java
// Trocar de:
String url = "http://localhost/syncjava.php";

// Para:
String url = "https://www.datse.com.br/dev/syncjava.php";
```

Ou use configuração externa:

**Editar:** `config/api.properties`
```properties
api.sync.url=https://www.datse.com.br/dev/syncjava.php
```

## 🔍 Troubleshooting

### Erro: "Conexão perdida"

**Causa:** PHP não consegue conectar ao MySQL.

**Solução:**
1. Verifique se as credenciais em `syncjava.php` estão corretas
2. Confirme que o banco `prova1` existe
3. Teste conexão manual via phpMyAdmin
4. Verifique se o usuário MySQL tem permissões no banco

### Erro: "Login invalido" (mas credenciais estão corretas)

**Causa:** Formato da senha não coincide.

**Solução:**
1. Verifique o valor exato da coluna `senha` na tabela:
   ```sql
   SELECT nome, senha FROM usuarios WHERE nome='JOAO';
   ```
2. Se a senha estiver em MD5, certifique-se de que é o MD5 correto de '1234':
   ```
   81dc9bdb52d04dc20036dbd8313ed055
   ```
3. Se usar bcrypt, gere novo hash e atualize:
   ```sql
   UPDATE usuarios SET senha='$2y$10$...' WHERE nome='JOAO';
   ```

### Erro 404: "Not Found"

**Causa:** Arquivos não foram enviados corretamente ou pasta `dev/` não existe.

**Solução:**
1. Confirme que arquivos existem em `/public_html/dev/`
2. Verifique permissões (devem ser 644)
3. Teste URL completa no navegador

### Erro 500: "Internal Server Error"

**Causa:** Erro de sintaxe PHP ou extensão mysqli não instalada.

**Solução:**
1. Ative log de erros PHP no cPanel ou adicione ao arquivo:
   ```php
   ini_set('display_errors', 1);
   error_reporting(E_ALL);
   ```
2. Verifique se extensão mysqli está habilitada no servidor
3. Revise sintaxe do arquivo (copie novamente do repositório)

## ✅ Checklist de Deploy

- [ ] Credenciais do banco ajustadas em `syncjava.php` e `syncjava2.php`
- [ ] Banco `prova1` criado no servidor
- [ ] Tabelas `usuarios`, `categorias`, `transacoes` criadas
- [ ] Usuário JOAO inserido no banco
- [ ] Arquivos enviados para `/public_html/dev/`
- [ ] Permissões ajustadas para 644
- [ ] Teste via navegador retorna JSON (mesmo que erro 401)
- [ ] Teste via curl/PowerShell retorna HTTP 200 com credenciais corretas
- [ ] Desktop Java atualizado com URL remota

## 📞 Suporte

Se precisar de ajuda com:
- **Credenciais MySQL:** Entre em contato com o provedor de hospedagem (datse.com.br)
- **Acesso FTP/cPanel:** Solicite ao administrador do servidor
- **Problemas de código:** Veja `docs/INTEGRACAO_BD_ENDPOINTS.md`

---

**Última atualização:** 2 de novembro de 2025  
**Versão:** 1.0
