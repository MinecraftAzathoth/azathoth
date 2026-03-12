package com.azathoth.core.common.util

import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.milliseconds

class CoroutineSchedulerTest {

    @Test
    fun `test schedule executes after delay`() {
        runBlocking {
            val scheduler = CoroutineScheduler()
            var executed = false

            scheduler.schedule(50.milliseconds) { executed = true }
            assertFalse(executed)

            delay(150)
            assertTrue(executed)

            scheduler.shutdown()
        }
    }

    @Test
    fun `test cancel prevents execution`() {
        runBlocking {
            val scheduler = CoroutineScheduler()
            var executed = false

            val task = scheduler.schedule(200.milliseconds) { executed = true }
            task.cancel()

            delay(300)
            assertFalse(executed)
            assertTrue(task.isCancelled)

            scheduler.shutdown()
        }
    }

    @Test
    fun `test scheduleAtFixedRate repeats`() {
        runBlocking {
            val scheduler = CoroutineScheduler()
            var count = 0

            val task = scheduler.scheduleAtFixedRate(10.milliseconds, 50.milliseconds) { count++ }

            delay(250)
            task.cancel()
            assertTrue(count >= 2, "Expected at least 2 executions, got $count")

            scheduler.shutdown()
        }
    }

    @Test
    fun `test async executor submit and await`() = runTest {
        val executor = CoroutineAsyncExecutor()
        val task = executor.submit { 42 }

        assertEquals(42, task.await())
        assertTrue(task.isDone)

        executor.shutdown()
    }

    @Test
    fun `test async executor submitAll`() = runTest {
        val executor = CoroutineAsyncExecutor()
        val tasks = executor.submitAll(listOf(
            { 1 },
            { 2 },
            { 3 }
        ))

        val results = tasks.map { it.await() }
        assertEquals(listOf(1, 2, 3), results)

        executor.shutdown()
    }
}
