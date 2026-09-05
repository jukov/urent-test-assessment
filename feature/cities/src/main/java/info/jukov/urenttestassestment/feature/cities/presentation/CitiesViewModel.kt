package info.jukov.urenttestassestment.feature.cities.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.jukov.urenttestassestment.core.model.City
import info.jukov.urenttestassestment.core.pagination.PaginationStateMachine
import info.jukov.urenttestassestment.core.pagination.PaginationStatus
import info.jukov.urenttestassestment.feature.cities.domain.CitiesError
import info.jukov.urenttestassestment.feature.cities.domain.CitiesRepository
import info.jukov.urenttestassestment.feature.cities.domain.CitiesResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

public class CitiesViewModel(
    private val repository: CitiesRepository,
    private val countryFormatter: CountryNameFormatter,
    private val savedStateHandle: SavedStateHandle
) : ViewModel(),
    ContainerHost<CitiesState, CitiesSideEffect> {
    override val container = container<CitiesState, CitiesSideEffect>(
        CitiesState(query = savedStateHandle.get<String>(QUERY_KEY).orEmpty())
    )
    private val pagination = PaginationStateMachine<City, Long>(City::id)
    private var requestJob: Job? = null
    private var generation = 0
    private var activeQuery: String? = null

    init {
        loadInitial(savedStateHandle.get<String>(QUERY_KEY).orEmpty().trim())
    }

    public fun onQueryChanged(value: String) {
        savedStateHandle[QUERY_KEY] = value
        intent { reduce { state.copy(query = value) } }
        val normalized = value.trim()
        if (normalized == activeQuery) return
        requestJob?.cancel()
        val currentGeneration = ++generation
        requestJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MILLIS)
            requestJob = null
            loadInitial(normalized, currentGeneration)
        }
    }

    public fun loadNextPage() {
        val query = activeQuery ?: return
        if (!pagination.start()) return
        publishContent()
        requestPage(query, pagination.snapshot.nextPage, generation)
    }

    public fun retry() {
        when (container.stateFlow.value.content) {
            is CitiesContentState.Error -> loadInitial(container.stateFlow.value.query.trim())
            is CitiesContentState.Content -> loadNextPage()
            else -> Unit
        }
    }

    public fun onCityClick(id: Long) = intent { postSideEffect(CitiesSideEffect.OpenCity(id)) }

    private fun loadInitial(query: String, expectedGeneration: Int = ++generation) {
        requestJob?.cancel()
        activeQuery = query
        pagination.reset()
        intent { reduce { state.copy(content = CitiesContentState.Loading) } }
        check(pagination.start())
        requestPage(query, FIRST_PAGE, expectedGeneration)
    }

    private fun requestPage(query: String, page: Int, expectedGeneration: Int) {
        requestJob = viewModelScope.launch {
            when (val result = repository.cities(query.ifEmpty { null }, page, PAGE_SIZE)) {
                is CitiesResult.Success -> if (expectedGeneration == generation && query == activeQuery) {
                    pagination.append(result.value)
                    publishContent()
                }

                is CitiesResult.Failure -> if (expectedGeneration == generation && query == activeQuery) {
                    pagination.fail(CitiesRequestException(result.error))
                    publishFailure(result.error)
                }
            }
        }
    }

    private fun publishContent() = intent {
        val snapshot = pagination.snapshot
        val content = if (snapshot.items.isEmpty() && snapshot.status !is PaginationStatus.Loading) {
            CitiesContentState.Empty(activeQuery.orEmpty())
        } else if (snapshot.items.isEmpty()) {
            CitiesContentState.Loading
        } else {
            CitiesContentState.Content(snapshot.items.map(::toUi), snapshot.status.toUi())
        }
        reduce { state.copy(content = content) }
    }

    private fun publishFailure(error: CitiesError) = intent {
        val snapshot = pagination.snapshot
        val message = error.userMessage()
        val content = if (snapshot.items.isEmpty()) {
            CitiesContentState.Error(message)
        } else {
            CitiesContentState.Content(snapshot.items.map(::toUi), CitiesPaginationState.Error(message))
        }
        reduce { state.copy(content = content) }
    }

    private fun toUi(city: City) = CityUi(city.id, "${city.name}, ${countryFormatter.format(city.countryCode)}")

    private fun PaginationStatus.toUi() = when (this) {
        PaginationStatus.Idle -> CitiesPaginationState.Idle
        PaginationStatus.Loading -> CitiesPaginationState.Loading
        PaginationStatus.EndReached -> CitiesPaginationState.EndReached
        is PaginationStatus.Error -> CitiesPaginationState.Error(cause.message.orEmpty())
    }

    private fun CitiesError.userMessage() = when (this) {
        CitiesError.Network -> "network"
        CitiesError.Server -> "server"
        CitiesError.InvalidResponse -> "invalid_response"
        CitiesError.Unknown -> "unknown"
    }

    private class CitiesRequestException(error: CitiesError) : RuntimeException(error.toString())

    public companion object {
        public const val SEARCH_DEBOUNCE_MILLIS: Long = 300L
        public const val FIRST_PAGE: Int = 1
        public const val PAGE_SIZE: Int = 20
        private const val QUERY_KEY = "cities_query"
    }
}
