# Task 44: Lifecycle-Aware Listeners Testing Guide

## Overview
This guide provides comprehensive testing procedures to verify that Firestore listeners are properly managed throughout the app's lifecycle.

## Prerequisites
- Android Studio with Android Profiler
- Physical device or emulator running Android API 23+
- Logcat access
- Firebase project configured
- At least 2 test accounts for multi-device testing

## Test Categories

### 1. Logcat Verification Tests

#### Test 1.1: Fragment Lifecycle Logging
**Objective**: Verify lifecycle methods are called correctly

**Steps**:
1. Open Android Studio Logcat
2. Filter by tag: `ChatFragment`
3. Launch the app and navigate to Chat tab
4. Observe logs:
   ```
   ChatFragment: onStart - Firestore listeners will be attached
   ```
5. Press Home button (app goes to background)
6. Observe logs:
   ```
   ChatFragment: onStop - Firestore listeners will be detached
   ```
7. Return to app
8. Observe logs:
   ```
   ChatFragment: onStart - Firestore listeners will be attached
   ```

**Expected Result**: ✅ Logs show proper lifecycle transitions

**Repeat for**:
- TasksFragment
- GroupsFragment

#### Test 1.2: ViewModel Lifecycle Logging
**Objective**: Verify ViewModels are properly initialized and cleared

**Steps**:
1. Filter Logcat by tag: `ChatViewModel`
2. Navigate to Chat tab
3. Observe log:
   ```
   ChatViewModel: ChatViewModel initialized
   ```
4. Navigate away from Chat tab (destroy fragment)
5. Observe log:
   ```
   ChatViewModel: ChatViewModel cleared - all coroutines will be cancelled
   ```

**Expected Result**: ✅ ViewModel properly initialized and cleared

**Repeat for**:
- ChatRoomViewModel

#### Test 1.3: Data Flow Logging
**Objective**: Verify data is received when listeners are active

**Steps**:
1. Filter Logcat by tag: `ChatFragment`
2. Navigate to Chat tab
3. Observe log:
   ```
   ChatFragment: Received X chats
   ```
4. Send a message from another device
5. Observe log showing updated chat count
6. Put app in background
7. Send another message from another device
8. Observe: No new logs (listener detached)
9. Return to app
10. Observe log showing updated chat count (listener reattached)

**Expected Result**: ✅ Data updates only when app is in foreground

### 2. Memory Leak Tests

#### Test 2.1: Memory Profiler - Fragment Navigation
**Objective**: Verify no memory leaks when navigating between fragments

**Steps**:
1. Open Android Profiler → Memory
2. Start memory recording
3. Navigate to Chat tab
4. Wait 5 seconds
5. Navigate to Tasks tab
6. Wait 5 seconds
7. Repeat steps 3-6 ten times
8. Force garbage collection (trash icon in profiler)
9. Analyze memory graph

**Expected Result**: 
✅ Memory usage stabilizes after garbage collection
✅ No continuous memory growth
✅ No leaked Fragment instances

**How to Check for Leaks**:
1. In Memory Profiler, click "Dump Java Heap"
2. Wait for heap dump to load
3. Filter by class name: `ChatFragment`
4. Check instance count
5. Should see 0-1 instances (current fragment only)

#### Test 2.2: Memory Profiler - Listener Cleanup
**Objective**: Verify Firestore listeners are cleaned up

**Steps**:
1. Open Android Profiler → Memory
2. Capture heap dump
3. Search for: `ListenerRegistration`
4. Note the count
5. Navigate between fragments 5 times
6. Capture another heap dump
7. Search for: `ListenerRegistration`
8. Compare counts

**Expected Result**: 
✅ Listener count doesn't continuously grow
✅ Old listeners are garbage collected

#### Test 2.3: LeakCanary Integration (Optional)
**Objective**: Automated memory leak detection

**Setup**:
```gradle
dependencies {
    debugImplementation 'com.squareup.leakcanary:leakcanary-android:2.12'
}
```

**Steps**:
1. Install debug build with LeakCanary
2. Navigate between fragments multiple times
3. Put app in background
4. Return to app
5. Check for LeakCanary notifications

**Expected Result**: ✅ No memory leak notifications

### 3. Network Activity Tests

#### Test 3.1: Network Profiler - Background Behavior
**Objective**: Verify network activity stops when app is backgrounded

**Steps**:
1. Open Android Profiler → Network
2. Start network recording
3. Navigate to Chat tab
4. Observe network activity (Firestore connections)
5. Put app in background (Home button)
6. Wait 10 seconds
7. Observe network activity

**Expected Result**: 
✅ Network activity drops significantly after ~5 seconds
✅ No continuous Firestore polling in background

#### Test 3.2: Network Profiler - Foreground Behavior
**Objective**: Verify network activity resumes when app returns to foreground

**Steps**:
1. Continue from Test 3.1
2. Return app to foreground
3. Observe network activity

**Expected Result**: 
✅ Network activity resumes immediately
✅ Firestore connections reestablished

### 4. Functional Tests

#### Test 4.1: Real-Time Updates - Foreground
**Objective**: Verify real-time updates work when app is in foreground

**Setup**: Two devices with different accounts

**Steps**:
1. Device A: Open Chat tab
2. Device B: Send a message to Device A
3. Device A: Verify message appears immediately

**Expected Result**: ✅ Message appears in real-time

#### Test 4.2: Real-Time Updates - Background
**Objective**: Verify updates are received when returning from background

**Setup**: Two devices with different accounts

**Steps**:
1. Device A: Open Chat tab
2. Device A: Put app in background
3. Device B: Send a message to Device A
4. Wait 5 seconds
5. Device A: Return app to foreground
6. Device A: Verify message appears

**Expected Result**: ✅ Message appears when app returns to foreground

#### Test 4.3: Task Statistics Updates
**Objective**: Verify task statistics update correctly

**Steps**:
1. Navigate to Tasks tab
2. Note current statistics (overdue, due today, completed)
3. Create a new task with today's due date
4. Verify "Due Today" count increases
5. Put app in background
6. Create another task (via another device or web)
7. Return app to foreground
8. Verify statistics are updated

**Expected Result**: ✅ Statistics update correctly in both scenarios

#### Test 4.4: Group Statistics Updates
**Objective**: Verify group statistics update correctly

**Steps**:
1. Navigate to Groups tab
2. Note current group count
3. Join a new group
4. Verify group count increases
5. Put app in background
6. Leave the group (via another device or web)
7. Return app to foreground
8. Verify group count decreases

**Expected Result**: ✅ Statistics update correctly in both scenarios

### 5. Edge Case Tests

#### Test 5.1: Rapid Fragment Switching
**Objective**: Verify no crashes or memory issues with rapid navigation

**Steps**:
1. Rapidly switch between tabs 20 times
2. Observe app behavior
3. Check Logcat for errors

**Expected Result**: 
✅ No crashes
✅ No ANR (Application Not Responding)
✅ Smooth navigation

#### Test 5.2: Configuration Changes
**Objective**: Verify listeners survive configuration changes

**Steps**:
1. Navigate to Chat tab
2. Rotate device (portrait ↔ landscape)
3. Verify chats are still displayed
4. Send a message from another device
5. Verify message appears

**Expected Result**: 
✅ Data persists after rotation
✅ Listeners still work after rotation

#### Test 5.3: Low Memory Conditions
**Objective**: Verify app handles low memory gracefully

**Steps**:
1. Enable "Don't keep activities" in Developer Options
2. Navigate to Chat tab
3. Press Home button
4. Open several other apps
5. Return to app
6. Verify app recreates properly

**Expected Result**: 
✅ App recreates without crashes
✅ Data is reloaded
✅ Listeners reattach

#### Test 5.4: Network Interruption
**Objective**: Verify app handles network loss gracefully

**Steps**:
1. Navigate to Chat tab with active internet
2. Verify chats load
3. Turn off WiFi/mobile data
4. Observe app behavior
5. Turn on internet again
6. Observe app behavior

**Expected Result**: 
✅ App shows offline indicator
✅ Cached data still visible
✅ Listeners reconnect when online

### 6. Performance Tests

#### Test 6.1: Startup Time
**Objective**: Measure app startup time

**Steps**:
1. Force stop app
2. Use `adb shell am start -W` to measure startup
3. Record time
4. Repeat 5 times
5. Calculate average

**Expected Result**: ✅ Consistent startup times (no degradation)

#### Test 6.2: Battery Usage
**Objective**: Measure battery impact

**Steps**:
1. Charge device to 100%
2. Use app normally for 1 hour
3. Check battery usage in Settings
4. Compare with previous version (if available)

**Expected Result**: ✅ Battery usage is reasonable (< 5% per hour of active use)

#### Test 6.3: CPU Usage
**Objective**: Verify CPU usage is optimized

**Steps**:
1. Open Android Profiler → CPU
2. Start CPU recording
3. Navigate between tabs
4. Put app in background
5. Wait 30 seconds
6. Return to foreground
7. Analyze CPU usage

**Expected Result**: 
✅ CPU usage drops to near-zero when backgrounded
✅ CPU spikes only during active operations

### 7. Automated Tests (Optional)

#### Test 7.1: Espresso UI Test
```kotlin
@Test
fun testFragmentLifecycle() {
    // Launch activity
    val scenario = launchFragmentInContainer<ChatFragment>()
    
    // Verify fragment is started
    scenario.onFragment { fragment ->
        assertTrue(fragment.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
    }
    
    // Move to stopped state
    scenario.moveToState(Lifecycle.State.CREATED)
    
    // Verify listeners are detached (check logs or internal state)
    
    // Move back to started state
    scenario.moveToState(Lifecycle.State.STARTED)
    
    // Verify listeners are reattached
}
```

#### Test 7.2: ViewModel Test
```kotlin
@Test
fun testViewModelCleanup() {
    val viewModel = ChatViewModel()
    
    // Verify ViewModel is initialized
    assertNotNull(viewModel)
    
    // Simulate clearing
    viewModel.onCleared()
    
    // Verify coroutines are cancelled (check internal state)
}
```

## Test Results Template

### Test Execution Report

**Date**: ___________
**Tester**: ___________
**Device**: ___________
**Android Version**: ___________
**App Version**: ___________

| Test ID | Test Name | Status | Notes |
|---------|-----------|--------|-------|
| 1.1 | Fragment Lifecycle Logging | ☐ Pass ☐ Fail | |
| 1.2 | ViewModel Lifecycle Logging | ☐ Pass ☐ Fail | |
| 1.3 | Data Flow Logging | ☐ Pass ☐ Fail | |
| 2.1 | Memory Profiler - Fragment Navigation | ☐ Pass ☐ Fail | |
| 2.2 | Memory Profiler - Listener Cleanup | ☐ Pass ☐ Fail | |
| 3.1 | Network Profiler - Background | ☐ Pass ☐ Fail | |
| 3.2 | Network Profiler - Foreground | ☐ Pass ☐ Fail | |
| 4.1 | Real-Time Updates - Foreground | ☐ Pass ☐ Fail | |
| 4.2 | Real-Time Updates - Background | ☐ Pass ☐ Fail | |
| 4.3 | Task Statistics Updates | ☐ Pass ☐ Fail | |
| 4.4 | Group Statistics Updates | ☐ Pass ☐ Fail | |
| 5.1 | Rapid Fragment Switching | ☐ Pass ☐ Fail | |
| 5.2 | Configuration Changes | ☐ Pass ☐ Fail | |
| 5.3 | Low Memory Conditions | ☐ Pass ☐ Fail | |
| 5.4 | Network Interruption | ☐ Pass ☐ Fail | |
| 6.1 | Startup Time | ☐ Pass ☐ Fail | |
| 6.2 | Battery Usage | ☐ Pass ☐ Fail | |
| 6.3 | CPU Usage | ☐ Pass ☐ Fail | |

**Overall Result**: ☐ Pass ☐ Fail

**Issues Found**:
1. ___________
2. ___________
3. ___________

**Recommendations**:
1. ___________
2. ___________
3. ___________

## Common Issues and Solutions

### Issue 1: Listeners Not Detaching
**Symptoms**: Network activity continues in background
**Solution**: Verify `collectWithLifecycle` is used instead of `lifecycleScope.launch`

### Issue 2: Data Not Updating After Resume
**Symptoms**: Stale data shown when returning from background
**Solution**: Verify `viewLifecycleOwner` is used, not `this`

### Issue 3: Memory Leaks
**Symptoms**: Memory usage grows continuously
**Solution**: Verify ViewModels use `viewModelScope`, not `GlobalScope`

### Issue 4: Crashes on Configuration Change
**Symptoms**: App crashes on rotation
**Solution**: Verify ViewModel is properly scoped to Fragment/Activity

## Conclusion

Complete all tests in this guide to ensure lifecycle-aware listeners are working correctly. Pay special attention to memory profiler tests as they directly verify the absence of memory leaks.

If all tests pass, the implementation successfully prevents memory leaks and optimizes resource usage.
