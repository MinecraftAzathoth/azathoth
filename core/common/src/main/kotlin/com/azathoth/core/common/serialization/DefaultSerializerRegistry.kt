package com.azathoth.core.common.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import java.util.concurrent.ConcurrentHashMap

/**
 * 序列化器注册表实现
 */
class DefaultSerializerRegistry : SerializerRegistry {

    private val serializers = ConcurrentHashMap<String, Serializer>()

    @Volatile
    private var defaultFormat: String = "json"

    override fun register(format: String, serializer: Serializer) {
        serializers[format] = serializer
    }

    override fun get(format: String): Serializer? = serializers[format]

    override fun getDefault(): Serializer =
        serializers[defaultFormat]
            ?: throw IllegalStateException("默认序列化器 '$defaultFormat' 未注册")

    override fun setDefault(format: String) {
        require(serializers.containsKey(format)) { "序列化器 '$format' 未注册" }
        defaultFormat = format
    }

    companion object {
        /** 创建预配置的注册表（包含 JSON 和 Protobuf） */
        @OptIn(ExperimentalSerializationApi::class)
        fun withDefaults(): DefaultSerializerRegistry = DefaultSerializerRegistry().apply {
            register("json", KotlinxJsonSerializer())
            register("protobuf", KotlinxProtobufSerializer())
        }
    }
}
