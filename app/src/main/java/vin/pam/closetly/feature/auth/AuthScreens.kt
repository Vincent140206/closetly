package vin.pam.closetly.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import vin.pam.closetly.core.ui.components.ClosetlyButton
import vin.pam.closetly.core.ui.components.ClosetlyButtonStyle
import vin.pam.closetly.core.ui.components.ClosetlyCard
import vin.pam.closetly.core.ui.components.ClosetlyScaffold
import vin.pam.closetly.core.ui.components.ClosetlyTextButton
import vin.pam.closetly.ui.theme.ClosetlySpacing

@Composable
fun LoginScreen(
    uiState: AuthUiState,
    onLogin: (email: String, password: String) -> Unit,
    onRegister: () -> Unit,
    onBack: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val canSubmit = uiState.sessionState !is AuthSessionState.Initializing &&
        uiState.sessionState !is AuthSessionState.Unavailable &&
        !uiState.isSubmitting

    AuthFormScaffold(
        title = "Login",
        heading = "Welcome back.",
        supportingText = "Sign in to continue building your wardrobe story.",
        onBack = onBack,
    ) {
        SessionAvailabilityMessage(uiState.sessionState)
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            enabled = !uiState.isSubmitting,
            shape = MaterialTheme.shapes.medium,
        )
        PasswordField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            visible = passwordVisible,
            onVisibilityChange = { passwordVisible = !passwordVisible },
            imeAction = ImeAction.Done,
            enabled = !uiState.isSubmitting,
        )
        FeedbackMessage(uiState.errorMessage, isError = true)
        FeedbackMessage(uiState.noticeMessage, isError = false)
        ClosetlyButton(
            text = if (uiState.isSubmitting) "Signing in..." else "Login",
            onClick = { onLogin(email, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = canSubmit,
            isLoading = uiState.isSubmitting,
        )
        ClosetlyButton(
            text = "Create an account",
            onClick = onRegister,
            modifier = Modifier.fillMaxWidth(),
            style = ClosetlyButtonStyle.Secondary,
            enabled = !uiState.isSubmitting,
        )
    }
}

@Composable
private fun AuthFormScaffold(
    title: String,
    heading: String,
    supportingText: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    ClosetlyScaffold(title = title, onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(ClosetlySpacing.Lg),
            verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Lg),
        ) {
            Text(
                text = heading,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ClosetlyCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Md),
                    content = content,
                )
            }
        }
    }
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visible: Boolean,
    onVisibilityChange: () -> Unit,
    imeAction: ImeAction,
    enabled: Boolean,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (visible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = {
            ClosetlyTextButton(
                text = if (visible) "Hide" else "Show",
                onClick = onVisibilityChange,
                enabled = enabled,
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction,
        ),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
    )
}

@Composable
internal fun SessionAvailabilityMessage(sessionState: AuthSessionState) {
    val message = when (sessionState) {
        AuthSessionState.Initializing -> "Restoring the saved session..."
        is AuthSessionState.Failure -> sessionState.message
        is AuthSessionState.Unavailable -> sessionState.message
        else -> null
    }
    FeedbackMessage(
        message = message,
        isError = sessionState is AuthSessionState.Failure ||
            sessionState is AuthSessionState.Unavailable,
    )
}

@Composable
internal fun FeedbackMessage(message: String?, isError: Boolean) {
    if (message == null) return
    Text(
        text = message,
        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.bodyMedium,
    )
}
