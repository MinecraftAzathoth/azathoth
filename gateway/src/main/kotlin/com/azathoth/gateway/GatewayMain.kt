package com.azathoth.gateway

import com.azathoth.core.common.AzathothConstants
import com.azathoth.gateway.auth.DefaultAuthenticatorChain
import com.azathoth.gateway.auth.InMemoryLoginRateLimiter
import com.azathoth.gateway.auth.OfflineModeAuthenticator
import com.azathoth.gateway.balancer.BalancingStrategy
import com.azathoth.gateway.balancer.DefaultGatewayLoadBalancer
import com.azathoth.gateway.balancer.DefaultHealthChecker
import com.azathoth.gateway.grpc.GatewayServiceGrpcImpl
import com.azathoth.gateway.routing.DefaultInstanceRegistry
import com.azathoth.gateway.routing.DefaultRouter
import com.azathoth.gateway.session.DefaultSessionManager
import com.azathoth.gateway.transfer.DefaultTransferManager
import io.github.oshai.kotlinlogging.KotlinLogging
import io.grpc.ServerBuilder

private val logger = KotlinLogging.logger {}

/**
 * Azathoth Gateway 入口点
 *
 * Gateway 负责：
 * - 玩家连接管理和认证
 * - 负载均衡和路由
 * - 与 Game Instance 的无缝传输
 */
suspend fun main(args: Array<String>) {
    logger.info { "Starting ${AzathothConstants.NAME} Gateway v${AzathothConstants.VERSION}" }

    // 认证子系统
    val authenticatorChain = DefaultAuthenticatorChain().apply {
        addAuthenticator(OfflineModeAuthenticator())
    }
    val rateLimiter = InMemoryLoginRateLimiter()
    logger.info { "认证子系统已初始化 (离线模式)" }

    // 会话管理
    val sessionManager = DefaultSessionManager()
    logger.info { "会话管理器已初始化" }

    // 负载均衡
    val loadBalancer = DefaultGatewayLoadBalancer(BalancingStrategy.ROUND_ROBIN)
    val instanceRegistry = DefaultInstanceRegistry()
    val healthChecker = DefaultHealthChecker(
        instancesProvider = { instanceRegistry.getAllInstances().toList() }
    )
    logger.info { "负载均衡器已初始化 (策略: ${loadBalancer.strategy})" }

    // 路由
    val router = DefaultRouter(instanceRegistry, loadBalancer)
    logger.info { "路由器已初始化" }

    // 传送管理
    val transferManager = DefaultTransferManager()
    logger.info { "传送管理器已初始化" }

    // gRPC 服务器
    val grpcPort = System.getenv("GRPC_PORT")?.toIntOrNull() ?: 9090
    val grpcServer = ServerBuilder.forPort(grpcPort)
        .addService(GatewayServiceGrpcImpl(instanceRegistry))
        .build()
        .start()
    logger.info { "gRPC 服务器已启动，端口: $grpcPort (已注册: GatewayService)" }

    // 启动健康检查
    healthChecker.startPeriodicCheck(30_000L)

    logger.info { "Gateway started on port ${AzathothConstants.DEFAULT_GATEWAY_PORT}" }

    // 等待 gRPC 服务器终止
    Runtime.getRuntime().addShutdownHook(Thread {
        logger.info { "正在关闭 Gateway..." }
        grpcServer.shutdown()
        logger.info { "Gateway 已关闭" }
    })

    grpcServer.awaitTermination()
}
