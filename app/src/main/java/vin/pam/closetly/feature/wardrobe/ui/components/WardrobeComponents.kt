package vin.pam.closetly.feature.wardrobe.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import vin.pam.closetly.R
import vin.pam.closetly.BuildConfig

@Composable
fun WardrobePhoto(path: String?, description: String?, modifier: Modifier = Modifier) {
    // Store stable resource names locally, but give Coil 3 a drawable ID for bundled demo images.
    val resourcePrefix = "android.resource://${BuildConfig.APPLICATION_ID}/drawable/"
    val imageModel: Any? = when (path) {
        "${resourcePrefix}wardrobe_demo_denim" -> R.drawable.wardrobe_demo_denim
        "${resourcePrefix}wardrobe_demo_sneakers" -> R.drawable.wardrobe_demo_sneakers
        "${resourcePrefix}wardrobe_demo_sweater" -> R.drawable.wardrobe_demo_sweater
        "${resourcePrefix}wardrobe_demo_skirt" -> R.drawable.wardrobe_demo_skirt
        else -> path
    }
    Box(modifier.background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
        if (path == null) {
            Icon(Icons.Outlined.Checkroom, description, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            SubcomposeAsyncImage(
                model = imageModel,
                contentDescription = description,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                error = {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(Icons.Outlined.Checkroom, null, Modifier.size(36.dp))
                        Text("Foto tidak tersedia", style = MaterialTheme.typography.bodySmall)
                    }
                },
            )
        }
    }
}

@Composable
fun WardrobeBrandHeader(onProfile: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Image(
            painterResource(R.drawable.ic_closetly_splash), null,
            modifier = Modifier.size(28.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
        )
        Text("Closetly", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.weight(1f))
        IconButton(
            onClick = onProfile,
            modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        ) {
            Icon(Icons.Outlined.PersonOutline, "Buka profil", tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}
