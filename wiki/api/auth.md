# 认证模块 API

## 数据模型

### UserRole (用户角色)

| 值             | 描述    |
|---------------|-------|
| `USER`        | 普通用户  |
| `DEVELOPER`   | 开发者   |
| `MODERATOR`   | 版主    |
| `ADMIN`       | 管理员   |
| `SUPER_ADMIN` | 超级管理员 |

### UserInfo (用户信息)

| 字段          | 类型        | 描述      |
|-------------|-----------|---------|
| userId      | string    | 用户唯一标识  |
| username    | string    | 用户名     |
| email       | string    | 邮箱地址    |
| avatarUrl   | string?   | 头像URL   |
| role        | UserRole  | 用户角色    |
| verified    | boolean   | 邮箱是否已验证 |
| createdAt   | datetime  | 注册时间    |
| lastLoginAt | datetime? | 最后登录时间  |

### AuthToken (认证令牌)

| 字段           | 类型       | 描述                |
|--------------|----------|-------------------|
| accessToken  | string   | 访问令牌              |
| refreshToken | string   | 刷新令牌              |
| expiresAt    | datetime | 过期时间              |
| tokenType    | string   | 令牌类型，固定为 "Bearer" |

---

## 用户登录

```
POST /auth/login
```

### 请求体

| 字段       | 类型      | 必填 | 描述              |
|----------|---------|----|-----------------|
| username | string  | 是  | 用户名或邮箱          |
| password | string  | 是  | 密码              |
| remember | boolean | 否  | 记住登录状态，默认 false |

### 响应

```json
{
  "success": true,
  "user": {
    "userId": "user_123",
    "username": "developer",
    "email": "dev@example.com",
    "role": "DEVELOPER",
    "verified": true
  },
  "token": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "refresh_token_here",
    "expiresAt": "2024-01-01T12:00:00Z",
    "tokenType": "Bearer"
  }
}
```

### 错误码

| 错误码                 | 描述       |
|---------------------|----------|
| INVALID_CREDENTIALS | 用户名或密码错误 |
| USER_BANNED         | 用户已被封禁   |
| EMAIL_NOT_VERIFIED  | 邮箱未验证    |

---

## 用户注册

```
POST /auth/register
```

### 请求体

| 字段         | 类型     | 必填 | 描述         |
|------------|--------|----|------------|
| username   | string | 是  | 用户名，3-20字符 |
| email      | string | 是  | 邮箱地址       |
| password   | string | 是  | 密码，至少8位    |
| inviteCode | string | 否  | 邀请码        |

### 响应

```json
{
  "success": true,
  "user": {
    "userId": "user_456",
    "username": "newuser",
    "email": "new@example.com",
    "role": "USER",
    "verified": false
  },
  "token": {
    "accessToken": "...",
    "refreshToken": "...",
    "expiresAt": "2024-01-01T12:00:00Z",
    "tokenType": "Bearer"
  }
}
```

### 错误码

| 错误码                 | 描述     |
|---------------------|--------|
| USERNAME_EXISTS     | 用户名已存在 |
| EMAIL_EXISTS        | 邮箱已被注册 |
| INVALID_INVITE_CODE | 邀请码无效  |
| PASSWORD_TOO_WEAK   | 密码强度不足 |

---

## 刷新令牌

```
POST /auth/refresh
```

### 请求体

| 字段           | 类型     | 必填 | 描述   |
|--------------|--------|----|------|
| refreshToken | string | 是  | 刷新令牌 |

### 响应

```json
{
  "success": true,
  "token": {
    "accessToken": "new_access_token",
    "refreshToken": "new_refresh_token",
    "expiresAt": "2024-01-02T12:00:00Z",
    "tokenType": "Bearer"
  }
}
```

---

## 登出

```
POST /auth/logout
```

需要认证。

### 响应

```json
{
  "success": true
}
```

---

## 验证令牌

```
GET /auth/validate
```

需要认证。

### 响应

```json
{
  "success": true,
  "user": {
    "userId": "user_123",
    "username": "developer",
    "email": "dev@example.com",
    "role": "DEVELOPER",
    "verified": true
  }
}
```

---

## 发送验证邮件

```
POST /auth/send-verification
```

需要认证。

### 响应

```json
{
  "success": true
}
```

---

## 验证邮箱

```
POST /auth/verify-email
```

### 请求体

| 字段    | 类型     | 必填 | 描述     |
|-------|--------|----|--------|
| token | string | 是  | 邮箱验证令牌 |

### 响应

```json
{
  "success": true
}
```

---

## 请求重置密码

```
POST /auth/forgot-password
```

### 请求体

| 字段    | 类型     | 必填 | 描述   |
|-------|--------|----|------|
| email | string | 是  | 邮箱地址 |

### 响应

```json
{
  "success": true
}
```

---

## 重置密码

```
POST /auth/reset-password
```

### 请求体

| 字段          | 类型     | 必填 | 描述     |
|-------------|--------|----|--------|
| token       | string | 是  | 重置密码令牌 |
| newPassword | string | 是  | 新密码    |

### 响应

```json
{
  "success": true
}
```

---

## 获取用户信息

```
GET /users/{userId}
```

### 响应

```json
{
  "success": true,
  "user": {
    "userId": "user_123",
    "username": "developer",
    "avatarUrl": "https://...",
    "role": "DEVELOPER",
    "createdAt": "2023-01-01T00:00:00Z"
  }
}
```

---

## 更新用户资料

```
PATCH /users/{userId}/profile
```

需要认证，只能更新自己的资料。

### 请求体

| 字段          | 类型     | 必填 | 描述        |
|-------------|--------|----|-----------|
| displayName | string | 否  | 显示名称      |
| bio         | string | 否  | 个人简介      |
| avatarUrl   | string | 否  | 头像URL     |
| website     | string | 否  | 个人网站      |
| github      | string | 否  | GitHub用户名 |

### 响应

```json
{
  "success": true,
  "user": { ... }
}
```

---

## 封禁用户 (管理员)

```
POST /admin/users/{userId}/ban
```

需要管理员权限。

### 请求体

| 字段       | 类型     | 必填 | 描述              |
|----------|--------|----|-----------------|
| reason   | string | 是  | 封禁原因            |
| duration | long   | 否  | 封禁时长(秒)，null为永久 |

### 响应

```json
{
  "success": true
}
```

---

## 解封用户 (管理员)

```
POST /admin/users/{userId}/unban
```

需要管理员权限。

### 响应

```json
{
  "success": true
}
```
