# JC-Club Auth 认证服务技术文档

## 模块描述与作用

### 模块定位

`jc-club-auth` 是 JC-Club 系统的核心认证授权模块，承担着整个平台的用户身份验证、权限控制和安全管理的职责。作为微服务架构中的基础服务层，它为其他业务模块提供统一的认证和授权能力，是保障系统安全访问的第一道防线。

该模块采用前后端分离架构模式，通过 RESTful API 与前端应用及后端微服务进行交互，实现了认证逻辑与业务逻辑的解耦，确保了系统的安全性和可维护性。

### 核心作用

#### 1. 统一身份认证中心

`jc-club-auth` 作为整个 JC-Club 系统的唯一认证入口，负责处理所有用户的登录、登出和身份验证操作。通过集成 Sa-Token 认证框架，模块提供了轻量级且高性能的会话管理机制，支持多种认证方式（用户名密码、验证码等），确保用户身份的安全验证。

模块实现了集中化的用户管理功能，包括用户注册、信息查询、状态管理和权限分配等。所有用户相关的操作都经过统一的认证和授权检查，防止未授权访问敏感数据和功能。

#### 2. 权限控制与访问管理

模块基于 RBAC（基于角色的访问控制）模型设计，实现了细粒度的权限管理。通过用户、角色、权限三层架构，支持灵活的权限分配和变更。系统管理员可以根据业务需求，为不同用户分配不同的角色和权限，实现精细化的访问控制。

权限控制贯穿整个请求处理流程，从登录拦截器的预处理，到业务接口的权限验证，形成了完整的安全防护链条。这种设计确保了只有经过授权的用户才能访问相应的系统资源。

#### 3. 会话安全与状态管理

通过 Redis 实现分布式会话存储，支持集群环境下的会话一致性。会话管理采用了安全的 Token 机制，确保用户身份在分布式环境中的正确传递和验证。模块还实现了会话超时自动过期、异地登录检测等安全特性，有效防止会话劫持和未授权访问。

会话状态通过 ThreadLocal 进行线程内传递，结合拦截器机制实现了登录信息的透明化处理。业务代码无需直接操作会话对象，即可获取当前用户信息，降低了开发复杂度和安全风险。

#### 4. 安全防护体系

模块构建了多层次的安全防护体系，包括密码加密存储、SQL 注入防护、XSS 攻击防护等。通过 Druid 连接池的监控功能，系统可以实时监控数据库访问情况，及时发现和处理异常访问行为。

登录验证码、密码强度校验、登录失败次数限制等机制，有效防止暴力破解和恶意登录。同时，模块支持审计日志功能，记录关键操作的执行情况，为安全事件调查提供依据。

### 在系统中的位置

`jc-club-auth` 在 JC-Club 微服务架构中处于基础服务层，是其他业务模块依赖的核心基础设施。所有需要用户身份验证的微服务都通过 Feign 接口调用认证服务，验证用户身份和权限。

```
用户请求 → API网关 → 认证服务验证 → 业务服务处理 → 响应返回
```

模块与其他模块的关系：
- **向上对接**：为前端应用和第三方系统提供认证接口
- **向下依赖**：通过 Redis 实现会话存储，通过 MySQL 实现用户数据持久化
- **横向协作**：为各业务模块提供统一的用户身份和权限信息

### 价值与意义

`jc-club-auth` 模块的存在为 JC-Club 系统带来了以下核心价值：

1. **安全保障**：提供专业的身份认证和权限控制能力，保护系统资源不被未授权访问
2. **统一管理**：集中化的用户管理简化了用户数据的维护工作，避免了数据不一致问题
3. **业务聚焦**：业务开发人员无需关注认证授权的技术细节，可以专注于业务逻辑实现
4. **扩展灵活**：模块化的架构设计支持认证方式的灵活扩展和升级
5. **运维便利**：完善的监控和日志功能便于系统运维和问题排查

## 项目概述

`jc-club-auth` 是基于 Spring Boot 开发的微服务认证授权模块，采用了经典的分层架构模式，提供完整的用户认证、授权、用户管理等功能。该服务集成了 Redis 缓存、Sa-Token 认证框架、MyBatis-Plus 数据库访问等技术栈，为整个 JC-Club 系统提供统一的认证服务。

## 架构特点

### 分层架构设计

```
jc-club-auth/
├── jc-club-auth-api/              # API接口层
├── jc-club-auth-application/      # 应用层
│   └── jc-club-auth-application-controller/  # 控制器层
├── jc-club-auth-common/           # 公共组件层
├── jc-club-auth-domain/           # 领域层
├── jc-club-auth-infra/            # 基础设施层
└── jc-club-auth-starter/          # 启动器层
```

- **API层**：定义对外接口和DTO对象
- **应用层**：处理业务逻辑，调用领域服务
- **领域层**：核心业务逻辑和实体
- **基础设施层**：数据库访问、缓存等基础设施
- **启动器层**：Spring Boot 启动配置

### 模块职责划分

| 模块 | 职责 | 主要功能 |
|------|------|----------|
| `jc-club-auth-api` | API接口定义 | UserFeignService、DTO对象、Result响应 |
| `jc-club-auth-application` | 应用层 | 控制器、业务逻辑编排 |
| `jc-club-auth-common` | 公共组件 | 工具类、枚举、通用实体 |
| `jc-club-auth-domain` | 领域层 | 业务实体、领域服务、Redis配置 |
| `jc-club-auth-infra` | 基础设施层 | MyBatis配置、SQL映射、拦截器 |
| `jc-club-auth-starter` | 启动器层 | Spring Boot启动配置 |

## 技术栈

### 核心技术组件

| 技术组件 | 版本 | 用途 |
|----------|------|------|
| Spring Boot | 2.4.2 | 应用框架 |
| Spring MVC | 内置 | Web层框架 |
| MyBatis-Plus | 内置 | 数据库访问框架 |
| Druid | 内置 | 数据库连接池 |
| Redis | 最新版 | 缓存和会话存储 |
| Sa-Token | 最新版 | 轻量级认证框架 |
| MySQL | 8.0+ | 数据库 |
| Jackson | 2.12.7 | JSON序列化 |
| Gson | 2.8.6 | JSON处理 |
| Log4j2 | 内置 | 日志框架 |

### 关键技术特性

1. **Sa-Token 认证框架**
   - 轻量级、高性能的权限认证框架
   - 支持多种认证模式（Cookie、Token、Header）
   - 内置会话管理和权限控制

2. **MyBatis-Plus 增强**
   - 提供了SQL日志拦截器
   - 支持分页插件
   - 具备完整的SQL执行监控

3. **Redis 集成**
   - 会话存储
   - 缓存加速
   - 分布式锁支持

## 核心功能

### 1. 用户管理功能

#### 1.1 用户注册
```java
@RequestMapping("register")
public Result<Boolean> register(@RequestBody AuthUserDTO authUserDTO)
```
- 用户信息验证
- 密码加密存储
- 重复用户名检查

#### 1.2 用户信息更新
```java
@RequestMapping("update")
public Result<Boolean> update(@RequestBody AuthUserDTO authUserDTO)
```
- 个人信息修改
- 状态变更管理

#### 1.3 用户查询
```java
@RequestMapping("getUserInfo")
public Result<AuthUserDTO> getUserInfo(@RequestBody AuthUserDTO authUserDTO)

@RequestMapping("listByIds")
public Result<List<AuthUserDTO>> listUserInfoByIds(@RequestBody List<String> userNameList)
```
- 单个用户信息查询
- 批量用户信息获取

#### 1.4 用户状态管理
```java
@RequestMapping("changeStatus")
public Result<Boolean> changeStatus(@RequestBody AuthUserDTO authUserDTO)
```
- 用户启用/禁用
- 状态持久化

#### 1.5 用户删除
```java
@RequestMapping("delete")
public Result<Boolean> delete(@RequestBody AuthUserDTO authUserDTO)
```
- 软删除实现
- 关联数据处理

### 2. 认证功能

#### 2.1 用户登录
```java
@RequestMapping("doLogin")
public Result<SaTokenInfo> doLogin(@RequestParam("validCode") String validCode)
```
- 基于验证码的登录
- Sa-Token 会话创建
- 登录状态验证

#### 2.2 登录状态查询
```java
@RequestMapping("isLogin")
public String isLogin()
```
- 当前会话状态检查
- 登录状态实时查询

#### 2.3 用户退出
```java
@RequestMapping("logOut")
public Result logOut(@RequestParam String userName)
```
- 会话销毁
- 登录状态清除

## Redis 在认证系统中的作用

### 1. 会话存储
- **Sa-Token 会话管理**：存储用户登录状态、Token信息
- **会话持久化**：确保分布式环境下的会话一致性

### 2. 缓存加速
```java
@Component
public class RedisUtil {
    // 构建缓存key
    public String buildKey(String... strObjs)
    
    // 字符串缓存操作
    public void set(String key, String value)
    public String get(String key)
    
    // 带过期时间的设置
    public boolean setNx(String key, String value, Long time, TimeUnit timeUnit)
}
```

### 3. 缓存策略
- **键值约定**：使用点号分隔的层级结构 `module.function.param`
- **序列化配置**：Jackson2JsonRedisSerializer 支持复杂对象序列化
- **过期策略**：支持设置TTL，自动清理过期数据

### 4. 应用场景
- 用户登录状态缓存
- 权限信息缓存
- 防重复提交控制
- 分布式锁实现

## 登录拦截器机制

### 拦截器实现

#### 1. 依赖组件
```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-webmvc</artifactId>
</dependency>
```

#### 2. 拦截器核心逻辑
```java
public class LoginInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) throws Exception {
        String loginId = request.getHeader("loginId");
        if (StringUtils.isNotBlank(loginId)) {
            LoginContextHolder.set("loginId", loginId);
        }
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, 
                              HttpServletResponse response, 
                              Object handler, 
                              Exception ex) throws Exception {
        LoginContextHolder.remove();
    }
}
```

### 拦截器配置

#### 1. 配置类实现
```java
@Configuration
public class GlobalConfig extends WebMvcConfigurationSupport {
    
    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/user/doLogin");
    }
}
```

#### 2. 拦截规则
- **拦截路径**：`/**` (所有路径)
- **排除路径**：`/user/doLogin` (登录接口不拦截)

## 拦截器的作用和意义

### 1. 登录状态验证
- **请求头解析**：从HTTP请求头中提取`loginId`
- **状态设置**：将登录信息设置到线程本地变量中
- **全局访问**：业务代码可通过`LoginContextHolder`获取当前用户信息

### 2. 用户上下文管理
```java
public class LoginContextHolder {
    private static final InheritableThreadLocal<Map<String, Object>> THREAD_LOCAL
            = new InheritableThreadLocal<>();
    
    public static void set(String key, Object val)
    public static Object get(String key)
    public static String getLoginId()
    public static void remove()
}
```

### 3. 线程安全设计
- **InheritableThreadLocal**：支持子线程继承父线程的登录上下文
- **ConcurrentHashMap**：保证线程安全的Map操作
- **自动清理**：请求完成后自动清理ThreadLocal，防止内存泄漏

### 4. 核心价值
1. **透明化认证**：业务代码无需关心认证细节
2. **全局用户信息**：在任何地方都能获取当前登录用户信息
3. **低侵入性**：通过拦截器实现，对现有业务代码零侵入
4. **异步支持**：支持异步任务中的用户上下文传递

## 数据库设计

### 核心表结构

| 表名 | 作用 | 核心字段 |
|------|------|----------|
| `auth_user` | 用户信息表 | user_name, password, status |
| `auth_role` | 角色表 | role_name, role_code |
| `auth_permission` | 权限表 | permission_name, permission_code |
| `auth_user_role` | 用户角色关联表 | user_id, role_id |
| `auth_role_permission` | 角色权限关联表 | role_id, permission_id |

### MyBatis 配置特点

1. **SQL日志监控**
```java
@Intercepts({
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class})
})
public class SqlStatementInterceptor implements Interceptor
```

2. **性能监控**
   - 记录SQL执行时间
   - 慢查询日志记录
   - 不同执行时间的分级日志

3. **完整SQL输出**
```java
public class MybatisPlusAllSqlLog implements InnerInterceptor
```
   - 输出完整SQL语句
   - 参数替换后的SQL
   - 便于调试和问题排查

## 配置管理

### Redis 配置
```yaml
spring:
  redis:
    database: 1
    host: 192.168.30.128
    port: 6379
    password: jichi1234
    timeout: 2s
    lettuce:
      pool:
        max-active: 200
        max-wait: -1ms
        max-idle: 10
        min-idle: 0
```

### 数据库配置
```yaml
spring:
  datasource:
    username: root
    password: [encrypted]
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/jc-club
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      initial-size: 20
      min-idle: 20
      max-active: 100
```

### Druid 监控配置
```yaml
spring:
  datasource:
    druid:
      stat-view-servlet:
        enabled: true
        url-pattern: /druid/*
        login-username: admin
        login-password: 123456
      filter:
        stat:
          enabled: true
          slow-sql-millis: 2000
          log-slow-sql: true
```

## 安全特性

### 1. 密码安全
- 加密存储
- 盐值处理
- 强度验证

### 2. SQL注入防护
- MyBatis预编译语句
- 参数化查询
- SQL日志监控

### 3. 连接池安全
- Druid连接池配置
- 连接池监控
- SQL执行统计

### 4. 访问控制
- Sa-Token权限控制
- 登录状态验证
- 会话管理

## 监控和运维

### 1. 日志配置
```xml
<configuration>
    <logger name="sys-sql" level="INFO"/>
</configuration>
```

### 2. 性能监控
- SQL执行时间监控
- 连接池状态监控
- Redis操作统计

### 3. 健康检查
- 数据库连接状态
- Redis连接状态
- 服务可用性检查

## 扩展性设计

### 1. 模块化架构
- 分层清晰，职责明确
- 模块间低耦合
- 便于功能扩展

### 2. 插件化设计
- 拦截器可配置
- 扩展点预留
- 灵活的功能组装

### 3. 配置化管理
- 外部化配置
- 环境差异处理
- 热更新支持

## 总结

`jc-club-auth` 认证服务是一个设计优良、功能完善的微服务认证模块，具有以下特点：

1. **架构清晰**：采用分层架构，职责明确，易于维护和扩展
2. **技术选型合理**：使用成熟的Spring生态和优秀的开源组件
3. **功能完善**：涵盖用户管理、认证授权、会话管理等核心功能
4. **性能优秀**：集成Redis缓存，提供高性能的数据访问
5. **安全可靠**：多层次的安全防护机制
6. **监控完善**：全面的日志和性能监控

该认证服务为整个JC-Club系统提供了坚实的认证基础，是微服务架构中的重要组成部分。