package vin.pam.closetly.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class DesignSystemTokenTest {
    @Test
    fun lightPalette_matchesDesignSystemV02() {
        assertEquals(Color(0xFFFACC15), GoldenPrimary)
        assertEquals(Color(0xFFEAB308), GoldenPressed)
        assertEquals(Color(0xFFFDE68A), SoftYellow)
        assertEquals(Color(0xFFFFFBEB), Vanilla)
        assertEquals(Color(0xFFFAFAF7), OffWhite)
        assertEquals(Color(0xFFFFFFFF), CardWhite)
        assertEquals(Color(0xFF171717), Heading)
        assertEquals(Color(0xFF262626), Body)
        assertEquals(Color(0xFF62625F), Muted)
        assertEquals(Color(0xFFE8E5DC), Border)
    }
}
