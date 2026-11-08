package vin.pam.closetly.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vin.pam.closetly.R
import vin.pam.closetly.core.ui.components.ClosetlyButton
import vin.pam.closetly.ui.theme.ClosetlySpacing
import vin.pam.closetly.ui.theme.ClosetlyTheme

@Composable
fun LoginScreen(
    uiState: AuthUiState,
    onLogin: (email: String, password: String) -> Unit,
    onRegister: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val enabled = !uiState.isSubmitting
    val canSubmit = enabled && uiState.sessionState !is AuthSessionState.Initializing &&
        uiState.sessionState !is AuthSessionState.Unavailable
    val submit = { onLogin(email, password) }
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(colors.surface)
            .safeDrawingPadding()
            .imePadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .padding(horizontal = ClosetlySpacing.Xxl, vertical = ClosetlySpacing.Xl),
        ) {
            AuthHeader(onBack)
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(ClosetlySpacing.Xxl))

            Text(
                text = stringResource(R.string.login_heading),
                modifier = Modifier.semantics { heading() },
                style = typography.displaySmall,
                color = colors.onSurface,
            )
            Spacer(Modifier.height(ClosetlySpacing.Xl))
            Column(verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Lg)) {
                AuthField(
                    label = stringResource(R.string.login_email),
                    placeholder = stringResource(R.string.login_email_placeholder),
                    value = email,
                    onValueChange = { email = it },
                    enabled = enabled,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    ),
                )
                AuthField(
                    label = stringResource(R.string.login_password),
                    placeholder = stringResource(R.string.login_password_placeholder),
                    value = password,
                    onValueChange = { password = it },
                    enabled = enabled,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { if (canSubmit) submit() }),
                    leadingIcon = {
                        Icon(painterResource(R.drawable.ic_register_lock), contentDescription = null)
                    },
                )
            }
            Spacer(Modifier.height(ClosetlySpacing.Xxl))

            SessionAvailabilityMessage(uiState.sessionState)
            FeedbackMessage(uiState.errorMessage, isError = true)
            FeedbackMessage(uiState.noticeMessage, isError = false)
            ClosetlyButton(
                text = stringResource(if (uiState.isSubmitting) R.string.login_submitting else R.string.login_submit),
                onClick = submit,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                enabled = canSubmit,
                isLoading = uiState.isSubmitting,
                shape = MaterialTheme.shapes.medium,
                trailingIcon = {
                    Icon(painterResource(R.drawable.ic_closetly_arrow_forward), contentDescription = null)
                },
            )
            Spacer(Modifier.height(ClosetlySpacing.Sm))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ClosetlySpacing.Md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HorizontalDivider(Modifier.weight(1f), color = colors.outline)
                Text(
                    text = stringResource(R.string.login_or),
                    style = typography.labelSmall,
                    color = colors.onSurfaceVariant,
                )
                HorizontalDivider(Modifier.weight(1f), color = colors.outline)
            }

            Spacer(Modifier.weight(2f))
            Spacer(Modifier.height(ClosetlySpacing.Xl))
            TextButton(
                onClick = onRegister,
                enabled = enabled,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                val prompt = stringResource(R.string.login_register_prompt)
                val action = stringResource(R.string.login_register_action)
                Text(
                    text = buildAnnotatedString {
                        append(prompt)
                        append("  ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(action) }
                    },
                    style = typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = colors.onSurface.copy(alpha = if (enabled) 1f else 0.38f),
                )
            }
        }
    }
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

@Preview(name = "Closetly Login", widthDp = 393, heightDp = 852)
@Preview(name = "Closetly Login Dark", widthDp = 393, heightDp = 852, uiMode = 0x20)
@Composable
private fun LoginScreenPreview() {
    ClosetlyTheme {
        LoginScreen(
            uiState = AuthUiState(sessionState = AuthSessionState.NotAuthenticated),
            onLogin = { _, _ -> },
            onRegister = {},
            onBack = {},
        )
    }
}
