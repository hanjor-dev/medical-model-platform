# 权限管理模块 - 接口文档

## 📋 模块概述

权限管理模块提供权限查询、权限配置、角色权限管理等核心功能，支持基于角色的访问控制（RBAC）和动态权限配置。

## 🔐 接口列表

### 1. 获取用户权限

**接口描述**: 获取当前用户的角色和权限列表

**请求路径**: `GET /permissions/user`

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
    "username": "zhang123",
    "role": "USER",
    "permissions": [
      "user:profile:read",
      "user:profile:update",
      "credit:balance:read",
      "credit:transaction:read"
    ],
    "rolePermissions": [
      {
        "permissionCode": "user:profile:read",
        "permissionName": "查看用户信息",
        "permissionType": "MENU"
      }
    ]
  },
  "timestamp": 1673836800000
}
```

**响应字段说明**:
| 字段名 | 类型 | 说明 |
|--------|------|------|
| userId | Long | 用户ID |
| username | String | 用户名 |
| role | String | 用户角色 |
| permissions | Array | 权限代码列表 |
| rolePermissions | Array | 权限详细信息列表 |

---

### 2. 获取菜单权限

**接口描述**: 根据用户权限返回可访问的菜单树

**请求路径**: `GET /permissions/menus`

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
  "data": [
    {
      "id": 1,
      "menuName": "用户中心",
      "menuCode": "user:center",
      "menuType": "MENU",
      "menuPath": "/user",
      "menuIcon": "user",
      "sortOrder": 1,
      "status": 1,
      "children": [
        {
          "id": 2,
          "menuName": "个人信息",
          "menuCode": "user:profile",
          "menuType": "MENU",
          "menuPath": "/user/profile",
          "menuIcon": "profile",
          "sortOrder": 1,
          "status": 1
        }
      ]
    }
  ],
  "timestamp": 1673836800000
}
```

**响应字段说明**:
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 菜单ID |
| menuName | String | 菜单名称 |
| menuCode | String | 菜单代码 |
| menuType | String | 菜单类型（MENU/BUTTON） |
| menuPath | String | 菜单路径 |
| menuIcon | String | 菜单图标 |
| sortOrder | Integer | 排序 |
| status | Integer | 状态（0:禁用 1:启用） |
| children | Array | 子菜单列表 |

---

### 3. 获取权限树

**接口描述**: 获取完整的权限树形结构，仅超级管理员可访问

**请求路径**: `GET /permissions/tree`

**请求方式**: `GET`

**权限要求**: SUPER_ADMIN权限

**请求参数**: 无

**请求头**:
```
Authorization: Bearer {super_admin_token}
```

**响应结果**:
```json
{
  "code": 200,
  "message": "获取权限树成功",
  "data": [
    {
      "id": 1,
      "permissionCode": "system:manage",
      "permissionName": "系统管理",
      "permissionType": "MENU",
      "status": 1,
      "sortOrder": 1,
      "children": [
        {
          "id": 2,
          "permissionCode": "system:config:manage",
          "permissionName": "配置管理",
          "permissionType": "MENU",
          "status": 1,
          "sortOrder": 1
        }
      ]
    }
  ],
  "timestamp": 1673836800000
}
```

---

### 4. 获取角色权限树

**接口描述**: 获取指定角色的权限树，标记已分配权限

**请求路径**: `GET /permissions/role/{role}/tree`

**请求方式**: `GET`

**权限要求**: SUPER_ADMIN权限

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| role | String | 是 | 角色名称 | "ADMIN" |

**请求头**:
```
Authorization: Bearer {super_admin_token}
```

**响应结果**:
```json
{
  "code": 200,
  "message": "获取角色权限树成功",
  "data": [
    {
      "id": 1,
      "permissionCode": "system:manage",
      "permissionName": "系统管理",
      "permissionType": "MENU",
      "status": 1,
      "sortOrder": 1,
      "isAssigned": true,
      "children": [
        {
          "id": 2,
          "permissionCode": "system:config:manage",
          "permissionName": "配置管理",
          "permissionType": "MENU",
          "status": 1,
          "sortOrder": 1,
          "isAssigned": false
        }
      ]
    }
  ],
  "timestamp": 1673836800000
}
```

**响应字段说明**:
| 字段名 | 类型 | 说明 |
|--------|------|------|
| isAssigned | Boolean | 是否已分配给该角色 |

---

### 5. 分页查询权限

**接口描述**: 支持按权限类型和关键词筛选

**请求路径**: `GET /permissions`

**请求方式**: `GET`

**权限要求**: SUPER_ADMIN权限

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| current | Integer | 否 | 页码，默认1 | 1 |
| size | Integer | 否 | 每页大小，默认10 | 10 |
| permissionType | String | 否 | 权限类型 | "MENU" |
| keyword | String | 否 | 关键词搜索 | "系统" |

**请求头**:
```
Authorization: Bearer {super_admin_token}
```

**响应结果**:
```json
{
  "code": 200,
  "message": "查询权限成功",
  "data": {
    "records": [
      {
        "id": 1,
        "permissionCode": "system:manage",
        "permissionName": "系统管理",
        "permissionType": "MENU",
        "permissionCategory": "SYSTEM",
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

### 6. 创建权限

**接口描述**: 创建新的权限项

**请求路径**: `POST /permissions`

**请求方式**: `POST`

**权限要求**: SUPER_ADMIN权限

**请求参数**:
```json
{
  "permissionCode": "system:new:permission",
  "permissionName": "新权限",
  "permissionType": "MENU",
  "permissionCategory": "SYSTEM",
  "description": "新权限描述",
  "status": 1,
  "sortOrder": 10
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| permissionCode | String | 是 | 权限代码，唯一 | "system:new:permission" |
| permissionName | String | 是 | 权限名称 | "新权限" |
| permissionType | String | 是 | 权限类型 | "MENU" |
| permissionCategory | String | 是 | 权限分类 | "SYSTEM" |
| description | String | 否 | 权限描述 | "新权限描述" |
| status | Integer | 否 | 状态，默认1 | 1 |
| sortOrder | Integer | 否 | 排序，默认0 | 10 |

**响应结果**:
```json
{
  "code": 200,
  "message": "权限创建成功",
  "data": 123,
  "timestamp": 1673836800000
}
```

**响应字段说明**:
| 字段名 | 类型 | 说明 |
|--------|------|------|
| data | Long | 新创建的权限ID |

---

### 7. 更新权限

**接口描述**: 更新指定权限的信息

**请求路径**: `PUT /permissions/{id}`

**请求方式**: `PUT`

**权限要求**: SUPER_ADMIN权限

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| id | Long | 是 | 权限ID | 123 |

**请求体**:
```json
{
  "permissionCode": "system:updated:permission",
  "permissionName": "更新后的权限",
  "permissionType": "MENU",
  "permissionCategory": "SYSTEM",
  "description": "更新后的权限描述",
  "status": 1,
  "sortOrder": 5
}
```

**响应结果**:
```json
{
  "code": 200,
  "message": "权限更新成功",
  "data": null,
  "timestamp": 1673836800000
}
```

---

### 8. 删除权限

**接口描述**: 删除指定权限（不能有子权限）

**请求路径**: `DELETE /permissions/{id}`

**请求方式**: `DELETE`

**权限要求**: SUPER_ADMIN权限

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| id | Long | 是 | 权限ID | 123 |

**请求头**:
```
Authorization: Bearer {super_admin_token}
```

**响应结果**:
```json
{
  "code": 200,
  "message": "权限删除成功",
  "data": null,
  "timestamp": 1673836800000
}
```

---

### 9. 配置角色权限

**接口描述**: 为指定角色分配权限

**请求路径**: `PUT /permissions/role`

**请求方式**: `PUT`

**权限要求**: SUPER_ADMIN权限

**请求参数**:
```json
{
  "role": "ADMIN",
  "permissionIds": [1, 2, 3, 4]
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| role | String | 是 | 角色名称 | "ADMIN" |
| permissionIds | Array | 是 | 权限ID列表 | [1, 2, 3, 4] |

**响应结果**:
```json
{
  "code": 200,
  "message": "角色权限配置成功",
  "data": null,
  "timestamp": 1673836800000
}
```

---

### 10. 刷新权限缓存

**接口描述**: 刷新所有用户的权限缓存

**请求路径**: `POST /api/permissions/cache/refresh`

**请求方式**: `POST`

**权限要求**: SUPER_ADMIN权限

**请求参数**: 无

**请求头**:
```
Authorization: Bearer {super_admin_token}
```

**响应结果**:
```json
{
  "code": 200,
  "message": "权限缓存刷新成功",
  "data": null,
  "timestamp": 1673836800000
}
```

## 🔒 权限控制

### 权限类型
- **MENU**: 菜单权限，控制页面访问
- **BUTTON**: 按钮权限，控制功能操作
- **API**: 接口权限，控制API调用

### 权限分类
- **SYSTEM**: 系统权限
- **USER**: 用户权限
- **FILE**: 文件权限
- **TASK**: 任务权限
- **SECURITY**: 安全权限
- **CACHE**: 缓存权限

### 角色权限
- **USER**: 基础用户权限
- **ADMIN**: 管理员权限
- **SUPER_ADMIN**: 超级管理员权限

## 📝 错误码说明

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 4001 | 参数验证失败 | 检查请求参数格式和必填项 |
| 4002 | 权限不存在 | 检查权限ID是否正确 |
| 4003 | 权限代码已存在 | 使用其他权限代码 |
| 4004 | 权限有子权限 | 先删除子权限 |
| 4005 | 权限已被分配 | 先取消权限分配 |
| 4006 | 权限不足 | 检查用户角色和权限 |

## 🚀 使用示例

### 获取用户权限
```bash
curl -X GET "http://localhost:8080/permissions/user" \
  -H "Authorization: Bearer {token}"
```

### 获取菜单权限
```bash
curl -X GET "http://localhost:8080/permissions/menus" \
  -H "Authorization: Bearer {token}"
```

### 创建权限（超级管理员）
```bash
curl -X POST "http://localhost:8080/permissions" \
  -H "Authorization: Bearer {super_admin_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "permissionCode": "system:new:permission",
    "permissionName": "新权限",
    "permissionType": "MENU",
    "permissionCategory": "SYSTEM",
    "description": "新权限描述"
  }'
```

### 配置角色权限（超级管理员）
```bash
curl -X PUT "http://localhost:8080/permissions/role" \
  -H "Authorization: Bearer {super_admin_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "role": "ADMIN",
    "permissionIds": [1, 2, 3, 4]
  }'
```

## 📊 业务规则

### 权限管理
- 权限代码必须唯一
- 权限支持树形结构
- 删除权限前需检查依赖关系

### 角色权限
- 角色权限可以动态配置
- 权限变更后需要刷新缓存
- 支持权限的批量分配

### 权限验证
- 前端根据菜单权限控制页面显示
- 后端根据API权限控制接口访问
- 支持细粒度的权限控制

---

> 💡 **提示**: 权限管理模块支持灵活的权限配置，可以根据业务需求动态调整用户权限，实现精细化的访问控制。 