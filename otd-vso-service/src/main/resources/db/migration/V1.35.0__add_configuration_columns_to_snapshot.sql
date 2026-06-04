-- ============================================================
-- 补充 vso_order_vehicle_snapshot 表缺失的 configuration 列
-- V1.34.0 删除了 build_config_code/build_config_name 但未添加替代列
-- ============================================================

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

-- vso_order_vehicle_snapshot 表：添加 configuration_code 和 configuration_name
CALL add_column_if_not_exists('vso_order_vehicle_snapshot', 'configuration_code', "VARCHAR(64) DEFAULT NULL COMMENT '配置编码' AFTER `sale_model_name`");
CALL add_column_if_not_exists('vso_order_vehicle_snapshot', 'configuration_name', "VARCHAR(256) DEFAULT NULL COMMENT '配置名称' AFTER `configuration_code`");

-- 清理存储过程
DROP PROCEDURE IF EXISTS add_column_if_not_exists;

SET FOREIGN_KEY_CHECKS = 1;
