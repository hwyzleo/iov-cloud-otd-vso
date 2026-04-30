-- ============================================================
-- VSO 车辆销售订单系统 - 配置表
-- Flyway 版本：V1.4.0
-- 描述：创建超时配置、车源占用有效期配置表并初始化数据
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 超时任务配置表
CREATE TABLE IF NOT EXISTS `vso_config_timeout` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `config_key` VARCHAR(64) NOT NULL COMMENT '配置键',
    `config_name` VARCHAR(128) NOT NULL COMMENT '配置名称',
    `threshold_minutes` INT NOT NULL COMMENT '超时阈值分钟数',
    `trigger_strategy` VARCHAR(32) NOT NULL COMMENT '触发策略',
    `enable_flag` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='超时任务配置表';

-- 车源占用有效期配置表
CREATE TABLE IF NOT EXISTS `vso_config_vehicle_occupancy` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `occupancy_rule_key` VARCHAR(64) NOT NULL COMMENT '规则键',
    `model_code` VARCHAR(64) DEFAULT NULL COMMENT '车型编码',
    `order_type` VARCHAR(32) DEFAULT NULL COMMENT '订单类型',
    `store_code` VARCHAR(64) DEFAULT NULL COMMENT '门店编码',
    `region_code` VARCHAR(64) DEFAULT NULL COMMENT '区域编码',
    `activity_type` VARCHAR(32) DEFAULT NULL COMMENT '活动类型',
    `customer_type` VARCHAR(32) DEFAULT NULL COMMENT '客户类型',
    `finance_order_flag` TINYINT DEFAULT NULL COMMENT '是否金融订单',
    `special_approval_flag` TINYINT DEFAULT NULL COMMENT '是否特殊审批订单',
    `occupancy_hours` INT NOT NULL COMMENT '占用时长小时数',
    `enable_flag` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
    `priority` INT NOT NULL DEFAULT 0 COMMENT '优先级',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_occupancy_rule_key` (`occupancy_rule_key`),
    KEY `idx_occupancy_search` (`model_code`, `order_type`, `store_code`, `region_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车源占用有效期配置表';

-- ============================================================
-- 初始化配置数据
-- ============================================================

-- 初始化超时配置
INSERT INTO `vso_config_timeout` (`config_key`, `config_name`, `threshold_minutes`, `trigger_strategy`, `remark`) VALUES
('small_order_pay_timeout', '小订单待支付意向金超时', 30, 'invalid', '小订单待支付意向金超时自动失效'),
('formal_order_audit_timeout', '正式订单待审核超时', 1440, 'remind', '正式订单待审核超时仅提醒'),
('deposit_timeout', '正式订单定金待支付超时', 60, 'close', '定金待支付超时自动关闭订单'),
('contract_sign_timeout', '合同待签署超时', 2880, 'remind', '合同待签署超时仅提醒')
ON DUPLICATE KEY UPDATE `config_name` = VALUES(`config_name`);

-- 初始化车源占用有效期配置（通用规则）
INSERT INTO `vso_config_vehicle_occupancy` (`occupancy_rule_key`, `occupancy_hours`, `enable_flag`, `priority`, `remark`) VALUES
('GENERAL_RULE', 24, 1, 0, '通用车源占用有效期规则：24 小时')
ON DUPLICATE KEY UPDATE `occupancy_hours` = VALUES(`occupancy_hours`);

SET FOREIGN_KEY_CHECKS = 1;
