<#
 .SYNOPSIS
   Gera comandos SQL para cadastrar um usuário (ex: JOAO) no servidor.
   Produz duas variantes: MD5 (legado) e bcrypt (php password_hash) quando possível.

Usage:
    .\generate_add_user_sql.ps1 -User JOAO -Password YOUR_PASSWORD -Name "João da Silva" -Email joao@exemplo.com

#>

param(
    [string]$User = 'JOAO',
    [string]$Password = $env:TEST_PASSWORD,
    [string]$Name = 'João da Silva',
    [string]$Email = 'joao@exemplo.com'
)

if (-not $Password -or $Password -eq '') {
    Write-Host "Info: If you prefer non-interactive execution, set TEST_PASSWORD env var before running this script."
    $securePwd = Read-Host -AsSecureString "Password for user (typing will be hidden)"
    $Password = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePwd))
}

Write-Host "-- Gerando SQL para usuário: $User`n"

Write-Host "-- Variante A1: MD5 (legado)"
$md5Sql = "INSERT INTO usuarios (login, senha, nome, email) VALUES ('$User', MD5('$Password'), '$Name', '$Email');"
Write-Host $md5Sql

Write-Host "`n-- Variante A2: bcrypt (recomendado se o servidor aceitar hashes do PHP)"

# Tenta usar PHP para gerar o hash bcrypt quando disponível
try {
    $php = Get-Command php -ErrorAction Stop
    Write-Host "PHP encontrado em: $($php.Source) — gerando hash bcrypt..."
    $escaped = $Password -replace "'", "\\'"
    $hash = php -r "echo password_hash('$escaped', PASSWORD_BCRYPT);"
    if ($hash) {
        $bcryptSql = "INSERT INTO usuarios (login, senha, nome, email) VALUES ('$User', '$hash', '$Name', '$Email');"
        Write-Host $bcryptSql
    } else {
        Write-Host "-- Falha ao gerar hash via PHP — gere manualmente com: php -r \"echo password_hash('YOUR_PASSWORD', PASSWORD_BCRYPT);\""
    }
} catch {
    Write-Host "-- PHP não encontrado. Para gerar o hash bcrypt execute no servidor (PHP):"
    Write-Host "-- php -r \"echo password_hash('YOUR_PASSWORD', PASSWORD_BCRYPT);\""
    Write-Host "-- Substitua o hash no exemplo abaixo e execute no banco:"
    Write-Host "-- INSERT INTO usuarios (login, senha, nome, email) VALUES ('$User', '\$2y\$...hash...', '$Name', '$Email');"
}

Write-Host "`n-- Atenção: ajuste nomes de tabela/colunas conforme o schema do servidor antes de executar."
