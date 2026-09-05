package info.jukov.urenttestassestment.feature.map.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point

class MapMarkerDataTest {
    private val city = MapCityUi(7, "Moscow", 55.75, 37.62, 10_381_222)

    @Test
    fun `loading recompositions and identical responses do not invalidate the map source`() {
        val data = MapMarkerData()
        val first = data.update(listOf(city))
        val json = data.json

        repeat(3) { assertNull(data.update(listOf(city.copy()))) }
        assertSame(first, data.features)
        assertSame(json, data.json)
    }

    @Test
    fun `new snapshot retains stable feature ids and updates names and coordinates`() {
        val data = MapMarkerData()
        val first = data.update(listOf(city))!!
        val updated = city.copy(name = "Updated", latitude = 56.0)
        val features = nativePlacementOrder(data.update(listOf(updated, city.copy(id = 8)))!!)

        assertEquals(first.features()!!.single().id(), features.first().id())
        assertEquals("7", features.first().id())
        assertEquals("Updated", features.first().getStringProperty("name"))
        assertEquals(Point.fromLngLat(37.62, 56.0), features.first().geometry())
        assertEquals(listOf("7", "8"), features.map { it.id() })
    }

    @Test
    fun `overlapping responses update by id and refresh recency without duplicates`() {
        val other = city.copy(id = 8)
        val updated = city.copy(name = "Updated")
        val merged = mergeMapCities(listOf(city, other), listOf(other, updated, city.copy(id = 9)))

        assertEquals(listOf(other, updated, city.copy(id = 9)), merged)
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
            val features = nativePlacementOrder(data.update(mergeMapCities(listOf(cached), listOf(loaded)))!!)

            assertEquals(listOf("Moscow", "Khimki"), features.map { it.getStringProperty("name") })
            // MapLibre places lower sort keys first when overlap is disabled.
            assertEquals(listOf(-13, -8), features.map { it.getNumberProperty(POPULATION_PRIORITY).toInt() })
        }
    }

    @Test
    fun `fresh population changes recompute cached priorities while retaining stable ids`() {
        val other = city.copy(id = 8, population = 100)
        val data = MapMarkerData()
        val cached = listOf(city, other)
        data.update(cached)
        val updated = other.copy(population = 20_000_000)
        val features = nativePlacementOrder(data.update(mergeMapCities(cached, listOf(updated)))!!)

        assertEquals(listOf("8", "7"), features.map { it.id() })
        assertEquals(listOf(-14, -13), features.map { it.getNumberProperty(POPULATION_PRIORITY).toInt() })
    }

    @Test
    fun `equal populations use stable id priority and reordered inputs do not invalidate source`() {
        val cities = listOf(city.copy(id = 9), city, city.copy(id = 8))
        val data = MapMarkerData()
        val snapshot = data.update(cities)!!

        assertEquals(listOf("7", "8", "9"), nativePlacementOrder(snapshot).map { it.id() })
        assertEquals(
            listOf(-13, -13, -13),
            snapshot.features()!!.map {
                it.getNumberProperty(POPULATION_PRIORITY).toInt()
            }
        )
        assertNull(data.update(cities.reversed()))
        assertSame(snapshot, data.features)
    }

    @Test
    fun `large city sets have bounded native sort groups and monotonic population priority`() {
        val populations = listOf(Long.MIN_VALUE, 0L) + (1L..10_000L).map { it * 3_001 } + Long.MAX_VALUE
        val priorities = populations.map(::populationPriority)

        assertTrue(priorities.distinct().size <= 15)
        assertTrue(priorities.zipWithNext().all { (smaller, larger) -> smaller >= larger })
        assertTrue(populationPriority(10_381_222) < populationPriority(239_967))
    }

    @Test
    fun `adding a larger city does not change the priority of known pins`() {
        val data = MapMarkerData()
        val before = data.update(listOf(city))!!.features()!!.single().getNumberProperty(POPULATION_PRIORITY)
        val after = data.update(listOf(city, city.copy(id = 8, population = 20_000_000)))!!.features()!!

        assertEquals(before, after.single { it.id() == "7" }.getNumberProperty(POPULATION_PRIORITY))
    }

    @Test
    fun `native insertion of equal keys places larger cities first within a population band`() {
        val larger = city.copy(id = 1, population = 400_000)
        val smaller = city.copy(id = 2, population = 250_000)
        assertEquals(populationPriority(larger.population), populationPriority(smaller.population))
        listOf(listOf(larger, smaller), listOf(smaller, larger)).forEach { input ->
            assertEquals(listOf("1", "2"), nativePlacementOrder(MapMarkerData().update(input)!!).map { it.id() })
        }
    }

    // SymbolLayout 13.4.1 uses lower_bound (insert BEFORE equal sort keys);
    // placement consumes that sequence in forward order for SymbolZOrder.Auto.
    private fun nativePlacementOrder(snapshot: FeatureCollection): List<Feature> {
        val placed = mutableListOf<Feature>()
        snapshot.features()!!.forEach { feature ->
            val key = feature.getNumberProperty(POPULATION_PRIORITY).toInt()
            val index = placed.indexOfFirst { it.getNumberProperty(POPULATION_PRIORITY).toInt() >= key }
            placed.add(if (index < 0) placed.size else index, feature)
        }
        return placed
    }
}
