# Task 19: Quick Start Testing Guide

## 🚀 Quick Start - Get Testing in 5 Minutes

This is your fastest path to start testing the critical bug fixes.

---

## Step 1: Fix Build (If Needed)

If you see build errors:

```bash
./gradlew clean
./gradlew build
```

If Firebase DataConnect errors appear, temporarily comment out DataConnect dependencies in `app/build.gradle.kts`.

---

## Step 2: Install the App

```bash
./gradlew installDebug
```

---

## Step 3: Run Quick Verification (15 minutes)

Open the app and test these critical flows:

### ✅ 1. Sign In (2 min)
- Tap "Sign in with Google"
- Select account
- **Expected:** Sign in succeeds, no errors

### ✅ 2. Create Group (2 min)
- Go to Groups tab
- Tap "Create Group"
- Fill in details and create
- **Expected:** Group appears immediately

### ✅ 3. Create Task (2 min)
- Go to Tasks tab
- Tap "Add Task"
- Fill in details and create
- **Expected:** Task appears in list and calendar

### ✅ 4. Send Message (2 min)
- Go to Chats tab
- Open or create a chat
- Send a text message
- **Expected:** Message sends without errors

### ✅ 5. Upload Profile Picture (2 min)
- Go to Profile tab
- Tap profile picture
- Select an image
- **Expected:** Image uploads and displays

### ✅ 6. Test AI Assistant (2 min)
- Go to Tasks tab
- Tap "AI Assistant"
- Send: "Create a math homework task"
- **Expected:** AI responds and creates task

### ✅ 7. Test Dark Mode (2 min)
- Go to device Settings
- Switch to dark mode
- Return to app
- **Expected:** App displays correctly in dark mode

### ✅ 8. Test Offline (3 min)
- Turn on airplane mode
- Send a message
- Turn off airplane mode
- **Expected:** Message sends when back online

---

## Step 4: Check for Errors

Open Logcat and look for:

### ❌ Should NOT See:
- PERMISSION_DENIED errors
- IllegalArgumentException
- NullPointerException
- Crashes

### ✅ Should See:
- Successful Firestore operations
- Successful uploads
- Real-time updates

---

## Step 5: Verify in Firebase Console

### Check Firestore:
1. Go to Firebase Console → Firestore
2. Verify:
   - `users/{userId}` has fcmToken
   - `groups/{groupId}` has memberIds array
   - `tasks/{taskId}` has correct userId
   - `chats/{chatId}/messages` has messages

### Check Storage:
1. Go to Firebase Console → Storage
2. Verify:
   - `profile_pictures/{userId}/` has image
   - `chat_attachments/{chatId}/` has files (if sent)

---

## ✅ Quick Pass/Fail

**PASS if:**
- ✓ All 8 quick tests work
- ✓ No errors in Logcat
- ✓ Data appears in Firebase Console

**FAIL if:**
- ❌ Any test fails
- ❌ PERMISSION_DENIED errors
- ❌ App crashes
- ❌ Data not in Firebase

---

## Next Steps

### If All Tests Pass ✅
1. Proceed with comprehensive testing
2. See: `TASK_19_COMPREHENSIVE_TESTING_GUIDE.md`
3. Use: `TASK_19_VERIFICATION_CHECKLIST.md`

### If Tests Fail ❌
1. Note which test failed
2. Check Logcat for errors
3. Check Firebase Console for data
4. Review the specific test section in comprehensive guide
5. Fix issues and re-test

---

## Automated Tests (Optional)

If build is working:

```bash
# Run all tests
./gradlew test

# View results
start build/reports/tests/test/index.html
```

**Expected:** All tests pass

---

## Common Issues

### Issue: Can't Sign In
- **Check:** Google Sign-In enabled in Firebase Console
- **Check:** SHA-1 fingerprint added to Firebase
- **Check:** `google-services.json` is up to date

### Issue: Permission Denied Errors
- **Check:** Firestore rules deployed
- **Check:** User is authenticated
- **Check:** Field names match (memberIds, not members)

### Issue: Images Won't Upload
- **Check:** Storage rules deployed
- **Check:** File size under 10MB
- **Check:** User is authenticated

### Issue: AI Assistant Not Working
- **Check:** `GEMINI_API_KEY` in `local.properties`
- **Check:** API key is valid
- **Check:** Network connectivity

---

## Critical Path Test (5 minutes)

Complete this flow without errors:

1. Sign in with Google ✓
2. Create a group ✓
3. Create a task ✓
4. Send a chat message ✓
5. Upload profile picture ✓

**If this works, core functionality is good!**

---

## Time Estimates

- **Quick Verification:** 15 minutes
- **Quick + Automated Tests:** 30 minutes
- **Comprehensive Testing:** 3-4 hours
- **Full Regression:** 6-8 hours

---

## Documentation Reference

- **Quick Verification:** This file
- **Detailed Testing:** `TASK_19_COMPREHENSIVE_TESTING_GUIDE.md`
- **Checklist:** `TASK_19_VERIFICATION_CHECKLIST.md`
- **Test Commands:** `TASK_19_TEST_EXECUTION_SCRIPT.md`
- **Summary:** `TASK_19_COMPLETION_SUMMARY.md`

---

## Support

**Having issues?**
1. Check Logcat for detailed errors
2. Review Firebase Console for data
3. Consult comprehensive testing guide
4. Check security rules in Firebase Console

---

## Success!

If all quick tests pass:
- ✅ Core functionality is working
- ✅ Critical bugs are fixed
- ✅ App is ready for detailed testing

**Great job! Now proceed with comprehensive testing for production readiness.**

---

**Remember:** This is a quick verification. For production release, complete the comprehensive testing guide.
