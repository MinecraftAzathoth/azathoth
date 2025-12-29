package com.azathoth.website.module.payment

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
 * 提现渠道
 */
enum class WithdrawChannel {
    ALIPAY,
    BANK_CARD
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
