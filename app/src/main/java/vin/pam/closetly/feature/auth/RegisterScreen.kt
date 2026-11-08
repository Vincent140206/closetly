package vin.pam.closetly.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vin.pam.closetly.R
import vin.pam.closetly.core.ui.components.ClosetlyButton
import vin.pam.closetly.core.ui.components.ClosetlyTextButton
import vin.pam.closetly.ui.theme.ClosetlyDimensions
import vin.pam.closetly.ui.theme.ClosetlyFullShape
import vin.pam.closetly.ui.theme.ClosetlySpacing
import vin.pam.closetly.ui.theme.ClosetlyTheme

private val WordmarkFont = FontFamily(Font(R.font.harabara_mais_demo))

@Composable
fun RegisterScreen(
    uiState: AuthUiState,
    onRegister: (displayName: String, username: String, email: String, password: String, confirmPassword: String) -> Unit,
    onLogin: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var displayName by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val enabled = !uiState.isSubmitting
    val canSubmit = enabled && uiState.sessionState !is AuthSessionState.Initializing &&
        uiState.sessionState !is AuthSessionState.Unavailable
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Box(
        modifier = modifier.fillMaxSize().background(colors.surface).safeDrawingPadding().imePadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ClosetlySpacing.Xxl, vertical = ClosetlySpacing.Xl),
        ) {
            RegisterHeader(onBack)
            Spacer(Modifier.height(ClosetlySpacing.Xxl))
            Text(
                text = "Buat akunmu",
                modifier = Modifier.semantics { heading() },
                style = typography.displaySmall,
                color = colors.onSurface,
            )
            Spacer(Modifier.height(ClosetlySpacing.Sm))
            Text(
                text = "Mulai perjalanan gayamu bersama Closetly. Kurasi lemari pakaian digital impianmu hari ini.",
                style = typography.bodyLarge,
                color = colors.onSurfaceVariant,
            )
            Spacer(Modifier.height(ClosetlySpacing.Xxl))
            Column(verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Lg)) {
                RegisterField(
                    label = "Nama Lengkap",
                    hint = "Sesuai KTP/Panggilan",
                    placeholder = "Masukkan nama lengkap",
                    value = displayName,
                    onValueChange = { displayName = it },
                    enabled = enabled,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next,
                    ),
                )
                Column(verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm)) {
                    RegisterField(
                        label = "Username",
                        placeholder = "Buat username",
                        value = username,
                        onValueChange = { username = it },
                        enabled = enabled,
                        keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, imeAction = ImeAction.Next),
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_register_info),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = colors.onSurfaceVariant,
                        )
                        Text(
                            text = "Gunakan huruf kecil, angka, dan tanpa spasi",
                            style = typography.bodySmall,
                            color = colors.onSurfaceVariant,
                        )
                    }
                }
                RegisterField(
                    label = "Email",
                    placeholder = "Masukkan alamat email",
                    value = email,
                    onValueChange = { email = it },
                    enabled = enabled,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                )
                Column(verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm)) {
                    RegisterField(
                        label = "Password",
                        placeholder = "Buat password",
                        value = password,
                        onValueChange = { password = it },
                        enabled = enabled,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        leadingIcon = {
                            Icon(painterResource(R.drawable.ic_register_lock), contentDescription = null)
                        },
                    )
                    PasswordRequirements(password)
                }
                RegisterField(
                    label = "Konfirmasi Password",
                    placeholder = "Ketik ulang password",
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    enabled = enabled,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                )
            }
            Spacer(Modifier.height(ClosetlySpacing.Xxl))
            SessionAvailabilityMessage(uiState.sessionState)
            FeedbackMessage(uiState.errorMessage, isError = true)
            FeedbackMessage(uiState.noticeMessage, isError = false)
            Text(
                text = buildAnnotatedString {
                    val bold = SpanStyle(fontWeight = FontWeight.Bold)
                    append("Dengan mendaftar, kamu menyetujui ")
                    withStyle(bold) { append("Syarat Ketentuan") }
                    append(" dan ")
                    withStyle(bold) { append("Kebijakan Privasi") }
                    append(" Closetly.")
                },
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = colors.onSurfaceVariant,
                style = typography.bodySmall,
            )
            Spacer(Modifier.height(ClosetlySpacing.Md))
            ClosetlyButton(
                text = if (uiState.isSubmitting) "Membuat akun…" else "Daftar Masuk",
                onClick = { onRegister(displayName, username, email, password, confirmPassword) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                enabled = canSubmit,
                isLoading = uiState.isSubmitting,
                shape = MaterialTheme.shapes.medium,
                trailingIcon = {
                    Icon(painterResource(R.drawable.ic_closetly_arrow_forward), contentDescription = null)
                },
            )
            Spacer(Modifier.height(ClosetlySpacing.Xl))
            TextButton(
                onClick = onLogin,
                enabled = enabled,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("Sudah punya akun?  ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Masuk") }
                    },
                    textAlign = TextAlign.Center,
                    style = typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = if (enabled) 1f else 0.38f),
                )
            }
        }
    }
}

@Composable
private fun RegisterHeader(onBack: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.padding(horizontal = ClosetlyDimensions.MinimumTouchTarget),
            shape = ClosetlyFullShape,
            color = colors.primary,
            contentColor = colors.onPrimary,
            shadowElevation = 2.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = ClosetlySpacing.Lg, vertical = ClosetlySpacing.Sm),
                horizontalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_closetly_splash),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Closetly",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = WordmarkFont,
                    fontWeight = FontWeight.Normal,
                )
            }
        }
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
            Icon(
                painter = painterResource(R.drawable.ic_closetly_arrow_forward),
                contentDescription = "Kembali",
                modifier = Modifier.rotate(180f),
                tint = colors.onSurface,
            )
        }
    }
}

@Composable
private fun RegisterField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    keyboardOptions: KeyboardOptions,
    hint: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val isPassword = keyboardOptions.keyboardType == KeyboardType.Password
    var visible by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, modifier = Modifier.weight(1f), style = typography.titleMedium, color = colors.onSurface)
            if (hint != null) {
                Text(
                    text = hint,
                    modifier = Modifier.weight(1f),
                    style = typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.End,
                )
            }
        }
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = label },
            placeholder = { Text(placeholder, style = typography.bodyMedium) },
            textStyle = typography.bodyLarge,
            singleLine = true,
            enabled = enabled,
            leadingIcon = leadingIcon,
            trailingIcon = if (isPassword) {
                {
                    ClosetlyTextButton(
                        text = if (visible) "Tutup" else "Lihat",
                        onClick = { visible = !visible },
                        enabled = enabled,
                        contentColor = colors.onSurfaceVariant,
                    )
                }
            } else {
                null
            },
            keyboardOptions = keyboardOptions,
            visualTransformation = if (isPassword && !visible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            shape = MaterialTheme.shapes.medium,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colors.surfaceVariant,
                unfocusedContainerColor = colors.surfaceVariant,
                disabledContainerColor = colors.surfaceVariant,
                focusedIndicatorColor = colors.primary,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                cursorColor = colors.onSurface,
            ),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PasswordRequirements(password: String) {
    val meetsLength = AuthInputValidator.hasMinimumPasswordLength(password)
    val hasNumber = AuthInputValidator.hasPasswordNumber(password)
    val filledSegments = listOf(password.isNotEmpty(), meetsLength, hasNumber).count { it }
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ClosetlySpacing.Xs),
    ) {
        repeat(3) { segment ->
            Spacer(
                Modifier.weight(1f).height(4.dp).background(
                    color = if (segment < filledSegments) colors.primary else colors.outlineVariant,
                    shape = ClosetlyFullShape,
                ),
            )
        }
    }
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(ClosetlySpacing.Sm),
        verticalArrangement = Arrangement.spacedBy(ClosetlySpacing.Xs),
    ) {
        PasswordRequirement("Min. ${AuthInputValidator.MinimumPasswordLength} karakter", meetsLength)
        PasswordRequirement("Kombinasi angka", hasNumber)
    }
}

@Composable
private fun PasswordRequirement(label: String, met: Boolean) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.semantics { stateDescription = if (met) "Terpenuhi" else "Belum terpenuhi" },
        shape = ClosetlyFullShape,
        color = if (met) colors.primaryContainer else colors.outlineVariant,
        contentColor = if (met) colors.onPrimaryContainer else colors.onSurface,
    ) {
        Text(
            text = if (met) "$label  ✓" else label,
            modifier = Modifier.padding(horizontal = ClosetlySpacing.Md, vertical = ClosetlySpacing.Xs),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Preview(name = "Closetly Register", widthDp = 393, heightDp = 852)
@Preview(name = "Closetly Register Dark", widthDp = 393, heightDp = 852, uiMode = 0x20)
@Composable
private fun RegisterScreenPreview() {
    ClosetlyTheme {
        RegisterScreen(
            uiState = AuthUiState(sessionState = AuthSessionState.NotAuthenticated),
            onRegister = { _, _, _, _, _ -> },
            onLogin = {},
            onBack = {},
        )
    }
}
