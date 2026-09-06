package info.jukov.urenttestassessment.feature.map.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.test.platform.app.InstrumentationRegistry
import info.jukov.urenttestassessment.core.ui.theme.URentTheme
import info.jukov.urenttestassessment.feature.map.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MapSheetGestureTest {
    @get:Rule
    val compose = createComposeRule()
    private val city = MapCityUi(1, "Moscow", 55.75, 37.62, 12_655_000, "RU")

    @Test
    fun draggingContentMovesSheetAndZoomTogetherBeforeReleaseThenDismisses() {
        val selected = mutableStateOf<MapCityUi?>(city)
        var dismissals = 0
        compose.setContent {
            URentTheme {
                MapCityOverlay(selected.value, {}, {
                    dismissals++
                    selected.value = null
                }, {})
            }
        }
        val content = compose.onNodeWithTag("city_sheet_content")
        val handle = compose.onNodeWithTag("city_sheet_handle", useUnmergedTree = true)
        val zoom = compose.onNodeWithContentDescription(
            InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.map_zoom_in)
        )
        val sheetTop = handle.fetchSemanticsNode().boundsInRoot.top
        val zoomTop = zoom.fetchSemanticsNode().boundsInRoot.top
        compose.mainClock.autoAdvance = false
        content.performTouchInput {
            down(Offset(width / 2f, 40f))
            moveBy(Offset(0f, 90f), 100)
        }
        compose.mainClock.advanceTimeByFrame()
        val firstSheetTop = handle.fetchSemanticsNode().boundsInRoot.top
        val firstZoomTop = zoom.fetchSemanticsNode().boundsInRoot.top
        assertTrue(firstSheetTop > sheetTop)
        assertEquals((firstSheetTop - sheetTop) / 2, firstZoomTop - zoomTop, 2f)
        content.performTouchInput { moveBy(Offset(0f, 180f), 100) }
        compose.mainClock.advanceTimeByFrame()
        val nextSheetTop = handle.fetchSemanticsNode().boundsInRoot.top
        assertTrue(nextSheetTop > firstSheetTop)
        assertEquals((nextSheetTop - sheetTop) / 2, zoom.fetchSemanticsNode().boundsInRoot.top - zoomTop, 2f)
        assertEquals(0, dismissals)
        content.performTouchInput { up() }
        compose.mainClock.autoAdvance = true
        compose.waitForIdle()
        compose.onNodeWithText("Moscow").assertDoesNotExist()
        assertEquals(1, dismissals)
    }

    @Test
    fun shortSlowDragReturnsToExpandedPositionWithoutDismissing() {
        var dismissals = 0
        compose.setContent { URentTheme { MapCityOverlay(city, {}, { dismissals++ }, {}) } }
        val handle = compose.onNodeWithTag("city_sheet_handle", useUnmergedTree = true)
        val top = handle.fetchSemanticsNode().boundsInRoot.top
        handle.performTouchInput {
            down(center)
            moveBy(Offset(0f, 35f), 500)
            advanceEventTime(500)
            up()
        }
        compose.waitForIdle()
        assertEquals(top, handle.fetchSemanticsNode().boundsInRoot.top, 1f)
        assertEquals(0, dismissals)
    }

    @Test
    fun selectingAnotherCityDuringExternalHideKeepsNewDetailsOpen() {
        val selected = mutableStateOf<MapCityUi?>(city)
        compose.setContent { URentTheme { MapCityOverlay(selected.value, {}, { selected.value = null }, {}) } }
        compose.waitForIdle()
        compose.mainClock.autoAdvance = false
        compose.runOnIdle { selected.value = null }
        compose.mainClock.advanceTimeBy(32)
        compose.runOnIdle { selected.value = city.copy(id = 2, name = "Paris", countryCode = "FR") }
        compose.mainClock.autoAdvance = true
        compose.waitForIdle()
        compose.onNodeWithText("Moscow").assertDoesNotExist()
        compose.onNodeWithText("Paris").assertExists()
    }

    @Test
    fun accessibilityOffersDismissWithoutUnsupportedPartialCollapse() {
        val selected = mutableStateOf<MapCityUi?>(city)
        compose.setContent { URentTheme { MapCityOverlay(selected.value, {}, { selected.value = null }, {}) } }
        compose.waitForIdle()
        compose.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.Collapse)).assertCountEquals(0)
        compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.Dismiss))
            .performSemanticsAction(SemanticsActions.Dismiss)
        compose.onNodeWithText("Moscow").assertDoesNotExist()
    }

    @Test
    fun sheetMeasurementIsReportedForEachSelectedCityEvenWhenHeightMatches() {
        val selected = mutableStateOf<MapCityUi?>(city)
        val measurements = mutableMapOf<Long, Int>()
        compose.setContent {
            URentTheme {
                MapCityOverlay(selected.value, {
                }, {}, {}, onSheetMeasured = { id, height -> measurements[id] = height })
            }
        }
        compose.waitForIdle()
        compose.runOnIdle {
            assertTrue(requireNotNull(measurements[1]) > 0)
            selected.value = city.copy(id = 2, name = "Paris")
        }
        compose.waitForIdle()
        compose.runOnIdle { assertEquals(measurements[1], measurements[2]) }
    }
}
