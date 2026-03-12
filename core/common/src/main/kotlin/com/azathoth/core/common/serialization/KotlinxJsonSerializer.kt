package com.azathoth.core.common.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

/**
 * 基于 kotlinx.serialization 的 JSON 序列化器
 */
class KotlinxJsonSerializer(
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
        isLenient = true
    }
) : JsonSerializer {

    override fun <T : Any> serialize(obj: T): ByteArray = toJson(obj).toByteArray(Charsets.UTF_8)

    override fun <T : Any> deserialize(bytes: ByteArray, clazz: KClass<T>): T =
        fromJson(bytes.toString(Charsets.UTF_8), clazz)

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> toJson(obj: T): String {
        val serializer = serializer(obj::class.java) as KSerializer<T>
        return json.encodeToString(serializer, obj)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> fromJson(json: String, clazz: KClass<T>): T {
        val serializer = serializer(clazz.java) as KSerializer<T>
        return this.json.decodeFromString(serializer, json)
    }
}
