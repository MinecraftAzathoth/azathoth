package com.azathoth.core.common.lifecycle

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AbstractLifecycleTest {

    private class TestLifecycle : AbstractLifecycle() {
        var initialized = false
        var started = false
        var stopped = false
        var reloaded = false

        override suspend fun doInitialize() { initialized = true }
        override suspend fun doStart() { started = true }
        override suspend fun doStop() { stopped = true }
        override suspend fun doReload() { reloaded = true }
    }

    @Test
    fun `test full lifecycle`() = runTest {
        val lc = TestLifecycle()
        assertEquals(LifecycleState.CREATED, lc.state)

        lc.initialize()
        assertEquals(LifecycleState.INITIALIZED, lc.state)
        assertTrue(lc.initialized)

        lc.start()
        assertEquals(LifecycleState.RUNNING, lc.state)
        assertTrue(lc.started)
        assertTrue(lc.isRunning)

        lc.reload()
        assertTrue(lc.reloaded)

        lc.stop()
        assertEquals(LifecycleState.STOPPED, lc.state)
        assertTrue(lc.stopped)
    }

    @Test
    fun `test invalid transition throws`() = runTest {
        val lc = TestLifecycle()
        assertThrows(IllegalArgumentException::class.java) {
            kotlinx.coroutines.test.runTest { lc.start() }
        }
    }

    @Test
    fun `test listener receives state changes`() = runTest {
        val lc = TestLifecycle()
        val transitions = mutableListOf<Pair<LifecycleState, LifecycleState>>()

        lc.addListener(object : LifecycleListener {
            override suspend fun onStateChange(oldState: LifecycleState, newState: LifecycleState) {
                transitions.add(oldState to newState)
            }
        })

        lc.initialize()
        lc.start()
        lc.stop()

        assertEquals(6, transitions.size) // CREATED→INITIALIZING, INITIALIZING→INITIALIZED, INITIALIZED→STARTING, STARTING→RUNNING, RUNNING→STOPPING, STOPPING→STOPPED
    }

    @Test
    fun `test failed state on exception`() = runTest {
        val lc = object : AbstractLifecycle() {
            override suspend fun doInitialize() { throw RuntimeException("boom") }
            override suspend fun doStart() {}
            override suspend fun doStop() {}
        }

        assertThrows(RuntimeException::class.java) {
            kotlinx.coroutines.test.runTest { lc.initialize() }
        }
        assertEquals(LifecycleState.FAILED, lc.state)
    }
}
