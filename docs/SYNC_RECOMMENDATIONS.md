Recomendações para corrigir o erro 404 do endpoint /syncjava.php

Objetivo
- Documentar passos reproduzíveis e comandos para diagnosticar e corrigir o problema onde a aplicação desktop Java recebe HTTP 404 (Object not found) ao tentar sincronizar com o endpoint de sincronização.
 
A URL de produção atualmente usada pelo projeto é:

```
http://www.datse.com.br/dev/syncjava2.php
```
Para testes locais você pode usar o mock incluído no repositório com a URL:

```
http://127.0.0.1:8000/syncjava.php
```

Resumo do problema observado
- O servidor Apache/PHP responde em porta 80 (não houve Connection Refused), mas retornou HTTP 404, indicando que o recurso /syncjava.php não foi encontrado no DocumentRoot ou não está acessível pela URL configurada.

Ações recomendadas (ordem sugerida)

1) Verificar rapidamente o status do endpoint via PowerShell
- GET (mostra resposta e corpo) — produção:

```powershell
Invoke-WebRequest -Uri 'http://www.datse.com.br/dev/syncjava2.php' -Method GET -UseBasicParsing -ErrorAction SilentlyContinue | Select-Object StatusCode, StatusDescription, Content
```

- POST (como o cliente faz) — produção:

```powershell
Invoke-WebRequest -Uri 'http://www.datse.com.br/dev/syncjava2.php' -Method POST -Body @{ test='1' } -UseBasicParsing -ErrorAction SilentlyContinue | Select-Object StatusCode, StatusDescription, Content
```

Se quiser testar localmente com o mock do repositório, use:

```powershell
Invoke-WebRequest -Uri 'http://127.0.0.1:8000/syncjava.php' -Method POST -Body @{ test='1' } -UseBasicParsing -ErrorAction SilentlyContinue | Select-Object StatusCode, StatusDescription, Content
```

2) Localizar o DocumentRoot do servidor Apache/PHP
- XAMPP: normalmente `C:\xampp\htdocs\`
- Apache padrão no Windows: `C:\Program Files\Apache24\htdocs\`
- Se não souber, procure o `httpd.conf` e localize `DocumentRoot`:

```powershell
Select-String -Path 'C:\xampp\apache\conf\httpd.conf' -Pattern 'DocumentRoot' -Context 0,1
# ou procure no diretório da instalação do Apache
Get-ChildItem -Recurse -Path 'C:\' -Filter httpd.conf -ErrorAction SilentlyContinue | Select-String -Pattern 'DocumentRoot' -Context 0,1
```

3) Verificar se `syncjava.php` existe no DocumentRoot

```powershell
Test-Path 'C:\xampp\htdocs\syncjava.php'
Get-Content 'C:\xampp\htdocs\syncjava.php' -ErrorAction SilentlyContinue
```

4) Se não existir, criar um `syncjava.php` mínimo para testes
- Conteúdo do arquivo de teste (colocar em DocumentRoot/syncjava.php):

```php
<?php
header('Content-Type: application/json; charset=utf-8');
// Exemplo simples: lê campos POST e retorna JSON
$response = [
    'status' => 'ok',
    'received' => $_POST
];
echo json_encode($response);
```

-- Verifique no browser apontando para a URL de produção (se o stub estiver implantado no servidor) ou, para testes locais, aponte para o mock `http://127.0.0.1:8000/syncjava.php` (veja passo 1).

5) Conferir permissões e logs do Apache
- Logs comuns (XAMPP): `C:\xampp\apache\logs\error.log` e `access.log`
- Ler últimas linhas do log:

```powershell
Get-Content 'C:\xampp\apache\logs\error.log' -Tail 50
```

6) Se o arquivo existir mas em subpasta, atualize `config/api.properties`
- Ex.: se o endpoint real estiver em um caminho diferente, atualize a propriedade `api.sync.url` para apontar corretamente. Exemplo (produção):

```
api.sync.url=http://www.datse.com.br/dev/syncjava2.php
```

Ou (para testes locais usando o mock):

```
api.sync.url=http://127.0.0.1:8000/syncjava.php
```

7) Verificar se o servidor exige algum cabeçalho, autenticação ou método diferente
- O `HttpSyncUtil` envia um POST form-url-encoded com campos `encrypted_data`, `salt`, `client_id`.
- Se o servidor espera JSON ou outros nomes, alinhe o `HttpSyncUtil.buildEncryptedPayload()` ou atualize o servidor para aceitar os campos enviados.

8) Ambiente seguro e testes
- Para desenvolvimento, mantenha `tools/mock_sync_server.py` no repositório e altere temporariamente `config/api.properties` para `http://127.0.0.1:8000/syncjava.php` enquanto trabalha localmente.
- Quando estiver pronto para produção, a URL deve apontar para o endpoint real.

9) Melhorias futuras (quando for implementar a correção)
- Implementar resposta padronizada do servidor (JSON com status e mensagem) para facilitar parsing no cliente.
- Implementar marcação `synced` na tabela `transacoes` para idempotência.
- Adicionar um modo de desenvolvimento (`api.mode=mock|prod`) no `config/api.properties` para alternar facilmente entre mock e real.
- Melhorar os logs do cliente (gravar em arquivo) com request/response (sem incluir segredos) para debugging.

Anexos/Comandos úteis
- Comando para testar conectividade ao host/porta (produção):
```powershell
Test-NetConnection -ComputerName www.datse.com.br -Port 80
```

- Comando para testar conectividade local (mock):
```powershell
Test-NetConnection -ComputerName 127.0.0.1 -Port 8000
```

- Comando para startar servidor PHP embutido (teste rápido, precisa do PHP instalado). Use porta 8000 para evitar requerer privilégios de administrador:
```powershell
# execute na pasta que contém syncjava.php
php -S 127.0.0.1:8000
```

- Exemplo de resposta esperada (JSON simples):
```
{"status":"ok","received":{"encrypted_data":"...","salt":"...","client_id":"..."}}
```

Observações finais
- Esses passos são reproduzíveis e seguros. Comece pelo item 1 para confirmar o status, depois vá para 3/4 para colocar um endpoint de teste ou criar o arquivo necessário no DocumentRoot.
- Me diga quando quiser que eu aplique (crie o `syncjava.php` de teste no DocumentRoot, ou implemente o `api.mode` toggle e a flag `synced`).

Credenciais de teste (informação fornecida)
----------------------------------------
O servidor de teste/API aceita o login abaixo para testes funcionais. Use essas credenciais apenas em ambiente de desenvolvimento:

- Usuário: JOAO
- Senha: 1234

Como usar essas credenciais nos testes
-------------------------------------
1) Como Basic Auth (cabeçalho HTTP Authorization)

Exemplo PowerShell (envia form-encoded com Basic Auth) — produção:

```powershell
$user = 'JOAO'
$pass = '1234'
$pair = [System.Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes("$user`:$pass"))
Invoke-WebRequest -Uri 'http://www.datse.com.br/dev/syncjava2.php' -Method POST -Headers @{ Authorization = "Basic $pair" } -Body @{ test='1' } -UseBasicParsing
```

Exemplo PowerShell (local mock):

```powershell
$user = 'JOAO'
$pass = '1234'
$pair = [System.Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes("$user`:$pass"))
Invoke-WebRequest -Uri 'http://127.0.0.1:8000/syncjava.php' -Method POST -Headers @{ Authorization = "Basic $pair" } -Body @{ test='1' } -UseBasicParsing
```

Exemplo cURL (Linux/WSL/git-bash) — produção:

```bash
curl -u JOAO:1234 -X POST -d "test=1" http://www.datse.com.br/dev/syncjava2.php
```

Exemplo cURL (local mock):

```bash
curl -u JOAO:1234 -X POST -d "test=1" http://127.0.0.1:8000/syncjava.php
```

2) Como campos do formulário (se o servidor esperar credenciais no corpo do POST)

Envie `username` e `password` como parte do formulário (exemplo PowerShell) — produção:

```powershell
Invoke-WebRequest -Uri 'http://www.datse.com.br/dev/syncjava2.php' -Method POST -Body @{ username='JOAO'; password='1234'; test='1' } -UseBasicParsing
```

Exemplo PowerShell (local mock):

```powershell
Invoke-WebRequest -Uri 'http://127.0.0.1:8000/syncjava.php' -Method POST -Body @{ username='JOAO'; password='1234'; test='1' } -UseBasicParsing
```

Configuração no projeto (sugestão)
----------------------------------
Para facilitar o uso dessas credenciais em testes e não espalhar strings no código, sugiro adicionar as seguintes propriedades (opcionais) em `config/api.properties`:

```
# credenciais opcionais (apenas para desenvolvimento)
api.auth.user=JOAO
api.auth.password=1234
```

E então alterar `controle.config.APIConfig` para carregar `api.auth.user` e `api.auth.password` (com fallback para variáveis de ambiente). No `HttpSyncUtil.sendPost` você pode incluir o cabeçalho Authorization Basic quando as credenciais estiverem presentes:

```java
String user = APIConfig.getAuthUser();
String pass = APIConfig.getAuthPassword();
if (user != null && pass != null) {
    String basic = java.util.Base64.getEncoder().encodeToString((user + ":" + pass).getBytes(java.nio.charset.StandardCharsets.UTF_8));
    con.setRequestProperty("Authorization", "Basic " + basic);
}
```

Segurança — cuidado importante
-----------------------------
- Não comite credenciais reais no repositório. Estas credenciais (JOAO/1234) foram fornecidas apenas para testes locais.
- Para produção, sempre use segredos via variáveis de ambiente ou um cofre de segredos. Nunca deixe senhas no `config/api.properties` em repositórios públicos.
- Considere usar HTTPS em produção para proteger credenciais em trânsito (TLS).

Próximos passos sugeridos (quando formos implementar as mudanças no projeto)
- Implementar carregamento de `api.auth.user`/`api.auth.password` em `APIConfig` com fallback para variáveis de ambiente.
- Atualizar `HttpSyncUtil.sendPost` para adicionar cabeçalho `Authorization` quando as credenciais estiverem presentes.
- Opcional: adicionar uma entrada na UI de configurações para modo 'Dev/Test' que habilite credenciais de teste apenas locais.

