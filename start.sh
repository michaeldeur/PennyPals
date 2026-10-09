#!/usr/bin/env bash
# Exit immediately if any command fails
set -e

echo "================================================================"
echo " Starting Penny Pals Environment"
echo "================================================================"

# =============================================================================
# Step 1: Check & Auto-Install Required Tools (Java 21)
# =============================================================================
echo "=== [Step 1/4] Checking & Preparing Java 21 Environment ==="

# Helper function to get major Java version
get_java_version() {
    if command -v java >/dev/null 2>&1; then
        local VER_STR
        VER_STR=$(java -version 2>&1 | head -n 1)
        local MAJOR_VER
        MAJOR_VER=$(echo "$VER_STR" | awk -F '"' '{print $2}' | cut -d'.' -f1)
        if [ "$MAJOR_VER" -eq 1 ]; then
            MAJOR_VER=$(echo "$VER_STR" | awk -F '"' '{print $2}' | cut -d'.' -f2)
        fi
        echo "$MAJOR_VER"
    else
        echo "0"
    fi
}

JAVA_VER=$(get_java_version)

# Install Java 21 if missing or lower version
if [ "$JAVA_VER" -lt 21 ]; then
    echo "--> Active Java version is ${JAVA_VER} (Java 21+ required)."

    # Check if apt-get is available (Ubuntu / Debian)
    if command -v apt-get >/dev/null 2>&1; then
        echo "--> Attempting automatic installation of OpenJDK 21 via apt-get..."
        if [ "$EUID" -ne 0 ]; then
            SUDO_CMD="sudo"
        else
            SUDO_CMD=""
        fi

        $SUDO_CMD apt-get update -qq
        $SUDO_CMD apt-get install -y openjdk-21-jdk
    else
        echo "ERROR: Java 21 is required, but could not be auto-installed."
        echo "Please install OpenJDK 21 or higher for your OS and re-run ./start.sh."
        exit 1
    fi
fi

# Re-check Java version after installation attempt
FINAL_JAVA_VER=$(get_java_version)
if [ "$FINAL_JAVA_VER" -lt 21 ]; then
    echo "ERROR: Java 21 installation verified failed. Detected version: ${FINAL_JAVA_VER}."
    exit 1
fi

if [ ! -f "./mvnw" ]; then
    echo "ERROR: Maven wrapper (./mvnw) is missing from the repository root."
    exit 1
fi

echo "✓ OpenJDK 21 and Maven wrapper verified."
echo ""

# =============================================================================
# Step 2: Install Project Dependencies & Build
# =============================================================================
echo "=== [Step 2/4] Installing Dependencies & Compiling Application ==="

chmod +x ./mvnw
./mvnw clean package -DskipTests

echo "✓ Dependencies resolved and package built successfully."
echo ""

# =============================================================================
# Step 3: Initialize Database & Workspace Directories
# =============================================================================
echo "=== [Step 3/4] Initializing Storage Directories ==="

mkdir -p ./data

echo "✓ Local storage directory ready."
echo ""

# =============================================================================
# Step 4: Start Application & Print URL
# =============================================================================
echo "=== [Step 4/4] Starting Penny Pals Application ==="

JAR_FILE=$(find target -name "*.jar" ! -name "*-plain.jar" | head -n 1)

if [ -z "$JAR_FILE" ]; then
    echo "ERROR: Executable JAR file not found in target/ directory."
    exit 1
fi

echo ""
echo "================================================================"
echo " Penny Pals server is starting up!"
echo " Open your browser to: http://localhost:8080"
echo " Press Ctrl+C to stop the application."
echo "================================================================"
echo ""

exec java -jar "$JAR_FILE"