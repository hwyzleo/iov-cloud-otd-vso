-- ============================================================
-- VSO 车辆销售订单系统 - 字段名统一
-- Flyway 版本：V1.10.0
-- 描述：将 order_num 字段统一改为 order_no
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 修改 vso_order_log 表字段名
-- ============================================================

-- 修改字段名 order_num 为 order_no
ALTER TABLE `vso_order_log` 
CHANGE COLUMN `order_num` `order_no` VARCHAR(50) NOT NULL COMMENT '订单编码';

-- 修改索引名 idx_order_num 为 idx_order_no
ALTER TABLE `vso_order_log` 
DROP INDEX `idx_order_num`,
ADD INDEX `idx_order_no` (`order_no`) USING BTREE;

SET FOREIGN_KEY_CHECKS = 1;