package com.azathoth.game.engine.tick

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

private val logger = KotlinLogging.logger {}

/**
 * 默认 Tick 任务实现
 */
class DefaultTickTask(
    override val taskId: Int,
    override val isSync: Boolean,
    private val delay: Long,
    private val period: Long,
    private val syncAction: (() -> Unit)? = null,
    private val asyncAction: (suspend () -> Unit)? = null
) : TickTask {
    @Volatile
    override var isCancelled: Boolean = false

    var nextExecutionTick: Long = 0L

    override fun cancel() {
        isCancelled = true
    }

    fun shouldExecute(currentTick: Long): Boolean =
        !isCancelled && currentTick >= nextExecutionTick

    fun executeSync() {
        syncAction?.invoke()
    }

    suspend fun executeAsync() {
        asyncAction?.invoke()
    }

    fun scheduleNext(): Boolean {
        if (period <= 0) return false
        nextExecutionTick += period
        return true
    }
}

/**
 * 默认 Tick 调度器实现
 */
class DefaultTickScheduler : TickScheduler {
    @Volatile
    override var currentTick: Long = 0L
    override val targetTps: Int = 20

    @Volatile
    override var currentTps: Double = 20.0

    @Volatile
    override var lastTickDuration: Long = 0L

    private val taskIdCounter = AtomicInteger(0)
    private val tasks = ConcurrentHashMap<Int, DefaultTickTask>()

    override fun runTask(task: () -> Unit): TickTask = scheduleTask(0, 0, syncAction = task)

    override fun runTaskLater(delay: Long, task: () -> Unit): TickTask = scheduleTask(delay, 0, syncAction = task)

    override fun runTaskTimer(delay: Long, period: Long, task: () -> Unit): TickTask = scheduleTask(delay, period, syncAction = task)

    override fun runTaskAsync(task: suspend () -> Unit): TickTask = scheduleTask(0, 0, isSync = false, asyncAction = task)

    override fun runTaskLaterAsync(delay: Long, task: suspend () -> Unit): TickTask = scheduleTask(delay, 0, isSync = false, asyncAction = task)

    override fun runTaskTimerAsync(delay: Long, period: Long, task: suspend () -> Unit): TickTask = scheduleTask(delay, period, isSync = false, asyncAction = task)

    override fun cancelTask(taskId: Int) {
        tasks[taskId]?.cancel()
        tasks.remove(taskId)
    }

    override fun cancelAllTasks() {
        tasks.values.forEach { it.cancel() }
        tasks.clear()
    }

    override fun getPendingTaskCount(): Int = tasks.size

    private fun scheduleTask(
        delay: Long,
        period: Long,
        isSync: Boolean = true,
        syncAction: (() -> Unit)? = null,
        asyncAction: (suspend () -> Unit)? = null
    ): TickTask {
        val id = taskIdCounter.incrementAndGet()
        val task = DefaultTickTask(id, isSync, delay, period, syncAction, asyncAction)
        task.nextExecutionTick = currentTick + delay
        tasks[id] = task
        return task
    }

    suspend fun processTick(scope: CoroutineScope) {
        val toRemove = mutableListOf<Int>()
        for ((id, task) in tasks) {
            if (task.isCancelled) {
                toRemove.add(id)
                continue
            }
            if (task.shouldExecute(currentTick)) {
                try {
                    if (task.isSync) {
                        task.executeSync()
                    } else {
                        scope.launch { task.executeAsync() }
                    }
                } catch (e: Exception) {
                    logger.error(e) { "Tick 任务 $id 执行失败" }
                }
                if (!task.scheduleNext()) {
                    toRemove.add(id)
                }
            }
        }
        toRemove.forEach { tasks.remove(it) }
    }
}

/**
 * 默认内存使用情况
 */
data class DefaultMemoryUsage(
    override val used: Long,
    override val max: Long,
    override val free: Long
) : MemoryUsage {
    companion object {
        fun current(): DefaultMemoryUsage {
            val runtime = Runtime.getRuntime()
            val max = runtime.maxMemory()
            val total = runtime.totalMemory()
            val free = runtime.freeMemory()
            return DefaultMemoryUsage(total - free, max, free)
        }
    }
}

data class DefaultTpsStats(
    override val current: Double,
    override val average1m: Double,
    override val average5m: Double,
    override val average15m: Double
) : TpsStats

data class DefaultWorldStats(
    override val worldName: String,
    override val loadedChunks: Int,
    override val entityCount: Int,
    override val playerCount: Int
) : WorldStats

data class DefaultPerformanceReport(
    override val timestamp: Long,
    override val tpsStats: TpsStats,
    override val memoryStats: MemoryUsage,
    override val worldStats: Map<String, WorldStats>
) : PerformanceReport {
    override fun export(): String = buildString {
        appendLine("=== 性能报告 ===")
        appendLine("时间: $timestamp")
        appendLine("TPS: 当前=${tpsStats.current}, 1m=${tpsStats.average1m}, 5m=${tpsStats.average5m}, 15m=${tpsStats.average15m}")
        appendLine("内存: ${memoryStats.used / 1024 / 1024}MB / ${memoryStats.max / 1024 / 1024}MB (${String.format("%.1f", memoryStats.usagePercent)}%)")
        worldStats.forEach { (name, stats) ->
            appendLine("世界[$name]: 区块=${stats.loadedChunks}, 实体=${stats.entityCount}, 玩家=${stats.playerCount}")
        }
    }
}

/**
 * 默认性能监控实现
 */
class DefaultPerformanceMonitor : PerformanceMonitor {
    private val tpsHistory = CopyOnWriteArrayList<Double>()
    private val tickDurations = CopyOnWriteArrayList<Long>()

    @Volatile
    override var averageTps1m: Double = 20.0
    @Volatile
    override var averageTps5m: Double = 20.0
    @Volatile
    override var averageTps15m: Double = 20.0
    @Volatile
    override var averageTickDuration: Double = 0.0
    @Volatile
    override var maxTickDuration: Long = 0L

    override val memoryUsage: MemoryUsage get() = DefaultMemoryUsage.current()

    fun recordTick(tps: Double, duration: Long) {
        tpsHistory.add(tps)
        tickDurations.add(duration)
        if (duration > maxTickDuration) maxTickDuration = duration

        // 保留最近 18000 个 tick (15分钟 @ 20TPS)
        while (tpsHistory.size > 18000) tpsHistory.removeAt(0)
        while (tickDurations.size > 18000) tickDurations.removeAt(0)

        val size = tpsHistory.size
        averageTps1m = tpsHistory.takeLast(1200.coerceAtMost(size)).average()
        averageTps5m = tpsHistory.takeLast(6000.coerceAtMost(size)).average()
        averageTps15m = tpsHistory.average()
        averageTickDuration = tickDurations.average()
    }

    override fun reset() {
        tpsHistory.clear()
        tickDurations.clear()
        maxTickDuration = 0L
        averageTps1m = 20.0
        averageTps5m = 20.0
        averageTps15m = 20.0
        averageTickDuration = 0.0
    }

    override fun getReport(): PerformanceReport = DefaultPerformanceReport(
        timestamp = System.currentTimeMillis(),
        tpsStats = DefaultTpsStats(averageTps1m, averageTps1m, averageTps5m, averageTps15m),
        memoryStats = memoryUsage,
        worldStats = emptyMap()
    )
}

/**
 * 默认 Tick 管理器实现
 * 使用协程驱动 tick 循环，目标 20 TPS (50ms/tick)
 */
class DefaultTickManager(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) : TickManager {
    private val scheduler = DefaultTickScheduler()
    private val monitor = DefaultPerformanceMonitor()
    private val listeners = CopyOnWriteArrayList<TickListener>()
    private val _isRunning = AtomicBoolean(false)
    private var tickJob: Job? = null

    override val isRunning: Boolean get() = _isRunning.get()

    override fun start() {
        if (_isRunning.getAndSet(true)) return
        logger.info { "启动 Tick 管理器, 目标 TPS: ${scheduler.targetTps}" }
        tickJob = scope.launch {
            val msPerTick = 1000L / scheduler.targetTps
            while (isActive && _isRunning.get()) {
                val tickStart = System.currentTimeMillis()

                // 执行调度任务
                scheduler.processTick(this)

                // 通知监听器
                val sortedListeners = listeners.sortedBy { it.priority }
                for (listener in sortedListeners) {
                    try {
                        listener.onTick(scheduler.currentTick)
                    } catch (e: Exception) {
                        logger.error(e) { "Tick 监听器执行失败" }
                    }
                }

                val tickEnd = System.currentTimeMillis()
                val duration = tickEnd - tickStart
                scheduler.lastTickDuration = duration
                scheduler.currentTick++

                val tps = if (duration > 0) (1000.0 / duration).coerceAtMost(20.0) else 20.0
                scheduler.currentTps = tps
                monitor.recordTick(tps, duration)

                val sleepTime = msPerTick - duration
                if (sleepTime > 0) {
                    delay(sleepTime)
                }
            }
        }
    }

    override fun stop() {
        if (!_isRunning.getAndSet(false)) return
        tickJob?.cancel()
        tickJob = null
        scheduler.cancelAllTasks()
        logger.info { "Tick 管理器已停止" }
    }

    override fun registerListener(listener: TickListener) {
        listeners.add(listener)
    }

    override fun unregisterListener(listener: TickListener) {
        listeners.remove(listener)
    }

    override fun getScheduler(): TickScheduler = scheduler

    fun getPerformanceMonitor(): PerformanceMonitor = monitor
}


