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

extract_json_field() {
  local field="$1"
  sed -n 's/.*"'"$field"'"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' /tmp/smoke_response.json | head -n 1
}

http_code() {
  local method="$1"
  local url="$2"
  local body="${3:-}"
  local token="${4:-}"
  local auth_header=()

  if [[ -n "$token" ]]; then
    auth_header=(-H "Authorization: Bearer $token")
  fi

  if [[ -n "$body" ]]; then
    curl -sS -o /tmp/smoke_response.json -w "%{http_code}" \
      -X "$method" "$url" \
      -H "Content-Type: application/json" \
      "${auth_header[@]}" \
      -c "$COOKIE_JAR" -b "$COOKIE_JAR" \
      -d "$body"
  else
    curl -sS -o /tmp/smoke_response.json -w "%{http_code}" \
      -X "$method" "$url" \
      "${auth_header[@]}" \
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
assert_status "$code" "201" "Register user"

login_payload=$(cat <<JSON
{"username":"$USERNAME","password":"$PASSWORD"}
JSON
)
code="$(http_code POST "$BASE_URL/api/v1/auth/login" "$login_payload")"
assert_status "$code" "200" "Login user"

ACCESS_TOKEN="$(extract_json_field accessToken)"
REFRESH_TOKEN="$(extract_json_field refreshToken)"
if [[ -z "$ACCESS_TOKEN" || -z "$REFRESH_TOKEN" ]]; then
  echo "[FAIL] Login response missing accessToken or refreshToken"
  cat /tmp/smoke_response.json || true
  exit 1
fi

code="$(http_code GET "$BASE_URL/api/v1/auth/me" "" "$ACCESS_TOKEN")"
assert_status "$code" "200" "Get current user"

create_task_payload=$(cat <<JSON
{"title":"$TASK_TITLE","description":"Created by smoke test","status":"TODO"}
JSON
)
code="$(http_code POST "$BASE_URL/api/v1/tasks" "$create_task_payload" "$ACCESS_TOKEN")"
assert_status "$code" "201" "Create task"

code="$(http_code GET "$BASE_URL/api/v1/tasks" "" "$ACCESS_TOKEN")"
assert_status "$code" "200" "List tasks"

refresh_payload=$(cat <<JSON
{"refreshToken":"$REFRESH_TOKEN"}
JSON
)
code="$(http_code POST "$BASE_URL/api/v1/auth/refresh" "$refresh_payload")"
assert_status "$code" "200" "Refresh access token"

NEW_ACCESS_TOKEN="$(extract_json_field accessToken)"
if [[ -n "$NEW_ACCESS_TOKEN" ]]; then
  ACCESS_TOKEN="$NEW_ACCESS_TOKEN"
fi

code="$(http_code POST "$BASE_URL/api/v1/auth/logout" "" "$ACCESS_TOKEN")"
assert_status "$code" "200" "Logout user"

code="$(http_code GET "$BASE_URL/api/v1/tasks")"
assert_status "$code" "401" "Reject unauthenticated tasks access"

echo "All smoke tests passed."
echo "Created user: $USERNAME"
echo "Created task: $TASK_TITLE"
