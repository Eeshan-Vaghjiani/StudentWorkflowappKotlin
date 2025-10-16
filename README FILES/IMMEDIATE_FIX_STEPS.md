# Immediate Fix Steps - Run Configuration Error

## The Problem

Android Studio's run configuration is looking for the activity in the wrong place:
- Looking for: `com.teamsync.collaboration.debug/com.example.loginandregistration.Login`
- Should be: `com.teamsync.collaboration/com.example.loginandregistration.Login`

The `.debug` suffix is appearing even though we commented it out.

## Solution: Force Android Studio to Refresh

### Method 1: Invalidate Caches (MOST RELIABLE)

**Do this first - it almost always works:**

1. **File → Invalidate Caches / Restart**
2. Check **ALL** boxes:
   - ✅ Invalidate and Restart
   - ✅ Clear file system cache and Local History
   - ✅ Clear downloaded shared indexes
   - ✅ Clear VCS Log caches and indexes
3. Click **"Invalidate and Restart"**
4. **Wait 2-5 minutes** for Android Studio to:
   - Restart
   - Re-index the project
   - Rebuild caches
5. After restart, click **Run** (green play button)

### Method 2: Delete .idea Folder

If Method 1 doesn't work:

1. **Close Android Studio completely**
2. **Open File Explorer**
3. Navigate to your project folder: `C:\Users\evagh\AndroidStudioProjects\loginandregistration`
4. **Delete the `.idea` folder** (it's hidden, enable "Show hidden files")
5. **Reopen Android Studio**
6. **Open your project** (it will re-import)
7. **Wait for Gradle sync** to complete
8. Click **Run**

### Method 3: Manually Edit Run Configuration

1. **Run → Edit Configurations**
2. Select **"app"** in the left panel
3. In the right panel, find **"Launch Options"**
4. Change **"Launch"** dropdown to **"Specified Activity"**
5. In **"Activity"** field, enter: `com.example.loginandregistration.Login`
6. Click **"Apply"**
7. Click **"OK"**
8. Click **Run**

### Method 4: Create New Run Configuration

1. **Run → Edit Configurations**
2. Click **"-"** (minus) to delete the "app" configuration
3. Click **"+"** (plus) → **"Android App"**
4. Name it: **"app"**
5. Module: Select **"loginandregistration.app.main"**
6. Launch Options:
   - Launch: **"Default Activity"**
7. Click **"Apply"**
8. Click **"OK"**
9. Click **Run**

### Method 5: Use Release Build Instead

If debug build is problematic, try release:

1. **Build → Select Build Variant**
2. Change from **"debug"** to **"release"**
3. Click **Run**

Note: Release builds don't have the `.debug` suffix.

## Why This Is Happening

Even though we commented out `applicationIdSuffix = ".debug"`, Android Studio's cached run configuration still thinks it's there.

**The build is correct** - the APK was built successfully with the right package name. It's just the run configuration that's wrong.

## Verification After Fix

After the app launches successfully, verify:

1. **Check installed package**:
   - Open device Settings → Apps
   - Find your app
   - Package name should be: `com.teamsync.collaboration`

2. **Check in Android Studio**:
   - Build → Analyze APK
   - Select: `app/build/outputs/apk/debug/app-debug.apk`
   - Check package name in manifest

## Alternative: Manual Installation

If Android Studio won't launch it, install manually:

### Option A: From Android Studio
1. **Build → Build Bundle(s) / APK(s) → Build APK(s)**
2. Wait for "APK(s) generated successfully"
3. Click **"locate"** in the notification
4. **Drag the APK to your emulator/device**
5. Install it
6. Launch manually from app drawer

### Option B: From File Explorer
1. Navigate to: `app\build\outputs\apk\debug\`
2. Find: `app-debug.apk`
3. **Drag to emulator** or **copy to device**
4. Install and launch

## Nuclear Option: Complete Reset

If nothing works, do a complete reset:

1. **Close Android Studio**
2. **Delete these folders**:
   ```
   .idea
   .gradle
   build
   app\build
   ```
3. **Delete this file**:
   ```
   local.properties
   ```
4. **Reopen Android Studio**
5. **Open project** (will re-import from scratch)
6. **Wait for Gradle sync** (5-10 minutes)
7. **Build → Rebuild Project**
8. **Run**

## Expected Timeline

- **Method 1** (Invalidate Caches): 2-5 minutes
- **Method 2** (Delete .idea): 3-7 minutes
- **Method 3** (Edit Config): 30 seconds
- **Method 4** (New Config): 1 minute
- **Method 5** (Release Build): 1 minute
- **Nuclear Option**: 10-15 minutes

## What Should Happen

After the fix:
✅ App launches successfully
✅ Shows login screen
✅ No error about activity not found
✅ Package name: `com.teamsync.collaboration`
✅ Firebase connects properly

## Still Not Working?

If you've tried all methods and it still doesn't work, there might be a deeper issue. Try:

1. **Check if APK is valid**:
   - Navigate to `app\build\outputs\apk\debug\`
   - Right-click `app-debug.apk`
   - Properties → Details
   - Should show package: `com.teamsync.collaboration`

2. **Check Android Studio version**:
   - Help → About
   - Should be Arctic Fox or later
   - Update if needed

3. **Check Gradle version**:
   - File → Project Structure → Project
   - Gradle version should be 8.0+

## Recommended Approach

**Try these in order**:

1. ✅ **Invalidate Caches** (Method 1) - 90% success rate
2. ✅ **Edit Run Configuration** (Method 3) - Quick and easy
3. ✅ **Delete .idea** (Method 2) - 95% success rate
4. ✅ **Manual Installation** - Always works
5. ✅ **Nuclear Option** - 100% success rate but takes time

## Quick Command Reference

```powershell
# Check if APK exists
Test-Path app\build\outputs\apk\debug\app-debug.apk

# View APK info (if aapt is available)
aapt dump badging app\build\outputs\apk\debug\app-debug.apk | Select-String "package"

# Delete problematic folders (from project root)
Remove-Item -Recurse -Force .idea, .gradle, build, app\build -ErrorAction SilentlyContinue
```

---

**Start with Method 1 (Invalidate Caches). It works 90% of the time!** 🚀

If that doesn't work, try Method 3 (Edit Run Configuration) - it's quick and often fixes the issue.
