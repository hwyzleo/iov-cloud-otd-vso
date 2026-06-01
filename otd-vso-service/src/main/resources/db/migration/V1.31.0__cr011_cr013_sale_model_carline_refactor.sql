-- ============================================================
-- CR-011/CR-013 销售车型粒度上提至 Carline 重构
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 创建通用存储过程：检查列是否存在
DROP PROCEDURE IF EXISTS add_column_if_not_exists;
DELIMITER //
CREATE PROCEDURE add_column_if_not_exists(
    IN p_table_name VARCHAR(128),
    IN p_column_name VARCHAR(128),
    IN p_column_definition VARCHAR(512)
)
BEGIN
    DECLARE column_count INT;
    SELECT COUNT(*) INTO column_count
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
    AND table_name = p_table_name
    AND column_name = p_column_name;
    
    IF column_count = 0 THEN
        SET @sql = CONCAT('ALTER TABLE `', p_table_name, '` ADD COLUMN `', p_column_name, '` ', p_column_definition);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

-- 创建通用存储过程：检查索引是否存在
DROP PROCEDURE IF EXISTS drop_index_if_exists;
DELIMITER //
CREATE PROCEDURE drop_index_if_exists(IN p_table_name VARCHAR(128), IN p_index_name VARCHAR(128))
BEGIN
    DECLARE index_count INT;
    SELECT COUNT(*) INTO index_count
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
    AND table_name = p_table_name
    AND index_name = p_index_name;
    
    IF index_count > 0 THEN
        SET @sql = CONCAT('ALTER TABLE `', p_table_name, '` DROP INDEX `', p_index_name, '`');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

-- ============================================================
-- 1. 修改 vso_sale_model 表
-- ============================================================
-- 1.1 新增 carline_code 字段
CALL add_column_if_not_exists('vso_sale_model', 'carline_code', "VARCHAR(50) DEFAULT NULL COMMENT 'MDM Carline 编码（1:1 关联）' AFTER `sale_code`");

-- 1.2 删除 variant_code 唯一索引
CALL drop_index_if_exists('vso_sale_model', 'uk_variant_code');

-- 1.3-1.6 删除旧字段（使用存储过程检查列是否存在）
DROP PROCEDURE IF EXISTS drop_column_if_exists;
DELIMITER //
CREATE PROCEDURE drop_column_if_exists(
    IN p_table_name VARCHAR(128),
    IN p_column_name VARCHAR(128)
)
BEGIN
    DECLARE column_count INT;
    SELECT COUNT(*) INTO column_count
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
    AND table_name = p_table_name
    AND column_name = p_column_name;
    
    IF column_count > 0 THEN
        SET @sql = CONCAT('ALTER TABLE `', p_table_name, '` DROP COLUMN `', p_column_name, '`');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

CALL drop_column_if_exists('vso_sale_model', 'variant_code');
CALL drop_column_if_exists('vso_sale_model', 'base_price');
CALL drop_column_if_exists('vso_sale_model', 'earnest_money_price');
CALL drop_column_if_exists('vso_sale_model', 'down_payment_price');

-- 1.7 添加 carline_code 唯一索引（先检查是否已存在）
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists FROM information_schema.statistics 
WHERE table_schema = DATABASE() AND table_name = 'vso_sale_model' AND index_name = 'uk_carline_code';
SET @sql = IF(@index_exists = 0, 'ALTER TABLE `vso_sale_model` ADD UNIQUE KEY `uk_carline_code` (`carline_code`)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================
-- 2. 新增 vso_sale_model_model_policy 表：Model 销售策略
-- ============================================================
CREATE TABLE IF NOT EXISTS `vso_sale_model_model_policy` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `sale_model_code` VARCHAR(50) NOT NULL COMMENT '销售车型编码',
    `model_code` VARCHAR(50) NOT NULL COMMENT 'Model 编码',
    `sale_status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '销售状态：active/off_shelf',
    `available_regions` JSON DEFAULT NULL COMMENT '可售区域列表，为空表示全国',
    `channels` JSON DEFAULT NULL COMMENT '可售渠道列表，为空表示全渠道',
    `marketing_name` VARCHAR(255) DEFAULT NULL COMMENT '营销名称',
    `marketing_image` VARCHAR(500) DEFAULT NULL COMMENT '营销图片 URL',
    `marketing_copy` TEXT DEFAULT NULL COMMENT '营销文案',
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
    UNIQUE KEY `uk_sale_model` (`sale_model_code`, `model_code`),
    KEY `idx_sale_model_code` (`sale_model_code`),
    KEY `idx_model_code` (`model_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Model 销售策略';

-- ============================================================
-- 3. 新增 vso_sale_model_variant_policy 表：Variant 销售策略
-- ============================================================
CREATE TABLE IF NOT EXISTS `vso_sale_model_variant_policy` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `sale_model_code` VARCHAR(50) NOT NULL COMMENT '销售车型编码',
    `variant_code` VARCHAR(50) NOT NULL COMMENT 'Variant 编码',
    `sale_status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '销售状态：active/off_shelf',
    `variant_price` DECIMAL(18,2) DEFAULT NULL COMMENT 'Variant 价格（必填）',
    `earnest_money_price` DECIMAL(18,2) DEFAULT NULL COMMENT '意向金价格',
    `down_payment_price` DECIMAL(18,2) DEFAULT NULL COMMENT '定金价格',
    `available_regions` JSON DEFAULT NULL COMMENT '可售区域列表，为空表示全国',
    `channels` JSON DEFAULT NULL COMMENT '可售渠道列表，为空表示全渠道',
    `marketing_name` VARCHAR(255) DEFAULT NULL COMMENT '营销名称',
    `marketing_image` VARCHAR(500) DEFAULT NULL COMMENT '营销图片 URL',
    `marketing_copy` TEXT DEFAULT NULL COMMENT '营销文案',
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
    UNIQUE KEY `uk_sale_variant` (`sale_model_code`, `variant_code`),
    KEY `idx_sale_model_code` (`sale_model_code`),
    KEY `idx_variant_code` (`variant_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Variant 销售策略';

-- ============================================================
-- 4. 新增 mdm_projection_carline 表：Carline 投影
-- ============================================================
CREATE TABLE IF NOT EXISTS `mdm_projection_carline` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `carline_code` VARCHAR(50) NOT NULL COMMENT 'Carline 编码',
    `carline_name` VARCHAR(255) DEFAULT NULL COMMENT 'Carline 名称',
    `model_codes` JSON DEFAULT NULL COMMENT '下属 Model 编码列表',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_carline_code` (`carline_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MDM Carline 投影';

-- ============================================================
-- 5. 新增 mdm_projection_model 表：Model 投影
-- ============================================================
CREATE TABLE IF NOT EXISTS `mdm_projection_model` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `model_code` VARCHAR(50) NOT NULL COMMENT 'Model 编码',
    `model_name` VARCHAR(255) DEFAULT NULL COMMENT 'Model 名称',
    `carline_code` VARCHAR(50) DEFAULT NULL COMMENT '所属 Carline 编码',
    `variant_codes` JSON DEFAULT NULL COMMENT '下属 Variant 编码列表',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_model_code` (`model_code`),
    KEY `idx_carline_code` (`carline_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MDM Model 投影';

-- ============================================================
-- 6. 修改 vso_order_vehicle_snapshot 表：新增字段
-- ============================================================
CALL add_column_if_not_exists('vso_order_vehicle_snapshot', 'carline_code', "VARCHAR(50) DEFAULT NULL COMMENT 'Carline 编码' AFTER `sale_model_code`");
CALL add_column_if_not_exists('vso_order_vehicle_snapshot', 'model_code', "VARCHAR(50) DEFAULT NULL COMMENT 'Model 编码' AFTER `carline_code`");
CALL add_column_if_not_exists('vso_order_vehicle_snapshot', 'model_policy_snapshot', "JSON DEFAULT NULL COMMENT 'Model 销售策略快照' AFTER `sale_policy_snapshot`");
CALL add_column_if_not_exists('vso_order_vehicle_snapshot', 'variant_policy_snapshot', "JSON DEFAULT NULL COMMENT 'Variant 销售策略快照' AFTER `model_policy_snapshot`");
CALL add_column_if_not_exists('vso_order_vehicle_snapshot', 'config_policy_snapshot', "JSON DEFAULT NULL COMMENT 'Configuration 销售白名单快照' AFTER `variant_policy_snapshot`");

-- ============================================================
-- 7. 修改 vso_wishlist 表：新增字段
-- ============================================================
CALL add_column_if_not_exists('vso_wishlist', 'model_code', "VARCHAR(50) DEFAULT NULL COMMENT 'Model 编码' AFTER `sale_model_code`");
CALL add_column_if_not_exists('vso_wishlist', 'variant_code', "VARCHAR(50) DEFAULT NULL COMMENT 'Variant 编码' AFTER `model_code`");

-- ============================================================
-- 8. 修改 vso_sale_model_config_policy 表：归属键扩展
-- ============================================================
CALL drop_index_if_exists('vso_sale_model_config_policy', 'uk_sale_config');
CALL add_column_if_not_exists('vso_sale_model_config_policy', 'variant_code', "VARCHAR(50) DEFAULT NULL COMMENT 'Variant 编码' AFTER `sale_model_code`");

-- 添加新唯一索引
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists FROM information_schema.statistics 
WHERE table_schema = DATABASE() AND table_name = 'vso_sale_model_config_policy' AND index_name = 'uk_sale_variant_config';
SET @sql = IF(@index_exists = 0, 'ALTER TABLE `vso_sale_model_config_policy` ADD UNIQUE KEY `uk_sale_variant_config` (`sale_model_code`, `variant_code`, `configuration_code`)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================
-- 9. 修改 vso_sale_model_option_policy 表：归属键扩展
-- ============================================================
CALL drop_index_if_exists('vso_sale_model_option_policy', 'uk_sale_option');
CALL add_column_if_not_exists('vso_sale_model_option_policy', 'variant_code', "VARCHAR(50) DEFAULT NULL COMMENT 'Variant 编码' AFTER `sale_model_code`");

-- 添加新唯一索引
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists FROM information_schema.statistics 
WHERE table_schema = DATABASE() AND table_name = 'vso_sale_model_option_policy' AND index_name = 'uk_sale_variant_option';
SET @sql = IF(@index_exists = 0, 'ALTER TABLE `vso_sale_model_option_policy` ADD UNIQUE KEY `uk_sale_variant_option` (`sale_model_code`, `variant_code`, `option_code`)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 清理存储过程
DROP PROCEDURE IF EXISTS add_column_if_not_exists;
DROP PROCEDURE IF EXISTS drop_index_if_exists;
DROP PROCEDURE IF EXISTS drop_column_if_exists;

SET FOREIGN_KEY_CHECKS = 1;
