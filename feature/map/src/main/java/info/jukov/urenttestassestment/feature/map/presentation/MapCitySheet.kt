package info.jukov.urenttestassestment.feature.map.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import info.jukov.urenttestassestment.core.ui.city.CityDetails
import info.jukov.urenttestassestment.core.ui.city.CityDetailsContent
import info.jukov.urenttestassestment.core.ui.city.toUi

@Composable
internal fun MapCitySheet(city: MapCityUi, onSearch: () -> Unit, maxHeight: Dp) {
    val locale = LocalConfiguration.current.locales[0]
    val details = remember(city, locale) {
        CityDetails(city.id, city.name, city.countryCode, city.population).toUi(locale)
    }
    CityDetailsContent(details, onSearch, Modifier.heightIn(max = maxHeight).testTag("city_sheet_content"))
}

@Composable
internal fun MapCitySheetHandle() {
    Box(
        Modifier.fillMaxWidth().height(MAP_CITY_SHEET_HANDLE_HEIGHT).testTag("city_sheet_handle"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier.size(48.dp, 4.dp)
                .background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(2.dp))
        )
    }
}

internal val MAP_CITY_SHEET_HANDLE_HEIGHT = 40.dp
