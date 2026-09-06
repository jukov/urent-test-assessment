package info.jukov.urenttestassessment.core.ui.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import info.jukov.urenttestassessment.core.ui.theme.ButtonPrimaryBackground
import info.jukov.urenttestassessment.core.ui.theme.ButtonPrimaryBackgroundPressed
import info.jukov.urenttestassessment.core.ui.theme.URentShapes

@Composable
fun URentButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = URentShapes.radius16,
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonContainerColor(isPressed)
        ),
        interactionSource = interactionSource
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

internal fun buttonContainerColor(isPressed: Boolean): Color =
    if (isPressed) ButtonPrimaryBackgroundPressed else ButtonPrimaryBackground
