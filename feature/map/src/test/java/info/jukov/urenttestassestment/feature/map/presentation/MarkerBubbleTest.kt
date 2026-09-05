package info.jukov.urenttestassestment.feature.map.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.maplibre.android.maps.ImageStretches

class MarkerBubbleTest {
    @Test
    fun `sprite density gives an sixteen dp outer corner radius`() {
        val pixelRatio = MarkerBubbleShape.DENSITY_DPI / 160f

        assertEquals(16f, MarkerBubbleShape.CORNER_RADIUS_PX / pixelRatio, 0f)
        assertEquals(1f, MarkerBubbleShape.BORDER_WIDTH_PX / pixelRatio, 0f)
    }

    @Test
    fun `stretch regions exclude full corners and pointer on both axes`() {
        MarkerBubbleShape.stretchesX.forEach { stretch ->
            assertTrue(stretch.first >= MarkerBubbleShape.CORNER_RADIUS_PX)
            assertTrue(stretch.second <= MarkerBubbleShape.WIDTH_PX - MarkerBubbleShape.CORNER_RADIUS_PX)
            assertTrue(stretch.first < stretch.second)
            assertTrue(
                stretch.second <= MarkerBubbleShape.CENTER_X_PX - MarkerBubbleShape.POINTER_HALF_WIDTH_PX ||
                    stretch.first >= MarkerBubbleShape.CENTER_X_PX + MarkerBubbleShape.POINTER_HALF_WIDTH_PX
            )
        }
        val vertical = MarkerBubbleShape.stretchesY.single()
        assertTrue(vertical.first >= MarkerBubbleShape.CORNER_RADIUS_PX)
        assertTrue(vertical.second <= MarkerBubbleShape.BODY_HEIGHT_PX - MarkerBubbleShape.CORNER_RADIUS_PX)
        assertTrue(vertical.first < vertical.second)
    }

    @Test
    fun `short and long labels preserve corners and a centered fixed width pointer`() {
        val ratio = MarkerBubbleShape.PIXEL_RATIO
        listOf(40f, 44f, 100f, 180f).forEach { widthDp ->
            val widthPx = widthDp * ratio
            fun mapped(x: Float) = stretchedCoordinate(
                x,
                widthPx - MarkerBubbleShape.WIDTH_PX,
                MarkerBubbleShape.stretchesX
            )
            val radius = MarkerBubbleShape.CORNER_RADIUS_PX
            val center = MarkerBubbleShape.CENTER_X_PX
            val halfPointer = MarkerBubbleShape.POINTER_HALF_WIDTH_PX
            assertEquals(16f, mapped(radius) / ratio, 0f)
            assertEquals(16f, (widthPx - mapped(MarkerBubbleShape.WIDTH_PX - radius)) / ratio, 0f)
            assertEquals(widthPx / 2, mapped(center), 0f)
            assertEquals(halfPointer * 2, mapped(center + halfPointer) - mapped(center - halfPointer), 0f)
        }
    }

    @Test
    fun `shortest label has enough room for fixed corners and pointer`() {
        val ratio = MarkerBubbleShape.PIXEL_RATIO
        val fixedWidth = MarkerBubbleShape.WIDTH_PX - MarkerBubbleShape.stretchesX.sumOf {
            (it.second - it.first).toDouble()
        }.toFloat()
        val fixedHeight = MarkerBubbleShape.BODY_HEIGHT_PX - MarkerBubbleShape.stretchesY.sumOf {
            (it.second - it.first).toDouble()
        }.toFloat()
        val minimumWidth = MarkerLabelLayout.padding[1] + MarkerLabelLayout.padding[3]
        val minimumHeight = MarkerLabelLayout.TEXT_SIZE + MarkerLabelLayout.padding[0] + MarkerLabelLayout.padding[2]

        assertTrue(minimumWidth * ratio >= fixedWidth)
        assertTrue(minimumHeight * ratio >= fixedHeight)
    }

    @Test
    fun `pointer tip remains at coordinate for one and multiple text lines`() {
        val body = MarkerBubbleShape.content.contentArray
        assertEquals(MarkerBubbleShape.BODY_HEIGHT_PX.toFloat(), body[3], 0f)
        assertEquals("bottom", MarkerLabelLayout.TEXT_ANCHOR)
        val ratio = MarkerBubbleShape.PIXEL_RATIO
        listOf(12f, 28f, 44f).forEach { textHeight ->
            val textBottom = MarkerLabelLayout.textOffset[1] * MarkerLabelLayout.TEXT_SIZE
            val bodyTop = textBottom - textHeight - MarkerLabelLayout.padding[0]
            val bodyHeight = textHeight + MarkerLabelLayout.padding[0] + MarkerLabelLayout.padding[2]
            val extra = bodyHeight * ratio - body[3]
            val tip = stretchedCoordinate(
                MarkerBubbleShape.HEIGHT_PX.toFloat(),
                extra,
                MarkerBubbleShape.stretchesY
            )
            assertEquals(0f, bodyTop + tip / ratio, 0.00001f)
            assertEquals(
                MarkerBubbleShape.POINTER_HEIGHT_PX,
                tip - stretchedCoordinate(body[3], extra, MarkerBubbleShape.stretchesY),
                0f
            )
        }
    }

    // Each stretch interval absorbs its proportional share; all other pixels stay fixed.
    private fun stretchedCoordinate(position: Float, extra: Float, stretches: List<ImageStretches>): Float {
        val length = stretches.sumOf { (it.second - it.first).toDouble() }.toFloat()
        val before = stretches.sumOf {
            (position - it.first).coerceIn(0f, it.second - it.first).toDouble()
        }.toFloat()
        return position + extra * before / length
    }
}
