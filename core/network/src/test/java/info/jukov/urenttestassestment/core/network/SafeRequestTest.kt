package info.jukov.urenttestassestment.core.network

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test

class SafeRequestTest {
    @Test
    fun `deserializes successful response and ignores unknown fields`() = runTest {
        val client = clientResponding(
            body = """{"name":"Tomsk","ignored":true}"""
        )

        val result = safeRequest<TestPayload> { client.get("api/cities") }

        assertEquals(NetworkResult.Success(TestPayload("Tomsk")), result)
        client.close()
    }

    @Test
    fun `maps unsuccessful status without exposing response body`() = runTest {
        val engine = MockEngine {
            respondError(HttpStatusCode.InternalServerError, "sensitive response")
        }
        val client = createHttpClient(engine)

        val result = safeRequest<TestPayload> { client.get("api/cities?secret=value") }

        assertEquals(
            NetworkResult.Failure(NetworkError.Http(HttpStatusCode.InternalServerError.value)),
            result
        )
        client.close()
    }

    @Test
    fun `maps malformed json to serialization error`() = runTest {
        val client = clientResponding(body = "not-json")

        val result = safeRequest<TestPayload> { client.get("api/cities") }

        assertEquals(NetworkResult.Failure(NetworkError.Serialization), result)
        client.close()
    }

    @Test
    fun `maps io failure to connection error`() = runTest {
        val client = createHttpClient(MockEngine { throw IOException("offline") })

        val result = safeRequest<TestPayload> { client.get("api/cities") }

        assertEquals(NetworkResult.Failure(NetworkError.Connection), result)
        client.close()
    }

    @Test
    fun `maps ktor timeout failures to timeout error`() = runTest {
        val timeouts = listOf(
            HttpRequestTimeoutException("http://example.test", 1L),
            ConnectTimeoutException("http://example.test"),
            SocketTimeoutException("socket timed out")
        )

        timeouts.forEach { timeout ->
            val result = safeRequest<TestPayload> { throw timeout }

            assertEquals(NetworkResult.Failure(NetworkError.Timeout), result)
        }
    }

    @Test
    fun `does not swallow coroutine cancellation`() {
        val cancellation = CancellationException("cancelled")

        val thrown = assertThrows(CancellationException::class.java) {
            runTest {
                safeRequest<TestPayload> { throw cancellation }
            }
        }

        assertSame(cancellation, thrown)
    }

    private fun clientResponding(body: String) = createHttpClient(
        MockEngine {
            respond(
                content = body,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
    )

    @Serializable
    private data class TestPayload(val name: String)
}
