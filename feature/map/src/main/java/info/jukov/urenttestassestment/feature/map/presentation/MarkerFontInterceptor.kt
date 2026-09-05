package info.jukov.urenttestassestment.feature.map.presentation

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

// An application interceptor responds before OkHttp attempts DNS or any network I/O.
internal class MarkerFontInterceptor(private val glyphs: MarkerGlyphAssets) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url
        val path = url.pathSegments
        val isPinFont = path.size == GLYPH_PATH_SEGMENTS && path[0] == "fonts" && path[1] == MarkerLabelLayout.FONT
        if (url.scheme != "https" || url.host != GLYPH_HOST || !isPinFont) {
            return chain.proceed(request)
        }
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(HTTP_OK)
            .message("OK")
            .body(glyphs.load(path.last()).toResponseBody(GLYPH_MEDIA_TYPE))
            .build()
    }
}

private const val GLYPH_HOST = "tiles.openfreemap.org"
private const val GLYPH_PATH_SEGMENTS = 3
private const val HTTP_OK = 200
private val GLYPH_MEDIA_TYPE = "application/x-protobuf".toMediaType()
