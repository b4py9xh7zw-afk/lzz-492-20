#!/bin/bash

echo "=========================================="
echo "脚手架项目启动脚本"
echo "=========================================="

# 检查MySQL是否运行
echo "📦 检查MySQL服务..."
if ! mysqladmin ping -h localhost --silent 2>/dev/null; then
    echo "❌ MySQL未运行，请先启动MySQL服务"
    echo "   请确保MySQL已安装并运行在localhost:3306"
    exit 1
fi

echo "✅ MySQL已就绪"

# 启动后端
echo "🚀 启动后端服务..."
cd backend
mvn spring-boot:run &
BACKEND_PID=$!
cd ..

# 等待后端启动
echo "⏳ 等待后端服务启动..."
sleep 15

# 启动前端
echo "🚀 启动前端服务..."
cd frontend
npm install
npm run dev &
FRONTEND_PID=$!
cd ..

echo "=========================================="
echo "✅ 服务启动完成！"
echo "=========================================="
echo "后端服务: http://localhost:8088"
echo "前端PC端: http://localhost:3000/pc"
echo "前端H5端: http://localhost:3000/h5"
echo "=========================================="
echo "按 Ctrl+C 停止所有服务"

# 等待用户中断
trap "kill $BACKEND_PID $FRONTEND_PID; exit" INT
wait
