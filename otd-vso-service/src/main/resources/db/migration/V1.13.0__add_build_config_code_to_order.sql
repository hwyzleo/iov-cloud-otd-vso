-- 为订单主表添加生产配置代码字段

ALTER TABLE `vso_order` 
ADD COLUMN `build_config_code` VARCHAR(64) DEFAULT NULL COMMENT '生产配置编码' AFTER `vehicle_vin`;

-- 添加索引以便快速查询
ALTER TABLE `vso_order` 
ADD INDEX `idx_build_config_code` (`build_config_code`);