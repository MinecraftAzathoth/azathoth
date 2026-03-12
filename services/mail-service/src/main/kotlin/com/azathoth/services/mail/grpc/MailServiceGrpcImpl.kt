package com.azathoth.services.mail.grpc

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.Result
import com.azathoth.core.grpc.mail.*
import com.azathoth.services.mail.model.MailAttachment
import com.azathoth.services.mail.model.MailType
import com.azathoth.services.mail.service.MailService
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * MailService gRPC 实现
 */
class MailServiceGrpcImpl(
    private val mailService: MailService
) : MailServiceGrpcKt.MailServiceCoroutineImplBase() {

    override suspend fun sendMail(request: SendMailRequest): SendMailResponse {
        logger.debug { "gRPC sendMail: ${request.senderId} -> ${request.recipientId}" }

        val result = mailService.sendMail(
            senderId = request.senderId.takeIf { it.isNotBlank() }?.let { PlayerId(it) },
            senderName = request.senderName,
            recipientId = PlayerId(request.recipientId),
            subject = request.subject,
            content = request.content,
            type = request.type.toModel(),
            attachments = request.attachmentsList.map { it.toModel() },
            expiresAt = request.expiresAt.takeIf { it > 0 }
        )

        return when (result) {
            is Result.Success -> sendMailResponse {
                success = true
                mailId = result.value.mailId
            }
            is Result.Failure -> sendMailResponse {
                success = false
                errorMessage = result.error.message
            }
        }
    }

    override suspend fun getMailbox(request: GetMailboxRequest): GetMailboxResponse {
        val result = mailService.getMailbox(
            playerId = PlayerId(request.playerId),
            unreadOnly = request.unreadOnly,
            page = request.page.takeIf { it > 0 } ?: 1,
            pageSize = request.pageSize.takeIf { it > 0 } ?: 20
        )

        return getMailboxResponse {
            mails.addAll(result.mails.map { it.toProto() })
            totalCount = result.totalCount
            unreadCount = result.unreadCount
        }
    }

    override suspend fun readMail(request: ReadMailRequest): ReadMailResponse {
        val result = mailService.readMail(PlayerId(request.playerId), request.mailId)

        return when (result) {
            is Result.Success -> readMailResponse {
                success = true
                mail = result.value.toProto()
            }
            is Result.Failure -> readMailResponse {
                success = false
                errorMessage = result.error.message
            }
        }
    }

    override suspend fun claimAttachments(request: ClaimAttachmentsRequest): ClaimAttachmentsResponse {
        val result = mailService.claimAttachments(PlayerId(request.playerId), request.mailId)

        return when (result) {
            is Result.Success -> claimAttachmentsResponse {
                success = true
                claimedItems.addAll(result.value.map { it.toProto() })
            }
            is Result.Failure -> claimAttachmentsResponse {
                success = false
                errorMessage = result.error.message
            }
        }
    }

    override suspend fun deleteMail(request: DeleteMailRequest): DeleteMailResponse {
        val result = mailService.deleteMail(PlayerId(request.playerId), request.mailId)

        return when (result) {
            is Result.Success -> deleteMailResponse { success = true }
            is Result.Failure -> deleteMailResponse {
                success = false
                errorMessage = result.error.message
            }
        }
    }

    override suspend fun sendSystemMail(request: SendSystemMailRequest): SendSystemMailResponse {
        val recipientIds = request.recipientIdsList.map { PlayerId(it) }
        if (recipientIds.isEmpty() && !request.sendToAll) {
            return sendSystemMailResponse {
                success = false
                errorMessage = "收件人列表为空"
            }
        }

        val count = mailService.sendSystemMail(
            subject = request.subject,
            content = request.content,
            recipientIds = recipientIds,
            attachments = request.attachmentsList.map { it.toModel() },
            expiresAt = request.expiresAt.takeIf { it > 0 }
        )

        return sendSystemMailResponse {
            success = true
            sentCount = count
        }
    }
}

// ─── 转换函数 ────────────────────────────────────────────

private fun com.azathoth.core.grpc.mail.MailType.toModel(): MailType = when (this) {
    com.azathoth.core.grpc.mail.MailType.PLAYER -> MailType.PLAYER
    com.azathoth.core.grpc.mail.MailType.SYSTEM -> MailType.SYSTEM
    com.azathoth.core.grpc.mail.MailType.REWARD -> MailType.REWARD
    com.azathoth.core.grpc.mail.MailType.GUILD -> MailType.GUILD
    com.azathoth.core.grpc.mail.MailType.TRADE -> MailType.TRADE
    else -> MailType.SYSTEM
}

private fun MailType.toProto(): com.azathoth.core.grpc.mail.MailType = when (this) {
    MailType.PLAYER -> com.azathoth.core.grpc.mail.MailType.PLAYER
    MailType.SYSTEM -> com.azathoth.core.grpc.mail.MailType.SYSTEM
    MailType.REWARD -> com.azathoth.core.grpc.mail.MailType.REWARD
    MailType.GUILD -> com.azathoth.core.grpc.mail.MailType.GUILD
    MailType.TRADE -> com.azathoth.core.grpc.mail.MailType.TRADE
}

private fun Attachment.toModel(): MailAttachment = MailAttachment(
    itemId = itemId,
    amount = amount,
    metadata = metadataMap
)

private fun MailAttachment.toProto(): Attachment = attachment {
    itemId = this@toProto.itemId
    amount = this@toProto.amount
    metadata.putAll(this@toProto.metadata)
}

private fun com.azathoth.services.mail.model.Mail.toProto(): MailData = mailData {
    mailId = this@toProto.mailId
    senderId = this@toProto.senderId?.value ?: ""
    senderName = this@toProto.senderName
    recipientId = this@toProto.recipientId.value
    subject = this@toProto.subject
    content = this@toProto.content
    type = this@toProto.type.toProto()
    attachments.addAll(this@toProto.attachments.map { it.toProto() })
    read = this@toProto.read
    claimed = this@toProto.claimed
    sentAt = this@toProto.sentAt
    expiresAt = this@toProto.expiresAt
}
