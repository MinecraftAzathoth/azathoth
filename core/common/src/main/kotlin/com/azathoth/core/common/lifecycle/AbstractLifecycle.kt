package com.azathoth.core.common.lifecycle

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.CopyOnWriteArrayList

private val logger = KotlinLogging.logger {}

/**
 * 抽象生命周期实现 — 提供完整的状态机逻辑
 *
 * 合法状态转换:
 * CREATED → INITIALIZING → INITIALIZED → STARTING → RUNNING → STOPPING → STOPPED
 * 任何状态 → FAILED
 */
abstract class AbstractLifecycle : ObservableLifecycle, ReloadableLifecycle {

    @Volatile
    override var state: LifecycleState = LifecycleState.CREATED
        protected set

    private val listeners = CopyOnWriteArrayList<LifecycleListener>()
    private val mutex = Mutex()

    override fun addListener(listener: LifecycleListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: LifecycleListener) {
        listeners.remove(listener)
    }

    final override suspend fun initialize() {
        mutex.withLock {
            checkTransition(LifecycleState.CREATED, LifecycleState.INITIALIZING)
            transition(LifecycleState.INITIALIZING)
            try {
                doInitialize()
                transition(LifecycleState.INITIALIZED)
            } catch (e: Exception) {
                transition(LifecycleState.FAILED)
                logger.error(e) { "初始化失败" }
                throw e
            }
        }
    }

    final override suspend fun start() {
        mutex.withLock {
            checkTransition(LifecycleState.INITIALIZED, LifecycleState.STARTING)
            transition(LifecycleState.STARTING)
            try {
                doStart()
                transition(LifecycleState.RUNNING)
            } catch (e: Exception) {
                transition(LifecycleState.FAILED)
                logger.error(e) { "启动失败" }
                throw e
            }
        }
    }

    final override suspend fun stop() {
        mutex.withLock {
            if (state == LifecycleState.STOPPED || state == LifecycleState.CREATED) return
            transition(LifecycleState.STOPPING)
            try {
                doStop()
                transition(LifecycleState.STOPPED)
            } catch (e: Exception) {
                transition(LifecycleState.FAILED)
                logger.error(e) { "停止失败" }
                throw e
            }
        }
    }

    override suspend fun reload() {
        mutex.withLock {
            require(state == LifecycleState.RUNNING) { "只能在 RUNNING 状态下重载，当前状态: $state" }
            try {
                doReload()
            } catch (e: Exception) {
                logger.error(e) { "重载失败" }
                throw e
            }
        }
    }

    // --- 子类实现的钩子方法 ---

    /** 初始化逻辑 */
    protected abstract suspend fun doInitialize()

    /** 启动逻辑 */
    protected abstract suspend fun doStart()

    /** 停止逻辑 */
    protected abstract suspend fun doStop()

    /** 重载逻辑（默认空实现） */
    protected open suspend fun doReload() {}

    // --- 内部工具 ---

    private suspend fun transition(newState: LifecycleState) {
        val oldState = state
        state = newState
        logger.debug { "生命周期状态转换: $oldState → $newState" }
        for (listener in listeners) {
            try {
                listener.onStateChange(oldState, newState)
            } catch (e: Exception) {
                logger.warn(e) { "生命周期监听器异常" }
            }
        }
    }

    private fun checkTransition(expectedCurrent: LifecycleState, target: LifecycleState) {
        require(state == expectedCurrent) {
            "非法状态转换: 期望 $expectedCurrent → $target，当前状态: $state"
        }
    }
}
