package info.jukov.urenttestassestment.feature.cities.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import info.jukov.urenttestassestment.feature.cities.R
import info.jukov.urenttestassestment.feature.cities.domain.CitiesError

@Composable
internal fun FailureState(error: CitiesError, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(errorText(error), textAlign = TextAlign.Center)
        Button(onClick = onRetry) { Text(stringResource(R.string.cities_retry)) }
    }
}

@Composable
internal fun errorText(error: CitiesError): String = when (error) {
    CitiesError.Network -> stringResource(R.string.cities_error_network)

    CitiesError.Server,
    CitiesError.InvalidResponse,
    CitiesError.Unknown -> stringResource(R.string.cities_error_generic)
}
