package info.jukov.urenttestassestment.core.ui.component

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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.unit.dp
import info.jukov.urenttestassestment.core.ui.theme.AccentBrand
import info.jukov.urenttestassestment.core.ui.theme.BackgroundPrimary
import info.jukov.urenttestassestment.core.ui.theme.BackgroundTertiary
import info.jukov.urenttestassestment.core.ui.theme.URentShapes
import info.jukov.urenttestassestment.core.ui.theme.URentSpacing

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
    val textStyle = LocalTextStyle.current
    val containerModifier = modifier
        .defaultMinSize(minHeight = 56.dp)
        .background(
            color = if (isFocused) BackgroundPrimary else BackgroundTertiary,
            shape = URentShapes.radius16
        ).let {
            if (isFocused) it.border(1.5.dp, AccentBrand, URentShapes.radius16) else it
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
