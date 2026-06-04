-- V1.37.0__rename_snapshot_option_breakdown.sql
-- Add model_name and variant_name fields, rename option_price_breakdown to option_breakdown

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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

-- Add new fields
CALL add_column_if_not_exists('vso_order_vehicle_snapshot', 'model_name', "VARCHAR(100) DEFAULT NULL COMMENT '车型名称' AFTER `model_code`");
CALL add_column_if_not_exists('vso_order_vehicle_snapshot', 'variant_name', "VARCHAR(100) DEFAULT NULL COMMENT '版本名称' AFTER `variant_code`");

-- Rename field: option_price_breakdown -> option_breakdown
-- 使用存储过程安全处理列重命名
DROP PROCEDURE IF EXISTS rename_column_if_exists;
DELIMITER //
CREATE PROCEDURE rename_column_if_exists(
    IN p_table_name VARCHAR(128),
    IN p_old_column_name VARCHAR(128),
    IN p_new_column_name VARCHAR(128),
    IN p_column_definition VARCHAR(512)
)
BEGIN
    DECLARE column_count INT;
    SELECT COUNT(*) INTO column_count
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
    AND table_name = p_table_name
    AND column_name = p_old_column_name;

    IF column_count > 0 THEN
        SET @sql = CONCAT('ALTER TABLE `', p_table_name, '` CHANGE COLUMN `', p_old_column_name, '` `', p_new_column_name, '` ', p_column_definition);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

CALL rename_column_if_exists('vso_order_vehicle_snapshot', 'option_price_breakdown', 'option_breakdown', "JSON DEFAULT NULL COMMENT '选项明细（JSON，含 optionCode/optionFamilyCode/optionFamilyName/optionName/optionPrice）'");

-- 清理存储过程
DROP PROCEDURE IF EXISTS add_column_if_not_exists;
DROP PROCEDURE IF EXISTS rename_column_if_exists;

SET FOREIGN_KEY_CHECKS = 1;
