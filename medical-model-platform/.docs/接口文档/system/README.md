# 系统配置模块 - 接口文档

## 📋 模块概述

系统配置模块提供系统配置管理、字典管理、配置审计等核心功能，支持动态配置管理和完整的操作审计。

## 🔐 接口列表

### 1. 系统配置管理

#### 1.1 分页查询配置列表

**接口描述**: 支持按配置分类和关键词筛选

**请求路径**: `GET /api/system/configs`

**请求方式**: `GET`

**权限要求**: `system:config:query`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| current | Integer | 否 | 页码，默认1 | 1 |
| size | Integer | 否 | 每页大小，默认10 | 10 |
| configCategory | String | 否 | 配置分类 | "SYSTEM" |
| configType | String | 否 | 配置类型 | "STRING" |
| keyword | String | 否 | 关键词搜索 | "数据库" |

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
    "records": [
      {
        "id": 1,
        "configKey": "database.url",
        "configValue": "jdbc:mysql://localhost:3306/medical_platform",
        "configType": "STRING",
        "configCategory": "SYSTEM",
        "description": "数据库连接地址",
        "status": 1,
        "sortOrder": 1,
        "createTime": "2025-01-15T01:20:00",
        "updateTime": "2025-01-15T01:20:00"
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
| configKey | String | 配置键，唯一标识 |
| configValue | String | 配置值 |
| configType | String | 配置类型（STRING/NUMBER/BOOLEAN/JSON） |
| configCategory | String | 配置分类 |
| description | String | 配置描述 |
| status | Integer | 配置状态（0:禁用 1:启用） |
| sortOrder | Integer | 排序 |

---

#### 1.2 根据ID获取配置

**接口描述**: 根据ID获取配置详细信息

**请求路径**: `GET /api/system/configs/{id}`

**请求方式**: `GET`

**权限要求**: `system:config:query`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| id | Long | 是 | 配置ID | 1 |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "id": 1,
    "configKey": "database.url",
    "configValue": "jdbc:mysql://localhost:3306/medical_platform",
    "configType": "STRING",
    "configCategory": "SYSTEM",
    "description": "数据库连接地址",
    "status": 1,
    "sortOrder": 1,
    "createTime": "2025-01-15T01:20:00",
    "updateTime": "2025-01-15T01:20:00"
  },
  "timestamp": 1673836800000
}
```

---

#### 1.3 创建配置

**接口描述**: 创建新的系统配置项

**请求路径**: `POST /api/system/configs`

**请求方式**: `POST`

**权限要求**: `system:config:create`

**请求参数**:
```json
{
  "configKey": "app.name",
  "configValue": "医疗影像模型管理平台",
  "configType": "STRING",
  "configCategory": "SYSTEM",
  "description": "应用名称",
  "status": 1,
  "sortOrder": 10
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| configKey | String | 是 | 配置键，唯一标识 | "app.name" |
| configValue | String | 是 | 配置值 | "医疗影像模型管理平台" |
| configType | String | 是 | 配置类型 | "STRING" |
| configCategory | String | 是 | 配置分类 | "SYSTEM" |
| description | String | 否 | 配置描述 | "应用名称" |
| status | Integer | 否 | 状态，默认1 | 1 |
| sortOrder | Integer | 否 | 排序，默认0 | 10 |

**响应结果**:
```json
{
  "code": 200,
  "message": "配置创建成功",
  "data": 123,
  "timestamp": 1673836800000
}
```

---

#### 1.4 更新配置

**接口描述**: 更新指定配置的信息

**请求路径**: `PUT /api/system/configs/{id}`

**请求方式**: `PUT`

**权限要求**: `system:config:update`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| id | Long | 是 | 配置ID | 123 |

**请求体**:
```json
{
  "configKey": "app.name",
  "configValue": "医疗影像AI平台",
  "configType": "STRING",
  "configCategory": "SYSTEM",
  "description": "应用名称（更新）",
  "status": 1,
  "sortOrder": 5
}
```

**响应结果**:
```json
{
  "code": 200,
  "message": "配置更新成功",
  "data": null,
  "timestamp": 1673836800000
}
```

---

#### 1.5 删除配置

**接口描述**: 删除指定配置项

**请求路径**: `DELETE /api/system/configs/{id}`

**请求方式**: `DELETE`

**权限要求**: `system:config:delete`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| id | Long | 是 | 配置ID | 123 |

**响应结果**:
```json
{
  "code": 200,
  "message": "配置删除成功",
  "data": null,
  "timestamp": 1673836800000
}
```

---

#### 1.6 批量删除配置

**接口描述**: 批量删除多个配置项

**请求路径**: `DELETE /api/system/configs/batch`

**请求方式**: `DELETE`

**权限要求**: `system:config:delete`

**请求参数**:
```json
[1, 2, 3, 4]
```

**响应结果**:
```json
{
  "code": 200,
  "message": "配置批量删除成功",
  "data": null,
  "timestamp": 1673836800000
}
```

---

#### 1.7 更新配置状态

**接口描述**: 启用或禁用配置项

**请求路径**: `PUT /api/system/configs/{id}/status`

**请求方式**: `PUT`

**权限要求**: `system:config:update`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| id | Long | 是 | 配置ID | 123 |
| status | Integer | 是 | 状态（0:禁用 1:启用） | 1 |

**响应结果**:
```json
{
  "code": 200,
  "message": "配置状态更新成功",
  "data": null,
  "timestamp": 1673836800000
}
```

---

#### 1.8 根据配置键获取配置

**接口描述**: 根据配置键获取配置信息

**请求路径**: `GET /api/system/configs/key/{configKey}`

**请求方式**: `GET`

**权限要求**: `system:config:query`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| configKey | String | 是 | 配置键 | "database.url" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "id": 1,
    "configKey": "database.url",
    "configValue": "jdbc:mysql://localhost:3306/medical_platform",
    "configType": "STRING",
    "configCategory": "SYSTEM",
    "description": "数据库连接地址",
    "status": 1,
    "sortOrder": 1,
    "createTime": "2025-01-15T01:20:00",
    "updateTime": "2025-01-15T01:20:00"
  },
  "timestamp": 1673836800000
}
```

---

#### 1.9 获取配置值

**接口描述**: 获取配置的原始值

**请求路径**: `GET /api/system/configs/value/{configKey}`

**请求方式**: `GET`

**权限要求**: `system:config:query`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| configKey | String | 是 | 配置键 | "database.url" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": "jdbc:mysql://localhost:3306/medical_platform",
  "timestamp": 1673836800000
}
```

---

#### 1.10 获取配置默认值

**接口描述**: 获取配置的默认值

**请求路径**: `GET /api/system/configs/value/{configKey}/default`

**请求方式**: `GET`

**权限要求**: `system:config:query`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| configKey | String | 是 | 配置键 | "database.url" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": "jdbc:mysql://localhost:3306/default_db",
  "timestamp": 1673836800000
}
```

---

#### 1.11 获取整数配置值

**接口描述**: 获取配置的整数值

**请求路径**: `GET /api/system/configs/value/{configKey}/int`

**请求方式**: `GET`

**权限要求**: `system:config:query`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| configKey | String | 是 | 配置键 | "max.connections" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": 100,
  "timestamp": 1673836800000
}
```

---

#### 1.12 获取整数配置默认值

**接口描述**: 获取配置的整数默认值

**请求路径**: `GET /api/system/configs/value/{configKey}/int/default`

**请求方式**: `GET`

**权限要求**: `system:config:query`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| configKey | String | 是 | 配置键 | "max.connections" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": 50,
  "timestamp": 1673836800000
}
```

---

#### 1.13 获取布尔配置值

**接口描述**: 获取配置的布尔值

**请求路径**: `GET /api/system/configs/value/{configKey}/boolean`

**请求方式**: `GET`

**权限要求**: `system:config:query`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| configKey | String | 是 | 配置键 | "debug.enabled" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": true,
  "timestamp": 1673836800000
}
```

---

#### 1.14 获取布尔配置默认值

**接口描述**: 获取配置的布尔默认值

**请求路径**: `GET /api/system/configs/value/{configKey}/boolean/default`

**请求方式**: `GET`

**权限要求**: `system:config:query`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| configKey | String | 是 | 配置键 | "debug.enabled" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": false,
  "timestamp": 1673836800000
}
```

---

#### 1.15 获取JSON配置值

**接口描述**: 获取配置的JSON值

**请求路径**: `GET /api/system/configs/value/{configKey}/json`

**请求方式**: `GET`

**权限要求**: `system:config:query`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| configKey | String | 是 | 配置键 | "redis.config" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "host": "localhost",
    "port": 6379,
    "database": 0
  },
  "timestamp": 1673836800000
}
```

---

#### 1.16 获取配置分类列表

**接口描述**: 获取所有配置分类

**请求路径**: `GET /api/system/configs/categories`

**请求方式**: `GET`

**权限要求**: `system:config:query`

**请求参数**: 无

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    "SYSTEM",
    "USER",
    "FILE",
    "TASK",
    "SECURITY",
    "CACHE"
  ],
  "timestamp": 1673836800000
}
```

---

#### 1.17 获取指定分类的配置

**接口描述**: 获取指定分类下的所有配置

**请求路径**: `GET /api/system/configs/categories/{category}`

**请求方式**: `GET`

**权限要求**: `system:config:query`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| category | String | 是 | 配置分类 | "SYSTEM" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 1,
      "configKey": "database.url",
      "configValue": "jdbc:mysql://localhost:3306/medical_platform",
      "configType": "STRING",
      "configCategory": "SYSTEM",
      "description": "数据库连接地址",
      "status": 1,
      "sortOrder": 1
    }
  ],
  "timestamp": 1673836800000
}
```

---

#### 1.18 批量更新配置状态

**接口描述**: 批量更新多个配置的状态

**请求路径**: `PUT /api/system/configs/batch/status`

**请求方式**: `PUT`

**权限要求**: `system:config:update`

**请求参数**:
```json
{
  "configIds": [1, 2, 3, 4],
  "status": 1
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| configIds | Array | 是 | 配置ID列表 | [1, 2, 3, 4] |
| status | Integer | 是 | 状态（0:禁用 1:启用） | 1 |

**响应结果**:
```json
{
  "code": 200,
  "message": "批量状态更新成功",
  "data": null,
  "timestamp": 1673836800000
}
```

---

#### 1.19 批量更新配置排序

**接口描述**: 批量更新多个配置的排序

**请求路径**: `PUT /api/system/configs/batch/sort`

**请求方式**: `PUT`

**权限要求**: `system:config:update`

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
| id | Long | 是 | 配置ID | 1 |
| sortOrder | Integer | 是 | 排序值 | 10 |

**响应结果**:
```json
{
  "code": 200,
  "message": "批量排序更新成功",
  "data": null,
  "timestamp": 1673836800000
}
```

---

#### 1.20 刷新配置缓存

**接口描述**: 刷新所有配置的缓存

**请求路径**: `POST /api/system/configs/cache/refresh`

**请求方式**: `POST`

**权限要求**: `system:config:cache`

**请求参数**: 无

**响应结果**:
```json
{
  "code": 200,
  "message": "配置缓存刷新成功",
  "data": null,
  "timestamp": 1673836800000
}
```

---

#### 1.21 清除配置缓存

**接口描述**: 清除所有配置的缓存

**请求路径**: `DELETE /api/system/configs/cache`

**请求方式**: `DELETE`

**权限要求**: `system:config:cache`

**请求参数**: 无

**响应结果**:
```json
{
  "code": 200,
  "message": "配置缓存清除成功",
  "data": null,
  "timestamp": 1673836800000
}
```

---

### 2. 系统字典管理

#### 2.1 分页查询字典列表

**接口描述**: 支持按字典类型和关键词筛选

**请求路径**: `GET /api/system/dicts`

**请求方式**: `GET`

**权限要求**: `system:dict:query`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| current | Integer | 否 | 页码，默认1 | 1 |
| size | Integer | 否 | 每页大小，默认10 | 10 |
| dictType | String | 否 | 字典类型 | "USER_STATUS" |
| keyword | String | 否 | 关键词搜索 | "状态" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "records": [
      {
        "id": 1,
        "dictCode": "USER_STATUS",
        "dictName": "用户状态",
        "dictValue": "1",
        "dictLabel": "启用",
        "dictType": "USER_STATUS",
        "description": "用户账户状态",
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

#### 2.2 获取字典树形结构

**接口描述**: 获取字典的树形结构

**请求路径**: `GET /api/system/dicts/tree`

**请求方式**: `GET`

**权限要求**: `system:dict:query`

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 1,
      "dictCode": "SYSTEM_CONFIG",
      "dictName": "系统配置",
      "dictType": "CATEGORY",
      "children": [
        {
          "id": 2,
          "dictCode": "DATABASE_CONFIG",
          "dictName": "数据库配置",
          "dictType": "CATEGORY",
          "children": []
        }
      ]
    }
  ],
  "timestamp": 1673836800000
}
```

---

#### 2.3 根据字典类型获取数据

**接口描述**: 根据字典类型获取字典数据列表

**请求路径**: `GET /api/system/dicts/type/{dictType}`

**请求方式**: `GET`

**权限要求**: 无需认证

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| dictType | String | 是 | 字典类型 | "USER_STATUS" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "dictValue": "1",
      "dictLabel": "启用",
      "status": 1,
      "sortOrder": 1
    },
    {
      "dictValue": "0",
      "dictLabel": "禁用",
      "status": 1,
      "sortOrder": 2
    }
  ],
  "timestamp": 1673836800000
}
```

---

#### 2.4 创建字典

**接口描述**: 创建新的字典项

**请求路径**: `POST /api/system/dicts`

**请求方式**: `POST`

**权限要求**: `system:dict:create`

**请求参数**:
```json
{
  "dictCode": "NEW_STATUS",
  "dictName": "新状态",
  "dictValue": "NEW",
  "dictLabel": "新状态标签",
  "dictType": "STATUS",
  "description": "新状态描述",
  "status": 1,
  "sortOrder": 10
}
```

**响应结果**:
```json
{
  "code": 200,
  "message": "字典创建成功",
  "data": 123,
  "timestamp": 1673836800000
}
```

---

#### 2.5 更新字典

**接口描述**: 更新指定字典的信息

**请求路径**: `PUT /api/system/dicts/{id}`

**请求方式**: `PUT`

**权限要求**: `system:dict:update`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| id | Long | 是 | 字典ID | 123 |

**响应结果**:
```json
{
  "code": 200,
  "message": "字典更新成功",
  "data": null,
  "timestamp": 1673836800000
}
```

---

#### 2.6 删除字典

**接口描述**: 删除指定字典项

**请求路径**: `DELETE /api/system/dicts/{id}`

**请求方式**: `DELETE`

**权限要求**: `system:dict:delete`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| id | Long | 是 | 字典ID | 123 |

**响应结果**:
```json
{
  "code": 200,
  "message": "字典删除成功",
  "data": null,
  "timestamp": 1673836800000
}
```

---

#### 2.7 获取字典分类

**接口描述**: 获取指定字典的分类信息

**请求路径**: `GET /api/system/dicts/category/{dictCode}`

**请求方式**: `GET`

**权限要求**: `system:dict:query`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| dictCode | String | 是 | 字典代码 | "USER_STATUS" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "dictCode": "USER_STATUS",
    "dictName": "用户状态",
    "dictType": "CATEGORY",
    "description": "用户账户状态分类"
  },
  "timestamp": 1673836800000
}
```

---

#### 2.8 获取字典项列表

**接口描述**: 获取指定字典下的所有字典项

**请求路径**: `GET /api/system/dicts/items/{dictCode}`

**请求方式**: `GET`

**权限要求**: `system:dict:query`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| dictCode | String | 是 | 字典代码 | "USER_STATUS" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "dictValue": "1",
      "dictLabel": "启用",
      "status": 1,
      "sortOrder": 1
    },
    {
      "dictValue": "0",
      "dictLabel": "禁用",
      "status": 1,
      "sortOrder": 2
    }
  ],
  "timestamp": 1673836800000
}
```

---

#### 2.9 获取字典树

**接口描述**: 获取指定字典的树形结构

**请求路径**: `GET /api/system/dicts/tree/{dictCode}`

**请求方式**: `GET`

**权限要求**: `system:dict:query`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| dictCode | String | 是 | 字典代码 | "SYSTEM_CONFIG" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "dictCode": "SYSTEM_CONFIG",
      "dictName": "系统配置",
      "dictType": "CATEGORY",
      "children": [
        {
          "dictCode": "DATABASE_CONFIG",
          "dictName": "数据库配置",
          "dictType": "CATEGORY",
          "children": []
        }
      ]
    }
  ],
  "timestamp": 1673836800000
}
```

---

#### 2.10 根据名称获取字典

**接口描述**: 根据字典名称获取字典信息

**请求路径**: `GET /api/system/dicts/name/{dictCode}`

**请求方式**: `GET`

**权限要求**: `system:dict:query`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| dictCode | String | 是 | 字典代码 | "USER_STATUS" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "dictCode": "USER_STATUS",
    "dictName": "用户状态",
    "dictType": "STATUS",
    "description": "用户账户状态"
  },
  "timestamp": 1673836800000
}
```

---

#### 2.11 根据标签获取字典

**接口描述**: 根据字典标签获取字典信息

**请求路径**: `GET /api/system/dicts/label/{dictCode}`

**请求方式**: `GET`

**权限要求**: `system:dict:query`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| dictCode | String | 是 | 字典代码 | "USER_STATUS" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "dictValue": "1",
      "dictLabel": "启用",
      "status": 1,
      "sortOrder": 1
    }
  ],
  "timestamp": 1673836800000
}
```

---

#### 2.12 获取字典映射

**接口描述**: 获取字典的值标签映射关系

**请求路径**: `GET /api/system/dicts/mapping/{dictCode}`

**请求方式**: `GET`

**权限要求**: `system:dict:query`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| dictCode | String | 是 | 字典代码 | "USER_STATUS" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "1": "启用",
    "0": "禁用"
  },
  "timestamp": 1673836800000
}
```

---

#### 2.13 获取字典反向映射

**接口描述**: 获取字典的标签值反向映射关系

**请求路径**: `GET /api/system/dicts/reverse-mapping/{dictCode}`

**请求方式**: `GET`

**权限要求**: `system:dict:query`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| dictCode | String | 是 | 字典代码 | "USER_STATUS" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "启用": "1",
    "禁用": "0"
  },
  "timestamp": 1673836800000
}
```

---

#### 2.14 批量创建字典

**接口描述**: 批量创建多个字典项

**请求路径**: `POST /api/system/dicts/batch`

**请求方式**: `POST`

**权限要求**: `system:dict:create`

**请求参数**:
```json
[
  {
    "dictCode": "NEW_STATUS_1",
    "dictName": "新状态1",
    "dictValue": "NEW1",
    "dictLabel": "新状态标签1",
    "dictType": "STATUS",
    "description": "新状态描述1"
  },
  {
    "dictCode": "NEW_STATUS_2",
    "dictName": "新状态2",
    "dictValue": "NEW2",
    "dictLabel": "新状态标签2",
    "dictType": "STATUS",
    "description": "新状态描述2"
  }
]
```

**响应结果**:
```json
{
  "code": 200,
  "message": "批量创建成功",
  "data": [123, 124],
  "timestamp": 1673836800000
}
```

---

#### 2.15 批量更新字典状态

**接口描述**: 批量更新多个字典项的状态

**请求路径**: `PUT /api/system/dicts/batch/status`

**请求方式**: `PUT`

**权限要求**: `system:dict:update`

**请求参数**:
```json
{
  "dictIds": [1, 2, 3, 4],
  "status": 1
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| dictIds | Array | 是 | 字典ID列表 | [1, 2, 3, 4] |
| status | Integer | 是 | 状态（0:禁用 1:启用） | 1 |

**响应结果**:
```json
{
  "code": 200,
  "message": "批量状态更新成功",
  "data": null,
  "timestamp": 1673836800000
}
```

---

#### 2.16 批量更新字典排序

**接口描述**: 批量更新多个字典项的排序

**请求路径**: `PUT /api/system/dicts/batch/sort`

**请求方式**: `PUT`

**权限要求**: `system:dict:update`

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
| id | Long | 是 | 字典ID | 1 |
| sortOrder | Integer | 是 | 排序值 | 10 |

**响应结果**:
```json
{
  "code": 200,
  "message": "批量排序更新成功",
  "data": null,
  "timestamp": 1673836800000
}
```

---

#### 2.17 获取模块列表

**接口描述**: 获取所有模块列表

**请求路径**: `GET /api/system/dicts/modules`

**请求方式**: `GET`

**权限要求**: `system:dict:query`

**请求参数**: 无

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    "USER",
    "SYSTEM",
    "CREDIT",
    "PERMISSION"
  ],
  "timestamp": 1673836800000
}
```

---

#### 2.18 获取模块分类

**接口描述**: 获取指定模块下的所有分类

**请求路径**: `GET /api/system/dicts/modules/{module}/categories`

**请求方式**: `GET`

**权限要求**: `system:dict:query`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| module | String | 是 | 模块名称 | "USER" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    "USER_STATUS",
    "USER_ROLE",
    "USER_TYPE"
  ],
  "timestamp": 1673836800000
}
```

---

#### 2.19 刷新字典缓存

**接口描述**: 刷新所有字典的缓存

**请求路径**: `POST /api/system/dicts/cache/refresh`

**请求方式**: `POST`

**权限要求**: `system:dict:cache`

**请求参数**: 无

**响应结果**:
```json
{
  "code": 200,
  "message": "字典缓存刷新成功",
  "data": null,
  "timestamp": 1673836800000
}
```

---

#### 2.20 清除字典缓存

**接口描述**: 清除所有字典的缓存

**请求路径**: `DELETE /api/system/dicts/cache`

**请求方式**: `DELETE`

**权限要求**: `system:dict:cache`

**请求参数**: 无

**响应结果**:
```json
{
  "code": 200,
  "message": "字典缓存清除成功",
  "data": null,
  "timestamp": 1673836800000
}
```

---

### 3. 配置审计

#### 3.1 分页查询配置变更日志

**接口描述**: 查询配置变更的历史记录

**请求路径**: `GET /api/system/config-audit/logs`

**请求方式**: `GET`

**权限要求**: `system:config:audit`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| current | Integer | 否 | 页码，默认1 | 1 |
| size | Integer | 否 | 每页大小，默认10 | 10 |
| configKey | String | 否 | 配置键 | "database.url" |
| operationType | String | 否 | 操作类型 | "UPDATE" |
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
        "id": 1,
        "configKey": "database.url",
        "operationType": "UPDATE",
        "oldValue": "jdbc:mysql://localhost:3306/old_db",
        "newValue": "jdbc:mysql://localhost:3306/new_db",
        "operatorId": 1234567890,
        "operatorName": "admin",
        "operationTime": "2025-01-15T01:20:00",
        "remark": "数据库迁移"
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

#### 3.2 根据配置ID查询日志

**接口描述**: 根据配置ID查询变更日志

**请求路径**: `GET /api/system/config-audit/logs/config/{configId}`

**请求方式**: `GET`

**权限要求**: `system:config:audit`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| configId | Long | 是 | 配置ID | 123 |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 1,
      "configKey": "database.url",
      "operationType": "UPDATE",
      "oldValue": "jdbc:mysql://localhost:3306/old_db",
      "newValue": "jdbc:mysql://localhost:3306/new_db",
      "operatorId": 1234567890,
      "operatorName": "admin",
      "operationTime": "2025-01-15T01:20:00",
      "remark": "数据库迁移"
    }
  ],
  "timestamp": 1673836800000
}
```

---

#### 3.3 根据配置键查询日志

**接口描述**: 根据配置键查询变更日志

**请求路径**: `GET /api/system/config-audit/logs/key/{configKey}`

**请求方式**: `GET`

**权限要求**: `system:config:audit`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| configKey | String | 是 | 配置键 | "database.url" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 1,
      "configKey": "database.url",
      "operationType": "UPDATE",
      "oldValue": "jdbc:mysql://localhost:3306/old_db",
      "newValue": "jdbc:mysql://localhost:3306/new_db",
      "operatorId": 1234567890,
      "operatorName": "admin",
      "operationTime": "2025-01-15T01:20:00",
      "remark": "数据库迁移"
    }
  ],
  "timestamp": 1673836800000
}
```

---

#### 3.4 获取配置变更统计

**接口描述**: 获取配置变更的统计信息

**请求路径**: `GET /api/system/config-audit/statistics`

**请求方式**: `GET`

**权限要求**: `system:config:audit`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| startTime | String | 否 | 开始时间 | "2025-01-01" |
| endTime | String | 否 | 结束时间 | "2025-01-15" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "totalChanges": 150,
    "createCount": 20,
    "updateCount": 100,
    "deleteCount": 30,
    "topOperators": [
      {
        "operatorName": "admin",
        "changeCount": 80
      }
    ],
    "topConfigs": [
      {
        "configKey": "database.url",
        "changeCount": 15
      }
    ]
  },
  "timestamp": 1673836800000
}
```

---

#### 3.5 获取配置变更趋势

**接口描述**: 获取配置变更的趋势分析

**请求路径**: `GET /api/system/config-audit/trend`

**请求方式**: `GET`

**权限要求**: `system:config:audit`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| startTime | String | 否 | 开始时间 | "2025-01-01" |
| endTime | String | 否 | 结束时间 | "2025-01-15" |
| interval | String | 否 | 时间间隔（DAY/WEEK/MONTH） | "DAY" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "date": "2025-01-15",
      "changeCount": 15,
      "createCount": 2,
      "updateCount": 12,
      "deleteCount": 1
    }
  ],
  "timestamp": 1673836800000
}
```

---

#### 3.6 获取配置变更影响分析

**接口描述**: 获取配置变更的影响分析

**请求路径**: `GET /api/system/config-audit/impact-analysis`

**请求方式**: `GET`

**权限要求**: `system:config:audit`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| configKey | String | 否 | 配置键 | "database.url" |
| startTime | String | 否 | 开始时间 | "2025-01-01" |
| endTime | String | 否 | 结束时间 | "2025-01-15" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "configKey": "database.url",
    "changeFrequency": 15,
    "impactLevel": "HIGH",
    "affectedServices": ["user-service", "order-service"],
    "riskAssessment": "中等风险"
  },
  "timestamp": 1673836800000
}
```

---

#### 3.7 获取回滚建议

**接口描述**: 获取配置回滚的建议

**请求路径**: `GET /api/system/config-audit/rollback-recommendations/{configKey}`

**请求方式**: `GET`

**权限要求**: `system:config:audit`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| configKey | String | 是 | 配置键 | "database.url" |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "configKey": "database.url",
    "recommendRollback": true,
    "reason": "配置变更后系统性能下降",
    "suggestedValue": "jdbc:mysql://localhost:3306/old_db",
    "rollbackSteps": [
      "备份当前配置",
      "恢复到上一个稳定版本",
      "重启相关服务"
    ]
  },
  "timestamp": 1673836800000
}
```

---

#### 3.8 清理审计日志

**接口描述**: 清理过期的审计日志

**请求路径**: `DELETE /api/system/config-audit/logs/cleanup`

**请求方式**: `DELETE`

**权限要求**: `system:config:audit`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| beforeDate | String | 是 | 清理此日期之前的日志 | "2024-01-01" |
| dryRun | Boolean | 否 | 是否仅预览，默认false | false |

**响应结果**:
```json
{
  "code": 200,
  "message": "日志清理成功",
  "data": {
    "deletedCount": 1000,
    "freedSpace": "50MB",
    "affectedConfigs": ["database.url", "redis.host"]
  },
  "timestamp": 1673836800000
}
```

---

#### 3.9 导出审计日志

**接口描述**: 导出配置变更审计日志

**请求路径**: `POST /api/system/config-audit/logs/export`

**请求方式**: `POST`

**权限要求**: `system:config:audit`

**请求参数**:
```json
{
  "startTime": "2025-01-01",
  "endTime": "2025-01-15",
  "configKeys": ["database.url", "redis.host"],
  "operationTypes": ["CREATE", "UPDATE", "DELETE"],
  "exportFormat": "EXCEL"
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| startTime | String | 否 | 开始时间 | "2025-01-01" |
| endTime | String | 否 | 结束时间 | "2025-01-15" |
| configKeys | Array | 否 | 配置键列表 | ["database.url"] |
| operationTypes | Array | 否 | 操作类型列表 | ["UPDATE"] |
| exportFormat | String | 否 | 导出格式（EXCEL/CSV） | "EXCEL" |

**响应结果**:
```json
{
  "code": 200,
  "message": "导出成功",
  "data": {
    "downloadUrl": "https://example.com/downloads/audit_logs_20250115.xlsx",
    "fileSize": "2.5MB",
    "recordCount": 150
  },
  "timestamp": 1673836800000
}
```

**接口描述**: 查询配置变更的历史记录

**请求路径**: `GET /api/system/configs/audit/logs`

**请求方式**: `GET`

**权限要求**: `system:config:audit`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| current | Integer | 否 | 页码，默认1 | 1 |
| size | Integer | 否 | 每页大小，默认10 | 10 |
| configKey | String | 否 | 配置键 | "database.url" |
| operationType | String | 否 | 操作类型 | "UPDATE" |
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
        "id": 1,
        "configKey": "database.url",
        "operationType": "UPDATE",
        "oldValue": "jdbc:mysql://localhost:3306/old_db",
        "newValue": "jdbc:mysql://localhost:3306/new_db",
        "operatorId": 1234567890,
        "operatorName": "admin",
        "operationTime": "2025-01-15T01:20:00",
        "remark": "数据库迁移"
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



## 🔒 权限控制

### 配置管理权限
- **system:config:query**: 查询配置
- **system:config:create**: 创建配置
- **system:config:update**: 更新配置
- **system:config:delete**: 删除配置
- **system:config:audit**: 审计配置

### 字典管理权限
- **system:dict:query**: 查询字典
- **system:dict:create**: 创建字典
- **system:dict:update**: 更新字典
- **system:dict:delete**: 删除字典

## 📝 错误码说明

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 4001 | 参数验证失败 | 检查请求参数格式和必填项 |
| 4002 | 配置不存在 | 检查配置ID是否正确 |
| 4003 | 配置键已存在 | 使用其他配置键 |
| 4004 | 字典代码已存在 | 使用其他字典代码 |
| 4005 | 权限不足 | 检查用户权限 |
| 4006 | 配置正在使用 | 先禁用配置再删除 |

## 🚀 使用示例

### 查询配置列表
```bash
curl -X GET "http://localhost:8080/api/system/configs?configCategory=SYSTEM&keyword=数据库" \
  -H "Authorization: Bearer {token}"
```

### 创建配置
```bash
curl -X POST "http://localhost:8080/api/system/configs" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "configKey": "app.version",
    "configValue": "1.0.0",
    "configType": "STRING",
    "configCategory": "SYSTEM",
    "description": "应用版本号"
  }'
```

### 更新配置
```bash
curl -X PUT "http://localhost:8080/api/system/configs/123" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "configValue": "1.1.0",
    "description": "应用版本号（更新）"
  }'
```

### 查询字典数据
```bash
curl -X GET "http://localhost:8080/api/system/dicts/type/USER_STATUS" \
  -H "Authorization: Bearer {token}"
```

### 查询配置审计日志
```bash
curl -X GET "http://localhost:8080/api/system/configs/audit/logs?configKey=database.url&operationType=UPDATE" \
  -H "Authorization: Bearer {token}"
```

## 📊 业务规则

### 配置管理
- 配置键必须唯一
- 配置支持多种数据类型
- 配置变更自动记录审计日志
- 支持配置的批量操作

### 字典管理
- 字典支持树形结构
- 字典代码必须唯一
- 字典数据支持排序和状态控制
- 支持字典的层级管理

### 审计功能
- 所有配置变更自动记录
- 支持变更历史查询
- 提供变更统计信息
- 记录操作人和操作时间

---

> 💡 **提示**: 系统配置模块支持动态配置管理，可以在运行时修改系统行为，无需重启应用。所有配置变更都会记录审计日志，确保操作可追溯。 