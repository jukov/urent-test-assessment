package info.jukov.urenttestassestment.feature.cities.presentation

import info.jukov.urenttestassestment.feature.cities.domain.CitiesError

public data class CityUi(val id: Long, val title: String)

public data class CitiesState(
    val query: String = "",
    val content: CitiesContentState = CitiesContentState.EmptyProgress
)

public sealed interface CitiesContentState {
    public data object EmptyProgress : CitiesContentState
    public data class Empty(val query: String) : CitiesContentState
    public data class EmptyFailure(val error: CitiesError) : CitiesContentState
    public data class Content(val cities: List<CityUi>) : CitiesContentState
    public data class ContentProgress(val cities: List<CityUi>) : CitiesContentState
    public data class ContentFailure(val cities: List<CityUi>, val error: CitiesError) : CitiesContentState
    public data class ContentRefreshing(val cities: List<CityUi>) : CitiesContentState
    public data class ContentEndReached(val cities: List<CityUi>) : CitiesContentState
}

public sealed interface CitiesSideEffect {
    public data class OpenCity(val cityId: Long) : CitiesSideEffect
    public data class ShowRefreshFailure(val error: CitiesError) : CitiesSideEffect
}
