# Task 29: Connection Status Indicator - Visual Guide

## Overview
This visual guide describes the appearance and behavior of the connection status indicator banner.

## Banner States

### 1. Online State (Hidden)
```
┌─────────────────────────────────────┐
│         [App Content]               │
│                                     │
│  No banner visible - normal state  │
│                                     │
└─────────────────────────────────────┘
```
**Description**: When the device has internet connection, the banner is completely hidden and the app displays normally.

---

### 2. Offline State (Red Banner)
```
┌─────────────────────────────────────┐
│ ⚠️  No internet connection          │  ← Red banner (#FF3B30)
├─────────────────────────────────────┤
│         [App Content]               │
│                                     │
│  Banner slides down from top        │
│                                     │
└─────────────────────────────────────┘
```
**Description**: 
- **Color**: Red (#FF3B30 - colorError)
- **Icon**: Error/warning icon (⚠️)
- **Text**: "No internet connection"
- **Animation**: Slides down from top (300ms)
- **Behavior**: Remains visible while offline

---

### 3. Connecting State (Orange Banner)
```
┌─────────────────────────────────────┐
│ 🔄  Connecting...                   │  ← Orange banner (#FF9500)
├─────────────────────────────────────┤
│         [App Content]               │
│                                     │
│  Banner changes color and text      │
│                                     │
└─────────────────────────────────────┘
```
**Description**:
- **Color**: Orange (#FF9500 - colorWarning)
- **Icon**: Sync/refresh icon (🔄)
- **Text**: "Connecting..."
- **Duration**: Displays for 1.5 seconds
- **Animation**: Transitions from red, then slides up (300ms)
- **Behavior**: Shows briefly when connection is restored

---

## Animation Sequence

### Going Offline
```
Step 1: Normal State (No Banner)
┌─────────────────────────────────────┐
│         [App Content]               │
└─────────────────────────────────────┘

Step 2: Connection Lost Detected
┌─────────────────────────────────────┐
│ [Banner starting to slide down]     │  ← translationY: -height → 0
│         [App Content]               │
└─────────────────────────────────────┘

Step 3: Banner Fully Visible (300ms later)
┌─────────────────────────────────────┐
│ ⚠️  No internet connection          │
├─────────────────────────────────────┤
│         [App Content]               │
└─────────────────────────────────────┘
```

### Going Online
```
Step 1: Offline State (Red Banner)
┌─────────────────────────────────────┐
│ ⚠️  No internet connection          │
├─────────────────────────────────────┤
│         [App Content]               │
└─────────────────────────────────────┘

Step 2: Connection Restored
┌─────────────────────────────────────┐
│ 🔄  Connecting...                   │  ← Color changes to orange
├─────────────────────────────────────┤
│         [App Content]               │
└─────────────────────────────────────┘

Step 3: After 1.5 seconds
┌─────────────────────────────────────┐
│ [Banner starting to slide up]       │  ← translationY: 0 → -height
│         [App Content]               │
└─────────────────────────────────────┘

Step 4: Banner Hidden (300ms later)
┌─────────────────────────────────────┐
│         [App Content]               │
└─────────────────────────────────────┘
```

---

## Layout Structure

### MainActivity Layout
```
┌─────────────────────────────────────┐
│  [Status Bar]                       │
├─────────────────────────────────────┤
│  [ConnectionStatusView]             │  ← Banner (hidden by default)
├─────────────────────────────────────┤
│                                     │
│  [Fragment Container]               │
│  - HomeFragment                     │
│  - GroupsFragment                   │
│  - TasksFragment                    │
│  - CalendarFragment                 │
│  - ChatFragment                     │
│                                     │
├─────────────────────────────────────┤
│  [Bottom Navigation]                │
└─────────────────────────────────────┘
```

### ChatRoomActivity Layout
```
┌─────────────────────────────────────┐
│  [Status Bar]                       │
├─────────────────────────────────────┤
│  [Toolbar with Chat Name]           │
├─────────────────────────────────────┤
│  [ConnectionStatusView]             │  ← Banner (hidden by default)
├─────────────────────────────────────┤
│  [Load More Progress]               │
├─────────────────────────────────────┤
│                                     │
│  [Messages RecyclerView]            │
│                                     │
├─────────────────────────────────────┤
│  [Typing Indicator]                 │
├─────────────────────────────────────┤
│  [Message Input Area]               │
└─────────────────────────────────────┘
```

---

## Visual Specifications

### Banner Dimensions
- **Width**: Match parent (full screen width)
- **Height**: Wrap content (~48dp with padding)
- **Padding**: 12dp all sides
- **Elevation**: 4dp (casts shadow)

### Icon Specifications
- **Size**: 20dp x 20dp
- **Margin**: 8dp end margin
- **Tint**: White (#FFFFFF)
- **Offline Icon**: `android.R.drawable.stat_notify_error`
- **Connecting Icon**: `android.R.drawable.stat_notify_sync`

### Text Specifications
- **Size**: 14sp
- **Color**: White (#FFFFFF)
- **Style**: Bold
- **Alignment**: Center vertical

### Color Specifications
- **Offline Background**: #FF3B30 (Red - colorError)
- **Connecting Background**: #FF9500 (Orange - colorWarning)
- **Text/Icon Color**: #FFFFFF (White)

---

## User Interaction

### No Direct Interaction
The banner is **informational only** and does not respond to user taps or gestures. It automatically appears and disappears based on network connectivity.

### Behavior Flow
```
[Online] → [Connection Lost] → [Show Red Banner]
                                      ↓
                              [User sees offline state]
                                      ↓
                              [Connection Restored]
                                      ↓
                              [Show Orange Banner]
                                      ↓
                              [Wait 1.5 seconds]
                                      ↓
                              [Hide Banner] → [Online]
```

---

## Accessibility

### Screen Reader Announcements
When using TalkBack or other screen readers:
- **Offline**: "No internet connection"
- **Connecting**: "Connecting..."
- **Online**: Banner disappears (no announcement)

### Visual Accessibility
- **High Contrast**: Red and orange colors provide clear visual distinction
- **Icon Support**: Icons supplement text for better comprehension
- **Text Size**: 14sp is readable on most devices
- **Bold Text**: Ensures readability

---

## Responsive Design

### Portrait Mode
```
┌─────────────────────┐
│ ⚠️  No internet     │
│     connection      │
├─────────────────────┤
│   [App Content]     │
│                     │
└─────────────────────┘
```

### Landscape Mode
```
┌───────────────────────────────────────────┐
│ ⚠️  No internet connection                │
├───────────────────────────────────────────┤
│   [App Content]                           │
└───────────────────────────────────────────┘
```

### Different Screen Sizes
- **Small Phones**: Banner scales to screen width
- **Tablets**: Banner spans full width
- **Foldables**: Adapts to current screen configuration

---

## Implementation Details

### View Hierarchy
```
ConnectionStatusView (LinearLayout)
└── view_connection_status.xml (merge)
    └── LinearLayout (banner)
        ├── ImageView (statusIcon)
        └── TextView (statusText)
```

### State Management
```kotlin
// State tracking
private var wasOffline = false
private var isAnimating = false

// State transitions
Online → Offline: wasOffline = true, show red banner
Offline → Online: show orange banner, wait 1.5s, hide, wasOffline = false
```

---

## Testing Checklist

### Visual Verification
- [ ] Banner appears at correct position (top of screen)
- [ ] Banner has proper elevation/shadow
- [ ] Red color is correct for offline state
- [ ] Orange color is correct for connecting state
- [ ] Icon is visible and properly sized
- [ ] Text is readable and properly styled
- [ ] Banner width matches screen width
- [ ] Banner doesn't overlap important content

### Animation Verification
- [ ] Slide down animation is smooth (300ms)
- [ ] Slide up animation is smooth (300ms)
- [ ] No animation jank or stuttering
- [ ] Animations don't overlap
- [ ] Banner appears/disappears at correct times

### Functional Verification
- [ ] Banner shows when going offline
- [ ] Banner shows "Connecting..." when reconnecting
- [ ] Banner hides after connection is stable
- [ ] Works in MainActivity
- [ ] Works in ChatRoomActivity
- [ ] Handles rapid connection changes
- [ ] No crashes or errors

---

## Screenshots Locations

When testing, capture screenshots of:
1. **Normal state** (no banner)
2. **Offline state** (red banner)
3. **Connecting state** (orange banner)
4. **MainActivity with banner**
5. **ChatRoomActivity with banner**
6. **Portrait orientation**
7. **Landscape orientation**

Save screenshots to: `screenshots/task_29/`

---

## Comparison with Design Requirements

| Requirement | Implementation | Status |
|------------|----------------|--------|
| Banner at top of screen | ✅ Positioned at top | ✅ |
| Show "No internet connection" | ✅ Red banner with text | ✅ |
| Show "Connecting..." | ✅ Orange banner with text | ✅ |
| Hide when online | ✅ Slides up and hides | ✅ |
| Use ConnectionMonitor | ✅ Integrated | ✅ |
| Animate slide in/out | ✅ 300ms animations | ✅ |

---

## Future Enhancements (Optional)

Potential improvements for future iterations:
- Add haptic feedback when connection state changes
- Add sound notification for connection loss (optional setting)
- Show network type (WiFi/Mobile) when connected
- Add "Retry" button for manual connection check
- Customize banner colors via theme
- Add animation for color transitions
- Show connection speed indicator
