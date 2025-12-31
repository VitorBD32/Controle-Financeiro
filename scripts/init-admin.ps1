param(
    [string]$User = 'YOUR_USER',
    [string]$Email = 'admin@example.com',
    [string]$Name = 'Administrator',
    [string]$Password = $env:TEST_PASSWORD
)

if (-not $Password -or $Password -eq '') {
    $securePwd = Read-Host -AsSecureString "Password for admin user (typing will be hidden)"
    $Password = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePwd))
}

if (-not $Password) {
    Write-Host "Password not provided. Set TEST_PASSWORD env var or pass --Password argument." -ForegroundColor Red
    exit 1
}

Write-Host "Building project and creating admin user..." -ForegroundColor Cyan
mvn -DskipTests package

$classPath = "target/classes"

java -cp $classPath controle.util.InitAdmin --user=$User --email=$Email --name=$Name --password=$Password
