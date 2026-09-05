package info.jukov.urenttestassestment

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
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import info.jukov.urenttestassestment.core.ui.theme.URentTheme
import org.junit.Rule
import org.junit.Test

class CityNavigationTest {
    @get:Rule
    val compose = createComposeRule()

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
}
