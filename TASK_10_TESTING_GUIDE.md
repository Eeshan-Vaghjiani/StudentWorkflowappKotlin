# Task 10 Testing Guide: Quick Start

## Quick Test: Notification Permission

### Test on Android 13+ Device/Emulator

1. **First Launch Test**
   ```bash
   # Uninstall app first
   adb uninstall com.example.loginandregistration
   
   # Install and run
   ./gradlew installDebug
   ```
   
   - Open the app
   - You should see a dialog titled "Enable Notifications"
   - Dialog explains: "Stay connected with your team!"
   - Lists benefits: messages, task reminders, group activities
   - Tap "Allow" → System permission dialog appears
   - Grant permission
   - Check logcat: `adb logcat | grep FCM`
   - Should see: "FCM token saved successfully"

2. **Permission Denied Test**
   ```bash
   # Uninstall and reinstall
   adb uninstall com.example.loginandregistration
   ./gradlew installDebug
   ```
   
   - Open app
   - Tap "Not Now" on rationale dialog
   - App should continue working normally
   - Close and reopen app
   - Rationale dialog appears again
   - Deny permission in system dialog
   - Close and reopen app 2-3 times
   - After multiple denials, settings dialog should appear
   - Tap "Open Settings" to verify it opens system settings

3. **Already Granted Test**
   - With permission already granted, close and reopen app
   - No dialogs should appear
   - App starts normally

### Test on Android 12 or Lower

```bash
# Use Android 12 emulator
# No permission dialogs should appear
# FCM token should be saved automatically
```

## Quick Test: Deep Linking

### Method 1: Using Firebase Console (Easiest)

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. Navigate to: **Cloud Messaging** → **Send test message**
4. Fill in:
   - **Notification title**: "Test Chat"
   - **Notification text**: "Hello from notification"
   - **Target**: Select your device (must have app installed)
5. Click **Advanced options** → **Custom data**
6. Add key-value pairs:
   ```
   type: chat
   chatId: test_chat_123
   chatName: Test Chat
   senderName: Test User
   message: Hello from notification
   ```
7. Click **Test** → Select your device → **Test**
8. Tap the notification on your device
9. Verify ChatRoomActivity opens

### Method 2: Using ADB (For Quick Testing)

**Send Chat Notification:**
```bash
adb shell am start -n com.example.loginandregistration/.ChatRoomActivity \
  --es chatId "test_chat_123" \
  --es chatName "Test Chat" \
  --ez fromNotification true
```

**Send Task Notification:**
```bash
adb shell am start -n com.example.loginandregistration/.MainActivity \
  --es taskId "test_task_123" \
  --ez fromNotification true
```

**Send Group Notification:**
```bash
adb shell am start -n com.example.loginandregistration/.MainActivity \
  --es groupId "test_group_123" \
  --es groupName "Test Group" \
  --ez fromNotification true
```

### Method 3: Using Backend API (Most Realistic)

If you have a backend that sends FCM notifications, use this payload:

**Chat Notification:**
```json
{
  "to": "USER_FCM_TOKEN",
  "data": {
    "type": "chat",
    "chatId": "actual_chat_id",
    "chatName": "Project Team",
    "senderName": "John Doe",
    "message": "Hey, are you available?",
    "senderImageUrl": "https://...",
    "timestamp": "1234567890"
  }
}
```

**Task Notification:**
```json
{
  "to": "USER_FCM_TOKEN",
  "data": {
    "type": "task",
    "taskId": "actual_task_id",
    "taskTitle": "Complete Report",
    "taskDescription": "Finish quarterly report",
    "dueDate": "Tomorrow",
    "priority": "High"
  }
}
```

**Group Notification:**
```json
{
  "to": "USER_FCM_TOKEN",
  "data": {
    "type": "group",
    "groupId": "actual_group_id",
    "groupName": "Development Team",
    "message": "New member added to the group",
    "updateType": "member_added",
    "actionUserName": "Jane Smith"
  }
}
```

## Expected Results

### Permission Flow
✅ Rationale dialog shows clear explanation  
✅ System permission dialog appears after "Allow"  
✅ FCM token saved to Firestore after grant  
✅ App works without permission (no crashes)  
✅ Settings dialog appears after multiple denials  

### Deep Linking Flow
✅ Chat notification opens ChatRoomActivity  
✅ Correct chat loads with proper data  
✅ Messages marked as read automatically  
✅ Task notification navigates to tasks screen  
✅ Group notification navigates to groups screen  
✅ Back button navigation works correctly  

## Common Issues & Solutions

### Issue: Permission dialog doesn't appear
**Solution:** Make sure you're testing on Android 13+ (API 33+)
```bash
# Check Android version
adb shell getprop ro.build.version.sdk
# Should be 33 or higher
```

### Issue: Notification doesn't appear
**Solution:** Check if notifications are enabled
```bash
# Check notification settings
adb shell dumpsys notification | grep "com.example.loginandregistration"
```

### Issue: Deep link doesn't work
**Solution:** Check logcat for errors
```bash
adb logcat | grep -E "(MainActivity|ChatRoomActivity|NotificationHelper)"
```

### Issue: FCM token not saved
**Solution:** Check Firestore rules and authentication
```bash
# Check if user is authenticated
adb logcat | grep "FirebaseAuth"
```

## Debugging Tips

### View Logcat Filtered
```bash
# View all app logs
adb logcat | grep "com.example.loginandregistration"

# View FCM logs
adb logcat | grep "FCM"

# View notification logs
adb logcat | grep "NotificationHelper"

# View permission logs
adb logcat | grep "NotificationPermission"
```

### Check Firestore Data
1. Open Firebase Console
2. Go to Firestore Database
3. Navigate to `users` collection
4. Find your user document
5. Verify `fcmToken` field exists

### Test Notification Channels
```bash
# List notification channels
adb shell dumpsys notification | grep "com.example.loginandregistration"

# Should see:
# - chat_messages (HIGH importance)
# - task_reminders (DEFAULT importance)
# - group_updates (DEFAULT importance)
```

## Quick Verification (5 Minutes)

1. **Install app** (1 min)
   ```bash
   ./gradlew installDebug
   ```

2. **Test permission** (2 min)
   - Open app
   - Grant notification permission
   - Verify no crashes

3. **Test deep linking** (2 min)
   ```bash
   # Send test intent
   adb shell am start -n com.example.loginandregistration/.ChatRoomActivity \
     --es chatId "test" --es chatName "Test" --ez fromNotification true
   ```
   - Verify ChatRoomActivity opens
   - Verify no crashes

✅ **If all above works, Task 10 is successfully implemented!**

## Next Steps

After verifying Task 10:
1. Mark task as complete in tasks.md
2. Proceed to Task 11: Integrate FCM token management
3. Or test with real Firebase Cloud Messaging

## Need Help?

- Check `TASK_10_IMPLEMENTATION_SUMMARY.md` for detailed implementation
- Check `TASK_10_VERIFICATION_CHECKLIST.md` for comprehensive testing
- Review code comments in `NotificationPermissionHelper.kt`
- Check Firebase Console for notification delivery logs
