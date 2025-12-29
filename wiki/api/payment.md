# 支付模块 API

## 数据模型

### PaymentChannel (支付渠道)

| 值 | 描述 |
|----|------|
| `WECHAT` | 微信支付 |
| `ALIPAY` | 支付宝 |

### OrderStatus (订单状态)

| 值 | 描述 |
|----|------|
| `PENDING` | 待支付 |
| `PAID` | 已支付 |
| `CANCELLED` | 已取消 |
| `REFUNDED` | 已退款 |
| `EXPIRED` | 已过期 |

### WithdrawStatus (提现状态)

| 值 | 描述 |
|----|------|
| `PENDING` | 待审核 |
| `PROCESSING` | 处理中 |
| `COMPLETED` | 已完成 |
| `REJECTED` | 已拒绝 |

### WithdrawChannel (提现渠道)

| 值 | 描述 |
|----|------|
| `ALIPAY` | 支付宝 |
| `BANK_CARD` | 银行卡 |

### TransactionType (交易类型)

| 值 | 描述 |
|----|------|
| `RECHARGE` | 充值 |
| `PURCHASE` | 购买 |
| `EARNINGS` | 收益 |
| `WITHDRAW` | 提现 |
| `REFUND` | 退款 |
| `ADJUSTMENT` | 调整 |

### Order (订单)

| 字段 | 类型 | 描述 |
|------|------|------|
| orderId | string | 订单ID |
| userId | string | 用户ID |
| resourceId | string | 资源ID |
| resourceName | string | 资源名称 |
| amount | long | 金额(分) |
| currency | string | 货币类型 |
| channel | PaymentChannel? | 支付渠道 |
| status | OrderStatus | 订单状态 |
| paymentUrl | string? | 支付跳转URL |
| paidAt | datetime? | 支付时间 |
| createdAt | datetime | 创建时间 |
| expiresAt | datetime | 过期时间 |

### Balance (余额)

| 字段 | 类型 | 描述 |
|------|------|------|
| userId | string | 用户ID |
| available | long | 可用余额(分) |
| frozen | long | 冻结余额(分) |
| total | long | 总余额(分) |
| currency | string | 货币类型 |

### WithdrawRequest (提现申请)

| 字段 | 类型 | 描述 |
|------|------|------|
| withdrawId | string | 提现ID |
| userId | string | 用户ID |
| amount | long | 提现金额(分) |
| fee | long | 手续费(分) |
| netAmount | long | 实际到账(分) |
| channel | WithdrawChannel | 提现渠道 |
| accountInfo | string | 收款账户信息 |
| status | WithdrawStatus | 状态 |
| reason | string? | 拒绝原因 |
| createdAt | datetime | 申请时间 |
| processedAt | datetime? | 处理时间 |

### EarningsRecord (收益记录)

| 字段 | 类型 | 描述 |
|------|------|------|
| recordId | string | 记录ID |
| userId | string | 用户ID |
| orderId | string | 关联订单ID |
| resourceId | string | 资源ID |
| resourceName | string | 资源名称 |
| grossAmount | long | 总金额(分) |
| platformFee | long | 平台抽成(分) |
| netAmount | long | 净收入(分) |
| createdAt | datetime | 创建时间 |

### TransactionRecord (交易记录)

| 字段 | 类型 | 描述 |
|------|------|------|
| transactionId | string | 交易ID |
| userId | string | 用户ID |
| type | TransactionType | 交易类型 |
| amount | long | 金额(分) |
| balance | long | 交易后余额(分) |
| description | string | 描述 |
| referenceId | string? | 关联ID |
| createdAt | datetime | 创建时间 |

### EarningsStats (收益统计)

| 字段 | 类型 | 描述 |
|------|------|------|
| totalEarnings | long | 总收益(分) |
| thisMonthEarnings | long | 本月收益(分) |
| lastMonthEarnings | long | 上月收益(分) |
| totalWithdrawn | long | 已提现(分) |
| pendingWithdraw | long | 待提现(分) |

---

## 创建订单

```
POST /payment/orders
```

需要认证。

### 请求体

| 字段 | 类型 | 必填 | 描述 |
|------|------|------|------|
| resourceId | string | 是 | 资源ID |
| channel | PaymentChannel | 是 | 支付渠道 |

### 响应

```json
{
  "success": true,
  "order": {
    "orderId": "order_123",
    "userId": "user_456",
    "resourceId": "res_789",
    "resourceName": "高级技能系统",
    "amount": 9900,
    "currency": "CNY",
    "channel": "ALIPAY",
    "status": "PENDING",
    "paymentUrl": "https://...",
    "createdAt": "2024-01-01T00:00:00Z",
    "expiresAt": "2024-01-01T00:30:00Z"
  }
}
```

---

## 获取订单详情

```
GET /payment/orders/{orderId}
```

需要认证。

### 响应

```json
{
  "success": true,
  "order": { ... }
}
```

---

## 取消订单

```
POST /payment/orders/{orderId}/cancel
```

需要认证，只能取消待支付的订单。

### 响应

```json
{
  "success": true
}
```

---

## 查询订单状态

```
GET /payment/orders/{orderId}/status
```

需要认证。

### 响应

```json
{
  "success": true,
  "status": "PAID"
}
```

---

## 获取用户订单列表

```
GET /payment/orders
```

需要认证。

### 查询参数

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| status | OrderStatus | 否 | 状态筛选 |
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

### 响应

```json
{
  "success": true,
  "orders": [ ... ],
  "pagination": { ... }
}
```

---

## 申请退款

```
POST /payment/orders/{orderId}/refund
```

需要认证。

### 请求体

| 字段 | 类型 | 必填 | 描述 |
|------|------|------|------|
| reason | string | 是 | 退款原因 |

### 响应

```json
{
  "success": true
}
```

---

## 获取钱包余额

```
GET /wallet/balance
```

需要认证。

### 响应

```json
{
  "success": true,
  "balance": {
    "userId": "user_123",
    "available": 100000,
    "frozen": 5000,
    "total": 105000,
    "currency": "CNY"
  }
}
```

---

## 申请提现

```
POST /wallet/withdraw
```

需要认证。

### 请求体

| 字段 | 类型 | 必填 | 描述 |
|------|------|------|------|
| amount | long | 是 | 提现金额(分)，最低10000(100元) |
| channel | WithdrawChannel | 是 | 提现渠道 |
| accountInfo | string | 是 | 收款账户信息 |

### 响应

```json
{
  "success": true,
  "withdraw": {
    "withdrawId": "wd_123",
    "amount": 100000,
    "fee": 0,
    "netAmount": 100000,
    "channel": "ALIPAY",
    "status": "PENDING",
    "createdAt": "2024-01-01T00:00:00Z"
  }
}
```

### 错误码

| 错误码 | 描述 |
|--------|------|
| INSUFFICIENT_BALANCE | 余额不足 |
| BELOW_MINIMUM | 低于最低提现金额 |
| PENDING_WITHDRAW_EXISTS | 已有待处理的提现申请 |

---

## 获取提现记录

```
GET /wallet/withdraws
```

需要认证。

### 查询参数

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| status | WithdrawStatus | 否 | 状态筛选 |
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

### 响应

```json
{
  "success": true,
  "withdraws": [ ... ],
  "pagination": { ... }
}
```

---

## 获取收益记录

```
GET /wallet/earnings
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
  "earnings": [
    {
      "recordId": "earn_123",
      "orderId": "order_456",
      "resourceId": "res_789",
      "resourceName": "高级技能系统",
      "grossAmount": 9900,
      "platformFee": 990,
      "netAmount": 8910,
      "createdAt": "2024-01-01T00:00:00Z"
    }
  ],
  "pagination": { ... }
}
```

---

## 获取交易记录

```
GET /wallet/transactions
```

需要认证。

### 查询参数

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| type | TransactionType | 否 | 类型筛选 |
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

### 响应

```json
{
  "success": true,
  "transactions": [
    {
      "transactionId": "txn_123",
      "type": "EARNINGS",
      "amount": 8910,
      "balance": 108910,
      "description": "资源销售收益 - 高级技能系统",
      "referenceId": "order_456",
      "createdAt": "2024-01-01T00:00:00Z"
    }
  ],
  "pagination": { ... }
}
```

---

## 获取收益统计

```
GET /wallet/stats
```

需要认证。

### 响应

```json
{
  "success": true,
  "stats": {
    "totalEarnings": 1000000,
    "thisMonthEarnings": 50000,
    "lastMonthEarnings": 80000,
    "totalWithdrawn": 500000,
    "pendingWithdraw": 100000
  }
}
```

---

## 管理员API

### 获取待审核提现列表

```
GET /admin/withdraws/pending
```

需要管理员权限。

### 响应

```json
{
  "success": true,
  "withdraws": [ ... ],
  "pagination": { ... }
}
```

---

### 审核通过提现

```
POST /admin/withdraws/{withdrawId}/approve
```

需要管理员权限。

### 响应

```json
{
  "success": true
}
```

---

### 审核拒绝提现

```
POST /admin/withdraws/{withdrawId}/reject
```

需要管理员权限。

### 请求体

| 字段 | 类型 | 必填 | 描述 |
|------|------|------|------|
| reason | string | 是 | 拒绝原因 |

### 响应

```json
{
  "success": true
}
```

---

### 标记提现完成

```
POST /admin/withdraws/{withdrawId}/complete
```

需要管理员权限。

### 响应

```json
{
  "success": true
}
```
