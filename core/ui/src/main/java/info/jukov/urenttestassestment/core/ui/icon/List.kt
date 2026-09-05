@file:Suppress("MaxLineLength")

package info.jukov.urenttestassestment.core.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val URentIcons.List: ImageVector
    get() = listIcon

private val listIcon by lazy {
    ImageVector.Builder("List", 24.dp, 24.dp, 24f, 24f)
        .addPath(
            pathData = PathParser().parsePathString(
                "M4 17C4.55228 17 5 17.4477 5 18C5 18.5523 4.55228 19 4 19C3.44772 19 3 18.5523 3 18C3 17.4477 3.44772 17 4 17ZM19 17C19.5523 17 20 17.4477 20 18C20 18.5523 19.5523 19 19 19H8C7.44772 19 7 18.5523 7 18C7 17.4477 7.44772 17 8 17H19ZM4 11C4.55228 11 5 11.4477 5 12C5 12.5523 4.55228 13 4 13C3.44772 13 3 12.5523 3 12C3 11.4477 3.44772 11 4 11ZM19 11C19.5523 11 20 11.4477 20 12C20 12.5523 19.5523 13 19 13H8C7.44772 13 7 12.5523 7 12C7 11.4477 7.44772 11 8 11H19ZM4 5C4.55228 5 5 5.44772 5 6C5 6.55228 4.55228 7 4 7C3.44772 7 3 6.55228 3 6C3 5.44772 3.44772 5 4 5ZM19 5C19.5523 5 20 5.44772 20 6C20 6.55228 19.5523 7 19 7H8C7.44772 7 7 6.55228 7 6C7 5.44772 7.44772 5 8 5H19Z"
            ).toNodes(),
            fill = SolidColor(Color(0xFF804AFF))
        ).build()
}
