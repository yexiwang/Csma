ALTER TABLE `orders` ADD COLUMN `pickup_time` datetime DEFAULT NULL COMMENT '取餐时间' AFTER `delivery_time`;
