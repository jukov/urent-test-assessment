package info.jukov.urenttestassessment.feature.map.presentation

import androidx.lifecycle.SavedStateHandle
import info.jukov.urenttestassessment.core.model.City
import info.jukov.urenttestassessment.feature.map.domain.Coordinate
import info.jukov.urenttestassessment.feature.map.domain.MapRepository
import info.jukov.urenttestassessment.feature.map.domain.MapResult
import info.jukov.urenttestassessment.feature.map.domain.MapViewport
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.orbitmvi.orbit.test.test

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val viewport = MapViewport(Coordinate(55.75, 37.62), 20000)
    private val camera = MapCamera(55.75, 37.62, 10.0)

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `idle events debounce to latest viewport and equivalent viewport does not reload`() = runTest(dispatcher) {
        val requests = mutableListOf<MapViewport>()
        val vm = viewModel {
            requests += it
            MapResult.Success(emptyList())
        }
        vm.onCameraIdle(camera, viewport)
        advanceTimeBy(200)
        val next = viewport.copy(radius = 30000)
        vm.onCameraIdle(camera, next)
        advanceTimeBy(299)
        runCurrent()
        assertEquals(0, requests.size)
        advanceTimeBy(1)
        advanceUntilIdle()
        assertEquals(listOf(next), requests)
        assertEquals(MapLoadState.Empty, vm.container.stateFlow.value.loadState)
        vm.onCameraIdle(camera, next)
        advanceUntilIdle()
        assertEquals(1, requests.size)
    }

    @Test
    fun `city focus saves camera without requesting cities and next user move loads`() = runTest(dispatcher) {
        val requests = mutableListOf<MapViewport>()
        val vm = viewModel {
            requests += it
            success(1)
        }
        vm.onCameraIdle(camera, viewport)
        advanceUntilIdle()
        vm.selectCity(1)
        val content = vm.container.stateFlow.value
        val focusedCamera = camera.copy(latitude = 55.8)
        vm.onCameraIdle(focusedCamera, null)
        advanceUntilIdle()
        assertEquals(listOf(viewport), requests)
        assertEquals(content, vm.container.stateFlow.value)
        assertEquals(focusedCamera, vm.camera)

        val moved = viewport.copy(center = Coordinate(55.8, 37.62))
        vm.onCameraIdle(focusedCamera, moved)
        advanceUntilIdle()
        assertEquals(listOf(viewport, moved), requests)
    }

    @Test
    fun `focus does not cancel pending viewport request or replace its retry target`() = runTest(dispatcher) {
        val requests = mutableListOf<MapViewport>()
        val vm = viewModel {
            requests += it
            MapResult.Failure
        }
        vm.onCameraIdle(camera, viewport)
        advanceTimeBy(100)
        vm.onCameraIdle(camera.copy(latitude = 56.0), null)
        advanceUntilIdle()
        assertEquals(listOf(viewport), requests)
        assertEquals(MapLoadState.Error, vm.container.stateFlow.value.loadState)
        vm.retry()
        advanceUntilIdle()
        assertEquals(listOf(viewport, viewport), requests)
    }

    @Test
    fun `content is published through Orbit`() = runTest(dispatcher) {
        viewModel { success(1) }.test(this) {
            containerHost.onCameraIdle(camera, viewport)
            expectState { MapState(listOf(MapCityUi(1, "City 1", 55.75, 37.62, 100, "RU")), MapLoadState.Content) }
        }
    }

    @Test
    fun `error preserves markers and retry immediately repeats failed viewport`() = runTest(dispatcher) {
        var calls = 0
        val vm = viewModel {
            calls++
            if (calls == 2) MapResult.Failure else success(calls.toLong())
        }
        vm.onCameraIdle(camera, viewport)
        advanceUntilIdle()
        vm.onCameraIdle(camera, viewport.copy(radius = 30000))
        advanceUntilIdle()
        assertEquals(MapLoadState.Error, vm.container.stateFlow.value.loadState)
        assertEquals(1L, vm.container.stateFlow.value.cities.single().id)
        vm.retry()
        runCurrent()
        assertEquals(3, calls)
        assertEquals(listOf(3L), vm.container.stateFlow.value.cities.map(MapCityUi::id))
    }

    @Test
    fun `late response cannot replace cities after viewport changed`() = runTest(dispatcher) {
        val old = CompletableDeferred<MapResult>()
        var calls = 0
        val vm = viewModel {
            calls++
            if (calls == 1) withContext(NonCancellable) { old.await() } else success(2)
        }
        vm.onCameraIdle(camera, viewport)
        advanceTimeBy(300)
        runCurrent()
        vm.onCameraIdle(camera, viewport.copy(radius = 30000))
        advanceUntilIdle()
        assertEquals(2L, vm.container.stateFlow.value.cities.single().id)
        old.complete(success(1))
        advanceUntilIdle()
        assertEquals(2L, vm.container.stateFlow.value.cities.single().id)
    }

    @Test
    fun `new batch replaces previous cities and updates overlapping cities`() = runTest(dispatcher) {
        val next = CompletableDeferred<MapResult>()
        var calls = 0
        val vm = viewModel {
            if (++calls == 1) {
                MapResult.Success(
                    listOf(City(1, "Moscow", "RU", 100, 55.75, 37.62), City(2, "Paris", "FR", 200, 48.85, 2.35))
                )
            } else {
                next.await()
            }
        }
        vm.onCameraIdle(camera, viewport)
        advanceUntilIdle()
        val previous = vm.container.stateFlow.value.cities
        assertEquals(listOf(1L, 2L), previous.map(MapCityUi::id))

        vm.onCameraIdle(camera, viewport.copy(radius = 30000))
        advanceUntilIdle()
        assertEquals(MapLoadState.Loading, vm.container.stateFlow.value.loadState)
        assertEquals(previous, vm.container.stateFlow.value.cities)
        next.complete(
            MapResult.Success(
                listOf(City(2, "Updated Paris", "FR", 300, 48.86, 2.36), City(3, "London", "GB", 400, 51.5, -0.12))
            )
        )
        advanceUntilIdle()

        assertEquals(
            listOf(
                MapCityUi(2, "Updated Paris", 48.86, 2.36, 300, "FR"),
                MapCityUi(3, "London", 51.5, -0.12, 400, "GB")
            ),
            vm.container.stateFlow.value.cities
        )
        assertEquals(MapLoadState.Content, vm.container.stateFlow.value.loadState)
    }

    @Test
    fun `fresh responses update city data and selected details`() = runTest(dispatcher) {
        var calls = 0
        val vm = viewModel {
            calls++
            if (calls == 1) {
                success(1)
            } else {
                MapResult.Success(
                    listOf(City(1, "Updated", "RU", 200, 56.0, 38.0))
                )
            }
        }
        vm.onCameraIdle(camera, viewport)
        advanceUntilIdle()
        vm.selectCity(1)
        vm.onCameraIdle(camera, viewport.copy(radius = 30000))
        advanceUntilIdle()

        assertEquals(listOf(MapCityUi(1, "Updated", 56.0, 38.0, 200, "RU")), vm.container.stateFlow.value.cities)
        assertEquals(vm.container.stateFlow.value.cities.single(), vm.container.stateFlow.value.selectedCity)
    }

    @Test
    fun `empty viewport result removes pins from the previous response`() = runTest(dispatcher) {
        var calls = 0
        val vm = viewModel {
            calls++
            if (calls == 1) success(1) else MapResult.Success(emptyList())
        }
        vm.onCameraIdle(camera, viewport)
        advanceUntilIdle()
        vm.selectCity(1)
        vm.onCameraIdle(camera, viewport.copy(center = Coordinate(0.0, 0.0)))
        advanceUntilIdle()

        assertEquals(MapLoadState.Empty, vm.container.stateFlow.value.loadState)
        assertEquals(emptyList<MapCityUi>(), vm.container.stateFlow.value.cities)
    }

    @Test
    fun `camera survives recreation without saving city list`() = runTest(dispatcher) {
        val saved = SavedStateHandle()
        val repo = object : MapRepository {
            override suspend fun cities(viewport: MapViewport) = MapResult.Success(emptyList())
        }
        MapViewModel(repo, saved, dispatcher).onCameraIdle(camera, viewport)
        advanceUntilIdle()
        assertEquals(camera, MapViewModel(repo, saved, dispatcher).camera)
        assertEquals(setOf("map_lat", "map_lng", "map_zoom"), saved.keys())
    }

    @Test
    fun `incomplete saved camera falls back to one complete default camera`() {
        val fields = listOf("map_lat" to 12.0, "map_lng" to 34.0, "map_zoom" to 5.0)
        val repo = object : MapRepository {
            override suspend fun cities(viewport: MapViewport) = MapResult.Success(emptyList())
        }
        // All seven incomplete combinations, including an empty SavedStateHandle.
        for (mask in 0 until 7) {
            val state = fields.filterIndexed { index, _ -> mask and (1 shl index) != 0 }.toMap()
            assertEquals(MapCamera(), MapViewModel(repo, SavedStateHandle(state), dispatcher).camera)
        }
    }

    @Test
    fun `selection uses loaded data and survives viewport failure and process recreation`() = runTest(dispatcher) {
        val savedState = SavedStateHandle()
        var calls = 0
        val repository = object : MapRepository {
            override suspend fun cities(viewport: MapViewport): MapResult =
                if (++calls == 1) success(7) else MapResult.Failure
        }
        val vm = MapViewModel(repository, savedState, dispatcher)
        vm.onCameraIdle(camera, viewport)
        advanceUntilIdle()
        vm.selectCity(7)
        val city = vm.container.stateFlow.value.selectedCity!!
        assertEquals("RU", city.countryCode)
        assertEquals(100L, city.population)
        vm.selectCity(999)
        assertEquals(city, vm.container.stateFlow.value.selectedCity)
        vm.selectCity(7)
        vm.onCameraIdle(camera, viewport.copy(radius = 30000))
        advanceUntilIdle()
        assertEquals(city, vm.container.stateFlow.value.selectedCity)
        val restored =
            MapViewModel(
                repository,
                SavedStateHandle(
                    savedState.keys().associateWith {
                        savedState.get<Any?>(it)
                    }
                ),
                dispatcher
            )
        assertEquals(city, restored.container.stateFlow.value.selectedCity)
        restored.selectCity(null)
        assertEquals(null, restored.container.stateFlow.value.selectedCity)
    }

    @Test
    fun `selecting another pin replaces details and closing clears saved selection`() = runTest(dispatcher) {
        val savedState = SavedStateHandle()
        val repository = object : MapRepository {
            override suspend fun cities(viewport: MapViewport) = MapResult.Success(
                listOf(City(1, "Moscow", "RU", 100, 55.75, 37.62), City(2, "Paris", "FR", 200, 48.85, 2.35))
            )
        }
        val vm = MapViewModel(repository, savedState, dispatcher)
        vm.onCameraIdle(camera, viewport)
        advanceUntilIdle()
        vm.selectCity(1)
        vm.selectCity(2)
        assertEquals("Paris", vm.container.stateFlow.value.selectedCity?.name)
        vm.selectCity(null)
        assertEquals(null, MapViewModel(repository, savedState, dispatcher).container.stateFlow.value.selectedCity)
    }

    private fun viewModel(load: suspend (MapViewport) -> MapResult) = MapViewModel(
        object : MapRepository {
            override suspend fun cities(viewport: MapViewport) = load(viewport)
        },
        SavedStateHandle(),
        dispatcher
    )

    private fun success(id: Long) = MapResult.Success(listOf(City(id, "City $id", "RU", 100, 55.75, 37.62)))
}
