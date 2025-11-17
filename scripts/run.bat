@echo off
cd /d %~dp0\..
java --add-opens java.desktop/com.sun.imageio.plugins.png=ALL-UNNAMED --add-opens java.desktop/com.sun.imageio.plugins.jpeg=ALL-UNNAMED --add-opens java.desktop/com.sun.imageio.plugins.gif=ALL-UNNAMED --add-opens java.desktop/com.sun.imageio.plugins.bmp=ALL-UNNAMED --add-opens java.desktop/com.sun.imageio.plugins.wbmp=ALL-UNNAMED -jar target\puml-server-0.0.1-SNAPSHOT.jar

