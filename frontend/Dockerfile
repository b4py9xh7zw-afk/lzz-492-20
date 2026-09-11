# 前端 Dockerfile
# 使用多阶段构建，减小最终镜像体积

# ==================== 构建阶段 ====================
FROM node:20-alpine AS builder

# 设置工作目录
WORKDIR /app

# 配置 npm 使用淘宝镜像源（加速国内下载）
RUN npm config set registry https://registry.npmmirror.com

# 先复制 package 文件（利用 Docker 层缓存）
COPY package*.json ./

# 使用 npm ci 安装依赖（比 npm install 更快，且确定性安装）
# 注意：必须确保 package-lock.json 已提交到仓库
RUN npm ci

# 复制构建入口脚本（在复制源代码之前）
COPY build-entrypoint.sh /app/build-entrypoint.sh
RUN chmod +x /app/build-entrypoint.sh

# 复制源代码
COPY . .

# 构建前端项目
RUN npm run build

# ==================== 生产阶段 ====================
FROM nginx:alpine

# 复制构建产物到 nginx 静态文件目录
COPY --from=builder /app/dist /usr/share/nginx/html

# 暴露端口（nginx 默认 80，但会在 docker-compose 中映射到 3000）
EXPOSE 80

# 启动 nginx
CMD ["nginx", "-g", "daemon off;"]
