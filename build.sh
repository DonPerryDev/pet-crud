#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "==> Building backend..."
cd "$SCRIPT_DIR/back"
./gradlew clean build -x test

echo "==> Building frontend..."
cd "$SCRIPT_DIR/front"
npm install
npm run build

echo "==> Build complete."
