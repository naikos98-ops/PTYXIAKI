# Load environment variables from .env file
function Load-Env {
    param (
        [string]$envPath = ".env"
    )
    if (Test-Path $envPath) {
        Get-Content $envPath | ForEach-Object {
            $line = $_.Trim()
            if ($line -and -not $line.StartsWith("#")) {
                if ($line -match '^\s*([^#=\s]+)\s*=\s*(.*)\s*$') {
                    $name = $Matches[1].Trim()
                    $value = $Matches[2].Trim()
                    # Remove surrounding quotes if present
                    if ($value -match '^"(.*)"$' -or $value -match "^'(.*)'$") {
                        $value = $Matches[1]
                    }
                    $env:$name = $value
                }
            }
        }
        Write-Host "Loaded environment variables from $envPath" -ForegroundColor Green
    }
}

# Local Java Path configuration
$toolsDir = "c:\Users\Naiko\Desktop\EstateSol\tools"
$javaPath = "$toolsDir\jdk\jdk-17.0.19+10"

if (-not (Test-Path $javaPath)) {
    Write-Host "Java 17 is not set up! Running tools\setup_java.ps1 first..." -ForegroundColor Yellow
    & ".\tools\setup_java.ps1"
}

# Set JAVA_HOME and update Path for the current session
$env:JAVA_HOME = $javaPath
$env:PATH = "$javaPath\bin;$env:PATH"

# Load the environment variables
Load-Env

Write-Host "JAVA_HOME has been set to: $env:JAVA_HOME" -ForegroundColor Green

# Let the user choose what to start
Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "           EstateSol Runner Script        " -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "1. Start Backend (Spring Boot)"
Write-Host "2. Start Frontend (Vue.js Dev Server)"
Write-Host "3. Start BOTH (Backend and Frontend in separate windows)"
Write-Host "==========================================" -ForegroundColor Cyan
$choice = Read-Host "Select an option (1-3)"

# Command helper to parse and load .env inside a sub-process
$loadEnvSubCommand = "if (Test-Path '../.env') { Get-Content '../.env' | ForEach-Object { if (`$_ -match '^\s*([^#=\s]+)\s*=\s*(.*)\s*`$') { `$name = `$Matches[1].Trim(); `$value = `$Matches[2].Trim(); if (`$value -match '^`\"(.*)`\"`$' -or `$value -match '^`''(.*)`''`$') { `$value = `$Matches[1] }; Set-Item -Path 'Env:\'+`$name -Value `$value } } }"

if ($choice -eq "1") {
    Write-Host "Starting Backend..." -ForegroundColor Green
    cd EstateSol
    .\mvnw.cmd spring-boot:run
} elseif ($choice -eq "2") {
    Write-Host "Starting Frontend..." -ForegroundColor Green
    cd VUE-JS
    npm run dev
} elseif ($choice -eq "3") {
    Write-Host "Starting Backend in a new window..." -ForegroundColor Green
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "`$env:JAVA_HOME='$javaPath'; `$env:PATH='$javaPath\bin;'+`$env:PATH; cd EstateSol; $loadEnvSubCommand; .\mvnw.cmd spring-boot:run"
    
    Write-Host "Starting Frontend in a new window..." -ForegroundColor Green
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd VUE-JS; npm run dev"
} else {
    Write-Host "Invalid option. Exiting." -ForegroundColor Red
}

