package com.azathoth.core.common.util

import kotlinx.coroutines.Job
import kotlin.time.Duration

/**
 * 任务调度器接口
 */
interface Scheduler {
    /**
     * 延迟执行任务
     */
    fun schedule(delay: Duration, task: suspend () -> Unit): ScheduledTask
    
    /**
     * 周期性执行任务
     */
    fun scheduleAtFixedRate(
        initialDelay: Duration,
        period: Duration,
        task: suspend () -> Unit
    ): ScheduledTask
    
    /**
     * 固定延迟执行任务
     */
    fun scheduleWithFixedDelay(
        initialDelay: Duration,
        delay: Duration,
        task: suspend () -> Unit
    ): ScheduledTask
    
    /**
     * 关闭调度器
     */
    suspend fun shutdown()
}

/**
 * 已调度任务
 */
interface ScheduledTask {
    /** 任务ID */
    val id: String
    
    /** 是否已取消 */
    val isCancelled: Boolean
    
    /** 是否已完成 */
    val isDone: Boolean
    
    /** 取消任务 */
    fun cancel(): Boolean
    
    /** 获取关联的 Job */
    fun asJob(): Job
}

/**
 * 异步任务执行器
 */
interface AsyncExecutor {
    /**
     * 提交异步任务
     */
    fun <T> submit(task: suspend () -> T): AsyncTask<T>
    
    /**
     * 批量提交任务
     */
    fun <T> submitAll(tasks: List<suspend () -> T>): List<AsyncTask<T>>
    
    /**
     * 关闭执行器
     */
    suspend fun shutdown()
}

/**
 * 异步任务
 */
interface AsyncTask<T> {
    /** 任务ID */
    val id: String
    
    /** 是否已完成 */
    val isDone: Boolean
    
    /** 是否已取消 */
    val isCancelled: Boolean
    
    /** 等待结果 */
    suspend fun await(): T
    
    /** 取消任务 */
    fun cancel(): Boolean
}
