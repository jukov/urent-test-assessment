package info.jukov.urenttestassessment.feature.map.presentation

import android.graphics.PointF
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.viewinterop.AndroidView
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

class MapCityCameraTest {
    @get:Rule
    val compose = createComposeRule()
    private lateinit var map: MapLibreMap
    private lateinit var view: MapView
    private lateinit var camera: MapCityCamera
    private lateinit var updates: MapViewportUpdates
    private val idleRequests = mutableListOf<Boolean>()

    @Test
    fun bottomPinAnimatesToUncoveredCenterWithoutChangingZoomOrPadding() {
        setUpMap()
        lateinit var city: MapCityUi
        var bottom = 0
        val animatedTransitionFinished = AtomicBoolean(false)
        compose.runOnIdle {
            city = cityAt(1, 0.8f, 0.9f)
            bottom = (view.height * 0.4f).toInt()
            // Native frames can be skipped on CI; observe the transition contract instead of FPS.
            view.addOnCameraDidChangeListener { animated ->
                if (animated) animatedTransitionFinished.set(true)
            }
            camera.focus(city, bottom)
        }
        compose.waitUntil(5_000) { animatedTransitionFinished.get() }
        awaitCentered(city, bottom)
        compose.runOnIdle {
            assertEquals(9.5, map.cameraPosition.zoom, 0.001)
            assertTrue(map.cameraPosition.padding!!.all { it == 0.0 })
        }
    }

    @Test
    fun selectingAnotherCityReplacesAnimationAndUsesItsSheetHeight() {
        setUpMap()
        lateinit var first: MapCityUi
        lateinit var second: MapCityUi
        var bottom = 0
        compose.runOnIdle {
            first = cityAt(1, 0.8f, 0.9f)
            second = cityAt(2, 0.2f, 0.2f)
            bottom = (view.height * 0.5f).toInt()
            camera.focus(first, view.height / 3)
            camera.focus(second, bottom)
        }
        awaitCentered(second, bottom)
        // A later tap on the same city can center it again after a manual camera move.
        compose.runOnIdle {
            map.cameraPosition = CameraPosition.Builder(map.cameraPosition).target(first.coordinate()).build()
            camera.focus(second, bottom)
        }
        awaitCentered(second, bottom)
    }

    @Test
    fun dismissingDuringFocusCancelsThePendingCameraMove() {
        setUpMap()
        val completed = AtomicBoolean(false)
        lateinit var stopped: CameraPosition
        compose.runOnIdle {
            camera.focus(cityAt(1, 0.8f, 0.9f), view.height / 2)
            camera.cancel()
            stopped = map.cameraPosition
            // Run after the original animation would have completed.
            view.postDelayed({ completed.set(true) }, 500)
        }
        compose.waitUntil(5_000) { completed.get() }
        compose.runOnIdle { assertEquals(stopped, map.cameraPosition) }
    }

    @Test
    fun oldAnimationCallbackCannotPreventCancellingTheNewSelection() {
        setUpMap()
        val completed = AtomicBoolean(false)
        lateinit var stopped: CameraPosition
        compose.runOnIdle {
            val first = cityAt(1, 0.8f, 0.9f)
            val second = cityAt(2, 0.2f, 0.2f)
            camera.focus(first, view.height / 3)
            camera.focus(second, view.height / 2)
            // Let the first animation's posted cancellation callback run before cancelling B.
            view.post {
                camera.cancel()
                stopped = map.cameraPosition
                view.postDelayed({ completed.set(true) }, 500)
            }
        }
        compose.waitUntil(5_000) { completed.get() }
        compose.runOnIdle { assertEquals(stopped, map.cameraPosition) }
    }

    @Test
    fun focusAndRepeatedSelectionDoNotLoadButZoomDoes() {
        setUpMap()
        lateinit var city: MapCityUi
        compose.runOnIdle {
            idleRequests.clear()
            map.cameraPosition = CameraPosition.Builder(map.cameraPosition).zoom(9.5123456).build()
            city = cityAt(1, 0.8f, 0.9f)
            camera.focus(cityAt(2, 0.2f, 0.1f), view.height / 3)
            camera.focus(city, view.height / 2)
        }
        awaitCentered(city, view.height / 2)
        awaitIdleRequest(false)
        compose.runOnIdle {
            assertTrue(idleRequests.none { it })
            idleRequests.clear()
            updates.onUserMove()
            map.animateCamera(CameraUpdateFactory.zoomIn(), 100)
        }
        awaitIdleRequest(true)
    }

    @Test
    fun gestureInterruptingFocusLoadsTheUserViewport() {
        setUpMap()
        compose.runOnIdle {
            idleRequests.clear()
            camera.focus(cityAt(1, 0.8f, 0.9f), view.height / 2)
        }
        compose.runOnIdle { view.panAcrossMap() }
        awaitIdleRequest(true)
    }

    @Test
    fun cancellingFocusDoesNotLoadOrSuppressTheNextGesture() {
        setUpMap()
        compose.runOnIdle {
            idleRequests.clear()
            camera.focus(cityAt(1, 0.8f, 0.9f), view.height / 2)
            view.postDelayed({ camera.cancel() }, 50)
        }
        awaitIdleRequest(false)
        compose.runOnIdle {
            assertTrue(idleRequests.none { it })
            idleRequests.clear()
        }
        compose.runOnIdle { view.panAcrossMap() }
        awaitIdleRequest(true)
    }

    @Test
    fun twoFingerTapDuringFocusZoomsOutAndLoadsItsViewport() {
        setUpMap()
        compose.runOnIdle {
            idleRequests.clear()
            camera.focus(cityAt(1, 0.8f, 0.9f), view.height / 2)
            view.postDelayed({ view.tapWithTwoFingers() }, 50)
        }
        awaitIdleRequest(true)
        compose.runOnIdle { assertTrue(map.cameraPosition.zoom < 9.5) }
    }

    private fun awaitIdleRequest(load: Boolean) {
        compose.waitUntil(5_000) {
            var found = false
            compose.runOnIdle { found = idleRequests.contains(load) }
            found
        }
    }

    private fun setUpMap() {
        val ready = AtomicBoolean(false)
        compose.setContent {
            val context = LocalContext.current
            val mapView = remember {
                MapLibre.getInstance(context)
                MapView(context).apply { onCreate(null) }
            }
            DisposableEffect(mapView) {
                mapView.onStart()
                mapView.onResume()
                onDispose {
                    if (::camera.isInitialized) camera.cancel()
                    if (::updates.isInitialized) updates.dispose()
                    mapView.onPause()
                    mapView.onStop()
                    mapView.onDestroy()
                }
            }
            AndroidView(
                modifier = Modifier.fillMaxSize().testTag("native_map"),
                factory = {
                    view = mapView
                    mapView.getMapAsync { nativeMap ->
                        map = nativeMap
                        updates = MapViewportUpdates(map)
                        camera = MapCityCamera(map, updates::onCityFocus)
                        map.addOnCameraIdleListener {
                            idleRequests += updates.loadCities
                        }
                        map.setStyle(Style.Builder().fromJson(EMPTY_STYLE)) {
                            map.cameraPosition = CameraPosition.Builder()
                                .target(LatLng(55.75, 37.62)).zoom(9.5).build()
                            ready.set(true)
                        }
                    }
                    mapView
                }
            )
        }
        compose.waitUntil(5_000) { ready.get() }
        compose.waitForIdle()
    }

    private fun cityAt(id: Long, xFraction: Float, yFraction: Float): MapCityUi {
        val coordinate = map.projection.fromScreenLocation(PointF(view.width * xFraction, view.height * yFraction))
        return MapCityUi(id, "City $id", coordinate.latitude, coordinate.longitude, 100, "RU")
    }

    private fun awaitCentered(city: MapCityUi, bottom: Int) {
        compose.waitUntil(5_000) {
            var centered = false
            compose.runOnIdle {
                val point = map.projection.toScreenLocation(city.coordinate())
                centered = abs(point.x - view.width / 2f) < 2 && abs(point.y - (view.height - bottom) / 2f) < 2
            }
            centered
        }
    }
}

private fun MapCityUi.coordinate() = LatLng(latitude, longitude)

private const val EMPTY_STYLE = """{"version":8,"sources":{},"layers":[]}"""
