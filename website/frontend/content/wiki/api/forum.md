---
title: 论坛模块 API
description: 帖子、回复、互动相关接口
navigation:
  title: 论坛模块
  order: 6
---

# 论坛模块 API

## 数据模型

### PostStatus (帖子状态)

| 值          | 描述  |
|------------|-----|
| `NORMAL`   | 正常  |
| `PINNED`   | 置顶  |
| `FEATURED` | 精华  |
| `LOCKED`   | 锁定  |
| `HIDDEN`   | 隐藏  |
| `DELETED`  | 已删除 |

### PostSortBy (帖子排序)

| 值 | 描述 |
|----|------|
| `LATEST` | 最新发布 |
| `HOT` | 热门 |
| `FEATURED` | 精华 |
| `REPLIES` | 最多回复 |

### NotificationType (通知类型)

| 值 | 描述 |
|----|------|
| `SYSTEM` | 系统通知 |
| `REPLY` | 回复通知 |
| `LIKE` | 点赞通知 |
| `FOLLOW` | 关注通知 |
| `MENTION` | @提及 |
| `PURCHASE` | 购买通知 |
| `EARNING` | 收益通知 |

### ForumCategory (板块)

| 字段 | 类型 | 描述 |
|------|------|------|
| categoryId | string | 板块ID |
| name | string | 板块名称 |
| description | string | 板块描述 |
| icon | string | 图标 |
| order | int | 排序 |
| postCount | long | 帖子数量 |
| lastPostAt | datetime? | 最后发帖时间 |

### ForumPost (帖子)

| 字段 | 类型 | 描述 |
|------|------|------|
| postId | string | 帖子ID |
| title | string | 标题 |
| content | string | 内容(Markdown) |
| authorId | string | 作者ID |
| authorName | string | 作者名称 |
| authorAvatar | string? | 作者头像 |
| categoryId | string | 板块ID |
| tags | string[] | 标签 |
| status | PostStatus | 状态 |
| isPinned | boolean | 是否置顶 |
| isFeatured | boolean | 是否精华 |
| isLocked | boolean | 是否锁定 |
| viewCount | long | 浏览量 |
| likeCount | int | 点赞数 |
| replyCount | int | 回复数 |
| createdAt | datetime | 创建时间 |
| updatedAt | datetime | 更新时间 |
| lastReplyAt | datetime? | 最后回复时间 |

### ForumReply (回复)

| 字段 | 类型 | 描述 |
|------|------|------|
| replyId | string | 回复ID |
| postId | string | 帖子ID |
| parentId | string? | 父回复ID(楼中楼) |
| content | string | 内容 |
| authorId | string | 作者ID |
| authorName | string | 作者名称 |
| authorAvatar | string? | 作者头像 |
| likeCount | int | 点赞数 |
| floor | int | 楼层 |
| createdAt | datetime | 创建时间 |
| updatedAt | datetime? | 更新时间 |

### Notification (通知)

| 字段 | 类型 | 描述 |
|------|------|------|
| notificationId | string | 通知ID |
| userId | string | 用户ID |
| type | NotificationType | 通知类型 |
| title | string | 标题 |
| content | string | 内容 |
| link | string? | 跳转链接 |
| read | boolean | 是否已读 |
| createdAt | datetime | 创建时间 |

### UserForumStats (用户论坛统计)

| 字段 | 类型 | 描述 |
|------|------|------|
| userId | string | 用户ID |
| postCount | int | 发帖数 |
| replyCount | int | 回复数 |
| likeReceived | int | 获赞数 |
| level | int | 等级 |
| experience | long | 经验值 |
| badges | Badge[] | 徽章列表 |

### Badge (徽章)

| 字段 | 类型 | 描述 |
|------|------|------|
| badgeId | string | 徽章ID |
| name | string | 名称 |
| description | string | 描述 |
| icon | string | 图标 |
| earnedAt | datetime | 获得时间 |

---

## 获取板块列表

```
GET /forum/categories
```

### 响应

```json
{
  "success": true,
  "categories": [
    {
      "categoryId": "cat_1",
      "name": "公告",
      "description": "官方公告和更新日志",
      "icon": "megaphone",
      "order": 1,
      "postCount": 50,
      "lastPostAt": "2024-01-01T00:00:00Z"
    }
  ]
}
```

---

## 获取帖子列表

```
GET /forum/posts
```

### 查询参数

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| categoryId | string | 否 | 板块ID筛选 |
| sortBy | PostSortBy | 否 | 排序方式，默认 LATEST |
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

### 响应

```json
{
  "success": true,
  "posts": [
    {
      "postId": "post_123",
      "title": "Azathoth 2.0 发布公告",
      "authorName": "admin",
      "authorAvatar": "https://...",
      "categoryId": "cat_1",
      "tags": ["公告", "更新"],
      "isPinned": true,
      "isFeatured": true,
      "viewCount": 10000,
      "likeCount": 500,
      "replyCount": 100,
      "createdAt": "2024-01-01T00:00:00Z",
      "lastReplyAt": "2024-01-02T00:00:00Z"
    }
  ],
  "pagination": { ... }
}
```

---

## 搜索帖子

```
GET /forum/posts/search
```

### 查询参数

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| keyword | string | 是 | 搜索关键词 |
| categoryId | string | 否 | 板块ID筛选 |
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

### 响应

```json
{
  "success": true,
  "posts": [ ... ],
  "pagination": { ... }
}
```

---

## 获取帖子详情

```
GET /forum/posts/{postId}
```

### 响应

```json
{
  "success": true,
  "post": {
    "postId": "post_123",
    "title": "Azathoth 2.0 发布公告",
    "content": "# 更新内容\n\n...",
    "authorId": "user_admin",
    "authorName": "admin",
    "authorAvatar": "https://...",
    "categoryId": "cat_1",
    "tags": ["公告", "更新"],
    "status": "NORMAL",
    "isPinned": true,
    "isFeatured": true,
    "isLocked": false,
    "viewCount": 10001,
    "likeCount": 500,
    "replyCount": 100,
    "createdAt": "2024-01-01T00:00:00Z",
    "updatedAt": "2024-01-01T12:00:00Z",
    "lastReplyAt": "2024-01-02T00:00:00Z"
  }
}
```

---

## 创建帖子

```
POST /forum/posts
```

需要认证。

### 请求体

| 字段 | 类型 | 必填 | 描述 |
|------|------|------|------|
| title | string | 是 | 标题，最长100字符 |
| content | string | 是 | 内容(Markdown) |
| categoryId | string | 是 | 板块ID |
| tags | string[] | 否 | 标签列表，最多5个 |

### 响应

```json
{
  "success": true,
  "post": { ... }
}
```

---

## 更新帖子

```
PATCH /forum/posts/{postId}
```

需要认证，只能更新自己的帖子。

### 请求体

| 字段 | 类型 | 必填 | 描述 |
|------|------|------|------|
| title | string | 否 | 标题 |
| content | string | 否 | 内容 |
| tags | string[] | 否 | 标签 |

### 响应

```json
{
  "success": true,
  "post": { ... }
}
```

---

## 删除帖子

```
DELETE /forum/posts/{postId}
```

需要认证，只能删除自己的帖子。

### 响应

```json
{
  "success": true
}
```

---

## 获取帖子回复

```
GET /forum/posts/{postId}/replies
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
  "replies": [
    {
      "replyId": "reply_123",
      "postId": "post_123",
      "content": "很棒的更新！",
      "authorId": "user_456",
      "authorName": "developer",
      "authorAvatar": "https://...",
      "likeCount": 10,
      "floor": 1,
      "createdAt": "2024-01-01T01:00:00Z"
    }
  ],
  "pagination": { ... }
}
```

---

## 创建回复

```
POST /forum/posts/{postId}/replies
```

需要认证。

### 请求体

| 字段 | 类型 | 必填 | 描述 |
|------|------|------|------|
| content | string | 是 | 回复内容 |
| parentId | string | 否 | 父回复ID(楼中楼) |

### 响应

```json
{
  "success": true,
  "reply": { ... }
}
```

---

## 点赞帖子

```
POST /forum/posts/{postId}/like
```

需要认证。

### 响应

```json
{
  "success": true
}
```

---

## 取消点赞帖子

```
DELETE /forum/posts/{postId}/like
```

需要认证。

### 响应

```json
{
  "success": true
}
```

---

## 点赞回复

```
POST /forum/replies/{replyId}/like
```

需要认证。

### 响应

```json
{
  "success": true
}
```

---

## 取消点赞回复

```
DELETE /forum/replies/{replyId}/like
```

需要认证。

### 响应

```json
{
  "success": true
}
```

---

## 收藏帖子

```
POST /forum/posts/{postId}/favorite
```

需要认证。

### 响应

```json
{
  "success": true
}
```

---

## 取消收藏帖子

```
DELETE /forum/posts/{postId}/favorite
```

需要认证。

### 响应

```json
{
  "success": true
}
```

---

## 获取用户收藏

```
GET /forum/favorites
```

需要认证。

### 查询参数

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

### 响应

```json
{
  "success": true,
  "posts": [ ... ],
  "pagination": { ... }
}
```

---

## 关注用户

```
POST /users/{userId}/follow
```

需要认证。

### 响应

```json
{
  "success": true
}
```

---

## 取消关注

```
DELETE /users/{userId}/follow
```

需要认证。

### 响应

```json
{
  "success": true
}
```

---

## 获取关注列表

```
GET /users/{userId}/following
```

### 响应

```json
{
  "success": true,
  "users": ["user_1", "user_2", "user_3"]
}
```

---

## 获取粉丝列表

```
GET /users/{userId}/followers
```

### 响应

```json
{
  "success": true,
  "users": ["user_4", "user_5"]
}
```

---

## 获取通知列表

```
GET /notifications
```

需要认证。

### 查询参数

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| type | NotificationType | 否 | 类型筛选 |
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

### 响应

```json
{
  "success": true,
  "notifications": [
    {
      "notificationId": "notif_123",
      "type": "REPLY",
      "title": "新回复",
      "content": "developer 回复了你的帖子",
      "link": "/forum/posts/post_123",
      "read": false,
      "createdAt": "2024-01-01T00:00:00Z"
    }
  ],
  "pagination": { ... }
}
```

---

## 获取未读通知数量

```
GET /notifications/unread-count
```

需要认证。

### 响应

```json
{
  "success": true,
  "count": 5
}
```

---

## 标记通知已读

```
POST /notifications/{notificationId}/read
```

需要认证。

### 响应

```json
{
  "success": true
}
```

---

## 标记全部已读

```
POST /notifications/read-all
```

需要认证。

### 响应

```json
{
  "success": true
}
```

---

## 管理员API

### 置顶帖子

```
POST /admin/posts/{postId}/pin
```

需要版主权限。

### 请求体

| 字段 | 类型 | 必填 | 描述 |
|------|------|------|------|
| pinned | boolean | 是 | 是否置顶 |

### 响应

```json
{
  "success": true
}
```

---

### 设为精华

```
POST /admin/posts/{postId}/feature
```

需要版主权限。

### 请求体

| 字段 | 类型 | 必填 | 描述 |
|------|------|------|------|
| featured | boolean | 是 | 是否精华 |

### 响应

```json
{
  "success": true
}
```

---

### 锁定帖子

```
POST /admin/posts/{postId}/lock
```

需要版主权限。

### 请求体

| 字段 | 类型 | 必填 | 描述 |
|------|------|------|------|
| locked | boolean | 是 | 是否锁定 |

### 响应

```json
{
  "success": true
}
```
