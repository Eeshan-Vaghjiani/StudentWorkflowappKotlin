# Task 18: Profile Picture Upload - Testing Guide

## Pre-Testing Setup

### 1. Build and Install
```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 2. Ensure Firebase Configuration
- Firebase project is set up
- `google-services.json` is in place
- Firebase Storage is enabled
- User is logged in

### 3. Device Requirements
- Android device or emulator (API 23+)
- Camera (for camera testing)
- Gallery with images (for gallery testing)
- Internet connection

## Test Cases

### Test 1: View Profile Screen
**Steps**:
1. Launch the app
2. Log in with valid credentials
3. Navigate to Profile tab

**Expected Results**:
- ✅ Profile screen displays
- ✅ User email is shown
- ✅ User ID is shown
- ✅ Display name is shown
- ✅ Profile picture area is visible (circular)
- ✅ Edit button (FAB) is visible
- ✅ Default placeholder icon is shown if no profile picture

**Status**: [ ]

---

### Test 2: Open Profile Picture Options
**Steps**:
1. Navigate to Profile tab
2. Tap the edit button (camera FAB)

**Expected Results**:
- ✅ Bottom sheet dialog appears
- ✅ "Take Photo" option is visible
- ✅ "Choose from Gallery" option is visible
- ✅ "Cancel" option is visible
- ✅ Dialog has proper styling

**Status**: [ ]

---

### Test 3: Cancel Profile Picture Selection
**Steps**:
1. Tap edit button
2. Tap "Cancel" in bottom sheet

**Expected Results**:
- ✅ Bottom sheet dismisses
- ✅ No action is taken
- ✅ Profile screen remains unchanged

**Status**: [ ]

---

### Test 4: Choose from Gallery - Grant Permission
**Steps**:
1. Tap edit button
2. Tap "Choose from Gallery"
3. If prompted, grant media/storage permission
4. Select an image from gallery

**Expected Results**:
- ✅ Gallery picker opens
- ✅ Permission dialog appears (if first time)
- ✅ After granting permission, gallery is accessible
- ✅ Selected image triggers upload

**Status**: [ ]

---

### Test 5: Choose from Gallery - Upload Success
**Steps**:
1. Tap edit button
2. Tap "Choose from Gallery"
3. Select a valid image (JPG/PNG)
4. Wait for upload to complete

**Expected Results**:
- ✅ Bottom sheet dismisses
- ✅ Progress bar appears
- ✅ Upload status text shows "Uploading... X%"
- ✅ Progress updates from 0% to 100%
- ✅ Edit button is disabled during upload
- ✅ Profile picture updates with new image
- ✅ Progress bar disappears
- ✅ Success toast message appears
- ✅ Edit button is re-enabled
- ✅ Image is displayed in circular frame

**Status**: [ ]

---

### Test 6: Take Photo - Request Camera Permission
**Steps**:
1. Tap edit button
2. Tap "Take Photo"
3. If prompted, observe permission dialog

**Expected Results**:
- ✅ Camera permission dialog appears (if first time)
- ✅ Dialog explains why permission is needed
- ✅ User can grant or deny permission

**Status**: [ ]

---

### Test 7: Take Photo - Permission Denied
**Steps**:
1. Tap edit button
2. Tap "Take Photo"
3. Deny camera permission

**Expected Results**:
- ✅ Toast message appears: "Camera permission is required to take photos"
- ✅ Camera does not open
- ✅ User returns to profile screen

**Status**: [ ]

---

### Test 8: Take Photo - Permission Granted
**Steps**:
1. Tap edit button
2. Tap "Take Photo"
3. Grant camera permission (if prompted)
4. Take a photo
5. Confirm/accept the photo

**Expected Results**:
- ✅ Camera app opens
- ✅ User can take a photo
- ✅ After confirming, upload begins
- ✅ Progress bar and status appear
- ✅ Profile picture updates with new photo
- ✅ Success message appears

**Status**: [ ]

---

### Test 9: Image Compression Verification
**Steps**:
1. Select a large image (>2MB) from gallery
2. Wait for upload to complete
3. Check Firebase Storage console

**Expected Results**:
- ✅ Image is compressed before upload
- ✅ Uploaded file size is under 500KB
- ✅ Image quality is acceptable
- ✅ Image dimensions are max 800x800
- ✅ Upload completes successfully

**Status**: [ ]

---

### Test 10: Firestore Update Verification
**Steps**:
1. Upload a profile picture
2. Check Firebase Console > Firestore
3. Navigate to `users/{userId}` document

**Expected Results**:
- ✅ `photoUrl` field is updated
- ✅ URL points to Firebase Storage
- ✅ URL format: `https://firebasestorage.googleapis.com/...`
- ✅ Timestamp in filename is recent

**Status**: [ ]

---

### Test 11: Profile Picture Persistence
**Steps**:
1. Upload a profile picture
2. Navigate away from Profile tab
3. Return to Profile tab
4. Close and reopen the app
5. Navigate to Profile tab

**Expected Results**:
- ✅ Profile picture persists when navigating away
- ✅ Profile picture loads from Firestore on app restart
- ✅ Image loads from cache (fast)
- ✅ No re-upload occurs

**Status**: [ ]

---

### Test 12: Multiple Uploads
**Steps**:
1. Upload a profile picture
2. Wait for completion
3. Immediately upload a different picture
4. Repeat 2-3 times

**Expected Results**:
- ✅ Each upload completes successfully
- ✅ Latest image is displayed
- ✅ Old images remain in Storage (not deleted)
- ✅ Firestore always has latest URL
- ✅ No crashes or errors

**Status**: [ ]

---

### Test 13: Upload Error Handling - No Internet
**Steps**:
1. Turn on airplane mode
2. Tap edit button
3. Select an image
4. Observe behavior

**Expected Results**:
- ✅ Upload fails gracefully
- ✅ Error toast appears with message
- ✅ Progress bar disappears
- ✅ Edit button is re-enabled
- ✅ Old profile picture remains (if any)
- ✅ No crash

**Status**: [ ]

---

### Test 14: Upload Error Handling - Large File
**Steps**:
1. Try to upload an extremely large image (>10MB)
2. Observe behavior

**Expected Results**:
- ✅ Image is compressed automatically
- ✅ Compressed version is under 500KB
- ✅ Upload succeeds
- ✅ OR error message if compression fails

**Status**: [ ]

---

### Test 15: UI State During Upload
**Steps**:
1. Select a large image to upload
2. During upload, try to:
   - Tap edit button again
   - Navigate away
   - Rotate device

**Expected Results**:
- ✅ Edit button is disabled during upload
- ✅ Progress bar is visible
- ✅ Upload continues if navigating away
- ✅ State is preserved on rotation (if applicable)

**Status**: [ ]

---

### Test 16: Default Avatar Display
**Steps**:
1. Create a new user account (or use account without profile picture)
2. Navigate to Profile tab

**Expected Results**:
- ✅ Default person icon is displayed
- ✅ Icon is in circular frame
- ✅ Icon is properly styled
- ✅ No broken image or error

**Status**: [ ]

---

### Test 17: Circular Crop Transformation
**Steps**:
1. Upload a rectangular image (e.g., 1920x1080)
2. Observe how it's displayed

**Expected Results**:
- ✅ Image is displayed in circular frame
- ✅ Image is center-cropped
- ✅ No distortion
- ✅ Entire circle is filled

**Status**: [ ]

---

### Test 18: Storage Path Verification
**Steps**:
1. Upload a profile picture
2. Check Firebase Storage console
3. Navigate to storage structure

**Expected Results**:
- ✅ File is in `profile_images/{userId}/` folder
- ✅ Filename format: `{timestamp}.jpg`
- ✅ File is accessible via URL
- ✅ File has proper metadata

**Status**: [ ]

---

### Test 19: Logout and Login
**Steps**:
1. Upload a profile picture
2. Logout
3. Login again
4. Navigate to Profile tab

**Expected Results**:
- ✅ Profile picture loads correctly
- ✅ Same image as before logout
- ✅ No re-upload required

**Status**: [ ]

---

### Test 20: Different Image Formats
**Steps**:
1. Upload a JPG image
2. Upload a PNG image
3. Upload a WebP image (if available)

**Expected Results**:
- ✅ All formats are accepted
- ✅ All are converted to JPG
- ✅ All display correctly
- ✅ Compression works for all formats

**Status**: [ ]

---

## Performance Testing

### Test 21: Upload Speed
**Steps**:
1. Upload a profile picture
2. Measure time from selection to completion

**Expected Results**:
- ✅ Compression completes in <2 seconds
- ✅ Upload completes in <5 seconds (on good connection)
- ✅ Total time is reasonable

**Status**: [ ]

---

### Test 22: Memory Usage
**Steps**:
1. Open Android Profiler
2. Upload multiple large images
3. Monitor memory usage

**Expected Results**:
- ✅ No memory leaks
- ✅ Memory is released after upload
- ✅ No OutOfMemoryError

**Status**: [ ]

---

## Edge Cases

### Test 23: Very Small Image
**Steps**:
1. Upload a very small image (e.g., 100x100)

**Expected Results**:
- ✅ Image uploads successfully
- ✅ No upscaling occurs
- ✅ Image displays correctly

**Status**: [ ]

---

### Test 24: Square Image
**Steps**:
1. Upload a perfectly square image (e.g., 1000x1000)

**Expected Results**:
- ✅ Image uploads successfully
- ✅ No distortion
- ✅ Displays perfectly in circular frame

**Status**: [ ]

---

### Test 25: Portrait vs Landscape
**Steps**:
1. Upload a portrait image (e.g., 1080x1920)
2. Upload a landscape image (e.g., 1920x1080)

**Expected Results**:
- ✅ Both upload successfully
- ✅ Both are center-cropped appropriately
- ✅ Both display well in circular frame

**Status**: [ ]

---

## Regression Testing

### Test 26: Existing Features Still Work
**Steps**:
1. Test logout functionality
2. Test email display
3. Test user ID display

**Expected Results**:
- ✅ All existing features work as before
- ✅ No regressions introduced

**Status**: [ ]

---

## Accessibility Testing

### Test 27: Content Descriptions
**Steps**:
1. Enable TalkBack
2. Navigate to Profile tab
3. Focus on profile picture and edit button

**Expected Results**:
- ✅ Profile picture has content description
- ✅ Edit button has content description
- ✅ Descriptions are meaningful

**Status**: [ ]

---

## Summary Checklist

### Core Functionality
- [ ] Profile picture display works
- [ ] Edit button opens options
- [ ] Camera option works
- [ ] Gallery option works
- [ ] Upload with progress works
- [ ] Firestore update works
- [ ] Image persistence works

### Permissions
- [ ] Camera permission handled correctly
- [ ] Storage/media permission handled correctly
- [ ] Permission denial handled gracefully

### Error Handling
- [ ] Network errors handled
- [ ] Upload failures handled
- [ ] Invalid files handled
- [ ] User feedback provided

### UI/UX
- [ ] Progress feedback is clear
- [ ] Loading states are proper
- [ ] Success messages appear
- [ ] Error messages are helpful
- [ ] Circular display looks good

### Performance
- [ ] Compression works efficiently
- [ ] Upload speed is acceptable
- [ ] No memory leaks
- [ ] No crashes

### Integration
- [ ] Firebase Storage integration works
- [ ] Firestore update works
- [ ] Coil image loading works
- [ ] FileProvider works

## Known Issues
(Document any issues found during testing)

---

## Test Environment
- **Device**: _________________
- **Android Version**: _________________
- **App Version**: _________________
- **Date Tested**: _________________
- **Tester**: _________________

## Notes
(Add any additional observations or notes)
