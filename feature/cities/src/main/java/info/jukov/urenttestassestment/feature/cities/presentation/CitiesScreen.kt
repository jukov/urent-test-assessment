package info.jukov.urenttestassestment.feature.cities.presentation

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.jukov.urenttestassestment.core.ui.icon.Pin
import info.jukov.urenttestassestment.core.ui.icon.URentIcons
import info.jukov.urenttestassestment.core.ui.theme.URentTheme
import info.jukov.urenttestassestment.feature.cities.R

@Composable
public fun CitiesScreen(
    state: CitiesState,
    onQueryChanged: (String) -> Unit,
    onLoadNextPage: () -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onCityClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val isListScrolled by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 }
    }
    val headerElevation by animateDpAsState(
        targetValue = headerElevationFor(isListScrolled),
        label = "cities-header-elevation"
    )
    LaunchedEffect(state.content) {
        if (state.content is CitiesContentState.EmptyProgress) listState.scrollToItem(0)
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CitiesHeader(
            query = state.query,
            elevation = headerElevation,
            onQueryChanged = onQueryChanged
        )
        when (val content = state.content) {
            CitiesContentState.EmptyProgress -> CitiesSkeleton()

            is CitiesContentState.Empty -> MessageState(stringResource(R.string.cities_empty), null)

            is CitiesContentState.EmptyFailure -> MessageState(errorText(content.message), onRetry)

            is CitiesContentState.Content,
            is CitiesContentState.ContentProgress,
            is CitiesContentState.ContentFailure,
            is CitiesContentState.ContentRefreshing,
            is CitiesContentState.ContentEndReached -> CitiesList(
                content,
                onLoadNextPage,
                onRefresh,
                onRetry,
                onCityClick,
                listState
            )
        }
    }
}

@Composable
private fun CitiesList(
    content: CitiesContentState,
    onLoadNextPage: () -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onCityClick: (Long) -> Unit,
    listState: LazyListState
) {
    val cities = content.cities()
    val shouldLoadMore by remember(cities.size) {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            cities.isNotEmpty() && lastVisible >= cities.lastIndex - PREFETCH_DISTANCE
        }
    }
    LaunchedEffect(shouldLoadMore, content) {
        if (shouldLoadMore && content is CitiesContentState.Content) onLoadNextPage()
    }
    if (content is CitiesContentState.ContentFailure) {
        CitiesLazyList(content, cities, listState, onRetry, onCityClick)
    } else {
        PullToRefreshBox(
            isRefreshing = content is CitiesContentState.ContentRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            CitiesLazyList(content, cities, listState, onRetry, onCityClick)
        }
    }
}

@Composable
private fun CitiesLazyList(
    content: CitiesContentState,
    cities: List<CityUi>,
    listState: LazyListState,
    onRetry: () -> Unit,
    onCityClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(cities, key = CityUi::id) { city ->
            CityRow(city, onCityClick)
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
        }
        item {
            when (content) {
                is CitiesContentState.ContentProgress -> Box(
                    Modifier.fillMaxWidth().padding(20.dp),
                    Alignment.Center
                ) {
                    CircularProgressIndicator(Modifier.size(24.dp))
                }

                is CitiesContentState.ContentFailure -> MessageState(errorText(content.message), onRetry)

                else -> Unit
            }
        }
    }
}

@Composable
private fun CityRow(city: CityUi, onClick: (Long) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(city.id) }
            .padding(vertical = 20.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(URentIcons.Pin, contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(24.dp))
        Text(city.title, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun CitiesSkeleton() {
    val transition = rememberInfiniteTransition(label = "cities-shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "cities-shimmer-alpha"
    )
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        repeat(SKELETON_ITEMS) { index ->
            Row(
                Modifier.fillMaxWidth().height(76.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    URentIcons.Pin,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Box(
                    Modifier
                        .width(if (index % 2 == 0) 164.dp else 205.dp)
                        .height(22.dp)
                        .alpha(alpha)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                )
            }
            if (index != SKELETON_ITEMS - 1) HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
        }
    }
}

@Composable
private fun MessageState(message: String, retry: (() -> Unit)?) {
    Column(
        Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(message, textAlign = TextAlign.Center)
        if (retry != null) Button(onClick = retry) { Text(stringResource(R.string.cities_retry)) }
    }
}

@Composable
private fun errorText(code: String): String = when (code) {
    "network" -> stringResource(R.string.cities_error_network)
    else -> stringResource(R.string.cities_error_generic)
}

private fun CitiesContentState.cities(): List<CityUi> = when (this) {
    is CitiesContentState.Content -> cities

    is CitiesContentState.ContentProgress -> cities

    is CitiesContentState.ContentFailure -> cities

    is CitiesContentState.ContentRefreshing -> cities

    is CitiesContentState.ContentEndReached -> cities

    CitiesContentState.EmptyProgress,
    is CitiesContentState.Empty,
    is CitiesContentState.EmptyFailure -> emptyList()
}

@Preview(showBackground = true)
@Composable
private fun CitiesContentPreview() = URentTheme {
    CitiesScreen(
        CitiesState(
            content = CitiesContentState.Content(
                listOf(CityUi(1, "Москва, Россия"), CityUi(2, "Лондон, Великобритания"))
            )
        ),
        {},
        {},
        {},
        {},
        {}
    )
}

@Preview(showBackground = true)
@Composable
private fun CitiesLoadingPreview() = URentTheme {
    CitiesScreen(CitiesState(), {}, {}, {}, {}, {})
}

private const val SKELETON_ITEMS = 5
private const val PREFETCH_DISTANCE = 4
