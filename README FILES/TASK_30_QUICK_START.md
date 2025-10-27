# Task 30: Offline Image Caching - Quick Start Guide

## 🚀 Quick Test (5 Minutes)

Want to quickly verify offline image caching works? Follow these steps:

### Step 1: Build and Install (1 minute)
```bash
# Clean and build the app
./gradlew clean assembleDebug

# Install on device/emulator
./gradlew installDebug
```

### Step 2: Load Images Online (2 minutes)
1. Launch the app and login
2. Navigate to **Chat** tab
3. Open 2-3 different chats
4. Scroll through messages to load images
5. View some profile pictures

### Step 3: Test Offline (2 minutes)
1. **Enable airplane mode** on your device
2. Navigate away from the app (press Home)
3. Reopen the app
4. Navigate to **Chat** tab
5. Open the same chats you viewed earlier

### ✅ Expected Result
- All previously viewed images should display instantly
- Profile pictures should load from cache
- Message images should load from cache
- No loading spinners or error icons

### ❌ If It Doesn't Work
- Check that Coil is initialized in `TeamCollaborationApp.kt`
- Verify you viewed the images while online first
- Check device storage isn't full
- Review logs: `adb logcat | grep -i "coil\|imageloader"`

---

## 📋 Full Test Checklist

For comprehensive testing, follow these scenarios:

### ✅ Scenario 1: Chat List Profile Pictures
- [ ] View chat list online (images load)
- [ ] Enable airplane mode
- [ ] View chat list again (images from cache)

### ✅ Scenario 2: Message Images
- [ ] Open chat with images online
- [ ] Enable airplane mode
- [ ] Open same chat (images from cache)

### ✅ Scenario 3: Full-Screen Images
- [ ] Tap image to view full-screen online
- [ ] Enable airplane mode
- [ ] Tap same image (loads from cache)

### ✅ Scenario 4: App Restart
- [ ] Load images online
- [ ] Force close app
- [ ] Enable airplane mode
- [ ] Reopen app (images from cache)

### ✅ Scenario 5: New Content Offline
- [ ] Enable airplane mode
- [ ] Open chat never viewed before
- [ ] See placeholders (expected - not cached)

---

## 🔍 What to Look For

### Good Signs ✅
- Images appear instantly when cached
- No loading spinners for cached images
- Smooth scrolling in chat list
- Full-screen images work offline
- No error messages

### Bad Signs ❌
- Loading spinners for cached images
- Error icons instead of images
- Blank spaces where images should be
- App crashes when offline
- Slow image loading from cache

---

## 🛠️ Troubleshooting

### Problem: Images not caching
**Solution:** Check `TeamCollaborationApp.kt` has Coil initialization:
```kotlin
val imageLoader = ImageLoaderConfig.createImageLoader(this)
Coil.setImageLoader(imageLoader)
```

### Problem: Images not loading offline
**Solution:** Ensure images were viewed online first. Cache only stores what was previously loaded.

### Problem: Placeholders showing offline
**Solution:** This is expected for images never viewed online. Only cached images display offline.

### Problem: Build errors
**Solution:** The build directory might be locked on Windows. Close Android Studio and try again.

---

## 📊 Verify Cache is Working

### Check Cache Size
```bash
# View cache directory
adb shell ls -lh /data/data/com.example.loginandregistration/cache/image_cache/

# Check total size
adb shell du -sh /data/data/com.example.loginandregistration/cache/image_cache/
```

### Check Logs
```bash
# View Coil logs
adb logcat | grep -i "coil"

# View app logs
adb logcat | grep -i "TeamCollaborationApp"
```

### Expected Log Output
```
D/TeamCollaborationApp: Firestore offline persistence enabled
D/TeamCollaborationApp: Coil ImageLoader configured with 50MB disk cache
```

---

## 📈 Performance Check

### Memory Usage
1. Open Android Studio
2. Go to **View → Tool Windows → Profiler**
3. Select your app
4. Monitor memory while loading images
5. Should stay under app memory limit

### Cache Hit Rate
Load the same images multiple times and observe:
- **First load:** Slow (downloads from network)
- **Second load:** Fast (loads from memory cache)
- **After app restart:** Fast (loads from disk cache)

---

## 🎯 Success Criteria

Your implementation is working correctly if:

1. ✅ Images load instantly when cached
2. ✅ Images display offline after being viewed online
3. ✅ Profile pictures work in all screens
4. ✅ Message images work in chat rooms
5. ✅ Full-screen images work offline
6. ✅ Cache persists after app restart
7. ✅ Placeholders show for uncached images
8. ✅ No crashes or errors
9. ✅ Memory usage is reasonable
10. ✅ Cache size stays under 50MB

---

## 📱 Test Devices

### Recommended Test Devices
- **Emulator:** Pixel 5 API 30+ (easy network control)
- **Physical:** Any Android device API 23+

### Network Control
- **Emulator:** Use network toggle in extended controls
- **Physical:** Use airplane mode

---

## ⏱️ Time Estimates

- **Quick Test:** 5 minutes
- **Full Test:** 30 minutes
- **Performance Test:** 15 minutes
- **Edge Cases:** 20 minutes
- **Total:** ~70 minutes for complete testing

---

## 📚 Additional Resources

### Detailed Documentation
- **TASK_30_IMPLEMENTATION_SUMMARY.md** - Technical details
- **TASK_30_TESTING_GUIDE.md** - 15 comprehensive test cases
- **TASK_30_VISUAL_GUIDE.md** - Diagrams and visualizations
- **TASK_30_VERIFICATION_CHECKLIST.md** - Complete checklist

### Code Files
- **utils/ImageLoaderConfig.kt** - Cache configuration
- **utils/CoilExtensions.kt** - Helper functions
- **TeamCollaborationApp.kt** - Initialization

---

## 🎉 You're Done!

If all tests pass, Task 30 is successfully implemented and working correctly.

**Next Steps:**
1. Mark task as tested ✅
2. Move to next task in Phase 7
3. Or complete Checkpoint 6 full testing

---

## 💡 Pro Tips

1. **Test on real device** for accurate performance
2. **Clear cache between tests** to verify fresh behavior
3. **Monitor logs** to understand what's happening
4. **Test with slow network** to see caching benefits
5. **Load many images** to test cache eviction

---

## ❓ Need Help?

### Common Questions

**Q: How do I know if caching is working?**  
A: Images should load instantly on second view, even offline.

**Q: Why do some images show placeholders offline?**  
A: Only images viewed online are cached. New images need network.

**Q: Can I increase cache size?**  
A: Yes, modify `ImageLoaderConfig.kt` and change 50MB to desired size.

**Q: How do I clear the cache?**  
A: Use `ImageLoaderConfig.clearCache(imageLoader)` or clear app data.

**Q: Does this work with all image types?**  
A: Yes, profile pictures, chat images, and message images all cache.

---

**Happy Testing! 🚀**
