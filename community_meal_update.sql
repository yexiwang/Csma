-- 社区老年助餐服务系统 - 数据库变更脚本
-- 基于 Sky Take Out (苍穹外卖) 数据库结构进行改造

USE `sky_take_out`;

-- -----------------------------------------------------
-- 1. 新增表：dining_point (助餐点)
-- -----------------------------------------------------
DROP TABLE IF EXISTS `dining_point`;
CREATE TABLE `dining_point` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(50) NOT NULL COMMENT '助餐点名称',
  `address` varchar(255) DEFAULT NULL COMMENT '详细地址',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `operating_hours` varchar(50) DEFAULT NULL COMMENT '营业时间',
  `status` int DEFAULT '1' COMMENT '状态 0:休息 1:营业',
  `image` varchar(255) DEFAULT NULL COMMENT '助餐点图片',
  `grid_coverage` json DEFAULT NULL COMMENT '服务网格范围(JSON数组)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint DEFAULT NULL COMMENT '创建人',
  `update_user` bigint DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='助餐点';

-- -----------------------------------------------------
-- 2. 新增表：elderly (老人档案)
-- -----------------------------------------------------
DROP TABLE IF EXISTS `elderly`;
CREATE TABLE `elderly` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '关联用户ID(家属或本人)',
  `name` varchar(32) NOT NULL COMMENT '老人姓名',
  `gender` char(1) DEFAULT NULL COMMENT '性别',
  `age` int DEFAULT NULL COMMENT '年龄',
  `phone` varchar(11) DEFAULT NULL COMMENT '联系电话',
  `address` varchar(255) DEFAULT NULL COMMENT '详细居住地址',
  `grid_code` varchar(50) DEFAULT NULL COMMENT '所属网格/片区',
  `health_info` text DEFAULT NULL COMMENT '健康状况(慢性病/过敏源)',
  `special_needs` varchar(255) DEFAULT NULL COMMENT '特殊需求',
  `id_card` varchar(18) DEFAULT NULL COMMENT '身份证号',
  `image` varchar(255) DEFAULT NULL COMMENT '照片',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='老人档案';

-- -----------------------------------------------------
-- 3. 新增表：volunteer_stats (志愿者服务记录)
-- -----------------------------------------------------
DROP TABLE IF EXISTS `volunteer_stats`;
CREATE TABLE `volunteer_stats` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '志愿者用户ID',
  `total_orders` int DEFAULT '0' COMMENT '累计服务单量',
  `total_hours` decimal(10,1) DEFAULT '0.0' COMMENT '累计服务时长(小时)',
  `rating` decimal(2,1) DEFAULT '5.0' COMMENT '综合评分',
  `level` int DEFAULT '1' COMMENT '等级',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='志愿者服务统计';

-- -----------------------------------------------------
-- 4. 修改表：employee (员工表)
-- -----------------------------------------------------
-- 增加 dining_point_id 字段
ALTER TABLE `employee` ADD COLUMN `dining_point_id` bigint DEFAULT NULL COMMENT '所属助餐点ID' AFTER `status`;

-- -----------------------------------------------------
-- 5. 修改表：user (C端用户表)
-- -----------------------------------------------------
-- 增加账号密码登录支持及角色标识
ALTER TABLE `user` ADD COLUMN `username` varchar(32) DEFAULT NULL COMMENT '登录账号' AFTER `openid`;
ALTER TABLE `user` ADD COLUMN `password` varchar(64) DEFAULT NULL COMMENT '登录密码' AFTER `username`;
ALTER TABLE `user` ADD COLUMN `role` varchar(20) DEFAULT 'FAMILY' COMMENT '角色: FAMILY(家属/老人), VOLUNTEER(志愿者)' AFTER `password`;
ALTER TABLE `user` ADD COLUMN `status` int DEFAULT '1' COMMENT '状态 0:禁用 1:启用' AFTER `role`;

-- 更新 existing users to have a default role if needed
-- UPDATE `user` SET `role` = 'FAMILY' WHERE `role` IS NULL;

-- -----------------------------------------------------
-- 6. 修改表：orders (订单表)
-- -----------------------------------------------------
-- 增加老人、志愿者、助餐点关联及补贴信息
ALTER TABLE `orders` ADD COLUMN `elder_id` bigint DEFAULT NULL COMMENT '用餐老人ID' AFTER `user_id`;
ALTER TABLE `orders` ADD COLUMN `volunteer_id` bigint DEFAULT NULL COMMENT '配送志愿者ID' AFTER `elder_id`;
ALTER TABLE `orders` ADD COLUMN `dining_point_id` bigint DEFAULT NULL COMMENT '出餐助餐点ID' AFTER `volunteer_id`;
ALTER TABLE `orders` ADD COLUMN `subsidy_amount` decimal(10,2) DEFAULT '0.00' COMMENT '补贴金额' AFTER `amount`;
ALTER TABLE `orders` ADD COLUMN `personal_pay` decimal(10,2) DEFAULT '0.00' COMMENT '自付金额' AFTER `subsidy_amount`;
ALTER TABLE `orders` ADD COLUMN `expected_time` datetime DEFAULT NULL COMMENT '期望送达时间' AFTER `order_time`;

-- -----------------------------------------------------
-- 7. 修改表：dish (菜品表)
-- -----------------------------------------------------
-- 增加助餐点关联及营养标签
ALTER TABLE `dish` ADD COLUMN `dining_point_id` bigint DEFAULT NULL COMMENT '所属助餐点ID' AFTER `category_id`;
ALTER TABLE `dish` ADD COLUMN `nutrition_tags` varchar(255) DEFAULT NULL COMMENT '营养标签(低糖,低盐等)' AFTER `description`;
ALTER TABLE `dish` ADD COLUMN `suitability` varchar(255) DEFAULT NULL COMMENT '适宜人群' AFTER `nutrition_tags`;

