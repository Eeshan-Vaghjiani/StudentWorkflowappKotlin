# Task 9: Google Sign-In Flow - Visual Diagram

## Complete Sign-In Flow

```
┌─────────────────────────────────────────────────────────────────────┐
│                         USER INITIATES SIGN-IN                       │
│                    (Taps "Sign in with Google")                      │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         SHOW LOADING STATE                           │
│                  - Display loading overlay                           │
│                  - Disable sign-in buttons                           │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    LAUNCH GOOGLE SIGN-IN INTENT                      │
│              googleSignInHelper.getSignInIntent()                    │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                                 ▼
                    ┌────────────┴────────────┐
                    │                         │
                    ▼                         ▼
        ┌───────────────────┐    ┌───────────────────┐
        │  USER SELECTS     │    │  USER CANCELS     │
        │  GOOGLE ACCOUNT   │    │  SIGN-IN          │
        └─────────┬─────────┘    └─────────┬─────────┘
                  │                         │
                  │                         ▼
                  │              ┌───────────────────┐
                  │              │ RESULT_CANCELED   │
                  │              │ - Hide loading    │
                  │              │ - Log cancellation│
                  │              │ - NO error shown  │
                  │              │ - Stay on login   │
                  │              └───────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                   GOOGLE SIGN-IN RESULT RECEIVED                     │
│              handleSignInResult(data: Intent?)                       │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                    ┌────────────┴────────────┐
                    │                         │
                    ▼                         ▼
        ┌───────────────────┐    ┌───────────────────┐
        │    SUCCESS        │    │     FAILURE       │
        │ GoogleSignInAccount│    │  ApiException     │
        └─────────┬─────────┘    └─────────┬─────────┘
                  │                         │
                  │                         ▼
                  │              ┌───────────────────┐
                  │              │ handleGoogleSign  │
                  │              │ InError()         │
                  │              │ - Parse error code│
                  │              │ - Show user-      │
                  │              │   friendly message│
                  │              │ - Hide loading    │
                  │              └───────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                  AUTHENTICATE WITH FIREBASE                          │
│         googleSignInHelper.authenticateWithFirebase()                │
│         - Get Google credential                                      │
│         - Sign in with Firebase Auth                                 │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                    ┌────────────┴────────────┐
                    │                         │
                    ▼                         ▼
        ┌───────────────────┐    ┌───────────────────┐
        │    SUCCESS        │    │     FAILURE       │
        │  Firebase User    │    │  Exception        │
        └─────────┬─────────┘    └─────────┬─────────┘
                  │                         │
                  │                         ▼
                  │              ┌───────────────────┐
                  │              │ Show error message│
                  │              │ Hide loading      │
                  │              │ Stay on login     │
                  │              └───────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                  CREATE/UPDATE USER IN FIRESTORE                     │
│              createOrUpdateUserInFirestore()                         │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                                 ▼
                    ┌────────────┴────────────┐
                    │                         │
                    ▼                         ▼
        ┌───────────────────┐    ┌───────────────────┐
        │  USER EXISTS      │    │  NEW USER         │
        │  - Update fields  │    │  - Create document│
        │  - displayName    │    │  - All required   │
        │  - photoUrl       │    │    fields         │
        │  - lastActive     │    │  - Initialize     │
        │  - isOnline       │    │    defaults       │
        └─────────┬─────────┘    └─────────┬─────────┘
                  │                         │
                  └────────────┬────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                       SAVE FCM TOKEN                                 │
│                  saveFcmTokenAfterLogin()                            │
│         - Get FCM token from Firebase Messaging                      │
│         - Update user document with token                            │
│         - Non-blocking (continues on failure)                        │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                    ┌────────────┴────────────┐
                    │                         │
                    ▼                         ▼
        ┌───────────────────┐    ┌───────────────────┐
        │    SUCCESS        │    │     FAILURE       │
        │ - Log success     │    │ - Log warning     │
        │ - Token saved     │    │ - Continue anyway │
        └─────────┬─────────┘    └─────────┬─────────┘
                  │                         │
                  └────────────┬────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                       SHOW SUCCESS MESSAGE                           │
│                    "Welcome back!"                                   │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│                       NAVIGATE TO DASHBOARD                          │
│                  - Hide loading overlay                              │
│                  - Clear activity stack                              │
│                  - Start MainActivity                                │
└─────────────────────────────────────────────────────────────────────┘
```

## Error Handling Flow

```
┌─────────────────────────────────────────────────────────────────────┐
│                         ERROR OCCURS                                 │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                    ┌────────────┴────────────┐
                    │                         │
                    ▼                         ▼
        ┌───────────────────┐    ┌───────────────────┐
        │  GOOGLE SIGN-IN   │    │  FIREBASE AUTH    │
        │  ERROR            │    │  ERROR            │
        └─────────┬─────────┘    └─────────┬─────────┘
                  │                         │
                  ▼                         ▼
        ┌───────────────────┐    ┌───────────────────┐
        │ Parse Error Code: │    │ Parse Exception:  │
        │ - 12501: Cancelled│    │ - Network error   │
        │ - 12500: Config   │    │ - Auth error      │
        │ - 7: Network      │    │ - Permission error│
        │ - 10: Developer   │    │ - Unknown error   │
        │ - Other: Generic  │    │                   │
        └─────────┬─────────┘    └─────────┬─────────┘
                  │                         │
                  └────────────┬────────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │  Is Cancellation?    │
                    └──────┬───────────┬───┘
                           │           │
                      YES  │           │  NO
                           ▼           ▼
                ┌──────────────┐  ┌──────────────┐
                │ Silent Handle│  │ Show Error   │
                │ - Log only   │  │ Message      │
                │ - No UI msg  │  │ - User-      │
                │              │  │   friendly   │
                └──────────────┘  └──────┬───────┘
                                         │
                                         ▼
                              ┌──────────────────┐
                              │ Hide Loading     │
                              │ Enable Buttons   │
                              │ Stay on Login    │
                              └──────────────────┘
```

## User Document Creation Flow

```
┌─────────────────────────────────────────────────────────────────────┐
│                    CHECK IF USER EXISTS                              │
│              firestore.collection("users")                           │
│                    .document(userId).get()                           │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                    ┌────────────┴────────────┐
                    │                         │
                    ▼                         ▼
        ┌───────────────────┐    ┌───────────────────┐
        │  DOCUMENT EXISTS  │    │  NEW USER         │
        └─────────┬─────────┘    └─────────┬─────────┘
                  │                         │
                  ▼                         ▼
        ┌───────────────────┐    ┌───────────────────┐
        │ UPDATE FIELDS:    │    │ CREATE DOCUMENT:  │
        │ ✓ displayName     │    │ ✓ uid             │
        │ ✓ photoUrl        │    │ ✓ email           │
        │ ✓ profileImageUrl │    │ ✓ displayName     │
        │ ✓ lastActive      │    │ ✓ photoUrl        │
        │ ✓ isOnline        │    │ ✓ profileImageUrl │
        │ ✓ authProvider    │    │ ✓ authProvider    │
        │                   │    │ ✓ createdAt       │
        │ (Preserve other   │    │ ✓ lastActive      │
        │  existing fields) │    │ ✓ isOnline        │
        │                   │    │ ✓ fcmToken        │
        │                   │    │ ✓ aiPromptsUsed   │
        │                   │    │ ✓ aiPromptsLimit  │
        │                   │    │ ✓ bio             │
        │                   │    │ ✓ phoneNumber     │
        │                   │    │ ✓ notifications   │
        │                   │    │   Enabled         │
        │                   │    │ ✓ emailNotif...   │
        │                   │    │ ✓ tasksCompleted  │
        │                   │    │ ✓ groupsJoined    │
        └─────────┬─────────┘    └─────────┬─────────┘
                  │                         │
                  └────────────┬────────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │  Operation Result    │
                    └──────┬───────────┬───┘
                           │           │
                      SUCCESS│         │FAILURE
                           ▼           ▼
                ┌──────────────┐  ┌──────────────┐
                │ Log Success  │  │ Log Error    │
                │ Continue     │  │ Continue     │
                │ (Non-blocking)│  │ (Non-blocking)│
                └──────────────┘  └──────────────┘
```

## FCM Token Saving Flow

```
┌─────────────────────────────────────────────────────────────────────┐
│                    SAVE FCM TOKEN AFTER LOGIN                        │
│                  (Coroutine - Asynchronous)                          │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    GET CURRENT USER ID                               │
│              auth.currentUser?.uid                                   │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                    ┌────────────┴────────────┐
                    │                         │
                    ▼                         ▼
        ┌───────────────────┐    ┌───────────────────┐
        │  USER FOUND       │    │  NO USER          │
        └─────────┬─────────┘    └─────────┬─────────┘
                  │                         │
                  │                         ▼
                  │              ┌───────────────────┐
                  │              │ Return Failure    │
                  │              │ Log Warning       │
                  │              │ Continue Login    │
                  │              └───────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    GET FCM TOKEN                                     │
│              messaging.token.await()                                 │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                    ┌────────────┴────────────┐
                    │                         │
                    ▼                         ▼
        ┌───────────────────┐    ┌───────────────────┐
        │  TOKEN RECEIVED   │    │  TOKEN FAILED     │
        └─────────┬─────────┘    └─────────┬─────────┘
                  │                         │
                  │                         ▼
                  │              ┌───────────────────┐
                  │              │ Log Error         │
                  │              │ Return Failure    │
                  │              │ Continue Login    │
                  │              └───────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    UPDATE USER DOCUMENT                              │
│         firestore.collection("users")                                │
│              .document(userId)                                       │
│              .update("fcmToken", token)                              │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                    ┌────────────┴────────────┐
                    │                         │
                    ▼                         ▼
        ┌───────────────────┐    ┌───────────────────┐
        │    SUCCESS        │    │     FAILURE       │
        │ - Log success     │    │ - Log warning     │
        │ - Return true     │    │ - Return false    │
        │ - Continue login  │    │ - Continue login  │
        └───────────────────┘    └───────────────────┘
                  │                         │
                  └────────────┬────────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │  Login Continues     │
                    │  (Non-blocking)      │
                    └──────────────────────┘
```

## State Management

```
┌─────────────────────────────────────────────────────────────────────┐
│                         LOGIN ACTIVITY STATE                         │
└─────────────────────────────────────────────────────────────────────┘

Initial State:
├─ Loading: false
├─ Buttons: enabled
└─ User: null

Sign-In Started:
├─ Loading: true
├─ Buttons: disabled
└─ User: null

Google Account Selected:
├─ Loading: true
├─ Buttons: disabled
└─ User: pending

Firebase Auth Success:
├─ Loading: true (still processing)
├─ Buttons: disabled
└─ User: authenticated

Firestore Update:
├─ Loading: true
├─ Buttons: disabled
└─ User: authenticated + profile data

FCM Token Saved:
├─ Loading: true
├─ Buttons: disabled
└─ User: authenticated + profile + token

Success Complete:
├─ Loading: false
├─ Buttons: enabled
└─ User: fully initialized
    └─ Navigate to Dashboard

Error State:
├─ Loading: false
├─ Buttons: enabled
└─ User: null
    └─ Show error message
    └─ Stay on login screen

Cancelled State:
├─ Loading: false
├─ Buttons: enabled
└─ User: null
    └─ No error message
    └─ Stay on login screen
```

## Component Interaction

```
┌──────────────────┐
│  Login Activity  │
└────────┬─────────┘
         │
         ├─────────────────────────────────┐
         │                                 │
         ▼                                 ▼
┌──────────────────┐            ┌──────────────────┐
│ GoogleSignIn     │            │ ErrorHandler     │
│ Helper           │            │ Utility          │
└────────┬─────────┘            └──────────────────┘
         │
         ├─────────────────────────────────┐
         │                                 │
         ▼                                 ▼
┌──────────────────┐            ┌──────────────────┐
│ Firebase Auth    │            │ Firebase         │
│                  │            │ Firestore        │
└──────────────────┘            └──────────────────┘
         │
         ▼
┌──────────────────┐
│ Notification     │
│ Repository       │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ Firebase         │
│ Messaging        │
└──────────────────┘
```

## Key Features

### ✅ Non-Blocking Operations
- Firestore operations don't block login
- FCM token save doesn't block login
- User can access app even if secondary operations fail

### ✅ Graceful Error Handling
- User-friendly error messages
- Specific error codes handled
- Cancellation handled silently
- Detailed logging for debugging

### ✅ Complete User Initialization
- All required fields populated
- Smart update vs create logic
- Consistent data structure
- Proper timestamps

### ✅ Asynchronous Processing
- Coroutines for FCM token
- Non-blocking UI
- Proper scope management
- Cancellation support

### ✅ Security
- Proper authentication flow
- Secure token handling
- Firebase security rules
- No sensitive data in logs
