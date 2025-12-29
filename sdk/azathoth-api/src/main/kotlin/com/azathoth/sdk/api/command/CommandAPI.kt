package com.azathoth.sdk.api.command

/**
 * 命令发送者
 */
interface CommandSender {
    /** 发送者名称 */
    val name: String
    
    /** 是否是玩家 */
    val isPlayer: Boolean
    
    /** 是否是控制台 */
    val isConsole: Boolean
    
    /** 发送消息 */
    suspend fun sendMessage(message: String)
    
    /** 发送多条消息 */
    suspend fun sendMessages(vararg messages: String)
    
    /** 检查权限 */
    fun hasPermission(permission: String): Boolean
}

/**
 * 命令上下文
 */
interface CommandContext {
    /** 命令发送者 */
    val sender: CommandSender
    
    /** 命令名称 */
    val commandName: String
    
    /** 命令别名（如果使用了别名） */
    val alias: String
    
    /** 原始参数 */
    val rawArgs: Array<String>
    
    /** 获取字符串参数 */
    fun getString(index: Int): String?
    
    /** 获取整数参数 */
    fun getInt(index: Int): Int?
    
    /** 获取双精度参数 */
    fun getDouble(index: Int): Double?
    
    /** 获取布尔参数 */
    fun getBoolean(index: Int): Boolean?
    
    /** 获取玩家参数 */
    fun getPlayer(index: Int): String?
    
    /** 获取剩余参数（从指定位置开始） */
    fun getRemainingArgs(fromIndex: Int): Array<String>
    
    /** 连接剩余参数 */
    fun joinRemainingArgs(fromIndex: Int, separator: String = " "): String
}

/**
 * 命令执行结果
 */
sealed class CommandResult {
    /** 成功 */
    object Success : CommandResult()
    
    /** 参数错误 */
    data class InvalidArgs(val message: String) : CommandResult()
    
    /** 权限不足 */
    data class NoPermission(val permission: String) : CommandResult()
    
    /** 只能玩家执行 */
    object PlayerOnly : CommandResult()
    
    /** 只能控制台执行 */
    object ConsoleOnly : CommandResult()
    
    /** 执行失败 */
    data class Failure(val message: String) : CommandResult()
}

/**
 * 命令执行器
 */
interface CommandExecutor {
    /**
     * 执行命令
     */
    suspend fun execute(context: CommandContext): CommandResult
    
    /**
     * Tab 补全
     */
    fun tabComplete(context: CommandContext): List<String> = emptyList()
}

/**
 * 命令定义
 */
interface Command {
    /** 命令名称 */
    val name: String
    
    /** 命令别名 */
    val aliases: List<String> get() = emptyList()
    
    /** 命令描述 */
    val description: String get() = ""
    
    /** 使用方法 */
    val usage: String get() = "/$name"
    
    /** 所需权限 */
    val permission: String? get() = null
    
    /** 是否只能玩家执行 */
    val playerOnly: Boolean get() = false
    
    /** 子命令 */
    val subCommands: List<Command> get() = emptyList()
    
    /** 执行器 */
    val executor: CommandExecutor
}

/**
 * 命令注解
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class CommandInfo(
    val name: String,
    val aliases: Array<String> = [],
    val description: String = "",
    val usage: String = "",
    val permission: String = "",
    val playerOnly: Boolean = false
)

/**
 * 子命令注解
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class SubCommand(
    val name: String,
    val aliases: Array<String> = [],
    val description: String = "",
    val permission: String = ""
)

/**
 * 命令管理器
 */
interface CommandManager {
    /**
     * 注册命令
     */
    fun registerCommand(command: Command, plugin: Any)
    
    /**
     * 注册带注解的命令处理器
     */
    fun registerCommands(handler: Any, plugin: Any)
    
    /**
     * 注销命令
     */
    fun unregisterCommand(name: String)
    
    /**
     * 注销插件的所有命令
     */
    fun unregisterAll(plugin: Any)
    
    /**
     * 执行命令
     */
    suspend fun dispatch(sender: CommandSender, commandLine: String): CommandResult
    
    /**
     * 获取命令
     */
    fun getCommand(name: String): Command?
    
    /**
     * 获取所有命令
     */
    fun getCommands(): Collection<Command>
}
