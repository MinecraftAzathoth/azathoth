package com.azathoth.website.module.auth

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
