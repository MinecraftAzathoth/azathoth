package com.azathoth.services.activity.service

import com.azathoth.core.common.result.Result
import com.azathoth.services.activity.model.ActivityInfo
import java.time.Duration
import java.time.Instant

/**
 * 活动调度服务接口
 */
interface ActivityScheduler {
    /**
     * 调度活动开始
     */
    suspend fun scheduleActivityStart(activityId: String, startTime: Instant): Result<Unit>

    /**
     * 调度活动结束
     */
    suspend fun scheduleActivityEnd(activityId: String, endTime: Instant): Result<Unit>

    /**
     * 取消调度
     */
    suspend fun cancelSchedule(activityId: String): Result<Unit>

    /**
     * 获取即将开始的活动
     */
    suspend fun getUpcomingActivities(within: Duration): Result<List<ActivityInfo>>
}
