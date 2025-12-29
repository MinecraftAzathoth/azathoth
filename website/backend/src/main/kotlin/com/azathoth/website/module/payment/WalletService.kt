package com.azathoth.website.module.payment

/**
 * 钱包服务接口
 */
interface WalletService {
    /**
     * 获取余额
     */
    suspend fun getBalance(userId: String): Balance

    /**
     * 申请提现
     */
    suspend fun requestWithdraw(userId: String, amount: Long, channel: WithdrawChannel, accountInfo: String): WithdrawRequest?

    /**
     * 获取提现记录
     */
    suspend fun getWithdrawRequests(userId: String, page: Int, pageSize: Int): List<WithdrawRequest>

    /**
     * 获取收益记录
     */
    suspend fun getEarningsRecords(userId: String, page: Int, pageSize: Int): List<EarningsRecord>

    /**
     * 获取交易记录
     */
    suspend fun getTransactions(userId: String, page: Int, pageSize: Int): List<TransactionRecord>

    /**
     * 获取收益统计
     */
    suspend fun getEarningsStats(userId: String): EarningsStats
}

/**
 * 提现审核服务接口
 */
interface WithdrawReviewService {
    /**
     * 获取待审核提现
     */
    suspend fun getPendingWithdraws(page: Int, pageSize: Int): List<WithdrawRequest>

    /**
     * 审核通过
     */
    suspend fun approve(withdrawId: String, operatorId: String): Boolean

    /**
     * 审核拒绝
     */
    suspend fun reject(withdrawId: String, operatorId: String, reason: String): Boolean

    /**
     * 标记处理完成
     */
    suspend fun markCompleted(withdrawId: String, operatorId: String): Boolean
}
