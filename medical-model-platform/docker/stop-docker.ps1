# Docker 环境停止脚本
# 医学影像模型管理平台

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "医学影像模型管理平台 - Docker环境停止" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# 进入docker目录（脚本所在目录）
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptPath

Write-Host "当前目录: $scriptPath" -ForegroundColor Gray
Write-Host ""

# 检查是否有运行中的容器
$runningContainers = docker-compose ps -q
if (-not $runningContainers) {
    Write-Host "没有运行中的Docker容器" -ForegroundColor Yellow
    Write-Host ""
    exit 0
}

# 询问是否删除数据
Write-Host "停止方式:" -ForegroundColor Yellow
Write-Host "  1. 仅停止容器（保留数据）- 推荐" -ForegroundColor White
Write-Host "  2. 停止并删除数据卷（清空所有数据）" -ForegroundColor White
Write-Host ""

$choice = Read-Host "请选择 (1/2，默认为1)"

if ($choice -eq "2") {
    Write-Host ""
    Write-Host "⚠ 警告: 此操作将删除所有数据（MySQL数据、Redis缓存、RabbitMQ队列）" -ForegroundColor Red
    $confirm = Read-Host "确认删除所有数据? (yes/no)"
    
    if ($confirm -eq "yes") {
        Write-Host ""
        Write-Host "正在停止并删除所有容器和数据卷..." -ForegroundColor Yellow
        docker-compose down -v
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✓ 已停止并清空所有数据" -ForegroundColor Green
            Write-Host ""
            Write-Host "提示: 数据目录仍然存在，如需完全清理请手动删除:" -ForegroundColor Yellow
            Write-Host "  - .\mysql\data\" -ForegroundColor Gray
            Write-Host "  - .\redis\data\" -ForegroundColor Gray
            Write-Host "  - .\rabbitmq\data\" -ForegroundColor Gray
        } else {
            Write-Host "✗ 停止失败" -ForegroundColor Red
        }
    } else {
        Write-Host "已取消操作" -ForegroundColor Yellow
    }
} else {
    Write-Host ""
    Write-Host "正在停止Docker服务..." -ForegroundColor Yellow
    docker-compose down
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Docker服务已停止（数据已保留）" -ForegroundColor Green
    } else {
        Write-Host "✗ 停止失败" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "重新启动服务:" -ForegroundColor Yellow
Write-Host "  .\start-docker.ps1" -ForegroundColor White
Write-Host ""
Write-Host "查看所有容器（包括已停止的）:" -ForegroundColor Yellow
Write-Host "  docker ps -a" -ForegroundColor White
Write-Host ""

