package vin.pam.closetly.core.ui.shell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import vin.pam.closetly.core.navigation.AppModule
import vin.pam.closetly.core.ui.components.ClosetlyButton
import vin.pam.closetly.core.ui.components.ClosetlyCard
import vin.pam.closetly.core.ui.components.ClosetlyScaffold
import vin.pam.closetly.ui.theme.ClosetlyFullShape
import vin.pam.closetly.ui.theme.ClosetlySpacing

@Composable
fun HomeScreen(
    authStatus: String,
    authActionLabel: String,
    isAuthActionEnabled: Boolean,
    onModuleClick: (AppModule) -> Unit,
    onProfileClick: () -> Unit,
    onAuthClick: () -> Unit,
) {
    ClosetlyScaffold(
        title = "Closetly",
        actionLabel = "Profile",
        onAction = onProfileClick,
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(160.dp),
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(
                start = ClosetlySpacing.Lg,
                top = ClosetlySpacing.Sm,
                end = ClosetlySpacing.Lg,
                bottom = ClosetlySpacing.Xxl,
            ),
            horizontalArrangement = Arrangement.spacedBy(ClosetlySpacing.Md),
            verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Md),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                HeroCard()
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                SessionCard(
                    authStatus = authStatus,
                    authActionLabel = authActionLabel,
                    isAuthActionEnabled = isAuthActionEnabled,
                    onAuthClick = onAuthClick,
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier.padding(top = ClosetlySpacing.Md),
                    verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Xs),
                ) {
                    Text(
                        text = "Explore Closetly",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "Every module stays accessible while each feature is developed.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(AppModule.entries, key = { it.name }) { module ->
                ModuleCard(module = module, onClick = { onModuleClick(module) })
            }
        }
    }
}

@Composable
private fun HeroCard() {
    ClosetlyCard(modifier = Modifier.fillMaxWidth()) {
        Surface(
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = ClosetlyFullShape,
        ) {
            Text(
                text = "YOUR CLOSET, ORGANIZED",
                modifier = Modifier.padding(
                    horizontal = ClosetlySpacing.Md,
                    vertical = ClosetlySpacing.Xs,
                ),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = "Your wardrobe,\nyour story.",
            modifier = Modifier.padding(top = ClosetlySpacing.Lg),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Build outfits, plan what to wear, and share inspiration from one thoughtful space.",
            modifier = Modifier.padding(top = ClosetlySpacing.Sm),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SessionCard(
    authStatus: String,
    authActionLabel: String,
    isAuthActionEnabled: Boolean,
    onAuthClick: () -> Unit,
) {
    ClosetlyCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Account",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = authStatus,
            modifier = Modifier.padding(top = ClosetlySpacing.Xs),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ClosetlyButton(
            text = authActionLabel,
            onClick = onAuthClick,
            modifier = Modifier.fillMaxWidth().padding(top = ClosetlySpacing.Md),
            enabled = isAuthActionEnabled,
        )
    }
}

@Composable
private fun ModuleCard(module: AppModule, onClick: () -> Unit) {
    ClosetlyCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = ClosetlyFullShape,
        ) {
            Text(
                text = module.owner,
                modifier = Modifier.padding(
                    horizontal = ClosetlySpacing.Sm,
                    vertical = ClosetlySpacing.Xs,
                ),
                style = MaterialTheme.typography.labelSmall,
            )
        }
        Text(
            text = module.title,
            modifier = Modifier.padding(top = ClosetlySpacing.Md),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = module.description,
            modifier = Modifier.padding(top = ClosetlySpacing.Xs),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
