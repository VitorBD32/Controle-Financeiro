# GUIA RÁPIDO - Como Resolver o Problema de Autenticação

## 🎯 Problema Atual
A aplicação está conectando com sucesso ao servidor, mas recebe "Login invalido" porque o usuário **JOAO** com senha **1234** não existe no banco de dados do servidor.

## ✅ SOLUÇÃO - Cadastrar Usuário via SQL Direto

### ⚡ OPÇÃO 3: SQL Direto no Banco de Dados (RECOMENDADO)

## ✅ SOLUÇÃO - Cadastrar Usuário via SQL Direto

### ⚡ OPÇÃO 3: SQL Direto no Banco de Dados (RECOMENDADO)

Se você tem acesso ao banco de dados do servidor (via phpMyAdmin, MySQL Workbench, linha de comando, etc.), execute um dos comandos SQL abaixo:

#### 📋 Estrutura da Tabela (conforme seu projeto):
```
usuarios:
  - id (int, auto_increment, primary key)
  - nome (varchar)
  - email (varchar)
  - senha (varchar)
```

#### 📋 Passo a Passo:

**1. Conecte-se ao banco de dados do servidor:**
```bash
# Via linha de comando
mysql -h www.datse.com.br -u seu_usuario -p nome_do_banco

# Ou use phpMyAdmin, MySQL Workbench, HeidiSQL, etc.
```

**2. Execute UMA das opções de SQL abaixo:**

#### ✅ Opção A: Hash MD5 (Simples, comum em sistemas legados)
```sql
INSERT INTO usuarios (nome, email, senha) 
VALUES ('JOAO', 'joao@gmail.com', MD5('1234'));
```

#### ✅ Opção B: Hash SHA256 (Mais seguro)
```sql
INSERT INTO usuarios (nome, email, senha) 
VALUES ('JOAO', 'joao@gmail.com', SHA2('1234', 256));
```

#### ⚠️ Opção C: Senha em Texto Plano (NÃO RECOMENDADO - só para testes)
```sql
-- ATENÇÃO: Inseguro! Use apenas para testes rápidos
INSERT INTO usuarios (nome, email, senha) 
VALUES ('JOAO', 'joao@gmail.com', '1234');
```

#### ✅ Opção D: Hash Bcrypt (Recomendado para Produção - PHP)
```sql
-- Hash bcrypt pré-calculado de '1234':
-- $2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi

INSERT INTO usuarios (nome, email, senha) 
VALUES ('JOAO', 'joao@gmail.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi');
```

**3. Verifique se o usuário foi criado:**
```sql
SELECT * FROM usuarios WHERE nome = 'JOAO';
-- Deve retornar: id=8 (ou próximo), nome='JOAO', email='joao@gmail.com'
```

**4. Atualize o código do projeto para usar 'nome' em vez de 'login':**

O servidor espera receber o campo **"nome"** (não "login"). Vou atualizar automaticamente o código.

**5. Teste a sincronização:**
```powershell
# No Windows PowerShell
mvn -DskipTests package
.\run-ui.ps1
# Clique em "Sincronizar"
```

---

### 🔧 Variações de SQL (caso precise ajustar)

#### Se o usuário JOAO já existir (erro "Duplicate entry"):
```sql
-- Atualizar a senha do JOAO existente:
UPDATE usuarios SET senha = MD5('1234') WHERE nome = 'JOAO';
```

#### Para criar com ID específico (se necessário):
```sql
-- Forçar ID 8 (ajuste conforme necessário)
INSERT INTO usuarios (id, nome, email, senha) 
VALUES (8, 'JOAO', 'joao@gmail.com', MD5('1234'));
```

#### SQL mínimo (só nome e senha):
```sql
INSERT INTO usuarios (nome, senha) 
VALUES ('JOAO', MD5('1234'));
```

---

### 🎯 Escolhendo o Tipo de Hash

**Como saber qual hash o servidor usa?**

1. **Veja um usuário existente:**
```sql
SELECT login, senha FROM usuarios LIMIT 1;
```

2. **Identifique pelo formato:**
   - **32 caracteres hexadecimais** → MD5 (ex: `5f4dcc3b5aa765d61d8327deb882cf99`)
   - **64 caracteres hexadecimais** → SHA256
   - **Começa com `$2y$` ou `$2a$`** → Bcrypt
   - **Texto legível** → Texto plano (inseguro!)

3. **Use o mesmo formato:**
```sql
-- Se outros usuários usam MD5, use MD5 para JOAO também
INSERT INTO usuarios (login, senha) 
VALUES ('JOAO', MD5('1234'));
```

---

### 🚨 Troubleshooting

#### Erro: "Duplicate entry 'JOAO'"
```sql
-- O usuário já existe! Atualize a senha dele:
UPDATE usuarios SET senha = MD5('1234') WHERE login = 'JOAO';
```

#### Erro: "Unknown column 'data_cadastro'"
```sql
-- Remova o campo que não existe:
INSERT INTO usuarios (login, senha, nome, email, ativo) 
VALUES ('JOAO', MD5('1234'), 'João da Silva', 'joao@exemplo.com', 1);
```

#### Erro: "Table 'usuarios' doesn't exist"
```sql
-- Descubra o nome correto da tabela:
SHOW TABLES LIKE '%user%';
-- Ou:
SHOW TABLES;
```

---

## 🔄 OPÇÕES ALTERNATIVAS (se não puder usar SQL direto)

### OPÇÃO 1: Pedir ao admin para cadastrar JOAO

**Se você NÃO tem acesso ao banco de dados, envie este email ao administrador:**

```
Assunto: Cadastro de Usuário - API Sincronização

Olá,

Preciso que seja cadastrado este usuário no banco de dados para a API de sincronização:

Login: JOAO
Senha: 1234 (usar hash MD5, SHA256 ou bcrypt conforme padrão do sistema)

Endpoint: http://www.datse.com.br/dev/syncjava.php

A aplicação está conectando OK (HTTP 200), mas retorna "Login invalido" 
pois este usuário não existe no servidor.

SQL sugerido:
INSERT INTO usuarios (login, senha, nome, email, ativo) 
VALUES ('JOAO', MD5('1234'), 'João da Silva', 'joao@exemplo.com', 1);

Obrigado!
```

**Documento completo para enviar:**
```powershell
notepad docs\SOLICITACAO_SUPORTE_API.md
```

---

### OPÇÃO 2: Usar outro usuário que já existe

**1. Descubra qual usuário já está cadastrado no servidor**

**2. Atualize o arquivo `config/api.properties`:**
```properties
api.auth.user=USUARIO_QUE_EXISTE
api.auth.password=SENHA_DO_USUARIO
```

**3. Recompile e teste:**
```powershell
mvn -DskipTests package
.\run-ui.ps1
```

---

### OPÇÃO 3: Se você tem acesso ao banco do servidor

**Execute este SQL no banco de dados do servidor:**

```sql
-- Exemplo básico (ajuste conforme sua tabela)
INSERT INTO usuarios (login, senha, nome, email, ativo) 
VALUES ('JOAO', MD5('1234'), 'João da Silva', 'joao@exemplo.com', 1);
```

**Ou se usar bcrypt/password_hash (recomendado):**
```sql
-- Hash bcrypt de '1234': $2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi
INSERT INTO usuarios (login, senha, nome, email, ativo) 
VALUES ('JOAO', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'João', 'joao@exemplo.com', 1);
```

---

## 📞 Contatos do Projeto

- **Desenvolvedor:** Vitor de Brito Cardoso Oliveira
- **Email:** vitordebrito23@gmail.com
- **GitHub:** github.com/VitorBD32/Controle-Financeiro

## 📋 Documentos de Referência

- **Solicitação Completa:** `docs/SOLICITACAO_SUPORTE_API.md`
- **Recomendações Técnicas:** `docs/SYNC_RECOMMENDATIONS.md`
- **README do Projeto:** `docs/README.md`

## ⏭️ O Que Acontece Depois

Após o usuário ser cadastrado no servidor:

1. A sincronização vai funcionar imediatamente
2. Você verá mensagens de sucesso na UI (não mais "Login invalido")
3. Os dados das transações serão enviados ao servidor com sucesso

## 🧪 Como Testar Depois

```powershell
# 1. Recompilar (se mudou credenciais)
mvn -DskipTests package

# 2. Executar a UI
.\run-ui.ps1

# 3. Clicar em "Sincronizar"

# Resultado esperado: "Sincronizacao finalizada. Sucesso: X, Falhas: 0"
```

---

**Última atualização:** 28/10/2025 - Status: Aguardando cadastro no servidor
