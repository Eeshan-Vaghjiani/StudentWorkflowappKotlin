# Quick Fix: Group Creation Permission Error

## The Problem
Getting "Missing or insufficient permissions" when creating groups.

## The Solution (Takes 30 seconds)

### Step 1: Sign Out
1. Open the app
2. Go to Profile/Settings
3. Click "Sign Out"

### Step 2: Close App
- Completely close the app (swipe away from recent apps)

### Step 3: Reopen and Sign In
1. Open the app again
2. Sign in with your credentials

### Step 4: Try Again
- Go to Groups tab
- Click "Create Group"
- Fill in the details
- Click "Create"

## Why This Works
Your authentication token expired. Signing out and back in refreshes it.

## If It Still Doesn't Work

### Option A: Clear App Data
Settings → Apps → TeamSync → Storage → Clear Data

### Option B: Reinstall
Uninstall and reinstall the app from Android Studio

## Technical Details
- ✅ Firestore rules have been updated
- ✅ Rules are now less restrictive
- ✅ Changes deployed to Firebase
- ⏳ You need to refresh your authentication token

## Need Help?
Check `GROUP_CREATION_FIX_COMPLETE.md` for detailed troubleshooting.
