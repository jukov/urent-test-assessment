package info.jukov.urenttestassessment.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import info.jukov.urenttestassessment.core.ui.theme.AccentBrand
import info.jukov.urenttestassessment.core.ui.theme.BackgroundPrimary
import info.jukov.urenttestassessment.core.ui.theme.BackgroundTertiary
import info.jukov.urenttestassessment.core.ui.theme.URentShapes
import info.jukov.urenttestassessment.core.ui.theme.URentSizes
import info.jukov.urenttestassessment.core.ui.theme.URentSpacing

@Composable
fun URentTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val visuals = textFieldVisuals(isFocused)
    val textStyle = LocalTextStyle.current
    val containerModifier = modifier
        .defaultMinSize(minHeight = URentSizes.inputMinHeight)
        .background(
            color = visuals.background,
            shape = URentShapes.radius16
        ).let {
            if (visuals.borderWidth > 0.dp) {
                it.border(visuals.borderWidth, visuals.borderColor, URentShapes.radius16)
            } else {
                it
            }
        }.padding(URentSpacing.space16)

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = containerModifier,
        enabled = enabled,
        singleLine = true,
        textStyle = textStyle,
        cursorBrush = SolidColor(textStyle.color.takeOrElse { AccentBrand }),
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(URentSpacing.space12)
            ) {
                leadingIcon?.invoke()
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) placeholder?.invoke()
                    innerTextField()
                }
                trailingIcon?.invoke()
            }
        }
    )
}

internal data class TextFieldVisuals(val background: Color, val borderColor: Color, val borderWidth: Dp)

internal fun textFieldVisuals(isFocused: Boolean): TextFieldVisuals = if (isFocused) {
    TextFieldVisuals(BackgroundPrimary, AccentBrand, URentSizes.selectedBorderWidth)
} else {
    TextFieldVisuals(BackgroundTertiary, Color.Transparent, 0.dp)
}
