package vin.pam.closetly.feature.profile

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
fun ProfileScreen(onLogin: () -> Unit, onRegister: () -> Unit, onBack: () -> Unit) {
    ClosetlyScaffold(title = "Profile", onBack = onBack) { padding ->
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
                text = "Profile data and account management will be implemented with the shared authentication feature.",
                modifier = Modifier.padding(top = 12.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            OutlinedButton(onClick = onLogin, modifier = Modifier.padding(top = 20.dp)) {
                Text("Login placeholder")
            }
            OutlinedButton(onClick = onRegister, modifier = Modifier.padding(top = 8.dp)) {
                Text("Registration placeholder")
            }
        }
    }
}
