package info.jukov.urenttestassestment.feature.map.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.maplibre.android.maps.ImageStretches

class MarkerBubbleTest {
    @Test
    fun `sprite density gives a sixteen dp outer corner radius`() {
        val pixelRatio = MarkerBubbleShape.DENSITY_DPI / 160f

        assertEquals(16f, MarkerBubbleShape.CORNER_RADIUS_PX / pixelRatio, 0f)
        assertEquals(1f, MarkerBubbleShape.BORDER_WIDTH_PX / pixelRatio, 0f)
    }

    @Test
    fun `pointer has a small rounded cap contained in its fixed width`() {
        val radius = MarkerBubbleShape.POINTER_TIP_RADIUS_PX

        assertEquals(2f, radius / MarkerBubbleShape.PIXEL_RATIO, 0f)
        assertTrue(radius > MarkerBubbleShape.BORDER_WIDTH_PX / 2)
        assertTrue(radius < MarkerBubbleShape.POINTER_HALF_WIDTH_PX)
        assertTrue(MarkerBubbleShape.POINTER_TIP_CENTER_Y_PX - radius >= MarkerBubbleShape.BODY_HEIGHT_PX)
    }

    @Test
    fun `rounded cap outer edge keeps the original coordinate anchor`() {
        val strokeInset = MarkerBubbleShape.BORDER_WIDTH_PX / 2
        val pathRadius = MarkerBubbleShape.POINTER_TIP_RADIUS_PX - strokeInset
        val outerBottom = MarkerBubbleShape.POINTER_TIP_CENTER_Y_PX + pathRadius + strokeInset

        assertEquals(MarkerBubbleShape.HEIGHT_PX.toFloat(), outerBottom, 0f)
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
        listOf(0f, 3f, 12f, 44f, 88f, 160f).forEach { textWidth ->
            // Native clamps icon quads to the fixed stretch content minimum.
            val widthDp = (textWidth + MarkerLabelLayout.padding[1] + MarkerLabelLayout.padding[3])
                .coerceAtLeast(40f)
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
    fun `single line body fits fixed corners without vertical clamping moving the tip`() {
        val ratio = MarkerBubbleShape.PIXEL_RATIO
        val fixedHeight = MarkerBubbleShape.BODY_HEIGHT_PX - MarkerBubbleShape.stretchesY.sumOf {
            (it.second - it.first).toDouble()
        }.toFloat()
        val minimumHeight = MarkerLabelLayout.LINE_HEIGHT + MarkerLabelLayout.padding[0] + MarkerLabelLayout.padding[2]

        assertEquals(32f, minimumHeight, 0f)
        assertTrue(minimumHeight * ratio >= fixedHeight)
    }

    @Test
    fun `pointer tip remains at coordinate for one and multiple text lines`() {
        val body = MarkerBubbleShape.content.contentArray
        assertEquals(MarkerBubbleShape.BODY_HEIGHT_PX.toFloat(), body[3], 0f)
        assertEquals("bottom", MarkerLabelLayout.TEXT_ANCHOR)
        val ratio = MarkerBubbleShape.PIXEL_RATIO
        listOf(1, 2, 3).forEach { lineCount ->
            val textHeight = lineCount * MarkerLabelLayout.LINE_HEIGHT
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
