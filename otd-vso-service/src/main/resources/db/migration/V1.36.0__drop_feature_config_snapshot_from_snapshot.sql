-- ============================================================
-- 删除 vso_order_vehicle_snapshot 表冗余的 feature_config_snapshot 列
-- 该列与 option_codes 语义重复，保留 option_codes 即可
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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

CALL drop_column_if_exists('vso_order_vehicle_snapshot', 'feature_config_snapshot');

DROP PROCEDURE IF EXISTS drop_column_if_exists;

SET FOREIGN_KEY_CHECKS = 1;
