---
title: 'gRPC 服务'
description: '使用 gRPC 进行跨服务通信'
navigation:
  order: 2
---

# gRPC 服务

Azathoth 使用 gRPC 实现高性能的跨服务通信，支持服务器集群和微服务架构。

## 概述

gRPC 是一个高性能的 RPC 框架，具有以下优势：

- **高性能**：基于 HTTP/2，支持多路复用
- **类型安全**：使用 Protocol Buffers 定义接口
- **跨语言**：支持多种编程语言
- **双向流**：支持服务端流、客户端流和双向流

## 定义服务

使用 Protocol Buffers 定义服务接口：

```protobuf
// activity_service.proto
syntax = "proto3";

package com.azathoth.grpc.activity;

option java_package = "com.azathoth.grpc.activity";
option java_multiple_files = true;

service ActivityService {
    // 获取活动列表
    rpc GetActivities(GetActivitiesRequest) returns (GetActivitiesResponse);

    // 参与活动
    rpc JoinActivity(JoinActivityRequest) returns (JoinActivityResponse);

    // 活动状态流
    rpc StreamActivityStatus(ActivityStatusRequest) returns (stream ActivityStatus);
}

message GetActivitiesRequest {
    string player_id = 1;
    ActivityType type = 2;
    int32 page = 3;
    int32 page_size = 4;
}

message GetActivitiesResponse {
    repeated Activity activities = 1;
    int32 total_count = 2;
}

message Activity {
    string activity_id = 1;
    string name = 2;
    string description = 3;
    ActivityType type = 4;
    int64 start_time = 5;
    int64 end_time = 6;
    ActivityStatus status = 7;
}

enum ActivityType {
    ACTIVITY_TYPE_UNKNOWN = 0;
    ACTIVITY_TYPE_DAILY = 1;
    ACTIVITY_TYPE_WEEKLY = 2;
    ACTIVITY_TYPE_SPECIAL = 3;
}

enum ActivityStatus {
    ACTIVITY_STATUS_UNKNOWN = 0;
    ACTIVITY_STATUS_NOT_STARTED = 1;
    ACTIVITY_STATUS_IN_PROGRESS = 2;
    ACTIVITY_STATUS_ENDED = 3;
}
```

## 实现服务

### 服务端实现

```kotlin
@GrpcService
class ActivityServiceImpl : ActivityServiceGrpcKt.ActivityServiceCoroutineImplBase() {

    @Inject
    lateinit var activityRepository: ActivityRepository

    override suspend fun getActivities(
        request: GetActivitiesRequest
    ): GetActivitiesResponse {
        val activities = activityRepository.findByType(
            type = request.type,
            page = request.page,
            pageSize = request.pageSize
        )

        return GetActivitiesResponse.newBuilder()
            .addAllActivities(activities.map { it.toProto() })
            .setTotalCount(activityRepository.countByType(request.type))
            .build()
    }

    override suspend fun joinActivity(
        request: JoinActivityRequest
    ): JoinActivityResponse {
        val result = activityRepository.join(
            playerId = request.playerId,
            activityId = request.activityId
        )

        return JoinActivityResponse.newBuilder()
            .setSuccess(result.success)
            .setMessage(result.message)
            .build()
    }

    override fun streamActivityStatus(
        request: ActivityStatusRequest
    ): Flow<ActivityStatus> = flow {
        while (true) {
            val status = activityRepository.getStatus(request.activityId)
            emit(status.toProto())
            delay(1000)
        }
    }
}
```

### 客户端调用

```kotlin
@Service
class ActivityClient(
    private val channel: ManagedChannel
) {
    private val stub = ActivityServiceGrpcKt.ActivityServiceCoroutineStub(channel)

    suspend fun getActivities(
        playerId: String,
        type: ActivityType
    ): List<Activity> {
        val request = GetActivitiesRequest.newBuilder()
            .setPlayerId(playerId)
            .setType(type)
            .build()

        val response = stub.getActivities(request)
        return response.activitiesList
    }

    suspend fun joinActivity(
        playerId: String,
        activityId: String
    ): Boolean {
        val request = JoinActivityRequest.newBuilder()
            .setPlayerId(playerId)
            .setActivityId(activityId)
            .build()

        val response = stub.joinActivity(request)
        return response.success
    }

    fun streamStatus(activityId: String): Flow<ActivityStatus> {
        val request = ActivityStatusRequest.newBuilder()
            .setActivityId(activityId)
            .build()

        return stub.streamActivityStatus(request)
    }
}
```

## 配置

### 服务端配置

```yaml
# application.yml
grpc:
  server:
    port: 9090
    max-inbound-message-size: 4MB
    keep-alive-time: 30s
    keep-alive-timeout: 5s

  security:
    enabled: true
    cert-chain: /path/to/cert.pem
    private-key: /path/to/key.pem
```

### 客户端配置

```yaml
# application.yml
grpc:
  client:
    activity-service:
      address: static://localhost:9090
      negotiation-type: TLS
      enable-keep-alive: true
      keep-alive-time: 30s
```

## 拦截器

### 日志拦截器

```kotlin
class LoggingInterceptor : ServerInterceptor {

    private val logger = KotlinLogging.logger {}

    override fun <ReqT, RespT> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        val methodName = call.methodDescriptor.fullMethodName
        val startTime = System.currentTimeMillis()

        logger.info { "gRPC call started: $methodName" }

        return object : ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(
            next.startCall(call, headers)
        ) {
            override fun onComplete() {
                val duration = System.currentTimeMillis() - startTime
                logger.info { "gRPC call completed: $methodName (${duration}ms)" }
                super.onComplete()
            }
        }
    }
}
```

### 认证拦截器

```kotlin
class AuthInterceptor(
    private val tokenValidator: TokenValidator
) : ServerInterceptor {

    override fun <ReqT, RespT> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        val token = headers.get(AUTH_TOKEN_KEY)

        if (token == null) {
            call.close(Status.UNAUTHENTICATED.withDescription("Missing token"), Metadata())
            return object : ServerCall.Listener<ReqT>() {}
        }

        val principal = tokenValidator.validate(token)
            ?: run {
                call.close(Status.UNAUTHENTICATED.withDescription("Invalid token"), Metadata())
                return object : ServerCall.Listener<ReqT>() {}
            }

        val context = Context.current().withValue(PRINCIPAL_KEY, principal)
        return Contexts.interceptCall(context, call, headers, next)
    }

    companion object {
        val AUTH_TOKEN_KEY: Metadata.Key<String> =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER)
        val PRINCIPAL_KEY: Context.Key<Principal> = Context.key("principal")
    }
}
```

## 错误处理

```kotlin
class GrpcExceptionHandler : ServerInterceptor {

    override fun <ReqT, RespT> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        return object : ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(
            next.startCall(call, headers)
        ) {
            override fun onHalfClose() {
                try {
                    super.onHalfClose()
                } catch (e: Exception) {
                    val status = when (e) {
                        is NotFoundException -> Status.NOT_FOUND
                        is ValidationException -> Status.INVALID_ARGUMENT
                        is PermissionDeniedException -> Status.PERMISSION_DENIED
                        else -> Status.INTERNAL
                    }.withDescription(e.message)

                    call.close(status, Metadata())
                }
            }
        }
    }
}
```

## 下一步

- [WebSocket](/docs/network/websocket) - 实时通信方案
- [数据包处理](/docs/network/packet) - 自定义协议包
- [协议设计](/docs/network/protocol) - 网络协议设计原则
