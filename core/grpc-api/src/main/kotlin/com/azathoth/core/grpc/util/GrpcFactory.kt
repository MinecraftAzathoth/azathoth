package com.azathoth.core.grpc.util

import io.github.oshai.kotlinlogging.KotlinLogging
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.BindableService
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

/**
 * gRPC 服务器构建工具
 */
object GrpcServerFactory {

    /**
     * 创建并启动 gRPC 服务器
     */
    fun create(
        port: Int,
        services: List<BindableService>,
        maxInboundMessageSize: Int = 4 * 1024 * 1024 // 4MB
    ): Server {
        val builder = ServerBuilder.forPort(port)
            .maxInboundMessageSize(maxInboundMessageSize)

        services.forEach { service ->
            builder.addService(service)
            logger.info { "注册 gRPC 服务: ${service.bindService().serviceDescriptor.name}" }
        }

        val server = builder.build()
        logger.info { "gRPC 服务器创建完成，端口: $port" }
        return server
    }

    /**
     * 启动服务器并阻塞等待关闭
     */
    fun startAndBlock(server: Server) {
        server.start()
        logger.info { "gRPC 服务器已启动，端口: ${server.port}" }

        Runtime.getRuntime().addShutdownHook(Thread {
            logger.info { "正在关闭 gRPC 服务器..." }
            server.shutdown()
            try {
                if (!server.awaitTermination(30, TimeUnit.SECONDS)) {
                    server.shutdownNow()
                }
            } catch (e: InterruptedException) {
                server.shutdownNow()
            }
            logger.info { "gRPC 服务器已关闭" }
        })

        server.awaitTermination()
    }
}

/**
 * gRPC 客户端通道工厂
 */
object GrpcChannelFactory {

    /**
     * 创建 gRPC 通道
     */
    fun create(
        host: String,
        port: Int,
        usePlaintext: Boolean = true,
        maxInboundMessageSize: Int = 4 * 1024 * 1024
    ): ManagedChannel {
        val builder = ManagedChannelBuilder.forAddress(host, port)
            .maxInboundMessageSize(maxInboundMessageSize)

        if (usePlaintext) {
            builder.usePlaintext()
        }

        val channel = builder.build()
        logger.info { "gRPC 通道已创建: $host:$port" }
        return channel
    }

    /**
     * 安全关闭通道
     */
    fun shutdown(channel: ManagedChannel, timeoutSeconds: Long = 5) {
        channel.shutdown()
        try {
            if (!channel.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
                channel.shutdownNow()
            }
        } catch (e: InterruptedException) {
            channel.shutdownNow()
        }
    }
}
