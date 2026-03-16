package com.azathoth.game.mechanics.ai.mob

import com.azathoth.game.engine.entity.DefaultLivingEntity
import com.azathoth.game.engine.world.World
import com.azathoth.game.mechanics.ai.controller.AIController
import com.azathoth.game.mechanics.ai.perception.PerceptionSystem
import com.azathoth.game.mechanics.ai.threat.ThreatTable
import java.util.UUID

/**
 * Mob 实体
 *
 * 基于 [MobDefinition] 创建的带 AI 的生物实体。
 * 持有 [AIController] 和 [ThreatTable]。
 */
class MobEntity(
    val definition: MobDefinition,
    world: World,
    perception: PerceptionSystem,
    uuid: UUID = UUID.randomUUID()
) : DefaultLivingEntity(definition.entityType, world, uuid) {

    /** 仇恨表 */
    val threatTable = ThreatTable()

    /** AI 控制器 */
    val aiController: AIController

    init {
        // 从定义初始化属性
        maxHealth = definition.maxHealth
        health = definition.maxHealth
        customName = definition.name

        // 创建行为树并绑定控制器
        val behaviorTree = definition.behaviorTreeFactory(perception)
        aiController = AIController(this, behaviorTree)

        // 将仇恨表和定义信息写入黑板
        aiController.context.set("threatTable", threatTable)
        aiController.context.set("definition", definition)
    }

    /**
     * 驱动 AI tick
     */
    suspend fun tickAI(tickNumber: Long) {
        aiController.tick(tickNumber)
    }
}
