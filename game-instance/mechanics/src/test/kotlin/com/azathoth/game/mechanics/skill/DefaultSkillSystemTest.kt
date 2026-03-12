package com.azathoth.game.mechanics.skill

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.game.engine.entity.EntityType
import com.azathoth.game.engine.entity.LivingEntity
import com.azathoth.game.engine.player.DefaultGamePlayer
import com.azathoth.game.engine.world.DefaultWorldManager
import com.azathoth.game.engine.world.WorldType
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class DefaultSkillSystemTest {
    private lateinit var skillSystem: DefaultSkillSystem
    private lateinit var player: DefaultGamePlayer

    @BeforeEach
    fun setup() = runTest {
        skillSystem = DefaultSkillSystem()
        val worldManager = DefaultWorldManager()
        val world = worldManager.createWorld("test", WorldType.TEST)
        player = DefaultGamePlayer(PlayerId.of("test-player"), "TestPlayer", world)
    }

    private fun createTestSkill(id: String = "fireball", cooldown: Duration = 5.seconds): Skill {
        return object : Skill {
            override val skillId = id
            override val name = "Fireball"
            override val description = "A ball of fire"
            override val type = SkillType.ACTIVE
            override val targetType = TargetType.SINGLE
            override val maxLevel = 5
            override fun getCooldown(level: Int) = cooldown
            override fun getCost(level: Int) = 10.0
            override fun getRange(level: Int) = 20.0
            override fun canUse(caster: LivingEntity, level: Int) = DefaultSkillUseResult.success()
            override suspend fun use(context: SkillContext): SkillUseResult = DefaultSkillUseResult.success("技能释放成功")
        }
    }

    @Test
    fun `registerSkill should add skill`() {
        val skill = createTestSkill()
        skillSystem.registerSkill(skill)

        assertNotNull(skillSystem.getSkill("fireball"))
        assertEquals(1, skillSystem.getAllSkills().size)
    }

    @Test
    fun `useSkill should fail if skill not registered`() = runTest {
        val result = skillSystem.useSkill(player, "nonexistent")
        assertFalse(result.success)
        assertEquals(SkillFailReason.CONDITION_NOT_MET, result.failReason)
    }

    @Test
    fun `useSkill should fail if player has not learned skill`() = runTest {
        skillSystem.registerSkill(createTestSkill())
        val result = skillSystem.useSkill(player, "fireball")
        assertFalse(result.success)
        assertEquals(SkillFailReason.CONDITION_NOT_MET, result.failReason)
    }

    @Test
    fun `useSkill should succeed after learning skill`() = runTest {
        skillSystem.registerSkill(createTestSkill())
        val data = skillSystem.getPlayerSkillData(player)
        data.learnSkill("fireball")

        val result = skillSystem.useSkill(player, "fireball")
        assertTrue(result.success)
    }

    @Test
    fun `useSkill should fail on cooldown`() = runTest {
        skillSystem.registerSkill(createTestSkill(cooldown = 60.seconds))
        val data = skillSystem.getPlayerSkillData(player)
        data.learnSkill("fireball")

        // 第一次使用成功
        val result1 = skillSystem.useSkill(player, "fireball")
        assertTrue(result1.success)

        // 第二次使用应该冷却中
        val result2 = skillSystem.useSkill(player, "fireball")
        assertFalse(result2.success)
        assertEquals(SkillFailReason.ON_COOLDOWN, result2.failReason)
    }

    @Test
    fun `player skill data should track learned skills`() = runTest {
        val data = skillSystem.getPlayerSkillData(player) as DefaultPlayerSkillData

        assertTrue(data.learnSkill("skill_a"))
        assertTrue(data.learnSkill("skill_b"))
        assertFalse(data.learnSkill("skill_a")) // 重复学习

        assertEquals(1, data.getSkillLevel("skill_a"))
        assertEquals(2, data.getLearnedSkills().size)
    }

    @Test
    fun `upgradeSkill should increase level`() = runTest {
        val data = skillSystem.getPlayerSkillData(player) as DefaultPlayerSkillData
        data.learnSkill("skill_a")

        assertTrue(data.upgradeSkill("skill_a"))
        assertEquals(2, data.getSkillLevel("skill_a"))
    }

    @Test
    fun `forgetSkill should remove skill`() = runTest {
        val data = skillSystem.getPlayerSkillData(player) as DefaultPlayerSkillData
        data.learnSkill("skill_a")

        assertTrue(data.forgetSkill("skill_a"))
        assertEquals(0, data.getSkillLevel("skill_a"))
        assertFalse(data.forgetSkill("skill_a")) // 已遗忘
    }

    @Test
    fun `resetAllCooldowns should clear all cooldowns`() = runTest {
        val data = skillSystem.getPlayerSkillData(player) as DefaultPlayerSkillData
        data.setCooldown("a", 60.seconds)
        data.setCooldown("b", 60.seconds)

        data.resetAllCooldowns()

        assertEquals(Duration.ZERO, data.getCooldownRemaining("a"))
        assertEquals(Duration.ZERO, data.getCooldownRemaining("b"))
    }
}
