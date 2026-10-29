package vin.pam.closetly.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import vin.pam.closetly.core.ui.components.ClosetlyScaffold
import vin.pam.closetly.feature.auth.AuthSession

@Composable
fun ProfileScreen(session: AuthSession?, onEdit: () -> Unit, onSignOut: () -> Unit, onBack: () -> Unit) {
    ClosetlyScaffold("Profile", onBack = onBack) { padding ->
        Column(Modifier.padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(session?.displayName ?: "Guest", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(session?.email.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (session?.isDemo == true) Text("DEMO PROFILE", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
            Button(onClick = onEdit, modifier = Modifier.fillMaxWidth()) { Text("Edit profile") }
            OutlinedButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) { Text("Leave demo mode") }
        }
    }
}

@Composable
fun EditProfileScreen(session: AuthSession?, onSave: (String, String) -> Unit, onBack: () -> Unit) {
    var name by rememberSaveable(session?.displayName) { mutableStateOf(session?.displayName.orEmpty()) }
    var email by rememberSaveable(session?.email) { mutableStateOf(session?.email.orEmpty()) }
    ClosetlyScaffold("Edit profile", onBack = onBack) { padding ->
        Column(Modifier.padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            OutlinedTextField(name, { name = it }, label = { Text("Display name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = { onSave(name, email) }, enabled = name.isNotBlank() && email.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Save profile") }
        }
    }
}
