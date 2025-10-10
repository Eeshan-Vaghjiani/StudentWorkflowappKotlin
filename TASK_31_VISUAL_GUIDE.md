# Task 31: Skeleton Loaders - Visual Guide

## Overview

This visual guide explains what skeleton loaders look like and how they work in the app.

## What is a Skeleton Loader?

A skeleton loader is a placeholder UI that shows the structure of content while it's loading. Instead of showing a blank screen or a spinning loader, users see a "skeleton" version of the content with animated shimmer effects.

## Visual Representation

### Chat List Skeleton

```
┌─────────────────────────────────────────────────┐
│  ┌────┐  ████████████████        ██████         │
│  │ ◯  │  ████████████████        ██████         │
│  └────┘  ████████████████████████               │
│           ████████████████                       │
├─────────────────────────────────────────────────┤
│  ┌────┐  ████████████████        ██████         │
│  │ ◯  │  ████████████████        ██████         │
│  └────┘  ████████████████████████               │
│           ████████████████                       │
├─────────────────────────────────────────────────┤
│  ┌────┐  ████████████████        ██████         │
│  │ ◯  │  ████████████████        ██████         │
│  └────┘  ████████████████████████               │
│           ████████████████                       │
└─────────────────────────────────────────────────┘

Legend:
◯ = Circular profile placeholder
█ = Shimmer placeholder
```

**Components:**
- Circular profile image (56dp diameter)
- Chat name (full width)
- Timestamp (right-aligned, 60dp)
- Last message line 1 (full width)
- Last message line 2 (shorter, 200dp)

### Message List Skeleton

```
┌─────────────────────────────────────────────────┐
│  ┌──┐  ████████                                  │
│  │◯ │  ┌──────────────────────────────┐         │
│  └──┘  │ ████████████████████████     │         │
│        │ ██████████████████           │         │
│        │ ██████                       │         │
│        └──────────────────────────────┘         │
│                                                  │
│  ┌──┐  ████████                                  │
│  │◯ │  ┌──────────────────────────────┐         │
│  └──┘  │ ████████████████████████     │         │
│        │ ██████████████████           │         │
│        │ ██████                       │         │
│        └──────────────────────────────┘         │
└─────────────────────────────────────────────────┘

Legend:
◯ = Circular sender profile (40dp)
█ = Shimmer placeholder
└─┘ = Message bubble background
```

**Components:**
- Circular sender profile (40dp diameter)
- Sender name (100dp width)
- Message bubble background
- Message text line 1 (220dp)
- Message text line 2 (180dp)
- Timestamp (60dp)

### Task List Skeleton

```
┌─────────────────────────────────────────────────┐
│  ┌──┐  ████████████████████████  ┌────────┐  ▶ │
│  │◯ │  ████████████████          │ ████   │    │
│  └──┘                             └────────┘    │
├─────────────────────────────────────────────────┤
│  ┌──┐  ████████████████████████  ┌────────┐  ▶ │
│  │◯ │  ████████████████          │ ████   │    │
│  └──┘                             └────────┘    │
├─────────────────────────────────────────────────┤
│  ┌──┐  ████████████████████████  ┌────────┐  ▶ │
│  │◯ │  ████████████████          │ ████   │    │
│  └──┘                             └────────┘    │
└─────────────────────────────────────────────────┘

Legend:
◯ = Circular task icon (40dp)
█ = Shimmer placeholder
└─┘ = Rounded chip background
▶ = Arrow indicator
```

**Components:**
- Circular task icon (40dp diameter)
- Task title (full width)
- Task subtitle (200dp width)
- Rounded status chip (80dp width, 12dp corner radius)
- Arrow indicator (12dp)

## Shimmer Animation

The shimmer effect is a gradient that moves across the skeleton:

```
Frame 1:  [▓░░░░░░░░░░░░░░░░░░░]
Frame 2:  [░▓░░░░░░░░░░░░░░░░░░]
Frame 3:  [░░▓░░░░░░░░░░░░░░░░░]
Frame 4:  [░░░▓░░░░░░░░░░░░░░░░]
Frame 5:  [░░░░▓░░░░░░░░░░░░░░░]
...
Frame N:  [░░░░░░░░░░░░░░░░░░▓░]

Legend:
▓ = Shimmer highlight (#F5F5F5)
░ = Base color (#E0E0E0)
```

**Animation Properties:**
- Duration: 1.5 seconds per cycle
- Direction: Left to right
- Repeat: Infinite
- Interpolation: Linear
- Shimmer width: 30% of view width

## Transition Animation

### Loading → Content Transition

```
Step 1: Skeleton Visible (100% opacity)
┌─────────────────────────┐
│  ┌──┐  ████████████     │
│  │◯ │  ████████         │
│  └──┘  ████████████     │
└─────────────────────────┘

Step 2: Skeleton Fading Out (50% opacity)
┌─────────────────────────┐
│  ┌──┐  ▒▒▒▒▒▒▒▒▒▒▒▒     │
│  │◯ │  ▒▒▒▒▒▒▒▒         │
│  └──┘  ▒▒▒▒▒▒▒▒▒▒▒▒     │
└─────────────────────────┘

Step 3: Content Fading In (50% opacity)
┌─────────────────────────┐
│  [👤]  John Doe    2:30  │
│        Hey, how are...   │
│                          │
└─────────────────────────┘

Step 4: Content Visible (100% opacity)
┌─────────────────────────┐
│  [👤]  John Doe    2:30  │
│        Hey, how are you? │
│                          │
└─────────────────────────┘

Timeline:
0ms ────────── 300ms ────────── 600ms
│               │                │
Skeleton 100%   Skeleton 0%     Content 100%
Content 0%      Content 0%      
```

## Color Scheme

### Light Mode
```
Base Color:    #E0E0E0 ░░░░░░░░░░
Shimmer Color: #F5F5F5 ▓▓▓▓▓▓▓▓▓▓
```

### Dark Mode (Future)
```
Base Color:    #2C2C2E ░░░░░░░░░░
Shimmer Color: #3A3A3C ▓▓▓▓▓▓▓▓▓▓
```

## Corner Radius Examples

### Circular (Profile/Icon)
```
┌────────┐
│  ┌──┐  │  radius = width / 2
│  │◯ │  │  Example: 40dp width → 20dp radius
│  └──┘  │
└────────┘
```

### Rounded (Chip)
```
┌────────────┐
│ ╭────────╮ │  radius = 12dp
│ │ ████   │ │  Fixed radius for chips
│ ╰────────╯ │
└────────────┘
```

### Slightly Rounded (Text)
```
┌────────────┐
│ ┌────────┐ │  radius = 4dp
│ │ ██████ │ │  Subtle rounding for text
│ └────────┘ │
└────────────┘
```

## Layout Structure

### Fragment Layout Hierarchy
```
FrameLayout (container)
├── RecyclerView (skeleton) [visibility: gone initially]
│   └── SkeletonAdapter
│       ├── item_chat_skeleton.xml (item 1)
│       ├── item_chat_skeleton.xml (item 2)
│       ├── item_chat_skeleton.xml (item 3)
│       ├── item_chat_skeleton.xml (item 4)
│       └── item_chat_skeleton.xml (item 5)
└── RecyclerView (content) [visibility: visible]
    └── ChatAdapter
        ├── item_chat.xml (actual data)
        └── ...
```

## State Diagram

```
┌─────────────┐
│   Initial   │
│   State     │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  Loading    │◄──────────┐
│  Started    │           │
└──────┬──────┘           │
       │                  │
       ▼                  │
┌─────────────┐           │
│  Skeleton   │           │
│  Visible    │           │
│  (Shimmer)  │           │
└──────┬──────┘           │
       │                  │
       ▼                  │
┌─────────────┐           │
│  Data       │           │
│  Loaded     │           │
└──────┬──────┘           │
       │                  │
       ▼                  │
┌─────────────┐           │
│  Fade Out   │           │
│  Skeleton   │           │
└──────┬──────┘           │
       │                  │
       ▼                  │
┌─────────────┐           │
│  Fade In    │           │
│  Content    │           │
└──────┬──────┘           │
       │                  │
       ▼                  │
┌─────────────┐           │
│  Content    │           │
│  Visible    │───Refresh─┘
└─────────────┘
```

## Comparison: Before vs After

### Before (Without Skeleton)
```
Loading State:
┌─────────────────────────┐
│                         │
│                         │
│      [Spinner]          │
│                         │
│                         │
└─────────────────────────┘

Problems:
- Blank screen
- No context
- Feels slow
- Jarring transition
```

### After (With Skeleton)
```
Loading State:
┌─────────────────────────┐
│  ┌──┐  ████████████     │
│  │◯ │  ████████         │
│  └──┘  ████████████     │
│  ┌──┐  ████████████     │
│  │◯ │  ████████         │
│  └──┘  ████████████     │
└─────────────────────────┘

Benefits:
- Shows structure
- Provides context
- Feels faster
- Smooth transition
```

## Real-World Examples

Apps that use skeleton loaders:
- Facebook (news feed)
- LinkedIn (profile, feed)
- YouTube (video list)
- Instagram (stories, feed)
- Twitter (timeline)
- Medium (article list)

## Implementation Checklist

Visual verification checklist:

- [ ] Skeleton matches actual content layout
- [ ] Circular elements are perfectly round
- [ ] Shimmer animation is smooth
- [ ] Shimmer moves left to right
- [ ] Animation loops continuously
- [ ] Fade out is smooth (300ms)
- [ ] Fade in is smooth (300ms)
- [ ] No flicker during transition
- [ ] Colors are subtle but visible
- [ ] Spacing matches actual content
- [ ] All skeleton items are identical
- [ ] Skeleton shows on initial load only
- [ ] Skeleton doesn't show on refresh

## Tips for Visual Design

1. **Match Dimensions:** Skeleton should match actual content size
2. **Subtle Colors:** Don't make skeleton too bright or dark
3. **Smooth Animation:** 1-2 second cycle is ideal
4. **Appropriate Count:** Show 5-8 items typically
5. **Consistent Spacing:** Match padding and margins exactly
6. **Corner Radius:** Use appropriate radius for each element type
7. **Fade Duration:** 300ms is smooth but not too slow

## Accessibility Considerations

For screen readers:
- Skeleton should have content description "Loading"
- Announce when content loads
- Don't interrupt user with loading announcements
- Ensure focus moves to content after loading

## Conclusion

Skeleton loaders provide a professional, modern loading experience that:
- Reduces perceived loading time
- Provides visual context
- Creates smooth transitions
- Matches industry best practices
- Improves overall user experience

The implementation is complete, reusable, and ready for production use.
