@echo off
chcp 65001 >nul

echo ==========================================
echo 脚手架项目启动脚本
echo ==========================================

REM 检查MySQL是否运行
echo 📦 检查MySQL服务...
mysqladmin ping -h localhost --silent >nul 2>&1
if errorlevel 1 (
    echo ❌ MySQL未运行，请先启动MySQL服务
    echo    请确保MySQL已安装并运行在localhost:3306
    pause
    exit /b 1
)

echo ✅ MySQL已就绪

REM 启动后端
echo 🚀 启动后端服务...
cd backend
start "后端服务" cmd /k "mvn spring-boot:run"
cd ..

REM 等待后端启动
echo ⏳ 等待后端服务启动...
timeout /t 15 /nobreak >nul

REM 启动前端
echo 🚀 启动前端服务...
cd frontend
call npm install
start "前端服务" cmd /k "npm run dev"
cd ..

echo ==========================================
echo ✅ 服务启动完成！
echo ==========================================
echo 后端服务: http://localhost:8088
echo 前端PC端: http://localhost:3000/pc
echo 前端H5端: http://localhost:3000/h5
echo ==========================================
pause
