# Stage 1: 构建阶段
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# 复制Maven配置
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# 下载依赖 (利用Docker层缓存)
RUN chmod +x mvnw && ./mvnw dependency:go-offline || true

# 复制源代码
COPY src ./src

# 构建应用 (跳过测试以加快构建速度)
RUN ./mvnw clean package -DskipTests

# Stage 2: 运行阶段
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="hanjor <hanjor@qq.com>"
LABEL description="医学影像模型管理平台"
LABEL version="3.0.0"

WORKDIR /app

# 安装必要工具和设置时区
RUN apk add --no-cache tzdata curl bash && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata

# 从构建阶段复制JAR文件
COPY --from=builder /app/target/medical-model-platform.jar app.jar

# 创建日志目录和数据目录
RUN mkdir -p /app/logs /app/data && \
    chmod -R 755 /app

# 暴露端口
EXPOSE 8080

# 环境变量
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs/heapdump.hprof" \
    SPRING_PROFILES_ACTIVE="prod"

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/api/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar --spring.profiles.active=$SPRING_PROFILES_ACTIVE"]

