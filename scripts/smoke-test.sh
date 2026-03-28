#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
COOKIE_JAR="$(mktemp)"
USERNAME="demo_user_$(date +%s)"
EMAIL="${USERNAME}@example.com"
PASSWORD="DemoPass123!"
TASK_TITLE="Smoke test task $(date +%H%M%S)"

cleanup() {
  rm -f "$COOKIE_JAR"
}
trap cleanup EXIT

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "[ERROR] Missing required command: $1"
    exit 1
  fi
}

http_code() {
  local method="$1"
  local url="$2"
  local body="${3:-}"

  if [[ -n "$body" ]]; then
    curl -sS -o /tmp/smoke_response.json -w "%{http_code}" \
      -X "$method" "$url" \
      -H "Content-Type: application/json" \
      -c "$COOKIE_JAR" -b "$COOKIE_JAR" \
      -d "$body"
  else
    curl -sS -o /tmp/smoke_response.json -w "%{http_code}" \
      -X "$method" "$url" \
      -c "$COOKIE_JAR" -b "$COOKIE_JAR"
  fi
}

assert_status() {
  local actual="$1"
  local expected="$2"
  local step="$3"
  if [[ "$actual" != "$expected" ]]; then
    echo "[FAIL] $step -> expected HTTP $expected, got $actual"
    echo "[DEBUG] Response body:"
    cat /tmp/smoke_response.json || true
    exit 1
  fi
  echo "[PASS] $step -> HTTP $actual"
}

require_cmd curl

echo "== Cloud Compute API Smoke Test =="
echo "Base URL: $BASE_URL"

code="$(http_code GET "$BASE_URL/api/v1/compute/ping")"
assert_status "$code" "200" "Ping endpoint"

register_payload=$(cat <<JSON
{"username":"$USERNAME","email":"$EMAIL","password":"$PASSWORD"}
JSON
)
code="$(http_code POST "$BASE_URL/api/v1/auth/register" "$register_payload")"
assert_status "$code" "200" "Register user"

login_payload=$(cat <<JSON
{"username":"$USERNAME","password":"$PASSWORD"}
JSON
)
code="$(http_code POST "$BASE_URL/api/v1/auth/login" "$login_payload")"
assert_status "$code" "200" "Login user"

code="$(http_code GET "$BASE_URL/api/v1/auth/me")"
assert_status "$code" "200" "Get current user"
if ! grep -q "$USERNAME" /tmp/smoke_response.json; then
  echo "[FAIL] Current user response does not contain expected username"
  cat /tmp/smoke_response.json || true
  exit 1
fi

task_payload=$(cat <<JSON
{"title":"$TASK_TITLE","description":"Auto smoke test task","status":"TODO"}
JSON
)
code="$(http_code POST "$BASE_URL/api/v1/tasks" "$task_payload")"
assert_status "$code" "201" "Create task"

code="$(http_code GET "$BASE_URL/api/v1/tasks")"
assert_status "$code" "200" "List tasks"
if ! grep -q "$TASK_TITLE" /tmp/smoke_response.json; then
  echo "[FAIL] Task list does not contain created task"
  cat /tmp/smoke_response.json || true
  exit 1
fi

code="$(http_code POST "$BASE_URL/api/v1/auth/logout")"
assert_status "$code" "200" "Logout user"

echo ""
echo "All smoke tests passed."
echo "Created user: $USERNAME"
echo "Created task: $TASK_TITLE"
