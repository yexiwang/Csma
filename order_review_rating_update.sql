-- 订单评价与志愿者评分聚合增量

CREATE TABLE IF NOT EXISTS `order_review` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `volunteer_user_id` bigint NOT NULL COMMENT '志愿者用户ID',
  `family_user_id` bigint NOT NULL COMMENT '家属用户ID',
  `score` int NOT NULL COMMENT '评分 1~5',
  `content` varchar(255) DEFAULT NULL COMMENT '评价内容',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`),
  KEY `idx_volunteer_user_id` (`volunteer_user_id`),
  KEY `idx_family_user_id` (`family_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单评价表';

ALTER TABLE `volunteer_stats`
  MODIFY COLUMN `rating` decimal(2,1) DEFAULT NULL COMMENT '综合评分';

UPDATE `volunteer_stats`
SET `rating` = NULL;
