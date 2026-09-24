#!/bin/bash
# Compiles all Java sources into the bin/ folder, using the MySQL driver jar in lib/
set -e
mkdir -p bin
find src -name "*.java" > sources.txt
javac -d bin -cp "lib/*" @sources.txt
rm sources.txt
echo "Compiled successfully into bin/"
