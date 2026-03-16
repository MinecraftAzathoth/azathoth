package com.azathoth.game.mechanics.ai.threat

import com.azathoth.game.engine.entity.LivingEntity
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ThreatTableTest {

    private lateinit var table: ThreatTable

    @BeforeEach
    fun setup() {
        table = ThreatTable()
    }

    @Test
    fun `addThreat 增加仇恨值`() {
        val target = createTarget(1)
        table.addThreat(target, 10.0)
        assertEquals(10.0, table.getThreat(target))
    }

    @Test
    fun `addThreat 累加仇恨值`() {
        val target = createTarget(1)
        table.addThreat(target, 10.0)
        table.addThreat(target, 5.0)
        assertEquals(15.0, table.getThreat(target))
    }

    @Test
    fun `removeThreat 移除目标`() {
        val target = createTarget(1)
        table.addThreat(target, 10.0)
        table.removeThreat(target)
        assertEquals(0.0, table.getThreat(target))
    }

    @Test
    fun `getTopThreat 返回最高仇恨目标`() {
        val t1 = createTarget(1)
        val t2 = createTarget(2)
        table.addThreat(t1, 5.0)
        table.addThreat(t2, 15.0)
        assertEquals(2, table.getTopThreat()?.entityId)
    }

    @Test
    fun `getTopThreat 忽略死亡目标`() {
        val alive = createTarget(1)
        val dead = createTarget(2, isDead = true)
        table.addThreat(alive, 5.0)
        table.addThreat(dead, 100.0)
        assertEquals(1, table.getTopThreat()?.entityId)
    }

    @Test
    fun `getThreatList 按仇恨值降序排列`() {
        val t1 = createTarget(1)
        val t2 = createTarget(2)
        val t3 = createTarget(3)
        table.addThreat(t1, 5.0)
        table.addThreat(t2, 15.0)
        table.addThreat(t3, 10.0)

        val list = table.getThreatList()
        assertEquals(3, list.size)
        assertEquals(2, list[0].first.entityId)
        assertEquals(3, list[1].first.entityId)
        assertEquals(1, list[2].first.entityId)
    }

    @Test
    fun `decay 衰减仇恨值`() {
        val target = createTarget(1)
        table.addThreat(target, 10.0)
        table.decay(0.5)
        assertEquals(5.0, table.getThreat(target))
    }

    @Test
    fun `decay 移除过低仇恨值`() {
        val target = createTarget(1)
        table.addThreat(target, 0.01)
        table.decay(0.5)
        assertFalse(table.hasThreat())
    }

    @Test
    fun `clear 清空仇恨表`() {
        table.addThreat(createTarget(1), 10.0)
        table.addThreat(createTarget(2), 20.0)
        table.clear()
        assertFalse(table.hasThreat())
    }

    @Test
    fun `hasThreat 空表返回 false`() {
        assertFalse(table.hasThreat())
    }

    @Test
    fun `hasThreat 有存活目标返回 true`() {
        table.addThreat(createTarget(1), 10.0)
        assertTrue(table.hasThreat())
    }

    private fun createTarget(id: Int, isDead: Boolean = false): LivingEntity = mockk(relaxed = true) {
        every { entityId } returns id
        every { this@mockk.isDead } returns isDead
        every { isRemoved } returns false
    }
}
