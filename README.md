# OJ Learning Platform — 智能 OJ 学习平台

基于 **Dify AI Chatflow** 和**远程 OJ**（LeetCode / 洛谷）的智能算法学习平台。
集成 AI 代码分析、智能题目推荐、在线比赛系统和团队协作功能，
通过 RabbitMQ 异步判题、Redis 分布式锁等技术保证系统的高并发与高可用。

---

## 系统架构

```
┌─────────────────────────────────────────────────────────────────┐
│                        用户浏览器 (React SPA)                     │
└──────────────────────────────┬──────────────────────────────────┘
                               │ HTTP / JWT
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Spring Boot 后端服务                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌───────────────┐   │
│  │ 题库模块  │  │ 比赛模块  │  │ 队伍模块  │  │  AI 助教模块   │   │
│  └─────┬────┘  └─────┬────┘  └──────────┘  └───────┬───────┘   │
│        │             │                              │           │
│        ▼             ▼                              ▼           │
│  ┌──────────┐  ┌──────────┐                  ┌──────────┐      │
│  │ OJ 网关   │  │ 判题队列  │                  │ Dify API  │      │
│  │(工厂模式) │  │(RabbitMQ)│                  │  客户端    │      │
│  └─────┬────┘  └─────┬────┘                  └──────────┘      │
└────────┼─────────────┼──────────────────────────────────────────┘
         │             │
         ▼             ▼
┌─────────────┐  ┌──────────┐  ┌───────┐  ┌──────────┐
│ LeetCode /  │  │ RabbitMQ │  │ MySQL │  │  Redis   │
│ Luogu API   │  │  Broker  │  │  8.0  │  │  缓存/锁  │
└─────────────┘  └──────────┘  └───────┘  └──────────┘
```

---

## 技术栈

| 层级 | 技术 |
|------|------|
| **前端** | React 18 + TypeScript + Vite + TailwindCSS + Monaco Editor + Zustand |
| **后端** | Java 17 + Spring Boot 3.2.5 + MyBatis-Plus 3.5.6 |
| **数据库** | MySQL 8.0 |
| **缓存/锁** | Redis（分布式锁 + 验证码存储 + 频率限制） |
| **消息队列** | RabbitMQ（异步判题 + 延迟轮询） |
| **认证** | JWT 无状态认证 + 拦截器鉴权 |
| **AI 集成** | Dify Chatflow（代码分析 / 渐进提示 / 智能推荐） |
| **HTTP 客户端** | OkHttp 4.12（调用远程 OJ API） |
| **邮件** | Spring Boot Mail + Redis 验证码 |

---

## 核心功能

### 1. 多平台题库聚合

- 支持 **LeetCode** 和 **洛谷** 双平台题目聚合
- 工厂 + 策略模式（`OjApiServiceFactory`）实现多 OJ 平台无缝切换
- 统一标签体系：跨平台标签归一化映射，支持按难度 / 算法标签 / 来源多维筛选
- 题目数据本地缓存，减少远程 API 调用

### 2. 在线代码编辑与提交

- 集成 **Monaco Editor**（VS Code 同款编辑器），支持 Java / Python / C++ 等多语言
- 后端代理提交到远程 OJ，自动处理 Cookie / CSRF Token 认证
- 指数退避重试机制（`OjExecutionService`），应对远程 API 限流和超时
- 代码草稿自动保存，切换题目不丢失

### 3. AI 智能助教（Dify Chatflow）

- **代码分析**：提交后自动分析时间/空间复杂度、边界条件、优化建议
- **渐进提示**：按提示级别逐步引导，避免直接给出答案
- **智能推荐**：根据做题历史和薄弱点推荐下一道练习题
- **自由问答**：支持对题目和代码的任意提问
- 每个练习会话绑定独立的 Dify 对话上下文，保持连贯的学习体验

### 4. 赛事系统

- 支持**个人赛**和**团队赛**两种模式
- 支持 **ACM / OI / CF** 三种赛制评分
- RabbitMQ 三队列异步判题架构：
  ```
  提交队列 → 轮询延迟队列（Dead Letter Exchange） → 轮询队列
  ```
- Redis 分布式锁保证并发提交的榜单一致性（Lua 脚本原子释放）
- 实时榜单（支持封榜机制）
- 完整的比赛生命周期：创建 → 报名 → 进行 → 封榜 → 结束

### 5. 团队协作

- 独立队伍系统：创建 / 解散 / 申请加入 / 审批 / 转让队长
- 比赛组队：队长创建队伍 → 成员加入 → 确认报名
- 队伍管理面板：成员管理、参赛记录、申请审批

### 6. 题单与智能组题

- **手动组题**：从题库选题创建题单
- **快速组题**：按难度分布和标签自动生成
- **AI 智能组题**：基于用户画像和学习目标，由 Dify 生成个性化题单

### 7. 用户画像与学习追踪

- 自动统计做题数据：各平台通过数 / 提交数 / 通过率
- 难度分布分析（Easy / Medium / Hard）
- 练习会话轨迹记录，可视化学习路径
- 用户画像：技能评级 / 强弱标签 / 目标等级

---

## 项目结构

```
oj-project/
├── backend/                          # Spring Boot 后端
│   └── src/main/java/com/ojplatform/
│       ├── config/                   # 配置类（RabbitMQ / Redis / Dify / OJ平台）
│       ├── controller/               # REST API 控制器（14 个）
│       ├── dto/                      # 数据传输对象
│       ├── entity/                   # MyBatis-Plus 实体类（20+ 张表）
│       ├── interceptor/              # JWT 鉴权 / 管理员拦截器
│       ├── mapper/                   # MyBatis-Plus Mapper
│       ├── service/                  # 业务逻辑层
│       │   ├── OjApiServiceFactory   # 工厂模式：多 OJ 平台切换
│       │   ├── OjExecutionService    # 指数退避重试
│       │   ├── RedisLockService      # 分布式锁（Lua 脚本）
│       │   ├── ContestJudge*         # RabbitMQ 异步判题
│       │   └── impl/                 # 服务实现
│       └── util/                     # 工具类（JWT / 难度映射）
│
├── frontend/                         # React 前端
│   └── src/
│       ├── api/                      # Axios API 封装（15 个模块）
│       ├── components/               # 公共组件（侧边栏 / 主题切换 / Toast）
│       ├── pages/                    # 页面组件（16 个页面）
│       ├── store/                    # Zustand 状态管理
│       ├── types/                    # TypeScript 类型定义
│       └── utils/                    # 工具函数
│
├── sql/                              # 数据库脚本
│   ├── schema.sql                    # 完整建表 DDL（20+ 张表）
│   └── migrations/                   # 增量迁移脚本
│
└── docs/                             # 文档
    ├── dify-chatflow-setup.md        # Dify Chatflow 编排指南
    └── dify-OJ 智能学习助手.yml       # Dify 应用导出文件
```

---

## 技术亮点

### RabbitMQ 三队列异步判题

```
用户提交 ─► contest-submit-queue ─► Worker 提交到远程OJ
                                         │
                                         ▼ (获得 submissionId)
                              contest-poll-delay-queue
                                  (TTL 延迟, DLX)
                                         │
                                         ▼ (延迟到期)
                              contest-poll-queue ─► Worker 轮询结果
                                         │
                                   ┌─────┴─────┐
                                   │            │
                                 有结果      无结果(重试)
                                   │            │
                                   ▼            ▼
                              更新榜单     重入延迟队列
                           (Redis 分布式锁)  (指数退避)
```

### Redis 分布式锁（Lua 脚本原子释放）

```java
// RedisLockService.java — 防止并发判题导致榜单不一致
public <T> T executeWithLock(String key, Duration ttl, Supplier<T> action) {
    String value = UUID.randomUUID().toString();
    Boolean locked = redis.opsForValue()
        .setIfAbsent(key, value, ttl);
    try {
        return action.get();
    } finally {
        // Lua 脚本保证 compare-and-delete 原子性
        redis.execute(UNLOCK_SCRIPT, List.of(key), value);
    }
}
```

### 工厂 + 策略模式（多 OJ 平台适配）

```java
// OjApiServiceFactory.java
@Component
public class OjApiServiceFactory {
    private final Map<String, OjApiService> serviceMap;

    public OjApiService get(String platform) {
        OjApiService raw = serviceMap.get(platform);
        // 包装 OjExecutionService，自动加上指数退避重试
        return wrapWithRetry(raw);
    }
}
```

---

## 数据库设计

共 20+ 张表，核心表关系：

```
users ──┬── submissions ──── problems
        │                      │
        ├── practice_sessions  ├── problem_tag_relations ── tags
        │       │              │
        │   session_problems───┘
        │
        ├── contest_registrations ── contests
        │                              │
        ├── contest_submissions ───────┤
        │                              │
        └── teams ── team_members      ├── contest_teams
             │                         │      │
             └── team_join_requests    └── contest_team_members
```

---

## 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0
- Redis 7+
- RabbitMQ 3.12+

### 后端启动

```bash
# 1. 导入数据库
mysql -u root -p < sql/schema.sql

# 2. 修改配置文件（数据库 / Redis / RabbitMQ 连接信息）
cd backend
cp src/main/resources/application.yml src/main/resources/application-local.yml
# 编辑 application-local.yml 填入本地配置

# 3. 启动
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### 前端启动

```bash
cd frontend
npm install
npm run dev
# 访问 http://localhost:5173
```

### Dify 配置

参考 [docs/dify-chatflow-setup.md](docs/dify-chatflow-setup.md) 搭建 AI Chatflow，
或直接导入 `docs/dify-OJ 智能学习助手.yml` 到 Dify 平台。

---

## 页面预览

| 题库首页 | 做题页面 |
|:--------:|:--------:|
| ![题库首页](ui1.png) | ![做题页面](ui2.png) |

---

## License

本项目仅供学习交流使用。
