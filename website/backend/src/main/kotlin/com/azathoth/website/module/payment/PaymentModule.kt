package com.azathoth.website.module.payment

import java.time.Instant

/**
 * 支付渠道
 */
enum class PaymentChannel {
    WECHAT,
    ALIPAY
}

/**
 * 订单状态
 */
enum class OrderStatus {
    PENDING,
    PAID,
    CANCELLED,
    REFUNDED,
    EXPIRED
}

/**
 * 提现状态
 */
enum class WithdrawStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    REJECTED
}

/**
 * 订单信息
 */
interface Order {
    val orderId: String
    val userId: String
    val resourceId: String
    val resourceName: String
    val amount: Long          // 分为单位
    val currency: String
    val channel: PaymentChannel?
    val status: OrderStatus
    val paymentUrl: String?
    val paidAt: Instant?
    val createdAt: Instant
    val expiresAt: Instant
}

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
 * 提现渠道
 */
enum class WithdrawChannel {
    ALIPAY,
    BANK_CARD
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
 * 交易类型
 */
enum class TransactionType {
    RECHARGE,     // 充值
    PURCHASE,     // 购买
    EARNINGS,     // 收益
    WITHDRAW,     // 提现
    REFUND,       // 退款
    ADJUSTMENT    // 调整
}

/**
 * 支付服务接口
 */
interface PaymentService {
    /**
     * 创建订单
     */
    suspend fun createOrder(userId: String, resourceId: String, channel: PaymentChannel): Order?

    /**
     * 获取订单
     */
    suspend fun getOrder(orderId: String): Order?

    /**
     * 取消订单
     */
    suspend fun cancelOrder(orderId: String): Boolean

    /**
     * 处理支付回调
     */
    suspend fun handlePaymentCallback(channel: PaymentChannel, payload: ByteArray): Boolean

    /**
     * 查询订单状态
     */
    suspend fun queryOrderStatus(orderId: String): OrderStatus

    /**
     * 获取用户订单列表
     */
    suspend fun getUserOrders(userId: String, page: Int, pageSize: Int): List<Order>

    /**
     * 申请退款
     */
    suspend fun requestRefund(orderId: String, reason: String): Boolean
}

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
 * 收益统计
 */
interface EarningsStats {
    val totalEarnings: Long
    val thisMonthEarnings: Long
    val lastMonthEarnings: Long
    val totalWithdrawn: Long
    val pendingWithdraw: Long
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
