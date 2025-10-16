# Task 42: Image Caching with Coil - Testing Guide

## Overview

This guide provides comprehensive testing procedures to verify that Coil image caching is working correctly with memory cache (25% of app memory) and disk cache (50MB).

---

## Prerequisites

Before testing:
- ✅ App is installed on device/emulator
- ✅ Internet connection available
- ✅ At least one chat with images exists
- ✅ User has profile picture set
- ✅ Ability to toggle airplane mode

---

## Test Suite 1: Memory Cache Verification

### Test 1.1: Instant Image Loading from Memory

**Objective:** Verify images load instantly from memory cache

**Steps:**
1. Open the app and navigate to a chat with images
2. Scroll down through 10-15 messages with images
3. Wait for all images to load completely
4. Scroll back up to the top
5. Observe image loading behavior

**Expected Results:**
- ✅ Images at the top load instantly (no loading indicator)
- ✅ No network requests made for previously viewed images
- ✅ Smooth scrolling with no lag
- ✅ Images appear immediately without fade-in animation

**Pass Criteria:**
- Images load in < 50ms from memory cache
- No visible loading delay

---

### Test 1.2: Memory Cache Limit

**Objective:** Verify memory cache respects 25% limit

**Steps:**
1. Open Android Studio Profiler
2. Navigate to Memory tab
3. Open the app
4. Load multiple chats with many images
5. Monitor memory usage
6. Calculate image cache memory usage

**Expected Results:**
- ✅ Image cache uses approximately 25% of app memory
- ✅ Memory usage stabilizes (doesn't grow indefinitely)
- ✅ Old images evicted when limit reached
- ✅ No OutOfMemoryError exceptions

**Pass Criteria:**
- Memory cache stays within configured limit
- App remains stable with many images

---

### Test 1.3: Memory Cache Across Screens

**Objective:** Verify memory cache works across different screens

**Steps:**
1. Open chat list (view profile pictures)
2. Open a chat room (view sender profile pictures)
3. Go back to chat list
4. Open the same chat again
5. Observe profile picture loading

**Expected Results:**
- ✅ Profile pictures load instantly on second view
- ✅ No re-downloading of images
- ✅ Consistent across all screens

**Pass Criteria:**
- Profile pictures cached and reused
- Instant loading on revisit

---

## Test Suite 2: Disk Cache Verification

### Test 2.1: Offline Image Loading

**Objective:** Verify images load from disk cache when offline

**Steps:**
1. Open the app with internet connection
2. Navigate to a chat with images
3. Scroll through all messages (load all images)
4. Wait 5 seconds for images to cache
5. Close the app completely (swipe away from recents)
6. Turn on airplane mode
7. Open the app
8. Navigate to the same chat
9. Observe image loading

**Expected Results:**
- ✅ All previously viewed images load successfully
- ✅ Images load from disk cache (no network errors)
- ✅ Loading time < 500ms per image
- ✅ No "failed to load" placeholders

**Pass Criteria:**
- 100% of previously viewed images load offline
- No network error messages

---

### Test 2.2: Disk Cache Persistence

**Objective:** Verify disk cache persists across app restarts

**Steps:**
1. Open the app and load images in a chat
2. Close the app completely
3. Wait 1 minute
4. Clear app from memory (force stop)
5. Open the app again
6. Navigate to the same chat
7. Observe image loading speed

**Expected Results:**
- ✅ Images load quickly from disk cache
- ✅ No re-downloading required
- ✅ Cache survives app restart
- ✅ Faster than initial load

**Pass Criteria:**
- Images load from disk cache after restart
- Loading time significantly reduced

---

### Test 2.3: Disk Cache Size Limit

**Objective:** Verify disk cache respects 50MB limit

**Steps:**
1. Using file explorer or ADB, navigate to:
   `/data/data/com.example.loginandregistration/cache/image_cache`
2. Check initial cache size
3. Load many different images (50+ unique images)
4. Check cache size again
5. Continue loading more images
6. Monitor cache size

**ADB Command:**
```bash
adb shell du -sh /data/data/com.example.loginandregistration/cache/image_cache
```

**Expected Results:**
- ✅ Cache directory exists
- ✅ Cache size grows as images are loaded
- ✅ Cache size stops at approximately 50MB
- ✅ Old images evicted when limit reached

**Pass Criteria:**
- Cache size ≤ 50MB
- Automatic eviction working

---

### Test 2.4: Cache Directory Structure

**Objective:** Verify cache directory is created correctly

**Steps:**
1. Fresh install the app
2. Open the app
3. Load at least one image
4. Check cache directory using ADB:

```bash
adb shell ls -la /data/data/com.example.loginandregistration/cache/
```

**Expected Results:**
- ✅ `image_cache` directory exists
- ✅ Directory contains cached image files
- ✅ Files have proper permissions
- ✅ Cache metadata files present

**Pass Criteria:**
- Cache directory created automatically
- Files stored correctly

---

## Test Suite 3: Profile Picture Caching

### Test 3.1: Profile Picture Memory Cache

**Objective:** Verify profile pictures are cached in memory

**Steps:**
1. Open chat list with multiple chats
2. Observe profile pictures loading
3. Scroll down and back up
4. Observe profile picture loading behavior

**Expected Results:**
- ✅ Profile pictures load instantly on scroll back
- ✅ Circular crop applied correctly
- ✅ No flickering or reloading
- ✅ Smooth scrolling

**Pass Criteria:**
- Profile pictures cached and reused
- No visible reload

---

### Test 3.2: Profile Picture Disk Cache

**Objective:** Verify profile pictures available offline

**Steps:**
1. View multiple user profiles (online)
2. Close app
3. Turn on airplane mode
4. Open app
5. View the same profiles

**Expected Results:**
- ✅ Profile pictures load from cache
- ✅ No network errors
- ✅ Default avatars shown only for new users
- ✅ Cached pictures display correctly

**Pass Criteria:**
- All previously viewed profile pictures available offline

---

### Test 3.3: Default Avatar Generation

**Objective:** Verify default avatars when no cache available

**Steps:**
1. Clear app cache
2. Turn on airplane mode
3. Open app
4. View user profiles

**Expected Results:**
- ✅ Default avatars with initials shown
- ✅ Colored backgrounds generated
- ✅ No crash or error
- ✅ Consistent colors for same user

**Pass Criteria:**
- Default avatars display correctly
- No errors when cache empty

---

## Test Suite 4: Chat Image Caching

### Test 4.1: Chat Image Memory Cache

**Objective:** Verify chat images cached in memory

**Steps:**
1. Open a chat with 20+ image messages
2. Scroll through all images
3. Scroll back to top
4. Observe loading behavior

**Expected Results:**
- ✅ Images load instantly from memory
- ✅ No loading indicators
- ✅ Smooth scrolling
- ✅ No lag or stutter

**Pass Criteria:**
- Instant image display from memory cache

---

### Test 4.2: Chat Image Disk Cache

**Objective:** Verify chat images available offline

**Steps:**
1. Load a chat with images (online)
2. Close app
3. Enable airplane mode
4. Open app and navigate to same chat

**Expected Results:**
- ✅ All images load from disk cache
- ✅ No network errors
- ✅ Full-screen viewer works offline
- ✅ Image quality maintained

**Pass Criteria:**
- 100% of cached images available offline

---

### Test 4.3: Image Viewer Cache

**Objective:** Verify full-screen image viewer uses cache

**Steps:**
1. Open a chat with images
2. Tap an image to view full-screen
3. Close viewer
4. Turn on airplane mode
5. Tap the same image again

**Expected Results:**
- ✅ Full-screen image loads from cache
- ✅ No network error
- ✅ Zoom and pan work correctly
- ✅ High quality maintained

**Pass Criteria:**
- Full-screen images cached and available offline

---

## Test Suite 5: Cache Management

### Test 5.1: Cache Statistics

**Objective:** Verify cache statistics are accurate

**Steps:**
1. Add debug code to log cache stats:
```kotlin
val imageLoader = Coil.imageLoader(context)
val stats = ImageLoaderConfig.getCacheStats(imageLoader)
Log.d("CacheStats", "Memory: ${stats["memoryCacheSize"]}/${stats["memoryCacheMaxSize"]}")
Log.d("CacheStats", "Disk: ${stats["diskCacheSize"]}/${stats["diskCacheMaxSize"]}")
```
2. Load various images
3. Check logcat for cache statistics

**Expected Results:**
- ✅ Statistics show current cache usage
- ✅ Memory cache size increases as images load
- ✅ Disk cache size increases as images load
- ✅ Max sizes match configuration (25% memory, 50MB disk)

**Pass Criteria:**
- Accurate cache statistics reported

---

### Test 5.2: Manual Cache Clearing

**Objective:** Verify cache can be cleared manually

**Steps:**
1. Load images in the app
2. Verify images are cached (test offline loading)
3. Add cache clear button in debug menu:
```kotlin
val imageLoader = Coil.imageLoader(context)
ImageLoaderConfig.clearCache(imageLoader)
```
4. Clear cache
5. Test offline loading again

**Expected Results:**
- ✅ Cache cleared successfully
- ✅ Images no longer available offline
- ✅ Images reload from network when online
- ✅ No app crash

**Pass Criteria:**
- Cache clearing works correctly
- App remains stable

---

### Test 5.3: Cache Eviction

**Objective:** Verify old images evicted when cache full

**Steps:**
1. Load 100+ unique images
2. Monitor cache size
3. Verify oldest images evicted
4. Try to view old images offline

**Expected Results:**
- ✅ Cache size stays within limits
- ✅ Oldest images evicted first (LRU)
- ✅ Recently viewed images retained
- ✅ No cache overflow

**Pass Criteria:**
- LRU eviction working correctly

---

## Test Suite 6: Performance Testing

### Test 6.1: Image Loading Speed

**Objective:** Measure image loading performance

**Steps:**
1. Clear app cache
2. Load 20 images (first time - network)
3. Measure average load time
4. Scroll back up (memory cache)
5. Measure average load time
6. Restart app and load same images (disk cache)
7. Measure average load time

**Expected Results:**
- ✅ Network load: 500-2000ms per image
- ✅ Memory cache: < 50ms per image
- ✅ Disk cache: 100-300ms per image
- ✅ Significant performance improvement with cache

**Pass Criteria:**
- Memory cache: 10-40x faster than network
- Disk cache: 2-10x faster than network

---

### Test 6.2: Scroll Performance

**Objective:** Verify smooth scrolling with cached images

**Steps:**
1. Open chat with 50+ messages with images
2. Scroll rapidly up and down
3. Monitor frame rate
4. Check for lag or stutter

**Expected Results:**
- ✅ Smooth scrolling (60 FPS)
- ✅ No frame drops
- ✅ No lag when images load
- ✅ Responsive UI

**Pass Criteria:**
- Consistent 60 FPS during scroll
- No visible lag

---

### Test 6.3: Memory Usage

**Objective:** Verify memory usage is reasonable

**Steps:**
1. Open Android Studio Profiler
2. Monitor memory usage
3. Load many images
4. Check for memory leaks
5. Force garbage collection
6. Verify memory released

**Expected Results:**
- ✅ Memory usage stable
- ✅ No memory leaks
- ✅ Cache respects 25% limit
- ✅ Memory released after GC

**Pass Criteria:**
- No memory leaks detected
- Stable memory usage

---

## Test Suite 7: Edge Cases

### Test 7.1: Low Memory Scenario

**Objective:** Verify app handles low memory correctly

**Steps:**
1. Use device with limited RAM (< 2GB)
2. Open multiple apps
3. Load images in chat
4. Monitor for crashes

**Expected Results:**
- ✅ App doesn't crash
- ✅ Cache size adjusted automatically
- ✅ Images still load (maybe slower)
- ✅ Graceful degradation

**Pass Criteria:**
- No crashes in low memory
- App remains functional

---

### Test 7.2: Corrupted Cache

**Objective:** Verify app handles corrupted cache files

**Steps:**
1. Load images to cache
2. Manually corrupt cache files using ADB:
```bash
adb shell "echo 'corrupted' > /data/data/com.example.loginandregistration/cache/image_cache/some_file"
```
3. Open app and load images

**Expected Results:**
- ✅ App doesn't crash
- ✅ Corrupted images reloaded from network
- ✅ Error handled gracefully
- ✅ Cache rebuilt automatically

**Pass Criteria:**
- Graceful handling of corrupted cache

---

### Test 7.3: Full Disk

**Objective:** Verify app handles full disk scenario

**Steps:**
1. Fill device storage to near capacity
2. Try to load new images
3. Monitor app behavior

**Expected Results:**
- ✅ App doesn't crash
- ✅ Error message shown if needed
- ✅ Existing cache still works
- ✅ Graceful degradation

**Pass Criteria:**
- No crashes when disk full
- Appropriate error handling

---

## Test Suite 8: Integration Testing

### Test 8.1: Cache with Offline Mode

**Objective:** Verify cache works with offline mode feature

**Steps:**
1. Load images online
2. Enable offline mode (Task 27)
3. Verify images load from cache
4. Check connection status banner

**Expected Results:**
- ✅ Images load from cache
- ✅ Offline banner shown
- ✅ No network errors
- ✅ Seamless experience

**Pass Criteria:**
- Cache integrates with offline mode

---

### Test 8.2: Cache with Image Compression

**Objective:** Verify cache works with compressed images

**Steps:**
1. Upload compressed image (Task 13)
2. Verify image cached
3. Load image offline
4. Check image quality

**Expected Results:**
- ✅ Compressed images cached correctly
- ✅ Quality maintained
- ✅ File size appropriate
- ✅ No double compression

**Pass Criteria:**
- Compressed images cached properly

---

## Automated Testing (Optional)

### Unit Tests

```kotlin
@Test
fun testImageLoaderConfiguration() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val imageLoader = ImageLoaderConfig.createImageLoader(context)
    
    assertNotNull(imageLoader.memoryCache)
    assertNotNull(imageLoader.diskCache)
    
    val memoryCache = imageLoader.memoryCache!!
    val diskCache = imageLoader.diskCache!!
    
    assertTrue(memoryCache.maxSize > 0)
    assertEquals(50 * 1024 * 1024L, diskCache.maxSize)
}

@Test
fun testCacheStats() {
    val imageLoader = ImageLoaderConfig.createImageLoader(context)
    val stats = ImageLoaderConfig.getCacheStats(imageLoader)
    
    assertTrue(stats.containsKey("memoryCacheSize"))
    assertTrue(stats.containsKey("diskCacheSize"))
}
```

---

## Performance Benchmarks

### Target Metrics:
- **Memory Cache Hit:** < 50ms
- **Disk Cache Hit:** < 300ms
- **Network Load:** 500-2000ms
- **Memory Usage:** ≤ 25% of app memory
- **Disk Usage:** ≤ 50MB
- **Scroll FPS:** ≥ 55 FPS

---

## Troubleshooting

### Issue: Images not caching
**Solution:** 
- Check internet connection
- Verify cache directory permissions
- Check available disk space

### Issue: Images not loading offline
**Solution:**
- Ensure images were fully loaded online first
- Check cache directory exists
- Verify disk cache size limit not reached

### Issue: High memory usage
**Solution:**
- Check memory cache configuration
- Verify cache eviction working
- Monitor for memory leaks

### Issue: Slow image loading
**Solution:**
- Check network speed
- Verify cache hit rate
- Monitor disk I/O performance

---

## Test Results Template

```
Test Suite: [Suite Name]
Test Case: [Test Name]
Date: [Date]
Tester: [Name]
Device: [Device Model]
Android Version: [Version]

Result: ✅ PASS / ❌ FAIL
Notes: [Any observations]
Issues Found: [List any issues]
```

---

## Conclusion

Complete all test suites to verify:
- ✅ Memory cache working (25% limit)
- ✅ Disk cache working (50MB limit)
- ✅ Offline image loading functional
- ✅ Performance improvements achieved
- ✅ Cache management working
- ✅ Integration with other features

**All tests passing = Task 42 verified complete! ✅**
