package com.azathoth.sdk.api.command

import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

/**
 * 默认命令上下文实现
 */
class DefaultCommandContext(
    override val sender: CommandSender,
    override val commandName: String,
    override val alias: String,
    override val rawArgs: Array<String>
) : CommandContext {

    override fun getString(index: Int): String? =
        rawArgs.getOrNull(index)

    override fun getInt(index: Int): Int? =
        rawArgs.getOrNull(index)?.toIntOrNull()

    override fun getDouble(index: Int): Double? =
        rawArgs.getOrNull(index)?.toDoubleOrNull()

    override fun getBoolean(index: Int): Boolean? =
        rawArgs.getOrNull(index)?.toBooleanStrictOrNull()

    override fun getPlayer(index: Int): String? =
        rawArgs.getOrNull(index)

    override fun getRemainingArgs(fromIndex: Int): Array<String> =
        if (fromIndex < rawArgs.size) rawArgs.copyOfRange(fromIndex, rawArgs.size) else emptyArray()

    override fun joinRemainingArgs(fromIndex: Int, separator: String): String =
        getRemainingArgs(fromIndex).joinToString(separator)
}

/**
 * 简单命令实现
 */
data class SimpleCommand(
    override val name: String,
    override val aliases: List<String> = emptyList(),
    override val description: String = "",
    override val usage: String = "/$name",
    override val permission: String? = null,
    override val playerOnly: Boolean = false,
    override val subCommands: List<Command> = emptyList(),
    override val executor: CommandExecutor
) : Command

/**
 * 默认命令管理器实现
 */
class DefaultCommandManager : CommandManager {

    // name -> Command
    private val commands = ConcurrentHashMap<String, Command>()
    // name -> plugin
    private val commandPlugins = ConcurrentHashMap<String, Any>()
    // alias -> primary name
    private val aliasMap = ConcurrentHashMap<String, String>()

    override fun registerCommand(command: Command, plugin: Any) {
        val name = command.name.lowercase()
        commands[name] = command
        commandPlugins[name] = plugin

        for (alias in command.aliases) {
            aliasMap[alias.lowercase()] = name
        }

        logger.debug { "注册命令: /$name (${command.aliases.joinToString()}) by $plugin" }
    }

    override fun registerCommands(handler: Any, plugin: Any) {
        val kClass = handler::class
        val info = kClass.annotations.filterIsInstance<CommandInfo>().firstOrNull()
            ?: throw IllegalArgumentException("命令处理器 ${kClass.simpleName} 缺少 @CommandInfo 注解")

        val subCommands = mutableListOf<Command>()

        for (func in kClass.members) {
            val subAnno = func.annotations.filterIsInstance<SubCommand>().firstOrNull() ?: continue

            val subExecutor = object : CommandExecutor {
                override suspend fun execute(context: CommandContext): CommandResult {
                    return try {
                        val javaMethod = func.javaClass.methods.find { it.name == "call" }
                            ?: (func as? kotlin.reflect.KFunction<*>)?.let { kf ->
                                kf.javaClass.methods.find { it.name == "invoke" }
                            }

                        // Use kotlin reflect to call
                        val result = (func as kotlin.reflect.KFunction<*>).call(handler, context)
                        result as? CommandResult ?: CommandResult.Success
                    } catch (e: Exception) {
                        logger.error(e) { "子命令 ${subAnno.name} 执行异常" }
                        CommandResult.Failure(e.message ?: "未知错误")
                    }
                }
            }

            subCommands.add(
                SimpleCommand(
                    name = subAnno.name,
                    aliases = subAnno.aliases.toList(),
                    description = subAnno.description,
                    permission = subAnno.permission.ifEmpty { null },
                    executor = subExecutor
                )
            )
        }

        // Main executor: if first arg matches a sub-command, delegate to it
        val mainExecutor = object : CommandExecutor {
            override suspend fun execute(context: CommandContext): CommandResult {
                val subName = context.getString(0)?.lowercase()
                if (subName != null) {
                    val sub = subCommands.find {
                        it.name.equals(subName, ignoreCase = true) ||
                            it.aliases.any { a -> a.equals(subName, ignoreCase = true) }
                    }
                    if (sub != null) {
                        val subPermission = sub.permission
                        if (subPermission != null && !context.sender.hasPermission(subPermission)) {
                            return CommandResult.NoPermission(subPermission)
                        }
                        val subContext = DefaultCommandContext(
                            sender = context.sender,
                            commandName = sub.name,
                            alias = subName,
                            rawArgs = context.getRemainingArgs(1)
                        )
                        return sub.executor.execute(subContext)
                    }
                }
                return CommandResult.InvalidArgs("用法: ${info.usage.ifEmpty { "/${info.name}" }}")
            }

            override fun tabComplete(context: CommandContext): List<String> {
                val subName = context.getString(0)?.lowercase()
                if (context.rawArgs.size <= 1) {
                    return subCommands.map { it.name }
                        .filter { subName == null || it.startsWith(subName) }
                }
                return emptyList()
            }
        }

        val command = SimpleCommand(
            name = info.name,
            aliases = info.aliases.toList(),
            description = info.description,
            usage = info.usage.ifEmpty { "/${info.name}" },
            permission = info.permission.ifEmpty { null },
            playerOnly = info.playerOnly,
            subCommands = subCommands,
            executor = mainExecutor
        )

        registerCommand(command, plugin)
    }

    override fun unregisterCommand(name: String) {
        val key = name.lowercase()
        val command = commands.remove(key)
        commandPlugins.remove(key)
        if (command != null) {
            for (alias in command.aliases) {
                aliasMap.remove(alias.lowercase())
            }
        }
    }

    override fun unregisterAll(plugin: Any) {
        val toRemove = commandPlugins.entries.filter { it.value === plugin }.map { it.key }
        toRemove.forEach { unregisterCommand(it) }
    }

    override suspend fun dispatch(sender: CommandSender, commandLine: String): CommandResult {
        val parts = commandLine.trimStart('/').split(" ")
        if (parts.isEmpty()) return CommandResult.InvalidArgs("空命令")

        val inputName = parts[0].lowercase()
        val args = if (parts.size > 1) parts.subList(1, parts.size).toTypedArray() else emptyArray()

        val primaryName = aliasMap[inputName] ?: inputName
        val command = commands[primaryName]
            ?: return CommandResult.Failure("未知命令: $inputName")

        // 权限检查
        val permission = command.permission
        if (permission != null && !sender.hasPermission(permission)) {
            return CommandResult.NoPermission(permission)
        }

        // 玩家限制检查
        if (command.playerOnly && !sender.isPlayer) {
            return CommandResult.PlayerOnly
        }

        val context = DefaultCommandContext(
            sender = sender,
            commandName = command.name,
            alias = inputName,
            rawArgs = args
        )

        return try {
            command.executor.execute(context)
        } catch (e: Exception) {
            logger.error(e) { "命令执行异常: /$inputName" }
            CommandResult.Failure(e.message ?: "未知错误")
        }
    }

    override fun getCommand(name: String): Command? {
        val key = name.lowercase()
        return commands[key] ?: aliasMap[key]?.let { commands[it] }
    }

    override fun getCommands(): Collection<Command> = commands.values

    /**
     * Tab 补全
     */
    fun tabComplete(sender: CommandSender, commandLine: String): List<String> {
        val parts = commandLine.trimStart('/').split(" ")
        if (parts.isEmpty()) return emptyList()

        val inputName = parts[0].lowercase()

        // 如果还在输入命令名
        if (parts.size == 1) {
            return commands.keys.filter { it.startsWith(inputName) } +
                aliasMap.keys.filter { it.startsWith(inputName) }
        }

        val primaryName = aliasMap[inputName] ?: inputName
        val command = commands[primaryName] ?: return emptyList()

        val args = parts.subList(1, parts.size).toTypedArray()
        val context = DefaultCommandContext(
            sender = sender,
            commandName = command.name,
            alias = inputName,
            rawArgs = args
        )

        return command.executor.tabComplete(context)
    }
}
