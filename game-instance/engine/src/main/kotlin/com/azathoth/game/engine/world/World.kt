package com.azathoth.game.engine.world

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.identity.WorldId
import com.azathoth.game.engine.entity.Entity
import com.azathoth.game.engine.player.GamePlayer

/**
 * 世界类型
 */
enum class WorldType {
    /** 主世界 */
    OVERWORLD,
    /** 副本世界 */
    INSTANCE,
    /** 竞技场 */
    ARENA,
    /** 测试世界 */
    TEST
}

/**
 * 区块位置
 */
data class ChunkPosition(val x: Int, val z: Int)

/**
 * 方块位置
 */
data class BlockPosition(val x: Int, val y: Int, val z: Int) {
    fun toChunkPosition(): ChunkPosition = ChunkPosition(x shr 4, z shr 4)
}

/**
 * 世界位置
 */
data class WorldPosition(
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float = 0f,
    val pitch: Float = 0f
) {
    fun toBlockPosition(): BlockPosition = BlockPosition(x.toInt(), y.toInt(), z.toInt())
}

/**
 * 世界接口
 */
interface World {
    /** 世界ID */
    val worldId: WorldId
    
    /** 世界名称 */
    val name: String
    
    /** 世界类型 */
    val type: WorldType
    
    /** 出生点 */
    val spawnPosition: WorldPosition
    
    /** 是否已加载 */
    val isLoaded: Boolean
    
    /** 世界时间 */
    var time: Long
    
    /** 天气状态 */
    var weather: Weather
    
    /** 获取已加载的区块数量 */
    val loadedChunkCount: Int
    
    /** 获取世界中的玩家 */
    fun getPlayers(): Collection<GamePlayer>
    
    /** 获取世界中的实体 */
    fun getEntities(): Collection<Entity>
    
    /** 通过ID获取实体 */
    fun getEntity(entityId: Int): Entity?
    
    /** 加载区块 */
    suspend fun loadChunk(position: ChunkPosition): Chunk
    
    /** 卸载区块 */
    suspend fun unloadChunk(position: ChunkPosition)
    
    /** 获取区块 */
    fun getChunk(position: ChunkPosition): Chunk?
    
    /** 检查区块是否已加载 */
    fun isChunkLoaded(position: ChunkPosition): Boolean
    
    /** 生成实体 */
    suspend fun spawnEntity(entity: Entity, position: WorldPosition)
    
    /** 移除实体 */
    suspend fun removeEntity(entity: Entity)
    
    /** 播放音效 */
    suspend fun playSound(sound: String, position: WorldPosition, volume: Float, pitch: Float)
    
    /** 播放粒子效果 */
    suspend fun spawnParticle(particle: String, position: WorldPosition, count: Int)
}

/**
 * 天气状态
 */
enum class Weather {
    CLEAR,
    RAIN,
    THUNDER
}

/**
 * 区块接口
 */
interface Chunk {
    /** 区块位置 */
    val position: ChunkPosition
    
    /** 所属世界 */
    val world: World
    
    /** 是否已加载 */
    val isLoaded: Boolean
    
    /** 获取方块 */
    fun getBlock(x: Int, y: Int, z: Int): Block
    
    /** 设置方块 */
    suspend fun setBlock(x: Int, y: Int, z: Int, block: Block)
    
    /** 获取区块内的实体 */
    fun getEntities(): Collection<Entity>
}

/**
 * 方块接口
 */
interface Block {
    /** 方块类型 */
    val type: String
    
    /** 方块位置 */
    val position: BlockPosition
    
    /** 方块数据 */
    val data: Map<String, Any>
}

/**
 * 世界管理器
 */
interface WorldManager {
    /** 创建世界 */
    suspend fun createWorld(name: String, type: WorldType, config: WorldConfig = WorldConfig.DEFAULT): World
    
    /** 加载世界 */
    suspend fun loadWorld(worldId: WorldId): World?
    
    /** 卸载世界 */
    suspend fun unloadWorld(worldId: WorldId)
    
    /** 删除世界 */
    suspend fun deleteWorld(worldId: WorldId)
    
    /** 获取世界 */
    fun getWorld(worldId: WorldId): World?
    
    /** 获取世界（按名称） */
    fun getWorldByName(name: String): World?
    
    /** 获取所有世界 */
    fun getWorlds(): Collection<World>
    
    /** 获取默认世界 */
    fun getDefaultWorld(): World
}

/**
 * 世界配置
 */
interface WorldConfig {
    /** 种子 */
    val seed: Long
    
    /** 生成器类型 */
    val generator: String
    
    /** 难度 */
    val difficulty: Int
    
    /** 是否启用 PVP */
    val pvpEnabled: Boolean
    
    /** 最大玩家数 */
    val maxPlayers: Int
    
    companion object {
        val DEFAULT: WorldConfig get() = object : WorldConfig {
            override val seed: Long = System.currentTimeMillis()
            override val generator: String = "default"
            override val difficulty: Int = 2
            override val pvpEnabled: Boolean = true
            override val maxPlayers: Int = 100
        }
    }
}
