package info.jukov.urenttestassestment.feature.map.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.jukov.urenttestassestment.feature.map.domain.MapRepository
import info.jukov.urenttestassestment.feature.map.domain.MapResult
import info.jukov.urenttestassestment.feature.map.domain.MapViewport
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.blockingIntent
import org.orbitmvi.orbit.viewmodel.container

public class MapViewModel(private val repository: MapRepository, private val savedState: SavedStateHandle) :
    ViewModel(),
    ContainerHost<MapState, Nothing> {
    override val container = container<MapState, Nothing>(MapState())
    private var viewport: MapViewport? = null
    private var request: Job? = null
    private var generation = 0

    public val camera: MapCamera
        get() = MapCamera(
            savedState["map_lat"] ?: MapCamera().latitude,
            savedState["map_lng"] ?: MapCamera().longitude,
            savedState["map_zoom"] ?: MapCamera().zoom
        )

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
            blockingIntent {
                if (expected != generation) return@blockingIntent
                reduce {
                    when (result) {
                        is MapResult.Success -> MapState(
                            cities = mergeMapCities(
                                state.cities,
                                result.cities.map { MapCityUi(it.id, it.name, it.latitude, it.longitude) }
                            ),
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
