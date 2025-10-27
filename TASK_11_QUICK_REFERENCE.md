# Task 11: Production Monitoring - Quick Reference

## Quick Access

### View Monitoring Dashboard
```kotlin
startActivity(Intent(this, MonitoringDashboardActivity::class.java))
```

### Check Metrics Programmatically
```kotlin
// Get session metrics
val metrics = ProductionMetricsMonitor.getSessionMetrics()
println("Success Rate: ${metrics.successRate}%")
println("Permission Errors: ${metrics.permissionErrors}")

// Check health
val isHealthy = ProductionMetricsMonitor.isHealthy()
val status = ProductionMetricsMonitor.getHealthStatus()

// Log to console and Crashlytics
ProductionMetricsMonitor.logMetrics()
```

## Key Metrics

| Metric | Good | Warning | Critical |
|--------|------|---------|----------|
| Permission Errors | 0 | 1-5 | > 5 |
| Success Rate | > 99% | 95-99% | < 95% |
| Failed Queries | < 1% | 1-5% | > 5% |

## Health Status Indicators

- ✅ **HEALTHY:** No permission errors, success rate > 95%
- ⚠️ **DEGRADED:** Success rate between 90-95%
- ❌ **UNHEALTHY:** Permission errors detected
- ℹ️ **NO DATA:** No queries recorded yet

## Common Commands

### Reset Metrics
```kotlin
ProductionMetricsMonitor.resetMetrics()
```

### Get Permission Error Count
```kotlin
val errorCount = ProductionMetricsMonitor.getPermissionErrorCount()
```

### Get Success Rate
```kotlin
val successRate = ProductionMetricsMonitor.getQuerySuccessRate()
```

## Crashlytics Custom Keys

Monitor these in Firebase Console:
- `session_permission_errors` - Current session errors
- `total_permission_errors` - All-time errors
- `session_success_rate` - Current session rate
- `overall_success_rate` - All-time rate
- `last_permission_error` - Last failed operation

## Troubleshooting Quick Checks

### Permission Errors Detected
1. Check Firestore rules deployment
2. Verify user authentication
3. Review query filters
4. Check operation logs in Crashlytics

### Low Success Rate
1. Check network connectivity
2. Verify Firestore service status
3. Review query syntax
4. Check data model consistency

### No Metrics Showing
1. Verify monitor initialization in Application.onCreate()
2. Ensure SafeFirestoreCall is being used
3. Check for direct Firestore calls bypassing wrapper

## Files Modified/Created

### New Files
- `ProductionMetricsMonitor.kt` - Core monitoring utility
- `MonitoringDashboardActivity.kt` - Visual dashboard
- `activity_monitoring_dashboard.xml` - Dashboard layout

### Modified Files
- `SafeFirestoreCall.kt` - Added metrics integration
- `TeamCollaborationApp.kt` - Added monitor initialization

## Testing Checklist

- [ ] Monitor initializes on app start
- [ ] Dashboard displays current metrics
- [ ] Permission errors are tracked
- [ ] Success rate calculates correctly
- [ ] Health status updates properly
- [ ] Export function works
- [ ] Reset function clears metrics
- [ ] Crashlytics receives logs

## Next Steps

1. Add dashboard access to your app (debug menu, settings, etc.)
2. Monitor metrics during testing
3. Check Crashlytics dashboard for trends
4. Review metrics after rule deployments
5. Set up alerts for critical thresholds
