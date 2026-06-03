-- 移除 vso_wishlist 表的遗留字段 sale_model 和 build_config_code
-- sale_model 已被 sale_model_code 替代（V1.28.0 MDM 对齐）
-- build_config_code 已被 configuration_code + option_codes 替代（V1.28.0 CR-010）

ALTER TABLE `vso_wishlist`
DROP INDEX `idx_sale_model`;

ALTER TABLE `vso_wishlist`
DROP COLUMN `sale_model`,
DROP COLUMN `build_config_code`;
