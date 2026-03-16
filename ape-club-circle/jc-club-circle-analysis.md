# jc-club-circle 模块分析文档

## 项目概述与作用

### 项目定位

jc-club-circle 是 JC-Club 社交平台的核心社区功能模块，专注于为用户提供围绕特定兴趣主题的社交互动空间。该模块采用微服务架构设计，作为 JC-Club 系统中负责内容社交的独立服务单元，承担着用户社交行为的核心处理职责。通过圈子的形式组织用户群体，支持动态发布、评论互动、消息通知等社交功能，构建了一个完整的社区社交生态系统。该模块在整体系统架构中与用户服务（jc-club-user）、认证服务（jc-club-auth）紧密协作，共同支撑起 JC-Club 平台的核心社交价值。

### 核心价值

jc-club-circle 模块为 JC-Club 平台带来了多重核心价值。首先，它提供了以兴趣为导向的社交组织形式，用户可以根据个人兴趣爱好加入不同的圈子，在志同道合的社区中进行深度交流，这种基于主题的社交模式显著提升了用户粘性和社交质量。其次，模块实现了完整的内容生产消费闭环，从动态发布到评论互动再到消息通知，形成了活跃的社区氛围和正向循环的内容生态。再者，通过敏感词过滤机制保障了社区内容的健康度，维护了良好的社区环境。最后，实时消息推送功能通过 WebSocket 技术实现，让用户能够即时获取社交动态反馈，大幅提升了用户体验和参与度。

### 项目结构

jc-club-circle 采用 Maven 多模块设计，包含父级项目和两个子模块。父级项目（jc-club-circle）定义了统一的基础依赖和版本管理策略，确保子模块间依赖版本的一致性。jc-club-circle-api 模块负责定义公共的请求对象（Request）、响应对象（VO）、枚举类型和通用结果封装，为服务端和客户端提供统一的接口契约。jc-club-circle-server 模块是业务服务的核心实现，包含了控制器（Controller）、服务层（Service）、数据访问层（DAO）、实体类（PO）、敏感词过滤组件、WebSocket 实时通信等全部业务逻辑和技术组件。这种分层清晰的模块化设计有利于代码的维护和扩展，同时也方便团队协作开发。

```
jc-club-circle/
├── jc-club-circle-api/          # API 模块，定义接口契约
│   └── src/main/java/com/jingdianjichi/circle/api/
│       ├── common/              # 通用组件（Result、PageInfo、TreeNode等）
│       ├── enums/               # 枚举定义（IsDeletedFlagEnum）
│       ├── req/                 # 请求对象（SaveShareCircleReq等）
│       └── vo/                  # 响应对象（ShareCircleVO等）
├── jc-club-circle-server/       # 服务实现模块
│   └── src/main/java/com/jingdianjichi/circle/server/
│       ├── config/              # 配置类（Redis、WebSocket、MyBatis等）
│       ├── controller/          # 控制器层
│       ├── dao/                 # 数据访问层
│       ├── entity/              # 实体类（PO、DTO）
│       ├── rpc/                 # 远程调用（UserRpc）
│       ├── sensitive/           # 敏感词过滤组件
│       ├── service/             # 服务层及实现
│       ├── util/                # 工具类
│       └── websocket/           # WebSocket实时通信
└── pom.xml                      # 父级POM配置
```

## 核心功能模块

### 圈子管理模块

圈子管理模块（ShareCircleController）是 jc-club-circle 的基础功能组件，负责平台中所有圈子的创建、查询、更新和删除操作。该模块采用了层级化的圈子设计理念，支持父级圈子下嵌套子圈子的树形结构，这种设计使得内容分类更加清晰有序，用户可以快速定位感兴趣的社区。圈子实体（ShareCircle）包含圈子ID、父级ID、圈子名称、圈子图标、创建人、创建时间、更新时间、删除标志等核心字段，通过 parentId 字段建立圈子间的层级关系，parentId 为 -1 表示顶级大类圈子。

在实现层面，圈子查询接口（listResult）采用了 Caffeine 本地缓存策略，缓存有效期设置为 30 秒，有效降低了数据库访问压力，同时保证了数据的相对实时性。查询结果会按照父子关系组织成树形结构返回，前端可以直接渲染多级分类菜单。新增圈子操作（save）接收 SaveShareCircleReq 请求对象，支持设置圈子名称、图标和父级圈子ID，系统会自动填充创建人、创建时间等元信息。更新和删除操作同样通过各自的接口实现，删除采用逻辑删除策略，通过 isDeleted 字段标记数据状态而非物理删除数据，保证了数据的可恢复性和完整性。

```java
// 圈子查询实现（基于缓存的树形结构查询）
public List<ShareCircleVO> listResult() {
    List<ShareCircleVO> res = CACHE.getIfPresent(1);
    return Optional.ofNullable(res).orElseGet(() -> {
        List<ShareCircle> list = super.list(Wrappers.<ShareCircle>lambdaQuery()
                .eq(ShareCircle::getIsDeleted, IsDeletedFlagEnum.UN_DELETED.getCode()));
        // 查询父级圈子
        List<ShareCircle> parentList = list.stream()
                .filter(item -> item.getParentId() == -1L)
                .collect(Collectors.toList());
        // 按父级ID分组构建树形结构
        Map<Long, List<ShareCircle>> map = list.stream()
                .collect(Collectors.groupingBy(ShareCircle::getParentId));
        List<ShareCircleVO> collect = parentList.stream().map(item -> {
            ShareCircleVO vo = new ShareCircleVO();
            vo.setId(item.getId());
            vo.setCircleName(item.getCircleName());
            vo.setIcon(item.getIcon());
            List<ShareCircle> shareCircles = map.get(item.getId());
            if (CollectionUtils.isEmpty(shareCircles)) {
                vo.setChildren(Collections.emptyList());
            } else {
                List<ShareCircleVO> children = shareCircles.stream().map(cItem -> {
                    ShareCircleVO cVo = new ShareCircleVO();
                    cVo.setId(cItem.getId());
                    cVo.setCircleName(cItem.getCircleName());
                    cVo.setIcon(cItem.getIcon());
                    cVo.setChildren(Collections.emptyList());
                    return cVo;
                }).collect(Collectors.toList());
                vo.setChildren(children);
            }
            return vo;
        }).collect(Collectors.toList());
        CACHE.put(1, collect);
        return collect;
    });
}
```

### 内容分享模块

内容分享模块（ShareMomentController）是 jc-club-circle 的核心社交功能，承载了用户在圈子内发布和消费动态内容的主要场景。该模块通过 ShareMoment 实体记录用户的动态信息，包括动态ID、所属圈子ID、文本内容、图片URL列表、回复数量、创建人、创建时间、更新时间、删除标志等字段。模块设计充分考虑了内容形式的多样性，同时支持纯文本和图文两种内容形态，图片URL 以 JSON 数组形式存储在 picUrls 字段中，便于前端灵活渲染展示。

动态发布功能（save）是用户产生内容的核心入口，接口接收 SaveMomentCircleReq 请求参数，包含圈子ID、文本内容、图片URL列表等关键信息。在处理流程中，系统首先进行参数校验，确保圈子ID有效且属于合法的小圈子（父级ID不为-1），然后调用敏感词过滤器对文本内容进行检查，只有通过敏感词过滤的内容才能成功发布。发布成功后，系统会记录发布人ID和当前时间戳，并将回复计数初始化为零。动态查询功能（getMoments）支持按圈子ID分页查询动态列表，返回结果中包含动态详情和发布人用户信息，通过 RPC 调用用户服务批量获取用户头像、昵称等元数据，丰富了动态展示内容。

```java
// 动态发布实现（包含敏感词过滤）
public Boolean saveMoment(SaveMomentCircleReq req) {
    ShareMoment moment = new ShareMoment();
    moment.setCircleId(req.getCircleId());
    moment.setContent(req.getContent());
    if (!CollectionUtils.isEmpty(req.getPicUrlList())) {
        moment.setPicUrls(JSON.toJSONString(req.getPicUrlList()));
    }
    moment.setReplyCount(0);
    moment.setCreatedBy(LoginUtil.getLoginId());
    moment.setCreatedTime(new Date());
    moment.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
    return super.save(moment);
}

// 分页查询动态（批量获取用户信息）
public PageResult<ShareMomentVO> getMoments(GetShareMomentReq req) {
    LambdaQueryWrapper<ShareMoment> queryWrapper = Wrappers.<ShareMoment>lambdaQuery()
            .eq(Objects.nonNull(req.getCircleId()), ShareMoment::getCircleId, req.getCircleId())
            .eq(ShareMoment::getIsDeleted, IsDeletedFlagEnum.UN_DELETED.getCode())
            .orderByDesc(ShareMoment::getCircleId);
    Page<ShareMoment> page = new Page<>(pageInfo.getPageNo(), pageInfo.getPageSize());
    Page<ShareMoment> pageRes = super.page(page, queryWrapper);
    // 批量查询用户信息
    List<String> userNameList = records.stream().map(ShareMoment::getCreatedBy)
            .distinct().collect(Collectors.toList());
    Map<String, UserInfo> userInfoMap = userRpc.batchGetUserInfo(userNameList);
    // 转换为VO并填充用户信息
    List<ShareMomentVO> list = records.stream().map(shareMoment -> {
        ShareMomentVO shareMomentVO = new ShareMomentVO();
        // 设置动态信息...
        UserInfo userInfo = userInfoMap.getOrDefault(shareMoment.getCreatedBy(), defaultUser);
        shareMomentVO.setUserIcon(userInfo.getAvatar());
        shareMomentVO.setUserName(userInfo.getNickname());
        return shareMomentVO;
    }).collect(Collectors.toList());
}
```

### 评论互动模块

评论互动模块（ShareCommentController）构建了 jc-club-circle 中用户间的深度互动机制，允许用户对动态内容发表观点和进行回复讨论。该模块通过 ShareCommentReply 实体存储评论和回复数据，实体设计支持两种交互形态：评论（replyType=1）表示用户对动态本身的直接评论，此时 parentId 设为 -1，toUser 指向动态作者；回复（replyType=2）表示用户对其他评论的回应，此时 parentId 指向被回复的评论ID，replyId 同样指向被回复的评论。这种设计使得评论系统能够支持多层级嵌套的讨论结构，形成类似树形的话题讨论区。

模块实现了完整的评论生命周期管理。发布评论时，系统会进行多重校验，包括验证动态ID的有效性、检查动态是否已被删除、确保评论内容非空等。校验通过后，系统会调用敏感词过滤检查文本内容，然后将评论数据写入数据库，同时自增动态的回复计数（incrReplyCount）。评论通知机制是社交功能的重要组成部分，当用户评论某条动态时，系统会通过 ShareMessageService 向动态作者发送消息通知，建立了评论者与被评论者之间的社交连接。删除评论采用递归删除策略，在删除某条评论时，会递归查找并删除该评论下的所有子回复，确保数据层级结构的正确性。

```java
// 评论发布实现（支持评论和回复两种类型）
public Boolean saveComment(SaveShareCommentReplyReq req) {
    ShareMoment moment = shareMomentMapper.selectById(req.getMomentId());
    ShareCommentReply comment = new ShareCommentReply();
    comment.setMomentId(req.getMomentId());
    comment.setReplyType(req.getReplyType());
    String loginId = LoginUtil.getLoginId();
    // 类型1为评论，类型2为回复
    if (req.getReplyType() == 1) {
        comment.setParentId(-1L);
        comment.setToId(req.getTargetId());
        comment.setToUser(loginId);
        comment.setToUserAuthor(
            Objects.nonNull(moment.getCreatedBy()) && loginId.equals(moment.getCreatedBy()) ? 1 : 0);
    } else {
        comment.setParentId(req.getTargetId());
        comment.setReplyId(req.getTargetId());
        comment.setReplyUser(loginId);
        comment.setReplayAuthor(
            Objects.nonNull(moment.getCreatedBy()) && loginId.equals(moment.getCreatedBy()) ? 1 : 0);
    }
    comment.setContent(req.getContent());
    if (!CollectionUtils.isEmpty(req.getPicUrlList())) {
        comment.setPicUrls(JSON.toJSONString(req.getPicUrlList()));
    }
    comment.setCreatedBy(LoginUtil.getLoginId());
    comment.setCreatedTime(new Date());
    comment.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
    // 每次评论，数量加1
    shareMomentMapper.incrReplyCount(moment.getId(), 1);
    return super.save(comment);
}

// 评论通知触发
public void comment(String fromId, String toId, Long targetId) {
    JSONObject message = new JSONObject();
    message.put("msgType", "COMMENT");
    message.put("msg", "评论了你的内容，快来看看把");
    message.put("targetId", targetId);
    ShareMessage shareMessage = new ShareMessage();
    shareMessage.setFromId(fromId);
    shareMessage.setToId(toId);
    shareMessage.setContent(message.toJSONString());
    shareMessage.setIsRead(2);
    shareMessage.setCreatedBy(fromId);
    shareMessage.setCreatedTime(new Date());
    // 发送WebSocket实时通知
    chickenSocket.sendMessage(toId, message.toJSONString());
}
```

### 消息通知模块

消息通知模块（ShareMessageController）承担了 jc-club-circle 中用户间社交通知的存储和分发功能，是连接用户社交行为的桥梁。该模块通过 ShareMessage 实体记录所有类型的系统通知消息，包括消息ID、发送人ID、接收人ID、消息内容、已读状态、创建时间、删除标志等字段。消息内容采用 JSON 格式存储，支持灵活的消息类型扩展，当前实现了评论通知（COMMENT）和回复通知两种类型，每种消息类型包含消息描述和目标内容ID等关键信息，便于前端进行消息展示和跳转处理。

消息查询功能（getMessages）实现了按接收用户分页查询未读消息的能力，查询结果会自动将消息标记为已读状态（isRead=1），确保用户不会重复收到通知提醒。模块设计了已读状态枚举，支持未读（isRead=2）和已读（isRead=1）两种状态，便于前端区分展示。在消息推送方面，模块集成了 WebSocket 实时通信能力，当产生新的社交通知时，不仅将消息持久化存储到数据库，还会通过 ChickenSocket 向目标用户推送实时 WebSocket 消息，让用户能够即时获知社交动态，大幅提升了社交互动的及时性和用户参与度。

### 敏感词过滤模块

敏感词过滤模块是 jc-club-circle 的内容安全保障组件，负责对用户发布的文本内容进行敏感词检测和过滤处理，维护社区内容的健康度。该模块包含 SensitiveWordsController 和 WordFilter 两个核心组件，前者提供敏感词的管理接口，后者实现具体的过滤算法。敏感词管理接口支持敏感词的添加和删除操作，管理员可以通过 /save 接口新增敏感词并设置类型，通过 /remove 接口删除不再需要的敏感词，所有敏感词数据持久化存储在 sensitive_words 数据库表中。

WordFilter 采用了基于前缀树（DFA）的敏感词匹配算法，实现了高效的内容检测能力。过滤器初始化时从数据库加载所有敏感词构建前缀树索引，检测时只需遍历一次文本即可完成敏感词识别，支持敏感词检测、内容替换、白名单三种核心功能。include 方法用于检测文本是否包含敏感词，replace 方法用于将敏感词替换为指定字符（如星号），同时还支持白名单机制，某些词汇可以标记为白名单以避免误判。在实际应用中，动态发布和评论发布接口都会调用 wordFilter.check() 方法对文本内容进行过滤，只有通过检测的内容才能成功发布。

```java
// 敏感词过滤器核心实现（DFA算法）
public class WordFilter {
    // 敏感词前缀树
    private final Map wordMap;

    public WordFilter(WordContext context) {
        this.wordMap = context.getWordMap();
    }

    // 检测是否包含敏感词
    public boolean include(final String text, final int skip) {
        boolean include = false;
        char[] charset = text.toCharArray();
        for (int i = 0; i < charset.length; i++) {
            FlagIndex fi = getFlagIndex(charset, i, skip);
            if (fi.isFlag()) {
                if (fi.isWhiteWord()) {
                    i += fi.getIndex().size() - 1;
                } else {
                    include = true;
                    break;
                }
            }
        }
        return include;
    }

    // 替换敏感词为指定字符
    public String replace(final String text, final int skip, final char symbol) {
        char[] charset = text.toCharArray();
        for (int i = 0; i < charset.length; i++) {
            FlagIndex fi = getFlagIndex(charset, i, skip);
            if (fi.isFlag()) {
                if (!fi.isWhiteWord()) {
                    for (int j : fi.getIndex()) {
                        charset[j] = symbol;
                    }
                } else {
                    i += fi.getIndex().size() - 1;
                }
            }
        }
        return new String(charset);
    }
}
```

### WebSocket 实时通信模块

WebSocket 实时通信模块通过 ChickenSocket 组件实现，为 jc-club-circle 提供了客户端与服务器之间的双向实时通信能力。该模块基于 Java WebSocket API 实现，使用 @ServerEndpoint 注解定义 WebSocket 端点（/chicken/socket），并通过自定义的 WebSocketServerConfig 配置获取用户身份信息。模块维护了全局的在线用户映射（ConcurrentHashMap），以用户ERP标识为键存储对应的 Socket 连接，支持用户的登录上线、消息推送、下线断开等核心操作。

连接管理方面，模块实现了完整的生命周期回调方法。onOpen 方法在连接建立时被调用，从 EndpointConfig 中获取用户身份信息（ERP），检查是否已有同用户的其他连接，如有则关闭旧连接并用新连接替代，然后将新连接加入在线映射并递增在线计数。onClose 方法在连接关闭时被调用，负责将用户从在线映射中移除并递减在线计数。onMessage 方法接收客户端消息，特别实现了心跳响应机制，收到 "ping" 消息时回复 "pong" 以维持连接活跃状态。sendMessage 方法提供了向指定用户推送消息的能力，消息通知模块正是利用此方法实现社交动态的实时推送。

```java
// WebSocket连接管理与消息推送
@ServerEndpoint(value = "/chicken/socket", configurator = WebSocketServerConfig.class)
@Component
public class ChickenSocket {
    // 在线用户映射
    private static final Map<String, ChickenSocket> clients = new ConcurrentHashMap<>();
    private static final AtomicInteger onlineCount = new AtomicInteger(0);
    private Session session;
    private String erp;

    @OnOpen
    public void onOpen(Session session, EndpointConfig conf) {
        Map<String, Object> userProperties = conf.getUserProperties();
        String erp = (String) userProperties.get("erp");
        this.erp = erp;
        this.session = session;
        // 同用户只保留一个连接
        if (clients.containsKey(this.erp)) {
            clients.get(this.erp).session.close();
            clients.remove(this.erp);
            onlineCount.decrementAndGet();
        }
        clients.put(this.erp, this);
        onlineCount.incrementAndGet();
        sendMessage("连接成功", this.session);
    }

    @OnClose
    public void onClose() {
        if (clients.containsKey(erp)) {
            clients.get(erp).session.close();
            clients.remove(erp);
            onlineCount.decrementAndGet();
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // 心跳响应
        if (message.equals("ping")) {
            this.sendMessage("pong", session);
        }
    }

    // 向指定用户推送消息
    public static void sendMessage(String erp, String message) {
        if (clients.containsKey(erp)) {
            try {
                clients.get(erp).session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                log.error("WebSocket发送消息失败: {}", e.getMessage());
            }
        }
    }
}
```

## 关键技术栈

### 后端框架与技术

jc-club-circle 的服务端开发基于成熟的 Spring Boot 2.4.2 框架，结合 Spring Cloud Alibaba 2021.1 版本构建微服务架构。Spring Boot 提供了自动配置、内嵌服务器、健康检查等便利特性，大幅简化了项目配置和部署工作。Spring Cloud Alibaba 生态中的 Nacos 组件负责服务注册与发现、配置中心等功能，使得 jc-club-circle 能够灵活融入 JC-Club 整体微服务体系。在数据访问层，项目采用 MyBatis-Plus 3.4.0 作为 ORM 框架，相比原生 MyBatis 提供了更便捷的 CRUD 操作、自动填充、逻辑删除、分页插件等增强功能，显著提升了开发效率。

数据存储方面，项目使用 MySQL 8.0.22 作为关系型数据库，通过 Druid 1.1.22 连接池管理数据库连接，Druid 提供了强大的监控和扩展能力。Redis 2.4.2（spring-boot-starter-data-redis）用于分布式缓存和会话管理，结合本地缓存 Caffeine，构建了多级缓存体系，有效平衡了数据一致性和访问性能。JSON 处理采用 FastJSON 1.2.24 和 Gson 2.8.6 两个库，前者用于业务场景的 JSON 序列化，后者用于 WebSocket 等场景。日志系统采用 Log4j2，结合 Log4j2-Spring 配置实现了灵活的日志管理。代码生成和简化方面使用了 Lombok 1.18.16 和 MapStruct 1.4.2，前者简化了实体类的 getter/setter/构造函数编写，后者实现了对象属性的高效映射。

### 数据库技术

jc-club-circle 的数据持久化层采用了 MySQL 8.0.22 作为主数据库，配合 Druid 连接池实现高效稳定的数据库访问。数据库表设计遵循规范化原则，核心业务表包括 share_circle（圈子信息表）、share_moment（动态信息表）、share_comment_reply（评论回复表）、share_message（消息通知表）、sensitive_words（敏感词表）等。每张表都包含 id（主键）、created_by（创建人）、created_time（创建时间）、updated_time（更新时间）、is_deleted（删除标志）等通用字段，通过 IsDeletedFlagEnum 枚举定义逻辑删除的具体取值（0-未删除，1-已删除）。

MyBatis-Plus 框架在数据访问层发挥核心作用，通过 @TableName 注解将实体类与数据库表映射，@TableId 注解定义主键策略（采用 AUTO 自增类型）。LambdaQueryWrapper 提供了类型安全的查询构建方式，避免了硬编码字段名带来的维护风险。分页功能由 MyBatis-Plus 的 Page 类实现，配合自定义的 PageInfo 和 PageResult 封装形成了统一的分页查询规范。批量操作能力通过 MyBatis-Plus 的 list、saveBatch 等方法实现，在用户信息批量查询等场景中发挥重要作用。此外，项目还通过自定义 SQL 拦截器（SqlStatementInterceptor）实现了 SQL 语句的自动日志记录，便于开发调试和性能分析。

### 缓存技术

jc-club-circle 采用了多级缓存策略来平衡数据一致性和访问性能。第一级缓存为本地缓存，使用 Caffeine 2.9.3 库实现，用于缓存圈子列表等变化频率较低的数据。Caffeine 是高性能的本地缓存库，提供了基于容量、时间和权重的多种过期策略。项目中将圈子列表缓存的有效期设置为 30 秒（expireAfterWrite），初始容量为 1，最大容量为 1，这种设计适用于数据量小但访问频繁的查询场景，既保证了缓存命中率，又避免了数据长时间不更新的问题。

第二级缓存为分布式缓存，使用 Redis 实现。Redis 作为 JC-Club 平台的统一缓存服务，承担了会话管理（通过 Sa-Token 集成）、热点数据缓存等功能。在 jc-club-circle 中，Redis 主要用于配合 Sa-Token 实现用户认证状态的跨服务共享，以及缓存从用户服务获取的用户信息数据。Redis 配置类（RedisConfig）封装了 RedisTemplate 的初始化逻辑，提供了序列化器和连接池的定制化配置。Caffeine 与 Redis 的组合形成了典型的 Cache-Aside 模式，查询时优先检查本地缓存，未命中则查询数据库或远程服务，实现了性能和一致性的平衡。

### 实时通信技术

实时通信能力是 jc-club-circle 社交功能的重要组成部分，通过 WebSocket 协议实现客户端与服务器的持久连接和双向通信。项目基于 Java 官方 WebSocket API（javax.websocket）实现，使用 Spring 管理的 @Component Bean（ChickenSocket）作为 WebSocket 端点。服务端通过 @ServerEndpoint 注解声明 WebSocket 端点路径（/chicken/socket），通过 @OnOpen、@OnClose、@OnMessage、@OnError 四个注解分别处理连接建立、连接关闭、消息接收、异常处理四个核心事件。

WebSocket 的用户身份识别通过自定义的 WebSocketServerConfig 配置器实现，在握手阶段从请求属性中提取用户身份信息并绑定到会话上下文。这种设计使得后续的推送操作可以精确到具体用户。连接管理使用 ConcurrentHashMap 存储所有在线用户的 WebSocket 连接，支持高并发场景下的线程安全访问。消息推送采用同步发送方式（session.getBasicRemote().sendText()），并通过异常捕获确保单次发送失败不影响其他用户的消息送达。客户端通过发送 "ping" 心跳消息维持连接活跃，服务端回复 "pong" 响应，这是 WebSocket 保持连接常用的心跳机制实现。

### 敏感词过滤技术

敏感词过滤是 jc-club-circle 内容安全体系的核心组件，采用了基于 DFA（Deterministic Finite Automaton，确定有限状态自动机）算法的敏感词匹配方案。DFA 算法通过构建敏感词的前缀树（Trie Tree）索引，将敏感词库编译为状态转移图，检测时只需遍历一次文本即可完成所有敏感词的识别，时间复杂度为 O(n)，其中 n 为文本长度。相比简单的字符串替换或正则匹配，DFA 算法在大批量敏感词场景下具有显著的性能优势。

WordFilter 类的核心数据结构 wordMap 是一个嵌套的 HashMap，外层 key 为字符，内层继续嵌套表示敏感词的下一个字符层级，叶子节点标记敏感词结束。这种结构支持任意长度敏感词的存储和检索。FlagIndex 类封装了匹配结果信息，包括是否匹配到敏感词（isFlag）、匹配到的敏感词索引列表（index）、是否为白名单词汇（isWhiteWord）。白名单机制允许某些词汇被标记为例外，避免因敏感词包含关系导致的误判。WordContext 类负责敏感词数据的加载和前缀树的构建，通常在应用启动时从数据库读取所有敏感词并初始化过滤器实例。

## 总结

jc-club-circle 作为 JC-Club 社交平台的核心模块，构建了一套完整的社区社交功能体系。从圈子管理到内容分享，从评论互动到消息通知，从内容安全到实时通信，每个功能模块都经过精心设计，形成了有机统一的整体。技术选型上，项目综合运用了 Spring Boot、MyBatis-Plus、Redis、WebSocket 等主流技术栈，在保证系统稳定性和可维护性的同时，提供了良好的用户体验和社交互动效果。多级缓存策略、DFA 敏感词过滤算法、WebSocket 实时通信等技术的应用，体现了项目对性能和用户体验的持续追求。