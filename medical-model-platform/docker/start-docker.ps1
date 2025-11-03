# Docker 环境快速启动脚本
# 医学影像模型管理平台

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "医学影像模型管理平台 - Docker环境启动" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# 获取脚本所在目录（docker目录）
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptPath

# 检查 .env 文件是否存在
Write-Host "0. 检查环境配置..." -ForegroundColor Yellow
if (-not (Test-Path ".env")) {
    Write-Host "   ⚠ 未找到 .env 文件" -ForegroundColor Yellow
    if (Test-Path "env.template") {
        Write-Host "   正在从 env.template 创建 .env 文件..." -ForegroundColor Gray
        Copy-Item "env.template" ".env"
        Write-Host "   ✓ 已创建 .env 文件（使用默认配置）" -ForegroundColor Green
        Write-Host "   提示: 生产环境请修改 .env 中的密码！" -ForegroundColor Yellow
    } else {
        Write-Host "   ✗ 错误: 找不到 env.template 文件!" -ForegroundColor Red
        Write-Host "   请确保在 docker 目录下运行此脚本" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "   ✓ 找到 .env 配置文件" -ForegroundColor Green
}

# 加载环境变量（用于后续密码测试）
Get-Content ".env" | ForEach-Object {
    if ($_ -match '^\s*([^#][^=]*?)\s*=\s*(.+?)\s*$') {
        $name = $matches[1]
        $value = $matches[2]
        Set-Variable -Name $name -Value $value -Scope Script
    }
}

# 检查Docker是否运行
Write-Host ""
Write-Host "1. 检查Docker环境..." -ForegroundColor Yellow
try {
    docker version | Out-Null
    Write-Host "   ✓ Docker运行正常" -ForegroundColor Green
} catch {
    Write-Host "   ✗ Docker未运行，请先启动Docker Desktop!" -ForegroundColor Red
    exit 1
}

# 检查端口占用
Write-Host ""
Write-Host "2. 检查端口占用..." -ForegroundColor Yellow
$ports = @($MYSQL_PORT, $REDIS_PORT, $RABBITMQ_PORT, $RABBITMQ_MANAGEMENT_PORT)
$portsOk = $true
foreach ($port in $ports) {
    $connection = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
    if ($connection) {
        Write-Host "   ⚠ 端口 $port 已被占用 (PID: $($connection.OwningProcess))" -ForegroundColor Yellow
        $portsOk = $false
    }
}
if ($portsOk) {
    Write-Host "   ✓ 所有端口可用" -ForegroundColor Green
} else {
    Write-Host ""
    $continue = Read-Host "   是否继续启动? (y/n)"
    if ($continue -ne "y") {
        exit 1
    }
}

# 启动Docker服务
Write-Host ""
Write-Host "3. 启动Docker服务..." -ForegroundColor Yellow
Write-Host "   正在启动 MySQL, Redis, RabbitMQ..." -ForegroundColor Gray

docker-compose up -d

if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✓ Docker服务启动命令执行成功" -ForegroundColor Green
} else {
    Write-Host "   ✗ Docker服务启动失败!" -ForegroundColor Red
    exit 1
}

# 等待服务启动
Write-Host ""
Write-Host "4. 等待服务启动完成..." -ForegroundColor Yellow
Write-Host "   提示: 首次启动需要下载镜像，可能需要5-10分钟" -ForegroundColor Gray
Write-Host ""

$maxWait = 60  # 最多等待60秒
$waited = 0
$allHealthy = $false

while ($waited -lt $maxWait) {
    Start-Sleep -Seconds 2
    $waited += 2
    
    $status = docker-compose ps --format json | ConvertFrom-Json
    $healthy = 0
    $total = 0
    
    foreach ($service in $status) {
        $total++
        if ($service.Health -eq "healthy") {
            $healthy++
        }
    }
    
    Write-Host "   等待中... ($healthy/$total 服务健康) [${waited}s]" -ForegroundColor Gray
    
    if ($healthy -eq $total -and $total -gt 0) {
        $allHealthy = $true
        break
    }
}

Write-Host ""

if ($allHealthy) {
    Write-Host "   ✓ 所有服务启动成功!" -ForegroundColor Green
} else {
    Write-Host "   ⚠ 服务可能尚未完全启动，请手动检查" -ForegroundColor Yellow
}

# 显示服务状态
Write-Host ""
Write-Host "5. 服务状态:" -ForegroundColor Yellow
docker-compose ps

# 显示服务信息（从环境变量读取）
Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "服务访问信息" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "MySQL:" -ForegroundColor Yellow
Write-Host "  地址: localhost:$MYSQL_PORT" -ForegroundColor White
Write-Host "  用户: root / $MYSQL_USER" -ForegroundColor White
Write-Host "  密码: $MYSQL_ROOT_PASSWORD / $MYSQL_PASSWORD" -ForegroundColor White
Write-Host "  数据库: $MYSQL_DATABASE" -ForegroundColor White
Write-Host ""
Write-Host "Redis:" -ForegroundColor Yellow
Write-Host "  地址: localhost:$REDIS_PORT" -ForegroundColor White
Write-Host "  密码: $REDIS_PASSWORD" -ForegroundColor White
Write-Host ""
Write-Host "RabbitMQ:" -ForegroundColor Yellow
Write-Host "  AMQP: localhost:$RABBITMQ_PORT" -ForegroundColor White
Write-Host "  管理界面: http://localhost:$RABBITMQ_MANAGEMENT_PORT" -ForegroundColor White
Write-Host "  用户: $RABBITMQ_DEFAULT_USER" -ForegroundColor White
Write-Host "  密码: $RABBITMQ_DEFAULT_PASS" -ForegroundColor White
Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# 测试连接
Write-Host "6. 测试服务连接..." -ForegroundColor Yellow

# 测试MySQL
Write-Host "   测试MySQL连接..." -ForegroundColor Gray
$mysqlTest = docker exec "$COMPOSE_PROJECT_NAME-mysql" mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -e "SELECT 1;" 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✓ MySQL连接成功" -ForegroundColor Green
} else {
    Write-Host "   ⚠ MySQL连接失败，请稍后重试" -ForegroundColor Yellow
}

# 测试Redis
Write-Host "   测试Redis连接..." -ForegroundColor Gray
$redisTest = docker exec "$COMPOSE_PROJECT_NAME-redis" redis-cli -a "$REDIS_PASSWORD" ping 2>&1
if ($redisTest -like "*PONG*") {
    Write-Host "   ✓ Redis连接成功" -ForegroundColor Green
} else {
    Write-Host "   ⚠ Redis连接失败，请稍后重试" -ForegroundColor Yellow
}

# 测试RabbitMQ
Write-Host "   测试RabbitMQ连接..." -ForegroundColor Gray
$rabbitTest = docker exec "$COMPOSE_PROJECT_NAME-rabbitmq" rabbitmq-diagnostics ping 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✓ RabbitMQ连接成功" -ForegroundColor Green
} else {
    Write-Host "   ⚠ RabbitMQ连接失败，请稍后重试" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "✓ Docker环境启动完成!" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "下一步操作:" -ForegroundColor Yellow
Write-Host "  1. 返回项目根目录（medical-model-platform）" -ForegroundColor White
Write-Host "  2. 在IDEA中配置环境（参考: Docker环境启动和IDEA配置指南.md）" -ForegroundColor White
Write-Host "  3. 启动Spring Boot应用: mvn spring-boot:run -Dspring-boot.run.profiles=local" -ForegroundColor White
Write-Host "  4. 访问API文档: http://localhost:8080/api/swagger-ui.html" -ForegroundColor White
Write-Host ""
Write-Host "常用命令:" -ForegroundColor Yellow
Write-Host "  查看日志: docker-compose logs -f [service_name]" -ForegroundColor White
Write-Host "  停止服务: .\stop-docker.ps1" -ForegroundColor White
Write-Host "  重启服务: docker-compose restart [service_name]" -ForegroundColor White
Write-Host ""
Write-Host "配置文件位置:" -ForegroundColor Yellow
Write-Host "  环境变量: .\docker\.env" -ForegroundColor White
Write-Host "  Docker配置: .\docker\docker-compose.yml" -ForegroundColor White
Write-Host ""

