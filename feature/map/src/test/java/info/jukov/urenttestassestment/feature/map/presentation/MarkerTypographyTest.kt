package info.jukov.urenttestassestment.feature.map.presentation

import org.junit.Assert.assertEquals
import org.junit.Test

class MarkerTypographyTest {
    @Test
    fun `pin typography is Roboto Bold at twelve pixels with eighteen pixel leading`() {
        assertEquals("Roboto Bold", MarkerLabelLayout.FONT)
        assertEquals(12f, MarkerLabelLayout.TEXT_SIZE, 0f)
        assertEquals(18f, MarkerLabelLayout.LINE_HEIGHT_EM * MarkerLabelLayout.TEXT_SIZE, 0f)
        assertEquals(0f, MarkerLabelLayout.LETTER_SPACING_EM, 0f)
        assertEquals("center", MarkerLabelLayout.TEXT_JUSTIFY)
        assertEquals(MarkerLabelLayout.padding[0], MarkerLabelLayout.padding[2])
    }
}
