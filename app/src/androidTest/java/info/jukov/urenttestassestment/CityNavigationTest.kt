package info.jukov.urenttestassestment

import androidx.activity.BackEventCompat
import androidx.activity.ComponentActivity
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import info.jukov.urenttestassestment.core.ui.R as UiR
import info.jukov.urenttestassestment.core.ui.city.CityDetails
import info.jukov.urenttestassestment.core.ui.theme.URentTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CityNavigationTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun detailsCrossfadeInBothDirections() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        compose.setContent {
            URentTheme {
                CityNavigation(listContent = { open ->
                    Button({ open(CityDetails(7, "Moscow", "RU", 12_655_000)) }) { Text("Open city") }
                })
            }
        }
        compose.mainClock.autoAdvance = false
        compose.onNodeWithText("Open city").performClick()
        compose.mainClock.advanceTimeBy(96)
        compose.onNodeWithText("Open city").assertExists()
        compose.onNodeWithText("Moscow").assertIsDisplayed()
        compose.mainClock.advanceTimeBy(500)
        compose.onNodeWithText("Open city").assertDoesNotExist()

        compose.onNodeWithContentDescription(context.getString(UiR.string.city_back)).performClick()
        compose.mainClock.advanceTimeBy(96)
        compose.onNodeWithText("Moscow").assertExists()
        compose.onNodeWithText("Open city").assertIsDisplayed()
        compose.mainClock.advanceTimeBy(500)
        compose.onNodeWithText("Moscow").assertDoesNotExist()
    }

    @Test
    fun predictiveBackKeepsDetailsGeometryAndSupportsCancelAndCommit() {
        compose.setContent {
            URentTheme {
                CityNavigation(listContent = { open ->
                    Button({ open(CityDetails(7, "Moscow", "RU", 12_655_000)) }) { Text("Open city") }
                })
            }
        }
        compose.onNodeWithText("Open city").performClick()
        val originalBounds = compose.onNodeWithText("Moscow").fetchSemanticsNode().boundsInRoot
        val dispatcher = compose.activity.onBackPressedDispatcher
        compose.mainClock.autoAdvance = false

        fun progressBack() {
            compose.runOnUiThread {
                dispatcher.dispatchOnBackStarted(BackEventCompat(0f, 400f, 0f, BackEventCompat.EDGE_LEFT))
            }
            compose.mainClock.advanceTimeByFrame()
            compose.runOnUiThread {
                dispatcher.dispatchOnBackProgressed(BackEventCompat(200f, 400f, 0.5f, BackEventCompat.EDGE_LEFT))
            }
            compose.mainClock.advanceTimeBy(96)
            assertEquals(originalBounds, compose.onNodeWithText("Moscow").fetchSemanticsNode().boundsInRoot)
            compose.onNodeWithText("Open city").assertExists()
        }

        progressBack()
        compose.runOnUiThread { dispatcher.dispatchOnBackCancelled() }
        compose.mainClock.advanceTimeBy(500)
        compose.onNodeWithText("Moscow").assertIsDisplayed()
        compose.onNodeWithText("Open city").assertDoesNotExist()

        progressBack()
        compose.runOnUiThread { dispatcher.onBackPressed() }
        compose.mainClock.advanceTimeBy(500)
        compose.onNodeWithText("Moscow").assertDoesNotExist()
        compose.onNodeWithText("Open city").assertIsDisplayed()
    }

    @Test
    fun tabsReplaceContentWithoutWaitingForAnAnimation() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        compose.setContent {
            URentTheme {
                CityNavigation(
                    listContent = { Text("List content") },
                    mapContent = { Text("Map content") }
                )
            }
        }
        compose.onNodeWithText("List content").assertIsDisplayed()
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

        switchTab(R.string.tab_map, "Map content", "List content")
        switchTab(R.string.tab_cities, "List content", "Map content")
    }

    @Test
    fun tabsSwitchContentAndRestoreListInput() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        compose.setContent {
            URentTheme {
                CityNavigation(
                    listContent = {
                        var query by rememberSaveable { mutableStateOf("") }
                        TextField(query, { query = it }, Modifier.testTag("query"))
                    },
                    mapContent = { Text("Map content") }
                )
            }
        }
        compose.onNodeWithTag("query").performTextInput("Moscow")
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
        compose.onNodeWithTag("query").assertTextContains("Moscow")
    }

    @Test
    fun listOpensDetailsWithSavedDataAndBackRestoresInput() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            URentTheme {
                CityNavigation(listContent = { open ->
                    androidx.compose.foundation.layout.Column {
                        var query by rememberSaveable { mutableStateOf("") }
                        TextField(query, { query = it }, Modifier.testTag("query"))
                        Button({ open(CityDetails(7, "Moscow & York", "RU", 12_655_000)) }) { Text("Open city") }
                    }
                })
            }
        }
        compose.onNodeWithTag("query").performTextInput("Moscow")
        compose.onNodeWithText("Open city").performClick()
        compose.onNodeWithText("Moscow & York").assertIsDisplayed()
        compose.onNodeWithText(context.getString(UiR.string.city_search)).assertIsDisplayed()
        compose.onNodeWithContentDescription(context.getString(R.string.tab_map)).assertDoesNotExist()
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("Moscow & York").assertIsDisplayed()
        compose.onNodeWithContentDescription(context.getString(UiR.string.city_back)).performClick()
        compose.onNodeWithTag("query").assertTextContains("Moscow")
    }
}
