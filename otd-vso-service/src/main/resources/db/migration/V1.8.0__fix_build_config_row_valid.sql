-- 修复已存在的数据：将row_valid设置为true
UPDATE `tb_sale_model_build_config` SET `row_valid` = 1 WHERE `row_valid` IS NULL OR `row_valid` = 0;

-- 验证修复结果
SELECT id, sale_code, build_config_code, enable, sort, row_valid FROM `tb_sale_model_build_config`;