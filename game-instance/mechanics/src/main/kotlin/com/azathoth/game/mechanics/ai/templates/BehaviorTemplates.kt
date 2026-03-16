package com.azathoth.game.mechanics.ai.templates

import com.azathoth.game.engine.world.WorldPosition
import com.azathoth.game.mechanics.ai.BehaviorTree
import com.azathoth.game.mechanics.ai.behaviorTree
import com.azathoth.game.mechanics.ai.nodes.actions.*
import com.azathoth.game.mechanics.ai.nodes.conditions.*
import com.azathoth.game.mechanics.ai.CompositeBuilder
import com.azathoth.game.mechanics.ai.BehaviorNode
import com.azathoth.game.mechanics.ai.perception.PerceptionSystem

/**
 * 预制行为树模板
 *
 * 用 DSL 定义常用 AI 行为模式。
 */
object BehaviorTemplates {

    /**
     * 近战攻击型 AI 配置
     */
    data class MeleeConfig(
        val sightRange: Double = 16.0,
        val chaseSpeed: Double = 0.2,
        val attackDamage: Double = 5.0,
        val attackRange: Double = 2.5,
        val fleeHealthPercent: Double = 0.2,
        val fleeSpeed: Double = 0.25,
        val fleeSafeDistance: Double = 20.0
    )

    /**
     * 远程攻击型 AI 配置
     */
    data class RangedConfig(
        val sightRange: Double = 24.0,
        val chaseSpeed: Double = 0.15,
        val attackDamage: Double = 3.0,
        val attackRange: Double = 16.0,
        val projectileSpeed: Double = 1.0,
        val fleeHealthPercent: Double = 0.3,
        val fleeSpeed: Double = 0.2,
        val fleeSafeDistance: Double = 20.0,
        val preferredDistance: Double = 10.0
    )

    /**
     * 近战攻击型 — 寻敌→追击→攻击，低血逃跑
     */
    fun aggressiveMelee(
        perception: PerceptionSystem,
        config: MeleeConfig = MeleeConfig()
    ): BehaviorTree = behaviorTree("aggressiveMelee") {
        selector("root") {
            // 优先级 1：低血逃跑
            sequence("fleeWhenLow") {
                node(IsHealthBelow(config.fleeHealthPercent))
                node(Flee(config.fleeSpeed, config.fleeSafeDistance))
            }
            // 优先级 2：有目标则追击攻击
            sequence("combatLoop") {
                // 确保有目标
                selector("ensureTarget") {
                    node(HasTarget())
                    node(FindTarget(perception, config.sightRange))
                }
                node(IsTargetAlive())
                // 追击或攻击
                selector("engageTarget") {
                    sequence("tryAttack") {
                        node(IsTargetInRange(config.attackRange))
                        node(MeleeAttack(config.attackDamage, config.attackRange))
                    }
                    node(ChaseTarget(config.chaseSpeed, config.attackRange))
                }
            }
            // 优先级 3：空闲
            node(Idle(40, 80))
        }
    }

    /**
     * 远程攻击型 — 保持距离射击
     */
    fun aggressiveRanged(
        perception: PerceptionSystem,
        config: RangedConfig = RangedConfig()
    ): BehaviorTree = behaviorTree("aggressiveRanged") {
        selector("root") {
            // 优先级 1：低血逃跑
            sequence("fleeWhenLow") {
                node(IsHealthBelow(config.fleeHealthPercent))
                node(Flee(config.fleeSpeed, config.fleeSafeDistance))
            }
            // 优先级 2：战斗
            sequence("combatLoop") {
                selector("ensureTarget") {
                    node(HasTarget())
                    node(FindTarget(perception, config.sightRange))
                }
                node(IsTargetAlive())
                selector("engageTarget") {
                    // 在射程内直接攻击
                    sequence("tryRangedAttack") {
                        node(IsTargetInRange(config.attackRange))
                        node(RangedAttack(config.attackDamage, config.attackRange, config.projectileSpeed))
                    }
                    // 不在射程内则追击
                    node(ChaseTarget(config.chaseSpeed, config.attackRange))
                }
            }
            // 优先级 3：空闲
            node(Idle(40, 80))
        }
    }

    /**
     * 被动巡逻型 — 巡逻路径点，被攻击后逃跑
     */
    fun passiveWanderer(
        waypoints: List<WorldPosition>,
        fleeSpeed: Double = 0.2,
        fleeSafeDistance: Double = 15.0,
        patrolSpeed: Double = 0.1
    ): BehaviorTree = behaviorTree("passiveWanderer") {
        selector("root") {
            // 被攻击后逃跑（有仇恨目标时）
            sequence("fleeFromThreat") {
                node(HasThreat())
                node(Flee(fleeSpeed, fleeSafeDistance))
            }
            // 正常巡逻
            node(Patrol(waypoints, patrolSpeed))
            // 空闲
            node(Idle(60, 120))
        }
    }

    /**
     * Boss 多阶段 AI
     *
     * @param phases 阶段列表，每个阶段包含血量阈值和对应的行为节点
     */
    fun bossAI(
        perception: PerceptionSystem,
        phases: List<BossPhase>
    ): BehaviorTree = behaviorTree("bossAI") {
        selector("root") {
            // 按阶段优先级（血量从低到高检查）
            for (phase in phases.sortedBy { it.healthThreshold }) {
                sequence("phase_${phase.name}") {
                    node(IsHealthBelow(phase.healthThreshold))
                    node(phase.behaviorNode)
                }
            }
            // 默认行为：近战攻击
            sequence("defaultCombat") {
                selector("ensureTarget") {
                    node(HasTarget())
                    node(FindTarget(perception, 20.0))
                }
                node(IsTargetAlive())
                selector("engage") {
                    sequence("tryAttack") {
                        node(IsTargetInRange(3.0))
                        node(MeleeAttack(10.0, 3.0))
                    }
                    node(ChaseTarget(0.25, 3.0))
                }
            }
        }
    }

    /**
     * Boss 阶段定义
     */
    data class BossPhase(
        /** 阶段名称 */
        val name: String,
        /** 血量阈值（0.0~1.0），低于此值触发 */
        val healthThreshold: Double,
        /** 该阶段的行为节点 */
        val behaviorNode: BehaviorNode
    )
}
