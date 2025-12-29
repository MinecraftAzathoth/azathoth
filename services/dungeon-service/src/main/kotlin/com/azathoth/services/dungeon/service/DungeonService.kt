package com.azathoth.services.dungeon.service

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.Result
import com.azathoth.services.dungeon.model.*

/**
 * 副本服务接口
 */
interface DungeonService {
    /**
     * 获取副本模板列表
     */
    suspend fun listTemplates(
        minLevel: Int? = null,
        maxLevel: Int? = null,
        difficulty: DungeonDifficulty? = null
    ): Result<List<DungeonTemplateInfo>>

    /**
     * 获取副本模板详情
     */
    suspend fun getTemplate(templateId: String): Result<DungeonTemplateInfo>

    /**
     * 创建副本实例
     */
    suspend fun createInstance(
        templateId: String,
        difficulty: DungeonDifficulty,
        leaderId: PlayerId,
        memberIds: List<PlayerId>
    ): Result<DungeonInstanceInfo>

    /**
     * 获取副本实例信息
     */
    suspend fun getInstance(instanceId: String): Result<DungeonInstanceInfo>

    /**
     * 加入副本
     */
    suspend fun joinInstance(instanceId: String, playerId: PlayerId): Result<JoinResult>

    /**
     * 离开副本
     */
    suspend fun leaveInstance(instanceId: String, playerId: PlayerId): Result<Unit>

    /**
     * 获取副本进度
     */
    suspend fun getProgress(instanceId: String): Result<DungeonProgress>

    /**
     * 完成副本
     */
    suspend fun completeInstance(instanceId: String, result: DungeonResult): Result<Unit>
}
