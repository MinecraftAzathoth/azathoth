package com.azathoth.game.engine.tick

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DefaultTickManagerTest {

    @Test
    fun `scheduler should execute immediate task`() = runTest {
        val scheduler = DefaultTickScheduler()
        var executed = false

        scheduler.runTask { executed = true }
        scheduler.processTick(this)

        assertTrue(executed)
    }

    @Test
    fun `scheduler should execute delayed task`() = runTest {
        val scheduler = DefaultTickScheduler()
        var executed = false

        scheduler.runTaskLater(2) { executed = true }

        // tick 0 - 不应执行
        scheduler.processTick(this)
        assertFalse(executed)

        scheduler.currentTick = 1
        scheduler.processTick(this)
        assertFalse(executed)

        scheduler.currentTick = 2
        scheduler.processTick(this)
        assertTrue(executed)
    }

    @Test
    fun `scheduler should execute periodic task`() = runTest {
        val scheduler = DefaultTickScheduler()
        var count = 0

        scheduler.runTaskTimer(0, 3) { count++ }

        // tick 0 - 执行
        scheduler.processTick(this)
        assertEquals(1, count)

        // tick 1, 2 - 不执行
        scheduler.currentTick = 1
        scheduler.processTick(this)
        scheduler.currentTick = 2
        scheduler.processTick(this)
        assertEquals(1, count)

        // tick 3 - 执行
        scheduler.currentTick = 3
        scheduler.processTick(this)
        assertEquals(2, count)
    }

    @Test
    fun `cancel task should stop execution`() = runTest {
        val scheduler = DefaultTickScheduler()
        var count = 0

        val task = scheduler.runTaskTimer(0, 1) { count++ }

        scheduler.processTick(this)
        assertEquals(1, count)

        task.cancel()
        scheduler.currentTick = 1
        scheduler.processTick(this)
        assertEquals(1, count) // 不再增加
    }

    @Test
    fun `cancelAllTasks should clear all tasks`() = runTest {
        val scheduler = DefaultTickScheduler()

        scheduler.runTask { }
        scheduler.runTaskLater(5) { }
        scheduler.runTaskTimer(0, 1) { }
        assertEquals(3, scheduler.getPendingTaskCount())

        scheduler.cancelAllTasks()
        assertEquals(0, scheduler.getPendingTaskCount())
    }

    @Test
    fun `tick manager should start and stop`() = runTest {
        val manager = DefaultTickManager()

        assertFalse(manager.isRunning)
        manager.start()
        assertTrue(manager.isRunning)

        delay(120) // 让它跑几个 tick
        assertTrue(manager.getScheduler().currentTick > 0)

        manager.stop()
        assertFalse(manager.isRunning)
    }

    @Test
    fun `tick listener should be called`() = runTest {
        val manager = DefaultTickManager()
        var tickCount = 0L

        val listener = object : TickListener {
            override suspend fun onTick(tick: Long) {
                tickCount = tick
            }
        }

        manager.registerListener(listener)
        manager.start()
        delay(120)
        manager.stop()

        assertTrue(tickCount > 0)
    }

    @Test
    fun `performance monitor should record data`() {
        val monitor = DefaultPerformanceMonitor()
        monitor.recordTick(20.0, 10)
        monitor.recordTick(19.0, 15)
        monitor.recordTick(18.0, 20)

        assertTrue(monitor.averageTps1m > 0)
        assertEquals(20L, monitor.maxTickDuration)
        assertTrue(monitor.averageTickDuration > 0)

        val report = monitor.getReport()
        assertNotNull(report)
        assertTrue(report.export().contains("性能报告"))
    }
}
