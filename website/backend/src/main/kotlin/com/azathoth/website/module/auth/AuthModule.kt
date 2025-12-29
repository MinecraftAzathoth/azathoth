package com.azathoth.website.module.auth

import java.time.Instant

/**
 * 用户角色
 */
enum class UserRole {
    USER,           // 普通用户
    DEVELOPER,      // 开发者
    MODERATOR,      // 版主
    ADMIN,          // 管理员
    SUPER_ADMIN     // 超级管理员
}

/**
 * 用户信息
 */
interface UserInfo {
    val userId: String
    val username: String
    val email: String
    val avatarUrl: String?
    val role: UserRole
    val verified: Boolean
    val createdAt: Instant
    val lastLoginAt: Instant?
}

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

/**
 * 认证服务接口
 */
interface AuthService {
    /**
     * 用户登录
     */
    suspend fun login(request: LoginRequest): AuthResult

    /**
     * 用户注册
     */
    suspend fun register(request: RegisterRequest): AuthResult

    /**
     * 刷新令牌
     */
    suspend fun refreshToken(refreshToken: String): AuthResult

    /**
     * 登出
     */
    suspend fun logout(accessToken: String): Boolean

    /**
     * 验证令牌
     */
    suspend fun validateToken(accessToken: String): UserInfo?

    /**
     * 发送验证邮件
     */
    suspend fun sendVerificationEmail(userId: String): Boolean

    /**
     * 验证邮箱
     */
    suspend fun verifyEmail(token: String): Boolean

    /**
     * 重置密码请求
     */
    suspend fun requestPasswordReset(email: String): Boolean

    /**
     * 重置密码
     */
    suspend fun resetPassword(token: String, newPassword: String): Boolean
}

/**
 * 用户服务接口
 */
interface UserService {
    /**
     * 获取用户信息
     */
    suspend fun getUser(userId: String): UserInfo?

    /**
     * 通过用户名获取用户
     */
    suspend fun getUserByUsername(username: String): UserInfo?

    /**
     * 更新用户资料
     */
    suspend fun updateProfile(userId: String, update: ProfileUpdate): UserInfo?

    /**
     * 更新用户角色
     */
    suspend fun updateRole(userId: String, role: UserRole): Boolean

    /**
     * 封禁用户
     */
    suspend fun banUser(userId: String, reason: String, duration: Long?): Boolean

    /**
     * 解封用户
     */
    suspend fun unbanUser(userId: String): Boolean
}

/**
 * 资料更新
 */
interface ProfileUpdate {
    val displayName: String?
    val bio: String?
    val avatarUrl: String?
    val website: String?
    val github: String?
}
