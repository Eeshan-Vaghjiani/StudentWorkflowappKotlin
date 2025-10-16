# Task 31: Skeleton Loaders - Quick Reference

## Quick Start

### 1. Add Skeleton Container to Layout

```xml
<FrameLayout>
    <!-- Skeleton -->
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/skeletonRecyclerView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:visibility="gone" />
    
    <!-- Content -->
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/contentRecyclerView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
</FrameLayout>
```

### 2. Setup Skeleton in Code

```kotlin
// Initialize views
private lateinit var skeletonRecyclerView: RecyclerView
private lateinit var contentRecyclerView: RecyclerView

// Setup skeleton adapter
skeletonRecyclerView.apply {
    layoutManager = LinearLayoutManager(context)
    adapter = SkeletonLoaderHelper.createSkeletonAdapter(
        R.layout.item_chat_skeleton,  // Your skeleton layout
        5  // Number of items
    )
}
```

### 3. Show/Hide Skeleton

```kotlin
// Show skeleton when loading
if (isLoading && adapter.itemCount == 0) {
    SkeletonLoaderHelper.showSkeleton(
        skeletonRecyclerView,
        contentRecyclerView
    )
}

// Show content when loaded
if (!isLoading) {
    SkeletonLoaderHelper.showContent(
        skeletonRecyclerView,
        contentRecyclerView
    )
}
```

## Available Skeleton Layouts

| Layout | Use For | Items |
|--------|---------|-------|
| `item_chat_skeleton.xml` | Chat list | 5 |
| `item_message_skeleton.xml` | Messages | 8 |
| `item_task_skeleton.xml` | Tasks | 5 |

## Key Classes

| Class | Purpose |
|-------|---------|
| `SkeletonLoader` | Custom view with shimmer animation |
| `SkeletonLoaderHelper` | Helper methods for show/hide |
| `SkeletonExtensions` | Extension functions for customization |

## Common Patterns

### Pattern 1: Initial Load Only
```kotlin
if (isLoading && adapter.itemCount == 0) {
    showSkeleton()
} else if (!isLoading) {
    showContent()
}
```

### Pattern 2: With ViewModel
```kotlin
viewLifecycleOwner.lifecycleScope.launch {
    viewModel.isLoading.collect { isLoading ->
        if (isLoading && adapter.itemCount == 0) {
            SkeletonLoaderHelper.showSkeleton(skeleton, content)
        } else if (!isLoading) {
            SkeletonLoaderHelper.showContent(skeleton, content)
        }
    }
}
```

### Pattern 3: Custom Item Count
```kotlin
val itemCount = when (screenSize) {
    ScreenSize.SMALL -> 3
    ScreenSize.MEDIUM -> 5
    ScreenSize.LARGE -> 8
}
adapter = SkeletonLoaderHelper.createSkeletonAdapter(layout, itemCount)
```

## Customization

### Change Animation Speed
```kotlin
// In SkeletonLoader.kt
shimmerAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
    duration = 1500  // Change this (milliseconds)
}
```

### Change Colors
```xml
<!-- In colors.xml -->
<color name="skeleton_base">#E0E0E0</color>
<color name="skeleton_shimmer">#F5F5F5</color>
```

### Set Corner Radius Programmatically
```kotlin
skeletonLoader.setCornerRadius(20f)  // In pixels
```

## Troubleshooting

| Problem | Solution |
|---------|----------|
| Skeleton not showing | Check visibility is set to `gone` initially |
| Animation not smooth | Check device performance, reduce duration |
| Layout mismatch | Compare skeleton dimensions with actual layout |
| Skeleton shows on refresh | Add `adapter.itemCount == 0` check |

## Best Practices

✅ **DO:**
- Show skeleton only on initial load
- Match skeleton dimensions to content
- Use 5-8 skeleton items
- Fade transitions (300ms)
- Check adapter count before showing

❌ **DON'T:**
- Show skeleton on pull-to-refresh
- Use too many skeleton items (>10)
- Make skeleton too bright/dark
- Skip fade animations
- Forget to hide skeleton

## File Locations

```
app/src/main/
├── java/.../views/
│   └── SkeletonLoader.kt
├── java/.../utils/
│   ├── SkeletonLoaderHelper.kt
│   └── SkeletonExtensions.kt
└── res/
    ├── layout/
    │   ├── item_chat_skeleton.xml
    │   ├── item_message_skeleton.xml
    │   └── item_task_skeleton.xml
    └── values/
        ├── colors.xml (skeleton colors)
        └── attrs.xml (custom attributes)
```

## Documentation

- **Implementation Guide:** `TASK_31_IMPLEMENTATION_GUIDE.md`
- **Testing Guide:** `TASK_31_TESTING_GUIDE.md`
- **Visual Guide:** `TASK_31_VISUAL_GUIDE.md`
- **Summary:** `TASK_31_IMPLEMENTATION_SUMMARY.md`

## Next Steps

1. Test implementation (see Testing Guide)
2. Add to other screens (Tasks, Messages)
3. Adjust colors for dark mode
4. Fine-tune animation timing
5. Mark task complete ✅

## Support

For issues or questions:
1. Check diagnostics: `getDiagnostics()`
2. Review implementation guide
3. Check testing guide for common issues
4. Verify layout structure matches examples
