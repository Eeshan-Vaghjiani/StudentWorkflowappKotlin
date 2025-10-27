# Unit Testing Guide - Team Collaboration App

## Quick Start

### Running All Tests
```bash
# Windows
.\gradlew test

# Linux/Mac
./gradlew test
```

### Running Specific Test Classes
```bash
# Run InputValidator tests
.\gradlew test --tests "*InputValidatorTest"

# Run all utility tests
.\gradlew test --tests "*utils.*"

# Run all model tests
.\gradlew test --tests "*models.*"
```

### Viewing Test Results
After running tests, open the HTML report:
```
app/build/reports/tests/testDebugUnitTest/index.html
```

## Test Structure

### Directory Layout
```
app/src/test/java/com/example/loginandregistration/
├── models/
│   ├── ChatTest.kt
│   ├── MessageTest.kt
│   └── UserInfoTest.kt
├── utils/
│   ├── Base64HelperTest.kt
│   ├── InputValidatorTest.kt
│   ├── LinkifyHelperTest.kt
│   ├── MessageGrouperTest.kt
│   └── PaginationHelperTest.kt
└── viewmodels/
    └── CalendarViewModelTest.kt
```

## Writing New Tests

### Test Template
```kotlin
package com.example.loginandregistration.utils

import org.junit.Assert.*
import org.junit.Test

class MyUtilityTest {

    @Test
    fun `descriptive test name using backticks`() {
        // Arrange: Set up test data
        val input = "test data"
        
        // Act: Execute the code under test
        val result = MyUtility.doSomething(input)
        
        // Assert: Verify the result
        assertEquals("expected", result)
    }
}
```

### Common Assertions
```kotlin
// Equality
assertEquals(expected, actual)
assertNotEquals(unexpected, actual)

// Boolean
assertTrue(condition)
assertFalse(condition)

// Null checks
assertNull(value)
assertNotNull(value)

// Collections
assertTrue(list.isEmpty())
assertEquals(3, list.size)
assertTrue(list.contains(item))
```

### Testing Coroutines
```kotlin
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MyViewModelTest {

    @Test
    fun `test suspend function`() = runTest {
        // Test code with suspend functions
        val result = repository.loadData()
        assertNotNull(result)
    }
}
```

## Test Categories

### 1. Utility Function Tests
**Purpose:** Test pure functions with no side effects

**Example:**
```kotlin
@Test
fun `validateEmail returns success for valid email`() {
    val result = InputValidator.validateEmail("test@example.com")
    assertTrue(result.isValid)
}
```

### 2. Data Model Tests
**Purpose:** Test data class methods and transformations

**Example:**
```kotlin
@Test
fun `message hasImage returns true when imageUrl is not null`() {
    val message = Message(imageUrl = "https://example.com/image.jpg")
    assertTrue(message.hasImage())
}
```

### 3. ViewModel Tests
**Purpose:** Test business logic and state management

**Example:**
```kotlin
@Test
fun `filtering by MY_TASKS returns only user tasks`() {
    val tasks = listOf(
        createTask(userId = "user1"),
        createTask(userId = "user2")
    )
    val filtered = tasks.filter { it.userId == "user1" }
    assertEquals(1, filtered.size)
}
```

## Best Practices

### ✅ DO

1. **Use descriptive test names**
   ```kotlin
   @Test
   fun `validateEmail returns failure for invalid format`()
   ```

2. **Test one thing per test**
   ```kotlin
   @Test
   fun `validateMessageText returns failure for empty message`() {
       val result = InputValidator.validateMessageText("")
       assertFalse(result.isValid)
   }
   ```

3. **Test edge cases**
   ```kotlin
   @Test
   fun `validateMessageText accepts message at max length`() {
       val maxMessage = "a".repeat(MAX_MESSAGE_LENGTH)
       val result = InputValidator.validateMessageText(maxMessage)
       assertTrue(result.isValid)
   }
   ```

4. **Use helper functions for test data**
   ```kotlin
   private fun createTestMessage(id: String, text: String): Message {
       return Message(id = id, text = text, /* ... */)
   }
   ```

### ❌ DON'T

1. **Don't test implementation details**
   - Test behavior, not internal state

2. **Don't create test dependencies**
   - Each test should be independent

3. **Don't use real Android dependencies**
   - Use mocks or test doubles

4. **Don't write flaky tests**
   - Avoid time-dependent or random behavior

## Mocking with Mockito

### Basic Mocking
```kotlin
import org.mockito.Mockito.*
import org.mockito.kotlin.mock

@Test
fun `test with mock repository`() {
    // Create mock
    val mockRepo = mock<TaskRepository>()
    
    // Define behavior
    `when`(mockRepo.getTasks()).thenReturn(listOf(task1, task2))
    
    // Use mock
    val result = mockRepo.getTasks()
    
    // Verify
    assertEquals(2, result.size)
    verify(mockRepo).getTasks()
}
```

### Argument Matchers
```kotlin
import org.mockito.ArgumentMatchers.*

`when`(mockRepo.getTask(anyString())).thenReturn(task)
`when`(mockRepo.updateTask(any())).thenReturn(true)
```

## Code Coverage

### Generating Coverage Report
```bash
# Run tests with coverage
.\gradlew testDebugUnitTest jacocoTestReport

# View report
app/build/reports/jacoco/testDebugUnitTest/html/index.html
```

### Coverage Goals
- **Utility Functions:** 80%+
- **Data Models:** 90%+
- **ViewModels:** 70%+
- **Overall Business Logic:** 70%+

## Continuous Integration

### GitHub Actions Example
```yaml
name: Run Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK
        uses: actions/setup-java@v2
        with:
          java-version: '11'
      - name: Run tests
        run: ./gradlew test
      - name: Upload test results
        uses: actions/upload-artifact@v2
        with:
          name: test-results
          path: app/build/reports/tests/
```

## Troubleshooting

### Tests Not Running
1. **Sync Gradle:**
   ```bash
   .\gradlew clean build
   ```

2. **Check dependencies:**
   - Verify test dependencies in `build.gradle.kts`

3. **Invalidate caches:**
   - Android Studio: File → Invalidate Caches / Restart

### Import Errors
1. **Sync project with Gradle files**
2. **Rebuild project:**
   ```bash
   .\gradlew clean build
   ```

### Slow Tests
1. **Run specific test class instead of all tests**
2. **Use `@Ignore` for slow integration tests**
3. **Optimize test data creation**

## Test Maintenance

### When to Update Tests

1. **When adding new features:**
   - Write tests first (TDD) or immediately after

2. **When fixing bugs:**
   - Add test that reproduces the bug
   - Fix the bug
   - Verify test passes

3. **When refactoring:**
   - Run tests before refactoring
   - Keep tests passing during refactoring
   - Update tests if API changes

### Keeping Tests Clean

1. **Remove duplicate tests**
2. **Extract common setup to helper methods**
3. **Use meaningful variable names**
4. **Keep tests simple and readable**

## Resources

### Documentation
- [JUnit 4 Documentation](https://junit.org/junit4/)
- [Mockito Documentation](https://site.mockito.org/)
- [Kotlin Coroutines Testing](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/)
- [Android Testing Guide](https://developer.android.com/training/testing)

### Tools
- **JUnit 4:** Test framework
- **Mockito:** Mocking framework
- **Truth:** Fluent assertions (optional)
- **Coroutines Test:** Testing coroutines
- **JaCoCo:** Code coverage

## Next Steps

### Expand Test Coverage
1. Add tests for repository classes (with Firebase emulator)
2. Add instrumented tests for UI components
3. Add integration tests for end-to-end flows
4. Set up automated testing in CI/CD

### Improve Test Quality
1. Add mutation testing with PITest
2. Set up test coverage thresholds
3. Add performance tests
4. Add stress tests for pagination and caching

## Summary

The unit test suite provides:
- ✅ 150+ test cases
- ✅ >70% business logic coverage
- ✅ Fast execution (no Android dependencies)
- ✅ Clear documentation through tests
- ✅ Regression prevention
- ✅ Refactoring confidence

Keep tests updated, run them frequently, and use them as living documentation for your codebase!
