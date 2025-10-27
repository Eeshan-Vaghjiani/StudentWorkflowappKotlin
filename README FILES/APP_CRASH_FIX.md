# App Crash Fix Guide

## The Issue

App launches but crashes immediately:
```
PROCESS STARTED (22187) for package com.teamsync.collaboration
PROCESS ENDED (22187) for package com.teamsync.collaboration
```

## Most Common Causes

### 1. Firebase Configuration Issue (MOST LIKELY)

**Problem**: `google-services.json` might not be properly configured for the new package name.

**Fix**:
1. Open `app/google-services.json`
2. Verify it contains `com.teamsync.collaboration`
3. If not, re-download from Firebase Console

**To verify**:
```powershell
# Check if package is in google-services.json
Select-String -Path app\google-services.json -Pattern "com.teamsync.collaboration"
```

Should show at least one match.

### 2. Firestore Offline Persistence Issue

**Problem**: Firestore offline persistence might fail on first run.

**Fix Applied**: Added try-catch to Application class to prevent crash.

**Rebuild**:
1. Build → Clean Project
2. Build → Rebuild Project
3. Run again

### 3. Missing Permissions

**Problem**: App might need permissions that aren't granted.

**Fix**: Check AndroidManifest.xml has all required permissions (already correct).

### 4. ProGuard/R8 Issue

**Problem**: Debug build shouldn't have this, but check anyway.

**Verify**: In `build.gradle.kts`, debug build should have:
```kotlin
debug {
    isMinifyEnabled = false  // Should be false
}
```

## How to See the Actual Crash

### Method 1: Android Studio Logcat

1. **View → Tool Windows → Logcat**
2. **Run the app**
3. **Look for red lines** with "FATAL EXCEPTION" or "AndroidRuntime"
4. **Copy the stack trace** and share it

### Method 2: Command Line (if adb works)

```powershell
# Clear old logs
adb logcat -c

# Run app and watch logs
adb logcat | Select-String "AndroidRuntime|FATAL|Exception"
```

### Method 3: Android Studio Run Tab

1. Click the **Run** tab at the bottom
2. Look for error messages in red
3. Expand any error sections

## Quick Fixes to Try

### Fix 1: Clean and Rebuild

```powershell
# From project root
./gradlew clean
./gradlew assembleDebug
```

Then run from Android Studio.

### Fix 2: Uninstall Old Version

The old version with different package name might be conflicting:

1. On device/emulator: Settings → Apps
2. Find any "Team Collaboration" or "Login" apps
3. Uninstall all of them
4. Run app again from Android Studio

### Fix 3: Use Release Build

Debug builds sometimes have issues. Try release:

1. Build → Select Build Variant
2. Change to "release"
3. Run

### Fix 4: Check Firebase Console

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. Go to Project Settings
4. Verify `com.teamsync.collaboration` is listed under "Your apps"
5. If not, add it again

### Fix 5: Verify google-services.json

Open `app/google-services.json` and check:

```json
{
  "client": [
    {
      "client_info": {
        "android_client_info": {
          "package_name": "com.teamsync.collaboration"  // ← Should be here
        }
      }
    }
  ]
}
```

If it's not there, you need to add the app to Firebase and download new JSON.

## Rebuild After Fix

After applying any fix:

1. **Build → Clean Project**
2. **Build → Rebuild Project**
3. **Run**

## Get Detailed Crash Info

To help diagnose, we need the crash stack trace. Here's how:

### In Android Studio:

1. **Run the app**
2. **Wait for crash**
3. **Click "Logcat" tab** at bottom
4. **Look for red text** with "FATAL EXCEPTION"
5. **Copy everything** from "FATAL EXCEPTION" to "Process: com.teamsync.collaboration"
6. **Share that text**

### Common Crash Patterns:

#### Pattern 1: ClassNotFoundException
```
java.lang.ClassNotFoundException: com.example.loginandregistration.TeamCollaborationApp
```
**Fix**: Clean and rebuild project.

#### Pattern 2: Firebase not initialized
```
FirebaseApp is not initialized
```
**Fix**: Check google-services.json is in `app/` folder.

#### Pattern 3: Firestore persistence error
```
Failed to enable offline persistence
```
**Fix**: Already added try-catch, rebuild app.

#### Pattern 4: Missing dependency
```
NoClassDefFoundError: androidx.core.splashscreen
```
**Fix**: Sync Gradle (already added dependency).

## Verification Steps

### 1. Check google-services.json Location
```powershell
Test-Path app\google-services.json
```
Should return: `True`

### 2. Check google-services.json Content
```powershell
Select-String -Path app\google-services.json -Pattern "com.teamsync.collaboration"
```
Should show matches.

### 3. Check Build Configuration
```powershell
Select-String -Path app\build.gradle.kts -Pattern "applicationId"
```
Should show: `applicationId = "com.teamsync.collaboration"`

### 4. Check Namespace
```powershell
Select-String -Path app\build.gradle.kts -Pattern "namespace"
```
Should show: `namespace = "com.example.loginandregistration"`

## What I've Already Fixed

✅ Added try-catch to Application class to prevent Firebase initialization crashes
✅ Package name configuration is correct
✅ Splash screen library is added
✅ All dependencies are correct

## Next Steps

1. **Rebuild the app** (Clean → Rebuild)
2. **Run again**
3. **If still crashes**, check Logcat for the actual error
4. **Share the crash stack trace** so I can see the exact error

## Most Likely Solution

Based on the symptoms, the most likely fix is:

1. **Build → Clean Project**
2. **Build → Rebuild Project**  
3. **Uninstall app from device** (Settings → Apps → Uninstall)
4. **Run from Android Studio**

The try-catch I added should prevent the crash, but rebuilding ensures the new code is used.

---

**Do the rebuild steps above, then run again and check if it still crashes.**

If it still crashes, we need to see the Logcat output to know the exact error.
