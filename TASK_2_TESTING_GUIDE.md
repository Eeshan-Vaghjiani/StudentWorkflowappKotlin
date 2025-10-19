# Task 2: Gemini AI Testing Guide

## Quick Testing Steps

### Prerequisites
- Ensure you have a valid Gemini API key configured
- App is built and installed on a test device
- Internet connection available

### Test 1: Basic AI Conversation ✅
**Purpose:** Verify the AI model is accessible and responding

1. Open the TeamSync app
2. Navigate to AI Assistant
3. Send message: `"Hello, can you help me with my studies?"`
4. **Expected:** AI responds with a helpful message (no 404 error)
5. **Verify in Logcat:**
   ```
   D/GeminiAssistantService: Sending message to Gemini API: Hello...
   D/GeminiAssistantService: Received response from Gemini API
   ```

### Test 2: Assignment Creation ✅
**Purpose:** Verify AI can create assignments with the new model

1. In AI Assistant, send: `"Create a math assignment on calculus due next Friday"`
2. **Expected:** AI responds with assignment details in JSON format
3. **Expected:** Assignment appears in your tasks list
4. **Verify:** Task has correct title, subject, and due date

### Test 3: Network Error Handling ✅
**Purpose:** Verify user-friendly error messages

1. Turn off WiFi and mobile data
2. Send a message to AI Assistant
3. **Expected:** See message: `"No internet connection. Please check your network and try again."`
4. Turn network back on
5. Retry the message
6. **Expected:** Message sends successfully

### Test 4: Invalid Request Handling ✅
**Purpose:** Verify error handling for malformed requests

1. This is automatically handled by the service
2. If a 400 error occurs, user sees: `"Invalid request. Please try rephrasing your message."`

### Test 5: Rate Limiting ✅
**Purpose:** Verify handling of API rate limits

1. Send 10+ messages rapidly to AI Assistant
2. If rate limited (429 error), **Expected:** `"Too many requests. Please wait a moment and try again."`
3. Wait 30 seconds
4. Try again - should work

### Test 6: Long Response Handling ✅
**Purpose:** Verify increased token limit works

1. Ask: `"Explain the theory of relativity in detail"`
2. **Expected:** Receive a comprehensive response (not truncated)
3. **Note:** maxOutputTokens increased from 1024 to 2048

## Logcat Monitoring

### Filter for AI Service Logs
```bash
adb logcat -s GeminiAssistantService
```

### Success Pattern
```
D/GeminiAssistantService: Sending message to Gemini API: [message]
D/GeminiAssistantService: Received response from Gemini API
```

### Error Pattern (Should NOT see 404 anymore)
```
❌ OLD: E/GeminiAssistantService: API call failed: 404 - models/gemini-pro is not found
✅ NEW: D/GeminiAssistantService: Received response from Gemini API
```

## Error Message Verification

| Error Type | HTTP Code | Expected User Message |
|------------|-----------|----------------------|
| Bad Request | 400 | "Invalid request. Please try rephrasing your message." |
| Unauthorized | 401/403 | "AI service authentication failed. Please contact support." |
| Not Found | 404 | "AI model not found. The service may be temporarily unavailable." |
| Rate Limit | 429 | "Too many requests. Please wait a moment and try again." |
| Server Error | 500/502/503 | "AI service is temporarily unavailable. Please try again later." |
| Timeout | - | "Request timed out. Please check your internet connection and try again." |
| No Network | - | "No internet connection. Please check your network and try again." |
| Network I/O | - | "Network error occurred. Please check your connection and try again." |

## Build and Deploy

### Build Debug APK
```bash
cd [project-root]
./gradlew assembleDebug
```

### Install on Device
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Run and Monitor
```bash
adb logcat | grep -E "GeminiAssistantService|AI"
```

## Troubleshooting

### Issue: Still getting 404 errors
**Solution:** 
- Verify the code changes were saved
- Clean and rebuild: `./gradlew clean assembleDebug`
- Uninstall old app version from device
- Install fresh build

### Issue: No response from AI
**Solution:**
- Check API key is valid
- Verify internet connection
- Check Logcat for specific error messages
- Ensure Gemini API is enabled in Google Cloud Console

### Issue: "Authentication failed" message
**Solution:**
- Verify API key in app configuration
- Check API key has Gemini API permissions
- Ensure API key is not expired or revoked

## Success Criteria

✅ All tests pass without 404 errors
✅ User-friendly error messages appear for all error types
✅ AI responses are complete and not truncated
✅ Assignment creation works correctly
✅ Network errors are handled gracefully
✅ App doesn't crash on API errors

## Status: Ready for Testing

The implementation is complete and ready for manual testing. Follow the test cases above to verify all functionality works as expected.
