package com.azathoth.game.mechanics.skill

import com.azathoth.game.engine.entity.LivingEntity
import com.azathoth.game.engine.player.GamePlayer
import com.azathoth.game.engine.world.WorldPosition
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private val logger = KotlinLogging.logger {}

/**
 * 默认技能使用结果
 */
data class DefaultSkillUseResult(
    override val success: Boolean,
    override val failReason: SkillFailReason? = null,
    override val message: String? = null
) : SkillUseResult {
    companion object {
        fun success(message: String? = null) = DefaultSkillUseResult(true, message = message)
        fun fail(reason: SkillFailReason, message: String? = null) = DefaultSkillUseResult(false, reason, message)
    }
}

/**
 * 默认技能上下文
 */
data class DefaultSkillContext(
    override val caster: LivingEntity,
    override val level: Int,
    override val targetEntity: LivingEntity? = null,
    override val targetPosition: WorldPosition? = null,
    override val parameters: Map<String, Any> = emptyMap()
) : SkillContext

/**
 * 默认玩家技能数据
 */
class DefaultPlayerSkillData(
    override val player: GamePlayer
) : PlayerSkillData {
    private val learnedSkills = ConcurrentHashMap<String, Int>()
    private val cooldowns = ConcurrentHashMap<String, Long>() // skillId -> expiry timestamp

    override fun getLearnedSkills(): Map<String, Int> = learnedSkills.toMap()

    override fun getSkillLevel(skillId: String): Int = learnedSkills[skillId] ?: 0

    override suspend fun learnSkill(skillId: String): Boolean {
        if (learnedSkills.containsKey(skillId)) return false
        learnedSkills[skillId] = 1
        logger.debug { "玩家 ${player.name} 学习技能 $skillId" }
        return true
    }

    override suspend fun upgradeSkill(skillId: String): Boolean {
        val current = learnedSkills[skillId] ?: return false
        learnedSkills[skillId] = current + 1
        logger.debug { "玩家 ${player.name} 升级技能 $skillId -> ${current + 1}" }
        return true
    }

    override suspend fun forgetSkill(skillId: String): Boolean {
        val removed = learnedSkills.remove(skillId) != null
        if (removed) {
            cooldowns.remove(skillId)
            logger.debug { "玩家 ${player.name} 遗忘技能 $skillId" }
        }
        return removed
    }

    override fun getCooldownRemaining(skillId: String): Duration {
        val expiry = cooldowns[skillId] ?: return Duration.ZERO
        val remaining = expiry - System.currentTimeMillis()
        return if (remaining > 0) remaining.milliseconds else Duration.ZERO
    }

    override fun setCooldown(skillId: String, duration: Duration) {
        cooldowns[skillId] = System.currentTimeMillis() + duration.inWholeMilliseconds
    }

    override fun resetCooldown(skillId: String) {
        cooldowns.remove(skillId)
    }

    override fun resetAllCooldowns() {
        cooldowns.clear()
    }
}

/**
 * 默认技能系统实现
 */
class DefaultSkillSystem : SkillSystem {
    private val skills = ConcurrentHashMap<String, Skill>()
    private val playerData = ConcurrentHashMap<Int, DefaultPlayerSkillData>()

    override fun registerSkill(skill: Skill) {
        skills[skill.skillId] = skill
        logger.info { "注册技能: ${skill.skillId} (${skill.name})" }
    }

    override fun getSkill(skillId: String): Skill? = skills[skillId]

    override fun getAllSkills(): Collection<Skill> = skills.values

    override suspend fun useSkill(
        caster: LivingEntity,
        skillId: String,
        target: LivingEntity?,
        targetPosition: WorldPosition?
    ): SkillUseResult {
        val skill = skills[skillId]
            ?: return DefaultSkillUseResult.fail(SkillFailReason.CONDITION_NOT_MET, "技能不存在: $skillId")

        // 获取技能等级
        val level = if (caster is GamePlayer) {
            val data = getPlayerSkillData(caster)
            val lvl = data.getSkillLevel(skillId)
            if (lvl <= 0) {
                return DefaultSkillUseResult.fail(SkillFailReason.CONDITION_NOT_MET, "未学习该技能")
            }

            // 冷却检测
            val cd = data.getCooldownRemaining(skillId)
            if (cd > Duration.ZERO) {
                return DefaultSkillUseResult.fail(SkillFailReason.ON_COOLDOWN, "技能冷却中: ${cd.inWholeSeconds}s")
            }
            lvl
        } else {
            1
        }

        // 前置检查
        val canUse = skill.canUse(caster, level)
        if (!canUse.success) return canUse

        // 构建上下文并执行
        val context = DefaultSkillContext(caster, level, target, targetPosition)
        val result = skill.use(context)

        // 成功则设置冷却
        if (result.success && caster is GamePlayer) {
            val data = getPlayerSkillData(caster)
            val cooldown = skill.getCooldown(level)
            if (cooldown > Duration.ZERO) {
                data.setCooldown(skillId, cooldown)
            }
        }

        logger.debug { "技能使用: ${caster.entityId} -> $skillId, 结果=${result.success}" }
        return result
    }

    override fun getPlayerSkillData(player: GamePlayer): PlayerSkillData {
        return playerData.getOrPut(player.entityId) { DefaultPlayerSkillData(player) }
    }
}
