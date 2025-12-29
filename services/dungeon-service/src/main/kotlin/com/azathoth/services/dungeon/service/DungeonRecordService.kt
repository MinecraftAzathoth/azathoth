package com.azathoth.services.dungeon.service

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.Result
import com.azathoth.services.dungeon.model.*

/**
 * 副本记录服务接口
 */
interface DungeonRecordService {
    /**
     * 获取玩家副本记录
     */
    suspend fun getPlayerRecords(
        playerId: PlayerId,
        templateId: String? = null,
        limit: Int = 20
    ): Result<List<DungeonRecord>, DungeonError>

    /**
     * 获取排行榜
     */
    suspend fun getLeaderboard(
        templateId: String,
        difficulty: DungeonDifficulty,
        type: LeaderboardType,
        limit: Int = 100
    ): Result<List<LeaderboardEntry>, DungeonError>

    /**
     * 获取玩家排名
     */
    suspend fun getPlayerRank(
        playerId: PlayerId,
        templateId: String,
        difficulty: DungeonDifficulty,
        type: LeaderboardType
    ): Result<LeaderboardEntry?, DungeonError>

    /**
     * 检查每日入场次数
     */
    suspend fun checkDailyEntries(
        playerId: PlayerId,
        templateId: String
    ): Result<EntryCheckResult, DungeonError>
}
