package info.jukov.urenttestassestment.feature.map.presentation

import java.io.FileInputStream
import java.io.IOException
import java.security.MessageDigest
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkerFontInterceptorTest {
    @Test
    fun `Latin and Cyrillic glyphs are served from packaged assets without network`() {
        val client = client(assets()) { error("Pin font must not reach the network") }
        val expected = mapOf(
            "0-255.pbf" to "9c78f773acfc25ea0a9844f959f293614873c74874ee47d8740c79f6ae16c62b",
            "1024-1279.pbf" to "a9f7ede0803b3d7be60b5995fc01f74cea1f5dfdc114cb6f41cdffea6fed1d7b"
        )
        expected.forEach { (range, hash) ->
            val request = Request.Builder().url("https://tiles.openfreemap.org/fonts/Roboto%20Bold/$range").build()
            client.newCall(request).execute().use { response ->
                assertEquals(200, response.code)
                assertEquals("application/x-protobuf", response.body.contentType().toString())
                assertEquals(hash, sha256(response.body.bytes()))
            }
        }
    }

    @Test
    fun `all BMP ranges including empty ones are bundled and archive opens once`() {
        var opens = 0
        val glyphs = MarkerGlyphAssets {
            opens++
            archive()
        }
        repeat(256) { block ->
            val start = block * 256
            assertTrue(glyphs.load("$start-${start + 255}.pbf").isNotEmpty())
        }
        glyphs.load("0-255.pbf")
        assertEquals(1, opens)
    }

    @Test
    fun `basemap fonts tiles and other hosts pass through unchanged`() {
        val forwarded = mutableListOf<String>()
        val client = client(MarkerGlyphAssets { error("Unrelated resource must not open the font archive") }) { chain ->
            forwarded += chain.request().url.toString()
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
                .code(200).message("OK").body("passthrough".toResponseBody()).build()
        }
        val urls = listOf(
            "https://tiles.openfreemap.org/fonts/Noto%20Sans%20Regular/0-255.pbf",
            "https://tiles.openfreemap.org/fonts/Roboto%20Bold,Noto%20Sans%20Regular/0-255.pbf",
            "https://tiles.openfreemap.org/planet/0/0/0.pbf",
            "https://example.com/fonts/Roboto%20Bold/0-255.pbf"
        )
        urls.forEach { url ->
            client.newCall(Request.Builder().url(url).build()).execute().use {
                assertEquals("passthrough", it.body.string())
            }
        }
        assertEquals(urls, forwarded)
    }

    @Test(expected = IOException::class)
    fun `missing local range fails without attempting a remote font download`() {
        val client = client(assets()) { error("Missing glyph range must not fall back to network") }
        client.newCall(
            Request.Builder().url("https://tiles.openfreemap.org/fonts/Roboto%20Bold/missing.pbf").build()
        ).execute().close()
    }

    private fun client(glyphs: MarkerGlyphAssets, next: Interceptor) = OkHttpClient.Builder()
        .addInterceptor(MarkerFontInterceptor(glyphs)).addInterceptor(next).build()

    private fun assets() = MarkerGlyphAssets { archive() }

    private fun archive() = FileInputStream(checkNotNull(System.getProperty("markerFontArchive")))

    private fun sha256(bytes: ByteArray) = MessageDigest.getInstance("SHA-256").digest(bytes)
        .joinToString("") { "%02x".format(it) }
}
