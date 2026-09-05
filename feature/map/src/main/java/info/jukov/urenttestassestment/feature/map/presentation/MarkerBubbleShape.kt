package info.jukov.urenttestassestment.feature.map.presentation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.DisplayMetrics
import androidx.core.graphics.createBitmap
import org.maplibre.android.maps.ImageStretches

// A 2x sprite with fixed 8dp corners. Only its straight middle sections can stretch.
internal object MarkerBubbleShape {
    const val DENSITY_DPI = DisplayMetrics.DENSITY_XHIGH
    const val SIZE_PX = 40
    const val CORNER_RADIUS_PX = 16f
    const val BORDER_WIDTH_PX = 2f
    val stretches = listOf(ImageStretches(CORNER_RADIUS_PX, SIZE_PX - CORNER_RADIUS_PX))
}

internal fun markerBubble(): Bitmap {
    val bitmap = createBitmap(MarkerBubbleShape.SIZE_PX, MarkerBubbleShape.SIZE_PX).apply {
        // MapLibre derives the sprite pixel ratio from bitmap density, not screen density.
        density = MarkerBubbleShape.DENSITY_DPI
    }
    val inset = MarkerBubbleShape.BORDER_WIDTH_PX / 2
    val bounds = RectF(inset, inset, bitmap.width - inset, bitmap.height - inset)
    val radius = MarkerBubbleShape.CORNER_RADIUS_PX - inset
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = MARKER_BACKGROUND_COLOR }
    canvas.drawRoundRect(bounds, radius, radius, paint)
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = MarkerBubbleShape.BORDER_WIDTH_PX
    paint.color = Color.WHITE
    canvas.drawRoundRect(bounds, radius, radius, paint)
    return bitmap
}

private const val MARKER_BACKGROUND_COLOR = 0xFF4E495B.toInt()
