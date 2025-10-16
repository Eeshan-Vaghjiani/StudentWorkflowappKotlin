# Task 14 Testing Guide: Attachment Picker

## Prerequisites
- Android device or emulator running API 23+
- App installed and logged in
- At least one chat conversation available

## Test Scenarios

### 1. Basic Functionality Test

#### Test 1.1: Attachment Button Visibility
1. Open the app and navigate to a chat room
2. Look at the message input area
3. **Expected:** Attachment button (paperclip icon) visible to the left of the text input field

#### Test 1.2: Bottom Sheet Display
1. Tap the attachment button
2. **Expected:** Bottom sheet slides up from bottom
3. **Expected:** Three options visible: Camera, Gallery, Documents
4. **Expected:** Each option has an icon and label

#### Test 1.3: Bottom Sheet Dismissal
1. Open the bottom sheet
2. Tap outside the bottom sheet or swipe down
3. **Expected:** Bottom sheet dismisses

### 2. Camera Permission Tests (Android 13+)

#### Test 2.1: Camera Permission - First Request
1. Fresh install or clear app data
2. Open chat and tap attachment button
3. Tap "Camera" option
4. **Expected:** Permission dialog appears requesting CAMERA permission
5. Tap "Allow"
6. **Expected:** Camera app opens
7. Take a photo
8. **Expected:** Toast message shows "Image selected: [URI]"
9. **Expected:** Bottom sheet dismisses

#### Test 2.2: Camera Permission - Denied Once
1. Open chat and tap attachment button
2. Tap "Camera" option
3. Tap "Deny" on permission dialog
4. Tap attachment button again
5. Tap "Camera" option
6. **Expected:** Rationale dialog appears explaining why permission is needed
7. Tap "Grant"
8. **Expected:** Permission dialog appears again
9. Tap "Allow"
10. **Expected:** Camera opens

#### Test 2.3: Camera Permission - Permanently Denied
1. Deny camera permission twice (select "Don't ask again")
2. Tap attachment button and select "Camera"
3. **Expected:** Dialog appears with option to open Settings
4. Tap "Settings"
5. **Expected:** App settings page opens
6. Grant camera permission
7. Return to app and try again
8. **Expected:** Camera opens without permission dialog

### 3. Gallery Permission Tests

#### Test 3.1: Gallery Permission - Android 13+ (READ_MEDIA_IMAGES)
**Device:** Android 13 or higher
1. Fresh install or clear app data
2. Open chat and tap attachment button
3. Tap "Gallery" option
4. **Expected:** Permission dialog requests READ_MEDIA_IMAGES
5. Tap "Allow"
6. **Expected:** Photo picker opens
7. Select an image
8. **Expected:** Toast message shows "Image selected: [URI]"
9. **Expected:** Bottom sheet dismisses

#### Test 3.2: Gallery Permission - Android 12 and below (READ_EXTERNAL_STORAGE)
**Device:** Android 12 or lower
1. Fresh install or clear app data
2. Open chat and tap attachment button
3. Tap "Gallery" option
4. **Expected:** Permission dialog requests READ_EXTERNAL_STORAGE
5. Tap "Allow"
6. **Expected:** Gallery/photo picker opens
7. Select an image
8. **Expected:** Toast message shows "Image selected: [URI]"

#### Test 3.3: Gallery Permission - Denied
1. Deny storage permission
2. Tap attachment button and select "Gallery"
3. **Expected:** Rationale dialog appears
4. Tap "Grant" and allow permission
5. **Expected:** Gallery opens

### 4. Document Picker Tests

#### Test 4.1: Document Picker - No Permission Required
1. Open chat and tap attachment button
2. Tap "Documents" option
3. **Expected:** Document picker opens immediately (no permission dialog)
4. **Expected:** Can browse files and folders

#### Test 4.2: Document Selection - PDF
1. Open document picker
2. Navigate to a PDF file
3. Select the PDF
4. **Expected:** Toast message shows "Document selected: [URI]"
5. **Expected:** Bottom sheet dismisses

#### Test 4.3: Document Selection - Various Types
Test with different file types:
- PDF (.pdf)
- Word document (.doc, .docx)
- Excel spreadsheet (.xls, .xlsx)
- PowerPoint (.ppt, .pptx)
- Text file (.txt)
- ZIP file (.zip)

**Expected:** All supported types can be selected

### 5. Edge Cases and Error Handling

#### Test 5.1: Camera - Cancel
1. Open camera from attachment picker
2. Press back button or cancel
3. **Expected:** Returns to chat without error
4. **Expected:** No toast message

#### Test 5.2: Gallery - Cancel
1. Open gallery from attachment picker
2. Press back button without selecting
3. **Expected:** Returns to chat without error
4. **Expected:** No toast message

#### Test 5.3: Document Picker - Cancel
1. Open document picker
2. Press back button without selecting
3. **Expected:** Returns to chat without error
4. **Expected:** No toast message

#### Test 5.4: Multiple Selections
1. Select an image from gallery
2. Immediately tap attachment button again
3. Select a document
4. **Expected:** Both selections work independently
5. **Expected:** No crashes or errors

#### Test 5.5: Rotation Test
1. Open attachment bottom sheet
2. Rotate device
3. **Expected:** Bottom sheet remains visible or gracefully dismisses
4. **Expected:** No crashes

### 6. UI/UX Tests

#### Test 6.1: Icon Visibility
1. Check all icons are visible and properly colored
2. **Expected:** Camera, Gallery, Document icons display correctly
3. **Expected:** Attachment button icon visible in chat input

#### Test 6.2: Touch Feedback
1. Tap each option in bottom sheet
2. **Expected:** Ripple effect shows on tap
3. **Expected:** Options respond immediately

#### Test 6.3: Accessibility
1. Enable TalkBack
2. Navigate to attachment button
3. **Expected:** "Attach file" announced
4. Open bottom sheet
5. **Expected:** Each option properly announced

### 7. Integration Tests

#### Test 7.1: After Image Selection
1. Select an image from gallery
2. **Expected:** Toast shows with URI
3. **Note:** Actual upload will be implemented in Task 15

#### Test 7.2: After Document Selection
1. Select a document
2. **Expected:** Toast shows with URI
3. **Note:** Actual upload will be implemented in Task 16

## Known Limitations (To Be Implemented)

- ✅ Attachment picker UI complete
- ✅ Permission handling complete
- ✅ File selection complete
- ⏳ Image upload (Task 15)
- ⏳ Document upload (Task 16)
- ⏳ Image compression (Task 15)
- ⏳ Progress indicators (Task 15, 16)
- ⏳ Message display with attachments (Task 15, 16)

## Troubleshooting

### Issue: Camera doesn't open
**Solution:** Check that CAMERA permission is granted in app settings

### Issue: Gallery doesn't open
**Solution:** 
- Android 13+: Check READ_MEDIA_IMAGES permission
- Android 12-: Check READ_EXTERNAL_STORAGE permission

### Issue: "Error opening camera" toast
**Solution:** 
- Check that device has a camera
- Check that FileProvider is properly configured
- Check that cache directory is accessible

### Issue: Document picker shows "No apps can perform this action"
**Solution:** Install a file manager app (most devices have one pre-installed)

### Issue: Bottom sheet doesn't appear
**Solution:** 
- Check that ChatRoomActivity is properly loaded
- Check logcat for errors
- Verify attachment button click listener is set

## Success Criteria

All tests should pass with the following results:
- ✅ Attachment button visible and clickable
- ✅ Bottom sheet displays with three options
- ✅ Camera permission requested and handled correctly
- ✅ Storage permission requested and handled correctly (version-specific)
- ✅ Document picker opens without permissions
- ✅ Selected files return URIs to activity
- ✅ Toast messages confirm selections
- ✅ No crashes or errors
- ✅ Proper error handling for denied permissions
- ✅ Settings navigation works for permanently denied permissions

## Next Steps After Testing

Once all tests pass:
1. Proceed to Task 15: Implement image message sending
2. Proceed to Task 16: Implement document message sending

These tasks will use the URIs returned by this attachment picker to upload files to Firebase Storage and send messages with attachments.
