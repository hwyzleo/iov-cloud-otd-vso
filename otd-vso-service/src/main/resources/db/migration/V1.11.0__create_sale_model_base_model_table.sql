-- 销售车型基础车型关联表
CREATE TABLE `vso_sale_model_base_model` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `sale_code` varchar(50) NOT NULL COMMENT '销售代码',
  `base_model_code` varchar(50) NOT NULL COMMENT '基础车型代码',
  `base_model_name` varchar(255) DEFAULT NULL COMMENT '基础车型名称',
  `base_model_image` json DEFAULT NULL COMMENT '基础车型图片',
  `base_model_price` decimal(10,2) DEFAULT NULL COMMENT '基础车型价格',
  `base_model_desc` text DEFAULT NULL COMMENT '基础车型描述',
  `base_model_param` text DEFAULT NULL COMMENT '基础车型参数',
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
  KEY `idx_base_model_code` (`base_model_code`),
  UNIQUE KEY `uk_sale_base_model` (`sale_code`, `base_model_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售车型基础车型关联表';