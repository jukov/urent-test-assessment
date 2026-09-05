package info.jukov.urenttestassestment.core.network

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.pluginOrNull
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class HttpClientFactoryTest {
    @Test
    fun `configures base url and json accept header`() = runTest {
        val engine = MockEngine { request ->
            assertEquals("http://example.test/api/cities?page=2", request.url.toString())
            assertEquals("application/json", request.headers[HttpHeaders.Accept])
            respond(
                content = "{}",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = createHttpClient(
            engine = engine,
            config = NetworkConfig(baseUrl = "http://example.test/")
        )

        assertEquals("{}", client.get("api/cities?page=2").bodyAsText())

        client.close()
    }

    @Test
    fun `installs logging only for debug clients`() {
        val releaseClient = createHttpClient(MockEngine { respond("{}") })
        val debugClient = createHttpClient(
            engine = MockEngine { respond("{}") },
            debugLogging = true
        )

        assertNull(releaseClient.pluginOrNull(Logging))
        assertNotNull(debugClient.pluginOrNull(Logging))

        releaseClient.close()
        debugClient.close()
    }

    @Test
    fun `redacts query and does not log response body`() = runTest {
        val messages = mutableListOf<String>()
        val client = createHttpClient(
            engine = MockEngine {
                respond(
                    content = "sensitive response body",
                    headers = headersOf(
                        HttpHeaders.ContentType to listOf("text/plain"),
                        HttpHeaders.SetCookie to listOf("session=response-secret")
                    )
                )
            },
            debugLogging = true,
            logger = object : Logger {
                override fun log(message: String) {
                    messages += message
                }
            }
        )

        client.get("api/cities?query=secret") {
            header("X-Api-Key", "request-secret")
        }.bodyAsText()

        val log = messages.joinToString("\n")
        assertFalse(log.contains("query=secret"))
        assertFalse(log.contains("sensitive response body"))
        assertFalse(log.contains("request-secret"))
        assertFalse(log.contains("response-secret"))
        client.close()
    }
}
