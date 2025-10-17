# Task 15: AI Task Creation - Visual Guide

## User Journey

### Step 1: Open Task Creation
```
┌─────────────────────────────────────┐
│  Tasks                    🔍 ➕     │
├─────────────────────────────────────┤
│                                     │
│  📊 Task Statistics                 │
│  ┌─────────────────────────────┐   │
│  │  8 Overdue  12 Today  45 ✓ │   │
│  └─────────────────────────────┘   │
│                                     │
│  📋 My Tasks                        │
│  ┌─────────────────────────────┐   │
│  │ □ Math Homework              │   │
│  │ □ Science Project            │   │
│  │ □ English Essay              │   │
│  └─────────────────────────────┘   │
│                                     │
│  [➕ New Task] [🤖 AI Assistant]   │
│                                     │
└─────────────────────────────────────┘
         ↓ Click ➕ Button
```

### Step 2: Task Creation Dialog
```
┌─────────────────────────────────────┐
│  Create New Task                    │
├─────────────────────────────────────┤
│                                     │
│  Task Title                         │
│  ┌─────────────────────────────┐   │
│  │ [Enter title...]            │   │
│  └─────────────────────────────┘   │
│                                     │
│  Description (Optional)             │
│  ┌─────────────────────────────┐   │
│  │ [Enter description...]      │   │
│  └─────────────────────────────┘   │
│                                     │
│  Subject                            │
│  ┌─────────────────────────────┐   │
│  │ [Enter subject...]          │   │
│  └─────────────────────────────┘   │
│                                     │
│  Category                           │
│  ⚪ Personal  ⚪ Group  ⚪ Assignment│
│                                     │
│  Priority                           │
│  ⚪ Low  ⚫ Medium  ⚪ High          │
│                                     │
│  Due Date (Optional)                │
│  ┌─────────────────────────────┐   │
│  │ [Select date...] 📅         │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ ✨ Need help? Ask AI to     │   │
│  │    create a task for you!   │   │
│  │                             │   │
│  │  [🤖 Create with AI]        │   │
│  └─────────────────────────────┘   │
│                                     │
│         [Cancel]  [Create Task]     │
└─────────────────────────────────────┘
         ↓ Click "Create with AI"
```

### Step 3: AI Prompt Dialog
```
┌─────────────────────────────────────┐
│  ✨ Create Task with AI             │
├─────────────────────────────────────┤
│                                     │
│  Describe the task you want to      │
│  create, and AI will help you       │
│  set it up.                         │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ Example: Create a math      │   │
│  │ homework assignment due     │   │
│  │ next Friday                 │   │
│  │                             │   │
│  │ [Type your description...] │   │
│  │                             │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│         [Cancel]  [🤖 Create with AI]│
└─────────────────────────────────────┘
         ↓ Enter prompt and click
```

### Step 4: Processing State
```
┌─────────────────────────────────────┐
│  ✨ Create Task with AI             │
├─────────────────────────────────────┤
│                                     │
│  Describe the task you want to      │
│  create, and AI will help you       │
│  set it up.                         │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ Create a math homework      │   │
│  │ assignment due next Friday  │   │
│  │                             │   │
│  │                             │   │
│  │                             │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│         ⏳ Processing...             │
│                                     │
│         [Cancel]  [🤖 Create with AI]│
│                   (disabled)        │
└─────────────────────────────────────┘
         ↓ AI processes request
```

### Step 5: Success State
```
┌─────────────────────────────────────┐
│  Tasks                    🔍 ➕     │
├─────────────────────────────────────┤
│                                     │
│  ✅ Task created successfully       │
│     with AI assistance!             │
│                                     │
│  📋 My Tasks                        │
│  ┌─────────────────────────────┐   │
│  │ □ Math Homework Assignment  │ ← NEW!
│  │   Mathematics • Due Friday  │   │
│  │                             │   │
│  │ □ Science Project           │   │
│  │ □ English Essay             │   │
│  └─────────────────────────────┘   │
│                                     │
└─────────────────────────────────────┘
```

## Example Prompts and Results

### Example 1: Simple Homework
```
┌─────────────────────────────────────┐
│  User Prompt:                       │
│  "Create a math homework            │
│   assignment due next Friday"       │
└─────────────────────────────────────┘
         ↓ AI Processing
┌─────────────────────────────────────┐
│  Created Task:                      │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │
│  Title: Math Homework Assignment    │
│  Description: Complete math         │
│               homework problems     │
│  Subject: Mathematics               │
│  Due Date: Oct 24, 2025 (Friday)   │
│  Priority: Medium                   │
│  Category: Assignment               │
└─────────────────────────────────────┘
```

### Example 2: Detailed Project
```
┌─────────────────────────────────────┐
│  User Prompt:                       │
│  "I need to finish my high priority │
│   science project about             │
│   photosynthesis by tomorrow"       │
└─────────────────────────────────────┘
         ↓ AI Processing
┌─────────────────────────────────────┐
│  Created Task:                      │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │
│  Title: Science Project -           │
│         Photosynthesis              │
│  Description: Complete science      │
│               project on            │
│               photosynthesis        │
│  Subject: Science                   │
│  Due Date: Oct 18, 2025 (Tomorrow) │
│  Priority: High                     │
│  Category: Assignment               │
└─────────────────────────────────────┘
```

### Example 3: Minimal Information
```
┌─────────────────────────────────────┐
│  User Prompt:                       │
│  "Study for exam"                   │
└─────────────────────────────────────┘
         ↓ AI Processing
┌─────────────────────────────────────┐
│  Created Task:                      │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │
│  Title: Study for Exam              │
│  Description: Prepare for upcoming  │
│               examination           │
│  Subject: General                   │
│  Due Date: Oct 24, 2025 (7 days)   │
│  Priority: Medium                   │
│  Category: Assignment               │
└─────────────────────────────────────┘
```

## Error States

### Error 1: Missing API Key
```
┌─────────────────────────────────────┐
│  ⚠️ AI Assistant is not configured  │
│                                     │
│  Please add GEMINI_API_KEY to      │
│  local.properties                   │
│                                     │
│  [Dismiss]                          │
└─────────────────────────────────────┘
```

### Error 2: Empty Prompt
```
┌─────────────────────────────────────┐
│  ⚠️ Please describe the task you    │
│     want to create                  │
│                                     │
│  [Dismiss]                          │
└─────────────────────────────────────┘
```

### Error 3: Network Error
```
┌─────────────────────────────────────┐
│  ❌ Failed to communicate with AI   │
│                                     │
│  Please check your internet         │
│  connection and try again           │
│                                     │
│  [Retry]  [Dismiss]                 │
└─────────────────────────────────────┘
```

## UI Components

### "Create with AI" Button
```
┌─────────────────────────────────────┐
│  ┌─────────────────────────────┐   │
│  │ ✨ Need help? Ask AI to     │   │
│  │    create a task for you!   │   │
│  │                             │   │
│  │  ┌───────────────────────┐  │   │
│  │  │ 🤖 Create with AI     │  │   │
│  │  └───────────────────────┘  │   │
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘

Style: Outlined button with AI icon
Color: Primary color border
Icon: ✨ (sparkles) or 🤖 (robot)
```

### AI Prompt Input
```
┌─────────────────────────────────────┐
│  ┌─────────────────────────────┐   │
│  │ Example: Create a math      │   │
│  │ homework assignment due     │   │
│  │ next Friday                 │   │
│  │                             │   │
│  │ [Type your description...] │   │
│  │                             │   │
│  │                             │   │
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘

Type: Multi-line text input
Min Lines: 3
Max Lines: 5
Hint: Example prompt
```

### Loading Indicator
```
┌─────────────────────────────────────┐
│                                     │
│            ⏳ Processing...          │
│                                     │
│         ●●●●●●●●○○○○○○○○            │
│                                     │
└─────────────────────────────────────┘

Type: Circular progress bar
Color: Primary color
Position: Center of dialog
```

## Color Scheme

### Light Mode
```
┌─────────────────────────────────────┐
│  Background: #FFFFFF (White)        │
│  Card: #F5F5F5 (Light Gray)        │
│  Primary: #6200EE (Purple)          │
│  Text: #000000 (Black)              │
│  Secondary Text: #757575 (Gray)     │
│  Border: #E0E0E0 (Light Gray)      │
└─────────────────────────────────────┘
```

### Dark Mode
```
┌─────────────────────────────────────┐
│  Background: #121212 (Dark Gray)    │
│  Card: #1E1E1E (Darker Gray)       │
│  Primary: #BB86FC (Light Purple)    │
│  Text: #FFFFFF (White)              │
│  Secondary Text: #B3B3B3 (Gray)     │
│  Border: #2C2C2C (Dark Gray)       │
└─────────────────────────────────────┘
```

## Animation Flow

### Button Press Animation
```
Normal State → Pressed State → Released State
    ↓              ↓                ↓
  100%           95%              100%
  scale          scale            scale
```

### Dialog Appearance
```
Hidden → Fade In → Slide Up → Visible
  ↓         ↓         ↓          ↓
 0%       50%       100%       100%
opacity  opacity   opacity    opacity
```

### Loading Animation
```
Idle → Spinning → Processing → Complete
 ↓        ↓           ↓           ↓
 ○       ●●○         ●●●●○       ✓
```

## Accessibility

### Screen Reader Announcements
```
1. Button Focus:
   "Create with AI button. Double tap to activate."

2. Dialog Open:
   "Create Task with AI dialog. Enter task description."

3. Processing:
   "Processing your request. Please wait."

4. Success:
   "Task created successfully with AI assistance."

5. Error:
   "Error: [error message]. Double tap to retry."
```

### Touch Targets
```
┌─────────────────────────────────────┐
│  Minimum Size: 48dp × 48dp          │
│                                     │
│  ┌────────────────────────────┐    │
│  │                            │    │
│  │    [Button Text]           │    │
│  │                            │    │
│  └────────────────────────────┘    │
│         48dp × 48dp                 │
└─────────────────────────────────────┘
```

## Responsive Design

### Phone (Portrait)
```
┌─────────────────┐
│  Dialog         │
│  ┌───────────┐  │
│  │  Content  │  │
│  │           │  │
│  │           │  │
│  └───────────┘  │
│  [Cancel] [OK]  │
└─────────────────┘
   Width: 90%
```

### Phone (Landscape)
```
┌─────────────────────────────────┐
│  Dialog                         │
│  ┌─────────────────────────┐   │
│  │  Content                │   │
│  └─────────────────────────┘   │
│  [Cancel]            [OK]       │
└─────────────────────────────────┘
   Width: 70%
```

### Tablet
```
┌─────────────────────────────────────┐
│                                     │
│     ┌─────────────────────────┐    │
│     │  Dialog                 │    │
│     │  ┌───────────────────┐  │    │
│     │  │  Content          │  │    │
│     │  │                   │  │    │
│     │  └───────────────────┘  │    │
│     │  [Cancel]      [OK]     │    │
│     └─────────────────────────┘    │
│                                     │
└─────────────────────────────────────┘
   Width: 50% (max 600dp)
```

## Integration Points

### From Tasks Screen
```
Tasks Screen
    ↓
[+ Button]
    ↓
Task Creation Dialog
    ↓
[Create with AI Button]
    ↓
AI Prompt Dialog
    ↓
AI Processing
    ↓
Task Created
    ↓
Back to Tasks Screen
```

### From Quick Actions
```
Tasks Screen
    ↓
[AI Assistant Button]
    ↓
AI Assistant Activity
    ↓
Create Task Action
    ↓
Task Created
    ↓
Back to Tasks Screen
```

## Summary

This visual guide provides a comprehensive overview of the AI task creation feature's user interface and user experience. The feature is designed to be:

- **Intuitive**: Clear visual hierarchy and flow
- **Accessible**: Proper touch targets and screen reader support
- **Responsive**: Works on all device sizes
- **Consistent**: Follows Material Design 3 guidelines
- **User-Friendly**: Clear feedback and error handling

The implementation ensures a smooth, professional experience that enhances the overall app usability.
