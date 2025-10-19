# Task 16: Gemini API Configuration - Testing Guide

## 🧪 Test Overview

This guide covers testing the Gemini API configuration to ensure proper setup and connectivity.

## 📋 Pre-Test Checklist

- [ ] API key obtained from https://makersuite.google.com/app/apikey
- [ ] API key added to `local.properties`
- [ ] Gradle project synced
- [ ] No build errors

## 🔬 Automated Tests

### Test Suite: GeminiAPIConnectivityTest

Location: `app/src/test/java/com/example/loginandregistration/GeminiAPIConnectivityTest.kt`

### Test 1: API Key Configuration ✅

**Purpose**: Verify API key is properly configured

**Command**:
```bash
./gradlew test --tests GeminiAPIConnectivityTest."test API key is configured"
```

**Expected Result**:
```
✅ PASSED
- API key is not null
- API key is not the placeholder value
- API key is not empty
```

**What It Tests**:
- BuildConfig.GEMINI_API_KEY exists
- Value is not "your_gemini_api_key_here"
- Value is not empty string

**Troubleshooting**:
```
❌ FAILED: "API key should be configured in local.properties"
→ Solution: Add GEMINI_API_KEY to local.properties
→ Sync Gradle and rebuild
```

---

### Test 2: API Connectivity ✅

**Purpose**: Verify API can be reached and responds

**Command**:
```bash
./gradlew test --tests GeminiAPIConnectivityTest."test API connectivity with simple request"
```

**Expected Result**:
```
✅ PASSED
✅ API connectivity test passed
📝 Response: [AI response text]
```

**What It Tests**:
- HTTP request succeeds
- Response is received
- Response contains valid content
- JSON parsing works

**Troubleshooting**:
```
⚠️ Skipping API connectivity test - API key not configured
→ Solution: Configure API key in local.properties

❌ FAILED: "API call failed: 401 Unauthorized"
→ Solution: Check API key is correct and enabled

❌ FAILED: "API call failed: 429 Too Many Requests"
→ Solution: Rate limit exceeded, wait and retry
```

---

### Test 3: Error Handling ✅

**Purpose**: Verify graceful handling of invalid API keys

**Command**:
```bash
./gradlew test --tests GeminiAPIConnectivityTest."test error handling with invalid API key"
```

**Expected Result**:
```
✅ PASSED
✅ Error handling test passed
📝 Expected error: [Error message]
```

**What It Tests**:
- Invalid API key is rejected
- Error is caught and handled
- Error message is informative
- App doesn't crash

**Expected Behavior**:
- API call fails (as expected)
- Result.isFailure returns true
- Error message is not null

---

### Test 4: Response Parsing ✅

**Purpose**: Verify API responses are parsed correctly

**Command**:
```bash
./gradlew test --tests GeminiAPIConnectivityTest."test API response parsing"
```

**Expected Result**:
```
✅ PASSED
✅ API response parsing test passed
📝 Response: [Parsed assignment data]
```

**What It Tests**:
- Complex prompts are handled
- JSON responses are parsed
- Assignment data is extracted
- Content structure is valid

---

### Test 5: Conversation History ✅

**Purpose**: Verify multi-turn conversations work

**Command**:
```bash
./gradlew test --tests GeminiAPIConnectivityTest."test conversation history handling"
```

**Expected Result**:
```
✅ PASSED
✅ Conversation history test passed
📝 Response: [Context-aware response]
```

**What It Tests**:
- Conversation context is preserved
- Follow-up messages work
- History is properly formatted
- Context influences responses

---

## 🎯 Running All Tests

### Run Complete Test Suite
```bash
./gradlew test --tests GeminiAPIConnectivityTest
```

### Expected Output
```
GeminiAPIConnectivityTest > test API key is configured PASSED
GeminiAPIConnectivityTest > test API connectivity with simple request PASSED
GeminiAPIConnectivityTest > test error handling with invalid API key PASSED
GeminiAPIConnectivityTest > test API response parsing PASSED
GeminiAPIConnectivityTest > test conversation history handling PASSED

BUILD SUCCESSFUL in 15s
5 tests completed, 5 passed
```

---

## 🧪 Manual Testing

### Test 1: Verify BuildConfig Access

**Steps**:
1. Open `GeminiAssistantService.kt`
2. Add temporary log:
   ```kotlin
   Log.d("GeminiAPI", "API Key configured: ${BuildConfig.GEMINI_API_KEY.take(10)}...")
   ```
3. Run the app
4. Check Logcat for the log message

**Expected**: Log shows first 10 characters of API key

---

### Test 2: Test in AI Assistant UI

**Steps**:
1. Run the app
2. Navigate to AI Assistant
3. Send message: "Hello, can you help me?"
4. Wait for response

**Expected**:
- Loading indicator appears
- Response received within 5 seconds
- Response is relevant to message
- No error messages

**Troubleshooting**:
```
❌ "API Error: Invalid API key"
→ Check API key in local.properties

❌ "Network Error"
→ Check internet connection
→ Verify API endpoint is accessible

❌ "Timeout"
→ Check network speed
→ Retry the request
```

---

### Test 3: Test Assignment Creation

**Steps**:
1. Open AI Assistant
2. Send message:
   ```
   Create an assignment:
   - Title: Math Homework
   - Subject: Mathematics
   - Due: Tomorrow
   - Description: Complete chapter 5
   ```
3. Wait for response
4. Check if task is created

**Expected**:
- AI acknowledges request
- Task is created in database
- Task appears in tasks list
- All fields are populated

---

## 📊 Test Results Interpretation

### All Tests Pass ✅
```
Status: Configuration is correct
Action: Proceed to next task
```

### Some Tests Fail ⚠️
```
Status: Partial configuration
Action: Review failed tests
Action: Fix issues and retest
```

### All Tests Fail ❌
```
Status: Configuration incomplete
Action: Review setup steps
Action: Check API key
Action: Sync Gradle
```

---

## 🔍 Debugging Tests

### Enable Verbose Logging

Add to test:
```kotlin
@Before
fun setup() {
    // Enable OkHttp logging
    val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()
}
```

### Check API Request/Response

Add logging in `GeminiAssistantService.kt`:
```kotlin
private suspend fun callGeminiAPI(prompt: String): AIResponse {
    Log.d("GeminiAPI", "Request: $prompt")
    val response = client.newCall(request).execute()
    val body = response.body?.string()
    Log.d("GeminiAPI", "Response: $body")
    return parseGeminiResponse(body)
}
```

---

## 🐛 Common Test Failures

### Failure: "API key should be configured"
```
Cause: API key not set or still placeholder
Fix: 
1. Open local.properties
2. Set GEMINI_API_KEY=your_actual_key
3. Sync Gradle
4. Rerun tests
```

### Failure: "API call failed: 401"
```
Cause: Invalid or expired API key
Fix:
1. Verify API key in Google Cloud Console
2. Generate new API key if needed
3. Update local.properties
4. Rerun tests
```

### Failure: "API call failed: 429"
```
Cause: Rate limit exceeded
Fix:
1. Wait 1-2 minutes
2. Rerun tests
3. Consider implementing rate limiting
```

### Failure: "Connection timeout"
```
Cause: Network issues or slow connection
Fix:
1. Check internet connection
2. Increase timeout in OkHttpClient
3. Retry the test
```

---

## 📈 Performance Benchmarks

### Expected Response Times

| Test | Expected Time | Acceptable Range |
|------|---------------|------------------|
| API Key Check | < 1ms | Instant |
| Simple Request | 2-5s | 1-10s |
| Error Handling | < 1s | Instant |
| Response Parsing | 2-5s | 1-10s |
| Conversation | 3-7s | 2-15s |

### If Tests Are Slow
- Check network speed
- Verify API endpoint latency
- Consider caching responses
- Implement request timeouts

---

## ✅ Test Completion Checklist

- [ ] All 5 automated tests pass
- [ ] BuildConfig access verified
- [ ] Manual UI test successful
- [ ] Assignment creation works
- [ ] Error handling verified
- [ ] Response times acceptable
- [ ] No memory leaks detected
- [ ] Logs show correct behavior

---

## 🎯 Success Criteria

### Configuration Complete When:
1. ✅ All automated tests pass
2. ✅ API key is properly configured
3. ✅ API connectivity verified
4. ✅ Error handling works
5. ✅ Response parsing successful
6. ✅ Manual tests pass
7. ✅ No security issues
8. ✅ Documentation complete

---

## 📝 Test Report Template

```markdown
# Gemini API Configuration Test Report

**Date**: [Date]
**Tester**: [Name]
**Environment**: [Debug/Release]

## Automated Tests
- [ ] API Key Configuration: PASS/FAIL
- [ ] API Connectivity: PASS/FAIL
- [ ] Error Handling: PASS/FAIL
- [ ] Response Parsing: PASS/FAIL
- [ ] Conversation History: PASS/FAIL

## Manual Tests
- [ ] BuildConfig Access: PASS/FAIL
- [ ] UI Integration: PASS/FAIL
- [ ] Assignment Creation: PASS/FAIL

## Issues Found
[List any issues]

## Notes
[Additional observations]

## Status
[COMPLETE / NEEDS WORK]
```

---

## 🚀 Next Steps After Testing

1. ✅ All tests pass → Mark task complete
2. ⚠️ Some tests fail → Fix issues and retest
3. ❌ All tests fail → Review configuration
4. 📝 Document any issues found
5. 🎯 Proceed to Task 17

---

## 📚 Additional Resources

- [Gemini API Documentation](https://ai.google.dev/docs)
- [OkHttp Testing Guide](https://square.github.io/okhttp/)
- [Gson Documentation](https://github.com/google/gson)
- [Android Testing Guide](https://developer.android.com/training/testing)
