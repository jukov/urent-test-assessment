package info.jukov.urenttestassestment.feature.map.presentation

import androidx.compose.runtime.Composable
import info.jukov.urenttestassestment.core.ui.city.rememberCitySearchAction
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState

@Composable
public fun MapRoute(viewModel: MapViewModel = koinViewModel()) {
    val search = rememberCitySearchAction()
    MapScreen(
        viewModel.collectAsState().value,
        viewModel.camera,
        viewModel::onCameraIdle,
        viewModel::retry,
        viewModel::selectCity,
        onSearchCity = search
    )
}
