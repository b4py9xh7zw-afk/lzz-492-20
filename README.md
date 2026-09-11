# 工作管理系统 - 前后端分离项目

## 📋 项目简介

**工作管理系统** 是一个前后端分离的 Java 项目，采用 SpringBoot + Vue3 技术栈，支持 PC 端和 H5 端双端适配。项目包含用户管理、文件管理和工作管理三大核心功能模块，提供现代化的UI/UX设计，界面美观、交互流畅。

**项目特色**：
- 🐳 **Docker 一键启动**：支持 Docker Compose 一键启动所有服务（数据库、后端、前端、Nginx）
- 🌐 **Nginx 反向代理**：前端通过 Nginx 代理访问后端 API，生产环境就绪
- 🔄 **CI/CD 自动化**：GitHub Actions 自动构建、测试，确保代码质量
- 📱 **多端适配**：同时支持 PC 端（Element Plus）和 H5 端（Vant）
- 🗄️ **中文支持**：数据库完全支持 UTF-8 编码，无乱码问题

## 🛠️ 技术栈

### 后端技术栈
- **框架**: SpringBoot 2.7.18
- **ORM**: MyBatis-Plus 3.5.5
- **数据库**: MySQL 8.0+
- **连接池**: Druid 1.2.20
- **工具类**: Hutool 5.8.23
- **JSON处理**: Fastjson2 2.0.47
- **JDK版本**: JDK 17 (Amazon Corretto 17 Alpine)
- **构建工具**: Maven 3.9+
- **容器化**: Docker + Docker Compose

### 前端技术栈
- **框架**: Vue 3.4.15
- **构建工具**: Vite 5.0.11
- **路由**: Vue Router 4.2.5
- **状态管理**: Pinia 2.1.7
- **PC端UI**: Element Plus 2.5.4
- **H5端UI**: Vant 4.8.3
- **样式**: Tailwind CSS 3.4.1
- **HTTP客户端**: Axios 1.6.5
- **日期处理**: Day.js 1.11.19
- **Node版本**: Node 20+
- **Web服务器**: Nginx Alpine

### 基础设施
- **数据库**: MySQL 8.0
- **反向代理**: Nginx Alpine
- **容器编排**: Docker Compose 3.8
- **CI/CD**: GitHub Actions

## 📁 项目结构

```
jobs/
├── backend/                          # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/scaffolding/
│   │   │   │       ├── ScaffoldingApplication.java  # 启动类
│   │   │   │       ├── common/                      # 通用类
│   │   │   │       │   ├── Result.java              # 统一返回结果
│   │   │   │       │   └── PageResult.java          # 分页结果
│   │   │   │       ├── config/                      # 配置类
│   │   │   │       │   ├── CorsConfig.java          # 跨域配置
│   │   │   │       │   ├── MyBatisPlusConfig.java   # MyBatis-Plus配置
│   │   │   │       │   └── WebMvcConfig.java         # Web MVC配置
│   │   │   │       ├── controller/                  # 控制器层
│   │   │   │       │   ├── FileController.java       # 文件管理控制器
│   │   │   │       │   ├── WorkController.java      # 工作管理控制器
│   │   │   │       │   └── UserController.java       # 用户管理控制器
│   │   │   │       ├── entity/                      # 实体层
│   │   │   │       │   ├── BaseEntity.java          # 基础实体类
│   │   │   │       │   ├── FileInfo.java            # 文件信息实体
│   │   │   │       │   ├── Work.java                # 工作管理实体
│   │   │   │       │   └── User.java                # 用户实体
│   │   │   │       ├── mapper/                      # 数据访问层
│   │   │   │       │   ├── FileInfoMapper.java      # 文件信息Mapper
│   │   │   │       │   ├── WorkMapper.java          # 工作管理Mapper
│   │   │   │       │   └── UserMapper.java          # 用户Mapper
│   │   │   │       ├── service/                     # 服务层
│   │   │   │       │   ├── FileInfoService.java      # 文件信息服务接口
│   │   │   │       │   ├── WorkService.java         # 工作管理服务接口
│   │   │   │       │   ├── UserService.java         # 用户服务接口
│   │   │   │       │   └── impl/                    # 服务实现类
│   │   │   │       │       ├── FileInfoServiceImpl.java
│   │   │   │       │       ├── WorkServiceImpl.java
│   │   │   │       │       └── UserServiceImpl.java
│   │   │   │       ├── exception/                   # 异常处理
│   │   │   │       └── utils/                       # 工具类
│   │   │   └── resources/
│   │   │       └── application.yml                  # 应用配置
│   │   └── pom.xml                                   # Maven配置
│   ├── Dockerfile                                    # 后端 Dockerfile
│   └── settings.xml                                  # Maven 镜像源配置
├── frontend/                          # 前端项目
│   ├── src/
│   │   ├── api/                      # API接口
│   │   │   ├── file.js               # 文件管理API
│   │   │   ├── work.js                # 工作管理API
│   │   │   └── user.js                # 用户管理API
│   │   ├── views/                    # 页面
│   │   │   ├── pc/                   # PC端页面
│   │   │   │   ├── Login.vue         # 登录页面
│   │   │   │   ├── File.vue          # 文件管理页面
│   │   │   │   ├── Work.vue          # 工作管理页面
│   │   │   │   └── User.vue          # 用户管理页面
│   │   │   └── h5/                   # H5端页面
│   │   │       ├── Login.vue         # 登录页面
│   │   │       ├── File.vue          # 文件管理页面
│   │   │       ├── Work.vue          # 工作管理页面
│   │   │       └── User.vue          # 用户管理页面
│   │   ├── layouts/                 # 布局组件
│   │   │   ├── PcLayout.vue          # PC端布局
│   │   │   └── H5Layout.vue          # H5端布局
│   │   ├── router/                   # 路由配置
│   │   ├── store/                    # 状态管理
│   │   └── utils/                    # 工具类
│   ├── package.json
│   ├── package-lock.json             # npm 锁文件（必须提交）
│   └── Dockerfile                    # 前端 Dockerfile
├── nginx/                             # Nginx 配置
│   └── nginx.conf                     # Nginx 配置文件
├── database/                          # 数据库脚本
│   └── init.sql                      # 初始化SQL（支持UTF-8中文）
├── .github/                           # GitHub配置
│   └── workflows/
│       └── ci.yml                    # GitHub Actions CI/CD配置
├── docker-compose.yml                 # Docker Compose 配置文件
├── start.sh                           # Linux/macOS 启动脚本
├── start.bat                          # Windows 启动脚本
└── README.md                          # 项目说明文档
```

## 🚀 快速启动

### 方式一：Docker Compose 一键启动（推荐）⭐

**这是最简单快捷的启动方式，无需安装 JDK、Node.js、Maven 等环境，一键启动所有服务！**

#### 前置要求
- 安装 [Docker](https://www.docker.com/get-started) 和 [Docker Compose](https://docs.docker.com/compose/install/)
- 确保 Docker 服务已启动

#### 启动步骤

1. **克隆项目**（如果还没有）
```bash
git clone <repository-url>
cd jobs
```

2. **一键启动所有服务**
```bash
docker-compose up -d
```

3. **查看服务状态**
```bash
docker-compose ps
```

4. **查看日志**（可选）
```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f mysql
docker-compose logs -f nginx
```

5. **访问应用**
   - **前端访问地址**：http://localhost:3000
     - PC端登录：http://localhost:3000/pc/login
     - H5端登录：http://localhost:3000/h5/login
     - 默认会跳转到登录页面
   - **后端API地址**：http://localhost:8088/api
   - **健康检查**：http://localhost:8088/api/actuator/health
   - **测试账号**：
     - 账号：`admin`
     - 密码：`123456`

#### 停止服务
```bash
# 停止所有服务（保留数据）
docker-compose stop

# 停止并删除容器（保留数据卷）
docker-compose down

# 停止并删除容器和数据卷（完全清理）
docker-compose down -v
```

#### Docker 服务说明

| 服务名称 | 容器名称 | 端口 | 说明 |
|---------|---------|------|------|
| mysql | scaffolding-mysql | 3306 | MySQL 数据库服务 |
| backend | scaffolding-backend | 8088 | Spring Boot 后端服务 |
| nginx | scaffolding-nginx | 3000 | Nginx 反向代理（前端静态文件 + API代理） |
| frontend-builder | scaffolding-frontend-builder | - | 前端构建服务（临时，构建完成后自动删除） |

#### Docker 数据卷说明

- `mysql_data`: MySQL 数据持久化存储
- `backend_uploads`: 后端文件上传目录
- `frontend_dist`: 前端构建产物

#### 注意事项

1. **首次启动**：首次启动需要构建 Docker 镜像和下载依赖，可能需要 5-10 分钟，请耐心等待
2. **数据库初始化**：数据库会在首次启动时自动执行 `database/init.sql` 脚本，创建表结构和测试数据
3. **中文支持**：数据库已配置 UTF-8 编码（utf8mb4），完全支持中文，不会出现乱码
4. **端口占用**：确保 3000、3306、8088 端口未被占用
5. **文件上传**：文件上传目录已配置为 Docker 数据卷，数据会持久化保存

### 方式二：本地开发环境启动

#### 环境要求
- **JDK**: 17+
- **Node.js**: 20+
- **Maven**: 3.6+
- **MySQL**: 8.0+
- **IDE**: IntelliJ IDEA / VSCode

#### 启动步骤

1. **启动数据库**
   - 安装并启动 MySQL 8.0+
   - 创建数据库：
   ```sql
   CREATE DATABASE scaffolding_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
   - 注意：数据库名称在 `application.yml` 中配置为 `scaffolding_db`
   - 执行初始化脚本：`database/init.sql`

2. **启动后端**
   ```bash
   cd backend
   mvn clean install
   mvn spring-boot:run
   ```
   - API地址：http://localhost:8088/api
   - 健康检查：http://localhost:8088/api/actuator/health

3. **启动前端**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   - PC端：http://localhost:3000/pc
   - H5端：http://localhost:3000/h5

### 方式三：使用启动脚本

**Linux/macOS:**
```bash
chmod +x start.sh
./start.sh
```

**Windows:**
```cmd
start.bat
```

## 📦 功能模块

### 1. 用户管理模块
- **用户登录**: 支持用户名和密码登录，登录页面显示测试账号信息
- **用户新增**: 支持新增用户，包含账号、密码、昵称，账号唯一性验证
- **用户编辑**: 支持编辑用户信息，账号唯一性验证（排除当前用户）
- **用户删除**: 支持删除用户记录（逻辑删除）
- **用户查询**: 支持用户列表查询、分页、条件筛选（按账号、昵称）
- **重置密码**: 支持重置指定用户的密码
- **账号去重**: 新增和编辑时自动检查账号是否重复，确保账号唯一性
- **默认账号**: 系统默认创建admin账号（密码：123456）

### 2. 文件管理模块
- **文件上传**: 仅支持图片文件上传（jpg、jpeg、png、gif、bmp、webp），最大100MB
- **文件预览**: 支持图片在线预览
- **文件删除**: 支持文件物理删除和数据库记录删除
- **文件列表**: 支持文件列表查询、分页、条件筛选（按文件名称、文件类型）
- **上传限制**: 前端和后端双重验证，只允许上传图片文件

### 3. 工作管理模块
- **工作新增**: 支持新增工作，包含工作名称、工作内容、工作状态、优先级、工作时间等
- **工作编辑**: 支持编辑工作信息
- **工作删除**: 支持删除工作记录
- **工作查询**: 支持工作列表查询、分页、条件筛选（按工作名称、工作状态、优先级）
- **工作状态**: 支持待处理、进行中、已完成、已取消四种状态
- **优先级管理**: 支持低、普通、高、紧急四种优先级
- **时间管理**: 支持工作时间、开始时间、结束时间设置

## 🔌 API接口说明

### 统一返回格式
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1234567890
}
```

### 分页返回格式
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 100,
    "records": [],
    "current": 1,
    "size": 10
  },
  "timestamp": 1234567890
}
```

### 主要接口

#### 用户管理
- `POST /api/user/login` - 用户登录
- `GET /api/user/page` - 分页查询用户（支持按账号、昵称筛选）
- `POST /api/user` - 新增用户（账号唯一性验证）
- `PUT /api/user/{id}` - 更新用户（账号唯一性验证，排除当前用户）
- `DELETE /api/user/{id}` - 删除用户
- `PUT /api/user/{id}/reset-password` - 重置密码

#### 文件管理
- `POST /api/file/upload` - 上传文件（仅支持图片：jpg、jpeg、png、gif、bmp、webp）
- `GET /api/file/preview/{id}` - 预览文件
- `DELETE /api/file/{id}` - 删除文件
- `GET /api/file/page` - 分页查询文件（支持按文件名称、文件类型筛选）

#### 工作管理
- `POST /api/work` - 新增工作
- `PUT /api/work/{id}` - 更新工作
- `DELETE /api/work/{id}` - 删除工作
- `GET /api/work/{id}` - 查询工作详情
- `GET /api/work/page` - 分页查询工作（支持按工作名称、工作状态、优先级筛选）

## ⚙️ 配置说明

### Docker 环境配置

**docker-compose.yml** 已配置好所有服务，包括：
- MySQL 数据库（自动初始化，支持中文）
- 后端服务（Spring Boot）
- 前端服务（Nginx + Vue 构建产物）
- 服务间网络通信

### 后端配置

**应用配置** (`application.yml`):
- Docker 环境使用环境变量自动配置
- 本地开发环境使用默认配置
- 数据库连接、文件上传路径等均可通过环境变量覆盖

### 前端配置

**Nginx 配置** (`nginx/nginx.conf`):
- 前端静态文件服务（端口 3000）
- API 反向代理（`/api` -> `http://backend:8088`）
- 不拦截任何请求头（保留所有原始请求头）
- 支持 Vue Router History 模式
- 文件上传大小限制：100MB

**开发环境** (`vite.config.js`):
- 开发服务器端口：3000
- API代理：`/api` -> `http://localhost:8088`

## 🗄️ 数据库设计

数据库使用 **UTF-8 编码**（utf8mb4），完全支持中文存储，不会出现乱码问题。

### 核心表结构

- **user**: 用户表
  - 存储用户信息：账号（username，唯一索引）、密码（password，不加密）、昵称（nickname）
  - 默认插入admin账号（username: admin, password: 123456）
  - 账号字段设置唯一索引，确保账号不重复

- **file_info**: 文件信息表
  - 存储文件的基本信息：文件名称、原始文件名、文件路径、文件大小、文件类型、文件扩展名、上传人信息等
  
- **work**: 工作管理表
  - 存储工作信息：工作名称、工作内容、工作状态（待处理/进行中/已完成/已取消）、优先级（低/普通/高/紧急）、工作时间、开始时间、结束时间、备注等

详细表结构请查看 `database/init.sql` 文件。

## 🔄 GitHub Actions CI/CD

项目已配置 GitHub Actions 自动化 CI/CD 流程，代码提交后会自动执行：

1. **后端构建和测试**: 使用 JDK 17 (Amazon Corretto) 构建后端项目并运行测试
2. **前端构建**: 使用 Node.js 20 构建前端项目（使用 npm ci 加速）
3. **Docker 镜像构建测试**: 构建后端和前端 Docker 镜像
4. **Docker Compose 集成测试**: 测试 Docker Compose 配置和服务启动

工作流文件位置：`.github/workflows/ci.yml`

**查看 CI 状态**: 在 GitHub 仓库的 Actions 标签页查看构建状态，确保显示为 ✅ Green。

### CI/CD 流程说明

- **触发条件**: 推送到 main/master 分支或创建 Pull Request
- **构建优化**: 
  - 使用 Maven 阿里云镜像源加速构建
  - 使用 npm 淘宝镜像源加速前端依赖下载
  - 使用 npm ci 代替 npm install（更快、更可靠）
  - Docker 层缓存优化
- **测试覆盖**: 后端单元测试、前端构建测试、Docker 构建测试、集成测试

## 🐳 Docker 镜像源配置

### 镜像源说明

项目已配置国内镜像源，加速构建：

1. **Docker 镜像**: 使用官方 Docker Hub 镜像
2. **npm 依赖**: 使用淘宝镜像源（https://registry.npmmirror.com）
3. **Maven 依赖**: 使用阿里云镜像源（https://maven.aliyun.com/repository/public）

### 使用的 Docker 镜像

| 服务 | 镜像 | 说明 |
|------|------|------|
| MySQL | `mysql:8.0` | 数据库服务 |
| 后端构建 | `maven:3.9-eclipse-temurin-17` | Maven 构建工具 |
| 后端运行 | `amazoncorretto:17-alpine` | JDK 17 运行环境 |
| 前端构建 | `node:20-alpine` | Node.js 构建环境 |
| 前端运行 | `nginx:alpine` | Nginx Web 服务器 |

## 📝 开发规范

### 代码规范
- 后端代码遵循阿里巴巴Java开发手册
- 前端代码遵循Vue官方编码规范
- 关键代码添加中文注释

### 命名规范
- 类名：大驼峰命名（PascalCase）
- 方法名/变量名：小驼峰命名（camelCase）
- 常量名：全大写下划线分隔（UPPER_SNAKE_CASE）
- 数据库表名/字段名：小写下划线分隔（snake_case）

### 接口规范
- RESTful API设计
- 统一返回结果格式
- 统一异常处理
- 请求头包含Content-Type: application/json

## ❓ 常见问题

### 1. Docker 启动失败

**问题**: `docker-compose up` 执行失败

**解决方案**:
- 检查 Docker 和 Docker Compose 是否已安装并启动
- 检查端口 3000、3306、8088 是否被占用
- 查看日志：`docker-compose logs -f`
- 尝试清理后重新启动：`docker-compose down -v && docker-compose up -d`

### 2. 数据库连接失败

**问题**: 后端无法连接数据库

**解决方案**:
- 检查 MySQL 容器是否正常启动：`docker-compose ps`
- 查看 MySQL 日志：`docker-compose logs mysql`
- 等待数据库初始化完成（首次启动需要时间）
- 检查数据库连接配置是否正确

### 3. 前端无法访问后端API

**问题**: 前端页面可以访问，但API请求失败

**解决方案**:
- 检查后端服务是否正常启动：`docker-compose ps`
- 查看后端日志：`docker-compose logs backend`
- 检查 Nginx 配置是否正确：`docker-compose logs nginx`
- 确认后端健康检查：`curl http://localhost:8088/api/actuator/health`

### 4. 中文乱码问题

**问题**: 数据库中的中文显示为乱码

**解决方案**:
- 数据库已配置 utf8mb4 字符集，正常情况下不会出现乱码
- 如果出现乱码，检查数据库连接 URL 是否包含 `useUnicode=true&characterEncoding=utf8`
- 检查前端页面编码是否为 UTF-8

### 5. 文件上传失败

**问题**: 文件上传功能无法使用

**解决方案**:
- 检查文件类型是否为图片（jpg、jpeg、png、gif、bmp、webp），系统只支持图片上传
- 检查文件大小是否超过 100MB 限制
- 查看后端日志确认错误信息：`docker-compose logs backend`
- 检查文件上传目录权限

### 8. 登录失败

**问题**: 无法登录系统

**解决方案**:
- 确认使用正确的测试账号：账号 `admin`，密码 `123456`
- 检查后端服务是否正常启动：`docker-compose logs backend`
- 确认数据库已初始化并包含用户表数据
- 查看后端日志确认具体错误信息

### 6. 首次启动很慢

**问题**: 首次 `docker-compose up` 执行很慢

**解决方案**:
- 这是正常现象，首次启动需要：
  - 下载 Docker 镜像（MySQL、Nginx、Maven、Node.js 等）
  - 构建后端和前端 Docker 镜像
  - 下载 Maven 和 npm 依赖
  - 初始化数据库
- 通常需要 5-10 分钟，请耐心等待
- 后续启动会快很多（利用 Docker 缓存）

### 9. GitHub Actions 构建失败

**问题**: GitHub Actions CI 状态显示为 Red

**解决方案**:
- 检查代码是否有语法错误
- 确保 `package-lock.json` 已提交到仓库
- 查看 GitHub Actions 日志定位具体错误
- 确保所有测试用例通过

## ✨ 技术亮点

- ✅ **Docker 一键启动**: 无需安装任何环境，一键启动所有服务
- ✅ **前后端分离**: 完全分离的架构设计
- ✅ **多端适配**: 同时支持 PC 端和 H5 端
- ✅ **用户认证**: 完整的用户登录和用户管理功能，支持账号去重验证
- ✅ **文件上传限制**: 仅支持图片上传，前后端双重验证
- ✅ **现代化UI/UX**: 遵循现代设计规范，界面美观、交互流畅
  - 圆角、阴影、渐变背景等视觉元素
  - Hover 和 Active 状态反馈
  - 骨架屏加载状态
  - 响应式布局，完美适配PC和移动端
- ✅ **中文支持**: 数据库完全支持 UTF-8，无乱码问题
- ✅ **CI/CD**: GitHub Actions 自动化构建和测试
- ✅ **生产就绪**: Nginx 反向代理、健康检查、日志记录等生产环境配置
- ✅ **开发友好**: 支持热重载和代理配置
- ✅ **镜像优化**: 使用 Alpine 镜像减小体积，多阶段构建优化

## 📄 许可证

MIT License

## 📮 联系方式

如有问题或建议，欢迎提交 Issue 或 Pull Request。

---

**Happy Coding! 🚀**
