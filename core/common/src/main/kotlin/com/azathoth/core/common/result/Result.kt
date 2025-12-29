package com.azathoth.core.common.result

/**
 * 操作结果封装
 */
sealed class Result<out T> {
    /** 成功结果 */
    data class Success<T>(val value: T) : Result<T>()
    
    /** 失败结果 */
    data class Failure(val error: AzathothError) : Result<Nothing>()
    
    /** 是否成功 */
    val isSuccess: Boolean get() = this is Success
    
    /** 是否失败 */
    val isFailure: Boolean get() = this is Failure
    
    /** 获取值，失败时返回null */
    fun getOrNull(): T? = when (this) {
        is Success -> value
        is Failure -> null
    }
    
    /** 获取错误，成功时返回null */
    fun errorOrNull(): AzathothError? = when (this) {
        is Success -> null
        is Failure -> error
    }
    
    /** 映射成功值 */
    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(value))
        is Failure -> this
    }
    
    /** 平面映射 */
    inline fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> transform(value)
        is Failure -> this
    }
    
    companion object {
        /** 创建成功结果 */
        fun <T> success(value: T): Result<T> = Success(value)
        
        /** 创建失败结果 */
        fun failure(error: AzathothError): Result<Nothing> = Failure(error)
        
        /** 创建失败结果 */
        fun failure(code: String, message: String): Result<Nothing> = 
            Failure(AzathothError(code, message))
    }
}

/**
 * Azathoth 错误定义
 */
data class AzathothError(
    /** 错误码 */
    val code: String,
    /** 错误消息 */
    val message: String,
    /** 错误详情 */
    val details: Map<String, Any> = emptyMap(),
    /** 原始异常 */
    val cause: Throwable? = null
)

/**
 * 错误码定义
 */
object ErrorCodes {
    // 通用错误 1xxx
    const val UNKNOWN = "1000"
    const val INVALID_ARGUMENT = "1001"
    const val NOT_FOUND = "1002"
    const val ALREADY_EXISTS = "1003"
    const val PERMISSION_DENIED = "1004"
    const val TIMEOUT = "1005"
    const val CANCELLED = "1006"
    
    // 认证错误 2xxx
    const val UNAUTHENTICATED = "2000"
    const val TOKEN_EXPIRED = "2001"
    const val TOKEN_INVALID = "2002"
    
    // 玩家错误 3xxx
    const val PLAYER_NOT_FOUND = "3000"
    const val PLAYER_OFFLINE = "3001"
    const val PLAYER_BANNED = "3002"
    
    // 服务错误 4xxx
    const val SERVICE_UNAVAILABLE = "4000"
    const val SERVICE_TIMEOUT = "4001"
    const val SERVICE_OVERLOADED = "4002"
    
    // 游戏错误 5xxx
    const val INSTANCE_NOT_FOUND = "5000"
    const val INSTANCE_FULL = "5001"
    const val TRANSFER_FAILED = "5002"
}
