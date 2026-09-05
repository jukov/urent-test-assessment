package info.jukov.urenttestassestment

import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import info.jukov.urenttestassestment.core.ui.R as UiR
import info.jukov.urenttestassestment.core.ui.theme.URentTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class CityNavigationTest {
    // Queue callbacks from Orbit's worker dispatcher for execution by the Compose test clock.
    @get:Rule
    val compose = createComposeRule(effectContext = StandardTestDispatcher())

    private val fixture = CitiesTestFixture()

    @Test
    fun tabsReplaceContentWithoutWaitingForAnAnimation() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        compose.setContent {
            URentTheme {
                CityNavigation(
                    listContent = { fixture.Route(it) },
                    mapContent = { Text("Map content") }
                )
            }
        }
        completeInitialLoad()
        compose.onNodeWithText("Moscow & York, Russia").assertIsDisplayed()
        compose.mainClock.autoAdvance = false

        fun switchTab(label: Int, shown: String, removed: String) {
            compose.onAllNodes(
                isSelectable() and hasAnyDescendant(hasContentDescription(context.getString(label))),
                useUnmergedTree = true
            )[0].performClick()
            // Apply navigation and settle composition without allowing a fade to finish.
            compose.mainClock.advanceTimeBy(64)
            compose.onNodeWithText(shown).assertIsDisplayed()
            compose.onNodeWithText(removed).assertDoesNotExist()
        }

        switchTab(R.string.tab_map, "Map content", "Moscow & York, Russia")
        switchTab(R.string.tab_cities, "Moscow & York, Russia", "Map content")
    }

    @Test
    fun tabsSwitchContentAndRestoreListInput() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        compose.setContent {
            URentTheme {
                CityNavigation(
                    listContent = { fixture.Route(it) },
                    mapContent = { Text("Map content") }
                )
            }
        }
        completeInitialLoad()
        compose.onNode(hasSetTextAction()).performTextInput("Moscow")
        compose.waitUntil(5_000) { fixture.requests.size == 2 }
        fixture.succeed(1)
        awaitCityRow()
        compose.onNodeWithText("Moscow & York, Russia").assertIsDisplayed()
        val map = compose.onAllNodes(
            isSelectable() and hasAnyDescendant(hasContentDescription(context.getString(R.string.tab_map))),
            useUnmergedTree = true
        )[0]
        map.performClick().assertIsSelected()
        compose.onNodeWithText("Map content").assertIsDisplayed()
        map.performClick()
        compose.onNodeWithText("Map content").assertIsDisplayed()
        compose.onAllNodes(
            isSelectable() and hasAnyDescendant(hasContentDescription(context.getString(R.string.tab_cities))),
            useUnmergedTree = true
        )[0].performClick().assertIsSelected()
        compose.onNode(hasSetTextAction()).assertTextContains("Moscow")
    }

    @Test
    fun detailsButtonStaysInPlaceWhileReturningToTabs() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        compose.setContent {
            URentTheme {
                CityNavigation(listContent = { fixture.Route(it) })
            }
        }
        completeInitialLoad()
        compose.onNodeWithText("Moscow & York, Russia").performClick()
        compose.waitUntil(5_000) {
            compose.onAllNodesWithText("Moscow & York").fetchSemanticsNodes().size == 1
        }
        compose.waitForIdle()
        val search = compose.onNodeWithText(context.getString(UiR.string.city_search))
        search.assertIsDisplayed()
        val originalBounds = search.fetchSemanticsNode().boundsInRoot
        compose.mainClock.autoAdvance = false

        compose.onNodeWithContentDescription(context.getString(UiR.string.city_back)).performClick()
        repeat(3) {
            compose.mainClock.advanceTimeBy(48)
            compose.onNodeWithContentDescription(context.getString(R.string.tab_map)).assertExists()
            assertEquals(originalBounds, search.fetchSemanticsNode().boundsInRoot)
        }

        compose.mainClock.autoAdvance = true
        search.assertDoesNotExist()
        compose.onNodeWithText("Moscow & York, Russia").assertIsDisplayed()
        compose.onNodeWithContentDescription(context.getString(R.string.tab_map)).assertIsDisplayed()
    }

    @Test
    fun listOpensDetailsWithSavedDataAndBackRestoresInput() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            URentTheme {
                CityNavigation(listContent = { fixture.Route(it) })
            }
        }
        completeInitialLoad()
        compose.onNode(hasSetTextAction()).performTextInput("Moscow")
        compose.waitUntil(5_000) { fixture.requests.size == 2 }
        fixture.succeed(1)
        awaitCityRow()
        compose.onNodeWithText("Moscow & York, Russia").assertIsDisplayed()
        compose.onNodeWithText("Moscow & York, Russia").performClick()
        compose.waitUntil(5_000) {
            compose.onAllNodesWithText("Moscow & York").fetchSemanticsNodes().size == 1
        }
        compose.waitForIdle()
        compose.onNodeWithText("Moscow & York").assertIsDisplayed()
        compose.onNodeWithText(context.getString(UiR.string.city_search)).assertIsDisplayed()
        compose.onNodeWithContentDescription(context.getString(R.string.tab_map)).assertDoesNotExist()
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("Moscow & York").assertIsDisplayed()
        compose.onNodeWithContentDescription(context.getString(UiR.string.city_back)).performClick()
        compose.onNode(hasSetTextAction()).assertTextContains("Moscow")
    }

    private fun completeInitialLoad() {
        compose.waitUntil(5_000) { fixture.requests.size == 1 }
        fixture.succeed(0)
        awaitCityRow()
        compose.onNodeWithText("Moscow & York, Russia").assertIsDisplayed()
    }

    private fun awaitCityRow() = compose.waitUntil(5_000) {
        compose.onAllNodesWithText("Moscow & York, Russia").fetchSemanticsNodes().size == 1
    }
}
