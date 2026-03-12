package com.azathoth.core.common.config

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MapConfigurationTest {

    @Test
    fun `test basic get and set`() {
        val config = MapConfiguration()
        config.set("name", "azathoth")
        config.set("port", 25565)
        config.set("debug", true)

        assertEquals("azathoth", config.getString("name"))
        assertEquals(25565, config.getInt("port"))
        assertTrue(config.getBoolean("debug"))
    }

    @Test
    fun `test nested key access`() {
        val config = MapConfiguration(
            mutableMapOf(
                "server" to mutableMapOf(
                    "host" to "localhost",
                    "port" to 8080
                )
            )
        )

        assertEquals("localhost", config.getString("server.host"))
        assertEquals(8080, config.getInt("server.port"))
    }

    @Test
    fun `test nested set`() {
        val config = MapConfiguration()
        config.set("database.host", "localhost")
        config.set("database.port", 5432)

        assertEquals("localhost", config.getString("database.host"))
        assertEquals(5432, config.getInt("database.port"))
    }

    @Test
    fun `test default values`() {
        val config = MapConfiguration()

        assertEquals("default", config.getString("missing", "default"))
        assertEquals(42, config.getInt("missing", 42))
        assertEquals(3.14, config.getDouble("missing", 3.14))
        assertFalse(config.getBoolean("missing", false))
    }

    @Test
    fun `test getSection returns sub-configuration`() {
        val config = MapConfiguration(
            mutableMapOf(
                "db" to mutableMapOf(
                    "host" to "localhost",
                    "port" to 5432
                )
            )
        )

        val section = config.getSection("db")
        assertNotNull(section)
        assertEquals("localhost", section!!.getString("host"))
        assertEquals(5432, section.getInt("port"))
    }

    @Test
    fun `test getStringList`() {
        val config = MapConfiguration(
            mutableMapOf("tags" to listOf("a", "b", "c"))
        )

        assertEquals(listOf("a", "b", "c"), config.getStringList("tags"))
    }

    @Test
    fun `test contains and keys`() {
        val config = MapConfiguration(
            mutableMapOf("a" to 1, "b" to 2)
        )

        assertTrue(config.contains("a"))
        assertFalse(config.contains("c"))
        assertEquals(setOf("a", "b"), config.keys())
    }

    @Test
    fun `test remove`() {
        val config = MapConfiguration(
            mutableMapOf("key" to "value")
        )

        config.remove("key")
        assertNull(config.getString("key"))
    }
}
