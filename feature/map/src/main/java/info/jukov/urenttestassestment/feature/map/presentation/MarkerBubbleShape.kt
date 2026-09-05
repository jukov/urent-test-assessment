package info.jukov.urenttestassestment.feature.map.presentation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.DisplayMetrics
import androidx.core.graphics.createBitmap
import org.maplibre.android.maps.ImageContent
import org.maplibre.android.maps.ImageStretches

// A 2x sprite: corners and the central pointer stay fixed while the straight edges stretch.
internal object MarkerBubbleShape {
    const val DENSITY_DPI = DisplayMetrics.DENSITY_XHIGH
    const val PIXEL_RATIO = DENSITY_DPI / DisplayMetrics.DENSITY_DEFAULT.toFloat()
    const val WIDTH_PX = 64
    const val BODY_HEIGHT_PX = 40
    const val POINTER_HEIGHT_PX = 12f
    const val HEIGHT_PX = BODY_HEIGHT_PX + POINTER_HEIGHT_PX.toInt()
    const val CENTER_X_PX = WIDTH_PX / 2f
    const val POINTER_HALF_WIDTH_PX = 8f
    const val CORNER_RADIUS_PX = 16f
    const val BORDER_WIDTH_PX = 2f
    val stretchesX = listOf(
        ImageStretches(CORNER_RADIUS_PX, CENTER_X_PX - POINTER_HALF_WIDTH_PX),
        ImageStretches(CENTER_X_PX + POINTER_HALF_WIDTH_PX, WIDTH_PX - CORNER_RADIUS_PX)
    )
    val stretchesY = listOf(ImageStretches(CORNER_RADIUS_PX, BODY_HEIGHT_PX - CORNER_RADIUS_PX))

    // Fit text and padding into the rectangular body, leaving the pointer outside it.
    val content = ImageContent(0f, 0f, WIDTH_PX.toFloat(), BODY_HEIGHT_PX.toFloat())
}

internal fun markerBubble(): Bitmap {
    val bitmap = createBitmap(MarkerBubbleShape.WIDTH_PX, MarkerBubbleShape.HEIGHT_PX).apply {
        // MapLibre derives the sprite pixel ratio from bitmap density, not screen density.
        density = MarkerBubbleShape.DENSITY_DPI
    }
    val inset = MarkerBubbleShape.BORDER_WIDTH_PX / 2
    val bounds = RectF(inset, inset, bitmap.width - inset, MarkerBubbleShape.BODY_HEIGHT_PX - inset)
    val radius = MarkerBubbleShape.CORNER_RADIUS_PX - inset
    val outline = Path().apply {
        addRoundRect(bounds, radius, radius, Path.Direction.CW)
        op(markerPointer(inset), Path.Op.UNION)
    }
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = MARKER_BACKGROUND_COLOR }
    canvas.drawPath(outline, paint)
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = MarkerBubbleShape.BORDER_WIDTH_PX
    paint.color = Color.WHITE
    canvas.drawPath(outline, paint)
    return bitmap
}

private fun markerPointer(inset: Float): Path {
    val center = MarkerBubbleShape.CENTER_X_PX
    val halfWidth = MarkerBubbleShape.POINTER_HALF_WIDTH_PX
    // Overlap the body so the union has a single outline without an internal border.
    val root = MarkerBubbleShape.BODY_HEIGHT_PX - MarkerBubbleShape.BORDER_WIDTH_PX
    val tip = MarkerBubbleShape.HEIGHT_PX - inset
    return Path().apply {
        moveTo(center - halfWidth, root)
        cubicTo(center - halfWidth / 2, root, center - inset, tip, center, tip)
        cubicTo(center + inset, tip, center + halfWidth / 2, root, center + halfWidth, root)
        close()
    }
}

private const val MARKER_BACKGROUND_COLOR = 0xFF4E495B.toInt()
