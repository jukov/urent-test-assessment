package info.jukov.urenttestassessment.startup

import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import info.jukov.urenttestassessment.R
import info.jukov.urenttestassessment.core.ui.theme.URentTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class StartupGateTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun navigationIsNotCreatedUntilAdmissionAndRetryStaysOnOfflineScreen() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val state = mutableStateOf(StartupState.Checking)
        var contentCreations = 0
        var retries = 0
        compose.setContent {
            URentTheme {
                StartupGate(state.value, { retries++ }) {
                    contentCreations++
                    Text("Main content")
                }
            }
        }
        compose.onNodeWithText("Main content").assertDoesNotExist()
        compose.runOnIdle {
            assertEquals(0, contentCreations)
            state.value = StartupState.Offline
        }
        val retry = context.getString(R.string.startup_retry)
        val title = context.getString(R.string.startup_offline_title)
        compose.onNodeWithText(title).assertIsDisplayed()
        compose.onNodeWithText(retry).performClick()
        compose.runOnIdle {
            assertEquals(1, retries)
            assertEquals(0, contentCreations)
            state.value = StartupState.Retrying
        }
        compose.onNodeWithText(title).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.startup_checking)).assertIsNotEnabled()
        compose.onNodeWithText("Main content").assertDoesNotExist()
        compose.runOnIdle { state.value = StartupState.Ready }
        compose.onNodeWithText("Main content").assertIsDisplayed()
        compose.onNodeWithText(title).assertDoesNotExist()
    }
}
