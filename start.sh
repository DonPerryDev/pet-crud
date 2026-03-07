#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

cd "$SCRIPT_DIR"

echo "==> Building apps..."
./build.sh

echo "==> Starting all services..."
docker compose up --build -d

echo ""
echo "==> All services started."
echo "    Frontend : http://localhost:3000"
echo "    Backend  : http://localhost:8080"
echo "    Health   : http://localhost:9090/management/health"
echo ""
echo "To stop: docker compose down"
