# How to Apply the Group Creation Fix

## The Problem
You're still running the OLD version of the app that doesn't have the `memberIds` fix. The code has been updated, but Android Studio hasn't rebuilt and reinstalled the app yet.

## Solution: Rebuild and Reinstall

### Option 1: Clean Rebuild (Recommended)
```cmd
gradlew clean
gradlew assembleDebug
gradlew installDebug
```

### Option 2: Android Studio
1. Click **Build** → **Clean Project**
2. Wait for it to finish
3. Click **Build** → **Rebuild Project**
4. Click **Run** → **Run 'app'** (or press Shift+F10)

### Option 3: Quick Reinstall
1. In Android Studio, click the **Run** button (green play icon)
2. Make sure "Always install with package manager" is enabled in Run Configuration

## Verify the Fix
After rebuilding, try creating a group again. You should see:
- No more "PERMISSION_DENIED" errors
- Group creation succeeds
- You appear as the first member

## Why This Happened
The code change was made to `EnhancedGroupRepository.kt`, but:
1. The app uses `GroupRepository.kt` (which already had the fix)
2. The APK on your emulator is still the old version
3. Android needs to recompile and reinstall for changes to take effect

## If It Still Fails
Check the logcat for the exact error. If you still see:
```
Write failed at groups/xxxxx: PERMISSION_DENIED
```

Then the issue might be:
1. Firestore rules not deployed (run `firebase deploy --only firestore:rules`)
2. User authentication token expired (log out and log back in)
3. Different repository being used than expected
