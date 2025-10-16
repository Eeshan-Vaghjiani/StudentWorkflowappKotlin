# Task 30: Offline Image Caching - Visual Guide

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                     Application Startup                          │
│                                                                   │
│  TeamCollaborationApp.onCreate()                                 │
│         │                                                         │
│         ├─► Initialize Firebase                                  │
│         ├─► Enable Firestore Offline Persistence                 │
│         └─► Configure Coil ImageLoader                           │
│                    │                                              │
│                    └─► ImageLoaderConfig.createImageLoader()     │
│                              │                                    │
│                              ├─► Memory Cache (25% app memory)   │
│                              └─► Disk Cache (50MB)               │
└─────────────────────────────────────────────────────────────────┘
```

## Image Loading Flow

```
┌──────────────┐
│ User Opens   │
│ Chat Screen  │
└──────┬───────┘
       │
       ▼
┌──────────────────────────────────────────────────────────────┐
│ ChatAdapter.bind() calls imageView.load(profileImageUrl)    │
└──────┬───────────────────────────────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────────────────────────────┐
│              Coil ImageLoader Processing                      │
│                                                               │
│  Step 1: Check Memory Cache                                  │
│  ┌─────────────────────────────────┐                         │
│  │ Is image in memory cache?       │                         │
│  └─────┬───────────────────┬───────┘                         │
│        │ YES               │ NO                               │
│        ▼                   ▼                                  │
│  ┌─────────┐         Step 2: Check Disk Cache               │
│  │ Return  │         ┌─────────────────────────────────┐    │
│  │ Image   │         │ Is image in disk cache?         │    │
│  └─────────┘         └─────┬───────────────────┬───────┘    │
│                            │ YES               │ NO          │
│                            ▼                   ▼             │
│                      ┌─────────┐         Step 3: Network    │
│                      │ Load    │         ┌─────────────┐    │
│                      │ from    │         │ Download    │    │
│                      │ Disk    │         │ from URL    │    │
│                      └────┬────┘         └──────┬──────┘    │
│                           │                     │            │
│                           ▼                     ▼            │
│                      ┌─────────────────────────────┐        │
│                      │ Store in Memory Cache       │        │
│                      └──────────┬──────────────────┘        │
│                                 │                            │
│                                 ▼                            │
│                      ┌─────────────────────────────┐        │
│                      │ Store in Disk Cache         │        │
│                      └──────────┬──────────────────┘        │
│                                 │                            │
│                                 ▼                            │
│                      ┌─────────────────────────────┐        │
│                      │ Display Image               │        │
│                      └─────────────────────────────┘        │
└──────────────────────────────────────────────────────────────┘
```

## Cache Hierarchy

```
┌─────────────────────────────────────────────────────────────┐
│                    FASTEST (Instant)                         │
│  ┌────────────────────────────────────────────────────┐     │
│  │         Memory Cache (RAM)                         │     │
│  │  • Size: 25% of app memory (~50-100MB)            │     │
│  │  • Speed: Instant (nanoseconds)                    │     │
│  │  • Persistence: Cleared when app is killed        │     │
│  │  • Format: Decoded Bitmap objects                 │     │
│  └────────────────────────────────────────────────────┘     │
│                           ▼                                  │
│  ┌────────────────────────────────────────────────────┐     │
│  │         Disk Cache (Storage)                       │     │
│  │  • Size: 50MB                                      │     │
│  │  • Speed: Fast (milliseconds)                      │     │
│  │  • Persistence: Survives app restarts              │     │
│  │  • Format: Compressed image files                 │     │
│  │  • Location: /data/data/.../cache/image_cache/    │     │
│  └────────────────────────────────────────────────────┘     │
│                           ▼                                  │
│  ┌────────────────────────────────────────────────────┐     │
│  │         Network (Firebase Storage)                 │     │
│  │  • Size: Unlimited                                 │     │
│  │  • Speed: Slow (seconds)                           │     │
│  │  • Persistence: Permanent                          │     │
│  │  • Format: Original uploaded files                │     │
│  └────────────────────────────────────────────────────┘     │
│                    SLOWEST (Requires Network)                │
└─────────────────────────────────────────────────────────────┘
```

## Online vs Offline Behavior

### Online Mode
```
User Opens Chat
      │
      ▼
┌─────────────────┐
│ Check Memory    │──► Found ──► Display Instantly
│ Cache           │
└────────┬────────┘
         │ Not Found
         ▼
┌─────────────────┐
│ Check Disk      │──► Found ──► Load & Display (Fast)
│ Cache           │              Store in Memory
└────────┬────────┘
         │ Not Found
         ▼
┌─────────────────┐
│ Download from   │──► Success ──► Display Image
│ Network         │                 Store in Memory
│                 │                 Store in Disk
└─────────────────┘
```

### Offline Mode
```
User Opens Chat (Airplane Mode)
      │
      ▼
┌─────────────────┐
│ Check Memory    │──► Found ──► Display Instantly
│ Cache           │
└────────┬────────┘
         │ Not Found
         ▼
┌─────────────────┐
│ Check Disk      │──► Found ──► Load & Display (Fast)
│ Cache           │              Store in Memory
└────────┬────────┘
         │ Not Found
         ▼
┌─────────────────┐
│ Network Request │──► FAILS ──► Show Placeholder
│ (Will Fail)     │              (Image not cached)
└─────────────────┘
```

## Cache Eviction Policy (LRU)

```
Disk Cache Full (50MB Limit Reached)
      │
      ▼
┌─────────────────────────────────────────────────┐
│  Least Recently Used (LRU) Eviction             │
│                                                  │
│  Cache Contents (by last access time):          │
│  ┌──────────────────────────────────────────┐  │
│  │ [Oldest]                                 │  │
│  │  • profile_123.jpg (30 days ago)    ◄───┼──┼─ EVICTED FIRST
│  │  • chat_img_456.jpg (20 days ago)   ◄───┼──┼─ EVICTED SECOND
│  │  • profile_789.jpg (10 days ago)        │  │
│  │  • chat_img_012.jpg (5 days ago)        │  │
│  │  • profile_345.jpg (2 days ago)         │  │
│  │  • chat_img_678.jpg (1 day ago)         │  │
│  │  • profile_901.jpg (1 hour ago)         │  │
│  │ [Newest]                                 │  │
│  └──────────────────────────────────────────┘  │
│                                                  │
│  New Image Arrives:                             │
│  • Oldest image is deleted                      │
│  • New image is stored                          │
│  • Cache size stays under 50MB                  │
└─────────────────────────────────────────────────┘
```

## Image Types and Caching

```
┌─────────────────────────────────────────────────────────────┐
│                    Image Type Matrix                         │
├──────────────────┬──────────────┬──────────────┬────────────┤
│ Image Type       │ Cached?      │ Transform    │ Priority   │
├──────────────────┼──────────────┼──────────────┼────────────┤
│ Profile Pictures │ ✅ Yes       │ Circle Crop  │ High       │
│ Chat Images      │ ✅ Yes       │ None         │ High       │
│ Message Images   │ ✅ Yes       │ None         │ Medium     │
│ Group Icons      │ ✅ Yes       │ Circle Crop  │ Medium     │
│ Document Icons   │ ❌ No        │ None         │ Low        │
│ UI Icons         │ ❌ No        │ None         │ N/A        │
└──────────────────┴──────────────┴──────────────┴────────────┘
```

## Cache Storage Structure

```
/data/data/com.example.loginandregistration/
└── cache/
    └── image_cache/
        ├── v1/                          (Coil version)
        │   ├── data/                    (Actual cached images)
        │   │   ├── 1a2b3c4d.0           (Hashed filename)
        │   │   ├── 5e6f7g8h.0
        │   │   ├── 9i0j1k2l.0
        │   │   └── ...
        │   └── journal                  (Cache metadata)
        └── .nomedia                     (Hide from gallery)

Total Size: Up to 50MB
Files: Varies (depends on image sizes)
Format: Original format (JPEG, PNG, etc.)
```

## Performance Comparison

```
┌─────────────────────────────────────────────────────────────┐
│              Image Load Time Comparison                      │
│                                                               │
│  Scenario 1: First Load (Not Cached)                        │
│  ┌────────────────────────────────────────────────┐         │
│  │ Network Download: ████████████████ 2000ms      │         │
│  │ Disk Write:       ██ 100ms                     │         │
│  │ Memory Store:     █ 10ms                       │         │
│  │ Display:          █ 5ms                        │         │
│  │ TOTAL:            ████████████████ ~2115ms     │         │
│  └────────────────────────────────────────────────┘         │
│                                                               │
│  Scenario 2: Reload (Memory Cache Hit)                      │
│  ┌────────────────────────────────────────────────┐         │
│  │ Memory Lookup:    █ 1ms                        │         │
│  │ Display:          █ 5ms                        │         │
│  │ TOTAL:            █ ~6ms                       │         │
│  └────────────────────────────────────────────────┘         │
│                                                               │
│  Scenario 3: After App Restart (Disk Cache Hit)             │
│  ┌────────────────────────────────────────────────┐         │
│  │ Disk Read:        ███ 50ms                     │         │
│  │ Memory Store:     █ 10ms                       │         │
│  │ Display:          █ 5ms                        │         │
│  │ TOTAL:            ████ ~65ms                   │         │
│  └────────────────────────────────────────────────┘         │
│                                                               │
│  Scenario 4: Offline (Disk Cache Hit)                       │
│  ┌────────────────────────────────────────────────┐         │
│  │ Disk Read:        ███ 50ms                     │         │
│  │ Memory Store:     █ 10ms                       │         │
│  │ Display:          █ 5ms                        │         │
│  │ TOTAL:            ████ ~65ms                   │         │
│  └────────────────────────────────────────────────┘         │
│                                                               │
│  Scenario 5: Offline (Not Cached)                           │
│  ┌────────────────────────────────────────────────┐         │
│  │ Network Attempt:  ████ 500ms (timeout)         │         │
│  │ Show Placeholder: █ 5ms                        │         │
│  │ TOTAL:            █████ ~505ms                 │         │
│  └────────────────────────────────────────────────┘         │
└─────────────────────────────────────────────────────────────┘

Speed Improvement:
• Memory Cache: 350x faster than network
• Disk Cache:   30x faster than network
```

## User Experience Flow

### First Time User
```
Day 1: First App Launch
├─► Login
├─► View Chat List
│   └─► Profile pictures download (2s each)
│       └─► Cached to disk ✓
├─► Open Chat Room
│   └─► Message images download (3s each)
│       └─► Cached to disk ✓
└─► Close App

Day 2: Second App Launch (Online)
├─► Login
├─► View Chat List
│   └─► Profile pictures load instantly (<100ms)
│       └─► From disk cache ✓
├─► Open Chat Room
│   └─► Message images load instantly (<100ms)
│       └─► From disk cache ✓
└─► Close App

Day 3: Offline Usage
├─► Enable Airplane Mode
├─► Login (cached credentials)
├─► View Chat List
│   └─► Profile pictures load instantly
│       └─► From disk cache ✓
├─► Open Chat Room
│   └─► Message images load instantly
│       └─► From disk cache ✓
│   └─► Try to send message
│       └─► Queued for later ✓
└─► Disable Airplane Mode
    └─► Queued message sends ✓
```

## Memory Management

```
┌─────────────────────────────────────────────────────────────┐
│              App Memory Allocation                           │
│                                                               │
│  Total App Memory: 256MB (example device)                    │
│  ┌────────────────────────────────────────────────────┐     │
│  │                                                     │     │
│  │  App Code & Resources:  ████████ 80MB              │     │
│  │  UI & Views:            ████ 40MB                  │     │
│  │  Firestore Cache:       ████ 40MB                  │     │
│  │  Image Memory Cache:    ████ 64MB (25%)            │     │
│  │  Other:                 ████ 32MB                  │     │
│  │                                                     │     │
│  └────────────────────────────────────────────────────┘     │
│                                                               │
│  Image Memory Cache Breakdown (64MB):                        │
│  ┌────────────────────────────────────────────────────┐     │
│  │  • ~40 profile pictures (1.5MB each)               │     │
│  │  • ~10 chat images (2MB each)                      │     │
│  │  • Overhead & metadata                             │     │
│  └────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
```

## Configuration Summary

```
┌─────────────────────────────────────────────────────────────┐
│              Coil ImageLoader Configuration                  │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  Memory Cache:                                               │
│  • Size: 25% of app memory                                  │
│  • Policy: LRU eviction                                     │
│  • Format: Decoded Bitmaps                                  │
│                                                               │
│  Disk Cache:                                                 │
│  • Size: 50MB maximum                                       │
│  • Policy: LRU eviction                                     │
│  • Format: Compressed images                                │
│  • Location: app cache directory                            │
│                                                               │
│  Network:                                                    │
│  • Cache headers: Ignored                                   │
│  • Retry policy: Default                                    │
│  • Timeout: Default                                         │
│                                                               │
│  Transformations:                                            │
│  • Circle crop (profile pictures)                           │
│  • Cached after transformation                              │
│                                                               │
│  Placeholders:                                               │
│  • Loading: ic_menu_gallery                                 │
│  • Error: ic_menu_report_image                              │
│  • Offline: Same as error                                   │
└─────────────────────────────────────────────────────────────┘
```

## Troubleshooting Flowchart

```
Image Not Displaying?
      │
      ├─► Online? ──► NO ──► Was image cached? ──► NO ──► Expected
      │                                                     (Show placeholder)
      │                      └─► YES ──► Check disk space
      │                                  Check cache corruption
      │
      └─► YES ──► Check network connection
                  Check Firebase Storage rules
                  Check image URL validity
                  Check Coil initialization
```

## Visual Indicators

```
┌─────────────────────────────────────────────────────────────┐
│              What User Sees                                  │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  Loading State:                                              │
│  ┌──────────┐                                                │
│  │  ⟳       │  Placeholder icon                             │
│  │          │  (While downloading)                           │
│  └──────────┘                                                │
│                                                               │
│  Cached State:                                               │
│  ┌──────────┐                                                │
│  │  [IMG]   │  Image appears instantly                      │
│  │          │  (From cache)                                  │
│  └──────────┘                                                │
│                                                               │
│  Error State:                                                │
│  ┌──────────┐                                                │
│  │  ⚠       │  Error icon                                   │
│  │          │  (Failed to load)                              │
│  └──────────┘                                                │
│                                                               │
│  Offline Uncached:                                           │
│  ┌──────────┐                                                │
│  │  👤      │  Default avatar/placeholder                   │
│  │          │  (Image not cached)                            │
│  └──────────┘                                                │
└─────────────────────────────────────────────────────────────┘
```

This visual guide provides a comprehensive overview of how offline image caching works in the app, making it easier to understand the implementation and troubleshoot issues.
