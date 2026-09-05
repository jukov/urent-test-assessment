@file:Suppress("MaxLineLength")

package info.jukov.urenttestassestment.core.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val URentIcons.Close: ImageVector
    get() = closeIcon

private val closeIcon by lazy {
    ImageVector.Builder("Close", 24.dp, 24.dp, 48f, 48f)
        .addPath(
            pathData = PathParser().parsePathString(
                "M37.3788 10.6424C36.5506 9.81418 35.2127 9.81418 34.3845 10.6424L24 21.0057L13.6155 10.6212C12.7873 9.79295 11.4494 9.79295 10.6212 10.6212C9.79295 11.4494 9.79295 12.7873 10.6212 13.6155L21.0057 24L10.6212 34.3845C9.79295 35.2127 9.79295 36.5506 10.6212 37.3788C11.4494 38.2071 12.7873 38.2071 13.6155 37.3788L24 26.9943L34.3845 37.3788C35.2127 38.2071 36.5506 38.2071 37.3788 37.3788C38.2071 36.5506 38.2071 35.2127 37.3788 34.3845L26.9943 24L37.3788 13.6155C38.1858 12.8085 38.1858 11.4494 37.3788 10.6424Z"
            ).toNodes(),
            fill = SolidColor(Color.White)
        ).build()
}
