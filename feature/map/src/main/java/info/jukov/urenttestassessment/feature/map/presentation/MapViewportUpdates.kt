package info.jukov.urenttestassessment.feature.map.presentation

import kotlin.math.abs
import org.maplibre.android.gestures.MoveGestureDetector
import org.maplibre.android.maps.MapLibreMap

internal class MapViewportUpdates(private val map: MapLibreMap) {
    // Focus only pans. Zoom changes also identify gestures whose move-start event was coalesced.
    private var focusZoom: Double? = null
    val loadCities: Boolean
        get() = focusZoom?.let { abs(map.cameraPosition.zoom - it) > 1e-6 } ?: true

    // easeCamera/animateCamera report API_ANIMATION; built-in user animations use DEVELOPER_ANIMATION.
    private val started = MapLibreMap.OnCameraMoveStartedListener { reason ->
        if (reason != MapLibreMap.OnCameraMoveStartedListener.REASON_API_ANIMATION) onUserMove()
    }

    // MapLibre can coalesce move-start events when a gesture interrupts an animation.
    // Gesture callbacks still report that the user has taken control of the camera.
    private val move = object : MapLibreMap.OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) = onUserMove()
        override fun onMove(detector: MoveGestureDetector) = Unit
        override fun onMoveEnd(detector: MoveGestureDetector) = Unit
    }
    init {
        map.addOnCameraMoveStartedListener(started)
        map.addOnMoveListener(move)
    }

    fun onCityFocus() {
        // Keep ownership through completion/cancellation and any duplicate idle events.
        // A later user gesture or zoom explicitly enables viewport requests again.
        focusZoom = map.cameraPosition.zoom
    }

    fun onUserMove() {
        focusZoom = null
    }

    fun dispose() {
        map.removeOnCameraMoveStartedListener(started)
        map.removeOnMoveListener(move)
    }
}
