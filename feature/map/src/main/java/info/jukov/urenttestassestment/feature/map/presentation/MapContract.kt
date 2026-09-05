package info.jukov.urenttestassestment.feature.map.presentation

public data class MapCityUi(val id: Long, val name: String, val latitude: Double, val longitude: Double)

public enum class MapLoadState { Loading, Content, Empty, Error }

public data class MapState(
    // Cities accumulated during this screen lifetime; loadState describes the latest viewport request.
    val cities: List<MapCityUi> = emptyList(),
    val loadState: MapLoadState = MapLoadState.Loading
)

public data class MapCamera(val latitude: Double = 55.75204, val longitude: Double = 37.61781, val zoom: Double = 9.5)
