# ApeClub 微信公众号模块使用文档

## 📋 目录
1. [模块概述](#模块概述)
2. [核心功能](#核心功能)
3. [技术架构](#技术架构)
4. [环境准备](#环境准备)
5. [配置说明](#配置说明)
6. [内网穿透配置](#内网穿透配置)
7. [微信公众号配置](#微信公众号配置)
8. [功能实现详解](#功能实现详解)
9. [部署运行](#部署运行)
10. [常见问题](#常见问题)

---

## 模块概述

**ape-club-wx** 是 ApeClub 项目的微信公众号服务模块，作为独立的 Spring Boot 应用运行在 **3012 端口**。

### 主要作用

1. **微信公众号消息接收与处理**
   - 接收微信服务器推送的用户消息
   - 处理用户关注/取消关注事件
   - 自动回复用户消息

2. **验证码登录功能**
   - 用户通过公众号获取登录验证码
   - 验证码存储在 Redis 中，有效期 5 分钟
   - 支持与主系统的登录认证集成

3. **消息路由与分发**
   - 使用策略模式处理不同类型的消息
   - 支持扩展新的消息处理器

---

## 核心功能

### 1. 用户关注事件处理
- **触发条件**: 用户关注公众号
- **响应内容**: "感谢您的关注，我是经典鸡翅！欢迎来学习从0到1社区项目"
- **实现类**: `SubscribeMsgHandler.java`

### 2. 验证码生成功能
- **触发条件**: 用户发送文本消息 "验证码"
- **功能流程**:
  1. 生成 3 位随机数字验证码（000-999）
  2. 存储到 Redis：`loginCode.{验证码}` → `{用户OpenID}`
  3. 有效期：5 分钟
  4. 返回验证码给用户
- **实现类**: `ReceiveTextMsgHandler.java`

### 3. 消息签名验证
- **作用**: 验证微信服务器请求的合法性
- **算法**: SHA1 签名
- **Token**: `adwidhaidwoaid`（硬编码在 `CallBackController.java:19`）

---

## 技术架构

### 架构设计模式

```
┌─────────────────────────────────────────────┐
│          微信服务器                          │
└──────────────┬──────────────────────────────┘
               │ HTTP POST/GET
               ↓
┌─────────────────────────────────────────────┐
│      CallBackController (回调入口)          │
│  - GET: 验签接口                             │
│  - POST: 消息接收接口                        │
└──────────────┬──────────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────────┐
│      WxChatMsgFactory (消息工厂)            │
│  - 根据消息类型路由到对应 Handler            │
└──────────────┬──────────────────────────────┘
               │
       ┌───────┴───────┐
       ↓               ↓
┌──────────────┐ ┌──────────────────┐
│ Subscribe    │ │ ReceiveTextMsg   │
│ MsgHandler   │ │ Handler          │
│ (关注事件)   │ │ (文本消息)       │
└──────────────┘ └────────┬─────────┘
                          │↓
                  ┌──────────────┐
                  │    Redis     │
                  │ (验证码存储) │
                  └──────────────┘
```

### 核心组件

| 组件 | 文件路径 | 作用 |
|------|---------|------|
| **Controller** | `CallBackController.java` | 微信回调接口入口 |
| **消息工厂** | `WxChatMsgFactory.java` | 消息类型路由分发 |
| **消息处理器** | `SubscribeMsgHandler.java`<br>`ReceiveTextMsgHandler.java` | 具体消息处理逻辑 |
| **Redis工具** | `RedisUtil.java` | Redis 操作封装 |
| **XML解析** | `MessageUtil.java` | 微信 XML 消息解析 |
| **签名验证** | `SHA1.java` | 微信签名验证 |

---

## 环境准备

### 必需环境

1. **JDK 8+**
2. **Maven 3.6+**
3. **Redis 服务器**
   - 默认配置：`localhost:6379`
   - 数据库索引：1
4. **微信公众号**（测试号或正式号）
5. **内网穿透工具**（本地开发必需）

### 推荐内网穿透工具

| 工具 | 特点 | 官网 |
|------|------|------|
| **Cloudflare Tunnel** | 🌟 免费、稳定、支持自定义域名 | https://www.cloudflare.com |
| **ngrok** | 稳定，国外服务器 | https://ngrok.com |
| **花生壳** | 国内服务，免费版有限制 | https://hsk.oray.com |
| **natapp** | 国内服务，基于 ngrok | https://natapp.cn |
| **cpolar** | 国内服务，免费版稳定 | https://www.cpolar.com |

---

## 配置说明

### application.yml 配置

```yaml
server:
  port: 3012  # 服务端口

spring:
  redis:
    database: 1           # Redis 数据库索引
    host: localhost       # Redis 服务器地址
    port: 6379           # Redis 端口
    password:            # Redis 密码（如有）
    timeout: 2s          # 连接超时
    lettuce:
      pool:
        max-active: 200  # 最大连接数
        max-wait: -1ms   # 最大等待时间
        max-idle: 10     # 最大空闲连接
        min-idle: 0      # 最小空闲连接
```

### 关键配置项修改

#### 1. 修改微信 Token（可选）

**文件**: `CallBackController.java:19`

```java
// 当前 Token
private static final String token = "adwidhaidwoaid";

// 修改为自定义 Token（需与微信公众号后台配置一致）
private static final String token = "your_custom_token";
```

#### 2. 修改 Redis 配置

**文件**: `application.yml`

```yaml
spring:
  redis:
    host: your_redis_host    # 修改为实际 Redis 地址
    port: 6379
    password: your_password  # 如果有密码
    database: 1
```

#### 3. 修改服务端口（可选）

```yaml
server:
  port: 3012  # 修改为其他端口
```

---

## 内网穿透配置

### 为什么需要内网穿透？

微信公众号的消息推送机制要求：
1. **回调 URL 必须是公网可访问的 HTTP/HTTPS 地址**
2. 本地开发环境（localhost）无法直接被微信服务器访问
3. 需要通过内网穿透工具将本地服务暴露到公网

### 使用 Cloudflare Tunnel 配置（推荐）

**优势**:
- ✅ 完全免费
- ✅ 支持自定义域名（需要域名托管在 Cloudflare）
- ✅ 自动 HTTPS 证书
- ✅ 稳定可靠，不限流量
- ✅ 无需开放防火墙端口

#### 步骤 1: 注册 Cloudflare 账号

1. 访问 https://dash.cloudflare.com/sign-up
2. 注册并登录账号
3. 如果有域名，将域名 DNS 托管到 Cloudflare（可选，但推荐）

#### 步骤 2: 安装 cloudflared

**Windows**:
```bash
# 下载安装包
https://github.com/cloudflare/cloudflared/releases/latest

# 或使用 winget
winget install --id Cloudflare.cloudflared
```

**Mac**:
```bash
brew install cloudflared
```

**Linux**:
```bash
# Debian/Ubuntu
wget https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64.deb
sudo dpkg -i cloudflared-linux-amd64.deb

# CentOS/RHEL
wget https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-x86_64.rpm
sudo rpm -i cloudflared-linux-x86_64.rpm
```

#### 步骤 3: 登录 Cloudflare

```bash
cloudflared tunnel login
```

执行后会打开浏览器，选择要使用的域名（如果没有域名可以跳过）。

#### 步骤 4: 创建隧道

```bash
# 创建名为 apeclub-wx 的隧道
cloudflared tunnel create apeclub-wx
```

会生成一个隧道 ID 和凭证文件，记录下隧道 ID。

#### 步骤 5: 配置隧道

创建配置文件 `~/.cloudflared/config.yml`（Windows: `C:\Users\YourName\.cloudflared\config.yml`）:

```yaml
tunnel: <your-tunnel-id>
credentials-file: C:\Users\YourName\.cloudflared\<tunnel-id>.json

ingress:
  - hostname: wx.yourdomain.com  # 替换为你的域名
    service: http://localhost:3012
  - service: http_status:404
```

**如果没有域名**，使用临时域名:
```yaml
tunnel: <your-tunnel-id>
credentials-file: /path/to/<tunnel-id>.json

ingress:
  - service: http://localhost:3012
```

#### 步骤 6: 配置 DNS（如果使用自定义域名）

```bash
cloudflared tunnel route dns apeclub-wx wx.yourdomain.com
```

#### 步骤 7: 启动隧道

```bash
cloudflared tunnel run apeclub-wx
```

启动后会显示：
```
Your tunnel is now running at: https://wx.yourdomain.com
或
Your tunnel is now running at: https://random-name.trycloudflare.com
```

**记录公网地址**，用于配置微信公众号。

#### 快速启动方式（无需配置文件）

如果只是临时测试，可以使用快速启动：

```bash
# 直接启动，自动生成临时域名
cloudflared tunnel --url http://localhost:3012
```

会自动生成类似 `https://abc-def-ghi.trycloudflare.com` 的临时域名。

**注意**: 临时域名每次启动都会变化，不适合长期使用。

---

### 使用 ngrok 配置示例

#### 步骤 1: 下载并安装 ngrok

```bash
# Windows
下载: https://ngrok.com/download
解压到任意目录

# Mac
brew install ngrok

# Linux
wget https://bin.equinox.io/c/4VmDzA7iaHb/ngrok-stable-linux-amd64.zip
unzip ngrok-stable-linux-amd64.zip
```

#### 步骤 2: 启动内网穿透

```bash
# 启动 ngrok，映射本地 3012 端口
ngrok http 3012
```

#### 步骤 3: 获取公网地址

启动后会显示：

```
Session Status                online
Account                       your_account
Version                       2.3.40
Region                        United States (us)
Web Interface                 http://127.0.0.1:4040
Forwarding                    http://abc123.ngrok.io -> http://localhost:3012
Forwarding                    https://abc123.ngrok.io -> http://localhost:3012
```

**记录公网地址**: `http://abc123.ngrok.io`（每次启动地址会变化）

### 使用 natapp 配置示例（推荐国内用户）

#### 步骤 1: 注册并获取 authtoken

1. 访问 https://natapp.cn
2. 注册账号并登录
3. 购买免费隧道（或付费隧道）
4. 获取 authtoken

#### 步骤 2: 下载客户端

```bash
# Windows: 下载 natapp.exe
# Linux/Mac: 下载对应版本
```

#### 步骤 3: 配置并启动

```bash
# Windows
natapp.exe -authtoken=your_authtoken

# Linux/Mac
./natapp -authtoken=your_authtoken
```

#### 步骤 4: 获取公网地址

```
Forwarding                    http://abc123.natapp1.cc -> 127.0.0.1:3012
```

**记录公网地址**: `http://abc123.natapp1.cc`

### 内网穿透工具对比

| 工具 | 免费版 | 固定域名 | HTTPS | 稳定性 | 国内访问 | 推荐指数 |
|------|--------|---------|-------|--------|---------|---------|
| **Cloudflare Tunnel** | ✅ 完全免费 | ✅ 支持（需域名） | ✅ 自动 | ⭐⭐⭐⭐⭐ | 🟢 良好 | ⭐⭐⭐⭐⭐ |
| **ngrok** | ⚠️ 有限制 | ❌ 需付费 | ✅ 支持 | ⭐⭐⭐⭐ | 🟡 一般 | ⭐⭐⭐⭐ |
| **natapp** | ⚠️ 有限制 | ❌ 需付费 | ✅ 支持 | ⭐⭐⭐⭐ | 🟢 优秀 | ⭐⭐⭐⭐ |
| **cpolar** | ⚠️ 有限制 | ❌ 需付费 | ✅ 支持 | ⭐⭐⭐ | 🟢 优秀 | ⭐⭐⭐ |
| **花生壳** | ⚠️ 有限制 | ❌ 需付费 | ⚠️ 部分 | ⭐⭐⭐ | 🟢 优秀 | ⭐⭐⭐ |

**推荐方案**:
- 🥇 **有域名**: Cloudflare Tunnel（完全免费 + 固定域名 + HTTPS）
- 🥈 **无域名**: natapp 付费版（国内访问快）
- 🥉 **临时测试**: Cloudflare Tunnel 快速模式或 ngrok

---

## 微信公众号配置

### 步骤 1: 申请测试公众号

1. 访问微信公众平台测试号申请页面：
   - https://mp.weixin.qq.com/debug/cgi-bin/sandbox?t=sandbox/login

2. 使用微信扫码登录

3. 获取测试号信息：
   - **appID**: 应用ID
   - **appsecret**: 应用密钥

### 步骤 2: 配置服务器地址

在测试号管理页面找到 **"接口配置信息"** 部分：

1. **URL（服务器地址）**:
   ```
   http://your_ngrok_domain/callback

   # 示例
   http://abc123.ngrok.io/callback
   ```

2. **Token（令牌）**:
   ```
   adwidhaidwoaid
   ```
   （与 `CallBackController.java:19` 中的 token 保持一致）

3. **EncodingAESKey（消息加解密密钥）**:
   - 点击"随机生成"按钮

4. **消息加解密方式**:
   - 选择 **"明文模式"**（简化开发）
   - 生产环境建议使用 **"安全模式"**

5. 点击 **"提交"** 按钮

### 步骤 3: 验证配置

提交后，微信服务器会向你的回调地址发送 GET 请求进行验签：

```
GET http://your_domain/callback?signature=xxx&timestamp=xxx&nonce=xxx&echostr=xxx
```

如果配置正确，会显示 **"配置成功"**。

### 步骤 4: 关注测试公众号

1. 在测试号管理页面找到 **"测试号二维码"**
2. 使用微信扫码关注
3. 发送 **"验证码"** 测试功能

---

## 功能实现详解

### 1. 验签流程实现

**文件**: `CallBackController.java:24-38`

```java
@GetMapping("callback")
public String callback(@RequestParam("signature") String signature,
                      @RequestParam("timestamp") String timestamp,
                      @RequestParam("nonce") String nonce,
                      @RequestParam("echostr") String echostr) {
    log.info("get验签请求参数：signature:{}，timestamp:{}，nonce:{}，echostr:{}",
            signature, timestamp, nonce, echostr);

    // 使用 SHA1 算法生成签名
    String shaStr = SHA1.getSHA1(token, timestamp, nonce, "");
    // 比对签名
    if (signature.equals(shaStr)) {
        return echostr;  // 验签成功，返回 echostr
    }
    return "unknown";
}
```

**验签原理**:
1. 微信服务器将 token、timestamp、nonce 三个参数进行字典序排序
2. 拼接成字符串后进行 SHA1 加密
3. 与 signature 参数比对，一致则验证通过

### 2. 消息接收与处理流程

**文件**: `CallBackController.java:40-68`

```java
@PostMapping(value = "callback", produces = "application/xml;charset=UTF-8")
public String callback(@RequestBody String requestBody,
                      @RequestParam("signature") String signature,
                      @RequestParam("timestamp") String timestamp,
                      @RequestParam("nonce") String nonce,
                      @RequestParam(value = "msg_signature", required = false) String msgSignature) {
    log.info("接收到微信消息：requestBody：{}", requestBody);

    // 1. 解析 XML 消息
    Map<String, String> messageMap = MessageUtil.parseXml(requestBody);

    // 2. 提取消息类型
    String msgType = messageMap.get("MsgType");
    String event = messageMap.get("Event") == null ? "" : messageMap.get("Event");

    // 3. 构建消息类型 Key
    StringBuilder msgTypeKey = new StringBuilder(msgType);
    if (StringUtils.isNotBlank(event)) {
        msgTypeKey.append(".").append(event);
    }

    // 4. 获取对应的消息处理器
    WxChatMsgHandler wxChatMsgHandler = wxChatMsgFactory.getHandlerByMsgType(msgTypeKey.toString());

    // 5. 处理消息并返回回复内容
    String replyContent = wxChatMsgHandler.dealMsg(messageMap);

    return replyContent;
}
```

**消息类型映射**:

| 微信消息类型 | msgTypeKey | Handler |
|-------------|-----------|---------|
| 关注事件 | `event.subscribe` | `SubscribeMsgHandler` |
| 文本消息 | `text` | `ReceiveTextMsgHandler` |

### 3. 验证码生成实现

**文件**: `ReceiveTextMsgHandler.java:31-58`

```java
@Override
public String dealMsg(Map<String, String> messageMap) {
    String content = messageMap.get("Content");
    // 检查是否为"验证码"关键词
    if ("验证码".equals(content)) {
        // 生成 3 位随机验证码
        Random random = new Random();
        int num = random.nextInt(1000);
        String numKey = String.valueOf(num);

        // 存储到 Redis
        String loginKey = redisUtil.buildKey("loginCode", numKey);
        redisUtil.setNx(loginKey, messageMap.get("FromUserName"), 5L, TimeUnit.MINUTES);

        // 构建回复消息
        String toUserName = messageMap.get("ToUserName");
        String fromUserName = messageMap.get("FromUserName");
        return buildTextMsg(toUserName, fromUserName, numKey);
    }

    return buildTextMsg(messageMap.get("ToUserName"),
                       messageMap.get("FromUserName"),
                       content);
}
```

**Redis 存储结构**:
```
Key: loginCode.{验证码}
Value: {用户OpenID}
TTL: 300秒（5分钟）

示例:
Key: loginCode.123
Value: oABC123xyz...
TTL: 300
```

### 4. 消息处理器扩展

如需添加新的消息类型处理，按以下步骤操作：

#### 步骤 1: 在枚举中添加新类型

**文件**: `WxChatMsgTypeEnum.java`

```java
public enum WxChatMsgTypeEnum {
    SUBSCRIBE("event.subscribe", "用户关注事件"),
    TEXT_MSG("text", "接收用户文本消息"),
    IMAGE_MSG("image", "接收用户图片消息");  // 新增

    // ...
}
```

#### 步骤 2: 创建新的 Handler

```java
@Component
public class ReceiveImageMsgHandler implements WxChatMsgHandler {

    @Override
    public WxChatMsgTypeEnum getMsgType() {
        return WxChatMsgTypeEnum.IMAGE_MSG;
    }

    @Override
    public String dealMsg(Map<String, String> messageMap) {
        // 处理图片消息逻辑
        String picUrl = messageMap.get("PicUrl");
        String mediaId = messageMap.get("MediaId");

        // 返回回复消息
        return buildTextMsg(messageMap.get("ToUserName"),
                           messageMap.get("FromUserName"),
                           "收到您的图片");
    }
}
```

#### 步骤 3: 自动注册

`WxChatMsgFactory` 会在 Spring 容器初始化后自动扫描并注册所有实现了 `WxChatMsgHandler` 接口的 Bean。

---

## 部署运行

### 本地开发环境运行

#### 步骤 1: 启动 Redis

```bash
# Windows
redis-server.exe

# Linux/Mac
redis-server
```

#### 步骤 2: 启动应用

```bash
# 方式 1: Maven 命令
cd ape-club-wx
mvn spring-boot:run

# 方式 2: IDEA 运行
右键 WxApplication.java -> Run 'WxApplication'

# 方式 3: 打包运行
mvn clean package
java -jar target/ape-club-wx-1.0-SNAPSHOT.jar
```

#### 步骤 3: 启动内网穿透

```bash
# ngrok
ngrok http 3012

# natapp
natapp -authtoken=your_token
```

#### 步骤 4: 配置微信公众号

将内网穿透获得的公网地址配置到微信公众号后台。

#### 步骤 5: 测试功能

1. 关注测试公众号
2. 发送 "验证码" 获取验证码
3. 查看 Redis 中的数据：
   ```bash
   redis-cli
   SELECT 1
   KEYS loginCode.*
   GET loginCode.123
   ```

### 生产环境部署

#### 方式 1: 直接部署

```bash
# 打包
mvn clean package -DskipTests

# 上传到服务器
scp target/ape-club-wx-1.0-SNAPSHOT.jar user@server:/app/

# 启动
nohup java -jar /app/ape-club-wx-1.0-SNAPSHOT.jar > /app/logs/wx.log 2>&1 &
```

#### 方式 2: Docker 部署

创建 `Dockerfile`:

```dockerfile
FROM openjdk:8-jre-alpine
WORKDIR /app
COPY target/ape-club-wx-1.0-SNAPSHOT.jar app.jar
EXPOSE 3012
ENTRYPOINT ["java", "-jar", "app.jar"]
```

构建并运行:

```bash
# 构建镜像
docker build -t apeclub-wx:1.0 .

# 运行容器
docker run -d \
  --name apeclub-wx \
  -p 3012:3012 \
  -e SPRING_REDIS_HOST=redis_host \
  -e SPRING_REDIS_PASSWORD=redis_password \
  apeclub-wx:1.0
```

#### 方式 3: Nginx 反向代理

```nginx
server {
    listen 80;
    server_name wx.yourdomain.com;

    location /callback {
        proxy_pass http://localhost:3012/callback;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

---

## 常见问题

### 1. 配置提交失败：Token验证失败

**原因**:
- Token 不一致
- 服务未启动
- 内网穿透未生效
- 防火墙拦截

**解决方案**:
```bash
# 1. 检查 Token 是否一致
# CallBackController.java:19 的 token 值
# 与微信公众号后台配置的 Token 值

# 2. 检查服务是否启动
curl http://localhost:3012/callback

# 3. 检查内网穿透
curl http://your_ngrok_domain/callback

# 4. 查看日志
tail -f logs/application.log
```

### 2. 发送消息无响应

**原因**:
- Handler 未正确注册
- 消息类型不匹配
- Redis 连接失败

**解决方案**:
```bash
# 1. 检查 Handler 是否注册
# 查看启动日志中的 Bean 注册信息

# 2. 检查消息类型
# 在 CallBackController 中添加日志
log.info("msgTypeKey: {}", msgTypeKey.toString());

# 3. 检查 Redis 连接
redis-cli ping
```

### 3. 验证码无法存储到 Redis

**原因**:
- Redis 未启动
- Redis 配置错误
- 网络不通

**解决方案**:
```bash
# 1. 检查 Redis 是否启动
redis-cli ping

# 2. 检查 Redis 配置
# application.yml 中的 host、port、password

# 3. 测试 Redis 连接
redis-cli -h localhost -p 6379 -a password
SELECT 1
PING
```

### 4. 内网穿透地址频繁变化

**原因**:
- 使用免费版 ngrok，每次启动地址都会变化

**解决方案**:
1. **购买 ngrok 付费版**，获取固定域名
2. **使用 natapp 付费隧道**，获取固定域名
3. **部署到公网服务器**，使用固定 IP 或域名

### 5. 微信消息延迟或丢失

**原因**:
- 服务器响应超时（微信要求 5 秒内响应）
- 内网穿透不稳定
- 服务器性能不足

**解决方案**:
```java
// 1. 优化消息处理逻辑，避免耗时操作
// 2. 使用异步处理
@Async
public void processMessage(Map<String, String> messageMap) {
    // 耗时操作
}

// 3. 先返回"success"，再异步处理
return "success";
```

### 6. 如何调试微信消息

**方法 1: 使用微信开发者工具**
- 下载：https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html
- 可以模拟发送各种类型的消息

**方法 2: 使用 Postman 模拟**
```bash
POST http://localhost:3012/callback
Content-Type: application/xml

<xml>
  <ToUserName><![CDATA[公众号ID]]></ToUserName>
  <FromUserName><![CDATA[用户OpenID]]></FromUserName>
  <CreateTime>1234567890</CreateTime>
  <MsgType><![CDATA[text]]></MsgType>
  <Content><![CDATA[验证码]]></Content>
  <MsgId>1234567890123456</MsgId>
</xml>
```

**方法 3: 查看日志**
```bash
# 在 CallBackController 中添加详细日志
log.info("接收到的消息: {}", messageMap);
log.info("消息类型: {}", msgTypeKey);
log.info("处理结果: {}", replyContent);
```

---

## 总结

### 是否需要内网穿透？

| 场景 | 是否需要 | 说明 |
|------|---------|------|
| **本地开发** | ✅ 必需 | 微信服务器无法访问 localhost |
| **公网服务器** | ❌ 不需要 | 直接使用公网 IP 或域名 |
| **内网服务器** | ✅ 必需 | 需要通过内网穿透或端口映射 |

### 实现方式总结

1. **消息接收**: 通过 HTTP POST 接口接收微信服务器推送的 XML 消息
2. **消息解析**: 使用 dom4j 解析 XML 为 Map 结构
3. **消息路由**: 通过工厂模式根据消息类型分发到对应 Handler
4. **消息处理**: Handler 处理业务逻辑并返回 XML 格式回复
5. **数据存储**: 使用 Redis 存储验证码等临时数据
6. **安全验证**: 使用 SHA1 签名验证微信服务器请求合法性

### 关键配置清单

- ✅ Redis 服务启动
- ✅ application.yml 配置正确
- ✅ 内网穿透工具启动（本地开发）
- ✅ 微信公众号后台配置回调 URL
- ✅ Token 保持一致
- ✅ 服务正常启动在 3012 端口

### 核心文件路径

```
ape-club-wx/
├── src/main/java/com/jingdianjichi/wx/
│   ├── WxApplication.java                    # 启动类
│   ├── controller/CallBackController.java    # 微信回调接口
│   ├── handler/
│   │   ├── WxChatMsgFactory.java            # 消息工厂
│   │   ├── SubscribeMsgHandler.java         # 关注事件处理
│   │   └── ReceiveTextMsgHandler.java       # 文本消息处理
│   ├── redis/
│   │   ├── RedisConfig.java                 # Redis配置
│   │   └── RedisUtil.java                   # Redis工具
│   └── utils/
│       ├── MessageUtil.java                 # XML解析
│       └── SHA1.java                        # 签名验证
└── src/main/resources/
    └── application.yml                       # 应用配置
```

---

**文档版本**: v1.0
**最后更新**: 2026-02-04
**维护者**: ApeClub Team
**联系方式**: 如有问题请提交 Issue

---

## 附录

### 微信公众号消息类型参考

| 消息类型 | MsgType | 说明 |
|---------|---------|------|
| 文本消息 | text | 用户发送的文本内容 |
| 图片消息 | image | 用户发送的图片 |
| 语音消息 | voice | 用户发送的语音 |
| 视频消息 | video | 用户发送的视频 |
| 地理位置 | location | 用户发送的地理位置 |
| 链接消息 | link | 用户分享的链接 |
| 关注事件 | event.subscribe | 用户关注公众号 |
| 取消关注 | event.unsubscribe | 用户取消关注 |
| 点击菜单 | event.CLICK | 用户点击自定义菜单 |

### 相关资源

- [微信公众平台开发文档](https://developers.weixin.qq.com/doc/offiaccount/Getting_Started/Overview.html)
- [微信公众平台测试号申请](https://mp.weixin.qq.com/debug/cgi-bin/sandbox?t=sandbox/login)
- [ngrok 官网](https://ngrok.com)
- [natapp 官网](https://natapp.cn)
- [Redis 官方文档](https://redis.io/documentation)

