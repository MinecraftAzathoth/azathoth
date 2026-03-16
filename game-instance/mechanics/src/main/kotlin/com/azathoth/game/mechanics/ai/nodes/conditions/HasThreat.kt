package com.azathoth.game.mechanics.ai.nodes.conditions

import com.azathoth.game.mechanics.ai.BehaviorNode
import com.azathoth.game.mechanics.ai.NodeStatus
import com.azathoth.game.mechanics.ai.context.BehaviorContext
import com.azathoth.game.mechanics.ai.threat.ThreatTable

/**
 * 条件：仇恨表是否非空
 */
class HasThreat(name: String = "hasThreat") : BehaviorNode(name) {

    override suspend fun tick(context: BehaviorContext): NodeStatus {
        val threatTable = context.get<ThreatTable>("threatTable")
        return if (threatTable != null && threatTable.hasThreat()) {
            NodeStatus.SUCCESS
        } else {
            NodeStatus.FAILURE
        }
    }
}
