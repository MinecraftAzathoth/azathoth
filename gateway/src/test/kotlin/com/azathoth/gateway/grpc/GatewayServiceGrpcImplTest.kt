package com.azathoth.gateway.grpc

import com.azathoth.core.common.identity.InstanceId
import com.azathoth.core.grpc.gateway.heartbeatRequest
import com.azathoth.core.grpc.gateway.registerInstanceRequest
import com.azathoth.core.grpc.gateway.transferPlayerRequest
import com.azathoth.core.grpc.gateway.unregisterInstanceRequest
import com.azathoth.gateway.routing.DefaultInstanceRegistry
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GatewayServiceGrpcImplTest {

    private lateinit var registry: DefaultInstanceRegistry
    private lateinit var grpcService: GatewayServiceGrpcImpl

    @BeforeEach
    fun setup() {
        registry = DefaultInstanceRegistry()
        grpcService = GatewayServiceGrpcImpl(registry)
    }

    @Test
    fun `registerInstance 注册新实例`() = runTest {
        val request = registerInstanceRequest {
            instanceId = "inst-1"
            instanceType = "WORLD"
            host = "localhost"
            port = 25565
            maxPlayers = 100
        }
        val response = grpcService.registerInstance(request)

        assertTrue(response.success)
        assertNotNull(registry.getInstance(InstanceId("inst-1")))
    }

    @Test
    fun `registerInstance 未知类型回退到 WORLD`() = runTest {
        val request = registerInstanceRequest {
            instanceId = "inst-2"
            instanceType = "UNKNOWN_TYPE"
            host = "localhost"
            port = 25566
            maxPlayers = 50
        }
        val response = grpcService.registerInstance(request)

        assertTrue(response.success)
        val instance = registry.getInstance(InstanceId("inst-2"))
        assertNotNull(instance)
    }

    @Test
    fun `unregisterInstance 注销实例`() = runTest {
        // 先注册
        grpcService.registerInstance(registerInstanceRequest {
            instanceId = "inst-1"
            instanceType = "LOBBY"
            host = "localhost"
            port = 25565
            maxPlayers = 100
        })

        val response = grpcService.unregisterInstance(unregisterInstanceRequest {
            instanceId = "inst-1"
        })

        assertTrue(response.success)
        assertNull(registry.getInstance(InstanceId("inst-1")))
    }

    @Test
    fun `heartbeat 更新实例玩家数`() = runTest {
        // 先注册
        grpcService.registerInstance(registerInstanceRequest {
            instanceId = "inst-1"
            instanceType = "WORLD"
            host = "localhost"
            port = 25565
            maxPlayers = 100
        })

        val response = grpcService.heartbeat(heartbeatRequest {
            instanceId = "inst-1"
            currentPlayers = 42
            cpuUsage = 35.5f
            memoryUsage = 60.0f
            timestamp = System.currentTimeMillis()
        })

        assertTrue(response.acknowledged)
        assertEquals(42, registry.getInstance(InstanceId("inst-1"))?.currentPlayers)
    }

    @Test
    fun `transferPlayer 目标实例不存在返回失败`() = runTest {
        val response = grpcService.transferPlayer(transferPlayerRequest {
            playerId = "player-1"
            fromInstance = "inst-1"
            toInstance = "nonexistent"
        })

        assertFalse(response.success)
        assertTrue(response.errorMessage.contains("不存在"))
    }

    @Test
    fun `transferPlayer 目标实例存在且未满返回成功`() = runTest {
        // 注册目标实例
        grpcService.registerInstance(registerInstanceRequest {
            instanceId = "inst-target"
            instanceType = "WORLD"
            host = "localhost"
            port = 25566
            maxPlayers = 100
        })

        val response = grpcService.transferPlayer(transferPlayerRequest {
            playerId = "player-1"
            fromInstance = "inst-source"
            toInstance = "inst-target"
        })

        assertTrue(response.success)
    }
}
