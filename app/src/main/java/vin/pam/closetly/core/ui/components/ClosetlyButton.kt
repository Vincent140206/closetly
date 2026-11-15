package vin.pam.closetly.core.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import vin.pam.closetly.ui.theme.CardWhite
import vin.pam.closetly.ui.theme.ClosetlyDimensions
import vin.pam.closetly.ui.theme.ClosetlyFullShape
import vin.pam.closetly.ui.theme.ClosetlySpacing
import vin.pam.closetly.ui.theme.GoldenPressed
import vin.pam.closetly.ui.theme.Heading

enum class ClosetlyButtonStyle {
    Primary,
    Secondary,
    Dark,
}

@Composable
fun ClosetlyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ClosetlyButtonStyle = ClosetlyButtonStyle.Primary,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: ImageVector? = null,
    shape: Shape = ClosetlyFullShape,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isInteractive = enabled && !isLoading
    val colorScheme = MaterialTheme.colorScheme
    val contentColor = when (style) {
        ClosetlyButtonStyle.Primary -> colorScheme.onPrimary
        ClosetlyButtonStyle.Secondary -> colorScheme.onSurface
        ClosetlyButtonStyle.Dark -> CardWhite
    }
    val content: @Composable () -> Unit = {
        Row(
            horizontalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = contentColor,
                    strokeWidth = 2.dp,
                )
            }
            // ini jujur rada bingung leading icon & trailing icon, bs pake keduanya kah?
            if (!isLoading && leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
            Text(text = text, style = MaterialTheme.typography.labelLarge)
            if (!isLoading) trailingIcon?.invoke()
        }
    }
    val sizedModifier = modifier.defaultMinSize(minHeight = ClosetlyDimensions.MinimumTouchTarget)

    if (style == ClosetlyButtonStyle.Secondary) {
        OutlinedButton(
            onClick = onClick,
            modifier = sizedModifier,
            enabled = isInteractive,
            shape = shape,
            interactionSource = interactionSource,
            border = ButtonDefaults.outlinedButtonBorder(enabled = isInteractive),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (isPressed) colorScheme.surfaceVariant else colorScheme.surface,
                contentColor = colorScheme.onSurface,
                disabledContainerColor = colorScheme.surfaceVariant,
                disabledContentColor = colorScheme.onSurfaceVariant,
            ),
            content = { content() },
        )
    } else {
        val containerColor = when (style) {
            ClosetlyButtonStyle.Primary -> if (isPressed) GoldenPressed else colorScheme.primary
            ClosetlyButtonStyle.Dark -> Heading
            ClosetlyButtonStyle.Secondary -> Color.Unspecified
        }
        Button(
            onClick = onClick,
            modifier = sizedModifier,
            enabled = isInteractive,
            shape = shape,
            interactionSource = interactionSource,
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor,
                disabledContainerColor = colorScheme.primaryContainer,
                disabledContentColor = colorScheme.onPrimaryContainer.copy(alpha = 0.62f),
            ),
            content = { content() },
        )
    }
}

@Composable
fun ClosetlyTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentColor: Color = Color.Unspecified,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = ClosetlyDimensions.MinimumTouchTarget),
        enabled = enabled,
        shape = ClosetlyFullShape,
        colors = ButtonDefaults.textButtonColors(contentColor = contentColor),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}
