-- 为 vso_order 表添加付款方式字段

ALTER TABLE `vso_order` 
ADD COLUMN `payment_method` VARCHAR(32) DEFAULT NULL COMMENT '付款方式：full_payment-全款，loan-贷款' 
AFTER `customer_type`;

-- 为 vso_order 表添加上牌城市字段

ALTER TABLE `vso_order` 
ADD COLUMN `license_city` VARCHAR(64) DEFAULT NULL COMMENT '上牌城市编码' 
AFTER `payment_method`;