-- ============================================================
-- VSO 车辆销售订单系统 - 给订单主体关系表添加订购人类型和证件类型字段
-- Flyway 版本：V1.23.0
-- ============================================================

SET NAMES utf8mb4;

SET @dbname = DATABASE();
SET @tablename = 'vso_order_party';

-- 添加 person_type 字段（订购人类型：1-本人、2-代理人等）
SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'person_type') = 0,
  'ALTER TABLE vso_order_party ADD COLUMN person_type INT DEFAULT NULL COMMENT ''订购人类型：1-本人、2-代理人等''',
  'SELECT 1'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 id_type 字段（证件类型：1-身份证、2-护照、3-营业执照、4-组织机构代码等）
SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'id_type') = 0,
  'ALTER TABLE vso_order_party ADD COLUMN id_type INT DEFAULT NULL COMMENT ''证件类型：1-身份证、2-护照、3-营业执照、4-组织机构代码等''',
  'SELECT 1'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;