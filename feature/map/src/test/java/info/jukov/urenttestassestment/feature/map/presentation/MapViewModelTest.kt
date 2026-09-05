package info.jukov.urenttestassestment.feature.map.presentation

import androidx.lifecycle.SavedStateHandle
import info.jukov.urenttestassestment.core.model.City
import info.jukov.urenttestassestment.feature.map.domain.Coordinate
import info.jukov.urenttestassestment.feature.map.domain.MapRepository
import info.jukov.urenttestassestment.feature.map.domain.MapResult
import info.jukov.urenttestassestment.feature.map.domain.MapViewport
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
    fun `content is published through Orbit`() = runTest(dispatcher) {
        viewModel { success(1) }.test(this) {
            containerHost.onCameraIdle(camera, viewport)
            expectState { MapState(listOf(MapCityUi(1, "City 1", 55.75, 37.62)), MapLoadState.Content) }
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
        assertEquals(3L, vm.container.stateFlow.value.cities.single().id)
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
    fun `camera survives recreation without saving city list`() = runTest(dispatcher) {
        val saved = SavedStateHandle()
        val repo = object : MapRepository {
            override suspend fun cities(viewport: MapViewport) = MapResult.Success(emptyList())
        }
        MapViewModel(repo, saved).onCameraIdle(camera, viewport)
        advanceUntilIdle()
        assertEquals(camera, MapViewModel(repo, saved).camera)
        assertEquals(setOf("map_lat", "map_lng", "map_zoom"), saved.keys())
    }

    private fun viewModel(load: suspend (MapViewport) -> MapResult) = MapViewModel(
        object : MapRepository {
            override suspend fun cities(viewport: MapViewport) = load(viewport)
        },
        SavedStateHandle()
    )

    private fun success(id: Long) = MapResult.Success(listOf(City(id, "City $id", "RU", 100, 55.75, 37.62)))
}
