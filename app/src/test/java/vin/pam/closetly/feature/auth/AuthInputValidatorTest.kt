package vin.pam.closetly.feature.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthInputValidatorTest {
    @Test
    fun login_requiresValidEmailAndPassword() {
        assertEquals(
            "Enter a valid email address.",
            AuthInputValidator.loginError("invalid-email", "password"),
        )
        assertEquals(
            "Password is required.",
            AuthInputValidator.loginError("user@example.com", ""),
        )
        assertNull(AuthInputValidator.loginError("user@example.com", "password"))
    }

    @Test
    fun registration_checksNameUsernameAndPassword() {
        assertEquals(
            "Display name must contain at least 2 characters.",
            AuthInputValidator.registrationError("A", "alice", "user@example.com", "secret12", "secret12"),
        )
        assertEquals(
            "Password confirmation does not match.",
            AuthInputValidator.registrationError("Alice", "alice", "user@example.com", "secret12", "secret13"),
        )
        assertEquals(
            "Gunakan huruf kecil dan angka tanpa spasi untuk username.",
            AuthInputValidator.registrationError("Alice", "Alice 1", "user@example.com", "secret12", "secret12"),
        )
        assertEquals(
            "Password harus memiliki minimal 8 karakter.",
            AuthInputValidator.registrationError("Alice", "alice", "user@example.com", "short1", "short1"),
        )
        assertEquals(
            "Password harus mengandung angka.",
            AuthInputValidator.registrationError("Alice", "alice", "user@example.com", "secretword", "secretword"),
        )
        assertNull(
            AuthInputValidator.registrationError(
                "Alice",
                "alice1",
                "user@example.com",
                "secret12",
                "secret12",
            ),
        )
    }
}
