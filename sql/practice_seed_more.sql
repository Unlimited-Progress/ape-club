SET @login_id = 'REPLACE_WITH_YOUR_LOGIN_ID';

INSERT INTO `practice_set` (`id`, `set_name`, `set_type`, `set_heat`, `set_desc`, `primary_category_id`, `is_deleted`, `created_by`, `created_time`, `update_by`, `update_time`) VALUES
(2001, '缓存与一致性热身卷', 2, 38, '覆盖 Redis、缓存稳定性与一致性治理的基础题型。', 1, 0, @login_id, '2026-03-17 11:00:00', NULL, NULL),
(2002, '消息队列专项卷', 2, 27, '围绕 RocketMQ、重试、死信与延迟消息的专项训练。', 1, 0, @login_id, '2026-03-17 11:00:00', NULL, NULL),
(2003, '数据库稳定性套卷', 2, 31, '聚焦 MySQL 索引、binlog 与事务隔离。', 1, 0, @login_id, '2026-03-17 11:00:00', NULL, NULL);

INSERT INTO `practice_set_detail` (`id`, `set_id`, `subject_id`, `subject_type`, `is_deleted`, `created_by`, `created_time`, `update_by`, `update_time`) VALUES
(2101, 2001, 111, 1, 0, @login_id, '2026-03-17 11:01:00', NULL, NULL),
(2102, 2001, 112, 1, 0, @login_id, '2026-03-17 11:01:00', NULL, NULL),
(2103, 2001, 113, 1, 0, @login_id, '2026-03-17 11:01:00', NULL, NULL),
(2104, 2001, 114, 1, 0, @login_id, '2026-03-17 11:01:00', NULL, NULL),
(2105, 2001, 121, 2, 0, @login_id, '2026-03-17 11:01:00', NULL, NULL),
(2106, 2001, 122, 2, 0, @login_id, '2026-03-17 11:01:00', NULL, NULL),
(2107, 2001, 127, 3, 0, @login_id, '2026-03-17 11:01:00', NULL, NULL),
(2108, 2001, 128, 3, 0, @login_id, '2026-03-17 11:01:00', NULL, NULL),
(2109, 2002, 115, 1, 0, @login_id, '2026-03-17 11:01:00', NULL, NULL),
(2110, 2002, 116, 1, 0, @login_id, '2026-03-17 11:01:00', NULL, NULL),
(2111, 2002, 117, 1, 0, @login_id, '2026-03-17 11:01:00', NULL, NULL),
(2112, 2002, 123, 2, 0, @login_id, '2026-03-17 11:01:00', NULL, NULL),
(2113, 2002, 124, 2, 0, @login_id, '2026-03-17 11:01:00', NULL, NULL),
(2114, 2002, 129, 3, 0, @login_id, '2026-03-17 11:01:00', NULL, NULL),
(2115, 2003, 118, 1, 0, @login_id, '2026-03-17 11:01:00', NULL, NULL),
(2116, 2003, 119, 1, 0, @login_id, '2026-03-17 11:01:00', NULL, NULL),
(2117, 2003, 120, 1, 0, @login_id, '2026-03-17 11:01:00', NULL, NULL),
(2118, 2003, 125, 2, 0, @login_id, '2026-03-17 11:01:00', NULL, NULL),
(2119, 2003, 126, 2, 0, @login_id, '2026-03-17 11:01:00', NULL, NULL),
(2120, 2003, 130, 3, 0, @login_id, '2026-03-17 11:01:00', NULL, NULL);

INSERT INTO `practice_info` (`id`, `set_id`, `complete_status`, `time_use`, `submit_time`, `correct_rate`, `is_deleted`, `created_by`, `created_time`, `update_by`, `update_time`) VALUES
(3001, 2001, 1, '00:12:36', '2026-03-17 11:20:00', 75.00, 0, @login_id, '2026-03-17 11:08:00', NULL, NULL),
(3002, 2002, 0, '00:05:18', '2026-03-17 11:40:00', 0.00, 0, @login_id, '2026-03-17 11:35:00', NULL, NULL),
(3003, 2003, 1, '00:14:10', '2026-03-17 12:05:00', 66.67, 0, @login_id, '2026-03-17 11:50:00', NULL, NULL),
(3004, 2001, 1, '00:10:02', '2026-03-17 12:35:00', 87.50, 0, @login_id, '2026-03-17 12:20:00', NULL, NULL);

INSERT INTO `practice_detail` (`id`, `practice_id`, `subject_id`, `subject_type`, `answer_status`, `answer_content`, `is_deleted`, `created_by`, `created_time`, `update_by`, `update_time`) VALUES
(4001, 3001, 111, 1, 1, '2', 0, @login_id, '2026-03-17 11:09:00', NULL, NULL),
(4002, 3001, 112, 1, 1, '2', 0, @login_id, '2026-03-17 11:10:00', NULL, NULL),
(4003, 3001, 113, 1, 0, '2', 0, @login_id, '2026-03-17 11:11:00', NULL, NULL),
(4004, 3001, 114, 1, 1, '2', 0, @login_id, '2026-03-17 11:12:00', NULL, NULL),
(4005, 3001, 121, 2, 1, '1,2,3', 0, @login_id, '2026-03-17 11:13:00', NULL, NULL),
(4006, 3001, 122, 2, 0, '1,2', 0, @login_id, '2026-03-17 11:14:00', NULL, NULL),
(4007, 3001, 127, 3, 1, '1', 0, @login_id, '2026-03-17 11:15:00', NULL, NULL),
(4008, 3001, 128, 3, 1, '0', 0, @login_id, '2026-03-17 11:16:00', NULL, NULL),
(4009, 3002, 115, 1, 1, '2', 0, @login_id, '2026-03-17 11:36:00', NULL, NULL),
(4010, 3002, 116, 1, 0, '1', 0, @login_id, '2026-03-17 11:37:00', NULL, NULL),
(4011, 3002, 123, 2, 1, '1,2,3', 0, @login_id, '2026-03-17 11:38:00', NULL, NULL),
(4012, 3003, 118, 1, 1, '2', 0, @login_id, '2026-03-17 11:52:00', NULL, NULL),
(4013, 3003, 119, 1, 1, '2', 0, @login_id, '2026-03-17 11:53:00', NULL, NULL),
(4014, 3003, 120, 1, 1, '3', 0, @login_id, '2026-03-17 11:54:00', NULL, NULL),
(4015, 3003, 125, 2, 0, '1,2', 0, @login_id, '2026-03-17 11:55:00', NULL, NULL),
(4016, 3003, 126, 2, 1, '1,3', 0, @login_id, '2026-03-17 11:56:00', NULL, NULL),
(4017, 3003, 130, 3, 0, '1', 0, @login_id, '2026-03-17 11:57:00', NULL, NULL),
(4018, 3004, 111, 1, 1, '2', 0, @login_id, '2026-03-17 12:21:00', NULL, NULL),
(4019, 3004, 112, 1, 1, '2', 0, @login_id, '2026-03-17 12:22:00', NULL, NULL),
(4020, 3004, 113, 1, 1, '3', 0, @login_id, '2026-03-17 12:23:00', NULL, NULL),
(4021, 3004, 114, 1, 1, '2', 0, @login_id, '2026-03-17 12:24:00', NULL, NULL),
(4022, 3004, 121, 2, 1, '1,2,3', 0, @login_id, '2026-03-17 12:25:00', NULL, NULL),
(4023, 3004, 122, 2, 1, '1,2,3', 0, @login_id, '2026-03-17 12:26:00', NULL, NULL),
(4024, 3004, 127, 3, 1, '1', 0, @login_id, '2026-03-17 12:27:00', NULL, NULL),
(4025, 3004, 128, 3, 0, '1', 0, @login_id, '2026-03-17 12:28:00', NULL, NULL);
