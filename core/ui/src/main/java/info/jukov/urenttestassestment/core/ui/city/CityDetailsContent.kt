package info.jukov.urenttestassestment.core.ui.city

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.jukov.urenttestassestment.core.ui.R
import info.jukov.urenttestassestment.core.ui.component.URentButton
import info.jukov.urenttestassestment.core.ui.theme.URentSpacing
import info.jukov.urenttestassestment.core.ui.theme.URentTheme

@Composable
public fun CityDetailsContent(
    city: CityDetailsUi,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    fillHeight: Boolean = false
) {
    val scrollingModifier = if (fillHeight) modifier else modifier.verticalScroll(rememberScrollState())
    Column(scrollingModifier.padding(URentSpacing.space16)) {
        Column(
            if (fillHeight) {
                Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())
            } else {
                Modifier.fillMaxWidth()
            },
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            CityField(stringResource(R.string.city_label), city.name)
            CityField(stringResource(R.string.city_country_label), city.country)
            CityField(
                stringResource(R.string.city_population_label),
                stringResource(R.string.city_population_value, city.population)
            )
        }
        Spacer(Modifier.heightIn(min = 28.dp))
        URentButton(
            stringResource(R.string.city_search),
            onSearch,
            Modifier.fillMaxWidth().heightIn(min = 56.dp)
        )
    }
}

@Composable
private fun CityField(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(showBackground = true, locale = "ru")
@Composable
private fun CityDetailsPreview() {
    URentTheme { CityDetailsContent(CityDetailsUi("Москва", "Россия", "12 655 000"), {}) }
}
