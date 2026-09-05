package info.jukov.urenttestassestment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import info.jukov.urenttestassestment.core.ui.city.CityDetails
import info.jukov.urenttestassestment.core.ui.icon.List
import info.jukov.urenttestassestment.core.ui.icon.Map
import info.jukov.urenttestassestment.core.ui.icon.URentIcons
import info.jukov.urenttestassestment.feature.cities.presentation.CitiesRoute
import info.jukov.urenttestassestment.feature.cities.presentation.CityDetailsRoute
import info.jukov.urenttestassestment.feature.map.presentation.MapRoute
import kotlinx.serialization.Serializable

@Serializable
internal data object CityListDestination

@Serializable
internal data object CityMapDestination

@Serializable
internal data class CityDetailsDestination(
    val id: Long,
    val name: String,
    val countryCode: String,
    val population: Long
)

@Composable
internal fun CityNavigation(
    listContent: @Composable ((CityDetails) -> Unit) -> Unit = { CitiesRoute(onOpenCity = it) },
    mapContent: @Composable () -> Unit = { MapRoute() }
) {
    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    val selectedTab = CityTab.entries.firstOrNull { tab ->
        entry?.destination?.hasRoute(tab.destination::class) == true
    } ?: CityTab.List
    val showingDetails = entry?.destination?.hasRoute<CityDetailsDestination>() == true
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (!showingDetails) {
                CityBottomBar(selectedTab) { tab ->
                    if (tab != selectedTab) {
                        navController.navigate(tab.destination) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController,
            startDestination = CityListDestination,
            modifier = Modifier.padding(padding).consumeWindowInsets(padding)
        ) {
            composable<CityListDestination> {
                listContent { city ->
                    navController.navigate(
                        CityDetailsDestination(city.id, city.name, city.countryCode, city.population)
                    )
                }
            }
            composable<CityDetailsDestination> { detailsEntry ->
                val city = detailsEntry.toRoute<CityDetailsDestination>()
                CityDetailsRoute(CityDetails(city.id, city.name, city.countryCode, city.population)) {
                    navController.popBackStack()
                }
            }
            composable<CityMapDestination> { mapContent() }
        }
    }
}

@Composable
internal fun CityBottomBar(selectedTab: CityTab, onSelect: (CityTab) -> Unit) {
    Surface(shadowElevation = 4.dp) {
        Row(Modifier.fillMaxWidth().navigationBarsPadding().height(64.dp).selectableGroup()) {
            CityTab.entries.forEach { tab ->
                val selected = tab == selectedTab
                Box(
                    Modifier.weight(1f).height(64.dp)
                        .selectable(selected, role = Role.Tab, onClick = { onSelect(tab) }),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        Modifier.size(64.dp, 32.dp).background(
                            if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            RoundedCornerShape(percent = 50)
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            when (tab) {
                                CityTab.List -> URentIcons.List
                                CityTab.Map -> URentIcons.Map
                            },
                            stringResource(tab.labelRes),
                            Modifier.size(24.dp),
                            tint = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }
    }
}
