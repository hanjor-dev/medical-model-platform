# 用户管理模块 - 接口文档

## 📋 模块概述

用户管理模块提供用户信息管理、密码修改、子账号管理等核心功能，支持多级用户管理架构。

## 🔐 接口列表

### 1. 获取用户信息

**接口描述**: 获取当前登录用户的详细信息

**请求路径**: `GET /user/profile`

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
    "id": 1234567890,
    "username": "zhang123",
    "email": "zhang123@example.com",
    "phone": "13800138000",
    "nickname": "张三",
    "avatar": "https://example.com/avatar.jpg",
    "status": 1,
    "role": "USER",
    "parentUserId": null,
    "referralCode": "XYZ98765",
    "referrerUserId": 9876543210,
    "lastLoginTime": "2025-01-15T01:20:00",
    "lastLoginIp": "192.168.1.100",
    "createTime": "2025-01-15T01:20:00",
    "updateTime": "2025-01-15T01:20:00"
  },
  "timestamp": 1673836800000
}
```

**响应字段说明**:
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 用户ID |
| username | String | 用户名 |
| email | String | 邮箱地址 |
| phone | String | 手机号 |
| nickname | String | 昵称 |
| avatar | String | 头像URL |
| status | Integer | 账户状态（0:禁用 1:启用） |
| role | String | 用户角色 |
| parentUserId | Long | 上级用户ID |
| referralCode | String | 个人推荐码 |
| referrerUserId | Long | 推荐人用户ID |
| lastLoginTime | String | 最后登录时间 |
| lastLoginIp | String | 最后登录IP |
| createTime | String | 创建时间 |
| updateTime | String | 更新时间 |

---

### 2. 修改用户信息

**接口描述**: 修改当前用户的个人信息

**请求路径**: `PUT /user/profile`

**请求方式**: `PUT`

**权限要求**: 需要认证

**请求参数**:
```json
{
  "email": "zhang123@example.com",
  "phone": "13800138000",
  "nickname": "张三",
  "avatar": "https://example.com/avatar.jpg"
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| email | String | 否 | 邮箱地址，唯一 | "zhang123@example.com" |
| phone | String | 否 | 手机号 | "13800138000" |
| nickname | String | 否 | 昵称，1-50个字符 | "张三" |
| avatar | String | 否 | 头像URL | "https://example.com/avatar.jpg" |

**响应结果**:
```json
{
  "code": 200,
  "message": "修改成功",
  "data": null,
  "timestamp": 1673836800000
}
```

---

### 3. 修改密码

**接口描述**: 修改当前用户的登录密码

**请求路径**: `PUT /user/password`

**请求方式**: `PUT`

**权限要求**: 需要认证

**请求参数**:
```json
{
  "oldPassword": "abc123456",
  "newPassword": "newpass123",
  "confirmPassword": "newpass123"
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| oldPassword | String | 是 | 原密码 | "abc123456" |
| newPassword | String | 是 | 新密码，6-20个字符 | "newpass123" |
| confirmPassword | String | 是 | 确认新密码 | "newpass123" |

**响应结果**:
```json
{
  "code": 200,
  "message": "密码修改成功",
  "data": null,
  "timestamp": 1673836800000
}
```

---

### 4. 获取子账号列表

**接口描述**: 管理员查看自己管理的子账号列表

**请求路径**: `GET /user/children`

**请求方式**: `GET`

**权限要求**: ADMIN权限

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| page | Integer | 否 | 页码，默认1 | 1 |
| size | Integer | 否 | 每页大小，默认10 | 10 |

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
        "id": 1234567891,
        "username": "subuser1",
        "email": "subuser1@example.com",
        "nickname": "子用户1",
        "status": 1,
        "role": "USER",
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
| records | Array | 子账号列表 |
| total | Long | 总记录数 |
| size | Integer | 每页大小 |
| current | Integer | 当前页码 |
| pages | Integer | 总页数 |

---

### 5. 创建子账号

**接口描述**: 管理员为自己创建子账号

**请求路径**: `POST /user/children`

**请求方式**: `POST`

**权限要求**: ADMIN权限

**请求参数**:
```json
{
  "username": "subuser1",
  "email": "subuser1@example.com",
  "password": "abc123456",
  "nickname": "子用户1"
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| username | String | 是 | 用户名，3-20个字符，唯一 | "subuser1" |
| email | String | 否 | 邮箱地址，唯一 | "subuser1@example.com" |
| password | String | 是 | 密码，6-20个字符 | "abc123456" |
| nickname | String | 否 | 昵称，1-50个字符 | "子用户1" |

**响应结果**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "userId": 1234567891,
    "username": "subuser1",
    "email": "subuser1@example.com",
    "nickname": "子用户1",
    "referralCode": "DEF45678",
    "createTime": "2025-01-15T01:20:00"
  },
  "timestamp": 1673836800000
}
```

## 🔒 权限控制

### 用户角色
- **USER**: 普通用户，只能管理自己的信息
- **ADMIN**: 管理员，可以管理子账号
- **SUPER_ADMIN**: 超级管理员，可以管理所有用户

### 权限范围
- 普通用户只能查看和修改自己的信息
- 管理员可以创建和管理子账号
- 超级管理员可以管理所有用户和角色

## 📝 错误码说明

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 4001 | 参数验证失败 | 检查请求参数格式和必填项 |
| 4002 | 用户不存在 | 检查用户ID是否正确 |
| 4003 | 原密码错误 | 检查原密码是否正确 |
| 4004 | 新密码确认不匹配 | 确保新密码和确认密码一致 |
| 4005 | 权限不足 | 检查用户角色和权限 |
| 4006 | 邮箱已存在 | 使用其他邮箱地址 |
| 4007 | 用户名已存在 | 选择其他用户名 |

## 🚀 使用示例

### 获取用户信息
```bash
curl -X GET "http://localhost:8080/user/profile" \
  -H "Authorization: Bearer {token}"
```

### 修改用户信息
```bash
curl -X PUT "http://localhost:8080/user/profile" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "nickname": "新昵称",
    "phone": "13900139000"
  }'
```

### 修改密码
```bash
curl -X PUT "http://localhost:8080/user/password" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "oldPassword": "abc123456",
    "newPassword": "newpass123",
    "confirmPassword": "newpass123"
  }'
```

### 创建子账号（管理员）
```bash
curl -X POST "http://localhost:8080/user/children" \
  -H "Authorization: Bearer {admin_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "subuser1",
    "email": "subuser1@example.md",
    "password": "abc123456",
    "nickname": "子用户1"
  }'
```

## 📊 业务规则

### 用户信息修改
- 用户名注册后不可修改
- 邮箱修改需要验证唯一性
- 手机号可选，支持格式验证

### 密码修改
- 必须验证原密码
- 新密码不能与原密码相同
- 新密码需要确认输入

### 子账号管理
- 只有ADMIN角色可以创建子账号
- 子账号自动关联到创建者
- 子账号继承部分权限

---

> 💡 **提示**: 用户管理模块支持多级用户架构，管理员可以为自己的团队创建子账号，实现分层管理。 