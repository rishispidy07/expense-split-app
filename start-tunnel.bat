@echo off
echo ========================================
echo   SplitEase - Public URL via Cloudflare
echo ========================================
echo.
echo Starting tunnel to expose port 8080...
echo.

if not exist "cloudflared.exe" (
    echo [ERROR] cloudflared.exe not found in project folder.
    echo Download from: https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-windows-amd64.exe
    pause
    exit /b 1
)

cloudflared.exe tunnel --url http://127.0.0.1:8080
