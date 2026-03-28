#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${1:-}"

if [[ -z "$BASE_URL" ]]; then
  echo "Usage: $0 <base-url>"
  echo "Example: $0 http://13.250.10.20:8080"
  exit 1
fi

BASE_URL="${BASE_URL%/}"

check_endpoint() {
  local name="$1"
  local url="$2"
  local expected_status="$3"

  local status
  status="$(curl -s -o /tmp/verify_body.txt -w "%{http_code}" "$url" || true)"

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

check_endpoint "Ping API" "$BASE_URL/api/v1/compute/ping" "200" || failures=$((failures + 1))
check_endpoint "Tasks API" "$BASE_URL/api/v1/tasks" "200" || failures=$((failures + 1))
check_endpoint "Swagger UI" "$BASE_URL/swagger-ui/index.html" "200" || failures=$((failures + 1))
check_endpoint "OpenAPI JSON" "$BASE_URL/v3/api-docs" "200" || failures=$((failures + 1))

if [[ "$failures" -gt 0 ]]; then
  echo "Deployment verification failed with $failures failing check(s)."
  exit 1
fi

echo "Deployment verification succeeded. All checks passed."
