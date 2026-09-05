package info.jukov.urenttestassestment.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.Description
import org.junit.runners.model.Statement

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRuleTest {
    @Test
    fun `rule installs the supplied dispatcher and resets it after success`() {
        val dispatcher = StandardTestDispatcher()
        val rule = MainDispatcherRule(dispatcher)

        try {
            rule.evaluate {
                runTest { assertSame(dispatcher.scheduler, testScheduler) }
            }
            assertNotSame(dispatcher.scheduler, StandardTestDispatcher().scheduler)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `rule resets its dispatcher and preserves the failure when a test throws`() {
        val rule = MainDispatcherRule()
        val failure = AssertionError("Test failure")

        try {
            val actual = assertThrows(AssertionError::class.java) {
                rule.evaluate {
                    runTest { assertSame(rule.dispatcher.scheduler, testScheduler) }
                    throw failure
                }
            }
            assertSame(failure, actual)
            assertNotSame(rule.dispatcher.scheduler, StandardTestDispatcher().scheduler)
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun MainDispatcherRule.evaluate(action: () -> Unit) {
        val statement = object : Statement() {
            override fun evaluate() = action()
        }
        apply(statement, Description.EMPTY).evaluate()
    }
}
