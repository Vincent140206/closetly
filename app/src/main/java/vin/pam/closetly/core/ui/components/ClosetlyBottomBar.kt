package vin.pam.closetly.core.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import vin.pam.closetly.ui.theme.ClosetlyFullShape
import vin.pam.closetly.ui.theme.ClosetlySpacing

@Immutable
data class ClosetlyBottomBarItem(
    val id: String,
    val label: String,
    @param:DrawableRes val iconRes: Int,
    /** Ikon saat aktif (mis. versi filled dari desain). Default: sama dengan [iconRes]. */
    @param:DrawableRes val selectedIconRes: Int = iconRes,
)

@Composable
fun ClosetlyBottomBar(
    items: List<ClosetlyBottomBarItem>,
    selectedItemId: String,
    onItemSelected: (ClosetlyBottomBarItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        tonalElevation = 0.dp,
        windowInsets = NavigationBarDefaults.windowInsets,
    ) {
//        items.forEach { item ->
//            val selected = item.id == selectedItemId
//            NavigationBarItem(
//                selected = selected,
//                onClick = { onItemSelected(item) },
//                icon = {
//                    Icon(
//                        painter = painterResource(item.iconRes),
//                        contentDescription = item.label,
//                    )
//                },
//                label = {
//                    Text(text = item.label, style = MaterialTheme.typography.labelSmall)
//                },
//                alwaysShowLabel = true,
//                colors = NavigationBarItemDefaults.colors(
//                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
//                    selectedTextColor = MaterialTheme.colorScheme.onBackground,
//                    indicatorColor = MaterialTheme.colorScheme.primary,
//                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
//                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
//                ),
//            )
//        }
        Box(
            modifier = modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = ClosetlySpacing.Lg, vertical = ClosetlySpacing.Sm),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = ClosetlyFullShape,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = ClosetlySpacing.Sm,
                        vertical = ClosetlySpacing.Sm,
                    ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    items.forEach { item ->
                        val selected = item.id == selectedItemId
                        val contentColor = if (selected) {
                            MaterialTheme.colorScheme.onBackground
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 56.dp)
                                .clip(ClosetlyFullShape)
                                .selectable(
                                    selected = selected,
                                    role = Role.Tab,
                                    onClick = { onItemSelected(item) },
                                )
                                .padding(
                                    horizontal = ClosetlySpacing.Xs,
                                    vertical = ClosetlySpacing.Xs
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                painter = painterResource(
                                    if (selected) item.selectedIconRes else item.iconRes,
                                ),
                                contentDescription = null,
                                tint = contentColor,
                                modifier = Modifier.size(24.dp),
                            )
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                ),
                                color = contentColor,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                            )
                        }
                    }
                }
            }
        }
    }
}
