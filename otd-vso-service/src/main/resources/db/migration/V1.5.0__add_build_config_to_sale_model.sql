-- 为销售车型表增加生产配置代码字段
ALTER TABLE `tb_sale_model` ADD COLUMN `build_config_code` VARCHAR(50) DEFAULT NULL COMMENT '生产配置代码' AFTER `sale_code`;

-- 为销售车型表增加基础车型代码字段
ALTER TABLE `tb_sale_model` ADD COLUMN `base_model_code` VARCHAR(50) DEFAULT NULL COMMENT '基础车型代码' AFTER `build_config_code`;

-- 为生产配置代码字段添加索引
ALTER TABLE `tb_sale_model` ADD INDEX `idx_build_config_code` (`build_config_code`);