#!/bin/bash

cd "$(dirname "$0")/.."

echo "Building PUML Server..."
./mvnw clean package -DskipTests

if [ $? -eq 0 ]; then
    echo ""
    echo "Build successful! JAR file created in target/puml-server-0.0.6-SNAPSHOT.jar"
else
    echo ""
    echo "Build failed!"
    exit 1
fi

