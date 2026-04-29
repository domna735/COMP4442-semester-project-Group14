#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-https://localhost:8443}"
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
    curl -k -sS -o /tmp/smoke_response.json -w "%{http_code}" \
      -X "$method" "$url" \
      -H "Content-Type: application/json" \
      -c "$COOKIE_JAR" -b "$COOKIE_JAR" \
      -d "$body"
  else
    curl -k -sS -o /tmp/smoke_response.json -w "%{http_code}" \
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

# --------------------------
# Main Test Flow
# --------------------------
echo "== Cloud Compute API Smoke Test =="
echo "Base URL: $BASE_URL"
echo ""

require_cmd curl
require_cmd date

# 1. Ping
echo "--- 1. Ping ---"
status=$(http_code GET "$BASE_URL/api/v1/compute/ping")
assert_status "$status" 200 "Ping endpoint"

# 2. Register
echo "--- 2. Register User ---"
register_body="{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}"
status=$(http_code POST "$BASE_URL/api/v1/auth/register" "$register_body")
assert_status "$status" 201 "Register user"

# 3. Login
echo "--- 3. Login User ---"
login_body="{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}"
status=$(http_code POST "$BASE_URL/api/v1/auth/login" "$login_body")
assert_status "$status" 200 "Login user"

# Extract access token from response if needed (for non-cookie auth)
if grep -q '"accessToken"' /tmp/smoke_response.json; then
  ACCESS_TOKEN=$(grep -o '"accessToken":"[^"]*"' /tmp/smoke_response.json | cut -d'"' -f4)
fi

# 4. Get Current User
echo "--- 4. Get Current User ---"
status=$(http_code GET "$BASE_URL/api/v1/auth/me")
assert_status "$status" 200 "Get current user"

# 5. Create Task
echo "--- 5. Create Task ---"
task_body="{\"title\":\"$TASK_TITLE\",\"description\":\"Smoke test task\"}"
status=$(http_code POST "$BASE_URL/api/v1/tasks" "$task_body")
assert_status "$status" 201 "Create task"

# 6. List Tasks
echo "--- 6. List Tasks ---"
status=$(http_code GET "$BASE_URL/api/v1/tasks")
assert_status "$status" 200 "List tasks"

# 7. Logout
echo "--- 7. Logout ---"
status=$(http_code POST "$BASE_URL/api/v1/auth/logout")
assert_status "$status" 200 "Logout user"

echo ""
echo "== All smoke tests passed =="
echo "Created user: $USERNAME"
echo "Created task: $TASK_TITLE"


