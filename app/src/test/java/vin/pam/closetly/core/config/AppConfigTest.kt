package vin.pam.closetly.core.config

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppConfigTest {
    @Test
    fun supabaseConfiguration_requiresBothValues() {
        assertFalse(AppConfig(null, null).isSupabaseConfigured)
        assertFalse(AppConfig("https://example.supabase.co", null).isSupabaseConfigured)
        assertTrue(AppConfig("https://example.supabase.co", "publishable-key").isSupabaseConfigured)
    }
}
