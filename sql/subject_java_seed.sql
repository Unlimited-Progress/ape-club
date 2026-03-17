SET @login_id = 'oGXh93Hq0NTupmI_PnWRB60oaIKg';

INSERT IGNORE INTO `subject_category` (`id`, `category_name`, `category_type`, `image_url`, `parent_id`, `created_by`, `created_time`, `update_by`, `update_time`, `is_deleted`) VALUES
(5, 'Java', 2, 'https://image/category.icon', 1, @login_id, '2026-03-17 11:00:00', NULL, NULL, 0);

INSERT IGNORE INTO `subject_label` (`id`, `label_name`, `sort_num`, `category_id`, `created_by`, `created_time`, `update_by`, `update_time`, `is_deleted`) VALUES
(51, 'JVM', 7, 1, @login_id, '2026-03-17 11:01:00', NULL, NULL, 0),
(52, '集合框架', 8, 1, @login_id, '2026-03-17 11:01:00', NULL, NULL, 0),
(53, '并发编程', 9, 1, @login_id, '2026-03-17 11:01:00', NULL, NULL, 0),
(54, 'Spring', 10, 1, @login_id, '2026-03-17 11:01:00', NULL, NULL, 0);

INSERT IGNORE INTO `subject_category_label` (`category_id`, `label_id`, `sort_num`, `created_by`, `created_time`, `update_by`, `update_time`, `is_deleted`) VALUES
(5, 51, 1, @login_id, '2026-03-17 11:01:10', NULL, NULL, 0),
(5, 52, 2, @login_id, '2026-03-17 11:01:10', NULL, NULL, 0),
(5, 53, 3, @login_id, '2026-03-17 11:01:10', NULL, NULL, 0),
(5, 54, 4, @login_id, '2026-03-17 11:01:10', NULL, NULL, 0);

INSERT IGNORE INTO `subject_info` (`id`, `subject_name`, `subject_difficult`, `settle_name`, `subject_type`, `subject_score`, `subject_parse`, `created_by`, `created_time`, `update_by`, `update_time`, `is_deleted`) VALUES
(135, 'Java 中负责把类字节码加载到内存并转为运行时结构的机制是？', 1, NULL, 1, 1, '类加载机制负责加载、验证、准备、解析和初始化等阶段。', @login_id, '2026-03-17 11:05:00', NULL, NULL, 0),
(136, 'JDK 8 中 ConcurrentHashMap 主要依赖什么来提升并发更新性能？', 2, NULL, 1, 1, 'JDK 8 之后不再采用分段锁，核心是 CAS + synchronized。', @login_id, '2026-03-17 11:05:00', NULL, NULL, 0),
(137, 'ArrayList 在未指定扩容策略时，扩容后的容量通常接近原来的多少？', 1, NULL, 1, 1, 'ArrayList 新容量通常约为旧容量的 1.5 倍。', @login_id, '2026-03-17 11:05:00', NULL, NULL, 0),
(138, 'Java 内存模型中，下面哪些属于常见的 happens-before 规则？', 3, NULL, 2, 2, '程序次序、锁规则、volatile 规则和线程启动/终止规则都属于 happens-before。', @login_id, '2026-03-17 11:06:00', NULL, NULL, 0),
(139, 'Spring 声明式事务失效的常见场景有哪些？', 3, NULL, 2, 2, '自调用、非 public 方法、异常类型不匹配和代理失效都很常见。', @login_id, '2026-03-17 11:06:00', NULL, NULL, 0),
(140, 'String 在 Java 中是不可变对象。', 1, NULL, 3, 1, 'String 的内容一旦创建不可修改，这也是其可缓存 hash 和适合作为键的重要原因。', @login_id, '2026-03-17 11:07:00', NULL, NULL, 0),
(141, 'HashMap 是线程安全的集合实现，适合多线程环境直接共享写入。', 1, NULL, 3, 1, 'HashMap 不是线程安全集合，多线程写入需额外同步或改用并发容器。', @login_id, '2026-03-17 11:07:00', NULL, NULL, 0),
(142, '解释一下 JVM 双亲委派模型，以及它解决了什么问题。', 3, NULL, 4, 3, '双亲委派保证核心类优先由上层类加载器加载，避免核心 API 被随意篡改。', @login_id, '2026-03-17 11:08:00', NULL, NULL, 0),
(143, 'synchronized 和 ReentrantLock 分别适合什么场景？', 3, NULL, 4, 3, 'synchronized 简洁稳定，ReentrantLock 在可中断、超时、公平锁等场景更灵活。', @login_id, '2026-03-17 11:08:00', NULL, NULL, 0);

INSERT IGNORE INTO `subject_mapping` (`id`, `subject_id`, `category_id`, `label_id`, `created_by`, `created_time`, `update_by`, `update_time`, `is_deleted`) VALUES
(252, 135, 5, 51, @login_id, '2026-03-17 11:10:00', NULL, NULL, 0),
(253, 136, 5, 53, @login_id, '2026-03-17 11:10:00', NULL, NULL, 0),
(254, 137, 5, 52, @login_id, '2026-03-17 11:10:00', NULL, NULL, 0),
(255, 138, 5, 53, @login_id, '2026-03-17 11:10:00', NULL, NULL, 0),
(256, 139, 5, 54, @login_id, '2026-03-17 11:10:00', NULL, NULL, 0),
(257, 140, 5, 51, @login_id, '2026-03-17 11:10:00', NULL, NULL, 0),
(258, 141, 5, 52, @login_id, '2026-03-17 11:10:00', NULL, NULL, 0),
(259, 142, 5, 51, @login_id, '2026-03-17 11:10:00', NULL, NULL, 0),
(260, 143, 5, 53, @login_id, '2026-03-17 11:10:00', NULL, NULL, 0);

INSERT IGNORE INTO `subject_radio` (`id`, `subject_id`, `option_type`, `option_content`, `is_correct`, `created_by`, `created_time`, `update_by`, `update_time`, `is_deleted`) VALUES
(1041, 135, 1, '垃圾回收机制', 0, @login_id, '2026-03-17 11:12:00', NULL, NULL, 0),
(1042, 135, 2, '类加载机制', 1, @login_id, '2026-03-17 11:12:00', NULL, NULL, 0),
(1043, 135, 3, '字节码增强器', 0, @login_id, '2026-03-17 11:12:00', NULL, NULL, 0),
(1044, 135, 4, '即时编译缓存', 0, @login_id, '2026-03-17 11:12:00', NULL, NULL, 0),
(1045, 136, 1, 'Segment 分段锁', 0, @login_id, '2026-03-17 11:12:00', NULL, NULL, 0),
(1046, 136, 2, 'CAS + synchronized', 1, @login_id, '2026-03-17 11:12:00', NULL, NULL, 0),
(1047, 136, 3, '只依赖 volatile', 0, @login_id, '2026-03-17 11:12:00', NULL, NULL, 0),
(1048, 136, 4, 'ThreadLocal', 0, @login_id, '2026-03-17 11:12:00', NULL, NULL, 0),
(1049, 137, 1, '扩到原来的 2 倍', 0, @login_id, '2026-03-17 11:12:00', NULL, NULL, 0),
(1050, 137, 2, '扩到原来的 1.5 倍左右', 1, @login_id, '2026-03-17 11:12:00', NULL, NULL, 0),
(1051, 137, 3, '固定增加 10', 0, @login_id, '2026-03-17 11:12:00', NULL, NULL, 0),
(1052, 137, 4, '保持不变', 0, @login_id, '2026-03-17 11:12:00', NULL, NULL, 0);

INSERT IGNORE INTO `subject_multiple` (`id`, `subject_id`, `option_type`, `option_content`, `is_correct`, `created_by`, `created_time`, `update_by`, `update_time`, `is_deleted`) VALUES
(1125, 138, 1, '程序次序规则', 1, @login_id, '2026-03-17 11:14:00', NULL, NULL, 0),
(1126, 138, 2, '锁定规则', 1, @login_id, '2026-03-17 11:14:00', NULL, NULL, 0),
(1127, 138, 3, 'volatile 变量规则', 1, @login_id, '2026-03-17 11:14:00', NULL, NULL, 0),
(1128, 138, 4, 'CSS 层叠优先级规则', 0, @login_id, '2026-03-17 11:14:00', NULL, NULL, 0),
(1129, 139, 1, '同类内部方法自调用', 1, @login_id, '2026-03-17 11:14:00', NULL, NULL, 0),
(1130, 139, 2, '@Transactional 标在 private 方法上', 1, @login_id, '2026-03-17 11:14:00', NULL, NULL, 0),
(1131, 139, 3, '抛出异常但未命中回滚规则', 1, @login_id, '2026-03-17 11:14:00', NULL, NULL, 0),
(1132, 139, 4, '数据库连接池初始大小为 10', 0, @login_id, '2026-03-17 11:14:00', NULL, NULL, 0);

INSERT IGNORE INTO `subject_judge` (`id`, `subject_id`, `is_correct`, `created_by`, `created_time`, `update_by`, `update_time`, `is_deleted`) VALUES
(1205, 140, 1, @login_id, '2026-03-17 11:16:00', NULL, NULL, 0),
(1206, 141, 0, @login_id, '2026-03-17 11:16:00', NULL, NULL, 0);

INSERT IGNORE INTO `subject_brief` (`id`, `subject_id`, `subject_answer`, `created_by`, `created_time`, `update_by`, `update_time`, `is_deleted`) VALUES
(74, 142, '<p>双亲委派是指一个类加载器收到类加载请求时，先把请求委派给父加载器，父加载器无法完成时子加载器才会自己尝试加载。</p><ul><li>这样可以保证 <code>java.lang.*</code> 这类核心类优先由启动类加载器加载</li><li>避免业务代码自定义同名核心类造成 API 污染或安全问题</li><li>也让类加载职责更稳定，减少重复加载</li></ul>', @login_id, '2026-03-17 11:18:00', NULL, NULL, 0),
(75, 143, '<p><code>synchronized</code> 和 <code>ReentrantLock</code> 都能做互斥控制，但适用点不完全一样。</p><ul><li><code>synchronized</code> 语法简单，JVM 原生支持，适合大多数基础同步场景</li><li><code>ReentrantLock</code> 支持可中断获取锁、超时尝试、公平锁和多条件队列</li><li>在需要更细粒度锁控制时，通常优先考虑 <code>ReentrantLock</code></li></ul>', @login_id, '2026-03-17 11:18:00', NULL, NULL, 0);
