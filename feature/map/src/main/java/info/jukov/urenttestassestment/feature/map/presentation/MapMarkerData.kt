package info.jukov.urenttestassestment.feature.map.presentation

import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point

// Keep the source snapshot across loading states and style reloads. Re-uploading identical
// GeoJSON unnecessarily invalidates MapLibre's tiles and symbol placement.
internal class MapMarkerData {
    private var inputCities: List<MapCityUi> = emptyList()
    private var cities: List<MapCityUi> = emptyList()
    var features: FeatureCollection = FeatureCollection.fromFeatures(emptyList())
        private set
    var json: String = EMPTY_CITY_GEOJSON
        private set

    fun update(cities: List<MapCityUi>): FeatureCollection? {
        if (inputCities == cities) return null
        inputCities = cities
        // Native 13.4.1 inserts each feature before existing equal sort keys.
        // Reverse the desired order within a band so larger cities / smaller IDs
        // are placed first after that insertion. Different bands use their key.
        val ranked = cities.sortedWith(compareBy<MapCityUi> { it.population }.thenByDescending { it.id })
        return if (this.cities == ranked) {
            null
        } else {
            this.cities = ranked
            features = FeatureCollection.fromFeatures(
                ranked.map { city ->
                    Feature.fromGeometry(
                        Point.fromLngLat(city.longitude, city.latitude),
                        null,
                        city.id.toString()
                    ).apply {
                        addStringProperty("name", city.name)
                        addNumberProperty("id", city.id)
                        // A bounded number of keys lets MapLibre batch pins together.
                        addNumberProperty(POPULATION_PRIORITY, populationPriority(city.population))
                    }
                }
            )
            json = features.toJson()
            features
        }
    }
}

internal const val EMPTY_CITY_GEOJSON = "{\"type\":\"FeatureCollection\",\"features\":[]}"
internal const val POPULATION_PRIORITY = "populationPriority"

// Unique keys split native icon/text draw segments per city. Use at most 15 bands;
// source ordering compensates for native insertion within each band. Cross-tile
// collision priority is intentionally shared by cities in the same population band.
private val populationThresholds = longArrayOf(
    1_000, 2_000, 5_000, 10_000, 20_000, 50_000, 100_000,
    200_000, 500_000, 1_000_000, 2_000_000, 5_000_000, 10_000_000, 20_000_000
)

internal fun populationPriority(population: Long): Int = -populationThresholds.count { population >= it }
