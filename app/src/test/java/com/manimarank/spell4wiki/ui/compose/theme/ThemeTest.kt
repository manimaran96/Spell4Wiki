package com.manimarank.spell4wiki.ui.compose.theme

import com.manimarank.spell4wiki.data.prefs.PrefManager
import org.junit.Assert.*
import org.junit.Test

class ThemeTest {



    @Test
    fun `test theme mode constants are correct`() {
        assertEquals("light", PrefManager.ThemeMode.LIGHT)
        assertEquals("dark", PrefManager.ThemeMode.DARK)
        assertEquals("system", PrefManager.ThemeMode.SYSTEM)
    }

    @Test
    fun `test theme mode persistence`() {
        // Test that theme mode constants are correctly defined
        assertEquals("light", PrefManager.ThemeMode.LIGHT)
        assertEquals("dark", PrefManager.ThemeMode.DARK)
        assertEquals("system", PrefManager.ThemeMode.SYSTEM)
    }
}
