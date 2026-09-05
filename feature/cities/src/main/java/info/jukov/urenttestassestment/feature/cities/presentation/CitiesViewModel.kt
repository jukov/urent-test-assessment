package info.jukov.urenttestassestment.feature.cities.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.jukov.urenttestassestment.core.model.City
import info.jukov.urenttestassestment.core.pagination.PaginationState
import info.jukov.urenttestassestment.core.pagination.PaginationStateMachine
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
    private var debounceJob: Job? = null
    private var requestJob: Job? = null
    private var generation = 0
    private var activeQuery: String? = null
    private var latestQuery: String = savedStateHandle.get<String>(QUERY_KEY).orEmpty()

    init {
        loadInitial(savedStateHandle.get<String>(QUERY_KEY).orEmpty().trim())
    }

    public fun onQueryChanged(value: String) {
        latestQuery = value
        savedStateHandle[QUERY_KEY] = value
        intent { reduce { state.copy(query = value) } }
        val normalized = value.trim()
        debounceJob?.cancel()
        debounceJob = null
        if (normalized == activeQuery) return
        debounceJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MILLIS)
            debounceJob = null
            loadInitial(normalized)
        }
    }

    public fun loadNextPage() {
        val query = activeQuery ?: return
        if (!pagination.startAppend()) return
        publishState()
        requestPage(query, pagination.nextPage, generation)
    }

    public fun refresh() {
        val query = latestQuery.trim()
        if (!pagination.startRefresh()) return
        debounceJob?.cancel()
        debounceJob = null
        requestJob?.cancel()
        val currentGeneration = ++generation
        activeQuery = query
        publishState()
        requestPage(query, FIRST_PAGE, currentGeneration)
    }

    public fun retry() {
        when (container.stateFlow.value.content) {
            is CitiesContentState.EmptyFailure -> loadInitial(container.stateFlow.value.query.trim())
            is CitiesContentState.ContentFailure -> loadNextPage()
            else -> Unit
        }
    }

    public fun onCityClick(id: Long) = intent { postSideEffect(CitiesSideEffect.OpenCity(id)) }

    private fun loadInitial(query: String) {
        debounceJob?.cancel()
        debounceJob = null
        requestJob?.cancel()
        val expectedGeneration = ++generation
        activeQuery = query
        pagination.startInitial()
        publishState()
        requestPage(query, FIRST_PAGE, expectedGeneration)
    }

    private fun requestPage(query: String, page: Int, expectedGeneration: Int) {
        requestJob = viewModelScope.launch {
            when (val result = repository.cities(query.ifEmpty { null }, page, PAGE_SIZE)) {
                is CitiesResult.Success -> if (expectedGeneration == generation && query == activeQuery) {
                    pagination.append(result.value)
                    publishState()
                }

                is CitiesResult.Failure -> if (expectedGeneration == generation && query == activeQuery) {
                    pagination.fail(CitiesRequestException(result.error))
                    publishState()
                }
            }
        }
    }

    private fun publishState() = intent {
        reduce {
            state.copy(content = pagination.state.toUi(activeQuery.orEmpty(), countryFormatter))
        }
    }

    public companion object {
        public const val SEARCH_DEBOUNCE_MILLIS: Long = 300L
        public const val FIRST_PAGE: Int = 1
        public const val PAGE_SIZE: Int = 20
        private const val QUERY_KEY = "cities_query"
    }
}

private fun PaginationState<City>.toUi(query: String, formatter: CountryNameFormatter): CitiesContentState =
    when (this) {
        PaginationState.EmptyProgress -> CitiesContentState.EmptyProgress

        PaginationState.Empty -> CitiesContentState.Empty(query)

        is PaginationState.EmptyFailure -> CitiesContentState.EmptyFailure(errorCode(cause))

        is PaginationState.Content -> CitiesContentState.Content(items.map { it.toUi(formatter) })

        is PaginationState.ContentProgress -> CitiesContentState.ContentProgress(items.map { it.toUi(formatter) })

        is PaginationState.ContentFailure -> {
            CitiesContentState.ContentFailure(items.map { it.toUi(formatter) }, errorCode(cause))
        }

        is PaginationState.ContentRefreshing -> CitiesContentState.ContentRefreshing(items.map { it.toUi(formatter) })

        is PaginationState.ContentEndReached -> CitiesContentState.ContentEndReached(items.map { it.toUi(formatter) })
    }

private fun City.toUi(formatter: CountryNameFormatter) = CityUi(id, "$name, ${formatter.format(countryCode)}")

private fun errorCode(cause: Throwable): String = (cause as? CitiesRequestException)?.error?.userMessage() ?: "unknown"

private fun CitiesError.userMessage(): String = when (this) {
    CitiesError.Network -> "network"
    CitiesError.Server -> "server"
    CitiesError.InvalidResponse -> "invalid_response"
    CitiesError.Unknown -> "unknown"
}

private class CitiesRequestException(val error: CitiesError) : RuntimeException(error.toString())
