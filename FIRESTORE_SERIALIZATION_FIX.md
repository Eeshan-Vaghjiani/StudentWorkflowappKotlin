# Firestore Serialization Fix - Conflicting Getters

## Issue
App was crashing with: `Found conflicting getters for name isOnline on class com.example.loginandregistration.models.FirebaseUser`

## Root Cause
The `FirebaseUser` model had both `online` and `isOnline` boolean properties. Firestore's serializer automatically generates an `isOnline()` getter for the `online` property (Java bean convention), which conflicted with the explicit `isOnline` property.

## Fix Applied
1. Removed the duplicate `isOnline` property from `FirebaseUser.kt`
2. Updated `UserRepository.kt` to use `online` instead of `isOnline`
3. Kept only the `online` property to match Firestore document structure

## Files Modified
- `app/src/main/java/com/example/loginandregistration/models/FirebaseUser.kt`
- `app/src/main/java/com/example/loginandregistration/repository/UserRepository.kt`

## Testing
Rebuild and run the app. The crash should be resolved and user profile creation should work correctly.
