package com.azathoth.services.admin.auth

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AuthServiceTest {

    private lateinit var authService: AuthService

    @BeforeEach
    fun setup() {
        authService = AuthService(
            jwtSecret = "test-secret",
            accessTokenExpireMinutes = 30,
            refreshTokenExpireDays = 7
        )
    }

    @Test
    fun `登录成功返回 token 和用户信息`() {
        val result = authService.login("admin", "azathoth_dev")

        assertTrue(result.success)
        assertNotNull(result.token)
        assertNotNull(result.user)
        assertEquals("admin", result.user?.username)
        assertEquals("SUPER_ADMIN", result.user?.role)
        assertTrue(result.token!!.accessToken.isNotBlank())
        assertTrue(result.token!!.refreshToken.isNotBlank())
        assertEquals("Bearer", result.token!!.tokenType)
    }

    @Test
    fun `错误密码登录失败`() {
        val result = authService.login("admin", "wrong_password")

        assertFalse(result.success)
        assertNull(result.token)
        assertNull(result.user)
        assertNotNull(result.error)
    }

    @Test
    fun `不存在的用户登录失败`() {
        val result = authService.login("nonexistent", "password")

        assertFalse(result.success)
        assertNotNull(result.error)
    }

    @Test
    fun `refresh token 刷新成功`() {
        val loginResult = authService.login("admin", "azathoth_dev")
        val refreshToken = loginResult.token!!.refreshToken

        val refreshResult = authService.refresh(refreshToken)

        assertTrue(refreshResult.success)
        assertNotNull(refreshResult.token)
        assertNotNull(refreshResult.user)
        assertEquals("admin", refreshResult.user?.username)
        assertTrue(refreshResult.token!!.accessToken.isNotBlank())
    }

    @Test
    fun `无效 refresh token 刷新失败`() {
        val result = authService.refresh("invalid-token")

        assertFalse(result.success)
        assertNotNull(result.error)
    }

    @Test
    fun `refresh token 只能使用一次`() {
        val loginResult = authService.login("admin", "azathoth_dev")
        val refreshToken = loginResult.token!!.refreshToken

        val first = authService.refresh(refreshToken)
        assertTrue(first.success)

        val second = authService.refresh(refreshToken)
        assertFalse(second.success)
    }

    @Test
    fun `logout 后 refresh token 失效`() {
        val loginResult = authService.login("admin", "azathoth_dev")
        val refreshToken = loginResult.token!!.refreshToken

        authService.logout(loginResult.user!!.userId)

        val result = authService.refresh(refreshToken)
        assertFalse(result.success)
    }

    @Test
    fun `getUserById 返回正确用户`() {
        val user = authService.getUserById("admin-001")

        assertNotNull(user)
        assertEquals("admin", user?.username)
        assertEquals("SUPER_ADMIN", user?.role)
    }

    @Test
    fun `getUserById 不存在的用户返回 null`() {
        val user = authService.getUserById("nonexistent")
        assertNull(user)
    }
}
