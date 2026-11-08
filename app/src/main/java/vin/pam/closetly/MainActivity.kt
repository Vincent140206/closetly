package vin.pam.closetly

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.handleDeeplinks
import kotlinx.coroutines.launch
import vin.pam.closetly.core.data.SupabaseProvider
import vin.pam.closetly.ui.theme.ClosetlyTheme

class MainActivity : ComponentActivity() {
    private val container get() = (application as ClosetlyApplication).container

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) handleAuthCallback(intent)
        setContent {
            ClosetlyTheme {
                ClosetlyApp(container = container)
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

        val supabase = container.supabaseClient
        if (supabase == null || uri.getQueryParameter("code").isNullOrBlank()) {
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
