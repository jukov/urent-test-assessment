package info.jukov.urenttestassestment.feature.cities.presentation

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
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
    fun `header scroll request moves list to top without animation`() {
        val listState = LazyListState(firstVisibleItemIndex = 5, firstVisibleItemScrollOffset = 20)

        requestScrollToTop(listState)

        assertEquals(0, listState.firstVisibleItemIndex)
        assertEquals(0, listState.firstVisibleItemScrollOffset)
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
        assertTrue(
            CitiesContentState.ContentFailure(
                cities = cities,
                message = "network"
            ).isPullToRefreshEnabled()
        )
        assertTrue(CitiesContentState.ContentRefreshing(cities = cities).isPullToRefreshEnabled())
        assertTrue(CitiesContentState.ContentEndReached(cities = cities).isPullToRefreshEnabled())
    }

    @Test
    fun `pull to refresh is disabled for non-refreshable states`() {
        assertFalse(CitiesContentState.EmptyProgress.isPullToRefreshEnabled())
        assertFalse(CitiesContentState.Empty(query = "unknown").isPullToRefreshEnabled())
        assertFalse(CitiesContentState.EmptyFailure(message = "network").isPullToRefreshEnabled())
    }

    @Test
    fun `shimmer dark color remains visible against background`() {
        val background = Color.White

        val shimmer = shimmerDarkColor(background = background, content = Color.Black)

        assertTrue(shimmer.luminance() < background.luminance())
    }

    @Test
    fun `pagination progress and failure reserve the same height`() {
        val cities = listOf(CityUi(id = 1, title = "Москва, Россия"))
        val progressHeight = paginationFooterMinHeight(
            CitiesContentState.ContentProgress(cities = cities)
        )
        val failureHeight = paginationFooterMinHeight(
            CitiesContentState.ContentFailure(
                cities = cities,
                message = "network"
            )
        )

        assertTrue(progressHeight > 0.dp)
        assertEquals(progressHeight, failureHeight)
    }
}
