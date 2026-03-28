#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOG_FILE="$ROOT_DIR/.one-click-dev.log"
BASE_URL="${BASE_URL:-http://localhost:8080}"
STOP_AFTER_TEST=0
STARTED_BY_SCRIPT=0
APP_PID=""

usage() {
  cat <<EOF
Usage: ./scripts/one-click-dev.sh [--stop-after-test]

Options:
  --stop-after-test   Stop the Spring Boot process after smoke test

Environment variables:
  BASE_URL            Target base URL for health check and smoke test (default: http://localhost:8080)
EOF
}

for arg in "$@"; do
  case "$arg" in
    --stop-after-test)
      STOP_AFTER_TEST=1
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown option: $arg"
      usage
      exit 1
      ;;
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
  code=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/v1/compute/ping" || true)
  [[ "$code" == "200" ]]
}

wait_for_server() {
  local retries=90
  while (( retries > 0 )); do
    if is_up; then
      return 0
    fi
    sleep 1
    ((retries--))
  done
  return 1
}

cleanup() {
  if [[ "$STOP_AFTER_TEST" -eq 1 && "$STARTED_BY_SCRIPT" -eq 1 && -n "$APP_PID" ]]; then
    echo "Stopping Spring Boot process (PID: $APP_PID)..."
    kill "$APP_PID" >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT

require_cmd java
require_cmd mvn
require_cmd curl

echo "== One-Click Local Setup and Test =="
echo "Project root: $ROOT_DIR"

if is_up; then
  echo "Spring Boot app already running at $BASE_URL"
else
  echo "Starting Spring Boot app with H2 dev profile..."
  (
    cd "$ROOT_DIR"
    mvn -q -DskipTests spring-boot:run > "$LOG_FILE" 2>&1
  ) &
  APP_PID=$!
  STARTED_BY_SCRIPT=1

  if wait_for_server; then
    echo "Application is up: $BASE_URL"
  else
    echo "[ERROR] Application did not start within timeout."
    echo "Check logs: $LOG_FILE"
    exit 1
  fi
fi

echo "Running API smoke test..."
"$ROOT_DIR/scripts/smoke-test.sh"

echo ""
echo "Setup + smoke test completed successfully."
if [[ "$STOP_AFTER_TEST" -eq 1 ]]; then
  if [[ "$STARTED_BY_SCRIPT" -eq 1 ]]; then
    echo "The app will be stopped because --stop-after-test was used."
  else
    echo "--stop-after-test was requested, but the app was already running and was not stopped by this script."
  fi
else
  echo "App is available at: $BASE_URL"
  if [[ "$STARTED_BY_SCRIPT" -eq 1 ]]; then
    echo "Log file: $LOG_FILE"
    echo "To stop app later: kill $APP_PID"
  fi
fi
