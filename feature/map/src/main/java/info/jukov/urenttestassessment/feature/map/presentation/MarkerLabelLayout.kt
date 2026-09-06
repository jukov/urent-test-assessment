package info.jukov.urenttestassessment.feature.map.presentation

import org.maplibre.android.style.layers.Property

internal object MarkerLabelLayout {
    const val FONT = "Roboto Bold"
    const val TEXT_SIZE = 12f
    const val LINE_HEIGHT = 18f
    const val LINE_HEIGHT_EM = LINE_HEIGHT / TEXT_SIZE
    const val LETTER_SPACING_EM = 0f
    const val TEXT_JUSTIFY = Property.TEXT_JUSTIFY_CENTER
    const val TEXT_ANCHOR = Property.TEXT_ANCHOR_BOTTOM

    // A single line fits in a 32dp body, the minimum height for 16dp corners.
    // Roboto's visible capitals sit about 2dp above MapLibre's line-box center.
    // Balance the visible text by giving the logical box more padding above it.
    private const val TOP_PADDING = 9f
    private const val BOTTOM_PADDING = 5f
    private const val HORIZONTAL_PADDING = 10f

    // MapLibre's stretch metadata keeps very short labels at the sprite's fixed
    // minimum width (40dp), without adding that much padding to every city name.
    val padding = arrayOf(TOP_PADDING, HORIZONTAL_PADDING, BOTTOM_PADDING, HORIZONTAL_PADDING)

    // icon-text-fit ignores icon-anchor. Anchor the text's bottom above the body padding
    // and the fixed pointer, so the pointer's outer tip lands on the GeoJSON coordinate.
    val textOffset = arrayOf(
        0f,
        -(BOTTOM_PADDING + MarkerBubbleShape.POINTER_HEIGHT_PX / MarkerBubbleShape.PIXEL_RATIO) / TEXT_SIZE
    )
}
