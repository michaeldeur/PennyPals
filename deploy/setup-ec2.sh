#!/usr/bin/env bash
# Exit immediately if any command fails
set -e

echo "================================================================"
echo " Starting One-Time EC2 Server Setup for Penny Pals"
echo "================================================================"

echo "=== Step 1: Updating System Packages ==="
apt-get update -y && apt-get upgrade -y

echo "=== Step 2: Installing JDK 21, Nginx, rsync, & SQLite3 ==="
apt-get install -y openjdk-21-jdk nginx rsync sqlite3

echo "=== Step 3: Creating Application Directory & Data Storage ==="
mkdir -p /opt/pennypals/data
chown -R ubuntu:ubuntu /opt/pennypals

echo "=== Step 4: Writing & Enabling Nginx Reverse Proxy Configuration ==="
cat << 'EOF' > /etc/nginx/conf.d/pennypals.conf
server {
    listen 80 default_server;
    listen [::]:80 default_server;
    server_name _;
    server_tokens off;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 10s;
        proxy_read_timeout 30s;
    }
}
EOF

if [ -L /etc/nginx/sites-enabled/default ]; then
    rm -f /etc/nginx/sites-enabled/default
fi

nginx -t
systemctl enable nginx
systemctl restart nginx

echo "=== Step 5: Setting Up Systemd Auto-Boot Service ==="
cp deploy/pennypals.service /etc/systemd/system/pennypals.service
systemctl daemon-reload
systemctl enable pennypals.service

echo "================================================================"
echo " EC2 Setup Complete! Installed Java Version:"
java -version
echo "================================================================"