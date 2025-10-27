# Memory Management Implementation Guide

## Overview

This guide documents the memory management and cache clearing implementation for the Team Collaboration app. The implementation ensures efficient memory usage, prevents out-of-memory errors, and provides automatic cache management.

## Requirements Addressed

- **Requirement 10.8**: Monitor memory usage and clear image cache when memory is low
- **Requirement 10.9**: Implement cache size limits and avoid loading large bitmaps into memory

## Components Implemented

### 1. MemoryManager Utility (`utils/MemoryManager.kt`)

A comprehensive utility class for memory monitoring and optimization.

#### Features:
- **Memory Statistics**: Get detailed memory usage information
- **Low Memory Detection**: Detect when device is in low or critical memory state
- **Cache Management**: Automatically clear caches based on memory pressure
- **Bitmap Optimization**: Load bitmaps efficiently with sampling
- **Recommended Dimensions**: Calculate optimal image dimensions based on available memory

#### Key Methods:

```kotlin
// Get current memory statistics
val stats = MemoryManager.getMemoryStats(context)

// Check if memory is low
if (MemoryManager.isLowMemory(context)) {
    // Take action
}

// Handle memory trim events
MemoryManager.handleMemoryTrim(imageLoader, level)

// Load bitmap efficiently
val bitmap = MemoryManager.decodeSampledBitmapFromUri(
    context, uri, reqWidth, reqHeight
)

// Get recommended image dimensions
val (width, height) = MemoryManager.getRecommendedImageDimensions(context)

// Log memory statistics for debugging
MemoryManager.logMemoryStats(context)
```

### 2. Enhanced Application Class (`TeamCollaborationApp.kt`)

The Application class now implements automatic memory management:

#### Features:
- **onTrimMemory()**: Responds to system memory trim events
- **onLowMemory()**: Clears all caches when system is critically low on memory
- **Automatic Logging**: Logs memory statistics on startup and during memory events

#### Memory Trim Levels Handled:

| Level | Action |
|-------|--------|
| TRIM_MEMORY_RUNNING_CRITICAL | Clear all caches (memory + disk) |
| TRIM_MEMORY_COMPLETE | Clear all caches (memory + disk) |
| TRIM_MEMORY_RUNNING_LOW | Clear memory cache only |
| TRIM_MEMORY_MODERATE | Clear memory cache only |
| TRIM_MEMORY_RUNNING_MODERATE | Trim memory cache to 50% |
| TRIM_MEMORY_BACKGROUND | Trim memory cache to 50% |

### 3. Enhanced ImageCompressor (`utils/ImageCompressor.kt`)

Updated to use MemoryManager for efficient bitmap loading:

#### Improvements:
- Uses `MemoryManager.decodeSampledBitmapFromUri()` for memory-efficient decoding
- Checks memory before loading bitmaps
- Automatically reduces dimensions on low-memory devices
- Immediately recycles bitmaps after use

### 4. Memory Monitor Activity (`MemoryMonitorActivity.kt`)

A debugging tool for monitoring memory usage in real-time.

#### Features:
- Real-time memory statistics (auto-refresh every 2 seconds)
- Image cache statistics (memory and disk)
- Manual cache clearing
- Visual indicators for memory status (Good 🟢, Low 🟡, Critical 🔴)
- Recommended image dimensions display

#### Access:
1. Open Debug Activity from the app
2. Tap "Memory Monitor" button
3. View real-time statistics

### 5. Enhanced ImageLoaderConfig (`utils/ImageLoaderConfig.kt`)

Already configured with optimal cache settings:

#### Configuration:
- **Memory Cache**: 25% of app memory
- **Disk Cache**: 50MB
- **Cache Location**: `app/cache/image_cache/`
- **Cache Headers**: Disabled for offline support

#### Methods:
```kotlin
// Get cache statistics
val stats = ImageLoaderConfig.getCacheStats(imageLoader)

// Clear all caches
ImageLoaderConfig.clearCache(imageLoader)
```

## Memory Thresholds

### Low Memory Threshold
- **Trigger**: When available memory < 15% of total memory
- **Action**: Use reduced image dimensions, warn user

### Critical Memory Threshold
- **Trigger**: When available memory < 10% of total memory
- **Action**: Clear all caches, use minimal image dimensions

## Bitmap Loading Best Practices

### 1. Always Use Sampling

```kotlin
// BAD - Loads full bitmap into memory
val bitmap = BitmapFactory.decodeStream(inputStream)

// GOOD - Uses sampling for efficient memory usage
val bitmap = MemoryManager.decodeSampledBitmapFromUri(
    context, uri, 1920, 1080
)
```

### 2. Recycle Bitmaps Immediately

```kotlin
// Use bitmap
bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)

// Recycle immediately after use
bitmap.recycle()
```

### 3. Use RGB_565 for Non-Transparent Images

```kotlin
val options = BitmapFactory.Options().apply {
    inPreferredConfig = Bitmap.Config.RGB_565 // Uses 50% less memory than ARGB_8888
}
```

### 4. Check Memory Before Loading

```kotlin
if (MemoryManager.isLowMemory(context)) {
    // Use smaller dimensions or skip loading
    val (width, height) = MemoryManager.getRecommendedImageDimensions(context)
}
```

## RecyclerView Optimization

All adapters already use ViewHolder pattern efficiently:

### MessageAdapter
- ✅ Uses ListAdapter with DiffUtil
- ✅ Efficient ViewHolder reuse
- ✅ Coil for image loading with caching
- ✅ Recycles views properly

### ChatAdapter
- ✅ Uses ListAdapter with DiffUtil
- ✅ Efficient ViewHolder reuse
- ✅ Coil for image loading with caching
- ✅ Generates avatars only when needed

### Best Practices Applied:
1. **ViewHolder Pattern**: All adapters use ViewHolder for view recycling
2. **DiffUtil**: Efficient list updates without full refresh
3. **Image Loading**: Coil handles memory and disk caching automatically
4. **View Recycling**: Views are properly recycled in RecyclerView

## Testing Memory Management

### 1. Using Memory Monitor Activity

```
1. Open app
2. Navigate to Debug Activity
3. Tap "Memory Monitor"
4. Observe real-time statistics
5. Test cache clearing
```

### 2. Using Android Profiler

```
1. Open Android Studio
2. Run app on device/emulator
3. Open Profiler (View > Tool Windows > Profiler)
4. Select Memory profiler
5. Monitor memory usage while using app
6. Look for:
   - Memory leaks (increasing baseline)
   - Excessive allocations
   - GC events
```

### 3. Testing Low Memory Scenarios

#### Method 1: Using ADB
```bash
# Simulate low memory
adb shell am send-trim-memory <package-name> RUNNING_LOW

# Simulate critical memory
adb shell am send-trim-memory <package-name> RUNNING_CRITICAL

# Simulate complete memory trim
adb shell am send-trim-memory <package-name> COMPLETE
```

#### Method 2: Using Developer Options
```
1. Enable Developer Options on device
2. Go to Settings > Developer Options
3. Find "Don't keep activities"
4. Enable it to test memory cleanup
```

### 4. Testing on Low-End Devices

Recommended test devices:
- Devices with < 2GB RAM
- Android API 23-26 (older versions)
- Emulator with limited RAM (512MB - 1GB)

## Performance Metrics

### Target Metrics:
- **Memory Usage**: < 100MB for typical usage
- **Cache Size**: Memory cache < 25% of app memory, Disk cache < 50MB
- **Image Load Time**: < 500ms for cached images
- **No OOM Errors**: Zero OutOfMemoryError crashes

### Monitoring:
```kotlin
// Log memory stats periodically
MemoryManager.logMemoryStats(context, "PerformanceCheck")

// Check cache stats
val stats = ImageLoaderConfig.getCacheStats(imageLoader)
Log.d("CacheStats", "Memory: ${stats["memoryCacheSize"]} / ${stats["memoryCacheMaxSize"]}")
```

## Troubleshooting

### Issue: OutOfMemoryError when loading images

**Solution:**
1. Check if using MemoryManager for bitmap loading
2. Verify image dimensions are reasonable
3. Ensure bitmaps are recycled after use
4. Check memory statistics with MemoryManager

### Issue: App slow after loading many images

**Solution:**
1. Clear image cache: `ImageLoaderConfig.clearCache(imageLoader)`
2. Check if memory is low: `MemoryManager.isLowMemory(context)`
3. Reduce image dimensions for low-memory devices
4. Enable pagination for long lists

### Issue: Images not loading on low-end devices

**Solution:**
1. Use recommended dimensions: `MemoryManager.getRecommendedImageDimensions(context)`
2. Reduce compression quality for smaller file sizes
3. Implement progressive loading (thumbnails first)
4. Clear cache to free memory

## Code Examples

### Example 1: Loading Image with Memory Check

```kotlin
fun loadImage(context: Context, uri: Uri, imageView: ImageView) {
    // Check memory before loading
    if (MemoryManager.isCriticalMemory(context)) {
        // Show placeholder instead
        imageView.setImageResource(R.drawable.placeholder)
        return
    }
    
    // Get recommended dimensions
    val (width, height) = MemoryManager.getRecommendedImageDimensions(context)
    
    // Load with Coil (handles caching automatically)
    imageView.load(uri) {
        size(width, height)
        crossfade(true)
        placeholder(R.drawable.placeholder)
        error(R.drawable.error)
    }
}
```

### Example 2: Compressing Image with Memory Awareness

```kotlin
suspend fun compressAndUpload(context: Context, uri: Uri) {
    try {
        // Check memory before compression
        if (MemoryManager.isLowMemory(context)) {
            // Use smaller dimensions
            val compressedFile = ImageCompressor.compressImageForBase64(
                context, uri, maxWidth = 600, maxHeight = 600, quality = 60
            )
            uploadImage(compressedFile)
        } else {
            // Use normal dimensions
            val compressedFile = ImageCompressor.compressImage(context, uri)
            uploadImage(compressedFile)
        }
    } catch (e: OutOfMemoryError) {
        // Clear cache and retry with smaller dimensions
        ImageLoaderConfig.clearCache(Coil.imageLoader(context))
        val compressedFile = ImageCompressor.compressImageForBase64(
            context, uri, maxWidth = 400, maxHeight = 400, quality = 50
        )
        uploadImage(compressedFile)
    }
}
```

### Example 3: Monitoring Memory in Activity

```kotlin
class ChatRoomActivity : AppCompatActivity() {
    
    override fun onResume() {
        super.onResume()
        
        // Log memory stats when activity resumes
        MemoryManager.logMemoryStats(this, "ChatRoomActivity")
        
        // Show warning if memory is low
        if (MemoryManager.isLowMemory(this)) {
            Toast.makeText(
                this,
                "Low memory detected. Some features may be limited.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        
        // Handle memory trim in activity
        val imageLoader = Coil.imageLoader(this)
        MemoryManager.handleMemoryTrim(imageLoader, level)
    }
}
```

## Summary

The memory management implementation provides:

✅ **Automatic Memory Monitoring**: System-level callbacks handle memory pressure
✅ **Efficient Bitmap Loading**: Sampling and optimization prevent OOM errors
✅ **Smart Cache Management**: Automatic clearing based on memory state
✅ **Developer Tools**: Memory Monitor Activity for debugging
✅ **Best Practices**: ViewHolder pattern, DiffUtil, proper recycling
✅ **Low-End Device Support**: Adaptive image dimensions and quality

The implementation ensures the app runs smoothly on devices with varying memory capacities while maintaining good performance and user experience.
