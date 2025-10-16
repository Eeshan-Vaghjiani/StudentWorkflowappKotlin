# Task 45: Memory Management - Quick Reference

## Quick Access

### Memory Monitor Activity
```
Debug Activity → Memory Monitor button
```

### Key Classes
- `MemoryManager` - Memory monitoring and optimization
- `ImageLoaderConfig` - Coil cache configuration
- `ImageCompressor` - Efficient image compression
- `TeamCollaborationApp` - Automatic memory management

## Common Use Cases

### 1. Check Memory Status
```kotlin
// Check if memory is low
if (MemoryManager.isLowMemory(context)) {
    // Take action (reduce quality, skip loading, etc.)
}

// Check if memory is critical
if (MemoryManager.isCriticalMemory(context)) {
    // Take urgent action (clear caches, minimal operations)
}

// Get detailed stats
val stats = MemoryManager.getMemoryStats(context)
Log.d("Memory", "Available: ${stats.percentAvailable * 100}%")
```

### 2. Load Bitmap Efficiently
```kotlin
// Load with automatic sampling
val bitmap = MemoryManager.decodeSampledBitmapFromUri(
    context, 
    imageUri, 
    reqWidth = 1920, 
    reqHeight = 1080
)

// Use bitmap
bitmap?.let {
    // Do something with bitmap
    it.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
    
    // IMPORTANT: Recycle immediately after use
    it.recycle()
}
```

### 3. Get Recommended Image Dimensions
```kotlin
// Get dimensions based on available memory
val (width, height) = MemoryManager.getRecommendedImageDimensions(context)

// Use for compression
val compressedFile = ImageCompressor.compressImage(
    context, uri, width, height, quality = 80
)
```

### 4. Clear Image Cache
```kotlin
// Get image loader
val imageLoader = Coil.imageLoader(context)

// Clear all caches
ImageLoaderConfig.clearCache(imageLoader)

// Or handle memory trim
MemoryManager.handleMemoryTrim(imageLoader, ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW)
```

### 5. Log Memory Stats
```kotlin
// Log current memory statistics
MemoryManager.logMemoryStats(context, "MyActivity")

// Output:
// Memory Statistics:
// - Total Memory: 2048 MB
// - Available Memory: 512 MB
// - Used Memory: 128 MB
// - Max Memory: 256 MB
// - Percent Available: 25%
// - Low Memory: NO
```

### 6. Get Cache Statistics
```kotlin
val imageLoader = Coil.imageLoader(context)
val stats = ImageLoaderConfig.getCacheStats(imageLoader)

val memoryCacheSize = stats["memoryCacheSize"] as Long
val memoryCacheMax = stats["memoryCacheMaxSize"] as Long
val diskCacheSize = stats["diskCacheSize"] as Long
val diskCacheMax = stats["diskCacheMaxSize"] as Long

Log.d("Cache", "Memory: $memoryCacheSize / $memoryCacheMax")
Log.d("Cache", "Disk: $diskCacheSize / $diskCacheMax")
```

## Memory Thresholds

| State | Available Memory | Action |
|-------|------------------|--------|
| Good | > 15% | Normal operations |
| Low | < 15% | Reduce quality, use smaller dimensions |
| Critical | < 10% | Clear caches, minimal operations |

## Image Dimensions by Memory

| Available Memory | Recommended Size | Quality |
|------------------|------------------|---------|
| > 100 MB | 1920x1920 | 80% |
| 50-100 MB | 1280x1280 | 80% |
| < 50 MB | 800x800 | 70% |

## Testing Commands

```bash
# Simulate low memory
adb shell am send-trim-memory com.example.loginandregistration RUNNING_LOW

# Simulate critical memory
adb shell am send-trim-memory com.example.loginandregistration RUNNING_CRITICAL

# View memory info
adb shell dumpsys meminfo com.example.loginandregistration

# Monitor memory events
adb logcat | grep -E "MemoryManager|TeamCollaborationApp"
```

## Best Practices

### ✅ DO
- Use `MemoryManager.decodeSampledBitmapFromUri()` for loading bitmaps
- Check memory before loading large images
- Recycle bitmaps immediately after use
- Use Coil for image loading (handles caching automatically)
- Log memory stats when debugging performance issues
- Test on low-end devices (< 2GB RAM)

### ❌ DON'T
- Load full-resolution bitmaps without sampling
- Keep bitmap references longer than needed
- Ignore low memory warnings
- Load many large images simultaneously
- Forget to recycle bitmaps
- Skip testing on low-end devices

## Integration Examples

### In Activity
```kotlin
class MyActivity : AppCompatActivity() {
    
    override fun onResume() {
        super.onResume()
        
        // Check memory on resume
        if (MemoryManager.isLowMemory(this)) {
            showLowMemoryWarning()
        }
    }
    
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        
        // Handle memory trim
        val imageLoader = Coil.imageLoader(this)
        MemoryManager.handleMemoryTrim(imageLoader, level)
    }
}
```

### In ViewModel
```kotlin
class MyViewModel : ViewModel() {
    
    fun loadImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                // Check memory first
                if (MemoryManager.isCriticalMemory(context)) {
                    _error.value = "Low memory - cannot load image"
                    return@launch
                }
                
                // Get recommended dimensions
                val (width, height) = MemoryManager.getRecommendedImageDimensions(context)
                
                // Compress image
                val file = ImageCompressor.compressImage(context, uri, width, height)
                
                // Upload or process file
                uploadImage(file)
                
            } catch (e: Exception) {
                _error.value = "Failed to load image: ${e.message}"
            }
        }
    }
}
```

### In Adapter
```kotlin
class MyAdapter : RecyclerView.Adapter<MyAdapter.ViewHolder>() {
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        
        // Load image with Coil (handles caching automatically)
        holder.imageView.load(item.imageUrl) {
            crossfade(true)
            placeholder(R.drawable.placeholder)
            error(R.drawable.error)
            
            // Optional: Reduce size on low memory
            if (MemoryManager.isLowMemory(holder.itemView.context)) {
                size(400, 400)
            }
        }
    }
}
```

## Troubleshooting

### OutOfMemoryError
1. Check if using `MemoryManager.decodeSampledBitmapFromUri()`
2. Verify bitmaps are recycled after use
3. Clear cache: `ImageLoaderConfig.clearCache(imageLoader)`
4. Reduce image dimensions

### Slow Performance
1. Check memory usage in Memory Monitor Activity
2. Clear cache if needed
3. Verify RecyclerView is using ViewHolder pattern
4. Check for memory leaks in Android Profiler

### Images Not Loading
1. Check if memory is critically low
2. Clear cache and retry
3. Reduce image dimensions
4. Check network connectivity

## Performance Targets

- **Memory Usage:** < 100MB typical, < 150MB heavy
- **Cache Size:** Memory ≤ 25% app memory, Disk ≤ 50MB
- **Image Load:** < 500ms from cache
- **Scroll FPS:** 60 FPS
- **OOM Crashes:** Zero

## Documentation

- **Full Guide:** `MEMORY_MANAGEMENT_GUIDE.md`
- **Testing:** `TASK_45_TESTING_CHECKLIST.md`
- **Summary:** `TASK_45_IMPLEMENTATION_SUMMARY.md`

## Support

For issues or questions:
1. Check Memory Monitor Activity for current state
2. Review logs: `adb logcat | grep MemoryManager`
3. Check Android Profiler for memory leaks
4. Refer to full documentation guides
