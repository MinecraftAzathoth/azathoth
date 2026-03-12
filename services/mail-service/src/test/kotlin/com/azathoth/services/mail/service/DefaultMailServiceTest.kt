package com.azathoth.services.mail.service

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.Result
import com.azathoth.services.mail.model.Mail
import com.azathoth.services.mail.model.MailAttachment
import com.azathoth.services.mail.model.MailType
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DefaultMailServiceTest {

    private lateinit var mailService: DefaultMailService

    private val player1 = PlayerId("player-1")
    private val player2 = PlayerId("player-2")

    @BeforeEach
    fun setup() {
        mailService = DefaultMailService()
    }

    // ─── 发送邮件 ────────────────────────────────────────

    @Nested
    inner class SendMailTests {

        @Test
        fun `发送普通邮件成功`() = runTest {
            val result = mailService.sendMail(
                senderId = player1,
                senderName = "玩家1",
                recipientId = player2,
                subject = "你好",
                content = "这是一封测试邮件"
            )

            assertTrue(result.isSuccess)
            val mail = (result as Result.Success).value
            assertEquals("你好", mail.subject)
            assertEquals(player2, mail.recipientId)
            assertFalse(mail.read)
            assertFalse(mail.claimed)
        }

        @Test
        fun `发送带附件的邮件`() = runTest {
            val attachments = listOf(
                MailAttachment("diamond_sword", 1),
                MailAttachment("gold_ingot", 64)
            )

            val result = mailService.sendMail(
                senderId = player1,
                senderName = "玩家1",
                recipientId = player2,
                subject = "礼物",
                content = "送你一把钻石剑",
                attachments = attachments
            )

            assertTrue(result.isSuccess)
            val mail = (result as Result.Success).value
            assertEquals(2, mail.attachments.size)
            assertTrue(mail.hasAttachments)
            assertTrue(mail.canClaim)
        }

        @Test
        fun `空标题发送失败`() = runTest {
            val result = mailService.sendMail(
                senderId = player1,
                senderName = "玩家1",
                recipientId = player2,
                subject = "",
                content = "内容"
            )

            assertTrue(result.isFailure)
        }

        @Test
        fun `空内容发送失败`() = runTest {
            val result = mailService.sendMail(
                senderId = player1,
                senderName = "玩家1",
                recipientId = player2,
                subject = "标题",
                content = ""
            )

            assertTrue(result.isFailure)
        }
    }

    // ─── 邮箱查询 ────────────────────────────────────────

    @Nested
    inner class MailboxTests {

        @Test
        fun `获取空邮箱`() = runTest {
            val result = mailService.getMailbox(player1)

            assertEquals(0, result.totalCount)
            assertEquals(0, result.unreadCount)
            assertTrue(result.mails.isEmpty())
        }

        @Test
        fun `获取邮箱列表`() = runTest {
            repeat(3) { i ->
                mailService.sendMail(player1, "系统", player2, "邮件$i", "内容$i")
            }

            val result = mailService.getMailbox(player2)

            assertEquals(3, result.totalCount)
            assertEquals(3, result.unreadCount)
        }

        @Test
        fun `只获取未读邮件`() = runTest {
            mailService.sendMail(player1, "系统", player2, "邮件1", "内容1")
            val mail2 = mailService.sendMail(player1, "系统", player2, "邮件2", "内容2")
            mailService.readMail(player2, (mail2 as Result.Success).value.mailId)

            val result = mailService.getMailbox(player2, unreadOnly = true)

            assertEquals(1, result.totalCount)
            assertEquals(1, result.unreadCount)
        }

        @Test
        fun `邮箱按时间倒序排列`() = runTest {
            mailService.sendMail(
                senderId = player1, senderName = "系统", recipientId = player2,
                subject = "第一封", content = "内容",
                expiresAt = System.currentTimeMillis() + Mail.DEFAULT_EXPIRE_MS
            )
            // 确保时间戳不同
            Thread.sleep(10)
            mailService.sendMail(
                senderId = player1, senderName = "系统", recipientId = player2,
                subject = "第二封", content = "内容",
                expiresAt = System.currentTimeMillis() + Mail.DEFAULT_EXPIRE_MS
            )

            val result = mailService.getMailbox(player2)

            assertEquals("第二封", result.mails[0].subject)
            assertEquals("第一封", result.mails[1].subject)
        }

        @Test
        fun `分页查询`() = runTest {
            repeat(5) { i ->
                mailService.sendMail(player1, "系统", player2, "邮件$i", "内容$i")
            }

            val page1 = mailService.getMailbox(player2, page = 1, pageSize = 2)
            val page2 = mailService.getMailbox(player2, page = 2, pageSize = 2)

            assertEquals(2, page1.mails.size)
            assertEquals(2, page2.mails.size)
            assertEquals(5, page1.totalCount)
        }
    }

    // ─── 读取邮件 ────────────────────────────────────────

    @Nested
    inner class ReadMailTests {

        @Test
        fun `读取邮件标记为已读`() = runTest {
            val sent = mailService.sendMail(player1, "系统", player2, "标题", "内容")
            val mailId = (sent as Result.Success).value.mailId

            val result = mailService.readMail(player2, mailId)

            assertTrue(result.isSuccess)
            assertTrue((result as Result.Success).value.read)
        }

        @Test
        fun `读取不存在的邮件失败`() = runTest {
            val result = mailService.readMail(player2, "nonexistent")
            assertTrue(result.isFailure)
        }

        @Test
        fun `未读邮件计数正确`() = runTest {
            mailService.sendMail(player1, "系统", player2, "邮件1", "内容")
            val sent2 = mailService.sendMail(player1, "系统", player2, "邮件2", "内容")
            mailService.readMail(player2, (sent2 as Result.Success).value.mailId)

            assertEquals(1, mailService.getUnreadCount(player2))
        }
    }

    // ─── 领取附件 ────────────────────────────────────────

    @Nested
    inner class ClaimTests {

        @Test
        fun `领取附件成功`() = runTest {
            val attachments = listOf(MailAttachment("diamond", 10))
            val sent = mailService.sendMail(
                player1, "系统", player2, "奖励", "恭喜",
                attachments = attachments
            )
            val mailId = (sent as Result.Success).value.mailId

            val result = mailService.claimAttachments(player2, mailId)

            assertTrue(result.isSuccess)
            val claimed = (result as Result.Success).value
            assertEquals(1, claimed.size)
            assertEquals("diamond", claimed[0].itemId)
            assertEquals(10, claimed[0].amount)
        }

        @Test
        fun `重复领取失败`() = runTest {
            val attachments = listOf(MailAttachment("diamond", 10))
            val sent = mailService.sendMail(
                player1, "系统", player2, "奖励", "恭喜",
                attachments = attachments
            )
            val mailId = (sent as Result.Success).value.mailId

            mailService.claimAttachments(player2, mailId)
            val result = mailService.claimAttachments(player2, mailId)

            assertTrue(result.isFailure)
        }

        @Test
        fun `无附件邮件领取失败`() = runTest {
            val sent = mailService.sendMail(player1, "系统", player2, "普通邮件", "内容")
            val mailId = (sent as Result.Success).value.mailId

            val result = mailService.claimAttachments(player2, mailId)

            assertTrue(result.isFailure)
        }
    }

    // ─── 删除邮件 ────────────────────────────────────────

    @Nested
    inner class DeleteTests {

        @Test
        fun `删除邮件成功`() = runTest {
            val sent = mailService.sendMail(player1, "系统", player2, "标题", "内容")
            val mailId = (sent as Result.Success).value.mailId

            val result = mailService.deleteMail(player2, mailId)
            assertTrue(result.isSuccess)

            val mailbox = mailService.getMailbox(player2)
            assertEquals(0, mailbox.totalCount)
        }

        @Test
        fun `删除不存在的邮件失败`() = runTest {
            val result = mailService.deleteMail(player2, "nonexistent")
            assertTrue(result.isFailure)
        }
    }

    // ─── 系统邮件 ────────────────────────────────────────

    @Nested
    inner class SystemMailTests {

        @Test
        fun `批量发送系统邮件`() = runTest {
            val recipients = listOf(player1, player2, PlayerId("player-3"))

            val count = mailService.sendSystemMail(
                subject = "维护公告",
                content = "服务器将于今晚维护",
                recipientIds = recipients,
                attachments = listOf(MailAttachment("compensation_box", 1))
            )

            assertEquals(3, count)
            assertEquals(1, mailService.getUnreadCount(player1))
            assertEquals(1, mailService.getUnreadCount(player2))

            val mailbox = mailService.getMailbox(player1)
            assertEquals(MailType.SYSTEM, mailbox.mails[0].type)
            assertEquals("系统", mailbox.mails[0].senderName)
        }
    }

    // ─── 过期清理 ────────────────────────────────────────

    @Nested
    inner class ExpirationTests {

        @Test
        fun `过期邮件不出现在邮箱中`() = runTest {
            val result = mailService.sendMail(
                senderId = player1,
                senderName = "系统",
                recipientId = player2,
                subject = "过期邮件",
                content = "内容",
                expiresAt = System.currentTimeMillis() - 1000 // 已过期
            )
            assertTrue(result.isSuccess)

            val mailbox = mailService.getMailbox(player2)
            assertEquals(0, mailbox.totalCount)
        }

        @Test
        fun `清理过期邮件`() = runTest {
            mailService.sendMail(
                player1, "系统", player2, "过期", "内容",
                expiresAt = System.currentTimeMillis() - 1000
            )
            mailService.sendMail(player1, "系统", player2, "正常", "内容")

            val cleaned = mailService.cleanExpiredMails()
            assertEquals(1, cleaned)
        }

        @Test
        fun `过期邮件不能领取附件`() = runTest {
            val sent = mailService.sendMail(
                player1, "系统", player2, "过期奖励", "内容",
                attachments = listOf(MailAttachment("diamond", 1)),
                expiresAt = System.currentTimeMillis() - 1000
            )
            val mailId = (sent as Result.Success).value.mailId

            val result = mailService.claimAttachments(player2, mailId)
            assertTrue(result.isFailure)
        }
    }
}
