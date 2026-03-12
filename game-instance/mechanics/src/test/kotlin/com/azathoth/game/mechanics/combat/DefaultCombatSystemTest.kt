package com.azathoth.game.mechanics.combat

import com.azathoth.game.engine.entity.*
import com.azathoth.game.engine.world.DefaultWorldManager
import com.azathoth.game.engine.world.WorldType
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultCombatSystemTest {
    private lateinit var combatSystem: DefaultCombatSystem
    private lateinit var attacker: DefaultLivingEntity
    private lateinit var defender: DefaultLivingEntity

    @BeforeEach
    fun setup() = runTest {
        combatSystem = DefaultCombatSystem()
        val worldManager = DefaultWorldManager()
        val world = worldManager.createWorld("test", WorldType.TEST)
        attacker = DefaultLivingEntity(EntityType.MOB, world)
        defender = DefaultLivingEntity(EntityType.MOB, world)
        attacker.maxHealth = 100.0
        attacker.health = 100.0
        defender.maxHealth = 100.0
        defender.health = 100.0
    }

    private fun createContext(baseDamage: Double = 20.0): DefaultDamageContext {
        val source = SimpleDamageSource(DamageType.PHYSICAL, attacker, DamageCause.ENTITY_ATTACK)
        return DefaultDamageContext(attacker, defender, source, baseDamage, DamageType.PHYSICAL)
    }

    @Test
    fun `calculateDamage should apply defense reduction`() {
        // 默认防御 5.0, 无穿透
        // damage = 20 * (100 / (100 + 5)) = 19.047...
        val context = createContext(20.0)
        val damage = combatSystem.calculateDamage(context)

        assertTrue(damage > 0)
        assertTrue(damage < 20.0) // 防御减免后应小于基础伤害
    }

    @Test
    fun `cancelled context should return zero damage`() {
        val context = createContext()
        context.isCancelled = true
        val damage = combatSystem.calculateDamage(context)
        assertEquals(0.0, damage)
    }

    @Test
    fun `modifier should be applied in priority order`() {
        val log = mutableListOf<String>()

        combatSystem.registerModifier(object : DamageModifier {
            override val name = "second"
            override val priority = 10
            override fun modify(context: DamageContext) {
                log.add("second")
                context.finalDamage *= 1.5
            }
        })

        combatSystem.registerModifier(object : DamageModifier {
            override val name = "first"
            override val priority = 1
            override fun modify(context: DamageContext) {
                log.add("first")
                context.finalDamage += 10
            }
        })

        val context = createContext(20.0)
        combatSystem.calculateDamage(context)

        assertEquals(listOf("first", "second"), log)
    }

    @Test
    fun `applyDamage should reduce defender health`() = runTest {
        // 设置 0 防御以简化测试
        combatSystem.setCombatStats(attacker, DefaultCombatStats(attack = 10.0, critRate = 0.0))
        combatSystem.setCombatStats(defender, DefaultCombatStats(defense = 0.0, dodgeRate = 0.0, blockRate = 0.0))

        val context = createContext(20.0)
        combatSystem.applyDamage(context)

        assertTrue(defender.health < 100.0)
    }

    @Test
    fun `canAttack should return false for dead entities`() = runTest {
        attacker.kill()
        assertFalse(combatSystem.canAttack(attacker, defender))
    }

    @Test
    fun `canAttack should return false for self attack`() {
        assertFalse(combatSystem.canAttack(attacker, attacker))
    }

    @Test
    fun `unregisterModifier should remove modifier`() {
        combatSystem.registerModifier(object : DamageModifier {
            override val name = "test"
            override val priority = 0
            override fun modify(context: DamageContext) {
                context.isCancelled = true
            }
        })

        combatSystem.unregisterModifier("test")

        val context = createContext()
        combatSystem.calculateDamage(context)
        assertFalse(context.isCancelled)
    }

    @Test
    fun `penetration should reduce effective defense`() {
        // 高穿透应该让更多伤害穿过
        combatSystem.setCombatStats(attacker, DefaultCombatStats(penetration = 100.0, critRate = 0.0))
        combatSystem.setCombatStats(defender, DefaultCombatStats(defense = 50.0, dodgeRate = 0.0, blockRate = 0.0))

        val context = createContext(100.0)
        val damage = combatSystem.calculateDamage(context)

        // 穿透100 > 防御50, 有效防御=0, damage = 100 * (100/100) = 100
        assertEquals(100.0, damage, 0.01)
    }
}
