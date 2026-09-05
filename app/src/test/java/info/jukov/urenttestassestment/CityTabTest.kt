package info.jukov.urenttestassestment

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class CityTabTest {
    @Test
    fun `tabs have distinct routes and matching accessible labels`() {
        assertSame(CityListDestination, CityTab.List.destination)
        assertSame(CityMapDestination, CityTab.Map.destination)
        assertEquals(R.string.tab_cities, CityTab.List.labelRes)
        assertEquals(R.string.tab_map, CityTab.Map.labelRes)
        assertEquals(CityTab.entries.size, CityTab.entries.map { it.destination }.distinct().size)
    }
}
