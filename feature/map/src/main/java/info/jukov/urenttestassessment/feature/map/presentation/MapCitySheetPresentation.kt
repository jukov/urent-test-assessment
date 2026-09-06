package info.jukov.urenttestassessment.feature.map.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
internal data class MapCitySheetPresentation(val city: MapCityUi?, val state: SheetState)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun rememberMapCitySheet(selected: MapCityUi?, onDismiss: () -> Unit): MapCitySheetPresentation {
    val state = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.PartiallyExpanded }
    )
    var retainedCity by remember { mutableStateOf(selected) }
    val dismiss by rememberUpdatedState(onDismiss)
    // Keep the content measured until an external dismissal finishes animating.
    SideEffect { if (selected != null) retainedCity = selected }
    LaunchedEffect(selected?.id) {
        if (selected == null) {
            state.hide()
            retainedCity = null
        } else {
            snapshotFlow { state.hasExpandedState }.first { it }
            state.expand()
        }
    }
    LaunchedEffect(selected?.id) {
        if (selected != null) {
            // Ignore the initial Hidden value while the first layout prepares the anchors.
            snapshotFlow { state.targetValue }.first { it != SheetValue.Hidden }
            snapshotFlow {
                state.currentValue == SheetValue.Hidden &&
                    state.targetValue == SheetValue.Hidden && !state.isAnimationRunning
            }.first { it }
            dismiss()
        }
    }
    return MapCitySheetPresentation(selected ?: retainedCity, state)
}
