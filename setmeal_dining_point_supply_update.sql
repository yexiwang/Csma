-- 第二轮：套餐所属助餐点与供给体系增量脚本
-- 目标：
-- 1. 安全新增 setmeal.dining_point_id
-- 2. 安全新增索引 idx_setmeal_dining_point_id
-- 3. 按“套餐内有效菜品唯一助餐点”规则回填历史数据
-- 4. 输出未回填异常套餐清单，供人工修复

SET @setmeal_dining_point_column_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'setmeal'
    AND COLUMN_NAME = 'dining_point_id'
);

SET @alter_setmeal_add_dining_point_sql := IF(
  @setmeal_dining_point_column_exists = 0,
  'ALTER TABLE `setmeal` ADD COLUMN `dining_point_id` bigint DEFAULT NULL COMMENT ''所属助餐点ID'' AFTER `category_id`',
  'SELECT ''setmeal.dining_point_id already exists'' AS message'
);

PREPARE stmt_alter_setmeal_add_dining_point FROM @alter_setmeal_add_dining_point_sql;
EXECUTE stmt_alter_setmeal_add_dining_point;
DEALLOCATE PREPARE stmt_alter_setmeal_add_dining_point;

SET @setmeal_dining_point_index_exists := (
  SELECT COUNT(*)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'setmeal'
    AND INDEX_NAME = 'idx_setmeal_dining_point_id'
);

SET @create_setmeal_dining_point_index_sql := IF(
  @setmeal_dining_point_index_exists = 0,
  'CREATE INDEX `idx_setmeal_dining_point_id` ON `setmeal` (`dining_point_id`)',
  'SELECT ''idx_setmeal_dining_point_id already exists'' AS message'
);

PREPARE stmt_create_setmeal_dining_point_index FROM @create_setmeal_dining_point_index_sql;
EXECUTE stmt_create_setmeal_dining_point_index;
DEALLOCATE PREPARE stmt_create_setmeal_dining_point_index;

-- 自动回填条件说明：
-- 1. 套餐当前 dining_point_id 为空
-- 2. 套餐下至少存在一条可关联到 dish 的有效菜品记录
-- 3. 所有关联到的有效菜品都属于同一个非空 dining_point_id
UPDATE `setmeal` s
JOIN (
  SELECT
    sd.`setmeal_id`,
    MIN(d.`dining_point_id`) AS resolved_dining_point_id
  FROM `setmeal_dish` sd
  JOIN `dish` d ON d.`id` = sd.`dish_id`
  GROUP BY sd.`setmeal_id`
  HAVING COUNT(d.`id`) > 0
     AND COUNT(DISTINCT d.`dining_point_id`) = 1
     AND SUM(CASE WHEN d.`dining_point_id` IS NULL THEN 1 ELSE 0 END) = 0
) resolved ON resolved.`setmeal_id` = s.`id`
SET
  s.`dining_point_id` = resolved.`resolved_dining_point_id`,
  s.`update_time` = NOW()
WHERE s.`dining_point_id` IS NULL;

-- 已自动回填结果
SELECT
  s.`id`,
  s.`name`,
  s.`dining_point_id`
FROM `setmeal` s
WHERE s.`dining_point_id` IS NOT NULL
ORDER BY s.`id`;

-- 未回填异常数据判定规则：
-- 1. 套餐下无有效菜品
-- 2. 套餐下存在菜品未绑定 dining_point_id
-- 3. 套餐下菜品跨多个 dining_point_id
SELECT
  s.`id`,
  s.`name`,
  COALESCE(stats.`valid_dish_count`, 0) AS `valid_dish_count`,
  COALESCE(stats.`distinct_dining_point_count`, 0) AS `distinct_dining_point_count`,
  COALESCE(stats.`null_dining_point_dish_count`, 0) AS `null_dining_point_dish_count`,
  stats.`dining_point_ids`
FROM `setmeal` s
LEFT JOIN (
  SELECT
    sd.`setmeal_id`,
    COUNT(d.`id`) AS `valid_dish_count`,
    COUNT(DISTINCT d.`dining_point_id`) AS `distinct_dining_point_count`,
    SUM(CASE WHEN d.`dining_point_id` IS NULL THEN 1 ELSE 0 END) AS `null_dining_point_dish_count`,
    GROUP_CONCAT(DISTINCT d.`dining_point_id` ORDER BY d.`dining_point_id`) AS `dining_point_ids`
  FROM `setmeal_dish` sd
  LEFT JOIN `dish` d ON d.`id` = sd.`dish_id`
  GROUP BY sd.`setmeal_id`
) stats ON stats.`setmeal_id` = s.`id`
WHERE s.`dining_point_id` IS NULL
ORDER BY s.`id`;

-- 建议人工处理方式：
-- 1. 无有效菜品：先修复套餐菜品关系，再回填 dining_point_id
-- 2. 存在空助餐点菜品：先补齐 dish.dining_point_id，再回填套餐
-- 3. 跨助餐点套餐：拆分套餐或统一调整套餐内菜品所属助餐点后，再补填套餐 dining_point_id
