#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"

if [[ ! -f "$SCRIPT_DIR/.env.prod" ]]; then
  echo "Missing $SCRIPT_DIR/.env.prod"
  echo "Copy $SCRIPT_DIR/.env.prod.example to $SCRIPT_DIR/.env.prod and fill values."
  exit 1
fi

set -a
source "$SCRIPT_DIR/.env.prod"
set +a

require_var() {
  local name="$1"
  local value="${!name:-}"
  if [[ -z "$value" ]]; then
    echo "Missing required environment variable: $name"
    exit 1
  fi
}

require_var DB_URL
require_var DB_USERNAME
require_var DB_PASSWORD
require_var DB_DRIVER_CLASS_NAME
require_var JWT_PRIVATE_KEY_PATH
require_var JWT_PUBLIC_KEY_PATH

echo "Using profile: ${SPRING_PROFILES_ACTIVE:-prod}"
echo "Using DB URL: $DB_URL"

JAR_PATH="$ROOT_DIR/target/cloud-compute-service-0.0.1-SNAPSHOT.jar"
if [[ ! -f "$JAR_PATH" ]]; then
  echo "Missing $JAR_PATH"
  echo "Build first: mvn clean package -DskipTests"
  exit 1
fi

exec java -jar "$JAR_PATH" --spring.profiles.active="${SPRING_PROFILES_ACTIVE:-prod}"
