# build.ps1 - compila e executa o projeto com Maven se disponível, senão usa javac/java
param()

function Start-Build {
    Write-Host "Tentando usar Maven..."
    if (Get-Command mvn -ErrorAction SilentlyContinue) {
        Write-Host "Maven encontrado. Executando mvn clean compile exec:java..."
        mvn clean compile
        # use the mainClass configured in pom.xml; if needed, fallback to passing property before goal
        if (Get-Command mvn -ErrorAction SilentlyContinue) {
            Write-Host "Executando mvn exec do plugin (coordenadas explícitas) para evitar ambiguidade..."
            # Evita que o PowerShell tente interpretar tokens que começam com '-' como parâmetros do próprio shell.
            # Usa Start-Process com ArgumentList para passar os argumentos literalmente.
            $args = @('-Dexec.mainClass=controle.Main', 'org.codehaus.mojo:exec-maven-plugin:3.1.0:java')
            $proc = Start-Process -FilePath mvn -ArgumentList $args -NoNewWindow -Wait -PassThru
            if ($proc.ExitCode -ne 0) {
                Write-Warning "mvn exec retornou código de erro $($proc.ExitCode)"
                Write-Host "Tentando fallback usando tokenizador --% (PowerShell pass-through)..."
                # fallback: usa --% para evitar parsing; nem todos os ambientes PowerShell suportam --%, mas vale tentar
                & mvn --% -Dexec.mainClass=controle.Main org.codehaus.mojo:exec-maven-plugin:3.1.0:java
                if ($LASTEXITCODE -ne 0) { Write-Warning "Fallback mvn --% também falhou com código $LASTEXITCODE" }
            }
        }
        return
    }

    Write-Host "Maven não encontrado. Tentando compilar com javac e garantir driver MySQL em lib/*."

    # Garante pasta lib
    New-Item -ItemType Directory -Path .\lib -ErrorAction SilentlyContinue | Out-Null

    # Se não houver driver MySQL, tenta baixar a versão mais recente do Maven Central
    $jarFound = Get-ChildItem -Path .\lib -Filter "mysql-connector-java*.jar" -File -ErrorAction SilentlyContinue
    if (-not $jarFound) {
        Write-Host "Driver MySQL não encontrado em lib/. Tentando baixar automaticamente..."
        try {
            $meta = Invoke-WebRequest -UseBasicParsing 'https://repo1.maven.org/maven2/mysql/mysql-connector-java/maven-metadata.xml'
            [xml]$m = $meta.Content
            $latest = $m.metadata.versioning.latest
            if (-not $latest) { $latest = $m.metadata.versioning.release }
            $url = "https://repo1.maven.org/maven2/mysql/mysql-connector-java/$latest/mysql-connector-java-$latest.jar"
            Write-Host "Baixando $url"
            Invoke-WebRequest -UseBasicParsing -Uri $url -OutFile (".\lib\mysql-connector-java-$latest.jar") -ErrorAction Stop
            Write-Host "Download concluído: .\lib\mysql-connector-java-$latest.jar"
        } catch {
            Write-Warning "Falha ao baixar automaticamente o driver: $($_.Exception.Message)"
            Write-Host "Por favor, baixe manualmente o JAR do MySQL Connector e coloque em .\lib, ou instale o Maven."
        }
    } else {
        Write-Host "Driver MySQL detectado: $($jarFound.Name)"
    }

    # Compilar com javac incluindo lib/*
    if (Test-Path .\out) { Remove-Item -Recurse -Force .\out }
    New-Item -ItemType Directory -Path .\out | Out-Null
    $files = Get-ChildItem -Path .\src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
    Write-Host "Compilando fontes..."
    & javac -d .\out -cp "lib/*" $files

    # Executar com classpath incluindo jars em lib
    $cp = ".\out;lib/*"
    Write-Host "Executando controle.Main..."
    & java -cp $cp controle.Main
}

Start-Build
