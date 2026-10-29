package vin.pam.closetly.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import vin.pam.closetly.core.ui.components.ClosetlyScaffold

data class HomeFeature(val title: String, val owner: String, val description: String, val onClick: () -> Unit)

@Composable
fun HomeScreen(
    userName: String,
    onProfile: () -> Unit,
    onWardrobe: () -> Unit,
    onOutfits: () -> Unit,
    onPlanner: () -> Unit,
    onInspiration: () -> Unit,
    onWishlist: () -> Unit,
    onWearHistory: () -> Unit,
) {
    val features = listOf(
        HomeFeature("Digital Wardrobe", "Vincent", "Catalog clothes and categories", onWardrobe),
        HomeFeature("Mix & Match", "Janice", "Build reusable outfit combinations", onOutfits),
        HomeFeature("Outfit Planner", "Vara", "Schedule looks for dates and activities", onPlanner),
        HomeFeature("Style Inspiration", "Sela", "Collect personal fashion references", onInspiration),
        HomeFeature("Fashion Wishlist", "Gres", "Track items you want to purchase", onWishlist),
        HomeFeature("Wear History", "Aurel", "Browse an archive of worn outfits", onWearHistory),
    )
    ClosetlyScaffold("Hello, $userName", actionLabel = "Profile", onAction = onProfile) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(160.dp),
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Column(Modifier.padding(bottom = 8.dp)) {
                    Text("Your closet, one place", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("UI milestone • local demo data", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            items(features) { feature ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = feature.onClick),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(feature.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(feature.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(feature.owner, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
