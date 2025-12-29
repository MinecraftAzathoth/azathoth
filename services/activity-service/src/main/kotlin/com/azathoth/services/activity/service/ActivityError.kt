package com.azathoth.services.activity.service

/**
 * 活动错误类型
 */
sealed class ActivityError(val message: String) {
    class ActivityNotFound(activityId: String) : ActivityError("Activity not found: $activityId")
    class ActivityNotActive(activityId: String) : ActivityError("Activity not active: $activityId")
    class ActivityEnded(activityId: String) : ActivityError("Activity has ended: $activityId")
    class AlreadyJoined(activityId: String) : ActivityError("Already joined activity: $activityId")
    class NotJoined(activityId: String) : ActivityError("Not joined activity: $activityId")
    class RewardAlreadyClaimed(rewardId: String) : ActivityError("Reward already claimed: $rewardId")
    class RewardConditionNotMet(rewardId: String) : ActivityError("Reward condition not met: $rewardId")
    class QuestNotFound(questId: String) : ActivityError("Quest not found: $questId")
    class QuestNotAvailable(questId: String) : ActivityError("Quest not available: $questId")
    class QuestAlreadyAccepted(questId: String) : ActivityError("Quest already accepted: $questId")
    class QuestNotInProgress(questId: String) : ActivityError("Quest not in progress: $questId")
    class QuestNotCompleted(questId: String) : ActivityError("Quest not completed: $questId")
    class PrerequisitesNotMet(questId: String) : ActivityError("Prerequisites not met: $questId")
    class AchievementNotFound(achievementId: String) : ActivityError("Achievement not found: $achievementId")
    class LevelTooLow(required: Int, actual: Int) : ActivityError("Level too low: required $required, actual $actual")
    class InternalError(cause: String) : ActivityError("Internal error: $cause")
}
