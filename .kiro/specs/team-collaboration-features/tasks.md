# Implementation Plan - Team Collaboration Features

This implementation plan breaks down each feature into discrete, testable tasks. Each top-level task represents a complete feature that can be run and tested independently.

## Task Execution Guidelines

- Each numbered task is a complete, runnable feature
- Sub-tasks build up to the complete feature
- Test the app after completing each numbered task
- Optional sub-tasks (marked with *) can be skipped
- All tasks reference specific requirements from requirements.md

---

## Phase 1: Real-Time Chat System

- [x] 1. Create chat data models and repository



  - Create `models/Chat.kt` with Chat, ChatType, UserInfo data classes
  - Create `models/Message.kt` with Message, MessageStatus data classes
  - Create `repository/ChatRepository.kt` with all chat operations
  - Implement `getUserChats()` to fetch user's chats from Firestore
  - Implement `getOrCreateDirectChat()` for 1-on-1 chats
  - Implement `getOrCreateGroupChat()` for group chats
  - Implement `sendMessage()` to send text messages
  - Implement `getChatMessages()` with real-time listeners
  - Implement `markMessagesAsRead()` to update read status
  - _Requirements: 1.1, 1.2, 1.3, 1.4_

- [x] 2. Build chat list screen





  - Update `ChatFragment.kt` to display list of chats
  - Create `adapters/ChatAdapter.kt` for RecyclerView
  - Show chat name, last message, timestamp, unread count
  - Implement real-time updates using Flow
  - Add search bar for filtering chats
  - Add tabs for "All", "Groups", "Direct Messages"
  - Implement pull-to-refresh
  - Handle click to open chat room
  - Show empty state when no chats exist
  - _Requirements: 1.1_

- [x] 3. Build chat room screen










  - Create `ChatRoomActivity.kt` for individual chat
  - Create `adapters/MessageAdapter.kt` with sent/received ViewHolders
  - Display messages in reverse RecyclerView (newest at bottom)
  - Show sender profile picture for received messages
  - Implement message input field with send button
  - Send messages when button clicked or Enter pressed
  - Scroll to bottom when new message arrives
  - Show timestamp headers (group by time)
  - Display loading indicator when sending
  - _Requirements: 1.2, 1.3, 1.4_

- [x] 4. Implement typing indicators and read receipts





  - Add `updateTypingStatus()` to ChatRepository
  - Add `getTypingUsers()` with real-time listener
  - Show "User is typing..." indicator at bottom of chat
  - Update `markMessagesAsRead()` when messages are visible
  - Add checkmark icons to sent messages (single/double)
  - Change checkmark color when message is read
  - Update message status in real-time
  - _Requirements: 1.5, 1.6_
- [x] 5. Add user search for direct messages





- [ ] 5. Add user search for direct messages

  - Create `UserSearchDialog.kt` fragment
  - Add search input field with real-time filtering
  - Implement `searchUsers()` in ChatRepository
  - Display search results in RecyclerView
  - Show user profile picture and name
  - Handle click to create direct chat
  - Navigate to chat room after creation
  - Show "No users found" empty state
  - _Requirements: 1.7_


- [x] 6. Implement message pagination



  - Add pagination to `getChatMessages()` in repository
  - Load initial 50 messages
  - Detect scroll to top in RecyclerView
  - Load next 50 messages when scrolled to top
  - Show loading indicator while loading more
  - Prevent duplicate loading
  - Handle case when no more messages exist
  - _Requirements: 1.8_

- [x] 7. Add offline message queue






  - Create `OfflineMessageQueue.kt` helper class
  - Queue messages when offline
  - Show "Sending..." status for queued messages
  - Auto-send when connection restored
  - Show "Failed" status if send fails after retry
  - Allow manual retry for failed messages
  - _Requirements: 1.9_

**✅ Checkpoint 1: Test Chat System**
- Run app and verify you can see chat list
- Create a direct chat with another user
- Send and receive messages in real-time
- Verify typing indicators work
- Verify read receipts show correctly
- Test message pagination by scrolling up

---

## Phase 2: Push Notifications

- [x] 8. Set up Firebase Cloud Messaging service





  - Create `services/MyFirebaseMessagingService.kt`
  - Extend `FirebaseMessagingService`
  - Implement `onNewToken()` to save FCM token
  - Implement `onMessageReceived()` to handle notifications
  - Register service in AndroidManifest.xml
  - Create `NotificationChannels.kt` helper
  - Create channels for Chat, Tasks, Groups
  - _Requirements: 2.1, 2.2_

- [x] 9. Implement notification display





  - Create `NotificationHelper.kt` utility class
  - Implement `showChatNotification()` with message preview
  - Implement `showTaskNotification()` for task reminders
  - Implement `showGroupNotification()` for group updates
  - Add notification icons and colors
  - Set notification priority to HIGH for chats
  - Add sound and vibration
  - Group notifications by chat
  - _Requirements: 2.3, 2.4_

- [x] 10. Add notification permissions and deep linking




  - Create `NotificationPermissionHelper.kt`
  - Request POST_NOTIFICATIONS permission on Android 13+
  - Show rationale dialog if permission denied
  - Create PendingIntents for notification taps
  - Implement deep linking to ChatRoomActivity
  - Implement deep linking to TaskDetailsActivity
  - Pass chat/task ID via intent extras
  - Handle notification tap in activities
  - _Requirements: 2.1, 2.5_

- [x] 11. Integrate FCM token management




  - Create `repository/NotificationRepository.kt`
  - Implement `saveFcmToken()` to update user document
  - Call `saveFcmToken()` in onNewToken()
  - Call `saveFcmToken()` on app start if token exists
  - Implement `getUserTokens()` to fetch recipient tokens
  - Update `sendMessage()` to trigger notifications
  - Only send notifications if recipient is not in chat
  - _Requirements: 2.2, 2.8_

- [x] 12. Add task reminder notifications





  - Create WorkManager worker for task reminders
  - Schedule reminder 24 hours before due date
  - Send notification with task title and due date
  - Add "Mark Complete" action button
  - Add "View Task" action button
  - Cancel reminder if task is completed
  - Reschedule if due date changes
  - _Requirements: 2.6_

**✅ Checkpoint 2: Test Notifications**
- Run app and grant notification permission
- Send a message from another device/account
- Verify notification appears on lock screen
- Tap notification and verify it opens correct chat
- Test with app in foreground (should not show notification)
- Test with app in background (should show notification)

---

## Phase 3: File and Image Sharing

- [x] 13. Create image compression and base64 encoding (NO FIREBASE STORAGE)





  - Create `utils/ImageCompressor.kt`
  - Compress images to max 800x800, 70% quality for smaller size
  - Resize images while maintaining aspect ratio
  - Create `utils/Base64Helper.kt` for encoding/decoding
  - Implement `encodeImageToBase64()` for storing in Firestore
  - Implement `decodeBase64ToImage()` for displaying images
  - Keep compressed images under 1MB for Firestore limits
  - _Requirements: 3.2, 3.3_

- [x] 14. Add attachment picker to chat





  - Create `AttachmentBottomSheet.kt` dialog
  - Add options: Camera, Gallery, Documents
  - Request CAMERA permission when camera selected
  - Request READ_MEDIA_IMAGES permission when gallery selected
  - Use ActivityResultContracts for image picker
  - Use ActivityResultContracts for document picker
  - Show attachment button in chat input area
  - _Requirements: 3.1, 3.10_

- [x] 15. Implement image message sending (BASE64 IN FIRESTORE)





  - Update `sendImageMessage()` in ChatRepository
  - Compress image before encoding
  - Encode image to base64 string
  - Show progress indicator during compression
  - Save message with base64ImageData field to Firestore
  - Display image from base64 in chat
  - Handle encoding failures with retry option
  - _Requirements: 3.2, 3.3, 3.4, 3.9_

- [x] 16. Implement document message sending (EXTERNAL LINKS ONLY)





  - Update `sendDocumentMessage()` in ChatRepository
  - For now, only support sharing document URLs/links
  - Save message with documentUrl, name to Firestore
  - Display document link with icon and name in chat
  - User can paste links to Google Drive, Dropbox, etc.
  - Show message: "Share document links (Google Drive, Dropbox, etc.)"
  - Handle validation of URLs
  - _Requirements: 3.6, 3.7, 3.9_

- [x] 17. Add image viewer and document link opening





  - Create `ImageViewerActivity.kt` for full-screen images
  - Load image from base64 data
  - Implement pinch-to-zoom for images
  - Add swipe-to-dismiss gesture
  - For document links, open URL in browser using Intent
  - Show toast: "Opening link in browser"
  - Handle case when no browser available
  - _Requirements: 3.5, 3.7_

- [x] 18. Implement profile picture upload (BASE64 IN FIRESTORE)





  - Update `ProfileFragment.kt` with edit button
  - Show options: Take Photo, Choose from Gallery
  - Crop image to square using library or custom cropper
  - Compress to under 200KB for Firestore
  - Encode to base64 string
  - Update user document with profileImageBase64 field
  - Display profile picture from base64 in UI
  - _Requirements: 3.8, 5.1, 5.2, 5.3, 5.4, 5.5_

- [x] 19. Display profile pictures throughout app




  - Load profile pictures in ChatAdapter
  - Load profile pictures in MessageAdapter
  - Load profile pictures in MembersAdapter
  - Load profile pictures in UserSearchDialog
  - Use Coil for image loading with caching
  - Create `DefaultAvatarGenerator.kt` for users without pictures
  - Generate colored avatar with initials
  - Cache generated avatars
  - _Requirements: 5.6, 5.7, 5.8_

**✅ Checkpoint 3: Test File Sharing**
- Run app and open a chat
- Tap attachment button and select an image
- Verify image uploads with progress indicator
- Verify image displays in chat
- Tap image to view full-screen
- Send a document (PDF, etc.)
- Tap document to download and open
- Update your profile picture
- Verify profile picture shows in chats and messages

---

## Phase 4: Enhanced Calendar View

- [x] 20. Integrate calendar library and display tasks





  - Add Kizitonwose Calendar View to `CalendarFragment.kt`
  - Configure calendar to show current month
  - Create `CalendarViewModel.kt` for data management
  - Load all user tasks from Firestore
  - Create `DayBinder.kt` for custom day cell rendering
  - Show date number in each cell
  - Add colored dot indicators for dates with tasks
  - Highlight today's date
  - Highlight selected date
  - _Requirements: 4.1, 4.2, 4.9_

- [x] 21. Implement date selection and task filtering





  - Handle date click to select date
  - Display tasks for selected date below calendar
  - Create RecyclerView with TaskAdapter for date tasks
  - Show task title, priority, category, status
  - Implement swipe left/right to change months
  - Add filter chips: All Tasks, My Tasks, Group Tasks
  - Filter tasks based on selected chip
  - Update calendar dots when filter changes
  - _Requirements: 4.3, 4.4, 4.6, 4.8_

- [x] 22. Add task details navigation





  - Handle task click in calendar task list
  - Navigate to existing TaskDetailsActivity or create new one
  - Pass task ID via intent
  - Display full task details (title, description, due date, etc.)
  - Show assigned members
  - Add edit and delete buttons
  - Allow marking task as complete
  - Update calendar when task is modified
  - _Requirements: 4.5, 4.7_

**✅ Checkpoint 4: Test Calendar**
- Run app and open calendar screen
- Verify current month is displayed
- Verify dates with tasks show colored dots
- Tap a date and verify tasks for that date appear below
- Tap a task and verify details screen opens
- Swipe to change months
- Test filter chips (All, My Tasks, Group)
- Create a new task with due date and verify calendar updates

---

## Phase 5: Message Rich Content

- [x] 23. Implement link detection and clickable URLs





  - Create `utils/LinkifyHelper.kt`
  - Detect URLs in message text using regex
  - Make URLs clickable in TextViews
  - Open URLs in browser when clicked
  - Highlight URLs with different color
  - Handle various URL formats (http, https, www)
  - _Requirements: 6.1, 6.2_
- [x] 24. Add message status indicators









- [x] 24. Add message status indicators

  - Create `MessageStatusView.kt` custom view
  - Show clock icon for "Sending" status
  - Show single checkmark for "Sent" status
  - Show double gray checkmark for "Delivered" status
  - Show double blue checkmark for "Read" status
  - Show error icon for "Failed" status with retry button
  - Update status in real-time as messages are delivered/read
  - _Requirements: 6.6, 6.7, 6.8_

- [x] 25. Implement message grouping and layout improvements





  - Create `MessageGrouper.kt` utility
  - Group consecutive messages from same sender
  - Show sender name and picture only for first message in group
  - Show timestamp only every 5 minutes
  - Align sent messages to right with different background
  - Align received messages to left with sender picture
  - Add message bubbles with rounded corners
  - Add tail indicator pointing to sender
  - _Requirements: 6.4, 6.5, 6.9_

- [x] 26. Add long-press context menu for messages





  - Implement long-press listener on message items
  - Show context menu with options: Copy, Delete, Forward
  - Implement copy text to clipboard
  - Implement delete message (only for sender)
  - Show confirmation dialog before delete
  - Update Firestore when message deleted
  - Remove message from UI immediately
  - _Requirements: 8.7_

**✅ Checkpoint 5: Test Rich Content**
- Run app and send a message with a URL
- Verify URL is clickable and opens in browser
- Send multiple messages and verify status indicators
- Verify checkmarks change from single to double
- Verify messages from same sender are grouped
- Long-press a message and verify context menu appears
- Test copy and delete options

---

## Phase 6: Offline Support

- [x] 27. Enable Firestore offline persistence




  - Create `FirestoreConfig.kt` configuration class
  - Enable offline persistence in Application class or MainActivity
  - Set cache size to unlimited or appropriate size
  - Test that data loads from cache when offline
  - _Requirements: 7.1, 7.2_

- [x] 28. Implement offline message queue





  - Create `OfflineMessageQueue.kt` using SharedPreferences
  - Queue messages when send fails due to no connection
  - Show "Sending..." status for queued messages
  - Create `ConnectionMonitor.kt` to detect connectivity changes
  - Auto-send queued messages when connection restored
  - Show "Failed" status if send fails after multiple retries
  - Allow manual retry for failed messages
  - _Requirements: 7.3, 7.4_

- [x] 29. Add connection status indicator





  - Create connection status banner at top of screen
  - Show "No internet connection" when offline
  - Show "Connecting..." when reconnecting
  - Hide banner when online
  - Use ConnectionMonitor to detect status changes
  - Animate banner slide in/out
  - _Requirements: 7.5_

- [x] 30. Implement offline image caching (BASE64 ALREADY CACHED)





  - Since images are stored as base64 in Firestore
  - They are automatically cached with Firestore offline persistence
  - Configure Coil for decoded bitmap caching in memory
  - Set memory cache to 25% of available memory
  - Display cached images when offline from Firestore cache
  - Show placeholder only if data not yet loaded
  - _Requirements: 7.6, 7.7_

**✅ Checkpoint 6: Test Offline Support**
- Run app and load some chats and messages
- Turn on airplane mode
- Verify chats and messages still display from cache
- Try to send a message (should queue)
- Turn off airplane mode
- Verify queued message sends automatically
- Verify connection status banner appears/disappears correctly

---

## Phase 7: User Experience Enhancements

- [x] 31. Add skeleton loaders for loading states





  - Create `SkeletonLoader.kt` custom view with shimmer effect
  - Add skeleton layout for chat list items
  - Add skeleton layout for message list items
  - Add skeleton layout for task list items
  - Show skeleton while loading data
  - Fade out skeleton and fade in actual content
  - _Requirements: 8.1_

- [x] 32. Create empty state views





  - Create `EmptyStateView.kt` custom view
  - Add empty state for "No chats yet"
  - Add empty state for "No messages"
  - Add empty state for "No tasks"
  - Add empty state for "No internet connection"
  - Include icon, title, description, and optional action button
  - Show appropriate empty state based on context
  - _Requirements: 8.2_

- [x] 33. Implement swipe-to-refresh

  - Add SwipeRefreshLayout to ChatFragment
  - Add SwipeRefreshLayout to TasksFragment
  - Add SwipeRefreshLayout to GroupsFragment
  - Refresh data from Firestore when pulled down
  - Show loading indicator while refreshing
  - Hide indicator when refresh completes
  - _Requirements: 8.6_

- [x] 34. Add animations and transitions





  - Create `AnimationUtils.kt` with common animations
  - Add fade-in animation for new messages
  - Add slide-up animation for bottom sheets
  - Add scale animation for button presses
  - Add shared element transition for image viewer
  - Add smooth scroll animation for RecyclerViews
  - Add ripple effect for clickable items
  - _Requirements: 8.3, 8.5_

- [x] 35. Implement error handling with user feedback




  - Create `ErrorHandler.kt` utility class
  - Show Snackbar for network errors with retry button
  - Show dialog for permission errors with settings button
  - Show toast for validation errors
  - Log errors to console for debugging
  - Provide helpful error messages (not technical jargon)
  - _Requirements: 8.4_

- [x] 36. Add dark mode support






  - Create night theme in `values-night/themes.xml`
  - Define dark colors for all UI elements
  - Test all screens in dark mode
  - Ensure text is readable on dark backgrounds
  - Ensure images and icons look good in dark mode
  - Respect system dark mode setting
  - _Requirements: 8.8_

**✅ Checkpoint 7: Test UX Enhancements**
- Run app and observe skeleton loaders while data loads
- Navigate to empty screens and verify empty states show
- Pull down on lists to refresh
- Observe animations when sending messages, opening dialogs
- Trigger various errors and verify user-friendly messages
- Enable dark mode on device and verify app looks good

---

## Phase 8: Security and Privacy

- [x] 37. Implement Firestore security rules





  - Create `firestore.rules` file in project root
  - Add authentication check for all operations
  - Add user ownership rules for users collection
  - Add group membership rules for groups collection
  - Add participant rules for chats collection
  - Add sender rules for messages subcollection
  - Add task ownership rules for tasks collection
  - Deploy rules to Firebase Console
  - _Requirements: 9.1, 9.2, 9.3, 9.6_

- [x] 38. ~~Implement Storage security rules~~ (NOT USING FIREBASE STORAGE)









  - NOT NEEDED - We're using base64 in Firestore instead
  - Images stored directly in Firestore documents
  - Security handled by Firestore rules (Task 37)
  - Document links are external URLs (Google Drive, etc.)
  - Validate base64 data size in app before saving
  - Limit images to 1MB encoded size
  - _Requirements: 9.4, 9.5_

- [x] 39. Configure ProGuard for release builds





  - Update `proguard-rules.pro` file
  - Add keep rules for Firebase classes
  - Add keep rules for data model classes
  - Add keep rules for Coil
  - Add keep rules for Kotlin coroutines
  - Enable minification in release build type
  - Test release build to ensure no crashes
  - _Requirements: 9.7_

- [ ] 40. Add input validation and sanitization

  - Validate message text length (max 5000 characters)
  - Validate file sizes before upload
  - Sanitize user input to prevent injection
  - Validate email format in user search
  - Validate group name length and characters
  - Show validation errors inline
  - _Requirements: 9.8_

**✅ Checkpoint 8: Test Security**
- Deploy Firestore and Storage rules
- Try to access another user's data (should fail)
- Try to access a group you're not a member of (should fail)
- Try to upload a file larger than limit (should fail)
- Build release APK and test on device
- Verify app works correctly with ProGuard enabled

---

## Phase 9: Performance Optimization

- [ ] 41. Implement message pagination
  - Create `PaginationHelper.kt` utility class
  - Update `getChatMessages()` to load 50 messages initially
  - Detect scroll to top in RecyclerView
  - Load next 50 messages when scrolled to top
  - Show loading indicator while loading more
  - Prevent duplicate loading with flag
  - Handle case when no more messages exist
  - _Requirements: 10.1, 10.2_

- [ ] 42. Configure image caching with Coil
  - Create `ImageLoaderConfig.kt`
  - Configure memory cache (25% of app memory)
  - Configure disk cache (50MB)
  - Set cache directory
  - Apply configuration globally
  - Test that images load from cache
  - _Requirements: 10.3, 10.4_

- [ ] 43. Optimize Firestore queries with indexes
  - Create `firestore.indexes.json` file
  - Add composite index for messages (chatId + timestamp)
  - Add composite index for tasks (assignedTo + dueDate)
  - Add composite index for chats (participants + lastMessageTime)
  - Deploy indexes to Firebase Console
  - Test that queries run fast
  - _Requirements: 10.5_

- [ ] 44. Implement lifecycle-aware listeners
  - Detach Firestore listeners in onStop()
  - Re-attach listeners in onStart()
  - Cancel coroutines in ViewModel onCleared()
  - Use viewModelScope for coroutines
  - Avoid memory leaks with WeakReference if needed
  - Test with Android Profiler
  - _Requirements: 10.6, 10.7_

- [ ] 45. Add memory management and cache clearing

  - Monitor memory usage with Profiler
  - Clear image cache when memory is low
  - Implement cache size limits
  - Use RecyclerView ViewHolder pattern efficiently
  - Avoid loading large bitmaps into memory
  - Test on low-end devices
  - _Requirements: 10.8, 10.9_

**✅ Checkpoint 9: Test Performance**
- Open a chat with many messages
- Scroll to top and verify pagination loads more messages
- Open multiple chats and verify images load quickly from cache
- Monitor memory usage in Android Profiler
- Test on a low-end device (if available)
- Verify app is responsive and doesn't lag

---

## Phase 10: Final Polish and Testing

- [ ] 46. Create comprehensive manual testing checklist
  - Test all authentication flows (login, register, logout)
  - Test all group operations (create, join, leave, manage members)
  - Test all task operations (create, edit, delete, complete)
  - Test all chat operations (send text, image, document)
  - Test notifications (receive, tap, open correct screen)
  - Test calendar (view tasks, filter, navigate months)
  - Test profile (update picture, view profile)
  - Test offline mode (queue messages, sync when online)
  - Test on multiple devices simultaneously
  - Test on different Android versions (API 23-34)

- [ ] 47. Fix any bugs found during testing
  - Document bugs with steps to reproduce
  - Prioritize bugs by severity
  - Fix critical bugs first
  - Test fixes thoroughly
  - Regression test to ensure no new bugs introduced
  - check if all the stats in the app are loading properly from the database 

- [ ] 48. Prepare app for release
  - Update app icon (replace default icon)
  - Create splash screen
  - Update app name and description
  - Set version name and version code
  - Generate signed APK/AAB
  - Test signed build on device
  - Prepare screenshots for Play Store

- [ ] 49. Write unit tests for critical functionality
  - Test repository methods
  - Test ViewModel logic
  - Test utility functions
  - Test data model conversions
  - Aim for >70% code coverage on business logic

- [ ] 50. Create user documentation
  - Write README with setup instructions
  - Document Firebase configuration steps
  - Create user guide with screenshots
  - Document known issues and limitations
  - Add troubleshooting section

**✅ Final Checkpoint: Complete App Testing**
- Run through entire manual testing checklist
- Verify all features work end-to-end
- Test with multiple users on different devices
- Verify notifications work on lock screen
- Verify offline mode works correctly
- Verify app is stable and performant
- App is ready for deployment!

---

## Summary

**Total Tasks:** 50 (40 required, 10 optional)

**Estimated Time:**
- Phase 1 (Chat): 6-8 hours
- Phase 2 (Notifications): 3-4 hours
- Phase 3 (File Sharing): 4-5 hours
- Phase 4 (Calendar): 2-3 hours
- Phase 5 (Rich Content): 2-3 hours
- Phase 6 (Offline): 2-3 hours
- Phase 7 (UX): 3-4 hours
- Phase 8 (Security): 2-3 hours
- Phase 9 (Performance): 2-3 hours
- Phase 10 (Polish): 3-4 hours

**Total: 29-40 hours**

**Testing Checkpoints:** 10 (one after each phase)

**Requirements Coverage:** All 10 requirements fully covered

Each task builds incrementally on previous tasks, and you can run and test the app after completing each numbered task to verify functionality before moving forward.
