# User Profile Migration Guide

## Overview

This guide explains how to run the user profile migration script to create profile documents for existing users who don't have them.

## Background

The user profile initialization feature was implemented to automatically create user profile documents in Firestore when users sign in. However, users who authenticated before this feature was implemented may not have profile documents, which can cause issues with chat and other features.

## Migration Script

The migration script is located at:
```
app/src/main/java/com/example/loginandregistration/utils/UserProfileMigration.kt
```

## How to Run the Migration

### Option 1: Using Debug Activity (Recommended)

1. Build and run the app in debug mode
2. Navigate to the Debug Activity (if accessible from your app's menu)
3. Tap the **"Migrate User Profiles"** button (orange button)
4. Wait for the migration to complete
5. Review the results displayed on screen

The migration will:
- Check all existing user documents in Firestore
- Create profiles for the current authenticated user if missing
- Display statistics about profiles created and any errors

### Option 2: Programmatic Usage

You can also run the migration programmatically from any Activity or Fragment:

```kotlin
import androidx.lifecycle.lifecycleScope
import com.example.loginandregistration.utils.UserProfileMigration
import kotlinx.coroutines.launch

class YourActivity : AppCompatActivity() {
    
    private val migration = UserProfileMigration()
    
    fun runMigration() {
        lifecycleScope.launch {
            val result = migration.migrateExistingUsers()
            
            // Handle result
            if (result.success) {
                Log.d("Migration", "Created ${result.profilesCreated} profiles")
            } else {
                Log.e("Migration", "Errors: ${result.errors}")
            }
        }
    }
}
```

## Verification

After running the migration, you can verify that all users have profiles:

### Using Debug Activity

1. In the Debug Activity, tap the **"Verify All Profiles"** button (green button)
2. Review the verification results

### Programmatically

```kotlin
lifecycleScope.launch {
    val (totalUsers, validProfiles) = migration.verifyAllProfilesExist()
    
    if (totalUsers == validProfiles) {
        Log.d("Verification", "All users have valid profiles")
    } else {
        Log.w("Verification", "${totalUsers - validProfiles} users missing profiles")
    }
}
```

## Migration Result

The migration returns a `MigrationResult` object with the following information:

- **totalUsersChecked**: Number of user documents checked
- **profilesCreated**: Number of new profiles created
- **profilesAlreadyExisted**: Number of users who already had profiles
- **errors**: List of any errors encountered
- **success**: Boolean indicating if migration completed without errors

## Important Notes

### Current Limitations

The current implementation:
- Checks existing user documents in the Firestore `users` collection
- Creates a profile for the currently authenticated user if missing
- **Does NOT** iterate through all Firebase Authentication users

### Why This Approach?

This approach is used because:
1. It doesn't require Firebase Admin SDK (which requires server-side code)
2. It works within the app's existing security rules
3. It handles the most common case (current user missing profile)

### For Complete Migration

If you need to migrate ALL authentication users (including those who never had Firestore data):

1. You would need to use Firebase Admin SDK
2. This requires server-side code (Cloud Functions or backend server)
3. Example Cloud Function:

```javascript
const admin = require('firebase-admin');

async function migrateAllUsers() {
  const auth = admin.auth();
  const firestore = admin.firestore();
  
  let nextPageToken;
  let migratedCount = 0;
  
  do {
    const listUsersResult = await auth.listUsers(1000, nextPageToken);
    
    for (const userRecord of listUsersResult.users) {
      const userId = userRecord.uid;
      const userDoc = firestore.collection('users').doc(userId);
      const snapshot = await userDoc.get();
      
      if (!snapshot.exists) {
        await userDoc.set({
          userId: userId,
          displayName: userRecord.displayName || '',
          email: userRecord.email || '',
          photoUrl: userRecord.photoURL || '',
          online: false,
          createdAt: admin.firestore.FieldValue.serverTimestamp(),
          lastActive: admin.firestore.FieldValue.serverTimestamp()
        });
        migratedCount++;
      }
    }
    
    nextPageToken = listUsersResult.pageToken;
  } while (nextPageToken);
  
  console.log(`Migrated ${migratedCount} users`);
}
```

## Automatic Profile Creation

Going forward, profiles are automatically created when users sign in through:

1. **Login Activity**: Creates profile after successful Google Sign-In
2. **MainActivity**: Checks and creates profile on app start if missing
3. **UserProfileRepository**: Provides `ensureUserProfileExists()` method

## Troubleshooting

### Migration Shows 0 Profiles Created

This is normal if:
- All users already have profiles
- The current user already has a profile
- There are no other user documents in Firestore

### Permission Denied Errors

If you see permission denied errors:
1. Check that Firestore security rules allow users to create their own profiles
2. Verify the user is authenticated
3. Check the rules in `firestore.rules`

### Network Errors

If migration fails due to network errors:
1. Check internet connection
2. Verify Firebase project is accessible
3. Try again when connection is stable

## When to Run Migration

Run the migration:
- **Once** after deploying the user profile initialization feature
- After discovering users without profiles
- As part of troubleshooting chat or other profile-dependent features

## Monitoring

After migration, monitor:
- User sign-in success rate
- Chat functionality success rate
- "User not found" errors (should be zero)
- Profile creation errors in logs

## Related Files

- Migration script: `app/src/main/java/com/example/loginandregistration/utils/UserProfileMigration.kt`
- Debug Activity: `app/src/main/java/com/example/loginandregistration/DebugActivity.kt`
- User Profile Repository: `app/src/main/java/com/example/loginandregistration/repository/UserProfileRepository.kt`
- Firestore Rules: `firestore.rules`

## Requirements Addressed

This migration script addresses:
- **Requirement 1.1**: Automatic user profile creation on sign-in
- **Requirement 5.1**: Profile synchronization for existing users
