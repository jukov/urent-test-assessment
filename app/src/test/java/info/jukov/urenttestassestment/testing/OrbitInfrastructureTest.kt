package info.jukov.urenttestassestment.testing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.test.test

/** Verifies Orbit's state/effect assertions work with the selected coroutine test runtime. */
class OrbitInfrastructureTest {
    @Test
    fun `orbit can test asynchronous states and side effects on JVM`() = runTest {
        TestContainer(backgroundScope).test(this) {
            containerHost.load()
            expectState { true }
            expectSideEffect("loaded")
        }
    }

    private class TestContainer(scope: CoroutineScope) : ContainerHost<Boolean, String> {
        override val container = scope.container<Boolean, String>(false)

        fun load() = intent {
            delay(300)
            reduce { true }
            postSideEffect("loaded")
        }
    }
}
