package info.jukov.urenttestassessment.feature.map.presentation

import android.content.Context
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import info.jukov.urenttestassessment.feature.map.domain.MapViewport
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapView

@Composable
internal fun MapSurface(
    cities: List<MapCityUi>,
    camera: MapCamera,
    onIdle: (MapCamera, MapViewport?) -> Unit,
    onController: (MapController?) -> Unit,
    onStyleError: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    selectedCityId: Long? = null,
    selectedColor: Int = 0,
    onSelectCity: (Long?) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentSelection by rememberUpdatedState(onSelectCity)
    val currentIdle by rememberUpdatedState(onIdle)
    val currentError by rememberUpdatedState(onStyleError)
    val currentController by rememberUpdatedState(onController)
    var controller by remember { mutableStateOf<MapController?>(null) }
    val mapView = remember { createMapView(context) }
    DisposableEffect(mapView, lifecycle) {
        var disposed = false
        val layoutListener = View.OnLayoutChangeListener {
                _,
                left,
                top,
                right,
                bottom,
                oldLeft,
                oldTop,
                oldRight,
                oldBottom
            ->
            if (right - left != oldRight - oldLeft || bottom - top != oldBottom - oldTop) {
                controller?.publishViewport()
            }
        }
        mapView.addOnLayoutChangeListener(layoutListener)
        val failure = MapView.OnDidFailLoadingMapListener { currentError(true) }
        mapView.addOnDidFailLoadingMapListener(failure)
        val observer = mapView.lifecycleObserver()
        lifecycle.addObserver(observer)
        mapView.getMapAsync { map ->
            if (disposed) return@getMapAsync
            controller =
                MapController(map, camera, { c, v ->
                    currentIdle(c, v)
                }, { currentError(it) }, { currentSelection(it) }, selectedColor)
            currentController(controller)
        }
        onDispose {
            disposed = true
            mapView.removeOnLayoutChangeListener(layoutListener)
            lifecycle.removeObserver(observer)
            controller?.dispose()
            currentController(null)
            mapView.removeOnDidFailLoadingMapListener(failure)
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) mapView.onPause()
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) mapView.onStop()
            mapView.onDestroy()
        }
    }
    AndroidView(factory = { mapView }, modifier = modifier, update = {
        controller?.update(cities)
        controller?.select(selectedCityId)
    })
}

private fun createMapView(context: Context): MapView {
    MapLibre.getInstance(context)
    MarkerFontHttp.configure(context)
    return MapView(context).apply { onCreate(null) }
}

private fun MapView.lifecycleObserver() = LifecycleEventObserver { _, event ->
    when (event) {
        Lifecycle.Event.ON_START -> onStart()
        Lifecycle.Event.ON_RESUME -> onResume()
        Lifecycle.Event.ON_PAUSE -> onPause()
        Lifecycle.Event.ON_STOP -> onStop()
        else -> Unit
    }
}
