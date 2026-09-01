-- V1.9.0 统一表前缀为 vso_，清理废弃表
-- 1. 重命名 tb_ 前缀表为 vso_ 前缀（tb_order_log 为旧系统遗留表，存在才重命名，缺失则兜底建表）
-- 2. 删除 4 张已被 vso_ 表替代的废弃表
-- 3. 新增缺失的 vso_ 表

-- ============================================================
-- 一、重命名 tb_ 表为 vso_ 前缀
-- ============================================================

RENAME TABLE `tb_sale_model` TO `vso_sale_model`;
RENAME TABLE `tb_sale_model_config` TO `vso_sale_model_config`;
RENAME TABLE `tb_sale_model_build_config` TO `vso_sale_model_build_config`;
RENAME TABLE `tb_purchase_benefits` TO `vso_purchase_benefits`;

-- tb_order_log 是旧系统遗留表，全新数据库中不存在该表，
-- 若直接 RENAME 会在全新建库时失败（Error 1146）。
-- 存在才重命名，不存在则跳过，由下方 CREATE TABLE IF NOT EXISTS 兜底建表。
SET @legacy_order_log_exists = (
    SELECT COUNT(*) FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'tb_order_log'
);
SET @rename_order_log_sql = IF(@legacy_order_log_exists > 0,
    'RENAME TABLE `tb_order_log` TO `vso_order_log`',
    'SELECT 1');
PREPARE rename_order_log_stmt FROM @rename_order_log_sql;
EXECUTE rename_order_log_stmt;
DEALLOCATE PREPARE rename_order_log_stmt;

-- 订单日志表：旧系统遗留表 tb_order_log 缺失时兜底创建（全新建库场景），
-- 字段与 OrderLogPo/BasePo 对齐；order_num 字段由 V1.10.0 统一改名为 order_no。
CREATE TABLE IF NOT EXISTS `vso_order_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_num` varchar(50) NOT NULL COMMENT '订单编码',
  `operate_client` varchar(64) DEFAULT NULL COMMENT '操作终端',
  `account_id` varchar(64) DEFAULT NULL COMMENT '操作者 ID',
  `operator` varchar(64) DEFAULT NULL COMMENT '操作者',
  `operate_type` varchar(32) DEFAULT NULL COMMENT '操作类型',
  `operate_desc` varchar(255) DEFAULT NULL COMMENT '操作描述',
  `operate_time` timestamp NULL DEFAULT NULL COMMENT '操作时间',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `modify_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `modify_by` bigint DEFAULT NULL COMMENT '修改者',
  `row_version` int DEFAULT '0' COMMENT '记录版本',
  `row_valid` tinyint DEFAULT '1' COMMENT '是否有效',
  PRIMARY KEY (`id`),
  KEY `idx_order_num` (`order_num`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='车辆销售订单日志表';

-- ============================================================
-- 二、删除已被 vso_ 表替代的废弃表
-- ============================================================

-- tb_order 已被 vso_order 替代
DROP TABLE IF EXISTS `tb_order`;

-- tb_order_payment 已被 vso_payment 替代
DROP TABLE IF EXISTS `tb_order_payment`;

-- tb_order_model_config 已被 vso_order_vehicle_snapshot 替代
DROP TABLE IF EXISTS `tb_order_model_config`;

-- tb_purchase_agreement 已被 vso_contract 替代
DROP TABLE IF EXISTS `tb_purchase_agreement`;

-- ============================================================
-- 三、新增缺失的 vso_ 表
-- ============================================================

-- 小订单转正式订单转化关系表
CREATE TABLE IF NOT EXISTS `vso_order_transform` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `transform_id` varchar(64) NOT NULL COMMENT '转化业务 ID',
  `small_order_id` varchar(64) NOT NULL COMMENT '小订单业务 ID',
  `small_order_no` varchar(64) NOT NULL COMMENT '小订单号',
  `formal_order_id` varchar(64) NOT NULL COMMENT '正式订单业务 ID',
  `formal_order_no` varchar(64) NOT NULL COMMENT '正式订单号',
  `transform_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '转化时间',
  `transform_user_id` varchar(64) NOT NULL COMMENT '转化操作人 ID',
  `transform_user_role` varchar(32) NOT NULL COMMENT '转化操作人角色',
  `transform_scene` varchar(32) NOT NULL DEFAULT 'normal' COMMENT '转化场景',
  `transform_remark` varchar(255) DEFAULT NULL COMMENT '转化备注',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `modify_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `modify_by` bigint DEFAULT NULL COMMENT '修改者',
  `row_version` int DEFAULT '0' COMMENT '记录版本',
  `row_valid` tinyint DEFAULT '1' COMMENT '是否有效',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_transform_id` (`transform_id`),
  UNIQUE KEY `uk_small_order_transform` (`small_order_id`),
  KEY `idx_formal_order_transform` (`formal_order_id`),
  KEY `idx_transform_time` (`transform_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='小订单转正式订单转化关系表';

-- 订单版本差异表
CREATE TABLE IF NOT EXISTS `vso_order_version_diff` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_version_diff_id` varchar(64) NOT NULL COMMENT '差异业务 ID',
  `order_version_id` varchar(64) NOT NULL COMMENT '版本业务 ID',
  `field_name` varchar(128) NOT NULL COMMENT '字段名',
  `field_label` varchar(128) DEFAULT NULL COMMENT '字段展示名',
  `before_value` text COMMENT '变更前值',
  `after_value` text COMMENT '变更后值',
  `change_category` varchar(32) DEFAULT NULL COMMENT '差异类别',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `modify_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `modify_by` bigint DEFAULT NULL COMMENT '修改者',
  `row_version` int DEFAULT '0' COMMENT '记录版本',
  `row_valid` tinyint DEFAULT '1' COMMENT '是否有效',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_version_diff_id` (`order_version_diff_id`),
  KEY `idx_order_version_field` (`order_version_id`,`field_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单版本差异表';
