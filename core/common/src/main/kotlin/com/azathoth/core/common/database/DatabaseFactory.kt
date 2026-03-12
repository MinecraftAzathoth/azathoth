package com.azathoth.core.common.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

private val logger = KotlinLogging.logger {}

/**
 * 数据库配置
 */
data class DatabaseConfig(
    val jdbcUrl: String,
    val driver: String = "org.postgresql.Driver",
    val username: String,
    val password: String,
    val maxPoolSize: Int = 10,
    val minIdle: Int = 2,
    val idleTimeoutMs: Long = 600_000,
    val connectionTimeoutMs: Long = 30_000,
    val maxLifetimeMs: Long = 1_800_000
) {
    companion object {
        /**
         * 从环境变量创建配置，带默认开发值
         */
        fun fromEnv(
            prefix: String = "DB",
            defaultDb: String = "azathoth"
        ): DatabaseConfig {
            val host = System.getenv("${prefix}_HOST") ?: "localhost"
            val port = System.getenv("${prefix}_PORT") ?: "5432"
            val db = System.getenv("${prefix}_NAME") ?: defaultDb
            return DatabaseConfig(
                jdbcUrl = System.getenv("${prefix}_URL")
                    ?: "jdbc:postgresql://$host:$port/$db",
                username = System.getenv("${prefix}_USER") ?: "azathoth",
                password = System.getenv("${prefix}_PASSWORD") ?: "azathoth_dev",
                maxPoolSize = System.getenv("${prefix}_MAX_POOL_SIZE")?.toIntOrNull() ?: 10
            )
        }
    }
}

/**
 * 数据库工厂
 *
 * 管理 HikariCP 连接池和 Exposed 数据库初始化。
 */
class DatabaseFactory private constructor(
    private val dataSource: HikariDataSource,
    val database: Database
) : AutoCloseable {

    /**
     * 在挂起事务中执行数据库操作
     */
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        withContext(Dispatchers.IO) {
            suspendTransaction(db = database) { block() }
        }

    /**
     * 创建表（如果不存在）
     */
    fun createTables(vararg tables: Table) {
        transaction(database) {
            SchemaUtils.create(*tables)
        }
        logger.info { "数据库表已创建/验证: ${tables.joinToString { it.tableName }}" }
    }

    override fun close() {
        dataSource.close()
        logger.info { "数据库连接池已关闭" }
    }

    companion object {
        /**
         * 创建数据库工厂实例
         */
        fun create(config: DatabaseConfig): DatabaseFactory {
            val hikariConfig = HikariConfig().apply {
                jdbcUrl = config.jdbcUrl
                driverClassName = config.driver
                username = config.username
                password = config.password
                maximumPoolSize = config.maxPoolSize
                minimumIdle = config.minIdle
                idleTimeout = config.idleTimeoutMs
                connectionTimeout = config.connectionTimeoutMs
                maxLifetime = config.maxLifetimeMs
                isAutoCommit = false
            }

            val dataSource = HikariDataSource(hikariConfig)
            val database = Database.connect(dataSource)

            logger.info { "数据库连接池已创建: ${config.jdbcUrl} (pool=${config.maxPoolSize})" }
            return DatabaseFactory(dataSource, database)
        }
    }
}
