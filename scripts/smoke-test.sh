#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-https://localhost:8443}"
COOKIE_JAR="$(mktemp)"
USERNAME="demo_user_$(date +%s)"
EMAIL="${USERNAME}@example.com"
PASSWORD="DemoPass123!"
TASK_TITLE="Smoke test task $(date +%H%M%S)"
UPLOAD_FILE="$(mktemp /tmp/smoke-upload-XXXXXX.txt)"

cleanup() {
  rm -f "$COOKIE_JAR"
  rm -f "$UPLOAD_FILE"
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
  sed -n "s/.*\"$field\"[[:space:]]*:[[:space:]]*\"\([^\"]*\)\".*/\1/p" /tmp/smoke_response.json | head -n 1
}

http_code() {
  local method="$1"
  local url="$2"
  local body="${3:-}"
  local token="${4:-}"
  local auth_args=()

  if [[ -n "$token" ]]; then
    auth_args=(-H "Authorization: Bearer $token")
  fi

  if [[ -n "$body" ]]; then
    curl -k -sS -o /tmp/smoke_response.json -w "%{http_code}" \
      -X "$method" "$url" \
      -H "Content-Type: application/json" \
      "${auth_args[@]}" \
      -c "$COOKIE_JAR" -b "$COOKIE_JAR" \
      -d "$body"
  else
    curl -k -sS -o /tmp/smoke_response.json -w "%{http_code}" \
      -X "$method" "$url" \
      "${auth_args[@]}" \
      -c "$COOKIE_JAR" -b "$COOKIE_JAR"
  fi
}

assert_status() {
  local actual="$1"
  local expected="$2"
  local step="$3"
  if [[ "$actual" != "$expected" ]]; then
    echo "[FAIL] $step -> expected HTTP $expected, got $actual"
    cat /tmp/smoke_response.json || true
    exit 1
  fi
  echo "[PASS] $step -> HTTP $actual"
}

echo "== Cloud Compute API Smoke Test =="
echo "Base URL: $BASE_URL"
echo "Smoke upload $(date)" > "$UPLOAD_FILE"

require_cmd curl
require_cmd sed

code="$(http_code GET "$BASE_URL/api/v1/compute/ping")"
assert_status "$code" "200" "Ping endpoint"

register_body="{\"username\":\"$USERNAME\",\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}"
code="$(http_code POST "$BASE_URL/api/v1/auth/register" "$register_body")"
assert_status "$code" "201" "Register user"

login_body="{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}"
code="$(http_code POST "$BASE_URL/api/v1/auth/login" "$login_body")"
assert_status "$code" "200" "Login user"

ACCESS_TOKEN="$(extract_json_field accessToken)"
REFRESH_TOKEN="$(extract_json_field refreshToken)"
if [[ -z "$ACCESS_TOKEN" || -z "$REFRESH_TOKEN" ]]; then
  echo "[FAIL] Missing access/refresh token in login response"
  cat /tmp/smoke_response.json || true
  exit 1
fi

code="$(http_code GET "$BASE_URL/api/v1/auth/me" "" "$ACCESS_TOKEN")"
assert_status "$code" "200" "Get current user"

task_body="{\"title\":\"$TASK_TITLE\",\"description\":\"Test\"}"
code="$(http_code POST "$BASE_URL/api/v1/tasks" "$task_body" "$ACCESS_TOKEN")"
assert_status "$code" "201" "Create task"

code="$(http_code GET "$BASE_URL/api/v1/tasks" "" "$ACCESS_TOKEN")"
assert_status "$code" "200" "List tasks"

refresh_payload="{\"refreshToken\":\"$REFRESH_TOKEN\"}"
code="$(http_code POST "$BASE_URL/api/v1/auth/refresh" "$refresh_payload")"
assert_status "$code" "200" "Refresh access token"

NEW_ACCESS_TOKEN="$(extract_json_field accessToken)"
NEW_REFRESH_TOKEN="$(extract_json_field refreshToken)"
if [[ -n "$NEW_ACCESS_TOKEN" ]]; then
  ACCESS_TOKEN="$NEW_ACCESS_TOKEN"
fi
if [[ -z "$NEW_REFRESH_TOKEN" || "$NEW_REFRESH_TOKEN" == "$REFRESH_TOKEN" ]]; then
  echo "[FAIL] Refresh token was not rotated"
  cat /tmp/smoke_response.json || true
  exit 1
fi

upload_status="$(curl -k -sS -o /tmp/smoke_response.json -w '%{http_code}' \
  -X POST "$BASE_URL/api/v1/files/upload" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -F "file=@$UPLOAD_FILE;filename=smoke-test.txt")"
assert_status "$upload_status" "200" "Upload file"

UPLOADED_FILE_NAME="$(sed -n 's/.*File uploaded successfully: \(.*\)/\1/p' /tmp/smoke_response.json | head -n 1)"
if [[ -z "$UPLOADED_FILE_NAME" ]]; then
  # Fallback for JSON style responses, e.g. {"filename":"uuid-smoke-test.txt"}
  UPLOADED_FILE_NAME="$(sed -n 's/.*"filename"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' /tmp/smoke_response.json | head -n 1)"
fi
if [[ -z "$UPLOADED_FILE_NAME" ]]; then
  echo "[FAIL] Could not parse uploaded file name"
  cat /tmp/smoke_response.json || true
  exit 1
fi

code="$(http_code GET "$BASE_URL/api/v1/files/list" "" "$ACCESS_TOKEN")"
assert_status "$code" "200" "List files"
if ! grep -q "$UPLOADED_FILE_NAME" /tmp/smoke_response.json; then
  echo "[FAIL] Uploaded file not present in list response"
  cat /tmp/smoke_response.json || true
  exit 1
fi

download_status="$(curl -k -sS -o /tmp/smoke_response_download.bin -w '%{http_code}' \
  -X GET "$BASE_URL/api/v1/files/download/$UPLOADED_FILE_NAME" \
  -H "Authorization: Bearer $ACCESS_TOKEN")"
assert_status "$download_status" "200" "Download file"

code="$(http_code POST "$BASE_URL/api/v1/auth/logout" "" "$ACCESS_TOKEN")"
assert_status "$code" "200" "Logout user"

code="$(http_code GET "$BASE_URL/api/v1/tasks")"
assert_status "$code" "401" "Tasks API unauthenticated"

code="$(http_code GET "$BASE_URL/api/v1/files/list")"
assert_status "$code" "401" "Files API unauthenticated"

echo -e "\n== All tests passed =="
