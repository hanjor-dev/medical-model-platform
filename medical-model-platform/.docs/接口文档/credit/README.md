# 积分系统模块 - 接口文档

## 📋 模块概述

积分系统模块提供积分管理、积分交易、积分类型管理、积分场景管理等核心功能，支持多种积分类型和灵活的积分规则配置。

## 🔐 接口列表

### 1. 积分管理

#### 1.1 获取当前用户积分余额

**接口描述**: 返回用户所有积分类型的余额信息

**请求路径**: `GET /credits/balance`

**请求方式**: `GET`

**权限要求**: 需要认证

**请求参数**: 无

**请求头**:
```
Authorization: Bearer {token}
```

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "userId": 1234567890,
    "accounts": [
      {
        "creditTypeCode": "NORMAL",
        "creditTypeName": "普通积分",
        "unitName": "积分",
        "iconUrl": "https://example.com/normal.png",
        "colorCode": "#1890ff",
        "balance": 3000.00,
        "totalEarned": 5000.00,
        "totalConsumed": 2000.00
      },
      {
        "creditTypeCode": "PREMIUM",
        "creditTypeName": "高级积分",
        "unitName": "高级积分",
        "iconUrl": "https://example.com/premium.png",
        "colorCode": "#52c41a",
        "balance": 1500.00,
        "totalEarned": 2000.00,
        "totalConsumed": 500.00
      }
    ],
    "totalBalance": 4500.00,
    "accountCount": 2,
    "queryTime": "2025-01-15T01:20:00"
  },
  "timestamp": 1673836800000
}
```

**响应字段说明**:
| 字段名 | 类型 | 说明 |
|--------|------|------|
| userId | Long | 用户ID |
| accounts | Array | 积分账户列表 |
| totalBalance | BigDecimal | 总积分余额 |
| accountCount | Integer | 积分账户数量 |
| queryTime | LocalDateTime | 查询时间 |

---

#### 1.2 管理员积分分配

**接口描述**: 管理员向用户分配积分

**请求路径**: `POST /credits/allocate`

**请求方式**: `POST`

**权限要求**: ADMIN权限

**请求参数**:
```json
{
  "targetUserId": 1234567890,
  "creditTypeCode": "NORMAL",
  "amount": 1000.00,
  "description": "月度积分分配"
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| targetUserId | Long | 是 | 目标用户ID | 1234567890 |
| creditTypeCode | String | 是 | 积分类型编码 | "NORMAL" |
| amount | BigDecimal | 是 | 分配积分数 | 1000.00 |
| description | String | 否 | 分配描述 | "月度积分分配" |

**响应结果**:
```json
{
  "code": 200,
  "message": "积分分配成功",
  "data": {
    "transactionId": 9876543210,
    "targetUserId": 1234567890,
    "creditTypeCode": "NORMAL",
    "amount": 1000.00,
    "balanceAfter": 4000.00,
    "transactionTime": "2025-01-15T01:20:00"
  },
  "timestamp": 1673836800000
}
```

---

#### 1.3 积分消费

**接口描述**: 用户消费积分

**请求路径**: `POST /credits/consume`

**请求方式**: `POST`

**权限要求**: 需要认证

**请求参数**:
```json
{
  "creditTypeCode": "NORMAL",
  "amount": 100.00,
  "scenarioCode": "AI_MODEL_TRAINING",
  "description": "AI模型训练费用"
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| creditTypeCode | String | 是 | 积分类型编码 | "NORMAL" |
| amount | BigDecimal | 是 | 消费积分数 | 100.00 |
| scenarioCode | String | 是 | 消费场景编码 | "AI_MODEL_TRAINING" |
| description | String | 否 | 消费描述 | "AI模型训练费用" |

**响应结果**:
```json
{
  "code": 200,
  "message": "积分消费成功",
  "data": {
    "transactionId": 9876543211,
    "creditTypeCode": "NORMAL",
    "amount": 100.00,
    "balanceAfter": 3900.00,
    "transactionTime": "2025-01-15T01:20:00"
  },
  "timestamp": 1673836800000
}
```

---

#### 1.4 积分转账

**接口描述**: 用户向其他用户转账积分

**请求路径**: `POST /credits/transfer`

**请求方式**: `POST`

**权限要求**: 需要认证

**请求参数**:
```json
{
  "targetUserId": 1234567891,
  "creditTypeCode": "NORMAL",
  "amount": 500.00,
  "description": "积分转账"
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| targetUserId | Long | 是 | 目标用户ID | 1234567891 |
| creditTypeCode | String | 是 | 积分类型编码 | "NORMAL" |
| amount | BigDecimal | 是 | 转账积分数 | 500.00 |
| description | String | 否 | 转账描述 | "积分转账" |

**响应结果**:
```json
{
  "code": 200,
  "message": "积分转账成功",
  "data": {
    "transactionId": 9876543212,
    "targetUserId": 1234567891,
    "creditTypeCode": "NORMAL",
    "amount": 500.00,
    "balanceAfter": 3400.00,
    "transactionTime": "2025-01-15T01:20:00"
  },
  "timestamp": 1673836800000
}
```

**请求路径**: `POST /credits/consume`

**请求方式**: `POST`

**权限要求**: 需要认证

**请求参数**:
```json
{
  "creditTypeCode": "NORMAL",
  "amount": 100.00,
  "scenarioCode": "AI_MODEL_TRAINING",
  "description": "AI模型训练费用"
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| creditTypeCode | String | 是 | 积分类型编码 | "NORMAL" |
| amount | BigDecimal | 是 | 消费积分数 | 100.00 |
| scenarioCode | String | 是 | 消费场景编码 | "AI_MODEL_TRAINING" |
| description | String | 否 | 消费描述 | "AI模型训练费用" |

**响应结果**:
```json
{
  "code": 200,
  "message": "积分消费成功",
  "data": {
    "transactionId": 9876543211,
    "creditTypeCode": "NORMAL",
    "amount": 100.00,
    "balanceAfter": 3900.00,
    "transactionTime": "2025-01-15T01:20:00"
  },
  "timestamp": 1673836800000
}
```
---

#### 1.5 管理员查看用户积分余额

**接口描述**: 管理员查看指定用户的积分余额

**请求路径**: `GET /credits/admin/balance/{userId}`

**请求方式**: `GET`

**权限要求**: ADMIN权限

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| userId | Long | 是 | 用户ID | 1234567890 |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "userId": 1234567890,
    "accounts": [
      {
        "creditTypeCode": "NORMAL",
        "creditTypeName": "普通积分",
        "unitName": "积分",
        "iconUrl": "https://example.com/normal.png",
        "colorCode": "#1890ff",
        "balance": 3000.00,
        "totalEarned": 5000.00,
        "totalConsumed": 2000.00
      }
    ],
    "totalBalance": 3000.00,
    "accountCount": 1,
    "queryTime": "2025-01-15T01:20:00"
  },
  "timestamp": 1673836800000
}
```

---

#### 1.6 管理员积分分配

**接口描述**: 管理员向用户分配积分

**请求路径**: `POST /credits/admin/allocate`

**请求方式**: `POST`

**权限要求**: ADMIN权限

**请求参数**:
```json
{
  "targetUserId": 1234567890,
  "creditTypeCode": "NORMAL",
  "amount": 1000.00,
  "description": "月度积分分配",
  "allocationType": "MONTHLY"
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| targetUserId | Long | 是 | 目标用户ID | 1234567890 |
| creditTypeCode | String | 是 | 积分类型编码 | "NORMAL" |
| amount | BigDecimal | 是 | 分配积分数 | 1000.00 |
| description | String | 否 | 分配描述 | "月度积分分配" |
| allocationType | String | 否 | 分配类型 | "MONTHLY" |

**响应结果**:
```json
{
  "code": 200,
  "message": "积分分配成功",
  "data": {
    "transactionId": 9876543213,
    "targetUserId": 1234567890,
    "creditTypeCode": "NORMAL",
    "amount": 1000.00,
    "balanceAfter": 4000.00,
    "transactionTime": "2025-01-15T01:20:00"
  },
  "timestamp": 1673836800000
}
```

---

#### 1.7 获取分配历史

**接口描述**: 获取积分分配的历史记录

**请求路径**: `GET /credits/admin/allocation-history`

**请求方式**: `GET`

**权限要求**: ADMIN权限

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| current | Integer | 否 | 页码，默认1 | 1 |
| size | Integer | 否 | 每页大小，默认10 | 10 |
| targetUserId | Long | 否 | 目标用户ID | 1234567890 |
| creditTypeCode | String | 否 | 积分类型编码 | "NORMAL" |
| startTime | String | 否 | 开始时间 | "2025-01-01" |
| endTime | String | 否 | 结束时间 | "2025-01-15" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "records": [
      {
        "id": 9876543213,
        "targetUserId": 1234567890,
        "targetUsername": "zhang123",
        "creditTypeCode": "NORMAL",
        "amount": 1000.00,
        "description": "月度积分分配",
        "allocationType": "MONTHLY",
        "operatorId": 9999999999,
        "operatorName": "admin",
        "transactionTime": "2025-01-15T01:20:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  },
  "timestamp": 1673836800000
}
```

---

#### 1.8 获取管理员统计

**接口描述**: 获取积分管理的统计信息

**请求路径**: `GET /credits/admin/statistics`

**请求方式**: `GET`

**权限要求**: ADMIN权限

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| startTime | String | 否 | 开始时间 | "2025-01-01" |
| endTime | String | 否 | 结束时间 | "2025-01-15" |
| creditTypeCode | String | 否 | 积分类型编码 | "NORMAL" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "totalAllocated": 50000.00,
    "totalConsumed": 30000.00,
    "totalTransferred": 10000.00,
    "netChange": 10000.00,
    "userCount": 100,
    "topUsers": [
      {
        "userId": 1234567890,
        "username": "zhang123",
        "totalAllocated": 5000.00,
        "totalConsumed": 3000.00,
        "balance": 2000.00
      }
    ],
    "dailyStats": [
      {
        "date": "2025-01-15",
        "allocated": 1000.00,
        "consumed": 500.00,
        "transferred": 200.00
      }
    ]
  },
  "timestamp": 1673836800000
}
```

---

### 2. 积分交易

#### 2.1 分页查询交易记录

**接口描述**: 支持按积分类型、交易类型、时间范围等条件筛选

**请求路径**: `GET /api/credits/transactions`

**请求方式**: `GET`

**权限要求**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| current | Integer | 否 | 页码，默认1 | 1 |
| size | Integer | 否 | 每页大小，默认10 | 10 |
| creditTypeCode | String | 否 | 积分类型编码 | "NORMAL" |
| transactionType | String | 否 | 交易类型 | "ALLOCATE" |
| startTime | String | 否 | 开始时间 | "2025-01-01" |
| endTime | String | 否 | 结束时间 | "2025-01-15" |
| keyword | String | 否 | 关键词搜索 | "训练" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "records": [
      {
        "id": 9876543210,
        "userId": 1234567890,
        "creditTypeCode": "NORMAL",
        "transactionType": "ALLOCATE",
        "amount": 1000.00,
        "balanceBefore": 3000.00,
        "balanceAfter": 4000.00,
        "scenarioCode": "MONTHLY_ALLOCATION",
        "description": "月度积分分配",
        "operatorId": 9999999999,
        "operatorName": "admin",
        "transactionTime": "2025-01-15T01:20:00",
        "createTime": "2025-01-15T01:20:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  },
  "timestamp": 1673836800000
}
```

**响应字段说明**:
| 字段名 | 类型 | 说明 |
|--------|------|------|
| transactionType | String | 交易类型（ALLOCATE/CONSUME/TRANSFER/REFUND） |
| amount | BigDecimal | 交易积分数 |
| balanceBefore | BigDecimal | 交易前余额 |
| balanceAfter | BigDecimal | 交易后余额 |
| scenarioCode | String | 场景编码 |
| operatorId | Long | 操作人ID |
| operatorName | String | 操作人姓名 |

---

#### 2.2 获取交易统计

**接口描述**: 获取积分交易的统计信息

**请求路径**: `GET /api/credits/transactions/statistics`

**请求方式**: `GET`

**权限要求**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| startTime | String | 否 | 开始时间 | "2025-01-01" |
| endTime | String | 否 | 结束时间 | "2025-01-15" |
| creditTypeCode | String | 否 | 积分类型编码 | "NORMAL" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "totalTransactions": 150,
    "totalAllocated": 5000.00,
    "totalConsumed": 3000.00,
    "totalTransferred": 1000.00,
    "totalRefunded": 500.00,
    "netChange": 500.00,
    "topScenarios": [
      {
        "scenarioCode": "AI_MODEL_TRAINING",
        "scenarioName": "AI模型训练",
        "totalAmount": 2000.00,
        "transactionCount": 50
      }
    ],
    "dailyStats": [
      {
        "date": "2025-01-15",
        "allocated": 1000.00,
        "consumed": 500.00,
        "transferred": 200.00
      }
    ]
  },
  "timestamp": 1673836800000
}
```

---

#### 2.3 分页查询交易记录

**接口描述**: 分页查询积分交易记录

**请求路径**: `POST /api/credits/transactions/page`

**请求方式**: `POST`

**权限要求**: 需要认证

**请求参数**:
```json
{
  "current": 1,
  "size": 10,
  "creditTypeCode": "NORMAL",
  "transactionType": "CONSUME",
  "startTime": "2025-01-01",
  "endTime": "2025-01-15",
  "keyword": "训练"
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| current | Integer | 否 | 页码，默认1 | 1 |
| size | Integer | 否 | 每页大小，默认10 | 10 |
| creditTypeCode | String | 否 | 积分类型编码 | "NORMAL" |
| transactionType | String | 否 | 交易类型 | "CONSUME" |
| startTime | String | 否 | 开始时间 | "2025-01-01" |
| endTime | String | 否 | 结束时间 | "2025-01-15" |
| keyword | String | 否 | 关键词搜索 | "训练" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "records": [
      {
        "id": 9876543211,
        "userId": 1234567890,
        "creditTypeCode": "NORMAL",
        "transactionType": "CONSUME",
        "amount": 100.00,
        "balanceBefore": 4000.00,
        "balanceAfter": 3900.00,
        "scenarioCode": "AI_MODEL_TRAINING",
        "description": "AI模型训练费用",
        "transactionTime": "2025-01-15T01:20:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  },
  "timestamp": 1673836800000
}
```

---

#### 2.4 获取我的交易记录

**接口描述**: 获取当前用户的交易记录

**请求路径**: `GET /api/credits/transactions/my`

**请求方式**: `GET`

**权限要求**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| current | Integer | 否 | 页码，默认1 | 1 |
| size | Integer | 否 | 每页大小，默认10 | 10 |
| creditTypeCode | String | 否 | 积分类型编码 | "NORMAL" |
| transactionType | String | 否 | 交易类型 | "CONSUME" |
| startTime | String | 否 | 开始时间 | "2025-01-01" |
| endTime | String | 否 | 结束时间 | "2025-01-15" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "records": [
      {
        "id": 9876543211,
        "creditTypeCode": "NORMAL",
        "transactionType": "CONSUME",
        "amount": 100.00,
        "balanceBefore": 4000.00,
        "balanceAfter": 3900.00,
        "scenarioCode": "AI_MODEL_TRAINING",
        "description": "AI模型训练费用",
        "transactionTime": "2025-01-15T01:20:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  },
  "timestamp": 1673836800000
}
```

---

#### 2.5 获取用户交易记录

**接口描述**: 获取指定用户的交易记录（管理员权限）

**请求路径**: `GET /api/credits/transactions/user/{userId}`

**请求方式**: `GET`

**权限要求**: ADMIN权限

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| userId | Long | 是 | 用户ID | 1234567890 |
| current | Integer | 否 | 页码，默认1 | 1 |
| size | Integer | 否 | 每页大小，默认10 | 10 |
| creditTypeCode | String | 否 | 积分类型编码 | "NORMAL" |
| transactionType | String | 否 | 交易类型 | "CONSUME" |
| startTime | String | 否 | 开始时间 | "2025-01-01" |
| endTime | String | 否 | 结束时间 | "2025-01-15" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "records": [
      {
        "id": 9876543211,
        "userId": 1234567890,
        "creditTypeCode": "NORMAL",
        "transactionType": "CONSUME",
        "amount": 100.00,
        "balanceBefore": 4000.00,
        "balanceAfter": 3900.00,
        "scenarioCode": "AI_MODEL_TRAINING",
        "description": "AI模型训练费用",
        "transactionTime": "2025-01-15T01:20:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  },
  "timestamp": 1673836800000
}
```

---

#### 2.6 获取交易详情

**接口描述**: 根据交易ID获取交易详情

**请求路径**: `GET /api/credits/transactions/{transactionId}`

**请求方式**: `GET`

**权限要求**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| transactionId | Long | 是 | 交易ID | 9876543211 |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "id": 9876543211,
    "userId": 1234567890,
    "creditTypeCode": "NORMAL",
    "transactionType": "CONSUME",
    "amount": 100.00,
    "balanceBefore": 4000.00,
    "balanceAfter": 3900.00,
    "scenarioCode": "AI_MODEL_TRAINING",
    "description": "AI模型训练费用",
    "operatorId": null,
    "operatorName": null,
    "transactionTime": "2025-01-15T01:20:00",
    "createTime": "2025-01-15T01:20:00"
  },
  "timestamp": 1673836800000
}
```

---

#### 2.7 获取相关交易

**接口描述**: 获取与指定交易相关的其他交易

**请求路径**: `GET /api/credits/transactions/{transactionId}/related`

**请求方式**: `GET`

**权限要求**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| transactionId | Long | 是 | 交易ID | 9876543211 |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 9876543210,
      "creditTypeCode": "NORMAL",
      "transactionType": "ALLOCATE",
      "amount": 1000.00,
      "description": "月度积分分配",
      "transactionTime": "2025-01-15T01:00:00"
    }
  ],
  "timestamp": 1673836800000
}
```

---

#### 2.8 获取我的交易统计

**接口描述**: 获取当前用户的交易统计信息

**请求路径**: `GET /api/credits/transactions/my/statistics`

**请求方式**: `GET`

**权限要求**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| startTime | String | 否 | 开始时间 | "2025-01-01" |
| endTime | String | 否 | 结束时间 | "2025-01-15" |
| creditTypeCode | String | 否 | 积分类型编码 | "NORMAL" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "totalTransactions": 50,
    "totalAllocated": 5000.00,
    "totalConsumed": 3000.00,
    "totalTransferred": 1000.00,
    "netChange": 1000.00,
    "topScenarios": [
      {
        "scenarioCode": "AI_MODEL_TRAINING",
        "scenarioName": "AI模型训练",
        "totalAmount": 2000.00,
        "transactionCount": 20
      }
    ],
    "dailyStats": [
      {
        "date": "2025-01-15",
        "allocated": 1000.00,
        "consumed": 500.00,
        "transferred": 200.00
      }
    ]
  },
  "timestamp": 1673836800000
}
```

---

#### 2.9 获取用户交易统计

**接口描述**: 获取指定用户的交易统计信息（管理员权限）

**请求路径**: `GET /api/credits/transactions/user/{userId}/statistics`

**请求方式**: `GET`

**权限要求**: ADMIN权限

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| userId | Long | 是 | 用户ID | 1234567890 |
| startTime | String | 否 | 开始时间 | "2025-01-01" |
| endTime | String | 否 | 结束时间 | "2025-01-15" |
| creditTypeCode | String | 否 | 积分类型编码 | "NORMAL" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "userId": 1234567890,
    "username": "zhang123",
    "totalTransactions": 50,
    "totalAllocated": 5000.00,
    "totalConsumed": 3000.00,
    "totalTransferred": 1000.00,
    "netChange": 1000.00,
    "currentBalance": 2000.00,
    "topScenarios": [
      {
        "scenarioCode": "AI_MODEL_TRAINING",
        "scenarioName": "AI模型训练",
        "totalAmount": 2000.00,
        "transactionCount": 20
      }
    ],
    "dailyStats": [
      {
        "date": "2025-01-15",
        "allocated": 1000.00,
        "consumed": 500.00,
        "transferred": 200.00
      }
    ]
  },
  "timestamp": 1673836800000
}
```

---

### 3. 积分类型管理

#### 3.1 分页查询积分类型

**接口描述**: 支持按状态和关键词筛选

**请求路径**: `GET /api/credits/types`

**请求方式**: `GET`

**权限要求**: ADMIN权限

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| current | Integer | 否 | 页码，默认1 | 1 |
| size | Integer | 否 | 每页大小，默认10 | 10 |
| status | Integer | 否 | 状态（0:禁用 1:启用） | 1 |
| keyword | String | 否 | 关键词搜索 | "积分" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "records": [
      {
        "id": 1,
        "typeCode": "NORMAL",
        "typeName": "普通积分",
        "description": "基础积分类型",
        "unitName": "积分",
        "iconUrl": "https://example.com/normal.png",
        "colorCode": "#1890ff",
        "decimalPlaces": 2,
        "isTransferable": 1,
        "status": 1,
        "sortOrder": 1,
        "createTime": "2025-01-15T01:20:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  },
  "timestamp": 1673836800000
}
```

---

#### 3.2 创建积分类型

**接口描述**: 创建新的积分类型

**请求路径**: `POST /api/credits/types`

**请求方式**: `POST`

**权限要求**: ADMIN权限

**请求参数**:
```json
{
  "typeCode": "VIP_POINTS",
  "typeName": "VIP积分",
  "description": "VIP用户专用积分",
  "unitName": "VIP点",
  "iconUrl": "https://example.com/vip.png",
  "colorCode": "#f5222d",
  "decimalPlaces": 0,
  "isTransferable": 0,
  "status": 1,
  "sortOrder": 10
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| typeCode | String | 是 | 类型编码，唯一 | "VIP_POINTS" |
| typeName | String | 是 | 类型名称 | "VIP积分" |
| description | String | 否 | 类型描述 | "VIP用户专用积分" |
| unitName | String | 是 | 单位名称 | "VIP点" |
| iconUrl | String | 否 | 图标URL | "https://example.com/vip.png" |
| colorCode | String | 否 | 显示颜色 | "#f5222d" |
| decimalPlaces | Integer | 否 | 小数位数，默认2 | 0 |
| isTransferable | Integer | 否 | 是否可转账，默认1 | 0 |
| status | Integer | 否 | 状态，默认1 | 1 |
| sortOrder | Integer | 否 | 排序，默认0 | 10 |

**响应结果**:
```json
{
  "code": 200,
  "message": "积分类型创建成功",
  "data": 123,
  "timestamp": 1673836800000
}
```

---

#### 3.3 更新积分类型

**接口描述**: 更新指定积分类型的信息

**请求路径**: `PUT /api/credits/types/{id}`

**请求方式**: `PUT`

**权限要求**: ADMIN权限

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| id | Long | 是 | 积分类型ID | 123 |

**响应结果**:
```json
{
  "code": 200,
  "message": "积分类型更新成功",
  "data": null,
  "timestamp": 1673836800000
}
```

---

#### 3.4 删除积分类型

**接口描述**: 删除指定积分类型

**请求路径**: `DELETE /api/credits/types/{id}`

**请求方式**: `DELETE`

**权限要求**: ADMIN权限

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| id | Long | 是 | 积分类型ID | 123 |

**响应结果**:
```json
{
  "code": 200,
  "message": "积分类型删除成功",
  "data": null,
  "timestamp": 1673836800000
}
```

---

#### 3.5 启用积分类型

**接口描述**: 启用指定的积分类型

**请求路径**: `PUT /credits/types/{id}/enable`

**请求方式**: `PUT`

**权限要求**: ADMIN权限

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| id | Long | 是 | 积分类型ID | 123 |

**响应结果**:
```json
{
  "code": 200,
  "message": "积分类型启用成功",
  "data": null,
  "timestamp": 1673836800000
}
```

---

#### 3.6 禁用积分类型

**接口描述**: 禁用指定的积分类型

**请求路径**: `PUT /credits/types/{id}/disable`

**请求方式**: `PUT`

**权限要求**: ADMIN权限

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| id | Long | 是 | 积分类型ID | 123 |

**响应结果**:
```json
{
  "code": 200,
  "message": "积分类型禁用成功",
  "data": null,
  "timestamp": 1673836800000
}
```

---

#### 3.7 更新积分类型排序

**接口描述**: 更新积分类型的排序

**请求路径**: `PUT /credits/types/sort`

**请求方式**: `PUT`

**权限要求**: ADMIN权限

**请求参数**:
```json
[
  {
    "id": 1,
    "sortOrder": 10
  },
  {
    "id": 2,
    "sortOrder": 20
  }
]
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| id | Long | 是 | 积分类型ID | 1 |
| sortOrder | Integer | 是 | 排序值 | 10 |

**响应结果**:
```json
{
  "code": 200,
  "message": "排序更新成功",
  "data": null,
  "timestamp": 1673836800000
}
```

---

### 4. 积分场景管理

#### 4.1 分页查询积分场景

**接口描述**: 支持按场景类型和关键词筛选

**请求路径**: `GET /api/credits/scenarios`

**请求方式**: `GET`

**权限要求**: ADMIN权限

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| current | Integer | 否 | 页码，默认1 | 1 |
| size | Integer | 否 | 每页大小，默认10 | 10 |
| scenarioType | String | 否 | 场景类型 | "CONSUMPTION" |
| keyword | String | 否 | 关键词搜索 | "训练" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "records": [
      {
        "id": 1,
        "scenarioCode": "AI_MODEL_TRAINING",
        "scenarioName": "AI模型训练",
        "scenarioType": "CONSUMPTION",
        "description": "AI模型训练费用",
        "creditTypeCode": "NORMAL",
        "creditTypeName": "普通积分",
        "unitPrice": 10.00,
        "minAmount": 1.00,
        "maxAmount": 10000.00,
        "status": 1,
        "sortOrder": 1,
        "createTime": "2025-01-15T01:20:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  },
  "timestamp": 1673836800000
}
```

---

#### 4.2 创建积分场景

**接口描述**: 创建新的积分使用场景

**请求路径**: `POST /api/credits/scenarios`

**请求方式**: `POST`

**权限要求**: ADMIN权限

**请求参数**:
```json
{
  "scenarioCode": "FILE_STORAGE",
  "scenarioName": "文件存储",
  "scenarioType": "CONSUMPTION",
  "description": "文件存储费用",
  "creditTypeCode": "NORMAL",
  "unitPrice": 0.01,
  "minAmount": 0.01,
  "maxAmount": 1000.00,
  "status": 1,
  "sortOrder": 10
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| scenarioCode | String | 是 | 场景编码，唯一 | "FILE_STORAGE" |
| scenarioName | String | 是 | 场景名称 | "文件存储" |
| scenarioType | String | 是 | 场景类型 | "CONSUMPTION" |
| description | String | 否 | 场景描述 | "文件存储费用" |
| creditTypeCode | String | 是 | 积分类型编码 | "NORMAL" |
| unitPrice | BigDecimal | 是 | 单价 | 0.01 |
| minAmount | BigDecimal | 否 | 最小金额，默认0.01 | 0.01 |
| maxAmount | BigDecimal | 否 | 最大金额，默认无限制 | 1000.00 |
| status | Integer | 否 | 状态，默认1 | 1 |
| sortOrder | Integer | 否 | 排序，默认0 | 10 |

**响应结果**:
```json
{
  "code": 200,
  "message": "积分场景创建成功",
  "data": 123,
  "timestamp": 1673836800000
}
```

---

#### 4.3 更新积分场景

**接口描述**: 更新指定积分场景的信息

**请求路径**: `PUT /api/credits/scenarios/{id}`

**请求方式**: `PUT`

**权限要求**: ADMIN权限

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| id | Long | 是 | 积分场景ID | 123 |

**响应结果**:
```json
{
  "code": 200,
  "message": "积分场景更新成功",
  "data": null,
  "timestamp": 1673836800000
}
```

---

#### 4.4 删除积分场景

**接口描述**: 删除指定积分场景

**请求路径**: `DELETE /api/credits/scenarios/{id}`

**请求方式**: `DELETE`

**权限要求**: ADMIN权限

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| id | Long | 是 | 积分场景ID | 123 |

**响应结果**:
```json
{
  "code": 200,
  "message": "积分场景删除成功",
  "data": null,
  "timestamp": 1673836800000
}
```

---



**接口描述**: 获取所有启用的积分场景

**请求路径**: `GET /credits/scenarios/enabled`

**请求方式**: `GET`

**权限要求**: 无需认证

**请求参数**: 无

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 1,
      "scenarioCode": "AI_MODEL_TRAINING",
      "scenarioName": "AI模型训练",
      "scenarioType": "CONSUMPTION",
      "description": "AI模型训练费用",
      "creditTypeCode": "NORMAL",
      "unitPrice": 10.00,
      "minAmount": 1.00,
      "maxAmount": 10000.00,
      "status": 1,
      "sortOrder": 1
    }
  ],
  "timestamp": 1673836800000
}
```

---



**接口描述**: 根据积分类型获取相关的积分场景

**请求路径**: `GET /credits/scenarios/by-credit-type/{creditTypeCode}`

**请求方式**: `GET`

**权限要求**: 无需认证

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| creditTypeCode | String | 是 | 积分类型编码 | "NORMAL" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 1,
      "scenarioCode": "AI_MODEL_TRAINING",
      "scenarioName": "AI模型训练",
      "scenarioType": "CONSUMPTION",
      "description": "AI模型训练费用",
      "creditTypeCode": "NORMAL",
      "unitPrice": 10.00,
      "minAmount": 1.00,
      "maxAmount": 10000.00",
      "status": 1,
      "sortOrder": 1
    }
  ],
  "timestamp": 1673836800000
}
```

---

#### 4.5 获取消费场景

**接口描述**: 获取所有消费类型的积分场景

**请求路径**: `GET /credits/scenarios/consumption`

**请求方式**: `GET`

**权限要求**: 无需认证

**请求参数**: 无

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 1,
      "scenarioCode": "AI_MODEL_TRAINING",
      "scenarioName": "AI模型训练",
      "scenarioType": "CONSUMPTION",
      "description": "AI模型训练费用",
      "creditTypeCode": "NORMAL",
      "unitPrice": 10.00,
      "minAmount": 1.00,
      "maxAmount": 10000.00",
      "status": 1,
      "sortOrder": 1
    }
  ],
  "timestamp": 1673836800000
}
```

---

#### 4.6 获取奖励场景

**接口描述**: 获取所有奖励类型的积分场景

**请求路径**: `GET /credits/scenarios/reward`

**请求方式**: `GET`

**权限要求**: 无需认证

**请求参数**: 无

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 2,
      "scenarioCode": "TASK_COMPLETION",
      "scenarioName": "任务完成奖励",
      "scenarioType": "REWARD",
      "description": "完成任务获得的积分奖励",
      "creditTypeCode": "NORMAL",
      "unitPrice": 1.00,
      "minAmount": 1.00,
      "maxAmount": 1000.00",
      "status": 1,
      "sortOrder": 2
    }
  ],
  "timestamp": 1673836800000
}
```

---

#### 4.7 根据用户角色获取场景

**接口描述**: 根据用户角色获取可访问的积分场景

**请求路径**: `GET /credits/scenarios/by-user-role/{userRole}`

**请求方式**: `GET`

**权限要求**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| userRole | String | 是 | 用户角色 | "USER" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 1,
      "scenarioCode": "AI_MODEL_TRAINING",
      "scenarioName": "AI模型训练",
      "scenarioType": "CONSUMPTION",
      "description": "AI模型训练费用",
      "creditTypeCode": "NORMAL",
      "unitPrice": 10.00,
      "minAmount": 1.00,
      "maxAmount": 10000.00",
      "status": 1,
      "sortOrder": 1
    }
  ],
  "timestamp": 1673836800000
}
```

---

#### 4.8 根据场景代码获取场景

**接口描述**: 根据场景代码获取积分场景详情

**请求路径**: `GET /credits/scenarios/code/{scenarioCode}`

**请求方式**: `GET`

**权限要求**: 无需认证

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| scenarioCode | String | 是 | 场景代码 | "AI_MODEL_TRAINING" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "id": 1,
    "scenarioCode": "AI_MODEL_TRAINING",
    "scenarioName": "AI模型训练",
    "scenarioType": "CONSUMPTION",
    "description": "AI模型训练费用",
    "creditTypeCode": "NORMAL",
    "unitPrice": 10.00,
    "minAmount": 1.00,
    "maxAmount": 10000.00",
    "status": 1,
    "sortOrder": 1,
    "createTime": "2025-01-15T01:20:00",
    "updateTime": "2025-01-15T01:20:00"
  },
  "timestamp": 1673836800000
}
```

---

#### 4.9 启用积分场景

**接口描述**: 启用指定的积分场景

**请求路径**: `PUT /credits/scenarios/{id}/enable`

**请求方式**: `PUT`

**权限要求**: ADMIN权限

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| id | Long | 是 | 积分场景ID | 123 |

**响应结果**:
```json
{
  "code": 200,
  "message": "积分场景启用成功",
  "data": null,
  "timestamp": 1673836800000
}
```

---

#### 4.10 禁用积分场景

**接口描述**: 禁用指定的积分场景

**请求路径**: `PUT /credits/scenarios/{id}/disable`

**请求方式**: `PUT`

**权限要求**: ADMIN权限

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| id | Long | 是 | 积分场景ID | 123 |

**响应结果**:
```json
{
  "code": 200,
  "message": "积分场景禁用成功",
  "data": null,
  "timestamp": 1673836800000
}
```

---

#### 4.11 检查场景权限

**接口描述**: 检查当前用户是否有权限使用指定场景

**请求路径**: `GET /credits/scenarios/check-permission/{scenarioCode}`

**请求方式**: `GET`

**权限要求**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| scenarioCode | String | 是 | 场景代码 | "AI_MODEL_TRAINING" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "scenarioCode": "AI_MODEL_TRAINING",
    "hasPermission": true,
    "reason": "用户有足够积分",
    "requiredAmount": 100.00,
    "userBalance": 3000.00
  },
  "timestamp": 1673836800000
}
```

---

#### 4.12 检查每日限制

**接口描述**: 检查指定场景的每日使用限制

**请求路径**: `GET /credits/scenarios/check-daily-limit/{scenarioCode}`

**请求方式**: `GET`

**权限要求**: 需要认证

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| scenarioCode | String | 是 | 场景代码 | "AI_MODEL_TRAINING" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "scenarioCode": "AI_MODEL_TRAINING",
    "dailyLimit": 1000.00,
    "score": 1000.00,
    "usedToday": 500.00,
    "remainingToday": 500.00,
    "canUse": true,
    "nextResetTime": "2025-01-16T00:00:00"
  },
  "timestamp": 1673836800000
}
```

## 🔒 权限控制

### 积分管理权限
- **积分查询**: 所有认证用户
- **积分消费**: 所有认证用户
- **积分转账**: 所有认证用户（需积分类型支持）
- **积分分配**: ADMIN权限

### 积分类型管理权限
- **查询**: ADMIN权限
- **创建**: ADMIN权限
- **更新**: ADMIN权限
- **删除**: ADMIN权限

### 积分场景管理权限
- **查询**: ADMIN权限
- **创建**: ADMIN权限
- **更新**: ADMIN权限
- **删除**: ADMIN权限

## 📝 错误码说明

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 4001 | 参数验证失败 | 检查请求参数格式和必填项 |
| 4002 | 积分类型不存在 | 检查积分类型编码是否正确 |
| 4003 | 积分余额不足 | 检查用户积分余额 |
| 4004 | 积分类型不支持转账 | 选择支持转账的积分类型 |
| 4005 | 积分场景不存在 | 检查积分场景编码是否正确 |
| 4006 | 转账金额超出限制 | 检查转账金额是否在允许范围内 |
| 4007 | 权限不足 | 检查用户角色和权限 |

## 🚀 使用示例

### 查询积分余额
```bash
curl -X GET "http://localhost:8080/api/credits/balance" \
  -H "Authorization: Bearer {token}"
```

### 积分分配（管理员）
```bash
curl -X POST "http://localhost:8080/api/credits/allocate" \
  -H "Authorization: Bearer {admin_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "targetUserId": 1234567890,
    "creditTypeCode": "NORMAL",
    "amount": 1000.00,
    "description": "月度积分分配"
  }'
```

### 积分消费
```bash
curl -X POST "http://localhost:8080/api/credits/consume" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "creditTypeCode": "NORMAL",
    "amount": 100.00,
    "scenarioCode": "AI_MODEL_TRAINING",
    "description": "AI模型训练费用"
  }'
```

### 积分转账
```bash
curl -X POST "http://localhost:8080/api/credits/transfer" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "targetUserId": 1234567891,
    "creditTypeCode": "NORMAL",
    "amount": 500.00,
    "description": "积分转账"
  }'
```

### 查询交易记录
```bash
curl -X GET "http://localhost:8080/api/credits/transactions?creditTypeCode=NORMAL&transactionType=CONSUME" \
  -H "Authorization: Bearer {token}"
```

### 创建积分类型（管理员）
```bash
curl -X POST "http://localhost:8080/api/credits/types" \
  -H "Authorization: Bearer {admin_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "typeCode": "VIP_POINTS",
    "typeName": "VIP积分",
    "description": "VIP用户专用积分",
    "unitName": "VIP点",
    "colorCode": "#f5222d",
    "decimalPlaces": 0,
    "isTransferable": 0
  }'
```

### 创建积分场景（管理员）
```bash
curl -X POST "http://localhost:8080/api/credits/scenarios" \
  -H "Authorization: Bearer {admin_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "scenarioCode": "FILE_STORAGE",
    "scenarioName": "文件存储",
    "scenarioType": "CONSUMPTION",
    "description": "文件存储费用",
    "creditTypeCode": "NORMAL",
    "unitPrice": 0.01,
    "minAmount": 0.01,
    "maxAmount": 1000.00
  }'
```

## 📊 业务规则

### 积分管理
- 支持多种积分类型
- 积分余额不能为负数
- 积分转账需要类型支持
- 所有积分操作记录交易日志

### 积分类型
- 类型编码必须唯一
- 支持不同小数位数
- 可配置是否支持转账
- 支持图标和颜色配置

### 积分场景
- 场景编码必须唯一
- 支持消费和奖励场景
- 可配置单价和金额限制
- 关联特定积分类型

### 积分交易
- 所有交易记录完整日志
- 支持交易统计和分析
- 提供余额变更历史
- 支持多种交易类型

---

> 💡 **提示**: 积分系统模块支持灵活的积分规则配置，可以根据业务需求定义不同的积分类型和使用场景，实现精细化的积分管理。 