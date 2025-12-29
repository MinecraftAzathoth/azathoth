package com.azathoth.services.dungeon.service

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.Result
import com.azathoth.services.dungeon.model.MatchmakingRequest
import com.azathoth.services.dungeon.model.MatchmakingResult

/**
 * 匹配服务接口
 */
interface MatchmakingService {
    /**
     * 加入匹配队列
     */
    suspend fun joinQueue(request: MatchmakingRequest): Result<MatchmakingResult, DungeonError>

    /**
     * 离开匹配队列
     */
    suspend fun leaveQueue(playerId: PlayerId): Result<Unit, DungeonError>

    /**
     * 获取匹配状态
     */
    suspend fun getQueueStatus(playerId: PlayerId): Result<MatchmakingResult, DungeonError>

    /**
     * 确认匹配
     */
    suspend fun confirmMatch(playerId: PlayerId, matchId: String): Result<Unit, DungeonError>

    /**
     * 拒绝匹配
     */
    suspend fun declineMatch(playerId: PlayerId, matchId: String): Result<Unit, DungeonError>
}
