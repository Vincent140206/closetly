package vin.pam.closetly.feature.onboarding.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import vin.pam.closetly.R
import vin.pam.closetly.ui.theme.ClosetlyDimensions
import vin.pam.closetly.ui.theme.ClosetlySpacing

@Composable
internal fun OnboardingActions(
    onGetStarted: () -> Unit,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val shape = MaterialTheme.shapes.medium

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm)) {
        Button(
            onClick = onGetStarted,
            modifier = Modifier.fillMaxWidth().heightIn(min = ClosetlyDimensions.MinimumTouchTarget),
            shape = shape,
            contentPadding = PaddingValues(
                horizontal = ClosetlySpacing.Lg,
                vertical = ClosetlySpacing.Lg,
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.secondary,
                contentColor = colors.onSecondary,
            ),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.onboarding_get_started),
                    modifier = Modifier.weight(1f, fill = false),
                    style = typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
                Icon(
                    painter = painterResource(R.drawable.ic_closetly_arrow_forward),
                    contentDescription = null,
                    modifier = Modifier.size(ClosetlySpacing.Lg),
                )
            }
        }
        Button(
            onClick = onLogin,
            modifier = Modifier.fillMaxWidth().heightIn(min = ClosetlyDimensions.MinimumTouchTarget),
            shape = shape,
            contentPadding = PaddingValues(
                horizontal = ClosetlySpacing.Lg,
                vertical = ClosetlySpacing.Md,
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.secondaryContainer,
                contentColor = colors.onSecondaryContainer,
            ),
        ) {
            Text(
                text = stringResource(R.string.onboarding_login),
                style = typography.labelLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}
