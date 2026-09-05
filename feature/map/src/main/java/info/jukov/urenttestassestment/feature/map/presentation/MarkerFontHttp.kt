package info.jukov.urenttestassestment.feature.map.presentation

import android.content.Context
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import org.maplibre.android.module.http.HttpRequestUtil

internal object MarkerFontHttp {
    // Match MapLibre 13.4.1's default dispatcher so tile concurrency is unchanged.
    private const val MAX_REQUESTS_PER_HOST = 20
    private var configured = false

    @Synchronized
    fun configure(context: Context) {
        if (configured) return
        val assets = context.applicationContext.assets
        val glyphs = MarkerGlyphAssets { assets.open("fonts/roboto-bold-glyphs.zip") }
        HttpRequestUtil.setOkHttpClient(
            OkHttpClient.Builder()
                .dispatcher(Dispatcher().apply { maxRequestsPerHost = MAX_REQUESTS_PER_HOST })
                .addInterceptor(MarkerFontInterceptor(glyphs))
                .build()
        )
        configured = true
    }
}
