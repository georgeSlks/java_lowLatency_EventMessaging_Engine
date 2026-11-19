#!/bin/bash

set -e

cd "$(dirname "$0")"

echo "Loading the environment variables(.env) file..."

set -a
source .env
set +a

echo "Cleaning previous build(delete previous out/ and create it again)..."

rm -rf out
mkdir -p out

echo "Compiling Java sources..."

javac -d out $(find src/main/java -name "*.java")

echo "Starting Event Messaging Engine..."

echo "Max Payload     : 1 MiB"

java -cp out engine.Main
