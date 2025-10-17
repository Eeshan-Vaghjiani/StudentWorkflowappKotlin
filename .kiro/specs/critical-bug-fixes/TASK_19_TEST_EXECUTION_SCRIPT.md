# Task 19: Test Execution Script

## Overview

This document provides commands and scripts to execute all automated tests for the critical bug fixes.

---

## Prerequisites

Before running tests, ensure:
1. ✓ Build errors are resolved
2. ✓ All dependencies are synced
3. ✓ Firebase configuration is complete
4. ✓ API keys are configured in `local.properties`

---

## Build Verification

First, verify the project builds successfully:

```bash
# Clean the project
./gradlew clean

# Build the project
./gradlew build

# If build fails, check for compilation errors
./gradlew build --stacktrace
```

**Expected Result:** Build should complete without errors.

---

## Running All Unit Tests

### Run All Tests
```bash
./gradlew test
```

### Run Tests with Detailed Output
```bash
./gradlew test --info
```

### Run Tests with HTML Report
```bash
./gradlew test
# Open: build/reports/tests/test/index.html
```

---

## Running Specific Test Classes

### Google Sign-In Flow Tests
```bash
./gradlew test --tests GoogleSignInFlowTest
```

**Expected Tests:**
- testSignInCancellation_DoesNotShowError
- testUserDocumentInitialization_ContainsAllRequiredFields
- testFcmTokenSaving_CalledAfterSuccessfulSignIn
- testErrorHandling_ShowsUserFriendlyMessages

### Group Creation and Display Tests
```bash
./gradlew test --tests GroupCreationAndDisplayTest
```

**Expected Tests:**
- test FirebaseGroup has correct field names
- test group creation initializes all required fields
- test group settings isPublic field
- test memberIds and members arrays stay in sync
- test group member roles
- test group no-arg constructor for Firestore
- test join code format

### Task Creation and Display Tests
```bash
./gradlew test --tests TaskCreationAndDisplayTest
```

**Expected Tests:**
- test FirebaseTask has all required fields initialized
- test FirebaseTask with all fields
- test task category validation
- test task status validation
- test task priority validation
- test task with group assignment
- test task without group assignment
- test task due date is in future
- test task completion updates status and timestamp
- test task filtering by category
- test task filtering by status
- test task filtering by user
- test task date conversion for calendar
- test multiple tasks on same date
- test task copy with updates
- test task with empty optional fields
- test task sorting by due date
- test task with null due date sorts last

### Chat Message Sending and Reading Tests
```bash
./gradlew test --tests ChatMessageSendingAndReadingTest
```

**Expected Tests:**
- test sendMessage handles permission errors gracefully
- test markMessagesAsRead returns Result type
- test updateTypingStatus returns Result type
- test OfflineMessageQueue queues messages
- test Message model has required status field
- test retryMessage method exists
- test processQueuedMessages method exists
- test retryPendingMessagesWithBackoff method exists
- test offline queue methods exist
- test message validation prevents empty messages
- test MessageStatus enum has all required states
- test error handling for permission denied
- test error handling for network errors
- test retry logic with exponential backoff
- test message queue persistence
- test concurrent message sending
- test read status update reliability
- test typing status non-critical error handling

### AI Assistant Tests
```bash
./gradlew test --tests GeminiAssistantServiceTest
./gradlew test --tests GeminiAPIConnectivityTest
```

---

## Running Tests by Package

### Run All Model Tests
```bash
./gradlew test --tests "com.example.loginandregistration.models.*"
```

### Run All Repository Tests
```bash
./gradlew test --tests "com.example.loginandregistration.repository.*"
```

### Run All ViewModel Tests
```bash
./gradlew test --tests "com.example.loginandregistration.viewmodels.*"
```

### Run All Utility Tests
```bash
./gradlew test --tests "com.example.loginandregistration.utils.*"
```

---

## Test Reports

### HTML Test Report
After running tests, view the HTML report:
```bash
# Windows
start build/reports/tests/test/index.html

# Linux/Mac
open build/reports/tests/test/index.html
```

### XML Test Results
Test results are also available in XML format:
```
build/test-results/test/*.xml
```

### Console Output
For immediate feedback, use:
```bash
./gradlew test --console=verbose
```

---

## Continuous Testing

### Watch Mode (Auto-run on changes)
```bash
./gradlew test --continuous
```

### Run Tests on File Save
Use your IDE's test runner with auto-run enabled.

---

## Test Coverage

### Generate Coverage Report
```bash
./gradlew testDebugUnitTestCoverage
```

### View Coverage Report
```bash
# Open: build/reports/coverage/test/debug/index.html
```

---

## Debugging Failed Tests

### Run Single Test Method
```bash
./gradlew test --tests "GoogleSignInFlowTest.testSignInCancellation_DoesNotShowError"
```

### Run Tests with Stack Trace
```bash
./gradlew test --stacktrace
```

### Run Tests with Debug Logging
```bash
./gradlew test --debug
```

### Run Tests with Gradle Scan
```bash
./gradlew test --scan
```

---

## Known Build Issues

### Issue: Firebase DataConnect Compilation Errors

**Symptoms:**
- Unresolved reference errors in generated DataConnect files
- Build fails before tests can run

**Temporary Workaround:**
1. Comment out DataConnect dependencies in `build.gradle.kts`
2. Remove or exclude DataConnect generated files
3. Run tests without DataConnect functionality

**Proper Fix:**
1. Update Firebase DataConnect SDK to latest version
2. Regenerate DataConnect files
3. Ensure all DataConnect dependencies are compatible

### Issue: Missing API Keys

**Symptoms:**
- AI Assistant tests fail
- API connectivity tests fail

**Fix:**
Add to `local.properties`:
```properties
GEMINI_API_KEY=your_api_key_here
```

### Issue: Firebase Not Configured

**Symptoms:**
- Firebase tests fail with initialization errors
- Authentication tests fail

**Fix:**
1. Ensure `google-services.json` is in `app/` directory
2. Verify Firebase project is configured
3. Check Firebase dependencies in `build.gradle.kts`

---

## Test Execution Checklist

Before running tests:
- [ ] Project builds successfully (`./gradlew build`)
- [ ] All dependencies are synced
- [ ] `google-services.json` is present
- [ ] API keys are configured
- [ ] No compilation errors

Running tests:
- [ ] Run all tests (`./gradlew test`)
- [ ] Check test report for failures
- [ ] Investigate any failed tests
- [ ] Fix issues and re-run

After tests pass:
- [ ] Review test coverage
- [ ] Document any skipped tests
- [ ] Update test documentation
- [ ] Commit test results

---

## Expected Test Results

### All Tests Passing
```
BUILD SUCCESSFUL in Xs
XX actionable tasks: XX executed

Test Summary:
- GoogleSignInFlowTest: 4 tests, 4 passed
- GroupCreationAndDisplayTest: 8 tests, 8 passed
- TaskCreationAndDisplayTest: 19 tests, 19 passed
- ChatMessageSendingAndReadingTest: 18 tests, 18 passed
- GeminiAssistantServiceTest: X tests, X passed
- GeminiAPIConnectivityTest: X tests, X passed

Total: XX tests, XX passed, 0 failed, 0 skipped
```

### Test Failure Example
```
BUILD FAILED in Xs

Test Summary:
- GoogleSignInFlowTest: 4 tests, 3 passed, 1 failed
  - FAILED: testFcmTokenSaving_CalledAfterSuccessfulSignIn
    Expected: FCM token to be saved
    Actual: Token was null

Total: XX tests, XX passed, 1 failed, 0 skipped
```

---

## Integration with CI/CD

### GitHub Actions Example
```yaml
name: Run Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Run tests
        run: ./gradlew test
      - name: Upload test results
        uses: actions/upload-artifact@v2
        with:
          name: test-results
          path: build/reports/tests/
```

---

## Manual Testing After Automated Tests

Once all automated tests pass, proceed with manual testing:

1. **Install the app:**
   ```bash
   ./gradlew installDebug
   ```

2. **Follow the manual testing guide:**
   - See `TASK_19_COMPREHENSIVE_TESTING_GUIDE.md`
   - Use `TASK_19_VERIFICATION_CHECKLIST.md`

3. **Test on real devices:**
   - Test on different Android versions
   - Test on different screen sizes
   - Test with different network conditions

---

## Troubleshooting

### Tests Won't Run
1. Clean the project: `./gradlew clean`
2. Sync dependencies: `./gradlew --refresh-dependencies`
3. Rebuild: `./gradlew build`
4. Try again: `./gradlew test`

### Tests Timeout
1. Increase timeout in test configuration
2. Check for infinite loops in code
3. Verify mock objects are configured correctly

### Tests Fail Intermittently
1. Check for race conditions
2. Verify async operations are properly awaited
3. Add delays or use proper synchronization

### Cannot Find Test Classes
1. Verify test files are in correct directory: `app/src/test/`
2. Check package names match
3. Ensure test classes are public
4. Verify JUnit dependencies are included

---

## Next Steps

After all automated tests pass:

1. ✓ Mark automated testing as complete
2. ✓ Proceed with manual testing
3. ✓ Document any issues found
4. ✓ Create bug reports for failures
5. ✓ Re-test after fixes
6. ✓ Mark Task 19 as complete when all tests pass

---

## Additional Resources

- [JUnit 4 Documentation](https://junit.org/junit4/)
- [Mockito Documentation](https://site.mockito.org/)
- [Gradle Testing Guide](https://docs.gradle.org/current/userguide/java_testing.html)
- [Android Testing Guide](https://developer.android.com/training/testing)

---

**Note:** This script assumes the build issues with Firebase DataConnect are resolved. If build errors persist, those must be fixed before tests can run.
