-- ============================================================
-- VSO 车辆销售订单系统 - 重构订单门店归属字段（幂等版本）
-- Flyway 版本：V1.21.0
-- ============================================================

SET NAMES utf8mb4;

-- ============================================================
-- 一、修改 vso_order 表（幂等：检查字段是否存在再执行）
-- ============================================================

SET @dbname = DATABASE();
SET @tablename = 'vso_order';

-- 如果 region_code 存在，重命名为 owner_region_code
SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'region_code') > 0,
  'ALTER TABLE vso_order CHANGE COLUMN region_code owner_region_code varchar(64) DEFAULT NULL COMMENT ''归属区域编码''',
  'SELECT 1'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 如果 store_code 存在，重命名为 order_store_code
SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'store_code') > 0,
  'ALTER TABLE vso_order CHANGE COLUMN store_code order_store_code varchar(64) DEFAULT NULL COMMENT ''下单门店编码''',
  'SELECT 1'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 如果 owner_store_code 不存在，新增
SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'owner_store_code') = 0,
  'ALTER TABLE vso_order ADD COLUMN owner_store_code varchar(64) DEFAULT NULL COMMENT ''归属门店编码''',
  'SELECT 1'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 如果 delivery_store_code 不存在，新增
SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'delivery_store_code') = 0,
  'ALTER TABLE vso_order ADD COLUMN delivery_store_code varchar(64) DEFAULT NULL COMMENT ''交付门店编码''',
  'SELECT 1'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 如果 delivery_region_code 不存在，新增
SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'delivery_region_code') = 0,
  'ALTER TABLE vso_order ADD COLUMN delivery_region_code varchar(64) DEFAULT NULL COMMENT ''交付区域编码''',
  'SELECT 1'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 数据迁移：初始化归属门店编码
UPDATE vso_order SET owner_store_code = order_store_code WHERE owner_store_code IS NULL AND order_store_code IS NOT NULL;

-- ============================================================
-- 二、修改 vso_order_shadow_delete 表（如果存在）
-- ============================================================

SET @tablename = 'vso_order_shadow_delete';
SET @tableExists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename);

-- 如果表存在且 region_code 存在，重命名为 owner_region_code
SET @sql = (SELECT IF(
  @tableExists > 0 AND (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'region_code') > 0,
  'ALTER TABLE vso_order_shadow_delete CHANGE COLUMN region_code owner_region_code varchar(64) DEFAULT NULL COMMENT ''归属区域编码''',
  'SELECT 1'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 如果表存在且 store_code 存在，重命名为 order_store_code
SET @sql = (SELECT IF(
  @tableExists > 0 AND (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'store_code') > 0,
  'ALTER TABLE vso_order_shadow_delete CHANGE COLUMN store_code order_store_code varchar(64) DEFAULT NULL COMMENT ''下单门店编码''',
  'SELECT 1'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 如果表存在且 owner_store_code 不存在，新增
SET @sql = (SELECT IF(
  @tableExists > 0 AND (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'owner_store_code') = 0,
  'ALTER TABLE vso_order_shadow_delete ADD COLUMN owner_store_code varchar(64) DEFAULT NULL COMMENT ''归属门店编码''',
  'SELECT 1'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 如果表存在且 delivery_store_code 不存在，新增
SET @sql = (SELECT IF(
  @tableExists > 0 AND (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'delivery_store_code') = 0,
  'ALTER TABLE vso_order_shadow_delete ADD COLUMN delivery_store_code varchar(64) DEFAULT NULL COMMENT ''交付门店编码''',
  'SELECT 1'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 如果表存在且 delivery_region_code 不存在，新增
SET @sql = (SELECT IF(
  @tableExists > 0 AND (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'delivery_region_code') = 0,
  'ALTER TABLE vso_order_shadow_delete ADD COLUMN delivery_region_code varchar(64) DEFAULT NULL COMMENT ''交付区域编码''',
  'SELECT 1'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;