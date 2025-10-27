# Task 14: AI Assistant UI - Visual Guide

## Screen Layout Overview

```
┌─────────────────────────────────────┐
│  ← AI Assistant                     │  ← Toolbar
├─────────────────────────────────────┤
│                                     │
│  ┌─────────────────────────────┐   │
│  │      🧠 AI Brain Icon        │   │  ← Empty State
│  │                              │   │  (shown when no messages)
│  │      AI Assistant            │   │
│  │                              │   │
│  │  Ask me to help you create   │   │
│  │  assignments, organize       │   │
│  │  tasks, or provide study     │   │
│  │  tips!                       │   │
│  └─────────────────────────────┘   │
│                                     │
├─────────────────────────────────────┤
│  [Type message here...]        [→]  │  ← Input Area
└─────────────────────────────────────┘
```

## With Messages

```
┌─────────────────────────────────────┐
│  ← AI Assistant                     │
├─────────────────────────────────────┤
│                                     │
│  ┌─────────────────────────────┐   │
│  │ Create a Math homework      │   │  ← User Message
│  │ assignment                  │   │  (right-aligned, blue)
│  │                    10:30 AM │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ I'll help you create that   │   │  ← AI Message
│  │ assignment. What's the due  │   │  (left-aligned, gray)
│  │ date?                       │   │
│  │ 10:30 AM                    │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ Due next Friday             │   │  ← User Message
│  │                    10:31 AM │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ Great! I've created the     │   │  ← AI Message
│  │ assignment for you.         │   │  with Action Button
│  │                             │   │
│  │  [Create Assignment]        │   │
│  │ 10:31 AM                    │   │
│  └─────────────────────────────┘   │
│                                     │
├─────────────────────────────────────┤
│  [Type message here...]        [→]  │
└─────────────────────────────────────┘
```

## Loading State

```
┌─────────────────────────────────────┐
│  ← AI Assistant                     │
├─────────────────────────────────────┤
│                                     │
│  [Previous messages...]             │
│                                     │
├─────────────────────────────────────┤
│  ⏳ AI is thinking...               │  ← Loading Indicator
├─────────────────────────────────────┤
│  [Type message here...]        [→]  │
│  (disabled)                         │
└─────────────────────────────────────┘
```

## UI Components Breakdown

### 1. Toolbar
```
┌─────────────────────────────────────┐
│  ← AI Assistant                     │
└─────────────────────────────────────┘
```
- **Back Button**: Returns to Tasks screen
- **Title**: "AI Assistant"
- **Background**: Primary color (blue)
- **Text Color**: White

### 2. Empty State
```
┌─────────────────────────────────────┐
│                                     │
│         🧠                          │
│      (AI Brain Icon)                │
│                                     │
│      AI Assistant                   │
│                                     │
│  Ask me to help you create          │
│  assignments, organize tasks,       │
│  or provide study tips!             │
│                                     │
└─────────────────────────────────────┘
```
- **Icon**: AI brain (ic_ai_brain)
- **Size**: 120dp x 120dp
- **Opacity**: 50%
- **Title**: "AI Assistant"
- **Description**: Instructions for user

### 3. User Message
```
                    ┌─────────────────┐
                    │ Your message    │
                    │ text here       │
                    │        10:30 AM │
                    └─────────────────┘
```
- **Alignment**: Right
- **Background**: Blue (primary color)
- **Text Color**: White
- **Padding**: 12dp
- **Corner Radius**: 16dp
- **Timestamp**: Bottom right, small, semi-transparent

### 4. AI Message
```
┌─────────────────┐
│ AI response     │
│ text here       │
│ 10:30 AM        │
└─────────────────┘
```
- **Alignment**: Left
- **Background**: Gray (card background)
- **Text Color**: Dark (text primary)
- **Padding**: 12dp
- **Corner Radius**: 16dp
- **Timestamp**: Bottom left, small, secondary color

### 5. AI Message with Action
```
┌─────────────────────────────────┐
│ I've created the assignment     │
│ details for you.                │
│                                 │
│  [Create Assignment]            │
│                                 │
│ 10:31 AM                        │
└─────────────────────────────────┘
```
- **Action Button**: Material tonal button
- **Button Text**: "Create Assignment"
- **Button Icon**: ic_add
- **Margin Top**: 8dp

### 6. Loading Indicator
```
┌─────────────────────────────────────┐
│  ⏳ AI is thinking...               │
└─────────────────────────────────────┘
```
- **Background**: Card background
- **Padding**: 12dp
- **Progress Bar**: Small circular
- **Text**: "AI is thinking..."
- **Text Style**: Italic, secondary color

### 7. Message Input
```
┌─────────────────────────────────────┐
│  [Type message here...]        [→]  │
└─────────────────────────────────────┘
```
- **Input Field**: TextInputEditText
- **Hint**: "Ask me anything..."
- **Background**: Rounded rectangle
- **Max Lines**: 4
- **Min Height**: 48dp
- **Send Button**: FAB mini, blue, white icon

## Color Scheme

### Light Mode
- **Primary**: #007AFF (Blue)
- **Background**: #FFFFFF (White)
- **Card Background**: #FFFFFF (White)
- **Text Primary**: #000000 (Black)
- **Text Secondary**: #8E8E93 (Gray)
- **User Message**: #007AFF (Blue)
- **AI Message**: #F2F2F7 (Light Gray)

### Dark Mode
- **Primary**: #0A84FF (Light Blue)
- **Background**: #1C1C1E (Dark Gray)
- **Card Background**: #2C2C2E (Medium Gray)
- **Text Primary**: #FFFFFF (White)
- **Text Secondary**: #8E8E93 (Gray)
- **User Message**: #0A84FF (Light Blue)
- **AI Message**: #3A3A3C (Dark Gray)

## Typography

### Toolbar Title
- **Size**: 20sp
- **Weight**: Medium
- **Color**: White

### Message Text
- **Size**: 16sp
- **Weight**: Regular
- **Color**: White (user) / Text Primary (AI)

### Timestamp
- **Size**: 12sp
- **Weight**: Regular
- **Color**: White 70% (user) / Text Secondary (AI)

### Empty State Title
- **Size**: 20sp
- **Weight**: Bold
- **Color**: Text Primary

### Empty State Description
- **Size**: 14sp
- **Weight**: Regular
- **Color**: Text Secondary

### Loading Text
- **Size**: 14sp
- **Weight**: Regular
- **Style**: Italic
- **Color**: Text Secondary

## Spacing

### Message Spacing
- **Vertical**: 4dp between messages
- **Horizontal**: 16dp from edges
- **Max Width**: 80% of screen (user), 85% (AI)

### Padding
- **Message Bubble**: 12dp all sides
- **Input Area**: 8dp all sides
- **Empty State**: 32dp all sides
- **Loading Indicator**: 12dp all sides

### Margins
- **User Message**: 48dp from left
- **AI Message**: 48dp from right
- **Action Button**: 8dp from top

## Icons

### AI Brain Icon
- **Resource**: ic_ai_brain
- **Size**: 120dp x 120dp
- **Tint**: Primary color
- **Opacity**: 50%

### Send Icon
- **Resource**: ic_send
- **Size**: 24dp x 24dp
- **Tint**: White
- **Container**: FAB mini (40dp)

### Back Icon
- **Resource**: ic_arrow_back
- **Size**: 24dp x 24dp
- **Tint**: White

### Action Button Icon
- **Resource**: ic_add
- **Size**: 18dp x 18dp
- **Tint**: Primary color

## Animations

### Message Appearance
- **Type**: Fade in + Slide up
- **Duration**: 200ms
- **Interpolator**: Decelerate

### Loading Indicator
- **Type**: Fade in/out
- **Duration**: 150ms
- **Interpolator**: Linear

### Scroll to Bottom
- **Type**: Smooth scroll
- **Duration**: 300ms
- **Delay**: 100ms after message added

## Interaction States

### Send Button
- **Normal**: Blue background, white icon
- **Pressed**: Darker blue (ripple effect)
- **Disabled**: Gray background, gray icon

### Input Field
- **Normal**: White background, black text
- **Focused**: Blue underline
- **Disabled**: Gray background, gray text

### Action Button
- **Normal**: Tonal button style
- **Pressed**: Ripple effect
- **Disabled**: Gray background, gray text

## Accessibility

### Content Descriptions
- **Back Button**: "Navigate back"
- **Send Button**: "Send message"
- **AI Icon**: "AI Assistant"
- **Action Button**: "Create Assignment"

### Touch Targets
- **Minimum Size**: 48dp x 48dp
- **Send Button**: 40dp FAB (acceptable for secondary action)
- **Action Button**: 48dp height minimum

### Text Contrast
- **User Message**: White on Blue (4.5:1 ratio)
- **AI Message**: Black on Light Gray (7:1 ratio)
- **Dark Mode**: White on Dark Gray (12:1 ratio)

## Responsive Design

### Portrait Mode
- **Message Width**: 80-85% of screen
- **Input Height**: Wrap content (48dp min)
- **Toolbar Height**: 56dp

### Landscape Mode
- **Message Width**: 60-70% of screen
- **Input Height**: Same as portrait
- **Toolbar Height**: 48dp

### Small Screens (<360dp width)
- **Message Width**: 90% of screen
- **Font Size**: Slightly reduced
- **Padding**: Reduced to 8dp

### Large Screens (>600dp width)
- **Message Width**: 60% of screen
- **Max Width**: 600dp
- **Centered Layout**: Messages centered

## State Indicators

### Empty State
```
Visible when: messages.isEmpty()
Hidden when: messages.isNotEmpty()
```

### Loading State
```
Visible when: isLoading == true
Hidden when: isLoading == false
```

### Error State
```
Display: Toast message
Duration: Short (2 seconds)
Position: Bottom of screen
```

### Success State
```
Display: Toast message
Duration: Short (2 seconds)
Position: Bottom of screen
```

## User Flow Diagram

```
┌─────────────┐
│ Tasks Screen│
└──────┬──────┘
       │ Tap "AI Assistant"
       ▼
┌─────────────────┐
│ AI Assistant    │
│ (Empty State)   │
└──────┬──────────┘
       │ Type message
       ▼
┌─────────────────┐
│ Message Sent    │
│ (Loading)       │
└──────┬──────────┘
       │ AI responds
       ▼
┌─────────────────┐
│ AI Response     │
│ (with Action)   │
└──────┬──────────┘
       │ Tap "Create Assignment"
       ▼
┌─────────────────┐
│ Task Created    │
│ (Success Toast) │
└──────┬──────────┘
       │ Back button
       ▼
┌─────────────────┐
│ Tasks Screen    │
│ (New task shown)│
└─────────────────┘
```

## Best Practices

### Message Display
- ✅ Show timestamps for all messages
- ✅ Group messages by sender
- ✅ Auto-scroll to latest message
- ✅ Clean JSON from AI responses
- ✅ Format long messages properly

### Loading States
- ✅ Show loading immediately
- ✅ Disable input during loading
- ✅ Clear loading on response
- ✅ Show "AI is thinking..." text

### Error Handling
- ✅ Show user-friendly errors
- ✅ Don't crash on errors
- ✅ Allow retry after error
- ✅ Log errors for debugging

### Performance
- ✅ Use DiffUtil for updates
- ✅ Recycle views properly
- ✅ Avoid blocking main thread
- ✅ Smooth scrolling

### Accessibility
- ✅ Content descriptions
- ✅ Proper touch targets
- ✅ High contrast colors
- ✅ Readable text sizes

## Summary

The AI Assistant UI provides:
- Clean, modern chat interface
- Intuitive message sending
- Clear loading states
- Helpful empty state
- Action buttons for tasks
- Consistent design language
- Excellent user experience
