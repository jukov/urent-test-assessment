package info.jukov.urenttestassessment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import info.jukov.urenttestassessment.core.ui.theme.URentTheme
import info.jukov.urenttestassessment.startup.StartupRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(SYSTEM_BAR_SCRIM, SYSTEM_BAR_SCRIM),
            navigationBarStyle = SystemBarStyle.light(SYSTEM_BAR_SCRIM, SYSTEM_BAR_SCRIM)
        )
        setContent {
            URentTheme {
                StartupRoute { CityNavigation() }
            }
        }
    }
}

internal const val SYSTEM_BAR_SCRIM: Int = android.graphics.Color.TRANSPARENT
