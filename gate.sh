#!/bin/bash
set -e

# The Project Gate
echo "🚀 Running the Project Gate..."

# 1. Check for Gradle wrapper
if [ ! -f "gradlew" ]; then
  echo "❌ Error: gradlew not found. Please run 'gradle wrapper' first."
  exit 1
fi

# 2. Linting
echo "🔍 Running Lint..."
./gradlew lintDebug

# 3. Unit Tests
echo "🧪 Running Unit Tests..."
./gradlew testDebugUnitTest

# 3.5. Instrumentation (Compose UI) Tests
if [ "$RUN_COMPOSE_TEST" = "true" ]; then
    echo "⚡️ Running Compose UI Tests..."
    ./gradlew connectedDebugAndroidTest
fi

# 4. Build Assembly
echo "🏗️ Running Build Assembly..."
./gradlew assembleDebug

# 5. Memory Scan (optional)
echo "🧠 Scanning Project Memory..."
if [ ! -f "MEMORY.md" ]; then
  echo "⚠️ Warning: MEMORY.md not found. Agents must maintain memory."
fi

echo "✅ The Gate has passed. Ready for commit."
