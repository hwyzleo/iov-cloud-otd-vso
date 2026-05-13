-- V1.16.0__remove_small_order_no.sql
-- 移除 small_order_no 字段，统一使用 order_no

-- 1. 将现有小订单的 small_order_no 复制到 order_no（如果 order_no 为空）
UPDATE vso_order 
SET order_no = small_order_no 
WHERE order_no IS NULL 
  AND small_order_no IS NOT NULL 
  AND row_valid = 1;

-- 2. 删除 vso_order 表的 small_order_no 字段（会自动删除相关索引）
ALTER TABLE vso_order DROP COLUMN small_order_no;

-- 3. 删除 vso_order_shadow_delete 表的 origin_small_order_no 字段
ALTER TABLE vso_order_shadow_delete DROP COLUMN origin_small_order_no;

-- 4. 删除小订单转正式订单转化关系表（不再需要，转化通过 order_type 变更记录）
DROP TABLE IF EXISTS vso_order_transform;
