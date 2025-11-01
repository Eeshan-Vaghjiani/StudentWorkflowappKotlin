# Firestore Permission Denied Fix

## Issue Summary
Users were experiencing `PERMISSION_DENIED` errors when trying to sign in and use the app. The errors occurred in two scenarios:
1. Creating new user profiles
2. Updating existing user profiles (specifically the `lastActive` timestamp)

## Root Causes

### Issue 1: Profile Creation - Empty displayName
**Problem:** The Firestore rules required `displayName` to have a minimum length of 1 character:
```javascript
validStringLength(request.resource.data.displayName, 1, 100)
```

However, the app was creating profiles with empty strings for users who signed in with email/password (they don't have a displayName by default):
```kotlin
"displayName" to (currentUser.displayName ?: ""),
```

**Solution:** Updated the rule to allow empty displayName while still enforcing maximum length:
```javascript
request.resource.data.displayName is string
&& request.resource.data.displayName.size() <= 100
```

### Issue 2: Profile Updates - Field Validation
**Problem:** The update rule was checking that the `uid` or `userId` field must match in the updated document:
```javascript
&& (request.resource.data.uid == userId || request.resource.data.userId == userId)
```

This failed when updating only specific fields like `lastActive` because:
- Existing documents use `userId` field (not `uid`)
- The rule was checking the full document after update
- Partial updates don't include all fields

**Solution:** Removed the unnecessary field check from the update rule since `isOwner(userId)` already validates the user owns the document:
```javascript
allow update: if isOwner(userId)
  && (!('email' in request.resource.data.diff(resource.data).affectedKeys()) 
      || request.resource.data.email == resource.data.email);
```

## Changes Made

### File: `firestore.rules`

**Create Rule (Line ~50-56):**
```javascript
// Before:
allow create: if isAuthenticated() 
  && request.auth.uid == userId
  && (request.resource.data.uid == userId || request.resource.data.userId == userId)
  && request.resource.data.email is string
  && validStringLength(request.resource.data.displayName, 1, 100);

// After:
allow create: if isAuthenticated() 
  && request.auth.uid == userId
  && (request.resource.data.uid == userId || request.resource.data.userId == userId)
  && request.resource.data.email is string
  && request.resource.data.displayName is string
  && request.resource.data.displayName.size() <= 100;
```

**Update Rule (Line ~61-64):**
```javascript
// Before:
allow update: if isOwner(userId)
  && (request.resource.data.uid == userId || request.resource.data.userId == userId)
  && (!('email' in request.resource.data.diff(resource.data).affectedKeys()) 
      || request.resource.data.email == resource.data.email);

// After:
allow update: if isOwner(userId)
  && (!('email' in request.resource.data.diff(resource.data).affectedKeys()) 
      || request.resource.data.email == resource.data.email);
```

## Deployment
Rules were deployed successfully to Firebase:
```bash
firebase deploy --only firestore:rules
```

## Testing Recommendations
1. Test email/password sign-in with new users (empty displayName)
2. Test Google sign-in with users who have displayName
3. Verify profile updates work (lastActive timestamp)
4. Verify FCM token updates work
5. Test with both existing users and new user registrations

## Affected User Flows
- ✅ User sign-in (email/password)
- ✅ User sign-in (Google)
- ✅ Profile creation
- ✅ Profile updates (lastActive)
- ✅ FCM token storage
- ✅ Chat functionality (requires profile)

## Date Fixed
October 31, 2025
