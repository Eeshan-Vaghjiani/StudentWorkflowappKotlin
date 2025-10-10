# EmptyStateView Usage Guide

## Quick Start

The `EmptyStateView` is a custom view that displays empty states with icons, titles, descriptions, and optional action buttons.

## Basic Usage

### 1. Add to Layout XML

```xml
<com.example.loginandregistration.views.EmptyStateView
    android:id="@+id/emptyStateView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:visibility="gone" />
```

### 2. Initialize in Fragment/Activity

```kotlin
private lateinit var emptyStateView: EmptyStateView

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    emptyStateView = view.findViewById(R.id.emptyStateView)
}
```

### 3. Show Empty State

```kotlin
// Show without action button
emptyStateView.showNoTasks()

// Show with action button
emptyStateView.showNoTasks {
    // Handle action button click
    navigateToCreateTask()
}
```

## Pre-built Empty States

### No Chats
```kotlin
emptyStateView.showNoChats {
    // Optional: Handle "Start Chat" button click
    openUserSearchDialog()
}
```

### No Messages
```kotlin
emptyStateView.showNoMessages()
```

### No Tasks
```kotlin
emptyStateView.showNoTasks {
    // Optional: Handle "Create Task" button click
    openCreateTaskScreen()
}
```

### No Internet Connection
```kotlin
emptyStateView.showNoInternet {
    // Optional: Handle "Retry" button click
    retryConnection()
}
```

### No Groups
```kotlin
emptyStateView.showNoGroups {
    // Optional: Handle "Create Group" button click
    openCreateGroupDialog()
}
```

### No Search Results
```kotlin
emptyStateView.showNoSearchResults()
```

### No Notifications
```kotlin
emptyStateView.showNoNotifications()
```

## Custom Empty State

```kotlin
emptyStateView.showCustom(
    icon = R.drawable.ic_custom_icon,
    title = "Custom Title",
    description = "This is a custom description",
    actionButtonText = "Custom Action"
) {
    // Handle custom action
    performCustomAction()
}
```

## Advanced Usage with Extension Functions

### Automatic Management with RecyclerView

```kotlin
// Automatically show/hide based on adapter item count
recyclerView.manageEmptyState(emptyStateView) {
    showNoTasks {
        navigateToCreateTask()
    }
}
```

### Manual Toggle

```kotlin
// Show empty state and hide content
contentView.showEmptyState(emptyStateView) {
    showNoGroups()
}

// Hide empty state and show content
contentView.hideEmptyState(emptyStateView)

// Toggle based on condition
contentView.toggleEmptyState(emptyStateView, items.isEmpty()) {
    showNoTasks()
}
```

### Programmatic Addition

```kotlin
// Add EmptyStateView to a ViewGroup programmatically
val emptyStateView = parentLayout.addEmptyStateView()
emptyStateView.showNoChats()
```

## Complete Integration Examples

### Example 1: TasksFragment

```kotlin
class TasksFragment : Fragment() {
    private lateinit var emptyStateView: EmptyStateView
    private lateinit var tasksRecyclerView: RecyclerView
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        emptyStateView = view.findViewById(R.id.emptyStateView)
        tasksRecyclerView = view.findViewById(R.id.tasksRecyclerView)
        
        // Observe tasks
        viewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            if (tasks.isEmpty()) {
                emptyStateView.visibility = View.VISIBLE
                emptyStateView.showNoTasks {
                    // Navigate to create task
                    findNavController().navigate(R.id.createTaskFragment)
                }
                tasksRecyclerView.visibility = View.GONE
            } else {
                emptyStateView.visibility = View.GONE
                tasksRecyclerView.visibility = View.VISIBLE
            }
        }
    }
}
```

### Example 2: GroupsFragment with Search

```kotlin
class GroupsFragment : Fragment() {
    private lateinit var emptyStateView: EmptyStateView
    private lateinit var groupsRecyclerView: RecyclerView
    private var isSearching = false
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        emptyStateView = view.findViewById(R.id.emptyStateView)
        groupsRecyclerView = view.findViewById(R.id.groupsRecyclerView)
        
        // Observe groups
        viewModel.groups.observe(viewLifecycleOwner) { groups ->
            if (groups.isEmpty()) {
                emptyStateView.visibility = View.VISIBLE
                
                if (isSearching) {
                    // Show "No Results" for search
                    emptyStateView.showNoSearchResults()
                } else {
                    // Show "No Groups" with action button
                    emptyStateView.showNoGroups {
                        showCreateGroupDialog()
                    }
                }
                
                groupsRecyclerView.visibility = View.GONE
            } else {
                emptyStateView.visibility = View.GONE
                groupsRecyclerView.visibility = View.VISIBLE
            }
        }
    }
}
```

### Example 3: Using Extension Functions

```kotlin
class ChatFragment : Fragment() {
    private lateinit var emptyStateView: EmptyStateView
    private lateinit var chatsRecyclerView: RecyclerView
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        emptyStateView = view.findViewById(R.id.emptyStateView)
        chatsRecyclerView = view.findViewById(R.id.chatsRecyclerView)
        
        // Automatic management - no manual visibility handling needed!
        chatsRecyclerView.manageEmptyState(emptyStateView) {
            showNoChats {
                val dialog = UserSearchDialog.newInstance()
                dialog.show(parentFragmentManager, UserSearchDialog.TAG)
            }
        }
    }
}
```

### Example 4: Offline State

```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var emptyStateView: EmptyStateView
    private lateinit var contentLayout: ViewGroup
    
    private fun observeConnectionStatus() {
        connectionMonitor.isConnected.observe(this) { isConnected ->
            if (!isConnected) {
                contentLayout.showEmptyState(emptyStateView) {
                    showNoInternet {
                        // Retry connection
                        connectionMonitor.checkConnection()
                    }
                }
            } else {
                contentLayout.hideEmptyState(emptyStateView)
            }
        }
    }
}
```

## Layout XML Examples

### Basic Layout

```xml
<FrameLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recyclerView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
    
    <com.example.loginandregistration.views.EmptyStateView
        android:id="@+id/emptyStateView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:visibility="gone" />
</FrameLayout>
```

### With SwipeRefreshLayout

```xml
<androidx.swiperefreshlayout.widget.SwipeRefreshLayout
    android:id="@+id/swipeRefreshLayout"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <FrameLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent">
        
        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/recyclerView"
            android:layout_width="match_parent"
            android:layout_height="match_parent" />
        
        <com.example.loginandregistration.views.EmptyStateView
            android:id="@+id/emptyStateView"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:visibility="gone" />
    </FrameLayout>
</androidx.swiperefreshlayout.widget.SwipeRefreshLayout>
```

## Best Practices

### 1. Always Hide Initially
```xml
android:visibility="gone"
```

### 2. Use Appropriate Empty State
Choose the right pre-built method for your context:
- Lists: `showNoChats()`, `showNoTasks()`, `showNoGroups()`
- Search: `showNoSearchResults()`
- Network: `showNoInternet()`
- Custom: `showCustom()`

### 3. Provide Action Buttons When Helpful
```kotlin
// Good - provides clear next step
emptyStateView.showNoTasks {
    navigateToCreateTask()
}

// Also good - no action needed
emptyStateView.showNoMessages()
```

### 4. Handle Visibility Properly
```kotlin
// Option 1: Manual
if (isEmpty) {
    emptyStateView.visibility = View.VISIBLE
    contentView.visibility = View.GONE
} else {
    emptyStateView.visibility = View.GONE
    contentView.visibility = View.VISIBLE
}

// Option 2: Extension function (recommended)
contentView.toggleEmptyState(emptyStateView, isEmpty) {
    showNoTasks()
}

// Option 3: Automatic with RecyclerView (best)
recyclerView.manageEmptyState(emptyStateView) {
    showNoTasks()
}
```

### 5. Consider Context
```kotlin
// Different empty states for different contexts
when {
    isSearching && items.isEmpty() -> emptyStateView.showNoSearchResults()
    !isConnected -> emptyStateView.showNoInternet { retry() }
    items.isEmpty() -> emptyStateView.showNoTasks { create() }
}
```

## Customization

### Change Icon Size
Modify `view_empty_state.xml`:
```xml
<ImageView
    android:id="@+id/emptyStateIcon"
    android:layout_width="100dp"  <!-- Change size -->
    android:layout_height="100dp"
    ... />
```

### Change Text Styles
Modify `view_empty_state.xml`:
```xml
<TextView
    android:id="@+id/emptyStateTitle"
    android:textSize="24sp"  <!-- Change size -->
    android:textStyle="bold"
    ... />
```

### Change Button Style
Modify `view_empty_state.xml`:
```xml
<com.google.android.material.button.MaterialButton
    android:id="@+id/emptyStateActionButton"
    style="@style/Widget.MaterialComponents.Button.OutlinedButton"  <!-- Change style -->
    ... />
```

## Troubleshooting

### Empty State Not Showing
1. Check visibility: `emptyStateView.visibility = View.VISIBLE`
2. Verify layout hierarchy (should be in FrameLayout with content)
3. Check if content view is hiding it (z-order)

### Action Button Not Appearing
1. Ensure you're passing both `actionButtonText` and `onActionClick`
2. Check if button text is null or empty

### Icon Not Displaying
1. Verify drawable resource exists
2. Check if icon is too large/small
3. Verify tint color is visible against background

### Text Truncated
1. Check `maxWidth` constraint in layout
2. Verify parent layout has enough space
3. Consider using shorter text

## Migration from Old Empty States

### Before (Old Approach)
```xml
<LinearLayout
    android:id="@+id/emptyStateLayout"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:visibility="gone">
    
    <ImageView ... />
    <TextView ... />
    <TextView ... />
</LinearLayout>
```

### After (New Approach)
```xml
<com.example.loginandregistration.views.EmptyStateView
    android:id="@+id/emptyStateView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:visibility="gone" />
```

### Code Migration
```kotlin
// Before
emptyStateLayout.visibility = View.VISIBLE

// After
emptyStateView.visibility = View.VISIBLE
emptyStateView.showNoChats()
```

---

**For more examples, see**: `ChatFragment.kt` for a complete integration example.
