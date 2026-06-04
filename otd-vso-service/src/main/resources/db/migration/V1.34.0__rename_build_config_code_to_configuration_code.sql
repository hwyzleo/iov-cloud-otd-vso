-- ============================================================
-- 清理遗留的 build_config_code 字段
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 创建存储过程：检查列是否存在后删除
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

-- 创建存储过程：重命名列（仅当旧列存在且新列不存在时）
DROP PROCEDURE IF EXISTS rename_column_if_needed;
DELIMITER //
CREATE PROCEDURE rename_column_if_needed(
    IN p_table_name VARCHAR(128),
    IN p_old_column VARCHAR(128),
    IN p_new_column VARCHAR(128),
    IN p_column_definition VARCHAR(512)
)
BEGIN
    DECLARE old_count INT;
    DECLARE new_count INT;
    SELECT COUNT(*) INTO old_count
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
    AND table_name = p_table_name
    AND column_name = p_old_column;
    SELECT COUNT(*) INTO new_count
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
    AND table_name = p_table_name
    AND column_name = p_new_column;
    
    IF old_count > 0 AND new_count = 0 THEN
        SET @sql = CONCAT('ALTER TABLE `', p_table_name, '` CHANGE COLUMN `', p_old_column, '` `', p_new_column, '` ', p_column_definition);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

-- ============================================================
-- 1. vso_order 表：如果 build_config_code 存在则重命名
-- ============================================================
CALL rename_column_if_needed('vso_order', 'build_config_code', 'configuration_code', "VARCHAR(64) DEFAULT NULL COMMENT '配置编码'");

-- ============================================================
-- 2. vso_order_vehicle_snapshot 表：删除旧的 build_config_code/build_config_name
-- ============================================================
CALL drop_column_if_exists('vso_order_vehicle_snapshot', 'build_config_code');
CALL drop_column_if_exists('vso_order_vehicle_snapshot', 'build_config_name');

-- 清理存储过程
DROP PROCEDURE IF EXISTS drop_column_if_exists;
DROP PROCEDURE IF EXISTS rename_column_if_needed;

SET FOREIGN_KEY_CHECKS = 1;
