# jc-club-practice 项目分析文档

## 1. 项目概述与作用

### 1.1 项目背景
jc-club-practice是一个基于Spring Boot和Spring Cloud构建的在线练习微服务项目，旨在提供系统化的在线练习、答题和评估功能，帮助用户进行知识学习和技能提升。

### 1.2 主要目标
- 提供多样化的练习模式（专项练习、模拟套题）
- 实现完整的练习流程管理（开始练习、答题、提交、评估）
- 生成详细的练习报告和技能分析
- 支持练习进度追踪和历史记录管理
- 构建高可用、可扩展的微服务架构

### 1.3 应用场景
- **在线学习平台**：为用户提供知识点专项练习和模拟测试
- **技能评估系统**：通过标准化练习评估用户技能水平
- **企业培训系统**：为员工提供岗位相关的技能练习和考核
- **教育机构**：为学生提供课程练习和考试模拟

### 1.4 解决的实际问题
- 缺乏系统化的练习管理平台
- 无法准确评估学习效果和技能掌握程度
- 练习过程无法追踪和分析
- 难以提供个性化的学习建议

## 2. 核心功能模块

### 2.1 练习集管理模块

#### 2.1.1 专项练习内容获取
**功能描述**：获取系统中所有专项练习内容，按岗位和分类组织展示
**使用场景**：用户选择专项练习时，展示可练习的知识点分类
**实现逻辑**：
```java
@RequestMapping("getSpecialPracticeContent")
public Result<List<SpecialPracticeVO>> getSpecialPracticeContent(){
    // 查询拥有单选，多选，判断的岗位
    // 查询岗位下分类
    // 根据分类和类型查询题目
}
```

#### 2.1.2 开始练习
**功能描述**：根据用户选择的标签创建新的练习集
**使用场景**：用户选择特定标签组合后，生成对应的练习题目集
**实现逻辑**：
```java
@PostMapping(value = "/addPractice")
public Result<PracticeSetVO> addPractice(@RequestBody GetPracticeSubjectListReq req) {
    // 参数校验
    // 根据标签ids生成练习集
    // 返回练习集信息
}
```

#### 2.1.3 获取练习题目
**功能描述**：获取特定练习集的所有题目
**使用场景**：用户进入练习界面后，加载所有需要完成的题目
**实现逻辑**：
```java
@PostMapping(value = "/getSubjects")
public Result<PracticeSubjectListVO> getSubjects(@RequestBody GetPracticeSubjectsReq req) {
    // 参数校验
    // 根据练习id获取所有题目
    // 返回题目列表
}
```

#### 2.1.4 获取题目详情
**功能描述**：获取单个题目的详细信息
**使用场景**：用户查看特定题目的详细内容和选项
**实现逻辑**：
```java
@PostMapping(value = "/getPracticeSubject")
public Result<PracticeSubjectVO> getPracticeSubject(@RequestBody GetPracticeSubjectReq req) {
    // 参数校验
    // 根据题目id和类型获取题目详情
    // 返回题目详细信息
}
```

#### 2.1.5 获取模拟套题内容
**功能描述**：分页获取系统中的模拟套题列表
**使用场景**：用户选择模拟考试时，展示可选择的套题
**实现逻辑**：
```java
@PostMapping(value = "/getPreSetContent")
public Result<PageResult<PracticeSetVO>> getPreSetContent(@RequestBody GetPreSetReq req) {
    // 参数校验
    // 分页查询模拟套题
    // 返回套题列表
}
```

#### 2.1.6 获取未完成练习
**功能描述**：获取用户所有未完成的练习集
**使用场景**：用户查看历史练习记录，继续未完成的练习
**实现逻辑**：
```java
@PostMapping(value = "/getUnCompletePractice")
public Result<PageResult<UnCompletePracticeSetVO>> getUnCompletePractice(@RequestBody GetUnCompletePracticeReq req) {
    // 参数校验
    // 查询未完成的练习集
    // 返回练习集列表
}
```

### 2.2 练习详情管理模块

#### 2.2.1 提交题目答案
**功能描述**：提交单个题目的答案
**使用场景**：用户完成单个题目后，提交答案并继续下一题
**实现逻辑**：
```java
@PostMapping(value = "/submitSubject")
public Result<Boolean> submitSubject(@RequestBody SubmitSubjectDetailReq req) {
    // 参数校验
    // 保存题目答案和用时
    // 返回提交结果
}
```

#### 2.2.2 提交完整练习
**功能描述**：提交整个练习集的所有答案
**使用场景**：用户完成所有题目后，提交整个练习进行评分
**实现逻辑**：
```java
@PostMapping(value = "/submit")
public Result<Boolean> submit(@RequestBody SubmitPracticeDetailReq req) {
    // 参数校验
    // 计算练习得分和完成状态
    // 更新练习记录
    // 返回提交结果
}
```

#### 2.2.3 获取得分详情
**功能描述**：获取练习中每题的得分情况
**使用场景**：用户查看练习结果时，了解每题的得分和对错情况
**实现逻辑**：
```java
@PostMapping(value = "/getScoreDetail")
public Result<List<ScoreDetailVO>> getScoreDetail(@RequestBody GetScoreDetailReq req) {
    // 参数校验
    // 查询每题的得分详情
    // 返回得分详情列表
}
```

#### 2.2.4 获取题目详细解析
**功能描述**：获取特定题目的详细解析
**使用场景**：用户查看错题解析或学习题目解答思路
**实现逻辑**：
```java
@PostMapping(value = "/getSubjectDetail")
public Result<SubjectDetailVO> getSubjectDetail(@RequestBody GetSubjectDetailReq req) {
    // 参数校验
    // 查询题目详细解析
    // 返回解析内容
}
```

#### 2.2.5 生成评估报告
**功能描述**：生成练习的详细评估报告
**使用场景**：用户完成练习后，查看综合评估结果和技能分析
**实现逻辑**：
```java
@PostMapping(value = "/getReport")
public Result<ReportVO> getReport(@RequestBody GetReportReq req) {
    // 参数校验
    // 分析练习数据生成评估报告
    // 返回报告内容
}
```

#### 2.2.6 获取练习排行榜
**功能描述**：获取练习排行榜信息
**使用场景**：用户查看自己在练习中的排名情况
**实现逻辑**：
```java
@PostMapping(value = "/getPracticeRankList")
public Result<List<RankVO>> getPracticeRankList() {
    // 查询练习排行榜数据
    // 返回排名列表
}
```

#### 2.2.7 放弃练习
**功能描述**：放弃当前正在进行的练习
**使用场景**：用户因各种原因无法继续练习时，主动放弃
**实现逻辑**：
```java
@PostMapping(value = "/giveUp")
public Result<Boolean> giveUp(@RequestParam("practiceId") Long practiceId) {
    // 参数校验
    // 更新练习状态为已放弃
    // 返回操作结果
}
```

## 3. 关键技术栈

### 3.1 基础架构
| 技术组件 | 版本 | 用途 |
|---------|------|------|
| Spring Boot | 2.4.2 | 微服务基础框架 |
| Spring Cloud | 2020.0.6 | 微服务生态系统 |
| Spring Cloud Alibaba | 2021.1 | 服务注册与配置管理 |

### 3.2 数据访问
| 技术组件 | 版本 | 用途 |
|---------|------|------|
| MyBatis-Plus | 3.4.0 | ORM框架 |
| MySQL | 8.0.22 | 关系型数据库 |
| Redis | - | 缓存中间件 |

### 3.3 工具库
| 技术组件 | 版本 | 用途 |
|---------|------|------|
| Lombok | 1.18.16 | 简化Java代码 |
| MapStruct | 1.4.2.Final | 对象映射 |
| Google Guava | 19.0 | 工具类库 |
| Apache Commons Lang3 | 3.11 | 通用工具类 |
| FastJSON | 1.2.24 | JSON处理 |

### 3.4 日志与监控
| 技术组件 | 版本 | 用途 |
|---------|------|------|
| Log4j2 | - | 日志框架 |
| SLF4J | - | 日志门面 |

### 3.5 构建与部署
| 技术组件 | 版本 | 用途 |
|---------|------|------|
| Maven | - | 项目构建工具 |
| Nacos | - | 服务注册与配置中心 |

## 4. 项目结构

### 4.1 模块结构
```
jc-club-practice/
├── jc-club-practice-api/        # API模块，定义公共接口、DTO和VO
│   ├── src/main/java/com/jingdianjichi/circle/api/
│   │   ├── common/              # 通用类（分页、结果包装）
│   │   ├── enums/               # 枚举类
│   │   ├── req/                 # 请求参数类
│   │   └── vo/                  # 返回结果类
│   └── pom.xml
├── jc-club-practice-server/     # 服务模块，实现业务逻辑
│   ├── src/main/java/com/jingdianjichi/practice/server/
│   │   ├── config/              # 配置类
│   │   ├── controller/          # 控制器
│   │   ├── dao/                 # 数据访问层
│   │   ├── entity/              # 实体类
│   │   ├── rpc/                 # 远程服务调用
│   │   ├── service/             # 业务逻辑层
│   │   └── util/                # 工具类
│   ├── src/main/resources/      # 配置文件
│   └── pom.xml
└── pom.xml                      # 根POM文件
```

### 4.2 核心类关系
- **控制器层**：PracticeSetController（练习集管理）、PracticeDetailController（练习详情管理）
- **服务层**：PracticeSetService（练习集业务）、PracticeDetailService（练习详情业务）
- **数据访问层**：基于MyBatis-Plus的DAO接口
- **数据传输**：使用DTO/VO模式进行数据封装和传输

## 5. 技术亮点

### 5.1 分层架构设计
采用经典的Controller-Service-Dao三层架构，职责清晰，便于维护和扩展。

### 5.2 微服务架构
基于Spring Cloud和Nacos构建微服务，支持服务注册与发现、配置中心等功能。

### 5.3 完善的异常处理
使用Google Guava Preconditions进行参数校验，结合统一的结果包装类（Result）处理异常情况。

### 5.4 灵活的练习模式
支持专项练习和模拟套题两种主要练习模式，满足不同用户需求。

### 5.5 详细的数据分析
提供练习报告、技能评估、排行榜等功能，帮助用户了解学习效果。

## 6. 总结

jc-club-practice是一个功能完善、架构清晰的在线练习微服务项目，采用了主流的Java技术栈和微服务架构，实现了从练习创建、答题、提交到评估的完整流程。项目具有良好的可扩展性和维护性，能够满足不同场景下的在线练习需求，为用户提供系统化的学习和评估解决方案。