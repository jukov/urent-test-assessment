package info.jukov.urenttestassestment.feature.map.presentation

import android.graphics.Color
import info.jukov.urenttestassestment.feature.map.domain.Coordinate
import info.jukov.urenttestassestment.feature.map.domain.MapViewport
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression.get
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory.iconImage
import org.maplibre.android.style.layers.PropertyFactory.iconTextFit
import org.maplibre.android.style.layers.PropertyFactory.iconTextFitPadding
import org.maplibre.android.style.layers.PropertyFactory.textAnchor
import org.maplibre.android.style.layers.PropertyFactory.textColor
import org.maplibre.android.style.layers.PropertyFactory.textField
import org.maplibre.android.style.layers.PropertyFactory.textFont
import org.maplibre.android.style.layers.PropertyFactory.textMaxWidth
import org.maplibre.android.style.layers.PropertyFactory.textOffset
import org.maplibre.android.style.layers.PropertyFactory.textSize
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource

internal class MapController(
    private val map: MapLibreMap,
    camera: MapCamera,
    private val onIdle: (MapCamera, MapViewport) -> Unit,
    private val onStyleError: (Boolean) -> Unit
) {
    private var disposed = false
    private val markerData = MapMarkerData()
    private val idleListener = MapLibreMap.OnCameraIdleListener { publishViewport() }

    init {
        map.uiSettings.isRotateGesturesEnabled = false
        map.uiSettings.isTiltGesturesEnabled = false
        map.cameraPosition = CameraPosition.Builder()
            .target(LatLng(camera.latitude, camera.longitude)).zoom(camera.zoom).build()
        map.addOnCameraIdleListener(idleListener)
        loadStyle()
    }

    fun loadStyle() {
        if (disposed) return
        onStyleError(false)
        map.setStyle(Style.Builder().fromUri(STYLE_URL)) { style ->
            if (disposed) return@setStyle
            style.addImage(
                BUBBLE_IMAGE,
                markerBubble(),
                MarkerBubbleShape.stretchesX,
                MarkerBubbleShape.stretchesY,
                MarkerBubbleShape.content
            )
            style.addSource(GeoJsonSource(CITIES_SOURCE, markerData.features))
            style.addLayer(
                SymbolLayer(CITIES_LAYER, CITIES_SOURCE).withProperties(
                    iconImage(BUBBLE_IMAGE),
                    iconTextFit(Property.ICON_TEXT_FIT_BOTH),
                    iconTextFitPadding(MarkerLabelLayout.padding),
                    textField(get("name")),
                    textSize(MarkerLabelLayout.TEXT_SIZE),
                    textAnchor(MarkerLabelLayout.TEXT_ANCHOR),
                    textOffset(MarkerLabelLayout.textOffset),
                    textColor(Color.WHITE),
                    textFont(arrayOf("Noto Sans Regular")),
                    textMaxWidth(LABEL_MAX_WIDTH)
                )
            )
            publishViewport()
        }
    }

    fun update(cities: List<MapCityUi>) {
        if (disposed) return
        val features = markerData.update(cities) ?: return
        map.style?.getSourceAs<GeoJsonSource>(CITIES_SOURCE)?.setGeoJson(features)
    }

    fun zoom(increase: Boolean) {
        map.animateCamera(if (increase) CameraUpdateFactory.zoomIn() else CameraUpdateFactory.zoomOut())
    }

    fun dispose() {
        disposed = true
        map.removeOnCameraIdleListener(idleListener)
    }

    fun publishViewport() {
        val camera = map.cameraPosition
        val center = camera.target
        if (disposed || center == null) return
        val region = map.projection.visibleRegion
        onIdle(
            MapCamera(center.latitude, center.longitude, camera.zoom),
            MapViewport.fromCorners(
                center.toCoordinate(),
                listOf(region.farLeft, region.farRight, region.nearLeft, region.nearRight)
                    .map { (it ?: return).toCoordinate() }
            )
        )
    }
}

private fun LatLng.toCoordinate() = Coordinate(latitude, longitude)

private const val STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"
private const val CITIES_SOURCE = "urent-cities"
private const val CITIES_LAYER = "urent-city-labels"
private const val BUBBLE_IMAGE = "urent-city-bubble"

private const val LABEL_MAX_WIDTH = 12f
