package info.jukov.urenttestassessment.feature.map.data

import info.jukov.urenttestassessment.core.network.NetworkResult
import info.jukov.urenttestassessment.core.network.safeRequest
import info.jukov.urenttestassessment.feature.map.domain.MapViewport
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.Serializable

internal class MapApi(private val client: HttpClient) {
    suspend fun cities(viewport: MapViewport): NetworkResult<MapResponseDto> = safeRequest {
        client.get("api/cities/map") {
            parameter("centerLat", viewport.center.latitude)
            parameter("centerLng", viewport.center.longitude)
            parameter("radius", viewport.radius)
        }
    }
}

@Serializable
internal data class MapResponseDto(val items: List<MapCityDto>)

@Serializable
internal data class MapCityDto(
    val id: Long,
    val name: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    val pop: Long
)
