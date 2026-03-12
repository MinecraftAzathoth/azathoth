package com.azathoth.game.engine.world

import com.azathoth.core.common.identity.WorldId
import com.azathoth.game.engine.entity.Entity
import com.azathoth.game.engine.player.GamePlayer
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

/**
 * 简单方块实现
 */
data class SimpleBlock(
    override val type: String,
    override val position: BlockPosition,
    override val data: Map<String, Any> = emptyMap()
) : Block

/**
 * 默认区块实现
 */
class DefaultChunk(
    override val position: ChunkPosition,
    override val world: World
) : Chunk {
    @Volatile
    override var isLoaded: Boolean = true

    private val blocks = ConcurrentHashMap<Long, Block>()
    private val entities = ConcurrentHashMap<Int, Entity>()

    private fun blockKey(x: Int, y: Int, z: Int): Long =
        (x.toLong() and 0xF) or ((z.toLong() and 0xF) shl 4) or ((y.toLong() and 0xFFFF) shl 8)

    override fun getBlock(x: Int, y: Int, z: Int): Block {
        val key = blockKey(x, y, z)
        return blocks[key] ?: SimpleBlock(
            type = "air",
            position = BlockPosition(
                position.x * 16 + x,
                y,
                position.z * 16 + z
            )
        )
    }

    override suspend fun setBlock(x: Int, y: Int, z: Int, block: Block) {
        blocks[blockKey(x, y, z)] = block
    }

    override fun getEntities(): Collection<Entity> = entities.values

    fun addEntity(entity: Entity) {
        entities[entity.entityId] = entity
    }

    fun removeEntity(entityId: Int) {
        entities.remove(entityId)
    }
}

/**
 * 默认世界实现
 */
class DefaultWorld(
    override val worldId: WorldId,
    override val name: String,
    override val type: WorldType,
    private val config: WorldConfig
) : World {
    override val spawnPosition: WorldPosition = WorldPosition(0.0, 64.0, 0.0)

    @Volatile
    override var isLoaded: Boolean = true

    override var time: Long = 0L
    override var weather: Weather = Weather.CLEAR

    private val chunks = ConcurrentHashMap<ChunkPosition, DefaultChunk>()
    private val entities = ConcurrentHashMap<Int, Entity>()
    private val players = ConcurrentHashMap<Int, GamePlayer>()

    override val loadedChunkCount: Int get() = chunks.size

    override fun getPlayers(): Collection<GamePlayer> = players.values

    override fun getEntities(): Collection<Entity> = entities.values

    override fun getEntity(entityId: Int): Entity? = entities[entityId]

    override suspend fun loadChunk(position: ChunkPosition): Chunk {
        return chunks.getOrPut(position) {
            DefaultChunk(position, this).also {
                logger.debug { "加载区块 $position 于世界 $name" }
            }
        }
    }

    override suspend fun unloadChunk(position: ChunkPosition) {
        chunks.remove(position)?.let { chunk ->
            (chunk as DefaultChunk).isLoaded = false
            logger.debug { "卸载区块 $position 于世界 $name" }
        }
    }

    override fun getChunk(position: ChunkPosition): Chunk? = chunks[position]

    override fun isChunkLoaded(position: ChunkPosition): Boolean = chunks.containsKey(position)

    override suspend fun spawnEntity(entity: Entity, position: WorldPosition) {
        entities[entity.entityId] = entity
        if (entity is GamePlayer) {
            players[entity.entityId] = entity
        }
        val chunkPos = position.toBlockPosition().toChunkPosition()
        val chunk = loadChunk(chunkPos) as DefaultChunk
        chunk.addEntity(entity)
        logger.debug { "生成实体 ${entity.entityId} 于世界 $name 位置 $position" }
    }

    override suspend fun removeEntity(entity: Entity) {
        entities.remove(entity.entityId)
        if (entity is GamePlayer) {
            players.remove(entity.entityId)
        }
        val chunkPos = entity.position.toBlockPosition().toChunkPosition()
        (chunks[chunkPos] as? DefaultChunk)?.removeEntity(entity.entityId)
    }

    override suspend fun playSound(sound: String, position: WorldPosition, volume: Float, pitch: Float) {
        logger.debug { "播放音效 $sound 于 $position" }
    }

    override suspend fun spawnParticle(particle: String, position: WorldPosition, count: Int) {
        logger.debug { "生成粒子 $particle x$count 于 $position" }
    }
}

/**
 * 默认世界管理器实现
 */
class DefaultWorldManager : WorldManager {
    private val worlds = ConcurrentHashMap<WorldId, DefaultWorld>()
    private val worldsByName = ConcurrentHashMap<String, DefaultWorld>()

    @Volatile
    private var defaultWorld: DefaultWorld? = null

    override suspend fun createWorld(name: String, type: WorldType, config: WorldConfig): World {
        val worldId = WorldId.of(UUID.randomUUID().toString())
        val world = DefaultWorld(worldId, name, type, config)
        worlds[worldId] = world
        worldsByName[name] = world
        if (defaultWorld == null) {
            defaultWorld = world
        }
        logger.info { "创建世界: $name (${worldId.value}), 类型: $type" }
        return world
    }

    override suspend fun loadWorld(worldId: WorldId): World? {
        return worlds[worldId]?.also { it.isLoaded = true }
    }

    override suspend fun unloadWorld(worldId: WorldId) {
        worlds[worldId]?.let { world ->
            world.isLoaded = false
            logger.info { "卸载世界: ${world.name}" }
        }
    }

    override suspend fun deleteWorld(worldId: WorldId) {
        worlds.remove(worldId)?.let { world ->
            worldsByName.remove(world.name)
            world.isLoaded = false
            if (defaultWorld?.worldId == worldId) {
                defaultWorld = worlds.values.firstOrNull()
            }
            logger.info { "删除世界: ${world.name}" }
        }
    }

    override fun getWorld(worldId: WorldId): World? = worlds[worldId]

    override fun getWorldByName(name: String): World? = worldsByName[name]

    override fun getWorlds(): Collection<World> = worlds.values

    override fun getDefaultWorld(): World {
        return defaultWorld ?: throw IllegalStateException("没有可用的默认世界")
    }
}
