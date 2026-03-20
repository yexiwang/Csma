USE `sky_take_out`;

-- 管理员端志愿者管理增量脚本
-- 请在执行唯一索引前确认 user.username 不存在重复值

ALTER TABLE `user`
  ADD COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间' AFTER `create_time`;

UPDATE `user`
SET `update_time` = `create_time`
WHERE `update_time` IS NULL;

ALTER TABLE `user`
  ADD UNIQUE KEY `uk_user_username` (`username`);

INSERT INTO `volunteer_stats` (`user_id`, `total_orders`, `total_hours`, `rating`, `level`, `create_time`, `update_time`)
SELECT
  u.`id`,
  0,
  0.0,
  5.0,
  1,
  NOW(),
  NOW()
FROM `user` u
LEFT JOIN `volunteer_stats` vs ON vs.`user_id` = u.`id`
WHERE u.`role` = 'VOLUNTEER'
  AND vs.`user_id` IS NULL;
