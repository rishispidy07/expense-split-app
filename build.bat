@echo off
echo ========================================
echo   SplitEase - Build
echo ========================================
cd /d "%~dp0"
javac -cp "backend\src;backend\lib\*" -d backend\bin -sourcepath backend\src backend\src\com\expensesplit\App.java
echo.
echo Build complete. Classes in backend\bin
pause
