package info.jukov.urenttestassestment.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val URentColorScheme = lightColorScheme(
    primary = ButtonPrimaryBackground,
    onPrimary = BackgroundPrimary,
    primaryContainer = BackgroundBrand,
    onPrimaryContainer = ContentPrimary,
    error = AccentDestructive,
    background = BackgroundPrimary,
    onBackground = ContentPrimary,
    surface = BackgroundPrimary,
    onSurface = ContentPrimary,
    surfaceVariant = BackgroundTertiary,
    onSurfaceVariant = ContentSecondary,
    outline = AccentBrand
)

@Composable
fun URentTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = URentColorScheme,
        typography = URentTypography,
        shapes = MaterialTheme.shapes.copy(
            small = URentShapes.radius16,
            medium = URentShapes.radius16,
            large = URentShapes.radius16
        ),
        content = content
    )
}
