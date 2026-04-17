#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"

BASE_URL="${1:-}"
OUTPUT_DIR="${2:-$ROOT_DIR/evidence/ec2}"

if [[ -z "$BASE_URL" ]]; then
  echo "Usage: $0 <ec2-base-url> [output-dir]"
  echo "Example: $0 http://13.250.10.20:8080"
  exit 1
fi

BASE_URL="${BASE_URL%/}"
TIMESTAMP="$(date +%Y%m%d-%H%M%S)"
SAFE_HOST="$(echo "$BASE_URL" | sed 's#https\?://##; s#[/:]#_#g')"
RUN_DIR="$OUTPUT_DIR/${TIMESTAMP}_${SAFE_HOST}"
VERIFY_LOG="$RUN_DIR/verify-deploy.log"
META_FILE="$RUN_DIR/metadata.txt"

mkdir -p "$RUN_DIR"

echo "== Live EC2 Verification Archive =="
echo "Base URL: $BASE_URL"
echo "Output: $RUN_DIR"

{
  echo "timestamp=$TIMESTAMP"
  echo "base_url=$BASE_URL"
  echo "host=$(hostname || true)"
  echo "user=$(whoami || true)"
  echo "git_commit=$(git -C "$ROOT_DIR" rev-parse --short HEAD 2>/dev/null || echo unknown)"
} > "$META_FILE"

set +e
"$SCRIPT_DIR/verify-deploy.sh" "$BASE_URL" | tee "$VERIFY_LOG"
VERIFY_EXIT=${PIPESTATUS[0]}
set -e

if [[ "$VERIFY_EXIT" -ne 0 ]]; then
  echo "verify_exit_code=$VERIFY_EXIT" >> "$META_FILE"
  echo
  echo "[ERROR] Verification failed. Logs archived at: $VERIFY_LOG"
  exit "$VERIFY_EXIT"
fi

echo "verify_exit_code=0" >> "$META_FILE"

echo
# Optional screenshots: works if a Chromium-compatible browser exists.
CAPTURED_SCREENSHOTS=0
SCREENSHOT_TOOL=""
if command -v chromium >/dev/null 2>&1; then
  SCREENSHOT_TOOL="chromium"
elif command -v chromium-browser >/dev/null 2>&1; then
  SCREENSHOT_TOOL="chromium-browser"
elif command -v google-chrome >/dev/null 2>&1; then
  SCREENSHOT_TOOL="google-chrome"
fi

if [[ -n "$SCREENSHOT_TOOL" ]]; then
  "$SCREENSHOT_TOOL" --headless --disable-gpu --hide-scrollbars \
    --screenshot="$RUN_DIR/screenshot-swagger-ui.png" \
    "$BASE_URL/swagger-ui/index.html" >/dev/null 2>&1 || true

  "$SCREENSHOT_TOOL" --headless --disable-gpu --hide-scrollbars \
    --screenshot="$RUN_DIR/screenshot-home.png" \
    "$BASE_URL/" >/dev/null 2>&1 || true

  if [[ -f "$RUN_DIR/screenshot-swagger-ui.png" || -f "$RUN_DIR/screenshot-home.png" ]]; then
    CAPTURED_SCREENSHOTS=1
  fi
fi

echo "screenshots_auto_captured=$CAPTURED_SCREENSHOTS" >> "$META_FILE"

echo
if [[ "$CAPTURED_SCREENSHOTS" -eq 1 ]]; then
  echo "[PASS] Verification succeeded. Evidence archived with screenshots: $RUN_DIR"
else
  echo "[PASS] Verification succeeded. Evidence archived: $RUN_DIR"
  echo "[INFO] No headless browser found, so screenshots were not auto-captured."
  echo "[INFO] Take at least one manual screenshot of verifier output and save it into: $RUN_DIR"
fi
