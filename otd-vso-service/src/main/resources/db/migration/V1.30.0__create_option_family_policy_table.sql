-- ============================================================
-- 新增 vso_sale_model_option_family_policy 表：OptionFamily 销售策略
-- 支持按销售车型设置 OptionFamily 的营销标题、图片、描述
-- ============================================================
CREATE TABLE IF NOT EXISTS `vso_sale_model_option_family_policy` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `sale_model_code` VARCHAR(50) NOT NULL COMMENT '销售车型编码',
    `option_family_code` VARCHAR(50) NOT NULL COMMENT 'OptionFamily 编码',
    `marketing_title` VARCHAR(255) DEFAULT NULL COMMENT '营销标题',
    `marketing_image` VARCHAR(500) DEFAULT NULL COMMENT '营销图片',
    `marketing_desc` TEXT DEFAULT NULL COMMENT '营销描述',
    `sort_weight` INT DEFAULT 0 COMMENT '排序权重',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` VARCHAR(64) DEFAULT '' COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    `description` VARCHAR(500) DEFAULT '' COMMENT '描述',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sale_family` (`sale_model_code`, `option_family_code`),
    KEY `idx_sale_model_code` (`sale_model_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OptionFamily 销售策略';
