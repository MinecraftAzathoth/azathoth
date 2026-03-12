package com.azathoth.core.common.config

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ReflectionConfigurationBinderTest {

    data class ServerConfig(
        val host: String,
        val port: Int,
        val debug: Boolean = false
    )

    data class AppConfig(
        val name: String,
        val version: String = "1.0.0"
    )

    private val binder = ReflectionConfigurationBinder()

    @Test
    fun `test bind simple data class`() {
        val config = MapConfiguration(
            mutableMapOf(
                "host" to "localhost",
                "port" to 8080,
                "debug" to true
            )
        )

        val result = binder.bind(config, ServerConfig::class)
        assertEquals("localhost", result.host)
        assertEquals(8080, result.port)
        assertTrue(result.debug)
    }

    @Test
    fun `test bind with default values`() {
        val config = MapConfiguration(
            mutableMapOf(
                "host" to "0.0.0.0",
                "port" to 9090
            )
        )

        val result = binder.bind(config, ServerConfig::class)
        assertEquals("0.0.0.0", result.host)
        assertEquals(9090, result.port)
        assertFalse(result.debug) // 使用默认值
    }

    @Test
    fun `test bind missing required field throws`() {
        val config = MapConfiguration(
            mutableMapOf("port" to 8080)
        )

        assertThrows(IllegalArgumentException::class.java) {
            binder.bind(config, ServerConfig::class)
        }
    }

    @Test
    fun `test unbind produces configuration`() {
        val obj = AppConfig("azathoth", "2.0.0")
        val config = binder.unbind(obj)

        assertEquals("azathoth", config.getString("name"))
        assertEquals("2.0.0", config.getString("version"))
    }
}
