package com.azathoth.game.mechanics.skill

import com.azathoth.game.engine.entity.LivingEntity
import com.azathoth.game.engine.player.GamePlayer
import com.azathoth.game.engine.world.WorldPosition
import kotlin.time.Duration

/**
 * 技能类型
 */
enum class SkillType {
    /** 主动技能 */
    ACTIVE,
    /** 被动技能 */
    PASSIVE,
    /** 切换技能 */
    TOGGLE,
    /** 蓄力技能 */
    CHANNELED
}

/**
 * 技能目标类型
 */
enum class TargetType {
    /** 自身 */
    SELF,
    /** 单体目标 */
    SINGLE,
    /** 区域 */
    AREA,
    /** 锥形 */
    CONE,
    /** 直线 */
    LINE,
    /** 全局 */
    GLOBAL
}

/**
 * 技能定义
 */
interface Skill {
    /** 技能ID */
    val skillId: String
    
    /** 技能名称 */
    val name: String
    
    /** 技能描述 */
    val description: String
    
    /** 技能类型 */
    val type: SkillType
    
    /** 目标类型 */
    val targetType: TargetType
    
    /** 最大等级 */
    val maxLevel: Int
    
    /** 冷却时间 */
    fun getCooldown(level: Int): Duration
    
    /** 消耗（如魔力） */
    fun getCost(level: Int): Double
    
    /** 施法距离 */
    fun getRange(level: Int): Double
    
    /** 是否可以使用 */
    fun canUse(caster: LivingEntity, level: Int): SkillUseResult
    
    /** 使用技能 */
    suspend fun use(context: SkillContext): SkillUseResult
}

/**
 * 技能上下文
 */
interface SkillContext {
    /** 施法者 */
    val caster: LivingEntity
    
    /** 技能等级 */
    val level: Int
    
    /** 目标实体 */
    val targetEntity: LivingEntity?
    
    /** 目标位置 */
    val targetPosition: WorldPosition?
    
    /** 附加参数 */
    val parameters: Map<String, Any>
}

/**
 * 技能使用结果
 */
interface SkillUseResult {
    /** 是否成功 */
    val success: Boolean
    
    /** 失败原因 */
    val failReason: SkillFailReason?
    
    /** 消息 */
    val message: String?
}

/**
 * 技能失败原因
 */
enum class SkillFailReason {
    /** 冷却中 */
    ON_COOLDOWN,
    /** 资源不足 */
    INSUFFICIENT_RESOURCE,
    /** 距离过远 */
    OUT_OF_RANGE,
    /** 无效目标 */
    INVALID_TARGET,
    /** 被沉默 */
    SILENCED,
    /** 被眩晕 */
    STUNNED,
    /** 条件不满足 */
    CONDITION_NOT_MET,
    /** 取消 */
    CANCELLED
}

/**
 * 玩家技能数据
 */
interface PlayerSkillData {
    /** 玩家 */
    val player: GamePlayer
    
    /** 已学习的技能 */
    fun getLearnedSkills(): Map<String, Int>
    
    /** 获取技能等级 */
    fun getSkillLevel(skillId: String): Int
    
    /** 学习技能 */
    suspend fun learnSkill(skillId: String): Boolean
    
    /** 升级技能 */
    suspend fun upgradeSkill(skillId: String): Boolean
    
    /** 遗忘技能 */
    suspend fun forgetSkill(skillId: String): Boolean
    
    /** 获取冷却剩余时间 */
    fun getCooldownRemaining(skillId: String): Duration
    
    /** 设置冷却 */
    fun setCooldown(skillId: String, duration: Duration)
    
    /** 重置冷却 */
    fun resetCooldown(skillId: String)
    
    /** 重置所有冷却 */
    fun resetAllCooldowns()
}

/**
 * 技能系统
 */
interface SkillSystem {
    /**
     * 注册技能
     */
    fun registerSkill(skill: Skill)
    
    /**
     * 获取技能
     */
    fun getSkill(skillId: String): Skill?
    
    /**
     * 获取所有技能
     */
    fun getAllSkills(): Collection<Skill>
    
    /**
     * 使用技能
     */
    suspend fun useSkill(
        caster: LivingEntity,
        skillId: String,
        target: LivingEntity? = null,
        targetPosition: WorldPosition? = null
    ): SkillUseResult
    
    /**
     * 获取玩家技能数据
     */
    fun getPlayerSkillData(player: GamePlayer): PlayerSkillData
}
