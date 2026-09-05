package info.jukov.urenttestassestment.feature.cities.presentation

import androidx.compose.runtime.Composable
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
public fun CitiesRoute(onOpenCity: (Long) -> Unit = {}, viewModel: CitiesViewModel = koinViewModel()) {
    val state = viewModel.collectAsState().value
    viewModel.collectSideEffect { effect ->
        when (effect) {
            is CitiesSideEffect.OpenCity -> onOpenCity(effect.cityId)
        }
    }
    CitiesScreen(
        state = state,
        onQueryChanged = viewModel::onQueryChanged,
        onLoadNextPage = viewModel::loadNextPage,
        onRefresh = viewModel::refresh,
        onRetry = viewModel::retry,
        onCityClick = viewModel::onCityClick
    )
}
