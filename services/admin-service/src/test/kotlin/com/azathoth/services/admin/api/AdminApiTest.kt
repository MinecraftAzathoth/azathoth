package com.azathoth.services.admin.api

import com.azathoth.services.admin.api.model.*
import com.azathoth.services.admin.api.routes.adminRoutes
import com.azathoth.services.admin.api.routes.authRoutes
import com.azathoth.services.admin.auth.AuthService
import com.auth0.jwt.JWT
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AdminApiTest {

    private val authService = AuthService(
        jwtSecret = "test-secret-key-for-testing",
        accessTokenExpireMinutes = 30,
        refreshTokenExpireDays = 7
    )

    private fun ApplicationTestBuilder.configureTestApp() {
        install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
        }
        install(Authentication) {
            jwt("auth-jwt") {
                verifier(
                    JWT.require(authService.algorithm)
                        .withIssuer(authService.issuer)
                        .withAudience(authService.audience)
                        .build()
                )
                validate { credential ->
                    if (credential.payload.subject != null) JWTPrincipal(credential.payload) else null
                }
            }
        }
        routing {
            route("/api") {
                authRoutes(authService)
                authenticate("auth-jwt") {
                    adminRoutes()
                }
            }
        }
    }

    private fun ApplicationTestBuilder.jsonClient() = createClient {
        install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
        }
    }

    private fun loginAndGetToken(): String {
        val result = authService.login("admin", "azathoth_dev")
        return result.token!!.accessToken
    }

    // ─── Auth ────────────────────────────────────────────

    @Test
    fun `POST login 成功返回 token`() = testApplication {
        configureTestApp()
        val client = jsonClient()

        val response = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("admin", "azathoth_dev"))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val result = response.body<AuthResult>()
        assertTrue(result.success)
        assertNotNull(result.token)
        assertEquals("admin", result.user?.username)
    }

    @Test
    fun `POST login 错误密码返回 401`() = testApplication {
        configureTestApp()
        val client = jsonClient()

        val response = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("admin", "wrong"))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET me 需要认证`() = testApplication {
        configureTestApp()
        val client = jsonClient()

        val response = client.get("/api/auth/me")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET me 带 token 返回用户信息`() = testApplication {
        configureTestApp()
        val client = jsonClient()
        val token = loginAndGetToken()

        val response = client.get("/api/auth/me") {
            bearerAuth(token)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val user = response.body<UserInfo>()
        assertEquals("admin", user.username)
        assertEquals("SUPER_ADMIN", user.role)
    }

    // ─── Players ─────────────────────────────────────────

    @Test
    fun `GET players stats 返回统计数据`() = testApplication {
        configureTestApp()
        val client = jsonClient()
        val token = loginAndGetToken()

        val response = client.get("/api/players/stats") {
            bearerAuth(token)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val stats = response.body<PlayerStatsDto>()
        assertTrue(stats.totalPlayers > 0)
        assertTrue(stats.onlinePlayers > 0)
    }

    @Test
    fun `GET players 返回分页列表`() = testApplication {
        configureTestApp()
        val client = jsonClient()
        val token = loginAndGetToken()

        val response = client.get("/api/players?page=1&pageSize=10") {
            bearerAuth(token)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val paged = response.body<PagedResponse<PlayerInfoDto>>()
        assertTrue(paged.items.isNotEmpty())
        assertEquals(1, paged.page)
    }

    @Test
    fun `GET players 支持搜索`() = testApplication {
        configureTestApp()
        val client = jsonClient()
        val token = loginAndGetToken()

        val response = client.get("/api/players?search=Dragon") {
            bearerAuth(token)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val paged = response.body<PagedResponse<PlayerInfoDto>>()
        assertTrue(paged.items.all { it.username.contains("Dragon", ignoreCase = true) })
    }

    @Test
    fun `GET player by id 返回玩家详情`() = testApplication {
        configureTestApp()
        val client = jsonClient()
        val token = loginAndGetToken()

        val response = client.get("/api/players/p-001") {
            bearerAuth(token)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val player = response.body<PlayerInfoDto>()
        assertEquals("DragonSlayer", player.username)
    }

    @Test
    fun `GET player by id 不存在返回 404`() = testApplication {
        configureTestApp()
        val client = jsonClient()
        val token = loginAndGetToken()

        val response = client.get("/api/players/nonexistent") {
            bearerAuth(token)
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    // ─── Instances ───────────────────────────────────────

    @Test
    fun `GET instances stats 返回统计`() = testApplication {
        configureTestApp()
        val client = jsonClient()
        val token = loginAndGetToken()

        val response = client.get("/api/instances/stats") {
            bearerAuth(token)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val stats = response.body<InstanceStatsDto>()
        assertTrue(stats.totalInstances > 0)
    }

    @Test
    fun `GET instances 返回实例列表`() = testApplication {
        configureTestApp()
        val client = jsonClient()
        val token = loginAndGetToken()

        val response = client.get("/api/instances") {
            bearerAuth(token)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val instances = response.body<List<InstanceInfoDto>>()
        assertTrue(instances.isNotEmpty())
    }

    // ─── Activities ──────────────────────────────────────

    @Test
    fun `GET activities 返回活动列表`() = testApplication {
        configureTestApp()
        val client = jsonClient()
        val token = loginAndGetToken()

        val response = client.get("/api/activities") {
            bearerAuth(token)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val activities = response.body<List<ActivityInfoDto>>()
        assertTrue(activities.isNotEmpty())
    }

    // ─── Announcements ───────────────────────────────────

    @Test
    fun `GET announcements 返回公告列表`() = testApplication {
        configureTestApp()
        val client = jsonClient()
        val token = loginAndGetToken()

        val response = client.get("/api/announcements") {
            bearerAuth(token)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val announcements = response.body<List<AnnouncementInfoDto>>()
        assertTrue(announcements.isNotEmpty())
    }

    @Test
    fun `POST announcements 创建公告`() = testApplication {
        configureTestApp()
        val client = jsonClient()
        val token = loginAndGetToken()

        val response = client.post("/api/announcements") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(CreateAnnouncementRequest("测试公告", "这是测试内容", "NORMAL"))
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val announcement = response.body<AnnouncementInfoDto>()
        assertEquals("测试公告", announcement.title)
        assertTrue(announcement.published)
    }

    // ─── Logs ────────────────────────────────────────────

    @Test
    fun `GET logs 返回日志列表`() = testApplication {
        configureTestApp()
        val client = jsonClient()
        val token = loginAndGetToken()

        val response = client.get("/api/logs") {
            bearerAuth(token)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val logs = response.body<List<LogEntryDto>>()
        assertTrue(logs.isNotEmpty())
    }

    @Test
    fun `GET logs 支持按级别过滤`() = testApplication {
        configureTestApp()
        val client = jsonClient()
        val token = loginAndGetToken()

        val response = client.get("/api/logs?level=ERROR") {
            bearerAuth(token)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val logs = response.body<List<LogEntryDto>>()
        assertTrue(logs.all { it.level == "ERROR" })
    }

    // ─── Analytics ───────────────────────────────────────

    @Test
    fun `GET analytics 返回分析数据`() = testApplication {
        configureTestApp()
        val client = jsonClient()
        val token = loginAndGetToken()

        val response = client.get("/api/analytics") {
            bearerAuth(token)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val data = response.body<List<AnalyticsDataDto>>()
        assertEquals(24, data.size)
    }

    // ─── 未认证访问 ──────────────────────────────────────

    @Test
    fun `未认证访问管理 API 返回 401`() = testApplication {
        configureTestApp()
        val client = jsonClient()

        val endpoints = listOf(
            "/api/players/stats",
            "/api/players",
            "/api/instances",
            "/api/activities",
            "/api/announcements",
            "/api/logs",
            "/api/analytics"
        )

        for (endpoint in endpoints) {
            val response = client.get(endpoint)
            assertEquals(HttpStatusCode.Unauthorized, response.status, "Endpoint $endpoint should require auth")
        }
    }
}
