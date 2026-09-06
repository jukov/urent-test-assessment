package info.jukov.urenttestassessment.feature.cities.presentation

import androidx.lifecycle.SavedStateHandle
import info.jukov.urenttestassessment.core.model.City
import info.jukov.urenttestassessment.core.pagination.Page
import info.jukov.urenttestassessment.core.ui.city.CityDetails
import info.jukov.urenttestassessment.core.ui.city.CountryNameFormatter
import info.jukov.urenttestassessment.feature.cities.domain.CitiesError
import info.jukov.urenttestassessment.feature.cities.domain.CitiesRepository
import info.jukov.urenttestassessment.feature.cities.domain.CitiesResult
import java.util.Locale
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.orbitmvi.orbit.test.test

@OptIn(ExperimentalCoroutinesApi::class)
class CitiesViewModelTest {
    private lateinit var dispatcher: TestDispatcher

    @Before
    fun setUp() {
        dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `initial load publishes formatted content`() = runTest(dispatcher) {
        val repository = FakeRepository(mutableListOf(success(city(1, "Moscow", "RU"), total = 1)))
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        val content = viewModel.container.stateFlow.first {
            it.content is CitiesContentState.ContentEndReached
        }.content as CitiesContentState.ContentEndReached
        assertEquals("Moscow, Russia", content.cities.single().title)
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
        advanceTimeBy(299)
        runCurrent()
        assertEquals(1, repository.requests.size)
        advanceTimeBy(1)
        advanceUntilIdle()

        assertEquals("Mos", repository.requests.last().query)
        assertEquals(input, viewModel.container.stateFlow.first { it.query == input }.query)
    }

    @Test
    fun `query state is updated synchronously`() = runTest(dispatcher) {
        val repository = FakeRepository(mutableListOf(success(city(1), total = 1), success(city(2), total = 1)))
        val viewModel = viewModel(repository)

        viewModel.onQueryChanged("Moscow")

        assertEquals("Moscow", viewModel.container.stateFlow.value.query)
    }

    @Test
    fun `every raw query change restarts debounce`() = runTest(dispatcher) {
        val repository = FakeRepository(mutableListOf(success(city(1), total = 1), success(city(2), total = 1)))
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        viewModel.onQueryChanged("New ")
        runCurrent()
        advanceTimeBy(200)
        viewModel.onQueryChanged("New  ")
        runCurrent()
        advanceTimeBy(299)
        runCurrent()
        assertEquals(1, repository.requests.size)

        advanceTimeBy(1)
        advanceUntilIdle()
        assertEquals("New", repository.requests.last().query)
        assertEquals(2, repository.requests.size)
    }

    @Test
    fun `returning to active query cancels pending search`() = runTest(dispatcher) {
        val repository = FakeRepository(mutableListOf(success(city(1), total = 1)))
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        viewModel.onQueryChanged("Paris")
        runCurrent()
        viewModel.onQueryChanged("   ")
        runCurrent()
        advanceTimeBy(CitiesViewModel.SEARCH_DEBOUNCE_MILLIS)
        advanceUntilIdle()

        assertEquals(1, repository.requests.size)
    }

    @Test
    fun `returning to active query keeps pending initial request`() = runTest(dispatcher) {
        val initial = CompletableDeferred<CitiesResult<Page<City>>>()
        val repository = DeferredRepository(initial)
        val viewModel = viewModel(repository)
        runCurrent()

        viewModel.onQueryChanged("Paris")
        runCurrent()
        viewModel.onQueryChanged("   ")
        runCurrent()
        initial.complete(success(city(1), total = 1))

        viewModel.container.stateFlow.first { it.content is CitiesContentState.ContentEndReached }
        advanceTimeBy(CitiesViewModel.SEARCH_DEBOUNCE_MILLIS)
        advanceUntilIdle()
        assertEquals(1, repository.requests.size)
    }

    @Test
    fun `returning to active query keeps pending pagination request`() = runTest(dispatcher) {
        val initial = CompletableDeferred<CitiesResult<Page<City>>>()
        val next = CompletableDeferred<CitiesResult<Page<City>>>()
        val repository = DeferredRepository(initial, next)
        val viewModel = viewModel(repository)
        runCurrent()
        initial.complete(success(city(1), total = 40))
        viewModel.container.stateFlow.first { it.content is CitiesContentState.Content }
        viewModel.loadNextPage()
        runCurrent()

        viewModel.onQueryChanged("Paris")
        runCurrent()
        viewModel.onQueryChanged("   ")
        runCurrent()
        next.complete(success(city(2), page = 2, total = 40))

        val content = viewModel.container.stateFlow.first {
            (it.content as? CitiesContentState.ContentEndReached)?.cities?.size == 2
        }.content as CitiesContentState.ContentEndReached
        assertEquals(listOf(1L, 2L), content.cities.map(CityUi::id))
        assertEquals(2, repository.requests.size)
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
            it.content is CitiesContentState.ContentFailure
        }.content as CitiesContentState.ContentFailure
        assertEquals(listOf(1L), failed.cities.map(CityUi::id))

        viewModel.retry()
        advanceUntilIdle()
        val recovered = viewModel.container.stateFlow.first {
            (it.content as? CitiesContentState.ContentEndReached)?.cities?.size == 2
        }.content as CitiesContentState.ContentEndReached
        assertEquals(listOf(1L, 2L), recovered.cities.map(CityUi::id))
        assertEquals(listOf(1, 2, 2), repository.requests.map(Request::page))
    }

    @Test
    fun `refresh replaces content after pagination failure`() = runTest(dispatcher) {
        val repository = FakeRepository(
            mutableListOf(
                success(city(1), total = 40),
                CitiesResult.Failure(CitiesError.Network),
                success(city(2), total = 40)
            )
        )
        val viewModel = viewModel(repository)
        advanceUntilIdle()
        viewModel.loadNextPage()
        advanceUntilIdle()
        viewModel.refresh()

        val content = viewModel.container.stateFlow.first {
            (it.content as? CitiesContentState.Content)?.cities?.singleOrNull()?.id == 2L
        }.content as CitiesContentState.Content
        assertEquals(listOf(2L), content.cities.map(CityUi::id))
        assertEquals(listOf(1, 2, 1), repository.requests.map(Request::page))
    }

    @Test
    fun `initial error can be retried`() = runTest(dispatcher) {
        val repository =
            FakeRepository(mutableListOf(CitiesResult.Failure(CitiesError.Server), success(city(1), total = 1)))
        val viewModel = viewModel(repository)
        advanceUntilIdle()
        val failure = viewModel.container.stateFlow.first {
            it.content is CitiesContentState.EmptyFailure
        }.content as CitiesContentState.EmptyFailure
        assertEquals(CitiesError.Server, failure.error)

        viewModel.retry()
        advanceUntilIdle()
        assertTrue(
            viewModel.container.stateFlow.first {
                it.content is CitiesContentState.ContentEndReached
            }.content is CitiesContentState.ContentEndReached
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
            it.content is CitiesContentState.ContentEndReached
        }.content as CitiesContentState.ContentEndReached
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
            (it.content as? CitiesContentState.ContentEndReached)?.cities?.size == 2
        }.content as CitiesContentState.ContentEndReached
        assertEquals(listOf(1L, 2L), content.cities.map(CityUi::id))
    }

    @Test
    fun `refresh keeps content visible and replaces first page`() = runTest(dispatcher) {
        val initial = CompletableDeferred<CitiesResult<Page<City>>>()
        val refresh = CompletableDeferred<CitiesResult<Page<City>>>()
        val viewModel = viewModel(DeferredRepository(initial, refresh))
        runCurrent()
        initial.complete(success(city(1), total = 40))
        viewModel.container.stateFlow.first { it.content is CitiesContentState.Content }

        viewModel.refresh()
        val refreshing = viewModel.container.stateFlow.first {
            it.content is CitiesContentState.ContentRefreshing
        }.content as CitiesContentState.ContentRefreshing
        assertEquals(listOf(1L), refreshing.cities.map(CityUi::id))

        refresh.complete(success(city(2), total = 40))
        val refreshed = viewModel.container.stateFlow.first {
            it.content is CitiesContentState.Content
        }.content as CitiesContentState.Content
        assertEquals(listOf(2L), refreshed.cities.map(CityUi::id))
    }

    @Test
    fun `refresh failure restores content and emits error side effect`() = runTest(dispatcher) {
        val repository = FakeRepository(
            mutableListOf(
                success(city(1), total = 40),
                CitiesResult.Failure(CitiesError.Network)
            )
        )
        val viewModel = viewModel(repository)
        advanceUntilIdle()
        val sideEffect = async(start = CoroutineStart.UNDISPATCHED) {
            viewModel.container.sideEffectFlow.first()
        }

        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(CitiesSideEffect.ShowRefreshFailure(CitiesError.Network), sideEffect.await())
        val content = viewModel.container.stateFlow.value.content as CitiesContentState.Content
        assertEquals(listOf(1L), content.cities.map(CityUi::id))
    }

    @Test
    fun `refresh uses pending query and clears content from previous query`() = runTest(dispatcher) {
        val initial = CompletableDeferred<CitiesResult<Page<City>>>()
        val refresh = CompletableDeferred<CitiesResult<Page<City>>>()
        val repository = DeferredRepository(initial, refresh)
        val viewModel = viewModel(repository)
        runCurrent()
        initial.complete(success(city(1), total = 40))
        viewModel.container.stateFlow.first { it.content is CitiesContentState.Content }

        viewModel.onQueryChanged("  Mos  ")
        runCurrent()
        viewModel.refresh()
        runCurrent()

        assertEquals(Request("Mos", 1, 20), repository.requests.last())
        viewModel.container.stateFlow.first {
            it.content is CitiesContentState.EmptyProgress
        }
        refresh.complete(success(city(2), total = 40))
        val content = viewModel.container.stateFlow.first {
            it.content is CitiesContentState.Content
        }.content as CitiesContentState.Content
        advanceTimeBy(CitiesViewModel.SEARCH_DEBOUNCE_MILLIS)
        advanceUntilIdle()

        assertEquals(2, repository.requests.size)
        assertEquals("  Mos  ", viewModel.container.stateFlow.value.query)
        assertEquals(listOf(2L), content.cities.map(CityUi::id))
    }

    @Test
    fun `failed refresh of pending search blocks pagination until first page retry succeeds`() = runTest(dispatcher) {
        val repository = FakeRepository(
            mutableListOf(
                success(city(1, "Paris", "FR"), total = 40),
                CitiesResult.Failure(CitiesError.Network),
                success(city(2, "Moscow", "RU"), total = 40),
                success(city(3, "Moscow Mills", "US"), page = 2, total = 40)
            )
        )
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        viewModel.onQueryChanged("  Mos  ")
        viewModel.refresh()
        advanceUntilIdle()
        viewModel.loadNextPage()
        advanceUntilIdle()

        assertEquals(listOf(Request(null, 1, 20), Request("Mos", 1, 20)), repository.requests)
        val failure = viewModel.container.stateFlow.first {
            it.content is CitiesContentState.EmptyFailure
        }.content as CitiesContentState.EmptyFailure
        assertEquals(CitiesError.Network, failure.error)
        assertEquals("  Mos  ", viewModel.container.stateFlow.value.query)

        viewModel.retry()
        advanceUntilIdle()
        val firstPage = viewModel.container.stateFlow.first {
            it.content is CitiesContentState.Content
        }.content as CitiesContentState.Content
        assertEquals(listOf("Moscow, Russia"), firstPage.cities.map(CityUi::title))

        viewModel.loadNextPage()
        advanceUntilIdle()
        val content = viewModel.container.stateFlow.first {
            it.content is CitiesContentState.ContentEndReached
        }.content as CitiesContentState.ContentEndReached
        assertEquals(listOf(2L, 3L), content.cities.map(CityUi::id))
        assertEquals(
            listOf(Request(null, 1, 20), Request("Mos", 1, 20), Request("Mos", 1, 20), Request("Mos", 2, 20)),
            repository.requests
        )
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
        val selected = city(7, "Москва", "RU").copy(population = 12_655_000)
        val vm = viewModel(FakeRepository(mutableListOf(success(selected, total = 1))))
        advanceUntilIdle()
        vm.test(this) {
            containerHost.onCityClick(7)
            expectSideEffect(CitiesSideEffect.OpenCity(CityDetails(7, "Москва", "RU", 12_655_000)))
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun `late old search success cannot replace newer search results`() = runTest(dispatcher) {
        assertLateSearchIgnored(success(city(99), total = 1))
    }

    @Test
    fun `late old search failure cannot replace newer search results`() = runTest(dispatcher) {
        assertLateSearchIgnored(CitiesResult.Failure(CitiesError.Server), whileLoading = true)
    }

    private suspend fun kotlinx.coroutines.test.TestScope.assertLateSearchIgnored(
        late: CitiesResult<Page<City>>,
        whileLoading: Boolean = false
    ) {
        val repository = NonCooperativeRepository()
        val vm = viewModel(repository)
        runCurrent()
        repository.responses[0].complete(success(city(1), total = 1))
        vm.container.stateFlow.first { it.content is CitiesContentState.ContentEndReached }
        vm.onQueryChanged("Old")
        advanceTimeBy(300)
        runCurrent()
        vm.onQueryChanged("New")
        advanceTimeBy(300)
        runCurrent()
        assertTrue(repository.jobs[1].isCancelled)
        if (whileLoading) {
            vm.container.stateFlow.first { it.content is CitiesContentState.EmptyProgress }
            repository.responses[1].complete(late)
            repository.jobs[1].join()
            vm.onCityClick(-1).join()
            assertEquals(CitiesContentState.EmptyProgress, vm.container.stateFlow.value.content)
        }
        repository.responses[2].complete(success(city(2), total = 1))
        vm.container.stateFlow.first {
            (it.content as? CitiesContentState.ContentEndReached)?.cities?.singleOrNull()?.id ==
                2L
        }
        val expected = vm.container.stateFlow.value
        assertEquals("New", expected.query)
        assertEquals(listOf(2L), (expected.content as CitiesContentState.ContentEndReached).cities.map(CityUi::id))
        repository.responses[1].complete(late)
        repository.jobs[1].join()
        vm.onCityClick(-1).join()
        assertEquals(expected, vm.container.stateFlow.value)
        assertEquals(listOf(null, "Old", "New"), repository.requests.map(Request::query))
    }

    @Test
    fun `new search cancels pagination and ignores its late response`() = runTest(dispatcher) {
        val repository = NonCooperativeRepository()
        val vm = viewModel(repository)
        runCurrent()
        repository.responses[0].complete(success(city(1), total = 60))
        vm.container.stateFlow.first { it.content is CitiesContentState.Content }
        vm.loadNextPage()
        runCurrent()
        vm.onQueryChanged("New")
        advanceTimeBy(300)
        runCurrent()
        assertTrue(repository.jobs[1].isCancelled)
        vm.container.stateFlow.first { it.content is CitiesContentState.EmptyProgress }
        repository.responses[2].complete(success(city(2), total = 40))
        vm.container.stateFlow.first { (it.content as? CitiesContentState.Content)?.cities?.singleOrNull()?.id == 2L }
        repository.responses[1].complete(success(city(99), page = 2, total = 60))
        repository.jobs[1].join()
        vm.onCityClick(-1).join()
        assertEquals(
            listOf(2L),
            (vm.container.stateFlow.value.content as CitiesContentState.Content).cities.map(CityUi::id)
        )
        vm.loadNextPage()
        runCurrent()
        repository.responses[3].complete(success(city(3), page = 2, total = 40))
        vm.container.stateFlow.first { it.content is CitiesContentState.ContentEndReached }
        assertEquals(
            listOf(2L, 3L),
            (vm.container.stateFlow.value.content as CitiesContentState.ContentEndReached).cities.map(CityUi::id)
        )
        assertEquals(
            listOf(Request(null, 1, 20), Request(null, 2, 20), Request("New", 1, 20), Request("New", 2, 20)),
            repository.requests
        )
    }

    // Simulates a transport that delivers a response even after its caller is cancelled.
    private class NonCooperativeRepository : CitiesRepository {
        val requests = mutableListOf<Request>()
        val jobs = mutableListOf<Job>()
        val responses = mutableListOf<CompletableDeferred<CitiesResult<Page<City>>>>()

        override suspend fun cities(query: String?, page: Int, limit: Int): CitiesResult<Page<City>> {
            requests += Request(query, page, limit)
            jobs += currentCoroutineContext()[Job]!!
            val response = CompletableDeferred<CitiesResult<Page<City>>>()
            responses += response
            return withContext(NonCancellable) { response.await() }
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
