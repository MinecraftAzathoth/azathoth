package com.azathoth.services.dungeon.service

import com.azathoth.core.common.identity.PlayerId

/**
 * 副本错误类型
 */
sealed class DungeonError(val message: String) {
    class TemplateNotFound(templateId: String) : DungeonError("Template not found: $templateId")
    class InstanceNotFound(instanceId: String) : DungeonError("Instance not found: $instanceId")
    class InstanceFull(instanceId: String) : DungeonError("Instance is full: $instanceId")
    class InstanceClosed(instanceId: String) : DungeonError("Instance is closed: $instanceId")
    class PlayerAlreadyInInstance(playerId: PlayerId) : DungeonError("Player already in instance: $playerId")
    class PlayerNotInInstance(playerId: PlayerId) : DungeonError("Player not in instance: $playerId")
    class LevelTooLow(required: Int, actual: Int) : DungeonError("Level too low: required $required, actual $actual")
    class DailyLimitReached(templateId: String) : DungeonError("Daily entry limit reached: $templateId")
    class WeeklyLimitReached(templateId: String) : DungeonError("Weekly entry limit reached: $templateId")
    class MatchmakingFailed(reason: String) : DungeonError("Matchmaking failed: $reason")
    class AlreadyInQueue(playerId: PlayerId) : DungeonError("Player already in queue: $playerId")
    class NotInQueue(playerId: PlayerId) : DungeonError("Player not in queue: $playerId")
    class MatchExpired(matchId: String) : DungeonError("Match expired: $matchId")
    class InternalError(cause: String) : DungeonError("Internal error: $cause")
}
