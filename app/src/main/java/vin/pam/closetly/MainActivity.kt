package vin.pam.closetly

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.handleDeeplinks
import kotlinx.coroutines.launch
import vin.pam.closetly.core.data.SupabaseProvider
import vin.pam.closetly.feature.auth.AuthViewModel
import vin.pam.closetly.ui.theme.ClosetlyTheme

class MainActivity : ComponentActivity() {
    private val container get() = (application as ClosetlyApplication).container
    private val authViewModel by viewModels<AuthViewModel> {
        AuthViewModel.factory(container.authRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) handleAuthCallback(intent)
        setContent {
            ClosetlyTheme {
                ClosetlyApp(container = container, authViewModel = authViewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleAuthCallback(intent)
    }

    private fun handleAuthCallback(intent: Intent) {
        if (intent.action != Intent.ACTION_VIEW) return
        val uri = intent.data ?: return
        if (uri.scheme != SupabaseProvider.AUTH_CALLBACK_SCHEME ||
            uri.host != SupabaseProvider.AUTH_CALLBACK_HOST
        ) return

        val code = uri.getQueryParameter("code")
        if (uri.path == SupabaseProvider.PASSWORD_RECOVERY_PATH) {
            authViewModel.openPasswordRecovery(code)
            return
        }
        if (!uri.path.isNullOrEmpty() && uri.path != "/") return

        val supabase = container.supabaseClient
        if (supabase == null || code.isNullOrBlank()) {
            showAuthCallbackError()
            return
        }
        lifecycleScope.launch {
            supabase.auth.awaitInitialization()
            supabase.handleDeeplinks(intent, onError = {
                lifecycleScope.launch { showAuthCallbackError() }
            })
        }
    }

    private fun showAuthCallbackError() {
        Toast.makeText(this, R.string.auth_callback_error, Toast.LENGTH_LONG).show()
    }
}
