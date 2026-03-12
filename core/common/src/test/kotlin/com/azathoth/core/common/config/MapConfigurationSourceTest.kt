package com.azathoth.core.common.config

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MapConfigurationSourceTest {

    @Test
    fun `test basic operations`() {
        val source = MapConfigurationSource("test", priority = 10)

        assertEquals("test", source.name)
        assertEquals(10, source.priority)

        source.set("key1", "value1")
        assertEquals("value1", source.getRaw("key1"))
        assertTrue(source.contains("key1"))
        assertEquals(setOf("key1"), source.keys())

        source.remove("key1")
        assertNull(source.getRaw("key1"))
        assertFalse(source.contains("key1"))
    }
}
