package com.azathoth.game.engine.tick

/**
 * Tick 任务
 */
interface TickTask {
    /** 任务ID */
    val taskId: Int
    
    /** 是否已取消 */
    val isCancelled: Boolean
    
    /** 是否同步执行 */
    val isSync: Boolean
    
    /** 取消任务 */
    fun cancel()
}

/**
 * Tick 调度器
 */
interface TickScheduler {
    /** 当前 Tick 数 */
    val currentTick: Long
    
    /** 目标 TPS */
    val targetTps: Int
    
    /** 当前 TPS */
    val currentTps: Double
    
    /** 最近一次 Tick 耗时（毫秒） */
    val lastTickDuration: Long
    
    /**
     * 在主线程同步执行任务
     */
    fun runTask(task: () -> Unit): TickTask
    
    /**
     * 延迟执行任务（同步）
     */
    fun runTaskLater(delay: Long, task: () -> Unit): TickTask
    
    /**
     * 周期性执行任务（同步）
     */
    fun runTaskTimer(delay: Long, period: Long, task: () -> Unit): TickTask
    
    /**
     * 异步执行任务
     */
    fun runTaskAsync(task: suspend () -> Unit): TickTask
    
    /**
     * 延迟异步执行任务
     */
    fun runTaskLaterAsync(delay: Long, task: suspend () -> Unit): TickTask
    
    /**
     * 周期性异步执行任务
     */
    fun runTaskTimerAsync(delay: Long, period: Long, task: suspend () -> Unit): TickTask
    
    /**
     * 取消任务
     */
    fun cancelTask(taskId: Int)
    
    /**
     * 取消所有任务
     */
    fun cancelAllTasks()
    
    /**
     * 获取待执行任务数
     */
    fun getPendingTaskCount(): Int
}

/**
 * Tick 监听器
 */
interface TickListener {
    /** 优先级 */
    val priority: Int get() = 0
    
    /** 每 Tick 调用 */
    suspend fun onTick(tick: Long)
}

/**
 * Tick 管理器
 */
interface TickManager {
    /** 启动 Tick 循环 */
    fun start()
    
    /** 停止 Tick 循环 */
    fun stop()
    
    /** 是否正在运行 */
    val isRunning: Boolean
    
    /** 注册 Tick 监听器 */
    fun registerListener(listener: TickListener)
    
    /** 注销 Tick 监听器 */
    fun unregisterListener(listener: TickListener)
    
    /** 获取调度器 */
    fun getScheduler(): TickScheduler
}

/**
 * 性能监控
 */
interface PerformanceMonitor {
    /** 平均 TPS（1分钟） */
    val averageTps1m: Double
    
    /** 平均 TPS（5分钟） */
    val averageTps5m: Double
    
    /** 平均 TPS（15分钟） */
    val averageTps15m: Double
    
    /** 平均 Tick 耗时 */
    val averageTickDuration: Double
    
    /** 最大 Tick 耗时 */
    val maxTickDuration: Long
    
    /** 内存使用量 */
    val memoryUsage: MemoryUsage
    
    /** 重置统计 */
    fun reset()
    
    /** 获取详细报告 */
    fun getReport(): PerformanceReport
}

/**
 * 内存使用情况
 */
interface MemoryUsage {
    /** 已使用内存（字节） */
    val used: Long
    
    /** 最大内存（字节） */
    val max: Long
    
    /** 空闲内存（字节） */
    val free: Long
    
    /** 使用率 */
    val usagePercent: Double get() = used.toDouble() / max * 100
}

/**
 * 性能报告
 */
interface PerformanceReport {
    /** 生成时间 */
    val timestamp: Long
    
    /** TPS 统计 */
    val tpsStats: TpsStats
    
    /** 内存统计 */
    val memoryStats: MemoryUsage
    
    /** 世界统计 */
    val worldStats: Map<String, WorldStats>
    
    /** 导出为字符串 */
    fun export(): String
}

/**
 * TPS 统计
 */
interface TpsStats {
    val current: Double
    val average1m: Double
    val average5m: Double
    val average15m: Double
}

/**
 * 世界统计
 */
interface WorldStats {
    val worldName: String
    val loadedChunks: Int
    val entityCount: Int
    val playerCount: Int
}
