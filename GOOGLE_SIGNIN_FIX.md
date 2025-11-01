# Google Sign-In Fix Guide

## Problem Identified
Google Sign-In is failing because your Firebase project is missing an Android OAuth client for the package name `com.teamsync.collaboration`.

## What I Fixed
✅ Updated `strings.xml` with the correct Web client ID: `52796977485-2ckv1qu3dbdqc1dtmptj94s243pb03ir.apps.googleusercontent.com`

## What You Need to Do

### Option 1: Add Android OAuth Client (Recommended)
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project: `android-logreg`
3. Go to **Project Settings** (gear icon) → **General** tab
4. Scroll down to **Your apps** section
5. Find the app with package name `com.teamsync.collaboration`
6. Click **Add fingerprint** and add your SHA-1 certificate fingerprint

#### Get Your SHA-1 Fingerprint:
Run this command in your project directory:
```bash
cd android
gradlew signingReport
```

Or for debug keystore:
```bash
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

7. Copy the SHA-1 fingerprint
8. Add it to your Firebase app configuration
9. Download the updated `google-services.json` and replace the current one
10. Rebuild your app

### Option 2: Quick Test (Use existing package)
Temporarily change your package name back to `com.example.loginandregistration` in `build.gradle.kts`:

```kotlin
defaultConfig {
    applicationId = "com.example.loginandregistration"  // Change this line
    // ... rest of config
}
```

Then rebuild and test.

## Current Configuration Status

### google-services.json has:
- ✅ Package: `com.example.loginandregistration` - Has Android OAuth client
- ❌ Package: `com.teamsync.collaboration` - Missing Android OAuth client (only has Web client)

### Your app uses:
- Package: `com.teamsync.collaboration` (from build.gradle.kts)

## Why This Happens
Google Sign-In requires:
1. A Web OAuth client ID (for Firebase Auth) - ✅ You have this
2. An Android OAuth client ID (for Google Sign-In SDK) - ❌ Missing for your current package

The Android OAuth client is tied to your package name + SHA-1 certificate fingerprint. When these don't match, Google Sign-In returns "cancelled" even though the user didn't cancel.

## Testing After Fix
1. Clean and rebuild: `gradlew clean build`
2. Uninstall the app from your device/emulator
3. Install fresh: `gradlew installDebug`
4. Test Google Sign-In

## Additional Notes
- The error "Google sign-in cancelled by user" is misleading - it's actually a configuration error
- Error code 12501 typically means SHA-1 mismatch or missing OAuth client
- Make sure to use the debug keystore SHA-1 for development builds
