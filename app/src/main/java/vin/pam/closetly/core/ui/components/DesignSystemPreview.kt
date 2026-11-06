package vin.pam.closetly.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vin.pam.closetly.R
import vin.pam.closetly.ui.theme.ClosetlySpacing
import vin.pam.closetly.ui.theme.ClosetlyTheme

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun ClosetlyDesignSystemPreview() {
    ClosetlyTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Lg),
            ) {
                item {
                    Column(
                        modifier = Modifier.padding(ClosetlySpacing.Lg),
                        verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Md),
                    ) {
                        Text("Closetly Design System", style = MaterialTheme.typography.headlineMedium)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = MaterialTheme.shapes.large,
                                ),
                        )
                        Text("Display", style = MaterialTheme.typography.displaySmall)
                        Text("Heading", style = MaterialTheme.typography.headlineMedium)
                        Text("Section", style = MaterialTheme.typography.titleLarge)
                        Text("Subheading", style = MaterialTheme.typography.titleMedium)
                        Text("Body copy for feature content.", style = MaterialTheme.typography.bodyMedium)
                        Text("Caption and supporting metadata", style = MaterialTheme.typography.bodySmall)
                        ClosetlyButton(
                            text = "Primary button",
                            onClick = {},
                            modifier = Modifier.fillMaxWidth(),
                        )
                        ClosetlyButton(
                            text = "Secondary button",
                            onClick = {},
                            modifier = Modifier.fillMaxWidth(),
                            style = ClosetlyButtonStyle.Secondary,
                        )
                        ClosetlyButton(
                            text = "Dark button",
                            onClick = {},
                            modifier = Modifier.fillMaxWidth(),
                            style = ClosetlyButtonStyle.Dark,
                        )
                        ClosetlyButton(
                            text = "Disabled button",
                            onClick = {},
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                        )
                        ClosetlyCard(modifier = Modifier.fillMaxWidth()) {
                            Text("White content card", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "Cards stay quiet so clothing and editorial content can lead.",
                                modifier = Modifier.padding(top = ClosetlySpacing.Xs),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                item {
                    ClosetlyBottomBar(
                        items = previewBottomBarItems,
                        selectedItemId = "home",
                        onItemSelected = {},
                    )
                }
            }
        }
    }
}

private val previewBottomBarItems = listOf(
    ClosetlyBottomBarItem("home", "Beranda", R.drawable.ic_closetly_home),
    ClosetlyBottomBarItem("wardrobe", "Lemari", R.drawable.ic_closetly_wardrobe),
    ClosetlyBottomBarItem("outfit", "Outfit", R.drawable.ic_closetly_outfit),
    ClosetlyBottomBarItem("planner", "Planner", R.drawable.ic_closetly_planner),
    ClosetlyBottomBarItem("community", "Komunitas", R.drawable.ic_closetly_community),
)
