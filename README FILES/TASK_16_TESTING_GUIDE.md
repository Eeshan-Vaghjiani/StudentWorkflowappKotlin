# Task 16: Document Message Sending - Testing Guide

## Prerequisites

Before testing, ensure:
- ✅ App is built and installed on device/emulator
- ✅ User is logged in
- ✅ At least one chat exists (group or direct)
- ✅ Device has some document files available
- ✅ Internet connection is available
- ✅ Firebase Storage is properly configured

## Test Scenarios

### 1. Basic Document Sending

**Steps:**
1. Open the app and navigate to Chat tab
2. Open any existing chat
3. Tap the attachment button (📎)
4. Select "Documents" option
5. Choose a PDF file from device storage
6. Observe the progress dialog

**Expected Results:**
- ✅ Attachment bottom sheet appears with three options
- ✅ Document picker opens with file browser
- ✅ Progress dialog shows "Uploading Document"
- ✅ Progress updates from 0% to 100%
- ✅ Dialog dismisses automatically
- ✅ Document appears in chat with PDF icon
- ✅ File name is displayed correctly
- ✅ File size is shown (e.g., "2.5 MB")
- ✅ Success toast appears
- ✅ Chat list shows "📄 filename.pdf" as last message

### 2. Different File Types

**Test each file type:**

#### PDF Files
- Select a PDF document
- Verify icon shows save/document icon
- Verify file displays correctly

#### Word Documents (.doc, .docx)
- Select a Word document
- Verify icon shows edit icon
- Verify file displays correctly

#### Excel Spreadsheets (.xls, .xlsx)
- Select an Excel file
- Verify icon shows sort/table icon
- Verify file displays correctly

#### PowerPoint Presentations (.ppt, .pptx)
- Select a PowerPoint file
- Verify icon shows slideshow icon
- Verify file displays correctly

#### Text Files (.txt)
- Select a text file
- Verify icon shows edit icon
- Verify file displays correctly

#### ZIP Archives (.zip)
- Select a ZIP file
- Verify icon shows upload/archive icon
- Verify file displays correctly

**Expected Results:**
- ✅ Each file type displays with appropriate icon
- ✅ All files upload successfully
- ✅ File names and sizes are correct
- ✅ Messages appear in correct order

### 3. Large File Upload

**Steps:**
1. Open a chat
2. Tap attachment button
3. Select a document close to 10MB
4. Observe upload progress

**Expected Results:**
- ✅ Progress dialog shows and updates smoothly
- ✅ Upload completes successfully
- ✅ File appears in chat
- ✅ File size shows correctly (e.g., "9.8 MB")

### 4. File Size Limit Exceeded

**Steps:**
1. Open a chat
2. Tap attachment button
3. Try to select a document larger than 10MB
4. Observe the result

**Expected Results:**
- ✅ Error message appears: "Document size exceeds maximum allowed size"
- ✅ Upload is prevented
- ✅ No message is created
- ✅ User can try again with smaller file

### 5. Long File Names

**Steps:**
1. Create or find a file with a very long name (50+ characters)
2. Send it in a chat
3. Observe how it displays

**Expected Results:**
- ✅ File name is truncated with ellipsis (...)
- ✅ Full name is still stored in database
- ✅ Layout doesn't break
- ✅ File size is still visible

### 6. Multiple Documents in Sequence

**Steps:**
1. Open a chat
2. Send 3 different documents one after another
3. Wait for each to complete before sending next

**Expected Results:**
- ✅ All three documents upload successfully
- ✅ They appear in correct chronological order
- ✅ Each has correct icon, name, and size
- ✅ No messages are lost or duplicated

### 7. Document + Text Message

**Steps:**
1. Open a chat
2. Send a document
3. Immediately send a text message
4. Send another document

**Expected Results:**
- ✅ Document appears first
- ✅ Text message appears second
- ✅ Second document appears third
- ✅ All messages are properly separated
- ✅ Timestamps are correct

### 8. Receiving Documents

**Steps:**
1. Have another user send you a document
2. Observe how it appears in your chat

**Expected Results:**
- ✅ Document appears on left side (received)
- ✅ Sender's avatar/initials are shown
- ✅ Sender's name is shown (if first in group)
- ✅ Document icon, name, and size are displayed
- ✅ Timestamp is shown
- ✅ Real-time update (no refresh needed)

### 9. Offline Document Sending

**Steps:**
1. Open a chat
2. Turn on airplane mode
3. Try to send a document
4. Observe the behavior
5. Turn off airplane mode

**Expected Results:**
- ✅ Document is queued for sending
- ✅ Message shows "SENDING" status
- ✅ Toast shows offline message
- ✅ When online, document uploads automatically
- ✅ Status changes to "SENT"
- ✅ Recipient receives the document

### 10. Upload Failure Handling

**Steps:**
1. Open a chat
2. Start uploading a large document
3. Turn on airplane mode during upload
4. Observe the error handling

**Expected Results:**
- ✅ Progress dialog shows error
- ✅ Error toast appears with message
- ✅ Message shows "FAILED" status
- ✅ User can tap to retry
- ✅ Retry dialog appears with options

### 11. Progress Indicator Accuracy

**Steps:**
1. Open a chat
2. Send a medium-sized document (2-5 MB)
3. Watch the progress dialog closely

**Expected Results:**
- ✅ Progress starts at 0%
- ✅ Progress updates smoothly (not jumping)
- ✅ Progress reaches 100% before dismissing
- ✅ Dialog doesn't freeze or hang
- ✅ Percentage is accurate

### 12. Chat List Update

**Steps:**
1. Send a document in a chat
2. Go back to chat list
3. Observe the last message preview

**Expected Results:**
- ✅ Chat shows "📄 filename.pdf" as last message
- ✅ Timestamp is updated
- ✅ Chat moves to top of list
- ✅ Unread count doesn't increase for sender

### 13. Notification Trigger

**Steps:**
1. Send a document to another user
2. Check if they receive a notification (if they're not in the chat)

**Expected Results:**
- ✅ Recipient receives push notification
- ✅ Notification shows "📄 filename.pdf"
- ✅ Tapping notification opens the chat
- ✅ Document is visible in chat

### 14. Special Characters in File Name

**Steps:**
1. Create or find a file with special characters (e.g., "Report #1 (Final).pdf")
2. Send it in a chat
3. Observe how it displays

**Expected Results:**
- ✅ File name displays correctly with special characters
- ✅ Upload succeeds
- ✅ No encoding issues
- ✅ File can be identified correctly

### 15. Rapid Document Sending

**Steps:**
1. Open a chat
2. Quickly send 5 documents without waiting for uploads to complete
3. Observe the behavior

**Expected Results:**
- ✅ All documents are queued
- ✅ Progress dialogs appear for each
- ✅ All uploads complete successfully
- ✅ Documents appear in correct order
- ✅ No crashes or freezes

## Performance Testing

### Upload Speed Test
1. Send a 5MB document
2. Measure time from selection to completion
3. Expected: < 30 seconds on good connection

### UI Responsiveness Test
1. Send a document
2. Try to scroll messages during upload
3. Expected: UI remains responsive, no lag

### Memory Usage Test
1. Send 10 documents in a row
2. Monitor app memory usage
3. Expected: No memory leaks, stable memory usage

## Edge Cases

### Empty File
- Try to send a 0-byte file
- Expected: Should upload but show "0 B" size

### File Without Extension
- Send a file without extension
- Expected: Shows default icon, uploads successfully

### Very Small File
- Send a 1KB text file
- Expected: Shows "1 KB", uploads instantly

### Duplicate File Names
- Send same file twice
- Expected: Both upload with unique storage paths

## Regression Testing

Ensure existing features still work:
- ✅ Text messages send correctly
- ✅ Image messages send correctly
- ✅ Typing indicators work
- ✅ Read receipts work
- ✅ Message pagination works
- ✅ Offline message queue works

## Device Compatibility

Test on:
- ✅ Android 13+ (with new storage permissions)
- ✅ Android 10-12 (with legacy permissions)
- ✅ Android 7-9 (minimum supported)
- ✅ Different screen sizes
- ✅ Different manufacturers (Samsung, Google, etc.)

## Known Issues to Watch For

1. **Permission Denied**: If document picker doesn't open, check storage permissions
2. **Upload Timeout**: Very large files on slow connections may timeout
3. **File Not Found**: If file is deleted before upload completes
4. **Storage Full**: If device storage is full

## Troubleshooting

### Document Picker Doesn't Open
- Check if app has storage permissions
- Try restarting the app
- Check if file manager app is installed

### Upload Fails Immediately
- Check internet connection
- Verify Firebase Storage is configured
- Check Firebase console for errors
- Verify Storage security rules allow uploads

### Progress Stuck at 0%
- Check network connection
- Verify file exists and is accessible
- Check Firebase Storage quota

### Document Doesn't Appear
- Check Firestore console for message document
- Verify chat ID is correct
- Check for JavaScript errors in Firebase console

## Success Criteria

Task 16 is considered successful if:
- ✅ All basic document sending scenarios work
- ✅ All file types are supported and display correctly
- ✅ Progress indicator works accurately
- ✅ Error handling is robust
- ✅ Offline queuing works
- ✅ UI is responsive during uploads
- ✅ No crashes or memory leaks
- ✅ Documents appear for both sender and recipient
- ✅ Chat list updates correctly
- ✅ Notifications are triggered

## Reporting Issues

If you find any issues during testing:
1. Note the exact steps to reproduce
2. Capture screenshots or screen recording
3. Check logcat for error messages
4. Note device model and Android version
5. Report with all details

## Next Steps

After successful testing of Task 16:
- Proceed to Task 17: Image viewer and document download
- This will add the ability to view images full-screen and download/open documents
