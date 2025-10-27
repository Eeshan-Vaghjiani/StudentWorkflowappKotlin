# Task 30: Offline Image Caching - Testing Guide

## Prerequisites
- App installed on device or emulator
- Firebase configured with test data
- At least 2 user accounts for testing
- Some chats with messages containing images
- Some users with profile pictures

## Test Environment Setup

### Option 1: Android Emulator
1. Open Android Studio
2. Start an emulator (API 23+)
3. Install the app: `./gradlew installDebug`
4. Enable/disable network via emulator controls

### Option 2: Physical Device
1. Connect device via USB
2. Enable Developer Options and USB Debugging
3. Install the app: `./gradlew installDebug`
4. Use airplane mode to test offline functionality

## Detailed Test Cases

### Test Case 1: Profile Picture Caching in Chat List

**Objective:** Verify profile pictures are cached and display offline

**Steps:**
1. Launch app and login
2. Navigate to Chat tab
3. Observe chat list with profile pictures loading
4. Wait for all images to load completely
5. Enable airplane mode on device
6. Navigate to another tab (Tasks or Groups)
7. Navigate back to Chat tab
8. Scroll through chat list

**Expected Results:**
- ✅ Profile pictures display immediately from cache
- ✅ No loading spinners or placeholders
- ✅ Images appear crisp and clear
- ✅ Circular crop is maintained

**Actual Results:**
- [ ] Pass
- [ ] Fail (describe issue): _______________

---

### Test Case 2: Message Images in Chat Room

**Objective:** Verify message images are cached and display offline

**Steps:**
1. Open a chat room with image messages
2. Scroll through messages to load all images
3. Wait for images to fully load
4. Enable airplane mode
5. Navigate away from chat room
6. Navigate back to the same chat room
7. Scroll through messages

**Expected Results:**
- ✅ All previously viewed images display from cache
- ✅ Images load instantly without delay
- ✅ No error icons or placeholders
- ✅ Image quality is preserved

**Actual Results:**
- [ ] Pass
- [ ] Fail (describe issue): _______________

---

### Test Case 3: Full-Screen Image Viewer Offline

**Objective:** Verify full-screen images work offline

**Steps:**
1. Open a chat with image messages (online)
2. Tap an image to view full-screen
3. Close full-screen viewer
4. Enable airplane mode
5. Tap the same image again

**Expected Results:**
- ✅ Image opens in full-screen viewer
- ✅ Image displays at full resolution
- ✅ Pinch-to-zoom works correctly
- ✅ No loading indicator or error

**Actual Results:**
- [ ] Pass
- [ ] Fail (describe issue): _______________

---

### Test Case 4: User Search Profile Pictures

**Objective:** Verify profile pictures in search results are cached

**Steps:**
1. Open chat tab (online)
2. Tap "New Chat" or search icon
3. Search for users and view their profile pictures
4. Close search dialog
5. Enable airplane mode
6. Open search dialog again
7. Search for the same users

**Expected Results:**
- ✅ Previously viewed profile pictures display
- ✅ Images load from cache instantly
- ✅ Circular crop is applied
- ✅ No network errors

**Actual Results:**
- [ ] Pass
- [ ] Fail (describe issue): _______________

---

### Test Case 5: Group Members Profile Pictures

**Objective:** Verify member profile pictures are cached

**Steps:**
1. Open a group with multiple members (online)
2. View group details/members list
3. Scroll through all members to load pictures
4. Navigate away
5. Enable airplane mode
6. Navigate back to group members list

**Expected Results:**
- ✅ All member profile pictures display
- ✅ Images load from cache
- ✅ No placeholders for cached images
- ✅ Smooth scrolling performance

**Actual Results:**
- [ ] Pass
- [ ] Fail (describe issue): _______________

---

### Test Case 6: New Content While Offline

**Objective:** Verify placeholders show for uncached images

**Steps:**
1. Enable airplane mode
2. Open a chat you've never viewed before
3. Observe profile pictures and images

**Expected Results:**
- ✅ Placeholder icons display for uncached images
- ✅ No crash or error messages
- ✅ App remains functional
- ✅ Text messages still display correctly

**Actual Results:**
- [ ] Pass
- [ ] Fail (describe issue): _______________

---

### Test Case 7: Cache Persistence After App Restart

**Objective:** Verify disk cache persists across app restarts

**Steps:**
1. Load several chats with images (online)
2. View multiple profile pictures
3. Force close the app completely
4. Enable airplane mode
5. Reopen the app
6. Navigate to previously viewed chats

**Expected Results:**
- ✅ Previously cached images still display
- ✅ No re-downloading occurs
- ✅ Images load quickly from disk cache
- ✅ App functions normally offline

**Actual Results:**
- [ ] Pass
- [ ] Fail (describe issue): _______________

---

### Test Case 8: Profile Picture Upload and Cache

**Objective:** Verify uploaded profile pictures are cached

**Steps:**
1. Navigate to Profile tab (online)
2. Upload a new profile picture
3. Wait for upload to complete
4. Navigate to Chat tab
5. Enable airplane mode
6. Navigate back to Profile tab

**Expected Results:**
- ✅ New profile picture displays from cache
- ✅ No loading indicator
- ✅ Image quality is maintained
- ✅ Circular crop is applied

**Actual Results:**
- [ ] Pass
- [ ] Fail (describe issue): _______________

---

### Test Case 9: Memory Cache vs Disk Cache

**Objective:** Verify disk cache works when memory cache is cleared

**Steps:**
1. Load many images (online)
2. Navigate to several different chats
3. Open other apps to consume memory
4. Wait 5 minutes
5. Return to the app
6. Enable airplane mode
7. Navigate to previously viewed chats

**Expected Results:**
- ✅ Images reload from disk cache
- ✅ Slight delay (disk read) but no network call
- ✅ All images eventually display
- ✅ No errors or placeholders

**Actual Results:**
- [ ] Pass
- [ ] Fail (describe issue): _______________

---

### Test Case 10: Network Recovery

**Objective:** Verify caching works after network is restored

**Steps:**
1. Start with airplane mode enabled
2. Open app and navigate to chats
3. Observe placeholders for uncached images
4. Disable airplane mode
5. Wait for network to reconnect
6. Observe images loading

**Expected Results:**
- ✅ Images download and display
- ✅ Images are cached for future offline use
- ✅ Smooth transition from placeholder to image
- ✅ No duplicate downloads

**Actual Results:**
- [ ] Pass
- [ ] Fail (describe issue): _______________

---

## Performance Testing

### Test Case 11: Cache Size Limit

**Objective:** Verify cache doesn't exceed 50MB

**Steps:**
1. Load many different images (100+)
2. View various chats and profiles
3. Check cache size using ADB:
   ```
   adb shell du -sh /data/data/com.example.loginandregistration/cache/image_cache
   ```

**Expected Results:**
- ✅ Cache size stays under 50MB
- ✅ Old images are evicted (LRU)
- ✅ App doesn't crash or slow down
- ✅ Most recent images are retained

**Actual Results:**
- Cache size: _______ MB
- [ ] Pass
- [ ] Fail (describe issue): _______________

---

### Test Case 12: Memory Usage

**Objective:** Verify memory cache doesn't cause OOM errors

**Steps:**
1. Open Android Studio Profiler
2. Launch app and monitor memory
3. Load many images rapidly
4. Scroll through multiple chats
5. Observe memory graph

**Expected Results:**
- ✅ Memory usage stays reasonable
- ✅ No OutOfMemoryError crashes
- ✅ Memory is released when not needed
- ✅ GC runs periodically

**Actual Results:**
- Peak memory: _______ MB
- [ ] Pass
- [ ] Fail (describe issue): _______________

---

## Edge Cases

### Test Case 13: Low Storage Space

**Objective:** Verify app handles low storage gracefully

**Steps:**
1. Fill device storage to near capacity
2. Launch app and load images
3. Observe behavior

**Expected Results:**
- ✅ App doesn't crash
- ✅ Graceful degradation (fewer cached images)
- ✅ User is not notified (silent failure)
- ✅ App remains functional

**Actual Results:**
- [ ] Pass
- [ ] Fail (describe issue): _______________

---

### Test Case 14: Corrupted Cache

**Objective:** Verify app handles corrupted cache files

**Steps:**
1. Load some images (online)
2. Close app
3. Manually corrupt cache files using ADB
4. Reopen app (offline)
5. Navigate to chats

**Expected Results:**
- ✅ App doesn't crash
- ✅ Corrupted images show placeholders
- ✅ Valid cached images still display
- ✅ App recovers gracefully

**Actual Results:**
- [ ] Pass
- [ ] Fail (describe issue): _______________

---

### Test Case 15: Slow Network

**Objective:** Verify caching works on slow connections

**Steps:**
1. Enable network throttling (2G speed)
2. Open app and load images
3. Observe caching behavior
4. Disable throttling
5. Reload same images

**Expected Results:**
- ✅ Images cache even on slow network
- ✅ Cached images load instantly on reload
- ✅ No duplicate downloads
- ✅ Progress indicators work correctly

**Actual Results:**
- [ ] Pass
- [ ] Fail (describe issue): _______________

---

## Checkpoint 6: Complete Offline Support Test

**Objective:** Verify all offline features work together

**Steps:**
1. **Online Phase:**
   - Login to app
   - Open several chats and view messages
   - View profile pictures in chat list
   - Open group details and view members
   - Send a few messages
   - View some images in full-screen

2. **Offline Phase:**
   - Enable airplane mode
   - Navigate through app
   - View previously loaded chats
   - View profile pictures
   - Try to send a message (should queue)
   - View cached images

3. **Recovery Phase:**
   - Disable airplane mode
   - Observe queued message sending
   - Verify connection status banner
   - Load new content

**Expected Results:**
- ✅ All previously viewed images display offline
- ✅ Messages queue when offline
- ✅ Connection status indicator works
- ✅ Queued messages send when online
- ✅ App remains fully functional offline
- ✅ Smooth transition between online/offline

**Actual Results:**
- [ ] Pass
- [ ] Fail (describe issue): _______________

---

## Debugging Tips

### View Cache Contents
```bash
adb shell ls -lh /data/data/com.example.loginandregistration/cache/image_cache/
```

### Clear Cache Manually
```bash
adb shell rm -rf /data/data/com.example.loginandregistration/cache/image_cache/
```

### Monitor Network Calls
```bash
adb logcat | grep -i "coil\|imageloader"
```

### Check Disk Space
```bash
adb shell df -h /data
```

## Common Issues and Solutions

| Issue | Possible Cause | Solution |
|-------|---------------|----------|
| Images not caching | Coil not initialized | Check TeamCollaborationApp.onCreate() |
| Images not loading offline | Cache policy disabled | Verify CachePolicy.ENABLED in config |
| High memory usage | Cache size too large | Reduce memory cache percentage |
| Slow image loading | Disk I/O bottleneck | Check device storage speed |
| Placeholders showing | Images not cached yet | Load images online first |

## Test Summary

Total Test Cases: 15
- Passed: _____
- Failed: _____
- Skipped: _____

**Overall Status:** [ ] PASS [ ] FAIL

**Notes:**
_________________________________
_________________________________
_________________________________

**Tester:** _______________
**Date:** _______________
**Device:** _______________
**Android Version:** _______________
