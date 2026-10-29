package vin.pam.closetly.core.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppModuleTest {
    @Test
    fun moduleCatalog_hasSixUniqueOwnedModules() {
        val modules = AppModule.entries

        assertEquals(6, modules.size)
        assertEquals(modules.size, modules.map { it.title }.distinct().size)
        assertTrue(modules.all { it.owner.isNotBlank() })
        assertTrue(modules.all { it.branch.startsWith("feature/") })
    }
}
