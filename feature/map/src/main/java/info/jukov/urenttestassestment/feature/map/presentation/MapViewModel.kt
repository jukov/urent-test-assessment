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
    override val container = container<MapState, Nothing>(MapState(selectedCity = restoreSelectedCity()))
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

    /** A null viewport saves a presentation-only camera move without changing the cities request. */
    public fun onCameraIdle(camera: MapCamera, viewport: MapViewport?) {
        savedState["map_lat"] = camera.latitude
        savedState["map_lng"] = camera.longitude
        savedState["map_zoom"] = camera.zoom
        if (viewport == null || this.viewport == viewport) return
        this.viewport = viewport
        load(viewport, debounce = true)
    }

    public fun retry() {
        if (container.stateFlow.value.loadState == MapLoadState.Error) {
            viewport?.let { load(it, debounce = false) }
        }
    }

    public fun selectCity(id: Long?) = blockingIntent {
        val city = if (id == null) null else state.cities.firstOrNull { it.id == id } ?: return@blockingIntent
        saveSelectedCity(city)
        reduce { state.copy(selectedCity = city) }
    }

    private fun saveSelectedCity(city: MapCityUi?) {
        savedState["selected_city_id"] = city?.id
        savedState["selected_city_name"] = city?.name
        savedState["selected_city_country"] = city?.countryCode
        savedState["selected_city_population"] = city?.population
        savedState["selected_city_lat"] = city?.latitude
        savedState["selected_city_lng"] = city?.longitude
    }

    @Suppress("ComplexCondition") // Restore only a complete city snapshot.
    private fun restoreSelectedCity(): MapCityUi? {
        val id = savedState.get<Long>("selected_city_id")
        val name = savedState.get<String>("selected_city_name")
        val latitude = savedState.get<Double>("selected_city_lat")
        val longitude = savedState.get<Double>("selected_city_lng")
        val population = savedState.get<Long>("selected_city_population")
        val country = savedState.get<String>("selected_city_country")
        if (id == null || name == null || latitude == null || longitude == null || population == null ||
            country == null
        ) {
            return null
        }
        return MapCityUi(id, name, latitude, longitude, population, country)
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
                        result.cities.map {
                            MapCityUi(it.id, it.name, it.latitude, it.longitude, it.population, it.countryCode)
                        }
                    )
                }
            } else {
                cached
            }
            blockingIntent {
                if (expected != generation) return@blockingIntent
                val selected = state.selectedCity?.let { current ->
                    cities.firstOrNull { it.id == current.id }
                        ?: current
                }
                if (selected != state.selectedCity) saveSelectedCity(selected)
                reduce {
                    when (result) {
                        is MapResult.Success -> state.copy(
                            cities = cities,
                            selectedCity = selected,
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
