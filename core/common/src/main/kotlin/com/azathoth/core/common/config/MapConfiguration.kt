package com.azathoth.core.common.config

/**
 * 基于 Map 的配置实现
 */
open class MapConfiguration(
    private val data: MutableMap<String, Any?> = mutableMapOf()
) : MutableConfiguration {

    override fun getString(key: String, default: String?): String? =
        resolve(key)?.toString() ?: default

    override fun getInt(key: String, default: Int): Int =
        resolve(key)?.let { toInt(it) } ?: default

    override fun getLong(key: String, default: Long): Long =
        resolve(key)?.let { toLong(it) } ?: default

    override fun getDouble(key: String, default: Double): Double =
        resolve(key)?.let { toDouble(it) } ?: default

    override fun getBoolean(key: String, default: Boolean): Boolean =
        resolve(key)?.let { toBoolean(it) } ?: default

    @Suppress("UNCHECKED_CAST")
    override fun getStringList(key: String): List<String> =
        (resolve(key) as? List<*>)?.map { it.toString() } ?: emptyList()

    @Suppress("UNCHECKED_CAST")
    override fun getSection(key: String): Configuration? {
        val value = resolve(key)
        return when (value) {
            is Map<*, *> -> MapConfiguration((value as Map<String, Any?>).toMutableMap())
            else -> null
        }
    }

    override fun keys(): Set<String> = data.keys.toSet()

    override fun contains(key: String): Boolean = resolve(key) != null

    override fun set(key: String, value: Any?) {
        val parts = key.split(".")
        if (parts.size == 1) {
            data[key] = value
            return
        }
        var current = data
        for (i in 0 until parts.size - 1) {
            @Suppress("UNCHECKED_CAST")
            current = current.getOrPut(parts[i]) { mutableMapOf<String, Any?>() } as MutableMap<String, Any?>
        }
        current[parts.last()] = value
    }

    override fun remove(key: String) {
        val parts = key.split(".")
        if (parts.size == 1) {
            data.remove(key)
            return
        }
        var current: MutableMap<String, Any?> = data
        for (i in 0 until parts.size - 1) {
            @Suppress("UNCHECKED_CAST")
            current = (current[parts[i]] as? MutableMap<String, Any?>) ?: return
        }
        current.remove(parts.last())
    }

    override suspend fun save() {
        // MapConfiguration 不持久化，子类可覆盖
    }

    /** 获取底层数据的只读副本 */
    fun toMap(): Map<String, Any?> = data.toMap()

    // --- 内部工具 ---

    @Suppress("UNCHECKED_CAST")
    private fun resolve(key: String): Any? {
        // 先尝试直接查找
        data[key]?.let { return it }
        // 再尝试点号分隔的嵌套查找
        val parts = key.split(".")
        var current: Any? = data
        for (part in parts) {
            current = (current as? Map<String, Any?>)?.get(part) ?: return null
        }
        return current
    }

    private fun toInt(v: Any): Int = when (v) {
        is Number -> v.toInt()
        is String -> v.toInt()
        else -> throw IllegalArgumentException("Cannot convert $v to Int")
    }

    private fun toLong(v: Any): Long = when (v) {
        is Number -> v.toLong()
        is String -> v.toLong()
        else -> throw IllegalArgumentException("Cannot convert $v to Long")
    }

    private fun toDouble(v: Any): Double = when (v) {
        is Number -> v.toDouble()
        is String -> v.toDouble()
        else -> throw IllegalArgumentException("Cannot convert $v to Double")
    }

    private fun toBoolean(v: Any): Boolean = when (v) {
        is Boolean -> v
        is String -> v.toBooleanStrict()
        else -> throw IllegalArgumentException("Cannot convert $v to Boolean")
    }
}
