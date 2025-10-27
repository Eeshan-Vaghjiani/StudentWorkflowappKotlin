# Task 40: Input Validation Testing Guide

## Overview
This guide provides step-by-step instructions to test all input validation and sanitization features implemented in Task 40.

## Prerequisites
- App installed on device or emulator
- Logged in with a test account
- Access to test files (images, documents) of various sizes

## Test Scenarios

### 1. Message Text Validation

#### Test 1.1: Empty Message
**Steps:**
1. Open any chat
2. Leave message input field empty
3. Tap send button

**Expected Result:**
- Message should not be sent
- No error shown (empty messages are silently ignored)

#### Test 1.2: Very Long Message
**Steps:**
1. Open any chat
2. Type or paste a message with more than 5000 characters
3. Tap send button

**Expected Result:**
- Error message displayed: "Message is too long (max 5000 characters)"
- Error shown both as Toast and inline on EditText
- Message not sent

#### Test 1.3: Message with Special Characters
**Steps:**
1. Open any chat
2. Type message with HTML/script characters: `<script>alert('test')</script>`
3. Send message

**Expected Result:**
- Message sent successfully
- Special characters removed/sanitized
- Displayed message should be safe

#### Test 1.4: Normal Message
**Steps:**
1. Open any chat
2. Type a normal message (e.g., "Hello, how are you?")
3. Send message

**Expected Result:**
- Message sent successfully
- Appears in chat immediately
- No errors

---

### 2. Image Upload Validation

#### Test 2.1: Large Image (> 5MB)
**Steps:**
1. Open any chat
2. Tap attachment button
3. Select "Gallery"
4. Choose an image larger than 5MB (uncompressed)

**Expected Result:**
- After compression, if still > 5MB: Error message "Image is too large (max 5MB)"
- Upload fails
- User can try again with smaller image

#### Test 2.2: Normal Image
**Steps:**
1. Open any chat
2. Tap attachment button
3. Select "Gallery"
4. Choose a normal image (< 5MB)

**Expected Result:**
- Progress dialog shows upload progress
- Image compressed automatically
- Image sent successfully
- Appears in chat

#### Test 2.3: Profile Picture Upload
**Steps:**
1. Go to Profile screen
2. Tap profile picture or edit button
3. Select "Choose from Gallery"
4. Choose an image

**Expected Result:**
- Image compressed to under 500KB
- Upload progress shown
- Profile picture updated
- New picture visible throughout app

---

### 3. Document Upload Validation

#### Test 3.1: Large Document (> 10MB)
**Steps:**
1. Open any chat
2. Tap attachment button
3. Select "Documents"
4. Choose a document larger than 10MB

**Expected Result:**
- Error message: "Document is too large (max 10MB)"
- Upload fails
- Clear error message with size limit

#### Test 3.2: Normal Document
**Steps:**
1. Open any chat
2. Tap attachment button
3. Select "Documents"
4. Choose a PDF or document < 10MB

**Expected Result:**
- Progress dialog shows upload progress
- Document sent successfully
- Appears in chat with file icon and name
- Can tap to download and open

---

### 4. Email Validation

#### Test 4.1: Invalid Email in User Search
**Steps:**
1. Open Chat screen
2. Tap "New Chat" or search icon
3. Type invalid email: `notanemail`
4. Wait for search

**Expected Result:**
- No validation error (allows partial name search)

**Steps (continued):**
5. Type invalid email with @: `notanemail@`
6. Wait for search

**Expected Result:**
- Error message: "Invalid email format"
- Error shown inline on search field
- No search performed

#### Test 4.2: Valid Email in User Search
**Steps:**
1. Open Chat screen
2. Tap "New Chat" or search icon
3. Type valid email: `test@example.com`
4. Wait for search

**Expected Result:**
- No validation error
- Search performed
- Results shown if user exists

#### Test 4.3: Invalid Email in Add Member
**Steps:**
1. Open a group you own/admin
2. Go to Members section
3. Tap "Add Member"
4. Enter invalid email: `notanemail@`
5. Tap Add

**Expected Result:**
- Error message: "Invalid email format"
- Member not added
- Can try again with valid email

#### Test 4.4: Valid Email in Add Member
**Steps:**
1. Open a group you own/admin
2. Go to Members section
3. Tap "Add Member"
4. Enter valid email of existing user
5. Tap Add

**Expected Result:**
- No validation error
- Member added successfully
- Appears in member list

---

### 5. Group Name Validation

#### Test 5.1: Too Short Group Name
**Steps:**
1. Go to Groups screen
2. Tap "Create Group"
3. Enter name with < 3 characters: `AB`
4. Tap Create

**Expected Result:**
- Error message: "Group name must be at least 3 characters"
- Group not created

#### Test 5.2: Too Long Group Name
**Steps:**
1. Go to Groups screen
2. Tap "Create Group"
3. Enter name with > 50 characters
4. Tap Create

**Expected Result:**
- Error message: "Group name is too long (max 50 characters)"
- Group not created

#### Test 5.3: Invalid Characters in Group Name
**Steps:**
1. Go to Groups screen
2. Tap "Create Group"
3. Enter name with special characters: `Test@Group#123`
4. Tap Create

**Expected Result:**
- Error message: "Group name can only contain letters, numbers, spaces, hyphens, and underscores"
- Group not created

#### Test 5.4: Valid Group Name
**Steps:**
1. Go to Groups screen
2. Tap "Create Group"
3. Enter valid name: `Study Group 2024`
4. Tap Create

**Expected Result:**
- No validation error
- Group created successfully
- Appears in group list

#### Test 5.5: Update Group Name (Invalid)
**Steps:**
1. Open a group you own
2. Go to Settings
3. Tap "Edit Name"
4. Enter invalid name (too short, too long, or special chars)
5. Save

**Expected Result:**
- Error message with specific validation failure
- Name not updated

#### Test 5.6: Update Group Name (Valid)
**Steps:**
1. Open a group you own
2. Go to Settings
3. Tap "Edit Name"
4. Enter valid new name
5. Save

**Expected Result:**
- No validation error
- Name updated successfully
- New name visible throughout app

---

### 6. Group Description Validation

#### Test 6.1: Too Long Description
**Steps:**
1. Go to Groups screen
2. Tap "Create Group"
3. Enter valid name
4. Enter description with > 500 characters
5. Tap Create

**Expected Result:**
- Error message: "Description is too long (max 500 characters)"
- Group not created

#### Test 6.2: Valid Description
**Steps:**
1. Go to Groups screen
2. Tap "Create Group"
3. Enter valid name
4. Enter description < 500 characters
5. Tap Create

**Expected Result:**
- No validation error
- Group created with description
- Description visible in group details

#### Test 6.3: Empty Description (Optional)
**Steps:**
1. Go to Groups screen
2. Tap "Create Group"
3. Enter valid name
4. Leave description empty
5. Tap Create

**Expected Result:**
- No validation error
- Group created without description
- Works fine (description is optional)

---

### 7. Input Sanitization

#### Test 7.1: HTML in Message
**Steps:**
1. Open any chat
2. Type message: `<b>Bold text</b>`
3. Send message

**Expected Result:**
- Message sent
- HTML tags removed or escaped
- Displayed safely without rendering HTML

#### Test 7.2: Script in Message
**Steps:**
1. Open any chat
2. Type message: `<script>alert('XSS')</script>`
3. Send message

**Expected Result:**
- Message sent
- Script tags removed
- No script execution
- Safe display

#### Test 7.3: Multiple Spaces in Group Name
**Steps:**
1. Create group with name: `Test    Group    2024` (multiple spaces)
2. Tap Create

**Expected Result:**
- Multiple spaces normalized to single spaces
- Group name: `Test Group 2024`
- Sanitized properly

---

## Edge Cases

### Edge Case 1: Exactly at Limit
**Test:** Send message with exactly 5000 characters
**Expected:** Should succeed (at limit, not over)

### Edge Case 2: Unicode Characters
**Test:** Send message with emojis and unicode: `Hello 👋 世界`
**Expected:** Should work correctly, unicode counted properly

### Edge Case 3: Whitespace Only
**Test:** Send message with only spaces/tabs
**Expected:** Treated as empty, not sent

### Edge Case 4: File Size After Compression
**Test:** Upload 6MB image that compresses to 3MB
**Expected:** Should succeed (validated after compression)

---

## Automated Testing Commands

If you have unit tests set up, run:

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests InputValidatorTest

# Run with coverage
./gradlew testDebugUnitTest jacocoTestReport
```

---

## Validation Summary Checklist

Use this checklist to track your testing progress:

### Message Validation
- [ ] Empty message (ignored)
- [ ] Message > 5000 chars (error)
- [ ] Message with special chars (sanitized)
- [ ] Normal message (success)

### File Upload Validation
- [ ] Image > 5MB (error)
- [ ] Normal image (success)
- [ ] Document > 10MB (error)
- [ ] Normal document (success)
- [ ] Profile picture upload (success)

### Email Validation
- [ ] Invalid email in search (error)
- [ ] Valid email in search (success)
- [ ] Invalid email in add member (error)
- [ ] Valid email in add member (success)

### Group Name Validation
- [ ] Name < 3 chars (error)
- [ ] Name > 50 chars (error)
- [ ] Name with special chars (error)
- [ ] Valid name (success)
- [ ] Update with invalid name (error)
- [ ] Update with valid name (success)

### Group Description Validation
- [ ] Description > 500 chars (error)
- [ ] Valid description (success)
- [ ] Empty description (success)

### Input Sanitization
- [ ] HTML in message (sanitized)
- [ ] Script in message (sanitized)
- [ ] Multiple spaces (normalized)

### Edge Cases
- [ ] Exactly at limit (success)
- [ ] Unicode characters (success)
- [ ] Whitespace only (ignored)
- [ ] File size after compression (validated correctly)

---

## Reporting Issues

If you find any validation issues during testing:

1. **Document the issue:**
   - What you did (steps)
   - What you expected
   - What actually happened
   - Screenshots if applicable

2. **Check the logs:**
   - Use `adb logcat` to see error messages
   - Look for tags: `ChatRepository`, `StorageRepository`, `InputValidator`

3. **Verify the validation:**
   - Check `InputValidator.kt` for the validation rule
   - Ensure the rule is being called in the repository/activity

---

## Success Criteria

All tests should pass with:
- ✅ Appropriate error messages for invalid input
- ✅ Successful operations for valid input
- ✅ No crashes or unexpected behavior
- ✅ Clear user feedback (inline errors, toasts)
- ✅ Sanitized input stored safely in database

---

## Notes

- Some validations happen on the client side (UI) for immediate feedback
- All validations also happen on the repository level for security
- Firestore security rules provide an additional layer of validation
- File size limits are enforced before upload to save bandwidth
