package com.azathoth.gateway.auth

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.ErrorCodes
import com.azathoth.core.common.result.Result
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

// --- Data Classes ---

data class SimpleAuthRequest(
    override val authType: AuthType,
    override val username: String,
    override val credentials: String,
    override val clientInfo: ClientInfo,
    override val requestTime: Long = System.currentTimeMillis()
) : AuthRequest

data class SimpleClientInfo(
    override val version: String,
    override val protocolVersion: Int,
    override val brand: String? = null,
    override val remoteAddress: String,
    override val deviceId: String? = null
) : ClientInfo

data class SimpleAuthResult(
    override val success: Boolean,
    override val playerId: PlayerId? = null,
    override val playerName: String? = null,
    override val accessToken: String? = null,
    override val tokenExpireAt: Long? = null,
    override val properties: Map<String, String> = emptyMap(),
    override val error: String? = null,
    override val errorCode: String? = null
) : AuthResult

// --- DefaultAuthenticatorChain ---

class DefaultAuthenticatorChain : AuthenticatorChain {
    private val authenticators = ConcurrentHashMap<AuthType, Authenticator>()

    override fun addAuthenticator(authenticator: Authenticator) {
        authenticator.supportedTypes.forEach { type ->
            authenticators[type] = authenticator
            logger.info { "注册认证器: $type -> ${authenticator::class.simpleName}" }
        }
    }

    override fun removeAuthenticator(authType: AuthType) {
        authenticators.remove(authType)
        logger.info { "移除认证器: $authType" }
    }

    override fun getAuthenticator(authType: AuthType): Authenticator? = authenticators[authType]

    override suspend fun authenticate(request: AuthRequest): Result<AuthResult> {
        val authenticator = authenticators[request.authType]
            ?: return Result.failure(
                ErrorCodes.UNAUTHENTICATED,
                "不支持的认证类型: ${request.authType}"
            )
        return authenticator.authenticate(request)
    }
}

// --- OfflineModeAuthenticator ---

class OfflineModeAuthenticator : Authenticator {
    override val supportedTypes: Set<AuthType> = setOf(AuthType.PASSWORD)

    override suspend fun authenticate(request: AuthRequest): Result<AuthResult> {
        val username = request.username
        if (username.isBlank()) {
            return Result.failure(ErrorCodes.INVALID_ARGUMENT, "用户名不能为空")
        }
        if (username.length > 16) {
            return Result.failure(ErrorCodes.INVALID_ARGUMENT, "用户名长度不能超过16个字符")
        }

        val offlineUuid = UUID.nameUUIDFromBytes("OfflinePlayer:$username".toByteArray())
        val playerId = PlayerId.of(offlineUuid)
        val token = UUID.randomUUID().toString()

        logger.debug { "离线认证成功: $username -> $offlineUuid" }

        return Result.success(
            SimpleAuthResult(
                success = true,
                playerId = playerId,
                playerName = username,
                accessToken = token,
                tokenExpireAt = System.currentTimeMillis() + 86_400_000L,
                properties = mapOf("auth_mode" to "offline")
            )
        )
    }

    override suspend fun validateToken(token: String): Result<AuthResult> {
        return Result.failure(ErrorCodes.TOKEN_INVALID, "离线模式不支持令牌验证")
    }

    override suspend fun refreshToken(refreshToken: String): Result<AuthResult> {
        return Result.failure(ErrorCodes.TOKEN_INVALID, "离线模式不支持令牌刷新")
    }

    override suspend fun revokeToken(token: String) {
        // 离线模式无需操作
    }
}

// --- InMemoryLoginRateLimiter ---

class InMemoryLoginRateLimiter(
    private val maxAttempts: Int = 5,
    private val lockoutDurationMs: Long = 300_000L // 5 minutes
) : LoginRateLimiter {

    private data class AttemptRecord(
        var failures: Int = 0,
        var lockoutUntil: Long = 0L
    )

    private val records = ConcurrentHashMap<String, AttemptRecord>()

    override suspend fun allowAttempt(identifier: String): Boolean {
        val record = records[identifier] ?: return true
        if (record.lockoutUntil > 0 && System.currentTimeMillis() < record.lockoutUntil) {
            return false
        }
        // 锁定期过了，重置
        if (record.lockoutUntil > 0 && System.currentTimeMillis() >= record.lockoutUntil) {
            records.remove(identifier)
            return true
        }
        return record.failures < maxAttempts
    }

    override suspend fun recordFailure(identifier: String) {
        val record = records.getOrPut(identifier) { AttemptRecord() }
        record.failures++
        if (record.failures >= maxAttempts) {
            record.lockoutUntil = System.currentTimeMillis() + lockoutDurationMs
            logger.warn { "账户已锁定: $identifier, 锁定至 ${record.lockoutUntil}" }
        }
    }

    override suspend fun recordSuccess(identifier: String) {
        records.remove(identifier)
    }

    override suspend fun getRemainingAttempts(identifier: String): Int {
        val record = records[identifier] ?: return maxAttempts
        if (record.lockoutUntil > 0 && System.currentTimeMillis() >= record.lockoutUntil) {
            records.remove(identifier)
            return maxAttempts
        }
        return (maxAttempts - record.failures).coerceAtLeast(0)
    }

    override suspend fun getLockoutRemaining(identifier: String): Long {
        val record = records[identifier] ?: return 0L
        if (record.lockoutUntil <= 0) return 0L
        val remaining = record.lockoutUntil - System.currentTimeMillis()
        return if (remaining > 0) remaining / 1000 else 0L
    }

    override suspend fun reset(identifier: String) {
        records.remove(identifier)
    }
}
