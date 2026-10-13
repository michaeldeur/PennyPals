#!/usr/bin/env bash
#
# Push this project to an EC2 instance that has already been prepared with
# deploy/setup-ec2.sh.
#
# Run it from the project root on your own machine (macOS or Linux):
#
#   ./deploy/deploy.sh -h 54.12.34.56 -i ~/keys/webdev.pem
#
# What it does:
#   1. Run local automated tests.
#   2. rsync the source code to /opt/pennypals on the server (target/ matches,
#      .git, and local database files are left behind).
#   3. Build the application on the server (or update dependencies) and restart systemd.
#   4. Verify the application health endpoint.
#
# Options:
#   -h HOST   public IP or DNS name of the instance    (required)
#   -i KEY    path to your .pem private key            (optional if ssh-agent has it)
#   -u USER   SSH login user (default: ubuntu; use 'ec2-user' for Amazon Linux)
#   -d DIR    remote directory (default: /opt/pennypals)

set -euo pipefail

REMOTE_HOST=""
SSH_KEY=""
REMOTE_USER="ubuntu"
REMOTE_DIR="/opt/pennypals"
SERVICE="pennypals"

# Print the comment block at the top of this file as the help text.
usage() {
    awk 'NR>1 { if (/^#/) { sub(/^# ?/, ""); print } else { exit } }' "$0"
    exit 1
}

while getopts ":h:i:u:d:" option; do
    case "$option" in
        h) REMOTE_HOST="$OPTARG" ;;
        i) SSH_KEY="$OPTARG" ;;
        u) REMOTE_USER="$OPTARG" ;;
        d) REMOTE_DIR="$OPTARG" ;;
        :) echo "ERROR: -$OPTARG needs a value" >&2; usage ;;
        \?) echo "ERROR: unknown option -$OPTARG" >&2; usage ;;
    esac
done

[ -n "$REMOTE_HOST" ] || { echo "ERROR: -h HOST is required" >&2; usage; }

# Run from the project root no matter where the script was called from.
PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_DIR"

[ -f pom.xml ] || { echo "ERROR: no pom.xml in $PROJECT_DIR" >&2; exit 1; }

# Build the ssh options once and reuse them for both ssh and rsync.
SSH_OPTS=(-o StrictHostKeyChecking=accept-new)
if [ -n "$SSH_KEY" ]; then
    [ -f "$SSH_KEY" ] || { echo "ERROR: key file not found: $SSH_KEY" >&2; exit 1; }
    SSH_OPTS+=(-i "$SSH_KEY")
fi

TARGET="${REMOTE_USER}@${REMOTE_HOST}"

echo "==> Running local tests before deploying"
./mvnw test

echo "==> Copying files to ${TARGET}:${REMOTE_DIR}"
# Ensure remote directory exists first
ssh "${SSH_OPTS[@]}" "$TARGET" "sudo mkdir -p ${REMOTE_DIR} && sudo chown -R ${REMOTE_USER}:${REMOTE_USER} ${REMOTE_DIR}"

rsync -az --delete \
    --exclude '.git' \
    --exclude 'target' \
    --exclude '.mvn' \
    --exclude '*.db' \
    --exclude '*.sqlite' \
    --exclude '.DS_Store' \
    --exclude '*.log' \
    -e "ssh ${SSH_OPTS[*]}" \
    ./ "${TARGET}:${REMOTE_DIR}/"

echo "==> Building application on server and restarting the service"
ssh "${SSH_OPTS[@]}" "$TARGET" "
    set -e
    cd '${REMOTE_DIR}'
    ./mvnw clean package -DskipTests
    sudo systemctl restart ${SERVICE}
    sleep 3
    sudo systemctl is-active --quiet ${SERVICE} && echo 'service is running'
"

echo "==> Checking application availability"
if curl -fsS --max-time 10 "http://${REMOTE_HOST}:8080/"; then
    printf '\n\n==> Deployed. Open http://%s:8080 in your browser.\n' "$REMOTE_HOST"
else
    printf '\n'
    echo "WARNING: the application did not respond on port 8080." >&2
    echo "Check the EC2 security group (port 8080 inbound) and the logs:" >&2
    echo "  ssh ${SSH_OPTS[*]} $TARGET 'journalctl -u ${SERVICE} -n 50'" >&2
    exit 1
fi