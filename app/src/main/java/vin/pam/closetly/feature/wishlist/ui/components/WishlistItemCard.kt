package vin.pam.closetly.feature.wishlist.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import vin.pam.closetly.core.ui.components.ClosetlyButton
import vin.pam.closetly.core.ui.components.ClosetlyCard
import vin.pam.closetly.ui.theme.ClosetlyRadius
import vin.pam.closetly.ui.theme.ClosetlySpacing
import vin.pam.closetly.ui.theme.ClosetlyTheme

@Composable
fun WishlistItemCard(
    name: String,
    isPurchased: Boolean,
    onOpenDetail: () -> Unit,
    modifier: Modifier = Modifier,
    imageModel: String? = null,
    priceLabel: String? = null,
    store: String? = null,
    priorityLabel: String? = null,
    onMarkPurchased: (() -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    ClosetlyCard(
        modifier = modifier,
        onClick = onOpenDetail,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(ClosetlyRadius.Md))
                    .background(colors.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                if (imageModel.isNullOrBlank()) {
                    Text(
                        text = "Belum ada foto",
                        style = typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                } else {
                    AsyncImage(
                        model = imageModel,
                        contentDescription = "Foto $name",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
            }

            Text(text = name, style = typography.titleSmall)

            if (!priceLabel.isNullOrBlank()) {
                Text(text = priceLabel, style = typography.bodyMedium)
            }

            if (!store.isNullOrBlank()) {
                Text(
                    text = store,
                    style = typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }

            if (!priorityLabel.isNullOrBlank()) {
                Text(
                    text = "Prioritas $priorityLabel",
                    style = typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }

            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (isPurchased) colors.secondaryContainer else colors.surfaceVariant,
                contentColor = if (isPurchased) {
                    colors.onSecondaryContainer
                } else {
                    colors.onSurfaceVariant
                },
            ) {
                Text(
                    text = if (isPurchased) "Sudah Dibeli" else "Belum Dibeli",
                    style = typography.labelSmall,
                    modifier = Modifier.padding(
                        horizontal = ClosetlySpacing.Sm,
                        vertical = ClosetlySpacing.Xs,
                    ),
                )
            }

            if (!isPurchased && onMarkPurchased != null) {
                ClosetlyButton(
                    text = "Tandai sudah dibeli",
                    onClick = onMarkPurchased,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WishlistItemCardPreview() {
    var isPurchased by remember { mutableStateOf(false) }

    ClosetlyTheme {
        WishlistItemCard(
            name = "Jaket denim biru",
            isPurchased = isPurchased,
            onOpenDetail = { },
            modifier = Modifier.width(170.dp),
            priceLabel = "Rp499.000",
            store = "Erigo",
            priorityLabel = "Tinggi",
            onMarkPurchased = { isPurchased = true },
        )
    }
}