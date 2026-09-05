package info.jukov.urenttestassestment.feature.cities.presentation

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import info.jukov.urenttestassestment.core.ui.icon.Pin
import info.jukov.urenttestassestment.core.ui.icon.URentIcons

@Composable
internal fun CitiesSkeleton() {
    val transition = rememberInfiniteTransition(label = "cities-shimmer")
    val color by transition.animateColor(
        initialValue = shimmerDarkColor(
            background = MaterialTheme.colorScheme.background,
            content = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        targetValue = MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "cities-shimmer-color"
    )
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        repeat(SKELETON_ITEMS) { index ->
            Row(
                Modifier.fillMaxWidth().height(76.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    URentIcons.Pin,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Box(
                    Modifier
                        .width(if (index % 2 == 0) 164.dp else 205.dp)
                        .height(22.dp)
                        .background(color, RoundedCornerShape(12.dp))
                )
            }
            if (index != SKELETON_ITEMS - 1) HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
        }
    }
}

internal fun shimmerDarkColor(background: Color, content: Color): Color =
    lerp(start = background, stop = content, fraction = SHIMMER_DARK_FRACTION)

private const val SKELETON_ITEMS = 5
private const val SHIMMER_DARK_FRACTION = 0.12f
