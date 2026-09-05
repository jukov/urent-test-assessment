package info.jukov.urenttestassestment.feature.map.presentation

import android.graphics.PointF
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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

    @Test
    fun bottomPinAnimatesToUncoveredCenterWithoutChangingZoomOrPadding() {
        setUpMap()
        lateinit var city: MapCityUi
        var bottom = 0
        var initialY = 0f
        val positions = mutableListOf<Float>()
        compose.runOnIdle {
            city = cityAt(1, 0.8f, 0.9f)
            bottom = (view.height * 0.4f).toInt()
            initialY = map.projection.toScreenLocation(city.coordinate()).y
            map.addOnCameraMoveListener { positions += map.projection.toScreenLocation(city.coordinate()).y }
            camera.focus(city, bottom)
        }
        awaitCentered(city, bottom)
        compose.runOnIdle {
            val targetY = (view.height - bottom) / 2f
            assertTrue(positions.any { it > targetY + 5 && it < initialY - 5 })
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
                    mapView.onPause()
                    mapView.onStop()
                    mapView.onDestroy()
                }
            }
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    view = mapView
                    mapView.getMapAsync { nativeMap ->
                        map = nativeMap
                        camera = MapCityCamera(map)
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
