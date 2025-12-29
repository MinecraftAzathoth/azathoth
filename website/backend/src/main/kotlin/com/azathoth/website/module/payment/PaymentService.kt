package com.azathoth.website.module.payment

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
