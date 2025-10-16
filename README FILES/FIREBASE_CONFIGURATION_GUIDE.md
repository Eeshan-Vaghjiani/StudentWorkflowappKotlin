# Firebase Configuration Guide

Complete step-by-step guide to configure Firebase for the Team Collaboration App.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Create Firebase Project](#create-firebase-project)
3. [Add Android App](#add-android-app)
4. [Configure Authentication](#configure-authentication)
5. [Setup Firestore Database](#setup-firestore-database)
6. [Deploy Security Rules](#deploy-security-rules)
7. [Create Firestore Indexes](#create-firestore-indexes)
8. [Enable Cloud Messaging](#enable-cloud-messaging)
9. [Verify Configuration](#verify-configuration)
10. [Troubleshooting](#troubleshooting)

## Prerequisites

Before starting, ensure you have:

- A Google account
- Android Studio installed
- The Team Collaboration App project open in Android Studio
- Internet connection

## Create Firebase Project

### Step 1: Access Firebase Console

1. Open your web browser
2. Navigate to [https://console.firebase.google.com/](https://console.firebase.google.com/)
3. Sign in with your Google account

### Step 2: Create New Project

1. Click the **"Add project"** button (or **"Create a project"**)
2. Enter project details:
   - **Project name**: `Team Collaboration App` (or your preferred name)
   - Click **"Continue"**

3. Google Analytics (Optional):
   - Toggle **"Enable Google Analytics for this project"** (recommended for production)
   - Click **"Continue"**

4. Configure Google Analytics (if enabled):
   - Select or create an Analytics account
   - Accept terms and conditions
   - Click **"Create project"**

5. Wait for project creation (30-60 seconds)
6. Click **"Continue"** when ready

## Add Android App

### Step 1: Register Android App

1. In the Firebase Console, click the **Android icon** to add an Android app
2. Fill in the registration form:
   - **Android package name**: `com.example.loginandregistration`
     - ⚠️ **Important**: This must match exactly!
     - Find it in `app/build.gradle.kts` under `applicationId`
   - **App nickname** (optional): `Team Collaboration App`
   - **Debug signing certificate SHA-1** (optional): Leave blank for now
3. Click **"Register app"**

### Step 2: Download Configuration File

1. Click **"Download google-services.json"**
2. Save the file to your computer
3. In Android Studio:
   - Switch to **Project** view (dropdown at top left)
   - Navigate to `app/` directory
   - Drag and drop `google-services.json` into the `app/` folder
   - Confirm the file is at: `app/google-services.json`

### Step 3: Add Firebase SDK

1. The project already has Firebase dependencies configured
2. Verify in `app/build.gradle.kts`:
   ```kotlin
   plugins {
       id("com.google.gms.google-services")
   }
   
   dependencies {
       implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
       implementation("com.google.firebase:firebase-auth-ktx")
       implementation("com.google.firebase:firebase-firestore-ktx")
       implementation("com.google.firebase:firebase-messaging-ktx")
   }
   ```

3. Sync Gradle files:
   - Click **"Sync Now"** in the banner at the top
   - Or: File → Sync Project with Gradle Files

4. Click **"Continue to console"** in Firebase setup wizard

## Configure Authentication

### Step 1: Enable Authentication

1. In Firebase Console, click **"Authentication"** in the left sidebar
2. Click **"Get started"** button
3. You'll see the "Sign-in method" tab

### Step 2: Enable Email/Password Provider

1. Click on **"Email/Password"** in the providers list
2. Toggle **"Enable"** to ON
3. Leave **"Email link (passwordless sign-in)"** OFF
4. Click **"Save"**

### Step 3: (Optional) Add Test Users

1. Click the **"Users"** tab
2. Click **"Add user"**
3. Enter test email and password
4. Click **"Add user"**

### Verification

- You should see "Email/Password" with a green checkmark in the providers list
- Status should show "Enabled"

## Setup Firestore Database

### Step 1: Create Database

1. In Firebase Console, click **"Firestore Database"** in the left sidebar
2. Click **"Create database"** button

### Step 2: Choose Security Rules

1. Select **"Start in test mode"**
   - ⚠️ **Note**: We'll add proper security rules in the next section
   - Test mode allows all reads/writes for 30 days
2. Click **"Next"**

### Step 3: Select Location

1. Choose a Cloud Firestore location:
   - Select the region closest to your users
   - Recommended: `us-central` (United States) or `europe-west` (Europe)
   - ⚠️ **Important**: Location cannot be changed later!
2. Click **"Enable"**

### Step 4: Wait for Database Creation

- Database creation takes 1-2 minutes
- You'll see a loading screen
- Once complete, you'll see the Firestore console

### Verification

- You should see an empty Firestore database
- Collections will be created automatically when the app runs

## Deploy Security Rules

### Step 1: Locate Rules File

1. In Android Studio, open `firestore.rules` in the project root
2. Review the security rules (already configured)

### Step 2: Deploy Rules to Firebase

1. In Firebase Console, go to **"Firestore Database"**
2. Click the **"Rules"** tab
3. You'll see the default test mode rules

4. Replace with production rules:
   - Delete all existing rules
   - Copy the entire contents of `firestore.rules` from your project
   - Paste into the Firebase Console editor

5. Click **"Publish"**

### Step 3: Verify Rules

The rules should include:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Helper functions
    function isAuthenticated() {
      return request.auth != null;
    }
    
    function isOwner(userId) {
      return request.auth.uid == userId;
    }
    
    // ... (rest of the rules)
  }
}
```

### Important Rules Explained

- **Users**: Users can read all profiles but only write their own
- **Groups**: Only group members can read, only admins can modify
- **Tasks**: Only assigned users and creator can access
- **Chats**: Only participants can read/write
- **Messages**: Only participants can read, only sender can delete

## Create Firestore Indexes

### Why Indexes Are Needed

Firestore requires indexes for complex queries (multiple fields, array-contains with ordering).

### Step 1: Locate Indexes File

1. In Android Studio, open `firestore.indexes.json` in the project root
2. Review the required indexes

### Step 2: Create Indexes Manually

#### Index 1: Messages by Chat and Time

1. In Firebase Console, go to **"Firestore Database"** → **"Indexes"** tab
2. Click **"Create index"**
3. Configure:
   - **Collection ID**: `messages`
   - **Fields to index**:
     - Field: `chatId`, Order: `Ascending`
     - Field: `timestamp`, Order: `Descending`
   - **Query scope**: `Collection`
4. Click **"Create index"**
5. Wait 2-5 minutes for index to build

#### Index 2: Tasks by Assignment and Due Date

1. Click **"Create index"** again
2. Configure:
   - **Collection ID**: `tasks`
   - **Fields to index**:
     - Field: `assignedTo`, Order: `Array-contains`
     - Field: `dueDate`, Order: `Ascending`
   - **Query scope**: `Collection`
3. Click **"Create index"**
4. Wait 2-5 minutes for index to build

#### Index 3: Chats by Participants and Time

1. Click **"Create index"** again
2. Configure:
   - **Collection ID**: `chats`
   - **Fields to index**:
     - Field: `participants`, Order: `Array-contains`
     - Field: `lastMessageTime`, Order: `Descending`
   - **Query scope**: `Collection`
3. Click **"Create index"**
4. Wait 2-5 minutes for index to build

### Step 3: Verify Indexes

- All indexes should show status: **"Enabled"** (green)
- If status is "Building", wait a few more minutes
- If status is "Error", delete and recreate the index

### Alternative: Deploy via Firebase CLI

If you have Firebase CLI installed:

```bash
firebase deploy --only firestore:indexes
```

## Enable Cloud Messaging

### Step 1: Access Cloud Messaging

1. In Firebase Console, click **"Cloud Messaging"** in the left sidebar
2. Cloud Messaging is enabled by default (no action needed)

### Step 2: Verify Configuration

1. You should see the **"Server key"** and **"Sender ID"**
2. These are automatically used by the app
3. No manual configuration required

### Step 3: (Optional) Configure Notification Settings

1. Scroll down to **"Firebase Cloud Messaging API"**
2. Ensure it's enabled (should be by default)
3. No changes needed

### How It Works

- App automatically registers for FCM on first launch
- FCM token is saved to user's Firestore document
- Notifications are sent via Firestore triggers (handled in app)

## Verify Configuration

### Checklist

Before running the app, verify:

- [ ] `google-services.json` is in `app/` directory
- [ ] Firebase Authentication is enabled with Email/Password
- [ ] Firestore Database is created
- [ ] Security rules are deployed
- [ ] All 3 Firestore indexes are created and enabled
- [ ] Cloud Messaging is enabled
- [ ] Gradle sync completed successfully

### Test Configuration

1. **Build the app**:
   ```bash
   ./gradlew assembleDebug
   ```
   - Should complete without errors

2. **Run the app**:
   - Connect a device or start emulator
   - Click Run in Android Studio
   - App should launch successfully

3. **Test registration**:
   - Tap "Register"
   - Enter email and password
   - Tap "Register" button
   - Should create account and login

4. **Verify in Firebase Console**:
   - Go to Authentication → Users
   - You should see the new user
   - Go to Firestore Database
   - You should see `users` collection with user document

## Troubleshooting

### google-services.json Not Found

**Error**: "File google-services.json is missing"

**Solution**:
1. Verify file is in `app/` directory (not project root)
2. File name must be exactly `google-services.json` (lowercase)
3. Sync Gradle files after adding
4. Clean and rebuild project

### Package Name Mismatch

**Error**: "No matching client found for package name"

**Solution**:
1. Verify package name in Firebase Console matches `applicationId` in `app/build.gradle.kts`
2. Both should be: `com.example.loginandregistration`
3. If different, download new `google-services.json` with correct package name

### Authentication Not Working

**Error**: "Authentication failed" or "User not found"

**Solution**:
1. Verify Email/Password provider is enabled in Firebase Console
2. Check internet connection
3. Verify `google-services.json` is correct
4. Try creating a test user in Firebase Console first

### Firestore Permission Denied

**Error**: "PERMISSION_DENIED: Missing or insufficient permissions"

**Solution**:
1. Verify security rules are deployed
2. Check that user is authenticated (logged in)
3. Verify rules allow the operation
4. Check Firestore Rules simulator in Firebase Console

### Index Not Found

**Error**: "The query requires an index"

**Solution**:
1. Firebase Console will show a link to create the index
2. Click the link to auto-create the index
3. Wait 2-5 minutes for index to build
4. Retry the operation

### Cloud Messaging Not Working

**Error**: Notifications not received

**Solution**:
1. Verify Cloud Messaging is enabled
2. Check notification permissions on device
3. Verify FCM token is saved to Firestore
4. Test with Firebase Console → Cloud Messaging → Send test message
5. Check device is not in Do Not Disturb mode

### Gradle Sync Failed

**Error**: "Failed to resolve: com.google.firebase:firebase-..."

**Solution**:
1. Check internet connection
2. Verify `google-services.json` is in correct location
3. Update Gradle plugin: `com.google.gms:google-services:4.4.0`
4. Sync again
5. Invalidate caches and restart Android Studio

## Next Steps

After successful configuration:

1. **Test all features**:
   - Registration and login
   - Create a group
   - Create a task
   - Send a message
   - Upload profile picture

2. **Review security**:
   - Test that users can't access other users' data
   - Verify group membership restrictions work
   - Test chat participant restrictions

3. **Monitor usage**:
   - Firebase Console → Analytics (if enabled)
   - Check Firestore usage and quotas
   - Monitor authentication activity

4. **Prepare for production**:
   - Review security rules
   - Set up billing alerts
   - Configure backup (Firestore export)
   - Add more administrators to Firebase project

## Firebase Quotas (Free Tier)

Be aware of Firebase free tier limits:

- **Authentication**: 10,000 verifications/month
- **Firestore**:
  - 50,000 reads/day
  - 20,000 writes/day
  - 20,000 deletes/day
  - 1 GB storage
- **Cloud Messaging**: Unlimited
- **Hosting**: 10 GB storage, 360 MB/day transfer

For production apps with more users, consider upgrading to Blaze (pay-as-you-go) plan.

## Additional Resources

- [Firebase Documentation](https://firebase.google.com/docs)
- [Firestore Security Rules Guide](https://firebase.google.com/docs/firestore/security/get-started)
- [Cloud Messaging Guide](https://firebase.google.com/docs/cloud-messaging)
- [Firebase Console](https://console.firebase.google.com/)

## Support

If you encounter issues not covered here:

1. Check Firebase Status: [https://status.firebase.google.com/](https://status.firebase.google.com/)
2. Review Firebase documentation
3. Check Stack Overflow with tag `firebase`
4. Contact Firebase support (paid plans only)

---

**Configuration complete! Your app is now ready to use Firebase services.**
