package info.jukov.urenttestassestment.feature.map.presentation

import org.maplibre.android.style.layers.Property

internal object MarkerLabelLayout {
    const val FONT = "Roboto Bold"
    const val TEXT_SIZE = 12f
    const val LINE_HEIGHT = 18f
    const val LINE_HEIGHT_EM = LINE_HEIGHT / TEXT_SIZE
    const val LETTER_SPACING_EM = 0f
    const val TEXT_JUSTIFY = Property.TEXT_JUSTIFY_CENTER
    const val TEXT_ANCHOR = Property.TEXT_ANCHOR_BOTTOM

    // Reserve room for 16dp corners and the fixed pointer even on the shortest labels.
    private const val VERTICAL_PADDING = 10f
    private const val HORIZONTAL_PADDING = 20f
    val padding = arrayOf(VERTICAL_PADDING, HORIZONTAL_PADDING, VERTICAL_PADDING, HORIZONTAL_PADDING)

    // icon-text-fit ignores icon-anchor. Anchor the text's bottom above the body padding
    // and the fixed pointer, so the pointer's outer tip lands on the GeoJSON coordinate.
    val textOffset = arrayOf(
        0f,
        -(VERTICAL_PADDING + MarkerBubbleShape.POINTER_HEIGHT_PX / MarkerBubbleShape.PIXEL_RATIO) / TEXT_SIZE
    )
}
