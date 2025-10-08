# Task 5 Testing Guide: User Search for Direct Messages

## Prerequisites
- App is installed and running on a device/emulator
- You have at least one other user account registered in the system
- You are logged in to the app

## Test Scenarios

### Test 1: Open User Search Dialog
**Steps:**
1. Open the app
2. Navigate to the Chat tab (bottom navigation)
3. Tap the blue FAB (+) button in the bottom-right corner

**Expected Result:**
- UserSearchDialog opens in full screen
- Toolbar shows "New Direct Message" title
- Search field is visible with placeholder text "Search by name or email..."
- Cancel button is visible in the toolbar
- Search field automatically has focus

**Status:** ⬜ Pass / ⬜ Fail

---

### Test 2: Search by Display Name
**Steps:**
1. Open UserSearchDialog (see Test 1)
2. Type a user's display name in the search field
3. Wait for results to appear

**Expected Result:**
- Loading indicator appears briefly
- Search results appear showing matching users
- Each result shows:
  - Profile picture or avatar with initials
  - User's display name (bold)
  - User's email address
- Current user is NOT in the results

**Status:** ⬜ Pass / ⬜ Fail

---

### Test 3: Search by Email
**Steps:**
1. Open UserSearchDialog
2. Type a user's email address in the search field
3. Wait for results to appear

**Expected Result:**
- Loading indicator appears briefly
- Search results appear showing matching users
- Results include users whose email matches the query

**Status:** ⬜ Pass / ⬜ Fail

---

### Test 4: Search Debouncing
**Steps:**
1. Open UserSearchDialog
2. Type quickly: "j" then "o" then "h" then "n"
3. Observe the loading indicator

**Expected Result:**
- Loading indicator does NOT appear after each letter
- Loading indicator appears only after you stop typing for ~300ms
- Only one search query is executed (not 4 separate queries)

**Status:** ⬜ Pass / ⬜ Fail

---

### Test 5: Empty Search Results
**Steps:**
1. Open UserSearchDialog
2. Type a name that doesn't exist (e.g., "zzzzzzzzz")
3. Wait for results

**Expected Result:**
- Loading indicator appears briefly
- Empty state appears showing:
  - Search icon (grayed out)
  - "No users found" title
  - "Try searching with a different name or email" message
- No search results are shown

**Status:** ⬜ Pass / ⬜ Fail

---

### Test 6: Create New Direct Chat
**Steps:**
1. Open UserSearchDialog
2. Search for a user you have NOT chatted with before
3. Tap on the user in the search results
4. Wait for navigation

**Expected Result:**
- Loading indicator appears briefly
- ChatRoomActivity opens
- Toolbar shows the selected user's name
- Chat is empty (no messages yet)
- You can type and send a message
- UserSearchDialog is dismissed

**Status:** ⬜ Pass / ⬜ Fail

---

### Test 7: Open Existing Direct Chat
**Steps:**
1. Create a direct chat with a user (see Test 6)
2. Send at least one message
3. Go back to Chat tab
4. Open UserSearchDialog again
5. Search for the same user
6. Tap on the user

**Expected Result:**
- Loading indicator appears briefly
- ChatRoomActivity opens
- The EXISTING chat is opened (not a new one)
- Previous messages are visible
- UserSearchDialog is dismissed

**Status:** ⬜ Pass / ⬜ Fail

---

### Test 8: Profile Pictures Display
**Steps:**
1. Open UserSearchDialog
2. Search for users who have profile pictures
3. Search for users who don't have profile pictures

**Expected Result:**
- Users WITH profile pictures:
  - Profile picture loads and displays
  - Image is circular
- Users WITHOUT profile pictures:
  - Avatar with initials displays
  - Avatar has a colored background
  - Initials are white and centered
  - Same user always has same color

**Status:** ⬜ Pass / ⬜ Fail

---

### Test 9: Cancel Dialog
**Steps:**
1. Open UserSearchDialog
2. Type something in the search field
3. Tap the "Cancel" button in the toolbar

**Expected Result:**
- Dialog closes immediately
- Returns to Chat tab
- No chat is created

**Status:** ⬜ Pass / ⬜ Fail

---

### Test 10: Clear Search
**Steps:**
1. Open UserSearchDialog
2. Type a search query and see results
3. Clear the search field (delete all text)

**Expected Result:**
- Search results disappear
- Empty state does NOT appear
- Screen shows blank RecyclerView

**Status:** ⬜ Pass / ⬜ Fail

---

### Test 11: Error Handling - Network Error
**Steps:**
1. Turn on airplane mode
2. Open UserSearchDialog
3. Type a search query

**Expected Result:**
- Loading indicator appears
- After timeout, error toast appears: "Error searching users: [error message]"
- Empty state appears

**Status:** ⬜ Pass / ⬜ Fail

---

### Test 12: Multiple Search Queries
**Steps:**
1. Open UserSearchDialog
2. Search for "john"
3. Wait for results
4. Clear and search for "jane"
5. Wait for results
6. Clear and search for "bob"

**Expected Result:**
- Each search returns appropriate results
- Previous results are cleared before new results appear
- No duplicate results
- No crashes or errors

**Status:** ⬜ Pass / ⬜ Fail

---

### Test 13: Long User Names and Emails
**Steps:**
1. Open UserSearchDialog
2. Search for users with very long names or emails

**Expected Result:**
- Long names are truncated with ellipsis (...)
- Long emails are truncated with ellipsis (...)
- Text doesn't overflow the card
- Layout remains intact

**Status:** ⬜ Pass / ⬜ Fail

---

### Test 14: Rapid User Selection
**Steps:**
1. Open UserSearchDialog
2. Search for a user
3. Quickly tap on a user result multiple times

**Expected Result:**
- Only one chat is created
- Only one ChatRoomActivity opens
- No duplicate chats
- No crashes

**Status:** ⬜ Pass / ⬜ Fail

---

### Test 15: Back Button Behavior
**Steps:**
1. Open UserSearchDialog
2. Press the device back button

**Expected Result:**
- Dialog closes
- Returns to Chat tab
- No chat is created

**Status:** ⬜ Pass / ⬜ Fail

---

## Edge Cases to Test

### Edge Case 1: Search for Self
**Expected:** Current user should NOT appear in search results

**Status:** ⬜ Pass / ⬜ Fail

---

### Edge Case 2: Special Characters in Search
**Test:** Search with special characters like "@", ".", "-"

**Expected:** Search should work correctly with email addresses containing special characters

**Status:** ⬜ Pass / ⬜ Fail

---

### Edge Case 3: Case Sensitivity
**Test:** Search for "JOHN" vs "john" vs "John"

**Expected:** Search should be case-insensitive and return same results

**Status:** ⬜ Pass / ⬜ Fail

---

### Edge Case 4: Partial Name Match
**Test:** Search for "jo" when user is "John Doe"

**Expected:** User should appear in results (prefix matching)

**Status:** ⬜ Pass / ⬜ Fail

---

### Edge Case 5: Empty Database
**Test:** Search when no other users exist in the system

**Expected:** Empty state appears with "No users found" message

**Status:** ⬜ Pass / ⬜ Fail

---

## Performance Tests

### Performance 1: Search Response Time
**Test:** Measure time from typing to results appearing

**Expected:** Results should appear within 1-2 seconds on good network

**Status:** ⬜ Pass / ⬜ Fail

---

### Performance 2: Scroll Performance
**Test:** Search for query that returns many results, scroll through list

**Expected:** Smooth scrolling with no lag or stuttering

**Status:** ⬜ Pass / ⬜ Fail

---

### Performance 3: Image Loading
**Test:** Search for users with profile pictures, observe loading

**Expected:** Images load progressively without blocking UI

**Status:** ⬜ Pass / ⬜ Fail

---

## UI/UX Tests

### UI 1: Material Design Compliance
**Check:**
- Proper elevation on cards
- Correct colors (primary blue, text colors)
- Proper spacing and padding
- Rounded corners on cards and search field

**Status:** ⬜ Pass / ⬜ Fail

---

### UI 2: Accessibility
**Check:**
- All interactive elements are tappable (min 48dp)
- Text is readable (proper contrast)
- Icons have content descriptions

**Status:** ⬜ Pass / ⬜ Fail

---

### UI 3: Responsive Layout
**Test:** Rotate device to landscape mode

**Expected:** Layout adjusts appropriately, all elements visible

**Status:** ⬜ Pass / ⬜ Fail

---

## Integration Tests

### Integration 1: Chat List Update
**Steps:**
1. Create a new direct chat via user search
2. Send a message
3. Go back to Chat tab

**Expected:** New chat appears in the chat list

**Status:** ⬜ Pass / ⬜ Fail

---

### Integration 2: Notification Integration
**Steps:**
1. Create direct chat with User A
2. Have User A send you a message
3. Receive notification

**Expected:** Notification works for newly created direct chats

**Status:** ⬜ Pass / ⬜ Fail

---

## Test Summary

**Total Tests:** 25
**Passed:** ___
**Failed:** ___
**Pass Rate:** ___%

## Issues Found

| Issue # | Description | Severity | Status |
|---------|-------------|----------|--------|
| 1 | | | |
| 2 | | | |
| 3 | | | |

## Notes

- Test on multiple devices/emulators if possible
- Test with different Android versions (API 23+)
- Test with different screen sizes
- Test with slow network conditions
- Test with multiple users in the database

## Sign-off

**Tester Name:** _______________
**Date:** _______________
**Build Version:** _______________
**Device/Emulator:** _______________
**Android Version:** _______________

**Overall Status:** ⬜ Approved / ⬜ Needs Fixes
