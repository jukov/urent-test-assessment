package info.jukov.urenttestassestment.feature.cities.presentation

import androidx.lifecycle.SavedStateHandle
import info.jukov.urenttestassestment.core.model.City
import info.jukov.urenttestassestment.core.pagination.Page
import info.jukov.urenttestassestment.feature.cities.domain.CitiesError
import info.jukov.urenttestassestment.feature.cities.domain.CitiesRepository
import info.jukov.urenttestassestment.feature.cities.domain.CitiesResult
import java.util.Locale
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.orbitmvi.orbit.test.test

@OptIn(ExperimentalCoroutinesApi::class)
class CitiesViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `initial load publishes formatted content`() = runTest(dispatcher) {
        val repository = FakeRepository(mutableListOf(success(city(1, "Moscow", "RU"), total = 1)))
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        val content = viewModel.container.stateFlow.first {
            it.content is CitiesContentState.Content
        }.content as CitiesContentState.Content
        assertEquals("Moscow, Russia", content.cities.single().title)
        assertEquals(CitiesPaginationState.EndReached, content.pagination)
        assertEquals(Request(null, 1, 20), repository.requests.single())
    }

    @Test
    fun `search is debounced and trims query`() = runTest(dispatcher) {
        val repository = FakeRepository(mutableListOf(success(city(1), total = 1), success(city(2), total = 1)))
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        val input = "  Mos  "
        viewModel.onQueryChanged(input)
        runCurrent()
        assertEquals(input, viewModel.container.stateFlow.value.query)
        advanceTimeBy(299)
        runCurrent()
        assertEquals(1, repository.requests.size)
        advanceTimeBy(1)
        advanceUntilIdle()

        assertEquals("Mos", repository.requests.last().query)
        assertEquals(input, viewModel.container.stateFlow.value.query)
    }

    @Test
    fun `pagination error preserves existing cities and retries same page`() = runTest(dispatcher) {
        val repository = FakeRepository(
            mutableListOf(
                success(city(1), total = 40),
                CitiesResult.Failure(CitiesError.Network),
                success(city(2), page = 2, total = 40)
            )
        )
        val viewModel = viewModel(repository)
        advanceUntilIdle()
        viewModel.loadNextPage()
        advanceUntilIdle()

        val failed = viewModel.container.stateFlow.first {
            (it.content as? CitiesContentState.Content)?.pagination is CitiesPaginationState.Error
        }.content as CitiesContentState.Content
        assertEquals(listOf(1L), failed.cities.map(CityUi::id))
        assertTrue(failed.pagination is CitiesPaginationState.Error)

        viewModel.retry()
        advanceUntilIdle()
        val recovered = viewModel.container.stateFlow.first {
            (it.content as? CitiesContentState.Content)?.cities?.size == 2
        }.content as CitiesContentState.Content
        assertEquals(listOf(1L, 2L), recovered.cities.map(CityUi::id))
        assertEquals(listOf(1, 2, 2), repository.requests.map(Request::page))
    }

    @Test
    fun `initial error can be retried`() = runTest(dispatcher) {
        val repository =
            FakeRepository(mutableListOf(CitiesResult.Failure(CitiesError.Server), success(city(1), total = 1)))
        val viewModel = viewModel(repository)
        advanceUntilIdle()
        assertTrue(
            viewModel.container.stateFlow.first {
                it.content is CitiesContentState.Error
            }.content is CitiesContentState.Error
        )

        viewModel.retry()
        advanceUntilIdle()
        assertTrue(
            viewModel.container.stateFlow.first {
                it.content is CitiesContentState.Content
            }.content is CitiesContentState.Content
        )
    }

    @Test
    fun `equivalent normalized input does not cancel initial load`() = runTest(dispatcher) {
        val pending = CompletableDeferred<CitiesResult<Page<City>>>()
        val repository = DeferredRepository(pending)
        val viewModel = viewModel(repository)
        runCurrent()

        viewModel.onQueryChanged("   ")
        pending.complete(success(city(1), total = 1))

        val content = viewModel.container.stateFlow.first {
            it.content is CitiesContentState.Content
        }.content as CitiesContentState.Content
        assertEquals(listOf(1L), content.cities.map(CityUi::id))
        assertEquals(1, repository.requests.size)
    }

    @Test
    fun `equivalent normalized input does not cancel pagination`() = runTest(dispatcher) {
        val initial = CompletableDeferred<CitiesResult<Page<City>>>()
        val next = CompletableDeferred<CitiesResult<Page<City>>>()
        val repository = DeferredRepository(initial, next)
        val viewModel = viewModel(repository)
        runCurrent()
        initial.complete(success(city(1), total = 40))
        viewModel.container.stateFlow.first { it.content is CitiesContentState.Content }

        viewModel.loadNextPage()
        runCurrent()
        viewModel.onQueryChanged("   ")
        next.complete(success(city(2), page = 2, total = 40))

        val content = viewModel.container.stateFlow.first {
            (it.content as? CitiesContentState.Content)?.cities?.size == 2
        }.content as CitiesContentState.Content
        assertEquals(listOf(1L, 2L), content.cities.map(CityUi::id))
    }

    @Test
    fun `restores query from saved state`() = runTest(dispatcher) {
        val repository = FakeRepository(mutableListOf(success(city(1), total = 1)))
        CitiesViewModel(
            repository,
            CountryNameFormatter(Locale.ENGLISH),
            SavedStateHandle(mapOf("cities_query" to "  Mos  "))
        )
        advanceUntilIdle()

        assertEquals("Mos", repository.requests.single().query)
    }

    @Test
    fun `city click emits navigation side effect through Orbit`() = runTest(dispatcher) {
        val pending = CompletableDeferred<CitiesResult<Page<City>>>()
        viewModel(DeferredRepository(pending)).test(this) {
            containerHost.onCityClick(7)
            expectSideEffect(CitiesSideEffect.OpenCity(7))
            cancelAndIgnoreRemainingItems()
        }
    }

    private fun viewModel(repository: CitiesRepository) =
        CitiesViewModel(repository, CountryNameFormatter(Locale.ENGLISH), SavedStateHandle())

    private fun city(id: Long, name: String = "City$id", country: String = "US") = City(id, name, country, 1, 0.0, 0.0)

    private fun success(city: City, page: Int = 1, total: Long) =
        CitiesResult.Success(Page(listOf(city), page, 20, total))

    private class FakeRepository(private val results: MutableList<CitiesResult<Page<City>>>) : CitiesRepository {
        val requests = mutableListOf<Request>()
        override suspend fun cities(query: String?, page: Int, limit: Int): CitiesResult<Page<City>> {
            requests += Request(query, page, limit)
            return results.removeAt(0)
        }
    }

    private class DeferredRepository(vararg results: CompletableDeferred<CitiesResult<Page<City>>>) : CitiesRepository {
        private val results = results.toMutableList()
        val requests = mutableListOf<Request>()

        override suspend fun cities(query: String?, page: Int, limit: Int): CitiesResult<Page<City>> {
            requests += Request(query, page, limit)
            return results.removeAt(0).await()
        }
    }

    private data class Request(val query: String?, val page: Int, val limit: Int)
}
