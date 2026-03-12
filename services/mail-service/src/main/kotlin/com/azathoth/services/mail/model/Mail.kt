package com.azathoth.services.mail.model

import com.azathoth.core.common.identity.PlayerId

/**
 * 邮件类型
 */
enum class MailType {
    PLAYER,
    SYSTEM,
    REWARD,
    GUILD,
    TRADE
}

/**
 * 邮件附件
 */
data class MailAttachment(
    val itemId: String,
    val amount: Int,
    val metadata: Map<String, String> = emptyMap()
)

/**
 * 邮件实体
 */
data class Mail(
    val mailId: String,
    val senderId: PlayerId?,
    val senderName: String,
    val recipientId: PlayerId,
    val subject: String,
    val content: String,
    val type: MailType = MailType.PLAYER,
    val attachments: List<MailAttachment> = emptyList(),
    var read: Boolean = false,
    var claimed: Boolean = false,
    val sentAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = sentAt + DEFAULT_EXPIRE_MS
) {
    /** 是否已过期 */
    val isExpired: Boolean get() = System.currentTimeMillis() > expiresAt

    /** 是否有附件 */
    val hasAttachments: Boolean get() = attachments.isNotEmpty()

    /** 是否可以领取附件 */
    val canClaim: Boolean get() = hasAttachments && !claimed && !isExpired

    companion object {
        /** 默认过期时间：30 天 */
        const val DEFAULT_EXPIRE_MS = 30L * 24 * 60 * 60 * 1000
    }
}
