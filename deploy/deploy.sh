#!/usr/bin/env bash
# Exit immediately if any command fails
set -e

# Parse command line flags -h <PUBLIC-IP> -i <KEY-PATH>
while getopts "h:i:" opt; do
  case ${opt} in
    h ) HOST=$OPTARG ;;
    i ) KEY=$OPTARG ;;
    \? ) echo "Usage: $0 -h <PUBLIC-IP> -i <KEY-PATH>"; exit 1 ;;
  esac
done

if [ -z "$HOST" ] || [ -z "$KEY" ]; then
    echo "Usage: $0 -h <PUBLIC-IP> -i <KEY-PATH>"
    exit 1
fi

echo "=== Building Production Executable Jar Locally ==="
./mvnw clean package -DskipTests -q

echo "=== Syncing Code to EC2 ($HOST) via rsync ==="
ssh -i "$KEY" ubuntu@$HOST "mkdir -p /opt/pennypals/target"
rsync -avz -e "ssh -i $KEY" target/pennypals-0.0.1-SNAPSHOT.jar ubuntu@$HOST:/opt/pennypals/target/

echo "=== Restarting Remote Penny Pals Service ==="
ssh -i "$KEY" ubuntu@$HOST "sudo systemctl restart pennypals.service"

echo "=== Performing Health Check ==="
sleep 5
HEALTH_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "http://$HOST/")

if [ "$HEALTH_STATUS" -eq 200 ]; then
    echo "Deployment successful! Site is live at http://$HOST/"
else
    echo "Warning: Health check returned HTTP $HEALTH_STATUS. Check server logs."
fi