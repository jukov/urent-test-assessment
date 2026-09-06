package info.jukov.urenttestassessment.feature.map.presentation

import android.graphics.PointF
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap

internal class MapCityCamera(private val map: MapLibreMap, private val onFocus: () -> Unit = {}) {
    private var animating = false
    private var generation = 0L

    fun focus(city: MapCityUi, sheetHeightPx: Int) {
        onFocus()
        cancel()
        val point = map.projection.toScreenLocation(LatLng(city.latitude, city.longitude))
        // The camera target is the full map's center. Move it half a sheet south in
        // screen space so the selected coordinate lands at the uncovered area's center.
        // No persistent camera padding is needed, so normal pan/zoom and saved cameras
        // retain their existing meaning after the sheet closes.
        val target = map.projection.fromScreenLocation(PointF(point.x, point.y + sheetHeightPx / 2f))
        animating = true
        val expectedGeneration = generation
        map.easeCamera(
            CameraUpdateFactory.newLatLng(target),
            CITY_FOCUS_DURATION_MILLIS,
            object : MapLibreMap.CancelableCallback {
                override fun onCancel() {
                    if (generation == expectedGeneration) animating = false
                }
                override fun onFinish() {
                    if (generation == expectedGeneration) animating = false
                }
            }
        )
    }

    fun cancel() {
        // MapLibre posts completion callbacks to Main; invalidate old callbacks before cancelling.
        generation++
        if (animating) map.cancelTransitions()
        animating = false
    }
}

private const val CITY_FOCUS_DURATION_MILLIS = 350
