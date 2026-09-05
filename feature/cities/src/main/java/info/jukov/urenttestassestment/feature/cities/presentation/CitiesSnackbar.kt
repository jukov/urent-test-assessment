package info.jukov.urenttestassestment.feature.cities.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import info.jukov.urenttestassestment.core.ui.icon.Close
import info.jukov.urenttestassestment.core.ui.icon.URentIcons
import info.jukov.urenttestassestment.core.ui.theme.URentShapes
import info.jukov.urenttestassestment.feature.cities.R

@Composable
internal fun CitiesSnackbar(data: SnackbarData) {
    Snackbar(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth()
            .heightIn(min = CITIES_SNACKBAR_MIN_HEIGHT),
        dismissAction = {
            IconButton(onClick = data::dismiss) {
                Icon(
                    imageVector = URentIcons.Close,
                    contentDescription = stringResource(R.string.cities_dismiss),
                    modifier = Modifier.size(CITIES_SNACKBAR_DISMISS_ICON_SIZE)
                )
            }
        },
        shape = URentShapes.radius16,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        dismissActionContentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Text(data.visuals.message)
    }
}

internal val CITIES_SNACKBAR_MIN_HEIGHT = 72.dp
internal val CITIES_SNACKBAR_DISMISS_ICON_SIZE = 18.dp
