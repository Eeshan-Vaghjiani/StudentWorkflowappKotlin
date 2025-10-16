# Build Fix Instructions - Package Name Mismatch

## Issue Fixed ✅

The package name mismatch between `build.gradle.kts` and `google-services.json` has been resolved.

## Changes Made

### 1. Updated applicationId
Changed from `com.teamsync.collaboration` to `com.example.loginandregistration` to match Firebase configuration.

### 2. Removed Debug Suffix
Commented out `applicationIdSuffix = ".debug"` because Firebase only has the base package name registered.

## What You Need to Do Now

### Step 1: Close Android Studio
1. Save all files
2. Close Android Studio completely
3. This releases file locks that prevent Gradle from cleaning

### Step 2: Delete Build Folders (Optional but Recommended)
Open PowerShell in the project directory and run:

```powershell
# Delete build folders
Remove-Item -Recurse -Force .\build -ErrorAction SilentlyContinue
Remove-Item -Recurse -Force .\app\build -ErrorAction SilentlyContinue
Remove-Item -Recurse -Force .\.gradle -ErrorAction SilentlyContinue
```

Or manually delete these folders:
- `build/`
- `app/build/`
- `.gradle/`

### Step 3: Reopen Android Studio
1. Open Android Studio
2. Open your project
3. Wait for indexing to complete

### Step 4: Sync Gradle
1. Click "Sync Project with Gradle Files" (elephant icon with arrow)
2. Or: File → Sync Project with Gradle Files
3. Wait for sync to complete
4. Should complete without errors now

### Step 5: Build the App
1. Build → Clean Project
2. Build → Rebuild Project
3. Or click the Run button (green play icon)

## Expected Result

✅ Build should succeed  
✅ App should install on device/emulator  
✅ Firebase should connect properly  

## Current Configuration

```kotlin
// app/build.gradle.kts
defaultConfig {
    applicationId = "com.example.loginandregistration"
    // ...
}

buildTypes {
    debug {
        // applicationIdSuffix = ".debug" // COMMENTED OUT
        versionNameSuffix = "-debug"
    }
}
```

**Package Names**:
- Debug: `com.example.loginandregistration`
- Release: `com.example.loginandregistration`

**Note**: Both debug and release now use the same package name. This means you cannot install both versions simultaneously on the same device.

## Alternative: Add Debug Package to Firebase

If you want to keep the `.debug` suffix (to install both versions), you need to:

### Option A: Add Debug App to Firebase Console

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select project: "android-logreg"
3. Click gear icon → Project settings
4. Scroll to "Your apps"
5. Click "Add app" → Android
6. Enter package name: `com.example.loginandregistration.debug`
7. Download new `google-services.json`
8. Replace `app/google-services.json`
9. Uncomment the `applicationIdSuffix` line in `build.gradle.kts`
10. Sync and build

### Option B: Keep Current Configuration (Recommended)

- Simpler setup
- Works immediately
- Only one version can be installed at a time
- Sufficient for most development needs

## Troubleshooting

### Still Getting "No matching client found"

**Check 1**: Verify applicationId
```bash
grep "applicationId" app/build.gradle.kts
```
Should show: `applicationId = "com.example.loginandregistration"`

**Check 2**: Verify google-services.json
```bash
grep "package_name" app/google-services.json
```
Should show: `"package_name": "com.example.loginandregistration"`

**Check 3**: Verify debug suffix is commented
```bash
grep "applicationIdSuffix" app/build.gradle.kts
```
Should show: `// applicationIdSuffix = ".debug"`

**Check 4**: Clean everything
1. Close Android Studio
2. Delete `build/`, `app/build/`, `.gradle/`
3. Reopen and sync

### Build Fails with File Lock Error

**Cause**: Android Studio has files open

**Solution**:
1. Close Android Studio
2. Wait 10 seconds
3. Reopen Android Studio
4. Try building again

### Firebase Not Connecting After Build

**Check 1**: Verify google-services.json location
- Must be at: `app/google-services.json`
- Not in project root

**Check 2**: Verify Firebase services enabled
- Authentication: Email/Password enabled
- Firestore: Database created
- Cloud Messaging: Enabled

**Check 3**: Check logcat for errors
- Look for Firebase initialization errors
- Look for authentication errors

## Summary

✅ **Fixed**: Package name now matches Firebase configuration  
✅ **Simplified**: Removed debug suffix for easier setup  
✅ **Ready**: Close Android Studio, reopen, sync, and build  

## Next Steps

1. **Close Android Studio** (important!)
2. **Delete build folders** (optional but recommended)
3. **Reopen Android Studio**
4. **Sync Gradle**
5. **Build and run**

The app should now build successfully! 🚀

---

**Need Help?**
- Check `PACKAGE_NAME_FIX_GUIDE.md` for detailed explanation
- Check `FIREBASE_CONFIGURATION_GUIDE.md` for Firebase setup
- Check `TROUBLESHOOTING_GUIDE.md` for common issues
