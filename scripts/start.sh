#!/bin/bash

cd "$(dirname "$0")/.."

echo "Starting PUML Server on port 7235..."
echo ""

if [ ! -f "target/puml-server-0.0.1-SNAPSHOT.jar" ]; then
    echo "JAR file not found. Building project first..."
    ./scripts/build.sh
    if [ $? -ne 0 ]; then
        echo "Build failed. Cannot start server."
        exit 1
    fi
    echo ""
fi

java --add-opens java.desktop/com.sun.imageio.plugins.png=ALL-UNNAMED \
     --add-opens java.desktop/com.sun.imageio.plugins.jpeg=ALL-UNNAMED \
     --add-opens java.desktop/com.sun.imageio.plugins.gif=ALL-UNNAMED \
     --add-opens java.desktop/com.sun.imageio.plugins.bmp=ALL-UNNAMED \
     --add-opens java.desktop/com.sun.imageio.plugins.wbmp=ALL-UNNAMED \
     -jar target/puml-server-0.0.1-SNAPSHOT.jar

if [ $? -ne 0 ]; then
    echo ""
    echo "Server failed to start!"
    exit 1
fi

