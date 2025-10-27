package com.example.loginandregistration

import android.util.Log
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for fragment lifecycle crash fixes Tests Requirements: 7.1, 7.2, 7.3
 *
 * These tests verify:
 * - Rapid navigation between fragments doesn't cause crashes
 * - Navigation during loading states is handled safely
 * - Error scenarios during navigation don't crash the app
 */
@RunWith(AndroidJUnit4::class)
class FragmentLifecycleTest {

    private lateinit var auth: FirebaseAuth

    companion object {
        private const val TAG = "FragmentLifecycleTest"
    }

    @Before
    fun setup() {
        auth = FirebaseAuth.getInstance()

        // Ensure user is authenticated
        if (auth.currentUser == null) {
            fail("Test requires authenticated user. Please sign in before running tests.")
        }

        Log.d(TAG, "Test setup complete. User: ${auth.currentUser?.uid}")
    }

    @After
    fun cleanup() {
        Log.d(TAG, "Test cleanup complete")
    }

    /**
     * Test 1: Rapid navigation to and from HomeFragment Requirement: 7.1 - Manual testing shall
     * verify no crashes occur during navigation
     */
    @Test
    fun testRapidNavigationHomeFragment_doesNotCrash() = runBlocking {
        repeat(5) { iteration ->
            Log.d(TAG, "HomeFragment rapid navigation iteration: ${iteration + 1}")

            // Launch fragment
            val scenario =
                    launchFragmentInContainer<HomeFragment>(
                            themeResId = R.style.Theme_LoginAndRegistration
                    )

            // Wait briefly for initialization
            delay(100)

            // Move to STARTED state (simulating navigation away)
            scenario.moveToState(Lifecycle.State.STARTED)
            delay(50)

            // Move to CREATED state (view destroyed)
            scenario.moveToState(Lifecycle.State.CREATED)
            delay(50)

            // Close scenario
            scenario.close()
            delay(100)
        }

        Log.d(TAG, "HomeFragment rapid navigation test completed successfully")
    }

    /**
     * Test 2: Rapid navigation to and from ChatFragment Requirement: 7.1 - Manual testing shall
     * verify no crashes occur during navigation
     */
    @Test
    fun testRapidNavigationChatFragment_doesNotCrash() = runBlocking {
        repeat(5) { iteration ->
            Log.d(TAG, "ChatFragment rapid navigation iteration: ${iteration + 1}")

            val scenario =
                    launchFragmentInContainer<ChatFragment>(
                            themeResId = R.style.Theme_LoginAndRegistration
                    )

            delay(100)
            scenario.moveToState(Lifecycle.State.STARTED)
            delay(50)
            scenario.moveToState(Lifecycle.State.CREATED)
            delay(50)
            scenario.close()
            delay(100)
        }

        Log.d(TAG, "ChatFragment rapid navigation test completed successfully")
    }

    /**
     * Test 3: Rapid navigation to and from GroupsFragment Requirement: 7.1 - Manual testing shall
     * verify no crashes occur during navigation
     */
    @Test
    fun testRapidNavigationGroupsFragment_doesNotCrash() = runBlocking {
        repeat(5) { iteration ->
            Log.d(TAG, "GroupsFragment rapid navigation iteration: ${iteration + 1}")

            val scenario =
                    launchFragmentInContainer<GroupsFragment>(
                            themeResId = R.style.Theme_LoginAndRegistration
                    )

            delay(100)
            scenario.moveToState(Lifecycle.State.STARTED)
            delay(50)
            scenario.moveToState(Lifecycle.State.CREATED)
            delay(50)
            scenario.close()
            delay(100)
        }

        Log.d(TAG, "GroupsFragment rapid navigation test completed successfully")
    }

    /**
     * Test 4: Rapid navigation to and from TasksFragment Requirement: 7.1 - Manual testing shall
     * verify no crashes occur during navigation
     */
    @Test
    fun testRapidNavigationTasksFragment_doesNotCrash() = runBlocking {
        repeat(5) { iteration ->
            Log.d(TAG, "TasksFragment rapid navigation iteration: ${iteration + 1}")

            val scenario =
                    launchFragmentInContainer<TasksFragment>(
                            themeResId = R.style.Theme_LoginAndRegistration
                    )

            delay(100)
            scenario.moveToState(Lifecycle.State.STARTED)
            delay(50)
            scenario.moveToState(Lifecycle.State.CREATED)
            delay(50)
            scenario.close()
            delay(100)
        }

        Log.d(TAG, "TasksFragment rapid navigation test completed successfully")
    }

    /**
     * Test 5: Rapid navigation to and from ProfileFragment Requirement: 7.1 - Manual testing shall
     * verify no crashes occur during navigation
     */
    @Test
    fun testRapidNavigationProfileFragment_doesNotCrash() = runBlocking {
        repeat(5) { iteration ->
            Log.d(TAG, "ProfileFragment rapid navigation iteration: ${iteration + 1}")

            val scenario =
                    launchFragmentInContainer<ProfileFragment>(
                            themeResId = R.style.Theme_LoginAndRegistration
                    )

            delay(100)
            scenario.moveToState(Lifecycle.State.STARTED)
            delay(50)
            scenario.moveToState(Lifecycle.State.CREATED)
            delay(50)
            scenario.close()
            delay(100)
        }

        Log.d(TAG, "ProfileFragment rapid navigation test completed successfully")
    }

    /**
     * Test 6: Navigation during loading state - HomeFragment Requirement: 7.2 - Navigating away
     * from HomeFragment while loading shall not crash
     */
    @Test
    fun testNavigationDuringLoadingHomeFragment_doesNotCrash() = runBlocking {
        Log.d(TAG, "Testing navigation during loading for HomeFragment")

        val scenario =
                launchFragmentInContainer<HomeFragment>(
                        themeResId = R.style.Theme_LoginAndRegistration
                )

        // Immediately navigate away (simulating user navigating during data load)
        delay(50)
        scenario.moveToState(Lifecycle.State.CREATED)
        delay(100)
        scenario.close()

        Log.d(TAG, "Navigation during loading test completed successfully")
    }

    /**
     * Test 7: Multiple fragments lifecycle stress test Requirement: 7.1, 7.2 - Rapid navigation
     * between all fragments
     */
    @Test
    fun testMultipleFragmentsRapidNavigation_doesNotCrash() = runBlocking {
        Log.d(TAG, "Testing rapid navigation across multiple fragments")

        val fragments =
                listOf(
                        HomeFragment::class.java,
                        ChatFragment::class.java,
                        GroupsFragment::class.java,
                        TasksFragment::class.java,
                        ProfileFragment::class.java
                )

        repeat(3) { iteration ->
            Log.d(TAG, "Multi-fragment navigation iteration: ${iteration + 1}")

            fragments.forEach { fragmentClass ->
                val scenario =
                        launchFragmentInContainer(
                                fragmentClass = fragmentClass,
                                themeResId = R.style.Theme_LoginAndRegistration
                        )

                delay(50)
                scenario.moveToState(Lifecycle.State.CREATED)
                delay(50)
                scenario.close()
            }
        }

        Log.d(TAG, "Multi-fragment rapid navigation test completed successfully")
    }

    /**
     * Test 8: Fragment recreation (configuration change simulation) Requirement: 7.3 - Error
     * scenarios during navigation don't crash
     */
    @Test
    fun testFragmentRecreation_doesNotCrash() = runBlocking {
        Log.d(TAG, "Testing fragment recreation (simulating configuration change)")

        val scenario =
                launchFragmentInContainer<HomeFragment>(
                        themeResId = R.style.Theme_LoginAndRegistration
                )

        delay(200)

        // Simulate configuration change by recreating
        scenario.recreate()
        delay(200)

        // Navigate away
        scenario.moveToState(Lifecycle.State.CREATED)
        delay(100)
        scenario.close()

        Log.d(TAG, "Fragment recreation test completed successfully")
    }
}
