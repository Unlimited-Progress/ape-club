  2. 必需中间件

  MySQL 数据库

  - 版本: 5.7 或 8.0
  - 端口: 3306
  - 数据库名: ape-club（已从 jc-club 改名）
  - 用户名: root
  - 密码: 需要您自己设置
  - 字符集: utf8mb4
  - 时区: Asia/Shanghai

  安装步骤：

  Redis 缓存

  - 版本: 5.0+
  - 端口: 6379
  - 密码: 无（已清空，如需设置请在配置文件中添加）
  - 使用的数据库: 0 和 1

  安装步骤：

**Windows: 下载 Redis for Windows**

**启动: redis-server.exe**

**或使用 Docker:**

  docker run -d -p 6379:6379 --name redis redis:latest

  Nacos 注册中心

  - 版本: 2.0+
  - 端口: 8848
  - 地址: localhost:8848
  - 用户名/密码: nacos/nacos

  安装步骤：
1. 下载 Nacos: https://github.com/alibaba/nacos/releases

2. 解压后进入 bin 目录

3. 单机模式启动:

Windows: startup.cmd -m standalone

Linux/Mac: sh startup.sh -m standalone

4. 访问控制台: http://localhost:8848/nacos

  需要配置的服务：
  - ape-club-gateway-dev
  - ape-club-auth-dev
  - ape-club-oss-dev
  - ape-club-subject-dev
  - ape-club-practice-dev
  - ape-club-circle-dev

  MinIO 对象存储

  - 版本: 最新版
  - API 端口: 9000
  - 控制台端口: 9001
  - 地址: http://localhost:9000
  - AccessKey: minioadmin
  - SecretKey: minioadmin

  安装步骤：

使用 Docker 安装（推荐）:

  docker run -d \
    -p 9000:9000 \
    -p 9001:9001 \
    --name minio \
    -e "MINIO_ROOT_USER=minioadmin" \
    -e "MINIO_ROOT_PASSWORD=minioadmin" \
    minio/minio server /data --console-address ":9001"

访问控制台: http://localhost:9001

  3. 可选中间件

  Elasticsearch（如需搜索功能）

  - 版本: 7.x
  - 端口: 9200
  - 地址: localhost:9200

  安装步骤：

使用 Docker:

  docker run -d \
    -p 9200:9200 \
    -p 9300:9300 \
    -e "discovery.type=single-node" \
    --name elasticsearch \
    elasticsearch:7.17.0

  XXL-JOB（如需定时任务）

  - 版本: 2.3+
  - 端口: 8080
  - 地址: http://localhost:8080/xxl-job-admin
  - AccessToken: default_token

---
  🔧 已修改的配置文件清单

  Gateway 服务

  - ✅ ape-club-gateway/src/main/resources/bootstrap.yml
    - 应用名: jc-club-gateway-dev → ape-club-gateway-dev
    - Nacos: 192.168.30.128:8848 → localhost:8848
  - ✅ ape-club-gateway/src/main/resources/application.yml
    - 所有服务路由: jc-club-* → ape-club-*
    - Redis: 192.168.30.128 → localhost
    - Redis 密码: jichi1234 → 已清空

  Auth 服务

  - ✅ ape-club-auth/ape-club-auth-starter/src/main/resources/bootstrap.yml
    - 应用名: jc-club-auth-dev → ape-club-auth-dev
    - Nacos: 192.168.30.128:8848 → localhost:8848
  - ✅ ape-club-auth/ape-club-auth-starter/src/main/resources/application.yml
    - MySQL: 127.0.0.1:3306/jc-club → localhost:3306/ape-club
    - Redis: 192.168.30.128 → localhost
    - Redis 密码: jichi1234 → 已清空

  OSS 服务

  - ✅ ape-club-oss/src/main/resources/bootstrap.yml
    - 应用名: jc-club-oss-dev → ape-club-oss-dev
    - Nacos: 192.168.30.128:8848 → localhost:8848
  - ✅ ape-club-oss/src/main/resources/application.yml
    - MinIO: 192.168.30.128:9000 → localhost:9000

  Subject 服务

  - ✅ ape-club-subject/ape-club-starter/src/main/resources/bootstrap.yml
    - 应用名: jc-club-subject-dev → ape-club-subject-dev
    - Nacos: 192.168.30.128:8848 → localhost:8848
  - ✅ ape-club-subject/ape-club-starter/src/main/resources/application.yml
    - Redis: 192.168.30.128 → localhost
    - Redis 密码: jichi1234 → 已清空
    - Elasticsearch: 192.168.30.128:9200 → localhost:9200
    - XXL-JOB: 127.0.0.1 → localhost
    - XXL-JOB appname: jc-club-subjcet → ape-club-subject（修正拼写错误）

  Practice 服务

  - ✅ ape-club-practice/ape-club-practice-server/src/main/resources/bootstrap.yml
    - 应用名: jc-club-practice-dev → ape-club-practice-dev
    - Nacos: 117.72.14.166:8848 → localhost:8848
  - ✅ ape-club-practice/ape-club-practice-server/src/main/resources/application.yml
    - MySQL: 117.72.14.166:3306/jc-club → localhost:3306/ape-club
    - Redis: 117.72.14.166 → localhost
    - Redis 密码: jichi1234 → 已清空

  Circle 服务

  - ✅ ape-club-circle/ape-club-circle-server/src/main/resources/bootstrap.yml
    - 应用名: jc-club-circle-dev → ape-club-circle-dev
    - Nacos: 117.72.14.166:8848 → localhost:8848
  - ✅ ape-club-circle/ape-club-circle-server/src/main/resources/application.yml
    - MySQL: 127.0.0.1:3306/jc-club → localhost:3306/ape-club
    - Redis: 117.72.14.166 → localhost
    - Redis 密码: jichi1234 → 已清空

  WX 服务

  - ✅ ape-club-wx/src/main/resources/application.yml
    - Redis: 192.168.30.128 → localhost
    - Redis 密码: jichi1234 → 已清空

---
  ⚠️ 重要提示

  1. 数据库密码加密

  配置文件中的数据库密码使用了 Druid 加密，您需要：
  - 保留现有的加密密码
  - 或者重新生成加密密码
  - 或者修改为明文密码（不推荐）

  2. 需要手动创建的资源

  - MySQL 数据库：ape-club
