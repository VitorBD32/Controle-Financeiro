# Solicitação de Suporte - Integração API de Sincronização

**Data:** 28/10/2025  
**Projeto:** Sistema de Controle Financeiro Desktop (Java)  
**Desenvolvedor:** Vitor de Brito Cardoso Oliveira

---

## PROBLEMA IDENTIFICADO

A aplicação desktop está se conectando com sucesso ao endpoint:
```
http://www.datse.com.br/dev/syncjava.php
```

✅ **Conectividade:** OK (HTTP 200)  
✅ **Criptografia:** OK (AES-256-CBC com PBKDF2)  
✅ **Formato do payload:** OK (form-urlencoded com encrypted_data, salt, client_id)  
❌ **Autenticação:** FALHA - "Login invalido"

## DIAGNÓSTICO

Foram testados múltiplos formatos de autenticação:
- `{"login":"JOAO","senha":"1234"}` → Login invalido
- `{"username":"JOAO","password":"1234"}` → Login invalido  
- `{"user":"JOAO","pass":"1234"}` → Login invalido
- Payload sem credenciais → Login invalido
- Payload não criptografado → Login invalido

**Conclusão:** O usuário "JOAO" com senha "1234" não existe no banco de dados do servidor.

## SOLUÇÃO NECESSÁRIA

### Opção A: Cadastrar usuário no servidor (PREFERENCIAL)
Por favor, cadastrar no banco de dados do servidor:
- **Login/Username:** JOAO
- **Senha/Password:** 1234
- **Outros campos:** conforme estrutura da tabela de usuários

### Opção B: Fornecer credenciais válidas
Se já existem usuários cadastrados no servidor, fornecer:
- Login válido: _________________
- Senha válida: _________________

### Opção C: Informar estrutura esperada
Se o formato do JSON está incorreto, informar a estrutura esperada pelo endpoint, exemplo:
```json
{
  "user": "JOAO",
  "password": "1234",
  "data": {...}
}
```

## INFORMAÇÕES TÉCNICAS DO CLIENTE

### Payload enviado (exemplo decriptado):
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

### Headers HTTP:
```
POST /dev/syncjava.php HTTP/1.1
Host: www.datse.com.br
Content-Type: application/x-www-form-urlencoded; charset=UTF-8
```

### Parâmetros do POST:
```
encrypted_data=<base64_do_json_criptografado>
salt=<base64_do_salt_usado_no_pbkdf2>
client_id=<nome_do_computador>
```

### Criptografia:
- **Algoritmo:** AES-256-CBC
- **KDF:** PBKDF2WithHmacSHA256
- **Iterações:** 20.000
- **Salt:** 16 bytes aleatórios
- **IV:** 16 bytes aleatórios (prepended ao ciphertext)
- **Segredo compartilhado:** "sua-senha-secreta-aqui" (configurado em ambos os lados)

## TESTES REALIZADOS

1. ✅ Conectividade HTTP - 200 OK
2. ✅ Descoberta da URL correta (syncjava.php, não syncjava2.php)
3. ✅ Payload criptografado aceito pelo servidor
4. ✅ Diferentes formatos de campos testados
5. ❌ Validação de credenciais - todas rejeitadas

## PRÓXIMOS PASSOS

Após resolver a questão de autenticação:
1. Confirmar resposta de sucesso esperada (JSON? texto? código?)
2. Ajustar cliente para interpretar resposta corretamente
3. Implementar lógica de retry/idempotência se necessário

## CONTATO

**Desenvolvedor:** Vitor de Brito Cardoso Oliveira  
**Email:** vitordebrito23@gmail.com  
**Projeto:** github.com/VitorBD32/Controle-Financeiro

---

## EXEMPLO DE TESTE MANUAL (cURL)

Para testar no servidor sem criptografia:
```bash
curl -v -X POST "http://www.datse.com.br/dev/syncjava.php" \
  -H "Content-Type: application/x-www-form-urlencoded; charset=UTF-8" \
  --data-urlencode "login=JOAO" \
  --data-urlencode "senha=1234" \
  --data-urlencode "teste=manual"
```

Se precisar cadastrar o usuário via SQL:
```sql
-- Exemplo (ajustar conforme estrutura real da tabela)
INSERT INTO usuarios (login, senha, nome, email) 
VALUES ('JOAO', MD5('1234'), 'João da Silva', 'joao@exemplo.com');
-- OU usar password_hash() se for PHP moderno:
-- VALUES ('JOAO', '$2y$10$...hash_bcrypt...', 'João', 'joao@exemplo.com');
```

---

**Aguardando retorno para prosseguir com a integração.**
