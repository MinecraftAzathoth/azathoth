package com.azathoth.core.common.util

import kotlinx.coroutines.*
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration

/**
 * 基于 Kotlin 协程的任务调度器
 */
class CoroutineScheduler(
    private val scope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default + CoroutineName("azathoth-scheduler")
    )
) : Scheduler {

    override fun schedule(delay: Duration, task: suspend () -> Unit): ScheduledTask {
        val taskId = UUID.randomUUID().toString()
        val job = scope.launch {
            delay(delay)
            task()
        }
        return CoroutineScheduledTask(taskId, job)
    }

    override fun scheduleAtFixedRate(
        initialDelay: Duration,
        period: Duration,
        task: suspend () -> Unit
    ): ScheduledTask {
        val taskId = UUID.randomUUID().toString()
        val job = scope.launch {
            delay(initialDelay)
            while (isActive) {
                val start = System.currentTimeMillis()
                task()
                val elapsed = System.currentTimeMillis() - start
                val remaining = period.inWholeMilliseconds - elapsed
                if (remaining > 0) delay(remaining)
            }
        }
        return CoroutineScheduledTask(taskId, job)
    }

    override fun scheduleWithFixedDelay(
        initialDelay: Duration,
        delay: Duration,
        task: suspend () -> Unit
    ): ScheduledTask {
        val taskId = UUID.randomUUID().toString()
        val job = scope.launch {
            delay(initialDelay)
            while (isActive) {
                task()
                delay(delay)
            }
        }
        return CoroutineScheduledTask(taskId, job)
    }

    override suspend fun shutdown() {
        scope.cancel()
    }
}

/**
 * 基于协程 Job 的 ScheduledTask 实现
 */
private class CoroutineScheduledTask(
    override val id: String,
    private val job: Job
) : ScheduledTask {

    override val isCancelled: Boolean get() = job.isCancelled
    override val isDone: Boolean get() = job.isCompleted

    override fun cancel(): Boolean {
        if (job.isCompleted) return false
        job.cancel()
        return true
    }

    override fun asJob(): Job = job
}

/**
 * 基于 Kotlin 协程的异步执行器
 */
class CoroutineAsyncExecutor(
    private val scope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default + CoroutineName("azathoth-executor")
    )
) : AsyncExecutor {

    override fun <T> submit(task: suspend () -> T): AsyncTask<T> {
        val taskId = UUID.randomUUID().toString()
        val deferred = scope.async { task() }
        return CoroutineAsyncTask(taskId, deferred)
    }

    override fun <T> submitAll(tasks: List<suspend () -> T>): List<AsyncTask<T>> =
        tasks.map { submit(it) }

    override suspend fun shutdown() {
        scope.cancel()
    }
}

/**
 * 基于协程 Deferred 的 AsyncTask 实现
 */
private class CoroutineAsyncTask<T>(
    override val id: String,
    private val deferred: Deferred<T>
) : AsyncTask<T> {

    override val isDone: Boolean get() = deferred.isCompleted
    override val isCancelled: Boolean get() = deferred.isCancelled

    override suspend fun await(): T = deferred.await()

    override fun cancel(): Boolean {
        if (deferred.isCompleted) return false
        deferred.cancel()
        return true
    }
}
