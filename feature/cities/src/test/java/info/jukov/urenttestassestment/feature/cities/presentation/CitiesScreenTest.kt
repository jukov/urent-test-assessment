package info.jukov.urenttestassestment.feature.cities.presentation

import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

    @Test
    fun `pull to refresh distance includes status bar height`() {
        val statusBarHeight = 24.dp

        assertEquals(
            PullToRefreshDefaults.PositionalThreshold + statusBarHeight,
            pullToRefreshDistance(statusBarHeight = statusBarHeight)
        )
    }

    @Test
    fun `pull to refresh is enabled for refreshable content states`() {
        val cities = listOf(CityUi(id = 1, title = "Москва, Россия"))

        assertTrue(CitiesContentState.Content(cities = cities).isPullToRefreshEnabled())
        assertTrue(CitiesContentState.ContentProgress(cities = cities).isPullToRefreshEnabled())
        assertTrue(CitiesContentState.ContentRefreshing(cities = cities).isPullToRefreshEnabled())
        assertTrue(CitiesContentState.ContentEndReached(cities = cities).isPullToRefreshEnabled())
    }

    @Test
    fun `pull to refresh is disabled for non-refreshable states`() {
        val cities = listOf(CityUi(id = 1, title = "Москва, Россия"))

        assertFalse(CitiesContentState.EmptyProgress.isPullToRefreshEnabled())
        assertFalse(CitiesContentState.Empty(query = "unknown").isPullToRefreshEnabled())
        assertFalse(CitiesContentState.EmptyFailure(message = "network").isPullToRefreshEnabled())
        assertFalse(
            CitiesContentState.ContentFailure(
                cities = cities,
                message = "network"
            ).isPullToRefreshEnabled()
        )
    }
}
