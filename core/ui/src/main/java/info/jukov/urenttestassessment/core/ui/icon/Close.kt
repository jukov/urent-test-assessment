package info.jukov.urenttestassessment.core.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val URentIcons.Close: ImageVector by lazy {
    ImageVector.Builder("Close", 24.dp, 24.dp, 48f, 48f).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(37.3788f, 10.6424f)
            curveTo(36.5506f, 9.81418f, 35.2127f, 9.81418f, 34.3845f, 10.6424f)
            lineTo(24f, 21.0057f)
            lineTo(13.6155f, 10.6212f)
            curveTo(12.7873f, 9.79295f, 11.4494f, 9.79295f, 10.6212f, 10.6212f)
            curveTo(9.79295f, 11.4494f, 9.79295f, 12.7873f, 10.6212f, 13.6155f)
            lineTo(21.0057f, 24f)
            lineTo(10.6212f, 34.3845f)
            curveTo(9.79295f, 35.2127f, 9.79295f, 36.5506f, 10.6212f, 37.3788f)
            curveTo(11.4494f, 38.2071f, 12.7873f, 38.2071f, 13.6155f, 37.3788f)
            lineTo(24f, 26.9943f)
            lineTo(34.3845f, 37.3788f)
            curveTo(35.2127f, 38.2071f, 36.5506f, 38.2071f, 37.3788f, 37.3788f)
            curveTo(38.2071f, 36.5506f, 38.2071f, 35.2127f, 37.3788f, 34.3845f)
            lineTo(26.9943f, 24f)
            lineTo(37.3788f, 13.6155f)
            curveTo(38.1858f, 12.8085f, 38.1858f, 11.4494f, 37.3788f, 10.6424f)
            close()
        }
    }.build()
}
