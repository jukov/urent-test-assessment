package info.jukov.urenttestassestment.core.network

import info.jukov.urenttestassestment.core.logging.Logger
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.okhttp.OkHttpEngine
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.pluginOrNull
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.headersOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HttpClientFactoryTest {
    @Test
    fun `default client uses OkHttp engine`() {
        createHttpClient().use { client ->
            assertTrue(client.engine is OkHttpEngine)
        }
    }

    @Test
    fun `closing default client closes its engine`() = runTest {
        val client = createHttpClient()
        val engineJob = requireNotNull(client.engine.coroutineContext[Job])

        client.close()

        withContext(Dispatchers.IO) {
            withTimeout(5_000) { engineJob.join() }
        }
        assertTrue(engineJob.isCompleted)
    }

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
    fun `debug logger includes query and request and response bodies`() = runTest {
        val logger = RecordingLogger()
        val client = createHttpClient(
            engine = MockEngine {
                respond(
                    content = "response body",
                    headers = headersOf(HttpHeaders.ContentType, "text/plain")
                )
            },
            config = NetworkConfig(),
            debugLogging = true,
            logger = logger
        )

        client.post("api/cities?query=Tomsk") {
            contentType(ContentType.Application.Json)
            setBody("request body")
        }.bodyAsText()

        val log = logger.messages.joinToString("\n")
        assertEquals("Ktor", logger.tag)
        assertTrue(log.contains("query=Tomsk"))
        assertTrue(log.contains("request body"))
        assertTrue(log.contains("response body"))
        client.close()
    }

    private class RecordingLogger : Logger {
        val messages = mutableListOf<String>()
        var tag: String? = null

        override fun verbose(message: String, vararg args: Any?) = Unit

        override fun debug(message: String, vararg args: Any?) {
            messages += message
        }

        override fun info(message: String, vararg args: Any?) = Unit

        override fun warn(message: String, vararg args: Any?) = Unit

        override fun warn(throwable: Throwable, message: String, vararg args: Any?) = Unit

        override fun error(message: String, vararg args: Any?) = Unit

        override fun error(throwable: Throwable, message: String, vararg args: Any?) = Unit

        override fun withTag(tag: String): Logger = apply { this.tag = tag }
    }
}
