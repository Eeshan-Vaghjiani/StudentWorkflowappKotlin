# TeamSync - Quick Release Commands

## 🔑 Generate Keystore (One-time setup)

```bash
keytool -genkey -v -keystore teamsync-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias teamsync-key
```

**You'll be prompted for:**
- Keystore password (remember this!)
- Key password (remember this!)
- Your name
- Organization unit
- Organization name
- City/Locality
- State/Province
- Country code

**Important:** Store the keystore file securely and back it up!

---

## 📝 Configure Signing

Add to `local.properties` (create if doesn't exist):

```properties
KEYSTORE_FILE=C:/path/to/teamsync-release-key.jks
KEYSTORE_PASSWORD=your_keystore_password
KEY_ALIAS=teamsync-key
KEY_PASSWORD=your_key_password
```

---

## 🏗️ Build Commands

### Clean Build
```bash
./gradlew clean
```

### Build Debug APK (for testing)
```bash
./gradlew assembleDebug
```
Output: `app/build/outputs/apk/debug/TeamSync-v1.0.0-debug.apk`

### Build Release APK (for direct installation)
```bash
./gradlew assembleRelease
```
Output: `app/build/outputs/apk/release/TeamSync-v1.0.0-release.apk`

### Build Release AAB (for Play Store)
```bash
./gradlew bundleRelease
```
Output: `app/build/outputs/bundle/release/TeamSync-v1.0.0-release.aab`

---

## 📱 Install Commands

### Install Debug Build
```bash
./gradlew installDebug
```

### Install Release Build
```bash
adb install app/build/outputs/apk/release/TeamSync-v1.0.0-release.apk
```

### Uninstall App
```bash
adb uninstall com.teamsync.collaboration
```

---

## 🔍 Verify Build

### Check APK Size
```bash
ls -lh app/build/outputs/apk/release/
```

### Analyze APK Contents
```bash
./gradlew :app:analyzeReleaseBundle
```

### Check Dependencies
```bash
./gradlew :app:dependencies
```

---

## 📸 Capture Screenshots

### Using ADB
```bash
# Take screenshot
adb shell screencap -p /sdcard/screenshot.png

# Pull to computer
adb pull /sdcard/screenshot.png ./screenshots/

# Delete from device
adb shell rm /sdcard/screenshot.png
```

### Recommended Screens to Capture:
1. Login screen
2. Home dashboard
3. Groups list
4. Task management
5. Chat interface
6. Calendar view
7. Profile screen

---

## 🧪 Testing Commands

### Run on Connected Device
```bash
# List devices
adb devices

# Install and run
./gradlew installRelease
adb shell am start -n com.teamsync.collaboration/.Login
```

### Check Logs
```bash
# View all logs
adb logcat

# Filter by app
adb logcat | grep TeamSync

# Clear logs
adb logcat -c
```

### Check App Info
```bash
# Get package info
adb shell dumpsys package com.teamsync.collaboration

# Check permissions
adb shell dumpsys package com.teamsync.collaboration | grep permission
```

---

## 🚀 Pre-Release Checklist

Run these commands before releasing:

```bash
# 1. Clean build
./gradlew clean

# 2. Build release
./gradlew bundleRelease

# 3. Check build succeeded
ls -lh app/build/outputs/bundle/release/

# 4. Install and test
adb install app/build/outputs/apk/release/TeamSync-v1.0.0-release.apk

# 5. Check for errors
adb logcat | grep -E "ERROR|FATAL"
```

---

## 📦 Build Outputs

After successful build, you'll find:

**APK (for testing):**
```
app/build/outputs/apk/release/TeamSync-v1.0.0-release.apk
```

**AAB (for Play Store):**
```
app/build/outputs/bundle/release/TeamSync-v1.0.0-release.aab
```

**Mapping File (for crash reports):**
```
app/build/outputs/mapping/release/mapping.txt
```
⚠️ **Save this file!** You'll need it to deobfuscate crash reports.

---

## 🔄 Version Update

When releasing a new version:

1. Update `app/build.gradle.kts`:
```kotlin
versionCode = 2  // Increment
versionName = "1.0.1"  // Update
```

2. Rebuild:
```bash
./gradlew clean bundleRelease
```

---

## ⚠️ Troubleshooting

### Build Fails
```bash
# Clear Gradle cache
./gradlew clean --refresh-dependencies

# Clear build folder
rm -rf app/build
./gradlew clean
```

### Signing Issues
```bash
# Verify keystore
keytool -list -v -keystore teamsync-release-key.jks

# Check signing config
./gradlew signingReport
```

### Installation Fails
```bash
# Uninstall old version
adb uninstall com.teamsync.collaboration

# Reinstall
adb install -r app/build/outputs/apk/release/TeamSync-v1.0.0-release.apk
```

---

## 📊 Build Size Analysis

```bash
# Analyze APK size
./gradlew :app:analyzeReleaseBundle

# Check what's taking space
unzip -l app/build/outputs/apk/release/TeamSync-v1.0.0-release.apk | sort -k4 -n
```

---

## 🎯 Quick Test Script

Save as `test-release.sh`:

```bash
#!/bin/bash

echo "🧹 Cleaning..."
./gradlew clean

echo "🏗️ Building release..."
./gradlew assembleRelease

echo "📦 Checking output..."
ls -lh app/build/outputs/apk/release/

echo "📱 Installing..."
adb install -r app/build/outputs/apk/release/TeamSync-v1.0.0-release.apk

echo "🚀 Launching..."
adb shell am start -n com.teamsync.collaboration/.Login

echo "✅ Done! Check device."
```

Make executable: `chmod +x test-release.sh`
Run: `./test-release.sh`

---

## 📞 Need Help?

- Check `RELEASE_PREPARATION_GUIDE.md` for detailed instructions
- Review ProGuard rules in `app/proguard-rules.pro`
- Check Firebase configuration in `app/google-services.json`
- Verify manifest in `app/src/main/AndroidManifest.xml`

---

*Quick Reference for TeamSync v1.0.0*
