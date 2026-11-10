-- ============================================================
-- VSO 车辆销售订单系统 - C 端购车全流程补全（OTD-VSO-DSN-CR-002）
-- Flyway 版本：V1.39.0
-- 说明：全部变更为增量新增，均有默认值或可空；不执行破坏性删除，不提供 down migration
-- ============================================================

SET NAMES utf8mb4;

-- ============================================================
-- 一、vso_order 订单主表
-- ============================================================

ALTER TABLE `vso_order`
    ADD COLUMN `down_payment_time` TIMESTAMP NULL DEFAULT NULL COMMENT '定金支付完成或意向金转定金完成时间' AFTER `lock_time`,
    ADD COLUMN `purchase_plan` INT DEFAULT NULL COMMENT '购买计划' AFTER `payment_method`,
    ADD COLUMN `delivery_person_id` VARCHAR(64) DEFAULT NULL COMMENT '交付人员ID' AFTER `vehicle_vin`,
    ADD COLUMN `delivery_person_name` VARCHAR(128) DEFAULT NULL COMMENT '交付人员姓名' AFTER `delivery_person_id`,
    ADD COLUMN `transport_apply_person_id` VARCHAR(64) DEFAULT NULL COMMENT '发运申请人ID' AFTER `delivery_finish_time`,
    ADD COLUMN `transport_apply_person_name` VARCHAR(128) DEFAULT NULL COMMENT '发运申请人姓名' AFTER `transport_apply_person_id`,
    ADD COLUMN `transport_apply_time` TIMESTAMP NULL DEFAULT NULL COMMENT '发运申请时间' AFTER `transport_apply_person_name`;

ALTER TABLE `vso_order`
    ADD INDEX `idx_order_auto_lock` (`order_state`, `down_payment_time`, `row_valid`);

-- ============================================================
-- 二、vso_order_amount 订单金额表
-- ============================================================

ALTER TABLE `vso_order_amount`
    ADD COLUMN `upgraded_from_small` TINYINT NOT NULL DEFAULT 0 COMMENT '是否由小定升级且存在超额金额' AFTER `calculation_version`;

-- ============================================================
-- 三、vso_payment 支付单过期时间
-- ============================================================

ALTER TABLE `vso_payment`
    ADD COLUMN `expire_time` TIMESTAMP NULL DEFAULT NULL COMMENT '支付单待支付截止时间' AFTER `pay_time`,
    ADD INDEX `idx_payment_active` (`order_id`, `payment_stage`, `payment_status`, `expire_time`, `row_valid`);

-- ============================================================
-- 四、vso_order_party 用户订单关系索引
-- ============================================================

ALTER TABLE `vso_order_party`
    ADD INDEX `idx_order_party_user_role` (`user_id`, `party_role`, `row_valid`, `order_id`);

-- ============================================================
-- 五、vso_purchase_agreement 购车协议模板表
-- ============================================================

CREATE TABLE IF NOT EXISTS `vso_purchase_agreement` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `agreement_id` VARCHAR(64) NOT NULL COMMENT '协议业务 ID',
    `agreement_code` VARCHAR(64) NOT NULL COMMENT '协议代码',
    `sale_model_code` VARCHAR(64) NOT NULL DEFAULT 'GLOBAL' COMMENT '销售车型编码，GLOBAL 表示全局协议',
    `version_no` INT NOT NULL COMMENT '协议版本号',
    `title` VARCHAR(256) NOT NULL COMMENT '协议标题',
    `intro` TEXT COMMENT '协议简介',
    `detail` LONGTEXT NOT NULL COMMENT '协议详情',
    `agreement_status` VARCHAR(32) NOT NULL COMMENT '协议状态：draft-草稿，active-生效，off_shelf-下架',
    `effective_from` TIMESTAMP NULL DEFAULT NULL COMMENT '生效开始时间',
    `effective_to` TIMESTAMP NULL DEFAULT NULL COMMENT '生效结束时间',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT NOT NULL DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT NOT NULL DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_purchase_agreement_id` (`agreement_id`),
    UNIQUE KEY `uk_purchase_agreement_version` (`sale_model_code`, `agreement_code`, `version_no`, `row_valid`),
    KEY `idx_purchase_agreement_query` (`sale_model_code`, `agreement_code`, `agreement_status`, `effective_from`, `effective_to`, `row_valid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='C端购车协议模板表';

-- ============================================================
-- 六、vso_order_agreement_confirmation 订单协议确认快照表
-- ============================================================

CREATE TABLE IF NOT EXISTS `vso_order_agreement_confirmation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `confirmation_id` VARCHAR(64) NOT NULL COMMENT '确认记录业务 ID',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `agreement_id` VARCHAR(64) NOT NULL COMMENT '协议业务 ID',
    `agreement_code` VARCHAR(64) NOT NULL COMMENT '协议代码',
    `version_no` INT NOT NULL COMMENT '协议版本号',
    `title_snapshot` VARCHAR(256) NOT NULL COMMENT '协议标题快照',
    `intro_snapshot` TEXT COMMENT '协议简介快照',
    `detail_snapshot` LONGTEXT NOT NULL COMMENT '协议详情快照',
    `confirm_time` TIMESTAMP NOT NULL COMMENT '确认时间',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT NOT NULL DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT NOT NULL DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_agreement_confirmation_id` (`confirmation_id`),
    UNIQUE KEY `uk_order_agreement_confirmation` (`order_id`, `agreement_id`, `row_valid`),
    KEY `idx_order_agreement_code` (`order_id`, `agreement_code`, `row_valid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单协议确认快照表';

-- ============================================================
-- 七、vso_refund_item 退款原支付明细表
-- ============================================================

CREATE TABLE IF NOT EXISTS `vso_refund_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `refund_item_id` VARCHAR(64) NOT NULL COMMENT '退款明细业务 ID',
    `refund_id` VARCHAR(64) NOT NULL COMMENT '退款主单业务 ID',
    `payment_id` VARCHAR(64) NOT NULL COMMENT '原支付业务 ID',
    `payment_no` VARCHAR(64) NOT NULL COMMENT '原支付单号',
    `refund_amount` DECIMAL(18,2) NOT NULL COMMENT '分摊到原支付单的退款金额',
    `refund_status` VARCHAR(32) NOT NULL COMMENT '退款明细状态：pending/processing/completed/failed',
    `external_refund_no` VARCHAR(64) DEFAULT NULL COMMENT '外部退款单号',
    `fail_reason` VARCHAR(255) DEFAULT NULL COMMENT '失败原因',
    `refund_time` TIMESTAMP NULL DEFAULT NULL COMMENT '明细退款完成时间',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT NOT NULL DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT NOT NULL DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_refund_item_id` (`refund_item_id`),
    UNIQUE KEY `uk_refund_payment` (`refund_id`, `payment_id`, `row_valid`),
    KEY `idx_refund_item_status` (`refund_id`, `refund_status`, `row_valid`),
    KEY `idx_refund_item_payment` (`payment_id`, `refund_status`, `row_valid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款原支付明细表';
