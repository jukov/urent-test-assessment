package info.jukov.urenttestassestment.core.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.jukov.urenttestassestment.core.ui.component.buttonContainerColor
import info.jukov.urenttestassestment.core.ui.component.textFieldVisuals
import info.jukov.urenttestassestment.core.ui.icon.URentIcons
import info.jukov.urenttestassestment.core.ui.theme.AccentBrand
import info.jukov.urenttestassestment.core.ui.theme.AccentDestructive
import info.jukov.urenttestassestment.core.ui.theme.BackgroundBrand
import info.jukov.urenttestassestment.core.ui.theme.BackgroundPrimary
import info.jukov.urenttestassestment.core.ui.theme.BackgroundTertiary
import info.jukov.urenttestassestment.core.ui.theme.ButtonPrimaryBackground
import info.jukov.urenttestassestment.core.ui.theme.ButtonPrimaryBackgroundPressed
import info.jukov.urenttestassestment.core.ui.theme.Title4
import info.jukov.urenttestassestment.core.ui.theme.URentSizes
import info.jukov.urenttestassestment.core.ui.theme.URentSpacing
import org.junit.Assert.assertEquals
import org.junit.Test

class DesignSystemTest {
    @Test
    fun `color tokens match the design specification`() {
        assertEquals(Color(0xFF804AFF), ButtonPrimaryBackground)
        assertEquals(Color(0xFF683BD3), ButtonPrimaryBackgroundPressed)
        assertEquals(Color(0xFFF6F6F7), BackgroundTertiary)
        assertEquals(Color.White, BackgroundPrimary)
        assertEquals(Color(0xFF804AFF), AccentBrand)
        assertEquals(Color(0xFFFF305A), AccentDestructive)
        assertEquals(Color(0xFFF5EDFF), BackgroundBrand)
    }

    @Test
    fun `title 4 uses semibold 16 by 20 typography`() {
        assertEquals(FontWeight.SemiBold, Title4.fontWeight)
        assertEquals(16.sp, Title4.fontSize)
        assertEquals(20.sp, Title4.lineHeight)
        assertEquals(0.sp, Title4.letterSpacing)
    }

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
        assertEquals(56.dp, URentSizes.inputMinHeight)
        assertEquals(16.dp, URentSpacing.space16)
        assertEquals(12.dp, URentSpacing.space12)
    }

    @Test
    fun `icons retain source svg dimensions`() {
        listOf(URentIcons.Search, URentIcons.Map, URentIcons.List, URentIcons.Pin).forEach {
            assertEquals(24.dp, it.defaultWidth)
            assertEquals(24.dp, it.defaultHeight)
            assertEquals(24f, it.viewportWidth)
            assertEquals(24f, it.viewportHeight)
        }
        assertEquals(48.dp, URentIcons.Close.defaultWidth)
        assertEquals(48.dp, URentIcons.Close.defaultHeight)
        assertEquals(48f, URentIcons.Close.viewportWidth)
        assertEquals(48f, URentIcons.Close.viewportHeight)
    }
}
