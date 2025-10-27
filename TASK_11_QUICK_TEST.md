# Task 11: Quick Test Guide

## 5-Minute Verification

Use this guide to quickly verify that production monitoring is working.

### Step 1: Launch App (30 seconds)

**Action:** Start the app

**Check Logcat:**
```bash
adb logcat -s TeamCollaborationApp:D ProductionMetrics:*
```

**Expected Output:**
```
D/TeamCollaborationApp: Production metrics monitoring initialized
I/ProductionMetrics: Production metrics monitoring initialized
```

✅ **Pass:** Initialization logs appear
❌ **Fail:** No logs or errors

---

### Step 2: Trigger Queries (1 minute)

**Action:** Navigate to Groups screen

**Expected Logcat:**
```
D/ProductionMetrics: Query attempt recorded: getUserGroups
D/ProductionMetrics: Query success recorded: getUserGroups
```

✅ **Pass:** Query logs appear
❌ **Fail:** No query logs

---

### Step 3: Open Dashboard (1 minute)

**Action:** Add this code to any activity and run:
```kotlin
startActivity(Intent(this, MonitoringDashboardActivity::class.java))
```

**Expected:**
- Dashboard opens
- Shows metrics > 0
- Health status: ✅ HEALTHY
- All buttons visible

✅ **Pass:** Dashboard works correctly
❌ **Fail:** Crash or wrong data

---

### Step 4: Test Refresh (30 seconds)

**Action:** 
1. Note current metrics
2. Navigate in app
3. Return to dashboard
4. Tap "Refresh"

**Expected:**
- Metrics update
- Total queries increases

✅ **Pass:** Refresh works
❌ **Fail:** No update

---

### Step 5: Test Export (1 minute)

**Action:** Tap "Export" button

**Expected:**
- Dialog appears
- Shows complete report
- "Copy" button works

✅ **Pass:** Export works
❌ **Fail:** Dialog doesn't appear

---

### Step 6: Check Crashlytics (1 minute)

**Action:** Open Firebase Console → Crashlytics

**Expected Custom Keys:**
- `monitoring_enabled`: true
- `session_total_queries`: > 0
- `session_success_rate`: ~100

✅ **Pass:** Keys visible
❌ **Fail:** No keys

---

## Quick Logcat Commands

### View All Monitoring
```bash
adb logcat -s ProductionMetrics:* SafeFirestoreCall:* TeamCollaborationApp:*
```

### View Only Errors
```bash
adb logcat -s ProductionMetrics:E SafeFirestoreCall:E
```

### View Metrics Reports
```bash
adb logcat -s ProductionMetrics:I | grep "==="
```

## Quick Code Tests

### Test Metrics Programmatically
```kotlin
// Get metrics
val metrics = ProductionMetricsMonitor.getSessionMetrics()
Log.d("TEST", "Queries: ${metrics.totalQueries}")
Log.d("TEST", "Success: ${metrics.successRate}%")
Log.d("TEST", "Errors: ${metrics.permissionErrors}")

// Check health
val healthy = ProductionMetricsMonitor.isHealthy()
Log.d("TEST", "Healthy: $healthy")

// Log report
ProductionMetricsMonitor.logMetrics()
```

### Test Safe Call
```kotlin
suspend fun testMonitoring() {
    val result = safeFirestoreCall("testOperation") {
        // Your Firestore operation
        db.collection("test").get().await()
    }
    
    result.onSuccess {
        Log.d("TEST", "Operation succeeded")
    }.onFailure {
        Log.e("TEST", "Operation failed", it)
    }
}
```

## Expected Results

After running the app and navigating:

**Session Metrics:**
- Total Queries: > 0
- Successful: > 0
- Failed: 0
- Permission Errors: 0
- Success Rate: 100%

**Health Status:**
- ✅ HEALTHY

**Crashlytics:**
- Custom keys present
- Values accurate

## Common Issues

### No Metrics Recorded
**Solution:** Check that `ProductionMetricsMonitor.initialize()` is called in Application.onCreate()

### Dashboard Crashes
**Solution:** Clean and rebuild project

### No Crashlytics Keys
**Solution:** Wait a few minutes for sync, check internet connection

## Success Criteria

✅ All 6 steps pass
✅ Metrics show correct values
✅ Dashboard works
✅ Crashlytics receives data
✅ No crashes

## If All Tests Pass

**Status:** ✅ Monitoring is working correctly

You can now:
1. Use monitoring in production
2. Set up Crashlytics alerts
3. Monitor metrics regularly

## If Any Test Fails

1. Check the specific step that failed
2. Review error logs
3. Consult `TASK_11_MONITORING_VERIFICATION_GUIDE.md`
4. Check `TASK_11_PRODUCTION_MONITORING_IMPLEMENTATION.md`

## Quick Reference

**Open Dashboard:**
```kotlin
startActivity(Intent(this, MonitoringDashboardActivity::class.java))
```

**Check Health:**
```kotlin
ProductionMetricsMonitor.isHealthy()
```

**Get Metrics:**
```kotlin
ProductionMetricsMonitor.getSessionMetrics()
```

**Log Report:**
```kotlin
ProductionMetricsMonitor.logMetrics()
```

**Reset:**
```kotlin
ProductionMetricsMonitor.resetMetrics()
```

---

**Total Time:** ~5 minutes
**Difficulty:** Easy
**Prerequisites:** App installed, Logcat access
