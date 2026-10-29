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
    fun registration_requiresMatchingPasswordsAndDisplayName() {
        assertEquals(
            "Display name must contain at least 2 characters.",
            AuthInputValidator.registrationError("A", "user@example.com", "secret1", "secret1"),
        )
        assertEquals(
            "Password confirmation does not match.",
            AuthInputValidator.registrationError("Alice", "user@example.com", "secret1", "secret2"),
        )
        assertNull(
            AuthInputValidator.registrationError(
                "Alice",
                "user@example.com",
                "secret1",
                "secret1",
            ),
        )
    }
}
