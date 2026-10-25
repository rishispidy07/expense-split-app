@echo off
title SplitEase - Public Tunnel
echo ========================================
echo   SplitEase - Starting Services
echo ========================================
cd /d "%~dp0"

echo [1/2] Starting Java server...
start "SplitEase-Server" /min java -cp "backend\classes;backend\lib\*" com.expensesplit.App
timeout /t 5 /nobreak >nul

curl -s http://localhost:8080 >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Server failed to start!
    pause
    exit /b 1
)
echo [OK] Server running on port 8080

echo [2/2] Starting public tunnel...
echo.
echo ========================================
echo   Wait for URL, then open on any device:
echo ========================================
echo.
cloudflared.exe tunnel --url http://127.0.0.1:8080
