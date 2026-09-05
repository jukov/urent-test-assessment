package info.jukov.urenttestassestment.feature.map.presentation

import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipInputStream

internal class MarkerGlyphAssets(private val openArchive: () -> InputStream) {
    private val ranges by lazy {
        ZipInputStream(openArchive()).use { archive ->
            buildMap {
                var entry = archive.nextEntry
                while (entry != null) {
                    put(entry.name, archive.readBytes())
                    entry = archive.nextEntry
                }
            }
        }
    }

    fun load(range: String): ByteArray =
        ranges[range] ?: throw IOException("Missing bundled Roboto glyph range: $range")
}
