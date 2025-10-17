# Task 6: File Attachments - Visual Guide

## Overview

This guide provides a visual walkthrough of the file attachment feature in ChatRoomActivity.

## User Flow Diagrams

### Image Attachment Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    Chat Room Screen                          │
│                                                              │
│  ┌────────────────────────────────────────────────────┐    │
│  │  [←] John Doe                                       │    │
│  └────────────────────────────────────────────────────┘    │
│                                                              │
│  ┌────────────────────────────────────────────────────┐    │
│  │  Previous messages...                               │    │
│  │                                                      │    │
│  └────────────────────────────────────────────────────┘    │
│                                                              │
│  ┌────────────────────────────────────────────────────┐    │
│  │  [📎] [Type a message...            ] [Send ➤]     │    │
│  └────────────────────────────────────────────────────┘    │
│       ↓ Click attachment button                             │
└─────────────────────────────────────────────────────────────┘

                            ↓

┌─────────────────────────────────────────────────────────────┐
│                  Attachment Bottom Sheet                     │
│                                                              │
│  Share                                                       │
│  ─────────────────────────────────────────────────────      │
│                                                              │
│  📷  Camera                                                  │
│  ─────────────────────────────────────────────────────      │
│                                                              │
│  🖼️  Gallery                                                 │
│  ─────────────────────────────────────────────────────      │
│                                                              │
│  📄  Document                                                │
│  ─────────────────────────────────────────────────────      │
│                                                              │
└─────────────────────────────────────────────────────────────┘
       ↓ Select Camera or Gallery
       
┌─────────────────────────────────────────────────────────────┐
│              Permission Request (if needed)                  │
│                                                              │
│  Camera permission is needed to take photos                 │
│                                                              │
│  [Cancel]                              [Grant]              │
└─────────────────────────────────────────────────────────────┘
       ↓ Grant permission
       
┌─────────────────────────────────────────────────────────────┐
│                  Camera / Gallery Picker                     │
│                                                              │
│  [Take photo / Select image]                                │
│                                                              │
└─────────────────────────────────────────────────────────────┘
       ↓ Select image
       
┌─────────────────────────────────────────────────────────────┐
│                   Upload Progress Dialog                     │
│                                                              │
│  Uploading Image                                            │
│                                                              │
│  45%                                                         │
│  ████████████░░░░░░░░░░░░░░                                │
│                                                              │
└─────────────────────────────────────────────────────────────┘
       ↓ Upload complete
       
┌─────────────────────────────────────────────────────────────┐
│                    Chat Room Screen                          │
│                                                              │
│  ┌────────────────────────────────────────────────────┐    │
│  │  Previous messages...                               │    │
│  └────────────────────────────────────────────────────┘    │
│                                                              │
│  ┌────────────────────────────────────────────────────┐    │
│  │                                          ┌────────┐ │    │
│  │                                          │ Image  │ │    │
│  │                                          │ 200x200│ │    │
│  │                                          │        │ │    │
│  │                                          └────────┘ │    │
│  │                                          2:30 PM ✓✓│    │
│  └────────────────────────────────────────────────────┘    │
│                                                              │
└─────────────────────────────────────────────────────────────┘
       ↓ Click image
       
┌─────────────────────────────────────────────────────────────┐
│                  Full Screen Image Viewer                    │
│                                                              │
│  [←]                                                         │
│                                                              │
│                    ┌──────────────┐                         │
│                    │              │                         │
│                    │  Full Image  │                         │
│                    │              │                         │
│                    └──────────────┘                         │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### Document Attachment Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    Chat Room Screen                          │
│                                                              │
│  [📎] Click attachment button                               │
│       ↓                                                      │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                  Attachment Bottom Sheet                     │
│                                                              │
│  📄  Document  ← Select this option                         │
│                                                              │
└─────────────────────────────────────────────────────────────┘
       ↓
       
┌─────────────────────────────────────────────────────────────┐
│                    Document Picker                           │
│                                                              │
│  Select a document:                                         │
│  • PDF files                                                │
│  • Word documents (.doc, .docx)                             │
│  • Excel spreadsheets (.xls, .xlsx)                         │
│  • PowerPoint presentations (.ppt, .pptx)                   │
│  • Text files (.txt)                                        │
│  • ZIP archives (.zip)                                      │
│                                                              │
└─────────────────────────────────────────────────────────────┘
       ↓ Select document
       
┌─────────────────────────────────────────────────────────────┐
│                   Upload Progress Dialog                     │
│                                                              │
│  Uploading Document                                         │
│                                                              │
│  67%                                                         │
│  ████████████████░░░░░░░░░                                 │
│                                                              │
└─────────────────────────────────────────────────────────────┘
       ↓ Upload complete
       
┌─────────────────────────────────────────────────────────────┐
│                    Chat Room Screen                          │
│                                                              │
│  ┌────────────────────────────────────────────────────┐    │
│  │                              ┌──────────────────┐  │    │
│  │                              │ 📄  Report.pdf   │  │    │
│  │                              │     2.5 MB       │  │    │
│  │                              └──────────────────┘  │    │
│  │                              2:30 PM ✓✓            │    │
│  └────────────────────────────────────────────────────┘    │
│                                                              │
└─────────────────────────────────────────────────────────────┘
       ↓ Click document
       
┌─────────────────────────────────────────────────────────────┐
│                  Download Progress Dialog                    │
│                                                              │
│  Downloading Document                                       │
│                                                              │
│  82%                                                         │
│  ████████████████████░░░░░                                 │
│                                                              │
└─────────────────────────────────────────────────────────────┘
       ↓ Download complete
       
┌─────────────────────────────────────────────────────────────┐
│                    Document Viewer App                       │
│                                                              │
│  Opens in appropriate app (PDF reader, Word, etc.)          │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

## UI Components

### Attachment Button
```
Location: Bottom of chat screen, left of message input

┌────────────────────────────────────────────────────────┐
│  [📎] [Type a message...                    ] [Send ➤] │
└────────────────────────────────────────────────────────┘
  ↑
  Attachment button (48dp x 48dp)
  - Icon: ic_attach
  - Color: text_secondary
  - Ripple effect on click
```

### Bottom Sheet Options
```
┌─────────────────────────────────────────────────────────┐
│  Share                                                   │
│  ──────────────────────────────────────────────────────  │
│                                                          │
│  📷  Camera                                              │
│  ──────────────────────────────────────────────────────  │
│                                                          │
│  🖼️  Gallery                                             │
│  ──────────────────────────────────────────────────────  │
│                                                          │
│  📄  Document                                            │
│  ──────────────────────────────────────────────────────  │
│                                                          │
└─────────────────────────────────────────────────────────┘

Each option:
- Icon (24dp x 24dp) + Text (16sp)
- Ripple effect on click
- Padding: 12dp
```

### Image Message Bubble (Sent)
```
                                    ┌──────────────────┐
                                    │                  │
                                    │   Image Preview  │
                                    │   (200x200dp)    │
                                    │                  │
                                    ├──────────────────┤
                                    │ Optional caption │
                                    ├──────────────────┤
                                    │ 2:30 PM      ✓✓  │
                                    └──────────────────┘
```

### Image Message Bubble (Received)
```
┌──┐  ┌──────────────────┐
│JD│  │                  │
└──┘  │   Image Preview  │
      │   (200x200dp)    │
      │                  │
      ├──────────────────┤
      │ Optional caption │
      ├──────────────────┤
      │ 2:30 PM          │
      └──────────────────┘
```

### Document Message Bubble (Sent)
```
                                    ┌──────────────────┐
                                    │ 📄  Report.pdf   │
                                    │     2.5 MB       │
                                    ├──────────────────┤
                                    │ Optional caption │
                                    ├──────────────────┤
                                    │ 2:30 PM      ✓✓  │
                                    └──────────────────┘
```

### Document Message Bubble (Received)
```
┌──┐  ┌──────────────────┐
│JD│  │ 📄  Report.pdf   │
└──┘  │     2.5 MB       │
      ├──────────────────┤
      │ Optional caption │
      ├──────────────────┤
      │ 2:30 PM          │
      └──────────────────┘
```

## Document Type Icons

```
PDF:        📄 (ic_menu_save)
Word:       📝 (ic_menu_edit)
Excel:      📊 (ic_menu_sort_by_size)
PowerPoint: 📽️ (ic_menu_slideshow)
ZIP:        📦 (ic_menu_upload)
Text:       📝 (ic_menu_edit)
Default:    📄 (ic_menu_save)
```

## Progress Indicators

### Upload Progress
```
┌─────────────────────────────────────────────────────────┐
│  Uploading Image                                         │
│                                                          │
│  45%                                                     │
│  ████████████░░░░░░░░░░░░░░                            │
│                                                          │
│  [Cannot be cancelled]                                   │
└─────────────────────────────────────────────────────────┘
```

### Download Progress
```
┌─────────────────────────────────────────────────────────┐
│  Downloading Document                                    │
│                                                          │
│  82%                                                     │
│  ████████████████████░░░░░                             │
│                                                          │
│  [Cannot be cancelled]                                   │
└─────────────────────────────────────────────────────────┘
```

## Error States

### Permission Denied
```
┌─────────────────────────────────────────────────────────┐
│  Permission Denied                                       │
│                                                          │
│  Camera permission is required to take photos.          │
│  You can grant permission in app settings.              │
│                                                          │
│  [Settings]                            [Cancel]          │
└─────────────────────────────────────────────────────────┘
```

### Upload Failed
```
┌─────────────────────────────────────────────────────────┐
│  Upload Failed                                           │
│                                                          │
│  Failed to upload image: Network error                  │
│                                                          │
│  [Retry]                               [Cancel]          │
└─────────────────────────────────────────────────────────┘
```

### File Too Large
```
┌─────────────────────────────────────────────────────────┐
│  File Too Large                                          │
│                                                          │
│  File size exceeds 10MB limit.                          │
│  Please select a smaller file.                          │
│                                                          │
│  [OK]                                                    │
└─────────────────────────────────────────────────────────┘
```

## Message Status Icons

```
Sending:   ⏱️ (clock icon)
Sent:      ✓  (single check)
Delivered: ✓✓ (double check)
Read:      ✓✓ (double check, blue)
Failed:    ⚠️ (warning icon, red)
```

## Color Scheme

### Light Mode
```
Background:         #FFFFFF
Card Background:    #F5F5F5
Primary Color:      #6200EE
Text Primary:       #000000
Text Secondary:     #757575
Sent Bubble:        #6200EE (primary)
Received Bubble:    #F5F5F5 (card background)
```

### Dark Mode
```
Background:         #121212
Card Background:    #1E1E1E
Primary Color:      #BB86FC
Text Primary:       #FFFFFF
Text Secondary:     #B3B3B3
Sent Bubble:        #BB86FC (primary dark)
Received Bubble:    #1E1E1E (card background)
```

## Animations

### Button Press
- Scale down to 0.95
- Duration: 100ms
- Bounce back to 1.0

### Bottom Sheet
- Slide up from bottom
- Duration: 250ms
- Decelerate interpolator

### Message Fade In
- Alpha 0.0 → 1.0
- Duration: 200ms
- Linear interpolator

### Image Load
- Crossfade transition
- Duration: 300ms

## Accessibility

### Content Descriptions
```
Attachment Button:  "Attach file"
Camera Option:      "Camera"
Gallery Option:     "Gallery"
Document Option:    "Document"
Image Message:      "Image message"
Document Icon:      "Document icon"
```

### Touch Targets
- All buttons: Minimum 48dp x 48dp
- Bottom sheet options: Full width, 48dp height
- Message bubbles: Full width of content

## Testing Scenarios

### Happy Path
1. ✅ Click attachment button
2. ✅ Select Camera
3. ✅ Grant permission
4. ✅ Take photo
5. ✅ See upload progress
6. ✅ See image in chat
7. ✅ Click to view full screen

### Error Path
1. ✅ Click attachment button
2. ✅ Select Gallery
3. ❌ Deny permission
4. ✅ See permission denied dialog
5. ✅ Click Settings
6. ✅ Grant permission in settings
7. ✅ Return to app
8. ✅ Try again successfully

### Offline Path
1. ✅ Disable internet
2. ✅ Click attachment button
3. ✅ Select image
4. ❌ Upload fails
5. ✅ Message queued
6. ✅ Enable internet
7. ✅ Message uploads automatically

## Performance Metrics

### Image Upload
- Compression: ~70% quality
- Max size: 10MB
- Average time: 2-5 seconds (depends on network)

### Document Upload
- No compression
- Max size: 10MB
- Average time: 3-8 seconds (depends on network)

### Image Display
- Coil caching: Memory + Disk
- Load time: <500ms (cached)
- Load time: 1-3 seconds (network)

## Conclusion

This visual guide provides a comprehensive overview of the file attachment feature UI/UX. All components are implemented and ready for testing.
