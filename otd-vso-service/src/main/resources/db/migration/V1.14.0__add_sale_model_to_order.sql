-- 为 vso_order 表添加销售车型字段
ALTER TABLE `vso_order` 
ADD COLUMN `sale_model` VARCHAR(64) DEFAULT NULL COMMENT '销售车型编码' AFTER `brand_code`;

-- 为销售车型添加索引
ALTER TABLE `vso_order` 
ADD INDEX `idx_sale_model` (`sale_model`);

-- 将 vso_wishlist 表的 sale_code 字段改名为 sale_model
ALTER TABLE `vso_wishlist` 
CHANGE COLUMN `sale_code` `sale_model` VARCHAR(64) NOT NULL COMMENT '销售车型编码';

-- 将索引名从 idx_sale_code 改为 idx_sale_model
ALTER TABLE `vso_wishlist` 
DROP INDEX `idx_sale_code`,
ADD INDEX `idx_sale_model` (`sale_model`);