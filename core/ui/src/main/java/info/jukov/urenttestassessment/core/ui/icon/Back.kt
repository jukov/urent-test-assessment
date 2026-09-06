package info.jukov.urenttestassessment.core.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

public val URentIcons.Back: ImageVector by lazy {
    ImageVector.Builder("Back", 24.dp, 24.dp, 24f, 24f, autoMirror = true).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(20f, 12f)
            lineTo(4f, 12f)
            moveTo(11f, 5f)
            lineTo(4f, 12f)
            lineTo(11f, 19f)
        }
    }.build()
}
