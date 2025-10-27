# Testing Quick Start Guide

## Quick Reference for Task 9 Testing

This guide provides quick commands and steps to execute all testing for the fragment lifecycle crash fix.

---

## 1. Automated UI Tests

### Run All Lifecycle Tests

```bash
# Windows
.\gradlew connectedAndroidTest --tests FragmentLifecycleTest

# Linux/Mac
./gradlew connectedAndroidTest --tests FragmentLifecycleTest
```

### Run Individual Tests

```bash
# Test HomeFragment rapid navigation
.\gradlew connectedAndroidTest --tests FragmentLifecycleTest.testRapidNavigationHomeFragment_doesNotCrash

# Test ChatFragment rapid navigation
.\gradlew connectedAndroidTest --tests FragmentLifecycleTest.testRapidNavigationChatFragment_doesNotCrash

# Test navigation during loading
.\gradlew connectedAndroidTest --tests FragmentLifecycleTest.testNavigationDuringLoadingHomeFragment_doesNotCrash

# Test multi-fragment navigation
.\gradlew connectedAndroidTest --tests FragmentLifecycleTest.testMultipleFragmentsRapidNavigation_doesNotCrash
```

### Prerequisites

- Android device or emulator connected
- User logged in to the app
- Firebase connection active

---

## 2. Manual Testing

### Quick Checklist

Follow the detailed guide in `MANUAL_TESTING_GUIDE.md`, or use this quick checklist:

**Rapid Navigation (5 minutes)**
- [ ] Test all 5 fragments with rapid tab switching (10x each)

**Error Scenarios (10 minutes)**
- [ ] Test with Airplane mode enabled
- [ ] Test with Firestore permission errors (if applicable)

**Configuration Changes (5 minutes)**
- [ ] Rotate device 3x on each fragment

**Background/Foreground (5 minutes)**
- [ ] Background app and return on each fragment

**Total Time:** ~25 minutes for quick manual testing

---

## 3. Production Monitoring

### Setup Monitoring

1. Deploy to production
2. Open Firebase Console → Crashlytics
3. Set up filters for NullPointerException crashes
4. Follow the 48-hour monitoring schedule in `PRODUCTION_MONITORING_GUIDE.md`

### Quick Monitoring Checks

**First 4 Hours (Check every hour):**
```
- Open Firebase Crashlytics
- Check crash-free rate
- Filter for NullPointerException
- Verify zero binding crashes
```

**Day 1-2 (Check every 4-6 hours):**
```
- Review crash trends
- Compare with baseline
- Check user feedback
- Document findings
```

---

## Test Results

### Expected Results

✅ **All automated tests pass** (8/8 tests)
✅ **No crashes during manual testing**
✅ **Zero NullPointerException in production** (48-hour period)
✅ **Crash-free rate maintained or improved**

### If Tests Fail

1. Check Logcat for error details
2. Review stack trace
3. Verify fragment lifecycle state
4. Check if binding is null
5. Report issue with reproduction steps

---

## Quick Commands Reference

```bash
# Build the app
.\gradlew assembleDebug

# Install on device
.\gradlew installDebug

# Run all tests
.\gradlew connectedAndroidTest

# Run only lifecycle tests
.\gradlew connectedAndroidTest --tests FragmentLifecycleTest

# View test report
# Open: app/build/reports/androidTests/connected/index.html

# Monitor Logcat
adb logcat | grep -E "FragmentLifecycleTest|FATAL"

# Clear app data (for fresh test)
adb shell pm clear com.teamsync.collaboration
```

---

## Documentation Links

- **Automated Tests:** `FragmentLifecycleTest.kt`
- **Manual Testing:** `MANUAL_TESTING_GUIDE.md`
- **Production Monitoring:** `PRODUCTION_MONITORING_GUIDE.md`
- **Completion Summary:** `TASK_9_COMPLETION_SUMMARY.md`

---

## Support

If you encounter issues:

1. Check the detailed guides in this directory
2. Review the requirements in `requirements.md`
3. Check the design document in `design.md`
4. Review completed tasks in `tasks.md`
