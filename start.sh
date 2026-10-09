#!/usr/bin/env bash
# Exit immediately if any command fails
set -e

echo "================================================================"
echo " Starting Penny Pals Environment"
echo "================================================================"

# Step 1: Check & Auto-Install Java 21 & Maven
echo "=== [Step 1/4] Checking & Preparing Build Tools ==="

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

# 1A. Ensure Java 21 is installed
JAVA_VER=$(get_java_version)
if [ "$JAVA_VER" -lt 21 ]; then
    echo "--> Java 21+ not detected (found version ${JAVA_VER}). Installing OpenJDK 21..."
    if command -v apt-get >/dev/null 2>&1; then
        SUDO_CMD=""
        [ "$EUID" -ne 0 ] && SUDO_CMD="sudo"
        $SUDO_CMD apt-get update -qq
        $SUDO_CMD apt-get install -y openjdk-21-jdk
    else
        echo "ERROR: Java 21 is required but could not be automatically installed."
        exit 1
    fi
fi

# 1B. Ensure Maven or Maven Wrapper is available
BUILD_CMD=""
if [ -f "./mvnw" ]; then
    chmod +x ./mvnw
    BUILD_CMD="./mvnw"
elif command -v mvn >/dev/null 2>&1; then
    BUILD_CMD="mvn"
else
    echo "--> Maven wrapper not found. Installing system Maven..."
    if command -v apt-get >/dev/null 2>&1; then
        SUDO_CMD=""
        [ "$EUID" -ne 0 ] && SUDO_CMD="sudo"
        $SUDO_CMD apt-get install -y maven
        BUILD_CMD="mvn"
    else
        echo "ERROR: Neither ./mvnw nor system 'mvn' command is available."
        exit 1
    fi
fi

echo "Build tools verified."
echo ""

# Step 2: Install Project Dependencies & Build JAR
echo "=== [Step 2/4] Compiling Application Dependencies ==="

$BUILD_CMD clean package -DskipTests

echo "Application built successfully."
echo ""

# =============================================================================
# Step 3: Initialize Database & Data Directory
# =============================================================================
echo "=== [Step 3/4] Initializing Local Storage ==="

mkdir -p ./data

echo "Local storage directories initialized."
echo ""

# =============================================================================
# Step 4: Launch Penny Pals Application
# =============================================================================
echo "=== [Step 4/4] Launching Penny Pals Application ==="

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