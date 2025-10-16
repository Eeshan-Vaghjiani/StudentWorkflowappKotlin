# Troubleshooting Guide

Comprehensive troubleshooting guide for the Team Collaboration App. Find solutions to common problems and error messages.

## Table of Contents

1. [Installation and Setup Issues](#installation-and-setup-issues)
2. [Authentication Problems](#authentication-problems)
3. [Connection and Sync Issues](#connection-and-sync-issues)
4. [Chat and Messaging Issues](#chat-and-messaging-issues)
5. [Task and Calendar Issues](#task-and-calendar-issues)
6. [Notification Problems](#notification-problems)
7. [Image and File Issues](#image-and-file-issues)
8. [Performance Issues](#performance-issues)
9. [Error Messages](#error-messages)
10. [Device-Specific Issues](#device-specific-issues)

## Installation and Setup Issues

### App Won't Install

**Problem**: Cannot install APK or app from Play Store

**Possible Causes**:
- Insufficient storage space
- Incompatible Android version
- Installation from unknown sources disabled
- Corrupted APK file

**Solutions**:

1. **Check storage space**:
   - Go to Settings → Storage
   - Ensure at least 100 MB free space
   - Delete unnecessary files or apps

2. **Check Android version**:
   - Go to Settings → About Phone
   - Check Android version
   - App requires Android 6.0 (API 23) or higher

3. **Enable unknown sources** (for APK installation):
   - Go to Settings → Security
   - Enable "Unknown sources" or "Install unknown apps"
   - Grant permission for your file manager

4. **Re-download APK**:
   - Delete current APK file
   - Download again from trusted source
   - Verify file size matches expected size

### App Crashes on Launch

**Problem**: App closes immediately after opening

**Possible Causes**:
- Missing Google Play Services
- Corrupted installation
- Incompatible device
- Missing google-services.json

**Solutions**:

1. **Update Google Play Services**:
   - Open Play Store
   - Search "Google Play Services"
   - Update if available

2. **Clear app data**:
   - Go to Settings → Apps → Team Collaboration
   - Tap "Storage"
   - Tap "Clear data" and "Clear cache"
   - Reopen app

3. **Reinstall app**:
   - Uninstall app completely
   - Restart device
   - Reinstall app
   - Try again

4. **Check device compatibility**:
   - Ensure device has Google Play Services
   - Huawei devices without GMS won't work

### Gradle Build Fails

**Problem**: Cannot build app in Android Studio

**Possible Causes**:
- Missing google-services.json
- Gradle sync issues
- Dependency conflicts
- Internet connection problems

**Solutions**:

1. **Verify google-services.json**:
   ```
   app/
   └── google-services.json  ← Must be here
   ```
   - File must be in `app/` directory
   - File name must be exact (lowercase)

2. **Sync Gradle**:
   - File → Sync Project with Gradle Files
   - Wait for sync to complete
   - Check for errors in Build output

3. **Clean and rebuild**:
   - Build → Clean Project
   - Wait for completion
   - Build → Rebuild Project

4. **Invalidate caches**:
   - File → Invalidate Caches / Restart
   - Select "Invalidate and Restart"
   - Wait for Android Studio to restart

5. **Check internet connection**:
   - Ensure stable internet for dependency downloads
   - Try using VPN if behind firewall
   - Check proxy settings if applicable

## Authentication Problems

### Cannot Register New Account

**Problem**: Registration fails with error message

**Possible Causes**:
- Invalid email format
- Weak password
- Email already registered
- No internet connection
- Firebase Authentication not enabled

**Solutions**:

1. **Check email format**:
   - Must be valid email: `user@example.com`
   - No spaces or special characters
   - Must include @ and domain

2. **Check password requirements**:
   - Minimum 6 characters
   - Recommended: 8+ characters
   - Mix of letters, numbers, symbols

3. **Try different email**:
   - Email may already be registered
   - Use a different email address
   - Or try logging in instead

4. **Check internet connection**:
   - Ensure WiFi or mobile data is on
   - Try opening a website to verify
   - Switch between WiFi and mobile data

5. **Verify Firebase setup**:
   - Check Firebase Console
   - Ensure Authentication is enabled
   - Verify Email/Password provider is enabled

### Cannot Login

**Problem**: Login fails with "Authentication failed" error

**Possible Causes**:
- Incorrect email or password
- Account doesn't exist
- No internet connection
- Firebase Authentication issues

**Solutions**:

1. **Verify credentials**:
   - Check email spelling carefully
   - Check password (case-sensitive)
   - Ensure no extra spaces

2. **Register if new user**:
   - Tap "Register" instead
   - Create a new account
   - Then try logging in

3. **Check internet connection**:
   - Ensure WiFi or mobile data is on
   - Try opening a website
   - Check signal strength

4. **Wait and retry**:
   - Firebase may be temporarily down
   - Wait 5 minutes
   - Try again

5. **Contact administrator**:
   - If password forgotten
   - Request password reset
   - Or create new account

### Automatically Logged Out

**Problem**: App logs out unexpectedly

**Possible Causes**:
- Session expired
- App data cleared
- Firebase token expired
- Device restarted

**Solutions**:

1. **Simply log back in**:
   - Enter your credentials
   - Tap "Login"
   - Should work normally

2. **Check "Remember me"** (if available):
   - Enable to stay logged in
   - Note: Not currently implemented

3. **Don't clear app data**:
   - Clearing data logs you out
   - Only clear if troubleshooting

4. **Update app**:
   - Check for app updates
   - Install latest version
   - May fix session issues

## Connection and Sync Issues

### "No Internet Connection" Banner

**Problem**: Banner shows even when connected

**Possible Causes**:
- Weak or unstable connection
- Firewall blocking Firebase
- DNS issues
- Airplane mode on

**Solutions**:

1. **Check connection**:
   - Open a website in browser
   - Verify internet actually works
   - Check WiFi or mobile data icon

2. **Toggle airplane mode**:
   - Turn on airplane mode
   - Wait 10 seconds
   - Turn off airplane mode
   - Wait for reconnection

3. **Switch networks**:
   - Switch from WiFi to mobile data
   - Or vice versa
   - See if connection improves

4. **Restart device**:
   - Power off device completely
   - Wait 30 seconds
   - Power on
   - Reopen app

5. **Check firewall**:
   - Ensure Firebase domains not blocked
   - Try different network
   - Disable VPN temporarily

### Data Not Syncing

**Problem**: Changes don't appear on other devices

**Possible Causes**:
- Offline mode active
- Firestore rules blocking sync
- Cache not cleared
- Different accounts

**Solutions**:

1. **Check connection**:
   - Ensure "No internet" banner is not showing
   - Verify internet connection works
   - Try pull-to-refresh

2. **Force refresh**:
   - Pull down on any list
   - Wait for refresh to complete
   - Check if data appears

3. **Verify same account**:
   - Check email in Profile tab
   - Ensure same account on all devices
   - Logout and login if needed

4. **Clear cache**:
   - Settings → Apps → Team Collaboration
   - Tap "Storage"
   - Tap "Clear cache" (not Clear data)
   - Reopen app

5. **Check Firestore rules**:
   - Verify rules are deployed
   - Check Firebase Console for errors
   - Review security rules

### Offline Mode Not Working

**Problem**: Cannot access data offline

**Possible Causes**:
- Data not loaded while online
- Offline persistence not enabled
- Cache cleared
- First time using app

**Solutions**:

1. **Load data while online first**:
   - Connect to internet
   - Open all chats you need
   - Scroll through messages
   - Open groups and tasks
   - Then go offline

2. **Verify offline persistence**:
   - Check `FirestoreConfig.kt`
   - Ensure `isPersistenceEnabled = true`
   - Rebuild app if changed

3. **Don't clear cache**:
   - Clearing cache removes offline data
   - Only clear if troubleshooting
   - Reload data after clearing

4. **Wait for initial sync**:
   - First launch requires internet
   - Wait for all data to load
   - Then offline mode will work

## Chat and Messaging Issues

### Messages Not Sending

**Problem**: Messages stuck in "Sending..." status

**Possible Causes**:
- No internet connection
- Firestore rules blocking write
- Message too long
- Chat doesn't exist

**Solutions**:

1. **Check internet connection**:
   - Look for "No internet" banner
   - Verify connection works
   - Messages will send when online

2. **Check message length**:
   - Maximum 5000 characters
   - Shorten message if too long
   - Split into multiple messages

3. **Retry sending**:
   - Long-press the message
   - Tap "Retry" if available
   - Or delete and resend

4. **Check Firestore rules**:
   - Verify you're a chat participant
   - Check Firebase Console for errors
   - Review security rules

5. **Restart app**:
   - Close app completely
   - Reopen app
   - Try sending again

### Messages Not Receiving

**Problem**: Not seeing new messages from others

**Possible Causes**:
- No internet connection
- Real-time listener not attached
- Firestore rules blocking read
- App in background too long

**Solutions**:

1. **Pull to refresh**:
   - Pull down on chat list
   - Or pull down in chat room
   - Wait for refresh

2. **Reopen chat**:
   - Go back to chat list
   - Reopen the chat
   - Messages should appear

3. **Check internet**:
   - Verify connection is active
   - Look for "No internet" banner
   - Try switching networks

4. **Restart app**:
   - Close app completely
   - Reopen app
   - Messages should sync

5. **Verify participant**:
   - Ensure you're still in the chat
   - Check you weren't removed
   - Verify in Firebase Console

### Typing Indicator Stuck

**Problem**: Shows "User is typing..." indefinitely

**Possible Causes**:
- User closed app while typing
- Network issue
- Firestore listener not updating

**Solutions**:

1. **Wait 30 seconds**:
   - Indicator should timeout automatically
   - If not, proceed to next step

2. **Refresh chat**:
   - Go back to chat list
   - Reopen the chat
   - Indicator should clear

3. **Restart app**:
   - Close app completely
   - Reopen app
   - Indicator should be gone

4. **Ignore it**:
   - Known issue
   - Doesn't affect functionality
   - Will clear eventually

### Read Receipts Not Updating

**Problem**: Messages show as "Delivered" but were read

**Possible Causes**:
- Poor network connection
- Firestore update delay
- Recipient offline

**Solutions**:

1. **Wait a moment**:
   - Updates may take a few seconds
   - Especially on slow connections
   - Check again in 10 seconds

2. **Check recipient is online**:
   - They may not have opened the message
   - Or they're offline
   - Receipts update when they read it

3. **Refresh chat**:
   - Pull down to refresh
   - Or reopen chat
   - Status should update

4. **Verify internet**:
   - Ensure stable connection
   - Poor connection delays updates
   - Try better network

## Task and Calendar Issues

### Tasks Not Appearing

**Problem**: Created tasks don't show in list

**Possible Causes**:
- Wrong filter selected
- Task assigned to someone else
- Task in different group
- Sync issue

**Solutions**:

1. **Check filters**:
   - Tap "All" chip at top
   - Ensure not filtering by category
   - Check "Show Completed" toggle

2. **Verify assignment**:
   - Open task details
   - Check who it's assigned to
   - Use "My Tasks" filter to see yours

3. **Check group**:
   - Group tasks only show in that group
   - Go to Groups tab
   - Open the group
   - Check tasks there

4. **Pull to refresh**:
   - Pull down on task list
   - Wait for refresh
   - Task should appear

5. **Restart app**:
   - Close and reopen app
   - Check task list again

### Calendar Not Showing Task Dots

**Problem**: Calendar looks empty but tasks exist

**Possible Causes**:
- Tasks don't have due dates
- Wrong filter selected
- Calendar not refreshed
- Date range issue

**Solutions**:

1. **Check task due dates**:
   - Open Tasks tab
   - Verify tasks have due dates set
   - Only tasks with dates show on calendar

2. **Pull to refresh**:
   - Pull down on calendar
   - Wait for refresh
   - Dots should appear

3. **Check filters**:
   - Tap "All Tasks" chip
   - Ensure not filtering incorrectly
   - Try different filters

4. **Navigate months**:
   - Swipe to different month
   - Swipe back
   - Dots may appear after refresh

5. **Restart app**:
   - Close and reopen app
   - Calendar should reload

### Cannot Complete Task

**Problem**: "Mark as Complete" button doesn't work

**Possible Causes**:
- No internet connection
- Firestore rules blocking update
- Not assigned to task
- Task already completed

**Solutions**:

1. **Check internet**:
   - Ensure connection is active
   - Task will complete when online
   - Check for "No internet" banner

2. **Verify assignment**:
   - Check you're assigned to task
   - Only assigned users can complete
   - Or task creator

3. **Check status**:
   - Task may already be completed
   - Check status field
   - Refresh task details

4. **Try checkbox**:
   - Use checkbox in task list instead
   - May work when button doesn't
   - Or vice versa

5. **Restart app**:
   - Close and reopen app
   - Try completing again

## Notification Problems

### Not Receiving Notifications

**Problem**: No notifications for new messages or tasks

**Possible Causes**:
- Notification permission denied
- Battery optimization enabled
- Do Not Disturb mode on
- Notification channel disabled
- FCM token not saved

**Solutions**:

1. **Check notification permission**:
   - Settings → Apps → Team Collaboration
   - Tap "Notifications"
   - Ensure "Allow notifications" is ON
   - Enable all channels

2. **Disable battery optimization**:
   - Settings → Apps → Team Collaboration
   - Tap "Battery"
   - Select "Don't optimize" or "Unrestricted"
   - Confirm change

3. **Check Do Not Disturb**:
   - Swipe down notification shade
   - Check if DND is enabled
   - Disable or allow app in DND settings

4. **Check notification channels**:
   - Settings → Apps → Team Collaboration → Notifications
   - Ensure "Chat Messages" is enabled
   - Ensure "Task Reminders" is enabled
   - Check sound and vibration settings

5. **Restart app**:
   - Close app completely
   - Reopen app
   - FCM token will refresh

6. **Reinstall app**:
   - Uninstall app
   - Restart device
   - Reinstall app
   - Grant notification permission

### Notifications Delayed

**Problem**: Notifications arrive late or not at all

**Possible Causes**:
- Battery optimization
- Doze mode
- Network issues
- Device manufacturer restrictions

**Solutions**:

1. **Disable battery optimization** (see above)

2. **Add to autostart** (Xiaomi, Huawei, etc.):
   - Settings → Apps → Team Collaboration
   - Find "Autostart" or "Startup"
   - Enable autostart
   - Restart device

3. **Lock app in recents**:
   - Open recent apps
   - Find Team Collaboration
   - Swipe down or tap lock icon
   - Prevents app from being killed

4. **Check network**:
   - Ensure stable internet connection
   - FCM requires internet
   - Try WiFi instead of mobile data

5. **Device-specific settings**:
   - Samsung: Disable "Put app to sleep"
   - Xiaomi: Disable "Battery saver"
   - Huawei: Add to "Protected apps"
   - OnePlus: Disable "Battery optimization"

### Notification Sound Not Playing

**Problem**: Notifications appear but no sound

**Possible Causes**:
- Device on silent/vibrate
- Notification channel sound disabled
- Do Not Disturb mode
- Custom ROM issue

**Solutions**:

1. **Check device volume**:
   - Press volume up button
   - Ensure not on silent or vibrate
   - Check notification volume specifically

2. **Check notification sound**:
   - Settings → Apps → Team Collaboration → Notifications
   - Tap "Chat Messages" channel
   - Tap "Sound"
   - Select a sound
   - Test it

3. **Check Do Not Disturb**:
   - Disable DND mode
   - Or allow priority notifications
   - Add app to priority list

4. **Try different sound**:
   - Change notification sound
   - Use default system sound
   - Test with different sounds

## Image and File Issues

### Images Not Uploading

**Problem**: Image upload fails or gets stuck

**Possible Causes**:
- Image too large
- No internet connection
- Storage permission denied
- Compression failed

**Solutions**:

1. **Check image size**:
   - Original image should be under 5MB
   - Very large images may fail
   - Try smaller image

2. **Check internet**:
   - Ensure stable connection
   - Upload requires internet
   - Try WiFi for better speed

3. **Check permission**:
   - Settings → Apps → Team Collaboration → Permissions
   - Ensure "Storage" or "Photos" is allowed
   - Grant permission if denied

4. **Compress image first**:
   - Use gallery app to reduce size
   - Or use image editor
   - Then try uploading

5. **Try different image**:
   - Test with a smaller image
   - If works, original image is the issue
   - Use compressed version

### Images Not Loading

**Problem**: Images show as blank or don't display

**Possible Causes**:
- No internet connection
- Image data corrupted
- Cache full
- Base64 decode error

**Solutions**:

1. **Check internet**:
   - Images require internet to load
   - Unless previously cached
   - Connect to internet

2. **Clear image cache**:
   - Settings → Apps → Team Collaboration
   - Tap "Storage"
   - Tap "Clear cache"
   - Reopen app

3. **Refresh chat**:
   - Go back to chat list
   - Reopen chat
   - Images should reload

4. **Restart app**:
   - Close app completely
   - Reopen app
   - Images should load

5. **Re-upload image**:
   - If you sent it, delete message
   - Upload image again
   - May fix corruption

### Profile Picture Not Updating

**Problem**: Profile picture doesn't change after upload

**Possible Causes**:
- Upload failed
- Image too large
- Cache not cleared
- Firestore update failed

**Solutions**:

1. **Check upload completion**:
   - Wait for upload to finish
   - Look for progress indicator
   - Don't close screen too early

2. **Use smaller image**:
   - Profile pictures compressed to 200KB
   - Very large images may fail
   - Try smaller image

3. **Clear cache**:
   - Settings → Apps → Team Collaboration
   - Tap "Storage"
   - Tap "Clear cache"
   - Reopen app

4. **Restart app**:
   - Close app completely
   - Reopen app
   - Picture should update

5. **Try again**:
   - Delete current picture (if possible)
   - Upload new picture
   - Wait for completion

## Performance Issues

### App is Slow or Laggy

**Problem**: App responds slowly to interactions

**Possible Causes**:
- Low device memory
- Cache too large
- Too many background apps
- Old device

**Solutions**:

1. **Clear app cache**:
   - Settings → Apps → Team Collaboration
   - Tap "Storage"
   - Tap "Clear cache"
   - Reopen app

2. **Close background apps**:
   - Open recent apps
   - Swipe away unused apps
   - Free up memory

3. **Restart device**:
   - Power off device
   - Wait 30 seconds
   - Power on
   - Reopen app

4. **Free up storage**:
   - Delete unnecessary files
   - Uninstall unused apps
   - Ensure 500MB+ free space

5. **Update app**:
   - Check for app updates
   - Install latest version
   - May include performance fixes

### High Battery Usage

**Problem**: App drains battery quickly

**Possible Causes**:
- Background sync enabled
- Too many notifications
- Real-time listeners active
- Location services on

**Solutions**:

1. **Disable background sync**:
   - Close app when not using
   - Don't leave running in background
   - Logout if not using for extended period

2. **Reduce notifications**:
   - Settings → Apps → Team Collaboration → Notifications
   - Disable non-essential channels
   - Reduce notification frequency

3. **Use dark mode**:
   - Enable dark mode in device settings
   - Saves battery on OLED screens
   - App respects system theme

4. **Close app properly**:
   - Don't just minimize app
   - Use back button to exit
   - Or force stop in settings

5. **Check battery usage**:
   - Settings → Battery
   - Check app usage
   - If excessive, reinstall app

### App Crashes Frequently

**Problem**: App closes unexpectedly

**Possible Causes**:
- Low memory
- Corrupted data
- Bug in app
- Incompatible device

**Solutions**:

1. **Clear app data**:
   - Settings → Apps → Team Collaboration
   - Tap "Storage"
   - Tap "Clear data" (will logout)
   - Reopen app and login

2. **Update app**:
   - Check for updates
   - Install latest version
   - May fix crash bugs

3. **Free up memory**:
   - Close background apps
   - Delete unnecessary files
   - Restart device

4. **Reinstall app**:
   - Uninstall app completely
   - Restart device
   - Reinstall app
   - Login again

5. **Report crash**:
   - Note what you were doing
   - Contact development team
   - Provide device and Android version

## Error Messages

### "Permission Denied"

**Meaning**: Firestore security rules blocked the operation

**Solutions**:
1. Verify you're logged in
2. Check you have access to the resource
3. Verify Firestore rules are deployed correctly
4. Logout and login again
5. Contact administrator if persists

### "Network Error"

**Meaning**: Cannot connect to Firebase services

**Solutions**:
1. Check internet connection
2. Try different network
3. Disable VPN temporarily
4. Check Firebase status page
5. Wait and retry

### "Authentication Failed"

**Meaning**: Login credentials are incorrect

**Solutions**:
1. Verify email and password
2. Check for typos
3. Try registering if new user
4. Contact administrator for password reset
5. Check internet connection

### "Storage Limit Exceeded"

**Meaning**: Firestore document size limit (1MB) exceeded

**Solutions**:
1. Use smaller images
2. Compress images more
3. Don't send very large files
4. Use document links instead
5. Contact administrator

### "Invalid Token"

**Meaning**: FCM token expired or invalid

**Solutions**:
1. Logout and login again
2. Clear app data
3. Reinstall app
4. Check internet connection
5. Wait for token refresh

### "Index Not Found"

**Meaning**: Required Firestore index doesn't exist

**Solutions**:
1. Check Firebase Console for index creation link
2. Click link to create index
3. Wait 2-5 minutes for index to build
4. Retry operation
5. Contact administrator

## Device-Specific Issues

### Samsung Devices

**Issue**: App stops working in background

**Solution**:
1. Settings → Apps → Team Collaboration
2. Tap "Battery"
3. Select "Unrestricted"
4. Disable "Put app to sleep"
5. Add to "Never sleeping apps"

### Xiaomi/MIUI Devices

**Issue**: Notifications not appearing

**Solution**:
1. Settings → Apps → Manage apps → Team Collaboration
2. Enable "Autostart"
3. Battery saver → No restrictions
4. Permissions → Enable all
5. Lock app in recents

### Huawei Devices

**Issue**: App won't work (no Google Services)

**Solution**:
- App requires Google Play Services
- Huawei devices without GMS cannot run app
- No workaround available
- Use different device

### OnePlus Devices

**Issue**: Notifications delayed

**Solution**:
1. Settings → Apps → Team Collaboration
2. Battery → Don't optimize
3. Advanced → Allow background activity
4. Lock app in recents
5. Disable "Adaptive Battery"

### Pixel Devices

**Issue**: Generally works well, no specific issues

**Note**: Pixel devices have stock Android and typically work best

## Getting More Help

If your issue isn't resolved:

1. **Check documentation**:
   - README.md
   - USER_GUIDE.md
   - KNOWN_ISSUES_AND_LIMITATIONS.md

2. **Search online**:
   - Stack Overflow
   - Android documentation
   - Firebase documentation

3. **Contact support**:
   - Provide detailed description
   - Include device and Android version
   - Include steps to reproduce
   - Include screenshots if applicable

4. **Report bugs**:
   - Use issue reporting system
   - Provide logs if available
   - Note frequency of issue

---

**Still having issues? Contact the development team for assistance.**
