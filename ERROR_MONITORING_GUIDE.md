# Error Monitoring Guide

## Overview

This guide provides comprehensive instructions for monitoring error rates, crashes, and API failures in the TeamSync Android app using Firebase Crashlytics and other monitoring tools.

## Monitoring Tools

### 1. Firebase Crashlytics

**Purpose:** Track crashes and non-fatal errors

**Access:** https://console.firebase.google.com → Your Project → Crashlytics

**Key Metrics:**
- Crash-free users percentage
- Crash-free sessions percentage
- Total crashes
- Affected users
- Crash trends

### 2. Firebase Performance Monitoring

**Purpose:** Track app performance and network requests

**Access:** https://console.firebase.google.com → Your Project → Performance

**Key Metrics:**
- App startup time
- Screen rendering time
- Network request duration
- Success/failure rates

### 3. Google Play Console Vitals

**Purpose:** Monitor app quality metrics

**Access:** https://play.google.com/console → Your App → Quality → Android vitals

**Key Metrics:**
- Crash rate
- ANR (Application Not Responding) rate
- Excessive wakeups
- Stuck wake locks

### 4. Firebase Analytics

**Purpose:** Track user behavior and custom events

**Access:** https://console.firebase.google.com → Your Project → Analytics

**Key Metrics:**
- Active users
- User retention
- Event counts
- Conversion funnels

## Critical Error Categories

### 1. Permission Denied Errors

**Error Code:** `PERMISSION_DENIED`

**Common Causes:**
- Firestore security rules blocking operations
- User authentication token expired
- User not in required group/chat
- Invalid data validation

**Monitoring:**
```kotlin
// Log permission errors
FirebaseCrashlytics.getInstance().apply {
    setCustomKey("error_type", "PERMISSION_DENIED")
    setCustomKey("operation", operationName)
    setCustomKey("user_id", userId)
    recordException(exception)
}
```

**Alert Threshold:**
- > 5% of operations fail with PERMISSION_DENIED
- Sudden spike in permission errors (> 2x baseline)

**Investigation Steps:**
1. Check Firestore rules in Firebase Console
2. Review recent rule changes
3. Check user authentication status
4. Verify data structure matches rules
5. Test operation manually with affected user

### 2. Network Errors

**Error Types:**
- `UnknownHostException` - No internet connection
- `SocketTimeoutException` - Request timeout
- `IOException` - General network failure

**Monitoring:**
```kotlin
// Log network errors
FirebaseCrashlytics.getInstance().apply {
    setCustomKey("error_type", "NETWORK_ERROR")
    setCustomKey("endpoint", apiEndpoint)
    setCustomKey("error_message", exception.message ?: "Unknown")
    recordException(exception)
}
```

**Alert Threshold:**
- > 10% of API calls fail
- Average response time > 5 seconds
- Timeout rate > 5%

**Investigation Steps:**
1. Check Firebase Status page
2. Review network logs
3. Test API endpoints manually
4. Check for regional outages
5. Verify timeout configurations

### 3. API Failures

**Error Types:**
- Gemini AI 404 errors
- Firebase API errors
- Third-party service failures

**Monitoring:**
```kotlin
// Log API failures
FirebaseCrashlytics.getInstance().apply {
    setCustomKey("error_type", "API_FAILURE")
    setCustomKey("api_name", "Gemini AI")
    setCustomKey("status_code", statusCode)
    setCustomKey("response_body", responseBody)
    recordException(exception)
}
```

**Alert Threshold:**
- > 5% of API calls fail
- Specific endpoint failure rate > 10%
- Consecutive failures > 3

**Investigation Steps:**
1. Check API service status
2. Verify API keys and configuration
3. Review API version compatibility
4. Test API calls manually
5. Check rate limits and quotas

### 4. Validation Errors

**Error Types:**
- Missing required fields
- Invalid data types
- Array size violations
- String length violations

**Monitoring:**
```kotlin
// Log validation errors
FirebaseCrashlytics.getInstance().apply {
    setCustomKey("error_type", "VALIDATION_ERROR")
    setCustomKey("validation_field", fieldName)
    setCustomKey("validation_rule", ruleName)
    recordException(ValidationException(message))
}
```

**Alert Threshold:**
- > 2% of operations fail validation
- Specific validation rule fails > 5%

**Investigation Steps:**
1. Review validation rules
2. Check client-side validation logic
3. Verify data transformation
4. Test with problematic data
5. Update validation messages

## Monitoring Dashboard Setup

### Firebase Crashlytics Dashboard

**Key Views:**
1. **Overview**
   - Crash-free users (target: > 99%)
   - Crash-free sessions (target: > 99.5%)
   - Total crashes (trend should be decreasing)

2. **Issues**
   - Sort by "Impacted users" (fix high-impact first)
   - Filter by "Open" status
   - Group by error type

3. **Velocity Alerts**
   - Set up alerts for crash spikes
   - Configure email notifications
   - Set threshold: > 1% crash rate

### Custom Monitoring Queries

**Firestore Permission Errors:**
```
error_type == "PERMISSION_DENIED"
```

**Network Failures:**
```
error_type == "NETWORK_ERROR"
```

**API Failures:**
```
error_type == "API_FAILURE" AND api_name == "Gemini AI"
```

**Validation Errors:**
```
error_type == "VALIDATION_ERROR"
```

## Alert Configuration

### Critical Alerts (Immediate Response)

**Crash Rate Alert:**
- Condition: Crash-free users < 95%
- Action: Investigate immediately, prepare hotfix
- Notification: Email + SMS to on-call engineer

**Permission Denied Spike:**
- Condition: Permission errors > 10% of operations
- Action: Check Firestore rules, rollback if needed
- Notification: Email to team lead

**API Complete Failure:**
- Condition: API success rate < 50%
- Action: Check service status, switch to fallback
- Notification: Email + Slack to team

### Warning Alerts (Monitor Closely)

**Elevated Crash Rate:**
- Condition: Crash-free users < 98%
- Action: Monitor trends, investigate patterns
- Notification: Email to team

**Network Error Increase:**
- Condition: Network errors > 15% of requests
- Action: Check connectivity patterns, review timeouts
- Notification: Email to team

**Validation Error Increase:**
- Condition: Validation errors > 5% of operations
- Action: Review validation rules, check data quality
- Notification: Email to team

### Info Alerts (Track Trends)

**Performance Degradation:**
- Condition: Average response time > 3 seconds
- Action: Profile performance, optimize queries
- Notification: Weekly summary email

**User Retention Drop:**
- Condition: Day 1 retention < 40%
- Action: Review user experience, check onboarding
- Notification: Weekly summary email

## Monitoring Procedures

### Daily Monitoring (5-10 minutes)

**Morning Check (9:00 AM):**
1. Open Firebase Crashlytics
2. Check crash-free rate (last 24 hours)
3. Review new crash issues
4. Check for alert emails
5. Verify no critical issues

**Evening Check (5:00 PM):**
1. Review day's metrics
2. Check for trends
3. Triage new issues
4. Update issue tracker
5. Plan fixes for next day

### Weekly Monitoring (30-60 minutes)

**Monday Review:**
1. Generate weekly report
2. Analyze crash trends
3. Review top 10 issues
4. Prioritize fixes
5. Update roadmap

**Metrics to Review:**
- Crash-free users (week over week)
- Top crash issues (by impact)
- Permission error trends
- API failure rates
- Network error patterns
- Validation error distribution

### Monthly Monitoring (2-4 hours)

**First Monday of Month:**
1. Generate monthly report
2. Analyze long-term trends
3. Review all open issues
4. Identify systemic problems
5. Plan improvements

**Metrics to Review:**
- Month-over-month crash rate
- User retention trends
- Feature usage analytics
- Performance metrics
- Error rate trends
- User feedback themes

## Error Investigation Workflow

### Step 1: Identify Error

**Sources:**
- Firebase Crashlytics alert
- User report
- Monitoring dashboard
- Automated test failure

**Gather Information:**
- Error message
- Stack trace
- User ID (if available)
- Device information
- App version
- Timestamp

### Step 2: Reproduce Error

**Reproduction Steps:**
1. Set up test environment
2. Use same app version
3. Follow user steps (if known)
4. Try different devices/OS versions
5. Test with different data

**If Cannot Reproduce:**
- Check for device-specific issues
- Review user's data state
- Test with user's account (with permission)
- Check for timing/race conditions

### Step 3: Analyze Root Cause

**Common Analysis Techniques:**
- Review stack trace
- Check recent code changes
- Review related issues
- Test edge cases
- Profile performance

**Questions to Ask:**
- When did this start happening?
- What changed recently?
- Is it affecting all users or specific subset?
- Is it device/OS specific?
- Is it data-dependent?

### Step 4: Implement Fix

**Fix Development:**
1. Create fix branch
2. Implement solution
3. Add tests to prevent regression
4. Test thoroughly
5. Code review

**Fix Validation:**
- Unit tests pass
- Integration tests pass
- Manual testing confirms fix
- No new issues introduced
- Performance not degraded

### Step 5: Deploy Fix

**Deployment Strategy:**
- Hotfix for critical issues (< 24 hours)
- Regular release for non-critical (next release cycle)
- Staged rollout for risky fixes

**Post-Deployment:**
- Monitor error rate
- Verify fix effectiveness
- Check for new issues
- Update documentation

### Step 6: Document and Close

**Documentation:**
- Update issue tracker
- Document root cause
- Document fix approach
- Update runbook if needed
- Share learnings with team

**Closure:**
- Mark issue as resolved
- Set up monitoring for recurrence
- Update tests
- Close related tickets

## Error Rate Targets

### Production Targets

**Crash Metrics:**
- Crash-free users: > 99%
- Crash-free sessions: > 99.5%
- ANR rate: < 0.5%

**Error Rates:**
- Permission denied: < 1% of operations
- Network errors: < 5% of requests
- API failures: < 2% of calls
- Validation errors: < 1% of operations

**Performance:**
- App startup time: < 2 seconds
- Screen load time: < 1 second
- API response time: < 1 second
- Frame rate: > 50 FPS

### Acceptable Ranges

**Warning Level (Monitor):**
- Crash-free users: 98-99%
- Permission denied: 1-3%
- Network errors: 5-10%
- API failures: 2-5%

**Critical Level (Immediate Action):**
- Crash-free users: < 98%
- Permission denied: > 3%
- Network errors: > 10%
- API failures: > 5%

## Reporting

### Daily Report Template

```markdown
# Daily Error Monitoring Report - [Date]

## Summary
- Crash-free users: XX.X%
- Total crashes: XXX
- New issues: X
- Resolved issues: X

## Critical Issues
- [Issue #1]: Description - Status
- [Issue #2]: Description - Status

## Trends
- Crash rate: [Up/Down/Stable]
- Permission errors: [Up/Down/Stable]
- API failures: [Up/Down/Stable]

## Actions Taken
- [Action 1]
- [Action 2]

## Next Steps
- [Next step 1]
- [Next step 2]
```

### Weekly Report Template

```markdown
# Weekly Error Monitoring Report - Week of [Date]

## Executive Summary
- Overall health: [Good/Fair/Poor]
- Crash-free users: XX.X% (±X.X% vs last week)
- Top issue: [Description]
- Issues resolved: X

## Metrics
- Total crashes: XXX (±XX vs last week)
- Affected users: XXX (±XX vs last week)
- Permission errors: XXX (±XX vs last week)
- API failures: XXX (±XX vs last week)

## Top 5 Issues
1. [Issue]: XX users affected - [Status]
2. [Issue]: XX users affected - [Status]
3. [Issue]: XX users affected - [Status]
4. [Issue]: XX users affected - [Status]
5. [Issue]: XX users affected - [Status]

## Improvements
- [Improvement 1]
- [Improvement 2]

## Concerns
- [Concern 1]
- [Concern 2]

## Action Items
- [ ] [Action item 1]
- [ ] [Action item 2]
```

## Tools and Scripts

### Crashlytics Query Script

```bash
#!/bin/bash
# Query Firebase Crashlytics for error rates

PROJECT_ID="your-project-id"
DATE=$(date +%Y-%m-%d)

echo "Fetching Crashlytics data for $DATE..."

# This requires Firebase CLI and appropriate permissions
firebase crashlytics:issues:list --project=$PROJECT_ID --json > crashlytics-$DATE.json

echo "Data saved to crashlytics-$DATE.json"
```

### Error Rate Calculator

```kotlin
// Calculate error rates from logs
fun calculateErrorRate(
    totalOperations: Int,
    failedOperations: Int
): Double {
    if (totalOperations == 0) return 0.0
    return (failedOperations.toDouble() / totalOperations) * 100
}

// Example usage
val permissionErrorRate = calculateErrorRate(
    totalOperations = 1000,
    failedOperations = 15
)
println("Permission error rate: ${"%.2f".format(permissionErrorRate)}%")
```

## Best Practices

### 1. Proactive Monitoring

- Check dashboards daily
- Set up automated alerts
- Review trends weekly
- Investigate anomalies immediately

### 2. Comprehensive Logging

- Log all errors with context
- Include user ID (when appropriate)
- Add custom keys for filtering
- Log non-fatal errors too

### 3. Fast Response

- Triage issues within 24 hours
- Fix critical issues within 48 hours
- Communicate with users
- Document all incidents

### 4. Continuous Improvement

- Learn from each incident
- Update monitoring based on patterns
- Improve error messages
- Enhance validation

### 5. User Communication

- Acknowledge known issues
- Provide status updates
- Notify when fixed
- Thank users for reports

## Emergency Response

### Critical Error Response Plan

**Phase 1: Detection (0-15 minutes)**
1. Receive alert
2. Verify issue is real
3. Assess impact
4. Notify team

**Phase 2: Triage (15-30 minutes)**
1. Identify root cause
2. Determine severity
3. Decide on response
4. Assign owner

**Phase 3: Mitigation (30-60 minutes)**
1. Implement temporary fix
2. Test fix
3. Deploy hotfix OR rollback
4. Monitor results

**Phase 4: Resolution (1-24 hours)**
1. Implement permanent fix
2. Test thoroughly
3. Deploy to production
4. Verify fix

**Phase 5: Post-Mortem (24-48 hours)**
1. Document incident
2. Analyze root cause
3. Identify improvements
4. Update procedures

## Resources

### Documentation
- [Firebase Crashlytics Docs](https://firebase.google.com/docs/crashlytics)
- [Firebase Performance Docs](https://firebase.google.com/docs/perf-mon)
- [Play Console Vitals](https://developer.android.com/topic/performance/vitals)

### Tools
- Firebase Console
- Google Play Console
- Android Studio Profiler
- Charles Proxy (network debugging)

### Support
- Firebase Support
- Stack Overflow
- Android Developer Community
- Team documentation

## Appendix

### Common Error Codes

**Firestore Error Codes:**
- `PERMISSION_DENIED` (7): Insufficient permissions
- `NOT_FOUND` (5): Document not found
- `ALREADY_EXISTS` (6): Document already exists
- `UNAVAILABLE` (14): Service unavailable
- `DEADLINE_EXCEEDED` (4): Request timeout

**HTTP Status Codes:**
- 400: Bad Request
- 401: Unauthorized
- 403: Forbidden
- 404: Not Found
- 500: Internal Server Error
- 503: Service Unavailable

### Useful Queries

**Find crashes by version:**
```
app_version == "1.0.1"
```

**Find crashes on specific device:**
```
device_model == "Pixel 6"
```

**Find crashes for specific user:**
```
user_id == "user123"
```

**Find recent crashes:**
```
timestamp > "2024-10-29T00:00:00Z"
```
