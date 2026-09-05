package info.jukov.urenttestassestment.feature.map.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import info.jukov.urenttestassestment.core.ui.city.CityDetails
import info.jukov.urenttestassestment.core.ui.city.CityDetailsContent
import info.jukov.urenttestassestment.core.ui.city.toUi
import info.jukov.urenttestassestment.core.ui.icon.Close
import info.jukov.urenttestassestment.core.ui.icon.URentIcons
import info.jukov.urenttestassestment.feature.map.R

@Composable
internal fun MapCitySheet(
    city: MapCityUi,
    onDismiss: () -> Unit,
    onSearch: () -> Unit,
    maxHeight: Dp,
    modifier: Modifier = Modifier
) {
    val locale = LocalConfiguration.current.locales[0]
    val details = remember(city, locale) {
        CityDetails(city.id, city.name, city.countryCode, city.population).toUi(locale)
    }
    Surface(
        modifier.fillMaxWidth().heightIn(max = maxHeight),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        shadowElevation = 4.dp
    ) {
        Column {
            Box(
                Modifier.fillMaxWidth().height(40.dp).pointerInput(onDismiss) {
                    var distance = 0f
                    detectVerticalDragGestures(
                        onDragStart = { distance = 0f },
                        onVerticalDrag = { change, amount ->
                            change.consume()
                            distance += amount
                        },
                        onDragEnd = { if (distance > 24.dp.toPx()) onDismiss() }
                    )
                }
            ) {
                Box(
                    Modifier.align(Alignment.Center).size(48.dp, 4.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(2.dp))
                )
                IconButton(onDismiss, Modifier.align(Alignment.CenterEnd)) {
                    Icon(URentIcons.Close, stringResource(R.string.map_close_city), Modifier.size(18.dp))
                }
            }
            CityDetailsContent(details, onSearch)
        }
    }
}
