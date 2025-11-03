# 医疗影像模型管理平台 - 接口文档

## 📋 文档概述

本文档详细描述了医疗影像模型管理平台的所有API接口，包括接口功能、请求参数、响应格式、权限要求等信息。该文档用于前端开发人员与后端接口进行联调。

## 🏗️ 系统架构

- **基础URL**: `http://localhost:8080`
- **认证方式**: Sa-Token JWT认证
- **响应格式**: 统一使用 `ApiResult<T>` 包装
- **数据格式**: JSON
- **字符编码**: UTF-8

## 📚 模块列表

### 1. [用户认证模块](./auth/README.md)
- 用户注册、登录、退出
- Token刷新
- 权限验证

### 2. [用户管理模块](./user/README.md)
- 用户信息管理
- 密码修改
- 子账号管理

### 3. [权限管理模块](./permission/README.md)
- 权限查询
- 权限配置
- 角色权限管理

### 4. [系统配置模块](./system/README.md)
- 系统配置管理
- 字典管理
- 配置审计

### 5. [积分系统模块](./credit/README.md)
- 积分管理
- 积分交易
- 积分类型管理
- 积分场景管理

## 🔐 通用认证说明

### 认证流程
1. 调用登录接口获取Token
2. 在后续请求的Header中添加：`Authorization: Bearer {token}`
3. 系统自动验证Token有效性
4. 根据用户角色和权限控制接口访问

### 权限级别
- **USER**: 普通用户权限
- **ADMIN**: 管理员权限
- **SUPER_ADMIN**: 超级管理员权限

## 📊 通用响应格式

### 成功响应
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1673836800000
}
```

### 失败响应
```json
{
  "code": 4001,
  "message": "错误描述",
  "data": null,
  "timestamp": 1673836800000
}
```

### 分页响应
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "records": [],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  },
  "timestamp": 1673836800000
}
```

## 🚀 快速开始

### 1. 获取Token
```bash
curl -X POST "http://localhost:8080/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "123456"
  }'
```

### 2. 使用Token访问接口
```bash
curl -X GET "http://localhost:8080/user/profile" \
  -H "Authorization: Bearer {your_token}"
```

## 📝 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 4001 | 参数验证失败 |
| 4002 | 数据不存在 |
| 4003 | 数据已存在 |
| 4004 | 操作不允许 |
| 5001 | 内部服务器错误 |
| 5002 | 服务不可用 |

## 🔧 开发环境

- **后端服务**: http://localhost:8080
- **Swagger文档**: http://localhost:8080/swagger-ui/index.html
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **认证**: Sa-Token

## 📞 技术支持

- **开发团队**: hanjor
- **联系邮箱**: hanjor@qq.com
- **项目地址**: [GitHub Repository]

---

> 💡 **提示**: 本文档会随着系统功能的更新而持续更新，请关注最新版本。 