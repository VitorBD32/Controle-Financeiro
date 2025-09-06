<#
run-ui.ps1
Cria o classpath runtime via Maven e executa a classe controle.ui.TelaUsuario
Uso: .\run-ui.ps1
#>
param()

Write-Host "Gerando classpath de runtime via Maven..."
# gera arquivo cp.txt com o classpath das dependências usando Start-Process para evitar problemas de parsing
$mvnArgs = @('-q', 'dependency:build-classpath', '-DincludeScope=runtime', '-Dmdep.outputFile=cp.txt')
$proc = Start-Process -FilePath mvn -ArgumentList $mvnArgs -NoNewWindow -Wait -PassThru
if ($proc.ExitCode -ne 0) {
    Write-Error "Falha ao gerar classpath via Maven (exit $($proc.ExitCode)). Verifique se o Maven está instalado e tente executar 'mvn dependency:build-classpath' manualmente."
    exit 1
}

$cp = Get-Content -Raw cp.txt
$fullCp = "target\classes;" + $cp
Write-Host "Executando TelaUsuario com classpath:" $fullCp

Start-Process -FilePath java -ArgumentList "-cp", $fullCp, "controle.ui.TelaUsuario" -NoNewWindow -Wait -PassThru

# limpa arquivo temporário
Remove-Item cp.txt -ErrorAction SilentlyContinue
