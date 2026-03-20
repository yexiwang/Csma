-- 第六次执行：物理删除 family_profile.name / family_profile.phone
-- 前置说明：
-- 1. 家属姓名主数据已收口到 user.name
-- 2. 家属联系电话主数据已收口到 user.phone
-- 3. family_profile VO 仍返回 name / phone，但来源改为 user 表联查

-- 1) 删除依赖 name 的索引
alter table family_profile
    drop index idx_family_profile_name_deleted;

-- 2) 物理删除兼容列
alter table family_profile
    drop column name,
    drop column phone;
