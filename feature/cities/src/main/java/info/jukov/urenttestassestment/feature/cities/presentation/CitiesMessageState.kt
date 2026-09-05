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

@Composable
internal fun MessageState(message: String, retry: (() -> Unit)?) {
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
internal fun errorText(code: String): String = when (code) {
    "network" -> stringResource(R.string.cities_error_network)
    else -> stringResource(R.string.cities_error_generic)
}
