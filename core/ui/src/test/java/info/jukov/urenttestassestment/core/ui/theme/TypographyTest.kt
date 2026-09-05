package info.jukov.urenttestassestment.core.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Test

class TypographyTest {
    @Test
    fun `screen title matches design specification`() {
        assertEquals(FontFamily.SansSerif, ScreenTitle.fontFamily)
        assertEquals(FontWeight.SemiBold, ScreenTitle.fontWeight)
        assertEquals(19.sp, ScreenTitle.fontSize)
        assertEquals(26.sp, ScreenTitle.lineHeight)
        assertEquals(0.sp, ScreenTitle.letterSpacing)
        assertEquals(TextAlign.Center, ScreenTitle.textAlign)
    }
}
