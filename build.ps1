# build.ps1 - compila e executa o projeto com Maven se disponível, senão usa javac/java
param()

function Start-Build {
    Write-Host "Tentando usar Maven..."
    if (Get-Command mvn -ErrorAction SilentlyContinue) {
        Write-Host "Maven encontrado. Executando mvn clean compile exec:java..."
        mvn clean compile
        if (Get-Command mvn -ErrorAction SilentlyContinue) {
            Write-Host "Executando mvn exec do plugin..."
            $args = @('-Dexec.mainClass=controle.Main', 'org.codehaus.mojo:exec-maven-plugin:3.1.0:java')
            $proc = Start-Process -FilePath mvn -ArgumentList $args -NoNewWindow -Wait -PassThru
            if ($proc.ExitCode -ne 0) {
                Write-Warning "mvn exec retornou código de erro $($proc.ExitCode)"
            }
        }
        return
    }

    Write-Host "Maven não encontrado. Compilando com javac..."

    # Garante pasta lib
    New-Item -ItemType Directory -Path .\lib -ErrorAction SilentlyContinue | Out-Null

    # Baixa dependências se necessário
    $jarFound = Get-ChildItem -Path .\lib -Filter "mysql-connector*.jar" -File -ErrorAction SilentlyContinue
    if (-not $jarFound) {
        try {
            $url = "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar"
            Write-Host "Baixando MySQL Connector..."
            Invoke-WebRequest -UseBasicParsing -Uri $url -OutFile ".\lib\mysql-connector-j-8.0.33.jar" -ErrorAction Stop
        } catch {
            Write-Warning "Falha ao baixar driver MySQL: $($_.Exception.Message)"
        }
    }

    $bcryptFound = Get-ChildItem -Path .\lib -Filter "jbcrypt*.jar" -File -ErrorAction SilentlyContinue
    if (-not $bcryptFound) {
        try {
            $url = "https://repo1.maven.org/maven2/org/mindrot/jbcrypt/0.4/jbcrypt-0.4.jar"
            Invoke-WebRequest -UseBasicParsing -Uri $url -OutFile ".\lib\jbcrypt-0.4.jar" -ErrorAction Stop
        } catch {
            Write-Warning "Falha ao baixar BCrypt: $($_.Exception.Message)"
        }
    }

    # Limpa e cria diretório de saída
    if (Test-Path .\out) { Remove-Item -Recurse -Force .\out }
    New-Item -ItemType Directory -Path .\out | Out-Null
    
    Write-Host "Compilando projeto com encoding UTF-8..."
    
    $baseSourcePath = ".\src\main\java"
    $libPath = "lib/*"
    
    # Compila as bibliotecas br/ e com/ primeiro
    Write-Host "  -> Compilando br.uespi.*"
    $brFiles = Get-ChildItem -Path ".\br" -Recurse -Filter *.java -ErrorAction SilentlyContinue | Select-Object -ExpandProperty FullName
    if ($brFiles) {
        & javac -encoding UTF-8 -d .\out -cp $libPath $brFiles
    }
    
    # ZXing agora usa JAR em lib/ (zxing-core-3.5.1.jar) - não precisa compilar fonte
    
    # Compila o projeto principal por pacote
    Write-Host "  -> Compilando controle.config.*"
    & javac -encoding UTF-8 -d .\out -cp "lib\*;.\out" -sourcepath $baseSourcePath "$baseSourcePath\controle\config\*.java" 2>$null
    
    Write-Host "  -> Compilando controle.model.*"
    & javac -encoding UTF-8 -d .\out -cp "lib\*;.\out" -sourcepath $baseSourcePath "$baseSourcePath\controle\model\*.java" 2>$null
    
    Write-Host "  -> Compilando controle.dao.*"
    & javac -encoding UTF-8 -d .\out -cp "lib\*;.\out" -sourcepath $baseSourcePath "$baseSourcePath\controle\dao\*.java" 2>$null
    
    Write-Host "  -> Compilando controle.util.*"
    & javac -encoding UTF-8 -d .\out -cp "lib\*;.\out" -sourcepath $baseSourcePath "$baseSourcePath\controle\util\*.java"
    
    Write-Host "  -> Compilando controle.api.*"
    & javac -encoding UTF-8 -d .\out -cp "lib\*;.\out" -sourcepath $baseSourcePath "$baseSourcePath\controle\api\*.java" 2>$null
    
    Write-Host "  -> Compilando controle.ui.*"
    & javac -encoding UTF-8 -d .\out -cp "lib\*;.\out" -sourcepath $baseSourcePath "$baseSourcePath\controle\ui\*.java"
    
    Write-Host "  -> Compilando controle.tools.*"
    & javac -encoding UTF-8 -d .\out -cp "lib\*;.\out" -sourcepath $baseSourcePath "$baseSourcePath\controle\tools\*.java" 2>$null
    
    Write-Host "  -> Compilando controle.Main e controle.Conexao"
    & javac -encoding UTF-8 -d .\out -cp "lib\*;.\out" -sourcepath $baseSourcePath "$baseSourcePath\controle\Main.java" "$baseSourcePath\controle\Conexao.java"
    
    $exitCode = $LASTEXITCODE

    if ($exitCode -eq 0) {
        $cp = ".\out;lib/*"
        Write-Host "Executando controle.Main..."
        & java -cp $cp controle.Main
    } else {
        Write-Warning "Compilação falhou com código $exitCode"
    }
}

Start-Build
