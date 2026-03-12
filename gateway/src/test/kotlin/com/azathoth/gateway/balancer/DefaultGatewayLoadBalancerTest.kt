package com.azathoth.gateway.balancer

import com.azathoth.core.common.identity.InstanceId
import com.azathoth.gateway.routing.GameInstance
import com.azathoth.gateway.routing.InstanceType
import com.azathoth.gateway.routing.SimpleGameInstance
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DefaultGatewayLoadBalancerTest {

    private fun instances(count: Int): List<GameInstance> = (1..count).map {
        SimpleGameInstance(
            instanceId = InstanceId.of("inst-$it"),
            type = InstanceType.LOBBY,
            name = "Instance $it",
            host = "localhost",
            port = 25565 + it,
            currentPlayers = it * 10,
            maxPlayers = 100
        )
    }

    @Test
    fun `round robin cycles through instances`() {
        val lb = DefaultGatewayLoadBalancer(BalancingStrategy.ROUND_ROBIN)
        val insts = instances(3)

        val selected = (1..6).map { lb.select(insts) }
        // Should cycle: 0,1,2,0,1,2
        assertEquals("inst-1", selected[0]?.instanceId?.value)
        assertEquals("inst-2", selected[1]?.instanceId?.value)
        assertEquals("inst-3", selected[2]?.instanceId?.value)
        assertEquals("inst-1", selected[3]?.instanceId?.value)
    }

    @Test
    fun `least connections picks instance with fewest players`() {
        val lb = DefaultGatewayLoadBalancer(BalancingStrategy.LEAST_CONNECTIONS)
        val insts = listOf(
            SimpleGameInstance(InstanceId.of("a"), InstanceType.LOBBY, "A", "localhost", 1, currentPlayers = 50),
            SimpleGameInstance(InstanceId.of("b"), InstanceType.LOBBY, "B", "localhost", 2, currentPlayers = 10),
            SimpleGameInstance(InstanceId.of("c"), InstanceType.LOBBY, "C", "localhost", 3, currentPlayers = 30)
        )
        val selected = lb.select(insts)
        assertEquals("b", selected?.instanceId?.value)
    }

    @Test
    fun `random returns non-null for non-empty list`() {
        val lb = DefaultGatewayLoadBalancer(BalancingStrategy.RANDOM)
        val insts = instances(5)
        repeat(20) {
            assertNotNull(lb.select(insts))
        }
    }

    @Test
    fun `weighted round robin favors instances with more capacity`() {
        val lb = DefaultGatewayLoadBalancer(BalancingStrategy.WEIGHTED_ROUND_ROBIN)
        val insts = listOf(
            SimpleGameInstance(InstanceId.of("a"), InstanceType.LOBBY, "A", "localhost", 1, currentPlayers = 90, maxPlayers = 100),
            SimpleGameInstance(InstanceId.of("b"), InstanceType.LOBBY, "B", "localhost", 2, currentPlayers = 10, maxPlayers = 100)
        )
        // Instance B has 90 capacity vs A's 10, so B should be selected much more often
        val counts = mutableMapOf<String, Int>()
        repeat(100) {
            val id = lb.select(insts)?.instanceId?.value ?: return@repeat
            counts[id] = (counts[id] ?: 0) + 1
        }
        assertTrue((counts["b"] ?: 0) > (counts["a"] ?: 0))
    }

    @Test
    fun `select returns null for empty list`() {
        val lb = DefaultGatewayLoadBalancer(BalancingStrategy.ROUND_ROBIN)
        assertNull(lb.select(emptyList()))
    }

    @Test
    fun `select weighted with weights`() {
        val lb = DefaultGatewayLoadBalancer(BalancingStrategy.ROUND_ROBIN)
        val inst1 = SimpleGameInstance(InstanceId.of("a"), InstanceType.LOBBY, "A", "localhost", 1)
        val inst2 = SimpleGameInstance(InstanceId.of("b"), InstanceType.LOBBY, "B", "localhost", 2)

        data class SimpleWeight(
            override val instance: GameInstance,
            override val staticWeight: Int,
            override val dynamicWeight: Int
        ) : InstanceWeight

        val weights = listOf(
            SimpleWeight(inst1, staticWeight = 10, dynamicWeight = 100),
            SimpleWeight(inst2, staticWeight = 90, dynamicWeight = 100)
        )

        val counts = mutableMapOf<String, Int>()
        repeat(1000) {
            val id = lb.selectWeighted(weights)?.instanceId?.value ?: return@repeat
            counts[id] = (counts[id] ?: 0) + 1
        }
        // B should be selected ~9x more than A
        assertTrue((counts["b"] ?: 0) > (counts["a"] ?: 0) * 2)
    }

    @Test
    fun `reset stats clears state`() {
        val lb = DefaultGatewayLoadBalancer(BalancingStrategy.ROUND_ROBIN)
        val insts = instances(3)
        lb.select(insts) // advance counter
        lb.select(insts)
        lb.resetStats()
        // After reset, should start from beginning
        val selected = lb.select(insts)
        assertEquals("inst-1", selected?.instanceId?.value)
    }
}
