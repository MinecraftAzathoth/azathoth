---
title: 市场模块 API
description: 资源搜索、发布、下载相关接口
navigation:
  title: 市场模块
  order: 3
---

# 市场模块 API

## 数据模型

### ResourceType (资源类型)

| 值 | 描述 |
|----|------|
| `PLUGIN` | 游戏插件 |
| `MODULE` | 功能模块 |
| `SERVICE` | 独立服务 |
| `TEMPLATE` | 项目模板 |
| `THEME` | 后台主题 |
| `TOOL` | 开发工具 |

### LicenseType (授权类型)

| 值 | 描述 |
|----|------|
| `FREE_OPEN_SOURCE` | 免费开源 |
| `FREE_CLOSED_SOURCE` | 免费闭源 |
| `PAID_PERPETUAL` | 付费买断 |
| `PAID_SUBSCRIPTION` | 付费订阅 |

### ResourceStatus (审核状态)

| 值 | 描述 |
|----|------|
| `DRAFT` | 草稿 |
| `PENDING` | 待审核 |
| `APPROVED` | 已通过 |
| `REJECTED` | 已拒绝 |
| `SUSPENDED` | 已下架 |

### MarketResource (市场资源)

| 字段 | 类型 | 描述 |
|------|------|------|
| resourceId | string | 资源ID |
| name | string | 资源名称 |
| slug | string | URL友好标识 |
| description | string | 资源描述 |
| type | ResourceType | 资源类型 |
| license | LicenseType | 授权类型 |
| authorId | string | 作者ID |
| authorName | string | 作者名称 |
| versions | ResourceVersion[] | 版本列表 |
| latestVersion | string | 最新版本号 |
| pricing | ResourcePricing? | 定价信息 |
| downloads | long | 下载量 |
| rating | double | 平均评分 (0-5) |
| reviewCount | int | 评论数量 |
| status | ResourceStatus | 状态 |
| minApiVersion | string | 最低API版本 |
| maxApiVersion | string? | 最高API版本 |
| dependencies | string[] | 依赖列表 |
| icon | string | 图标URL |
| screenshots | string[] | 截图URL列表 |
| tags | string[] | 标签 |
| createdAt | datetime | 创建时间 |
| updatedAt | datetime | 更新时间 |

### ResourceVersion (资源版本)

| 字段 | 类型 | 描述 |
|------|------|------|
| version | string | 版本号 |
| changelog | string | 更新日志 |
| downloadUrl | string | 下载地址 |
| fileSize | long | 文件大小(字节) |
| minApiVersion | string | 最低API版本 |
| releasedAt | datetime | 发布时间 |

### ResourcePricing (资源定价)

| 字段 | 类型 | 描述 |
|------|------|------|
| price | long | 价格(分) |
| currency | string | 货币类型(CNY) |
| subscriptionPeriod | int? | 订阅周期(天)，null为买断 |

### ResourceReview (资源评论)

| 字段 | 类型 | 描述 |
|------|------|------|
| reviewId | string | 评论ID |
| resourceId | string | 资源ID |
| userId | string | 用户ID |
| userName | string | 用户名 |
| rating | int | 评分 (1-5) |
| content | string | 评论内容 |
| createdAt | datetime | 创建时间 |
| updatedAt | datetime? | 更新时间 |
| helpful | int | 有帮助数 |
| authorReply | string? | 作者回复 |

---

## 搜索资源

```
GET /market/resources
```

### 查询参数

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| keyword | string | 否 | 搜索关键词 |
| type | ResourceType | 否 | 资源类型筛选 |
| license | LicenseType | 否 | 授权类型筛选 |
| minRating | double | 否 | 最低评分 |
| tags | string | 否 | 标签筛选(逗号分隔) |
| sortBy | string | 否 | 排序方式: RELEVANCE, DOWNLOADS, RATING, UPDATED, CREATED |
| sortOrder | string | 否 | 排序顺序: ASC, DESC |
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

### 响应

```json
{
  "success": true,
  "resources": [
    {
      "resourceId": "res_123",
      "name": "技能系统扩展",
      "slug": "skill-system-extension",
      "description": "为Azathoth添加更多技能类型",
      "type": "PLUGIN",
      "license": "FREE_OPEN_SOURCE",
      "authorName": "developer",
      "latestVersion": "1.2.0",
      "downloads": 1234,
      "rating": 4.5,
      "tags": ["skill", "rpg"]
    }
  ],
  "pagination": {
    "page": 1,
    "pageSize": 20,
    "totalCount": 100,
    "totalPages": 5
  }
}
```

---

## 获取资源详情

```
GET /market/resources/{resourceId}
```

或

```
GET /market/resources/slug/{slug}
```

### 响应

```json
{
  "success": true,
  "resource": {
    "resourceId": "res_123",
    "name": "技能系统扩展",
    "slug": "skill-system-extension",
    "description": "完整的技能系统扩展插件...",
    "type": "PLUGIN",
    "license": "FREE_OPEN_SOURCE",
    "authorId": "user_456",
    "authorName": "developer",
    "versions": [
      {
        "version": "1.2.0",
        "changelog": "新增被动技能支持",
        "fileSize": 102400,
        "minApiVersion": "2.0.0",
        "releasedAt": "2024-01-01T00:00:00Z"
      }
    ],
    "latestVersion": "1.2.0",
    "downloads": 1234,
    "rating": 4.5,
    "reviewCount": 23,
    "status": "APPROVED",
    "minApiVersion": "2.0.0",
    "dependencies": ["core-plugin"],
    "icon": "https://...",
    "screenshots": ["https://..."],
    "tags": ["skill", "rpg"],
    "createdAt": "2023-06-01T00:00:00Z",
    "updatedAt": "2024-01-01T00:00:00Z"
  }
}
```

---

## 创建资源

```
POST /market/resources
```

需要认证。

### 请求体

| 字段 | 类型 | 必填 | 描述 |
|------|------|------|------|
| name | string | 是 | 资源名称 |
| description | string | 是 | 资源描述 |
| type | ResourceType | 是 | 资源类型 |
| license | LicenseType | 是 | 授权类型 |
| pricing | ResourcePricing | 否 | 定价信息(付费资源必填) |
| minApiVersion | string | 是 | 最低API版本 |
| dependencies | string[] | 否 | 依赖资源ID列表 |
| tags | string[] | 否 | 标签列表 |

### 响应

```json
{
  "success": true,
  "resource": { ... }
}
```

---

## 更新资源

```
PATCH /market/resources/{resourceId}
```

需要认证，只能更新自己的资源。

### 请求体

| 字段 | 类型 | 必填 | 描述 |
|------|------|------|------|
| name | string | 否 | 资源名称 |
| description | string | 否 | 资源描述 |
| pricing | ResourcePricing | 否 | 定价信息 |
| icon | string | 否 | 图标URL |
| screenshots | string[] | 否 | 截图URL列表 |
| tags | string[] | 否 | 标签列表 |

### 响应

```json
{
  "success": true,
  "resource": { ... }
}
```

---

## 发布新版本

```
POST /market/resources/{resourceId}/versions
```

需要认证。

### 请求体 (multipart/form-data)

| 字段 | 类型 | 必填 | 描述 |
|------|------|------|------|
| version | string | 是 | 版本号(语义化) |
| changelog | string | 是 | 更新日志 |
| file | file | 是 | 资源文件 |
| minApiVersion | string | 是 | 最低API版本 |

### 响应

```json
{
  "success": true,
  "version": {
    "version": "1.3.0",
    "changelog": "修复若干bug",
    "fileSize": 102400,
    "minApiVersion": "2.0.0",
    "releasedAt": "2024-01-02T00:00:00Z"
  }
}
```

---

## 删除资源

```
DELETE /market/resources/{resourceId}
```

需要认证，只能删除自己的资源。

### 响应

```json
{
  "success": true
}
```

---

## 获取用户资源列表

```
GET /users/{userId}/resources
```

### 响应

```json
{
  "success": true,
  "resources": [ ... ]
}
```

---

## 获取热门资源

```
GET /market/popular
```

### 查询参数

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| type | ResourceType | 否 | 资源类型筛选 |
| limit | int | 否 | 返回数量，默认10 |

### 响应

```json
{
  "success": true,
  "resources": [ ... ]
}
```

---

## 获取最新资源

```
GET /market/latest
```

### 查询参数

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| type | ResourceType | 否 | 资源类型筛选 |
| limit | int | 否 | 返回数量，默认10 |

### 响应

```json
{
  "success": true,
  "resources": [ ... ]
}
```

---

## 获取资源评论

```
GET /market/resources/{resourceId}/reviews
```

### 查询参数

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

### 响应

```json
{
  "success": true,
  "reviews": [
    {
      "reviewId": "rev_123",
      "userId": "user_789",
      "userName": "reviewer",
      "rating": 5,
      "content": "非常好用的插件！",
      "createdAt": "2024-01-01T00:00:00Z",
      "helpful": 10,
      "authorReply": "感谢支持！"
    }
  ],
  "pagination": { ... }
}
```

---

## 创建评论

```
POST /market/resources/{resourceId}/reviews
```

需要认证。

### 请求体

| 字段 | 类型 | 必填 | 描述 |
|------|------|------|------|
| rating | int | 是 | 评分 (1-5) |
| content | string | 是 | 评论内容 |

### 响应

```json
{
  "success": true,
  "review": { ... }
}
```

---

## 回复评论

```
POST /market/reviews/{reviewId}/reply
```

需要认证，只有资源作者可以回复。

### 请求体

| 字段 | 类型 | 必填 | 描述 |
|------|------|------|------|
| reply | string | 是 | 回复内容 |

### 响应

```json
{
  "success": true,
  "review": { ... }
}
```

---

## 标记评论有帮助

```
POST /market/reviews/{reviewId}/helpful
```

需要认证。

### 响应

```json
{
  "success": true
}
```

---

## 获取下载链接

```
GET /market/resources/{resourceId}/download
```

可选认证（付费资源需要认证）。

### 查询参数

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| version | string | 否 | 版本号，默认最新版本 |

### 响应

```json
{
  "success": true,
  "downloadUrl": "https://cdn.azathoth.dev/...",
  "expiresAt": "2024-01-01T01:00:00Z"
}
```

---

## 获取下载统计

```
GET /market/resources/{resourceId}/stats
```

### 响应

```json
{
  "success": true,
  "stats": {
    "totalDownloads": 10000,
    "lastMonthDownloads": 500,
    "lastWeekDownloads": 100,
    "versionDownloads": {
      "1.2.0": 300,
      "1.1.0": 700
    }
  }
}
```
