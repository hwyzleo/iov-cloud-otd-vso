-- ============================================================
-- VSO 车辆销售订单系统 - 清理 V1.16.0 遗留孤儿唯一索引
-- Flyway 版本：V1.40.0
--
-- 背景（OTD-VSO-DSN-CR-002 实施中发现）：
--   V1.16.0 删除 vso_order.small_order_no 字段后，部分环境仍残留
--   uk_small_order_no_valid 索引，且该索引退化为仅包含 (row_valid)。
--   该残留索引等价于对 row_valid 建唯一约束，导致全库只能存在一条
--   row_valid=1 的订单，直接阻断 C 端购车多订单链路。
-- ============================================================

SET NAMES utf8mb4;

ALTER TABLE `vso_order` DROP INDEX `uk_small_order_no_valid`;
