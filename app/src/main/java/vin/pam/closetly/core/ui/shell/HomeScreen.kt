package vin.pam.closetly.core.ui.shell

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import vin.pam.closetly.R
import vin.pam.closetly.core.navigation.AppModule
import vin.pam.closetly.core.ui.components.ClosetlyButton
import vin.pam.closetly.ui.theme.ClosetlySpacing
import vin.pam.closetly.ui.theme.ClosetlyTheme

@Composable
fun HomeScreen(
    displayName: String,
    onModuleClick: (AppModule) -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
    bottomContentPadding: Dp = 0.dp,
) {
    var today by remember { mutableStateOf(LocalDate.now()) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { today = LocalDate.now() }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
        contentPadding = PaddingValues(
            top = ClosetlySpacing.Xl,
            bottom = bottomContentPadding + ClosetlySpacing.Xl,
        ),
        verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Lg),
    ) {
        item {
            HomeHeader(onProfileClick, Modifier.padding(horizontal = ClosetlySpacing.Xl))
        }
        item {
            Column(
                modifier = Modifier.padding(horizontal = ClosetlySpacing.Xl),
                verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Xs),
            ) {
                Text(
                    text = stringResource(R.string.home_greeting, displayName),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.home_greeting_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            OutfitHero(
                onClick = { onModuleClick(AppModule.Outfit) },
                modifier = Modifier.padding(horizontal = ClosetlySpacing.Xl),
            )
        }
        item {
            OutfitCalendar(
                today = today,
                onOpenPlanner = { onModuleClick(AppModule.Planner) },
                modifier = Modifier.padding(horizontal = ClosetlySpacing.Xl),
            )
        }
        item {
            CommunitySection(onOpenCommunity = { onModuleClick(AppModule.Inspiration) })
        }
        item {
            WardrobeSection(
                onOpenWardrobe = { onModuleClick(AppModule.Wardrobe) },
                modifier = Modifier.padding(horizontal = ClosetlySpacing.Xl),
            )
        }
        item {
            Row(
                modifier = Modifier.padding(horizontal = ClosetlySpacing.Xl),
                horizontalArrangement = Arrangement.spacedBy(ClosetlySpacing.Md),
            ) {
                ShortcutCard(
                    title = stringResource(R.string.home_wishlist),
                    icon = Icons.Outlined.FavoriteBorder,
                    onClick = { onModuleClick(AppModule.Wishlist) },
                    modifier = Modifier.weight(1f),
                )
                ShortcutCard(
                    title = stringResource(R.string.home_wear_history),
                    icon = Icons.Outlined.History,
                    onClick = { onModuleClick(AppModule.WearHistory) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun HomeHeader(onProfileClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(R.drawable.ic_closetly_splash),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
            modifier = Modifier.size(28.dp),
        )
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge.copy(letterSpacing = (-1).sp),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.weight(1f))
        IconButton(
            onClick = onProfileClick,
            modifier = Modifier.size(48.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        ) {
            Icon(
                imageVector = Icons.Outlined.PersonOutline,
                contentDescription = stringResource(R.string.home_open_profile),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(30.dp),
            )
        }
    }
}

@Composable
private fun OutfitHero(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp))
            .border(1.dp, colors.outlineVariant, RoundedCornerShape(26.dp))
            .background(Brush.verticalGradient(listOf(colors.secondaryContainer, colors.primaryContainer))),
    ) {
        val stacked = maxWidth < 300.dp || LocalDensity.current.fontScale > 1.2f
        if (stacked) {
            Column {
                Image(
                    painter = painterResource(R.drawable.home_outfit_hero),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.TopCenter,
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                )
                HeroCopy(onClick, Modifier.padding(ClosetlySpacing.Lg))
            }
        } else {
            Row(Modifier.heightIn(min = 190.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(0.44f).height(225.dp)) {
                    Image(
                        painter = painterResource(R.drawable.home_outfit_hero),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.horizontalGradient(
                                0.75f to colors.secondaryContainer.copy(alpha = 0f),
                                1f to colors.secondaryContainer,
                            ),
                        ),
                    )
                }
                HeroCopy(
                    onClick,
                    Modifier.weight(0.56f)
                        .padding(start = 8.dp, end = 18.dp, top = 20.dp, bottom = 20.dp),
                )
            }
        }
    }
}

@Composable
private fun HeroCopy(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val titleStyle = MaterialTheme.typography.titleLarge.copy(fontSize = 25.sp, lineHeight = 30.sp)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Md)) {
        Column {
            Text(
                stringResource(R.string.home_hero_title),
                style = titleStyle,
                color = colors.onSecondaryContainer,
            )
            Text(
                stringResource(R.string.home_hero_highlight),
                style = titleStyle,
                color = colors.onSecondaryContainer,
            )
            Box(Modifier.fillMaxWidth().height(3.dp).background(colors.primary, CircleShape))
        }
        Text(
            stringResource(R.string.home_hero_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSecondaryContainer,
        )
        ClosetlyButton(
            text = stringResource(R.string.home_try_now),
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun OutfitCalendar(today: LocalDate, onOpenPlanner: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val locale = remember { Locale.forLanguageTag("id-ID") }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.primaryContainer),
    ) {
        Column(
            Modifier.padding(ClosetlySpacing.Lg),
            verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Md),
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(32.dp).background(colors.secondaryContainer, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(painterResource(R.drawable.ic_closetly_planner), null, tint = colors.onSurface)
                    }
                    Text(
                        stringResource(R.string.home_calendar_title),
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.onSurface,
                    )
                }
                SectionLink(stringResource(R.string.home_view_calendar), onOpenPlanner)
            }
            HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.5f))
            Row(
                horizontalArrangement = Arrangement.spacedBy(ClosetlySpacing.Md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.width(72.dp).clip(RoundedCornerShape(16.dp))
                        .background(Brush.verticalGradient(listOf(colors.secondaryContainer, colors.primaryContainer)))
                        .border(1.dp, colors.outlineVariant, RoundedCornerShape(16.dp))
                        .padding(vertical = ClosetlySpacing.Md),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        today.format(DateTimeFormatter.ofPattern("EEEE", locale)),
                        style = MaterialTheme.typography.bodySmall, color = colors.onSecondaryContainer,
                    )
                    Text(
                        today.format(DateTimeFormatter.ofPattern("dd", locale)),
                        style = MaterialTheme.typography.headlineMedium, color = colors.onSecondaryContainer,
                    )
                    Text(
                        today.format(DateTimeFormatter.ofPattern("MMM", locale)),
                        style = MaterialTheme.typography.bodySmall, color = colors.onSecondaryContainer,
                    )
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Xs)) {
                    Text(stringResource(R.string.home_no_outfit), style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                    Text(stringResource(R.string.home_plan_subtitle), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    ClosetlyButton(text = stringResource(R.string.home_create_plan), onClick = onOpenPlanner)
                }
            }
        }
    }
}

@Composable
private fun CommunitySection(onOpenCommunity: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Md)) {
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = ClosetlySpacing.Xl),
            horizontalArrangement = Arrangement.SpaceBetween,
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.home_community_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            SectionLink(stringResource(R.string.home_view_all), onOpenCommunity)
        }
        Text(
            stringResource(R.string.home_community_subtitle),
            modifier = Modifier.padding(horizontal = ClosetlySpacing.Xl),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = ClosetlySpacing.Xl),
            horizontalArrangement = Arrangement.spacedBy(ClosetlySpacing.Md),
        ) {
            items(
                listOf(
                    R.drawable.home_inspiration_casual to "@dara",
                    R.drawable.home_inspiration_modest to "@salsabila",
                    R.drawable.home_outfit_hero to "@elena",
                ),
                key = { it.first },
            ) { (image, author) ->
                Surface(
                    onClick = onOpenCommunity,
                    modifier = Modifier.width(164.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column {
                        Image(
                            painterResource(image),
                            contentDescription = stringResource(R.string.home_inspiration_description, author),
                            modifier = Modifier.fillMaxWidth().height(196.dp),
                            contentScale = ContentScale.Crop,
                        )
                        Row(
                            modifier = Modifier.padding(ClosetlySpacing.Md),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Image(
                                painterResource(image), null,
                                modifier = Modifier.size(24.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop,
                            )
                            Text(author, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WardrobeSection(onOpenWardrobe: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Md)) {
        Text(stringResource(R.string.home_wardrobe_title), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        Surface(
            onClick = onOpenWardrobe,
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(ClosetlySpacing.Lg),
                horizontalArrangement = Arrangement.spacedBy(ClosetlySpacing.Md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(painterResource(R.drawable.ic_closetly_wardrobe), null, modifier = Modifier.size(36.dp))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.home_open_wardrobe), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.home_wardrobe_subtitle), style = MaterialTheme.typography.bodySmall)
                }
                Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun SectionLink(text: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, contentPadding = PaddingValues(horizontal = 4.dp)) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(4.dp))
        Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ShortcutCard(title: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(20.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
            .clickable(role = Role.Button, onClick = onClick).padding(ClosetlySpacing.Lg),
        verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm),
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurface)
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun HomeScreenPreview() {
    ClosetlyTheme { HomeScreen(displayName = "Vincent", onModuleClick = {}, onProfileClick = {}) }
}
