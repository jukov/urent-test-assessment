package info.jukov.urenttestassessment.startup

import info.jukov.urenttestassessment.testing.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.orbitmvi.orbit.test.test

@OptIn(ExperimentalCoroutinesApi::class)
class StartupViewModelTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Test
    fun `startup waits for system result before admitting content`() = runTest {
        val result = CompletableDeferred<Boolean>()
        StartupViewModel { result.await() }.test(this) {
            expectInitialState()
            runOnCreate()
            runCurrent()
            assertEquals(StartupState.Checking, containerHost.container.stateFlow.value)
            result.complete(true)
            expectState { StartupState.Ready }
        }
    }

    @Test
    fun `offline retry checks again and admits only after success`() = runTest {
        var available = false
        var calls = 0
        StartupViewModel {
            calls++
            available
        }.test(this) {
            expectInitialState()
            runOnCreate()
            expectState { StartupState.Offline }
            containerHost.retry()
            expectState { StartupState.Retrying }
            expectState { StartupState.Offline }
            available = true
            containerHost.retry()
            expectState { StartupState.Retrying }
            expectState { StartupState.Ready }
            assertEquals(3, calls)
            // A later loss of connectivity or stale retry cannot revoke admission.
            available = false
            containerHost.retry()
            runCurrent()
            assertEquals(3, calls)
            assertEquals(StartupState.Ready, containerHost.container.stateFlow.value)
        }
    }

    @Test
    fun `repeated taps do not start concurrent checks`() = runTest {
        val retryResult = CompletableDeferred<Boolean>()
        var calls = 0
        StartupViewModel { if (++calls == 1) false else retryResult.await() }.test(this) {
            expectInitialState()
            runOnCreate()
            expectState { StartupState.Offline }
            repeat(5) { containerHost.retry() }
            expectState { StartupState.Retrying }
            runCurrent()
            assertEquals(2, calls)
            retryResult.complete(true)
            expectState { StartupState.Ready }
        }
    }
}
