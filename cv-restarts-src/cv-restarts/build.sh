#!/bin/bash
# Build script for cv-restarts
# Requires: Java 21+, Maven 3.6+
echo "Building cv-restarts..."
mvn clean package -q
if [ $? -eq 0 ]; then
    echo "✅ Build successful! Plugin jar: target/cv-restarts.jar"
    echo "Copy it to your Paper server's plugins/ folder."
else
    echo "❌ Build failed. Check the error above."
fi
