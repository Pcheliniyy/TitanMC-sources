@echo off
title TitanMC Build
chcp 65001 >nul 2>&1

echo.
echo   ======================================
echo     TitanMC 1.16.5 - Build
echo   ======================================
echo.

set ROOT=%~dp0
set PAPER_JAR=%ROOT%build\paper-1.16.5.jar
set OUTPUT=%ROOT%server\TitanMC.jar

where java >nul 2>&1
if errorlevel 1 (
    echo   [ERROR] Java not found! Get JDK 11+: https://adoptium.net/
    pause
    exit /b 1
)
echo   [OK] Java found

if not exist "%ROOT%build" mkdir "%ROOT%build"
if not exist "%PAPER_JAR%" (
    echo   [..] Downloading Paper 1.16.5 ...
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://api.papermc.io/v2/projects/paper/versions/1.16.5/builds/794/downloads/paper-1.16.5-794.jar' -OutFile '%PAPER_JAR%' -UseBasicParsing"
    if errorlevel 1 (
        echo   [ERROR] Failed to download Paper!
        pause
        exit /b 1
    )
    echo   [OK] Paper downloaded
) else (
    echo   [OK] Paper found
)

echo   [..] Compiling TitanMC...
cd /d "%ROOT%"
call gradlew.bat buildTitan --no-daemon -q
if errorlevel 1 (
    echo   [ERROR] Build failed!
    pause
    exit /b 1
)
echo   [OK] Compiled

if not exist "%ROOT%server" mkdir "%ROOT%server"
copy /Y "%ROOT%build\libs\TitanMC-1.16.5.jar" "%OUTPUT%" >nul
echo   [OK] JAR ready

echo.
echo   ======================================
echo     DONE!
echo   ======================================
echo.
echo   JAR: server\TitanMC.jar
echo.
echo   Run:
echo     cd server
echo     java -Xmx4G -jar TitanMC.jar
echo.
pause
