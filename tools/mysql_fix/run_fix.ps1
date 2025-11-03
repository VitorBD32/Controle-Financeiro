<#
run_fix.ps1
PowerShell helper to apply the fix_auth.sql using the mysql CLI if available.

Usage (PowerShell):
  .\run_fix.ps1 -SqlFile .\fix_auth.sql -MysqlExecutable "C:\\xampp\\mysql\\bin\\mysql.exe" -DbRootUser root

This script does not embed passwords on the command-line. It will prompt for the
DB root password (if required) and then execute the SQL script.
#>

param(
    [string]$SqlFile = "fix_auth.sql",
    [string]$MysqlExecutable = "mysql",
    [string]$DbRootUser = "root"
)

if (-not (Test-Path $SqlFile)) {
    Write-Error "SQL file not found: $SqlFile"
    exit 1
}

Write-Host "Applying SQL file: $SqlFile using mysql executable: $MysqlExecutable"

$pwd = Read-Host -AsSecureString "Enter password for MySQL user '$DbRootUser' (press Enter for empty)"
$bstr = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($pwd)
$plain = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($bstr)
[System.Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)

$args = @()
$args += "-u"; $args += $DbRootUser
if ($plain -ne "") { $args += "-p$plain" }

try {
    & $MysqlExecutable @args -e "source $SqlFile"
    if ($LASTEXITCODE -eq 0) {
        Write-Host "SQL successfully applied."
    } else {
        Write-Error "mysql exited with code $LASTEXITCODE"
    }
} catch {
    Write-Error "Failed to run mysql: $_"
}
