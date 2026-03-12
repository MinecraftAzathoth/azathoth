package com.azathoth.services.admin.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.azathoth.services.admin.api.model.AuthResult
import com.azathoth.services.admin.api.model.AuthToken
import com.azathoth.services.admin.api.model.UserInfo
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * JWT 认证服务
 *
 * 管理 admin 用户的认证、令牌签发与刷新。
 * 当前使用内存存储，后续可替换为数据库实现。
 */
class AuthService(
    private val jwtSecret: String = System.getenv("JWT_SECRET") ?: "azathoth-admin-dev-secret-key",
    private val jwtIssuer: String = "azathoth-admin",
    private val jwtAudience: String = "azathoth-admin-api",
    private val accessTokenExpireMinutes: Long = 30,
    private val refreshTokenExpireDays: Long = 7
) {
    /** 内存用户存储（开发用） */
    private val users = ConcurrentHashMap<String, AdminUser>()

    /** 活跃的 refresh token → userId */
    private val refreshTokens = ConcurrentHashMap<String, String>()

    val algorithm: Algorithm = Algorithm.HMAC256(jwtSecret)
    val issuer: String get() = jwtIssuer
    val audience: String get() = jwtAudience

    init {
        // 默认管理员账户
        users["admin"] = AdminUser(
            userId = "admin-001",
            username = "admin",
            passwordHash = hashPassword("azathoth_dev"),
            email = "admin@azathoth.dev",
            role = "SUPER_ADMIN",
            permissions = listOf("*"),
            verified = true,
            createdAt = "2024-01-01T00:00:00Z"
        )
    }

    fun login(username: String, password: String): AuthResult {
        val user = users[username]
            ?: return AuthResult(success = false, error = "用户名或密码错误")

        if (!verifyPassword(password, user.passwordHash)) {
            return AuthResult(success = false, error = "用户名或密码错误")
        }

        val token = generateTokenPair(user)
        return AuthResult(
            success = true,
            user = user.toUserInfo(),
            token = token
        )
    }

    fun refresh(refreshToken: String): AuthResult {
        val userId = refreshTokens.remove(refreshToken)
            ?: return AuthResult(success = false, error = "无效的刷新令牌")

        val user = users.values.find { it.userId == userId }
            ?: return AuthResult(success = false, error = "用户不存在")

        val token = generateTokenPair(user)
        return AuthResult(
            success = true,
            user = user.toUserInfo(),
            token = token
        )
    }

    fun logout(userId: String) {
        refreshTokens.entries.removeIf { it.value == userId }
    }

    fun getUserById(userId: String): UserInfo? =
        users.values.find { it.userId == userId }?.toUserInfo()

    private fun generateTokenPair(user: AdminUser): AuthToken {
        val now = Instant.now()
        val accessExpiry = now.plus(accessTokenExpireMinutes, ChronoUnit.MINUTES)
        val refreshExpiry = now.plus(refreshTokenExpireDays, ChronoUnit.DAYS)

        val accessToken = JWT.create()
            .withIssuer(jwtIssuer)
            .withAudience(jwtAudience)
            .withSubject(user.userId)
            .withClaim("username", user.username)
            .withClaim("role", user.role)
            .withIssuedAt(now)
            .withExpiresAt(accessExpiry)
            .sign(algorithm)

        val refreshToken = UUID.randomUUID().toString()
        refreshTokens[refreshToken] = user.userId

        return AuthToken(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAt = accessExpiry.toString(),
            tokenType = "Bearer"
        )
    }

    private fun hashPassword(password: String): String =
        password.hashCode().toString(16)

    private fun verifyPassword(password: String, hash: String): Boolean =
        hashPassword(password) == hash
}

data class AdminUser(
    val userId: String,
    val username: String,
    val passwordHash: String,
    val email: String,
    val role: String,
    val permissions: List<String>,
    val verified: Boolean,
    val createdAt: String,
    val lastLoginAt: String? = null
) {
    fun toUserInfo() = UserInfo(
        userId = userId,
        username = username,
        email = email,
        role = role,
        permissions = permissions,
        verified = verified,
        createdAt = createdAt,
        lastLoginAt = lastLoginAt
    )
}
