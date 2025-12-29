package com.azathoth.core.common.config

import kotlin.reflect.KClass

/**
 * 配置源接口
 */
interface ConfigurationSource {
    /** 配置源名称 */
    val name: String
    
    /** 优先级（数值越小优先级越高） */
    val priority: Int
    
    /** 获取原始配置值 */
    fun getRaw(key: String): String?
    
    /** 是否包含指定键 */
    fun contains(key: String): Boolean
    
    /** 获取所有键 */
    fun keys(): Set<String>
}

/**
 * 可变配置源
 */
interface MutableConfigurationSource : ConfigurationSource {
    /** 设置配置值 */
    fun set(key: String, value: String)
    
    /** 移除配置值 */
    fun remove(key: String)
}

/**
 * 配置接口
 */
interface Configuration {
    /** 获取字符串值 */
    fun getString(key: String, default: String? = null): String?
    
    /** 获取整数值 */
    fun getInt(key: String, default: Int = 0): Int
    
    /** 获取长整数值 */
    fun getLong(key: String, default: Long = 0L): Long
    
    /** 获取双精度值 */
    fun getDouble(key: String, default: Double = 0.0): Double
    
    /** 获取布尔值 */
    fun getBoolean(key: String, default: Boolean = false): Boolean
    
    /** 获取字符串列表 */
    fun getStringList(key: String): List<String>
    
    /** 获取子配置 */
    fun getSection(key: String): Configuration?
    
    /** 获取所有键 */
    fun keys(): Set<String>
    
    /** 是否包含指定键 */
    fun contains(key: String): Boolean
}

/**
 * 可变配置
 */
interface MutableConfiguration : Configuration {
    /** 设置值 */
    fun set(key: String, value: Any?)
    
    /** 移除值 */
    fun remove(key: String)
    
    /** 保存配置 */
    suspend fun save()
}

/**
 * 配置加载器
 */
interface ConfigurationLoader<T : Configuration> {
    /** 从路径加载配置 */
    suspend fun load(path: String): T
    
    /** 保存配置到路径 */
    suspend fun save(config: T, path: String)
}

/**
 * 配置绑定接口 - 将配置绑定到数据类
 */
interface ConfigurationBinder {
    /** 绑定配置到指定类型 */
    fun <T : Any> bind(config: Configuration, clazz: KClass<T>): T
    
    /** 从对象生成配置 */
    fun <T : Any> unbind(obj: T): Configuration
}
