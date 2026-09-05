package info.jukov.urenttestassestment.feature.map.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkerBubbleTest {
    @Test
    fun `sprite density gives an eight dp outer corner radius`() {
        val pixelRatio = MarkerBubbleShape.DENSITY_DPI / 160f

        assertEquals(8f, MarkerBubbleShape.CORNER_RADIUS_PX / pixelRatio, 0f)
        assertEquals(1f, MarkerBubbleShape.BORDER_WIDTH_PX / pixelRatio, 0f)
    }

    @Test
    fun `stretch regions exclude the full corner arcs on both sides`() {
        val stretch = MarkerBubbleShape.stretches.single()

        assertTrue(stretch.first >= MarkerBubbleShape.CORNER_RADIUS_PX)
        assertTrue(stretch.second <= MarkerBubbleShape.SIZE_PX - MarkerBubbleShape.CORNER_RADIUS_PX)
        assertTrue(stretch.first < stretch.second)
    }

    @Test
    fun `short long and multiline labels keep the same corner dimensions`() {
        val pixelRatio = MarkerBubbleShape.DENSITY_DPI / 160f
        // Target extents in dp represent short/long labels and one/two text lines.
        listOf(28f, 40f, 100f, 180f).forEach { targetDp ->
            val targetPx = targetDp * pixelRatio
            val radius = MarkerBubbleShape.CORNER_RADIUS_PX
            assertEquals(8f, stretchedCoordinate(radius, targetPx) / pixelRatio, 0f)
            assertEquals(
                8f,
                (targetPx - stretchedCoordinate(MarkerBubbleShape.SIZE_PX - radius, targetPx)) / pixelRatio,
                0f
            )
        }
    }

    // Nine-slice mapping: only the registered interval absorbs the change in label size.
    private fun stretchedCoordinate(position: Float, targetSize: Float): Float {
        val stretch = MarkerBubbleShape.stretches.single()
        val extra = targetSize - MarkerBubbleShape.SIZE_PX
        return when {
            position <= stretch.first -> position
            position >= stretch.second -> position + extra
            else -> position + extra * (position - stretch.first) / (stretch.second - stretch.first)
        }
    }
}
