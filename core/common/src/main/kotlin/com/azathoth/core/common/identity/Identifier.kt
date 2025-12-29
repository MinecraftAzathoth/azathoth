package com.azathoth.core.common.identity

import java.util.UUID

/**
 * 标识符接口
 */
interface Identifier {
    /** 标识符值 */
    val value: String
    
    /** 是否为空 */
    val isEmpty: Boolean get() = value.isEmpty()
}

/**
 * UUID 标识符
 */
@JvmInline
value class UuidIdentifier(override val value: String) : Identifier {
    constructor(uuid: UUID) : this(uuid.toString())
    
    /** 转换为 UUID */
    fun toUuid(): UUID = UUID.fromString(value)
    
    companion object {
        /** 生成随机 UUID 标识符 */
        fun random(): UuidIdentifier = UuidIdentifier(UUID.randomUUID())
        
        /** 空标识符 */
        val EMPTY = UuidIdentifier("")
    }
}

/**
 * 玩家标识符
 */
@JvmInline
value class PlayerId(override val value: String) : Identifier {
    companion object {
        fun of(uuid: UUID): PlayerId = PlayerId(uuid.toString())
        fun of(value: String): PlayerId = PlayerId(value)
    }
}

/**
 * 实例标识符
 */
@JvmInline
value class InstanceId(override val value: String) : Identifier {
    companion object {
        fun of(value: String): InstanceId = InstanceId(value)
        fun generate(): InstanceId = InstanceId(UUID.randomUUID().toString())
    }
}

/**
 * 网关标识符
 */
@JvmInline
value class GatewayId(override val value: String) : Identifier {
    companion object {
        fun of(value: String): GatewayId = GatewayId(value)
        fun generate(): GatewayId = GatewayId(UUID.randomUUID().toString())
    }
}

/**
 * 世界标识符
 */
@JvmInline
value class WorldId(override val value: String) : Identifier {
    companion object {
        fun of(value: String): WorldId = WorldId(value)
    }
}

/**
 * 插件标识符
 */
@JvmInline
value class PluginId(override val value: String) : Identifier {
    companion object {
        fun of(value: String): PluginId = PluginId(value)
    }
}
