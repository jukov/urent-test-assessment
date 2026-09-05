package info.jukov.urenttestassestment.feature.map.presentation

import androidx.compose.runtime.Composable
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState

@Composable
public fun MapRoute(viewModel: MapViewModel = koinViewModel()) {
    MapScreen(viewModel.collectAsState().value, viewModel.camera, viewModel::onCameraIdle, viewModel::retry)
}
