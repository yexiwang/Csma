USE `sky_take_out`;

-- Create family_profile table if it does not exist.
SET @family_profile_exists = (
  SELECT COUNT(1)
  FROM information_schema.tables
  WHERE table_schema = DATABASE()
    AND table_name = 'family_profile'
);
SET @create_family_profile_sql = IF(
  @family_profile_exists = 0,
  'CREATE TABLE `family_profile` (
      `id` bigint NOT NULL AUTO_INCREMENT COMMENT ''primary key'',
      `user_id` bigint NOT NULL COMMENT ''linked FAMILY user id'',
      `name` varchar(50) NOT NULL COMMENT ''family name'',
      `phone` varchar(20) DEFAULT NULL COMMENT ''phone'',
      `remark` varchar(255) DEFAULT NULL COMMENT ''remark'',
      `status` int NOT NULL DEFAULT ''1'' COMMENT ''1 enabled 0 disabled'',
      `create_time` datetime DEFAULT NULL COMMENT ''create time'',
      `update_time` datetime DEFAULT NULL COMMENT ''update time'',
      `create_user` bigint DEFAULT NULL COMMENT ''create user'',
      `update_user` bigint DEFAULT NULL COMMENT ''update user'',
      `is_deleted` int NOT NULL DEFAULT ''0'' COMMENT ''0 active 1 deleted'',
      PRIMARY KEY (`id`),
      UNIQUE KEY `uk_family_profile_user_id` (`user_id`),
      KEY `idx_family_profile_status_deleted` (`status`, `is_deleted`),
      KEY `idx_family_profile_name_deleted` (`name`, `is_deleted`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT=''family profile''',
  'SELECT 1'
);
PREPARE stmt_create_family_profile FROM @create_family_profile_sql;
EXECUTE stmt_create_family_profile;
DEALLOCATE PREPARE stmt_create_family_profile;

-- Add elderly.is_deleted if missing.
SET @elderly_has_is_deleted = (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'elderly'
    AND column_name = 'is_deleted'
);
SET @alter_elderly_sql = IF(
  @elderly_has_is_deleted = 0,
  'ALTER TABLE `elderly`
     ADD COLUMN `is_deleted` int NOT NULL DEFAULT ''0'' COMMENT ''0 active 1 deleted'' AFTER `update_time`',
  'SELECT 1'
);
PREPARE stmt_alter_elderly FROM @alter_elderly_sql;
EXECUTE stmt_alter_elderly;
DEALLOCATE PREPARE stmt_alter_elderly;

-- Add elderly(user_id, is_deleted) index if missing.
SET @elderly_user_deleted_index_exists = (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'elderly'
    AND index_name = 'idx_elderly_user_deleted'
);
SET @create_elderly_index_sql = IF(
  @elderly_user_deleted_index_exists = 0,
  'CREATE INDEX `idx_elderly_user_deleted` ON `elderly` (`user_id`, `is_deleted`)',
  'SELECT 1'
);
PREPARE stmt_create_elderly_index FROM @create_elderly_index_sql;
EXECUTE stmt_create_elderly_index;
DEALLOCATE PREPARE stmt_create_elderly_index;

-- Backfill family profiles only for FAMILY users already referenced by elderly.
INSERT INTO `family_profile` (
  `user_id`, `name`, `phone`, `remark`, `status`,
  `create_time`, `update_time`, `create_user`, `update_user`, `is_deleted`
)
SELECT DISTINCT
  u.`id` AS `user_id`,
  COALESCE(NULLIF(TRIM(u.`name`), ''), CONCAT('Family', u.`id`)) AS `name`,
  u.`phone` AS `phone`,
  'Backfilled from elderly linkage' AS `remark`,
  1 AS `status`,
  NOW() AS `create_time`,
  NOW() AS `update_time`,
  NULL AS `create_user`,
  NULL AS `update_user`,
  0 AS `is_deleted`
FROM `elderly` e
INNER JOIN `user` u ON e.`user_id` = u.`id`
LEFT JOIN `family_profile` fp ON fp.`user_id` = u.`id`
WHERE u.`role` = 'FAMILY'
  AND fp.`id` IS NULL;

-- Diagnostic query: elderly linked to missing or non-FAMILY users.
SELECT
  e.`id` AS elderly_id,
  e.`name` AS elderly_name,
  e.`user_id`,
  u.`username`,
  u.`role`
FROM `elderly` e
LEFT JOIN `user` u ON e.`user_id` = u.`id`
WHERE u.`id` IS NULL
   OR COALESCE(u.`role`, '') <> 'FAMILY';
