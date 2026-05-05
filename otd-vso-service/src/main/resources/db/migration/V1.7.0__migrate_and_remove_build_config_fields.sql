-- 数据迁移：将原有的build_config_code迁移到关联表
INSERT INTO `tb_sale_model_build_config` (`sale_code`, `build_config_code`, `enable`, `sort`, `create_by`, `modify_by`)
SELECT `sale_code`, `build_config_code`, 1, 0, `create_by`, `modify_by`
FROM `tb_sale_model`
WHERE `build_config_code` IS NOT NULL AND `build_config_code` != '';

-- 删除SaleModel表中不再需要的字段
ALTER TABLE `tb_sale_model` DROP COLUMN `build_config_code`;
ALTER TABLE `tb_sale_model` DROP COLUMN `base_model_code`;