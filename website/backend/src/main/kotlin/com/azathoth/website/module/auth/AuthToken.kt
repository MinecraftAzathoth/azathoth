package com.azathoth.website.module.auth

import java.time.Instant

/**
 * 认证令牌
 */
interface AuthToken {
    val accessToken: String
    val refreshToken: String
    val expiresAt: Instant
    val tokenType: String
}

/**
 * 登录请求
 */
interface LoginRequest {
    val username: String
    val password: String
    val remember: Boolean
}

/**
 * 注册请求
 */
interface RegisterRequest {
    val username: String
    val email: String
    val password: String
    val inviteCode: String?
}

/**
 * 认证结果
 */
interface AuthResult {
    val success: Boolean
    val user: UserInfo?
    val token: AuthToken?
    val error: String?
}
