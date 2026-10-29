package vin.pam.closetly.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import vin.pam.closetly.core.ui.components.ClosetlyScaffold

@Composable
fun LoginScreen(onDemoEntry: () -> Unit, onRegister: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Closetly", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Text("Your wardrobe, thoughtfully organized.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(password, { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(20.dp))
        Button(onClick = onDemoEntry, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Enter demo mode") }
        Text(
            "Demo mode uses local sample data. It is not Supabase authentication.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 10.dp),
        )
        TextButton(onClick = onRegister, modifier = Modifier.fillMaxWidth()) { Text("Create an account UI") }
    }
}

@Composable
fun RegisterScreen(onDemoEntry: () -> Unit, onBack: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    ClosetlyScaffold("Register", onBack = onBack) { padding ->
        Column(Modifier.padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Registration scaffold", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            OutlinedTextField(name, { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField("", {}, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = onDemoEntry, modifier = Modifier.fillMaxWidth()) { Text("Continue in demo mode") }
            Text("Real sign-up will be connected through AuthRepository during the Supabase milestone.", style = MaterialTheme.typography.bodySmall)
        }
    }
}
