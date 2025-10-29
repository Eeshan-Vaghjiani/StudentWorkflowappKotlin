# Performance Monitoring Guide

## Overview

This guide provides comprehensive instructions for monitoring and optimizing performance metrics in the TeamSync Android app using Firebase Performance Monitoring and other tools.

## Performance Targets

### App Startup Time
- **Cold start:** < 2 seconds
- **Warm start:** < 1 second
- **Hot start:** < 0.5 seconds

### Screen Rendering
- **Screen load time:** < 1 second
- **Frame rate:** > 50 FPS (ideally 60 FPS)
- **Frozen frames:** < 1% of total frames
- **Slow frames:** < 5% of total frames

### Network Performance
- **API response time:** < 1 second
- **Success rate:** > 95%
- **Timeout rate:** < 2%
- **Retry rate:** < 5%

### Memory Usage
- **App memory:** < 200 MB (typical usage)
- **Memory leaks:** 0 detected
- **GC frequency:** < 10 per minute
- **OOM crashes:** 0

### Battery Usage
- **Background battery drain:** < 2% per hour
- **Foreground battery drain:** < 5% per hour
- **Wakelock duration:** < 1 minute per hour
- **Network usage:** Optimized (batch requests)

## Monitoring Tools

### 1. Firebase Performance Monitoring

**Purpose:** Track app performance metrics automatically

**Access:** https://console.firebase.google.com → Your Project → Performance

**Key Features:**
- Automatic trace collection
- Custom trace creation
- Network request monitoring
- Screen rendering metrics

**Setup:**
```kotlin
// Already configured in build.gradle.kts
implementation("com.google.firebase:firebase-perf-ktx")
```

**Custom Traces:**
```kotlin
// Measure specific operations
val trace = Firebase.performance.newTrace("task_creation")
trace.start()

try {
    // Perform operation
    createTask(task)
    trace.incrementMetric("success_count", 1)
} catch (e: Exception) {
    trace.incrementMetric("failure_count", 1)
} finally {
    trace.stop()
}
```

### 2. Android Studio Profiler

**Purpose:** Deep performance analysis during development

**Access:** Android Studio → View → Tool Windows → Profiler

**Profilers:**
- **CPU Profiler:** Identify slow methods
- **Memory Profiler:** Detect memory leaks
- **Network Profiler:** Analyze network traffic
- **Energy Profiler:** Monitor battery usage

**Usage:**
1. Connect device or emulator
2. Run app in debug mode
3. Open Profiler
4. Select profiler type
5. Record session
6. Analyze results

### 3. Google Play Console Vitals

**Purpose:** Monitor production performance

**Access:** https://play.google.com/console → Your App → Quality → Android vitals

**Metrics:**
- Crash rate
- ANR rate
- Excessive wakeups
- Stuck wake locks
- Slow rendering
- Frozen frames

### 4. Firebase Crashlytics

**Purpose:** Track performance-related crashes

**Access:** https://console.firebase.google.com → Your Project → Crashlytics

**Performance Issues:**
- OutOfMemoryError
- ANR (Application Not Responding)
- Timeout exceptions
- Resource exhaustion

## Key Performance Metrics

### 1. App Startup Time

**What to Monitor:**
- Time from app launch to first screen displayed
- Time to interactive (user can interact)
- Cold start vs warm start vs hot start

**Firebase Trace:**
```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        val trace = Firebase.performance.newTrace("app_startup")
        trace.start()
        
        // Initialize app components
        initializeFirebase()
        initializeRepositories()
        
        trace.stop()
    }
}
```

**Optimization Tips:**
- Defer non-critical initialization
- Use lazy initialization
- Avoid blocking operations in onCreate()
- Use SplashScreen API properly
- Minimize dependency injection overhead

**Target Breakdown:**
- Application.onCreate(): < 500ms
- First Activity.onCreate(): < 500ms
- First screen render: < 1000ms

### 2. Screen Rendering Performance

**What to Monitor:**
- Frame rate (FPS)
- Slow frames (> 16ms)
- Frozen frames (> 700ms)
- Jank (frame drops)

**Firebase Automatic Tracking:**
Firebase automatically tracks screen rendering for all activities and fragments.

**Manual Trace:**
```kotlin
class TasksFragment : Fragment() {
    private lateinit var screenTrace: Trace
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        screenTrace = Firebase.performance.newTrace("tasks_screen_load")
        screenTrace.start()
        
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            adapter.submitList(tasks)
            screenTrace.stop() // Stop when data is loaded
        }
    }
}
```

**Optimization Tips:**
- Use RecyclerView with DiffUtil
- Implement view recycling properly
- Avoid complex layouts (reduce nesting)
- Use ConstraintLayout for flat hierarchies
- Load images asynchronously
- Avoid overdraw
- Use hardware acceleration

### 3. Network Request Performance

**What to Monitor:**
- Request duration
- Success rate
- Failure rate
- Timeout rate
- Payload size

**Firebase Automatic Tracking:**
Firebase automatically tracks all HTTP/HTTPS requests.

**Custom Network Trace:**
```kotlin
suspend fun fetchTasks(): Result<List<Task>> {
    val trace = Firebase.performance.newTrace("fetch_tasks_api")
    trace.start()
    
    return try {
        val tasks = taskRepository.getTasks()
        trace.incrementMetric("success_count", 1)
        trace.putMetric("task_count", tasks.size.toLong())
        Result.success(tasks)
    } catch (e: Exception) {
        trace.incrementMetric("failure_count", 1)
        Result.failure(e)
    } finally {
        trace.stop()
    }
}
```

**Optimization Tips:**
- Use connection pooling
- Implement request caching
- Batch multiple requests
- Compress request/response data
- Use appropriate timeouts
- Implement retry logic
- Use CDN for static assets

### 4. Database Performance

**What to Monitor:**
- Query execution time
- Read/write latency
- Cache hit rate
- Index usage

**Firestore Performance:**
```kotlin
suspend fun getTasks(): List<FirebaseTask> {
    val trace = Firebase.performance.newTrace("firestore_get_tasks")
    trace.start()
    
    return try {
        val snapshot = firestore.collection("tasks")
            .whereEqualTo("userId", currentUserId)
            .get()
            .await()
        
        trace.putMetric("document_count", snapshot.size().toLong())
        snapshot.toObjects(FirebaseTask::class.java)
    } finally {
        trace.stop()
    }
}
```

**Optimization Tips:**
- Create appropriate indexes
- Limit query results
- Use pagination
- Implement local caching
- Batch write operations
- Use transactions for consistency
- Avoid N+1 query problems

### 5. Memory Usage

**What to Monitor:**
- Heap memory usage
- Native memory usage
- Memory leaks
- GC frequency
- Large object allocations

**Memory Profiling:**
```kotlin
// Use LeakCanary for leak detection (debug builds only)
debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")
```

**Optimization Tips:**
- Avoid memory leaks (use weak references)
- Release resources in onDestroy()
- Use appropriate image sizes
- Implement bitmap recycling
- Avoid static references to Context
- Use ViewBinding (auto-cleanup)
- Profile with Android Studio Memory Profiler

### 6. Battery Usage

**What to Monitor:**
- Background battery drain
- Foreground battery drain
- Wakelock usage
- Network usage patterns
- Location usage

**Battery Optimization:**
```kotlin
// Use WorkManager for background tasks
class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val trace = Firebase.performance.newTrace("background_sync")
        trace.start()
        
        return try {
            syncData()
            trace.incrementMetric("success_count", 1)
            Result.success()
        } catch (e: Exception) {
            trace.incrementMetric("failure_count", 1)
            Result.retry()
        } finally {
            trace.stop()
        }
    }
}
```

**Optimization Tips:**
- Use WorkManager for background tasks
- Batch network requests
- Use appropriate wakelock types
- Release wakelocks promptly
- Implement doze mode compatibility
- Use JobScheduler for deferred work
- Minimize location updates

## Performance Monitoring Dashboard

### Daily Performance Check

**Morning Review (5 minutes):**
1. Open Firebase Performance
2. Check app startup time (last 24h)
3. Review slow screens (> 1 second load time)
4. Check network request failures
5. Verify no performance regressions

**Key Questions:**
- Is startup time within target?
- Are any screens loading slowly?
- Are network requests succeeding?
- Are there any new performance issues?

### Weekly Performance Review

**Monday Review (30 minutes):**
1. Generate weekly performance report
2. Analyze trends (week over week)
3. Identify performance regressions
4. Prioritize optimization tasks
5. Update performance roadmap

**Metrics to Review:**
- App startup time trend
- Screen rendering performance
- Network request duration
- Memory usage patterns
- Battery usage trends
- User-perceived performance

### Monthly Performance Audit

**First Monday of Month (2 hours):**
1. Comprehensive performance analysis
2. Compare to previous month
3. Identify systemic issues
4. Plan optimization sprints
5. Update performance targets

**Deep Dive Areas:**
- Startup time optimization
- Screen rendering optimization
- Network optimization
- Memory optimization
- Battery optimization
- Database query optimization

## Performance Optimization Workflow

### Step 1: Identify Performance Issue

**Sources:**
- Firebase Performance alerts
- User complaints
- Play Console vitals
- Manual testing
- Profiler analysis

**Gather Data:**
- Affected screens/operations
- Performance metrics
- Device information
- User impact
- Frequency

### Step 2: Reproduce and Measure

**Reproduction:**
1. Set up test environment
2. Use same device/OS version
3. Follow exact steps
4. Measure with profiler
5. Document findings

**Measurement Tools:**
- Android Studio Profiler
- Firebase Performance
- Systrace
- Method tracing
- Memory profiler

### Step 3: Analyze Root Cause

**Common Causes:**
- Blocking UI thread
- Memory leaks
- Inefficient algorithms
- Excessive network requests
- Large image sizes
- Complex layouts
- Database query issues

**Analysis Techniques:**
- CPU profiling
- Memory profiling
- Network profiling
- Layout inspection
- Database query analysis

### Step 4: Implement Optimization

**Optimization Strategies:**
- Move work to background threads
- Implement caching
- Optimize layouts
- Reduce image sizes
- Batch operations
- Use efficient data structures
- Implement pagination

**Code Example:**
```kotlin
// Before: Blocking UI thread
fun loadTasks() {
    val tasks = repository.getTasks() // Blocks UI
    adapter.submitList(tasks)
}

// After: Background thread
fun loadTasks() {
    viewModelScope.launch {
        val tasks = withContext(Dispatchers.IO) {
            repository.getTasks() // Runs on background thread
        }
        adapter.submitList(tasks) // Updates UI on main thread
    }
}
```

### Step 5: Verify Improvement

**Verification:**
1. Measure performance after optimization
2. Compare to baseline
3. Test on multiple devices
4. Verify no regressions
5. Monitor in production

**Success Criteria:**
- Performance metric improved
- No new issues introduced
- User experience enhanced
- Targets met or exceeded

### Step 6: Monitor and Maintain

**Ongoing Monitoring:**
- Track optimized metric
- Watch for regressions
- Gather user feedback
- Update documentation
- Share learnings

## Performance Testing

### Manual Performance Testing

**Test Scenarios:**
1. **Cold Start Test**
   - Force stop app
   - Clear app data
   - Launch app
   - Measure time to first screen

2. **Warm Start Test**
   - Press home button
   - Wait 30 seconds
   - Relaunch app
   - Measure time to first screen

3. **Screen Load Test**
   - Navigate to each screen
   - Measure load time
   - Check for jank
   - Verify smooth scrolling

4. **Network Test**
   - Perform operations requiring network
   - Measure request duration
   - Test with slow network
   - Test offline behavior

5. **Memory Test**
   - Use app for 30 minutes
   - Navigate through all screens
   - Check memory usage
   - Look for memory leaks

### Automated Performance Testing

**Performance Test Example:**
```kotlin
@RunWith(AndroidJUnit4::class)
class PerformanceTest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    @Test
    fun testAppStartupTime() {
        val startTime = System.currentTimeMillis()
        
        activityRule.scenario.onActivity { activity ->
            // Wait for first screen to be fully loaded
            activity.findViewById<View>(R.id.main_content).waitUntilVisible()
        }
        
        val endTime = System.currentTimeMillis()
        val startupTime = endTime - startTime
        
        // Assert startup time is under 2 seconds
        assertTrue("Startup time too slow: ${startupTime}ms", startupTime < 2000)
    }
    
    @Test
    fun testScreenLoadTime() {
        val trace = Firebase.performance.newTrace("test_screen_load")
        trace.start()
        
        // Navigate to tasks screen
        onView(withId(R.id.nav_tasks)).perform(click())
        
        // Wait for tasks to load
        onView(withId(R.id.tasks_recycler_view)).check(matches(isDisplayed()))
        
        trace.stop()
        
        // Verify load time is under 1 second
        // (Check Firebase Performance console for results)
    }
}
```

## Performance Alerts

### Critical Performance Alerts

**App Startup Time > 3 seconds:**
- **Action:** Investigate immediately
- **Priority:** P0 (Critical)
- **Response Time:** < 4 hours

**Screen Load Time > 2 seconds:**
- **Action:** Investigate and optimize
- **Priority:** P1 (High)
- **Response Time:** < 24 hours

**Network Success Rate < 90%:**
- **Action:** Check service status, investigate failures
- **Priority:** P0 (Critical)
- **Response Time:** < 2 hours

**Memory Usage > 300 MB:**
- **Action:** Profile memory, find leaks
- **Priority:** P1 (High)
- **Response Time:** < 24 hours

### Warning Performance Alerts

**App Startup Time > 2 seconds:**
- **Action:** Monitor trend, plan optimization
- **Priority:** P2 (Medium)
- **Response Time:** < 1 week

**Frame Rate < 50 FPS:**
- **Action:** Profile rendering, optimize layouts
- **Priority:** P2 (Medium)
- **Response Time:** < 1 week

**Network Request Time > 2 seconds:**
- **Action:** Optimize queries, implement caching
- **Priority:** P2 (Medium)
- **Response Time:** < 1 week

## Performance Best Practices

### General Principles

1. **Measure First, Optimize Second**
   - Always measure before optimizing
   - Focus on actual bottlenecks
   - Verify improvements with data

2. **Optimize for User-Perceived Performance**
   - Prioritize visible operations
   - Show loading indicators
   - Provide immediate feedback

3. **Test on Real Devices**
   - Test on low-end devices
   - Test on different OS versions
   - Test with slow networks

4. **Monitor Continuously**
   - Track performance metrics
   - Set up alerts
   - Review regularly

5. **Iterate and Improve**
   - Make incremental improvements
   - Measure impact
   - Share learnings

### Code-Level Best Practices

**UI Thread:**
```kotlin
// ✗ Bad: Blocking UI thread
fun loadData() {
    val data = database.query() // Blocks UI
    updateUI(data)
}

// ✓ Good: Background thread
fun loadData() {
    viewModelScope.launch {
        val data = withContext(Dispatchers.IO) {
            database.query()
        }
        updateUI(data)
    }
}
```

**RecyclerView:**
```kotlin
// ✗ Bad: No view recycling
class MyAdapter : RecyclerView.Adapter<ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Complex operations on every bind
        holder.imageView.setImageBitmap(loadLargeImage())
    }
}

// ✓ Good: Efficient recycling
class MyAdapter : ListAdapter<Item, ViewHolder>(DiffCallback()) {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item) // Simple binding
        holder.imageView.load(item.imageUrl) // Async loading
    }
}
```

**Memory Management:**
```kotlin
// ✗ Bad: Memory leak
class MyFragment : Fragment() {
    private val handler = Handler() // Holds reference to Fragment
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        handler.postDelayed({ /* ... */ }, 10000)
    }
}

// ✓ Good: Proper cleanup
class MyFragment : Fragment() {
    private val handler = Handler()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        handler.postDelayed({ /* ... */ }, 10000)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null) // Cleanup
    }
}
```

## Resources

### Documentation
- [Firebase Performance Monitoring](https://firebase.google.com/docs/perf-mon)
- [Android Performance](https://developer.android.com/topic/performance)
- [Android Profiler](https://developer.android.com/studio/profile)
- [Play Console Vitals](https://developer.android.com/topic/performance/vitals)

### Tools
- Firebase Performance Console
- Android Studio Profiler
- Systrace
- LeakCanary
- StrictMode

### Best Practices
- [App Startup Time](https://developer.android.com/topic/performance/vitals/launch-time)
- [Rendering Performance](https://developer.android.com/topic/performance/rendering)
- [Memory Management](https://developer.android.com/topic/performance/memory)
- [Battery Optimization](https://developer.android.com/topic/performance/power)

## Appendix

### Performance Checklist

**Before Release:**
- [ ] App startup time < 2 seconds
- [ ] All screens load < 1 second
- [ ] Frame rate > 50 FPS
- [ ] No memory leaks detected
- [ ] Network requests optimized
- [ ] Images properly sized
- [ ] Database queries indexed
- [ ] Battery usage acceptable

**After Release:**
- [ ] Monitor Firebase Performance
- [ ] Check Play Console Vitals
- [ ] Review user feedback
- [ ] Track performance trends
- [ ] Address regressions quickly

### Common Performance Issues

| Issue | Symptom | Solution |
|-------|---------|----------|
| Slow startup | App takes > 2s to launch | Defer initialization, use lazy loading |
| Janky scrolling | Frame drops during scroll | Optimize RecyclerView, reduce layout complexity |
| High memory | App uses > 200 MB | Fix memory leaks, optimize images |
| Slow network | Requests take > 2s | Implement caching, optimize queries |
| Battery drain | High background usage | Use WorkManager, batch operations |
| ANR | App not responding | Move work off UI thread |
