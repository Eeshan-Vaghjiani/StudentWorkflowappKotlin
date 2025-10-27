# Task 31: Skeleton Loaders Implementation Guide

## Overview

This guide explains how to use the skeleton loader system implemented for loading states across the app.

## Components Created

### 1. SkeletonLoader Custom View
**Location:** `app/src/main/java/com/example/loginandregistration/views/SkeletonLoader.kt`

A custom view that displays an animated shimmer effect. Features:
- Smooth gradient animation that moves across the view
- Customizable colors and corner radius
- Automatically starts/stops animation based on visibility
- Lightweight and performant

**Custom Attributes:**
- `skeletonBaseColor`: Base color of the skeleton (default: #E0E0E0)
- `skeletonShimmerColor`: Shimmer highlight color (default: #F5F5F5)
- `skeletonCornerRadius`: Corner radius in dp (default: 8dp)

### 2. Skeleton Layouts

Three skeleton layouts matching the actual item layouts:

#### Chat List Skeleton
**Location:** `app/src/main/res/layout/item_chat_skeleton.xml`
- Circular profile image placeholder
- Chat name placeholder
- Timestamp placeholder
- Two-line message preview placeholder

#### Message List Skeleton
**Location:** `app/src/main/res/layout/item_message_skeleton.xml`
- Circular sender profile placeholder
- Sender name placeholder
- Message bubble with two text lines
- Timestamp placeholder

#### Task List Skeleton
**Location:** `app/src/main/res/layout/item_task_skeleton.xml`
- Circular task icon placeholder
- Task title placeholder
- Task subtitle placeholder
- Status chip placeholder
- Arrow indicator placeholder

### 3. SkeletonLoaderHelper Utility
**Location:** `app/src/main/java/com/example/loginandregistration/utils/SkeletonLoaderHelper.kt`

Helper class that simplifies skeleton loader management:
- `showSkeleton()`: Show skeleton and hide content
- `showContent()`: Hide skeleton with fade animation and show content
- `fadeIn()`: Fade in animation
- `fadeOut()`: Fade out animation
- `createSkeletonAdapter()`: Create adapter for skeleton items
- `SkeletonAdapter`: Simple adapter to display skeleton items

## Usage Examples

### Example 1: Chat Fragment (Already Implemented)

```kotlin
class ChatFragment : Fragment() {
    private lateinit var chatsRecyclerView: RecyclerView
    private lateinit var skeletonRecyclerView: RecyclerView
    
    private fun initializeViews(view: View) {
        chatsRecyclerView = view.findViewById(R.id.chatsRecyclerView)
        skeletonRecyclerView = view.findViewById(R.id.skeletonRecyclerView)
        
        setupSkeletonLoader()
    }
    
    private fun setupSkeletonLoader() {
        skeletonRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = SkeletonLoaderHelper.createSkeletonAdapter(
                R.layout.item_chat_skeleton,
                5  // Show 5 skeleton items
            )
            setHasFixedSize(true)
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading -> 
                if (isLoading && chatAdapter.itemCount == 0) {
                    // Show skeleton on initial load
                    SkeletonLoaderHelper.showSkeleton(
                        skeletonRecyclerView,
                        chatsRecyclerView
                    )
                } else if (!isLoading) {
                    // Show content with fade animation
                    SkeletonLoaderHelper.showContent(
                        skeletonRecyclerView,
                        chatsRecyclerView
                    )
                }
            }
        }
    }
}
```

### Example 2: Tasks Fragment

```kotlin
class TasksFragment : Fragment() {
    private lateinit var recyclerTasks: RecyclerView
    private lateinit var skeletonRecyclerView: RecyclerView
    
    private fun setupViews(view: View) {
        recyclerTasks = view.findViewById(R.id.recycler_tasks)
        skeletonRecyclerView = view.findViewById(R.id.skeletonRecyclerView)
        
        // Setup skeleton loader
        skeletonRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = SkeletonLoaderHelper.createSkeletonAdapter(
                R.layout.item_task_skeleton,
                5
            )
        }
    }
    
    private fun loadTasks() {
        // Show skeleton while loading
        SkeletonLoaderHelper.showSkeleton(skeletonRecyclerView, recyclerTasks)
        
        viewLifecycleOwner.lifecycleScope.launch {
            val tasks = taskRepository.getTasks()
            taskAdapter.submitList(tasks)
            
            // Show content with fade animation
            SkeletonLoaderHelper.showContent(skeletonRecyclerView, recyclerTasks)
        }
    }
}
```

### Example 3: ChatRoomActivity (Messages)

```kotlin
class ChatRoomActivity : AppCompatActivity() {
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var skeletonRecyclerView: RecyclerView
    
    private fun setupViews() {
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView)
        skeletonRecyclerView = findViewById(R.id.skeletonRecyclerView)
        
        // Setup skeleton loader
        skeletonRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatRoomActivity).apply {
                reverseLayout = true  // Match messages layout
            }
            adapter = SkeletonLoaderHelper.createSkeletonAdapter(
                R.layout.item_message_skeleton,
                8  // Show more items for messages
            )
        }
    }
    
    private fun loadMessages(chatId: String) {
        // Show skeleton
        SkeletonLoaderHelper.showSkeleton(skeletonRecyclerView, messagesRecyclerView)
        
        lifecycleScope.launch {
            chatRepository.getChatMessages(chatId).collect { messages ->
                messageAdapter.submitList(messages)
                
                // Show content
                SkeletonLoaderHelper.showContent(skeletonRecyclerView, messagesRecyclerView)
            }
        }
    }
}
```

## Layout Integration

### Step 1: Add Skeleton RecyclerView to Layout

Wrap your content RecyclerView in a FrameLayout and add a skeleton RecyclerView:

```xml
<FrameLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!-- Skeleton Loader Container -->
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/skeletonRecyclerView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:visibility="gone" />

    <!-- Actual Content -->
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/contentRecyclerView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
</FrameLayout>
```

### Step 2: Initialize Skeleton Adapter

```kotlin
skeletonRecyclerView.apply {
    layoutManager = LinearLayoutManager(context)
    adapter = SkeletonLoaderHelper.createSkeletonAdapter(
        R.layout.item_your_skeleton,  // Your skeleton layout
        5  // Number of skeleton items to show
    )
}
```

### Step 3: Show/Hide Based on Loading State

```kotlin
if (isLoading) {
    SkeletonLoaderHelper.showSkeleton(skeletonRecyclerView, contentRecyclerView)
} else {
    SkeletonLoaderHelper.showContent(skeletonRecyclerView, contentRecyclerView)
}
```

## Creating Custom Skeleton Layouts

To create a skeleton layout for a new item type:

1. **Analyze the original layout** - Identify key visual elements
2. **Replace content with SkeletonLoader views** - Use SkeletonLoader for each placeholder
3. **Match dimensions** - Keep the same sizes as the original
4. **Adjust corner radius** - Use appropriate corner radius for each element

Example:

```xml
<!-- Original TextView -->
<TextView
    android:id="@+id/titleTextView"
    android:layout_width="match_parent"
    android:layout_height="20dp"
    android:textSize="16sp" />

<!-- Skeleton Equivalent -->
<com.example.loginandregistration.views.SkeletonLoader
    android:layout_width="match_parent"
    android:layout_height="20dp"
    app:skeletonCornerRadius="4dp" />
```

## Best Practices

1. **Show skeleton only on initial load** - Don't show skeleton on pull-to-refresh
2. **Match skeleton count to visible items** - Show 5-8 skeleton items typically
3. **Use appropriate corner radius** - Circular for avatars (50%), rounded for text (4dp)
4. **Maintain layout consistency** - Skeleton should match actual content dimensions
5. **Fade animations** - Always use fade transitions for smooth UX
6. **Check adapter count** - Only show skeleton when adapter is empty

## Customization

### Custom Colors

Add to your theme or colors.xml:

```xml
<color name="skeleton_base">#E0E0E0</color>
<color name="skeleton_shimmer">#F5F5F5</color>
```

For dark mode, add to `values-night/colors.xml`:

```xml
<color name="skeleton_base">#2C2C2E</color>
<color name="skeleton_shimmer">#3A3A3C</color>
```

### Custom Animation Duration

Modify in SkeletonLoader.kt:

```kotlin
shimmerAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
    duration = 1500  // Change this value (milliseconds)
    repeatCount = ValueAnimator.INFINITE
    interpolator = LinearInterpolator()
}
```

## Testing

1. **Test on slow network** - Enable network throttling to see skeleton loaders
2. **Test empty states** - Verify skeleton shows when no data exists
3. **Test transitions** - Ensure smooth fade animations
4. **Test dark mode** - Verify skeleton colors work in dark theme
5. **Test different screen sizes** - Ensure skeleton layouts adapt properly

## Troubleshooting

### Skeleton not showing
- Check that `visibility` is set to `gone` initially
- Verify `showSkeleton()` is called before data loads
- Ensure skeleton adapter is properly initialized

### Animation not smooth
- Check device performance
- Reduce animation duration if needed
- Verify no heavy operations on UI thread

### Layout mismatch
- Compare skeleton layout dimensions with actual layout
- Ensure same padding and margins
- Match RecyclerView layout manager settings

## Requirements Coverage

This implementation satisfies **Requirement 8.1**:
- ✅ Created `SkeletonLoader.kt` custom view with shimmer effect
- ✅ Added skeleton layout for chat list items
- ✅ Added skeleton layout for message list items
- ✅ Added skeleton layout for task list items
- ✅ Show skeleton while loading data
- ✅ Fade out skeleton and fade in actual content

## Next Steps

To complete skeleton loader integration across the app:

1. Add skeleton support to TasksFragment
2. Add skeleton support to ChatRoomActivity
3. Add skeleton support to CalendarFragment (if needed)
4. Test all loading states thoroughly
5. Adjust skeleton item counts based on screen size
6. Add dark mode color support
