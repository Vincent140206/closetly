package vin.pam.closetly.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import vin.pam.closetly.core.ui.components.ClosetlyScaffold
import vin.pam.closetly.feature.auth.AuthSessionState

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
            AuthSessionState.Initializing -> ProfileLoading(
                message = "Restoring the saved session...",
                modifier = Modifier.padding(padding),
            )
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
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator()
            Text("Loading profile...")
        } else if (uiState.profile != null) {
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Display name") },
                singleLine = true,
                enabled = !uiState.isSaving,
            )
            OutlinedTextField(
                value = uiState.profile.email,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },
                singleLine = true,
                readOnly = true,
            )
            uiState.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            uiState.noticeMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.primary)
            }
            Button(
                onClick = { onUpdateDisplayName(displayName) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving,
            ) {
                Text(if (uiState.isSaving) "Saving..." else "Save display name")
            }
            OutlinedButton(
                onClick = onRefresh,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving,
            ) {
                Text("Reload profile")
            }
        } else {
            Text(
                text = uiState.errorMessage ?: "Profile data is unavailable.",
                color = MaterialTheme.colorScheme.error,
            )
            OutlinedButton(onClick = onRefresh) {
                Text("Try again")
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
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
            onClick = onLogin,
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text("Go to login")
        }
    }
}

@Composable
private fun ProfileLoading(message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Text(message, modifier = Modifier.padding(top = 12.dp))
    }
}
