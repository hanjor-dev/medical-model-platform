# 🐳 Docker配置和常用命令指南

> 医学影像模型管理平台 - Docker环境配置与使用说明

**最后更新**: 2025-10-31  
**文档版本**: v1.0

---

## 📋 目录

- [快速开始](#快速开始)
- [目录结构](#目录结构)
- [环境变量配置](#环境变量配置)
- [常用命令](#常用命令)
- [服务管理](#服务管理)
- [故障排查](#故障排查)
- [安全建议](#安全建议)

---

## 🚀 快速开始

### 方式一：手动启动（推荐，3步搞定）

```powershell
# 步骤1: 进入docker目录
cd medical-model-platform/docker

# 步骤2: 首次启动需要创建.env配置文件（只需执行一次）
Copy-Item env.template .env
# 如需修改密码，编辑.env文件（可选）
notepad .env

# 步骤3: 启动所有服务（后台运行）
docker-compose up -d

# 完成！查看服务状态
docker-compose ps
```

**✅ 验证启动成功**：所有服务的 STATE 列应显示 `Up` 或 `Up (healthy)`

### 方式二：脚本启动（自动化）

```powershell
# 进入docker目录
cd medical-model-platform/docker

# 使用启动脚本（自动创建.env）
.\start-docker.ps1

# 脚本会自动：
# - 检查Docker环境
# - 创建.env配置文件
# - 检查端口占用
# - 启动所有服务（MySQL、Redis、RabbitMQ）
# - 等待服务就绪
# - 测试服务连接
# - 显示访问信息
```

### 服务访问信息

```
📊 MySQL
- 地址: localhost:3306
- 用户名: root
- 密码: root123456 (可在.env中修改)
- 数据库: medical_model_platform

🔴 Redis
- 地址: localhost:6379
- 密码: redis123456 (可在.env中修改)

🐰 RabbitMQ
- AMQP地址: localhost:5672
- 管理界面: http://localhost:15672
- 用户名: admin
- 密码: admin123456 (可在.env中修改)
```

---

## 📁 目录结构

```
medical-model-platform/docker/
├── docker-compose.yml      # Docker编排配置（使用环境变量）
├── env.template            # 环境变量模板（提交Git）
├── .env                    # 本地环境变量（不提交Git）
├── README.md               # Docker配置说明
├── start-docker.ps1        # Windows启动脚本
├── stop-docker.ps1         # Windows停止脚本
│
├── mysql/                  # MySQL配置和数据
│   ├── conf/
│   │   └── my.cnf          # MySQL配置文件
│   ├── data/               # MySQL数据目录（不提交Git）
│   └── init/
│       └── 01-init.sql     # 数据库初始化脚本
│
├── redis/                  # Redis配置和数据
│   ├── conf/
│   │   └── redis.conf      # Redis配置文件
│   └── data/               # Redis数据目录（不提交Git）
│
└── rabbitmq/               # RabbitMQ数据
    └── data/               # RabbitMQ数据目录（不提交Git）
```

---

## ⚙️ 环境变量配置

### 配置文件说明

| 文件 | 说明 | 是否提交Git |
|------|------|------------|
| **env.template** | 环境变量模板，包含默认配置 | ✅ 提交 |
| **.env** | 本地环境变量，包含敏感信息 | ❌ 不提交 |

### 创建配置文件

```powershell
# 方式A: 使用启动脚本（推荐）
.\start-docker.ps1  # 自动创建.env

# 方式B: 手动创建
cd docker
Copy-Item env.template .env
notepad .env  # 修改密码
```

### 环境变量清单

#### MySQL配置

```bash
# MySQL配置
MYSQL_ROOT_PASSWORD=root123456        # root用户密码
MYSQL_DATABASE=medical_model_platform # 数据库名称
MYSQL_USER=medical_user               # 普通用户名
MYSQL_PASSWORD=medical123456          # 普通用户密码
MYSQL_PORT=3306                       # 外部访问端口
MYSQL_MAX_CONNECTIONS=500             # 最大连接数
```

#### Redis配置

```bash
# Redis配置
REDIS_PASSWORD=redis123456            # Redis密码
REDIS_PORT=6379                       # 外部访问端口
REDIS_MAX_MEMORY=512mb                # 最大内存限制
```

#### RabbitMQ配置

```bash
# RabbitMQ配置
RABBITMQ_DEFAULT_USER=admin           # 管理员用户名
RABBITMQ_DEFAULT_PASS=admin123456     # 管理员密码
RABBITMQ_PORT=5672                    # AMQP端口
RABBITMQ_MANAGEMENT_PORT=15672        # 管理界面端口
```

#### 通用配置

```bash
# 通用配置
TZ=Asia/Shanghai                      # 时区设置
COMPOSE_PROJECT_NAME=medical-platform # Docker项目名称
NETWORK_NAME=medical-network          # Docker网络名称
```

### 修改配置

```powershell
# 1. 编辑环境变量
cd docker
notepad .env

# 2. 修改你需要的配置（如密码、端口等）

# 3. 重启服务使配置生效
docker-compose down
docker-compose up -d

# 4. 验证配置
docker-compose config
```

---

## 🔧 常用命令（必看）

### 服务启动和停止（手动方式）

#### 启动服务

```powershell
# === 方式1: 启动所有服务（推荐） ===
cd medical-model-platform/docker
docker-compose up -d
# -d 参数表示后台运行（daemon模式）

# === 方式2: 启动单个服务 ===
cd medical-model-platform/docker
docker-compose up -d mysql      # 只启动MySQL
docker-compose up -d redis      # 只启动Redis
docker-compose up -d rabbitmq   # 只启动RabbitMQ

# === 方式3: 前台运行（查看实时日志，调试用） ===
cd medical-model-platform/docker
docker-compose up
# 不加-d参数，会在终端显示所有日志
# 按 Ctrl+C 停止服务

# === 方式4: 启动指定服务并查看日志 ===
cd medical-model-platform/docker
docker-compose up mysql  # 前台运行MySQL，查看日志
```

#### 停止服务

```powershell
# === 方式1: 停止所有服务（保留数据，推荐） ===
cd medical-model-platform/docker
docker-compose down
# 数据保存在数据卷中，下次启动数据还在

# === 方式2: 停止单个服务 ===
cd medical-model-platform/docker
docker-compose stop mysql      # 停止MySQL
docker-compose stop redis      # 停止Redis
docker-compose stop rabbitmq   # 停止RabbitMQ

# === 方式3: 停止并删除所有数据（⚠️ 危险！慎用） ===
cd medical-model-platform/docker
docker-compose down -v
# -v 参数会删除数据卷，所有数据将丢失！
# 仅在需要完全重置环境时使用
```

#### 重启服务

```powershell
# === 重启所有服务 ===
cd medical-model-platform/docker
docker-compose restart

# === 重启单个服务 ===
cd medical-model-platform/docker
docker-compose restart mysql
docker-compose restart redis
docker-compose restart rabbitmq

# === 完全重启（停止后重新启动） ===
cd medical-model-platform/docker
docker-compose down
docker-compose up -d
```

### 查看服务状态

```powershell
# 查看所有容器状态
docker-compose ps

# 查看所有容器详细信息
docker ps -a

# 查看特定服务状态
docker-compose ps mysql

# 查看服务健康状态
docker inspect medical-platform-mysql | findstr Health
docker inspect medical-platform-redis | findstr Health
docker inspect medical-platform-rabbitmq | findstr Health
```

### 查看日志

```powershell
# 查看所有服务日志（实时）
docker-compose logs -f

# 查看单个服务日志
docker-compose logs -f mysql
docker-compose logs -f redis
docker-compose logs -f rabbitmq

# 查看最近100行日志
docker-compose logs --tail=100 mysql

# 查看指定时间后的日志
docker-compose logs --since="2024-01-01T00:00:00" mysql
```

### 进入容器

```powershell
# === MySQL ===
# 进入MySQL容器
docker exec -it medical-platform-mysql bash

# 直接连接MySQL
docker exec -it medical-platform-mysql mysql -uroot -p
# 输入密码: .env中配置的MYSQL_ROOT_PASSWORD

# === Redis ===
# 进入Redis容器
docker exec -it medical-platform-redis sh

# 直接连接Redis
docker exec -it medical-platform-redis redis-cli -a [密码]

# === RabbitMQ ===
# 进入RabbitMQ容器
docker exec -it medical-platform-rabbitmq sh

# 查看RabbitMQ状态
docker exec medical-platform-rabbitmq rabbitmq-diagnostics ping
docker exec medical-platform-rabbitmq rabbitmqctl status
```

### 数据库操作

```powershell
# === 执行SQL命令 ===
docker exec medical-platform-mysql mysql -uroot -p[密码] -e "SHOW DATABASES;"
docker exec medical-platform-mysql mysql -uroot -p[密码] medical_model_platform -e "SHOW TABLES;"

# === 备份数据库 ===
# 备份单个数据库
docker exec medical-platform-mysql mysqldump -uroot -p[密码] medical_model_platform > backup.sql

# 备份所有数据库
docker exec medical-platform-mysql mysqldump -uroot -p[密码] --all-databases > all-backup.sql

# === 恢复数据库 ===
# 恢复数据库
docker exec -i medical-platform-mysql mysql -uroot -p[密码] medical_model_platform < backup.sql

# 从SQL文件恢复
cat backup.sql | docker exec -i medical-platform-mysql mysql -uroot -p[密码] medical_model_platform
```

### Redis操作

```powershell
# === Redis命令 ===
# 连接Redis
docker exec -it medical-platform-redis redis-cli -a [密码]

# 测试连接
docker exec medical-platform-redis redis-cli -a [密码] ping

# 查看所有key
docker exec medical-platform-redis redis-cli -a [密码] keys "*"

# 查看info
docker exec medical-platform-redis redis-cli -a [密码] info

# === 备份Redis ===
# 触发保存
docker exec medical-platform-redis redis-cli -a [密码] save

# 复制备份文件
docker cp medical-platform-redis:/data/dump.rdb ./redis-backup.rdb

# === 恢复Redis ===
# 停止Redis
docker-compose stop redis

# 复制备份文件
docker cp ./redis-backup.rdb medical-platform-redis:/data/dump.rdb

# 启动Redis
docker-compose start redis
```

### RabbitMQ操作

```powershell
# === RabbitMQ管理 ===
# 访问管理界面
start http://localhost:15672
# 用户名: admin
# 密码: .env中配置的RABBITMQ_DEFAULT_PASS

# 查看状态
docker exec medical-platform-rabbitmq rabbitmqctl status

# 查看队列
docker exec medical-platform-rabbitmq rabbitmqctl list_queues

# 查看交换机
docker exec medical-platform-rabbitmq rabbitmqctl list_exchanges

# 查看用户
docker exec medical-platform-rabbitmq rabbitmqctl list_users
```

### 网络管理

```powershell
# 查看Docker网络
docker network ls

# 查看医疗平台网络详情
docker network inspect medical-network

# 查看容器IP地址
docker inspect medical-platform-mysql | findstr IPAddress
docker inspect medical-platform-redis | findstr IPAddress
docker inspect medical-platform-rabbitmq | findstr IPAddress
```

### 资源清理

```powershell
# === 清理容器 ===
# 删除停止的容器
docker container prune

# === 清理镜像 ===
# 删除未使用的镜像
docker image prune

# 删除所有未使用的镜像
docker image prune -a

# === 清理卷 ===
# 删除未使用的卷
docker volume prune

# === 清理网络 ===
# 删除未使用的网络
docker network prune

# === 清理所有 ===
# 清理所有未使用的资源
docker system prune
docker system prune -a  # 包括镜像
docker system prune -a --volumes  # 包括镜像和卷
```

---

## 🎮 服务管理总结

### 日常使用流程（手动命令）

```powershell
# === 每天开发前：启动服务 ===
cd medical-model-platform/docker
docker-compose up -d
docker-compose ps  # 确认所有服务已启动

# === 开发过程中：查看日志（如果遇到问题） ===
docker-compose logs -f mysql      # 查看MySQL日志
docker-compose logs -f redis      # 查看Redis日志
docker-compose logs -f rabbitmq   # 查看RabbitMQ日志

# === 开发结束后：停止服务 ===
docker-compose down  # 保留数据

# === 需要重启某个服务 ===
docker-compose restart mysql
```

### 完整生命周期管理

```powershell
# === 首次启动 ===
cd medical-model-platform/docker
Copy-Item env.template .env  # 创建配置文件
docker-compose up -d         # 启动服务
docker-compose ps            # 验证状态

# === 日常启动 ===
cd medical-model-platform/docker
docker-compose up -d
docker-compose ps

# === 日常停止 ===
cd medical-model-platform/docker
docker-compose down

# === 修改配置后重启 ===
cd medical-model-platform/docker
notepad .env                 # 修改配置
docker-compose down          # 停止服务
docker-compose up -d         # 重新启动
docker-compose ps            # 验证状态

# === 完全重置环境（清空所有数据） ===
cd medical-model-platform/docker
docker-compose down -v       # ⚠️ 删除所有数据
docker-compose up -d         # 重新启动
```

### 使用PowerShell脚本（可选，自动化）

如果你希望更自动化的管理，可以使用提供的脚本：

```powershell
# 启动（自动检查环境、创建配置、测试连接）
cd medical-model-platform/docker
.\start-docker.ps1

# 停止
cd medical-model-platform/docker
.\stop-docker.ps1
# 选择选项：
# 1 - 停止服务（保留数据）
# 2 - 停止并删除数据卷（⚠️ 清空所有数据）
```

脚本提供的额外功能：
- ✅ 自动检查Docker环境
- ✅ 自动创建.env文件
- ✅ 检查端口占用
- ✅ 等待服务健康
- ✅ 测试服务连接
- ✅ 显示访问信息

---

## 🐛 故障排查

### 问题1: 容器启动失败

**症状**: `docker-compose up -d` 执行失败

**解决方案**:
```powershell
# 1. 检查.env文件是否存在
cd docker
ls .env

# 2. 验证docker-compose配置
docker-compose config

# 3. 查看详细错误
docker-compose up  # 不加-d参数，查看实时输出

# 4. 检查Docker Desktop是否运行
Get-Process "Docker Desktop"
```

### 问题2: 端口被占用

**症状**: `port is already allocated`

**解决方案**:
```powershell
# 查看端口占用
netstat -ano | findstr "3306"
netstat -ano | findstr "6379"
netstat -ano | findstr "5672"
netstat -ano | findstr "15672"

# 停止占用端口的进程
taskkill /PID [进程ID] /F

# 或修改.env中的端口配置
notepad .env
# 修改 MYSQL_PORT, REDIS_PORT 等
```

### 问题3: MySQL连接失败

**症状**: `Access denied for user 'root'@'localhost'`

**解决方案**:
```powershell
# 1. 确认密码是否正确
cd docker
cat .env | findstr MYSQL

# 2. 检查MySQL是否完全启动
docker-compose logs -f mysql
# 等待看到 "ready for connections"

# 3. 重置MySQL密码
docker-compose down mysql
docker volume rm medical-platform_mysql_data
docker-compose up -d mysql

# 4. 测试连接
docker exec medical-platform-mysql mysql -uroot -p[密码] -e "SELECT 1;"
```

### 问题4: Redis连接失败

**症状**: `NOAUTH Authentication required`

**解决方案**:
```powershell
# 1. 检查Redis密码
cat .env | findstr REDIS_PASSWORD

# 2. 使用正确密码连接
docker exec medical-platform-redis redis-cli -a [你的密码]

# 3. 检查Redis日志
docker-compose logs -f redis

# 4. 重启Redis
docker-compose restart redis
```

### 问题5: RabbitMQ管理界面无法访问

**症状**: `http://localhost:15672` 无法打开

**解决方案**:
```powershell
# 1. 检查RabbitMQ状态
docker-compose ps rabbitmq

# 2. 查看RabbitMQ日志
docker-compose logs -f rabbitmq

# 3. 等待插件启动完成（约30秒）
docker exec medical-platform-rabbitmq rabbitmq-plugins list

# 4. 手动启用管理插件
docker exec medical-platform-rabbitmq rabbitmq-plugins enable rabbitmq_management

# 5. 重启RabbitMQ
docker-compose restart rabbitmq
```

### 问题6: 容器健康检查失败

**症状**: 容器状态显示 `unhealthy`

**解决方案**:
```powershell
# 1. 查看健康检查日志
docker inspect medical-platform-mysql | findstr Health
docker logs medical-platform-mysql | findstr health

# 2. 查看容器日志
docker-compose logs -f mysql

# 3. 进入容器手动测试
docker exec -it medical-platform-mysql bash
mysql -uroot -p[密码] -e "SELECT 1;"

# 4. 重启容器
docker-compose restart mysql
```

### 问题7: 数据丢失

**症状**: 重启后数据消失

**原因**: 使用了 `docker-compose down -v` 删除了数据卷

**解决方案**:
```powershell
# 正确的停止方式（保留数据）
docker-compose down  # 不加-v参数

# 或使用停止脚本
.\stop-docker.ps1
# 选择选项1（保留数据）

# 恢复数据（如果有备份）
# MySQL
docker exec -i medical-platform-mysql mysql -uroot -p[密码] medical_model_platform < backup.sql

# Redis
docker cp redis-backup.rdb medical-platform-redis:/data/dump.rdb
docker-compose restart redis
```

---

## 🔒 安全建议

### 开发环境安全

1. **使用环境变量管理密码**
   ```powershell
   # ✅ 正确 - 密码在.env中
   cd docker
   notepad .env
   
   # ❌ 错误 - 密码硬编码在docker-compose.yml
   ```

2. **确保.env不提交到Git**
   ```powershell
   # 检查.gitignore
   cat .gitignore | findstr ".env"
   # 应该输出: .env
   
   # 检查Git状态
   git status
   # .env不应出现在待提交列表中
   ```

3. **定期更换密码**
   ```powershell
   # 每3个月更换一次密码
   cd docker
   notepad .env  # 修改所有PASSWORD配置
   docker-compose down
   docker-compose up -d
   ```

### 生产环境安全

1. **使用强密码**
   ```powershell
   # 生成强密码（PowerShell）
   -join ((33..126) | Get-Random -Count 20 | ForEach-Object {[char]$_})
   
   # 密码要求：
   # - 至少16位
   # - 包含大小写字母
   # - 包含数字
   # - 包含特殊字符
   ```

2. **限制文件权限（Linux）**
   ```bash
   chmod 600 .env          # 只有所有者可读写
   chmod 600 docker-compose.yml
   ```

3. **使用Docker Secrets**
   ```yaml
   # docker-compose.yml（生产环境）
   services:
     mysql:
       secrets:
         - mysql_root_password
   
   secrets:
     mysql_root_password:
       external: true
   ```

4. **网络隔离**
   ```yaml
   # docker-compose.yml
   networks:
     backend:
       internal: true  # 内部网络，无法访问外网
     frontend:
       internal: false # 外部网络
   ```

5. **启用TLS/SSL**
   ```yaml
   # 生产环境必须启用HTTPS
   # 使用Let's Encrypt或自签名证书
   ```

### 密码管理最佳实践

```powershell
# 1. 开发环境 - 简单密码即可
MYSQL_ROOT_PASSWORD=root123456

# 2. 测试环境 - 中等强度
MYSQL_ROOT_PASSWORD=Test@2024#MySQL

# 3. 生产环境 - 强密码 + 密钥管理
MYSQL_ROOT_PASSWORD=${使用AWS Secrets Manager或Azure Key Vault}

# 4. 定期轮换
# - 每3个月更换一次密码
# - 记录密码修改历史
# - 使用密码管理工具（如1Password、LastPass）
```

---

## 📚 附加资源

### 官方文档

- [Docker官方文档](https://docs.docker.com/)
- [Docker Compose文档](https://docs.docker.com/compose/)
- [MySQL Docker镜像](https://hub.docker.com/_/mysql)
- [Redis Docker镜像](https://hub.docker.com/_/redis)
- [RabbitMQ Docker镜像](https://hub.docker.com/_/rabbitmq)

### 相关文档

- **README.md** - 项目总览
- **B_JDK21和SpringBoot3升级总结.md** - 升级详情
- **C_Git使用指南.md** - Git版本管理

### 获取帮助

- 📧 项目负责人: hanjor@qq.com
- 📖 查看日志: `docker-compose logs -f`
- 🔍 检查状态: `docker-compose ps`

---

## 🎯 快速命令速查表（复制即用）

### 核心命令（手动方式，推荐）

```powershell
# ========================================
# 基础操作（最常用）
# ========================================

# 进入docker目录
cd medical-model-platform/docker

# 首次启动（创建配置文件）
Copy-Item env.template .env
docker-compose up -d
docker-compose ps

# 日常启动
docker-compose up -d

# 日常停止（保留数据）
docker-compose down

# 查看状态
docker-compose ps

# 查看日志
docker-compose logs -f

# ========================================
# 服务管理
# ========================================

# 启动所有服务
docker-compose up -d

# 停止所有服务（保留数据）
docker-compose down

# 重启所有服务
docker-compose restart

# 启动单个服务
docker-compose up -d mysql
docker-compose up -d redis
docker-compose up -d rabbitmq

# 停止单个服务
docker-compose stop mysql
docker-compose stop redis
docker-compose stop rabbitmq

# 重启单个服务
docker-compose restart mysql
docker-compose restart redis
docker-compose restart rabbitmq

# ========================================
# 日志查看
# ========================================

# 查看所有服务日志（实时）
docker-compose logs -f

# 查看单个服务日志
docker-compose logs -f mysql
docker-compose logs -f redis
docker-compose logs -f rabbitmq

# 查看最近100行日志
docker-compose logs --tail=100 mysql

# ========================================
# 进入容器
# ========================================

# 进入MySQL容器
docker exec -it medical-platform-mysql bash
docker exec -it medical-platform-mysql mysql -uroot -proot123456

# 进入Redis容器
docker exec -it medical-platform-redis sh
docker exec -it medical-platform-redis redis-cli -a redis123456

# 进入RabbitMQ容器
docker exec -it medical-platform-rabbitmq sh

# ========================================
# 数据备份与恢复
# ========================================

# MySQL备份
docker exec medical-platform-mysql mysqldump -uroot -proot123456 medical_model_platform > backup.sql

# MySQL恢复
docker exec -i medical-platform-mysql mysql -uroot -proot123456 medical_model_platform < backup.sql

# Redis备份
docker exec medical-platform-redis redis-cli -a redis123456 save
docker cp medical-platform-redis:/data/dump.rdb ./redis-backup.rdb

# Redis恢复
docker-compose stop redis
docker cp ./redis-backup.rdb medical-platform-redis:/data/dump.rdb
docker-compose start redis

# ========================================
# 配置修改
# ========================================

# 编辑环境变量
notepad .env

# 验证配置
docker-compose config

# 重启使配置生效
docker-compose down
docker-compose up -d

# ========================================
# 完全重置（清空所有数据）
# ========================================

# ⚠️ 危险操作：删除所有数据
docker-compose down -v
docker-compose up -d
```

### 脚本命令（可选）

```powershell
# 进入docker目录
cd medical-model-platform/docker

# 启动（自动化）
.\start-docker.ps1

# 停止（交互式选择）
.\stop-docker.ps1
```

---

**文档版本**: v1.0  
**最后更新**: 2025-10-31  
**维护者**: hanjor  
**项目**: Medical Model Platform

---

*祝Docker使用顺利！* 🐳✨

