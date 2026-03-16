package com.azathoth.services.admin

import com.azathoth.services.admin.api.routes.adminRoutes
import com.azathoth.services.admin.api.routes.authRoutes
import com.azathoth.services.admin.api.routes.rollbackProxyRoutes
import com.azathoth.services.admin.auth.AuthService
import com.azathoth.services.admin.moderation.DefaultModerationService
import com.auth0.jwt.JWT
import io.github.oshai.kotlinlogging.KotlinLogging
import io.grpc.ServerBuilder
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

private val logger = KotlinLogging.logger {}

fun main() {
    logger.info { "正在启动 Admin Service..." }

    val moderationService = DefaultModerationService()
    val authService = AuthService()
    val rollbackClient = HttpClient(CIO)
    logger.info { "业务组件已初始化 (ModerationService, AuthService, RollbackClient)" }

    val grpcPort = System.getenv("GRPC_PORT")?.toIntOrNull() ?: 9090
    val grpcServer = ServerBuilder.forPort(grpcPort).build().start()
    logger.info { "gRPC 服务器已启动，端口: $grpcPort" }

    val httpPort = System.getenv("HTTP_PORT")?.toIntOrNull() ?: 8080
    val httpServer = embeddedServer(Netty, port = httpPort) {
        configurePlugins(authService)
        configureRouting(authService, rollbackClient)
    }.start(wait = false)
    logger.info { "HTTP 服务器已启动，端口: $httpPort" }

    logger.info { "Admin Service 启动完成" }

    Runtime.getRuntime().addShutdownHook(Thread {
        logger.info { "正在关闭 Admin Service..." }
        httpServer.stop(1000, 5000)
        grpcServer.shutdown()
        rollbackClient.close()
        logger.info { "Admin Service 已关闭" }
    })

    grpcServer.awaitTermination()
}

private fun Application.configurePlugins(authService: AuthService) {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = false
            isLenient = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        })
    }

    install(CORS) {
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowCredentials = true
        anyHost()
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            logger.error(cause) { "未处理的异常: ${cause.message}" }
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("success" to false, "error" to (cause.message ?: "内部服务器错误"))
            )
        }
    }

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(
                JWT.require(authService.algorithm)
                    .withIssuer(authService.issuer)
                    .withAudience(authService.audience)
                    .build()
            )
            validate { credential ->
                if (credential.payload.subject != null) {
                    JWTPrincipal(credential.payload)
                } else null
            }
            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("success" to false, "error" to "令牌无效或已过期")
                )
            }
        }
    }
}

private fun Application.configureRouting(authService: AuthService, rollbackClient: HttpClient) {
    routing {
        get("/health/live") { call.respondText("OK") }
        get("/health/ready") { call.respondText("OK") }

        route("/api") {
            authRoutes(authService)

            authenticate("auth-jwt") {
                adminRoutes()
                rollbackProxyRoutes(rollbackClient)
            }
        }
    }
}
