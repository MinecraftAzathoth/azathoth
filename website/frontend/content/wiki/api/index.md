---
title: API 文档
description: Azathoth 官网后端 RESTful API 接口文档
navigation:
  title: 概述
  order: 1
---

# Azathoth Website API 文档

本文档描述了 Azathoth 官网后端提供的 RESTful API 接口。

## API 概述

基础URL: `https://api.azathoth.dev/v1`

所有 API 响应均为 JSON 格式，使用 UTF-8 编码。

## 认证

大多数 API 端点需要认证。请在请求头中包含 Bearer Token：

```
Authorization: Bearer <access_token>
```

## 通用响应格式

### 成功响应

```json
{
  "success": true,
  "data": { ... }
}
```

### 错误响应

```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "错误描述"
  }
}
```

## API 模块

| 模块 | 描述 |
|------|------|
| [认证模块](/wiki/api/auth) | 用户注册、登录、令牌管理 |
| [市场模块](/wiki/api/market) | 资源搜索、发布、下载 |
| [支付模块](/wiki/api/payment) | 订单、钱包、提现 |
| [论坛模块](/wiki/api/forum) | 帖子、回复、互动 |
| [生成器模块](/wiki/api/generator) | 项目脚手架生成 |
| [审核模块](/wiki/api/review) | 内容审核、举报处理 |

## HTTP 状态码

| 状态码 | 含义 |
|--------|------|
| 200 | 请求成功 |
| 201 | 创建成功 |
| 400 | 请求参数错误 |
| 401 | 未认证或令牌过期 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 409 | 资源冲突 |
| 422 | 验证失败 |
| 429 | 请求过于频繁 |
| 500 | 服务器内部错误 |

## 分页

支持分页的接口使用以下查询参数：

| 参数 | 类型 | 默认值 | 描述 |
|------|------|--------|------|
| page | int | 1 | 页码，从 1 开始 |
| pageSize | int | 20 | 每页数量，最大 100 |

分页响应格式：

```json
{
  "data": [...],
  "pagination": {
    "page": 1,
    "pageSize": 20,
    "totalCount": 100,
    "totalPages": 5
  }
}
```

## 速率限制

API 有请求频率限制：

- 认证接口：每分钟 10 次
- 普通接口：每分钟 60 次
- 搜索接口：每分钟 30 次

超出限制时返回 429 状态码，响应头包含：

```
X-RateLimit-Limit: 60
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1609459200
```
