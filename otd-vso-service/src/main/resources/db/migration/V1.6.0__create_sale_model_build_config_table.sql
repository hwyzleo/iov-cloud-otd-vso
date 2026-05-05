-- 销售车型生产配置关联表
CREATE TABLE `tb_sale_model_build_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `sale_code` varchar(50) NOT NULL COMMENT '销售代码',
  `build_config_code` varchar(50) NOT NULL COMMENT '生产配置代码',
  `enable` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `sort` int DEFAULT '0' COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `modify_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `modify_by` varchar(64) DEFAULT '' COMMENT '修改者',
  `row_version` int DEFAULT '0' COMMENT '行版本',
  `row_valid` tinyint(1) DEFAULT '1' COMMENT '行有效',
  `description` varchar(500) DEFAULT '' COMMENT '描述',
  PRIMARY KEY (`id`),
  KEY `idx_sale_code` (`sale_code`),
  KEY `idx_build_config_code` (`build_config_code`),
  UNIQUE KEY `uk_sale_build_config` (`sale_code`, `build_config_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售车型生产配置关联表';