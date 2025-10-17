# Firebase Setup Guide for TeamSync Collaboration App

## Table of Contents
1. [Overview](#overview)
2. [Current Firebase Project Analysis](#current-firebase-project-analysis)
3. [Prerequisites](#prerequisites)
4. [Step-by-Step Setup Guide](#step-by-step-setup-guide)
5. [Firestore vs Realtime Database](#firestore-vs-realtime-database)
6. [Configuration Files Reference](#configuration-files-reference)
7. [Verification Checklist](#verification-checklist)
8. [Troubleshooting](#troubleshooting)

---

## Overview

This guide provides comprehensive instructions for setting up a fresh Firebase project for the TeamSync Collaboration Android app. The app uses multiple Firebase services including Authentication, Firestore, Storage, Cloud Messaging (FCM), Analytics, and Crashlytics.

**Why a fresh setup?** Starting with a clean Firebase project ensures:
- Correct package name configuration (`com.teamsync.collaboration`)
- Proper security rules from the start
- Clean data structure without legacy issues
- Correct OAuth client configuration for Google Sign-In

---

## Current Firebase Project Analysis

### Existing Configuration
- **Project ID**: `android-logreg`
- **Project Number**: `52796977485`
- **Storage Bucket**: `android-logreg.firebasestorage.app`
- **Package Names**: 
  - Legacy: `com.example.loginandregistration`
  - Current: `com.teamsync.collaboration`

### Firebase Services in Use
1. **Authentication** - Google Sign-In, Email/Password
2. **Firestore Database** - Primary data storage
3. **Firebase Storage** - Profile pictures and chat attachments
4. **Cloud Messaging (FCM)** - Push notifications
5. **Analytics** - User behavior tracking
6. **Crashlytics** - Crash reporting


---

## Prerequisites

Before starting, ensure you have:

1. **Google Account** - For Firebase Console access
2. **Android Studio** - Latest stable version installed
3. **Firebase CLI** - For deploying security rules
4. **Git** - For version control
5. **SHA-1 Certificate** - For Google Sign-In configuration

### Installing Firebase CLI

```bash
# Using npm (Node.js required)
npm install -g firebase-tools

# Verify installation
firebase --version
```

### Getting Your SHA-1 Certificate

```bash
# For debug builds (development)
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android

# For Windows
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android

# For release builds (production)
keytool -list -v -keystore /path/to/your/release.keystore -alias your-key-alias
```

Copy the SHA-1 fingerprint - you'll need it for Google Sign-In setup.


---

## Step-by-Step Setup Guide

### Step 1: Create New Firebase Project

1. Navigate to [Firebase Console](https://console.firebase.google.com/)
2. Click **"Add project"** or **"Create a project"**
3. Enter project details:
   - **Project name**: `TeamSync-Collaboration` (or your preferred name)
   - **Project ID**: Will be auto-generated (e.g., `teamsync-collaboration-abc123`)
4. **Google Analytics**: 
   - Toggle ON (recommended for production)
   - Select or create Analytics account
   - Accept terms and conditions
5. Click **"Create project"**
6. Wait for project creation (30-60 seconds)
7. Click **"Continue"** when ready

### Step 2: Add Android App to Firebase Project

1. In Firebase Console, click the **Android icon** (or "Add app" → Android)
2. Enter app details:
   - **Android package name**: `com.teamsync.collaboration`
   - **App nickname**: `TeamSync Android` (optional but recommended)
   - **Debug signing certificate SHA-1**: Paste your SHA-1 from prerequisites
3. Click **"Register app"**
4. **Download `google-services.json`**:
   - Click "Download google-services.json"
   - Save the file (you'll place it in your project later)
5. Click **"Next"** through the SDK setup instructions (we'll handle this separately)
6. Click **"Continue to console"**


### Step 3: Enable Firebase Authentication

1. In Firebase Console, navigate to **Build** → **Authentication**
2. Click **"Get started"**
3. Go to **"Sign-in method"** tab

#### Enable Email/Password Authentication
1. Click on **"Email/Password"**
2. Toggle **"Enable"** to ON
3. Leave **"Email link (passwordless sign-in)"** OFF (unless needed)
4. Click **"Save"**

#### Enable Google Sign-In
1. Click on **"Google"**
2. Toggle **"Enable"** to ON
3. **Project support email**: Select your email from dropdown
4. Click **"Save"**
5. **Important**: Expand the "Web SDK configuration" section
6. Copy the **Web client ID** - you'll need this for Google Sign-In in the app

#### Add Additional SHA-1 Certificates (if needed)
1. Go to **Project Settings** (gear icon) → **Your apps**
2. Select your Android app
3. Scroll to **"SHA certificate fingerprints"**
4. Click **"Add fingerprint"**
5. Add SHA-1 for:
   - Debug keystore (for development)
   - Release keystore (for production)
   - CI/CD keystore (if using automated builds)


### Step 4: Setup Firestore Database

1. In Firebase Console, navigate to **Build** → **Firestore Database**
2. Click **"Create database"**
3. **Choose starting mode**:
   - Select **"Start in production mode"** (we'll deploy custom rules)
   - Click **"Next"**
4. **Select location**:
   - Choose a region closest to your users (e.g., `us-central1`, `europe-west1`, `asia-southeast1`)
   - **Important**: This cannot be changed later
   - Click **"Enable"**
5. Wait for database creation (30-60 seconds)

#### Deploy Firestore Security Rules

Your project already has security rules defined in `firestore.rules`. Deploy them:

```bash
# Navigate to your project root directory
cd /path/to/your/project

# Login to Firebase (if not already logged in)
firebase login

# Initialize Firebase in your project (if not already done)
firebase init

# Select:
# - Firestore: Configure security rules and indexes files
# - Storage: Configure security rules file
# Use existing files when prompted

# Deploy Firestore rules
firebase deploy --only firestore:rules

# Or deploy all rules at once
firebase deploy --only firestore:rules,storage:rules
```

#### Create Firestore Indexes (if needed)

The app currently uses `firestore.indexes.json` which is empty. If you encounter index errors during testing:

1. Firestore will provide a direct link in the error message
2. Click the link to auto-create the index
3. Or manually create indexes in Firebase Console → Firestore → Indexes


### Step 5: Setup Firebase Storage

1. In Firebase Console, navigate to **Build** → **Storage**
2. Click **"Get started"**
3. **Security rules**:
   - Select **"Start in production mode"** (we'll deploy custom rules)
   - Click **"Next"**
4. **Storage location**:
   - Should match your Firestore location
   - Click **"Done"**
5. Wait for storage initialization

#### Deploy Storage Security Rules

```bash
# Deploy storage rules
firebase deploy --only storage:rules
```

Your `storage.rules` file defines:
- **Profile pictures**: Users can upload to their own folder (`/profile_pictures/{userId}/`)
- **Chat attachments**: Authenticated users can upload (`/chat_attachments/{chatId}/`)
- **File size limit**: 10MB maximum for all uploads

#### Storage Structure
```
storage/
├── profile_pictures/
│   └── {userId}/
│       └── {imageFileName}.jpg
└── chat_attachments/
    └── {chatId}/
        └── {attachmentFileName}
```


### Step 6: Setup Cloud Messaging (FCM)

1. In Firebase Console, navigate to **Build** → **Cloud Messaging**
2. **No additional setup required** - FCM is automatically enabled
3. Note the **Server Key** (for backend services if needed):
   - Go to **Project Settings** → **Cloud Messaging** tab
   - Copy **Server key** (legacy) or use **Cloud Messaging API**

#### FCM Implementation in App

The app already has FCM configured:
- **Service**: `MyFirebaseMessagingService.kt`
- **Notification channels**: Chat, Tasks, Groups
- **Token management**: Automatic token refresh and storage

#### Test FCM Setup

After deploying the app:
1. Open the app and sign in
2. Check Firestore → `users/{userId}` → verify `fcmToken` field exists
3. Send a test notification from Firebase Console:
   - Go to **Engage** → **Cloud Messaging**
   - Click **"Send your first message"**
   - Enter notification details
   - Select your app
   - Send test message


### Step 7: Enable Firebase Analytics

1. In Firebase Console, navigate to **Engage** → **Analytics**
2. Analytics should be automatically enabled if you selected it during project creation
3. **No additional configuration needed** - the app uses `firebase-analytics-ktx`

### Step 8: Enable Firebase Crashlytics

1. In Firebase Console, navigate to **Operate** → **Crashlytics**
2. Click **"Enable Crashlytics"**
3. The app already has the Crashlytics SDK integrated
4. Crashes will appear after the first app launch

### Step 9: Update Project Configuration Files

Now update your local project with the new Firebase configuration:

#### 9.1 Replace `google-services.json`

```bash
# Backup old file (optional)
cp app/google-services.json app/google-services.json.backup

# Replace with new file downloaded from Firebase Console
# Copy the new google-services.json to app/ directory
```

**Verify the file contains**:
- Correct `package_name`: `com.teamsync.collaboration`
- New `project_id`
- New `storage_bucket`
- OAuth client IDs for Google Sign-In


#### 9.2 Update `local.properties` (if needed)

Add Gemini API key for AI Assistant feature:

```properties
# local.properties
sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk
GEMINI_API_KEY=your_gemini_api_key_here
```

Get Gemini API key from: https://makersuite.google.com/app/apikey

#### 9.3 Initialize Firebase in Project

```bash
# In your project root directory
firebase init

# Select services:
# ◉ Firestore: Configure security rules and indexes files
# ◉ Storage: Configure security rules file

# Use existing files:
# - firestore.rules
# - firestore.indexes.json
# - storage.rules

# Select your Firebase project from the list
# Or create a new project alias
```

This creates `.firebaserc` file:

```json
{
  "projects": {
    "default": "your-project-id"
  }
}
```


### Step 10: Build and Test the App

#### 10.1 Clean and Rebuild

```bash
# Clean the project
./gradlew clean

# For Windows
gradlew.bat clean

# Build the app
./gradlew assembleDebug

# Or build in Android Studio
# Build → Clean Project
# Build → Rebuild Project
```

#### 10.2 Run the App

1. Connect Android device or start emulator
2. Run the app from Android Studio
3. Or use command line:

```bash
./gradlew installDebug
adb shell am start -n com.teamsync.collaboration/.Login
```

#### 10.3 Test Core Features

1. **Authentication**:
   - Sign in with Google
   - Verify user document created in Firestore
   - Check FCM token saved

2. **Firestore**:
   - Create a group
   - Create a task
   - Send a chat message
   - Verify data appears in Firebase Console

3. **Storage**:
   - Upload profile picture
   - Send image in chat
   - Verify files in Storage Console

4. **FCM**:
   - Send test notification from Console
   - Verify notification received on device


---

## Firestore vs Realtime Database

### Why This App Uses Firestore

The TeamSync Collaboration app uses **Cloud Firestore** instead of **Realtime Database**. Here's why:

### Comparison Table

| Feature | Cloud Firestore | Realtime Database |
|---------|----------------|-------------------|
| **Data Model** | Collections & Documents (NoSQL) | JSON Tree |
| **Queries** | Rich queries with compound filters | Limited queries |
| **Scalability** | Automatic, scales better | Manual sharding needed |
| **Offline Support** | Built-in with local cache | Basic offline support |
| **Pricing** | Pay per operation | Pay per bandwidth |
| **Real-time Updates** | Yes, with listeners | Yes, native |
| **Security Rules** | More expressive | Basic rules |
| **Best For** | Complex apps, mobile-first | Simple apps, real-time sync |

### Why Firestore for TeamSync?

1. **Complex Data Structure**:
   - Groups with members, roles, and activities
   - Tasks with assignments and reminders
   - Chats with messages, typing status, and read receipts
   - Firestore's document-collection model fits perfectly

2. **Rich Queries**:
   - Filter tasks by user, date, and status
   - Query groups by member and visibility
   - Search messages by chat and timestamp
   - Firestore supports compound queries natively

3. **Offline Support**:
   - Users can view messages, tasks, and groups offline
   - Changes sync automatically when online
   - Firestore's offline cache is more robust

4. **Scalability**:
   - Automatic scaling as user base grows
   - No manual sharding required
   - Better performance with large datasets

5. **Security**:
   - Granular security rules per collection
   - Helper functions for complex permission checks
   - Subcollection-level security

### When to Use Realtime Database Instead

Consider Realtime Database if:
- Simple data structure (flat JSON)
- Primarily real-time sync (e.g., live cursors, presence)
- Very frequent small updates
- Lower latency requirements
- Bandwidth-based pricing is better for your use case

### Migration Note

If you have existing data in Realtime Database, Firebase provides migration tools. However, for this app, starting fresh with Firestore is recommended.


---

## Configuration Files Reference

### Files That Need Updating

When setting up a new Firebase project, these files need attention:

#### 1. `app/google-services.json` ⚠️ MUST UPDATE

**Location**: `app/google-services.json`

**Action**: Replace entire file with new one from Firebase Console

**Contains**:
- Project ID and number
- API keys
- OAuth client IDs
- Storage bucket URL

**How to get**: Firebase Console → Project Settings → Your apps → Download google-services.json

---

#### 2. `firestore.rules` ✅ ALREADY CONFIGURED

**Location**: `firestore.rules` (project root)

**Action**: Deploy to Firebase (no changes needed)

**Command**:
```bash
firebase deploy --only firestore:rules
```

**Contains**:
- User authentication rules
- Group membership permissions
- Task access controls
- Chat participant permissions
- Typing status rules
- Storage security rules

---

#### 3. `storage.rules` ✅ ALREADY CONFIGURED

**Location**: `storage.rules` (project root)

**Action**: Deploy to Firebase (no changes needed)

**Command**:
```bash
firebase deploy --only storage:rules
```

**Contains**:
- Profile picture upload permissions
- Chat attachment permissions
- File size limits (10MB)


---

#### 4. `firestore.indexes.json` ✅ ALREADY CONFIGURED

**Location**: `firestore.indexes.json` (project root)

**Action**: Deploy if you add custom indexes

**Command**:
```bash
firebase deploy --only firestore:indexes
```

**Current State**: Empty (indexes created automatically as needed)

---

#### 5. `app/build.gradle.kts` ✅ ALREADY CONFIGURED

**Location**: `app/build.gradle.kts`

**Action**: No changes needed (already has all Firebase dependencies)

**Firebase Dependencies**:
```kotlin
// Firebase BOM (manages versions)
implementation(platform("com.google.firebase:firebase-bom:33.5.1"))

// Firebase Services
implementation("com.google.firebase:firebase-auth-ktx")
implementation("com.google.firebase:firebase-firestore-ktx")
implementation("com.google.firebase:firebase-storage-ktx")
implementation("com.google.firebase:firebase-messaging-ktx")
implementation("com.google.firebase:firebase-analytics-ktx")
implementation("com.google.firebase:firebase-crashlytics-ktx")

// Google Sign-In
implementation("com.google.android.gms:play-services-auth:21.2.0")
```

---

#### 6. `build.gradle.kts` (Project Level) ✅ ALREADY CONFIGURED

**Location**: `build.gradle.kts` (project root)

**Action**: No changes needed

**Plugins**:
```kotlin
plugins {
    id("com.google.gms.google-services") version "4.4.3" apply false
    id("com.google.firebase.crashlytics") version "3.0.2" apply false
}
```


---

#### 7. `local.properties` ⚠️ MAY NEED UPDATE

**Location**: `local.properties` (project root, gitignored)

**Action**: Add Gemini API key for AI Assistant

**Format**:
```properties
sdk.dir=/path/to/android/sdk
GEMINI_API_KEY=your_api_key_here
```

**Get API Key**: https://makersuite.google.com/app/apikey

---

#### 8. `AndroidManifest.xml` ✅ ALREADY CONFIGURED

**Location**: `app/src/main/AndroidManifest.xml`

**Action**: No changes needed

**Firebase Components**:
- FCM Service: `MyFirebaseMessagingService`
- Permissions: Internet, notifications, storage
- FileProvider for camera images

---

#### 9. `.firebaserc` ⚠️ WILL BE CREATED

**Location**: `.firebaserc` (project root)

**Action**: Created by `firebase init`

**Format**:
```json
{
  "projects": {
    "default": "your-new-project-id"
  }
}
```

---

### Summary of Required Actions

| File | Action Required | Priority |
|------|----------------|----------|
| `app/google-services.json` | ⚠️ Replace with new file | **CRITICAL** |
| `firestore.rules` | ✅ Deploy to Firebase | **HIGH** |
| `storage.rules` | ✅ Deploy to Firebase | **HIGH** |
| `local.properties` | ⚠️ Add Gemini API key | **MEDIUM** |
| `.firebaserc` | ⚠️ Create via `firebase init` | **MEDIUM** |
| All other files | ✅ No changes needed | - |


---

## Firebase CLI Commands Reference

### Installation and Login

```bash
# Install Firebase CLI
npm install -g firebase-tools

# Login to Firebase
firebase login

# Logout
firebase logout

# Check current user
firebase login:list
```

### Project Initialization

```bash
# Initialize Firebase in project
firebase init

# Initialize specific services
firebase init firestore
firebase init storage

# Use existing project
firebase use --add

# List available projects
firebase projects:list

# Check current project
firebase use
```

### Deploying Rules

```bash
# Deploy Firestore rules only
firebase deploy --only firestore:rules

# Deploy Storage rules only
firebase deploy --only storage:rules

# Deploy Firestore indexes
firebase deploy --only firestore:indexes

# Deploy all rules
firebase deploy --only firestore:rules,storage:rules

# Deploy everything
firebase deploy
```

### Testing and Debugging

```bash
# Test Firestore rules locally
firebase emulators:start --only firestore

# Test Storage rules locally
firebase emulators:start --only storage

# Test all services locally
firebase emulators:start

# View Firestore data in emulator
# Open: http://localhost:4000
```

### Useful Commands

```bash
# View project info
firebase projects:list

# Open Firebase Console
firebase open

# View logs
firebase functions:log

# Check Firebase CLI version
firebase --version

# Update Firebase CLI
npm update -g firebase-tools
```


---

## Verification Checklist

Use this checklist to verify your Firebase setup is complete and working:

### ✅ Firebase Console Setup

- [ ] New Firebase project created
- [ ] Android app added with correct package name (`com.teamsync.collaboration`)
- [ ] SHA-1 certificate added for Google Sign-In
- [ ] `google-services.json` downloaded and placed in `app/` directory

### ✅ Authentication

- [ ] Email/Password authentication enabled
- [ ] Google Sign-In enabled
- [ ] Project support email configured
- [ ] Web client ID noted for Google Sign-In
- [ ] Test sign-in works in app
- [ ] User document created in Firestore after sign-in
- [ ] FCM token saved to user document

### ✅ Firestore Database

- [ ] Firestore database created
- [ ] Region selected (cannot be changed later)
- [ ] Security rules deployed (`firebase deploy --only firestore:rules`)
- [ ] Rules tested with authenticated user
- [ ] Test data created (user, group, task, message)
- [ ] Data visible in Firebase Console

### ✅ Firebase Storage

- [ ] Storage bucket created
- [ ] Security rules deployed (`firebase deploy --only storage:rules`)
- [ ] Profile picture upload tested
- [ ] Chat attachment upload tested
- [ ] Files visible in Storage Console
- [ ] File size limit (10MB) enforced

### ✅ Cloud Messaging (FCM)

- [ ] FCM automatically enabled (no action needed)
- [ ] App receives FCM token on launch
- [ ] Token saved to Firestore user document
- [ ] Test notification sent from Console
- [ ] Notification received on device
- [ ] Notification channels working (Chat, Tasks, Groups)


### ✅ Analytics & Crashlytics

- [ ] Firebase Analytics enabled
- [ ] Crashlytics enabled
- [ ] App launched at least once
- [ ] Analytics events visible in Console (may take 24 hours)
- [ ] Test crash reported (if needed)

### ✅ Project Configuration

- [ ] `google-services.json` replaced with new file
- [ ] `local.properties` contains Gemini API key
- [ ] `.firebaserc` created with correct project ID
- [ ] Firebase CLI installed and logged in
- [ ] All security rules deployed successfully

### ✅ App Functionality

- [ ] App builds without errors
- [ ] Google Sign-In works
- [ ] User can create groups
- [ ] User can create tasks
- [ ] User can send chat messages
- [ ] User can upload profile picture
- [ ] User can send image attachments
- [ ] AI Assistant works (if Gemini API key configured)
- [ ] Notifications received
- [ ] Offline mode works (data cached locally)

### ✅ Security

- [ ] Firestore rules prevent unauthorized access
- [ ] Storage rules prevent unauthorized uploads
- [ ] Users can only access their own data
- [ ] Group members can only access their groups
- [ ] Chat participants can only access their chats
- [ ] API keys not exposed in code (use BuildConfig)

### ✅ Production Readiness (Optional)

- [ ] Release keystore created
- [ ] Release SHA-1 added to Firebase
- [ ] ProGuard rules configured
- [ ] App signed with release key
- [ ] Release build tested
- [ ] Performance monitoring enabled
- [ ] Error reporting configured


---

## Troubleshooting

### Common Issues and Solutions

#### 1. Google Sign-In Fails with "Developer Error" or "Error 10"

**Symptoms**:
- Sign-in dialog doesn't appear
- Error: "Developer error" or "Error 10"
- Logcat shows: "Status{statusCode=DEVELOPER_ERROR}"

**Causes**:
- SHA-1 certificate not added to Firebase
- Wrong package name in `google-services.json`
- OAuth client not configured

**Solutions**:
```bash
# 1. Get your SHA-1 certificate
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android

# 2. Add SHA-1 to Firebase Console
# Project Settings → Your apps → SHA certificate fingerprints → Add fingerprint

# 3. Download new google-services.json
# Replace app/google-services.json with new file

# 4. Clean and rebuild
./gradlew clean
./gradlew assembleDebug

# 5. Uninstall and reinstall app
adb uninstall com.teamsync.collaboration
./gradlew installDebug
```

---

#### 2. Firestore Permission Denied Errors

**Symptoms**:
- Error: "PERMISSION_DENIED: Missing or insufficient permissions"
- Data not loading in app
- Writes failing silently

**Causes**:
- Security rules not deployed
- Rules using wrong field names
- User not authenticated

**Solutions**:
```bash
# 1. Verify rules are deployed
firebase deploy --only firestore:rules

# 2. Check rules in Firebase Console
# Firestore → Rules tab → Verify rules match firestore.rules file

# 3. Test rules in Console
# Firestore → Rules → Rules Playground
# Simulate read/write with authenticated user

# 4. Check user authentication in app
# Verify Firebase.auth.currentUser is not null

# 5. Enable debug logging
FirebaseFirestore.setLoggingEnabled(true)
```


---

#### 3. Storage Upload Fails

**Symptoms**:
- Images not uploading
- Error: "User does not have permission to access this object"
- Upload progress stuck at 0%

**Causes**:
- Storage rules not deployed
- User not authenticated
- File size exceeds limit (10MB)

**Solutions**:
```bash
# 1. Deploy storage rules
firebase deploy --only storage:rules

# 2. Verify rules in Firebase Console
# Storage → Rules tab

# 3. Check file size
# Max 10MB per file (defined in storage.rules)

# 4. Verify user authentication
# Check Firebase.auth.currentUser is not null

# 5. Check storage bucket URL
# Verify google-services.json has correct storage_bucket
```

---

#### 4. FCM Token Not Saved

**Symptoms**:
- Notifications not received
- `fcmToken` field missing in user document
- Logcat shows: "FCM token not saved"

**Causes**:
- User not authenticated when token generated
- Firestore permission error
- Network connectivity issue

**Solutions**:
```kotlin
// 1. Manually trigger token save after sign-in
// In LoginActivity after successful sign-in:
lifecycleScope.launch {
    val result = notificationRepository.saveFcmToken()
    if (result.isFailure) {
        Log.e("Login", "Failed to save FCM token", result.exceptionOrNull())
    }
}

// 2. Check Firestore rules allow token write
// users/{userId} should allow write if isOwner(userId)

// 3. Verify FCM service in AndroidManifest.xml
// Should have MyFirebaseMessagingService registered
```


---

#### 5. App Crashes on Launch

**Symptoms**:
- App crashes immediately after launch
- Error: "Default FirebaseApp is not initialized"
- Error: "google-services.json is missing"

**Causes**:
- `google-services.json` not in `app/` directory
- Google Services plugin not applied
- Build configuration error

**Solutions**:
```bash
# 1. Verify google-services.json location
# Must be in: app/google-services.json (not app/src/)

# 2. Verify plugin in app/build.gradle.kts
# Should have: id("com.google.gms.google-services")

# 3. Clean and rebuild
./gradlew clean
./gradlew assembleDebug

# 4. Check for multiple google-services.json files
find . -name "google-services.json"
# Should only be one in app/ directory

# 5. Verify package name matches
# google-services.json package_name should be: com.teamsync.collaboration
```

---

#### 6. Firebase CLI Commands Fail

**Symptoms**:
- `firebase deploy` fails
- Error: "Not authorized"
- Error: "No project active"

**Causes**:
- Not logged in to Firebase CLI
- Wrong project selected
- `.firebaserc` missing or incorrect

**Solutions**:
```bash
# 1. Login to Firebase
firebase login

# 2. List available projects
firebase projects:list

# 3. Select correct project
firebase use your-project-id

# 4. Verify .firebaserc exists
cat .firebaserc

# 5. Re-initialize if needed
firebase init

# 6. Check Firebase CLI version
firebase --version
# Update if outdated: npm update -g firebase-tools
```


---

#### 7. Gradle Build Fails

**Symptoms**:
- Build error: "Could not resolve com.google.firebase:firebase-bom"
- Error: "Plugin with id 'com.google.gms.google-services' not found"
- Sync fails

**Causes**:
- Missing dependencies
- Wrong plugin version
- Gradle cache corruption

**Solutions**:
```bash
# 1. Verify internet connection
ping google.com

# 2. Clear Gradle cache
./gradlew clean
rm -rf ~/.gradle/caches/

# 3. Invalidate caches in Android Studio
# File → Invalidate Caches → Invalidate and Restart

# 4. Verify plugin versions in build.gradle.kts
# Check: com.google.gms.google-services version matches libs.versions.toml

# 5. Sync project with Gradle files
# File → Sync Project with Gradle Files

# 6. Update Gradle wrapper (if needed)
./gradlew wrapper --gradle-version=8.12.3
```

---

#### 8. Data Not Syncing in Real-Time

**Symptoms**:
- Changes in Firestore Console don't appear in app
- Messages sent don't appear immediately
- Need to restart app to see updates

**Causes**:
- Firestore listeners not attached
- Offline persistence issues
- Network connectivity problems

**Solutions**:
```kotlin
// 1. Verify listeners are attached
// Check ViewModels use .addSnapshotListener() not .get()

// 2. Enable Firestore logging
FirebaseFirestore.setLoggingEnabled(true)

// 3. Check offline persistence
val settings = firestoreSettings {
    isPersistenceEnabled = true
}
firestore.firestoreSettings = settings

// 4. Verify network connectivity
// Check device has internet connection

// 5. Check listener lifecycle
// Ensure listeners are not removed prematurely
```


---

#### 9. Gemini AI Assistant Not Working

**Symptoms**:
- AI responses not generating
- Error: "API key not valid"
- Network timeout errors

**Causes**:
- Missing or invalid Gemini API key
- API key not in `local.properties`
- Network connectivity issues
- API quota exceeded

**Solutions**:
```bash
# 1. Get Gemini API key
# Visit: https://makersuite.google.com/app/apikey

# 2. Add to local.properties
echo "GEMINI_API_KEY=your_api_key_here" >> local.properties

# 3. Clean and rebuild
./gradlew clean
./gradlew assembleDebug

# 4. Verify BuildConfig contains key
# Check: BuildConfig.GEMINI_API_KEY is not empty

# 5. Check API quota
# Visit: https://console.cloud.google.com/apis/api/generativelanguage.googleapis.com/quotas

# 6. Test API directly
curl "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=YOUR_API_KEY" \
  -H 'Content-Type: application/json' \
  -d '{"contents":[{"parts":[{"text":"Hello"}]}]}'
```

---

#### 10. Package Name Mismatch

**Symptoms**:
- Error: "Package name mismatch"
- Google Sign-In fails
- Firebase services not working

**Causes**:
- `google-services.json` has wrong package name
- `applicationId` in `build.gradle.kts` doesn't match
- Multiple package names in project

**Solutions**:
```bash
# 1. Verify package name in google-services.json
# Should be: "package_name": "com.teamsync.collaboration"

# 2. Verify applicationId in app/build.gradle.kts
# Should be: applicationId = "com.teamsync.collaboration"

# 3. Verify namespace in app/build.gradle.kts
# Should be: namespace = "com.example.loginandregistration"
# (namespace is for code, applicationId is for app identity)

# 4. If mismatch, download new google-services.json
# Firebase Console → Project Settings → Your apps → Download

# 5. Clean and rebuild
./gradlew clean
./gradlew assembleDebug
```


---

### Getting Help

If you encounter issues not covered here:

1. **Check Firebase Status**: https://status.firebase.google.com/
2. **Firebase Documentation**: https://firebase.google.com/docs
3. **Stack Overflow**: Tag questions with `firebase`, `android`, `kotlin`
4. **Firebase Support**: https://firebase.google.com/support
5. **GitHub Issues**: Check the project repository for known issues

### Useful Debugging Commands

```bash
# View Android logs
adb logcat | grep -i firebase

# View app logs only
adb logcat | grep com.teamsync.collaboration

# Clear app data
adb shell pm clear com.teamsync.collaboration

# Check app permissions
adb shell dumpsys package com.teamsync.collaboration | grep permission

# View network traffic
adb shell tcpdump -i any -s 0 -w /sdcard/capture.pcap

# Check Firebase SDK versions
./gradlew app:dependencies | grep firebase
```

---

## Quick Reference Card

### Essential Commands

```bash
# Setup
firebase login
firebase init
firebase use your-project-id

# Deploy
firebase deploy --only firestore:rules,storage:rules

# Build
./gradlew clean assembleDebug

# Install
./gradlew installDebug

# Logs
adb logcat | grep -i firebase
```

### Essential Files

- `app/google-services.json` - Firebase configuration
- `firestore.rules` - Database security rules
- `storage.rules` - Storage security rules
- `local.properties` - API keys (gitignored)
- `.firebaserc` - Firebase project ID

### Essential URLs

- Firebase Console: https://console.firebase.google.com/
- Gemini API Keys: https://makersuite.google.com/app/apikey
- Firebase Status: https://status.firebase.google.com/
- Firebase Docs: https://firebase.google.com/docs

---

## Conclusion

You now have a complete Firebase setup for the TeamSync Collaboration app! 

**Next Steps**:
1. Complete the verification checklist
2. Test all features thoroughly
3. Monitor Firebase Console for errors
4. Set up production environment when ready
5. Configure release signing for Play Store

**Remember**:
- Keep `google-services.json` secure (don't commit to public repos)
- Keep API keys in `local.properties` (gitignored)
- Monitor Firebase usage and quotas
- Set up billing alerts for production
- Regularly backup Firestore data

Good luck with your app! 🚀

