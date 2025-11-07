# Debug Authentication Token Issue

## The Problem
Even after updating Firestore rules, group creation still fails with `PERMISSION_DENIED`.

## Evidence
1. ✅ User is authenticated (no "User not authenticated" error)
2. ✅ Firestore rules are correct and deployed
3. ✅ Write reaches Firestore (document ID is generated: `S2JTr1Ear2B5AEREEIht`)
4. ❌ Firestore rejects the write with `PERMISSION_DENIED`

## Root Cause
The authentication token is **invalid or expired**. This is confirmed by:
- Multiple permission errors throughout the session
- Error occurs even with correct rules
- Write reaches Firestore but is rejected at the security rules level

## Why This Happens
Firebase authentication tokens:
- Expire after 1 hour by default
- Are cached by the Firebase SDK
- Don't automatically refresh in all scenarios
- Can become invalid if the user's account state changes

## The Solution

### Option 1: Force Token Refresh (Recommended)
Add this code to force a token refresh before creating a group:

```kotlin
// In GroupRepository.kt, at the start of createGroup()
suspend fun createGroup(
    name: String,
    description: String,
    subject: String,
    privacy: String
): Result<String> = withContext(Dispatchers.IO) {
    val startTime = System.currentTimeMillis()
    
    // Force token refresh
    val user = auth.currentUser
        ?: return@withContext Result.failure(Exception("User not authenticated"))
    
    try {
        // Force refresh the ID token
        user.getIdToken(true).await()
        Log.d("GroupRepository", "Token refreshed successfully")
    } catch (e: Exception) {
        Log.e("GroupRepository", "Failed to refresh token", e)
        return@withContext Result.failure(
            Exception("Authentication token refresh failed. Please sign out and back in.")
        )
    }
    
    // ... rest of the function
}
```

### Option 2: Sign Out and Back In (Quick Fix)
1. Open the app
2. Go to Profile/Settings
3. Click "Sign Out"
4. Close the app
5. Reopen and sign in
6. Try creating a group

### Option 3: Check User Profile Exists
The user profile might not exist in Firestore. Check:

1. Open Firebase Console
2. Go to Firestore Database
3. Look for `users` collection
4. Find document with your user ID
5. Verify it has:
   - `uid` or `userId` field
   - `email` field
   - `displayName` field

If the profile doesn't exist, it needs to be created during sign-in.

## Testing the Fix

### Test 1: Check Current User
Add logging to see the current user:
```kotlin
val user = auth.currentUser
Log.d("GroupRepository", "Current user: ${user?.uid}")
Log.d("GroupRepository", "User email: ${user?.email}")
Log.d("GroupRepository", "User displayName: ${user?.displayName}")
```

### Test 2: Check Token
Add logging to see token info:
```kotlin
user.getIdToken(false).await().let { result ->
    Log.d("GroupRepository", "Token: ${result.token?.take(20)}...")
    Log.d("GroupRepository", "Token expiration: ${result.expirationTimestamp}")
}
```

### Test 3: Test Rules in Console
Go to Firebase Console → Firestore → Rules → Rules Playground

Test with:
- **Operation:** Create
- **Location:** `/groups/test123`
- **Authenticated:** Yes
- **User ID:** Your actual user ID
- **Data:**
```json
{
  "name": "Test",
  "description": "Test",
  "subject": "Test",
  "owner": "YOUR_USER_ID",
  "memberIds": ["YOUR_USER_ID"],
  "isActive": true
}
```

## Expected Results

### If Token Refresh Works:
- ✅ Token refreshes successfully
- ✅ Group creation succeeds
- ✅ No more PERMISSION_DENIED errors

### If Token Refresh Fails:
- ❌ Token refresh throws an exception
- → User needs to sign out and back in
- → Possible account issue (disabled, deleted, etc.)

### If User Profile Missing:
- ❌ User profile doesn't exist in Firestore
- → Profile needs to be created during sign-in
- → Check Login.kt and MainActivity.kt for profile creation logic

## Implementation Priority

1. **Immediate:** User should sign out/in (30 seconds)
2. **Short-term:** Add token refresh to createGroup() (5 minutes)
3. **Long-term:** Add automatic token refresh globally (30 minutes)

## Code Changes Needed

### 1. Add Token Refresh to GroupRepository
See Option 1 above

### 2. Add Global Token Refresh
Create a TokenManager class:
```kotlin
object TokenManager {
    suspend fun ensureValidToken(): Boolean {
        val user = FirebaseAuth.getInstance().currentUser ?: return false
        return try {
            user.getIdToken(true).await()
            true
        } catch (e: Exception) {
            Log.e("TokenManager", "Token refresh failed", e)
            false
        }
    }
}
```

### 3. Use Before Firestore Operations
```kotlin
if (!TokenManager.ensureValidToken()) {
    return Result.failure(Exception("Please sign out and back in"))
}
```

## Status
- ❌ Token refresh not implemented yet
- ⏳ User needs to sign out/in manually
- ⏳ Need to verify user profile exists
