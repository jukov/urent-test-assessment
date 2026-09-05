package info.jukov.urenttestassestment.feature.map.presentation

import org.maplibre.android.storage.Resource

// OpenFreeMap serves Noto Sans but not Roboto. Route only our pin font to an immutable
// fontnik/OpenMapTiles glyph build; basemap fonts and all other resources keep their URLs.
internal object MarkerFontResources {
    private const val GLYPH_ORIGIN = "https://tiles.openfreemap.org/fonts/"
    private const val GLYPH_BUILD = "https://raw.githubusercontent.com/korywka/fonts.pbf/" +
        "2d1fd482a5afa0a856277148ee0af3cc6a9db0ff/"
    private val fontPath = MarkerLabelLayout.FONT.replace(" ", "%20") + "/"
    private val rangePattern = Regex("[0-9]+-[0-9]+\\.pbf")

    fun transform(@Resource.Kind kind: Int, url: String): String {
        if (kind != Resource.GLYPHS) return url
        val prefix = GLYPH_ORIGIN + fontPath
        val normalized = url.replace(" ", "%20")
        val range = normalized.removePrefix(prefix)
        return if (normalized.startsWith(prefix) && rangePattern.matches(range)) {
            GLYPH_BUILD + fontPath + range
        } else {
            url
        }
    }
}
