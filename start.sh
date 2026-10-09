#!/usr/bin/env bash
# Exit immediately if any command fails
set -e

echo "================================================================"
echo " Starting Penny Pals Local Environment"
echo "================================================================"

# =============================================================================
# Step 1: Check Required Tools
# =============================================================================
echo "=== [Step 1/4] Checking Required Tools ==="

if ! command -v java >/dev/null 2>&1; then
    echo "ERROR: Java is not installed."
    echo "Please install OpenJDK 21 or higher as listed in the README."
    exit 1
fi

JAVA_VER_STRING=$(java -version 2>&1 | head -n 1)
JAVA_MAJOR_VERSION=$(echo "$JAVA_VER_STRING" | awk -F '"' '{print $2}' | cut -d'.' -f1)

if [ "$JAVA_MAJOR_VERSION" -eq 1 ]; then
    JAVA_MAJOR_VERSION=$(echo "$JAVA_VER_STRING" | awk -F '"' '{print $2}' | cut -d'.' -f2)
fi

if [ -z "$JAVA_MAJOR_VERSION" ] || [ "$JAVA_MAJOR_VERSION" -lt 21 ]; then
    echo "ERROR: Java 21 or higher is required to run Penny Pals."
    echo "Found active Java version: ${JAVA_MAJOR_VERSION}"
    echo "Please install or select OpenJDK 21 in your environment."
    exit 1
fi

if [ ! -f "./mvnw" ]; then
    echo "ERROR: Maven wrapper (./mvnw) is missing from project root."
    exit 1
fi

echo "✓ Java 21+ and Maven wrapper verified."
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
echo " Penny Pals server is launching!"
echo " Open your browser to: http://localhost:8080"
echo " Press Ctrl+C to stop the application."
echo "================================================================"
echo ""

exec java -jar "$JAR_FILE"