package info.jukov.urenttestassestment.feature.map.presentation

import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point

// Keep the source snapshot across loading states and style reloads. Re-uploading identical
// GeoJSON unnecessarily invalidates MapLibre's tiles and symbol placement.
internal class MapMarkerData {
    private var cities: List<MapCityUi> = emptyList()
    var features: FeatureCollection = FeatureCollection.fromFeatures(emptyList())
        private set

    fun update(cities: List<MapCityUi>): FeatureCollection? {
        val ranked = cities.sortedWith(compareByDescending<MapCityUi> { it.population }.thenBy { it.id })
        if (this.cities == ranked) return null
        this.cities = ranked
        features = FeatureCollection.fromFeatures(
            ranked.mapIndexed { rank, city ->
                Feature.fromGeometry(Point.fromLngLat(city.longitude, city.latitude), null, city.id.toString()).apply {
                    addStringProperty("name", city.name)
                    addNumberProperty("id", city.id)
                    // Lower symbol-sort-key wins collisions. Rank also gives equal
                    // populations a stable tie-breaker independent of cache/load order.
                    addNumberProperty(POPULATION_RANK, rank)
                }
            }
        )
        return features
    }
}

internal fun mergeMapCities(cached: List<MapCityUi>, loaded: List<MapCityUi>): List<MapCityUi> {
    val cities = cached.associateByTo(linkedMapOf(), MapCityUi::id)
    // Preserve state order on overlapping responses. Placement priority is assigned
    // separately from population, so cache insertion order cannot hide larger cities.
    loaded.forEach { cities[it.id] = it }
    return cities.values.toList()
}

internal const val POPULATION_RANK = "populationRank"
