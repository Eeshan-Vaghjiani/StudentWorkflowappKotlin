# Production Monitoring Guide

This guide explains how to monitor the user profile initialization fix and chat functionality in production using Firebase Analytics and Crashlytics.

## Overview

The production monitoring system tracks:
- **Profile creation success/failure rates**
- **Sign-in success rates**
- **Chat functionality success rates**
- **"User not found" errors (should be ZERO after fix)**

## Monitoring Implementation

### Events Tracked

#### Profile Events
- `profile_creation_attempt` - When profile creation is attempted
- `profile_creation_success` - When profile is created successfully
- `profile_creation_failure` - When profile creation fails
- `profile_update_success` - When profile is updated successfully
- `profile_update_failure` - When profile update fails

#### Sign-In Events
- `sign_in_attempt` - When user attempts to sign in
- `sign_in_success` - When sign-in succeeds
- `sign_in_failure` - When sign-in fails

#### Chat Events
- `chat_creation_attempt` - When chat creation is attempted
- `chat_creation_success` - When chat is created successfully
- `chat_creation_failure` - When chat creation fails

#### Message Events
- `message_send_attempt` - When message send is attempted
- `message_send_success` - When message is sent successfully
- `message_send_failure` - When message send fails

#### Critical Error Events
- `user_not_found_error` - **CRITICAL**: This should be ZERO after profile fix

## Viewing Metrics in Firebase Console

### 1. Firebase Analytics Dashboard

**Access**: Firebase Console → Analytics → Dashboard

#### Key Metrics to Monitor

**Profile Creation Success Rate**:
```
Success Rate = profile_creation_success / profile_creation_attempt × 100%
Target: > 99%
```

**Sign-In Success Rate**:
```
Success Rate = sign_in_success / sign_in_attempt × 100%
Target: > 95%
```

**Chat Creation Success Rate**:
```
Success Rate = chat_creation_success / chat_creation_attempt × 100%
Target: > 98%
```

**Message Send Success Rate**:
```
Success Rate = message_send_success / message_send_attempt × 100%
Target: > 95%
```

**User Not Found Errors**:
```
Count = user_not_found_error events
Target: 0 (ZERO)
```

### 2. Custom Analytics Reports

#### Create Custom Report for Profile Monitoring

1. Go to Firebase Console → Analytics → Custom Reports
2. Click "Create Custom Report"
3. Name: "Profile Initialization Monitoring"
4. Add metrics:
   - `profile_creation_attempt` (count)
   - `profile_creation_success` (count)
   - `profile_creation_failure` (count)
5. Add dimensions:
   - `is_new_user` (to see new vs existing users)
   - `error_type` (to see failure reasons)
6. Save and view

#### Create Custom Report for Chat Monitoring

1. Go to Firebase Console → Analytics → Custom Reports
2. Click "Create Custom Report"
3. Name: "Chat Functionality Monitoring"
4. Add metrics:
   - `chat_creation_attempt` (count)
   - `chat_creation_success` (count)
   - `chat_creation_failure` (count)
   - `message_send_attempt` (count)
   - `message_send_success` (count)
   - `message_send_failure` (count)
5. Add dimensions:
   - `chat_type` (DIRECT vs GROUP)
   - `error_type` (to see failure reasons)
6. Save and view

#### Create Alert for User Not Found Errors

1. Go to Firebase Console → Analytics → Custom Reports
2. Click "Create Custom Report"
3. Name: "CRITICAL: User Not Found Errors"
4. Add metrics:
   - `user_not_found_error` (count)
5. Add dimensions:
   - `context` (where the error occurred)
6. Set up alert:
   - Click "Create Alert"
   - Condition: `user_not_found_error > 0`
   - Notification: Email to team
7. Save

### 3. Firebase Crashlytics Dashboard

**Access**: Firebase Console → Crashlytics → Dashboard

#### Exception Types to Monitor

1. **ProfileCreationException**
   - Indicates profile creation failures
   - Check error_type parameter for root cause
   - Should be minimal after fix

2. **ProfileUpdateException**
   - Indicates profile update failures
   - Usually network-related

3. **SignInException**
   - Indicates sign-in failures
   - Check auth_method parameter (email vs google)

4. **ChatCreationException**
   - Indicates chat creation failures
   - Check chat_type parameter (DIRECT vs GROUP)

5. **MessageSendException**
   - Indicates message send failures
   - Check message_type parameter

6. **UserNotFoundException** ⚠️ **CRITICAL**
   - This should be ZERO after profile fix
   - If this appears, profile initialization is failing

### 4. BigQuery Export (Advanced)

For detailed analysis, export Analytics data to BigQuery:

1. Go to Firebase Console → Project Settings → Integrations
2. Enable BigQuery export
3. Use SQL queries to analyze data:

```sql
-- Profile creation success rate by day
SELECT
  event_date,
  COUNTIF(event_name = 'profile_creation_attempt') as attempts,
  COUNTIF(event_name = 'profile_creation_success') as successes,
  COUNTIF(event_name = 'profile_creation_failure') as failures,
  SAFE_DIVIDE(
    COUNTIF(event_name = 'profile_creation_success'),
    COUNTIF(event_name = 'profile_creation_attempt')
  ) * 100 as success_rate
FROM `your-project.analytics_XXXXX.events_*`
WHERE event_name IN ('profile_creation_attempt', 'profile_creation_success', 'profile_creation_failure')
GROUP BY event_date
ORDER BY event_date DESC
LIMIT 30;

-- User not found errors (should be ZERO)
SELECT
  event_date,
  event_timestamp,
  user_pseudo_id,
  (SELECT value.string_value FROM UNNEST(event_params) WHERE key = 'user_id') as user_id,
  (SELECT value.string_value FROM UNNEST(event_params) WHERE key = 'context') as context
FROM `your-project.analytics_XXXXX.events_*`
WHERE event_name = 'user_not_found_error'
ORDER BY event_timestamp DESC;

-- Chat creation success rate by type
SELECT
  (SELECT value.string_value FROM UNNEST(event_params) WHERE key = 'chat_type') as chat_type,
  COUNTIF(event_name = 'chat_creation_attempt') as attempts,
  COUNTIF(event_name = 'chat_creation_success') as successes,
  COUNTIF(event_name = 'chat_creation_failure') as failures,
  SAFE_DIVIDE(
    COUNTIF(event_name = 'chat_creation_success'),
    COUNTIF(event_name = 'chat_creation_attempt')
  ) * 100 as success_rate
FROM `your-project.analytics_XXXXX.events_*`
WHERE event_name IN ('chat_creation_attempt', 'chat_creation_success', 'chat_creation_failure')
GROUP BY chat_type;
```

## Success Criteria

After deploying the profile initialization fix, monitor these metrics:

### ✅ Profile Creation
- **Success Rate**: > 99%
- **Failure Rate**: < 1%
- **New User Profile Creation**: 100% success

### ✅ Sign-In
- **Success Rate**: > 95%
- **Profile Creation After Sign-In**: 100% success

### ✅ Chat Functionality
- **Chat Creation Success Rate**: > 98%
- **Message Send Success Rate**: > 95%
- **User Not Found Errors**: **0 (ZERO)**

### ⚠️ Alert Thresholds

Set up alerts for:
- `user_not_found_error` > 0 (immediate alert)
- `profile_creation_failure` > 10 per hour
- `chat_creation_failure` > 20 per hour
- `sign_in_failure` > 50 per hour

## Troubleshooting

### High Profile Creation Failure Rate

1. Check Crashlytics for ProfileCreationException
2. Look at error_type parameter:
   - `PERMISSION_DENIED`: Check Firestore rules
   - `UNAVAILABLE`: Network issues
   - `UNEXPECTED_ERROR`: Check logs

### User Not Found Errors Appearing

⚠️ **CRITICAL**: This indicates profile initialization is failing

1. Check if profiles are being created on sign-in
2. Verify Firestore rules allow profile creation
3. Check UserProfileRepository logs
4. Verify ensureUserProfileExists() is called after auth

### Low Chat Creation Success Rate

1. Check if it's related to missing profiles
2. Look at chat_type parameter (DIRECT vs GROUP)
3. Check Firestore rules for chat creation
4. Verify group membership data

## Daily Monitoring Checklist

- [ ] Check user_not_found_error count (must be 0)
- [ ] Review profile_creation_failure events
- [ ] Check sign_in_success rate
- [ ] Review chat_creation_failure events
- [ ] Check Crashlytics for new exceptions
- [ ] Review error_type distribution

## Weekly Monitoring Checklist

- [ ] Analyze trends in success rates
- [ ] Review BigQuery data for patterns
- [ ] Check for any new error types
- [ ] Verify alerts are working
- [ ] Update success rate targets if needed

## Contact

For monitoring issues or questions:
- Check Firebase Console first
- Review Crashlytics for exceptions
- Check application logs in Logcat
- Review this guide for troubleshooting steps

## Related Documents

- [User Profile Initialization Fix - Requirements](/.kiro/specs/user-profile-initialization-fix/requirements.md)
- [User Profile Initialization Fix - Design](/.kiro/specs/user-profile-initialization-fix/design.md)
- [User Profile Initialization Fix - Tasks](/.kiro/specs/user-profile-initialization-fix/tasks.md)
- [Error Monitoring Guide](/ERROR_MONITORING_GUIDE.md)
