package info.jukov.urenttestassestment.core.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import info.jukov.urenttestassestment.core.ui.component.buttonContainerColor
import info.jukov.urenttestassestment.core.ui.component.textFieldVisuals
import info.jukov.urenttestassestment.core.ui.icon.Close
import info.jukov.urenttestassestment.core.ui.icon.List
import info.jukov.urenttestassestment.core.ui.icon.Map
import info.jukov.urenttestassestment.core.ui.icon.Pin
import info.jukov.urenttestassestment.core.ui.icon.Search
import info.jukov.urenttestassestment.core.ui.icon.URentIcons
import info.jukov.urenttestassestment.core.ui.theme.AccentBrand
import info.jukov.urenttestassestment.core.ui.theme.BackgroundPrimary
import info.jukov.urenttestassestment.core.ui.theme.BackgroundTertiary
import info.jukov.urenttestassestment.core.ui.theme.ButtonPrimaryBackground
import info.jukov.urenttestassestment.core.ui.theme.ButtonPrimaryBackgroundPressed
import org.junit.Assert.assertEquals
import org.junit.Test

class DesignSystemTest {
    @Test
    fun `button and input state mappings match specification`() {
        assertEquals(ButtonPrimaryBackground, buttonContainerColor(isPressed = false))
        assertEquals(ButtonPrimaryBackgroundPressed, buttonContainerColor(isPressed = true))

        val unselected = textFieldVisuals(isFocused = false)
        assertEquals(BackgroundTertiary, unselected.background)
        assertEquals(Color.Transparent, unselected.borderColor)
        assertEquals(0.dp, unselected.borderWidth)

        val selected = textFieldVisuals(isFocused = true)
        assertEquals(BackgroundPrimary, selected.background)
        assertEquals(AccentBrand, selected.borderColor)
        assertEquals(1.5.dp, selected.borderWidth)
    }

    @Test
    fun `icons use expected dimensions and viewports`() {
        listOf(URentIcons.Search, URentIcons.Map, URentIcons.List, URentIcons.Pin).forEach {
            assertEquals(24.dp, it.defaultWidth)
            assertEquals(24.dp, it.defaultHeight)
            assertEquals(24f, it.viewportWidth)
            assertEquals(24f, it.viewportHeight)
        }
        assertEquals(24.dp, URentIcons.Close.defaultWidth)
        assertEquals(24.dp, URentIcons.Close.defaultHeight)
        assertEquals(48f, URentIcons.Close.viewportWidth)
        assertEquals(48f, URentIcons.Close.viewportHeight)
    }
}
