package vin.pam.closetly.feature.onboarding.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import vin.pam.closetly.R
import vin.pam.closetly.ui.theme.ClosetlyDimensions
import vin.pam.closetly.ui.theme.ClosetlySpacing

@Composable
internal fun OnboardingHero(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val shapes = MaterialTheme.shapes

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.primary, shapes.extraLarge)
            .padding(horizontal = ClosetlyDimensions.CardPadding, vertical = ClosetlySpacing.Xl),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_closetly_splash),
                contentDescription = null,
                modifier = Modifier.size(ClosetlySpacing.Xxl),
                colorFilter = ColorFilter.tint(colors.onPrimary),
            )
            Text(
                text = stringResource(R.string.app_name),
                style = typography.titleLarge,
                color = colors.onPrimary,
            )
        }
        Spacer(Modifier.height(ClosetlySpacing.Xxxl))
        Image(
            painter = painterResource(R.drawable.onboarding_collage),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = ClosetlySpacing.Lg, end = ClosetlySpacing.Sm)
                .aspectRatio(1.35f),
            contentScale = ContentScale.Fit,
        )
        Spacer(Modifier.height(ClosetlySpacing.Lg))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface, shapes.small)
                .padding(horizontal = ClosetlySpacing.Sm, vertical = ClosetlySpacing.Md),
            horizontalArrangement = Arrangement.spacedBy(ClosetlySpacing.Xs),
        ) {
            HeroStat(
                stringResource(R.string.onboarding_combinations_value),
                stringResource(R.string.onboarding_combinations_label),
                Modifier.weight(1f),
            )
            HeroStat(
                stringResource(R.string.onboarding_confusion_value),
                stringResource(R.string.onboarding_confusion_label),
                Modifier.weight(1f),
            )
            HeroStat(
                stringResource(R.string.onboarding_log_value),
                stringResource(R.string.onboarding_log_label),
                Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun HeroStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
