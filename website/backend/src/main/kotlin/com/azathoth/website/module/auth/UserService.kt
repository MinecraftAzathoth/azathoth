package com.azathoth.website.module.auth

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
