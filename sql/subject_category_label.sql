CREATE TABLE IF NOT EXISTS `subject_category_label` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category_id` bigint NOT NULL COMMENT '二级分类id',
  `label_id` bigint NOT NULL COMMENT '标签id',
  `sort_num` int DEFAULT 1 COMMENT '当前分类下排序',
  `created_by` varchar(64) DEFAULT NULL,
  `created_time` datetime DEFAULT NULL,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_subject_category_label` (`category_id`,`label_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类标签关联表';

INSERT INTO `subject_category_label` (`category_id`, `label_id`, `sort_num`, `created_by`, `created_time`, `update_by`, `update_time`, `is_deleted`)
SELECT sm.category_id,
       sm.label_id,
       COALESCE(MIN(sl.sort_num), MIN(sm.id)) AS sort_num,
       MIN(sm.created_by) AS created_by,
       MIN(sm.created_time) AS created_time,
       NULL AS update_by,
       NULL AS update_time,
       0 AS is_deleted
FROM subject_mapping sm
         LEFT JOIN subject_label sl on sl.id = sm.label_id
WHERE sm.is_deleted = 0
GROUP BY sm.category_id, sm.label_id
ON DUPLICATE KEY UPDATE
    sort_num = VALUES(sort_num),
    is_deleted = VALUES(is_deleted);
