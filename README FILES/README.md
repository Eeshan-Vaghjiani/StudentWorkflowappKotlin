# Team Collaboration App

A comprehensive Android team collaboration application built with Kotlin and Firebase, featuring real-time chat, task management, group collaboration, and more.

## 📱 Features

### Core Features
- **User Authentication**: Secure login and registration with Firebase Authentication
- **Real-Time Chat**: Direct messages and group chats with typing indicators and read receipts
- **Task Management**: Create, assign, and track tasks with priorities and due dates
- **Group Management**: Create and manage groups with member administration
- **Enhanced Calendar**: Visualize tasks on a calendar with filtering options
- **Push Notifications**: Real-time notifications for messages, tasks, and group updates
- **File Sharing**: Share images and document links in chats
- **Profile Management**: Upload and manage profile pictures
- **Offline Support**: Queue messages and sync when connection is restored
- **Dark Mode**: Full dark theme support

### Advanced Features
- Message pagination for performance
- Offline message queue with auto-sync
- Image compression and base64 storage
- Connection status monitoring
- Swipe-to-refresh on all lists
- Skeleton loaders for better UX
- Empty state views
- Rich message content with clickable links
- Message status indicators (sent, delivered, read)
- Firestore security rules
- ProGuard obfuscation for release builds

## 🚀 Getting Started

### Prerequisites

- Android Studio Arctic Fox or later
- JDK 11 or later
- Android SDK (API 23+)
- Firebase account
- Google Services JSON file

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd team-collaboration-app
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory
   - Click "OK"

3. **Configure Firebase** (See [Firebase Setup Guide](#firebase-setup))

4. **Sync Gradle**
   - Android Studio will prompt you to sync Gradle
   - Click "Sync Now"
   - Wait for dependencies to download

5. **Run the app**
   - Connect an Android device or start an emulator
   - Click the "Run" button (green play icon)
   - Select your device
   - Wait for the app to build and install

## 🔥 Firebase Setup

### Step 1: Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project"
3. Enter project name: "Team Collaboration App"
4. Enable Google Analytics (optional)
5. Click "Create project"

### Step 2: Add Android App

1. In Firebase Console, click "Add app" → Android icon
2. Enter package name: `com.example.loginandregistration`
3. Enter app nickname: "Team Collaboration App"
4. Click "Register app"
5. Download `google-services.json`
6. Place it in `app/` directory

### Step 3: Enable Authentication

1. In Firebase Console, go to "Authentication"
2. Click "Get started"
3. Enable "Email/Password" sign-in method
4. Click "Save"

### Step 4: Create Firestore Database

1. In Firebase Console, go to "Firestore Database"
2. Click "Create database"
3. Select "Start in test mode" (we'll add security rules later)
4. Choose a location (closest to your users)
5. Click "Enable"

### Step 5: Deploy Security Rules

1. Open `firestore.rules` in the project root
2. In Firebase Console, go to "Firestore Database" → "Rules"
3. Copy the contents of `firestore.rules`
4. Paste into the Firebase Console rules editor
5. Click "Publish"

### Step 6: Deploy Firestore Indexes

1. Open `firestore.indexes.json` in the project root
2. In Firebase Console, go to "Firestore Database" → "Indexes"
3. Click "Add index" for each index in the JSON file:
   - **Messages Index**: chatId (Ascending), timestamp (Descending)
   - **Tasks Index**: assignedTo (Array-contains), dueDate (Ascending)
   - **Chats Index**: participants (Array-contains), lastMessageTime (Descending)

### Step 7: Enable Cloud Messaging

1. In Firebase Console, go to "Cloud Messaging"
2. No additional setup required - FCM is enabled by default
3. Note: Server key is used for sending notifications (handled automatically)

### Step 8: Configure App

1. Open `app/google-services.json` and verify it contains your Firebase configuration
2. Sync Gradle files
3. Build and run the app

## 📖 User Guide

### Registration and Login

1. **Register a New Account**
   - Open the app
   - Tap "Register"
   - Enter your email and password
   - Tap "Register" button
   - You'll be automatically logged in

2. **Login**
   - Open the app
   - Enter your email and password
   - Tap "Login" button

### Creating and Managing Groups

1. **Create a Group**
   - Tap "Groups" tab
   - Tap the "+" button
   - Enter group name, description, and subject
   - Choose visibility (Public/Private)
   - Tap "Create Group"

2. **Join a Group**
   - Tap "Groups" tab
   - Tap "Join Group" button
   - Enter the 6-digit join code
   - Tap "Join"

3. **Manage Group Members**
   - Open a group
   - Tap "Members" tab
   - Tap "Add Members" to invite users
   - Long-press a member to remove (admins only)

### Task Management

1. **Create a Task**
   - Tap "Tasks" tab
   - Tap the "+" button
   - Enter task details:
     - Title
     - Description
     - Category (Personal, Group, Assignment)
     - Priority (Low, Medium, High)
     - Due date
     - Assigned members (for group tasks)
   - Tap "Create Task"

2. **View Tasks**
   - Tap "Tasks" tab to see all tasks
   - Filter by category using chips
   - Tap a task to view details

3. **Complete a Task**
   - Open task details
   - Tap "Mark as Complete" button
   - Task status changes to "Completed"

4. **Calendar View**
   - Tap "Calendar" tab
   - Dates with tasks show colored dots
   - Tap a date to see tasks for that day
   - Swipe left/right to change months

### Chat and Messaging

1. **Start a Direct Chat**
   - Tap "Chat" tab
   - Tap the "+" button
   - Search for a user by email
   - Tap the user to start chatting

2. **Group Chat**
   - Group chats are automatically created for each group
   - Open a group and tap "Chat" to access group chat

3. **Send Messages**
   - Type your message in the input field
   - Tap the send button
   - Message appears immediately

4. **Share Images**
   - Tap the attachment button (paperclip icon)
   - Choose "Camera" or "Gallery"
   - Select/take a photo
   - Image is compressed and sent

5. **Share Document Links**
   - Tap the attachment button
   - Choose "Documents"
   - Paste a link to Google Drive, Dropbox, etc.
   - Link is shared in chat

6. **Message Features**
   - **Typing Indicators**: See when others are typing
   - **Read Receipts**: Double checkmarks when message is read
   - **Message Status**: Clock (sending), checkmark (sent), double checkmark (delivered/read)
   - **Clickable Links**: URLs in messages are automatically clickable
   - **Long-press Menu**: Copy, delete, or forward messages

### Profile Management

1. **Update Profile Picture**
   - Tap "Profile" tab
   - Tap the profile picture
   - Choose "Take Photo" or "Choose from Gallery"
   - Crop and confirm
   - Picture is uploaded and displayed

2. **View Profile**
   - Tap "Profile" tab
   - See your email, name, and statistics
   - View groups and tasks

### Notifications

1. **Enable Notifications**
   - On first launch, grant notification permission
   - If denied, go to Settings → Apps → Team Collaboration → Notifications

2. **Notification Types**
   - **Chat Messages**: New messages when app is closed
   - **Task Reminders**: 24 hours before due date
   - **Group Updates**: When added to a group

3. **Notification Actions**
   - Tap notification to open relevant screen
   - Swipe to dismiss

### Offline Mode

1. **Using Offline**
   - App works offline with cached data
   - Send messages while offline (they're queued)
   - View previously loaded chats and tasks

2. **Syncing**
   - When connection is restored, queued messages are sent automatically
   - Connection status banner shows at top of screen
   - Pull down to refresh lists

## 🛠️ Troubleshooting

### Common Issues

#### App Won't Build

**Problem**: Gradle sync fails or build errors

**Solutions**:
1. Check that `google-services.json` is in the `app/` directory
2. Sync Gradle files: File → Sync Project with Gradle Files
3. Clean and rebuild: Build → Clean Project, then Build → Rebuild Project
4. Invalidate caches: File → Invalidate Caches / Restart
5. Check internet connection for dependency downloads

#### Login Fails

**Problem**: "Authentication failed" error

**Solutions**:
1. Verify Firebase Authentication is enabled in Firebase Console
2. Check that Email/Password provider is enabled
3. Verify `google-services.json` is correct
4. Check internet connection
5. Try registering a new account first

#### Messages Not Sending

**Problem**: Messages stuck in "Sending" status

**Solutions**:
1. Check internet connection
2. Verify Firestore is enabled in Firebase Console
3. Check Firestore security rules are deployed
4. Try logging out and back in
5. Clear app data and login again

#### Notifications Not Working

**Problem**: Not receiving push notifications

**Solutions**:
1. Grant notification permission: Settings → Apps → Team Collaboration → Notifications
2. Verify FCM is enabled in Firebase Console
3. Check that app is not in battery optimization mode
4. Restart the device
5. Reinstall the app

#### Images Not Loading

**Problem**: Profile pictures or chat images don't display

**Solutions**:
1. Check internet connection
2. Clear app cache: Settings → Apps → Team Collaboration → Storage → Clear Cache
3. Verify images were uploaded successfully
4. Check that base64 data is not corrupted
5. Try uploading a smaller image

#### Calendar Not Showing Tasks

**Problem**: Calendar view is empty or tasks don't appear

**Solutions**:
1. Verify tasks have due dates set
2. Pull down to refresh
3. Check filter settings (All Tasks vs My Tasks)
4. Verify you're assigned to the tasks
5. Check Firestore indexes are deployed

#### Offline Mode Issues

**Problem**: App doesn't work offline or messages don't sync

**Solutions**:
1. Ensure you've loaded data while online first
2. Check that Firestore offline persistence is enabled
3. Clear app data and reload while online
4. Verify connection monitor is working
5. Check queued messages in app storage

### Performance Issues

#### App is Slow

**Solutions**:
1. Clear image cache: Settings → Apps → Team Collaboration → Storage → Clear Cache
2. Reduce number of loaded messages (pagination helps)
3. Close and restart the app
4. Check device storage space
5. Update to latest version

#### High Battery Usage

**Solutions**:
1. Disable background sync if not needed
2. Reduce notification frequency
3. Close app when not in use
4. Check for app updates
5. Restart device

### Error Messages

#### "Permission Denied"

**Cause**: Firestore security rules blocking access

**Solution**: Verify security rules are deployed correctly and user is authenticated

#### "Network Error"

**Cause**: No internet connection or Firebase services down

**Solution**: Check internet connection, try again later, or check Firebase status

#### "Storage Limit Exceeded"

**Cause**: Firestore document size limit (1MB) exceeded

**Solution**: Reduce image size, compress more aggressively, or use smaller images

#### "Invalid Token"

**Cause**: FCM token expired or invalid

**Solution**: Logout and login again to refresh token

## 🔒 Security and Privacy

### Data Security

- All data is stored in Firebase Firestore with security rules
- Only authenticated users can access data
- Users can only access groups they're members of
- Messages are only visible to chat participants
- Profile pictures are stored as base64 in Firestore (no external storage)

### Privacy

- Email addresses are only visible to users in the same groups
- Profile pictures are optional
- Users control what information they share
- No data is shared with third parties
- Firebase Analytics can be disabled

### Best Practices

1. Use strong passwords (8+ characters, mix of letters, numbers, symbols)
2. Don't share your login credentials
3. Log out on shared devices
4. Keep the app updated
5. Review group members before sharing sensitive information

## 📋 Known Issues and Limitations

### Current Limitations

1. **File Sharing**: Only document links are supported (not direct file uploads)
2. **Image Storage**: Images stored as base64 in Firestore (1MB limit per document)
3. **Message History**: Limited to last 1000 messages per chat (pagination)
4. **Group Size**: Recommended maximum 50 members per group
5. **Offline Mode**: Only previously loaded data is available offline
6. **Search**: Basic text search only (no advanced filters)
7. **Video/Audio**: No video or audio calling support
8. **Encryption**: Messages are not end-to-end encrypted
9. **Export**: No chat history export feature
10. **Multi-device**: No sync between multiple devices for same account

### Known Bugs

1. **Typing Indicator**: May occasionally stick if user closes app while typing
2. **Read Receipts**: May not update immediately in poor network conditions
3. **Calendar Dots**: May not appear until calendar is refreshed
4. **Profile Picture**: Very large images may fail to upload (compress first)
5. **Notification Sound**: May not play on some devices with custom ROMs

### Planned Features

- Video/audio calling
- End-to-end encryption
- Message search
- Chat history export
- Multi-language support
- Tablet-optimized layouts
- Widget support
- Backup/restore functionality

## 🏗️ Architecture

### Tech Stack

- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **Backend**: Firebase (Firestore, Authentication, Cloud Messaging)
- **Async**: Kotlin Coroutines + Flow
- **UI**: ViewBinding, RecyclerView, Material Design Components
- **Image Loading**: Coil
- **Calendar**: Kizitonwose Calendar View
- **Dependency Injection**: Manual (no DI framework)

### Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/loginandregistration/
│   │   │   ├── adapters/          # RecyclerView adapters
│   │   │   ├── models/            # Data models
│   │   │   ├── repository/        # Data access layer
│   │   │   ├── services/          # Background services
│   │   │   ├── utils/             # Utility classes
│   │   │   ├── viewmodels/        # ViewModels
│   │   │   ├── views/             # Custom views
│   │   │   ├── *Fragment.kt       # UI fragments
│   │   │   └── *Activity.kt       # UI activities
│   │   ├── res/                   # Resources (layouts, drawables, etc.)
│   │   └── AndroidManifest.xml
│   └── test/                      # Unit tests
├── build.gradle.kts
└── google-services.json           # Firebase configuration
```

### Key Components

- **ChatRepository**: Manages chat and message operations
- **TaskRepository**: Manages task CRUD operations
- **GroupRepository**: Manages group operations
- **NotificationRepository**: Manages FCM tokens and notifications
- **ConnectionMonitor**: Monitors network connectivity
- **OfflineMessageQueue**: Queues messages when offline
- **ImageCompressor**: Compresses images before upload
- **Base64Helper**: Encodes/decodes images for Firestore storage

## 🧪 Testing

### Running Tests

```bash
# Run all unit tests
./gradlew test

# Run tests with coverage
./gradlew testDebugUnitTest jacocoTestReport

# Run specific test class
./gradlew test --tests "com.example.loginandregistration.utils.Base64HelperTest"
```

### Test Coverage

- Unit tests for repositories
- Unit tests for ViewModels
- Unit tests for utility classes
- Unit tests for data models
- Manual testing checklist for UI flows

### Manual Testing

See `TASK_46_MANUAL_TESTING_CHECKLIST.md` for comprehensive manual testing procedures.

## 📦 Building for Release

### Generate Signed APK

1. **Create Keystore** (first time only)
   ```bash
   keytool -genkey -v -keystore team-collaboration.keystore -alias team-collab -keyalg RSA -keysize 2048 -validity 10000
   ```

2. **Build Signed APK**
   - In Android Studio: Build → Generate Signed Bundle / APK
   - Select "APK"
   - Choose keystore file
   - Enter keystore password and key password
   - Select "release" build variant
   - Click "Finish"

3. **Locate APK**
   - APK will be in `app/release/app-release.apk`

### Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing)
./gradlew assembleRelease

# Build Android App Bundle (for Play Store)
./gradlew bundleRelease
```

## 📄 License

This project is for educational purposes. All rights reserved.

## 🤝 Contributing

This is a private project. Contributions are not currently accepted.

## 📞 Support

For issues or questions:
1. Check the [Troubleshooting](#troubleshooting) section
2. Review [Known Issues](#known-issues-and-limitations)
3. Contact the development team

## 📚 Additional Documentation

- [Firebase Setup Guide](FIREBASE_SETUP_GUIDE.md)
- [Manual Testing Checklist](TASK_46_MANUAL_TESTING_CHECKLIST.md)
- [Release Preparation Guide](RELEASE_PREPARATION_GUIDE.md)
- [Play Store Listing](PLAY_STORE_LISTING.md)
- [Quick Release Commands](QUICK_RELEASE_COMMANDS.md)

## 🎯 Version History

### Version 1.0.0 (Current)
- Initial release
- Real-time chat with direct and group messaging
- Task management with calendar view
- Group collaboration features
- Push notifications
- File and image sharing
- Profile management
- Offline support
- Dark mode

---

**Built with ❤️ using Kotlin and Firebase**
