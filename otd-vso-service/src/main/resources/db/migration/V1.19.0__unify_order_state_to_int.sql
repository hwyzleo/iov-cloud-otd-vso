-- ============================================================
-- VSO 车辆销售订单系统 - 统一订单状态为数值存储
-- Flyway 版本：V1.19.0
-- ============================================================

SET NAMES utf8mb4;

-- 1. 添加新字段 order_state (INT) - 如果不存在
SET @dbname = DATABASE();
SET @tablename = 'vso_order';
SET @columnname = 'order_state';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE `', @tablename, '` ADD COLUMN `', @columnname, '` INT DEFAULT NULL COMMENT ''订单状态数值'' AFTER `license_city`')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 2. 迁移数据：将 main_status 字符串转换为 order_state 数值
UPDATE `vso_order` SET `order_state` = CASE `main_status`
    WHEN 'PENDING_CREATE' THEN 100
    WHEN 'PENDING_SUBMIT' THEN 200
    WHEN 'PENDING_AUDIT' THEN 200
    WHEN 'PENDING_LOCK' THEN 300
    WHEN 'LOCKED' THEN 400
    WHEN 'PENDING_VEHICLE_ASSIGN' THEN 400
    WHEN 'VEHICLE_ASSIGNED' THEN 450
    WHEN 'PENDING_CONTRACT' THEN 450
    WHEN 'PENDING_PAYMENT' THEN 500
    WHEN 'PENDING_DELIVERY' THEN 600
    WHEN 'DELIVERED' THEN 650
    WHEN 'COMPLETED' THEN 700
    WHEN 'CANCELLED' THEN 950
    WHEN 'CLOSED' THEN 960
    WHEN 'WISHLIST' THEN 100
    WHEN 'EARNEST_MONEY_UNPAID' THEN 200
    WHEN 'EARNEST_MONEY_PAID' THEN 210
    WHEN 'DOWN_PAYMENT_UNPAID' THEN 300
    WHEN 'DOWN_PAYMENT_PAID' THEN 310
    WHEN 'ARRANGE_PRODUCTION' THEN 400
    WHEN 'ALLOCATION_VEHICLE' THEN 450
    WHEN 'APPLY_TRANSPORT' THEN 470
    WHEN 'PREPARE_TRANSPORT' THEN 500
    WHEN 'TRANSPORTING' THEN 550
    WHEN 'PREPARE_DELIVER' THEN 600
    WHEN 'FINAL_PAYMENT_PAID' THEN 620
    WHEN 'INVOICED' THEN 630
    WHEN 'DELIVERED' THEN 650
    WHEN 'ACTIVATED' THEN 700
    WHEN 'RETURN_APPLY' THEN 800
    WHEN 'RETURN_STORAGE' THEN 820
    WHEN 'RETURN_AUDIT' THEN 840
    WHEN 'RETURN_COMPLETED' THEN 860
    WHEN 'REFUND_APPLY' THEN 920
    WHEN 'REFUND_COMPLETE' THEN 925
    WHEN 'CANCEL' THEN 950
    WHEN 'EXPIRED' THEN 960
    ELSE NULL
END WHERE `order_state` IS NULL AND `main_status` IS NOT NULL;

-- 3. 添加新字段 previous_order_state (INT) - 如果不存在
SET @columnname = 'previous_order_state';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE `', @tablename, '` ADD COLUMN `', @columnname, '` INT DEFAULT NULL COMMENT ''关闭前上一有效状态数值'' AFTER `order_state`')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 4. 迁移 previous_main_status 数据
UPDATE `vso_order` SET `previous_order_state` = CASE `previous_main_status`
    WHEN 'PENDING_CREATE' THEN 100
    WHEN 'PENDING_SUBMIT' THEN 200
    WHEN 'PENDING_AUDIT' THEN 200
    WHEN 'PENDING_LOCK' THEN 300
    WHEN 'LOCKED' THEN 400
    WHEN 'PENDING_VEHICLE_ASSIGN' THEN 400
    WHEN 'VEHICLE_ASSIGNED' THEN 450
    WHEN 'PENDING_CONTRACT' THEN 450
    WHEN 'PENDING_PAYMENT' THEN 500
    WHEN 'PENDING_DELIVERY' THEN 600
    WHEN 'DELIVERED' THEN 650
    WHEN 'COMPLETED' THEN 700
    WHEN 'CANCELLED' THEN 950
    WHEN 'CLOSED' THEN 960
    WHEN 'WISHLIST' THEN 100
    WHEN 'EARNEST_MONEY_UNPAID' THEN 200
    WHEN 'EARNEST_MONEY_PAID' THEN 210
    WHEN 'DOWN_PAYMENT_UNPAID' THEN 300
    WHEN 'DOWN_PAYMENT_PAID' THEN 310
    WHEN 'ARRANGE_PRODUCTION' THEN 400
    WHEN 'ALLOCATION_VEHICLE' THEN 450
    WHEN 'APPLY_TRANSPORT' THEN 470
    WHEN 'PREPARE_TRANSPORT' THEN 500
    WHEN 'TRANSPORTING' THEN 550
    WHEN 'PREPARE_DELIVER' THEN 600
    WHEN 'FINAL_PAYMENT_PAID' THEN 620
    WHEN 'INVOICED' THEN 630
    WHEN 'DELIVERED' THEN 650
    WHEN 'ACTIVATED' THEN 700
    WHEN 'RETURN_APPLY' THEN 800
    WHEN 'RETURN_STORAGE' THEN 820
    WHEN 'RETURN_AUDIT' THEN 840
    WHEN 'RETURN_COMPLETED' THEN 860
    WHEN 'REFUND_APPLY' THEN 920
    WHEN 'REFUND_COMPLETE' THEN 925
    WHEN 'CANCEL' THEN 950
    WHEN 'EXPIRED' THEN 960
    ELSE NULL
END WHERE `previous_order_state` IS NULL AND `previous_main_status` IS NOT NULL;

-- 5. 更新索引 - 先删除再添加（处理可能的重复）
-- 检查并删除旧索引
SET @indexname = 'idx_status_time';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND INDEX_NAME = @indexname) > 0,
  CONCAT('ALTER TABLE `', @tablename, '` DROP INDEX `', @indexname, '`'),
  'SELECT 1'
));
PREPARE dropIfExists FROM @preparedStatement;
EXECUTE dropIfExists;
DEALLOCATE PREPARE dropIfExists;

SET @indexname = 'idx_store_sales_status';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND INDEX_NAME = @indexname) > 0,
  CONCAT('ALTER TABLE `', @tablename, '` DROP INDEX `', @indexname, '`'),
  'SELECT 1'
));
PREPARE dropIfExists FROM @preparedStatement;
EXECUTE dropIfExists;
DEALLOCATE PREPARE dropIfExists;

-- 添加新索引
ALTER TABLE `vso_order` ADD INDEX `idx_status_time` (`order_state`, `created_at_business`);
ALTER TABLE `vso_order` ADD INDEX `idx_store_sales_status` (`store_code`, `sales_code`, `order_state`);

-- 6. 同步修改影子删除表 - 添加 before_order_state 列如果不存在
SET @tablename = 'vso_order_shadow_delete';
SET @columnname = 'before_order_state';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE `', @tablename, '` ADD COLUMN `', @columnname, '` INT DEFAULT NULL COMMENT ''删除前订单状态数值'' AFTER `delete_reason`')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 7. 迁移影子删除表数据
UPDATE `vso_order_shadow_delete` SET `before_order_state` = CAST(`before_main_status` AS SIGNED) 
WHERE `before_order_state` IS NULL AND `before_main_status` IS NOT NULL;

-- 8. 删除旧字段（可选：先保留一段时间再删除，这里先注释）
-- ALTER TABLE `vso_order` DROP COLUMN `main_status`;
-- ALTER TABLE `vso_order` DROP COLUMN `previous_main_status`;
-- ALTER TABLE `vso_order_shadow_delete` DROP COLUMN `before_main_status`;