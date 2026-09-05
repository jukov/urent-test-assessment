package info.jukov.urenttestassestment.feature.cities.presentation

public data class CityUi(val id: Long, val title: String)

public data class CitiesState(val query: String = "", val content: CitiesContentState = CitiesContentState.Initial)

public sealed interface CitiesContentState {
    public data object Initial : CitiesContentState
    public data object Loading : CitiesContentState
    public data class Content(val cities: List<CityUi>, val pagination: CitiesPaginationState) : CitiesContentState
    public data class Empty(val query: String) : CitiesContentState
    public data class Error(val message: String) : CitiesContentState
}

public sealed interface CitiesPaginationState {
    public data object Idle : CitiesPaginationState
    public data object Loading : CitiesPaginationState
    public data object EndReached : CitiesPaginationState
    public data class Error(val message: String) : CitiesPaginationState
}

public sealed interface CitiesSideEffect {
    public data class OpenCity(val cityId: Long) : CitiesSideEffect
}
