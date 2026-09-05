package info.jukov.urenttestassestment.feature.map.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.maplibre.android.storage.Resource

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

    @Test
    fun `Latin and Cyrillic pin glyphs resolve to pinned Roboto build`() {
        listOf("0-255.pbf", "1024-1279.pbf").forEach { range ->
            val encoded = "https://tiles.openfreemap.org/fonts/Roboto%20Bold/$range"
            val transformed = MarkerFontResources.transform(Resource.GLYPHS, encoded)
            assertEquals(
                "https://raw.githubusercontent.com/korywka/fonts.pbf/" +
                    "2d1fd482a5afa0a856277148ee0af3cc6a9db0ff/Roboto%20Bold/$range",
                transformed
            )
            assertEquals(
                transformed,
                MarkerFontResources.transform(Resource.GLYPHS, encoded.replace("%20", " "))
            )
        }
    }

    @Test
    fun `basemap fonts unrelated hosts and non glyph resources are untouched`() {
        val urls = listOf(
            "https://tiles.openfreemap.org/fonts/Noto%20Sans%20Regular/0-255.pbf",
            "https://tiles.openfreemap.org/fonts/Roboto%20Bold,Noto%20Sans%20Regular/0-255.pbf",
            "https://example.com/fonts/Roboto%20Bold/0-255.pbf",
            "https://tiles.openfreemap.org/fonts/Roboto%20Bold/not-a-range.pbf"
        )
        urls.forEach { assertEquals(it, MarkerFontResources.transform(Resource.GLYPHS, it)) }
        val glyph = "https://tiles.openfreemap.org/fonts/Roboto%20Bold/0-255.pbf"
        assertEquals(glyph, MarkerFontResources.transform(Resource.TILE, glyph))
        assertTrue(MarkerFontResources.transform(Resource.GLYPHS, glyph).startsWith("https://"))
    }
}
