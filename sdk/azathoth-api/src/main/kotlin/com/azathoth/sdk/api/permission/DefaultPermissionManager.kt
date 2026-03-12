package com.azathoth.sdk.api.permission

import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

/**
 * 简单权限实现
 */
data class SimplePermission(
    override val node: String,
    override val description: String = "",
    override val default: PermissionDefault = PermissionDefault.FALSE,
    override val children: Map<String, Boolean> = emptyMap()
) : Permission

/**
 * 简单权限附件实现
 */
data class SimplePermissionAttachment(
    override val permission: String,
    override val value: Boolean,
    override val plugin: Any
) : PermissionAttachment

/**
 * 简单权限组实现
 */
class SimplePermissionGroup(
    override val name: String,
    override var displayName: String = name,
    override var prefix: String = "",
    override var suffix: String = "",
    override var priority: Int = 0,
    override val isDefault: Boolean = false,
    private val _permissions: MutableSet<String> = mutableSetOf(),
    private val _inheritance: MutableSet<String> = mutableSetOf()
) : PermissionGroup {

    override val permissions: Set<String> get() = _permissions.toSet()
    override val inheritance: Set<String> get() = _inheritance.toSet()

    override fun addPermission(permission: String) {
        _permissions.add(permission)
    }

    override fun removePermission(permission: String) {
        _permissions.remove(permission)
    }

    override fun hasPermission(permission: String): Boolean =
        _permissions.contains(permission)

    fun addInheritance(groupName: String) {
        _inheritance.add(groupName)
    }

    fun removeInheritance(groupName: String) {
        _inheritance.remove(groupName)
    }
}

/**
 * 默认权限管理器实现
 */
class DefaultPermissionManager : PermissionManager {

    private val registeredPermissions = ConcurrentHashMap<String, Permission>()
    private val groups = ConcurrentHashMap<String, SimplePermissionGroup>()
    private val playerGroups = ConcurrentHashMap<String, MutableSet<String>>() // playerId -> group names
    private val playerPermissions = ConcurrentHashMap<String, MutableMap<String, Boolean>>() // playerId -> (node -> value)

    init {
        // 创建默认组
        groups["default"] = SimplePermissionGroup(
            name = "default",
            displayName = "默认",
            isDefault = true,
            priority = 0
        )
    }

    override fun registerPermission(permission: Permission) {
        registeredPermissions[permission.node] = permission
        logger.debug { "注册权限: ${permission.node}" }
    }

    override fun unregisterPermission(node: String) {
        registeredPermissions.remove(node)
    }

    override fun getPermission(node: String): Permission? =
        registeredPermissions[node]

    override fun getPermissions(): Collection<Permission> =
        registeredPermissions.values

    override suspend fun createGroup(name: String): PermissionGroup {
        val group = SimplePermissionGroup(name = name)
        groups[name] = group
        logger.debug { "创建权限组: $name" }
        return group
    }

    override suspend fun deleteGroup(name: String) {
        if (name == "default") {
            throw IllegalArgumentException("不能删除默认组")
        }
        groups.remove(name)
        // 从所有玩家中移除该组
        playerGroups.values.forEach { it.remove(name) }
        logger.debug { "删除权限组: $name" }
    }

    override fun getGroup(name: String): PermissionGroup? = groups[name]

    override fun getGroups(): Collection<PermissionGroup> = groups.values

    override fun getDefaultGroup(): PermissionGroup =
        groups["default"] ?: error("默认组不存在")

    override suspend fun getPlayerGroups(playerId: String): List<PermissionGroup> {
        val groupNames = playerGroups[playerId] ?: return listOf(getDefaultGroup())
        if (groupNames.isEmpty()) return listOf(getDefaultGroup())
        return groupNames.mapNotNull { groups[it] }.sortedByDescending { it.priority }
    }

    override suspend fun addPlayerToGroup(playerId: String, groupName: String) {
        require(groups.containsKey(groupName)) { "权限组 $groupName 不存在" }
        playerGroups.getOrPut(playerId) { mutableSetOf() }.add(groupName)
        logger.debug { "玩家 $playerId 加入组 $groupName" }
    }

    override suspend fun removePlayerFromGroup(playerId: String, groupName: String) {
        playerGroups[playerId]?.remove(groupName)
    }

    override suspend fun setPlayerPermission(playerId: String, permission: String, value: Boolean) {
        playerPermissions.getOrPut(playerId) { mutableMapOf() }[permission] = value
    }

    override suspend fun unsetPlayerPermission(playerId: String, permission: String) {
        playerPermissions[playerId]?.remove(permission)
    }

    /**
     * 检查玩家是否拥有指定权限（考虑组继承）
     */
    fun hasPermission(playerId: String, permission: String, isOp: Boolean = false): Boolean {
        // 1. 检查玩家直接权限
        val directPerms = playerPermissions[playerId]
        if (directPerms != null && directPerms.containsKey(permission)) {
            return directPerms[permission]!!
        }

        // 2. 检查玩家所属组的权限（含继承）
        val groupNames = playerGroups[playerId]
        val effectiveGroups = mutableSetOf<String>()
        if (groupNames != null && groupNames.isNotEmpty()) {
            for (gn in groupNames) {
                collectGroupsRecursive(gn, effectiveGroups)
            }
        } else {
            collectGroupsRecursive("default", effectiveGroups)
        }

        for (gn in effectiveGroups) {
            val group = groups[gn] ?: continue
            if (group.hasPermission(permission)) return true
        }

        // 3. 检查注册权限的默认值
        val registered = registeredPermissions[permission]
        if (registered != null) {
            return when (registered.default) {
                PermissionDefault.TRUE -> true
                PermissionDefault.FALSE -> false
                PermissionDefault.OP -> isOp
                PermissionDefault.NOT_OP -> !isOp
            }
        }

        return false
    }

    private fun collectGroupsRecursive(groupName: String, collected: MutableSet<String>) {
        if (!collected.add(groupName)) return // 防止循环继承
        val group = groups[groupName] ?: return
        for (parent in group.inheritance) {
            collectGroupsRecursive(parent, collected)
        }
    }
}
