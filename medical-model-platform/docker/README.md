# 🐳 Docker开发环境配置说明

> 医学影像模型管理平台 - Docker环境配置

---

## 📁 目录结构

```
docker/
├── docker-compose.yml      # Docker编排配置
├── env.template            # 环境变量模板
├── .env                    # 环境变量配置（不提交Git）
├── start-docker.ps1        # Windows启动脚本
├── stop-docker.ps1         # Windows停止脚本
├── mysql/                  # MySQL配置和数据
│   ├── conf/               # MySQL配置文件
│   ├── data/               # MySQL数据目录（不提交Git）
│   └── init/               # 初始化SQL脚本
├── redis/                  # Redis配置和数据
│   ├── conf/               # Redis配置文件
│   └── data/               # Redis数据目录（不提交Git）
└── rabbitmq/               # RabbitMQ数据
    └── data/               # RabbitMQ数据目录（不提交Git）
```

---

## 🚀 快速开始

### 1. 创建环境变量文件

```powershell
# 方式A: 自动创建（推荐）
.\start-docker.ps1  # 脚本会自动检测并创建.env

# 方式B: 手动创建
Copy-Item env.template .env
```

### 2. 配置环境变量

编辑 `.env` 文件，修改敏感信息（生产环境必须修改！）：

```bash
# MySQL配置
MYSQL_ROOT_PASSWORD=your_secure_password  # ⚠️ 修改此密码
MYSQL_PASSWORD=your_secure_password       # ⚠️ 修改此密码

# Redis配置
REDIS_PASSWORD=your_secure_password       # ⚠️ 修改此密码

# RabbitMQ配置
RABBITMQ_DEFAULT_PASS=your_secure_password # ⚠️ 修改此密码
```

### 3. 启动服务

```powershell
# Windows - 使用自动化脚本（推荐）
.\start-docker.ps1

# 或手动启动
docker-compose up -d

# 查看服务状态
docker-compose ps
```

### 4. 验证服务

```powershell
# MySQL
docker exec medical-platform-mysql mysql -uroot -p[你的密码] -e "SELECT 1;"

# Redis
docker exec medical-platform-redis redis-cli -a [你的密码] ping

# RabbitMQ
docker exec medical-platform-rabbitmq rabbitmq-diagnostics ping
```

---

## ⚙️ 环境变量说明

### MySQL配置

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| MYSQL_ROOT_PASSWORD | root用户密码 | root123456 |
| MYSQL_DATABASE | 数据库名称 | medical_model_platform |
| MYSQL_USER | 普通用户名 | medical_user |
| MYSQL_PASSWORD | 普通用户密码 | medical123456 |
| MYSQL_PORT | 外部访问端口 | 3306 |
| MYSQL_MAX_CONNECTIONS | 最大连接数 | 500 |

### Redis配置

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| REDIS_PASSWORD | Redis密码 | redis123456 |
| REDIS_PORT | 外部访问端口 | 6379 |
| REDIS_MAX_MEMORY | 最大内存限制 | 512mb |

### RabbitMQ配置

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| RABBITMQ_DEFAULT_USER | 管理员用户名 | admin |
| RABBITMQ_DEFAULT_PASS | 管理员密码 | admin123456 |
| RABBITMQ_PORT | AMQP端口 | 5672 |
| RABBITMQ_MANAGEMENT_PORT | 管理界面端口 | 15672 |

### 通用配置

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| TZ | 时区设置 | Asia/Shanghai |
| COMPOSE_PROJECT_NAME | Docker项目名称 | medical-platform |
| NETWORK_NAME | Docker网络名称 | medical-network |

---

## 🔧 常用命令

### 服务管理

```powershell
# 启动所有服务
docker-compose up -d

# 启动单个服务
docker-compose up -d mysql
docker-compose up -d redis
docker-compose up -d rabbitmq

# 停止所有服务
docker-compose down

# 停止并删除数据
docker-compose down -v

# 重启服务
docker-compose restart

# 重启单个服务
docker-compose restart mysql
```

### 查看状态

```powershell
# 查看所有服务状态
docker-compose ps

# 查看服务日志
docker-compose logs -f          # 所有服务
docker-compose logs -f mysql    # 单个服务

# 查看最近100行日志
docker-compose logs --tail=100 mysql
```

### 进入容器

```powershell
# 进入MySQL容器
docker exec -it medical-platform-mysql bash
# 连接MySQL
mysql -uroot -p

# 进入Redis容器
docker exec -it medical-platform-redis sh
# 连接Redis
redis-cli -a [密码]

# 进入RabbitMQ容器
docker exec -it medical-platform-rabbitmq sh
```

### 数据备份

```powershell
# 备份MySQL数据库
docker exec medical-platform-mysql mysqldump -uroot -p[密码] medical_model_platform > backup.sql

# 恢复MySQL数据库
docker exec -i medical-platform-mysql mysql -uroot -p[密码] medical_model_platform < backup.sql

# 备份Redis数据
docker exec medical-platform-redis redis-cli -a [密码] save
docker cp medical-platform-redis:/data/dump.rdb ./redis-backup.rdb

# 恢复Redis数据
docker cp ./redis-backup.rdb medical-platform-redis:/data/dump.rdb
docker-compose restart redis
```

---

## 🐛 故障排查

### 问题1: 容器启动失败

**症状**: `docker-compose up -d` 执行失败

**解决方案**:
```powershell
# 1. 检查.env文件是否存在
ls .env

# 2. 验证docker-compose配置
docker-compose config

# 3. 查看详细错误
docker-compose up  # 不加-d参数查看错误

# 4. 检查端口占用
netstat -ano | findstr "3306"
netstat -ano | findstr "6379"
netstat -ano | findstr "5672"
```

### 问题2: MySQL连接失败

**症状**: `Access denied for user`

**解决方案**:
```powershell
# 1. 检查密码是否正确
cat .env | findstr MYSQL

# 2. 重置MySQL密码
docker-compose down mysql
docker volume rm medical-platform_mysql_data
docker-compose up -d mysql

# 3. 等待MySQL完全启动（约30秒）
docker-compose logs -f mysql
```

### 问题3: Redis连接失败

**症状**: `NOAUTH Authentication required`

**解决方案**:
```powershell
# 1. 检查Redis密码
cat .env | findstr REDIS_PASSWORD

# 2. 连接时使用正确的密码
redis-cli -h localhost -p 6379 -a [你的密码]

# 3. 重启Redis
docker-compose restart redis
```

### 问题4: 数据丢失

**症状**: 重启后数据消失

**原因**: 使用了 `docker-compose down -v` 删除了数据卷

**解决方案**:
```powershell
# 仅停止容器，保留数据
docker-compose down

# 或使用停止脚本
.\stop-docker.ps1  # 选择选项1（保留数据）
```

---

## 🔒 安全建议

### 开发环境

1. ✅ 使用 `.env` 文件管理密码
2. ✅ 确保 `.env` 在 `.gitignore` 中
3. ✅ 定期更换密码
4. ✅ 不要在代码中硬编码密码

### 生产环境

1. ⚠️ **必须修改所有默认密码**
2. ⚠️ 使用强密码（至少16位，包含大小写字母、数字、特殊字符）
3. ⚠️ 使用Docker Secrets或外部密钥管理系统
4. ⚠️ 限制容器网络访问
5. ⚠️ 启用TLS/SSL加密
6. ⚠️ 定期备份数据
7. ⚠️ 配置防火墙规则

### 密码强度建议

```bash
# 生成安全的随机密码（PowerShell）
-join ((33..126) | Get-Random -Count 20 | ForEach-Object {[char]$_})

# 或使用在线工具
https://passwordsgenerator.net/
```

---

## 📞 需要帮助？

- 📖 查看完整文档: [Docker环境启动和IDEA配置指南.md](../Docker环境启动和IDEA配置指南.md)
- 🚀 快速启动: [快速启动清单.md](../快速启动清单.md)
- 📧 联系作者: hanjor@qq.com

---

*最后更新: 2025-10-31*

