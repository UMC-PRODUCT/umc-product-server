#!/bin/bash
set -euo pipefail

[ -f /etc/codedeploy-app.env ] || exit 0

source /etc/codedeploy-app.env

cd "${SERVER_APP_DIRECTORY:-}" 2>/dev/null || exit 0

if docker compose version >/dev/null 2>&1; then
  docker compose down 2>/dev/null || true
elif command -v docker-compose >/dev/null 2>&1; then
  docker-compose down 2>/dev/null || true
fi
