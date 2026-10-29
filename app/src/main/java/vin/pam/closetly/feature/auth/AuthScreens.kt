package vin.pam.closetly.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import vin.pam.closetly.core.ui.components.ClosetlyScaffold

@Composable
fun LoginScreen(onRegister: () -> Unit, onBack: () -> Unit) {
    AuthenticationPlaceholder(
        title = "Login",
        message = "Authentication is not implemented yet. This screen reserves the shared login route for the authentication feature branch.",
        secondaryLabel = "Registration placeholder",
        onSecondaryAction = onRegister,
        onBack = onBack,
    )
}

@Composable
fun RegisterScreen(onLogin: () -> Unit, onBack: () -> Unit) {
    AuthenticationPlaceholder(
        title = "Register",
        message = "Registration is not implemented yet. No account or remote session will be created from this screen.",
        secondaryLabel = "Login placeholder",
        onSecondaryAction = onLogin,
        onBack = onBack,
    )
}

@Composable
private fun AuthenticationPlaceholder(
    title: String,
    message: String,
    secondaryLabel: String,
    onSecondaryAction: () -> Unit,
    onBack: () -> Unit,
) {
    ClosetlyScaffold(title = title, onBack = onBack) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(24.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Not implemented yet",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = message,
                modifier = Modifier.padding(top = 12.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            OutlinedButton(
                onClick = onSecondaryAction,
                modifier = Modifier.padding(top = 20.dp),
            ) {
                Text(secondaryLabel)
            }
        }
    }
}
