package vin.pam.closetly.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import vin.pam.closetly.R
import vin.pam.closetly.core.ui.components.ClosetlyButton
import vin.pam.closetly.core.ui.components.ClosetlyTextButton
import vin.pam.closetly.ui.theme.ClosetlySpacing

@Composable
internal fun PasswordResetRequestDialog(
    initialEmail: String,
    uiState: AuthUiState,
    canSubmit: Boolean,
    onRequest: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf(initialEmail) }
    val sent = uiState.noticeMessage != null
    val canDismiss = !uiState.isSubmitting
    AlertDialog(
        onDismissRequest = { if (canDismiss) onDismiss() },
        title = { Text(stringResource(R.string.password_reset_request_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Md),
            ) {
                if (sent) {
                    FeedbackMessage(uiState.noticeMessage, isError = false)
                } else {
                    Text(stringResource(R.string.password_reset_request_description))
                    AuthField(
                        label = stringResource(R.string.login_email),
                        placeholder = stringResource(R.string.login_email_placeholder),
                        value = email,
                        onValueChange = { email = it },
                        enabled = canDismiss,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = { if (canSubmit) onRequest(email) }),
                    )
                    SessionAvailabilityMessage(uiState.sessionState)
                    FeedbackMessage(uiState.errorMessage, isError = true)
                }
            }
        },
        confirmButton = {
            if (sent) {
                ClosetlyButton(text = stringResource(R.string.password_reset_close), onClick = onDismiss)
            } else {
                ClosetlyButton(
                    text = stringResource(
                        if (uiState.isSubmitting) R.string.password_reset_sending else R.string.password_reset_send,
                    ),
                    onClick = { onRequest(email) },
                    enabled = canSubmit,
                    isLoading = uiState.isSubmitting,
                )
            }
        },
        dismissButton = {
            if (!sent) {
                ClosetlyTextButton(
                    text = stringResource(R.string.password_reset_cancel),
                    onClick = onDismiss,
                    enabled = canDismiss,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
    )
}

@Composable
internal fun PasswordRecoveryDialog(
    state: PasswordRecoveryState,
    onUpdatePassword: (String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    val ready = state as? PasswordRecoveryState.Ready
    var password by remember(ready?.email) { mutableStateOf("") }
    var confirmation by remember(ready?.email) { mutableStateOf("") }
    val canDismiss = state != PasswordRecoveryState.Opening && ready?.isSaving != true
    val title = when (state) {
        PasswordRecoveryState.Opening -> R.string.password_reset_opening
        PasswordRecoveryState.InvalidLink -> R.string.password_reset_invalid_title
        PasswordRecoveryState.Complete -> R.string.password_reset_done_title
        is PasswordRecoveryState.Ready -> R.string.password_reset_new_title
    }

    AlertDialog(
        onDismissRequest = { if (canDismiss) onDismiss() },
        title = { Text(stringResource(title)) },
        text = {
            when (state) {
                PasswordRecoveryState.Opening -> Unit
                PasswordRecoveryState.InvalidLink ->
                    Text(stringResource(R.string.password_reset_invalid_description))
                PasswordRecoveryState.Complete ->
                    Text(stringResource(R.string.password_reset_done_description))
                is PasswordRecoveryState.Ready -> {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Md),
                    ) {
                        Text(stringResource(R.string.password_reset_new_description, state.email))
                        AuthField(
                            label = stringResource(R.string.password_reset_new_password),
                            placeholder = stringResource(R.string.login_password_placeholder),
                            value = password,
                            onValueChange = { password = it },
                            enabled = !state.isSaving,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next,
                            ),
                        )
                        AuthField(
                            label = stringResource(R.string.password_reset_confirmation),
                            placeholder = stringResource(R.string.password_reset_confirmation),
                            value = confirmation,
                            onValueChange = { confirmation = it },
                            enabled = !state.isSaving,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { if (!state.isSaving) onUpdatePassword(password, confirmation) },
                            ),
                        )
                        FeedbackMessage(state.errorMessage, isError = true)
                    }
                }
            }
        },
        confirmButton = {
            when (state) {
                PasswordRecoveryState.Opening -> Unit
                PasswordRecoveryState.InvalidLink, PasswordRecoveryState.Complete ->
                    ClosetlyButton(text = stringResource(R.string.password_reset_close), onClick = onDismiss)
                is PasswordRecoveryState.Ready ->
                    ClosetlyButton(
                        text = stringResource(
                            if (state.isSaving) R.string.password_reset_saving else R.string.password_reset_save,
                        ),
                        onClick = { onUpdatePassword(password, confirmation) },
                        isLoading = state.isSaving,
                    )
            }
        },
        dismissButton = {
            if (ready != null) {
                ClosetlyTextButton(
                    text = stringResource(R.string.password_reset_cancel),
                    onClick = onDismiss,
                    enabled = canDismiss,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
    )
}
