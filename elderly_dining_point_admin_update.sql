-- 老人档案所属助餐点字段增量脚本
-- 字段业务含义：表示该老人默认由哪个助餐点提供助餐服务

-- 已存在字段的环境：统一注释并补索引
ALTER TABLE elderly
  MODIFY COLUMN dining_point_id bigint DEFAULT NULL COMMENT '所属助餐点ID/默认助餐点ID';

CREATE INDEX idx_elderly_dining_point_id ON elderly (dining_point_id);

-- 若个别环境缺少该字段，则改用下面这段
-- ALTER TABLE elderly
--   ADD COLUMN dining_point_id bigint DEFAULT NULL COMMENT '所属助餐点ID/默认助餐点ID' AFTER address,
--   ADD INDEX idx_elderly_dining_point_id (dining_point_id);
