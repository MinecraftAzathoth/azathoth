package com.azathoth.game.mechanics.ai.mob

import com.azathoth.game.engine.entity.EntityType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MobRegistryTest {

    private lateinit var registry: DefaultMobRegistry

    @BeforeEach
    fun setup() {
        registry = DefaultMobRegistry()
    }

    @Test
    fun `register 和 get`() {
        val def = createDefinition("zombie")
        registry.register(def)
        assertEquals(def, registry.get("zombie"))
    }

    @Test
    fun `get 不存在的 id 返回 null`() {
        assertNull(registry.get("nonexistent"))
    }

    @Test
    fun `contains 检查是否已注册`() {
        val def = createDefinition("skeleton")
        assertFalse(registry.contains("skeleton"))
        registry.register(def)
        assertTrue(registry.contains("skeleton"))
    }

    @Test
    fun `getAll 返回所有定义`() {
        registry.register(createDefinition("zombie"))
        registry.register(createDefinition("skeleton"))
        registry.register(createDefinition("creeper"))
        assertEquals(3, registry.getAll().size)
    }

    @Test
    fun `register 相同 id 覆盖旧定义`() {
        val def1 = createDefinition("zombie", maxHealth = 20.0)
        val def2 = createDefinition("zombie", maxHealth = 40.0)
        registry.register(def1)
        registry.register(def2)
        assertEquals(40.0, registry.get("zombie")?.maxHealth)
    }

    private fun createDefinition(id: String, maxHealth: Double = 20.0) = MobDefinition(
        id = id,
        name = id.replaceFirstChar { it.uppercase() },
        entityType = EntityType.MOB,
        maxHealth = maxHealth,
        attack = 5.0,
        defense = 0.0,
        sightRange = 16.0,
        moveSpeed = 0.2,
        behaviorTreeFactory = { perception ->
            com.azathoth.game.mechanics.ai.behaviorTree("test") {
                selector {
                    action("idle") { com.azathoth.game.mechanics.ai.NodeStatus.SUCCESS }
                }
            }
        }
    )
}
