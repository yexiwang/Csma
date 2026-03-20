USE `sky_take_out`;

START TRANSACTION;

-- -----------------------------------------------------
-- 0. 清理同名演示数据，保证脚本可重复执行
-- -----------------------------------------------------
DELETE FROM `order_detail` WHERE `order_id` IN (98001, 98002);
DELETE FROM `orders` WHERE `id` IN (98001, 98002);
DELETE FROM `shopping_cart` WHERE `user_id` = 9101;
DELETE FROM `volunteer_stats` WHERE `id` = 9401 OR `user_id` = 9102;
DELETE FROM `elderly` WHERE `id` IN (9201, 9202);
DELETE FROM `address_book` WHERE `id` IN (9301);
DELETE FROM `dish` WHERE `id` IN (9501, 9502, 9503);
DELETE FROM `user` WHERE `id` IN (9101, 9102) OR `username` IN ('family_demo', 'volunteer_demo');
DELETE FROM `employee` WHERE `id` IN (9001, 9002) OR `username` IN ('admin_demo', 'operator_demo');
DELETE FROM `dining_point` WHERE `id` = 9001;

-- -----------------------------------------------------
-- 1. 助餐点
-- -----------------------------------------------------
INSERT INTO `dining_point` (
  `id`, `name`, `address`, `contact_phone`, `operating_hours`, `status`,
  `image`, `grid_coverage`, `create_time`, `update_time`, `create_user`, `update_user`
) VALUES (
  9001,
  '幸福食堂演示点',
  '北京市海淀区上地十街10号',
  '13800000001',
  '09:00-18:00',
  1,
  '',
  '["海淀区上地街道","演示网格A"]',
  '2026-03-18 09:00:00',
  '2026-03-18 09:00:00',
  1,
  1
);

-- -----------------------------------------------------
-- 2. 演示账号
--    员工端：/admin/employee/login
--    用户端：/user/user/login
--    统一密码：123456
-- -----------------------------------------------------
INSERT INTO `employee` (
  `id`, `name`, `username`, `password`, `phone`, `sex`, `id_number`,
  `status`, `role`, `dining_point_id`, `create_time`, `update_time`, `create_user`, `update_user`
) VALUES
(
  9001, '演示管理员', 'admin_demo', MD5('123456'), '13800000011', '1', '110101199001010011',
  1, 'ADMIN', NULL, '2026-03-18 09:05:00', '2026-03-18 09:05:00', 1, 1
),
(
  9002, '演示操作员', 'operator_demo', MD5('123456'), '13800000012', '1', '110101199001010012',
  1, 'OPERATOR', 9001, '2026-03-18 09:06:00', '2026-03-18 09:06:00', 9001, 9001
);

INSERT INTO `user` (
  `id`, `openid`, `username`, `password`, `role`, `status`, `name`, `phone`,
  `sex`, `id_number`, `avatar`, `create_time`
) VALUES
(
  9101, NULL, 'family_demo', MD5('123456'), 'FAMILY', 1, '张家属', '13800000021',
  '1', '110101198001010021', NULL, '2026-03-18 09:10:00'
),
(
  9102, NULL, 'volunteer_demo', MD5('123456'), 'VOLUNTEER', 1, '李志愿', '13800000022',
  '1', '110101199501010022', NULL, '2026-03-18 09:11:00'
);

-- -----------------------------------------------------
-- 3. 家属关联数据
-- -----------------------------------------------------
INSERT INTO `address_book` (
  `id`, `user_id`, `consignee`, `sex`, `phone`,
  `province_code`, `province_name`, `city_code`, `city_name`,
  `district_code`, `district_name`, `detail`, `label`, `is_default`
) VALUES (
  9301, 9101, '张家属', '1', '13800000021',
  '110000', '北京市', '110100', '北京市',
  '110108', '海淀区', '上地十街10号A座801', '家', 1
);

INSERT INTO `elderly` (
  `id`, `user_id`, `name`, `gender`, `age`, `phone`, `address`, `dining_point_id`,
  `grid_code`, `health_info`, `special_needs`, `id_card`, `image`, `create_time`, `update_time`
) VALUES
(
  9201, 9101, '张爷爷', '1', 78, '13800000031', '北京市海淀区上地十街10号A座801', 9001,
  'GRID-DEMO-01', '高血压，需清淡饮食', '少盐', '110101194801010031', NULL,
  '2026-03-18 09:15:00', '2026-03-18 09:15:00'
),
(
  9202, 9101, '王奶奶', '0', 81, '13800000032', '北京市海淀区上地十街10号A座801', 9001,
  'GRID-DEMO-01', '糖尿病，需软食', '低糖', '110101194501010032', NULL,
  '2026-03-18 09:16:00', '2026-03-18 09:16:00'
);

INSERT INTO `volunteer_stats` (
  `id`, `user_id`, `total_orders`, `total_hours`, `rating`, `level`, `create_time`, `update_time`
) VALUES (
  9401, 9102, 1, 2.0, 5.0, 1, '2026-03-18 09:20:00', '2026-03-18 09:20:00'
);

-- -----------------------------------------------------
-- 4. 演示菜品
--    依赖 reference/sky.sql 中已有的分类：
--    18 特色蒸菜、19 新鲜时蔬、21 汤类
-- -----------------------------------------------------
INSERT INTO `dish` (
  `id`, `name`, `category_id`, `price`, `image`, `description`, `status`,
  `create_time`, `update_time`, `create_user`, `update_user`,
  `dining_point_id`, `nutrition_tags`, `suitability`
) VALUES
(
  9501, '南瓜小米粥（演示）', 21, 8.00, '', '细软易消化，适合老人早餐或加餐', 1,
  '2026-03-18 09:30:00', '2026-03-18 09:30:00', 9001, 9001,
  9001, '软食,清淡', '老人'
),
(
  9502, '清蒸鱼块（演示）', 18, 16.00, '', '高蛋白、少油盐', 1,
  '2026-03-18 09:31:00', '2026-03-18 09:31:00', 9001, 9001,
  9001, '高蛋白,少油少盐', '老人'
),
(
  9503, '时蔬豆腐羹（演示）', 19, 12.00, '', '清淡暖胃，便于咀嚼和吞咽', 1,
  '2026-03-18 09:32:00', '2026-03-18 09:32:00', 9001, 9001,
  9001, '清淡,低脂', '老人'
);

-- -----------------------------------------------------
-- 5. 演示订单
--    98001: 待调度订单，可用于完整流转
--    98002: 已完成订单，可用于历史页面演示
-- -----------------------------------------------------
INSERT INTO `orders` (
  `id`, `number`, `status`, `user_id`, `elder_id`, `volunteer_id`, `dining_point_id`, `address_book_id`,
  `order_time`, `expected_time`, `checkout_time`, `pay_method`, `pay_status`,
  `amount`, `subsidy_amount`, `personal_pay`, `remark`, `phone`, `address`, `user_name`, `consignee`,
  `cancel_reason`, `rejection_reason`, `cancel_time`, `estimated_delivery_time`, `delivery_status`,
  `delivery_time`, `pack_amount`, `tableware_number`, `tableware_status`
) VALUES
(
  98001, 'DEMO202603180001', 2, 9101, 9201, NULL, 9001, 9301,
  '2026-03-18 10:00:00', '2026-03-18 11:00:00', '2026-03-18 10:01:00', 1, 1,
  28.00, 0.00, 28.00, '演示待调度订单，可用于管理员派单 -> 操作员出餐 -> 志愿者取餐/送达',
  '13800000021', '北京市海淀区上地十街10号A座801', '张家属', '张家属',
  NULL, NULL, NULL, '2026-03-18 11:00:00', 1,
  NULL, 0, 2, 1
),
(
  98002, 'DEMO202603170001', 6, 9101, 9202, 9102, 9001, 9301,
  '2026-03-17 11:00:00', '2026-03-17 12:00:00', '2026-03-17 11:01:00', 1, 1,
  20.00, 0.00, 20.00, '演示已完成订单，可用于家属历史订单和志愿者历史任务',
  '13800000021', '北京市海淀区上地十街10号A座801', '张家属', '张家属',
  NULL, NULL, NULL, '2026-03-17 12:00:00', 1,
  '2026-03-17 12:30:00', 0, 2, 1
);

INSERT INTO `order_detail` (
  `id`, `name`, `image`, `order_id`, `dish_id`, `setmeal_id`, `dish_flavor`, `number`, `amount`
) VALUES
(
  99001, '清蒸鱼块（演示）', '', 98001, 9502, NULL, NULL, 1, 16.00
),
(
  99002, '时蔬豆腐羹（演示）', '', 98001, 9503, NULL, NULL, 1, 12.00
),
(
  99003, '南瓜小米粥（演示）', '', 98002, 9501, NULL, NULL, 1, 8.00
),
(
  99004, '时蔬豆腐羹（演示）', '', 98002, 9503, NULL, NULL, 1, 12.00
);

COMMIT;
