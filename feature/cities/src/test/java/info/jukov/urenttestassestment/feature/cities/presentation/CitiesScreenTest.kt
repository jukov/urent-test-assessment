package info.jukov.urenttestassestment.feature.cities.presentation

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class CitiesScreenTest {
    @Test
    fun `header has no elevation at top of list`() {
        assertEquals(0.dp, headerElevationFor(isScrolled = false))
    }

    @Test
    fun `header has elevation after list scrolls`() {
        assertEquals(4.dp, headerElevationFor(isScrolled = true))
    }
}
