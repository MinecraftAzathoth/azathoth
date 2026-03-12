package com.azathoth.sdk.api.permission

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultPermissionManagerTest {

    private lateinit var manager: DefaultPermissionManager

    @BeforeEach
    fun setup() {
        manager = DefaultPermissionManager()
    }

    @Test
    fun `default group exists on init`() {
        val defaultGroup = manager.getDefaultGroup()
        assertNotNull(defaultGroup)
        assertEquals("default", defaultGroup.name)
        assertTrue(defaultGroup.isDefault)
    }

    @Test
    fun `create and retrieve group`() = runTest {
        val group = manager.createGroup("admin")
        assertNotNull(group)
        assertEquals("admin", group.name)
        assertEquals(group, manager.getGroup("admin"))
    }

    @Test
    fun `delete group`() = runTest {
        manager.createGroup("temp")
        assertNotNull(manager.getGroup("temp"))

        manager.deleteGroup("temp")
        assertNull(manager.getGroup("temp"))
    }

    @Test
    fun `cannot delete default group`() = runTest {
        assertThrows(IllegalArgumentException::class.java) {
            kotlinx.coroutines.runBlocking { manager.deleteGroup("default") }
        }
    }

    @Test
    fun `register and retrieve permission`() {
        val perm = SimplePermission(
            node = "test.permission",
            description = "测试权限",
            default = PermissionDefault.FALSE
        )

        manager.registerPermission(perm)
        assertEquals(perm, manager.getPermission("test.permission"))

        manager.unregisterPermission("test.permission")
        assertNull(manager.getPermission("test.permission"))
    }

    @Test
    fun `player group assignment`() = runTest {
        manager.createGroup("vip")

        manager.addPlayerToGroup("player1", "vip")
        val groups = manager.getPlayerGroups("player1")

        assertTrue(groups.any { it.name == "vip" })
    }

    @Test
    fun `player without group gets default group`() = runTest {
        val groups = manager.getPlayerGroups("newPlayer")
        assertEquals(1, groups.size)
        assertEquals("default", groups[0].name)
    }

    @Test
    fun `remove player from group`() = runTest {
        manager.createGroup("builder")
        manager.addPlayerToGroup("player1", "builder")

        manager.removePlayerFromGroup("player1", "builder")
        val groups = manager.getPlayerGroups("player1")

        // 回退到默认组
        assertEquals(1, groups.size)
        assertEquals("default", groups[0].name)
    }

    @Test
    fun `player direct permission`() = runTest {
        manager.setPlayerPermission("player1", "fly.use", true)
        assertTrue(manager.hasPermission("player1", "fly.use"))

        manager.unsetPlayerPermission("player1", "fly.use")
        assertFalse(manager.hasPermission("player1", "fly.use"))
    }

    @Test
    fun `group permission check`() = runTest {
        val group = manager.createGroup("mod") as SimplePermissionGroup
        group.addPermission("kick.use")
        group.addPermission("ban.use")

        manager.addPlayerToGroup("player1", "mod")

        assertTrue(manager.hasPermission("player1", "kick.use"))
        assertTrue(manager.hasPermission("player1", "ban.use"))
        assertFalse(manager.hasPermission("player1", "fly.use"))
    }

    @Test
    fun `group inheritance`() = runTest {
        val admin = manager.createGroup("admin") as SimplePermissionGroup
        admin.addPermission("admin.panel")

        val superAdmin = manager.createGroup("superadmin") as SimplePermissionGroup
        superAdmin.addPermission("server.stop")
        superAdmin.addInheritance("admin")

        manager.addPlayerToGroup("player1", "superadmin")

        // superadmin 继承 admin 的权限
        assertTrue(manager.hasPermission("player1", "server.stop"))
        assertTrue(manager.hasPermission("player1", "admin.panel"))
    }

    @Test
    fun `permission default values`() {
        manager.registerPermission(
            SimplePermission("always.true", default = PermissionDefault.TRUE)
        )
        manager.registerPermission(
            SimplePermission("op.only", default = PermissionDefault.OP)
        )

        assertTrue(manager.hasPermission("anyone", "always.true"))
        assertFalse(manager.hasPermission("anyone", "op.only", isOp = false))
        assertTrue(manager.hasPermission("anyone", "op.only", isOp = true))
    }

    @Test
    fun `direct permission overrides group permission`() = runTest {
        val group = manager.createGroup("builder") as SimplePermissionGroup
        group.addPermission("build.use")

        manager.addPlayerToGroup("player1", "builder")

        // 直接设置为 false 覆盖组权限
        manager.setPlayerPermission("player1", "build.use", false)
        assertFalse(manager.hasPermission("player1", "build.use"))
    }

    @Test
    fun `delete group removes from players`() = runTest {
        manager.createGroup("temp")
        manager.addPlayerToGroup("player1", "temp")

        assertTrue(manager.getPlayerGroups("player1").any { it.name == "temp" })

        manager.deleteGroup("temp")

        // 玩家回退到默认组
        val groups = manager.getPlayerGroups("player1")
        assertEquals(1, groups.size)
        assertEquals("default", groups[0].name)
    }

    @Test
    fun `circular inheritance does not cause infinite loop`() = runTest {
        val groupA = manager.createGroup("groupA") as SimplePermissionGroup
        groupA.addPermission("perm.a")
        groupA.addInheritance("groupB")

        val groupB = manager.createGroup("groupB") as SimplePermissionGroup
        groupB.addPermission("perm.b")
        groupB.addInheritance("groupA")

        manager.addPlayerToGroup("player1", "groupA")

        // 不应死循环，且两个权限都能获取
        assertTrue(manager.hasPermission("player1", "perm.a"))
        assertTrue(manager.hasPermission("player1", "perm.b"))
    }
}
