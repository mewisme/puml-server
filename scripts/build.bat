@echo off
cd /d %~dp0\..
echo Building PUML Server...
call mvnw.cmd clean package -DskipTests
if %ERRORLEVEL% EQU 0 (
    echo.
    echo Build successful! JAR file created in target\puml-server-0.0.3-SNAPSHOT.jar
) else (
    echo.
    echo Build failed!
    exit /b %ERRORLEVEL%
)

