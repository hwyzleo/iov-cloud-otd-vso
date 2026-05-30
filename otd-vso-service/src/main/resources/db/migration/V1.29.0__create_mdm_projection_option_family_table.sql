-- MDM OptionFamily 投影表
CREATE TABLE IF NOT EXISTS `mdm_projection_option_family` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `option_family_code` VARCHAR(50) NOT NULL COMMENT 'OptionFamily 编码',
    `option_family_name` VARCHAR(255) DEFAULT NULL COMMENT 'OptionFamily 名称',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_option_family_code` (`option_family_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MDM OptionFamily 投影';
