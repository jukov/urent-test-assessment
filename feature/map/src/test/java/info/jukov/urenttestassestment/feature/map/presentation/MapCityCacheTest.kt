package info.jukov.urenttestassestment.feature.map.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class MapCityCacheTest {
    @Test
    fun `overflow evicts the least recently received city and keeps refreshed cities`() {
        val cached = (1L..1_000L).map(::city)
        val refreshed = city(1).copy(name = "Updated", population = 200)
        val merged = mergeMapCities(cached, listOf(refreshed, city(1_001)))

        assertEquals(1_000, merged.size)
        assertFalse(merged.any { it.id == 2L })
        assertEquals(listOf(refreshed, city(1_001)), merged.takeLast(2))
        assertEquals(merged.size, merged.map { it.id }.distinct().size)
    }

    @Test
    fun `reading an unchanged city refreshes its recency before the next eviction`() {
        val cached = (1L..1_000L).map(::city)
        val touched = mergeMapCities(cached, listOf(city(1)))
        val merged = mergeMapCities(touched, listOf(city(1_001)))

        assertTrue(merged.any { it.id == 1L })
        assertFalse(merged.any { it.id == 2L })
    }

    @Test
    fun `even an oversized response respects the thousand city capacity`() {
        val merged = mergeMapCities(emptyList(), (1L..1_500L).map(::city))

        assertEquals((501L..1_500L).toList(), merged.map { it.id })
    }

    @Test
    fun `empty response preserves cached pins and their recency`() {
        val cached = listOf(city(1), city(2))

        assertSame(cached, mergeMapCities(cached, emptyList()))
    }

    @Test
    fun `refreshing a full cache changes recency without evicting existing cities`() {
        val cached = (1L..1_000L).map(::city)

        assertEquals(cached.reversed(), mergeMapCities(cached, cached.reversed()))
    }

    @Test
    fun `duplicate ids in one response keep the last value without extra eviction`() {
        val cached = (1L..1_000L).map(::city)
        val latest = city(1).copy(name = "Latest")
        val merged = mergeMapCities(cached, listOf(city(1), latest, city(1_001)))

        assertEquals(1_000, merged.size)
        assertEquals((3L..1_000L).toList() + listOf(1L, 1_001L), merged.map { it.id })
        assertEquals(latest, merged.single { it.id == 1L })
    }

    private fun city(id: Long) = MapCityUi(id, "City $id", 55.75, 37.62, 100)
}
