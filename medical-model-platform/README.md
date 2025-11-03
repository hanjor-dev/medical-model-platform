# 医学影像模型管理系统

> 基于微服务架构的医学影像AI处理平台，包含业务管理平台和AI计算引擎

## 🎯 项目概述

本项目是一个完整的医学影像AI处理解决方案，采用微服务架构设计，将业务逻辑和AI计算完全解耦，支持高并发、高可用的医学影像处理服务。

### 🏗️ 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                    医学影像模型管理系统                        │
├─────────────────────┬───────────────────────────────────────┤
│   业务管理平台        │           AI计算引擎                   │
│  (medical-model-    │        (ai-compute-engine)             │
│   platform)        │                                       │
├─────────────────────┼───────────────────────────────────────┤
│ • 用户管理           │ • 算法执行                             │
│ • 文件管理           │ • 任务调度                             │
│ • 任务编排           │ • 资源管理                             │
│ • 积分系统           │ • 性能监控                             │
│ • 实时通信           │ • 多点部署                             │
│ • 权限控制           │ • 负载均衡                             │
└─────────────────────┴───────────────────────────────────────┘
           │                           │
           └─────────── 通信方式 ────────────┘
                  • RabbitMQ 消息队列
                  • RESTful API 调用
                  • WebSocket 实时推送
```

## 📂 项目结构

```
医学影像模型管理系统/
├── medical-model-platform/     # 业务管理平台
│   ├── src/main/java/
│   │   └── com/okbug/platform/
│   │       ├── config/        # 配置类（Web、Swagger、SaToken、DB等）
│   │       ├── common/base/   # 基础类（异常处理、统一返回等）
│   │       ├── controller/    # 控制器层
│   │       ├── service/       # 服务层
│   │       ├── mapper/        # 数据访问层
│   │       └── entity/        # 实体类
│   ├── src/main/resources/
│   │   ├── application*.yml   # 多环境配置
│   │   └── logback-spring.xml # 日志配置
│   ├── logs/                  # 日志文件
│   ├── README.md             # 业务平台说明
│   └── pom.xml               # Maven配置
│
├── ai-compute-engine/          # AI计算引擎
│   ├── src/main/java/
│   │   └── com/okbug/compute/
│   │       ├── config/        # 配置类（Web、Swagger、DB等）
│   │       ├── common/base/   # 基础类（异常处理、统一返回等）
│   │       ├── algorithm/     # 算法模块
│   │       ├── controller/    # 控制器层
│   │       ├── service/       # 服务层
│   │       ├── mapper/        # 数据访问层
│   │       └── entity/        # 实体类
│   ├── src/main/resources/
│   │   ├── application*.yml   # 多环境配置
│   │   └── logback-spring.xml # 日志配置
│   ├── logs/                  # 日志文件
│   ├── README.md             # 计算引擎说明
│   └── pom.xml               # Maven配置
│
└── README.md                  # 项目总览（本文件）
```

## 🎉 项目状态

**✨ 已升级到现代化技术栈！**

- ✅ **JDK 21** - 最新LTS版本,支持虚拟线程
- ✅ **Spring Boot 3.3.6** - 重大版本升级
- ✅ **Docker开发环境** - 一键启动所有服务
- ✅ **SpringDoc OpenAPI 3.0** - 全新API文档
- ✅ **Jakarta EE 9** - 命名空间迁移完成

📖 **详细升级说明**: [升级完成总结.md](./medical-model-platform/升级完成总结.md)

---

## 🚀 快速开始

### 环境要求

- ✅ **JDK 21** (必须,推荐使用Eclipse Temurin)
- ✅ Maven 3.6+
- ✅ Docker Desktop (推荐使用Docker环境)
- ~~❌ JDK 1.8~~ (已不再支持)

### 🐳 Docker快速启动 (推荐)

1. **一键启动所有服务**
   ```powershell
   # Windows
   cd medical-model-platform
   .\docker\start-docker.ps1
   
   # Linux/Mac
   cd medical-model-platform/docker
   docker-compose up -d
   ```

2. **启动业务管理平台**
   ```bash
   cd medical-model-platform
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```
   - 访问地址：http://localhost:8080/api
   - API文档：http://localhost:8080/api/swagger-ui.html

3. **访问服务**
   - 📊 MySQL: `localhost:3306` (root/root123456)
   - 🔴 Redis: `localhost:6379` (密码: redis123456)
   - 🐰 RabbitMQ: `http://localhost:15672` (admin/admin123456)
   - 📖 API文档: `http://localhost:8080/api/swagger-ui.html`

📖 **详细启动指南**: 
- [Docker环境启动和IDEA配置指南.md](./medical-model-platform/Docker环境启动和IDEA配置指南.md)
- [快速启动清单.md](./medical-model-platform/快速启动清单.md)

### 传统方式启动

1. **手动启动基础服务**
   ```bash
   # 启动MySQL 8.0+
   systemctl start mysql
   
   # 启动Redis
   systemctl start redis
   
   # 启动RabbitMQ
   systemctl start rabbitmq-server
   ```

2. **创建数据库**
   ```sql
   CREATE DATABASE medical_model_platform 
   CHARACTER SET utf8mb4 
   COLLATE utf8mb4_unicode_ci;
   ```

3. **配置连接信息**
   - 修改 `application-local.yml` 中的数据库、Redis、RabbitMQ连接信息

4. **启动应用**
   ```bash
   cd medical-model-platform
   mvn spring-boot:run
   ```

## 🏗️ 技术栈对比

| 组件 | 业务管理平台 | AI计算引擎 | 说明 |
|------|-------------|-----------|------|
| **核心框架** | Spring Boot 3.3.6 | Spring Boot 2.7.4 | 业务平台已升级 |
| **JDK版本** | JDK 21 | JDK 1.8 | 业务平台已升级 |
| **认证授权** | SaToken 1.38.0 | - | Spring Boot 3版本 |
| **数据库** | MySQL 8.0 + MyBatis-Plus 3.5.7 | MySQL 8.0 | 共享数据库 |
| **缓存** | Redis 7.2 (会话+缓存) | - | 仅业务平台需要 |
| **消息队列** | RabbitMQ 3.13 | RabbitMQ | 服务间通信 |
| **文件存储** | 阿里云OSS + x-file-storage 2.2.0 | 阿里云OSS | 文件管理 |
| **API文档** | SpringDoc OpenAPI 2.6.0 | Springfox | 业务平台已升级 |
| **实时通信** | WebSocket | - | 仅业务平台需要 |
| **开发环境** | Docker Compose | - | 一键启动 |

## 🔄 服务间通信

### 1. 异步消息通信（主要）

```yaml
# RabbitMQ 队列设计
queues:
  - task.submit        # 任务提交队列
  - task.status.update # 任务状态更新队列
  - task.result        # 任务结果队列
  - engine.register    # 引擎注册队列
  - engine.heartbeat   # 引擎心跳队列
```

### 2. 同步API调用（辅助）

```http
# 业务平台 -> 计算引擎
POST /api/compute/submit    # 提交计算任务
GET  /api/compute/status    # 查询任务状态
POST /api/compute/cancel    # 取消任务

# 计算引擎 -> 业务平台
POST /api/platform/register # 引擎注册
POST /api/platform/heartbeat # 心跳上报
```

### 3. WebSocket实时推送

```javascript
// 前端连接WebSocket获取实时状态
const ws = new WebSocket('ws://localhost:8080/api/ws/task-status');
ws.onmessage = function(event) {
    const status = JSON.parse(event.data);
    updateTaskStatus(status);
};
```

## 📊 功能模块对比

| 功能模块 | 业务管理平台 | AI计算引擎 | 说明 |
|---------|-------------|-----------|------|
| **用户管理** | ✅ 完整实现 | ❌ 不涉及 | 注册、登录、权限 |
| **文件管理** | ✅ 上传+管理 | ✅ 处理+存储 | 协作处理 |
| **任务管理** | ✅ 创建+监控 | ✅ 执行+调度 | 协作处理 |
| **积分系统** | ✅ 完整实现 | ❌ 不涉及 | 计费管理 |
| **算法执行** | ❌ 不涉及 | ✅ 核心功能 | AI算法执行 |
| **资源管理** | ❌ 不涉及 | ✅ 核心功能 | CPU/内存/GPU |
| **实时通信** | ✅ WebSocket | ❌ 不涉及 | 状态推送 |
| **权限控制** | ✅ SaToken | ❌ 不涉及 | 访问控制 |

## 🔒 安全架构

### 业务管理平台安全

- **用户认证**: SaToken JWT认证
- **权限控制**: 基于角色的访问控制(RBAC)
- **会话管理**: Redis存储会话信息
- **接口保护**: 统一的拦截器验证

### AI计算引擎安全

- **内部通信**: 预共享密钥验证
- **IP白名单**: 限制访问来源
- **资源隔离**: 任务执行沙箱
- **数据加密**: 敏感数据传输加密

### 数据安全

- **数据库**: 连接加密 + 访问控制
- **文件存储**: 阿里云OSS权限控制
- **传输安全**: HTTPS + 消息签名
- **日志安全**: 敏感信息脱敏

## 📝 部署方案

### 单机部署

```bash
# 1. 基础环境
docker-compose up -d mysql redis rabbitmq

# 2. 业务平台
cd medical-model-platform
mvn spring-boot:run

# 3. 计算引擎
cd ai-compute-engine
mvn spring-boot:run
```

### 生产集群部署

```yaml
# docker-compose.prod.yml
version: '3.8'
services:
  # 业务平台集群
  medical-platform-1:
    image: medical-platform:latest
    ports: ["8080:8080"]
    
  medical-platform-2:
    image: medical-platform:latest  
    ports: ["8081:8080"]
    
  # 计算引擎集群
  ai-engine-1:
    image: ai-compute-engine:latest
    ports: ["8082:8082"]
    
  ai-engine-2:
    image: ai-compute-engine:latest
    ports: ["8083:8082"]
    
  # 负载均衡
  nginx:
    image: nginx:alpine
    ports: ["80:80", "443:443"]
    volumes: ["./nginx.conf:/etc/nginx/nginx.conf"]
```

## 📈 监控运维

### 应用监控

- **健康检查**: Spring Boot Actuator
- **性能指标**: JVM、HTTP、数据库指标
- **日志监控**: 分类日志文件
- **错误追踪**: 统一异常处理

### 业务监控

- **任务统计**: 成功率、平均耗时
- **用户活跃**: 登录、操作统计  
- **资源使用**: CPU、内存、存储
- **系统告警**: 异常情况自动告警

## 🎯 后续规划

### 短期目标（1-3个月）

- [ ] 完善数据库表结构设计
- [ ] 实现核心业务功能
- [ ] 添加基础AI算法支持
- [ ] 完善单元测试和集成测试

### 中期目标（3-6个月）

- [ ] 性能优化和稳定性提升
- [ ] 多种AI算法集成
- [ ] 监控告警系统
- [ ] 自动化部署流水线

## 🔧 开发规范

### 数据库操作规范
- **MyBatis-Plus Only**: 所有数据库操作必须使用MyBatis-Plus Java API执行
- **禁止手写SQL**: 不允许在resource/mapper目录下编写XML文件
- **禁止注解SQL**: 不允许在Mapper接口方法上使用@Select、@Insert等SQL注解
- **纯Java API**: 使用QueryWrapper、UpdateWrapper等进行复杂查询
- **统一风格**: 保持代码风格一致，提高可维护性

### 模块化目录结构规范
- **按功能模块组织**: 每个功能模块的代码必须放在对应的文件夹下
- **目录结构示例**: 
  - `entity/auth/` - 用户认证相关实体
  - `entity/credit/` - 积分相关实体
  - `mapper/auth/` - 用户认证相关Mapper
  - `controller/credit/` - 积分相关Controller
- **模块划分**: auth(认证)、user(用户)、credit(积分)、permission(权限)、system(系统)
- **避免混乱**: 不允许将所有文件直接放在顶级模块目录下

### 长期目标（6-12个月）

- [ ] 微服务架构进一步细化
- [ ] 支持更多医学影像格式
- [ ] AI模型训练平台
- [ ] 多租户支持

## 📞 联系方式

- 🧑‍💻 **项目负责人**: hanjor
- 📧 **联系邮箱**: hanjor@qq.com
- 🌐 **项目地址**: [GitHub Repository]
- 📋 **问题反馈**: [GitHub Issues]

## 📄 相关文档

### 升级与优化
- 🎉 [升级完成总结.md](./medical-model-platform/升级完成总结.md) - **Spring Boot 3.3.6 + JDK 21升级详情**
- 🐳 [Docker配置优化完成报告.md](./medical-model-platform/Docker配置优化完成报告.md) - **Docker安全配置与环境变量优化**

### 启动指南
- 🚀 [Docker环境启动和IDEA配置指南.md](./medical-model-platform/Docker环境启动和IDEA配置指南.md)
- ⚡ [快速启动清单.md](./medical-model-platform/快速启动清单.md)
- 🐳 [docker/README.md](./medical-model-platform/docker/README.md) - **Docker配置详细说明**

### 开发规范
- 📖 [启动指南-README.md](./medical-model-platform/启动指南-README.md)
- 🔐 [Git版本管理指南.md](./medical-model-platform/Git版本管理指南.md)

### API文档
- 📚 [业务管理平台API](http://localhost:8080/api/swagger-ui.html)
- 🤖 [AI计算引擎API](http://localhost:8082/swagger-ui.html)

---

> 💡 **提示**: 这是一个完整的企业级医学影像AI处理平台，采用现代化的微服务架构，已升级到**Spring Boot 3.3.6 + JDK 21**，具备高可用、高并发、可扩展的特性。 