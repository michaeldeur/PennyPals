#!/usr/bin/env bash
# Exit immediately if any command fails
set -e

echo "================================================================"
echo " Starting Penny Pals Environment"
echo "================================================================"

# Step 1: Check required files and build tools
echo "=== [Step 1/4] Checking Required Files & Build Tools ==="

# Verify that pom.xml exists in the current directory
if [ ! -f "pom.xml" ]; then
    echo "ERROR: pom.xml is missing from the repository root."
    echo "Please ensure you run ./start.sh from the root directory of the PennyPals project."
    exit 1
fi

# Function to extract active major Java version
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

# Auto-install OpenJDK 21 if active Java version is less than 21
JAVA_VER=$(get_java_version)
if [ "$JAVA_VER" -lt 21 ]; then
    echo "Active Java version is ${JAVA_VER} (Java 21+ required)."
    if command -v apt-get >/dev/null 2>&1; then
        echo "Auto-installing OpenJDK 21 via apt-get..."
        SUDO_CMD=""
        [ "$EUID" -ne 0 ] && SUDO_CMD="sudo"
        $SUDO_CMD apt-get update -qq
        $SUDO_CMD apt-get install -y openjdk-21-jdk
    else
        echo "ERROR: Java 21 is required but could not be automatically installed."
        echo "Please install OpenJDK 21 or higher and re-run ./start.sh."
        exit 1
    fi
fi

# Select Maven wrapper or system Maven, auto-installing Maven if both are missing
BUILD_CMD=""
if [ -f "./mvnw" ]; then
    chmod +x ./mvnw
    BUILD_CMD="./mvnw"
elif command -v mvn >/dev/null 2>&1; then
    BUILD_CMD="mvn"
else
    echo "Maven wrapper not found. Auto-installing Maven..."
    if command -v apt-get >/dev/null 2>&1; then
        SUDO_CMD=""
        [ "$EUID" -ne 0 ] && SUDO_CMD="sudo"
        $SUDO_CMD apt-get update -qq
        $SUDO_CMD apt-get install -y maven
        BUILD_CMD="mvn"
    else
        echo "ERROR: Neither ./mvnw nor system 'mvn' command is available."
        echo "Please install Maven or include ./mvnw in the repository root."
        exit 1
    fi
fi

echo "All required build tools and pom.xml verified."
echo ""

# Step 2: Build the application package
echo "=== [Step 2/4] Compiling Application Dependencies ==="

# Run quiet Maven build to prevent verbose stack traces on failure
if ! $BUILD_CMD clean package -DskipTests -q; then
    echo "ERROR: Failed to build Penny Pals application."
    echo "Please verify your Java code and pom.xml configuration."
    exit 1
fi

echo "Application compiled and packaged successfully."
echo ""

# Step 3: Set up runtime data directories
echo "=== [Step 3/4] Initializing Storage Directories ==="

# Create local storage directory for database files
mkdir -p ./data

echo "Local storage directory ready."
echo ""

# Step 4: Locate target JAR and launch application
echo "=== [Step 4/4] Starting Penny Pals Application ==="

# Locate compiled executable Spring Boot JAR file
JAR_FILE=$(find target -name "*.jar" ! -name "*-plain.jar" | head -n 1 2>/dev/null || true)

# Ensure executable JAR file exists before starting
if [ -z "$JAR_FILE" ] || [ ! -f "$JAR_FILE" ]; then
    echo "ERROR: Executable JAR file not found in target/ directory."
    echo "Ensure pom.xml produces a spring-boot-maven-plugin executable JAR."
    exit 1
fi

echo ""
echo "================================================================"
echo " Penny Pals server is launching!"
echo " Open your browser to: http://localhost:8080"
echo " Press Ctrl+C to stop the application."
echo "================================================================"
echo ""

# Launch the compiled Spring Boot application
exec java -jar "$JAR_FILE"