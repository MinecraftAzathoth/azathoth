package com.azathoth.services.player.repository

import com.azathoth.core.common.database.DatabaseConfig
import com.azathoth.core.common.database.DatabaseFactory
import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.Result
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * PostgresPlayerRepository 集成测试
 *
 * 使用 H2 内存数据库模拟 PostgreSQL 行为。
 */
class PostgresPlayerRepositoryTest {

    private lateinit var dbFactory: DatabaseFactory
    private lateinit var repository: PostgresPlayerRepository

    @BeforeEach
    fun setup() {
        dbFactory = DatabaseFactory.create(
            DatabaseConfig(
                jdbcUrl = "jdbc:h2:mem:test_${System.nanoTime()};DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
                driver = "org.h2.Driver",
                username = "sa",
                password = "",
                maxPoolSize = 2
            )
        )
        dbFactory.createTables(Players, PlayerStatsTable)
        repository = PostgresPlayerRepository(dbFactory)
    }

    @AfterEach
    fun teardown() {
        dbFactory.close()
    }

    // ─── CRUD ────────────────────────────────────────────

    @Nested
    inner class CrudTests {

        @Test
        fun `创建玩家成功`() = runTest {
            val result = repository.create(PlayerId("p-1"), "TestPlayer")

            assertTrue(result.isSuccess)
            val player = (result as Result.Success).value
            assertEquals("p-1", player.playerId.value)
            assertEquals("TestPlayer", player.username)
            assertEquals("TestPlayer", player.displayName)
            assertEquals(1, player.level)
            assertEquals(0L, player.experience)
        }

        @Test
        fun `重复创建玩家失败`() = runTest {
            repository.create(PlayerId("p-1"), "TestPlayer")
            val result = repository.create(PlayerId("p-1"), "TestPlayer2")

            assertTrue(result.isFailure)
        }

        @Test
        fun `重复用户名创建失败`() = runTest {
            repository.create(PlayerId("p-1"), "TestPlayer")
            val result = repository.create(PlayerId("p-2"), "TestPlayer")

            assertTrue(result.isFailure)
        }

        @Test
        fun `通过 ID 查找玩家`() = runTest {
            repository.create(PlayerId("p-1"), "TestPlayer")

            val player = repository.findById(PlayerId("p-1"))

            assertNotNull(player)
            assertEquals("TestPlayer", player?.username)
        }

        @Test
        fun `通过用户名查找玩家`() = runTest {
            repository.create(PlayerId("p-1"), "TestPlayer")

            val player = repository.findByUsername("TestPlayer")

            assertNotNull(player)
            assertEquals("p-1", player?.playerId?.value)
        }

        @Test
        fun `查找不存在的玩家返回 null`() = runTest {
            assertNull(repository.findById(PlayerId("nonexistent")))
            assertNull(repository.findByUsername("nonexistent"))
        }

        @Test
        fun `保存更新玩家数据`() = runTest {
            repository.create(PlayerId("p-1"), "TestPlayer")
            val player = repository.findById(PlayerId("p-1"))!!

            player.displayName = "新名字"
            player.level = 50
            player.experience = 125000
            player.gold = 99999
            player.diamond = 500

            val result = repository.save(player)
            assertTrue(result.isSuccess)

            val updated = repository.findById(PlayerId("p-1"))!!
            assertEquals("新名字", updated.displayName)
            assertEquals(50, updated.level)
            assertEquals(125000L, updated.experience)
            assertEquals(99999L, updated.gold)
            assertEquals(500L, updated.diamond)
        }

        @Test
        fun `删除玩家`() = runTest {
            repository.create(PlayerId("p-1"), "TestPlayer")

            val result = repository.delete(PlayerId("p-1"))
            assertTrue(result.isSuccess)

            assertNull(repository.findById(PlayerId("p-1")))
            assertNull(repository.getStats(PlayerId("p-1")))
        }

        @Test
        fun `删除不存在的玩家失败`() = runTest {
            val result = repository.delete(PlayerId("nonexistent"))
            assertTrue(result.isFailure)
        }

        @Test
        fun `exists 检查`() = runTest {
            repository.create(PlayerId("p-1"), "TestPlayer")

            assertTrue(repository.exists(PlayerId("p-1")))
            assertFalse(repository.exists(PlayerId("nonexistent")))
        }

        @Test
        fun `existsByUsername 检查`() = runTest {
            repository.create(PlayerId("p-1"), "TestPlayer")

            assertTrue(repository.existsByUsername("TestPlayer"))
            assertFalse(repository.existsByUsername("nonexistent"))
        }
    }

    // ─── 统计 ────────────────────────────────────────────

    @Nested
    inner class StatsTests {

        @Test
        fun `创建玩家时自动创建统计`() = runTest {
            repository.create(PlayerId("p-1"), "TestPlayer")

            val stats = repository.getStats(PlayerId("p-1"))
            assertNotNull(stats)
            assertEquals(0L, stats?.mobsKilled)
        }

        @Test
        fun `保存和读取统计`() = runTest {
            repository.create(PlayerId("p-1"), "TestPlayer")
            val stats = repository.getStats(PlayerId("p-1"))!!

            stats.mobsKilled = 100
            stats.playersKilled = 25
            stats.deaths = 10
            stats.dungeonsCompleted = 5

            val result = repository.saveStats(stats)
            assertTrue(result.isSuccess)

            val loaded = repository.getStats(PlayerId("p-1"))!!
            assertEquals(100L, loaded.mobsKilled)
            assertEquals(25L, loaded.playersKilled)
            assertEquals(10L, loaded.deaths)
            assertEquals(5L, loaded.dungeonsCompleted)
        }
    }

    // ─── 登录和在线时间 ──────────────────────────────────

    @Nested
    inner class LoginTests {

        @Test
        fun `更新最后登录时间`() = runTest {
            repository.create(PlayerId("p-1"), "TestPlayer")
            val before = repository.findById(PlayerId("p-1"))!!.lastLoginAt

            Thread.sleep(10)
            repository.updateLastLogin(PlayerId("p-1"))

            val after = repository.findById(PlayerId("p-1"))!!.lastLoginAt
            assertTrue(after > before)
        }

        @Test
        fun `累加在线时间`() = runTest {
            repository.create(PlayerId("p-1"), "TestPlayer")

            repository.addOnlineTime(PlayerId("p-1"), 3600)
            repository.addOnlineTime(PlayerId("p-1"), 1800)

            val player = repository.findById(PlayerId("p-1"))!!
            assertEquals(5400L, player.totalOnlineTime)
        }
    }

    // ─── 搜索和排行榜 ───────────────────────────────────

    @Nested
    inner class SearchTests {

        @Test
        fun `搜索玩家`() = runTest {
            repository.create(PlayerId("p-1"), "DragonSlayer")
            repository.create(PlayerId("p-2"), "ShadowMage")
            repository.create(PlayerId("p-3"), "DragonKnight")

            val results = repository.search("Dragon")
            assertEquals(2, results.size)
        }

        @Test
        fun `等级排行榜`() = runTest {
            createPlayerWithLevel("p-1", "Player1", 50)
            createPlayerWithLevel("p-2", "Player2", 80)
            createPlayerWithLevel("p-3", "Player3", 30)

            val leaderboard = repository.getLeaderboard(LeaderboardType.LEVEL, 10)
            assertEquals(3, leaderboard.size)
            assertEquals(80, leaderboard[0].level)
            assertEquals(50, leaderboard[1].level)
            assertEquals(30, leaderboard[2].level)
        }

        @Test
        fun `金币排行榜`() = runTest {
            createPlayerWithGold("p-1", "Player1", 1000)
            createPlayerWithGold("p-2", "Player2", 5000)
            createPlayerWithGold("p-3", "Player3", 3000)

            val leaderboard = repository.getLeaderboard(LeaderboardType.GOLD, 10)
            assertEquals(5000L, leaderboard[0].gold)
            assertEquals(3000L, leaderboard[1].gold)
        }

        private suspend fun createPlayerWithLevel(id: String, name: String, level: Int) {
            repository.create(PlayerId(id), name)
            val player = repository.findById(PlayerId(id))!!
            player.level = level
            repository.save(player)
        }

        private suspend fun createPlayerWithGold(id: String, name: String, gold: Long) {
            repository.create(PlayerId(id), name)
            val player = repository.findById(PlayerId(id))!!
            player.gold = gold
            repository.save(player)
        }
    }

    // ─── 封禁 ────────────────────────────────────────────

    @Nested
    inner class BanTests {

        @Test
        fun `封禁和解封玩家`() = runTest {
            repository.create(PlayerId("p-1"), "TestPlayer")
            val player = repository.findById(PlayerId("p-1"))!!

            player.isBanned = true
            player.banReason = "作弊"
            repository.save(player)

            val banned = repository.findById(PlayerId("p-1"))!!
            assertTrue(banned.isBanned)
            assertEquals("作弊", banned.banReason)

            banned.isBanned = false
            banned.banReason = null
            repository.save(banned)

            val unbanned = repository.findById(PlayerId("p-1"))!!
            assertFalse(unbanned.isBanned)
            assertNull(unbanned.banReason)
        }
    }
}
