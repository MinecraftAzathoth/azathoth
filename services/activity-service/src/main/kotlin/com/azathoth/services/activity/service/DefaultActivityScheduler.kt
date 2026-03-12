package com.azathoth.services.activity.service

import com.azathoth.core.common.result.ErrorCodes
import com.azathoth.core.common.result.Result
import com.azathoth.services.activity.model.ActivityInfo
import com.azathoth.services.activity.model.ActivityState
import com.azathoth.services.activity.model.SimpleActivityInfo
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

class DefaultActivityScheduler(
    private val activityService: DefaultActivityService
) : ActivityScheduler {

    private val scheduledJobs = ConcurrentHashMap<String, Job>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override suspend fun scheduleActivityStart(activityId: String, startTime: Instant): Result<Unit> {
        val activity = activityService.getActivity(activityId).getOrNull()
            ?: return Result.failure(ErrorCodes.NOT_FOUND, "活动不存在: $activityId")

        val delay = Duration.between(Instant.now(), startTime).toMillis()
        if (delay < 0) {
            return Result.failure(ErrorCodes.INVALID_ARGUMENT, "开始时间已过")
        }

        val job = scope.launch {
            delay(delay)
            if (activity is SimpleActivityInfo) {
                activityService.registerActivity(activity.copy(state = ActivityState.ACTIVE))
                logger.info { "活动 $activityId 已自动开启" }
            }
        }
        scheduledJobs["start:$activityId"] = job
        logger.info { "调度活动 $activityId 在 $startTime 开始" }
        return Result.success(Unit)
    }

    override suspend fun scheduleActivityEnd(activityId: String, endTime: Instant): Result<Unit> {
        val activity = activityService.getActivity(activityId).getOrNull()
            ?: return Result.failure(ErrorCodes.NOT_FOUND, "活动不存在: $activityId")

        val delay = Duration.between(Instant.now(), endTime).toMillis()
        if (delay < 0) {
            return Result.failure(ErrorCodes.INVALID_ARGUMENT, "结束时间已过")
        }

        val job = scope.launch {
            delay(delay)
            if (activity is SimpleActivityInfo) {
                activityService.registerActivity(activity.copy(state = ActivityState.ENDED))
                logger.info { "活动 $activityId 已自动结束" }
            }
        }
        scheduledJobs["end:$activityId"] = job
        logger.info { "调度活动 $activityId 在 $endTime 结束" }
        return Result.success(Unit)
    }

    override suspend fun cancelSchedule(activityId: String): Result<Unit> {
        val startJob = scheduledJobs.remove("start:$activityId")
        val endJob = scheduledJobs.remove("end:$activityId")

        if (startJob == null && endJob == null) {
            return Result.failure(ErrorCodes.NOT_FOUND, "没有找到该活动的调度: $activityId")
        }

        startJob?.cancel()
        endJob?.cancel()
        logger.info { "取消活动 $activityId 的调度" }
        return Result.success(Unit)
    }

    override suspend fun getUpcomingActivities(within: Duration): Result<List<ActivityInfo>> {
        val now = Instant.now()
        val deadline = now.plus(within)
        val activities = activityService.listActivities(state = ActivityState.SCHEDULED).getOrNull()
            ?: emptyList()

        val upcoming = activities.filter { it.startTime.isBefore(deadline) && it.startTime.isAfter(now) }
        return Result.success(upcoming)
    }

    fun shutdown() {
        scope.cancel()
        logger.info { "活动调度器已关闭" }
    }
}
