package com.azathoth.sdk.api.permission

/**
 * 权限默认值
 */
enum class PermissionDefault {
    /** 默认拥有 */
    TRUE,
    /** 默认没有 */
    FALSE,
    /** 管理员默认拥有 */
    OP,
    /** 管理员默认没有 */
    NOT_OP
}

/**
 * 权限定义
 */
interface Permission {
    /** 权限节点 */
    val node: String
    
    /** 权限描述 */
    val description: String
    
    /** 默认值 */
    val default: PermissionDefault
    
    /** 子权限 */
    val children: Map<String, Boolean>
}

/**
 * 权限持有者
 */
interface PermissionHolder {
    /** 检查是否有权限 */
    fun hasPermission(permission: String): Boolean
    
    /** 检查是否有权限（带 Permission 对象） */
    fun hasPermission(permission: Permission): Boolean
    
    /** 设置权限 */
    fun setPermission(permission: String, value: Boolean)
    
    /** 移除权限 */
    fun unsetPermission(permission: String)
    
    /** 获取所有权限 */
    fun getPermissions(): Set<PermissionAttachment>
    
    /** 是否是管理员 */
    val isOp: Boolean
}

/**
 * 权限附件
 */
interface PermissionAttachment {
    /** 权限节点 */
    val permission: String
    
    /** 权限值 */
    val value: Boolean
    
    /** 来源插件 */
    val plugin: Any
}

/**
 * 权限组
 */
interface PermissionGroup {
    /** 组名 */
    val name: String
    
    /** 显示名称 */
    var displayName: String
    
    /** 前缀 */
    var prefix: String
    
    /** 后缀 */
    var suffix: String
    
    /** 优先级 */
    var priority: Int
    
    /** 是否默认组 */
    val isDefault: Boolean
    
    /** 权限列表 */
    val permissions: Set<String>
    
    /** 继承的组 */
    val inheritance: Set<String>
    
    /** 添加权限 */
    fun addPermission(permission: String)
    
    /** 移除权限 */
    fun removePermission(permission: String)
    
    /** 检查是否有权限 */
    fun hasPermission(permission: String): Boolean
}

/**
 * 权限管理器
 */
interface PermissionManager {
    /**
     * 注册权限
     */
    fun registerPermission(permission: Permission)
    
    /**
     * 注销权限
     */
    fun unregisterPermission(node: String)
    
    /**
     * 获取权限定义
     */
    fun getPermission(node: String): Permission?
    
    /**
     * 获取所有权限
     */
    fun getPermissions(): Collection<Permission>
    
    /**
     * 创建权限组
     */
    suspend fun createGroup(name: String): PermissionGroup
    
    /**
     * 删除权限组
     */
    suspend fun deleteGroup(name: String)
    
    /**
     * 获取权限组
     */
    fun getGroup(name: String): PermissionGroup?
    
    /**
     * 获取所有权限组
     */
    fun getGroups(): Collection<PermissionGroup>
    
    /**
     * 获取默认组
     */
    fun getDefaultGroup(): PermissionGroup
    
    /**
     * 获取玩家所在的组
     */
    suspend fun getPlayerGroups(playerId: String): List<PermissionGroup>
    
    /**
     * 添加玩家到组
     */
    suspend fun addPlayerToGroup(playerId: String, groupName: String)
    
    /**
     * 从组中移除玩家
     */
    suspend fun removePlayerFromGroup(playerId: String, groupName: String)
    
    /**
     * 设置玩家权限
     */
    suspend fun setPlayerPermission(playerId: String, permission: String, value: Boolean)
    
    /**
     * 移除玩家权限
     */
    suspend fun unsetPlayerPermission(playerId: String, permission: String)
}
