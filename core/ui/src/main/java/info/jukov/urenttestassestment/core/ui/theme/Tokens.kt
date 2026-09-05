package info.jukov.urenttestassestment.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

object URentSpacing {
    val space12 = 12.dp
    val space16 = 16.dp
}

object URentShapes {
    val radius16 = RoundedCornerShape(16.dp)
    val bottomRadius16 = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 0.dp,
        bottomEnd = 16.dp,
        bottomStart = 16.dp
    )
}

object URentSizes {
    val inputMinHeight = 56.dp
    val selectedBorderWidth = 1.5.dp
}
