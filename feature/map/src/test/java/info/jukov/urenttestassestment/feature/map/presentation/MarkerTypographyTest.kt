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
    }

    @Test
    fun `visible Roboto capitals are centered in compact single and multiline pins`() {
        // Packaged Roboto glyph metrics for M / М: top = -5, height = 17 at 24px.
        // Moscow / Москва have the same ink bounds. Native's shaping yOffset is -17.
        val inkCenterFromLineCenter = (-17f + 5f + 17f / 2) * MarkerLabelLayout.TEXT_SIZE / 24f
        listOf(1, 2, 3).forEach { lines ->
            val textHeight = lines * MarkerLabelLayout.LINE_HEIGHT
            val textBottom = MarkerLabelLayout.textOffset[1] * MarkerLabelLayout.TEXT_SIZE
            val inkCenter = textBottom - textHeight / 2 + inkCenterFromLineCenter
            val bodyTop = textBottom - textHeight - MarkerLabelLayout.padding[0]
            val bodyBottom = textBottom + MarkerLabelLayout.padding[2]

            assertEquals((bodyTop + bodyBottom) / 2, inkCenter, 0.5f)
        }
    }

    @Test
    fun `Moscow pin is narrower and shorter while retaining room for sixteen dp corners`() {
        // Sum of the packaged glyph advances for Moscow, at the 24px glyph size.
        val textWidth = (21f + 13f + 12f + 12f + 13f + 17f) * MarkerLabelLayout.TEXT_SIZE / 24f
        val width = textWidth + MarkerLabelLayout.padding[1] + MarkerLabelLayout.padding[3]
        val height = MarkerLabelLayout.LINE_HEIGHT + MarkerLabelLayout.padding[0] + MarkerLabelLayout.padding[2]

        assertEquals(64f, width, 0f)
        assertEquals(32f, height, 0f)
    }
}
