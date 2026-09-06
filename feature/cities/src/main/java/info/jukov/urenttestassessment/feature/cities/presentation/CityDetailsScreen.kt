package info.jukov.urenttestassessment.feature.cities.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import info.jukov.urenttestassessment.core.ui.R as UiR
import info.jukov.urenttestassessment.core.ui.city.CityDetails
import info.jukov.urenttestassessment.core.ui.city.CityDetailsContent
import info.jukov.urenttestassessment.core.ui.city.CityDetailsUi
import info.jukov.urenttestassessment.core.ui.city.rememberCitySearchAction
import info.jukov.urenttestassessment.core.ui.city.toUi
import info.jukov.urenttestassessment.core.ui.icon.Back
import info.jukov.urenttestassessment.core.ui.icon.URentIcons

@Composable
public fun CityDetailsRoute(city: CityDetails, onBack: () -> Unit) {
    val locale = LocalConfiguration.current.locales[0]
    val content = remember(city, locale) { city.toUi(locale) }
    val search = rememberCitySearchAction()
    CityDetailsScreen(content, { search(city.name) }, onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CityDetailsScreen(city: CityDetailsUi, onSearch: () -> Unit, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(UiR.string.city_details_title), style = MaterialTheme.typography.headlineMedium)
                },
                navigationIcon = {
                    IconButton(onBack) { Icon(URentIcons.Back, stringResource(UiR.string.city_back)) }
                }
            )
        }
    ) { padding ->
        CityDetailsContent(city, onSearch, Modifier.fillMaxSize().padding(padding), fillHeight = true)
    }
}
