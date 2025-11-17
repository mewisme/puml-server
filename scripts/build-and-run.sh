#!/bin/bash

cd "$(dirname "$0")/.."

echo "========================================"
echo "  PUML Server - Build and Run"
echo "========================================"
echo ""

./scripts/build.sh
if [ $? -ne 0 ]; then
    exit 1
fi

echo ""
echo "========================================"
echo "  Starting Server..."
echo "========================================"
echo ""

./scripts/start.sh

