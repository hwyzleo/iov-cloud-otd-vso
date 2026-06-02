-- ============================================================
-- 销售策略表补充 model_code 字段
-- variant/config/option 三层策略表增加 model_code，使 VSO 表体系自包含
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
-- 1. vso_sale_model_variant_policy：加 model_code
-- ============================================================
CALL add_column_if_not_exists('vso_sale_model_variant_policy', 'model_code', "VARCHAR(50) DEFAULT NULL COMMENT 'Model 编码' AFTER `sale_model_code`");

-- 更新唯一索引：(sale_model_code, variant_code) → (sale_model_code, model_code, variant_code)
CALL drop_index_if_exists('vso_sale_model_variant_policy', 'uk_sale_variant');
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists FROM information_schema.statistics 
WHERE table_schema = DATABASE() AND table_name = 'vso_sale_model_variant_policy' AND index_name = 'uk_sale_model_variant';
SET @sql = IF(@index_exists = 0, 'ALTER TABLE `vso_sale_model_variant_policy` ADD UNIQUE KEY `uk_sale_model_variant` (`sale_model_code`, `model_code`, `variant_code`)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 model_code 索引
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists FROM information_schema.statistics 
WHERE table_schema = DATABASE() AND table_name = 'vso_sale_model_variant_policy' AND index_name = 'idx_model_code';
SET @sql = IF(@index_exists = 0, 'ALTER TABLE `vso_sale_model_variant_policy` ADD KEY `idx_model_code` (`model_code`)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================
-- 2. vso_sale_model_config_policy：加 model_code
-- ============================================================
CALL add_column_if_not_exists('vso_sale_model_config_policy', 'model_code', "VARCHAR(50) DEFAULT NULL COMMENT 'Model 编码' AFTER `sale_model_code`");

-- 更新唯一索引：(sale_model_code, variant_code, configuration_code) → (sale_model_code, model_code, variant_code, configuration_code)
CALL drop_index_if_exists('vso_sale_model_config_policy', 'uk_sale_variant_config');
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists FROM information_schema.statistics 
WHERE table_schema = DATABASE() AND table_name = 'vso_sale_model_config_policy' AND index_name = 'uk_sale_model_variant_config';
SET @sql = IF(@index_exists = 0, 'ALTER TABLE `vso_sale_model_config_policy` ADD UNIQUE KEY `uk_sale_model_variant_config` (`sale_model_code`, `model_code`, `variant_code`, `configuration_code`)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================
-- 3. vso_sale_model_option_policy：加 model_code
-- ============================================================
CALL add_column_if_not_exists('vso_sale_model_option_policy', 'model_code', "VARCHAR(50) DEFAULT NULL COMMENT 'Model 编码' AFTER `sale_model_code`");

-- 更新唯一索引：(sale_model_code, variant_code, option_code) → (sale_model_code, model_code, variant_code, option_code)
CALL drop_index_if_exists('vso_sale_model_option_policy', 'uk_sale_variant_option');
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists FROM information_schema.statistics 
WHERE table_schema = DATABASE() AND table_name = 'vso_sale_model_option_policy' AND index_name = 'uk_sale_model_variant_option';
SET @sql = IF(@index_exists = 0, 'ALTER TABLE `vso_sale_model_option_policy` ADD UNIQUE KEY `uk_sale_model_variant_option` (`sale_model_code`, `model_code`, `variant_code`, `option_code`)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 model_code 索引
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists FROM information_schema.statistics 
WHERE table_schema = DATABASE() AND table_name = 'vso_sale_model_option_policy' AND index_name = 'idx_option_model_code';
SET @sql = IF(@index_exists = 0, 'ALTER TABLE `vso_sale_model_option_policy` ADD KEY `idx_option_model_code` (`model_code`)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 清理存储过程
DROP PROCEDURE IF EXISTS add_column_if_not_exists;
DROP PROCEDURE IF EXISTS drop_index_if_exists;

SET FOREIGN_KEY_CHECKS = 1;
