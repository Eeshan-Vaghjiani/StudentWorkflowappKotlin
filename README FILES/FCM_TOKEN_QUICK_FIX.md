# FCM Token Error - Quick Fix Guide

## Problem
You're seeing this error in logcat:
```
FCMService: Failed to save FCM token via repository
java.lang.Exception: User not authenticated
```

## Solution Applied ✅

### What Was Wrong
1. FCM token was trying to save before user logged in (expected, but logged as ERROR)
2. FCM token was NOT being saved after successful login (actual bug)

### What Was Fixed
1. ✅ Changed error message to warning for pre-login token generation
2. ✅ Added FCM token saving after email/password login
3. ✅ Added FCM token saving after Google Sign-In

## Files Modified

1. **MyFirebaseMessagingService.kt** - Better error handling
2. **Login.kt** - Added FCM token saving after login

## To Build and Test

### Option 1: If Android Studio is Open
1. Close Android Studio completely
2. Run: `./gradlew clean`
3. Reopen Android Studio
4. Build and run the app

### Option 2: Command Line Build
```bash
# Stop all Gradle daemons
./gradlew --stop

# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Install to connected device/emulator
./gradlew installDebug
```

### Option 3: Use the Fix Script
Run the provided batch file:
```bash
./fix-build.bat
```

## What You Should See Now

### Before Login (Normal)
```
✅ FCMService: New FCM token: [token]
✅ FCMService: FCM token not saved - user not authenticated yet. Token will be saved on login.
```

### After Login (Success)
```
✅ LoginActivity: Login successful
✅ LoginActivity: FCM token saved successfully after login
✅ NotificationRepository: FCM token saved successfully: [token]
```

## Verify It Works

1. **Check Logcat:**
   - Filter by "FCMService" and "LoginActivity"
   - Look for "FCM token saved successfully after login"

2. **Check Firestore:**
   - Open Firebase Console
   - Go to Firestore Database
   - Open `users` collection
   - Find your user document
   - Verify `fcmToken` field has a value

## Troubleshooting

### Build Fails with "Unable to delete file"
**Cause:** Android Studio or emulator has files locked

**Fix:**
1. Close Android Studio
2. Stop emulator
3. Run: `./gradlew --stop`
4. Wait 10 seconds
5. Run: `./gradlew clean`

### Still See "User not authenticated" Error
**Check:** Is it logged as ERROR or WARNING?
- **WARNING** = Normal, expected behavior ✅
- **ERROR** = File wasn't updated correctly ❌

### Token Not Saving After Login
**Check logcat for:**
```
LoginActivity: Failed to save FCM token after login: [error message]
```

**Common causes:**
- No internet connection
- Firestore rules blocking write
- User document doesn't exist

## Next Steps

After successful build and test:
1. ✅ Verify FCM token is saved in Firestore
2. ✅ Test push notifications
3. ✅ Verify token updates on app reinstall
4. ✅ Test both email and Google Sign-In flows

## Need Help?

If you still see issues:
1. Share the full logcat output (filter by "FCM" and "Login")
2. Check Firebase Console for any Firestore errors
3. Verify your Firestore security rules allow token writes
