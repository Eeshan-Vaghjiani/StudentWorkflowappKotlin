# EmptyStateView Visual Guide

## Component Structure

```
┌─────────────────────────────────────────┐
│                                         │
│                                         │
│              [ICON]                     │
│            120x120dp                    │
│                                         │
│                                         │
│            Title Text                   │
│          20sp, Bold                     │
│                                         │
│         Description Text                │
│        14sp, Secondary                  │
│                                         │
│                                         │
│        [Action Button]                  │
│          (Optional)                     │
│                                         │
│                                         │
└─────────────────────────────────────────┘
```

## Empty State Types

### 1. No Chats Yet

```
┌─────────────────────────────────────────┐
│                                         │
│                                         │
│              💬                         │
│         (Chat Icon)                     │
│                                         │
│                                         │
│          No Chats Yet                   │
│                                         │
│   Start a conversation by creating      │
│   a new chat or joining a group         │
│                                         │
│                                         │
│        [  Start Chat  ]                 │
│                                         │
│                                         │
└─────────────────────────────────────────┘
```

**When to use**: Chat list is empty
**Action**: Opens user search dialog

---

### 2. No Messages

```
┌─────────────────────────────────────────┐
│                                         │
│                                         │
│              💭                         │
│       (Message Icon)                    │
│                                         │
│                                         │
│          No Messages                    │
│                                         │
│   Be the first to send a message        │
│         in this chat                    │
│                                         │
│                                         │
│                                         │
│                                         │
└─────────────────────────────────────────┘
```

**When to use**: Chat room has no messages yet
**Action**: None (user can type in input field)

---

### 3. No Tasks Yet

```
┌─────────────────────────────────────────┐
│                                         │
│                                         │
│              ✓                          │
│        (Task Icon)                      │
│                                         │
│                                         │
│          No Tasks Yet                   │
│                                         │
│   Create your first task to get         │
│   started with organizing your work     │
│                                         │
│                                         │
│        [ Create Task ]                  │
│                                         │
│                                         │
└─────────────────────────────────────────┘
```

**When to use**: Task list is empty
**Action**: Opens create task screen

---

### 4. No Internet Connection

```
┌─────────────────────────────────────────┐
│                                         │
│                                         │
│              📡                         │
│      (No Internet Icon)                 │
│                                         │
│                                         │
│     No Internet Connection              │
│                                         │
│   Please check your connection          │
│         and try again                   │
│                                         │
│                                         │
│          [  Retry  ]                    │
│                                         │
│                                         │
└─────────────────────────────────────────┘
```

**When to use**: Network connection is lost
**Action**: Retries connection/refresh

---

### 5. No Groups Yet

```
┌─────────────────────────────────────────┐
│                                         │
│                                         │
│              👥                         │
│       (Group Icon)                      │
│                                         │
│                                         │
│         No Groups Yet                   │
│                                         │
│   Create or join a group to             │
│     collaborate with others             │
│                                         │
│                                         │
│       [ Create Group ]                  │
│                                         │
│                                         │
└─────────────────────────────────────────┘
```

**When to use**: Group list is empty
**Action**: Opens create group dialog

---

### 6. No Search Results

```
┌─────────────────────────────────────────┐
│                                         │
│                                         │
│              🔍                         │
│       (Search Icon)                     │
│                                         │
│                                         │
│       No Results Found                  │
│                                         │
│   Try adjusting your search terms       │
│                                         │
│                                         │
│                                         │
│                                         │
│                                         │
└─────────────────────────────────────────┘
```

**When to use**: Search returns no results
**Action**: None (user can modify search)

---

### 7. No Notifications

```
┌─────────────────────────────────────────┐
│                                         │
│                                         │
│              🔔                         │
│    (Notification Icon)                  │
│                                         │
│                                         │
│       No Notifications                  │
│                                         │
│   You're all caught up! Check back      │
│       later for updates                 │
│                                         │
│                                         │
│                                         │
│                                         │
└─────────────────────────────────────────┘
```

**When to use**: Notification list is empty
**Action**: None

---

## Color Scheme

### Light Theme
- **Icon Tint**: `colorTextSecondary` (#8E8E93)
- **Title Color**: `colorTextPrimary` (#000000)
- **Description Color**: `colorTextSecondary` (#8E8E93)
- **Button Background**: `colorPrimaryBlue` (#007AFF)
- **Button Text**: `white` (#FFFFFF)

### Dark Theme (if implemented)
- **Icon Tint**: `dark_text_secondary` (#8E8E93)
- **Title Color**: `dark_text_primary` (#FFFFFF)
- **Description Color**: `dark_text_secondary` (#8E8E93)
- **Button Background**: `colorPrimaryBlue` (#007AFF)
- **Button Text**: `white` (#FFFFFF)

## Spacing Guidelines

```
┌─────────────────────────────────────────┐
│         32dp padding (all sides)        │
│                                         │
│              [ICON]                     │
│            120x120dp                    │
│                                         │
│         24dp margin bottom              │
│                                         │
│            Title Text                   │
│                                         │
│          8dp margin bottom              │
│                                         │
│         Description Text                │
│          max width: 280dp               │
│                                         │
│         24dp margin bottom              │
│                                         │
│        [Action Button]                  │
│                                         │
│                                         │
└─────────────────────────────────────────┘
```

## Typography

### Title
- **Size**: 20sp
- **Weight**: Bold
- **Color**: Primary text color
- **Alignment**: Center

### Description
- **Size**: 14sp
- **Weight**: Regular
- **Color**: Secondary text color
- **Alignment**: Center
- **Max Width**: 280dp

### Button
- **Size**: 14sp (default Material button)
- **Weight**: Medium
- **Color**: White on blue background
- **Style**: MaterialButton with rounded corners

## Icon Specifications

### Size
- **Width**: 120dp
- **Height**: 120dp

### Tint
- **Color**: Secondary text color
- **Opacity**: Applied via tint attribute

### Source
- Vector drawables (XML)
- 24x24dp viewport
- Scaled up to 120dp in layout

## Responsive Behavior

### Portrait Mode
```
┌──────────────┐
│              │
│              │
│    [ICON]    │
│              │
│    Title     │
│              │
│ Description  │
│              │
│   [Button]   │
│              │
│              │
└──────────────┘
```

### Landscape Mode
```
┌────────────────────────────────────┐
│                                    │
│  [ICON]    Title                   │
│            Description             │
│            [Button]                │
│                                    │
└────────────────────────────────────┘
```
(Note: Current implementation uses vertical layout for both)

## Animation Recommendations

### Fade In
```kotlin
emptyStateView.apply {
    alpha = 0f
    visibility = View.VISIBLE
    animate()
        .alpha(1f)
        .setDuration(300)
        .start()
}
```

### Slide Up
```kotlin
emptyStateView.apply {
    translationY = 50f
    alpha = 0f
    visibility = View.VISIBLE
    animate()
        .translationY(0f)
        .alpha(1f)
        .setDuration(300)
        .start()
}
```

## Accessibility

### Content Descriptions
- Icon has content description: "Empty state icon"
- Title is read by screen readers
- Description is read by screen readers
- Button has implicit content description from text

### Touch Targets
- Button meets minimum 48dp touch target
- Entire empty state is non-interactive except button

### Color Contrast
- Title: High contrast (black on white)
- Description: Medium contrast (gray on white)
- Button: High contrast (white on blue)

## States

### Default State
```
Visibility: GONE
```

### Loading State
```
Show skeleton loader instead
```

### Empty State
```
Visibility: VISIBLE
Icon: Appropriate for context
Title: Descriptive
Description: Helpful guidance
Button: Optional, context-dependent
```

### Error State
```
Use "No Internet" or custom error state
```

## Integration Patterns

### Pattern 1: Simple Toggle
```
┌─────────────────────────────────────────┐
│                                         │
│  if (items.isEmpty()) {                 │
│      show empty state                   │
│  } else {                               │
│      show content                       │
│  }                                      │
│                                         │
└─────────────────────────────────────────┘
```

### Pattern 2: With Loading
```
┌─────────────────────────────────────────┐
│                                         │
│  if (isLoading) {                       │
│      show skeleton                      │
│  } else if (items.isEmpty()) {          │
│      show empty state                   │
│  } else {                               │
│      show content                       │
│  }                                      │
│                                         │
└─────────────────────────────────────────┘
```

### Pattern 3: With Error
```
┌─────────────────────────────────────────┐
│                                         │
│  if (error != null) {                   │
│      show error state                   │
│  } else if (isLoading) {                │
│      show skeleton                      │
│  } else if (items.isEmpty()) {          │
│      show empty state                   │
│  } else {                               │
│      show content                       │
│  }                                      │
│                                         │
└─────────────────────────────────────────┘
```

## Comparison: Before vs After

### Before (Old Implementation)
```xml
<LinearLayout
    android:id="@+id/emptyStateLayout"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_gravity="center"
    android:gravity="center"
    android:orientation="vertical"
    android:visibility="gone">

    <ImageView
        android:layout_width="120dp"
        android:layout_height="120dp"
        android:alpha="0.3"
        android:src="@android:drawable/ic_dialog_email"
        app:tint="@color/colorTextSecondary" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:text="No chats yet"
        android:textColor="@color/colorTextPrimary"
        android:textSize="20sp"
        android:textStyle="bold" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:gravity="center"
        android:paddingStart="32dp"
        android:paddingEnd="32dp"
        android:text="Start a conversation..."
        android:textColor="@color/colorTextSecondary"
        android:textSize="14sp" />
</LinearLayout>
```

### After (New Implementation)
```xml
<com.example.loginandregistration.views.EmptyStateView
    android:id="@+id/emptyStateView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:visibility="gone" />
```

```kotlin
// In code
emptyStateView.showNoChats {
    openUserSearchDialog()
}
```

**Benefits**:
- ✅ Less XML boilerplate
- ✅ Consistent styling
- ✅ Reusable across app
- ✅ Easy to maintain
- ✅ Built-in action button support

---

**For implementation details, see**: `TASK_32_IMPLEMENTATION_SUMMARY.md`
**For usage examples, see**: `TASK_32_USAGE_GUIDE.md`
