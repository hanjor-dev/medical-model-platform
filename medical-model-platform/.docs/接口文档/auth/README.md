# 用户认证模块 - 接口文档

## 📋 模块概述

用户认证模块提供用户注册、登录、退出、Token刷新等核心认证功能，是系统安全访问的基础。

## 🔐 接口列表

### 1. 用户注册

**接口描述**: 新用户注册，支持推荐码机制

**请求路径**: `POST /auth/register`

**请求方式**: `POST`

**权限要求**: 无需认证

**请求参数**:
```json
{
  "username": "zhang123",
  "email": "zhang123@example.com",
  "password": "abc123456",
  "nickname": "张三",
  "referralCode": "ABC12345"
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| username | String | 是 | 用户名，3-20个字符，唯一 | "zhang123" |
| email | String | 否 | 邮箱地址，唯一 | "zhang123@example.com" |
| password | String | 是 | 密码，6-20个字符 | "abc123456" |
| nickname | String | 否 | 昵称，1-50个字符 | "张三" |
| referralCode | String | 否 | 推荐码，8位字符 | "ABC12345" |

**响应结果**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": 1234567890,
    "username": "zhang123",
    "email": "zhang123@example.com",
    "nickname": "张三",
    "referralCode": "XYZ98765",
    "createTime": "2025-01-15T01:20:00"
  },
  "timestamp": 1673836800000
}
```

**响应字段说明**:
| 字段名 | 类型 | 说明 |
|--------|------|------|
| userId | Long | 用户ID |
| username | String | 用户名 |
| email | String | 邮箱地址 |
| nickname | String | 昵称 |
| referralCode | String | 个人推荐码 |
| createTime | String | 创建时间 |

---

### 2. 用户登录

**接口描述**: 用户名/邮箱+密码登录，返回Token和用户信息

**请求路径**: `POST /auth/login`

**请求方式**: `POST`

**权限要求**: 无需认证

**请求参数**:
```json
{
  "username": "zhang123",
  "password": "abc123456"
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| username | String | 是 | 用户名或邮箱 | "zhang123" |
| password | String | 是 | 密码 | "abc123456" |

**响应结果**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
    "userId": 1234567890,
    "username": "zhang123",
    "role": "USER",
    "permissions": ["user:profile:read", "user:profile:update"]
  },
  "timestamp": 1673836800000
}
```

**响应字段说明**:
| 字段名 | 类型 | 说明 |
|--------|------|------|
| token | String | 访问令牌（Sa-Token） |
| userId | Long | 用户ID |
| username | String | 用户名 |
| role | String | 用户角色 |
| permissions | Array | 权限列表 |

---

### 3. 用户退出

**接口描述**: 退出登录，清除Token

**请求路径**: `POST /auth/logout`

**请求方式**: `POST`

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
  "message": "退出成功",
  "data": null,
  "timestamp": 1673836800000
}
```

---

### 4. Token刷新

**接口描述**: 刷新Token有效期

**请求路径**: `POST /auth/refresh`

**请求方式**: `POST`

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
  "message": "刷新成功",
  "data": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
  "timestamp": 1673836800000
}
```

**响应字段说明**:
| 字段名 | 类型 | 说明 |
|--------|------|------|
| data | String | 新的访问令牌 |

## 🔒 安全说明

### 密码安全
- 密码使用BCrypt加密存储
- 密码长度限制：6-20个字符
- 支持特殊字符、数字、字母组合

### Token安全
- Token有效期：24小时
- 支持Token刷新机制
- 退出时自动清除Token

### 登录限制
- 支持登录失败次数限制
- 账户锁定机制
- IP地址记录和限制

## 📝 错误码说明

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 4001 | 参数验证失败 | 检查请求参数格式和必填项 |
| 4002 | 用户不存在 | 检查用户名或邮箱是否正确 |
| 4003 | 密码错误 | 检查密码是否正确 |
| 4004 | 账户已锁定 | 联系管理员解锁账户 |
| 4005 | 推荐码无效 | 检查推荐码是否正确 |
| 4006 | 用户名已存在 | 选择其他用户名 |
| 4007 | 邮箱已存在 | 使用其他邮箱地址 |

## 🚀 使用示例

### 完整注册流程
```bash
# 1. 用户注册
curl -X POST "http://localhost:8080/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "zhang123",
    "email": "zhang123@example.com",
    "password": "abc123456",
    "nickname": "张三"
  }'

# 2. 用户登录
curl -X POST "http://localhost:8080/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "zhang123",
    "password": "abc123456"
  }'

# 3. 使用Token访问其他接口
curl -X GET "http://localhost:8080/user/profile" \
  -H "Authorization: Bearer {token}"
```

## 📊 业务规则

### 用户名规则
- 长度：3-20个字符
- 格式：字母、数字、下划线
- 唯一性：全局唯一，注册后不可修改

### 邮箱规则
- 格式：标准邮箱格式
- 唯一性：全局唯一
- 可选：注册时可以不填写

### 推荐码机制
- 自动生成：系统自动生成8位推荐码
- 推荐关系：记录推荐人和被推荐人关系
- 积分奖励：推荐成功可获得积分奖励

---

> 💡 **提示**: 认证模块是系统安全的基础，请确保在生产环境中启用HTTPS和适当的密码策略。 