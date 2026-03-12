package com.azathoth.core.common.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

/**
 * 基于 kotlinx.serialization 的 Protobuf 序列化器
 */
@OptIn(ExperimentalSerializationApi::class)
class KotlinxProtobufSerializer(
    private val protobuf: ProtoBuf = ProtoBuf {
        encodeDefaults = true
    }
) : ProtobufSerializer {

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> serialize(obj: T): ByteArray {
        val serializer = serializer(obj::class.java) as KSerializer<T>
        return protobuf.encodeToByteArray(serializer, obj)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> deserialize(bytes: ByteArray, clazz: KClass<T>): T {
        val serializer = serializer(clazz.java) as KSerializer<T>
        return protobuf.decodeFromByteArray(serializer, bytes)
    }
}
