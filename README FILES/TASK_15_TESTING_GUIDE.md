# Task 15: AI Task Creation - Testing Guide

## Prerequisites

### 1. Get Gemini API Key
1. Go to [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Sign in with your Google account
3. Click "Create API Key"
4. Copy the generated API key

### 2. Configure API Key
Add to `local.properties` in project root:
```properties
GEMINI_API_KEY=your_actual_api_key_here
```

### 3. Rebuild Project
```bash
./gradlew clean build
```

## Test Scenarios

### Scenario 1: Basic Task Creation with AI

**Steps:**
1. Launch the app and sign in
2. Navigate to Tasks tab
3. Click the "+" (Add Task) button
4. In the task creation dialog, click "Create with AI"
5. Enter prompt: "Create a math homework assignment due next Friday"
6. Click "Create with AI"

**Expected Results:**
- ✅ Loading indicator appears
- ✅ AI processes the request (2-5 seconds)
- ✅ Success message: "Task created successfully with AI assistance!"
- ✅ Both dialogs close automatically
- ✅ New task appears in the task list with:
  - Title: "Math Homework Assignment" (or similar)
  - Subject: "Mathematics"
  - Due date: Next Friday
  - Priority: Medium
  - Category: Assignment

### Scenario 2: Complex Task with Multiple Details

**Steps:**
1. Click "+" to add task
2. Click "Create with AI"
3. Enter prompt: "I need to finish my high priority science project about photosynthesis by tomorrow"
4. Click "Create with AI"

**Expected Results:**
- ✅ Task created with:
  - Title includes "science project" and "photosynthesis"
  - Subject: "Science"
  - Due date: Tomorrow
  - Priority: High
  - Description mentions photosynthesis

### Scenario 3: Minimal Information Prompt

**Steps:**
1. Click "+" to add task
2. Click "Create with AI"
3. Enter prompt: "Study for exam"
4. Click "Create with AI"

**Expected Results:**
- ✅ Task created with smart defaults:
  - Title: "Study for exam"
  - Subject: "General"
  - Due date: 7 days from now
  - Priority: Medium

### Scenario 4: Missing API Key

**Steps:**
1. Remove or comment out `GEMINI_API_KEY` from `local.properties`
2. Rebuild the app
3. Click "+" to add task
4. Click "Create with AI"

**Expected Results:**
- ✅ Toast message: "AI Assistant is not configured. Please add GEMINI_API_KEY to local.properties"
- ✅ Dialog remains open
- ✅ User can still create task manually

### Scenario 5: Empty Prompt

**Steps:**
1. Click "+" to add task
2. Click "Create with AI"
3. Leave the prompt field empty
4. Click "Create with AI"

**Expected Results:**
- ✅ Toast message: "Please describe the task you want to create"
- ✅ Dialog remains open
- ✅ No API call made

### Scenario 6: Network Error

**Steps:**
1. Enable airplane mode or disconnect internet
2. Click "+" to add task
3. Click "Create with AI"
4. Enter any prompt
5. Click "Create with AI"

**Expected Results:**
- ✅ Loading indicator appears
- ✅ After timeout, error message appears
- ✅ Snackbar shows: "Failed to communicate with AI: [error details]"
- ✅ Dialog remains open for retry

### Scenario 7: Rapid Multiple Submissions

**Steps:**
1. Click "+" to add task
2. Click "Create with AI"
3. Enter prompt
4. Click "Create with AI" multiple times rapidly

**Expected Results:**
- ✅ Button becomes disabled after first click
- ✅ Only one API call is made
- ✅ Loading indicator shows
- ✅ Button re-enables after completion

### Scenario 8: Cancel During Processing

**Steps:**
1. Click "+" to add task
2. Click "Create with AI"
3. Enter prompt
4. Click "Create with AI"
5. Immediately click "Cancel"

**Expected Results:**
- ✅ Dialog closes
- ✅ API call may complete in background
- ✅ No task is created (dialog closed before completion)
- ✅ No error messages shown

### Scenario 9: Various Task Types

**Test Prompts:**

1. **Personal Task:**
   - Prompt: "Remind me to call mom tomorrow"
   - Expected: Personal category, due tomorrow

2. **Group Task:**
   - Prompt: "Create a group meeting for project discussion next Monday"
   - Expected: Group category, due next Monday

3. **Assignment:**
   - Prompt: "English essay about Shakespeare due in 2 weeks"
   - Expected: Assignment category, subject: English, due in 14 days

4. **Urgent Task:**
   - Prompt: "URGENT: Submit report by end of day"
   - Expected: High priority, due today

5. **Long-term Task:**
   - Prompt: "Prepare for final exams in December"
   - Expected: Due date in December

### Scenario 10: AI Response Variations

**Steps:**
1. Try different phrasings for the same task:
   - "Create math homework"
   - "I need to do my math homework"
   - "Add a task for math homework"
   - "Math homework assignment"

**Expected Results:**
- ✅ All variations create similar tasks
- ✅ AI understands different phrasings
- ✅ Consistent task structure

## Performance Testing

### Response Time
- **Expected**: 2-5 seconds for AI response
- **Acceptable**: Up to 10 seconds
- **Timeout**: 30 seconds

### Load Testing
1. Create 10 tasks rapidly using AI
2. Verify all tasks are created correctly
3. Check for memory leaks
4. Verify UI remains responsive

## Error Scenarios

### 1. Invalid API Key
**Setup:** Use incorrect API key
**Expected:** Error message about authentication failure

### 2. API Quota Exceeded
**Setup:** Exceed Gemini API quota
**Expected:** Error message about quota limits

### 3. Malformed Response
**Setup:** (Difficult to test manually)
**Expected:** Graceful error handling, fallback to defaults

### 4. Firestore Permission Error
**Setup:** Sign out during task creation
**Expected:** Permission denied error with clear message

## Verification Checklist

### UI/UX
- [ ] "Create with AI" button is visible and styled correctly
- [ ] AI prompt dialog has clear instructions
- [ ] Loading indicator shows during processing
- [ ] Success message is clear and visible
- [ ] Error messages are user-friendly
- [ ] Dialogs close automatically on success
- [ ] Buttons are disabled during processing

### Functionality
- [ ] AI correctly parses task details
- [ ] Tasks are created in Firestore
- [ ] Tasks appear in the list immediately
- [ ] Real-time updates work correctly
- [ ] All task fields are populated
- [ ] Due dates are calculated correctly
- [ ] Priority levels are set appropriately

### Error Handling
- [ ] Missing API key is detected
- [ ] Network errors are handled gracefully
- [ ] Empty prompts are rejected
- [ ] Invalid responses are handled
- [ ] Firestore errors are caught
- [ ] User receives clear feedback

### Edge Cases
- [ ] Very long prompts (500+ characters)
- [ ] Special characters in prompts
- [ ] Emojis in prompts
- [ ] Multiple languages (if supported)
- [ ] Ambiguous prompts
- [ ] Prompts without clear task intent

## Debugging

### Enable Logging
Check Android Logcat for:
```
Tag: TasksViewModel
Tag: GeminiAssistantService
```

### Common Log Messages
- "Sending prompt to AI: [prompt]"
- "AI Response: [response]"
- "AI suggested creating an assignment"
- "Task created from AI: [title]"
- "Failed to create task from AI"

### Troubleshooting

**Problem:** No response from AI
- Check internet connection
- Verify API key is correct
- Check Logcat for errors
- Verify Gemini API is accessible

**Problem:** Task not created
- Check Firestore security rules
- Verify user is authenticated
- Check task data in logs
- Verify repository is working

**Problem:** Wrong task details
- Check AI response in logs
- Verify parsing logic
- Test with clearer prompts
- Check date parsing

## Test Data

### Sample Prompts for Testing
```
1. "Create a math homework assignment due next Friday"
2. "I need to study for my biology exam on Monday"
3. "High priority: finish science project by tomorrow"
4. "English literature essay due in 2 weeks"
5. "Call dentist to schedule appointment"
6. "Group meeting for project discussion next Tuesday"
7. "Prepare presentation for client meeting"
8. "Review code changes before deployment"
9. "Buy groceries for the week"
10. "Complete online course module by weekend"
```

### Expected Task Structures
Each task should have:
- ✅ Non-empty title
- ✅ Relevant description
- ✅ Appropriate subject
- ✅ Valid due date (future date)
- ✅ Priority (low/medium/high)
- ✅ Category (personal/group/assignment)
- ✅ Status: "pending"
- ✅ User ID
- ✅ Created timestamp

## Acceptance Criteria

### Must Have:
- ✅ "Create with AI" button in task dialog
- ✅ AI prompt dialog with text input
- ✅ Loading state during processing
- ✅ Success message on completion
- ✅ Error handling for all failure cases
- ✅ Task created in Firestore
- ✅ Task appears in list

### Should Have:
- ✅ Clear user instructions
- ✅ Helpful placeholder text
- ✅ Disabled buttons during processing
- ✅ Automatic dialog dismissal
- ✅ Comprehensive error messages

### Nice to Have:
- ⚪ Conversation history
- ⚪ Task preview before creation
- ⚪ Edit AI-generated task before saving
- ⚪ Multiple task creation from one prompt

## Regression Testing

After implementing this feature, verify:
- [ ] Manual task creation still works
- [ ] Existing tasks are not affected
- [ ] Task list updates correctly
- [ ] Task statistics are accurate
- [ ] Other task operations work (edit, delete)
- [ ] Navigation between screens works
- [ ] App doesn't crash on any screen

## Performance Metrics

### Target Metrics:
- API Response Time: < 5 seconds (average)
- Task Creation Time: < 1 second (after AI response)
- UI Responsiveness: No freezing
- Memory Usage: No significant increase
- Battery Impact: Minimal

### Monitoring:
- Check Android Profiler for memory leaks
- Monitor network requests
- Track API call frequency
- Measure battery consumption

## Conclusion

This testing guide covers all aspects of the AI task creation feature. Follow these scenarios systematically to ensure the feature works correctly and provides a great user experience.

**Status**: Ready for testing after API key configuration
**Priority**: High
**Estimated Testing Time**: 2-3 hours for comprehensive testing
