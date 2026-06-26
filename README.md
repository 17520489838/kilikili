# Kilikili — 视频分享平台  (前端完整代码已推送至另一个仓库kilikili_front)

> **一个功能完备的全栈视频分享平台，灵感来源于 Bilibili，支持视频上传转码、弹幕互动、用户社交与管理后台。**  
> 面向创作者与观众，提供流畅的视频消费与创作体验。

![Java](https://img.shields.io/badge/Java-8-%23ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-2.7.18-%236DB33F?logo=springboot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-%234479A1?logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7-%23DC382D?logo=redis&logoColor=white)
![Vue](https://img.shields.io/badge/Vue-3.4-%234FC08D?logo=vue.js&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-5.4-%233178C6?logo=typescript&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-5.2-%23646CFF?logo=vite&logoColor=white)
![Element Plus](https://img.shields.io/badge/Element_Plus-2.5-%23409EFF?logo=element&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

---

## 目录

- [项目简介](#项目简介)
- [项目背景与动机](#项目背景与动机)
- [核心功能](#核心功能)
- [技术栈与架构](#技术栈与架构)
- [挑战与解决方案](#挑战与解决方案)
- [快速开始](#快速开始)
- [使用示例](#使用示例)
- [测试](#测试)
- [项目结构](#项目结构)
- [贡献指南](#贡献指南)
- [许可证](#许可证)
- [未来改进](#未来改进)

---

## 项目简介

Kilikili 是一个前后端分离的视频分享平台，提供完整的视频上传、转码、播放、弹幕互动、用户社交以及后台管理能力。平台参考了 Bilibili 的核心交互模式（弹幕、投币、收藏等），采用现代 Web 技术栈构建，适用于学习视频平台开发、展示全栈能力或搭建轻量级视频社区。

## 项目背景与动机

视频分享平台是互联网最复杂也最具代表性的应用场景之一，涉及**大文件上传、媒体转码、实时评论（弹幕）、社交互动、内容审核**等多个技术领域。创建 Kilikili 的目标是：

- 完整实践全栈开发流程，从前端交互到后端服务、从数据库设计到容器化部署
- 探索视频平台的核心技术难点：分片上传、异步转码、弹幕渲染引擎
- 提供一个业务较为完整的综合性项目，展示架构设计能力、技术选型思路与编码质量
- 目标用户：希望搭建个人视频站点的站长、对视频平台有兴趣的开发者、以及技术面试中的作品展示

## 核心功能

### 用户端（前端 Web + 后端 Web API）

| 功能 | 描述 |
|------|------|
| **用户注册/登录** | 邮箱注册、算术验证码、自动登录/Token 续期 |
| **视频浏览** | 分类导航、分页加载、推荐视频、热门榜单 |
| **视频搜索** | 关键词搜索、热搜排行榜（Redis ZSet） |
| **视频播放** | 自定义 HTML5 播放器、进度拖拽、音量控制、全屏 |
| **弹幕系统** | Canvas 实时弹幕渲染、滚动/顶部/底部模式、自定义颜色 |
| **视频互动** | 点赞、收藏、投币（虚拟货币）、分享 |
| **评论系统** | 多级评论、置顶、用户自主删除 |
| **关注系统** | 关注/取关作者、粉丝列表、关注列表 |
| **创作者中心** | 视频管理、分片上传、发布配置、数据统计 |
| **消息通知** | WebSocket 实时推送（评论回复、点赞、关注等） |
| **用户主页** | 个人资料编辑、播放历史、收藏夹、主题切换 |
| **暗黑模式** | CSS 变量驱动的暗黑/亮色主题切换，跟随系统偏好 |

### 管理端（前端 Admin + 后端 Admin API）

| 功能 | 描述 |
|------|------|
| **仪表盘** | ECharts 实时数据统计（用户数、视频数、播放量趋势） |
| **视频管理** | 视频列表、内容审核（通过/拒绝）、推荐设置、删除 |
| **用户管理** | 用户列表、状态管理（正常/禁用） |
| **分类管理** | 二级分类增删改查、排序、图标与背景图 |
| **弹幕管理** | 弹幕列表浏览与删除 |
| **评论管理** | 评论列表、审核与删除 |
| **系统设置** | 全局配置项管理 |
| **操作日志** | 后台操作审计日志 |

## 技术栈与架构

### 系统架构图

```
┌─────────────────────────────────────────────────────────────┐
│                       浏览器                                │
│  ┌─────────────────────┐  ┌─────────────────────────────┐  │
│  │ kilikili_front_web  │  │  kilikili_front_admin       │  │
│  │ (Vue 3, port 3001)  │  │  (Vue 3, port 3000)        │  │
│  └────────┬────────────┘  └──────────┬──────────────────┘  │
│           │                          │                      │
└───────────┼──────────────────────────┼──────────────────────┘
            │                          │
            │   Vite Proxy /api/*       │   Vite Proxy /api/*
            ▼                          ▼
┌──────────────────────┐  ┌──────────────────────────────┐
│  kilikili-web        │  │  kilikili-admin              │
│  (Spring Boot, 7071) │  │  (Spring Boot, 7070)        │
│  — 用户端 API         │  │  — 管理端 API               │
└──────────┬───────────┘  └──────────┬───────────────────┘
           │                          │
           └──────────┬──────────────┘
                      │
          ┌───────────┴───────────┐
          │    kilikili-common    │
          │  (Shared Module)      │
          │  实体 / Mapper / 服务  │
          │  工具 / 配置 / Redis  │
          └───────────┬───────────┘
                      │
    ┌─────────────────┼──────────────────────┐
    │                 │                      │
    ▼                 ▼                      ▼
┌────────┐   ┌──────────────┐   ┌───────────────────┐
│ MySQL  │   │    Redis     │   │   MinIO / OSS     │
│ 8.0    │   │ 会话/缓存/排行│   │   对象存储         │
└────────┘   └──────────────┘   └───────────────────┘
                              ┌───────────────────┐
                              │   FFmpeg           │
                              │  视频转码(异步)    │
                              └───────────────────┘
```

### 后端技术选型

| 技术 | 用途 | 选型理由 |
|------|------|----------|
| **Spring Boot 2.7.18** | 应用框架 | 成熟稳定，生态丰富，快速构建 RESTful API |
| **MySQL 8.0** | 关系数据库 | 广泛使用的事务性数据库，适合结构化数据存储 |
| **MyBatis** | ORM 框架 | 相比 JPA 更灵活的 SQL 控制，便于复杂查询和性能优化 |
| **Redis** | 缓存/会话/排行 | 高性能内存数据存储，用于 Token 会话、热搜排行、在线人数统计 |
| **Elasticsearch** | 搜索引擎 | 预留全文搜索能力，支持更高效的多维视频搜索 |
| **MinIO** | 本地对象存储 | S3 兼容，本地开发调试友好，可无缝切换至云 OSS |
| **阿里云 OSS** | 云端对象存储 | 生产环境下的可靠云存储，CDN 加速支持 |
| **FFmpeg** | 视频转码 | 工业级音视频处理工具，自动检测最优 H.264 编码器 |
| **Redisson** | 分布式锁 | Redis 官方推荐的分布式锁实现，用于并发控制 |
| **WebSocket** | 实时推送 | 浏览器原生支持的双向通信协议，用于弹幕和消息通知 |
| **EasyCaptcha** | 验证码 | 轻量级算术验证码，防止机器注册/登录 |
| **FastJSON** | JSON 序列化 | 高性能 JSON 处理 |
| **Lombok** | 代码简化 | 减少 POJO 样板代码，提高开发效率 |
| **HikariCP** | 数据库连接池 | Spring Boot 默认连接池，高性能、轻量级 |

### 前端技术选型

| 技术 | 用途 | 选型理由 |
|------|------|----------|
| **Vue 3 + Composition API** | UI 框架 | 响应式、组合式 API 提升代码复用性与可维护性 |
| **TypeScript** | 类型系统 | 静态类型检查，减少运行时错误，提升大型项目开发体验 |
| **Vite** | 构建工具 | 极速热更新，原生 ESM 支持，构建效率远超 Webpack |
| **Element Plus** | UI 组件库 | Vue 3 生态最成熟的桌面端组件库，开箱即用 |
| **Pinia** | 状态管理 | Vue 3 官方推荐状态管理，TypeScript 友好，轻量无 boilerplate |
| **Vue Router** | 路由管理 | Vue 官方路由，支持懒加载、导航守卫 |
| **Axios** | HTTP 客户端 | 拦截器机制便于统一处理 Token 刷新和错误提示 |
| **ECharts** | 数据可视化 | 功能全面的图表库，用于管理端数据仪表盘 |
| **Video.js** | 视频播放 | HLS 流式播放支持，多平台兼容 |
| **Canvas 弹幕** | 弹幕渲染 | 性能优于 DOM 操作，适合大量弹幕高频渲染 |
| **DOMPurify** | XSS 防护 | 用户内容（评论、简介）展示前过滤恶意 HTML |

### 架构决策要点

1. **双后端 API 设计**：用户端和管理端拆分为独立 Spring Boot 应用，各自独立部署与扩展，避免管理接口暴露风险；同时共享 common 模块减少重复代码。
2. **Token 会话管理**：采用 Redis 存储用户会话（而非 JWT），便于服务端主动失效（如管理员封禁用户），支持 7 天自动续期。
3. **异步转码管道**：视频上传完成后，通过 `@Async` 异步启动 FFmpeg 转码任务，不阻塞上传响应；转码状态通过数据库字段追踪，前端轮询获取进度。
4. **分片上传 + 断点续传**：大文件分片上传，前端按片发送，后端合并；上传前预检已有分片实现续传。
5. **Canvas 弹幕**：使用 Canvas 而非 DOM 元素渲染弹幕，避免大量 DOM 操作导致的性能问题，支持每秒数百条弹幕的流畅渲染。
6. **弹性存储策略**：本地开发使用 MinIO 或本地文件系统，生产环境切换至阿里云 OSS，代码层面抽象统一接口。

## 挑战与解决方案

### 1. 视频大文件上传

**挑战**：用户上传的视频文件可能达到数 GB，直接上传会导致内存溢出、网络超时、失败重传成本高。

**解决方案**：采用**分片上传 + 断点续传**机制。前端将文件切分为多个 chunk（通常 1-5MB），逐个上传；后端接收后暂时存储到临时目录，所有分片上传完毕后在服务端合并。上传前先发起预检请求，返回已上传的分片列表，实现续传。

**关键代码**：`VideoFileServiceImpl.java` 中的 `preUploadVideo()`、`uploadVideo()` 和 `mergeAndCreateVideoFile()` 方法。

### 2. 弹幕实时渲染性能

**挑战**：弹幕（Danmaku）需要在视频播放时实时叠加显示，使用 DOM 元素渲染大量文字会导致严重的性能问题（重排、重绘）。

**解决方案**：使用 **Canvas 2D API** 实现弹幕渲染层。弹幕数据在内存中维护，通过 `requestAnimationFrame` 驱动渲染循环，每一帧清除画布并重新绘制所有活跃弹幕。支持滚动、顶部、底部三种模式，以及自定义颜色。

**关键代码**：`DanmakuLayer.vue` 中的 Canvas 渲染循环和弹幕轨道管理。

### 3. 异步视频转码可靠性

**挑战**：视频转码是 CPU/GPU 密集型操作，可能耗时数分钟，且 FFmpeg 可能因编码器问题、文件损坏等原因失败。

**解决方案**：
- 使用 `@Async` 在独立线程池中执行转码，不阻塞用户请求
- 自动检测系统可用的 H.264 编码器（`libx264` → `h264_mf` → `h264_amf` → `h264_nvenc`），分级降级
- 完整的转码状态追踪：上传完成 → 转码中 → 转码完成/失败
- 失败时记录日志并通过数据库状态通知前端
- 定时任务每天凌晨清理过期临时文件

**关键代码**：`VideoTranscodeServiceImpl.java` + `FFmpegUtils.java`。

### 4. 前端/后端 Token 双 Cookie 冲突

**挑战**：用户端和管理端运行在同一域名下，Cookie 中的 `token` 字段会冲突，导致管理端误用用户端 Token。

**解决方案**：管理端使用独立的 Cookie 名 `admin_token`，在拦截器中优先按 Cookie 名获取，同时兼容 Header 传参方式用于 API 调用场景。

**关键代码**：`AppInterceptor.java` 中的 Cookie 遍历逻辑。

### 5. 数据库与缓存一致性

**挑战**：视频播放量等计数器需要高并发更新，直接写数据库会造成行锁竞争和压力。

**解决方案**：播放量先通过 Redis `incrementex` 方法在内存中计数（带 5 分钟过期），定时或异步回写数据库。热搜关键词使用 Redis Sorted Set (`zaddCount`) 自动排序，淘汰低频词。

**关键代码**：`RedisUtils.java` 中的 `incrementex()` 和 `zaddCount()` 方法。

## 快速开始

### 前置依赖

| 工具 | 版本要求 | 用途 |
|------|----------|------|
| JDK | 1.8+ | 后端运行环境 |
| Maven | 3.6+ | 后端构建 |
| MySQL | 8.0+ | 数据库 |
| Redis | 6.x+ | 缓存/会话 |
| Node.js | 18+ | 前端构建 |
| FFmpeg | 4.x+ | 视频转码（可选，如无则跳过转码） |
| MinIO | 可选 | 本地对象存储 |

### 1. 克隆项目

```bash
git clone <your-repo-url>
cd kilikili-all
```

### 2. 数据库初始化

创建名为 `kilikili` 的 MySQL 数据库，并执行建表脚本（位于 `kilikili-java/kilikili-common/src/main/resources/` 目录下的 SQL 文件，如项目暂缺则需根据实体类手动生成 DDL）。

### 3. 启动后端

```bash
# 编辑数据库与 Redis 连接配置
# kilikili-web/src/main/resources/application.yml
# kilikili-admin/src/main/resources/application.yml

# 编译打包
cd kilikili-java
mvn clean package -DskipTests

# 启动用户端 API（端口 7071）
java -jar kilikili-web/target/kilikili-web-1.0.jar

# 启动管理端 API（端口 7070，新终端）
java -jar kilikili-admin/target/kilikili-admin-1.0.jar
```

### 4. 启动前端

```bash
# 用户端前端（端口 3001）
cd kilikili_front/kilikili_front_web
npm install
npm run dev

# 管理端前端（端口 3000，新终端）
cd kilikili_front/kilikili_front_admin
npm install
npm run dev
```

### 5. 访问

| 服务 | 地址 | 说明 |
|------|------|------|
| 用户端首页 | http://localhost:3001 | 视频浏览、上传、播放 |
| 管理后台 | http://localhost:3000 | 数据分析、内容审核 |
| 用户 API | http://localhost:7071 | 后端 REST 接口 |
| 管理 API | http://localhost:7070 | 管理端 REST 接口 |

默认管理员账号：`admin` / `admin123`（可在 `application.yml` 中修改）。

## 使用示例

### 用户端 API 示例

**获取验证码：**
```bash
curl -X POST http://localhost:7071/account/checkCode
```

**用户登录：**
```bash
curl -X POST http://localhost:7071/account/login \
  -d "email=user@example.com&password=mypassword&checkCodeKey=xxx&checkCode=1234"
```

**加载推荐视频：**
```bash
curl -X POST http://localhost:7071/video/loadRecommendVideo
```

**视频搜索：**
```bash
curl -X POST http://localhost:7071/video/search \
  -d "keyword=spring boot&pageNo=1"
```

**发送弹幕：**
```bash
curl -X POST http://localhost:7071/danmu/postDanmu \
  -d "videoId=xxx&fileId=xxx&text=Hello Kilikili&mode=0&color=%23ffffff&time=10"
```

### 前端组件示例

**VideoPlayer 组件用法：**
```vue
<template>
  <VideoPlayer
    :src="videoSource"
    @timeupdate="onTimeUpdate"
    @loaded="onVideoLoaded"
    @toggleDanmaku="danmakuVisible = !danmakuVisible"
  />
  <DanmakuLayer
    v-if="danmakuVisible"
    :danmaku-list="danmakuList"
    :playing="true"
    :current-time="currentTime"
  />
  <DanmakuInput @send="sendDanmaku" />
</template>
```

**分片上传文件：**
```typescript
// 1. 预上传
const { data } = await preUploadVideo(fileName, chunks)

// 2. 逐片上传
for (let i = 0; i < chunks; i++) {
  const chunk = file.slice(i * chunkSize, (i + 1) * chunkSize)
  await uploadVideoChunk(chunk, i, data.uploadId)
}

// 3. 发布视频（将 uploadId 与视频元数据关联）
await postVideo({ videoName, uploadFileList: data.uploadId })
```

## 测试

> 项目当前尚未集成自动化测试框架。以下为推荐的测试方案：

### 后端测试

```bash
# 运行所有单元测试
cd kilikili-java
mvn test

# 运行特定模块测试
mvn test -pl kilikili-web
```

建议增加的测试覆盖范围：
- **单元测试**：Service 层业务逻辑（JUnit 5 + Mockito）
- **集成测试**：Controller 层 API 接口（Spring MockMvc）
- **数据访问测试**：MyBatis Mapper 查询（@MybatisTest 或 Testcontainers）

### 前端测试

```bash
cd kilikili_front/kilikili_front_web
npm run test         # Vitest 单元测试（如配置）
npm run test:e2e     # Playwright/Cypress E2E（如配置）
```

## 项目结构

```
kilikili-all/
├── kilikili-java/                          # 后端（Spring Boot + Maven）
│   ├── pom.xml                             # 父 POM，统一依赖管理
│   │
│   ├── kilikili-web/                       # 用户端 API（端口 7071）
│   │   └── src/main/java/com/kilikili/web/
│   │       ├── controller/                 # REST API 控制器
│   │       │   ├── AccountController.java        # 登录/注册/验证码
│   │       │   ├── AccountInfoController.java    # 用户信息统计
│   │       │   ├── VideoController.java          # 视频浏览/搜索
│   │       │   ├── CommentController.java        # 评论管理
│   │       │   ├── DanmuController.java          # 弹幕
│   │       │   ├── UserActionController.java     # 点赞/收藏/投币
│   │       │   ├── FileController.java           # 文件上传/视频流
│   │       │   ├── UcenterController.java        # 创作者中心
│   │       │   ├── UhomeController.java          # 用户主页
│   │       │   ├── CategoryController.java       # 分类查询
│   │       │   ├── HistoryController.java        # 播放历史
│   │       │   ├── MessageController.java        # 消息通知
│   │       │   ├── SeriesController.java         # 视频合集
│   │       │   ├── SysSettingController.java     # 系统设置
│   │       │   └── ABaseController.java          # 基础控制器（Cookie/Token）
│   │       ├── config/                          # Web 配置（CORS/静态资源/FFmpeg）
│   │       └── kilikiliWebRunApplication.java   # 启动类
│   │
│   ├── kilikili-admin/                     # 管理端 API（端口 7070）
│   │   └── src/main/java/com/kilikili/admin/
│   │       ├── controller/                 # 管理员控制器
│   │       │   ├── AccountController.java        # 管理员登录
│   │       │   ├── IndexController.java          # 仪表盘统计
│   │       │   ├── VideoInfoController.java      # 视频列表/审核/推荐
│   │       │   ├── UserController.java           # 用户列表/状态管理
│   │       │   ├── CategoryController.java       # 分类管理
│   │       │   ├── InteractController.java       # 弹幕/评论管理
│   │       │   ├── SettingController.java        # 系统配置
│   │       │   └── FileController.java           # 图片上传
│   │       ├── config/                          # Web 配置
│   │       ├── interceptor/                     # 登录拦截器
│   │       └── kilikiliadminRunApplication.java # 启动类
│   │
│   └── kilikili-common/                    # 共享模块
│       └── src/main/java/com/kilikili/
│           ├── config/                     # 公共配置
│           │   ├── Appconfig.java                # 应用全局配置
│           │   ├── OssConfig.java                # 阿里云 OSS 配置
│           │   ├── RedisConfig.java              # Redis 序列化配置
│           │   ├── ThreadPoolConfig.java         # 异步线程池
│           │   ├── WebSocketConfig.java          # WebSocket 端点注册
│           │   └── ScheduleConfig.java           # 定时任务（清理临时文件）
│           ├── entity/                     # 数据实体
│           │   ├── po/                     # 数据库 PO（Video, UserInfo, Comment, Danmu...）
│           │   ├── dto/                    # 数据传输对象（TokenUserInfoDto）
│           │   ├── vo/                     # 视图对象（ResponseVO, PaginationResultVO）
│           │   ├── enums/                  # 枚举（视频状态、用户行为、评论状态...）
│           │   ├── query/                  # 查询参数封装
│           │   └── constants/              # 常量定义
│           ├── mappers/                    # MyBatis Mapper 接口
│           ├── service/                    # 服务接口 + 实现
│           │   ├── impl/                   # 业务逻辑实现（VideoServiceImpl, UserInfoServiceImpl...）
│           │   └── ...Service.java         # 服务接口
│           ├── redis/                      # Redis 工具（RedisUtils）
│           ├── utils/                      # 工具类
│           │   ├── FFmpegUtils.java              # FFmpeg 转码封装
│           │   ├── StringTools.java              # 字符串/随机数工具
│           │   └── CopyTools.java                # 对象拷贝工具
│           ├── websocket/                  # WebSocket 端点
│           ├── component/                  # RedisComponent（会话管理）
│           └── exception/                  # BusinessException
│
├── kilikili_front/                         # 前端（Vue 3 + TypeScript + Vite）
│   │
│   ├── kilikili_front_web/                # 用户端前端（端口 3001）
│   │   ├── vite.config.ts                 # Vite 配置（代理到 :7071）
│   │   ├── tsconfig.json
│   │   ├── package.json
│   │   └── src/
│   │       ├── api/                       # API 封装（auth, video, comment, danmu...）
│   │       ├── components/                # 可复用组件
│   │       │   ├── video/VideoPlayer.vue         # 视频播放器
│   │       │   ├── danmaku/DanmakuLayer.vue      # 弹幕渲染层（Canvas）
│   │       │   ├── danmaku/DanmakuInput.vue      # 弹幕输入框
│   │       │   ├── comment/CommentList.vue       # 评论列表
│   │       │   ├── comment/CommentItem.vue       # 评论项
│   │       │   └── common/                      # 通用组件（VideoCard, SkeletonCard, Sidebar...）
│   │       ├── views/                     # 页面视图
│   │       │   ├── home/Home.vue                 # 首页（视频流 + 分类导航）
│   │       │   ├── video/VideoDetail.vue         # 视频详情页
│   │       │   ├── auth/Login.vue / Register.vue # 登录/注册
│   │       │   ├── creator/                     # 创作者中心
│   │       │   ├── user/                        # 用户主页/设置/历史
│   │       │   ├── search/Search.vue            # 搜索结果页
│   │       │   └── message/Messages.vue         # 消息通知
│   │       ├── stores/                    # Pinia 状态管理（user, app, theme）
│   │       ├── router/                    # 路由配置（含导航守卫）
│   │       ├── layouts/                   # 布局组件
│   │       ├── types/                     # TypeScript 类型定义
│   │       └── utils/                     # 工具函数（request, storage, format）
│   │
│   └── kilikili_front_admin/              # 管理端前端（端口 3000）
│       ├── vite.config.ts                 # Vite 配置（代理到 :7070）
│       ├── tsconfig.json
│       ├── package.json
│       └── src/
│           ├── api/                       # Admin API 封装
│           ├── components/                # 管理端组件（AdminSidebar, AdminHeader, StatCard）
│           ├── views/                     # 后台页面
│           │   ├── dashboard/Dashboard.vue       # 数据仪表盘（ECharts）
│           │   ├── video/VideoList.vue           # 视频列表
│           │   ├── video/VideoAudit.vue          # 视频审核
│           │   ├── category/CategoryManage.vue   # 分类管理
│           │   ├── user/UserManage.vue           # 用户管理
│           │   ├── comment/CommentManage.vue     # 评论管理
│           │   ├── danmaku/DanmakuManage.vue     # 弹幕管理
│           │   ├── setting/SystemSetting.vue     # 系统设置
│           │   └── log/OperationLog.vue          # 操作日志
│           ├── stores/                    # Pinia 状态管理
│           ├── router/                    # 路由 + 鉴权守卫
│           ├── layouts/                   # 管理后台布局
│           ├── types/                     # 类型定义
│           └── utils/                     # 工具函数
```

## 贡献指南

欢迎贡献代码、提交 Issue 或建议！请遵循以下步骤：

1. Fork 项目并创建你的分支：`git checkout -b feature/amazing-feature`
2. 确保代码风格一致（遵循现有命名规范与文件结构）
3. 提交前运行测试确保不破坏现有功能
4. 创建 Pull Request，清晰描述改动内容和动机

### 开发规范

- **后端**：一个文件不超过 400 行，嵌套不超过 4 层，Service 层使用接口 + 实现模式
- **前端**：Vue 3 Composition API + `<script setup>`，TypeScript 严格模式
- **数据库**：MyBatis Mapper XML 中表名使用下划线命名，实体类使用驼峰命名
- **提交信息**：使用英文或中文，简明扼要

## 许可证

本项目基于 MIT 许可证开源。详见 `LICENSE` 文件。

## 未来改进

- [ ] **视频转码进度推送**：通过 WebSocket 将转码进度实时推送到前端
- [ ] **视频推荐算法**：基于用户行为和标签的协同过滤推荐
- [ ] **OSS/MinIO 统一抽象**：通过策略模式抽象存储服务，简化双存储切换
- [ ] **视频弹幕 WebSocket 实时同步**：当前弹幕为本地渲染，后续可加入实时弹幕同步
- [ ] **移动端适配**：响应式布局优化 + 移动端 H5 播放体验
- [ ] **单元测试与集成测试**：增加 JUnit + MockMvc + Testcontainers 测试覆盖
- [ ] **Docker 容器化**：提供 docker-compose 一键部署方案（MySQL + Redis + MinIO + Backend + Nginx）
- [ ] **视频多分辨率转码**：生成 360p/480p/720p/1080p 多版本，自适应码率切换
- [ ] **OAuth2 第三方登录**：支持 GitHub/Google 账号登录
- [ ] **Admin 操作日志完善**：增加更细粒度的操作审计
