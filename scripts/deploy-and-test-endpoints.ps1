# deploy-and-test-endpoints.ps1
# Script para deploy local dos endpoints PHP e execução de testes

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  Deploy e Teste de Endpoints PHP  " -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# 1. Verificar se XAMPP está instalado
Write-Host "[1/6] Verificando instalação do XAMPP..." -ForegroundColor Yellow
if (-not (Test-Path "C:\xampp")) {
    Write-Host "ERRO: XAMPP não encontrado em C:\xampp" -ForegroundColor Red
    Write-Host "Instale o XAMPP ou ajuste o caminho neste script." -ForegroundColor Red
    exit 1
}
Write-Host "✓ XAMPP encontrado" -ForegroundColor Green

# 2. Detectar DocumentRoot do Apache em execução
Write-Host "[2/6] Detectando DocumentRoot do Apache..." -ForegroundColor Yellow
$apacheProcess = Get-Process httpd -ErrorAction SilentlyContinue | Select-Object -First 1
$htdocsPath = "C:\xampp\htdocs"  # Default

if ($apacheProcess) {
    $apacheExePath = (Get-WmiObject Win32_Process -Filter "ProcessId = $($apacheProcess.Id)").ExecutablePath
    if ($apacheExePath) {
        $apacheDir = Split-Path -Parent (Split-Path -Parent $apacheExePath)
        $confFile = Join-Path $apacheDir "conf\httpd.conf"
        if (Test-Path $confFile) {
            $docRoot = Get-Content $confFile | Select-String '^DocumentRoot "(.+)"' | Select-Object -First 1
            if ($docRoot -match 'DocumentRoot "(.+)"') {
                $htdocsPath = $matches[1] -replace '/', '\'
                Write-Host "✓ Apache detectado em: $apacheDir" -ForegroundColor Green
                Write-Host "✓ DocumentRoot: $htdocsPath" -ForegroundColor Green
            }
        }
    }
}

# Copiar arquivos PHP para htdocs
Write-Host "[3/6] Copiando arquivos para $htdocsPath..." -ForegroundColor Yellow
$repoRoot = Split-Path -Parent $PSScriptRoot
$src1 = Join-Path $repoRoot "tools\server_stub\syncjava.php"
$src2 = Join-Path $repoRoot "tools\server_stub\syncjava2.php"

if (-not (Test-Path $src1)) {
    Write-Host "ERRO: Arquivo não encontrado: $src1" -ForegroundColor Red
    exit 1
}
if (-not (Test-Path $src2)) {
    Write-Host "ERRO: Arquivo não encontrado: $src2" -ForegroundColor Red
    exit 1
}

if (-not (Test-Path $htdocsPath)) {
    Write-Host "ERRO: Diretório htdocs não encontrado: $htdocsPath" -ForegroundColor Red
    exit 1
}

Copy-Item $src1 -Destination (Join-Path $htdocsPath "syncjava.php") -Force
Copy-Item $src2 -Destination (Join-Path $htdocsPath "syncjava2.php") -Force
Write-Host "✓ Arquivos copiados:" -ForegroundColor Green
Write-Host "  - $(Join-Path $htdocsPath 'syncjava.php')" -ForegroundColor Gray
Write-Host "  - $(Join-Path $htdocsPath 'syncjava2.php')" -ForegroundColor Gray

# 3. Verificar se MySQL está rodando
Write-Host "[4/6] Verificando MySQL..." -ForegroundColor Yellow
$mysqlRunning = Get-Process mysqld -ErrorAction SilentlyContinue
if (-not $mysqlRunning) {
    Write-Host "AVISO: MySQL não está rodando." -ForegroundColor Yellow
    Write-Host "Inicie o MySQL via XAMPP Control Panel antes de testar." -ForegroundColor Yellow
} else {
    Write-Host "✓ MySQL está rodando (PID: $($mysqlRunning.Id))" -ForegroundColor Green
}

# 4. Verificar banco prova1
Write-Host "[5/6] Verificando banco de dados prova1..." -ForegroundColor Yellow
try {
    $dbCheck = & "C:\xampp\mysql\bin\mysql.exe" -uroot -e "USE prova1; SELECT COUNT(*) as total FROM usuarios;" 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Banco prova1 acessível" -ForegroundColor Green
    } else {
        Write-Host "AVISO: Não foi possível acessar o banco prova1" -ForegroundColor Yellow
        Write-Host "Execute: CREATE DATABASE prova1; e crie as tabelas." -ForegroundColor Yellow
    }
} catch {
    Write-Host "AVISO: Erro ao verificar banco de dados" -ForegroundColor Yellow
}

# 5. Verificar se Apache está rodando
Write-Host "[6/6] Verificando Apache..." -ForegroundColor Yellow
$apacheRunning = Get-Process httpd -ErrorAction SilentlyContinue
if (-not $apacheRunning) {
    Write-Host "AVISO: Apache não está rodando." -ForegroundColor Yellow
    Write-Host "Inicie o Apache via XAMPP Control Panel antes de testar." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Para iniciar Apache e MySQL:" -ForegroundColor Cyan
    Write-Host '  1. Abra o XAMPP Control Panel' -ForegroundColor Gray
    Write-Host '  2. Clique em "Start" para Apache' -ForegroundColor Gray
    Write-Host '  3. Clique em "Start" para MySQL' -ForegroundColor Gray
    Write-Host ""
    Write-Host "Após iniciar, execute este script novamente para testar." -ForegroundColor Cyan
    exit 0
}
Write-Host "✓ Apache está rodando (PID: $($apacheRunning[0].Id))" -ForegroundColor Green

# 6. Executar testes HTTP
Write-Host "[7/7] Executando testes HTTP..." -ForegroundColor Yellow
Write-Host ""

$tests = @(
    @{
        Name = "Teste 1: POST com username/password (syncjava.php)"
        Uri = "http://localhost/syncjava.php"
        Body = @{ username='JOAO'; password='1234' }
    },
    @{
        Name = "Teste 2: POST com email/senha (syncjava2.php)"
        Uri = "http://localhost/syncjava2.php"
        Body = @{ email='joao23@gmail.com'; senha='1234' }
    },
    @{
        Name = "Teste 3: POST com nome/password (syncjava.php)"
        Uri = "http://localhost/syncjava.php"
        Body = @{ nome='JOAO'; password='1234' }
    },
    @{
        Name = "Teste 4: POST com encrypted_data (fallback)"
        Uri = "http://localhost/syncjava2.php"
        Body = @{ encrypted_data='TESTDATA'; client_id='powershell-test' }
    }
)

$successCount = 0
$failCount = 0

foreach ($test in $tests) {
    Write-Host "► $($test.Name)" -ForegroundColor Cyan
    try {
        $response = Invoke-RestMethod -Uri $test.Uri -Method Post -Body $test.Body -ErrorAction Stop
        if ($response.auth_ok -eq $true) {
            Write-Host "  ✓ SUCESSO: auth_ok = true" -ForegroundColor Green
            if ($response.user) {
                Write-Host "  Usuário: $($response.user.nome) ($($response.user.email))" -ForegroundColor Gray
            }
            $successCount++
        } else {
            Write-Host "  ✗ FALHA: auth_ok = false" -ForegroundColor Red
            Write-Host "  Mensagem: $($response.message)" -ForegroundColor Gray
            $failCount++
        }
    } catch {
        Write-Host "  ✗ ERRO: $($_.Exception.Message)" -ForegroundColor Red
        $failCount++
    }
    Write-Host ""
}

# Resumo
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  Resumo dos Testes" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Sucessos: $successCount" -ForegroundColor Green
Write-Host "Falhas:   $failCount" -ForegroundColor $(if ($failCount -gt 0) { "Red" } else { "Gray" })
Write-Host ""

if ($successCount -gt 0) {
    Write-Host "✓ Endpoints estão funcionando e conectados ao banco!" -ForegroundColor Green
    Write-Host ""
    Write-Host "URLs disponíveis:" -ForegroundColor Cyan
    Write-Host "  - http://localhost/syncjava.php" -ForegroundColor Gray
    Write-Host "  - http://localhost/syncjava2.php" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Configure o desktop Java para usar uma dessas URLs." -ForegroundColor Cyan
} else {
    Write-Host "⚠ Nenhum teste passou. Verifique:" -ForegroundColor Yellow
    Write-Host "  1. Apache e MySQL estão rodando?" -ForegroundColor Gray
    Write-Host "  2. Banco prova1 existe e tem tabela usuarios?" -ForegroundColor Gray
    Write-Host "  3. Usuário JOAO está cadastrado?" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Execute para inserir JOAO:" -ForegroundColor Cyan
    Write-Host '  & "C:\xampp\mysql\bin\mysql.exe" -uroot prova1 -e "INSERT INTO usuarios (nome, email, senha) VALUES (''JOAO'', ''joao23@gmail.com'', ''1234'');"' -ForegroundColor Gray
}
