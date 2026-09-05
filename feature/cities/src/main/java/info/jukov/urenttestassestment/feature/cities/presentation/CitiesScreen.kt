package info.jukov.urenttestassestment.feature.cities.presentation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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
    CitiesPullToRefresh(
        state = state.content,
        onRefresh = onRefresh,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CitiesHeader(
                query = state.query,
                elevation = headerElevation,
                onQueryChanged = onQueryChanged,
                onClick = { requestScrollToTop(listState) }
            )
            when (val content = state.content) {
                CitiesContentState.EmptyProgress -> CitiesSkeleton()

                is CitiesContentState.Empty -> MessageState(stringResource(R.string.cities_empty), null)

                is CitiesContentState.EmptyFailure -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    MessageState(errorText(content.message), onRetry)
                }

                is CitiesContentState.Content,
                is CitiesContentState.ContentProgress,
                is CitiesContentState.ContentFailure,
                is CitiesContentState.ContentRefreshing,
                is CitiesContentState.ContentEndReached -> CitiesList(
                    content,
                    onLoadNextPage,
                    onRetry,
                    onCityClick,
                    listState
                )
            }
        }
    }
}

@Composable
private fun CitiesList(
    content: CitiesContentState,
    onLoadNextPage: () -> Unit,
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
    CitiesLazyList(content, cities, listState, onRetry, onCityClick)
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
            PaginationFooter(content = content, onRetry = onRetry)
        }
    }
}

@Composable
private fun PaginationFooter(content: CitiesContentState, onRetry: () -> Unit) {
    val minHeight = paginationFooterMinHeight(content)
    if (minHeight > 0.dp) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight),
            contentAlignment = Alignment.Center
        ) {
            when (content) {
                is CitiesContentState.ContentProgress -> {
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
            .height(CITY_ROW_HEIGHT)
            .clickable { onClick(city.id) }
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(URentIcons.Pin, contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(24.dp))
        Text(
            text = city.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = CITY_TITLE_MAX_LINES,
            overflow = TextOverflow.Ellipsis
        )
    }
}

internal fun paginationFooterMinHeight(content: CitiesContentState): Dp = when (content) {
    is CitiesContentState.ContentProgress,
    is CitiesContentState.ContentFailure -> PAGINATION_FOOTER_MIN_HEIGHT

    else -> 0.dp
}

internal fun requestScrollToTop(listState: LazyListState) {
    listState.requestScrollToItem(index = 0)
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

private const val PREFETCH_DISTANCE = 4
internal const val CITY_TITLE_MAX_LINES = 2
internal val CITY_ROW_HEIGHT = 54.dp
private val PAGINATION_FOOTER_MIN_HEIGHT = 176.dp
