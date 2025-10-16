# Task 10 Verification Checklist

## Notification Permission Testing

### Android 13+ Devices

#### First Launch
- [ ] Open app for the first time
- [ ] Verify rationale dialog appears with clear explanation
- [ ] Dialog shows benefits: messages, task reminders, group updates
- [ ] Tap "Allow" button
- [ ] Verify system permission dialog appears
- [ ] Grant permission
- [ ] Verify no errors in logcat
- [ ] Check Firestore: user document should have fcmToken field

#### Permission Denied
- [ ] Uninstall and reinstall app
- [ ] Open app
- [ ] Tap "Not Now" on rationale dialog
- [ ] Verify app continues to work normally
- [ ] Close and reopen app
- [ ] Verify rationale dialog appears again
- [ ] Tap "Not Now" again
- [ ] Deny permission in system dialog
- [ ] Close and reopen app multiple times
- [ ] After 2+ denials, verify settings dialog appears
- [ ] Tap "Open Settings"
- [ ] Verify app settings page opens
- [ ] Enable notifications manually
- [ ] Return to app
- [ ] Verify FCM token is saved

#### Permission Already Granted
- [ ] With permission already granted, close and reopen app
- [ ] Verify no permission dialogs appear
- [ ] Verify app starts normally
- [ ] Check logcat for FCM token save confirmation

### Android 12 and Below
- [ ] Run app on Android 12 or lower device/emulator
- [ ] Verify no permission dialogs appear
- [ ] Verify FCM token is saved automatically
- [ ] Verify notifications work by default

## Deep Linking Testing

### Chat Notifications

#### App Closed
- [ ] Close app completely (swipe away from recents)
- [ ] Send a test chat notification (use Firebase Console or backend)
- [ ] Notification data should include:
  ```json
  {
    "type": "chat",
    "chatId": "test_chat_id",
    "chatName": "Test Chat",
    "senderName": "Test User",
    "message": "Hello from notification"
  }
  ```
- [ ] Tap notification
- [ ] Verify app opens to ChatRoomActivity
- [ ] Verify correct chat is displayed
- [ ] Verify chat name in toolbar matches
- [ ] Verify messages are marked as read

#### App in Background
- [ ] Open app and navigate to Home screen
- [ ] Press home button (app in background)
- [ ] Send chat notification
- [ ] Tap notification
- [ ] Verify ChatRoomActivity opens
- [ ] Verify correct chat loads
- [ ] Press back button
- [ ] Verify returns to previous screen (Home)

#### App in Foreground (Different Screen)
- [ ] Open app and stay on Home screen
- [ ] Send chat notification
- [ ] Tap notification
- [ ] Verify ChatRoomActivity opens
- [ ] Verify correct chat loads

#### App Already in Chat
- [ ] Open ChatRoomActivity for Chat A
- [ ] Send notification for Chat B
- [ ] Tap notification
- [ ] Verify ChatRoomActivity updates to show Chat B
- [ ] Verify no duplicate activities created
- [ ] Press back button once
- [ ] Verify returns to previous screen (not Chat A)

### Task Notifications

#### View Task Action
- [ ] Close app
- [ ] Send task notification:
  ```json
  {
    "type": "task",
    "taskId": "test_task_id",
    "taskTitle": "Complete Report",
    "taskDescription": "Finish quarterly report",
    "dueDate": "Tomorrow",
    "priority": "High"
  }
  ```
- [ ] Tap notification body (not action button)
- [ ] Verify app opens to MainActivity
- [ ] Verify Tasks tab is selected
- [ ] Verify task list is visible

#### Mark Complete Action
- [ ] Send task notification
- [ ] Tap "Mark Complete" action button
- [ ] Verify app opens to MainActivity
- [ ] Verify Tasks tab is selected
- [ ] Verify task completion logic triggered (check logs)

### Group Notifications

#### Group Update
- [ ] Close app
- [ ] Send group notification:
  ```json
  {
    "type": "group",
    "groupId": "test_group_id",
    "groupName": "Test Group",
    "message": "New member added",
    "updateType": "member_added",
    "actionUserName": "John Doe"
  }
  ```
- [ ] Tap notification
- [ ] Verify app opens to MainActivity
- [ ] Verify Groups tab is selected
- [ ] Verify groups list is visible

## Notification Display Testing

### Chat Notification Appearance
- [ ] Send chat notification
- [ ] Verify notification appears on lock screen
- [ ] Verify shows sender name
- [ ] Verify shows message preview
- [ ] Verify shows chat name as title
- [ ] Verify notification icon is visible
- [ ] Verify notification color is blue
- [ ] Verify sound plays
- [ ] Verify device vibrates

### Task Notification Appearance
- [ ] Send task notification with priority "High"
- [ ] Verify notification shows 🔴 emoji
- [ ] Verify shows task title
- [ ] Verify shows due date
- [ ] Verify "Mark Complete" button visible
- [ ] Verify "View Task" button visible
- [ ] Verify notification color is orange

### Group Notification Appearance
- [ ] Send group notification with updateType "member_added"
- [ ] Verify notification shows 👥 emoji
- [ ] Verify shows group name
- [ ] Verify shows update message
- [ ] Verify shows "By [username]" if provided
- [ ] Verify notification color is green

## Edge Cases

### Invalid Data
- [ ] Send notification with missing chatId
- [ ] Verify app doesn't crash
- [ ] Verify error logged in logcat
- [ ] Send notification with invalid chatId
- [ ] Tap notification
- [ ] Verify app handles gracefully (shows error or returns to home)

### Multiple Notifications
- [ ] Send 3 chat notifications from different chats
- [ ] Verify notifications are grouped
- [ ] Verify summary notification shows "New Messages"
- [ ] Tap individual notification
- [ ] Verify correct chat opens
- [ ] Verify other notifications remain

### Notification While Permission Denied
- [ ] Deny notification permission
- [ ] Try to send notification
- [ ] Verify notification doesn't appear (expected)
- [ ] Verify app still works normally

### Back Navigation
- [ ] Open app to Home screen
- [ ] Tap chat notification
- [ ] ChatRoomActivity opens
- [ ] Press back button
- [ ] Verify returns to Home screen (not Login)
- [ ] Press back button again
- [ ] Verify app exits

### Rapid Notification Taps
- [ ] Send multiple notifications quickly
- [ ] Tap notifications rapidly
- [ ] Verify app doesn't crash
- [ ] Verify correct screens open
- [ ] Verify no duplicate activities

## Integration Testing

### With Existing Features
- [ ] Verify chat list still works
- [ ] Verify sending messages still works
- [ ] Verify task list still works
- [ ] Verify group list still works
- [ ] Verify all bottom navigation tabs work
- [ ] Verify logout still works

### FCM Token Management
- [ ] Grant notification permission
- [ ] Check Firestore user document
- [ ] Verify fcmToken field exists and has value
- [ ] Uninstall and reinstall app
- [ ] Grant permission again
- [ ] Verify fcmToken is updated in Firestore

## Performance Testing

### Memory
- [ ] Open app with permission granted
- [ ] Send 10 notifications
- [ ] Tap each notification
- [ ] Check Android Profiler for memory leaks
- [ ] Verify memory usage is reasonable

### Battery
- [ ] Enable notification permission
- [ ] Use app normally for 30 minutes
- [ ] Check battery usage in system settings
- [ ] Verify app battery usage is reasonable

## Accessibility Testing

### Screen Reader
- [ ] Enable TalkBack
- [ ] Open app
- [ ] Navigate through permission dialogs
- [ ] Verify all text is readable
- [ ] Verify buttons are labeled correctly
- [ ] Tap notification with TalkBack
- [ ] Verify navigation works

### Large Text
- [ ] Enable large text in system settings
- [ ] Open app
- [ ] Verify permission dialogs are readable
- [ ] Verify notification text is readable
- [ ] Verify no text is cut off

## Regression Testing

### Existing Functionality
- [ ] Login still works
- [ ] Registration still works
- [ ] Chat list loads
- [ ] Sending messages works
- [ ] Creating groups works
- [ ] Creating tasks works
- [ ] Calendar view works
- [ ] Profile view works
- [ ] Logout works

## Documentation Verification

- [ ] Review NotificationPermissionHelper.kt code comments
- [ ] Verify all public methods are documented
- [ ] Verify parameter descriptions are clear
- [ ] Review implementation summary document
- [ ] Verify all features are documented
- [ ] Verify testing instructions are clear

## Sign-Off

### Developer Checklist
- [ ] All code compiles without errors
- [ ] No lint warnings for new code
- [ ] All diagnostics resolved
- [ ] Code follows project conventions
- [ ] Proper error handling implemented
- [ ] Logging added for debugging

### Testing Checklist
- [ ] All permission scenarios tested
- [ ] All deep linking scenarios tested
- [ ] All notification types tested
- [ ] Edge cases handled
- [ ] No crashes observed
- [ ] Performance is acceptable

### Ready for Next Task
- [ ] Task 10 marked as complete
- [ ] Implementation summary created
- [ ] Verification checklist completed
- [ ] All files committed
- [ ] Ready to proceed to Task 11

---

## Notes
- Use Firebase Console to send test notifications: https://console.firebase.google.com/
- Navigate to: Project → Cloud Messaging → Send test message
- Or use backend API to send notifications programmatically
- Check logcat for detailed debugging information
- Test on both physical devices and emulators
- Test on different Android versions (API 23-34)
