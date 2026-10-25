@echo off
echo ========================================
echo   SplitEase - Run Server
echo ========================================
cd /d "%~dp0"
java -cp "backend\bin;backend\lib\*" com.expensesplit.App
