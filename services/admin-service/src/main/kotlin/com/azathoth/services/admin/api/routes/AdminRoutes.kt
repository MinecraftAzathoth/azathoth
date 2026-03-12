package com.azathoth.services.admin.api.routes

import com.azathoth.services.admin.api.model.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.sin
import kotlin.random.Random

/**
 * 管理 API 路由
 *
 * 提供仪表盘所需的玩家、实例、活动、公告、日志、分析数据接口。
 * 当前返回模拟数据，后续对接 gRPC 微服务获取真实数据。
 */
fun Route.adminRoutes() {

    // ─── 玩家 ────────────────────────────────────────────

    route("/players") {
        get("/stats") {
            call.respond(
                PlayerStatsDto(
                    totalPlayers = 52340,
                    onlinePlayers = 1247,
                    newPlayersToday = 186,
                    newPlayersThisWeek = 1023,
                    activePlayersToday = 4520
                )
            )
        }

        get {
            val page = call.queryParameters["page"]?.toIntOrNull() ?: 1
            val pageSize = call.queryParameters["pageSize"]?.toIntOrNull() ?: 10
            val search = call.queryParameters["search"] ?: ""

            var filtered = mockPlayers
            if (search.isNotBlank()) {
                val q = search.lowercase()
                filtered = filtered.filter {
                    it.username.lowercase().contains(q) || it.displayName.contains(q)
                }
            }

            val totalCount = filtered.size
            val totalPages = if (totalCount == 0) 1 else ceil(totalCount.toDouble() / pageSize).toInt()
            val items = filtered.drop((page - 1) * pageSize).take(pageSize)

            call.respond(
                PagedResponse(
                    items = items,
                    totalCount = totalCount,
                    page = page,
                    pageSize = pageSize,
                    totalPages = totalPages
                )
            )
        }

        get("/{id}") {
            val id = call.pathParameters["id"] ?: return@get call.respond(
                io.ktor.http.HttpStatusCode.BadRequest
            )
            val player = mockPlayers.find { it.playerId == id }
            if (player != null) {
                call.respond(player)
            } else {
                call.respond(io.ktor.http.HttpStatusCode.NotFound,
                    ApiResponse<Unit>(success = false, error = "玩家不存在"))
            }
        }
    }

    // ─── 实例 ────────────────────────────────────────────

    route("/instances") {
        get("/stats") {
            call.respond(
                InstanceStatsDto(
                    totalInstances = 48,
                    activeInstances = 32,
                    cpuUsage = 42,
                    memoryUsage = 58,
                    byType = mapOf("MAIN_CITY" to 8, "DUNGEON" to 18, "ARENA" to 4, "EVENT" to 2)
                )
            )
        }

        get {
            call.respond(mockInstances)
        }
    }

    // ─── 活动 ────────────────────────────────────────────

    get("/activities") {
        call.respond(mockActivities)
    }

    // ─── 公告 ────────────────────────────────────────────

    route("/announcements") {
        get {
            call.respond(mockAnnouncements)
        }

        post {
            val request = call.receive<CreateAnnouncementRequest>()
            val announcement = AnnouncementInfoDto(
                announcementId = "ann-${System.currentTimeMillis()}",
                title = request.title,
                content = request.content,
                type = request.type,
                publishedAt = java.time.Instant.now().toString(),
                published = true,
                authorId = "admin-001",
                authorName = "系统管理员"
            )
            call.respond(io.ktor.http.HttpStatusCode.Created, announcement)
        }
    }

    // ─── 日志 ────────────────────────────────────────────

    get("/logs") {
        val level = call.queryParameters["level"]
        val logs = if (level != null && level != "ALL") {
            mockLogs.filter { it.level == level }
        } else {
            mockLogs
        }
        call.respond(logs)
    }

    // ─── 分析 ────────────────────────────────────────────

    get("/analytics") {
        call.respond(generateAnalytics())
    }
}

// ─── 模拟数据（后续替换为 gRPC 调用） ──────────────────────

private val mockPlayers = listOf(
    PlayerInfoDto("p-001", "DragonSlayer", "屠龙勇士", 85, 234500, 128000, 560, 5, "g-001", "黎明之光", true, "main-city-01", "2025-01-15T10:30:00Z", "2024-06-01T08:00:00Z"),
    PlayerInfoDto("p-002", "ShadowMage", "暗影法师", 72, 185200, 95000, 320, 3, online = true, currentInstance = "dungeon-fire-03", lastLoginAt = "2025-01-15T09:15:00Z", createdAt = "2024-07-15T12:00:00Z"),
    PlayerInfoDto("p-003", "IronGuard", "铁壁守卫", 60, 120000, 45000, 150, 1, online = false, lastLoginAt = "2025-01-14T22:00:00Z", createdAt = "2024-09-20T16:00:00Z", banStatus = BanStatusDto(banned = false)),
    PlayerInfoDto("p-004", "WindRanger", "风行者", 91, 310000, 200000, 880, 7, "g-002", "暗夜精灵", true, "arena-01", "2025-01-15T11:00:00Z", "2024-03-10T10:00:00Z"),
    PlayerInfoDto("p-005", "FrostQueen", "冰霜女王", 45, 78000, 32000, 90, 0, online = false, lastLoginAt = "2025-01-10T18:30:00Z", createdAt = "2024-11-05T14:00:00Z", banStatus = BanStatusDto(banned = true, reason = "使用外挂", bannedAt = "2025-01-12T00:00:00Z", bannedBy = "admin"))
)

private val mockInstances = listOf(
    InstanceInfoDto("main-city-01", "MAIN_CITY", templateName = "主城-东方", state = "IN_PROGRESS", playerCount = 342, maxPlayers = 500, cpu = 45, memory = 62, createdAt = "2025-01-15T00:00:00Z", region = "cn-east"),
    InstanceInfoDto("main-city-02", "MAIN_CITY", templateName = "主城-西方", state = "IN_PROGRESS", playerCount = 289, maxPlayers = 500, cpu = 38, memory = 55, createdAt = "2025-01-15T00:00:00Z", region = "cn-west"),
    InstanceInfoDto("dungeon-fire-03", "DUNGEON", templateName = "烈焰深渊", state = "IN_PROGRESS", playerCount = 5, maxPlayers = 10, cpu = 72, memory = 40, createdAt = "2025-01-15T09:00:00Z", region = "cn-east"),
    InstanceInfoDto("arena-01", "ARENA", templateName = "竞技场-S1", state = "WAITING", playerCount = 18, maxPlayers = 50, cpu = 15, memory = 20, createdAt = "2025-01-15T08:00:00Z", region = "cn-east"),
    InstanceInfoDto("event-lunar-01", "EVENT", templateName = "春节活动副本", state = "CREATING", playerCount = 0, maxPlayers = 100, cpu = 5, memory = 10, createdAt = "2025-01-15T11:00:00Z", region = "cn-south"),
    InstanceInfoDto("dungeon-ice-02", "DUNGEON", templateName = "冰霜王座", state = "COMPLETED", playerCount = 0, maxPlayers = 10, cpu = 0, memory = 5, createdAt = "2025-01-15T07:00:00Z", region = "cn-north")
)

private val mockActivities = listOf(
    ActivityInfoDto("a-001", "春节庆典", "春节限时活动，丰厚奖励等你来拿", "SEASONAL", "ACTIVE", "2025-01-20T00:00:00Z", "2025-02-10T23:59:59Z", 12500),
    ActivityInfoDto("a-002", "每日签到", "每日登录领取奖励", "PERMANENT", "ACTIVE", "2024-01-01T00:00:00Z", "2099-12-31T23:59:59Z", 45000),
    ActivityInfoDto("a-003", "公会战", "每周六公会对抗赛", "RECURRING", "SCHEDULED", "2025-01-18T20:00:00Z", "2025-01-18T22:00:00Z", 0),
    ActivityInfoDto("a-004", "限时Boss挑战", "击败世界Boss获取稀有装备", "LIMITED_TIME", "ENDING", "2025-01-14T10:00:00Z", "2025-01-15T10:00:00Z", 8200),
    ActivityInfoDto("a-005", "新手引导活动", "新玩家专属福利", "SPECIAL_EVENT", "ENDED", "2024-12-01T00:00:00Z", "2025-01-01T00:00:00Z", 3200)
)

private val mockAnnouncements = listOf(
    AnnouncementInfoDto("ann-001", "服务器维护公告", "亲爱的玩家，服务器将于1月16日凌晨2:00-6:00进行例行维护。", "MAINTENANCE", "2025-01-15T10:00:00Z", published = true, authorId = "admin-001", authorName = "系统管理员"),
    AnnouncementInfoDto("ann-002", "春节活动开启", "春节庆典活动将于1月20日正式开启！", "EVENT", "2025-01-14T12:00:00Z", published = true, authorId = "admin-002", authorName = "运营团队"),
    AnnouncementInfoDto("ann-003", "版本更新说明 v2.5.0", "新增冰霜王座副本、竞技场赛季S2、公会系统优化等内容。", "NORMAL", "2025-01-13T08:00:00Z", published = true, authorId = "admin-001", authorName = "系统管理员"),
    AnnouncementInfoDto("ann-004", "严厉打击外挂行为", "近期发现部分玩家使用第三方外挂程序，一经发现永久封禁。", "IMPORTANT", "2025-01-12T15:00:00Z", published = true, authorId = "admin-003", authorName = "安全团队")
)

private val mockLogs = listOf(
    LogEntryDto("2025-01-15T11:30:45Z", "INFO", "Gateway", "玩家 DragonSlayer 登录成功", playerId = "p-001"),
    LogEntryDto("2025-01-15T11:30:40Z", "INFO", "GameInstance", "实例 main-city-01 玩家数: 342", instanceId = "main-city-01"),
    LogEntryDto("2025-01-15T11:30:35Z", "WARN", "GameInstance", "实例 dungeon-fire-03 CPU 使用率超过 70%", instanceId = "dungeon-fire-03"),
    LogEntryDto("2025-01-15T11:30:30Z", "ERROR", "TradeService", "交易处理失败: 库存不足", playerId = "p-002", stackTrace = "InsufficientInventoryException at TradeService.execute(TradeService.kt:142)"),
    LogEntryDto("2025-01-15T11:30:25Z", "INFO", "DungeonService", "副本 冰霜王座 已完成", instanceId = "dungeon-ice-02"),
    LogEntryDto("2025-01-15T11:30:20Z", "WARN", "Gateway", "连接超时: 客户端 192.168.1.xxx"),
    LogEntryDto("2025-01-15T11:30:15Z", "INFO", "ActivityService", "活动 限时Boss挑战 即将结束"),
    LogEntryDto("2025-01-15T11:30:10Z", "ERROR", "ChatService", "消息发送失败: Kafka 连接中断"),
    LogEntryDto("2025-01-15T11:30:05Z", "INFO", "PlayerService", "玩家 FrostQueen 已被封禁", playerId = "p-005"),
    LogEntryDto("2025-01-15T11:30:00Z", "DEBUG", "GameInstance", "实例 event-lunar-01 创建中", instanceId = "event-lunar-01")
)

private fun generateAnalytics(): List<AnalyticsDataDto> =
    (0 until 24).map { i ->
        AnalyticsDataDto(
            timestamp = "2025-01-15T${i.toString().padStart(2, '0')}:00:00Z",
            onlinePlayers = floor(800 + sin(i / 3.8) * 400 + Random.nextInt(100)).toInt(),
            activeInstances = floor(15 + sin(i / 3.8) * 8 + Random.nextInt(3)).toInt(),
            transactions = 200 + Random.nextInt(150),
            revenue = 5000 + Random.nextInt(3000)
        )
    }


