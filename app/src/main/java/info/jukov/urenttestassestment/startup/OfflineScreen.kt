package info.jukov.urenttestassestment.startup

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.jukov.urenttestassestment.R
import info.jukov.urenttestassestment.core.ui.component.URentButton
import info.jukov.urenttestassestment.core.ui.theme.URentTheme

@Composable
internal fun OfflineScreen(onRetry: () -> Unit, checking: Boolean = false) {
    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp)) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(
                    Modifier.verticalScroll(rememberScrollState()).padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    OfflineIcon()
                    Text(
                        stringResource(R.string.startup_offline_title),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 24.sp,
                            lineHeight = 26.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        stringResource(R.string.startup_offline_message),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp,
                            lineHeight = 22.sp,
                            letterSpacing = 0.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
            URentButton(
                text = stringResource(if (checking) R.string.startup_checking else R.string.startup_retry),
                enabled = !checking,
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
            )
        }
    }
}

@Composable
private fun OfflineIcon() {
    val background = MaterialTheme.colorScheme.error
    val foreground = MaterialTheme.colorScheme.onPrimary
    Canvas(Modifier.size(88.dp)) {
        drawCircle(background)
        val start = size.width * CROSS_START
        val end = size.width * CROSS_END
        drawLine(foreground, Offset(start, start), Offset(end, end), 4.dp.toPx(), StrokeCap.Round)
        drawLine(foreground, Offset(start, end), Offset(end, start), 4.dp.toPx(), StrokeCap.Round)
    }
}

@Preview(locale = "ru", widthDp = 360, heightDp = 720)
@Composable
private fun OfflineScreenPreview() {
    URentTheme { OfflineScreen(onRetry = {}) }
}

private const val CROSS_START = 0.37f
private const val CROSS_END = 0.63f
