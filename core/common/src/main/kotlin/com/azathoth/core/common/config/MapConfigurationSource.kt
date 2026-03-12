package com.azathoth.core.common.config

/**
 * 基于 Map 的可变配置源
 */
class MapConfigurationSource(
    override val name: String,
    override val priority: Int = 100,
    private val data: MutableMap<String, String> = mutableMapOf()
) : MutableConfigurationSource {

    override fun getRaw(key: String): String? = data[key]

    override fun contains(key: String): Boolean = data.containsKey(key)

    override fun keys(): Set<String> = data.keys.toSet()

    override fun set(key: String, value: String) {
        data[key] = value
    }

    override fun remove(key: String) {
        data.remove(key)
    }
}
