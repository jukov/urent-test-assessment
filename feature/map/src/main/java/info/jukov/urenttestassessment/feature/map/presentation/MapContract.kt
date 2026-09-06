package info.jukov.urenttestassessment.feature.map.presentation

public data class MapCityUi(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val population: Long,
    val countryCode: String = ""
)

public enum class MapLoadState { Loading, Content, Empty, Error }

public data class MapState(
    // Cities from the latest successful response, retained during loading and errors.
    val cities: List<MapCityUi> = emptyList(),
    val loadState: MapLoadState = MapLoadState.Loading,
    val selectedCity: MapCityUi? = null
)

public data class MapCamera(val latitude: Double = 55.75204, val longitude: Double = 37.61781, val zoom: Double = 9.5)
