package info.jukov.urenttestassestment.feature.map.data

import info.jukov.urenttestassestment.feature.map.domain.Coordinate
import info.jukov.urenttestassestment.feature.map.domain.MapResult
import info.jukov.urenttestassestment.feature.map.domain.MapViewport
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultMapRepositoryTest {
    private val viewport = MapViewport(Coordinate(55.75, 37.62), 20000)

    @Test
    fun `map endpoint sends meters and coordinates and maps city fields`() = runTest {
        lateinit var request: HttpRequestData
        client(
            MockEngine {
                request = it
                respond(
                    """
                {"count":1,"items":[{"id":1,"name":"Moscow","country":"RU",
                  "lat":55.75,"lon":37.62,"pop":123}]}
                    """.trimIndent(),
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        ).use { client ->
            val city = (DefaultMapRepository(MapApi(client)).cities(viewport) as MapResult.Success).cities.single()
            assertEquals(HttpMethod.Get, request.method)
            assertEquals("/api/cities/map", request.url.encodedPath)
            assertEquals("55.75", request.url.parameters["centerLat"])
            assertEquals("37.62", request.url.parameters["centerLng"])
            assertEquals("20000", request.url.parameters["radius"])
            assertEquals("Moscow", city.name)
            assertEquals("RU", city.countryCode)
            assertEquals(123L, city.population)
            assertEquals(55.75, city.latitude, 0.0)
            assertEquals(37.62, city.longitude, 0.0)
        }
    }

    @Test
    fun `empty response is successful`() = runTest {
        client(
            MockEngine {
                respond("""{"count":0,"items":[]}""", headers = headersOf(HttpHeaders.ContentType, "application/json"))
            }
        ).use { client ->
            assertEquals(MapResult.Success(emptyList()), DefaultMapRepository(MapApi(client)).cities(viewport))
        }
    }

    @Test
    fun `http errors malformed json and transport errors become domain failure`() = runTest {
        val engines = listOf(
            MockEngine { respond("unavailable", HttpStatusCode.ServiceUnavailable) },
            MockEngine { respond("{broken", headers = headersOf(HttpHeaders.ContentType, "application/json")) },
            MockEngine { throw IOException("offline") }
        )
        engines.forEach { engine ->
            client(engine).use { client ->
                assertEquals(MapResult.Failure, DefaultMapRepository(MapApi(client)).cities(viewport))
            }
        }
    }

    @Test(expected = CancellationException::class)
    fun `cancellation is propagated`() = runTest {
        client(MockEngine { throw CancellationException() }).use { client ->
            DefaultMapRepository(MapApi(client)).cities(viewport)
        }
    }

    private fun client(engine: MockEngine) = HttpClient(engine) {
        expectSuccess = true
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }
}
