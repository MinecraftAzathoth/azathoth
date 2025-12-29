package com.azathoth.website.module.auth

import java.time.Instant

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
 * 资料更新
 */
interface ProfileUpdate {
    val displayName: String?
    val bio: String?
    val avatarUrl: String?
    val website: String?
    val github: String?
}
