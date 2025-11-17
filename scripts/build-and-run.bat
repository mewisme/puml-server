@echo off
cd /d %~dp0\..
echo ========================================
echo   PUML Server - Build and Run
echo ========================================
echo.

call scripts\build.bat
if %ERRORLEVEL% NEQ 0 (
    exit /b %ERRORLEVEL%
)

echo.
echo ========================================
echo   Starting Server...
echo ========================================
echo.

call scripts\start.bat

