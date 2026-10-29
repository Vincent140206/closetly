package vin.pam.closetly.feature.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AuthSession(
    val userId: String = "demo-user",
    val displayName: String = "Closetly Demo",
    val email: String = "demo@closetly.local",
    val isDemo: Boolean = true,
)

interface AuthRepository {
    val session: StateFlow<AuthSession?>
    fun enterDemo()
    fun updateProfile(displayName: String, email: String)
    fun signOut()
}

class DemoAuthRepository : AuthRepository {
    private val _session = MutableStateFlow<AuthSession?>(null)
    override val session: StateFlow<AuthSession?> = _session.asStateFlow()

    override fun enterDemo() { _session.value = AuthSession() }
    override fun updateProfile(displayName: String, email: String) {
        _session.value = _session.value?.copy(displayName = displayName, email = email)
    }
    override fun signOut() { _session.value = null }
}

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    val session: StateFlow<AuthSession?> = repository.session
    fun enterDemo() = repository.enterDemo()
    fun updateProfile(displayName: String, email: String) = repository.updateProfile(displayName, email)
    fun signOut() = repository.signOut()
}
