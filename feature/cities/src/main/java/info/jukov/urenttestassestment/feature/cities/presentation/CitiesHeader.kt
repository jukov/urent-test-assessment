package info.jukov.urenttestassestment.feature.cities.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import info.jukov.urenttestassestment.core.ui.component.URentTextField
import info.jukov.urenttestassestment.core.ui.icon.Search
import info.jukov.urenttestassestment.core.ui.icon.URentIcons
import info.jukov.urenttestassestment.core.ui.theme.URentShapes
import info.jukov.urenttestassestment.feature.cities.R

@Composable
internal fun CitiesHeader(query: String, elevation: Dp, onQueryChanged: (String) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().zIndex(1f),
        shape = URentShapes.bottomRadius16,
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
        shadowElevation = elevation
    ) {
        Column(
            modifier = Modifier.statusBarsPadding().padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))
            Text(stringResource(R.string.cities_title), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(28.dp))
            URentTextField(
                value = query,
                onValueChange = onQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        stringResource(R.string.cities_search_hint),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    Icon(URentIcons.Search, contentDescription = stringResource(R.string.cities_search_description))
                }
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

internal fun headerElevationFor(isScrolled: Boolean): Dp = if (isScrolled) 4.dp else 0.dp
