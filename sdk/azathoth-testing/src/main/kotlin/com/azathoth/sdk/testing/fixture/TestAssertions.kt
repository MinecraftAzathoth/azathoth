package com.azathoth.sdk.testing.fixture

import com.azathoth.sdk.testing.mock.MockPlayer
import com.azathoth.sdk.testing.mock.MockServer

/**
 * 测试断言扩展工具
 */
object PluginTestAssertions {

    /**
     * 断言事件被触发（通过回调记录检查）
     */
    inline fun <reified T> assertEventFired(
        events: List<Any>,
        message: String = "期望事件 ${T::class.simpleName} 被触发"
    ) {
        if (events.none { it is T }) {
            throw AssertionError("$message，但实际事件列表: ${events.map { it::class.simpleName }}")
        }
    }

    /**
     * 断言事件未被触发
     */
    inline fun <reified T> assertEventNotFired(
        events: List<Any>,
        message: String = "期望事件 ${T::class.simpleName} 未被触发"
    ) {
        if (events.any { it is T }) {
            throw AssertionError("$message，但事件已存在于列表中")
        }
    }

    /**
     * 断言玩家拥有权限
     */
    fun assertPlayerHasPermission(player: MockPlayer, permission: String) {
        if (!player.hasPermission(permission)) {
            throw AssertionError("期望玩家 '${player.name}' 拥有权限 '$permission'")
        }
    }

    /**
     * 断言玩家没有权限
     */
    fun assertPlayerLacksPermission(player: MockPlayer, permission: String) {
        if (player.hasPermission(permission)) {
            throw AssertionError("期望玩家 '${player.name}' 没有权限 '$permission'，但实际拥有")
        }
    }

    /**
     * 断言命令执行结果
     */
    suspend fun assertCommandResult(
        server: MockServer,
        sender: String,
        command: String,
        expectedResult: String
    ) {
        val result = server.executeCommand(sender, command)
        if (result != expectedResult) {
            throw AssertionError("命令 '$command' 期望结果 '$expectedResult'，实际结果 '$result'")
        }
    }

    /**
     * 断言命令执行结果包含指定内容
     */
    suspend fun assertCommandResultContains(
        server: MockServer,
        sender: String,
        command: String,
        expectedContent: String
    ) {
        val result = server.executeCommand(sender, command)
        if (expectedContent !in result) {
            throw AssertionError("命令 '$command' 期望结果包含 '$expectedContent'，实际结果 '$result'")
        }
    }

    /**
     * 断言玩家收到了指定数量的消息
     */
    fun assertMessageCount(player: MockPlayer, expectedCount: Int) {
        val actual = player.receivedMessages.size
        if (actual != expectedCount) {
            throw AssertionError("期望玩家 '${player.name}' 收到 $expectedCount 条消息，实际收到 $actual 条")
        }
    }

    /**
     * 断言玩家生命值
     */
    fun assertPlayerHealth(player: MockPlayer, expectedHealth: Double, tolerance: Double = 0.01) {
        if (kotlin.math.abs(player.health - expectedHealth) > tolerance) {
            throw AssertionError("期望玩家 '${player.name}' 生命值为 $expectedHealth，实际为 ${player.health}")
        }
    }

    /**
     * 断言服务器玩家数量
     */
    fun assertPlayerCount(server: MockServer, expectedCount: Int) {
        val actual = server.getPlayers().size
        if (actual != expectedCount) {
            throw AssertionError("期望服务器有 $expectedCount 个玩家，实际有 $actual 个")
        }
    }
}
