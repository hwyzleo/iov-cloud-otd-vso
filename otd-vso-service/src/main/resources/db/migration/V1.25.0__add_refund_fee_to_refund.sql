-- V1.25.0: 添加退款手续费字段
ALTER TABLE `vso_refund` ADD COLUMN `refund_fee` DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '退款手续费' AFTER `refund_amount`;
