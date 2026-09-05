package info.jukov.urenttestassestment.integration

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/** A test-only client: production API/repository contract tests will live in their owning modules. */
class HttpClientInfrastructureTest {
    @Test
    fun `mock engine intercepts requests and content negotiation decodes JSON`() = runTest {
        val engine = MockEngine { request ->
            assertEquals(HttpMethod.Get, request.method)
            assertEquals("/api/probe", request.url.encodedPath)
            assertEquals("New York", request.url.parameters["query"])
            respond(
                content = """{"status":"ok"}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        HttpClient(engine) {
            install(ContentNegotiation) { json() }
        }.use { client ->
            val body = client.get("https://example.invalid/api/probe") {
                url { parameters.append("query", "New York") }
            }.body<JsonObject>()
            assertEquals("ok", body.getValue("status").jsonPrimitive.content)
        }
    }

    @Test
    fun `mock HTTP errors propagate without a real server`() {
        val engine = MockEngine {
            respond("Unavailable", HttpStatusCode.ServiceUnavailable)
        }
        HttpClient(engine) { expectSuccess = true }.use { client ->
            val error = assertThrows(ServerResponseException::class.java) {
                runTest { client.get("https://example.invalid/api/probe") }
            }
            assertEquals(HttpStatusCode.ServiceUnavailable, error.response.status)
        }
    }
}
