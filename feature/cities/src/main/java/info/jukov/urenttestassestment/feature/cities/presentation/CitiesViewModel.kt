package info.jukov.urenttestassestment.feature.cities.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.jukov.urenttestassestment.core.model.City
import info.jukov.urenttestassestment.core.pagination.PaginationEffect
import info.jukov.urenttestassestment.core.pagination.PaginationState
import info.jukov.urenttestassestment.core.pagination.PaginationStateMachine
import info.jukov.urenttestassestment.feature.cities.domain.CitiesError
import info.jukov.urenttestassestment.feature.cities.domain.CitiesRepository
import info.jukov.urenttestassestment.feature.cities.domain.CitiesResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.blockingIntent
import org.orbitmvi.orbit.viewmodel.container

public class CitiesViewModel(
    private val repository: CitiesRepository,
    private val countryFormatter: CountryNameFormatter,
    private val savedStateHandle: SavedStateHandle
) : ViewModel(),
    ContainerHost<CitiesState, CitiesSideEffect> {
    private val initialQuery = savedStateHandle.get<String>(QUERY_KEY).orEmpty()
    override val container = container<CitiesState, CitiesSideEffect>(
        CitiesState(query = initialQuery)
    )
    private val pagination = PaginationStateMachine<City, Long>(City::id)
    private var debounceJob: Job? = null
    private var requestJob: Job? = null
    private var generation = 0
    private var appliedQuery: String = initialQuery.trim()
    private var inputQuery: String = initialQuery

    init {
        loadInitial(appliedQuery)
    }

    public fun onQueryChanged(value: String) {
        inputQuery = value
        savedStateHandle[QUERY_KEY] = value
        blockingIntent { reduce { state.copy(query = value) } }
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MILLIS)
            debounceJob = null
            val normalized = value.trim()
            if (normalized != appliedQuery) loadInitial(normalized)
        }
    }

    public fun loadNextPage() {
        if (!pagination.startAppend()) return
        publishState()
        requestPage(appliedQuery, pagination.nextPage, generation)
    }

    public fun refresh() {
        val query = inputQuery.trim()
        if (!pagination.startRefresh()) return
        debounceJob?.cancel()
        debounceJob = null
        requestJob?.cancel()
        val currentGeneration = ++generation
        appliedQuery = query
        publishState()
        requestPage(query, FIRST_PAGE, currentGeneration)
    }

    public fun retry() {
        when (container.stateFlow.value.content) {
            is CitiesContentState.EmptyFailure -> loadInitial(inputQuery.trim())
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
        appliedQuery = query
        pagination.startInitial()
        publishState()
        requestPage(query, FIRST_PAGE, expectedGeneration)
    }

    private fun requestPage(query: String, page: Int, expectedGeneration: Int) {
        requestJob = viewModelScope.launch {
            when (val result = repository.cities(query.ifEmpty { null }, page, PAGE_SIZE)) {
                is CitiesResult.Success -> if (expectedGeneration == generation && query == appliedQuery) {
                    pagination.append(result.value)
                    publishState()
                }

                is CitiesResult.Failure -> if (expectedGeneration == generation && query == appliedQuery) {
                    val effect = pagination.fail(CitiesRequestException(result.error))
                    publishState()
                    if (effect is PaginationEffect.RefreshFailure) {
                        publishRefreshFailure(effect)
                    }
                }
            }
        }
    }

    private fun publishState() = intent {
        reduce {
            state.copy(
                query = inputQuery,
                content = pagination.state.toUi(appliedQuery, countryFormatter)
            )
        }
    }

    private fun publishRefreshFailure(effect: PaginationEffect.RefreshFailure) = intent {
        postSideEffect(CitiesSideEffect.ShowRefreshFailure(effect.cause.toCitiesError()))
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

        is PaginationState.EmptyFailure -> CitiesContentState.EmptyFailure(cause.toCitiesError())

        is PaginationState.Content -> CitiesContentState.Content(items.map { it.toUi(formatter) })

        is PaginationState.ContentProgress -> CitiesContentState.ContentProgress(items.map { it.toUi(formatter) })

        is PaginationState.ContentFailure -> {
            CitiesContentState.ContentFailure(items.map { it.toUi(formatter) }, cause.toCitiesError())
        }

        is PaginationState.ContentRefreshing -> CitiesContentState.ContentRefreshing(items.map { it.toUi(formatter) })

        is PaginationState.ContentEndReached -> CitiesContentState.ContentEndReached(items.map { it.toUi(formatter) })
    }

private fun City.toUi(formatter: CountryNameFormatter) = CityUi(id, "$name, ${formatter.format(countryCode)}")

private fun Throwable.toCitiesError(): CitiesError = (this as? CitiesRequestException)?.error ?: CitiesError.Unknown

private class CitiesRequestException(val error: CitiesError) : RuntimeException(error.toString())
