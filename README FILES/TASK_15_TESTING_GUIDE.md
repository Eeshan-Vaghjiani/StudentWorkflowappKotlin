# Task 15: Image Message Sending - Testing Guide

## Prerequisites

Before testing, ensure:
- ✅ App is built successfully
- ✅ Firebase project is configured
- ✅ Firebase Storage is enabled
- ✅ Storage security rules allow authenticated uploads
- ✅ At least two test accounts exist
- ✅ Test devices/emulators have images in gallery

## Test Environment Setup

### 1. Firebase Storage Rules
Verify these rules are deployed:
```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /chat_images/{chatId}/{fileName} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.resource.size < 5 * 1024 * 1024;
    }
  }
}
```

### 2. Test Images
Prepare test images with different characteristics:
- Small image (< 100KB)
- Medium image (500KB - 1MB)
- Large image (2-5MB)
- Very large image (> 5MB) - should fail
- Portrait orientation
- Landscape orientation
- Square image
- Image with EXIF rotation data

## Test Cases

### TC1: Basic Image Sending (Happy Path)

**Steps:**
1. Open the app and log in
2. Navigate to Chat tab
3. Open an existing chat or create a new direct chat
4. Tap the attachment button (📎)
5. Select "Gallery" option
6. Choose a medium-sized image (500KB)
7. Observe the progress dialog

**Expected Results:**
- ✅ Attachment bottom sheet appears
- ✅ Gallery option is visible and clickable
- ✅ Image picker opens
- ✅ Progress dialog shows "Uploading Image"
- ✅ Progress updates from 0% to 100%
- ✅ Progress dialog dismisses automatically
- ✅ Image appears in chat as a message
- ✅ Image displays as 200x200dp thumbnail
- ✅ Timestamp appears below image
- ✅ Read receipt icon appears (clock → checkmark)
- ✅ Chat scrolls to show new message

**Actual Result:** _____________________

**Status:** ☐ Pass ☐ Fail

---

### TC2: Image Compression

**Steps:**
1. Open a chat
2. Send a large image (2-5MB)
3. Check Firebase Storage console
4. Verify uploaded file size

**Expected Results:**
- ✅ Image uploads successfully
- ✅ Uploaded file size is significantly smaller than original
- ✅ Image quality is acceptable
- ✅ Image dimensions are max 1920x1080
- ✅ File format is JPEG

**Actual Result:** _____________________

**Status:** ☐ Pass ☐ Fail

---

### TC3: Progress Indicator

**Steps:**
1. Open a chat
2. Send a large image (2-5MB)
3. Observe progress dialog closely

**Expected Results:**
- ✅ Progress dialog appears immediately
- ✅ Progress starts at 0%
- ✅ Progress updates smoothly (not jumping)
- ✅ Progress reaches 100%
- ✅ Dialog dismisses after 100%
- ✅ User cannot dismiss dialog during upload

**Actual Result:** _____________________

**Status:** ☐ Pass ☐ Fail

---

### TC4: Image Display in Chat

**Steps:**
1. Send an image message
2. Observe how it displays in chat
3. Scroll up and down
4. Close and reopen chat

**Expected Results:**
- ✅ Image displays as thumbnail (200x200dp)
- ✅ Image maintains aspect ratio
- ✅ Image is clear and not pixelated
- ✅ Timestamp shows below image
- ✅ Read receipt icon appears
- ✅ Image loads from cache on scroll
- ✅ Image persists after reopening chat

**Actual Result:** _____________________

**Status:** ☐ Pass ☐ Fail

---

### TC5: Receiving Image Messages

**Steps:**
1. Log in with User A on Device 1
2. Log in with User B on Device 2
3. User A sends image to User B
4. Observe on Device 2

**Expected Results:**
- ✅ User B receives message in real-time
- ✅ Image displays correctly
- ✅ Sender's name appears (if group chat)
- ✅ Sender's avatar appears
- ✅ Timestamp is correct
- ✅ Notification shows "📷 Photo"
- ✅ Chat list shows "📷 Photo" as last message

**Actual Result:** _____________________

**Status:** ☐ Pass ☐ Fail

---

### TC6: Image with Text

**Steps:**
1. Open a chat
2. Send an image
3. Verify if text can be added (Note: Current implementation doesn't support this in UI, but model supports it)

**Expected Results:**
- ✅ Image displays
- ✅ Text displays below image (if supported)
- ✅ Both are visible in message bubble

**Actual Result:** _____________________

**Status:** ☐ Pass ☐ Fail

---

### TC7: Multiple Images in Sequence

**Steps:**
1. Open a chat
2. Send 3 images quickly one after another
3. Observe upload and display

**Expected Results:**
- ✅ All 3 images upload successfully
- ✅ Progress dialogs appear for each
- ✅ Images appear in correct order
- ✅ No images are lost
- ✅ No duplicate images
- ✅ Timestamps are sequential

**Actual Result:** _____________________

**Status:** ☐ Pass ☐ Fail

---

### TC8: Image Upload Failure (Network Error)

**Steps:**
1. Open a chat
2. Enable airplane mode
3. Try to send an image
4. Observe behavior

**Expected Results:**
- ✅ Upload fails gracefully
- ✅ Error message appears
- ✅ Message is queued in offline queue
- ✅ Message shows FAILED status
- ✅ User can retry later

**Actual Result:** _____________________

**Status:** ☐ Pass ☐ Fail

---

### TC9: Image Upload Retry

**Steps:**
1. Send image while offline (fails)
2. Turn on internet connection
3. Wait for auto-retry or manually retry

**Expected Results:**
- ✅ Message auto-retries when online
- ✅ Upload succeeds
- ✅ Message status updates to SENT
- ✅ Image displays correctly
- ✅ Other user receives message

**Actual Result:** _____________________

**Status:** ☐ Pass ☐ Fail

---

### TC10: Image Too Large (> 5MB)

**Steps:**
1. Open a chat
2. Try to send an image larger than 5MB
3. Observe behavior

**Expected Results:**
- ✅ Upload fails
- ✅ Error message: "Image size exceeds maximum allowed size"
- ✅ Message is not sent
- ✅ No partial upload in Storage

**Actual Result:** _____________________

**Status:** ☐ Pass ☐ Fail

---

### TC11: Image Orientation (EXIF)

**Steps:**
1. Take a photo with device camera (portrait mode)
2. Send the photo in chat
3. Verify orientation

**Expected Results:**
- ✅ Image displays in correct orientation
- ✅ No rotation issues
- ✅ EXIF data is handled correctly
- ✅ Image is not sideways or upside down

**Actual Result:** _____________________

**Status:** ☐ Pass ☐ Fail

---

### TC12: Different Aspect Ratios

**Steps:**
1. Send a portrait image (9:16)
2. Send a landscape image (16:9)
3. Send a square image (1:1)
4. Observe display

**Expected Results:**
- ✅ All images display correctly
- ✅ Aspect ratios are maintained
- ✅ No stretching or distortion
- ✅ Images fit within 200x200dp bounds

**Actual Result:** _____________________

**Status:** ☐ Pass ☐ Fail

---

### TC13: Image Loading States

**Steps:**
1. Send an image
2. Observe loading states
3. Clear app cache and reopen chat

**Expected Results:**
- ✅ Placeholder shows while loading
- ✅ Smooth transition to actual image
- ✅ Error icon if load fails
- ✅ Images load from cache on reopen

**Actual Result:** _____________________

**Status:** ☐ Pass ☐ Fail

---

### TC14: Chat List Update

**Steps:**
1. Open a chat
2. Send an image
3. Go back to chat list
4. Observe chat item

**Expected Results:**
- ✅ Last message shows "📷 Photo"
- ✅ Timestamp updates
- ✅ Chat moves to top of list
- ✅ Unread count updates for recipient

**Actual Result:** _____________________

**Status:** ☐ Pass ☐ Fail

---

### TC15: Notification for Image Message

**Steps:**
1. User A sends image to User B
2. User B's app is in background
3. Observe notification

**Expected Results:**
- ✅ Notification appears
- ✅ Title shows sender name
- ✅ Body shows "📷 Photo"
- ✅ Tapping notification opens chat
- ✅ Image is visible in chat

**Actual Result:** _____________________

**Status:** ☐ Pass ☐ Fail

---

### TC16: Group Chat Image Sending

**Steps:**
1. Open a group chat
2. Send an image
3. Verify all members receive it

**Expected Results:**
- ✅ Image uploads successfully
- ✅ All group members receive image
- ✅ Sender's name and avatar show
- ✅ Notifications sent to all members
- ✅ Chat list updates for all members

**Actual Result:** _____________________

**Status:** ☐ Pass ☐ Fail

---

### TC17: Image Click (Placeholder)

**Steps:**
1. Send an image
2. Tap on the image

**Expected Results:**
- ✅ Nothing happens (feature not implemented yet)
- ✅ No crash
- ✅ TODO comment in code for task 17

**Actual Result:** _____________________

**Status:** ☐ Pass ☐ Fail

---

### TC18: Memory Management

**Steps:**
1. Send 20 images in a chat
2. Scroll up and down rapidly
3. Monitor app memory usage

**Expected Results:**
- ✅ No memory leaks
- ✅ Images load smoothly
- ✅ No OutOfMemoryError
- ✅ Coil caching works properly
- ✅ App remains responsive

**Actual Result:** _____________________

**Status:** ☐ Pass ☐ Fail

---

### TC19: Firebase Storage Verification

**Steps:**
1. Send an image
2. Open Firebase Console
3. Navigate to Storage
4. Check uploaded file

**Expected Results:**
- ✅ File exists at `chat_images/{chatId}/{timestamp}_{userId}.jpg`
- ✅ File is JPEG format
- ✅ File size is reasonable (compressed)
- ✅ Metadata includes uploader and timestamp
- ✅ Download URL is accessible

**Actual Result:** _____________________

**Status:** ☐ Pass ☐ Fail

---

### TC20: Firestore Message Document

**Steps:**
1. Send an image
2. Open Firebase Console
3. Navigate to Firestore
4. Check message document

**Expected Results:**
- ✅ Message document exists
- ✅ `imageUrl` field contains Storage URL
- ✅ `text` field is empty or contains caption
- ✅ `senderId`, `senderName` are correct
- ✅ `timestamp` is accurate
- ✅ `status` is SENT
- ✅ `readBy` array contains sender

**Actual Result:** _____________________

**Status:** ☐ Pass ☐ Fail

---

## Performance Tests

### PT1: Upload Speed
- Small image (< 100KB): _____ seconds
- Medium image (500KB): _____ seconds
- Large image (2MB): _____ seconds

**Expected:** < 5 seconds for medium images on good connection

---

### PT2: Image Loading Speed
- First load: _____ ms
- From cache: _____ ms

**Expected:** < 500ms first load, < 100ms from cache

---

### PT3: Compression Time
- 5MB image compression: _____ seconds

**Expected:** < 3 seconds

---

## Security Tests

### ST1: Unauthenticated Upload
**Steps:**
1. Log out
2. Try to send image (should not be possible)

**Expected:** Upload fails, user must be authenticated

---

### ST2: Storage Rules
**Steps:**
1. Try to upload file > 5MB
2. Try to access another user's image URL

**Expected:** 
- Large file rejected
- Other user's images accessible (read allowed)

---

## Regression Tests

### RT1: Text Messages Still Work
**Steps:**
1. Send a text message
2. Verify it works as before

**Expected:** No impact on text messaging

---

### RT2: Typing Indicator Still Works
**Steps:**
1. Type in message input
2. Verify typing indicator shows

**Expected:** Typing indicator unaffected

---

### RT3: Read Receipts Still Work
**Steps:**
1. Send text and image messages
2. Verify read receipts update

**Expected:** Read receipts work for both types

---

## Test Summary

| Category | Total | Passed | Failed | Skipped |
|----------|-------|--------|--------|---------|
| Functional | 17 | ___ | ___ | ___ |
| Performance | 3 | ___ | ___ | ___ |
| Security | 2 | ___ | ___ | ___ |
| Regression | 3 | ___ | ___ | ___ |
| **Total** | **25** | **___** | **___** | **___** |

## Issues Found

| ID | Severity | Description | Status |
|----|----------|-------------|--------|
| 1 | | | |
| 2 | | | |
| 3 | | | |

## Test Environment

- **Date:** _______________
- **Tester:** _______________
- **Device 1:** _______________
- **Device 2:** _______________
- **Android Version:** _______________
- **App Version:** 1.0
- **Network:** WiFi / 4G / 5G

## Notes

_Add any additional observations or comments here_

---

## Sign-off

- [ ] All critical tests passed
- [ ] All high-priority issues resolved
- [ ] Feature ready for production

**Tester Signature:** _______________
**Date:** _______________
