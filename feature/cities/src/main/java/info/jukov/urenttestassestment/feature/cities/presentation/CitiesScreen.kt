package info.jukov.urenttestassestment.feature.cities.presentation

import androidx.compose.animation.core.RepeatMode
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.jukov.urenttestassestment.core.ui.component.URentTextField
import info.jukov.urenttestassestment.core.ui.icon.Pin
import info.jukov.urenttestassestment.core.ui.icon.Search
import info.jukov.urenttestassestment.core.ui.icon.URentIcons
import info.jukov.urenttestassestment.core.ui.theme.URentTheme
import info.jukov.urenttestassestment.feature.cities.R

@Composable
public fun CitiesScreen(
    state: CitiesState,
    onQueryChanged: (String) -> Unit,
    onLoadNextPage: () -> Unit,
    onRetry: () -> Unit,
    onCityClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))
        Text(stringResource(R.string.cities_title), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(28.dp))
        URentTextField(
            value = state.query,
            onValueChange = onQueryChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(stringResource(R.string.cities_search_hint), color = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            trailingIcon = {
                Icon(URentIcons.Search, contentDescription = stringResource(R.string.cities_search_description))
            }
        )
        Spacer(Modifier.height(16.dp))
        when (val content = state.content) {
            CitiesContentState.Initial, CitiesContentState.Loading -> CitiesSkeleton()
            is CitiesContentState.Content -> CitiesList(content, onLoadNextPage, onRetry, onCityClick)
            is CitiesContentState.Empty -> MessageState(stringResource(R.string.cities_empty), null)
            is CitiesContentState.Error -> MessageState(errorText(content.message), onRetry)
        }
    }
}

@Composable
private fun CitiesList(
    content: CitiesContentState.Content,
    onLoadNextPage: () -> Unit,
    onRetry: () -> Unit,
    onCityClick: (Long) -> Unit
) {
    val listState = rememberLazyListState()
    val shouldLoadMore by remember(content.cities.size) {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            content.cities.isNotEmpty() && lastVisible >= content.cities.lastIndex - PREFETCH_DISTANCE
        }
    }
    LaunchedEffect(shouldLoadMore, content.pagination) {
        if (shouldLoadMore && content.pagination is CitiesPaginationState.Idle) onLoadNextPage()
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(content.cities, key = CityUi::id) { city ->
            CityRow(city, onCityClick)
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
        }
        item {
            when (val pagination = content.pagination) {
                CitiesPaginationState.Loading -> Box(Modifier.fillMaxWidth().padding(20.dp), Alignment.Center) {
                    CircularProgressIndicator(Modifier.size(24.dp))
                }

                is CitiesPaginationState.Error -> MessageState(errorText(pagination.message), onRetry)

                CitiesPaginationState.EndReached, CitiesPaginationState.Idle -> Unit
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
            .padding(vertical = 20.dp, horizontal = 4.dp),
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
    Column(Modifier.fillMaxWidth()) {
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

@Preview(showBackground = true)
@Composable
private fun CitiesContentPreview() = URentTheme {
    CitiesScreen(
        CitiesState(
            content = CitiesContentState.Content(
                listOf(CityUi(1, "Москва, Россия"), CityUi(2, "Лондон, Великобритания")),
                CitiesPaginationState.Idle
            )
        ),
        {},
        {},
        {},
        {}
    )
}

@Preview(showBackground = true)
@Composable
private fun CitiesLoadingPreview() = URentTheme {
    CitiesScreen(CitiesState(content = CitiesContentState.Loading), {}, {}, {}, {})
}

private const val SKELETON_ITEMS = 5
private const val PREFETCH_DISTANCE = 4
