#!/usr/bin/env bash
#
# Start the Penny Pals web application on your local machine.
# Automatically checks for Java 21 and installs it if missing.
#
#   ./start.sh              start the app on http://localhost:8080
#   ./start.sh --port 9000  listen somewhere other than port 8080
#   ./start.sh --help       show this message
#
# Works on macOS and Linux.

set -euo pipefail

REQUIRED_JAVA_VERSION=21
PORT_OVERRIDE=""

# --- helpers ----------------------------------------------------------------

say() {
    printf '==> %s\n' "$1"
}

die() {
    printf 'ERROR: %s\n' "$1" >&2
    exit 1
}

usage() {
    awk 'NR>1 { if (/^#/) { sub(/^# ?/, ""); print } else { exit } }' "$0"
    exit 0
}

# --- options ----------------------------------------------------------------

while [ $# -gt 0 ]; do
    case "$1" in
        -p|--port)
            [ $# -ge 2 ] || die "--port needs a number, for example: --port 9000"
            case "$2" in
                ''|*[!0-9]*) die "--port needs a number, got '$2'" ;;
            esac
            PORT_OVERRIDE="$2"
            shift 2
            ;;
        -h|--help)
            usage
            ;;
        *)
            die "unknown option '$1' (try --help)"
            ;;
    esac
done

# Run from the project root no matter where called from.
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$PROJECT_DIR"

# --- dependency checks & auto-install ---------------------------------------

if ! command -v java >/dev/null 2>&1; then
    say "Java is not installed. Attempting automatic installation..."
    if [[ "$OSTYPE" == "darwin"* ]]; then
        if command -v brew >/dev/null 2>&1; then
            brew install openjdk@21
            sudo ln -sfn /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-21.jdk
        else
            die "Homebrew is required to auto-install Java on macOS. Please install Homebrew or install Java 21 manually."
        fi
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        sudo apt-get update && sudo apt-get install -y openjdk-21-jdk
    else
        die "Unsupported operating system. Please install OpenJDK 21 manually."
    fi
fi

# Verify Java version major number
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" = "1" ]; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f2)
fi

if [ "$JAVA_VERSION" -lt "$REQUIRED_JAVA_VERSION" ] 2>/dev/null; then
    die "Java version is too old ($JAVA_VERSION). Penny Pals requires Java ${REQUIRED_JAVA_VERSION} or newer."
fi

[ -f pom.xml ] || die "no pom.xml here. Run this script from inside the project root folder."

# --- build & start ----------------------------------------------------------

say "Building Penny Pals with Maven..."
./mvnw clean package -DskipTests

SERVER_PORT="${PORT_OVERRIDE:-8080}"
say "Starting on http://localhost:${SERVER_PORT} -- press Ctrl+C to stop"

SPRING_ARGS=""
if [ -n "$PORT_OVERRIDE" ]; then
    SPRING_ARGS="--server.port=${PORT_OVERRIDE}"
fi

exec java -jar target/pennypals-0.0.1-SNAPSHOT.jar $SPRING_ARGS