# FCM Token Fix - Testing Steps

## Prerequisites
- Android Studio or VS Code with Android SDK
- Android Emulator or physical device
- Firebase project configured
- Logcat access

## Step-by-Step Testing Guide

### Step 1: Build the App

#### Option A: Using Android Studio
1. Close Android Studio completely
2. Delete the `app/build` folder manually if needed
3. Reopen Android Studio
4. Click **Build → Clean Project**
5. Click **Build → Rebuild Project**
6. Click **Run** button

#### Option B: Using Command Line
```bash
# Stop all Gradle processes
./gradlew --stop

# Clean build
./gradlew clean

# Build and install
./gradlew installDebug
```

#### Option C: Use Fix Script
```bash
./fix-build.bat
```

### Step 2: Prepare Logcat Monitoring

Open logcat and set up filters:

**Filter 1: FCM Service**
```
tag:FCMService
```

**Filter 2: Login Activity**
```
tag:LoginActivity
```

**Filter 3: Notification Repository**
```
tag:NotificationRepository
```

**Combined Filter:**
```
tag:FCMService | tag:LoginActivity | tag:NotificationRepository
```

### Step 3: Test App Start (Before Login)

1. **Uninstall the app** from device/emulator (fresh start)
2. **Install and launch** the app
3. **Check logcat** for these messages:

#### ✅ Expected Output (GOOD):
```
FCMService: New FCM token: dA9FTlbBBQf-IKYJMMCkXRP:APA91b...
FCMService: FCM token not saved - user not authenticated yet. Token will be saved on login.
```

#### ❌ Wrong Output (BAD):
```
FCMService: New FCM token: dA9FTlbBBQf-IKYJMMCkXRP:APA91b...
FCMService: Failed to save FCM token via repository
```

**Result:** If you see the ✅ expected output, the first fix is working!

### Step 4: Test Email/Password Login

1. **On login screen**, enter credentials:
   - Email: `test@example.com`
   - Password: `password123`

2. **Click Login button**

3. **Check logcat** for these messages:

#### ✅ Expected Output (GOOD):
```
LoginActivity: signInWithEmail:success
LoginActivity: FCM token saved successfully after login
NotificationRepository: FCM token saved successfully: dA9FTlbBBQf-IKYJMMCkXRP:APA91b...
```

#### ❌ Wrong Output (BAD):
```
LoginActivity: signInWithEmail:success
(No FCM token messages)
```

**Result:** If you see the ✅ expected output, email login fix is working!

### Step 5: Verify in Firestore

1. **Open Firebase Console**: https://console.firebase.google.com
2. **Navigate to:** Firestore Database
3. **Open collection:** `users`
4. **Find your user document** (by email or UID)
5. **Check for field:** `fcmToken`

#### ✅ Expected (GOOD):
```json
{
  "uid": "abc123...",
  "email": "test@example.com",
  "displayName": "Test User",
  "fcmToken": "dA9FTlbBBQf-IKYJMMCkXRP:APA91b...",  ← Should have a value!
  "isOnline": true,
  "lastActive": { "seconds": 1697545740 }
}
```

#### ❌ Wrong (BAD):
```json
{
  "uid": "abc123...",
  "email": "test@example.com",
  "fcmToken": "",  ← Empty or missing!
}
```

### Step 6: Test Google Sign-In

1. **Log out** from the app
2. **On login screen**, click **Sign in with Google**
3. **Select Google account**
4. **Check logcat** for these messages:

#### ✅ Expected Output (GOOD):
```
LoginActivity: Google sign-in successful, authenticating with Firebase
LoginActivity: Firebase authentication successful for user: xyz789
LoginActivity: FCM token saved successfully after login
NotificationRepository: FCM token saved successfully: dA9FTlbBBQf-IKYJMMCkXRP:APA91b...
```

**Result:** If you see the ✅ expected output, Google login fix is working!

### Step 7: Test Token Refresh

1. **Uninstall the app**
2. **Reinstall the app**
3. **Login again**
4. **Check Firestore** - token should be updated with new value

### Step 8: Test Push Notifications (Optional)

#### Using Firebase Console:
1. Go to **Firebase Console → Cloud Messaging**
2. Click **Send your first message**
3. Enter notification title and text
4. Click **Send test message**
5. Enter your FCM token (from Firestore)
6. Click **Test**

#### ✅ Expected:
- Notification appears on device
- App receives the notification

#### ❌ Wrong:
- No notification received
- Check if token is correct in Firestore

## Troubleshooting Guide

### Issue 1: Build Fails with "Unable to delete file"

**Symptoms:**
```
Unable to delete file 'C:\...\app\build\...\R.jar'
```

**Solution:**
1. Close Android Studio
2. Stop emulator
3. Run: `./gradlew --stop`
4. Wait 10 seconds
5. Manually delete `app/build` folder
6. Run: `./gradlew clean`

### Issue 2: Still See ERROR Instead of WARNING

**Symptoms:**
```
FCMService: Failed to save FCM token via repository (ERROR level)
```

**Solution:**
1. Verify `MyFirebaseMessagingService.kt` was updated correctly
2. Check if you're looking at old logs (clear logcat)
3. Rebuild the app completely
4. Uninstall and reinstall

### Issue 3: Token Not Saved After Login

**Symptoms:**
```
LoginActivity: Login successful
(No FCM token messages)
```

**Solution:**
1. Verify `Login.kt` was updated correctly
2. Check for compilation errors
3. Look for this line in `performLogin()`:
   ```kotlin
   saveFcmTokenAfterLogin()
   ```
4. Rebuild the app

### Issue 4: Token Empty in Firestore

**Symptoms:**
- `fcmToken` field is empty string `""`
- Or field doesn't exist

**Possible Causes:**
1. **No internet connection** - Check device connectivity
2. **Firestore rules blocking write** - Check security rules
3. **Token not generated yet** - Wait a few seconds after login
4. **Code not executed** - Check logcat for errors

**Solution:**
```kotlin
// Check Firestore rules allow token writes:
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

### Issue 5: Imports Not Resolved

**Symptoms:**
```
Unresolved reference: NotificationRepository
Unresolved reference: CoroutineScope
```

**Solution:**
1. Click **File → Sync Project with Gradle Files**
2. Click **File → Invalidate Caches → Invalidate and Restart**
3. Verify imports at top of `Login.kt`:
   ```kotlin
   import com.example.loginandregistration.repository.NotificationRepository
   import kotlinx.coroutines.CoroutineScope
   import kotlinx.coroutines.Dispatchers
   import kotlinx.coroutines.SupervisorJob
   import kotlinx.coroutines.cancelChildren
   import kotlinx.coroutines.launch
   ```

## Success Criteria Checklist

- [ ] App starts without ERROR logs (WARNING is OK)
- [ ] Email/password login saves FCM token
- [ ] Google Sign-In saves FCM token
- [ ] Token appears in Firestore with actual value
- [ ] Token updates on app reinstall
- [ ] Push notifications can be received
- [ ] Logcat shows "FCM token saved successfully after login"
- [ ] No memory leaks or crashes

## Expected Timeline

| Step | Expected Time |
|------|---------------|
| Build app | 2-5 minutes |
| Test app start | 30 seconds |
| Test email login | 1 minute |
| Verify Firestore | 1 minute |
| Test Google login | 2 minutes |
| Test notifications | 2 minutes |
| **Total** | **~10 minutes** |

## Next Steps After Success

1. ✅ Commit the changes to version control
2. ✅ Test on physical device (not just emulator)
3. ✅ Test with different user accounts
4. ✅ Implement push notification handling in app
5. ✅ Set up backend to send notifications using tokens
6. ✅ Monitor Firebase Cloud Messaging delivery reports

## Need More Help?

If issues persist:
1. Share full logcat output (use combined filter)
2. Share screenshot of Firestore user document
3. Verify Firebase project configuration
4. Check `google-services.json` is up to date
5. Verify app package name matches Firebase project
