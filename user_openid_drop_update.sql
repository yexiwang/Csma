USE `sky_take_out`;

SET @openid_column_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user'
      AND COLUMN_NAME = 'openid'
);

SET @drop_openid_sql := IF(
    @openid_column_exists > 0,
    'ALTER TABLE `user` DROP COLUMN `openid`',
    'SELECT ''user.openid already removed'' AS message'
);

PREPARE stmt FROM @drop_openid_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
