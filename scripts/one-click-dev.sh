#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOG_FILE="$ROOT_DIR/.one-click-dev.log"
BASE_URL="${BASE_URL:-https://localhost:8443}"
STOP_AFTER_TEST=0
STARTED_BY_SCRIPT=0
APP_PID=""

usage() {
  cat <<EOF
Usage: ./scripts/one-click-dev.sh [--stop-after-test]
Options:
  --stop-after-test   Stop the Spring Boot process after smoke test
EOF
}

for arg in "$@"; do
  case "$arg" in
    --stop-after-test) STOP_AFTER_TEST=1 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown option: $arg"; usage; exit 1 ;;
  esac
done

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "[ERROR] Missing required command: $1"
    exit 1
  fi
}

is_up() {
  local code
  code=$(curl -k -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/v1/compute/ping" || true)
  [[ "$code" == "200" ]]
}

wait_for_server() {
  local retries=90
  while (( retries > 0 )); do
    if is_up; then return 0; fi
    sleep 1
    ((retries--))
  done
  return 1
}

cleanup() {
  if [[ "$STOP_AFTER_TEST" -eq 1 && "$STARTED_BY_SCRIPT" -eq 1 && -n "$APP_PID" ]]; then
    kill "$APP_PID" 2>/dev/null || true
  fi
}
trap cleanup EXIT

require_cmd java mvn curl

echo "== One-Click Local Setup =="
echo "Project root: $ROOT_DIR"

if is_up; then
  echo "App running at $BASE_URL"
else
  echo "Starting Spring Boot (SSL/8443)..."
  (cd "$ROOT_DIR"; mvn -q -DskipTests spring-boot:run > "$LOG_FILE" 2>&1) &
  APP_PID=$!
  STARTED_BY_SCRIPT=1

  if wait_for_server; then
    echo "App ready: $BASE_URL"
  else
    echo "[ERROR] Startup failed"
    exit 1
  fi
fi

echo "Running tests..."
"$ROOT_DIR/scripts/smoke-test.sh"

echo -e "\nSetup completed!"
echo "App: $BASE_URL"
