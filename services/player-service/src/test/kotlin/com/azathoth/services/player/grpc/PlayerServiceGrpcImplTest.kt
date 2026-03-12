package com.azathoth.services.player.grpc

import com.azathoth.core.grpc.player.getPlayerRequest
import com.azathoth.core.grpc.player.playerData
import com.azathoth.core.grpc.player.playerJoinRequest
import com.azathoth.core.grpc.player.playerLeaveRequest
import com.azathoth.core.grpc.player.updatePlayerRequest
import com.azathoth.services.player.repository.InMemoryPlayerRepository
import com.azathoth.services.player.service.DefaultPlayerService
import com.azathoth.core.common.identity.PlayerId
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PlayerServiceGrpcImplTest {

    private lateinit var repository: InMemoryPlayerRepository
    private lateinit var playerService: DefaultPlayerService
    private lateinit var grpcService: PlayerServiceGrpcImpl

    @BeforeEach
    fun setup() = runTest {
        repository = InMemoryPlayerRepository()
        playerService = DefaultPlayerService(repository)
        grpcService = PlayerServiceGrpcImpl(playerService)

        // 预创建一个测试玩家
        playerService.getOrCreate(PlayerId("test-player-1"), "TestPlayer")
    }

    @Test
    fun `getPlayer 返回已存在的玩家`() = runTest {
        val request = getPlayerRequest { playerId = "test-player-1" }
        val response = grpcService.getPlayer(request)

        assertTrue(response.success)
        assertEquals("test-player-1", response.player.id)
        assertEquals("TestPlayer", response.player.name)
        assertEquals(1, response.player.level)
    }

    @Test
    fun `getPlayer 不存在的玩家返回失败`() = runTest {
        val request = getPlayerRequest { playerId = "nonexistent" }
        val response = grpcService.getPlayer(request)

        assertFalse(response.success)
        assertTrue(response.errorMessage.isNotBlank())
    }

    @Test
    fun `updatePlayer 更新玩家数据`() = runTest {
        val request = updatePlayerRequest {
            playerId = "test-player-1"
            data = playerData {
                name = "新名字"
                level = 10
                experience = 5000
            }
        }
        val response = grpcService.updatePlayer(request)

        assertTrue(response.success)
        assertEquals("新名字", response.player.name)
        assertEquals(10, response.player.level)
        assertEquals(5000, response.player.experience)
    }

    @Test
    fun `updatePlayer 不存在的玩家返回失败`() = runTest {
        val request = updatePlayerRequest {
            playerId = "nonexistent"
            data = playerData { name = "test" }
        }
        val response = grpcService.updatePlayer(request)

        assertFalse(response.success)
    }

    @Test
    fun `playerJoin 成功`() = runTest {
        val request = playerJoinRequest {
            playerId = "test-player-1"
            gatewayId = "gw-1"
            instanceId = "inst-1"
        }
        val response = grpcService.playerJoin(request)

        assertTrue(response.success)
    }

    @Test
    fun `playerLeave 成功`() = runTest {
        // 先 join
        grpcService.playerJoin(playerJoinRequest {
            playerId = "test-player-1"
            gatewayId = "gw-1"
            instanceId = "inst-1"
        })

        val response = grpcService.playerLeave(playerLeaveRequest {
            playerId = "test-player-1"
            reason = "disconnect"
        })

        assertTrue(response.success)
    }

    @Test
    fun `getPlayer 返回的 metadata 包含金币和钻石`() = runTest {
        // 给玩家加点金币
        playerService.addGold(PlayerId("test-player-1"), 1000, "test")

        val request = getPlayerRequest { playerId = "test-player-1" }
        val response = grpcService.getPlayer(request)

        assertTrue(response.success)
        assertEquals("1000", response.player.metadataMap["gold"])
    }
}
