package info.jukov.urenttestassestment.feature.map.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.jukov.urenttestassestment.feature.map.domain.MapRepository
import info.jukov.urenttestassestment.feature.map.domain.MapResult
import info.jukov.urenttestassestment.feature.map.domain.MapViewport
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.blockingIntent
import org.orbitmvi.orbit.viewmodel.container

public class MapViewModel(
    private val repository: MapRepository,
    private val savedState: SavedStateHandle,
    private val workerDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel(),
    ContainerHost<MapState, Nothing> {
    override val container = container<MapState, Nothing>(MapState())
    private var viewport: MapViewport? = null
    private var request: Job? = null
    private var generation = 0

    public val camera: MapCamera
        get() {
            val latitude = savedState.get<Double>("map_lat") ?: return MapCamera()
            val longitude = savedState.get<Double>("map_lng") ?: return MapCamera()
            val zoom = savedState.get<Double>("map_zoom") ?: return MapCamera()
            return MapCamera(latitude, longitude, zoom)
        }

    public fun onCameraIdle(camera: MapCamera, viewport: MapViewport) {
        savedState["map_lat"] = camera.latitude
        savedState["map_lng"] = camera.longitude
        savedState["map_zoom"] = camera.zoom
        if (this.viewport == viewport) return
        this.viewport = viewport
        load(viewport, debounce = true)
    }

    public fun retry() {
        if (container.stateFlow.value.loadState == MapLoadState.Error) {
            viewport?.let { load(it, debounce = false) }
        }
    }

    private fun load(viewport: MapViewport, debounce: Boolean) {
        request?.cancel()
        val expected = ++generation
        blockingIntent { reduce { state.copy(loadState = MapLoadState.Loading) } }
        request = viewModelScope.launch {
            if (debounce) delay(VIEWPORT_DEBOUNCE_MILLIS)
            val result = repository.cities(viewport)
            val cached = container.stateFlow.value.cities
            val cities = if (result is MapResult.Success) {
                withContext(workerDispatcher) {
                    mergeMapCities(
                        cached,
                        result.cities.map { MapCityUi(it.id, it.name, it.latitude, it.longitude, it.population) }
                    )
                }
            } else {
                cached
            }
            blockingIntent {
                if (expected != generation) return@blockingIntent
                reduce {
                    when (result) {
                        is MapResult.Success -> MapState(
                            cities = cities,
                            loadState = if (result.cities.isEmpty()) MapLoadState.Empty else MapLoadState.Content
                        )

                        MapResult.Failure -> state.copy(loadState = MapLoadState.Error)
                    }
                }
            }
        }
    }

    public companion object {
        public const val VIEWPORT_DEBOUNCE_MILLIS: Long = 300L
    }
}
