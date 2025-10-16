# Task 45: Memory Management - Visual Guide

## Accessing Memory Monitor

### Step 1: Open Debug Activity
```
Main App → Menu → Debug Tools
```

### Step 2: Tap Memory Monitor
```
Debug Activity → [Memory Monitor] Button
```

### Step 3: View Statistics
```
Memory Monitor Activity
├── Memory Statistics Card
│   ├── Total Memory
│   ├── Available Memory
│   ├── Used Memory
│   ├── Max Memory
│   ├── Percent Available
│   ├── Low Memory Status
│   ├── Threshold
│   ├── Memory Status (Good/Low/Critical)
│   └── Recommended Image Size
│
├── Cache Statistics Card
│   ├── Memory Cache
│   │   ├── Size
│   │   ├── Max
│   │   └── Usage %
│   └── Disk Cache
│       ├── Size
│       ├── Max
│       └── Usage %
│
└── Action Buttons
    ├── [Refresh] - Manual refresh
    └── [Clear Cache] - Clear all caches
```

## Memory Monitor Screen Layout

```
┌─────────────────────────────────────┐
│  ← Memory Monitor                   │
├─────────────────────────────────────┤
│                                     │
│  ┌───────────────────────────────┐ │
│  │ === MEMORY STATISTICS ===     │ │
│  │                               │ │
│  │ Total Memory: 2048 MB         │ │
│  │ Available Memory: 512 MB      │ │
│  │ Used Memory: 128 MB           │ │
│  │ Max Memory: 256 MB            │ │
│  │ Percent Available: 25%        │ │
│  │ Low Memory: NO ✓              │ │
│  │ Threshold: 256 MB             │ │
│  │                               │ │
│  │ Memory Status: GOOD 🟢        │ │
│  │ Recommended Image Size:       │ │
│  │ 1920x1920                     │ │
│  └───────────────────────────────┘ │
│                                     │
│  ┌───────────────────────────────┐ │
│  │ === CACHE STATISTICS ===      │ │
│  │                               │ │
│  │ Memory Cache:                 │ │
│  │   Size: 32 MB                 │ │
│  │   Max: 64 MB                  │ │
│  │   Usage: 50%                  │ │
│  │                               │ │
│  │ Disk Cache:                   │ │
│  │   Size: 25 MB                 │ │
│  │   Max: 50 MB                  │ │
│  │   Usage: 50%                  │ │
│  └───────────────────────────────┘ │
│                                     │
│  ┌─────────────┐ ┌──────────────┐ │
│  │  Refresh    │ │ Clear Cache  │ │
│  └─────────────┘ └──────────────┘ │
│                                     │
│  Statistics auto-refresh every     │
│  2 seconds                          │
└─────────────────────────────────────┘
```

## Memory Status Indicators

### 🟢 GOOD (> 15% available)
```
Memory Status: GOOD 🟢
Recommended Image Size: 1920x1920

✓ Normal operations
✓ Full quality images
✓ All features available
```

### 🟡 LOW (10-15% available)
```
Memory Status: LOW 🟡
Recommended Image Size: 1280x1280

⚠ Reduced image quality
⚠ Smaller dimensions
⚠ Some features limited
```

### 🔴 CRITICAL (< 10% available)
```
Memory Status: CRITICAL 🔴
Recommended Image Size: 800x800

🚨 Minimal operations
🚨 Lowest quality images
🚨 Cache clearing active
```

## Cache Statistics Interpretation

### Memory Cache
```
Size: 32 MB      ← Current cache size
Max: 64 MB       ← Maximum allowed (25% of app memory)
Usage: 50%       ← Percentage used

Interpretation:
- 0-50%: Good, plenty of space
- 50-80%: Normal, cache is being used
- 80-100%: High, old items being evicted
```

### Disk Cache
```
Size: 25 MB      ← Current cache size
Max: 50 MB       ← Maximum allowed
Usage: 50%       ← Percentage used

Interpretation:
- 0-50%: Good, plenty of space
- 50-80%: Normal, cache is being used
- 80-100%: High, old items being evicted
```

## Using the Buttons

### Refresh Button
```
[Refresh]
    ↓
Updates statistics immediately
(normally auto-refreshes every 2 seconds)
```

### Clear Cache Button
```
[Clear Cache]
    ↓
Clears both memory and disk caches
    ↓
Statistics update after 500ms
    ↓
Cache sizes drop to near zero
    ↓
Images reload from network/Firestore
```

## Monitoring Workflow

### Normal Monitoring
```
1. Open Memory Monitor
2. Observe statistics
3. Check memory status
4. Verify cache usage
5. Let auto-refresh run
6. Monitor for issues
```

### Troubleshooting Workflow
```
1. Open Memory Monitor
2. Check memory status
3. If LOW or CRITICAL:
   ├─ Tap [Clear Cache]
   ├─ Wait for refresh
   ├─ Check if status improves
   └─ If not, close other apps
4. Monitor cache growth
5. Check for memory leaks
```

## Interpreting Statistics

### Good Memory Health
```
✓ Memory Status: GOOD 🟢
✓ Percent Available: > 15%
✓ Cache Usage: < 80%
✓ No low memory warnings
✓ Recommended size: 1920x1920
```

### Warning Signs
```
⚠ Memory Status: LOW 🟡
⚠ Percent Available: 10-15%
⚠ Cache Usage: > 80%
⚠ Frequent cache clearing
⚠ Recommended size: < 1920x1920
```

### Critical Issues
```
🚨 Memory Status: CRITICAL 🔴
🚨 Percent Available: < 10%
🚨 Cache constantly full
🚨 Automatic cache clearing
🚨 Recommended size: 800x800
🚨 App may be slow
```

## Testing Scenarios

### Scenario 1: Normal Usage
```
1. Open Memory Monitor
2. Observe: GOOD 🟢
3. Navigate to chat with images
4. Return to Memory Monitor
5. Observe: Cache size increased
6. Memory status still GOOD 🟢
```

### Scenario 2: Heavy Usage
```
1. Open Memory Monitor
2. Load many chats with images
3. Return to Memory Monitor
4. Observe: Cache usage high (70-90%)
5. Memory status may be LOW 🟡
6. Tap [Clear Cache]
7. Observe: Cache cleared, status improves
```

### Scenario 3: Low Memory Device
```
1. Open Memory Monitor
2. Observe: LOW 🟡 or CRITICAL 🔴
3. Recommended size: 800x800
4. Navigate through app
5. Images load at reduced quality
6. App remains responsive
```

## ADB Testing Commands

### Simulate Low Memory
```bash
adb shell am send-trim-memory com.example.loginandregistration RUNNING_LOW
```
**Expected Result:**
- Memory cache cleared
- Log message: "Low memory - clearing memory cache"
- Disk cache remains

### Simulate Critical Memory
```bash
adb shell am send-trim-memory com.example.loginandregistration RUNNING_CRITICAL
```
**Expected Result:**
- All caches cleared
- Log message: "Critical memory - clearing all image caches"
- Both memory and disk caches cleared

### View Memory Info
```bash
adb shell dumpsys meminfo com.example.loginandregistration
```
**Expected Output:**
```
Applications Memory Usage (in Kilobytes):
Uptime: 12345678 Realtime: 12345678

** MEMINFO in pid 12345 [com.example.loginandregistration] **
                   Pss  Private  Private  SwapPss     Heap     Heap     Heap
                 Total    Dirty    Clean    Dirty     Size    Alloc     Free
                ------   ------   ------   ------   ------   ------   ------
  Native Heap    10000     9000     1000        0    20000    15000     5000
  Dalvik Heap     5000     4000     1000        0    10000     8000     2000
       .....
```

### Monitor Logs
```bash
adb logcat | grep -E "MemoryManager|TeamCollaborationApp|ImageCompressor"
```
**Expected Output:**
```
D/TeamCollaborationApp: Firestore offline persistence enabled
D/TeamCollaborationApp: Coil ImageLoader configured with 50MB disk cache
D/MemoryManager: Memory Statistics:
D/MemoryManager: - Total Memory: 2048 MB
D/MemoryManager: - Available Memory: 512 MB
...
```

## Common Patterns

### Pattern 1: Cache Growing
```
Time    Memory Cache    Disk Cache    Status
0:00    0 MB           0 MB          GOOD 🟢
0:30    10 MB          5 MB          GOOD 🟢
1:00    20 MB          15 MB         GOOD 🟢
1:30    32 MB          25 MB         GOOD 🟢
2:00    45 MB          35 MB         GOOD 🟢
2:30    50 MB          45 MB         GOOD 🟢
3:00    50 MB          50 MB         GOOD 🟢 (max reached)
```

### Pattern 2: Memory Pressure
```
Time    Available    Status         Action
0:00    512 MB      GOOD 🟢        None
1:00    256 MB      GOOD 🟢        None
2:00    128 MB      LOW 🟡         Clear memory cache
2:01    256 MB      GOOD 🟢        Recovered
3:00    64 MB       CRITICAL 🔴    Clear all caches
3:01    256 MB      GOOD 🟢        Recovered
```

### Pattern 3: Cache Clearing
```
Action              Memory Cache    Disk Cache
Before Clear        50 MB          45 MB
[Clear Cache]       ↓              ↓
After Clear         0 MB           0 MB
Load 1 image        2 MB           2 MB
Load 10 images      15 MB          12 MB
Load 50 images      45 MB          40 MB
```

## Tips for Developers

### Monitoring During Development
1. Keep Memory Monitor open in second window
2. Watch statistics while testing features
3. Look for unexpected memory growth
4. Verify cache clearing works
5. Test on low-memory scenarios

### Performance Testing
1. Load app with Memory Monitor
2. Use feature for 5-10 minutes
3. Check for memory leaks (continuous growth)
4. Verify cache stays within limits
5. Test cache clearing effectiveness

### Debugging Memory Issues
1. Open Memory Monitor
2. Reproduce issue
3. Check memory status
4. Review cache statistics
5. Check logcat for memory events
6. Use Android Profiler for details

## Summary

The Memory Monitor Activity provides:
- ✅ Real-time memory statistics
- ✅ Cache usage monitoring
- ✅ Visual status indicators
- ✅ Manual cache clearing
- ✅ Auto-refresh every 2 seconds
- ✅ Developer-friendly interface

Use it to:
- 🔍 Monitor memory health
- 🐛 Debug memory issues
- 📊 Track cache usage
- 🧪 Test memory scenarios
- ⚡ Optimize performance

---

**Quick Access:** Debug Activity → Memory Monitor  
**Auto-Refresh:** Every 2 seconds  
**Manual Actions:** Refresh, Clear Cache
