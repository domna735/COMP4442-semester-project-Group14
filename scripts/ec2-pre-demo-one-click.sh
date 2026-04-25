#!/usr/bin/env bash
set -euo pipefail

KEY_PATH_DEFAULT="/home/domna/COMP4442 Semester Project Group 14.pem"
HOST_DEFAULT="ubuntu@13.236.152.79"
BASE_URL_DEFAULT="http://13.236.152.79:8080"
INSTANCE_ID_DEFAULT="i-0f2d54704d5c42a6a"
REGION_DEFAULT="ap-southeast-2"
REMOTE_REPO_DEFAULT="~/COMP4442-semester-project-Group14"

KEY_PATH="${KEY_PATH:-$KEY_PATH_DEFAULT}"
HOST="${HOST:-$HOST_DEFAULT}"
BASE_URL="${BASE_URL:-$BASE_URL_DEFAULT}"
INSTANCE_ID="${INSTANCE_ID:-$INSTANCE_ID_DEFAULT}"
REGION="${REGION:-$REGION_DEFAULT}"
REMOTE_REPO="${REMOTE_REPO:-$REMOTE_REPO_DEFAULT}"

AUTO_START=false
RUN_VERIFY=false
OPEN_TUNNEL=false
STRICT_HOST_KEY=false
WAIT_SECONDS=300
RESOLVE_FROM_AWS=false
PREFER_DNS=false

usage() {
  cat <<EOF
Usage: $(basename "$0") [options]

One-click EC2 pre-demo warm-up:
- optional AWS EC2 start
- wait for SSH
- build + start app on EC2
- check LOCAL_PING and PUBLIC_PING
- optional deploy verify
- optional localhost tunnel fallback

Options:
  --auto-start           Start EC2 instance via AWS CLI first (if installed/configured)
  --resolve-from-aws     Resolve current public DNS/IP from AWS for this instance
  --prefer-dns           When resolving from AWS, use public DNS for SSH host
  --run-verify           Run deploy/ec2/verify-deploy.sh against BASE_URL
  --open-tunnel          Open SSH tunnel localhost:8080 -> EC2:localhost:8080 at end
  --key PATH             SSH private key path (default: $KEY_PATH_DEFAULT)
  --host USER@IP         SSH target host (default: $HOST_DEFAULT)
  --base-url URL         Public base URL (default: $BASE_URL_DEFAULT)
  --instance-id ID       EC2 instance ID for --auto-start (default: $INSTANCE_ID_DEFAULT)
  --region REGION        AWS region for --auto-start (default: $REGION_DEFAULT)
  --remote-repo PATH     Remote repository path (default: $REMOTE_REPO_DEFAULT)
  --wait-seconds N       Max wait for SSH in seconds (default: 300)
  --strict-host-key      Enable strict host key checking (default: off)
  -h, --help             Show this help
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --auto-start)
      AUTO_START=true
      ;;
    --run-verify)
      RUN_VERIFY=true
      ;;
    --resolve-from-aws)
      RESOLVE_FROM_AWS=true
      ;;
    --prefer-dns)
      PREFER_DNS=true
      ;;
    --open-tunnel)
      OPEN_TUNNEL=true
      ;;
    --key)
      KEY_PATH="$2"
      shift
      ;;
    --host)
      HOST="$2"
      shift
      ;;
    --base-url)
      BASE_URL="$2"
      shift
      ;;
    --instance-id)
      INSTANCE_ID="$2"
      shift
      ;;
    --region)
      REGION="$2"
      shift
      ;;
    --remote-repo)
      REMOTE_REPO="$2"
      shift
      ;;
    --wait-seconds)
      WAIT_SECONDS="$2"
      shift
      ;;
    --strict-host-key)
      STRICT_HOST_KEY=true
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      usage
      exit 1
      ;;
  esac
  shift
done

if [[ ! -f "$KEY_PATH" ]]; then
  echo "[ERROR] SSH key not found: $KEY_PATH" >&2
  exit 1
fi

SSH_COMMON=(-i "$KEY_PATH" -o ConnectTimeout=5)
if [[ "$STRICT_HOST_KEY" == false ]]; then
  SSH_COMMON+=(-o StrictHostKeyChecking=no)
fi

resolve_current_endpoint() {
  if ! command -v aws >/dev/null 2>&1; then
    echo "[ERROR] AWS CLI not found but --resolve-from-aws was provided." >&2
    exit 1
  fi

  local aws_out public_ip public_dns state
  aws_out=$(aws ec2 describe-instances \
    --instance-ids "$INSTANCE_ID" \
    --region "$REGION" \
    --query 'Reservations[0].Instances[0].[PublicIpAddress,PublicDnsName,State.Name]' \
    --output text)

  public_ip=$(awk '{print $1}' <<<"$aws_out")
  public_dns=$(awk '{print $2}' <<<"$aws_out")
  state=$(awk '{print $3}' <<<"$aws_out")

  if [[ -z "$state" || "$state" == "None" ]]; then
    echo "[ERROR] Unable to query instance state from AWS for $INSTANCE_ID" >&2
    exit 1
  fi

  echo "[INFO] AWS instance state: $state"

  if [[ -z "$public_ip" || "$public_ip" == "None" ]]; then
    echo "[WARN] Public IP not ready yet. Continuing with current HOST/BASE_URL values."
    return
  fi

  if [[ "$PREFER_DNS" == true && -n "$public_dns" && "$public_dns" != "None" ]]; then
    HOST="ubuntu@$public_dns"
  else
    HOST="ubuntu@$public_ip"
  fi

  BASE_URL="http://$public_ip:8080"
  echo "[INFO] Resolved HOST=$HOST"
  echo "[INFO] Resolved BASE_URL=$BASE_URL"
}

if [[ "$AUTO_START" == true ]]; then
  if ! command -v aws >/dev/null 2>&1; then
    echo "[ERROR] AWS CLI not found but --auto-start was provided." >&2
    exit 1
  fi
  echo "[INFO] Starting EC2 instance: $INSTANCE_ID ($REGION)"
  aws ec2 start-instances --instance-ids "$INSTANCE_ID" --region "$REGION" >/dev/null
fi

if [[ "$RESOLVE_FROM_AWS" == true ]]; then
  resolve_current_endpoint
fi

echo "[INFO] Using HOST=$HOST"
echo "[INFO] Using BASE_URL=$BASE_URL"

echo "[INFO] Waiting for SSH on $HOST (timeout: ${WAIT_SECONDS}s)"
deadline=$(( $(date +%s) + WAIT_SECONDS ))
while true; do
  if ssh "${SSH_COMMON[@]}" "$HOST" "echo SSH_OK" >/dev/null 2>&1; then
    echo "[PASS] SSH reachable"
    break
  fi

  now=$(date +%s)
  if (( now >= deadline )); then
    echo "[ERROR] SSH not reachable within ${WAIT_SECONDS}s" >&2
    exit 1
  fi

  echo "[INFO] Waiting for EC2 SSH..."
  sleep 10
done

echo "[INFO] Building artifact on EC2"
ssh "${SSH_COMMON[@]}" "$HOST" "cd $REMOTE_REPO && mvn -q -DskipTests clean package"

echo "[INFO] Restarting app on EC2"
ssh "${SSH_COMMON[@]}" "$HOST" "pkill -f cloud-compute-service-0.0.1-SNAPSHOT.jar || true; cd $REMOTE_REPO && nohup ./deploy/ec2/run-prod.sh > ~/cloud-compute-prod.log 2>&1 &"

echo "[INFO] Waiting for app readiness"
for _ in {1..30}; do
  local_ping=$(ssh "${SSH_COMMON[@]}" "$HOST" "curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/api/v1/compute/ping" || true)
  public_ping=$(curl -s -o /dev/null -w '%{http_code}' "$BASE_URL/api/v1/compute/ping" || true)
  echo "[INFO] LOCAL_PING=$local_ping PUBLIC_PING=$public_ping"
  if [[ "$local_ping" == "200" && "$public_ping" == "200" ]]; then
    echo "[PASS] Cloud pre-demo health checks passed"
    break
  fi
  sleep 5
done

local_ping=$(ssh "${SSH_COMMON[@]}" "$HOST" "curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/api/v1/compute/ping" || true)
public_ping=$(curl -s -o /dev/null -w '%{http_code}' "$BASE_URL/api/v1/compute/ping" || true)

echo "LOCAL_PING=$local_ping"
echo "PUBLIC_PING=$public_ping"

if [[ "$local_ping" != "200" ]]; then
  echo "[ERROR] LOCAL_PING is not 200. Check remote log:" >&2
  echo "ssh -i \"$KEY_PATH\" $HOST \"tail -n 80 ~/cloud-compute-prod.log\"" >&2
  exit 1
fi

if [[ "$public_ping" != "200" ]]; then
  echo "[WARN] PUBLIC_PING is not 200. Check security group / network."
  echo "[INFO] You can still demo with tunnel using --open-tunnel"
fi

if [[ "$RUN_VERIFY" == true ]]; then
  echo "[INFO] Running deploy verifier"
  "$(dirname "$0")/../deploy/ec2/verify-deploy.sh" "$BASE_URL"
fi

if [[ "$OPEN_TUNNEL" == true ]]; then
  echo "[INFO] Opening localhost tunnel at http://localhost:8080"
  echo "[INFO] Keep this terminal open during fallback demo."
  exec ssh "${SSH_COMMON[@]}" -L 8080:localhost:8080 "$HOST"
fi

echo "[DONE] Pre-demo one-click flow completed."
