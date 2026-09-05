package info.jukov.urenttestassestment.feature.map.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.jukov.urenttestassestment.feature.map.R
import info.jukov.urenttestassestment.feature.map.domain.MapViewport

@Composable
internal fun MapScreen(
    state: MapState,
    camera: MapCamera,
    onIdle: (MapCamera, MapViewport) -> Unit,
    onRetry: () -> Unit
) {
    var controller by remember { mutableStateOf<MapController?>(null) }
    var styleError by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize()) {
        MapSurface(
            state.cities,
            camera,
            onIdle,
            { controller = it },
            { styleError = it },
            Modifier.fillMaxSize()
        )
        MapZoomControls(
            onZoom = { controller?.zoom(it) },
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 12.dp)
        )
        MapStatus(
            state = if (styleError) MapLoadState.Error else state.loadState,
            onRetry = {
                if (styleError) controller?.loadStyle()
                onRetry()
            },
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(12.dp)
        )
    }
}

@Composable
internal fun MapZoomControls(onZoom: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(24.dp), shadowElevation = 4.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            ZoomButton(stringResource(R.string.map_zoom_in), "+") { onZoom(true) }
            HorizontalDivider(Modifier.size(width = 32.dp, height = 1.dp))
            ZoomButton(stringResource(R.string.map_zoom_out), "−") { onZoom(false) }
        }
    }
}

@Composable
private fun ZoomButton(description: String, symbol: String, onClick: () -> Unit) {
    IconButton(onClick, Modifier.size(52.dp, 48.dp).semantics { contentDescription = description }) {
        Text(symbol, fontSize = 26.sp)
    }
}

@Composable
internal fun MapStatus(state: MapLoadState, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    if (state == MapLoadState.Content) return
    Surface(modifier, shape = MaterialTheme.shapes.medium, shadowElevation = 2.dp) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (state) {
                MapLoadState.Loading -> {
                    CircularProgressIndicator(Modifier.size(24.dp))
                    Text(stringResource(R.string.map_loading))
                }

                MapLoadState.Empty -> Text(stringResource(R.string.map_empty))

                MapLoadState.Error -> {
                    Text(stringResource(R.string.map_error))
                    TextButton(onRetry) { Text(stringResource(R.string.map_retry)) }
                }

                MapLoadState.Content -> Unit
            }
        }
    }
}
