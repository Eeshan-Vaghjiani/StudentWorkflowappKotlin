#!/bin/bash
# Android App Release Preparation Script for Unix/Linux/Mac
# This script prepares the app for release by incrementing version and generating builds

echo "========================================"
echo "TeamSync App Release Preparation"
echo "========================================"
echo ""

# Configuration
BUILD_GRADLE="app/build.gradle.kts"
CURRENT_VERSION_CODE=$(grep "versionCode = " "$BUILD_GRADLE" | sed 's/[^0-9]*//g')
CURRENT_VERSION_NAME=$(grep "versionName = " "$BUILD_GRADLE" | sed 's/.*"\(.*\)".*/\1/')

echo "Current Version:"
echo "  Version Code: $CURRENT_VERSION_CODE"
echo "  Version Name: $CURRENT_VERSION_NAME"
echo ""

# Step 1: Increment version
echo "[1/6] Increment version numbers"
echo ""
echo "Version increment options:"
echo "  1) Patch (1.0.0 -> 1.0.1) - Bug fixes"
echo "  2) Minor (1.0.0 -> 1.1.0) - New features"
echo "  3) Major (1.0.0 -> 2.0.0) - Breaking changes"
echo "  4) Custom version"
echo "  5) Keep current version"
echo ""
read -p "Select option (1-5): " VERSION_OPTION

case $VERSION_OPTION in
    1)
        # Patch increment
        NEW_VERSION_NAME=$(echo $CURRENT_VERSION_NAME | awk -F. '{$NF = $NF + 1;} 1' | sed 's/ /./g')
        ;;
    2)
        # Minor increment
        NEW_VERSION_NAME=$(echo $CURRENT_VERSION_NAME | awk -F. '{$(NF-1) = $(NF-1) + 1; $NF = 0;} 1' | sed 's/ /./g')
        ;;
    3)
        # Major increment
        NEW_VERSION_NAME=$(echo $CURRENT_VERSION_NAME | awk -F. '{$1 = $1 + 1; $2 = 0; $3 = 0;} 1' | sed 's/ /./g')
        ;;
    4)
        # Custom version
        read -p "Enter new version name (e.g., 1.2.0): " NEW_VERSION_NAME
        ;;
    5)
        # Keep current
        NEW_VERSION_NAME=$CURRENT_VERSION_NAME
        ;;
    *)
        echo "Invalid option. Exiting."
        exit 1
        ;;
esac

NEW_VERSION_CODE=$((CURRENT_VERSION_CODE + 1))

echo ""
echo "New Version:"
echo "  Version Code: $NEW_VERSION_CODE"
echo "  Version Name: $NEW_VERSION_NAME"
echo ""
read -p "Proceed with version update? (yes/no): " CONFIRM
if [ "$CONFIRM" != "yes" ]; then
    echo "Version update cancelled"
    exit 0
fi

# Update build.gradle.kts
sed -i.bak "s/versionCode = $CURRENT_VERSION_CODE/versionCode = $NEW_VERSION_CODE/" "$BUILD_GRADLE"
sed -i.bak "s/versionName = \"$CURRENT_VERSION_NAME\"/versionName = \"$NEW_VERSION_NAME\"/" "$BUILD_GRADLE"
echo "Version updated in $BUILD_GRADLE"
echo ""

# Step 2: Clean build
echo "[2/6] Cleaning previous builds..."
./gradlew clean
if [ $? -ne 0 ]; then
    echo "ERROR: Clean failed"
    exit 1
fi
echo ""

# Step 3: Run tests
echo "[3/6] Running unit tests..."
read -p "Run tests before building? (yes/no): " RUN_TESTS
if [ "$RUN_TESTS" == "yes" ]; then
    ./gradlew test
    if [ $? -ne 0 ]; then
        echo "WARNING: Tests failed. Continue anyway? (yes/no)"
        read CONTINUE
        if [ "$CONTINUE" != "yes" ]; then
            exit 1
        fi
    fi
fi
echo ""

# Step 4: Build release APK
echo "[4/6] Building release APK..."
./gradlew assembleRelease
if [ $? -ne 0 ]; then
    echo "ERROR: Release build failed"
    exit 1
fi
echo "Release APK built successfully"
echo ""

# Step 5: Build release AAB (for Play Store)
echo "[5/6] Building release AAB (Android App Bundle)..."
./gradlew bundleRelease
if [ $? -ne 0 ]; then
    echo "ERROR: Bundle build failed"
    exit 1
fi
echo "Release AAB built successfully"
echo ""

# Step 6: Summary
echo "[6/6] Build Summary"
echo "========================================"
echo "Version: $NEW_VERSION_NAME (Code: $NEW_VERSION_CODE)"
echo ""
echo "Build artifacts:"
echo "  APK: app/build/outputs/apk/release/app-release.apk"
echo "  AAB: app/build/outputs/bundle/release/app-release.aab"
echo ""
echo "Next steps:"
echo "1. Test APK on multiple devices"
echo "2. Upload AAB to Google Play Console"
echo "3. Deploy to internal testing track first"
echo "4. Monitor for crashes and errors"
echo "5. Promote to production after validation"
echo ""
echo "Note: Ensure signing configuration is set up for production release"
echo "========================================"
