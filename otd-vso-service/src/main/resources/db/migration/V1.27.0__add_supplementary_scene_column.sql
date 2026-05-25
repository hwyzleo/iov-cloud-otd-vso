-- 为vso_supplementary_payment表新增supplementary_scene字段
-- 用于区分改配补款(config_change)和意向金转定金差额(earnest_to_down)

ALTER TABLE vso_supplementary_payment
ADD COLUMN supplementary_scene VARCHAR(32) DEFAULT 'config_change' COMMENT '补款场景：config_change-改配补款，earnest_to_down-意向金转定金差额';

-- 更新现有数据的场景为改配补款
UPDATE vso_supplementary_payment SET supplementary_scene = 'config_change' WHERE supplementary_scene IS NULL;

-- 设置字段为非空
ALTER TABLE vso_supplementary_payment
MODIFY COLUMN supplementary_scene VARCHAR(32) NOT NULL DEFAULT 'config_change' COMMENT '补款场景：config_change-改配补款，earnest_to_down-意向金转定金差额';
