package vin.pam.closetly.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import vin.pam.closetly.core.ui.components.ClosetlyButton
import vin.pam.closetly.core.ui.components.ClosetlyButtonStyle
import vin.pam.closetly.core.ui.components.ClosetlyCard
import vin.pam.closetly.core.ui.components.ClosetlyScaffold
import vin.pam.closetly.core.ui.components.LoadingState
import vin.pam.closetly.feature.auth.AuthSessionState
import vin.pam.closetly.ui.theme.ClosetlySpacing

@Composable
fun ProfileScreen(
    authState: AuthSessionState,
    uiState: ProfileUiState,
    onUpdateDisplayName: (String) -> Unit,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    onLogin: () -> Unit,
    onBack: () -> Unit,
) {
    ClosetlyScaffold(
        title = "Profile",
        onBack = onBack,
        actionLabel = if (authState is AuthSessionState.Authenticated) "Logout" else null,
        onAction = if (authState is AuthSessionState.Authenticated) onLogout else null,
    ) { padding ->
        when (authState) {
            AuthSessionState.Initializing -> LoadingState(modifier = Modifier.padding(padding))
            is AuthSessionState.Authenticated -> AuthenticatedProfileContent(
                uiState = uiState,
                onUpdateDisplayName = onUpdateDisplayName,
                onRefresh = onRefresh,
                modifier = Modifier.padding(padding),
            )
            AuthSessionState.NotAuthenticated -> SignedOutProfileContent(
                message = "Sign in to load profile data protected by Supabase Row Level Security.",
                onLogin = onLogin,
                modifier = Modifier.padding(padding),
            )
            is AuthSessionState.Failure -> SignedOutProfileContent(
                message = authState.message,
                onLogin = onLogin,
                modifier = Modifier.padding(padding),
            )
            is AuthSessionState.Unavailable -> SignedOutProfileContent(
                message = authState.message,
                onLogin = onLogin,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun AuthenticatedProfileContent(
    uiState: ProfileUiState,
    onUpdateDisplayName: (String) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var displayName by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(uiState.profile?.displayName) {
        displayName = uiState.profile?.displayName.orEmpty()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(ClosetlySpacing.Lg),
        verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Lg),
    ) {
        Text(
            text = "Your Closetly profile.",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Keep the name shown across your personal space and future Community posts up to date.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (uiState.isLoading) {
            ClosetlyCard(modifier = Modifier.fillMaxWidth()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Loading profile...",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                        .padding(top = ClosetlySpacing.Sm),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else if (uiState.profile != null) {
            ClosetlyCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Md)) {
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Display name") },
                        singleLine = true,
                        enabled = !uiState.isSaving,
                        shape = MaterialTheme.shapes.medium,
                    )
                    OutlinedTextField(
                        value = uiState.profile.email,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Email") },
                        singleLine = true,
                        readOnly = true,
                        shape = MaterialTheme.shapes.medium,
                    )
                    uiState.errorMessage?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    uiState.noticeMessage?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    ClosetlyButton(
                        text = if (uiState.isSaving) "Saving..." else "Save display name",
                        onClick = { onUpdateDisplayName(displayName) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isSaving,
                        isLoading = uiState.isSaving,
                    )
                    ClosetlyButton(
                        text = "Reload profile",
                        onClick = onRefresh,
                        modifier = Modifier.fillMaxWidth(),
                        style = ClosetlyButtonStyle.Secondary,
                        enabled = !uiState.isSaving,
                    )
                }
            }
        } else {
            ClosetlyCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = uiState.errorMessage ?: "Profile data is unavailable.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                ClosetlyButton(
                    text = "Try again",
                    onClick = onRefresh,
                    modifier = Modifier.fillMaxWidth().padding(top = ClosetlySpacing.Md),
                    style = ClosetlyButtonStyle.Secondary,
                )
            }
        }
    }
}

@Composable
private fun SignedOutProfileContent(
    message: String,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(ClosetlySpacing.Lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ClosetlyCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Your profile is private.",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = message,
                modifier = Modifier.padding(top = ClosetlySpacing.Sm),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ClosetlyButton(
                text = "Go to login",
                onClick = onLogin,
                modifier = Modifier.fillMaxWidth().padding(top = ClosetlySpacing.Lg),
            )
        }
    }
}
