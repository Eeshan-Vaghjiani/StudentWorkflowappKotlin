# Known Issues and Limitations

This document outlines current limitations, known bugs, and planned improvements for the Team Collaboration App.

## Current Limitations

### 1. File Sharing Limitations

#### Document Uploads
- **Limitation**: Direct document file uploads are not supported
- **Workaround**: Share links to documents hosted on Google Drive, Dropbox, OneDrive, etc.
- **Reason**: Firestore document size limits and cost optimization
- **Status**: Working as designed

#### Image Storage
- **Limitation**: Images are stored as base64 in Firestore with 1MB limit per document
- **Impact**: Very large images may fail to upload
- **Workaround**: Images are automatically compressed to 70% quality and resized to max 800x800px
- **Status**: Working as designed

#### File Size Limits
- **Images**: Compressed to under 1MB before storage
- **Documents**: Links only (no size limit on external storage)
- **Profile Pictures**: Compressed to under 200KB
- **Status**: Working as designed

### 2. Message History Limitations

#### Pagination
- **Limitation**: Only last 1000 messages per chat are accessible
- **Impact**: Very old messages in active chats may not be retrievable
- **Workaround**: None currently
- **Status**: Performance optimization

#### Message Search
- **Limitation**: No search within messages
- **Impact**: Cannot search for specific message content
- **Workaround**: Scroll through chat history manually
- **Status**: Planned for future release

#### Message Export
- **Limitation**: Cannot export chat history
- **Impact**: No backup of conversations
- **Workaround**: Take screenshots if needed
- **Status**: Planned for future release

### 3. Group Limitations

#### Group Size
- **Limitation**: Recommended maximum 50 members per group
- **Impact**: Performance may degrade with larger groups
- **Reason**: Real-time listener performance and notification scaling
- **Status**: Soft limit (not enforced)

#### Group Discovery
- **Limitation**: No browse/search for public groups
- **Impact**: Must know join code to join groups
- **Workaround**: Share join codes via other channels
- **Status**: Planned for future release

#### Group Roles
- **Limitation**: Only two roles: Admin and Member
- **Impact**: Cannot create custom roles or permissions
- **Workaround**: Use multiple groups for different permission levels
- **Status**: Working as designed

### 4. Offline Mode Limitations

#### Data Availability
- **Limitation**: Only previously loaded data is available offline
- **Impact**: Cannot access new content while offline
- **Workaround**: Load all needed data while online
- **Status**: Firestore offline persistence limitation

#### Image Uploads
- **Limitation**: Cannot upload images while offline
- **Impact**: Image messages must wait until online
- **Workaround**: Queue message and send when connection restored
- **Status**: Technical limitation

#### Real-time Updates
- **Limitation**: No real-time updates while offline
- **Impact**: Don't see new messages/tasks until back online
- **Workaround**: Pull to refresh when connection restored
- **Status**: Expected behavior

### 5. Search Limitations

#### User Search
- **Limitation**: Can only search users by exact email match
- **Impact**: Must know user's email to start direct chat
- **Workaround**: Ask for email via other channels
- **Status**: Firestore query limitation

#### Content Search
- **Limitation**: No full-text search for tasks, messages, or groups
- **Impact**: Cannot search across content
- **Workaround**: Use filters and manual browsing
- **Status**: Planned for future release

### 6. Notification Limitations

#### Notification Grouping
- **Limitation**: Notifications are grouped by chat, not by app
- **Impact**: Multiple notification entries for different chats
- **Workaround**: None
- **Status**: Android limitation

#### Notification Actions
- **Limitation**: Limited action buttons on notifications
- **Impact**: Cannot reply directly from notification
- **Workaround**: Tap notification to open app
- **Status**: Planned for future release

#### Notification Customization
- **Limitation**: Cannot customize notification sound per chat
- **Impact**: All chats use same notification sound
- **Workaround**: Use Android notification channels
- **Status**: Working as designed

### 7. Security Limitations

#### End-to-End Encryption
- **Limitation**: Messages are not end-to-end encrypted
- **Impact**: Firebase administrators can theoretically access messages
- **Workaround**: Don't share highly sensitive information
- **Status**: Planned for future release

#### Message Deletion
- **Limitation**: Deleted messages are only removed from UI, not Firestore
- **Impact**: Messages may still exist in database
- **Workaround**: Don't send sensitive information
- **Status**: Known issue (see below)

#### Password Reset
- **Limitation**: No in-app password reset functionality
- **Impact**: Must contact administrator to reset password
- **Workaround**: Use Firebase Console for password reset
- **Status**: Planned for future release

### 8. Platform Limitations

#### Android Only
- **Limitation**: No iOS, web, or desktop versions
- **Impact**: Can only use on Android devices
- **Workaround**: None
- **Status**: Working as designed

#### Multi-Device Sync
- **Limitation**: No sync between multiple devices for same account
- **Impact**: Each device has separate offline cache
- **Workaround**: Use one primary device
- **Status**: Firestore limitation

#### Tablet Optimization
- **Limitation**: UI is optimized for phones, not tablets
- **Impact**: May not use tablet screen space efficiently
- **Workaround**: Use in portrait mode
- **Status**: Planned for future release

### 9. Media Limitations

#### Video/Audio Calls
- **Limitation**: No video or audio calling support
- **Impact**: Cannot make calls within app
- **Workaround**: Use external calling apps
- **Status**: Planned for future release

#### Voice Messages
- **Limitation**: Cannot send voice messages
- **Impact**: Must type messages
- **Workaround**: Use external voice recording and share link
- **Status**: Planned for future release

#### GIF/Sticker Support
- **Limitation**: No built-in GIF or sticker support
- **Impact**: Cannot send animated content
- **Workaround**: Share GIF links
- **Status**: Not planned

### 10. Localization Limitations

#### Language Support
- **Limitation**: App is English only
- **Impact**: Non-English speakers may have difficulty
- **Workaround**: Use device translation features
- **Status**: Planned for future release

#### Date/Time Formats
- **Limitation**: Uses device locale for date/time formatting
- **Impact**: May not match user preference
- **Workaround**: Change device locale settings
- **Status**: Working as designed

## Known Bugs

### High Priority

#### 1. Typing Indicator Stuck
- **Description**: Typing indicator may remain visible if user closes app while typing
- **Impact**: Shows "User is typing..." indefinitely
- **Workaround**: Refresh chat or wait 30 seconds for timeout
- **Status**: Fix in progress
- **Affected Versions**: All versions

#### 2. Read Receipts Delay
- **Description**: Read receipts may not update immediately in poor network conditions
- **Impact**: Message shows as "Delivered" when actually read
- **Workaround**: Wait for network to stabilize
- **Status**: Investigating
- **Affected Versions**: All versions

### Medium Priority

#### 3. Calendar Dots Not Appearing
- **Description**: Calendar may not show dots for tasks until refreshed
- **Impact**: Tasks exist but calendar looks empty
- **Workaround**: Pull down to refresh calendar
- **Status**: Fix planned for next release
- **Affected Versions**: 1.0.0

#### 4. Profile Picture Upload Fails for Large Images
- **Description**: Very large images (>5MB original) may fail to compress and upload
- **Impact**: Profile picture doesn't update
- **Workaround**: Use smaller images or compress before selecting
- **Status**: Improved compression in progress
- **Affected Versions**: All versions

#### 5. Notification Sound on Custom ROMs
- **Description**: Notification sound may not play on some devices with custom Android ROMs
- **Impact**: Silent notifications
- **Workaround**: Set custom notification sound in device settings
- **Status**: Device-specific issue
- **Affected Versions**: All versions

### Low Priority

#### 6. Empty State Flicker
- **Description**: Empty state view may briefly appear before data loads
- **Impact**: Visual flicker on screen
- **Workaround**: None needed
- **Status**: UI optimization planned
- **Affected Versions**: 1.0.0

#### 7. Keyboard Overlap on Small Screens
- **Description**: Keyboard may overlap message input on very small screens (<4.5")
- **Impact**: Cannot see what you're typing
- **Workaround**: Use landscape mode or larger device
- **Status**: Low priority (rare device size)
- **Affected Versions**: All versions

#### 8. Dark Mode Image Contrast
- **Description**: Some images may have poor contrast in dark mode
- **Impact**: Images harder to see
- **Workaround**: Switch to light mode temporarily
- **Status**: UI refinement planned
- **Affected Versions**: 1.0.0

#### 9. Swipe-to-Refresh Sensitivity
- **Description**: Swipe-to-refresh may trigger too easily when scrolling
- **Impact**: Unintended refreshes
- **Workaround**: Scroll more carefully
- **Status**: Sensitivity adjustment planned
- **Affected Versions**: 1.0.0

#### 10. Message Timestamp Timezone
- **Description**: Message timestamps use device timezone, not sender's timezone
- **Impact**: Timestamps may be confusing for users in different timezones
- **Workaround**: Be aware of timezone differences
- **Status**: Working as designed
- **Affected Versions**: All versions

## Performance Issues

### 1. Large Group Chat Performance
- **Description**: Chats with 100+ members may load slowly
- **Impact**: Delay when opening chat
- **Workaround**: Use smaller groups or direct messages
- **Status**: Optimization planned

### 2. Image Loading in Long Chats
- **Description**: Scrolling through chats with many images may be slow
- **Impact**: Lag when scrolling
- **Workaround**: Clear image cache periodically
- **Status**: Improved caching planned

### 3. Calendar with Many Tasks
- **Description**: Calendar may be slow with 500+ tasks
- **Impact**: Delay when switching months
- **Workaround**: Archive completed tasks
- **Status**: Pagination planned

### 4. Initial App Launch
- **Description**: First launch after install may take 5-10 seconds
- **Impact**: Longer wait time
- **Workaround**: None needed (one-time only)
- **Status**: Expected behavior

## Compatibility Issues

### Android Version Compatibility

#### Android 6.0 (API 23)
- **Status**: Supported
- **Known Issues**: 
  - Runtime permissions may require manual grant
  - Some Material Design animations not available

#### Android 7.0-7.1 (API 24-25)
- **Status**: Fully supported
- **Known Issues**: None

#### Android 8.0-8.1 (API 26-27)
- **Status**: Fully supported
- **Known Issues**: 
  - Notification channels required (handled automatically)

#### Android 9.0 (API 28)
- **Status**: Fully supported
- **Known Issues**: None

#### Android 10 (API 29)
- **Status**: Fully supported
- **Known Issues**:
  - Scoped storage restrictions (handled automatically)

#### Android 11 (API 30)
- **Status**: Fully supported
- **Known Issues**: None

#### Android 12 (API 31)
- **Status**: Fully supported
- **Known Issues**:
  - Splash screen API differences (handled automatically)

#### Android 13 (API 33)
- **Status**: Fully supported
- **Known Issues**:
  - Notification permission required (prompted automatically)
  - Photo picker changes (handled automatically)

#### Android 14 (API 34)
- **Status**: Fully supported
- **Known Issues**: None

### Device-Specific Issues

#### Samsung Devices
- **Issue**: Battery optimization may stop background sync
- **Workaround**: Disable battery optimization for app
- **Status**: Device manufacturer limitation

#### Xiaomi/MIUI Devices
- **Issue**: Notifications may not appear due to aggressive battery management
- **Workaround**: Add app to autostart list and disable battery optimization
- **Status**: Device manufacturer limitation

#### Huawei Devices (without Google Services)
- **Issue**: App will not work (requires Google Play Services)
- **Workaround**: None
- **Status**: Firebase dependency

#### OnePlus Devices
- **Issue**: Notifications may be delayed
- **Workaround**: Disable battery optimization
- **Status**: Device manufacturer limitation

## Planned Improvements

### Short Term (Next Release)

1. **Fix typing indicator stuck issue**
2. **Improve calendar dot refresh**
3. **Better image compression for large files**
4. **UI refinements for dark mode**
5. **Performance optimization for large groups**

### Medium Term (Next 3 Months)

1. **Message search functionality**
2. **Group discovery and browsing**
3. **Password reset in-app**
4. **Chat history export**
5. **Improved offline mode**
6. **Notification reply actions**
7. **Custom notification sounds per chat**

### Long Term (6+ Months)

1. **End-to-end encryption**
2. **Video/audio calling**
3. **Voice messages**
4. **Multi-language support**
5. **Tablet-optimized layouts**
6. **Web version**
7. **iOS version**
8. **Advanced search and filters**
9. **Custom group roles**
10. **Backup and restore**

## Reporting Issues

### How to Report a Bug

1. **Check this document** to see if it's a known issue
2. **Gather information**:
   - Android version
   - Device model
   - App version
   - Steps to reproduce
   - Screenshots (if applicable)
3. **Contact development team** with details
4. **Include logs** if app crashes

### What to Include

- **Clear description** of the issue
- **Expected behavior** vs actual behavior
- **Steps to reproduce** the issue
- **Frequency**: Does it happen every time or occasionally?
- **Impact**: How does it affect your usage?
- **Workaround**: Have you found any temporary solution?

### Priority Levels

- **Critical**: App crashes or data loss
- **High**: Feature completely broken
- **Medium**: Feature partially broken or workaround exists
- **Low**: Minor UI issue or rare occurrence

## Workarounds Summary

### Quick Reference

| Issue | Workaround |
|-------|-----------|
| Typing indicator stuck | Refresh chat or wait 30 seconds |
| Calendar dots missing | Pull to refresh |
| Large image upload fails | Compress image first |
| Notifications not working | Check permissions and battery optimization |
| Messages not sending | Check internet connection |
| App is slow | Clear cache in device settings |
| Offline mode not working | Load data while online first |
| Can't find user | Search by exact email address |
| Group chat slow | Use smaller groups |
| Images not loading | Clear app cache |

## Version History

### Version 1.0.0 (Current)
- Initial release
- All core features implemented
- Known issues documented above

## Feedback

We value your feedback! If you encounter issues not listed here or have suggestions for improvements, please contact the development team.

---

**Last Updated**: January 2025  
**Document Version**: 1.0.0  
**App Version**: 1.0.0
