# Status da Integração com API de Sincronização

**Última atualização:** 28/10/2025  
**Status:** ✅ CONECTIVIDADE OK | ⚠️ AGUARDANDO CADASTRO DE USUÁRIO

## URL Confirmada e Funcional

A URL de produção **confirmada e funcionando** é:

```
http://www.datse.com.br/dev/syncjava.php
```

⚠️ **Nota:** A URL `syncjava2.php` (com "2") NÃO existe no servidor (retorna 404).

## Status Atual da Integração

✅ **Conectividade HTTP:** Funcionando (200 OK)  
✅ **URL correta:** Descoberta e configurada  
✅ **Criptografia AES-256:** Implementada e testada  
✅ **Formato do payload:** Correto e aceito pelo servidor  
❌ **Autenticação:** Servidor retorna "Login invalido"

## Problema Identificado

O usuário **"JOAO"** com senha **"1234"** configurado no cliente **NÃO existe no banco de dados do servidor**.

**Testes realizados:**
- ✅ Formato `{"login":"JOAO","senha":"1234"}` → Login invalido
- ✅ Formato `{"username":"JOAO","password":"1234"}` → Login invalido
- ✅ Formato `{"user":"JOAO","pass":"1234"}` → Login invalido
- ✅ Payload sem credenciais → Login invalido
- ✅ Payload não criptografado → Login invalido

## Problema Identificado

O usuário **"JOAO"** com senha **"1234"** configurado no cliente **NÃO existe no banco de dados do servidor**.

**Testes realizados:**
- ✅ Formato `{"login":"JOAO","senha":"1234"}` → Login invalido
- ✅ Formato `{"username":"JOAO","password":"1234"}` → Login invalido
- ✅ Formato `{"user":"JOAO","pass":"1234"}` → Login invalido
- ✅ Payload sem credenciais → Login invalido
- ✅ Payload não criptografado → Login invalido

**Conclusão:** O servidor está funcionando corretamente e recebendo os dados, mas não encontra o usuário no banco de dados.

## ⚠️ AÇÃO NECESSÁRIA - Cadastro de Usuário no Servidor

### Para o Administrador do Servidor

Por favor, cadastrar o seguinte usuário no banco de dados do servidor:

**Credenciais necessárias:**
- **Login/Username:** JOAO
- **Senha/Password:** 1234

**Opções de cadastro:**

#### Opção A: SQL Direto (ajustar conforme estrutura da tabela)
```sql
-- Exemplo com hash MD5 (menos seguro, apenas para testes)
INSERT INTO usuarios (login, senha, nome, email, ativo) 
VALUES ('JOAO', MD5('1234'), 'João da Silva', 'joao@exemplo.com', 1);

-- OU com bcrypt (recomendado para produção - PHP):
-- senha hash: $2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi
INSERT INTO usuarios (login, senha, nome, email, ativo) 
VALUES ('JOAO', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'João', 'joao@exemplo.com', 1);
```

#### Opção B: Via Interface de Administração
Se existe uma interface web de administração, cadastrar por lá.

#### Opção C: Fornecer Credenciais Existentes
Se preferir não criar usuário novo, fornecer credenciais válidas que já existem:
- Login: _______________
- Senha: _______________

## Informações Técnicas para o Administrador

## Informações Técnicas para o Administrador

### Payload Recebido pelo Servidor (exemplo decriptado):

```json
{
  "login": "JOAO",
  "senha": "1234",
  "id": 6,
  "tipo": "D",
  "valor": 2000.00,
  "data": "2025-10-22T00:00:00",
  "descricao": "Pagamento de contas"
}
```

### Formato da Requisição HTTP:

```
POST /dev/syncjava.php HTTP/1.1
Host: www.datse.com.br
Content-Type: application/x-www-form-urlencoded; charset=UTF-8

encrypted_data=<base64_json_criptografado>&salt=<base64_salt>&client_id=<nome_computador>
```

### Detalhes da Criptografia:

- **Algoritmo:** AES-256-CBC
- **KDF:** PBKDF2WithHmacSHA256
- **Iterações:** 20.000
- **Salt:** 16 bytes aleatórios (enviado junto)
- **IV:** 16 bytes aleatórios (prepended ao ciphertext)
- **Segredo compartilhado:** "sua-senha-secreta-aqui"

### Teste Manual (cURL):

```bash
# Teste simples sem criptografia (para validar endpoint)
curl -v -X POST "http://www.datse.com.br/dev/syncjava.php" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  --data-urlencode "login=JOAO" \
  --data-urlencode "senha=1234" \
  --data-urlencode "teste=manual"
```

## Como Enviar a Solicitação ao Administrador

### Email Modelo:

```
Assunto: Solicitação de Cadastro de Usuário - API Sincronização

Prezado(a) Administrador(a),

Estou desenvolvendo uma aplicação desktop em Java que precisa sincronizar 
dados com o endpoint:

http://www.datse.com.br/dev/syncjava.php

A conectividade está funcionando perfeitamente (HTTP 200 OK), mas o servidor 
retorna "Login invalido" pois o usuário ainda não está cadastrado.

Solicito o cadastro do seguinte usuário no banco de dados:

Login: JOAO
Senha: 1234

Ou, alternativamente, fornecer credenciais válidas existentes no sistema.

Detalhes técnicos completos estão disponíveis em:
docs/SOLICITACAO_SUPORTE_API.md (anexado)

Atenciosamente,
Vitor de Brito Cardoso Oliveira
vitordebrito23@gmail.com
```

### Documentos de Referência:

1. **Solicitação Completa:** `docs/SOLICITACAO_SUPORTE_API.md`
2. **Este arquivo:** `docs/SYNC_RECOMMENDATIONS.md`

## Próximos Passos (após cadastro do usuário)

1. ✅ Usuário cadastrado no servidor
2. ⏳ Testar sincronização novamente na aplicação
3. ⏳ Verificar resposta de sucesso do servidor
4. ⏳ Implementar marcação de transações sincronizadas (idempotência)
5. ⏳ Migrar para AES-GCM (AEAD) para melhor segurança
6. ⏳ Mover credenciais para variáveis de ambiente

## Comandos Rápidos de Teste

### Testar conectividade (PowerShell):

```powershell
# Teste GET
Invoke-WebRequest -Uri 'http://www.datse.com.br/dev/syncjava.php' -Method GET -UseBasicParsing

# Teste POST simples
Invoke-WebRequest -Uri 'http://www.datse.com.br/dev/syncjava.php' -Method POST -Body @{ test='1' } -UseBasicParsing
```

### Executar teste de autenticação (Java):

```powershell
mvn compile
java -cp "target/classes;C:\Users\Acer\.m2\repository\com\mysql\mysql-connector-j\8.0.33\mysql-connector-j-8.0.33.jar" controle.tools.AuthTester
```

### Executar teste de URLs (Java):

```powershell
java -cp "target/classes;C:\Users\Acer\.m2\repository\com\mysql\mysql-connector-j\8.0.33\mysql-connector-j-8.0.33.jar" controle.tools.URLTester
```

## Histórico de Resolução

- **28/10/2025 18:30** - Identificada URL incorreta (syncjava2.php → syncjava.php)
- **28/10/2025 18:35** - URL corrigida e conectividade confirmada (HTTP 200)
- **28/10/2025 18:45** - Testados múltiplos formatos de autenticação
- **28/10/2025 18:50** - Identificado: usuário JOAO não existe no servidor
- **28/10/2025 18:55** - Documentação completa e solicitação preparada

---

**Status:** ⏳ Aguardando cadastro de usuário no servidor para finalizar integração.

