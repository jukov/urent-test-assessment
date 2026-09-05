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
        if (this.cities == cities) return null
        this.cities = cities.toList()
        features = FeatureCollection.fromFeatures(
            cities.map { city ->
                Feature.fromGeometry(Point.fromLngLat(city.longitude, city.latitude), null, city.id.toString()).apply {
                    addStringProperty("name", city.name)
                    addNumberProperty("id", city.id)
                }
            }
        )
        return features
    }
}

internal fun mergeMapCities(cached: List<MapCityUi>, loaded: List<MapCityUi>): List<MapCityUi> {
    val cities = cached.associateByTo(linkedMapOf(), MapCityUi::id)
    // Replacing a value preserves insertion order, avoiding label priority changes when
    // overlapping viewport responses contain the same cities in a different order.
    loaded.forEach { cities[it.id] = it }
    return cities.values.toList()
}
