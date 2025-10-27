# Task 36: Dark Mode Support - Visual Guide

## Color Palette

### Light Mode vs Dark Mode

#### Background Colors
```
Light Mode          Dark Mode
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Background  #FFFFFF  →  #1C1C1E  (Base)
Cards       #FFFFFF  →  #2C2C2E  (Elevated)
Surface     #F2F2F7  →  #3A3A3C  (More elevated)
```

#### Text Colors
```
Light Mode          Dark Mode
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Primary     #000000  →  #FFFFFF
Secondary   #8E8E93  →  #8E8E93  (Same)
```

#### Accent Colors
```
Light Mode          Dark Mode
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Blue        #007AFF  →  #0A84FF  (Brighter)
Red         #FF3B30  →  #FF453A  (Brighter)
Orange      #FF9500  →  #FF9F0A  (Brighter)
Green       #34C759  →  #32D74B  (Brighter)
```

## Screen Examples

### Task List Screen

```
┌─────────────────────────────────────┐
│  Tasks                    [+]       │  ← Dark toolbar
├─────────────────────────────────────┤
│                                     │
│  ┌───────────────────────────────┐ │
│  │ 🔴 Complete Assignment        │ │  ← Dark card
│  │ Overdue • CS • 2 days ago     │ │
│  │ [Overdue]                     │ │  ← Red chip
│  └───────────────────────────────┘ │
│                                     │
│  ┌───────────────────────────────┐ │
│  │ 🟠 Study for Exam             │ │
│  │ Due today • Math • 4 hours    │ │
│  │ [Due Today]                   │ │  ← Orange chip
│  └───────────────────────────────┘ │
│                                     │
│  ┌───────────────────────────────┐ │
│  │ 🟢 Review Notes               │ │
│  │ Completed • Physics           │ │
│  │ [Completed]                   │ │  ← Green chip
│  └───────────────────────────────┘ │
│                                     │
└─────────────────────────────────────┘
```

### Chat Room Screen
```
┌─────────────────────────────────────┐
│  ← Computer Science Team            │  ← Dark toolbar
├─────────────────────────────────────┤
│                                     │
│  ┌─────────────────────┐           │
│  │ Hey everyone!       │           │  ← Gray bubble
│  │ 10:30 AM            │           │     (received)
│  └─────────────────────┘           │
│                                     │
│           ┌─────────────────────┐  │
│           │ Hi! How are you?    │  │  ← Blue bubble
│           │ 10:31 AM         ✓✓ │  │     (sent)
│           └─────────────────────┘  │
│                                     │
├─────────────────────────────────────┤
│  [📎] Type a message...      [➤]   │  ← Dark input
└─────────────────────────────────────┘
```

### Task Details Screen
```
┌─────────────────────────────────────┐
│  ← Task Details                     │
├─────────────────────────────────────┤
│                                     │
│  ┌───────────────────────────────┐ │
│  │ ▌Complete Assignment          │ │  ← Priority bar
│  │ ▌                             │ │     (red/orange/green)
│  │ ○ Pending                     │ │
│  │                               │ │
│  │ Description                   │ │
│  │ Finish the programming...     │ │
│  │                               │ │
│  │ ─────────────────────────────│ │  ← Subtle divider
│  │                               │ │
│  │ Category: Assignment          │ │
│  │ Priority: High                │ │
│  │ Due Date: Oct 15, 2025        │ │
│  │                               │ │
│  │ [Mark as Complete]            │ │
│  └───────────────────────────────┘ │
│                                     │
└─────────────────────────────────────┘
```

## Color Usage Guide

### Status Colors
```
Overdue:    ████ #FF453A (Bright Red)
Due Today:  ████ #FF9F0A (Bright Orange)
Pending:    ████ #0A84FF (Bright Blue)
Completed:  ████ #32D74B (Bright Green)
```

### Priority Colors
```
High:       ████ #FF453A (Bright Red)
Medium:     ████ #FF9F0A (Bright Orange)
Low:        ████ #32D74B (Bright Green)
```

### UI Element Colors
```
Background:     ████ #1C1C1E (Almost Black)
Card:           ████ #2C2C2E (Dark Gray)
Elevated:       ████ #3A3A3C (Medium Gray)
Divider:        ████ #48484A (Light Gray)
Text Primary:   ████ #FFFFFF (White)
Text Secondary: ████ #8E8E93 (Gray)
```

## Design Principles

### Contrast
- Minimum 4.5:1 for normal text
- Minimum 3:1 for large text
- Minimum 3:1 for UI components

### Hierarchy
```
Level 0 (Base):      #1C1C1E  ← Screen background
Level 1 (Elevated):  #2C2C2E  ← Cards, dialogs
Level 2 (Higher):    #3A3A3C  ← Buttons, inputs
Level 3 (Highest):   #48484A  ← Borders, dividers
```

### Color Brightness
- Accent colors are 10-15% brighter in dark mode
- Maintains color semantics (red=error, green=success)
- Ensures visibility on dark backgrounds

## Component Examples

### Button States
```
Normal:   [  Button Text  ]  ← #0A84FF background
Pressed:  [  Button Text  ]  ← Slightly darker
Disabled: [  Button Text  ]  ← 50% opacity
```

### Input Fields
```
┌─────────────────────────┐
│ Label                   │  ← #8E8E93
├─────────────────────────┤
│ Input text here         │  ← #FFFFFF text
└─────────────────────────┘  ← #3A3A3C background
```

### Cards
```
┌─────────────────────────┐
│ Card Title              │  ← #FFFFFF
│ Card content goes here  │  ← #8E8E93
│ with multiple lines     │
└─────────────────────────┘  ← #2C2C2E background
```

### Chips
```
[Overdue]    ← Red text, transparent bg
[Due Today]  ← Orange text, transparent bg
[Completed]  ← Green text, transparent bg
```

## Accessibility

### WCAG Compliance
- ✅ Text contrast: 4.5:1 minimum
- ✅ Large text: 3:1 minimum
- ✅ UI components: 3:1 minimum
- ✅ Color not sole indicator

### Readability
- White text on dark backgrounds
- Sufficient line spacing
- Appropriate font sizes
- Clear visual hierarchy

## Testing Checklist

### Visual Quality
- [ ] All text is readable
- [ ] Icons are visible
- [ ] Colors are distinguishable
- [ ] Proper contrast everywhere
- [ ] No visual glitches

### Color Accuracy
- [ ] Status colors match spec
- [ ] Priority colors match spec
- [ ] Background colors match spec
- [ ] Text colors match spec

### Consistency
- [ ] Same colors used throughout
- [ ] Consistent elevation
- [ ] Uniform spacing
- [ ] Aligned elements
