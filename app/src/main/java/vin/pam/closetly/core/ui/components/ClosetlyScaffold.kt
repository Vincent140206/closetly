package vin.pam.closetly.core.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable

@Composable
fun ClosetlyScaffold(
    title: String,
    onBack: (() -> Unit)? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = {
            ClosetlyTopBar(
                title = title,
                onBack = onBack,
                actionLabel = actionLabel,
                onAction = onAction,
            )
        },
        content = content,
    )
}
