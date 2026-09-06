package info.jukov.urenttestassessment.feature.cities.data

import info.jukov.urenttestassessment.feature.cities.domain.CitiesError
import info.jukov.urenttestassessment.feature.cities.domain.CitiesResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class DefaultCitiesRepositoryTest {
    @Test
    fun `sends expected parameters and maps response`() = runTest {
        lateinit var request: HttpRequestData
        val repository = repository { captured ->
            request = captured
            respond(
                """
                {
                  "items": [{"id": 1, "name": "Moscow", "country": "RU",
                    "lat": 55.75, "lon": 37.61, "pop": 13000000}],
                  "limit": 20, "page": 2, "total": 30
                }
                """.trimIndent(),
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val result = repository.cities("Mos", 2, 20) as CitiesResult.Success
        assertEquals("/api/cities", request.url.encodedPath)
        assertEquals("Mos", request.url.parameters["query"])
        assertEquals("2", request.url.parameters["page"])
        assertEquals("Moscow", result.value.items.single().name)
        assertEquals(30L, result.value.total)
    }

    @Test
    fun `omits empty query parameter`() = runTest {
        lateinit var request: HttpRequestData
        val repository = repository { captured ->
            request = captured
            respond(
                """{"items":[],"limit":20,"page":1,"total":0}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        assertTrue(repository.cities(null, 1, 20) is CitiesResult.Success)
        assertFalse(request.url.parameters.contains("query"))
    }

    private val clients = mutableListOf<HttpClient>()

    @After
    fun closeClients() = clients.forEach(HttpClient::close)

    @Test
    fun `http errors become server failures`() = runTest {
        for (status in listOf(
            HttpStatusCode.BadRequest,
            HttpStatusCode.Unauthorized,
            HttpStatusCode.TooManyRequests,
            HttpStatusCode.InternalServerError
        )) {
            val repository = repository { respond("error", status) }
            assertEquals(CitiesResult.Failure(CitiesError.Server), repository.cities(null, 1, 20))
        }
    }

    @Test
    fun `malformed and structurally invalid responses become invalid response failures`() = runTest {
        for (body in listOf("not json", """{"items":[{"id":1}],"page":1,"limit":20}""")) {
            val repository = repository {
                respond(body, headers = headersOf(HttpHeaders.ContentType, "application/json"))
            }
            assertEquals(CitiesResult.Failure(CitiesError.InvalidResponse), repository.cities(null, 1, 20))
        }
    }

    @Test
    fun `connection and timeout exceptions become network failures`() = runTest {
        for (exception in listOf(IOException("offline"), SocketTimeoutException("timeout"))) {
            val repository = repository { throw exception }
            assertEquals(CitiesResult.Failure(CitiesError.Network), repository.cities(null, 1, 20))
        }
    }

    @Test
    fun `unexpected exception becomes unknown failure`() = runTest {
        val repository = repository { throw IllegalStateException("unexpected") }
        assertEquals(CitiesResult.Failure(CitiesError.Unknown), repository.cities(null, 1, 20))
    }

    @Test
    fun `cancellation escapes repository without becoming a failure`() = runTest {
        val cancellation = CancellationException("search replaced")
        val repository = repository { throw cancellation }
        try {
            repository.cities(null, 1, 20)
            fail("Expected cancellation")
        } catch (actual: CancellationException) {
            assertEquals(cancellation.message, actual.message)
        }
    }

    private fun repository(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData
    ): DefaultCitiesRepository {
        val client = HttpClient(MockEngine { request -> handler(request) }) {
            expectSuccess = true
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        clients += client
        return DefaultCitiesRepository(CitiesApi(client))
    }
}
