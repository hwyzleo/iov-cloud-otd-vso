-- CR-010 销售车型与 MDM 对齐
-- 1. tb_sale_model 新增 Variant 关联和销售域字段
-- 2. 新增 tb_sale_model_config_policy 表（Configuration 销售白名单）
-- 3. 新增 tb_sale_model_option_policy 表（OptionCode 销售策略）
-- 4. 新增 MDM 投影表（variant/configuration/option）
-- 5. vso_order_vehicle_snapshot 新增 Option 相关字段
-- 6. vso_wishlist 新增字段

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. 修改 tb_sale_model 表：新增 Variant 关联和销售域字段
-- ============================================================
ALTER TABLE `tb_sale_model`
    ADD COLUMN `variant_code` VARCHAR(50) DEFAULT NULL COMMENT 'MDM Variant 编码' AFTER `sale_code`,
    ADD COLUMN `base_price` DECIMAL(18,2) DEFAULT NULL COMMENT '起售价' AFTER `down_payment_price`,
    ADD COLUMN `icon` VARCHAR(500) DEFAULT NULL COMMENT '车型图标 URL' AFTER `model_name`,
    ADD COLUMN `marketing_copy` TEXT DEFAULT NULL COMMENT '卖点文案' AFTER `images`,
    ADD COLUMN `listing_status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '上架状态：active/off_shelf' AFTER `enable`,
    ADD COLUMN `effective_from` TIMESTAMP DEFAULT NULL COMMENT '上架生效开始时间' AFTER `listing_status`,
    ADD COLUMN `effective_to` TIMESTAMP DEFAULT NULL COMMENT '上架生效结束时间' AFTER `effective_from`,
    ADD COLUMN `available_regions` JSON DEFAULT NULL COMMENT '可售区域列表，为空表示全国' AFTER `effective_to`,
    ADD COLUMN `channels` JSON DEFAULT NULL COMMENT '可售渠道列表，为空表示全渠道' AFTER `available_regions`;

ALTER TABLE `tb_sale_model`
    ADD UNIQUE KEY `uk_variant_code` (`variant_code`);

-- ============================================================
-- 2. 新增 tb_sale_model_config_policy 表：Configuration 销售白名单
-- ============================================================
CREATE TABLE IF NOT EXISTS `tb_sale_model_config_policy` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `sale_model_code` VARCHAR(50) NOT NULL COMMENT '销售车型编码',
    `configuration_code` VARCHAR(50) NOT NULL COMMENT 'Configuration 编码',
    `status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态：active/off_shelf',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sale_config` (`sale_model_code`, `configuration_code`),
    KEY `idx_sale_model_code` (`sale_model_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Configuration 销售白名单';

-- ============================================================
-- 3. 新增 tb_sale_model_option_policy 表：OptionCode 销售策略
-- ============================================================
CREATE TABLE IF NOT EXISTS `tb_sale_model_option_policy` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `sale_model_code` VARCHAR(50) NOT NULL COMMENT '销售车型编码',
    `option_code` VARCHAR(50) NOT NULL COMMENT 'OptionCode 编码',
    `option_family_code` VARCHAR(50) DEFAULT NULL COMMENT 'OptionFamily 编码',
    `sale_status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '销售状态：active/off_shelf/coming_soon',
    `option_price` DECIMAL(18,2) DEFAULT NULL COMMENT 'Option 价格',
    `available_regions` JSON DEFAULT NULL COMMENT '可售区域列表，为空表示全国',
    `channels` JSON DEFAULT NULL COMMENT '可售渠道列表，为空表示全渠道',
    `bundle_with` JSON DEFAULT NULL COMMENT '捆绑销售 OptionCode 列表',
    `mutex_with` JSON DEFAULT NULL COMMENT '互斥 OptionCode 列表',
    `marketing_title` VARCHAR(255) DEFAULT NULL COMMENT '营销标题',
    `marketing_image` VARCHAR(500) DEFAULT NULL COMMENT '营销图片 URL',
    `sort_weight` INT DEFAULT 0 COMMENT '排序权重',
    `effective_from` TIMESTAMP DEFAULT NULL COMMENT '生效开始时间',
    `effective_to` TIMESTAMP DEFAULT NULL COMMENT '生效结束时间',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sale_option` (`sale_model_code`, `option_code`),
    KEY `idx_sale_model_code` (`sale_model_code`),
    KEY `idx_option_code` (`option_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OptionCode 销售策略';

-- ============================================================
-- 4. 新增 MDM 投影表
-- ============================================================

-- MDM Variant 投影表
CREATE TABLE IF NOT EXISTS `mdm_projection_variant` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `variant_code` VARCHAR(50) NOT NULL COMMENT 'Variant 编码',
    `variant_name` VARCHAR(255) DEFAULT NULL COMMENT 'Variant 名称',
    `model_code` VARCHAR(50) DEFAULT NULL COMMENT 'Model 编码',
    `model_name` VARCHAR(255) DEFAULT NULL COMMENT 'Model 名称',
    `standard_options` JSON DEFAULT NULL COMMENT '标配 OptionCode 列表',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_variant_code` (`variant_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MDM Variant 投影';

-- MDM Configuration 投影表
CREATE TABLE IF NOT EXISTS `mdm_projection_configuration` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `configuration_code` VARCHAR(50) NOT NULL COMMENT 'Configuration 编码',
    `variant_code` VARCHAR(50) NOT NULL COMMENT '所属 Variant 编码',
    `option_codes` JSON DEFAULT NULL COMMENT '包含的 OptionCode 列表',
    `guide_price` DECIMAL(18,2) DEFAULT NULL COMMENT '指导价',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_configuration_code` (`configuration_code`),
    KEY `idx_variant_code` (`variant_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MDM Configuration 投影';

-- MDM OptionCode 投影表
CREATE TABLE IF NOT EXISTS `mdm_projection_option` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `option_code` VARCHAR(50) NOT NULL COMMENT 'OptionCode 编码',
    `option_family_code` VARCHAR(50) DEFAULT NULL COMMENT 'OptionFamily 编码',
    `option_name` VARCHAR(255) DEFAULT NULL COMMENT 'Option 名称',
    `mutex_with` JSON DEFAULT NULL COMMENT '互斥 OptionCode 列表',
    `bundle_with` JSON DEFAULT NULL COMMENT '捆绑 OptionCode 列表',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_option_code` (`option_code`),
    KEY `idx_option_family_code` (`option_family_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MDM OptionCode 投影';

-- ============================================================
-- 5. 修改 vso_order_vehicle_snapshot 表：新增 Option 相关字段
-- ============================================================
ALTER TABLE `vso_order_vehicle_snapshot`
    ADD COLUMN `variant_code` VARCHAR(50) DEFAULT NULL COMMENT 'Variant 编码' AFTER `sale_model_code`,
    ADD COLUMN `configuration_code` VARCHAR(50) DEFAULT NULL COMMENT 'Configuration 编码' AFTER `build_config_code`,
    ADD COLUMN `option_codes` JSON DEFAULT NULL COMMENT 'OptionCode 列表' AFTER `configuration_code`,
    ADD COLUMN `option_price_breakdown` JSON DEFAULT NULL COMMENT 'Option 价格明细' AFTER `option_codes`,
    ADD COLUMN `sale_policy_snapshot` JSON DEFAULT NULL COMMENT '销售策略快照' AFTER `option_price_breakdown`;

-- ============================================================
-- 6. 修改 vso_wishlist 表：新增字段
-- ============================================================
ALTER TABLE `vso_wishlist`
    ADD COLUMN `sale_model_code` VARCHAR(50) DEFAULT NULL COMMENT '销售车型编码' AFTER `user_id`,
    ADD COLUMN `configuration_code` VARCHAR(50) DEFAULT NULL COMMENT 'Configuration 编码' AFTER `sale_model_code`,
    ADD COLUMN `option_codes` JSON DEFAULT NULL COMMENT 'OptionCode 列表' AFTER `configuration_code`,
    ADD COLUMN `option_codes_hash` VARCHAR(64) DEFAULT NULL COMMENT 'OptionCode 排序后哈希' AFTER `option_codes`,
    ADD COLUMN `invalid_reason` VARCHAR(50) DEFAULT NULL COMMENT '失效原因枚举' AFTER `status`;

SET FOREIGN_KEY_CHECKS = 1;
