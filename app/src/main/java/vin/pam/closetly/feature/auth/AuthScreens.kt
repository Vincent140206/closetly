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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import vin.pam.closetly.core.ui.components.ClosetlyScaffold

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

    AuthFormScaffold(title = "Login", onBack = onBack) {
        Text(
            text = "Sign in to access your Supabase profile.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
        Button(
            onClick = { onLogin(email, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = canSubmit,
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(end = 8.dp),
                    strokeWidth = 2.dp,
                )
            }
            Text(if (uiState.isSubmitting) "Signing in..." else "Login")
        }
        OutlinedButton(
            onClick = onRegister,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSubmitting,
        ) {
            Text("Create an account")
        }
    }
}

@Composable
fun RegisterScreen(
    uiState: AuthUiState,
    onRegister: (
        displayName: String,
        email: String,
        password: String,
        confirmPassword: String,
    ) -> Unit,
    onLogin: () -> Unit,
    onBack: () -> Unit,
) {
    var displayName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }
    val canSubmit = uiState.sessionState !is AuthSessionState.Initializing &&
        uiState.sessionState !is AuthSessionState.Unavailable &&
        !uiState.isSubmitting

    AuthFormScaffold(title = "Register", onBack = onBack) {
        Text(
            text = "Create an account. Your profile row is created by the existing database trigger.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SessionAvailabilityMessage(uiState.sessionState)
        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Display name") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            enabled = !uiState.isSubmitting,
        )
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
        )
        PasswordField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            visible = passwordVisible,
            onVisibilityChange = { passwordVisible = !passwordVisible },
            imeAction = ImeAction.Next,
            enabled = !uiState.isSubmitting,
        )
        PasswordField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Confirm password",
            visible = confirmPasswordVisible,
            onVisibilityChange = { confirmPasswordVisible = !confirmPasswordVisible },
            imeAction = ImeAction.Done,
            enabled = !uiState.isSubmitting,
        )
        FeedbackMessage(uiState.errorMessage, isError = true)
        FeedbackMessage(uiState.noticeMessage, isError = false)
        Button(
            onClick = { onRegister(displayName, email, password, confirmPassword) },
            modifier = Modifier.fillMaxWidth(),
            enabled = canSubmit,
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(end = 8.dp),
                    strokeWidth = 2.dp,
                )
            }
            Text(if (uiState.isSubmitting) "Creating account..." else "Register")
        }
        TextButton(
            onClick = onLogin,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            enabled = !uiState.isSubmitting,
        ) {
            Text("Already registered? Login")
        }
    }
}

@Composable
private fun AuthFormScaffold(
    title: String,
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content,
        )
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
            TextButton(onClick = onVisibilityChange, enabled = enabled) {
                Text(if (visible) "Hide" else "Show")
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction,
        ),
        enabled = enabled,
    )
}

@Composable
private fun SessionAvailabilityMessage(sessionState: AuthSessionState) {
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
private fun FeedbackMessage(message: String?, isError: Boolean) {
    if (message == null) return
    Text(
        text = message,
        color = if (isError) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.primary
        },
        style = MaterialTheme.typography.bodyMedium,
    )
}
