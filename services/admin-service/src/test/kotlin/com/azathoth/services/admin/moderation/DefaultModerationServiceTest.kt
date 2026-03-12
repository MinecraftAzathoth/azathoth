package com.azathoth.services.admin.moderation

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.Result
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class DefaultModerationServiceTest {

    private lateinit var service: DefaultModerationService

    private val admin = PlayerId("admin-1")
    private val player = PlayerId("player-1")
    private val player2 = PlayerId("player-2")

    @BeforeEach
    fun setup() {
        service = DefaultModerationService()
    }

    @Test
    fun `warn should create warning`() = runTest {
        val result = service.warn(player, "不当言论", admin)
        assertTrue(result.isSuccess)
        val punishment = (result as Result.Success).value
        assertEquals(PunishmentType.WARNING, punishment.type)
        assertEquals(player, punishment.playerId)
        assertEquals("不当言论", punishment.reason)
        assertEquals(admin, punishment.executorId)
    }

    @Test
    fun `mute should create timed mute`() = runTest {
        val result = service.mute(player, 30.minutes, "刷屏", admin)
        assertTrue(result.isSuccess)
        val punishment = (result as Result.Success).value
        assertEquals(PunishmentType.MUTE, punishment.type)
        assertNotNull(punishment.expiresAt)
        assertTrue(service.isMuted(player))
    }

    @Test
    fun `ban with duration should create temp ban`() = runTest {
        val result = service.ban(player, 24.hours, "作弊", admin)
        assertTrue(result.isSuccess)
        val punishment = (result as Result.Success).value
        assertEquals(PunishmentType.TEMP_BAN, punishment.type)
        assertTrue(service.isBanned(player))
    }

    @Test
    fun `ban without duration should create perm ban`() = runTest {
        val result = service.ban(player, null, "严重违规", admin)
        assertTrue(result.isSuccess)
        val punishment = (result as Result.Success).value
        assertEquals(PunishmentType.PERM_BAN, punishment.type)
        assertTrue(service.isBanned(player))
    }

    @Test
    fun `revoke should deactivate punishment`() = runTest {
        val banResult = service.ban(player, null, "误封", admin)
        val punishment = (banResult as Result.Success).value

        assertTrue(service.isBanned(player))

        val revokeResult = service.revoke(punishment.punishmentId, "误操作", admin)
        assertTrue(revokeResult.isSuccess)

        assertFalse(service.isBanned(player))
    }

    @Test
    fun `revoke already revoked should fail`() = runTest {
        val punishment = (service.ban(player, null, "test", admin) as Result.Success).value
        service.revoke(punishment.punishmentId, "reason", admin)

        val result = service.revoke(punishment.punishmentId, "again", admin)
        assertTrue(result.isFailure)
    }

    @Test
    fun `revoke non-existent should fail`() = runTest {
        val result = service.revoke("non-existent", "reason", admin)
        assertTrue(result.isFailure)
    }

    @Test
    fun `getPunishments should return player history`() = runTest {
        service.warn(player, "reason1", admin)
        service.mute(player, 30.minutes, "reason2", admin)
        service.warn(player2, "other", admin)

        val punishments = service.getPunishments(player)
        assertEquals(2, punishments.size)
        assertTrue(punishments.all { it.playerId == player })
    }

    @Test
    fun `kick should create kick record`() = runTest {
        val result = service.kick(player, "AFK", admin)
        assertTrue(result.isSuccess)
        assertEquals(PunishmentType.KICK, (result as Result.Success).value.type)
    }

    @Test
    fun `banIp should work`() = runTest {
        val result = service.banIp("192.168.1.1", null, "VPN滥用", admin)
        assertTrue(result.isSuccess)
        assertTrue(service.isIpBanned("192.168.1.1"))
        assertFalse(service.isIpBanned("192.168.1.2"))
    }

    @Test
    fun `banIp with blank ip should fail`() = runTest {
        val result = service.banIp("", null, "test", admin)
        assertTrue(result.isFailure)
    }

    @Test
    fun `submitReport should succeed`() = runTest {
        val result = service.submitReport(player, player2, ReportCategory.CHEATING, "使用飞行外挂")
        assertTrue(result.isSuccess)
        val report = (result as Result.Success).value
        assertEquals(player, report.reporterId)
        assertEquals(player2, report.targetId)
        assertEquals(ReportCategory.CHEATING, report.category)
        assertEquals(ReportStatus.PENDING, report.status)
    }

    @Test
    fun `submitReport self should fail`() = runTest {
        val result = service.submitReport(player, player, ReportCategory.OTHER, "test")
        assertTrue(result.isFailure)
    }

    @Test
    fun `submitReport with blank content should fail`() = runTest {
        val result = service.submitReport(player, player2, ReportCategory.OTHER, "")
        assertTrue(result.isFailure)
    }

    @Test
    fun `handleReport should update status`() = runTest {
        val report = (service.submitReport(player, player2, ReportCategory.CHEATING, "外挂") as Result.Success).value

        val result = service.handleReport(report.reportId, admin, ReportStatus.RESOLVED, "已封禁")
        assertTrue(result.isSuccess)

        val updated = service.getReport(report.reportId)!!
        assertEquals(ReportStatus.RESOLVED, updated.status)
        assertEquals(admin, updated.handlerId)
        assertEquals("已封禁", updated.resolution)
    }

    @Test
    fun `handleReport already resolved should fail`() = runTest {
        val report = (service.submitReport(player, player2, ReportCategory.CHEATING, "外挂") as Result.Success).value
        service.handleReport(report.reportId, admin, ReportStatus.RESOLVED, "已处理")

        val result = service.handleReport(report.reportId, admin, ReportStatus.REJECTED, "重新处理")
        assertTrue(result.isFailure)
    }

    @Test
    fun `getPendingReports should return only pending`() = runTest {
        service.submitReport(player, player2, ReportCategory.CHEATING, "report1")
        val report2 = (service.submitReport(player2, player, ReportCategory.HARASSMENT, "report2") as Result.Success).value
        service.handleReport(report2.reportId, admin, ReportStatus.RESOLVED, "done")

        val pending = service.getPendingReports()
        assertEquals(1, pending.size)
        assertEquals(ReportStatus.PENDING, pending[0].status)
    }
}
