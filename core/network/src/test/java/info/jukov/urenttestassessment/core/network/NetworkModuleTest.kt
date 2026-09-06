package info.jukov.urenttestassessment.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import kotlinx.coroutines.Job
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.koin.dsl.koinApplication

class NetworkModuleTest {
    @Test
    fun `provides singleton client and closes it with koin graph`() {
        val koinApplication = koinApplication {
            modules(
                createNetworkModule { createHttpClient(MockEngine { respond("{}") }) }
            )
        }

        val first = koinApplication.koin.get<HttpClient>()
        val second = koinApplication.koin.get<HttpClient>()

        assertSame(first, second)
        assertTrue(first.coroutineContext[Job]?.isActive == true)

        koinApplication.close()

        assertFalse(first.coroutineContext[Job]?.isActive == true)
    }
}
