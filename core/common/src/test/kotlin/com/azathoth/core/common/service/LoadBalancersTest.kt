package com.azathoth.core.common.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LoadBalancersTest {

    private val instances = listOf(
        SimpleServiceInstance("a", "host-a", 8080, weight = 1),
        SimpleServiceInstance("b", "host-b", 8081, weight = 3),
        SimpleServiceInstance("c", "host-c", 8082, weight = 1)
    )

    @Test
    fun `test round robin cycles through instances`() {
        val lb = RoundRobinLoadBalancer()
        val selected = (1..6).map { lb.select(instances)!!.instanceId }
        assertEquals(listOf("a", "b", "c", "a", "b", "c"), selected)
    }

    @Test
    fun `test round robin returns null for empty list`() {
        val lb = RoundRobinLoadBalancer()
        assertNull(lb.select(emptyList()))
    }

    @Test
    fun `test weighted selects based on weight`() {
        val lb = WeightedLoadBalancer()
        val counts = mutableMapOf<String, Int>()
        repeat(1000) {
            val id = lb.select(instances)!!.instanceId
            counts[id] = (counts[id] ?: 0) + 1
        }
        // 权重为 3 的 b 应该被选中更多次
        assertTrue(counts["b"]!! > counts["a"]!!)
        assertTrue(counts["b"]!! > counts["c"]!!)
    }

    @Test
    fun `test random returns null for empty list`() {
        val lb = RandomLoadBalancer()
        assertNull(lb.select(emptyList()))
    }

    @Test
    fun `test random selects from list`() {
        val lb = RandomLoadBalancer()
        val selected = lb.select(instances)
        assertNotNull(selected)
        assertTrue(selected!!.instanceId in listOf("a", "b", "c"))
    }
}
