package vin.pam.closetly.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import vin.pam.closetly.ui.theme.ClosetlyDimensions

@Composable
fun ClosetlyCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    val elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)

    if (onClick == null) {
        Card(
            modifier = modifier,
            shape = MaterialTheme.shapes.large,
            colors = colors,
            elevation = elevation,
            border = border,
        ) {
            Column(
                modifier = Modifier.padding(ClosetlyDimensions.CardPadding),
                content = content,
            )
        }
    } else {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = MaterialTheme.shapes.large,
            colors = colors,
            elevation = elevation,
            border = border,
        ) {
            Column(
                modifier = Modifier.padding(ClosetlyDimensions.CardPadding),
                content = content,
            )
        }
    }
}
