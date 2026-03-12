package com.azathoth.services.mail.service

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.Result
import com.azathoth.services.mail.model.Mail
import com.azathoth.services.mail.model.MailAttachment
import com.azathoth.services.mail.model.MailType

/**
 * 邮件服务接口
 */
interface MailService {

    /**
     * 发送邮件
     */
    suspend fun sendMail(
        senderId: PlayerId?,
        senderName: String,
        recipientId: PlayerId,
        subject: String,
        content: String,
        type: MailType = MailType.PLAYER,
        attachments: List<MailAttachment> = emptyList(),
        expiresAt: Long? = null
    ): Result<Mail>

    /**
     * 获取玩家邮箱
     */
    suspend fun getMailbox(
        playerId: PlayerId,
        unreadOnly: Boolean = false,
        page: Int = 1,
        pageSize: Int = 20
    ): MailboxResult

    /**
     * 读取邮件
     */
    suspend fun readMail(playerId: PlayerId, mailId: String): Result<Mail>

    /**
     * 领取附件
     */
    suspend fun claimAttachments(playerId: PlayerId, mailId: String): Result<List<MailAttachment>>

    /**
     * 删除邮件
     */
    suspend fun deleteMail(playerId: PlayerId, mailId: String): Result<Unit>

    /**
     * 批量发送系统邮件
     */
    suspend fun sendSystemMail(
        subject: String,
        content: String,
        recipientIds: List<PlayerId>,
        attachments: List<MailAttachment> = emptyList(),
        expiresAt: Long? = null
    ): Int

    /**
     * 获取未读邮件数
     */
    suspend fun getUnreadCount(playerId: PlayerId): Int

    /**
     * 清理过期邮件
     */
    suspend fun cleanExpiredMails(): Int
}

/**
 * 邮箱查询结果
 */
data class MailboxResult(
    val mails: List<Mail>,
    val totalCount: Int,
    val unreadCount: Int
)
