package info.jukov.urenttestassestment.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRuleTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `main dispatcher uses the runTest virtual clock`() = runTest {
        assertSame(mainDispatcherRule.dispatcher.scheduler, testScheduler)
        var completed = false
        launch(Dispatchers.Main) {
            delay(300)
            completed = true
        }

        advanceTimeBy(300)
        assertFalse(completed)
        runCurrent()
        assertTrue(completed)
    }

    @Test
    fun `cancelled main dispatcher work cannot complete after advancing time`() = runTest {
        var completed = false
        val job = launch(Dispatchers.Main) {
            delay(300)
            completed = true
        }
        runCurrent()
        job.cancel()

        advanceTimeBy(300)
        runCurrent()
        assertFalse(completed)
        assertTrue(job.isCancelled)
    }
}
