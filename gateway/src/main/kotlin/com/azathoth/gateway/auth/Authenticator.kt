package com.azathoth.gateway.auth

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.Result
import com.azathoth.core.protocol.channel.Connection

/**
 * 认证类型
 */
enum class AuthType {
    /** 用户名密码 */
    PASSWORD,
    /** 令牌 */
    TOKEN,
    /** OAuth */
    OAUTH,
    /** Minecraft 官方认证 */
    MOJANG,
    /** Microsoft 认证 */
    MICROSOFT
}

/**
 * 认证请求
 */
interface AuthRequest {
    /** 认证类型 */
    val authType: AuthType
    
    /** 用户名 */
    val username: String
    
    /** 凭证（密码/令牌等） */
    val credentials: String
    
    /** 客户端信息 */
    val clientInfo: ClientInfo
    
    /** 请求时间 */
    val requestTime: Long
}

/**
 * 客户端信息
 */
interface ClientInfo {
    /** 客户端版本 */
    val version: String
    
    /** 协议版本 */
    val protocolVersion: Int
    
    /** 客户端品牌 */
    val brand: String?
    
    /** 远程地址 */
    val remoteAddress: String
    
    /** 设备ID */
    val deviceId: String?
}

/**
 * 认证结果
 */
interface AuthResult {
    /** 是否成功 */
    val success: Boolean
    
    /** 玩家ID */
    val playerId: PlayerId?
    
    /** 玩家名称 */
    val playerName: String?
    
    /** 访问令牌 */
    val accessToken: String?
    
    /** 令牌过期时间 */
    val tokenExpireAt: Long?
    
    /** 玩家属性 */
    val properties: Map<String, String>
    
    /** 错误信息 */
    val error: String?
    
    /** 错误码 */
    val errorCode: String?
}

/**
 * 认证器
 */
interface Authenticator {
    /** 支持的认证类型 */
    val supportedTypes: Set<AuthType>
    
    /**
     * 认证
     */
    suspend fun authenticate(request: AuthRequest): Result<AuthResult>
    
    /**
     * 验证令牌
     */
    suspend fun validateToken(token: String): Result<AuthResult>
    
    /**
     * 刷新令牌
     */
    suspend fun refreshToken(refreshToken: String): Result<AuthResult>
    
    /**
     * 注销令牌
     */
    suspend fun revokeToken(token: String)
}

/**
 * 认证链（支持多种认证方式）
 */
interface AuthenticatorChain {
    /**
     * 添加认证器
     */
    fun addAuthenticator(authenticator: Authenticator)
    
    /**
     * 移除认证器
     */
    fun removeAuthenticator(authType: AuthType)
    
    /**
     * 获取认证器
     */
    fun getAuthenticator(authType: AuthType): Authenticator?
    
    /**
     * 执行认证
     */
    suspend fun authenticate(request: AuthRequest): Result<AuthResult>
}

/**
 * 登录限流器
 */
interface LoginRateLimiter {
    /**
     * 检查是否允许登录尝试
     */
    suspend fun allowAttempt(identifier: String): Boolean
    
    /**
     * 记录失败尝试
     */
    suspend fun recordFailure(identifier: String)
    
    /**
     * 记录成功尝试
     */
    suspend fun recordSuccess(identifier: String)
    
    /**
     * 获取剩余尝试次数
     */
    suspend fun getRemainingAttempts(identifier: String): Int
    
    /**
     * 获取锁定剩余时间（秒）
     */
    suspend fun getLockoutRemaining(identifier: String): Long
    
    /**
     * 重置尝试计数
     */
    suspend fun reset(identifier: String)
}
