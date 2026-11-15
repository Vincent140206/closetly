package vin.pam.closetly.feature.outfit.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vin.pam.closetly.R
import vin.pam.closetly.core.ui.components.ClosetlyButton
import vin.pam.closetly.core.ui.components.ClosetlyButtonStyle
import vin.pam.closetly.core.ui.components.ErrorState
import vin.pam.closetly.core.ui.components.LoadingState
import vin.pam.closetly.feature.outfit.data.repository.FakeOutfitRepository
import vin.pam.closetly.feature.outfit.model.Outfit
import vin.pam.closetly.feature.outfit.ui.components.OutfitCollage
import vin.pam.closetly.ui.theme.ClosetlyDimensions
import vin.pam.closetly.ui.theme.ClosetlyFullShape
import vin.pam.closetly.ui.theme.ClosetlySpacing
import vin.pam.closetly.ui.theme.ClosetlyTheme

/**
 * Root screen Mix & Match (Outfit Collection).
 * Bottom bar dimiliki AppNavHost, jadi tidak dibuat di sini.
 */
@Composable
fun OutfitScreen(
    uiState: OutfitListUiState,
    onCreateOutfit: () -> Unit,
    onOutfitClick: (outfitId: String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val error = uiState.errorMessage
    when {
        uiState.isLoading -> LoadingState(modifier)
        error != null && uiState.outfits.isEmpty() -> ErrorState(error, modifier, onRetry)
        else -> OutfitCollection(uiState.outfits, onCreateOutfit, onOutfitClick, modifier)
    }
}

@Composable
private fun OutfitCollection(
    outfits: List<Outfit>,
    onCreateOutfit: () -> Unit,
    onOutfitClick: (String) -> Unit,
    modifier: Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(ClosetlyDimensions.ScreenHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(ClosetlySpacing.Md),
        verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Md),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                Text(
                    text = "Mix & Match",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Racik gaya favoritmu dari koleksi pakaianmu.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(ClosetlySpacing.Lg))
                ClosetlyButton(
                    text = "+ Buat Outfit",
                    onClick = onCreateOutfit,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        if (outfits.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    OutfitEmptyContent(onCreateOutfit)
                }
            }
        } else {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = ClosetlySpacing.Sm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Outfit-ku",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Surface(
                        shape = ClosetlyFullShape,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    ) {
                        Text(
                            text = "${outfits.size} Outfit",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(
                                horizontal = ClosetlySpacing.Md,
                                vertical = ClosetlySpacing.Xs,
                            ),
                        )
                    }
                }
            }
            items(outfits, key = { it.id }) { outfit ->
                OutfitCard(outfit = outfit, onClick = { onOutfitClick(outfit.id) })
            }
        }
    }
}

@Composable
private fun OutfitCard(outfit: Outfit, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(ClosetlySpacing.Sm)) {
            OutfitCollage(outfit.items, Modifier.fillMaxWidth().height(128.dp))
            Spacer(Modifier.height(ClosetlySpacing.Sm))
            Text(
                text = outfit.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (outfit.description.isNotBlank()) {
                Text(
                    text = "\"${outfit.description}\"",
                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(ClosetlySpacing.Xs))
            Text(
                text = "${outfit.items.size} ITEM",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun OutfitEmptyContent(onCreateOutfit: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = ClosetlySpacing.Xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_closetly_outfit),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(72.dp),
            )
        }
        Spacer(Modifier.height(ClosetlySpacing.Xl))
        Text(
            text = "Belum ada outfit tersimpan.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(ClosetlySpacing.Sm))
        Text(
            text = "Yuk, mulai kombinasikan pakaian dari lemari digitalmu!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(ClosetlySpacing.Xl))
        ClosetlyButton(
            text = "Buat Outfit Pertama  →",
            onClick = onCreateOutfit,
            style = ClosetlyButtonStyle.Dark,
        )
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun OutfitScreenPreview() = ClosetlyTheme {
    OutfitScreen(
        uiState = OutfitListUiState(isLoading = false, outfits = FakeOutfitRepository.SampleOutfits),
        onCreateOutfit = {}, onOutfitClick = {}, onRetry = {},
    )
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun OutfitScreenEmptyPreview() = ClosetlyTheme {
    OutfitScreen(
        uiState = OutfitListUiState(isLoading = false),
        onCreateOutfit = {}, onOutfitClick = {}, onRetry = {},
    )
}
