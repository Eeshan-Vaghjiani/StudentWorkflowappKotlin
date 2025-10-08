# Task 17 Testing Guide: Image Viewer and Document Download

## Prerequisites
- App installed on device/emulator
- At least two user accounts for testing
- Active internet connection
- Various test files ready (images, PDFs, documents)

## Test Scenarios

### 1. Image Viewer - Basic Functionality

#### Test 1.1: Open Image Viewer
**Steps:**
1. Open a chat with image messages
2. Tap on any image message
3. Observe the image viewer opens

**Expected Results:**
- ✅ Image viewer opens in full-screen mode
- ✅ Image loads and displays correctly
- ✅ Black background with no status bar
- ✅ Close button visible in top-left
- ✅ Download button visible in top-right

#### Test 1.2: Close Image Viewer
**Steps:**
1. Open image viewer
2. Tap the close button (X)

**Expected Results:**
- ✅ Image viewer closes
- ✅ Returns to chat screen
- ✅ Smooth fade-out animation

### 2. Pinch-to-Zoom Functionality

#### Test 2.1: Zoom In
**Steps:**
1. Open image viewer
2. Pinch outward (zoom in gesture)
3. Continue zooming to maximum

**Expected Results:**
- ✅ Image zooms in smoothly
- ✅ Zoom stops at 5x maximum
- ✅ Image quality remains good
- ✅ No lag or stuttering

#### Test 2.2: Zoom Out
**Steps:**
1. Zoom in on an image
2. Pinch inward (zoom out gesture)
3. Continue zooming to minimum

**Expected Results:**
- ✅ Image zooms out smoothly
- ✅ Zoom stops at 1x minimum
- ✅ Image returns to fit-center
- ✅ Pan position resets

#### Test 2.3: Double-Tap Zoom
**Steps:**
1. Open image viewer (1x zoom)
2. Double-tap anywhere on image
3. Double-tap again

**Expected Results:**
- ✅ First double-tap zooms to 2x
- ✅ Second double-tap returns to 1x
- ✅ Smooth zoom animation
- ✅ Zoom centers on tap location

### 3. Pan Functionality (When Zoomed)

#### Test 3.1: Pan While Zoomed
**Steps:**
1. Open image viewer
2. Zoom in to 2x or more
3. Drag image in all directions

**Expected Results:**
- ✅ Image pans smoothly
- ✅ Cannot pan beyond image bounds
- ✅ Panning feels natural and responsive
- ✅ No jumping or glitching

#### Test 3.2: Pan Constraints
**Steps:**
1. Zoom in to maximum (5x)
2. Try to pan beyond visible area

**Expected Results:**
- ✅ Panning stops at image edges
- ✅ Cannot see black background beyond image
- ✅ Smooth resistance at boundaries

### 4. Swipe-to-Dismiss Functionality

#### Test 4.1: Swipe Down to Dismiss
**Steps:**
1. Open image viewer (not zoomed)
2. Swipe down from center of image
3. Swipe past threshold (~200px)

**Expected Results:**
- ✅ Image follows finger movement
- ✅ Background fades as you swipe
- ✅ Image viewer closes when threshold reached
- ✅ Smooth animation

#### Test 4.2: Swipe Up to Dismiss
**Steps:**
1. Open image viewer
2. Swipe up from center of image
3. Swipe past threshold

**Expected Results:**
- ✅ Same behavior as swipe down
- ✅ Image viewer closes
- ✅ Smooth animation

#### Test 4.3: Incomplete Swipe
**Steps:**
1. Open image viewer
2. Swipe down but don't reach threshold
3. Release finger

**Expected Results:**
- ✅ Image animates back to center
- ✅ Background fades back to full opacity
- ✅ Image viewer remains open
- ✅ Smooth return animation

#### Test 4.4: Swipe While Zoomed
**Steps:**
1. Open image viewer
2. Zoom in to 2x
3. Try to swipe to dismiss

**Expected Results:**
- ✅ Swipe-to-dismiss is disabled when zoomed
- ✅ Swipe gesture pans the image instead
- ✅ Must zoom out to 1x to dismiss

### 5. Image Download Functionality

#### Test 5.1: Download Image
**Steps:**
1. Open image viewer
2. Tap download button (down arrow)
3. Wait for download to complete

**Expected Results:**
- ✅ Download progress bar appears at bottom
- ✅ Progress updates from 0% to 100%
- ✅ Success message appears: "Image saved to Downloads"
- ✅ Download button re-enables after completion

#### Test 5.2: Verify Downloaded Image
**Steps:**
1. Download an image
2. Open device's Downloads folder
3. Find the downloaded image

**Expected Results:**
- ✅ Image file exists in Downloads
- ✅ Filename format: IMG_[timestamp].jpg
- ✅ Image can be opened in gallery app
- ✅ Image quality is preserved

#### Test 5.3: Download Multiple Images
**Steps:**
1. Download first image
2. Immediately download another image
3. Check Downloads folder

**Expected Results:**
- ✅ Both images downloaded successfully
- ✅ Unique filenames (different timestamps)
- ✅ No file conflicts or overwrites

#### Test 5.4: Download Failure
**Steps:**
1. Turn off internet connection
2. Open image viewer
3. Tap download button

**Expected Results:**
- ✅ Error message appears
- ✅ Message explains the failure
- ✅ Download button remains enabled
- ✅ Can retry after reconnecting

### 6. Document Download and Opening

#### Test 6.1: Download PDF Document
**Steps:**
1. Send a PDF document in chat
2. Tap on the document message
3. Wait for download to complete

**Expected Results:**
- ✅ Progress dialog appears: "Downloading Document"
- ✅ Progress updates from 0% to 100%
- ✅ PDF opens in PDF reader app
- ✅ Document content is correct

#### Test 6.2: Download Word Document
**Steps:**
1. Send a .docx file in chat
2. Tap on the document message
3. Wait for download

**Expected Results:**
- ✅ Document downloads successfully
- ✅ Opens in Word or compatible app
- ✅ Document is readable

#### Test 6.3: Download Excel Spreadsheet
**Steps:**
1. Send a .xlsx file in chat
2. Tap on the document message

**Expected Results:**
- ✅ Downloads successfully
- ✅ Opens in Excel or compatible app

#### Test 6.4: Download Text File
**Steps:**
1. Send a .txt file in chat
2. Tap on the document message

**Expected Results:**
- ✅ Downloads successfully
- ✅ Opens in text editor

#### Test 6.5: Download ZIP Archive
**Steps:**
1. Send a .zip file in chat
2. Tap on the document message

**Expected Results:**
- ✅ Downloads successfully
- ✅ Opens in file manager or archive app

#### Test 6.6: No App to Open File
**Steps:**
1. Send an obscure file type (e.g., .xyz)
2. Tap on the document message
3. Wait for download

**Expected Results:**
- ✅ File downloads successfully
- ✅ Message appears: "No app found to open this file type. File saved to Downloads."
- ✅ File exists in Downloads folder
- ✅ User can manually open it later

#### Test 6.7: Document Download Failure
**Steps:**
1. Turn off internet connection
2. Tap on a document message

**Expected Results:**
- ✅ Error message appears
- ✅ Message explains the failure
- ✅ Can retry after reconnecting

#### Test 6.8: Verify Downloaded Documents
**Steps:**
1. Download several documents
2. Open Downloads folder
3. Check all files

**Expected Results:**
- ✅ All documents present in Downloads
- ✅ Original filenames preserved
- ✅ File sizes match original
- ✅ Files can be opened

### 7. Integration Tests

#### Test 7.1: Image in Sent Messages
**Steps:**
1. Send an image message
2. Tap on your sent image
3. Test all viewer features

**Expected Results:**
- ✅ Image viewer opens correctly
- ✅ All gestures work
- ✅ Download works

#### Test 7.2: Image in Received Messages
**Steps:**
1. Receive an image from another user
2. Tap on the received image
3. Test all viewer features

**Expected Results:**
- ✅ Image viewer opens correctly
- ✅ All gestures work
- ✅ Download works

#### Test 7.3: Multiple Images in Chat
**Steps:**
1. Open chat with multiple images
2. Tap different images
3. Test viewer for each

**Expected Results:**
- ✅ Correct image opens each time
- ✅ No image mixing or confusion
- ✅ Viewer works consistently

#### Test 7.4: Document in Sent Messages
**Steps:**
1. Send a document
2. Tap on your sent document
3. Verify download and open

**Expected Results:**
- ✅ Downloads correctly
- ✅ Opens in appropriate app

#### Test 7.5: Document in Received Messages
**Steps:**
1. Receive a document
2. Tap on the received document
3. Verify download and open

**Expected Results:**
- ✅ Downloads correctly
- ✅ Opens in appropriate app

### 8. Edge Cases and Error Scenarios

#### Test 8.1: Very Large Image
**Steps:**
1. Send a very large image (5MB+)
2. Open in image viewer
3. Test zoom and pan

**Expected Results:**
- ✅ Image loads (may take time)
- ✅ Zoom and pan work smoothly
- ✅ No out-of-memory errors

#### Test 8.2: Very Small Image
**Steps:**
1. Send a tiny image (< 100KB)
2. Open in image viewer

**Expected Results:**
- ✅ Image displays correctly
- ✅ Zoom works properly
- ✅ No pixelation at 1x zoom

#### Test 8.3: Portrait Image
**Steps:**
1. Send a portrait-oriented image
2. Open in image viewer
3. Test all features

**Expected Results:**
- ✅ Image fits screen correctly
- ✅ Zoom and pan work properly

#### Test 8.4: Landscape Image
**Steps:**
1. Send a landscape-oriented image
2. Open in image viewer
3. Test all features

**Expected Results:**
- ✅ Image fits screen correctly
- ✅ Zoom and pan work properly

#### Test 8.5: Corrupted Image
**Steps:**
1. Try to open a corrupted image
2. Observe behavior

**Expected Results:**
- ✅ Error message appears
- ✅ App doesn't crash
- ✅ Can return to chat

#### Test 8.6: Network Interruption During Download
**Steps:**
1. Start downloading a large file
2. Turn off internet mid-download
3. Observe behavior

**Expected Results:**
- ✅ Download fails gracefully
- ✅ Error message appears
- ✅ Can retry when connection restored

#### Test 8.7: Storage Full
**Steps:**
1. Fill device storage (if possible)
2. Try to download image/document

**Expected Results:**
- ✅ Error message about storage
- ✅ App doesn't crash
- ✅ User informed to free space

#### Test 8.8: Rapid Taps
**Steps:**
1. Rapidly tap on multiple images
2. Rapidly tap download button

**Expected Results:**
- ✅ Only one viewer opens
- ✅ Only one download starts
- ✅ No crashes or duplicates

### 9. Performance Tests

#### Test 9.1: Image Viewer Load Time
**Steps:**
1. Tap on image message
2. Measure time to display

**Expected Results:**
- ✅ Viewer opens in < 1 second
- ✅ Image loads in < 3 seconds (on good connection)
- ✅ Smooth transition

#### Test 9.2: Zoom Performance
**Steps:**
1. Open image viewer
2. Rapidly zoom in and out
3. Observe smoothness

**Expected Results:**
- ✅ No lag or stuttering
- ✅ Smooth 60fps animation
- ✅ Responsive to gestures

#### Test 9.3: Download Speed
**Steps:**
1. Download a 2MB image
2. Measure time

**Expected Results:**
- ✅ Downloads in reasonable time (depends on connection)
- ✅ Progress updates smoothly
- ✅ No freezing

### 10. Accessibility Tests

#### Test 10.1: Content Descriptions
**Steps:**
1. Enable TalkBack
2. Navigate image viewer

**Expected Results:**
- ✅ All buttons have descriptions
- ✅ Image has description
- ✅ TalkBack announces actions

#### Test 10.2: Large Text
**Steps:**
1. Enable large text in system settings
2. Open image viewer

**Expected Results:**
- ✅ UI remains usable
- ✅ Text is readable
- ✅ No layout issues

## Test Checklist Summary

### Image Viewer
- [ ] Opens correctly from sent messages
- [ ] Opens correctly from received messages
- [ ] Pinch-to-zoom works (1x to 5x)
- [ ] Double-tap zoom works
- [ ] Pan works when zoomed
- [ ] Pan constraints work
- [ ] Swipe-to-dismiss works
- [ ] Swipe disabled when zoomed
- [ ] Download button works
- [ ] Download progress shows
- [ ] Downloaded images in Downloads folder
- [ ] Close button works

### Document Download
- [ ] PDF downloads and opens
- [ ] Word documents download and open
- [ ] Excel files download and open
- [ ] Text files download and open
- [ ] ZIP files download and open
- [ ] Progress dialog shows
- [ ] Files saved to Downloads
- [ ] Handles no app to open file
- [ ] Error handling works

### Error Scenarios
- [ ] Network failure handled
- [ ] Corrupted files handled
- [ ] Storage full handled
- [ ] No app to open file handled

## Known Issues
None at this time.

## Performance Benchmarks
- Image viewer open time: < 1 second
- Image load time: < 3 seconds (good connection)
- Zoom gesture response: < 16ms (60fps)
- Download progress updates: Every 100ms

## Conclusion
All features implemented and ready for testing. Follow this guide systematically to ensure complete coverage of all functionality.
