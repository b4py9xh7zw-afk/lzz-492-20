# 后端 Dockerfile
# 使用多阶段构建，减小最终镜像体积

# ==================== 构建阶段 ====================
FROM maven:3.9-eclipse-temurin-17 AS build

# 设置工作目录
WORKDIR /app

# 复制 Maven 配置文件（阿里云镜像源）
COPY settings.xml /root/.m2/settings.xml

# 先复制 pom.xml（利用 Docker 层缓存）
COPY pom.xml .

# 预下载依赖（只要 pom.xml 不变，这一层就会被 Docker 缓存）
RUN mvn -s /root/.m2/settings.xml dependency:go-offline -B

# 复制源代码
COPY src ./src

# 构建项目（跳过测试，加快构建速度）
RUN mvn -s /root/.m2/settings.xml package -DskipTests -B

# ==================== 运行阶段 ====================
FROM amazoncorretto:17-alpine

# 设置工作目录
WORKDIR /app

# 创建文件上传目录
RUN mkdir -p /app/uploads

# 从构建阶段复制 JAR 包
COPY --from=build /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8088

# 设置 JVM 参数（优化内存使用）
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
