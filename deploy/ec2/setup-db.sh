#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="${1:-$SCRIPT_DIR/.env.prod}"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "[ERROR] Missing env file: $ENV_FILE"
  echo "Copy $SCRIPT_DIR/.env.prod.example to $SCRIPT_DIR/.env.prod and fill real values."
  exit 1
fi

set -a
# shellcheck disable=SC1090
source "$ENV_FILE"
set +a

require_var() {
  local name="$1"
  local value="${!name:-}"
  if [[ -z "$value" ]]; then
    echo "[ERROR] Missing required variable: $name"
    exit 1
  fi
}

require_var DB_URL
require_var DB_USERNAME
require_var DB_PASSWORD
require_var DB_DRIVER_CLASS_NAME

# Strip optional jdbc: prefix.
raw_url="${DB_URL#jdbc:}"

if [[ "$raw_url" =~ ^mysql://([^:/?#]+)(:([0-9]+))?/([^?]+) ]]; then
  DB_KIND="mysql"
  DB_HOST="${BASH_REMATCH[1]}"
  DB_PORT="${BASH_REMATCH[3]:-3306}"
  DB_NAME="${BASH_REMATCH[4]}"
elif [[ "$raw_url" =~ ^postgresql://([^:/?#]+)(:([0-9]+))?/([^?]+) ]]; then
  DB_KIND="postgresql"
  DB_HOST="${BASH_REMATCH[1]}"
  DB_PORT="${BASH_REMATCH[3]:-5432}"
  DB_NAME="${BASH_REMATCH[4]}"
elif [[ "$raw_url" =~ ^sqlite:(.+)$ ]]; then
  DB_KIND="sqlite"
  DB_PATH="${BASH_REMATCH[1]}"
else
  echo "[ERROR] Unsupported DB_URL format: $DB_URL"
  echo "Expected jdbc:mysql://..., jdbc:postgresql://..., or jdbc:sqlite:..."
  exit 1
fi

echo "== Database Setup Check =="
echo "Env file: $ENV_FILE"
echo "Driver: $DB_DRIVER_CLASS_NAME"
echo "Kind: $DB_KIND"

if [[ "$DB_KIND" == "sqlite" ]]; then
  sqlite_target="$DB_PATH"
  if [[ "$sqlite_target" != /* ]]; then
    sqlite_target="$PWD/$sqlite_target"
  fi

  sqlite_dir="$(dirname "$sqlite_target")"
  mkdir -p "$sqlite_dir"
  touch "$sqlite_target"

  if [[ ! -w "$sqlite_target" ]]; then
    echo "[ERROR] SQLite file is not writable: $sqlite_target"
    exit 1
  fi

  echo "[PASS] SQLite path ready: $sqlite_target"
  echo "[INFO] Hibernate will auto-create/update tables on app startup."
  exit 0
fi

if command -v nc >/dev/null 2>&1; then
  if nc -z -w 3 "$DB_HOST" "$DB_PORT"; then
    echo "[PASS] TCP reachable: $DB_HOST:$DB_PORT"
  else
    echo "[ERROR] Cannot reach DB host/port: $DB_HOST:$DB_PORT"
    echo "Check EC2 -> RDS security groups and subnet routing."
    exit 1
  fi
else
  echo "[WARN] nc not installed; skipping raw TCP check."
fi

if [[ "$DB_KIND" == "mysql" ]]; then
  if command -v mysql >/dev/null 2>&1; then
    MYSQL_PWD="$DB_PASSWORD" mysql \
      -h "$DB_HOST" \
      -P "$DB_PORT" \
      -u "$DB_USERNAME" \
      -D "$DB_NAME" \
      -e "SELECT 1;" >/dev/null
    echo "[PASS] MySQL credential check succeeded"
  else
    echo "[WARN] mysql client not installed; skipping SQL login test."
    echo "Install: sudo apt-get update && sudo apt-get install -y mysql-client"
  fi
elif [[ "$DB_KIND" == "postgresql" ]]; then
  if command -v psql >/dev/null 2>&1; then
    PGPASSWORD="$DB_PASSWORD" psql \
      "host=$DB_HOST port=$DB_PORT user=$DB_USERNAME dbname=$DB_NAME sslmode=prefer" \
      -c "SELECT 1;" >/dev/null
    echo "[PASS] PostgreSQL credential check succeeded"
  else
    echo "[WARN] psql client not installed; skipping SQL login test."
    echo "Install: sudo apt-get update && sudo apt-get install -y postgresql-client"
  fi
fi

echo "[PASS] Database setup checks completed."
echo "[NEXT] Start app with: ./deploy/ec2/run-prod.sh"
