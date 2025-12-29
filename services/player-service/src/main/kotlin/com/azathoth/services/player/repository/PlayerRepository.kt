package com.azathoth.services.player.repository

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.Result

/**
 * 玩家数据实体
 */
interface PlayerEntity {
    /** 玩家ID */
    val playerId: PlayerId
    
    /** 用户名 */
    val username: String
    
    /** 显示名称 */
    var displayName: String
    
    /** 等级 */
    var level: Int
    
    /** 经验值 */
    var experience: Long
    
    /** 金币 */
    var gold: Long
    
    /** 钻石 */
    var diamond: Long
    
    /** VIP 等级 */
    var vipLevel: Int
    
    /** 首次登录时间 */
    val firstLoginAt: Long
    
    /** 最后登录时间 */
    var lastLoginAt: Long
    
    /** 总在线时长（秒） */
    var totalOnlineTime: Long
    
    /** 是否被封禁 */
    var isBanned: Boolean
    
    /** 封禁原因 */
    var banReason: String?
    
    /** 封禁到期时间 */
    var banExpireAt: Long?
    
    /** 创建时间 */
    val createdAt: Long
    
    /** 更新时间 */
    var updatedAt: Long
}

/**
 * 玩家统计数据
 */
interface PlayerStats {
    /** 玩家ID */
    val playerId: PlayerId
    
    /** 击杀怪物数 */
    var mobsKilled: Long
    
    /** 击杀玩家数 */
    var playersKilled: Long
    
    /** 死亡次数 */
    var deaths: Long
    
    /** 完成副本数 */
    var dungeonsCompleted: Long
    
    /** 完成任务数 */
    var questsCompleted: Long
    
    /** 获得成就数 */
    var achievementsUnlocked: Int
    
    /** 行走距离 */
    var distanceTraveled: Long
    
    /** 方块挖掘数 */
    var blocksMined: Long
    
    /** 方块放置数 */
    var blocksPlaced: Long
}

/**
 * 玩家仓库接口
 */
interface PlayerRepository {
    /**
     * 创建玩家
     */
    suspend fun create(playerId: PlayerId, username: String): Result<PlayerEntity>
    
    /**
     * 根据ID查找
     */
    suspend fun findById(playerId: PlayerId): PlayerEntity?
    
    /**
     * 根据用户名查找
     */
    suspend fun findByUsername(username: String): PlayerEntity?
    
    /**
     * 保存玩家数据
     */
    suspend fun save(player: PlayerEntity): Result<PlayerEntity>
    
    /**
     * 删除玩家
     */
    suspend fun delete(playerId: PlayerId): Result<Unit>
    
    /**
     * 检查玩家是否存在
     */
    suspend fun exists(playerId: PlayerId): Boolean
    
    /**
     * 检查用户名是否存在
     */
    suspend fun existsByUsername(username: String): Boolean
    
    /**
     * 获取玩家统计
     */
    suspend fun getStats(playerId: PlayerId): PlayerStats?
    
    /**
     * 保存玩家统计
     */
    suspend fun saveStats(stats: PlayerStats): Result<PlayerStats>
    
    /**
     * 更新最后登录时间
     */
    suspend fun updateLastLogin(playerId: PlayerId)
    
    /**
     * 增加在线时长
     */
    suspend fun addOnlineTime(playerId: PlayerId, seconds: Long)
    
    /**
     * 搜索玩家
     */
    suspend fun search(keyword: String, limit: Int = 20): List<PlayerEntity>
    
    /**
     * 获取排行榜
     */
    suspend fun getLeaderboard(type: LeaderboardType, limit: Int = 100): List<PlayerEntity>
}

/**
 * 排行榜类型
 */
enum class LeaderboardType {
    LEVEL,
    GOLD,
    PVP_KILLS,
    DUNGEONS_COMPLETED,
    ACHIEVEMENTS
}
