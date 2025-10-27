# Rebuild and Run - Fix the Crash

## I've Fixed the Code

I added error handling to prevent the crash. Now you need to rebuild.

## Do These Steps in Android Studio:

### Step 1: Clean Project
1. Click **Build** menu
2. Click **Clean Project**
3. Wait for "BUILD SUCCESSFUL"

### Step 2: Rebuild Project
1. Click **Build** menu
2. Click **Rebuild Project**
3. Wait for "BUILD SUCCESSFUL"

### Step 3: Uninstall Old App (Important!)
1. On your device/emulator, go to **Settings → Apps**
2. Find the app (might be called "Team Collaboration" or "LoginAndRegistration")
3. **Uninstall it**
4. This ensures the new version installs cleanly

### Step 4: Run
1. Click the **Run** button (green play icon)
2. App should launch without crashing

## What I Fixed

Added try-catch error handling to the Application class so Firebase initialization errors won't crash the app.

## If It Still Crashes

We need to see the crash log. Do this:

1. **Run the app**
2. **Wait for crash**
3. **Click "Logcat" tab** at the bottom of Android Studio
4. **Look for red text** that says "FATAL EXCEPTION"
5. **Copy that entire section** (from "FATAL EXCEPTION" to the end)
6. **Share it with me**

Then I can see exactly what's causing the crash and fix it.

## Quick Checklist

- [ ] Build → Clean Project
- [ ] Build → Rebuild Project
- [ ] Uninstall old app from device
- [ ] Run from Android Studio

---

**Do these 4 steps and the app should work!** 🚀

If it still crashes, share the Logcat output so I can see the exact error.
