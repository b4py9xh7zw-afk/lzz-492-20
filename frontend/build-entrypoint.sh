#!/bin/sh
# 前端构建入口脚本
# 构建完成后将产物复制到输出目录

set -e

echo "开始构建前端项目..."

# 构建项目（已经在 Dockerfile 中完成）
# 这里只需要复制构建产物

if [ -d "/app/dist" ]; then
    echo "复制构建产物到输出目录..."
    mkdir -p /output
    cp -r /app/dist/* /output/
    echo "构建产物复制完成！"
    ls -la /output/
else
    echo "错误：构建产物目录不存在！"
    exit 1
fi
