package info.jukov.urenttestassessment.feature.cities.data

import kotlinx.serialization.Serializable

@Serializable
internal data class CitiesResponseDto(val items: List<CityDto>, val limit: Int, val page: Int, val total: Long? = null)

@Serializable
internal data class CityDto(
    val id: Long,
    val name: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    val pop: Long
)
