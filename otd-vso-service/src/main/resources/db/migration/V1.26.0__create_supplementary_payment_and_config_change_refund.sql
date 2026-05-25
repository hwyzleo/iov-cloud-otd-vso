-- V1.26.0: 创建改配补款和退款记录表

-- 改配补款记录表
CREATE TABLE IF NOT EXISTS `vso_supplementary_payment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `supplementary_no` VARCHAR(64) NOT NULL COMMENT '补款单号',
    `order_id` VARCHAR(64) NOT NULL COMMENT '关联订单ID',
    `supplementary_amount` DECIMAL(18,2) NOT NULL COMMENT '补款金额',
    `currency` VARCHAR(10) NOT NULL DEFAULT 'CNY' COMMENT '币种',
    `supplementary_status` VARCHAR(32) NOT NULL COMMENT '补款状态: pending/completed/cancelled/expired',
    `config_version_no` INT NOT NULL COMMENT '触发补款的配置版本号',
    `payment_id` VARCHAR(64) DEFAULT NULL COMMENT '关联支付ID',
    `expire_time` DATETIME NOT NULL COMMENT '补款过期时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_supplementary_no` (`supplementary_no`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_order_status` (`order_id`, `supplementary_status`),
    KEY `idx_expire_time` (`supplementary_status`, `expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='改配补款记录表';

-- 改配退款记录表
CREATE TABLE IF NOT EXISTS `vso_config_change_refund` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `refund_task_no` VARCHAR(64) NOT NULL COMMENT '退款任务单号',
    `order_id` VARCHAR(64) NOT NULL COMMENT '关联订单ID',
    `refund_amount` DECIMAL(18,2) NOT NULL COMMENT '退款金额',
    `currency` VARCHAR(10) NOT NULL DEFAULT 'CNY' COMMENT '币种',
    `refund_status` VARCHAR(32) NOT NULL COMMENT '退款状态: pending/processing/completed/failed',
    `config_version_no` INT NOT NULL COMMENT '触发退款的配置版本号',
    `refund_id` VARCHAR(64) DEFAULT NULL COMMENT '关联退款记录ID',
    `fail_reason` VARCHAR(255) DEFAULT NULL COMMENT '退款失败原因',
    `manual_audit_status` VARCHAR(32) DEFAULT NULL COMMENT '人工审核状态: pending/approved/rejected',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_refund_task_no` (`refund_task_no`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_order_status` (`order_id`, `refund_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='改配退款记录表';
