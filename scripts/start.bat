@echo off
cd /d %~dp0\..
echo Starting PUML Server on port 7235...
echo.
if not exist "target\puml-server-0.0.3-SNAPSHOT.jar" (
    echo JAR file not found. Building project first...
    call scripts\build.bat
    if %ERRORLEVEL% NEQ 0 (
        echo Build failed. Cannot start server.
        exit /b %ERRORLEVEL%
    )
    echo.
)

java --add-opens java.desktop/com.sun.imageio.plugins.png=ALL-UNNAMED ^
     --add-opens java.desktop/com.sun.imageio.plugins.jpeg=ALL-UNNAMED ^
     --add-opens java.desktop/com.sun.imageio.plugins.gif=ALL-UNNAMED ^
     --add-opens java.desktop/com.sun.imageio.plugins.bmp=ALL-UNNAMED ^
     --add-opens java.desktop/com.sun.imageio.plugins.wbmp=ALL-UNNAMED ^
     -jar target\puml-server-0.0.3-SNAPSHOT.jar

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Server failed to start!
    exit /b %ERRORLEVEL%
)

