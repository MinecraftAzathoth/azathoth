# 审核模块 API

## 数据模型

### ReviewType (审核类型)

| 值 | 描述 |
|----|------|
| `RESOURCE` | 资源审核 |
| `VERSION` | 版本审核 |
| `REPORT` | 举报审核 |
| `WITHDRAW` | 提现审核 |

### ReviewStatus (审核状态)

| 值 | 描述 |
|----|------|
| `PENDING` | 待审核 |
| `APPROVED` | 已通过 |
| `REJECTED` | 已拒绝 |
| `NEEDS_REVISION` | 需要修改 |

### ReportReason (举报原因)

| 值 | 描述 |
|----|------|
| `SPAM` | 垃圾内容 |
| `MALWARE` | 恶意软件 |
| `COPYRIGHT` | 版权问题 |
| `INAPPROPRIATE` | 不当内容 |
| `SCAM` | 欺诈 |
| `OTHER` | 其他 |

### ReportAction (举报处理动作)

| 值 | 描述 |
|----|------|
| `DISMISS` | 驳回 |
| `WARNING` | 警告 |
| `REMOVE_CONTENT` | 删除内容 |
| `SUSPEND_USER` | 暂停用户 |
| `BAN_USER` | 封禁用户 |

### ThreatSeverity (威胁严重程度)

| 值 | 描述 |
|----|------|
| `LOW` | 低 |
| `MEDIUM` | 中 |
| `HIGH` | 高 |
| `CRITICAL` | 严重 |

### ReviewItem (审核项)

| 字段 | 类型 | 描述 |
|------|------|------|
| reviewId | string | 审核ID |
| type | ReviewType | 审核类型 |
| targetId | string | 目标ID |
| targetName | string | 目标名称 |
| submitterId | string | 提交者ID |
| submitterName | string | 提交者名称 |
| status | ReviewStatus | 状态 |
| priority | int | 优先级(1-5) |
| submittedAt | datetime | 提交时间 |
| reviewedAt | datetime? | 审核时间 |
| reviewerId | string? | 审核员ID |
| reviewerName | string? | 审核员名称 |
| reviewNotes | string? | 审核备注 |

### ChecklistResult (检查项结果)

| 字段 | 类型 | 描述 |
|------|------|------|
| checkId | string | 检查项ID |
| name | string | 检查项名称 |
| description | string | 描述 |
| passed | boolean | 是否通过 |
| notes | string? | 备注 |

### ScanResult (扫描结果)

| 字段 | 类型 | 描述 |
|------|------|------|
| clean | boolean | 是否干净 |
| threats | Threat[] | 威胁列表 |

### Threat (威胁)

| 字段 | 类型 | 描述 |
|------|------|------|
| type | string | 威胁类型 |
| severity | ThreatSeverity | 严重程度 |
| description | string | 描述 |
| location | string? | 位置 |

### CompatibilityResult (兼容性结果)

| 字段 | 类型 | 描述 |
|------|------|------|
| compatible | boolean | 是否兼容 |
| issues | CompatibilityIssue[] | 问题列表 |

### CompatibilityIssue (兼容性问题)

| 字段 | 类型 | 描述 |
|------|------|------|
| type | string | 问题类型 |
| description | string | 描述 |
| suggestion | string? | 建议 |

### DependencyCheckResult (依赖检查结果)

| 字段 | 类型 | 描述 |
|------|------|------|
| valid | boolean | 是否有效 |
| missingDependencies | string[] | 缺失依赖 |
| conflictingDependencies | DependencyConflict[] | 冲突依赖 |

### DependencyConflict (依赖冲突)

| 字段 | 类型 | 描述 |
|------|------|------|
| dependency | string | 依赖名 |
| requiredVersion | string | 需要版本 |
| conflictingVersion | string | 冲突版本 |

### ReviewerStats (审核员统计)

| 字段 | 类型 | 描述 |
|------|------|------|
| reviewerId | string | 审核员ID |
| totalReviewed | int | 总审核数 |
| approvedCount | int | 通过数 |
| rejectedCount | int | 拒绝数 |
| averageReviewTime | long | 平均审核时长(ms) |
| thisMonthCount | int | 本月审核数 |

---

## 审核管理 API (需要版主权限)

### 获取待审核列表

```
GET /admin/reviews/pending
```

### 查询参数

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| type | ReviewType | 否 | 审核类型筛选 |
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

### 响应

```json
{
  "success": true,
  "items": [
    {
      "reviewId": "rev_123",
      "type": "RESOURCE",
      "targetId": "res_456",
      "targetName": "技能系统扩展",
      "submitterId": "user_789",
      "submitterName": "developer",
      "status": "PENDING",
      "priority": 3,
      "submittedAt": "2024-01-01T00:00:00Z",
      "reviewerId": null,
      "reviewerName": null
    }
  ],
  "pagination": { ... }
}
```

---

### 获取审核详情

```
GET /admin/reviews/{reviewId}
```

### 响应 (资源审核)

```json
{
  "success": true,
  "review": {
    "reviewId": "rev_123",
    "type": "RESOURCE",
    "targetId": "res_456",
    "targetName": "技能系统扩展",
    "submitterId": "user_789",
    "submitterName": "developer",
    "status": "PENDING",
    "priority": 3,
    "submittedAt": "2024-01-01T00:00:00Z",
    "resourceName": "技能系统扩展",
    "resourceType": "PLUGIN",
    "resourceDescription": "为Azathoth添加更多技能类型...",
    "downloadUrl": "https://...",
    "checklistResults": [
      {
        "checkId": "check_malware",
        "name": "恶意代码扫描",
        "description": "检查是否包含恶意代码",
        "passed": true,
        "notes": null
      },
      {
        "checkId": "check_api",
        "name": "API兼容性检查",
        "description": "检查是否与目标API版本兼容",
        "passed": true,
        "notes": null
      }
    ]
  }
}
```

---

### 领取审核任务

```
POST /admin/reviews/{reviewId}/claim
```

### 响应

```json
{
  "success": true
}
```

---

### 放弃审核任务

```
POST /admin/reviews/{reviewId}/unclaim
```

### 响应

```json
{
  "success": true
}
```

---

### 通过审核

```
POST /admin/reviews/{reviewId}/approve
```

### 请求体

| 字段 | 类型 | 必填 | 描述 |
|------|------|------|------|
| notes | string | 否 | 审核备注 |

### 响应

```json
{
  "success": true
}
```

---

### 拒绝审核

```
POST /admin/reviews/{reviewId}/reject
```

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

### 要求修改

```
POST /admin/reviews/{reviewId}/request-revision
```

### 请求体

| 字段 | 类型 | 必填 | 描述 |
|------|------|------|------|
| feedback | string | 是 | 修改意见 |

### 响应

```json
{
  "success": true
}
```

---

### 获取审核历史

```
GET /admin/reviews/history/{targetId}
```

### 响应

```json
{
  "success": true,
  "history": [
    {
      "reviewId": "rev_123",
      "status": "REJECTED",
      "reviewerName": "moderator",
      "reviewNotes": "包含未经授权的第三方代码",
      "reviewedAt": "2024-01-01T12:00:00Z"
    },
    {
      "reviewId": "rev_124",
      "status": "APPROVED",
      "reviewerName": "moderator",
      "reviewNotes": "问题已修复",
      "reviewedAt": "2024-01-02T12:00:00Z"
    }
  ]
}
```

---

### 获取审核员统计

```
GET /admin/reviewers/{reviewerId}/stats
```

### 响应

```json
{
  "success": true,
  "stats": {
    "reviewerId": "mod_123",
    "totalReviewed": 500,
    "approvedCount": 400,
    "rejectedCount": 80,
    "averageReviewTime": 3600000,
    "thisMonthCount": 50
  }
}
```

---

## 举报 API

### 提交举报

```
POST /reports
```

需要认证。

### 请求体

| 字段 | 类型 | 必填 | 描述 |
|------|------|------|------|
| targetType | string | 是 | 目标类型: resource, post, reply, user |
| targetId | string | 是 | 目标ID |
| reason | ReportReason | 是 | 举报原因 |
| content | string | 是 | 详细说明 |
| evidence | string[] | 否 | 证据URL列表 |

### 响应

```json
{
  "success": true,
  "reportId": "rpt_123"
}
```

---

### 获取我的举报历史

```
GET /reports/mine
```

需要认证。

### 响应

```json
{
  "success": true,
  "reports": [
    {
      "reviewId": "rpt_123",
      "type": "REPORT",
      "targetId": "res_456",
      "targetName": "恶意插件",
      "status": "PENDING",
      "submittedAt": "2024-01-01T00:00:00Z",
      "reportReason": "MALWARE",
      "reportContent": "该插件包含恶意代码..."
    }
  ]
}
```

---

### 处理举报 (管理员)

```
POST /admin/reports/{reportId}/process
```

需要管理员权限。

### 请求体

| 字段 | 类型 | 必填 | 描述 |
|------|------|------|------|
| action | ReportAction | 是 | 处理动作 |
| notes | string | 否 | 处理备注 |

### 响应

```json
{
  "success": true
}
```

---

## 自动检查 API (内部使用)

### 执行自动检查

```
POST /internal/autocheck/run
```

### 请求体 (multipart/form-data)

| 字段 | 类型 | 必填 | 描述 |
|------|------|------|------|
| resourceId | string | 是 | 资源ID |
| file | file | 是 | 资源文件 |

### 响应

```json
{
  "success": true,
  "results": [
    {
      "checkId": "malware_scan",
      "name": "恶意代码扫描",
      "passed": true
    },
    {
      "checkId": "api_compat",
      "name": "API兼容性",
      "passed": true
    },
    {
      "checkId": "dependency_check",
      "name": "依赖检查",
      "passed": false,
      "notes": "缺少依赖: core-plugin@2.0.0"
    }
  ]
}
```

---

### 恶意代码扫描

```
POST /internal/autocheck/malware
```

### 请求体 (multipart/form-data)

| 字段 | 类型 | 必填 | 描述 |
|------|------|------|------|
| file | file | 是 | 要扫描的文件 |

### 响应

```json
{
  "success": true,
  "result": {
    "clean": false,
    "threats": [
      {
        "type": "REFLECTION_ABUSE",
        "severity": "HIGH",
        "description": "检测到危险的反射调用",
        "location": "com/example/MaliciousClass.class"
      }
    ]
  }
}
```

---

### API 兼容性检查

```
POST /internal/autocheck/compatibility
```

### 请求体 (multipart/form-data)

| 字段 | 类型 | 必填 | 描述 |
|------|------|------|------|
| file | file | 是 | 要检查的文件 |
| targetApiVersion | string | 是 | 目标API版本 |

### 响应

```json
{
  "success": true,
  "result": {
    "compatible": false,
    "issues": [
      {
        "type": "DEPRECATED_API",
        "description": "使用了已弃用的API: PlayerService.getOldMethod()",
        "suggestion": "请使用 PlayerService.getNewMethod() 替代"
      }
    ]
  }
}
```

---

### 依赖检查

```
POST /internal/autocheck/dependencies
```

### 请求体 (multipart/form-data)

| 字段 | 类型 | 必填 | 描述 |
|------|------|------|------|
| file | file | 是 | 要检查的文件 |

### 响应

```json
{
  "success": true,
  "result": {
    "valid": false,
    "missingDependencies": ["core-plugin@2.0.0"],
    "conflictingDependencies": [
      {
        "dependency": "skill-api",
        "requiredVersion": "1.5.0",
        "conflictingVersion": "1.3.0"
      }
    ]
  }
}
```
