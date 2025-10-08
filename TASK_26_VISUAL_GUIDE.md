# Task 26 Visual Guide: Long-Press Context Menu for Messages

## Feature Overview

This guide provides visual descriptions of the long-press context menu feature for messages in the chat system.

## UI Components

### 1. Context Menu Appearance

```
┌─────────────────────────────────────┐
│  Chat Room                      ⋮   │
├─────────────────────────────────────┤
│                                     │
│  ┌──────────────────────┐          │
│  │ Hello! How are you?  │          │
│  │                      │          │
│  │ 10:30 AM          ✓✓ │          │
│  └──────────────────────┘          │
│                                     │
│         ┌──────────────┐            │
│         │ I'm good!    │            │
│         │              │            │
│         │ 10:31 AM  ✓✓ │            │
│         └──────────────┘            │
│                 ↓                   │
│         ┌──────────────┐            │
│         │ Copy         │ ← Context  │
│         │ Delete       │   Menu     │
│         │ Forward      │            │
│         └──────────────┘            │
│                                     │
└─────────────────────────────────────┘
```

**Description:**
- User long-presses on a message
- Context menu appears anchored near the message
- Menu shows available options based on message ownership

---

### 2. Context Menu for Own Messages

```
┌─────────────────────────────────────┐
│  Long-press on YOUR message         │
├─────────────────────────────────────┤
│                                     │
│         ┌──────────────┐            │
│         │ My message   │            │
│         │              │            │
│         │ 10:35 AM  ✓✓ │            │
│         └──────────────┘            │
│                 ↓                   │
│         ┌──────────────┐            │
│         │ Copy         │ ✓ Available│
│         │ Delete       │ ✓ Available│
│         │ Forward      │ ✓ Available│
│         └──────────────┘            │
│                                     │
└─────────────────────────────────────┘
```

**Options Available:**
- ✅ Copy - Copy message text to clipboard
- ✅ Delete - Delete your own message
- ✅ Forward - Forward message (placeholder)

---

### 3. Context Menu for Other Users' Messages

```
┌─────────────────────────────────────┐
│  Long-press on OTHER user's message │
├─────────────────────────────────────┤
│                                     │
│  ┌──────────────────────┐          │
│  │ Their message        │          │
│  │                      │          │
│  │ 10:32 AM          ✓✓ │          │
│  └──────────────────────┘          │
│         ↓                           │
│  ┌──────────────┐                  │
│  │ Copy         │ ✓ Available      │
│  │ Forward      │ ✓ Available      │
│  └──────────────┘                  │
│                                     │
│  ❌ Delete option NOT shown         │
│                                     │
└─────────────────────────────────────┘
```

**Options Available:**
- ✅ Copy - Copy message text to clipboard
- ❌ Delete - NOT available (not your message)
- ✅ Forward - Forward message (placeholder)

---

### 4. Copy Action Flow

```
Step 1: Long-press message
┌─────────────────────────────────────┐
│         ┌──────────────┐            │
│         │ Hello there! │            │
│         └──────────────┘            │
│                 ↓                   │
│         ┌──────────────┐            │
│         │ Copy         │ ← Tap this │
│         │ Delete       │            │
│         │ Forward      │            │
│         └──────────────┘            │
└─────────────────────────────────────┘

Step 2: Toast confirmation
┌─────────────────────────────────────┐
│                                     │
│         ┌──────────────┐            │
│         │ Hello there! │            │
│         └──────────────┘            │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ Copied to clipboard         │   │
│  └─────────────────────────────┘   │
│                                     │
└─────────────────────────────────────┘

Step 3: Paste anywhere
┌─────────────────────────────────────┐
│  Message input                      │
├─────────────────────────────────────┤
│  Hello there!                       │
│  ▌                                  │
└─────────────────────────────────────┘
```

---

### 5. Delete Action Flow

```
Step 1: Long-press your message
┌─────────────────────────────────────┐
│         ┌──────────────┐            │
│         │ My message   │            │
│         └──────────────┘            │
│                 ↓                   │
│         ┌──────────────┐            │
│         │ Copy         │            │
│         │ Delete       │ ← Tap this │
│         │ Forward      │            │
│         └──────────────┘            │
└─────────────────────────────────────┘

Step 2: Confirmation dialog
┌─────────────────────────────────────┐
│  ┌─────────────────────────────┐   │
│  │ Delete Message              │   │
│  ├─────────────────────────────┤   │
│  │                             │   │
│  │ Are you sure you want to    │   │
│  │ delete this message? This   │   │
│  │ action cannot be undone.    │   │
│  │                             │   │
│  ├─────────────────────────────┤   │
│  │  [Cancel]      [Delete]     │   │
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘

Step 3: Message deleted
┌─────────────────────────────────────┐
│                                     │
│  ┌──────────────────────┐          │
│  │ Previous message     │          │
│  └──────────────────────┘          │
│                                     │
│  [Message removed]                  │
│                                     │
│         ┌──────────────┐            │
│         │ Next message │            │
│         └──────────────┘            │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ Message deleted             │   │
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

---

### 6. Forward Action (Placeholder)

```
Step 1: Long-press message
┌─────────────────────────────────────┐
│         ┌──────────────┐            │
│         │ Some message │            │
│         └──────────────┘            │
│                 ↓                   │
│         ┌──────────────┐            │
│         │ Copy         │            │
│         │ Delete       │            │
│         │ Forward      │ ← Tap this │
│         └──────────────┘            │
└─────────────────────────────────────┘

Step 2: Coming soon toast
┌─────────────────────────────────────┐
│                                     │
│         ┌──────────────┐            │
│         │ Some message │            │
│         └──────────────┘            │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ Forward feature coming soon │   │
│  └─────────────────────────────┘   │
│                                     │
└─────────────────────────────────────┘
```

---

## Message Types

### Text Message Context Menu

```
┌─────────────────────────────────────┐
│         ┌──────────────────────┐    │
│         │ This is a text       │    │
│         │ message with some    │    │
│         │ content              │    │
│         │                      │    │
│         │ 10:45 AM          ✓✓ │    │
│         └──────────────────────┘    │
│                 ↓                   │
│         ┌──────────────┐            │
│         │ Copy         │ → Copies   │
│         │ Delete       │   text     │
│         │ Forward      │            │
│         └──────────────┘            │
└─────────────────────────────────────┘
```

---

### Image Message Context Menu

```
┌─────────────────────────────────────┐
│         ┌──────────────────────┐    │
│         │ ┌──────────────────┐ │    │
│         │ │                  │ │    │
│         │ │   [Image]        │ │    │
│         │ │                  │ │    │
│         │ └──────────────────┘ │    │
│         │ 10:46 AM          ✓✓ │    │
│         └──────────────────────┘    │
│                 ↓                   │
│         ┌──────────────┐            │
│         │ Copy         │ → Copies   │
│         │ Delete       │   image URL│
│         │ Forward      │            │
│         └──────────────┘            │
└─────────────────────────────────────┘
```

---

### Document Message Context Menu

```
┌─────────────────────────────────────┐
│         ┌──────────────────────┐    │
│         │ 📄 document.pdf      │    │
│         │    1.2 MB            │    │
│         │                      │    │
│         │ 10:47 AM          ✓✓ │    │
│         └──────────────────────┘    │
│                 ↓                   │
│         ┌──────────────┐            │
│         │ Copy         │ → Copies   │
│         │ Delete       │   filename │
│         │ Forward      │            │
│         └──────────────┘            │
└─────────────────────────────────────┘
```

---

## User Feedback

### Success Messages

```
┌─────────────────────────────────────┐
│  Copy Success:                      │
│  ┌─────────────────────────────┐   │
│  │ Copied to clipboard         │   │
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│  Delete Success:                    │
│  ┌─────────────────────────────┐   │
│  │ Message deleted             │   │
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│  Forward Placeholder:               │
│  ┌─────────────────────────────┐   │
│  │ Forward feature coming soon │   │
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

---

### Error Messages

```
┌─────────────────────────────────────┐
│  Delete Failed:                     │
│  ┌─────────────────────────────┐   │
│  │ Failed to delete message:   │   │
│  │ Network error               │   │
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│  Unauthorized Delete:               │
│  ┌─────────────────────────────┐   │
│  │ You can only delete your    │   │
│  │ own messages                │   │
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

---

## Interaction Patterns

### Long-Press Gesture

```
1. Touch and hold message
   ┌──────────────┐
   │ Message      │ ← Finger down
   └──────────────┘
   
2. Hold for ~500ms
   ┌──────────────┐
   │ Message      │ ← Still holding
   └──────────────┘
   
3. Context menu appears
   ┌──────────────┐
   │ Message      │
   └──────────────┘
          ↓
   ┌──────────────┐
   │ Copy         │ ← Menu appears
   │ Delete       │
   │ Forward      │
   └──────────────┘
```

---

### Menu Dismissal

```
Option 1: Tap outside menu
┌─────────────────────────────────────┐
│         ┌──────────────┐            │
│         │ Message      │            │
│         └──────────────┘            │
│                 ↓                   │
│         ┌──────────────┐            │
│         │ Copy         │            │
│         │ Delete       │            │
│         │ Forward      │            │
│         └──────────────┘            │
│                                     │
│  Tap here → ✕                       │
│                                     │
└─────────────────────────────────────┘
Menu closes, no action taken

Option 2: Press back button
┌─────────────────────────────────────┐
│  ← Back button                      │
├─────────────────────────────────────┤
│         ┌──────────────┐            │
│         │ Copy         │            │
│         │ Delete       │            │
│         │ Forward      │            │
│         └──────────────┘            │
└─────────────────────────────────────┘
Menu closes, no action taken
```

---

## Chat List Update After Delete

```
Before Delete:
┌─────────────────────────────────────┐
│  Chats                              │
├─────────────────────────────────────┤
│  👤 John Doe                        │
│     This is the last message        │
│     10:45 AM                        │
├─────────────────────────────────────┤
│  👤 Jane Smith                      │
│     Previous message                │
│     10:30 AM                        │
└─────────────────────────────────────┘

After Deleting Last Message:
┌─────────────────────────────────────┐
│  Chats                              │
├─────────────────────────────────────┤
│  👤 John Doe                        │
│     Previous message now shown      │
│     10:40 AM ← Updated              │
├─────────────────────────────────────┤
│  👤 Jane Smith                      │
│     Previous message                │
│     10:30 AM                        │
└─────────────────────────────────────┘
```

---

## Accessibility

### Screen Reader Announcements

```
Long-press detected:
"Context menu opened for message"

Copy selected:
"Copied to clipboard"

Delete selected:
"Delete message dialog opened"

Delete confirmed:
"Message deleted"

Delete cancelled:
"Delete cancelled"
```

---

## Animation Flow

```
1. Long-press detected
   Message → [Slight scale up]

2. Menu appears
   Menu → [Fade in + Slide up]

3. Option selected
   Menu → [Fade out]
   Action → [Execute]

4. Feedback shown
   Toast → [Slide up from bottom]
   Toast → [Fade out after 2s]
```

---

## State Indicators

### Message States

```
Sending:
┌──────────────┐
│ Message      │
│ 🕐 Sending   │
└──────────────┘

Sent:
┌──────────────┐
│ Message      │
│ ✓ Sent       │
└──────────────┘

Delivered:
┌──────────────┐
│ Message      │
│ ✓✓ Delivered │
└──────────────┘

Read:
┌──────────────┐
│ Message      │
│ ✓✓ Read      │
└──────────────┘

Failed:
┌──────────────┐
│ Message      │
│ ⚠ Failed     │
└──────────────┘
```

All states support long-press context menu!

---

## Tips for Testing

1. **Long-press duration:** Hold for at least 500ms
2. **Menu positioning:** Menu appears near the message
3. **Scroll position:** Menu works while scrolling
4. **Multiple messages:** Can long-press any message
5. **Different types:** Works for text, images, documents
6. **Network states:** Works online and offline (queues delete)

---

## Common Issues & Solutions

### Issue: Menu doesn't appear
**Solution:** Hold longer (500ms+), ensure touch is on message area

### Issue: Wrong message selected
**Solution:** Ensure finger is on the specific message bubble

### Issue: Delete option missing
**Solution:** Check if message is from current user (only own messages can be deleted)

### Issue: Delete fails
**Solution:** Check network connection, verify user authentication

---

## Summary

The long-press context menu provides an intuitive way to interact with messages:
- **Copy:** Quick text copying to clipboard
- **Delete:** Remove your own messages with confirmation
- **Forward:** Placeholder for future feature

The feature is designed to be:
- ✅ Intuitive (standard long-press gesture)
- ✅ Safe (confirmation for destructive actions)
- ✅ Secure (authorization checks)
- ✅ Responsive (immediate UI feedback)
- ✅ Accessible (screen reader support)
