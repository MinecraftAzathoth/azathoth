package com.azathoth.services.mail.service

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.ErrorCodes
import com.azathoth.core.common.result.Result
import com.azathoth.services.mail.model.Mail
import com.azathoth.services.mail.model.MailAttachment
import com.azathoth.services.mail.model.MailType
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

/**
 * 邮件服务内存实现
 *
 * 后续可替换为数据库持久化实现。
 */
class DefaultMailService : MailService {

    /** recipientId -> list of mails */
    private val mailboxes = ConcurrentHashMap<String, MutableList<Mail>>()

    override suspend fun sendMail(
        senderId: PlayerId?,
        senderName: String,
        recipientId: PlayerId,
        subject: String,
        content: String,
        type: MailType,
        attachments: List<MailAttachment>,
        expiresAt: Long?
    ): Result<Mail> {
        if (subject.isBlank()) {
            return Result.failure(ErrorCodes.INVALID_ARGUMENT, "邮件标题不能为空")
        }
        if (content.isBlank()) {
            return Result.failure(ErrorCodes.INVALID_ARGUMENT, "邮件内容不能为空")
        }

        val mail = Mail(
            mailId = UUID.randomUUID().toString(),
            senderId = senderId,
            senderName = senderName,
            recipientId = recipientId,
            subject = subject,
            content = content,
            type = type,
            attachments = attachments,
            expiresAt = expiresAt ?: (System.currentTimeMillis() + Mail.DEFAULT_EXPIRE_MS)
        )

        getOrCreateMailbox(recipientId).add(mail)
        logger.info { "邮件已发送: ${mail.mailId} -> ${recipientId.value} (${mail.type})" }
        return Result.success(mail)
    }

    override suspend fun getMailbox(
        playerId: PlayerId,
        unreadOnly: Boolean,
        page: Int,
        pageSize: Int
    ): MailboxResult {
        val allMails = getOrCreateMailbox(playerId)
            .filter { !it.isExpired }
            .sortedByDescending { it.sentAt }

        val filtered = if (unreadOnly) allMails.filter { !it.read } else allMails
        val unreadCount = allMails.count { !it.read }
        val paged = filtered.drop((page - 1) * pageSize).take(pageSize)

        return MailboxResult(
            mails = paged,
            totalCount = filtered.size,
            unreadCount = unreadCount
        )
    }

    override suspend fun readMail(playerId: PlayerId, mailId: String): Result<Mail> {
        val mail = findMail(playerId, mailId)
            ?: return Result.failure(ErrorCodes.NOT_FOUND, "邮件不存在")

        if (mail.isExpired) {
            return Result.failure(ErrorCodes.NOT_FOUND, "邮件已过期")
        }

        mail.read = true
        logger.debug { "邮件已读: $mailId (${playerId.value})" }
        return Result.success(mail)
    }

    override suspend fun claimAttachments(playerId: PlayerId, mailId: String): Result<List<MailAttachment>> {
        val mail = findMail(playerId, mailId)
            ?: return Result.failure(ErrorCodes.NOT_FOUND, "邮件不存在")

        if (mail.isExpired) {
            return Result.failure(ErrorCodes.NOT_FOUND, "邮件已过期")
        }
        if (!mail.hasAttachments) {
            return Result.failure(ErrorCodes.INVALID_ARGUMENT, "邮件没有附件")
        }
        if (mail.claimed) {
            return Result.failure(ErrorCodes.ALREADY_EXISTS, "附件已领取")
        }

        mail.claimed = true
        mail.read = true
        logger.info { "附件已领取: $mailId (${playerId.value}), ${mail.attachments.size} 个物品" }
        return Result.success(mail.attachments)
    }

    override suspend fun deleteMail(playerId: PlayerId, mailId: String): Result<Unit> {
        val mailbox = mailboxes[playerId.value] ?: return Result.failure(ErrorCodes.NOT_FOUND, "邮件不存在")
        val removed = mailbox.removeIf { it.mailId == mailId }
        if (!removed) {
            return Result.failure(ErrorCodes.NOT_FOUND, "邮件不存在")
        }
        logger.debug { "邮件已删除: $mailId (${playerId.value})" }
        return Result.success(Unit)
    }

    override suspend fun sendSystemMail(
        subject: String,
        content: String,
        recipientIds: List<PlayerId>,
        attachments: List<MailAttachment>,
        expiresAt: Long?
    ): Int {
        var count = 0
        for (recipientId in recipientIds) {
            val result = sendMail(
                senderId = null,
                senderName = "系统",
                recipientId = recipientId,
                subject = subject,
                content = content,
                type = MailType.SYSTEM,
                attachments = attachments,
                expiresAt = expiresAt
            )
            if (result.isSuccess) count++
        }
        logger.info { "系统邮件已发送: $subject -> $count/${recipientIds.size} 个玩家" }
        return count
    }

    override suspend fun getUnreadCount(playerId: PlayerId): Int =
        getOrCreateMailbox(playerId).count { !it.read && !it.isExpired }

    override suspend fun cleanExpiredMails(): Int {
        var cleaned = 0
        mailboxes.values.forEach { mails ->
            val before = mails.size
            mails.removeIf { it.isExpired }
            cleaned += before - mails.size
        }
        if (cleaned > 0) {
            logger.info { "清理过期邮件: $cleaned 封" }
        }
        return cleaned
    }

    private fun getOrCreateMailbox(playerId: PlayerId): MutableList<Mail> =
        mailboxes.getOrPut(playerId.value) { mutableListOf() }

    private fun findMail(playerId: PlayerId, mailId: String): Mail? =
        mailboxes[playerId.value]?.find { it.mailId == mailId }
}
