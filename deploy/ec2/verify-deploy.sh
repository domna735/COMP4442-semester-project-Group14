#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${1:-}"

if [[ -z "$BASE_URL" ]]; then
  echo "Usage: $0 <base-url>"
  echo "Example: $0 http://13.250.10.20:8080"
  exit 1
fi

BASE_URL="${BASE_URL%/}"

USERNAME="verify_user_$(date +%s)"
EMAIL="${USERNAME}@example.com"
PASSWORD="VerifyPass123!"
TASK_TITLE="Deploy verify task $(date +%H%M%S)"
UPLOAD_FILE="$(mktemp /tmp/verify-upload-XXXXXX.txt)"

cleanup() {
  rm -f "$UPLOAD_FILE"
}
trap cleanup EXIT

extract_json_field() {
  local field="$1"
  sed -n 's/.*"'"$field"'"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' /tmp/verify_body.txt | head -n 1
}

check_endpoint() {
  local name="$1"
  local url="$2"
  local expected_status="$3"
  local method="${4:-GET}"
  local body="${5:-}"
  local token="${6:-}"

  local status
  local auth_header=()
  if [[ -n "$token" ]]; then
    auth_header=(-H "Authorization: Bearer $token")
  fi

  if [[ -n "$body" ]]; then
    status="$(curl -s -o /tmp/verify_body.txt -w "%{http_code}" \
      -X "$method" "$url" \
      -H "Content-Type: application/json" \
      "${auth_header[@]}" \
      -d "$body" || true)"
  else
    status="$(curl -s -o /tmp/verify_body.txt -w "%{http_code}" \
      -X "$method" "$url" \
      "${auth_header[@]}" || true)"
  fi

  if [[ "$status" != "$expected_status" ]]; then
    echo "[FAIL] $name -> $url (expected $expected_status, got $status)"
    if [[ -s /tmp/verify_body.txt ]]; then
      echo "       Response: $(head -c 200 /tmp/verify_body.txt)"
    fi
    return 1
  fi

  echo "[PASS] $name -> $url ($status)"
  return 0
}

failures=0

echo "Deploy verification upload $(date)" > "$UPLOAD_FILE"

check_endpoint "Ping API" "$BASE_URL/api/v1/compute/ping" "200" || failures=$((failures + 1))
check_endpoint "Tasks API unauthenticated" "$BASE_URL/api/v1/tasks" "401" || failures=$((failures + 1))
check_endpoint "Files API unauthenticated" "$BASE_URL/api/v1/files/list" "401" || failures=$((failures + 1))
check_endpoint "Swagger UI" "$BASE_URL/swagger-ui/index.html" "200" || failures=$((failures + 1))
check_endpoint "OpenAPI JSON" "$BASE_URL/v3/api-docs" "200" || failures=$((failures + 1))

register_payload=$(cat <<JSON
{"username":"$USERNAME","email":"$EMAIL","password":"$PASSWORD"}
JSON
)
check_endpoint "Register user" "$BASE_URL/api/v1/auth/register" "201" "POST" "$register_payload" || failures=$((failures + 1))

login_payload=$(cat <<JSON
{"username":"$USERNAME","password":"$PASSWORD"}
JSON
)
check_endpoint "Login user" "$BASE_URL/api/v1/auth/login" "200" "POST" "$login_payload" || failures=$((failures + 1))
ACCESS_TOKEN="$(extract_json_field accessToken)"
REFRESH_TOKEN="$(extract_json_field refreshToken)"
if [[ -z "$ACCESS_TOKEN" || -z "$REFRESH_TOKEN" ]]; then
  echo "[FAIL] Login response missing accessToken or refreshToken"
  failures=$((failures + 1))
fi

check_endpoint "Get current user" "$BASE_URL/api/v1/auth/me" "200" "GET" "" "$ACCESS_TOKEN" || failures=$((failures + 1))

create_task_payload=$(cat <<JSON
{"title":"$TASK_TITLE","description":"Created by deployment verification","status":"TODO"}
JSON
)
check_endpoint "Create task" "$BASE_URL/api/v1/tasks" "201" "POST" "$create_task_payload" "$ACCESS_TOKEN" || failures=$((failures + 1))
check_endpoint "List tasks" "$BASE_URL/api/v1/tasks" "200" "GET" "" "$ACCESS_TOKEN" || failures=$((failures + 1))

upload_status="$(curl -s -o /tmp/verify_body.txt -w "%{http_code}" \
  -X POST "$BASE_URL/api/v1/files/upload" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -F "file=@$UPLOAD_FILE;filename=deploy-verify.txt" || true)"
if [[ "$upload_status" != "200" ]]; then
  echo "[FAIL] Upload file -> $BASE_URL/api/v1/files/upload (expected 200, got $upload_status)"
  if [[ -s /tmp/verify_body.txt ]]; then
    echo "       Response: $(head -c 200 /tmp/verify_body.txt)"
  fi
  failures=$((failures + 1))
else
  echo "[PASS] Upload file -> $BASE_URL/api/v1/files/upload ($upload_status)"
fi

UPLOADED_FILE_NAME="$(sed -n 's/.*File uploaded successfully: \(.*\)/\1/p' /tmp/verify_body.txt | head -n 1)"
if [[ -z "$UPLOADED_FILE_NAME" ]]; then
  echo "[FAIL] Upload response missing stored filename"
  failures=$((failures + 1))
fi

check_endpoint "List files" "$BASE_URL/api/v1/files/list" "200" "GET" "" "$ACCESS_TOKEN" || failures=$((failures + 1))
if [[ -n "$UPLOADED_FILE_NAME" ]] && ! grep -q "$UPLOADED_FILE_NAME" /tmp/verify_body.txt; then
  echo "[FAIL] Uploaded file is not listed"
  failures=$((failures + 1))
fi

check_endpoint "Download file" "$BASE_URL/api/v1/files/download/$UPLOADED_FILE_NAME" "200" "GET" "" "$ACCESS_TOKEN" || failures=$((failures + 1))

refresh_payload=$(cat <<JSON
{"refreshToken":"$REFRESH_TOKEN"}
JSON
)
check_endpoint "Refresh token" "$BASE_URL/api/v1/auth/refresh" "200" "POST" "$refresh_payload" || failures=$((failures + 1))

NEW_ACCESS_TOKEN="$(extract_json_field accessToken)"
NEW_REFRESH_TOKEN="$(extract_json_field refreshToken)"
if [[ -n "$NEW_ACCESS_TOKEN" ]]; then
  ACCESS_TOKEN="$NEW_ACCESS_TOKEN"
fi
if [[ -z "$NEW_REFRESH_TOKEN" ]]; then
  echo "[FAIL] Refresh response missing rotated refresh token"
  failures=$((failures + 1))
fi
if [[ -n "$NEW_REFRESH_TOKEN" && "$NEW_REFRESH_TOKEN" == "$REFRESH_TOKEN" ]]; then
  echo "[FAIL] Refresh token was not rotated"
  failures=$((failures + 1))
fi

old_refresh_payload=$(cat <<JSON
{"refreshToken":"$REFRESH_TOKEN"}
JSON
)
old_refresh_status="$(curl -s -o /tmp/verify_body.txt -w "%{http_code}" \
  -X POST "$BASE_URL/api/v1/auth/refresh" \
  -H "Content-Type: application/json" \
  -d "$old_refresh_payload" || true)"
if [[ "$old_refresh_status" == "200" ]]; then
  echo "[FAIL] Old refresh token should fail after rotation"
  failures=$((failures + 1))
else
  echo "[PASS] Old refresh token rejected after rotation ($old_refresh_status)"
fi
REFRESH_TOKEN="$NEW_REFRESH_TOKEN"

check_endpoint "Logout user" "$BASE_URL/api/v1/auth/logout" "200" "POST" "" "$ACCESS_TOKEN" || failures=$((failures + 1))

if [[ "$failures" -gt 0 ]]; then
  echo "Deployment verification failed with $failures failing check(s)."
  exit 1
fi

echo "Deployment verification succeeded. All checks passed."
