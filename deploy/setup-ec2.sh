#!/usr/bin/env bash
#
# One-time setup for a fresh EC2 instance for Penny Pals.
#
# Copy this whole deploy/ folder to the server and run it once as root:
#
#   scp -i mykey.pem -r deploy ubuntu@<PUBLIC-IP>:~
#   ssh -i mykey.pem ubuntu@<PUBLIC-IP>
#   sudo bash deploy/setup-ec2.sh
#
# Works on Amazon Linux 2023 and on Ubuntu 22.04/24.04.

set -euo pipefail

APP_NAME="pennypals"
APP_DIR="/opt/pennypals"
APP_USER="pennypals"
JAVA_VERSION="21"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
DEPLOY_USER="${SUDO_USER:-root}"

# --- helpers ----------------------------------------------------------------

say() {
    printf '\n==> %s\n' "$1"
}

die() {
    printf 'ERROR: %s\n' "$1" >&2
    exit 1
}

# --- checks -----------------------------------------------------------------

[ "$(id -u)" -eq 0 ] || die "run this with sudo: sudo bash $0"

[ -r /etc/os-release ] || die "cannot read /etc/os-release; unsupported system"
. /etc/os-release

case "${ID:-}" in
    amzn|rhel|centos|fedora)
        PKG="dnf"
        ;;
    ubuntu|debian)
        PKG="apt"
        export DEBIAN_FRONTEND=noninteractive
        ;;
    *)
        die "unsupported distribution '${ID:-unknown}'; expected Amazon Linux or Ubuntu"
        ;;
esac

say "Detected ${PRETTY_NAME:-$ID} (package manager: $PKG)"

# --- packages ---------------------------------------------------------------

say "Installing OpenJDK ${JAVA_VERSION}, Maven, nginx, and rsync"

if [ "$PKG" = "dnf" ]; then
    dnf -y update
    dnf -y install java-${JAVA_VERSION}-openjdk-devel maven nginx rsync
else
    apt-get update -y
    apt-get install -y ca-certificates curl gnupg rsync openjdk-21-jdk maven nginx
fi

say "Java version: $(java -version 2>&1 | head -n 1)"

# --- service account and directories ----------------------------------------

say "Creating the '$APP_USER' service account"

if ! id "$APP_USER" >/dev/null 2>&1; then
    useradd --system --no-create-home --shell /usr/sbin/nologin "$APP_USER"
else
    echo "user '$APP_USER' already exists, leaving it alone"
fi

say "Creating $APP_DIR"

mkdir -p "$APP_DIR/data"

chown -R "$DEPLOY_USER:$APP_USER" "$APP_DIR"
chown -R "$APP_USER:$APP_USER" "$APP_DIR/data"
chmod 750 "$APP_DIR"
chmod 770 "$APP_DIR/data"

# --- systemd ----------------------------------------------------------------

say "Installing the $APP_NAME systemd service"

install -m 644 "$SCRIPT_DIR/pennypals.service" "/etc/systemd/system/${APP_NAME}.service"
systemctl daemon-reload
systemctl enable "$APP_NAME"

# --- nginx ------------------------------------------------------------------

say "Configuring nginx as a reverse proxy on port 80"

install -m 644 "$SCRIPT_DIR/pennypals-nginx.conf" "/etc/nginx/conf.d/${APP_NAME}.conf"

if [ -L /etc/nginx/sites-enabled/default ]; then
    rm -f /etc/nginx/sites-enabled/default
fi

if command -v getenforce >/dev/null 2>&1 && [ "$(getenforce)" != "Disabled" ]; then
    setsebool -P httpd_can_network_connect 1 || true
fi

nginx -t
systemctl enable nginx
systemctl restart nginx

# --- firewall ---------------------------------------------------------------

if command -v ufw >/dev/null 2>&1 && ufw status | grep -q "Status: active"; then
    say "Opening ports 80 and 22 in ufw"
    ufw allow 80/tcp
    ufw allow 22/tcp
fi

# --- done -------------------------------------------------------------------

cat <<EOF

==> Server setup is complete.

The app is registered but not running yet, because no code has been deployed.
From your laptop, in the project folder, run:

    ./deploy/deploy.sh -h <PUBLIC-IP-OR-DNS> -i ~/path/to/key.pem

Then open http://<PUBLIC-IP> in a browser.

EOF