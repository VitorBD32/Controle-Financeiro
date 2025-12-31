<#
Usage:
  powershell -File .\scripts\check-db.ps1

This script compiles the project and runs the CheckDB Java utility.
#>

Write-Host "Compiling project..." -ForegroundColor Cyan
mvn -q compile

Write-Host "Running CheckDB..." -ForegroundColor Cyan
mvn -Dexec.mainClass=controle.tools.CheckDB -Dexec.classpathScope=runtime org.codehaus.mojo:exec-maven-plugin:3.1.0:exec
