package com.azathoth.website.module.payment

import java.time.Instant

/**
 * 提现申请
 */
interface WithdrawRequest {
    val withdrawId: String
    val userId: String
    val amount: Long
    val fee: Long
    val netAmount: Long
    val channel: WithdrawChannel
    val accountInfo: String
    val status: WithdrawStatus
    val reason: String?
    val createdAt: Instant
    val processedAt: Instant?
}

/**
 * 余额信息
 */
interface Balance {
    val userId: String
    val available: Long       // 可用余额
    val frozen: Long          // 冻结余额
    val total: Long           // 总余额
    val currency: String
}

/**
 * 收益记录
 */
interface EarningsRecord {
    val recordId: String
    val userId: String
    val orderId: String
    val resourceId: String
    val resourceName: String
    val grossAmount: Long     // 总金额
    val platformFee: Long     // 平台抽成
    val netAmount: Long       // 净收入
    val createdAt: Instant
}

/**
 * 交易记录
 */
interface TransactionRecord {
    val transactionId: String
    val userId: String
    val type: TransactionType
    val amount: Long
    val balance: Long         // 交易后余额
    val description: String
    val referenceId: String?
    val createdAt: Instant
}

/**
 * 收益统计
 */
interface EarningsStats {
    val totalEarnings: Long
    val thisMonthEarnings: Long
    val lastMonthEarnings: Long
    val totalWithdrawn: Long
    val pendingWithdraw: Long
}
