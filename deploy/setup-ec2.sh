#!/usr/bin/env bash
set -e

echo "================================================================"
echo " Starting One-Time EC2 Server Setup for Penny Pals"
echo "================================================================"

echo "=== Step 1: Updating System Packages ==="
apt-get update -y && apt-get upgrade -y

echo "=== Step 2: Installing JDK 21, Nginx, & System Utilities ==="
apt-get install -y openjdk-21-jdk nginx rsync sqlite3

echo "=== Step 3: Creating Application Directory ==="
mkdir -p /var/www/pennypals
chown -R ubuntu:ubuntu /var/www/pennypals

echo "=== Step 4: Writing & Enabling Nginx Reverse Proxy Configuration ==="
cat << 'EOF' > /etc/nginx/sites-available/pennypals
server {
    listen 80;
    server_name _;

    location / {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
EOF

rm -f /etc/nginx/sites-enabled/default
ln -sf /etc/nginx/sites-available/pennypals /etc/nginx/sites-enabled/pennypals
nginx -t
systemctl restart nginx

echo "=== Step 5: Setting Up Systemd Auto-Boot Service ==="
cp deploy/pennypals.service /etc/systemd/system/pennypals.service
systemctl daemon-reload
systemctl enable pennypals.service

echo "================================================================"
echo " EC2 Setup Complete! Installed Java Version:"
java -version
echo "================================================================"