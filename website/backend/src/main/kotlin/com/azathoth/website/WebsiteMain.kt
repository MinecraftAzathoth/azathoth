package com.azathoth.website

import com.azathoth.website.module.market.routes.configureMarketRoutes
import com.azathoth.website.module.market.table.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.github.oshai.kotlinlogging.KotlinLogging
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
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.migration.jdbc.MigrationUtils

private val logger = KotlinLogging.logger {}

/**
 * Azathoth Website Backend 入口点
 *
 * 提供：
 * - REST API 服务
 * - 开发者市场后端
 * - 项目生成器服务
 */
fun main() {
    logger.info { "Starting Azathoth Website Backend" }

    embeddedServer(Netty, port = 8080) {
        configurePlugins()
        configureSecurity()
        configureDatabase()
        configureRouting()
        configureMarketRoutes()
    }.start(wait = true)
}

fun Application.configurePlugins() {
    // JSON 序列化
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    // CORS
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        anyHost()
    }

    // 错误处理
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            logger.error(cause) { "Unhandled exception" }
            call.respondText(
                text = """{"success": false, "error": {"code": "INTERNAL_ERROR", "message": "${cause.message}"}}""",
                contentType = ContentType.Application.Json,
                status = HttpStatusCode.InternalServerError
            )
        }
    }
}

fun Application.configureSecurity() {
    val jwtSecret = environment.config.propertyOrNull("jwt.secret")?.getString() ?: "dev-secret-key"
    val jwtIssuer = environment.config.propertyOrNull("jwt.issuer")?.getString() ?: "azathoth"
    val jwtRealm = environment.config.propertyOrNull("jwt.realm")?.getString() ?: "azathoth"

    install(Authentication) {
        jwt("jwt") {
            realm = jwtRealm
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withIssuer(jwtIssuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("userId").asString() != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Token invalid or expired"))
            }
        }
    }
}

fun Application.configureDatabase() {
    val jdbcUrl = environment.config.propertyOrNull("database.url")?.getString()
        ?: "jdbc:postgresql://localhost:5432/azathoth"
    val user = environment.config.propertyOrNull("database.user")?.getString() ?: "azathoth"
    val password = environment.config.propertyOrNull("database.password")?.getString() ?: "azathoth"

    Database.connect(
        url = jdbcUrl,
        driver = "org.postgresql.Driver",
        user = user,
        password = password
    )

    transaction {
        MigrationUtils.statementsRequiredForDatabaseMigration(
            Resources,
            ResourceVersions,
            ResourcePricing,
            ResourceTags,
            ResourceScreenshots,
            ResourceDependencies,
            ResourceReviews,
            ReviewHelpful,
            DownloadRecords,
            UserPurchases
        )
    }

    logger.info { "Database connected and tables created" }
}

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Azathoth API Server")
        }

        get("/health") {
            call.respondText("OK")
        }
    }
}
