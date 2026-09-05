package info.jukov.urenttestassestment.feature.map.presentation

import android.graphics.Color
import info.jukov.urenttestassestment.feature.map.domain.Coordinate
import info.jukov.urenttestassestment.feature.map.domain.MapViewport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression.eq
import org.maplibre.android.style.expressions.Expression.get
import org.maplibre.android.style.expressions.Expression.literal
import org.maplibre.android.style.expressions.Expression.switchCase
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory.iconAllowOverlap
import org.maplibre.android.style.layers.PropertyFactory.iconImage
import org.maplibre.android.style.layers.PropertyFactory.iconTextFit
import org.maplibre.android.style.layers.PropertyFactory.iconTextFitPadding
import org.maplibre.android.style.layers.PropertyFactory.symbolSortKey
import org.maplibre.android.style.layers.PropertyFactory.textAllowOverlap
import org.maplibre.android.style.layers.PropertyFactory.textAnchor
import org.maplibre.android.style.layers.PropertyFactory.textColor
import org.maplibre.android.style.layers.PropertyFactory.textField
import org.maplibre.android.style.layers.PropertyFactory.textFont
import org.maplibre.android.style.layers.PropertyFactory.textJustify
import org.maplibre.android.style.layers.PropertyFactory.textLetterSpacing
import org.maplibre.android.style.layers.PropertyFactory.textLineHeight
import org.maplibre.android.style.layers.PropertyFactory.textMaxWidth
import org.maplibre.android.style.layers.PropertyFactory.textOffset
import org.maplibre.android.style.layers.PropertyFactory.textSize
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource

internal class MapController(
    private val map: MapLibreMap,
    camera: MapCamera,
    private val onIdle: (MapCamera, MapViewport?) -> Unit,
    private val onStyleError: (Boolean) -> Unit,
    private val onSelectCity: (Long?) -> Unit,
    private val selectedColor: Int
) {
    private val viewportUpdates = MapViewportUpdates(map)
    val cityCamera = MapCityCamera(map, viewportUpdates::onCityFocus)
    private var disposed = false
    private var selectedCityId: Long? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var markerJson = EMPTY_CITY_GEOJSON
    private val markerUpdates = MapMarkerUpdates(scope) { json ->
        markerJson = json
        map.style?.getSourceAs<GeoJsonSource>(CITIES_SOURCE)?.setGeoJson(json)
    }
    private val idleListener = MapLibreMap.OnCameraIdleListener { publishViewport(viewportUpdates.loadCities) }

    private val clickListener = MapLibreMap.OnMapClickListener { coordinate ->
        val feature = map.queryRenderedFeatures(map.projection.toScreenLocation(coordinate), CITIES_LAYER).firstOrNull()
        onSelectCity(feature?.id()?.toLongOrNull())
        feature != null
    }

    init {
        map.uiSettings.isRotateGesturesEnabled = false
        map.uiSettings.isTiltGesturesEnabled = false
        map.cameraPosition = CameraPosition.Builder()
            .target(LatLng(camera.latitude, camera.longitude)).zoom(camera.zoom).build()
        map.addOnCameraIdleListener(idleListener)
        map.addOnMapClickListener(clickListener)
        loadStyle()
    }

    fun loadStyle() {
        if (disposed) return
        onStyleError(false)
        map.setStyle(Style.Builder().fromUri(STYLE_URL)) { style ->
            if (disposed) return@setStyle
            style.addMarkerBubble()
            style.addMarkerBubble(SELECTED_BUBBLE_IMAGE, selectedColor)
            style.addSource(GeoJsonSource(CITIES_SOURCE, markerJson))
            style.addLayer(
                SymbolLayer(CITIES_LAYER, CITIES_SOURCE).withProperties(
                    symbolSortKey(get(POPULATION_PRIORITY)),
                    iconAllowOverlap(false),
                    textAllowOverlap(false),
                    iconImage(BUBBLE_IMAGE),
                    iconTextFit(Property.ICON_TEXT_FIT_BOTH),
                    iconTextFitPadding(MarkerLabelLayout.padding),
                    textField(get("name")),
                    textSize(MarkerLabelLayout.TEXT_SIZE),
                    textAnchor(MarkerLabelLayout.TEXT_ANCHOR),
                    textOffset(MarkerLabelLayout.textOffset),
                    textColor(Color.WHITE),
                    textFont(arrayOf(MarkerLabelLayout.FONT)),
                    textLineHeight(MarkerLabelLayout.LINE_HEIGHT_EM),
                    textLetterSpacing(MarkerLabelLayout.LETTER_SPACING_EM),
                    textJustify(MarkerLabelLayout.TEXT_JUSTIFY),
                    textMaxWidth(LABEL_MAX_WIDTH)
                )
            )
            updateSelection()
            publishViewport()
        }
    }

    fun update(cities: List<MapCityUi>) {
        if (disposed) return
        markerUpdates.submit(cities)
    }

    fun select(id: Long?) {
        if (selectedCityId == id) return
        selectedCityId = id
        updateSelection()
    }

    private fun updateSelection() {
        val selected = eq(get("id"), literal(selectedCityId ?: -1L))
        map.style?.getLayerAs<SymbolLayer>(CITIES_LAYER)?.setProperties(
            iconImage(switchCase(selected, literal(SELECTED_BUBBLE_IMAGE), literal(BUBBLE_IMAGE))),
            symbolSortKey(switchCase(selected, literal(SELECTED_PRIORITY), get(POPULATION_PRIORITY)))
        )
    }

    fun zoom(increase: Boolean) {
        viewportUpdates.onUserMove()
        map.animateCamera(if (increase) CameraUpdateFactory.zoomIn() else CameraUpdateFactory.zoomOut())
    }

    fun dispose() {
        cityCamera.cancel()
        viewportUpdates.dispose()
        disposed = true
        scope.cancel()
        map.removeOnCameraIdleListener(idleListener)
        map.removeOnMapClickListener(clickListener)
    }

    fun publishViewport(loadCities: Boolean = true) {
        val camera = map.cameraPosition
        val center = camera.target
        if (disposed || center == null) return
        val region = map.projection.visibleRegion
        onIdle(
            MapCamera(center.latitude, center.longitude, camera.zoom),
            if (!loadCities) {
                null
            } else {
                MapViewport.fromCorners(
                    center.toCoordinate(),
                    listOf(region.farLeft, region.farRight, region.nearLeft, region.nearRight)
                        .map { (it ?: return).toCoordinate() }
                )
            }
        )
    }
}

private fun Style.addMarkerBubble(image: String = BUBBLE_IMAGE, color: Int? = null) {
    val bitmap = if (color == null) markerBubble() else markerBubble(color)
    try {
        // Synchronous addImage copies pixel data; the temporary bitmap is no longer needed.
        addImage(
            image,
            bitmap,
            MarkerBubbleShape.stretchesX,
            MarkerBubbleShape.stretchesY,
            MarkerBubbleShape.content
        )
    } finally {
        bitmap.recycle()
    }
}

private fun LatLng.toCoordinate() = Coordinate(latitude, longitude)

private const val STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"
private const val CITIES_SOURCE = "urent-cities"
private const val CITIES_LAYER = "urent-city-labels"
private const val BUBBLE_IMAGE = "urent-city-bubble"

private const val LABEL_MAX_WIDTH = 12f

private const val SELECTED_BUBBLE_IMAGE = "urent-city-bubble-selected"
private const val SELECTED_PRIORITY = -100
