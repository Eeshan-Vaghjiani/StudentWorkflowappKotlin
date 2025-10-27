# Fix Run Configuration Error

## Error
```
Activity class {com.teamsync.collaboration.debug/com.example.loginandregistration.Login} does not exist
```

## Quick Fix - Do These Steps

### Step 1: Sync Gradle
1. Click **File → Sync Project with Gradle Files**
2. Wait for sync to complete (watch bottom right corner)

### Step 2: Clean Project
1. Click **Build → Clean Project**
2. Wait for "BUILD SUCCESSFUL" message

### Step 3: Rebuild Project
1. Click **Build → Rebuild Project**
2. Wait for rebuild to complete

### Step 4: Run App
1. Click the **Run** button (green play icon)
2. App should launch successfully

## If That Doesn't Work

### Option A: Invalidate Caches
1. Click **File → Invalidate Caches / Restart**
2. Select **"Invalidate and Restart"**
3. Wait for Android Studio to restart (1-2 minutes)
4. After restart, click **Run**

### Option B: Delete Run Configuration
1. Click **Run → Edit Configurations**
2. Find "app" in the left panel
3. Click the **-** (minus) button to delete it
4. Click **OK**
5. Click **Run** - Android Studio will create a new configuration
6. App should launch

### Option C: Manual Run Configuration
1. Click **Run → Edit Configurations**
2. Select "app" configuration
3. In "Launch Options":
   - Launch: **Default Activity**
   - Or Launch: **Specified Activity**
   - Activity: `com.example.loginandregistration.Login`
4. Click **OK**
5. Click **Run**

## Why This Happens

When you changed the `applicationId` from `com.example.loginandregistration` to `com.teamsync.collaboration`, Android Studio's run configuration got confused about where to find the launcher activity.

**The configuration is correct** - Android Studio just needs to refresh.

## What's Actually Correct

```kotlin
// In build.gradle.kts
namespace = "com.example.loginandregistration"  // Where code lives ✅
applicationId = "com.teamsync.collaboration"    // App package name ✅
```

This is **perfectly valid** - namespace and applicationId don't have to match!

## Verification

After the fix, the app should:
- ✅ Launch successfully
- ✅ Show the login screen
- ✅ Package name in device settings: `com.teamsync.collaboration`
- ✅ Code still in: `com.example.loginandregistration` package

## Still Not Working?

Try this nuclear option:

1. **Close Android Studio**
2. **Delete these folders**:
   ```powershell
   Remove-Item -Recurse -Force .idea
   Remove-Item -Recurse -Force .gradle
   Remove-Item -Recurse -Force build
   Remove-Item -Recurse -Force app\build
   ```
3. **Reopen Android Studio**
4. **Wait for full re-indexing** (5-10 minutes)
5. **Sync Gradle**
6. **Run**

## Expected Result

✅ App launches  
✅ Shows login screen  
✅ Package: `com.teamsync.collaboration`  
✅ Firebase connects  

---

**Just sync, clean, rebuild, and run. That should fix it!** 🚀
