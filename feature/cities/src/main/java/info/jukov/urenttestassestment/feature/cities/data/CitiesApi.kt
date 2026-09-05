package info.jukov.urenttestassestment.feature.cities.data

import info.jukov.urenttestassestment.core.network.NetworkResult
import info.jukov.urenttestassestment.core.network.safeRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter

internal class CitiesApi(private val client: HttpClient) {
    suspend fun cities(query: String?, page: Int, limit: Int): NetworkResult<CitiesResponseDto> = safeRequest {
        client.get("api/cities") {
            query?.let { parameter("query", it) }
            parameter("page", page)
            parameter("limit", limit)
        }
    }
}
