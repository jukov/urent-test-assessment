package info.jukov.urenttestassestment.feature.cities.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex

@Composable
internal fun CitiesPullToRefresh(
    state: CitiesContentState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val pullToRefreshState = rememberPullToRefreshState()
    val maxDistance = pullToRefreshDistance(
        statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    )
    val isRefreshing = state is CitiesContentState.ContentRefreshing

    Box(
        modifier = modifier
            .fillMaxSize()
            .pullToRefresh(
                isRefreshing = isRefreshing,
                state = pullToRefreshState,
                enabled = state.isPullToRefreshEnabled(),
                threshold = maxDistance,
                onRefresh = onRefresh
            )
    ) {
        content()
        PullToRefreshDefaults.Indicator(
            state = pullToRefreshState,
            isRefreshing = isRefreshing,
            maxDistance = maxDistance,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(PULL_TO_REFRESH_Z_INDEX)
        )
    }
}

internal fun pullToRefreshDistance(statusBarHeight: Dp): Dp =
    PullToRefreshDefaults.PositionalThreshold + statusBarHeight

internal fun CitiesContentState.isPullToRefreshEnabled(): Boolean = when (this) {
    is CitiesContentState.Content,
    is CitiesContentState.ContentProgress,
    is CitiesContentState.ContentFailure,
    is CitiesContentState.ContentRefreshing,
    is CitiesContentState.ContentEndReached -> true

    CitiesContentState.EmptyProgress,
    is CitiesContentState.Empty,
    is CitiesContentState.EmptyFailure -> false
}

private const val PULL_TO_REFRESH_Z_INDEX = 2f
