package vin.pam.closetly.core.ui.shell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import vin.pam.closetly.core.navigation.AppModule
import vin.pam.closetly.core.ui.components.ClosetlyCard
import vin.pam.closetly.core.ui.components.ClosetlyScaffold
import vin.pam.closetly.ui.theme.ClosetlyFullShape
import vin.pam.closetly.ui.theme.ClosetlySpacing

@Composable
fun ModulePlaceholderScreen(module: AppModule, onBack: () -> Unit) {
    ClosetlyScaffold(title = module.title, onBack = onBack) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(ClosetlySpacing.Lg).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            ClosetlyCard(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = ClosetlyFullShape,
                ) {
                    Text(
                        text = module.owner,
                        modifier = Modifier.padding(
                            horizontal = ClosetlySpacing.Md,
                            vertical = ClosetlySpacing.Xs,
                        ),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                Text(
                    text = "Not implemented yet",
                    modifier = Modifier.fillMaxWidth().padding(top = ClosetlySpacing.Lg),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "${module.title} is assigned to ${module.owner}.",
                    modifier = Modifier.fillMaxWidth().padding(top = ClosetlySpacing.Sm),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = module.branch,
                    modifier = Modifier.fillMaxWidth().padding(top = ClosetlySpacing.Md),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.tertiary,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
