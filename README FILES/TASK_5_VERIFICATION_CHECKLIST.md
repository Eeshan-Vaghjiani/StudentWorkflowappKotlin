# Task 5: Profile Picture Upload - Verification Checklist

## Quick Verification Guide

Use this checklist to verify that Task 5 (Profile Picture Upload) is working correctly.

## Pre-Testing Setup

- [ ] App is built and installed on device/emulator
- [ ] User is logged in with Google account
- [ ] Internet connection is available
- [ ] Camera permission can be granted (for camera test)
- [ ] Gallery has at least one image (for gallery test)

## Functional Tests

### 1. Gallery Upload Flow
- [ ] Navigate to Profile tab
- [ ] Tap the camera FAB button on profile picture
- [ ] Bottom sheet appears with three options
- [ ] Tap "Choose from Gallery"
- [ ] System gallery picker opens
- [ ] Select an image
- [ ] Progress bar appears showing upload percentage
- [ ] Status text shows "Uploading... X%"
- [ ] Upload completes successfully
- [ ] New profile picture displays in circular frame
- [ ] Success toast message appears
- [ ] Profile picture persists after app restart

### 2. Camera Upload Flow
- [ ] Navigate to Profile tab
- [ ] Tap the camera FAB button
- [ ] Bottom sheet appears
- [ ] Tap "Take Photo"
- [ ] Camera permission dialog appears (if first time)
- [ ] Grant camera permission
- [ ] Camera app launches
- [ ] Take a photo
- [ ] Confirm/accept the photo
- [ ] Progress bar appears
- [ ] Upload completes successfully
- [ ] New profile picture displays
- [ ] Success toast message appears

### 3. Cancel Flow
- [ ] Tap camera FAB button
- [ ] Bottom sheet appears
- [ ] Tap "Cancel"
- [ ] Bottom sheet dismisses
- [ ] No upload occurs
- [ ] Profile picture remains unchanged

### 4. Permission Denied Flow
- [ ] Deny camera permission when prompted
- [ ] Toast message appears: "Camera permission is required to take photos"
- [ ] No crash occurs
- [ ] Can still use gallery option

### 5. Error Handling
- [ ] Turn off internet connection
- [ ] Try to upload a profile picture
- [ ] Error toast appears with appropriate message
- [ ] Progress indicators hide
- [ ] FAB button re-enables
- [ ] App remains stable

## UI Verification

### Profile Fragment Layout
- [ ] Profile picture displays in circular frame with border
- [ ] Camera FAB button positioned at bottom-right of picture
- [ ] Progress bar hidden by default
- [ ] Status text hidden by default
- [ ] User name displays below picture
- [ ] Email displays correctly
- [ ] User ID displays correctly
- [ ] Logout button present and functional

### Bottom Sheet
- [ ] Title: "Change Profile Picture"
- [ ] Three buttons with icons:
  - [ ] Take Photo (camera icon)
  - [ ] Choose from Gallery (gallery icon)
  - [ ] Cancel
- [ ] Buttons are properly styled
- [ ] Bottom sheet dismisses after selection

### Progress Indicators
- [ ] Progress bar appears during upload
- [ ] Progress bar shows actual progress (0-100%)
- [ ] Status text shows percentage
- [ ] Both hide after upload completes
- [ ] FAB button disables during upload
- [ ] FAB button re-enables after upload

## Backend Verification

### Firebase Storage
- [ ] Open Firebase Console → Storage
- [ ] Navigate to `profile_images/{userId}/`
- [ ] Uploaded image file exists
- [ ] File name format: `{timestamp}.jpg`
- [ ] File size is reasonable (< 500KB)
- [ ] Metadata includes:
  - [ ] contentType: "image/jpeg"
  - [ ] uploadedBy: user's UID
  - [ ] uploadedAt: timestamp

### Firestore
- [ ] Open Firebase Console → Firestore
- [ ] Navigate to `users/{userId}` document
- [ ] `photoUrl` field contains Firebase Storage URL
- [ ] URL format: `https://firebasestorage.googleapis.com/...`
- [ ] URL is accessible (can open in browser)

## Code Quality Checks

- [x] No compiler errors
- [x] No diagnostic warnings
- [x] Proper error handling implemented
- [x] Memory leaks prevented (ViewBinding cleanup)
- [x] Coroutines properly scoped
- [x] Permissions declared in manifest
- [x] FileProvider configured correctly

## Performance Checks

- [ ] Image compression works (file size < 500KB)
- [ ] Upload completes in reasonable time (< 10 seconds on good connection)
- [ ] No UI freezing during upload
- [ ] Progress updates smoothly
- [ ] No memory leaks after multiple uploads

## Edge Cases

### Large Images
- [ ] Select a very large image (> 5MB original)
- [ ] Image is compressed before upload
- [ ] Upload succeeds
- [ ] Final file size is reasonable

### Multiple Uploads
- [ ] Upload a profile picture
- [ ] Immediately upload another one
- [ ] Second upload replaces first
- [ ] Old image remains in Storage (not deleted)
- [ ] Firestore URL updates to new image

### Rotation/Orientation
- [ ] Upload image in portrait mode
- [ ] Rotate device to landscape
- [ ] Profile picture still displays correctly
- [ ] Upload another image in landscape
- [ ] Both orientations work correctly

### App Lifecycle
- [ ] Start upload
- [ ] Press home button (app goes to background)
- [ ] Return to app
- [ ] Upload continues or shows appropriate state
- [ ] No crash occurs

## Accessibility

- [ ] Camera FAB has content description
- [ ] Profile picture ImageView has content description
- [ ] Bottom sheet buttons have proper labels
- [ ] Screen reader can navigate all elements

## Common Issues & Solutions

### Issue: Camera doesn't launch
**Solution:** Check camera permission in app settings

### Issue: Gallery picker doesn't open
**Solution:** Check storage permission in app settings (Android 12 and below)

### Issue: Upload fails with permission error
**Solution:** Check Firebase Storage security rules are deployed

### Issue: Image doesn't display after upload
**Solution:** 
1. Check internet connection
2. Verify URL in Firestore is valid
3. Check Coil dependency is included

### Issue: Progress bar doesn't show
**Solution:** Check layout XML has progress_upload and tv_upload_status views

## Test Results Template

```
Date: ___________
Tester: ___________
Device: ___________
Android Version: ___________

Gallery Upload: [ ] PASS [ ] FAIL
Camera Upload: [ ] PASS [ ] FAIL
Cancel Flow: [ ] PASS [ ] FAIL
Permission Denied: [ ] PASS [ ] FAIL
Error Handling: [ ] PASS [ ] FAIL
UI Display: [ ] PASS [ ] FAIL
Backend Storage: [ ] PASS [ ] FAIL
Backend Firestore: [ ] PASS [ ] FAIL

Notes:
_________________________________
_________________________________
_________________________________
```

## Success Criteria

Task 5 is considered successfully verified when:
- ✅ All functional tests pass
- ✅ UI displays correctly
- ✅ Backend data is stored properly
- ✅ No crashes or errors occur
- ✅ Performance is acceptable
- ✅ Edge cases are handled gracefully

## Next Task

After verification is complete, proceed to:
**Task 6: Update ChatRoomActivity to support file attachments**
