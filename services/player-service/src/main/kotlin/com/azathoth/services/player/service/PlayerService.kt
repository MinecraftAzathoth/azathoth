package com.azathoth.services.player.service

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.Result
import com.azathoth.services.player.repository.PlayerEntity
import com.azathoth.services.player.repository.PlayerStats

/**
 * 玩家服务接口
 */
interface PlayerService {
    /**
     * 获取或创建玩家
     */
    suspend fun getOrCreate(playerId: PlayerId, username: String): Result<PlayerEntity>
    
    /**
     * 获取玩家信息
     */
    suspend fun getPlayer(playerId: PlayerId): Result<PlayerEntity>
    
    /**
     * 更新玩家信息
     */
    suspend fun updatePlayer(player: PlayerEntity): Result<PlayerEntity>
    
    /**
     * 玩家上线处理
     */
    suspend fun onPlayerJoin(playerId: PlayerId)
    
    /**
     * 玩家下线处理
     */
    suspend fun onPlayerLeave(playerId: PlayerId)
    
    /**
     * 增加经验
     */
    suspend fun addExperience(playerId: PlayerId, amount: Long): Result<LevelUpResult>
    
    /**
     * 增加金币
     */
    suspend fun addGold(playerId: PlayerId, amount: Long, reason: String): Result<Long>
    
    /**
     * 扣除金币
     */
    suspend fun deductGold(playerId: PlayerId, amount: Long, reason: String): Result<Long>
    
    /**
     * 增加钻石
     */
    suspend fun addDiamond(playerId: PlayerId, amount: Long, reason: String): Result<Long>
    
    /**
     * 扣除钻石
     */
    suspend fun deductDiamond(playerId: PlayerId, amount: Long, reason: String): Result<Long>
    
    /**
     * 封禁玩家
     */
    suspend fun banPlayer(playerId: PlayerId, reason: String, durationSeconds: Long?): Result<Unit>
    
    /**
     * 解封玩家
     */
    suspend fun unbanPlayer(playerId: PlayerId): Result<Unit>
    
    /**
     * 检查是否被封禁
     */
    suspend fun isBanned(playerId: PlayerId): Boolean
    
    /**
     * 获取玩家统计
     */
    suspend fun getStats(playerId: PlayerId): Result<PlayerStats>
    
    /**
     * 更新统计数据
     */
    suspend fun updateStats(playerId: PlayerId, updater: (PlayerStats) -> Unit): Result<PlayerStats>
}

/**
 * 升级结果
 */
interface LevelUpResult {
    /** 是否升级 */
    val leveledUp: Boolean
    
    /** 新等级 */
    val newLevel: Int
    
    /** 当前经验 */
    val currentExperience: Long
    
    /** 升级所需经验 */
    val experienceToNextLevel: Long
    
    /** 升级次数 */
    val levelsGained: Int
}

/**
 * 背包服务接口
 */
interface InventoryService {
    /**
     * 获取玩家背包
     */
    suspend fun getInventory(playerId: PlayerId): Result<InventoryData>
    
    /**
     * 添加物品
     */
    suspend fun addItem(playerId: PlayerId, itemId: String, amount: Int): Result<Unit>
    
    /**
     * 移除物品
     */
    suspend fun removeItem(playerId: PlayerId, itemId: String, amount: Int): Result<Unit>
    
    /**
     * 检查是否有足够物品
     */
    suspend fun hasItem(playerId: PlayerId, itemId: String, amount: Int): Boolean
    
    /**
     * 获取物品数量
     */
    suspend fun getItemCount(playerId: PlayerId, itemId: String): Int
    
    /**
     * 清空背包
     */
    suspend fun clearInventory(playerId: PlayerId): Result<Unit>
}

/**
 * 背包数据
 */
interface InventoryData {
    /** 玩家ID */
    val playerId: PlayerId
    
    /** 物品列表 */
    val items: List<InventoryItem>
    
    /** 背包容量 */
    val capacity: Int
    
    /** 已使用格子数 */
    val usedSlots: Int
}

/**
 * 背包物品
 */
interface InventoryItem {
    /** 槽位 */
    val slot: Int
    
    /** 物品ID */
    val itemId: String
    
    /** 数量 */
    val amount: Int
    
    /** 物品数据 */
    val data: Map<String, Any>
}
