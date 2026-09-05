package info.jukov.urenttestassestment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import info.jukov.urenttestassestment.core.ui.theme.URentTheme
import info.jukov.urenttestassestment.feature.cities.presentation.CitiesRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(SYSTEM_BAR_SCRIM, SYSTEM_BAR_SCRIM),
            navigationBarStyle = SystemBarStyle.light(SYSTEM_BAR_SCRIM, SYSTEM_BAR_SCRIM)
        )
        setContent {
            URentTheme {
                CitiesRoute()
            }
        }
    }
}

internal const val SYSTEM_BAR_SCRIM: Int = android.graphics.Color.TRANSPARENT
