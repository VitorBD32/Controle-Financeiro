# 🔐 SISTEMA DE SEGURANÇA - CONTROLE FINANCEIRO

## Banco de Dados PROVA1 no DBeaver

O banco de dados está configurado e todas as tabelas estão visíveis no DBeaver:

### 📊 Tabelas do Sistema

| Tabela | Descrição | Registros |
|--------|-----------|-----------|
| `usuarios` | Usuários do sistema | 3 |
| `transacoes` | Transações financeiras | 8 |
| `categorias` | Categorias de transações | 2 |
| `cartoes` | Cartões cadastrados | 0 |
| `sistema_config` | Configurações do sistema | 32 |

### 🔒 Tabelas de Segurança (NOVAS)

| Tabela | Descrição | Função |
|--------|-----------|--------|
| `auditoria_log` | Log de auditoria | Registra todas as ações do sistema |
| `login_attempts` | Tentativas de login | Detecta ataques de força bruta |
| `sessoes_ativas` | Sessões de usuários | Controle de sessões ativas |
| `bloqueios_seguranca` | Bloqueios de IP/usuário | Proteção contra ataques |
| `dados_sensiveis` | Dados criptografados | Armazena dados sensíveis com criptografia |
| `alertas_seguranca` | Alertas de segurança | SQL Injection, XSS, etc. |
| `vw_resumo_seguranca` | View de resumo | Dashboard de segurança |

---

## 🛡️ Proteções Implementadas

### 1. **SQL Injection Prevention**
```java
SecurityManager.getInstance().validateAndSanitize(input, "campo");
```
- Detecção automática de padrões maliciosos
- Sanitização de entrada
- Escape de caracteres especiais
- Bloqueio e alerta de tentativas

### 2. **XSS (Cross-Site Scripting) Prevention**
```java
SecurityManager.getInstance().sanitizeXssInput(input);
```
- Escape de tags HTML
- Remoção de scripts maliciosos
- Proteção de formulários

### 3. **Criptografia AES-256-GCM**
```java
String encrypted = SecurityManager.getInstance().encrypt("dados_sensiveis");
String decrypted = SecurityManager.getInstance().decrypt(encrypted);
```
- Criptografia de senhas
- Criptografia de dados financeiros
- Criptografia de configurações

### 4. **Proteção contra Força Bruta**
```java
AuditoriaDAO.getInstance().registrarTentativaLogin(username, ip, sucesso, motivo, userAgent);
```
- Bloqueio após 5 tentativas falhas
- Lockout de 30 minutos
- Log de todas as tentativas

### 5. **Auditoria Completa**
```java
AuditoriaDAO.getInstance().logUsuario(userId, nome, "ACAO", "entidade", "detalhes");
```
- Registro de todas as ações
- Rastreamento de alterações
- Compliance LGPD

### 6. **Mascaramento de Dados**
```java
String masked = SecurityManager.getInstance().maskSensitiveData("12345678901", "CPF");
// Resultado: ***.456.***-**
```
- CPF: `***.456.***-**`
- Cartão: `**** **** **** 8888`
- Email: `us***@email.com`

---

## 📁 Classes de Segurança

```
src/main/java/controle/security/
├── SecurityManager.java      # Gerenciador principal de segurança
├── AuditoriaDAO.java         # DAO para logs de auditoria
└── SecureConfigLoader.java   # Carregador seguro de configurações
```

---

## 🔧 Configuração no DBeaver

Para visualizar o banco de dados no DBeaver:

1. **Conexão MySQL:**
   - Host: `127.0.0.1`
   - Porta: `3306`
   - Banco: `PROVA1`
   - Usuário: `root`
   - Senha: (configurada em db.properties)

2. **Verificar Tabelas:**
   ```sql
   USE PROVA1;
   SHOW TABLES;
   ```

3. **Ver Configurações:**
   ```sql
   SELECT * FROM sistema_config WHERE chave LIKE 'TAXA_%';
   ```

4. **Ver Resumo de Segurança:**
   ```sql
   SELECT * FROM vw_resumo_seguranca;
   ```

---

## 🚨 Alertas de Segurança

O sistema gera alertas automáticos para:

| Tipo | Severidade | Descrição |
|------|------------|-----------|
| `SQL_INJECTION` | CRÍTICA | Tentativa de injeção SQL detectada |
| `XSS` | ALTA | Tentativa de script malicioso |
| `BRUTE_FORCE` | ALTA | Múltiplas tentativas de login |
| `ACESSO_NAO_AUTORIZADO` | ALTA | Acesso a recurso não permitido |
| `SESSAO_SUSPEITA` | MÉDIA | Comportamento anômalo de sessão |

---

## 📈 Alíquotas da Reforma Tributária 2026

Configuradas em `sistema_config`:

| Imposto | Alíquota | Descrição |
|---------|----------|-----------|
| CBS | 8.8% | Contribuição sobre Bens e Serviços (Federal) |
| IBS | 17.7% | Imposto sobre Bens e Serviços (Estadual/Municipal) |
| IS | 0.0% | Imposto Seletivo |
| **Total IVA** | **26.5%** | IVA-Dual completo |

---

**Sistema atualizado em:** 07/12/2025
**Versão:** 2.0 - Segurança Reforçada
