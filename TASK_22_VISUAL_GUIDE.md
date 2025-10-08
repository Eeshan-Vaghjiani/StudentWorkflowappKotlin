# Task 22 Visual Guide: Task Details Navigation

## User Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     Calendar Screen                          │
│  ┌────────────────────────────────────────────────────┐     │
│  │         December 2024                              │     │
│  │  S  M  T  W  T  F  S                              │     │
│  │  1  2  3  4  5  6  7                              │     │
│  │  8  9 10 11 12 13 14                              │     │
│  │ 15 16 17 18 19 20 21                              │     │
│  │ 22 23 24 25 26 27 28                              │     │
│  │ 29 30 31                                          │     │
│  │     •     •  (dots indicate tasks)                │     │
│  └────────────────────────────────────────────────────┘     │
│                                                              │
│  Tasks for Monday, December 16, 2024                        │
│  ┌────────────────────────────────────────────────────┐     │
│  │ │ Submit Project Report                           │     │
│  │ │ Priority: High  ○ Pending                       │ <── Tap
│  └────────────────────────────────────────────────────┘     │
│  ┌────────────────────────────────────────────────────┐     │
│  │ │ Team Meeting                                    │     │
│  │ │ Priority: Medium  ○ Pending                     │     │
│  └────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ Tap Task
                            ▼
┌─────────────────────────────────────────────────────────────┐
│  ← Task Details                                             │
├─────────────────────────────────────────────────────────────┤
│  ┌────────────────────────────────────────────────────┐     │
│  │ │ Submit Project Report                           │     │
│  │ │ ! Overdue                                       │     │
│  │ │                                                 │     │
│  │ │ Description                                     │     │
│  │ │ Complete and submit the final project report   │     │
│  │ │                                                 │     │
│  │ │ Category:    Personal                           │     │
│  │ │ Priority:    High                               │     │
│  │ │ Subject:     Computer Science                   │     │
│  │ │ Due Date:    Dec 15, 2024                       │     │
│  │ │ Created:     Dec 1, 2024 at 09:00 AM           │     │
│  └────────────────────────────────────────────────────┘     │
│                                                              │
│  ┌────────────────────────────────────────────────────┐     │
│  │ Actions                                            │     │
│  │                                                    │     │
│  │ [✓ Mark as Complete]                              │ <── Tap
│  │ [✎ Edit Task]                                     │     │
│  │ [🗑 Delete Task]                                   │     │
│  └────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ Mark Complete
                            ▼
┌─────────────────────────────────────────────────────────────┐
│  Mark Task as Complete                                      │
│                                                              │
│  Are you sure you want to mark this task as complete?       │
│                                                              │
│                    [Cancel]  [Complete]                      │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ Confirm
                            ▼
┌─────────────────────────────────────────────────────────────┐
│  ← Task Details                                             │
├─────────────────────────────────────────────────────────────┤
│  ┌────────────────────────────────────────────────────┐     │
│  │ │ Submit Project Report                           │     │
│  │ │ ✓ Completed                                     │     │
│  │ │                                                 │     │
│  │ │ [Mark as Complete button is now hidden]        │     │
│  └────────────────────────────────────────────────────┘     │
│                                                              │
│  Toast: "Task marked as complete"                           │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ Press Back
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                     Calendar Screen                          │
│  (Calendar refreshes, task status updated)                  │
└─────────────────────────────────────────────────────────────┘
```

---

## Screen Layout Details

### Task Details Screen Layout

```
┌─────────────────────────────────────────────────────────────┐
│  ← Task Details                                    [Toolbar] │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌────────────────────────────────────────────────────┐     │
│  │ Task Info Card                                     │     │
│  │ ┌──────────────────────────────────────────────┐   │     │
│  │ │█│ Submit Project Report              [Title]│   │     │
│  │ │█│ ! Overdue                         [Status]│   │     │
│  │ │█│                                            │   │     │
│  │ │ │ Priority Indicator (colored bar)          │   │     │
│  │ └──────────────────────────────────────────────┘   │     │
│  │                                                     │     │
│  │ Description                                         │     │
│  │ Complete and submit the final project report       │     │
│  │                                                     │     │
│  │ ─────────────────────────────────────────────      │     │
│  │                                                     │     │
│  │ Category:     Personal                              │     │
│  │ Priority:     High                                  │     │
│  │ Subject:      Computer Science                      │     │
│  │ Due Date:     Dec 15, 2024                         │     │
│  │ Created:      Dec 1, 2024 at 09:00 AM             │     │
│  └────────────────────────────────────────────────────┘     │
│                                                              │
│  ┌────────────────────────────────────────────────────┐     │
│  │ Actions                                            │     │
│  │                                                    │     │
│  │ ┌────────────────────────────────────────────┐    │     │
│  │ │ ✓ Mark as Complete                         │    │     │
│  │ └────────────────────────────────────────────┘    │     │
│  │                                                    │     │
│  │ ┌────────────────────────────────────────────┐    │     │
│  │ │ ✎ Edit Task                                │    │     │
│  │ └────────────────────────────────────────────┘    │     │
│  │                                                    │     │
│  │ ┌────────────────────────────────────────────┐    │     │
│  │ │ 🗑 Delete Task                              │    │     │
│  │ └────────────────────────────────────────────┘    │     │
│  └────────────────────────────────────────────────────┘     │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## Color Coding Reference

### Priority Colors

**High Priority:**
```
┌────┐
│ ██ │ Red (#F44336)
└────┘
```

**Medium Priority:**
```
┌────┐
│ ██ │ Orange (#FF9800)
└────┘
```

**Low Priority:**
```
┌────┐
│ ██ │ Green (#4CAF50)
└────┘
```

### Status Colors

**Pending:**
```
○ Pending (Orange #FF9800)
```

**Completed:**
```
✓ Completed (Green #4CAF50)
```

**Overdue:**
```
! Overdue (Red #F44336)
```

---

## Button States

### Mark as Complete Button

**Visible (Pending/Overdue):**
```
┌─────────────────────────────┐
│ ✓ Mark as Complete          │ (Blue background)
└─────────────────────────────┘
```

**Hidden (Completed):**
```
[Button not displayed]
```

**Loading:**
```
┌─────────────────────────────┐
│ ⟳ Loading...                │ (Disabled)
└─────────────────────────────┘
```

### Edit Button

**Normal:**
```
┌─────────────────────────────┐
│ ✎ Edit Task                 │ (Secondary style)
└─────────────────────────────┘
```

### Delete Button

**Normal:**
```
┌─────────────────────────────┐
│ 🗑 Delete Task               │ (Red outline)
└─────────────────────────────┘
```

---

## Dialog Examples

### Mark as Complete Dialog

```
┌─────────────────────────────────────────┐
│  Mark Task as Complete                  │
│                                         │
│  Are you sure you want to mark this     │
│  task as complete?                      │
│                                         │
│                                         │
│              [Cancel]  [Complete]       │
└─────────────────────────────────────────┘
```

### Delete Confirmation Dialog

```
┌─────────────────────────────────────────┐
│  Delete Task                            │
│                                         │
│  Are you sure you want to delete this   │
│  task? This action cannot be undone.    │
│                                         │
│                                         │
│              [Cancel]  [Delete]         │
└─────────────────────────────────────────┘
```

---

## Toast Messages

### Success Messages

```
┌─────────────────────────────────────────┐
│ ✓ Task marked as complete               │
└─────────────────────────────────────────┘
```

```
┌─────────────────────────────────────────┐
│ ✓ Task deleted successfully             │
└─────────────────────────────────────────┘
```

### Info Messages

```
┌─────────────────────────────────────────┐
│ ℹ Edit functionality coming soon        │
└─────────────────────────────────────────┘
```

### Error Messages

```
┌─────────────────────────────────────────┐
│ ✗ Error: Task ID not found              │
└─────────────────────────────────────────┘
```

```
┌─────────────────────────────────────────┐
│ ✗ Failed to update task                 │
└─────────────────────────────────────────┘
```

---

## Loading States

### Initial Load

```
┌─────────────────────────────────────────┐
│  ← Task Details                         │
├─────────────────────────────────────────┤
│                                         │
│              ⟳                          │
│         Loading...                      │
│                                         │
└─────────────────────────────────────────┘
```

### Operation in Progress

```
┌─────────────────────────────────────────┐
│  Task Details                           │
│  [Content visible but dimmed]           │
│                                         │
│              ⟳                          │
│         Processing...                   │
│                                         │
│  [Buttons disabled]                     │
└─────────────────────────────────────────┘
```

---

## Empty/Missing Data States

### No Description

```
Description
No description provided (gray text)
```

### No Subject

```
[Subject row is completely hidden]
```

### No Due Date

```
Due Date:     No due date
```

---

## Interaction Patterns

### 1. View Task Details
```
Calendar → Tap Task → Task Details Screen
```

### 2. Mark as Complete
```
Task Details → Tap "Mark as Complete" → Confirm Dialog → 
Loading → Success Toast → Updated UI → Back → Refreshed Calendar
```

### 3. Delete Task
```
Task Details → Tap "Delete Task" → Confirm Dialog → 
Loading → Success Toast → Back to Calendar → Refreshed Calendar
```

### 4. Edit Task (Placeholder)
```
Task Details → Tap "Edit Task" → Toast Message → Stay on Screen
```

### 5. Navigate Back
```
Task Details → Tap Back Arrow/System Back → Calendar Screen
```

---

## Testing Scenarios Visualization

### Scenario 1: Complete a Pending Task

```
Before:                          After:
┌──────────────────┐            ┌──────────────────┐
│ Task Title       │            │ Task Title       │
│ ○ Pending        │    →       │ ✓ Completed      │
│                  │            │                  │
│ [Mark Complete]  │            │ [Button Hidden]  │
└──────────────────┘            └──────────────────┘
```

### Scenario 2: Delete a Task

```
Calendar Before:                Calendar After:
┌──────────────────┐            ┌──────────────────┐
│ • Task 1         │            │ • Task 2         │
│ • Task 2         │    →       │ • Task 3         │
│ • Task 3         │            │                  │
└──────────────────┘            └──────────────────┘
```

### Scenario 3: Priority Indicator

```
High Priority:      Medium Priority:    Low Priority:
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│█│ Task       │    │█│ Task       │    │█│ Task       │
│█│ High       │    │█│ Medium     │    │█│ Low        │
└──────────────┘    └──────────────┘    └──────────────┘
 Red                 Orange              Green
```

---

## Accessibility Considerations

### Screen Reader Announcements

```
"Task Details"
"Submit Project Report"
"Status: Overdue"
"Priority: High"
"Description: Complete and submit the final project report"
"Button: Mark as Complete"
"Button: Edit Task"
"Button: Delete Task"
```

### Focus Order

```
1. Back button
2. Task title
3. Task status
4. Description
5. Mark as Complete button
6. Edit Task button
7. Delete Task button
```

---

## Error State Examples

### Task Not Found

```
┌─────────────────────────────────────────┐
│  ← Task Details                         │
├─────────────────────────────────────────┤
│                                         │
│              ⚠                          │
│         Task not found                  │
│                                         │
│  [Activity closes automatically]        │
└─────────────────────────────────────────┘
```

### Network Error

```
┌─────────────────────────────────────────┐
│  ✗ Error loading task                   │
│                                         │
│  Please check your internet connection  │
│  and try again.                         │
└─────────────────────────────────────────┘
```

---

## Summary

This visual guide provides a comprehensive overview of the Task Details Navigation feature, including:

- User flow diagrams
- Screen layouts
- Color coding
- Button states
- Dialog examples
- Toast messages
- Loading states
- Interaction patterns
- Testing scenarios
- Accessibility considerations
- Error states

Use this guide alongside the Testing Guide and Verification Checklist for complete feature validation.

