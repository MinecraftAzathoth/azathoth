package com.azathoth.core.common.result

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ResultTest {

    @Test
    fun `test success result`() {
        val result = Result.success(42)
        assertTrue(result.isSuccess)
        assertFalse(result.isFailure)
        assertEquals(42, result.getOrNull())
        assertNull(result.errorOrNull())
    }

    @Test
    fun `test failure result`() {
        val result = Result.failure(ErrorCodes.NOT_FOUND, "not found")
        assertFalse(result.isSuccess)
        assertTrue(result.isFailure)
        assertNull(result.getOrNull())
        assertEquals(ErrorCodes.NOT_FOUND, result.errorOrNull()?.code)
    }

    @Test
    fun `test map transforms success`() {
        val result = Result.success(10).map { it * 2 }
        assertEquals(20, result.getOrNull())
    }

    @Test
    fun `test map preserves failure`() {
        val result: Result<Int> = Result.failure(ErrorCodes.UNKNOWN, "err")
        val mapped = result.map { it * 2 }
        assertTrue(mapped.isFailure)
    }

    @Test
    fun `test flatMap chains`() {
        val result = Result.success(10)
            .flatMap { Result.success(it + 5) }
            .flatMap { Result.success(it * 2) }
        assertEquals(30, result.getOrNull())
    }

    @Test
    fun `test flatMap short-circuits on failure`() {
        val result = Result.success(10)
            .flatMap<Int> { Result.failure(ErrorCodes.UNKNOWN, "err") }
            .flatMap { Result.success(it * 2) }
        assertTrue(result.isFailure)
    }
}
