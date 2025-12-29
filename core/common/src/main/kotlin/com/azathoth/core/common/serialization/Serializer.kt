package com.azathoth.core.common.serialization

import kotlin.reflect.KClass

/**
 * 序列化器接口
 */
interface Serializer {
    /** 序列化格式名称 */
    val format: String
    
    /** 序列化对象为字节数组 */
    fun <T : Any> serialize(obj: T): ByteArray
    
    /** 反序列化字节数组为对象 */
    fun <T : Any> deserialize(bytes: ByteArray, clazz: KClass<T>): T
}

/**
 * JSON 序列化器
 */
interface JsonSerializer : Serializer {
    override val format: String get() = "json"
    
    /** 序列化为 JSON 字符串 */
    fun <T : Any> toJson(obj: T): String
    
    /** 从 JSON 字符串反序列化 */
    fun <T : Any> fromJson(json: String, clazz: KClass<T>): T
}

/**
 * Protobuf 序列化器
 */
interface ProtobufSerializer : Serializer {
    override val format: String get() = "protobuf"
}

/**
 * 序列化器注册表
 */
interface SerializerRegistry {
    /** 注册序列化器 */
    fun register(format: String, serializer: Serializer)
    
    /** 获取序列化器 */
    fun get(format: String): Serializer?
    
    /** 获取默认序列化器 */
    fun getDefault(): Serializer
    
    /** 设置默认序列化器 */
    fun setDefault(format: String)
}
