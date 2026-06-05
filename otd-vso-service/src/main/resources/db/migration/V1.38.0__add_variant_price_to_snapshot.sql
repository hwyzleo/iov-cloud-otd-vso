-- V1.38.0__add_variant_price_to_snapshot.sql
-- Add variant_price field to vso_order_vehicle_snapshot table

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

-- Add variant_price field
CALL add_column_if_not_exists('vso_order_vehicle_snapshot', 'variant_price', "DECIMAL(10,2) DEFAULT NULL COMMENT '版本价格' AFTER `variant_name`");

-- 清理存储过程
DROP PROCEDURE IF EXISTS add_column_if_not_exists;

SET FOREIGN_KEY_CHECKS = 1;
