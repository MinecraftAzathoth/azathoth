package com.azathoth.game.dungeons.instance

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.game.dungeons.template.DungeonTemplate
import com.azathoth.game.engine.player.GamePlayer
import com.azathoth.game.engine.world.World
import kotlin.time.Duration

/**
 * 副本状态
 */
enum class DungeonState {
    /** 创建中 */
    CREATING,
    /** 等待玩家 */
    WAITING,
    /** 进行中 */
    IN_PROGRESS,
    /** 已完成 */
    COMPLETED,
    /** 失败 */
    FAILED,
    /** 关闭中 */
    CLOSING,
    /** 已关闭 */
    CLOSED
}

/**
 * 副本难度
 */
enum class DungeonDifficulty {
    NORMAL,
    HARD,
    NIGHTMARE,
    HELL
}

/**
 * 副本实例
 */
interface DungeonInstance {
    /** 实例ID */
    val instanceId: String
    
    /** 副本模板 */
    val template: DungeonTemplate
    
    /** 副本世界 */
    val world: World
    
    /** 副本状态 */
    val state: DungeonState
    
    /** 难度 */
    val difficulty: DungeonDifficulty
    
    /** 创建时间 */
    val createdAt: Long
    
    /** 开始时间 */
    val startedAt: Long?
    
    /** 已用时间 */
    val elapsedTime: Duration
    
    /** 剩余时间 */
    val remainingTime: Duration?
    
    /** 当前玩家列表 */
    fun getPlayers(): Collection<GamePlayer>
    
    /** 玩家数量 */
    val playerCount: Int
    
    /** 最大玩家数 */
    val maxPlayers: Int
    
    /** 当前阶段 */
    val currentPhase: DungeonPhase?
    
    /** 当前进度 */
    val progress: DungeonProgress
    
    /**
     * 添加玩家
     */
    suspend fun addPlayer(player: GamePlayer): Boolean
    
    /**
     * 移除玩家
     */
    suspend fun removePlayer(player: GamePlayer, reason: String = "")
    
    /**
     * 开始副本
     */
    suspend fun start()
    
    /**
     * 完成副本
     */
    suspend fun complete()
    
    /**
     * 副本失败
     */
    suspend fun fail(reason: String)
    
    /**
     * 关闭副本
     */
    suspend fun close()
    
    /**
     * 重置副本
     */
    suspend fun reset()
    
    /**
     * 推进到下一阶段
     */
    suspend fun advancePhase()
}

/**
 * 副本阶段
 */
interface DungeonPhase {
    /** 阶段ID */
    val phaseId: String
    
    /** 阶段名称 */
    val name: String
    
    /** 阶段顺序 */
    val order: Int
    
    /** 是否已完成 */
    val isCompleted: Boolean
    
    /** 阶段目标 */
    val objectives: List<DungeonObjective>
}

/**
 * 副本目标
 */
interface DungeonObjective {
    /** 目标ID */
    val objectiveId: String
    
    /** 目标描述 */
    val description: String
    
    /** 是否已完成 */
    val isCompleted: Boolean
    
    /** 当前进度 */
    val currentProgress: Int
    
    /** 目标进度 */
    val targetProgress: Int
    
    /** 进度百分比 */
    val progressPercent: Double get() = 
        if (targetProgress == 0) 100.0 else currentProgress.toDouble() / targetProgress * 100
}

/**
 * 副本进度
 */
interface DungeonProgress {
    /** 击杀怪物数 */
    val monstersKilled: Int
    
    /** Boss 击杀数 */
    val bossesKilled: Int
    
    /** 完成的阶段数 */
    val phasesCompleted: Int
    
    /** 总阶段数 */
    val totalPhases: Int
    
    /** 获得的分数 */
    val score: Int
    
    /** 死亡次数 */
    val deaths: Int
    
    /** 评级 */
    val rating: DungeonRating?
}

/**
 * 副本评级
 */
enum class DungeonRating {
    S,
    A,
    B,
    C,
    D,
    F
}

/**
 * 副本实例管理器
 */
interface DungeonInstanceManager {
    /**
     * 创建副本实例
     */
    suspend fun createInstance(
        template: DungeonTemplate,
        difficulty: DungeonDifficulty,
        leader: GamePlayer
    ): DungeonInstance
    
    /**
     * 获取实例
     */
    fun getInstance(instanceId: String): DungeonInstance?
    
    /**
     * 获取玩家所在的副本
     */
    fun getPlayerInstance(playerId: PlayerId): DungeonInstance?
    
    /**
     * 获取所有活跃实例
     */
    fun getActiveInstances(): Collection<DungeonInstance>
    
    /**
     * 关闭实例
     */
    suspend fun closeInstance(instanceId: String)
    
    /**
     * 清理过期实例
     */
    suspend fun cleanupExpiredInstances()
}
