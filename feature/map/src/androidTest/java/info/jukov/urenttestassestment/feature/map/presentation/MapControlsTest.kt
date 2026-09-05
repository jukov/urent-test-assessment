package info.jukov.urenttestassestment.feature.map.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import info.jukov.urenttestassestment.core.ui.R as UiR
import info.jukov.urenttestassestment.core.ui.theme.URentTheme
import info.jukov.urenttestassestment.feature.map.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MapControlsTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun zoomButtonsDispatchOppositeDirections() {
        val directions = mutableListOf<Boolean>()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        compose.setContent { URentTheme { MapZoomControls({ directions += it }) } }
        compose.onNodeWithContentDescription(context.getString(R.string.map_zoom_in)).performClick()
        compose.onNodeWithContentDescription(context.getString(R.string.map_zoom_out)).performClick()
        compose.runOnIdle { assertEquals(listOf(true, false), directions) }
    }

    @Test
    fun selectingCityRaisesZoomAndClosingRestoresIt() {
        val selected = mutableStateOf<MapCityUi?>(null)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var searches = 0
        compose.setContent {
            URentTheme { MapCityOverlay(selected.value, {}, { selected.value = null }, { searches++ }) }
        }
        val zoom = compose.onNodeWithContentDescription(context.getString(R.string.map_zoom_out))
        val originalTop = zoom.fetchSemanticsNode().boundsInRoot.top
        compose.runOnIdle { selected.value = MapCityUi(1, "Moscow", 55.75, 37.62, 12_655_000, "RU") }
        compose.onNodeWithText("Moscow").assertIsDisplayed()
        compose.onNodeWithText(context.getString(UiR.string.city_search)).performClick()
        compose.runOnIdle { assertEquals(1, searches) }
        val detailsTop = compose.onNodeWithText(
            context.getString(UiR.string.city_label)
        ).fetchSemanticsNode().boundsInRoot.top
        assertTrue(zoom.fetchSemanticsNode().boundsInRoot.top < originalTop)
        assertTrue(zoom.fetchSemanticsNode().boundsInRoot.bottom < detailsTop)
        compose.onNodeWithTag("city_sheet_handle", useUnmergedTree = true).performTouchInput { click() }
        compose.onNodeWithText("Moscow").assertDoesNotExist()
        assertEquals(originalTop, zoom.fetchSemanticsNode().boundsInRoot.top)
    }

    @Test
    fun holdingSheetHandleDoesNotDrawRippleAndReleaseStillDismisses() {
        val selected = mutableStateOf<MapCityUi?>(MapCityUi(1, "Moscow", 55.75, 37.62, 100, "RU"))
        compose.setContent { URentTheme { MapCityOverlay(selected.value, {}, { selected.value = null }, {}) } }
        val handle = compose.onNodeWithTag("city_sheet_handle", useUnmergedTree = true)
        val before = handle.captureToImage().asAndroidBitmap()
        compose.mainClock.autoAdvance = false
        handle.performTouchInput { down(center) }
        compose.mainClock.advanceTimeBy(200)
        val pressed = handle.captureToImage().asAndroidBitmap()
        assertTrue("The handle must not change appearance while pressed", before.sameAs(pressed))
        handle.performTouchInput { up() }
        compose.mainClock.autoAdvance = true
        compose.onNodeWithText("Moscow").assertDoesNotExist()
    }

    @Test
    fun shortMapKeepsBothZoomTargetsAndScrollableSearchAccessible() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var minTouchHeight = 0f
        var searches = 0
        compose.setContent {
            minTouchHeight = with(LocalDensity.current) { 48.dp.toPx() }
            URentTheme {
                Box(Modifier.height(240.dp)) {
                    MapCityOverlay(MapCityUi(1, "Moscow", 55.75, 37.62, 12_655_000, "RU"), {}, {}, { searches++ })
                }
            }
        }
        val zoomIn = compose.onNodeWithContentDescription(
            context.getString(R.string.map_zoom_in)
        ).fetchSemanticsNode().boundsInRoot
        val zoomOut = compose.onNodeWithContentDescription(
            context.getString(R.string.map_zoom_out)
        ).fetchSemanticsNode().boundsInRoot
        val handle = compose.onNodeWithTag(
            "city_sheet_handle",
            useUnmergedTree = true
        ).fetchSemanticsNode().boundsInRoot
        assertTrue(zoomIn.height >= minTouchHeight)
        assertTrue(zoomOut.height >= minTouchHeight)
        assertTrue(zoomOut.bottom <= handle.top)
        compose.onNodeWithText(
            context.getString(UiR.string.city_search)
        ).performScrollTo().assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(1, searches) }
    }
}
