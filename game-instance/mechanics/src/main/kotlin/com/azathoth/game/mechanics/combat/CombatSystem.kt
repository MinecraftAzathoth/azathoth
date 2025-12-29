package com.azathoth.game.mechanics.combat

import com.azathoth.game.engine.entity.DamageSource
import com.azathoth.game.engine.entity.DamageType
import com.azathoth.game.engine.entity.LivingEntity

/**
 * 战斗属性
 */
interface CombatStats {
    /** 攻击力 */
    val attack: Double
    
    /** 防御力 */
    val defense: Double
    
    /** 暴击率 */
    val critRate: Double
    
    /** 暴击伤害 */
    val critDamage: Double
    
    /** 攻击速度 */
    val attackSpeed: Double
    
    /** 生命偷取 */
    val lifeSteal: Double
    
    /** 穿透 */
    val penetration: Double
    
    /** 格挡率 */
    val blockRate: Double
    
    /** 闪避率 */
    val dodgeRate: Double
}

/**
 * 伤害计算上下文
 */
interface DamageContext {
    /** 攻击者 */
    val attacker: LivingEntity?
    
    /** 防御者 */
    val defender: LivingEntity
    
    /** 伤害来源 */
    val source: DamageSource
    
    /** 原始伤害 */
    val baseDamage: Double
    
    /** 伤害类型 */
    val damageType: DamageType
    
    /** 是否暴击 */
    var isCritical: Boolean
    
    /** 最终伤害 */
    var finalDamage: Double
    
    /** 是否被格挡 */
    var isBlocked: Boolean
    
    /** 是否被闪避 */
    var isDodged: Boolean
    
    /** 是否被取消 */
    var isCancelled: Boolean
}

/**
 * 伤害修改器
 */
interface DamageModifier {
    /** 修改器名称 */
    val name: String
    
    /** 优先级 */
    val priority: Int
    
    /** 修改伤害 */
    fun modify(context: DamageContext)
}

/**
 * 战斗系统
 */
interface CombatSystem {
    /**
     * 计算伤害
     */
    fun calculateDamage(context: DamageContext): Double
    
    /**
     * 应用伤害
     */
    suspend fun applyDamage(context: DamageContext)
    
    /**
     * 注册伤害修改器
     */
    fun registerModifier(modifier: DamageModifier)
    
    /**
     * 移除伤害修改器
     */
    fun unregisterModifier(name: String)
    
    /**
     * 获取实体的战斗属性
     */
    fun getCombatStats(entity: LivingEntity): CombatStats
    
    /**
     * 检查是否可以攻击
     */
    fun canAttack(attacker: LivingEntity, target: LivingEntity): Boolean
}

/**
 * 战斗事件监听器
 */
interface CombatListener {
    /** 攻击前 */
    suspend fun onPreDamage(context: DamageContext)
    
    /** 攻击后 */
    suspend fun onPostDamage(context: DamageContext)
    
    /** 实体死亡 */
    suspend fun onEntityDeath(entity: LivingEntity, killer: LivingEntity?)
}
