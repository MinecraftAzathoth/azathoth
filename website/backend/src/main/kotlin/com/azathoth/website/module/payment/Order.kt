package com.azathoth.website.module.payment

import java.time.Instant

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
