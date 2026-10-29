package vin.pam.closetly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import vin.pam.closetly.di.ClosetlyApplication
import vin.pam.closetly.ui.theme.ClosetlyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as ClosetlyApplication).container
        setContent {
            ClosetlyTheme { ClosetlyApp(container) }
        }
    }
}
