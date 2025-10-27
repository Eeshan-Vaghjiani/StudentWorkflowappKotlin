# Task 10: Quick Reference Guide

## Quick Start

### Run Automated Tests
```bash
# Windows
run-firestore-permission-tests.bat

# Or manually
gradlew.bat connectedAndroidTest --tests "com.example.loginandregistration.FirestorePermissionsIntegrationTest"
```

### View Test Results
```
app\build\reports\androidTests\connected\index.html
```

---

## Test Checklist

### Automated Tests (10 tests)
- [ ] Groups screen navigation
- [ ] Create new group
- [ ] View group activities
- [ ] Tasks screen functionality
- [ ] Create task
- [ ] Chat functionality
- [ ] Error handling
- [ ] Query without filters
- [ ] Concurrent queries
- [ ] Update owned resources

### Manual Tests (6 core areas)
- [ ] Groups screen navigation
- [ ] Creating new groups
- [ ] Viewing group activities
- [ ] Tasks screen
- [ ] Chat functionality
- [ ] Error message user-friendliness

---

## Key Requirements

**1.1**: Users can access group data without permission errors  
**4.5**: App handles operations correctly after deployment  
**5.4**: Error messages are user-friendly

---

## Success Criteria

✅ No permission denied errors  
✅ No app crashes  
✅ All features work correctly  
✅ Error messages are clear and actionable  
✅ Performance is acceptable  

---

## Common Issues & Solutions

### Issue: Tests fail with "No authenticated user"
**Solution**: Ensure you're logged into the app before running tests

### Issue: Permission denied errors
**Solution**: Verify Firestore rules are deployed correctly

### Issue: Tests timeout
**Solution**: Check internet connection and Firebase project status

### Issue: Can't find test results
**Solution**: Look in `app/build/reports/androidTests/connected/`

---

## Quick Commands

### Build and Install App
```bash
gradlew.bat installDebug
```

### Run Specific Test
```bash
gradlew.bat connectedAndroidTest --tests "*.testGroupsScreenNavigation_doesNotCrash"
```

### View Logcat
```bash
adb logcat | findstr "FirestorePermTest"
```

### Clear App Data
```bash
adb shell pm clear com.example.loginandregistration
```

---

## Test Data Cleanup

Tests automatically clean up created data in the `@After` method. If manual cleanup is needed:

```kotlin
// Delete test groups
db.collection("groups").document(groupId).delete()

// Delete test tasks
db.collection("tasks").document(taskId).delete()

// Delete test chats
db.collection("chats").document(chatId).delete()
```

---

## Monitoring During Tests

### Watch Firestore Console
1. Open Firebase Console
2. Go to Firestore Database
3. Monitor for new documents during tests

### Watch Logs
```bash
adb logcat -s FirestorePermTest:* AndroidRuntime:E
```

### Check Performance
- Open Android Studio Profiler
- Monitor CPU, Memory, Network during test execution

---

## Documentation

- **Manual Testing Guide**: `TASK_10_MANUAL_TESTING_GUIDE.md`
- **Test Execution Report**: `TASK_10_TEST_EXECUTION_REPORT.md`
- **Test Code**: `app/src/androidTest/.../FirestorePermissionsIntegrationTest.kt`

---

## Next Steps After Testing

1. Fill out test execution report
2. Document any issues found
3. Verify all requirements met
4. Mark task as complete
5. Proceed to Task 11: Monitor Production Metrics
