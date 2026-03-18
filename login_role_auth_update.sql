USE `sky_take_out`;

ALTER TABLE `employee`
  ADD COLUMN `role` varchar(20) NOT NULL DEFAULT 'ADMIN' COMMENT '角色: ADMIN, OPERATOR' AFTER `status`;

-- ALTER TABLE `employee`
--   ADD COLUMN `dining_point_id` bigint DEFAULT NULL COMMENT '所属助餐点ID' AFTER `role`;

UPDATE `employee`
SET `role` = 'ADMIN', `dining_point_id` = NULL
WHERE `username` = 'admin';

UPDATE `employee`
SET `role` = 'ADMIN'
WHERE `role` IS NULL OR `role` = '';

UPDATE `employee`
SET `password` = MD5('123456')
WHERE `password` = '123456';

UPDATE `user`
SET `password` = MD5('123456')
WHERE `password` = '123456';

UPDATE `user`
SET `role` = 'FAMILY'
WHERE `role` IS NULL OR `role` = '';
