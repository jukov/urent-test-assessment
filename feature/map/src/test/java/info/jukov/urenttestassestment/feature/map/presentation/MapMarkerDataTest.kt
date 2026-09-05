package info.jukov.urenttestassestment.feature.map.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.maplibre.geojson.Point

class MapMarkerDataTest {
    private val city = MapCityUi(7, "Moscow", 55.75, 37.62, 10_381_222)

    @Test
    fun `loading recompositions and identical responses do not invalidate the map source`() {
        val data = MapMarkerData()
        val first = data.update(listOf(city))

        repeat(3) { assertNull(data.update(listOf(city.copy()))) }
        assertSame(first, data.features)
    }

    @Test
    fun `new snapshot retains stable feature ids and updates names and coordinates`() {
        val data = MapMarkerData()
        val first = data.update(listOf(city))!!
        val updated = city.copy(name = "Updated", latitude = 56.0)
        val features = data.update(listOf(updated, city.copy(id = 8)))!!.features()!!

        assertEquals(first.features()!!.single().id(), features.first().id())
        assertEquals("7", features.first().id())
        assertEquals("Updated", features.first().getStringProperty("name"))
        assertEquals(Point.fromLngLat(37.62, 56.0), features.first().geometry())
        assertEquals(listOf("7", "8"), features.map { it.id() })
    }

    @Test
    fun `overlapping responses update by id without duplicates or reordering known cities`() {
        val other = city.copy(id = 8)
        val updated = city.copy(name = "Updated")
        val merged = mergeMapCities(listOf(city, other), listOf(other, updated, city.copy(id = 9)))

        assertEquals(listOf(updated, other, city.copy(id = 9)), merged)
    }

    @Test
    fun `reordered repeated response and empty response keep the existing source snapshot`() {
        val cached = listOf(city, city.copy(id = 8))
        val data = MapMarkerData()
        val snapshot = data.update(cached)

        assertNull(data.update(mergeMapCities(cached, cached.reversed())))
        assertNull(data.update(mergeMapCities(cached, emptyList())))
        assertSame(snapshot, data.features)
    }

    @Test
    fun `Moscow gets collision priority over Khimki regardless of which city was cached first`() {
        val khimki = city.copy(id = 8, name = "Khimki", population = 239_967)
        listOf(city to khimki, khimki to city).forEach { (cached, loaded) ->
            val data = MapMarkerData()
            data.update(listOf(cached))
            val features = data.update(mergeMapCities(listOf(cached), listOf(loaded)))!!.features()!!

            assertEquals(listOf("Moscow", "Khimki"), features.map { it.getStringProperty("name") })
            // MapLibre places lower sort keys first when overlap is disabled.
            assertEquals(listOf(0, 1), features.map { it.getNumberProperty(POPULATION_RANK).toInt() })
        }
    }

    @Test
    fun `fresh population changes recompute cached priorities while retaining stable ids`() {
        val other = city.copy(id = 8, population = 100)
        val data = MapMarkerData()
        val cached = listOf(city, other)
        data.update(cached)
        val updated = other.copy(population = 20_000_000)
        val features = data.update(mergeMapCities(cached, listOf(updated)))!!.features()!!

        assertEquals(listOf("8", "7"), features.map { it.id() })
        assertEquals(listOf(0, 1), features.map { it.getNumberProperty(POPULATION_RANK).toInt() })
    }

    @Test
    fun `equal populations use stable id priority and reordered inputs do not invalidate source`() {
        val cities = listOf(city.copy(id = 9), city, city.copy(id = 8))
        val data = MapMarkerData()
        val snapshot = data.update(cities)!!

        assertEquals(listOf("7", "8", "9"), snapshot.features()!!.map { it.id() })
        assertEquals(listOf(0, 1, 2), snapshot.features()!!.map { it.getNumberProperty(POPULATION_RANK).toInt() })
        assertNull(data.update(cities.reversed()))
        assertSame(snapshot, data.features)
    }
}
