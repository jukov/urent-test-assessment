package info.jukov.urenttestassestment.feature.cities.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import info.jukov.urenttestassestment.feature.cities.R
import info.jukov.urenttestassestment.feature.cities.domain.CitiesError
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
public fun CitiesRoute(onOpenCity: (Long) -> Unit = {}, viewModel: CitiesViewModel = koinViewModel()) {
    val state = viewModel.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }
    val networkError = stringResource(R.string.cities_error_network)
    val genericError = stringResource(R.string.cities_error_generic)
    viewModel.collectSideEffect { effect ->
        when (effect) {
            is CitiesSideEffect.OpenCity -> onOpenCity(effect.cityId)

            is CitiesSideEffect.ShowRefreshFailure -> snackbarHostState.showSnackbar(
                if (effect.error == CitiesError.Network) networkError else genericError
            )
        }
    }
    Box(Modifier.fillMaxSize()) {
        CitiesScreen(
            state = state,
            onQueryChanged = viewModel::onQueryChanged,
            onLoadNextPage = viewModel::loadNextPage,
            onRefresh = viewModel::refresh,
            onRetry = viewModel::retry,
            onCityClick = viewModel::onCityClick
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding(),
            snackbar = { data -> CitiesSnackbar(data) }
        )
    }
}
