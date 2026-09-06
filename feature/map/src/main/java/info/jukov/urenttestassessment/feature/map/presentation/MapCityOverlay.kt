package info.jukov.urenttestassessment.feature.map.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MapCityOverlay(
    selected: MapCityUi?,
    onZoom: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onSearch: () -> Unit,
    onSheetMeasured: (Long, Int) -> Unit = { _, _ -> }
) {
    val presentation = rememberMapCitySheet(selected, onDismiss)
    val sheetState = presentation.state
    val scope = rememberCoroutineScope()
    val handleHeightPx = with(LocalDensity.current) { MAP_CITY_SHEET_HANDLE_HEIGHT.roundToPx() }
    BoxWithConstraints(Modifier.fillMaxSize().clipToBounds()) {
        val height = maxHeight
        val sheetHeight = minOf(
            height * SHEET_HEIGHT_FRACTION,
            (height - MAP_ZOOM_CONTROLS_HEIGHT - 16.dp).coerceAtLeast(0.dp)
        )
        BottomSheetScaffold(
            scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState),
            containerColor = Color.Transparent,
            sheetPeekHeight = 0.dp,
            sheetContainerColor = MaterialTheme.colorScheme.surface,
            sheetMaxWidth = Dp.Unspecified,
            sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            sheetShadowElevation = 4.dp,
            sheetDragHandle = null,
            sheetContent = {
                presentation.city?.let { city ->
                    MapCitySheetHandle { scope.launch { sheetState.hide() } }
                    key(city.id) {
                        MapCitySheet(
                            city,
                            onSearch,
                            (sheetHeight - MAP_CITY_SHEET_HANDLE_HEIGHT).coerceAtLeast(0.dp),
                            Modifier.onSizeChanged { onSheetMeasured(city.id, it.height + handleHeightPx) }
                        )
                    }
                }
            }
        ) {
            Box(Modifier.fillMaxSize()) {
                MapZoomControls(
                    onZoom,
                    Modifier.align(Alignment.TopEnd).padding(end = 12.dp).offset {
                        // Read during placement: the sheet's anchors are already measured, and zoom
                        // follows the same offset during dragging and settling without a second animation.
                        val mapHeight = if (sheetState.hasExpandedState) sheetState.requireOffset() else height.toPx()
                        IntOffset(
                            0,
                            ((mapHeight.coerceIn(0f, height.toPx()) - MAP_ZOOM_CONTROLS_HEIGHT.toPx()) / 2).roundToInt()
                        )
                    }
                )
            }
        }
    }
}

private const val SHEET_HEIGHT_FRACTION = 0.65f
