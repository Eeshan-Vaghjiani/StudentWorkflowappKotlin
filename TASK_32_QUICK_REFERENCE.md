# EmptyStateView Quick Reference

## Import
```kotlin
import com.example.loginandregistration.views.EmptyStateView
```

## Add to Layout
```xml
<com.example.loginandregistration.views.EmptyStateView
    android:id="@+id/emptyStateView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:visibility="gone" />
```

## Pre-built Methods

| Method | Icon | Action Button | Use Case |
|--------|------|---------------|----------|
| `showNoChats()` | 💬 | Optional | Empty chat list |
| `showNoMessages()` | 💭 | No | Empty chat room |
| `showNoTasks()` | ✓ | Optional | Empty task list |
| `showNoInternet()` | 📡 | Optional | No connection |
| `showNoGroups()` | 👥 | Optional | Empty group list |
| `showNoSearchResults()` | 🔍 | No | No search results |
| `showNoNotifications()` | 🔔 | No | No notifications |
| `showCustom()` | Custom | Optional | Custom empty state |

## Basic Usage

### Without Action Button
```kotlin
emptyStateView.showNoMessages()
```

### With Action Button
```kotlin
emptyStateView.showNoChats {
    // Handle button click
    openUserSearchDialog()
}
```

### Custom Empty State
```kotlin
emptyStateView.showCustom(
    icon = R.drawable.ic_custom,
    title = "Custom Title",
    description = "Custom description",
    actionButtonText = "Action"
) {
    // Handle action
}
```

## Extension Functions

### Automatic with RecyclerView
```kotlin
recyclerView.manageEmptyState(emptyStateView) {
    showNoTasks()
}
```

### Manual Toggle
```kotlin
contentView.toggleEmptyState(emptyStateView, isEmpty) {
    showNoTasks()
}
```

### Show/Hide
```kotlin
// Show
contentView.showEmptyState(emptyStateView) {
    showNoGroups()
}

// Hide
contentView.hideEmptyState(emptyStateView)
```

## Common Patterns

### Pattern 1: Simple List
```kotlin
if (items.isEmpty()) {
    emptyStateView.visibility = View.VISIBLE
    emptyStateView.showNoTasks()
    recyclerView.visibility = View.GONE
} else {
    emptyStateView.visibility = View.GONE
    recyclerView.visibility = View.VISIBLE
}
```

### Pattern 2: With Loading
```kotlin
when {
    isLoading -> showSkeleton()
    items.isEmpty() -> {
        emptyStateView.visibility = View.VISIBLE
        emptyStateView.showNoTasks()
    }
    else -> showContent()
}
```

### Pattern 3: Search Context
```kotlin
if (items.isEmpty()) {
    if (isSearching) {
        emptyStateView.showNoSearchResults()
    } else {
        emptyStateView.showNoTasks()
    }
}
```

## Customization

### Configure Method
```kotlin
emptyStateView.configure(
    icon = R.drawable.ic_icon,
    title = "Title",
    description = "Description",
    actionButtonText = "Action",  // null to hide
    onActionClick = { /* action */ }  // null to hide
)
```

## Files

| File | Purpose |
|------|---------|
| `EmptyStateView.kt` | Custom view class |
| `view_empty_state.xml` | Layout file |
| `EmptyStateExtensions.kt` | Extension functions |
| `strings.xml` | String resources |
| `ic_*.xml` | Icon drawables |

## Dimensions

| Element | Size |
|---------|------|
| Icon | 120dp × 120dp |
| Title | 20sp, bold |
| Description | 14sp, regular |
| Padding | 32dp all sides |
| Icon margin bottom | 24dp |
| Title margin bottom | 8dp |
| Description margin bottom | 24dp |
| Description max width | 280dp |

## Colors

| Element | Color |
|---------|-------|
| Icon tint | `colorTextSecondary` |
| Title | `colorTextPrimary` |
| Description | `colorTextSecondary` |
| Button background | `colorPrimaryBlue` |
| Button text | `white` |

## Tips

✅ **DO**:
- Hide empty state initially (`visibility="gone"`)
- Use appropriate pre-built method for context
- Provide action buttons when helpful
- Use extension functions for cleaner code

❌ **DON'T**:
- Forget to toggle visibility
- Use generic messages
- Add action buttons without handlers
- Hardcode strings (use string resources)

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Not showing | Check visibility is VISIBLE |
| Button not showing | Pass both text and click handler |
| Icon not visible | Check tint color and drawable |
| Text truncated | Check maxWidth and parent size |

## Examples

See these files for complete examples:
- `ChatFragment.kt` - Integrated example
- `TASK_32_USAGE_GUIDE.md` - Detailed usage
- `TASK_32_VISUAL_GUIDE.md` - Visual examples

---

**Quick Start**: Add to layout → Initialize in code → Call show method → Done! 🎉
