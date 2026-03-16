package com.azathoth.game.mechanics.ai.threat

import com.azathoth.game.engine.entity.LivingEntity
import java.util.concurrent.ConcurrentHashMap

/**
 * 仇恨/威胁表
 *
 * 每个 AI 实体持有一个仇恨表，记录对各目标的威胁值。
 * 线程安全实现。
 */
class ThreatTable {

    private val threats = ConcurrentHashMap<Int, ThreatEntry>()

    /**
     * 增加对目标的仇恨值
     */
    fun addThreat(target: LivingEntity, amount: Double) {
        threats.compute(target.entityId) { _, existing ->
            if (existing != null) {
                existing.copy(threat = existing.threat + amount)
            } else {
                ThreatEntry(target, amount)
            }
        }
    }

    /**
     * 移除目标
     */
    fun removeThreat(target: LivingEntity) {
        threats.remove(target.entityId)
    }

    /**
     * 获取最高仇恨目标
     */
    fun getTopThreat(): LivingEntity? {
        return threats.values
            .filter { !it.target.isDead && !it.target.isRemoved }
            .maxByOrNull { it.threat }
            ?.target
    }

    /**
     * 获取排序后的仇恨列表（降序）
     */
    fun getThreatList(): List<Pair<LivingEntity, Double>> {
        return threats.values
            .filter { !it.target.isDead && !it.target.isRemoved }
            .sortedByDescending { it.threat }
            .map { it.target to it.threat }
    }

    /**
     * 清空仇恨表
     */
    fun clear() {
        threats.clear()
    }

    /**
     * 衰减所有仇恨值
     *
     * @param factor 衰减因子（0~1），例如 0.9 表示保留 90%
     */
    fun decay(factor: Double) {
        threats.replaceAll { _, entry ->
            entry.copy(threat = entry.threat * factor)
        }
        // 移除仇恨值过低的条目
        threats.entries.removeIf { it.value.threat < 0.01 }
    }

    /**
     * 是否有仇恨目标
     */
    fun hasThreat(): Boolean {
        return threats.values.any { !it.target.isDead && !it.target.isRemoved }
    }

    /**
     * 获取对指定目标的仇恨值
     */
    fun getThreat(target: LivingEntity): Double {
        return threats[target.entityId]?.threat ?: 0.0
    }

    /**
     * 仇恨条目
     */
    private data class ThreatEntry(
        val target: LivingEntity,
        val threat: Double
    )
}
