package com.azathoth.gateway.auth

import com.azathoth.core.common.result.Result
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DefaultAuthenticatorChainTest {

    @Test
    fun `offline authenticator accepts valid username`() = runTest {
        val auth = OfflineModeAuthenticator()
        val request = SimpleAuthRequest(
            authType = AuthType.PASSWORD,
            username = "TestPlayer",
            credentials = "",
            clientInfo = SimpleClientInfo(
                version = "1.21",
                protocolVersion = 767,
                remoteAddress = "127.0.0.1"
            )
        )
        val result = auth.authenticate(request)
        assertTrue(result.isSuccess)
        val authResult = (result as Result.Success).value
        assertTrue(authResult.success)
        assertEquals("TestPlayer", authResult.playerName)
        assertNotNull(authResult.playerId)
        assertNotNull(authResult.accessToken)
    }

    @Test
    fun `offline authenticator rejects blank username`() = runTest {
        val auth = OfflineModeAuthenticator()
        val request = SimpleAuthRequest(
            authType = AuthType.PASSWORD,
            username = "  ",
            credentials = "",
            clientInfo = SimpleClientInfo(
                version = "1.21",
                protocolVersion = 767,
                remoteAddress = "127.0.0.1"
            )
        )
        val result = auth.authenticate(request)
        assertTrue(result.isFailure)
    }

    @Test
    fun `offline authenticator rejects username longer than 16 chars`() = runTest {
        val auth = OfflineModeAuthenticator()
        val request = SimpleAuthRequest(
            authType = AuthType.PASSWORD,
            username = "A".repeat(17),
            credentials = "",
            clientInfo = SimpleClientInfo(
                version = "1.21",
                protocolVersion = 767,
                remoteAddress = "127.0.0.1"
            )
        )
        val result = auth.authenticate(request)
        assertTrue(result.isFailure)
    }

    @Test
    fun `offline authenticator generates deterministic UUID for same username`() = runTest {
        val auth = OfflineModeAuthenticator()
        fun makeRequest(name: String) = SimpleAuthRequest(
            authType = AuthType.PASSWORD,
            username = name,
            credentials = "",
            clientInfo = SimpleClientInfo(version = "1.21", protocolVersion = 767, remoteAddress = "127.0.0.1")
        )
        val r1 = (auth.authenticate(makeRequest("Steve")) as Result.Success).value
        val r2 = (auth.authenticate(makeRequest("Steve")) as Result.Success).value
        assertEquals(r1.playerId, r2.playerId)
    }

    @Test
    fun `chain delegates to correct authenticator`() = runTest {
        val chain = DefaultAuthenticatorChain()
        chain.addAuthenticator(OfflineModeAuthenticator())

        val request = SimpleAuthRequest(
            authType = AuthType.PASSWORD,
            username = "Player1",
            credentials = "",
            clientInfo = SimpleClientInfo(version = "1.21", protocolVersion = 767, remoteAddress = "127.0.0.1")
        )
        val result = chain.authenticate(request)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `chain returns failure for unsupported auth type`() = runTest {
        val chain = DefaultAuthenticatorChain()
        chain.addAuthenticator(OfflineModeAuthenticator())

        val request = SimpleAuthRequest(
            authType = AuthType.MOJANG,
            username = "Player1",
            credentials = "",
            clientInfo = SimpleClientInfo(version = "1.21", protocolVersion = 767, remoteAddress = "127.0.0.1")
        )
        val result = chain.authenticate(request)
        assertTrue(result.isFailure)
    }

    @Test
    fun `rate limiter allows initial attempts`() = runTest {
        val limiter = InMemoryLoginRateLimiter(maxAttempts = 3, lockoutDurationMs = 60_000L)
        assertTrue(limiter.allowAttempt("user1"))
        assertEquals(3, limiter.getRemainingAttempts("user1"))
    }

    @Test
    fun `rate limiter blocks after max failures`() = runTest {
        val limiter = InMemoryLoginRateLimiter(maxAttempts = 3, lockoutDurationMs = 60_000L)
        repeat(3) { limiter.recordFailure("user1") }
        assertFalse(limiter.allowAttempt("user1"))
        assertEquals(0, limiter.getRemainingAttempts("user1"))
        assertTrue(limiter.getLockoutRemaining("user1") > 0)
    }

    @Test
    fun `rate limiter resets on success`() = runTest {
        val limiter = InMemoryLoginRateLimiter(maxAttempts = 3, lockoutDurationMs = 60_000L)
        repeat(2) { limiter.recordFailure("user1") }
        limiter.recordSuccess("user1")
        assertTrue(limiter.allowAttempt("user1"))
        assertEquals(3, limiter.getRemainingAttempts("user1"))
    }

    @Test
    fun `rate limiter reset clears state`() = runTest {
        val limiter = InMemoryLoginRateLimiter(maxAttempts = 3, lockoutDurationMs = 60_000L)
        repeat(3) { limiter.recordFailure("user1") }
        limiter.reset("user1")
        assertTrue(limiter.allowAttempt("user1"))
    }
}
