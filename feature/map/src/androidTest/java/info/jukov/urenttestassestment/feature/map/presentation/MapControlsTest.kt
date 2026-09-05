package info.jukov.urenttestassestment.feature.map.presentation

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import info.jukov.urenttestassestment.core.ui.theme.URentTheme
import info.jukov.urenttestassestment.feature.map.R
import org.junit.Assert.assertEquals
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
}
