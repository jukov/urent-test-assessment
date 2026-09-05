package info.jukov.urenttestassestment.core.network

import io.ktor.client.request.get
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.utils.io.readByte
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OkHttpCancellationTest {
    @Test
    fun `cancelling an active response preserves cancellation and allows the next request`() = runTest {
        withContext(Dispatchers.IO) {
            MockWebServer().use { server ->
                server.start()
                createHttpClient(NetworkConfig(baseUrl = server.url("/").toString())).use { client ->
                    repeat(3) {
                        server.enqueue(
                            MockResponse.Builder()
                                .body("unfinished response")
                                .throttleBody(1, 1, TimeUnit.SECONDS)
                                .build()
                        )
                        val readingBody = CompletableDeferred<Unit>()
                        val request = launch {
                            client.prepareGet("api/cities?query=old").execute { response ->
                                val body = response.bodyAsChannel()
                                assertEquals('u'.code.toByte(), body.readByte())
                                readingBody.complete(Unit)
                                // The rest of the body is still arriving when this request is cancelled.
                                while (!body.isClosedForRead) {
                                    body.readByte()
                                }
                            }
                        }

                        withTimeout(5_000) { readingBody.await() }
                        assertFalse(request.isCompleted)
                        withTimeout(5_000) { request.cancelAndJoin() }
                        assertTrue(request.isCancelled)

                        server.enqueue(MockResponse.Builder().body("latest response").build())
                        assertEquals("latest response", client.get("api/cities?query=new").bodyAsText())
                    }
                }
            }
        }
    }
}
