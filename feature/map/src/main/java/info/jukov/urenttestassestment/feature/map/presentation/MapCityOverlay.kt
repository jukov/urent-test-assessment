package info.jukov.urenttestassestment.feature.map.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

@Composable
internal fun MapCityOverlay(
    selected: MapCityUi?,
    onZoom: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onSearch: () -> Unit
) {
    var sheetSize by remember { mutableStateOf(IntSize.Zero) }
    val sheetHeight = with(LocalDensity.current) { sheetSize.height.toDp() }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().padding(bottom = if (selected != null) sheetHeight else 0.dp)) {
            MapZoomControls(
                onZoom,
                Modifier.align(Alignment.CenterEnd).padding(end = 12.dp)
            )
        }
        if (selected != null) {
            MapCitySheet(
                selected,
                onDismiss,
                onSearch,
                maxHeight = minOf(
                    maxHeight * SHEET_HEIGHT_FRACTION,
                    (maxHeight - MAP_ZOOM_CONTROLS_HEIGHT - 16.dp).coerceAtLeast(0.dp)
                ),
                modifier = Modifier.align(Alignment.BottomCenter).onSizeChanged { sheetSize = it }
            )
        }
    }
}

private const val SHEET_HEIGHT_FRACTION = 0.65f
