package com.azathoth.sdk.api.command

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultCommandManagerTest {

    private lateinit var manager: DefaultCommandManager

    /** 测试用 CommandSender */
    class TestSender(
        override val name: String = "TestPlayer",
        override val isPlayer: Boolean = true,
        override val isConsole: Boolean = false,
        private val permissions: Set<String> = emptySet()
    ) : CommandSender {
        val messages = mutableListOf<String>()

        override suspend fun sendMessage(message: String) {
            messages.add(message)
        }

        override suspend fun sendMessages(vararg messages: String) {
            this.messages.addAll(messages)
        }

        override fun hasPermission(permission: String): Boolean =
            permissions.contains(permission)
    }

    @BeforeEach
    fun setup() {
        manager = DefaultCommandManager()
    }

    @Test
    fun `register and dispatch simple command`() = runTest {
        val command = SimpleCommand(
            name = "test",
            executor = object : CommandExecutor {
                override suspend fun execute(context: CommandContext): CommandResult {
                    context.sender.sendMessage("executed: ${context.rawArgs.joinToString()}")
                    return CommandResult.Success
                }
            }
        )

        manager.registerCommand(command, "plugin")
        val sender = TestSender()
        val result = manager.dispatch(sender, "test arg1 arg2")

        assertTrue(result is CommandResult.Success)
        assertEquals("executed: arg1, arg2", sender.messages.first())
    }

    @Test
    fun `dispatch with alias`() = runTest {
        val command = SimpleCommand(
            name = "teleport",
            aliases = listOf("tp"),
            executor = object : CommandExecutor {
                override suspend fun execute(context: CommandContext): CommandResult {
                    return CommandResult.Success
                }
            }
        )

        manager.registerCommand(command, "plugin")
        val result = manager.dispatch(TestSender(), "tp player1")

        assertTrue(result is CommandResult.Success)
    }

    @Test
    fun `dispatch unknown command returns failure`() = runTest {
        val result = manager.dispatch(TestSender(), "nonexistent")
        assertTrue(result is CommandResult.Failure)
    }

    @Test
    fun `dispatch checks permission`() = runTest {
        val command = SimpleCommand(
            name = "admin",
            permission = "admin.use",
            executor = object : CommandExecutor {
                override suspend fun execute(context: CommandContext): CommandResult =
                    CommandResult.Success
            }
        )

        manager.registerCommand(command, "plugin")

        // 没有权限
        val result1 = manager.dispatch(TestSender(permissions = emptySet()), "admin")
        assertTrue(result1 is CommandResult.NoPermission)

        // 有权限
        val result2 = manager.dispatch(TestSender(permissions = setOf("admin.use")), "admin")
        assertTrue(result2 is CommandResult.Success)
    }

    @Test
    fun `dispatch checks playerOnly`() = runTest {
        val command = SimpleCommand(
            name = "fly",
            playerOnly = true,
            executor = object : CommandExecutor {
                override suspend fun execute(context: CommandContext): CommandResult =
                    CommandResult.Success
            }
        )

        manager.registerCommand(command, "plugin")

        val consoleSender = TestSender(name = "Console", isPlayer = false, isConsole = true)
        val result = manager.dispatch(consoleSender, "fly")
        assertTrue(result is CommandResult.PlayerOnly)
    }

    @Test
    fun `tab complete returns matching commands`() {
        val cmd1 = SimpleCommand(name = "teleport", aliases = listOf("tp"), executor = noopExecutor())
        val cmd2 = SimpleCommand(name = "tell", executor = noopExecutor())
        val cmd3 = SimpleCommand(name = "help", executor = noopExecutor())

        manager.registerCommand(cmd1, "plugin")
        manager.registerCommand(cmd2, "plugin")
        manager.registerCommand(cmd3, "plugin")

        val completions = manager.tabComplete(TestSender(), "te")
        assertTrue(completions.contains("teleport"))
        assertTrue(completions.contains("tell"))
        assertFalse(completions.contains("help"))
    }

    @Test
    fun `unregister command`() = runTest {
        val command = SimpleCommand(name = "test", aliases = listOf("t"), executor = noopExecutor())
        manager.registerCommand(command, "plugin")

        assertNotNull(manager.getCommand("test"))
        assertNotNull(manager.getCommand("t"))

        manager.unregisterCommand("test")

        assertNull(manager.getCommand("test"))
        assertNull(manager.getCommand("t"))
    }

    @Test
    fun `unregisterAll removes all commands for plugin`() = runTest {
        val plugin = "myPlugin"
        manager.registerCommand(SimpleCommand(name = "cmd1", executor = noopExecutor()), plugin)
        manager.registerCommand(SimpleCommand(name = "cmd2", executor = noopExecutor()), plugin)
        manager.registerCommand(SimpleCommand(name = "cmd3", executor = noopExecutor()), "otherPlugin")

        assertEquals(3, manager.getCommands().size)

        manager.unregisterAll(plugin)

        assertEquals(1, manager.getCommands().size)
        assertNotNull(manager.getCommand("cmd3"))
    }

    @Test
    fun `command context parses args correctly`() {
        val ctx = DefaultCommandContext(
            sender = TestSender(),
            commandName = "test",
            alias = "test",
            rawArgs = arrayOf("hello", "42", "3.14", "true")
        )

        assertEquals("hello", ctx.getString(0))
        assertEquals(42, ctx.getInt(1))
        assertEquals(3.14, ctx.getDouble(2))
        assertEquals(true, ctx.getBoolean(3))
        assertNull(ctx.getString(10))
        assertEquals("42 3.14 true", ctx.joinRemainingArgs(1))
        assertArrayEquals(arrayOf("3.14", "true"), ctx.getRemainingArgs(2))
    }

    private fun noopExecutor() = object : CommandExecutor {
        override suspend fun execute(context: CommandContext): CommandResult = CommandResult.Success
    }
}
