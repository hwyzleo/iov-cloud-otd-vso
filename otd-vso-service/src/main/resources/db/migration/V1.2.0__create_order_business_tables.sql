-- ============================================================
-- VSO 车辆销售订单系统 - 订单业务流程表
-- Flyway 版本：V1.2.0
-- 描述：创建审批、配车、合同、支付、退款、金融、补贴、交付、上牌、发票表
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 订单资料主表
CREATE TABLE IF NOT EXISTS `vso_order_material` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `material_id` VARCHAR(64) NOT NULL COMMENT '资料业务 ID',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `material_type` VARCHAR(32) NOT NULL COMMENT '资料类型',
    `material_name` VARCHAR(128) NOT NULL COMMENT '资料名称',
    `material_status` VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '资料状态',
    `current_version_no` INT NOT NULL DEFAULT 1 COMMENT '当前资料版本号',
    `submitter_role` VARCHAR(32) DEFAULT NULL COMMENT '提交角色',
    `submitter_id` VARCHAR(64) DEFAULT NULL COMMENT '提交人 ID',
    `review_comment` VARCHAR(255) DEFAULT NULL COMMENT '最近一次审核意见',
    `timeout_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否超时',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_material_id` (`material_id`),
    KEY `idx_order_material_type` (`order_id`, `material_type`),
    KEY `idx_material_status` (`material_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单资料主表';

-- 订单资料版本表
CREATE TABLE IF NOT EXISTS `vso_order_material_version` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `material_version_id` VARCHAR(64) NOT NULL COMMENT '资料版本业务 ID',
    `material_id` VARCHAR(64) NOT NULL COMMENT '资料业务 ID',
    `version_no` INT NOT NULL COMMENT '版本号',
    `file_url` VARCHAR(512) DEFAULT NULL COMMENT '文件地址',
    `file_name` VARCHAR(255) DEFAULT NULL COMMENT '文件名称',
    `file_type` VARCHAR(64) DEFAULT NULL COMMENT '文件类型',
    `file_size` BIGINT DEFAULT NULL COMMENT '文件大小 (字节)',
    `submit_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    `review_status` VARCHAR(32) DEFAULT NULL COMMENT '审核状态',
    `review_time` TIMESTAMP NULL DEFAULT NULL COMMENT '审核时间',
    `reviewer_id` VARCHAR(64) DEFAULT NULL COMMENT '审核人 ID',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_material_version_id` (`material_version_id`),
    UNIQUE KEY `uk_material_version_no_valid` (`material_id`, `version_no`, `row_valid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单资料版本表';

-- 审批单主表
CREATE TABLE IF NOT EXISTS `vso_approval` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `approval_id` VARCHAR(64) NOT NULL COMMENT '审批业务 ID',
    `approval_no` VARCHAR(64) NOT NULL COMMENT '审批单号',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `approval_type` VARCHAR(32) NOT NULL COMMENT '审批类型',
    `approval_status` VARCHAR(32) NOT NULL COMMENT '审批状态',
    `apply_user_id` VARCHAR(64) NOT NULL COMMENT '申请人 ID',
    `apply_role` VARCHAR(32) NOT NULL COMMENT '申请人角色',
    `apply_reason` VARCHAR(255) DEFAULT NULL COMMENT '申请原因',
    `change_snapshot` TEXT DEFAULT NULL COMMENT '变更快照',
    `current_node_no` INT DEFAULT NULL COMMENT '当前审批节点序号',
    `executor_result` VARCHAR(32) DEFAULT NULL COMMENT '执行结果',
    `submit_time` TIMESTAMP NULL DEFAULT NULL COMMENT '提单时间',
    `finish_time` TIMESTAMP NULL DEFAULT NULL COMMENT '完成时间',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_approval_id` (`approval_id`),
    UNIQUE KEY `uk_approval_no_valid` (`approval_no`, `row_valid`),
    KEY `idx_order_approval_status` (`order_id`, `approval_status`),
    KEY `idx_apply_user_status` (`apply_user_id`, `approval_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批单主表';

-- 审批流转记录表
CREATE TABLE IF NOT EXISTS `vso_approval_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `approval_record_id` VARCHAR(64) NOT NULL COMMENT '审批记录业务 ID',
    `approval_id` VARCHAR(64) NOT NULL COMMENT '审批业务 ID',
    `node_no` INT NOT NULL COMMENT '节点序号',
    `approve_type` VARCHAR(32) NOT NULL COMMENT '处理动作',
    `approver_id` VARCHAR(64) DEFAULT NULL COMMENT '审批人 ID',
    `approver_role` VARCHAR(32) DEFAULT NULL COMMENT '审批人角色',
    `approve_comment` VARCHAR(255) DEFAULT NULL COMMENT '审批意见',
    `attachment_url` VARCHAR(512) DEFAULT NULL COMMENT '审批附件地址',
    `operate_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    `timeout_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否超时处理',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_approval_record_id` (`approval_record_id`),
    KEY `idx_approval_node` (`approval_id`, `node_no`),
    KEY `idx_approver_time` (`approver_id`, `operate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批流转记录表';

-- 配车与车辆绑定表
CREATE TABLE IF NOT EXISTS `vso_vehicle_assignment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `vehicle_assignment_id` VARCHAR(64) NOT NULL COMMENT '配车业务 ID',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `assignment_type` VARCHAR(32) NOT NULL COMMENT '动作类型',
    `vehicle_source_type` VARCHAR(32) DEFAULT NULL COMMENT '车源类型',
    `vin` VARCHAR(32) DEFAULT NULL COMMENT 'VIN',
    `vehicle_id` VARCHAR(64) DEFAULT NULL COMMENT '车辆业务 ID',
    `assign_status` VARCHAR(32) NOT NULL COMMENT '配车状态',
    `manual_assign_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否人工指定',
    `manual_assign_reason` VARCHAR(255) DEFAULT NULL COMMENT '人工指定原因',
    `unbind_reason` VARCHAR(255) DEFAULT NULL COMMENT '解绑原因',
    `occupy_expire_time` TIMESTAMP NULL DEFAULT NULL COMMENT '占用到期时间',
    `assign_time` TIMESTAMP NULL DEFAULT NULL COMMENT '配车时间',
    `bind_time` TIMESTAMP NULL DEFAULT NULL COMMENT '绑车时间',
    `release_time` TIMESTAMP NULL DEFAULT NULL COMMENT '释放车源时间',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_vehicle_assignment_id` (`vehicle_assignment_id`),
    KEY `idx_order_assign_status` (`order_id`, `assign_status`),
    KEY `idx_vin` (`vin`),
    KEY `idx_occupy_expire_time` (`occupy_expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配车与车辆绑定表';

-- 合同/协议/授权文件表
CREATE TABLE IF NOT EXISTS `vso_contract` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `contract_id` VARCHAR(64) NOT NULL COMMENT '合同业务 ID',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `contract_no` VARCHAR(64) NOT NULL COMMENT '合同号',
    `contract_type` VARCHAR(32) NOT NULL COMMENT '合同类型',
    `version_no` INT NOT NULL DEFAULT 1 COMMENT '版本号',
    `contract_status` VARCHAR(32) NOT NULL DEFAULT 'draft' COMMENT '合同状态',
    `sign_mode` VARCHAR(32) DEFAULT NULL COMMENT '签署方式',
    `sign_effective_rule` VARCHAR(32) DEFAULT NULL COMMENT '生效规则',
    `file_url` VARCHAR(512) DEFAULT NULL COMMENT '合同文件地址',
    `external_contract_no` VARCHAR(64) DEFAULT NULL COMMENT '外部电子签合同号',
    `generate_time` TIMESTAMP NULL DEFAULT NULL COMMENT '生成时间',
    `sign_time` TIMESTAMP NULL DEFAULT NULL COMMENT '签署时间',
    `effective_time` TIMESTAMP NULL DEFAULT NULL COMMENT '生效时间',
    `invalid_time` TIMESTAMP NULL DEFAULT NULL COMMENT '失效时间',
    `invalid_reason` VARCHAR(255) DEFAULT NULL COMMENT '失效原因',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_contract_id` (`contract_id`),
    KEY `idx_order_contract_type` (`order_id`, `contract_type`),
    UNIQUE KEY `uk_order_contract_type_version_valid` (`order_id`, `contract_type`, `version_no`, `row_valid`),
    KEY `idx_external_contract_no` (`external_contract_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同/协议/授权文件表';

-- 支付记录表
CREATE TABLE IF NOT EXISTS `vso_payment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `payment_id` VARCHAR(64) NOT NULL COMMENT '支付业务 ID',
    `payment_no` VARCHAR(64) NOT NULL COMMENT '支付单号',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `payment_stage` VARCHAR(32) NOT NULL COMMENT '支付阶段',
    `payment_channel` VARCHAR(32) NOT NULL COMMENT '支付渠道',
    `payment_amount` DECIMAL(18,2) NOT NULL COMMENT '支付金额',
    `payment_status` VARCHAR(32) NOT NULL COMMENT '支付状态',
    `initiator_role` VARCHAR(32) DEFAULT NULL COMMENT '发起角色',
    `initiator_id` VARCHAR(64) DEFAULT NULL COMMENT '发起人 ID',
    `authorized_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否存在授权依据',
    `authorized_proof_type` VARCHAR(64) DEFAULT NULL COMMENT '授权依据类型',
    `external_trade_no` VARCHAR(64) DEFAULT NULL COMMENT '外部交易单号',
    `pay_time` TIMESTAMP NULL DEFAULT NULL COMMENT '支付成功时间',
    `fail_reason` VARCHAR(255) DEFAULT NULL COMMENT '失败原因',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_payment_id` (`payment_id`),
    UNIQUE KEY `uk_payment_no_valid` (`payment_no`, `row_valid`),
    KEY `idx_order_stage_status` (`order_id`, `payment_stage`, `payment_status`),
    KEY `idx_external_trade_no` (`external_trade_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

-- 退款记录表
CREATE TABLE IF NOT EXISTS `vso_refund` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `refund_id` VARCHAR(64) NOT NULL COMMENT '退款业务 ID',
    `refund_no` VARCHAR(64) NOT NULL COMMENT '退款单号',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `payment_id` VARCHAR(64) DEFAULT NULL COMMENT '关联支付业务 ID',
    `refund_scene` VARCHAR(32) NOT NULL COMMENT '退款场景',
    `refund_amount` DECIMAL(18,2) NOT NULL COMMENT '退款金额',
    `refund_status` VARCHAR(32) NOT NULL COMMENT '退款状态',
    `approval_id` VARCHAR(64) DEFAULT NULL COMMENT '关联审批业务 ID',
    `external_refund_no` VARCHAR(64) DEFAULT NULL COMMENT '外部退款单号',
    `apply_time` TIMESTAMP NULL DEFAULT NULL COMMENT '申请时间',
    `refund_time` TIMESTAMP NULL DEFAULT NULL COMMENT '退款完成时间',
    `fail_reason` VARCHAR(255) DEFAULT NULL COMMENT '退款失败原因',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_refund_id` (`refund_id`),
    UNIQUE KEY `uk_refund_no_valid` (`refund_no`, `row_valid`),
    KEY `idx_order_refund_status` (`order_id`, `refund_status`),
    KEY `idx_approval_id` (`approval_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款记录表';

-- 金融申请表
CREATE TABLE IF NOT EXISTS `vso_finance_application` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `finance_application_id` VARCHAR(64) NOT NULL COMMENT '金融申请业务 ID',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `finance_type` VARCHAR(32) NOT NULL COMMENT '金融方案类型',
    `finance_provider` VARCHAR(64) DEFAULT NULL COMMENT '金融机构',
    `finance_status` VARCHAR(32) NOT NULL COMMENT '金融状态',
    `loan_amount` DECIMAL(18,2) DEFAULT NULL COMMENT '贷款金额',
    `interest_discount_amount` DECIMAL(18,2) DEFAULT NULL COMMENT '贴息金额',
    `apply_time` TIMESTAMP NULL DEFAULT NULL COMMENT '申请时间',
    `approve_time` TIMESTAMP NULL DEFAULT NULL COMMENT '审批通过时间',
    `disbursement_time` TIMESTAMP NULL DEFAULT NULL COMMENT '放款时间',
    `reject_reason` VARCHAR(255) DEFAULT NULL COMMENT '驳回原因',
    `external_apply_no` VARCHAR(64) DEFAULT NULL COMMENT '外部申请单号',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_finance_application_id` (`finance_application_id`),
    KEY `idx_order_finance_status` (`order_id`, `finance_status`),
    KEY `idx_external_apply_no` (`external_apply_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='金融申请表';

-- 补贴申请表
CREATE TABLE IF NOT EXISTS `vso_subsidy_application` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `subsidy_application_id` VARCHAR(64) NOT NULL COMMENT '补贴申请业务 ID',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `subsidy_type` VARCHAR(32) NOT NULL COMMENT '补贴类型',
    `subsidy_amount` DECIMAL(18,2) DEFAULT NULL COMMENT '补贴金额',
    `subsidy_status` VARCHAR(32) NOT NULL COMMENT '补贴状态',
    `apply_time` TIMESTAMP NULL DEFAULT NULL COMMENT '申请时间',
    `approve_time` TIMESTAMP NULL DEFAULT NULL COMMENT '审核通过时间',
    `grant_time` TIMESTAMP NULL DEFAULT NULL COMMENT '发放时间',
    `reject_reason` VARCHAR(255) DEFAULT NULL COMMENT '驳回原因',
    `external_apply_no` VARCHAR(64) DEFAULT NULL COMMENT '外部申请单号',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_subsidy_application_id` (`subsidy_application_id`),
    KEY `idx_order_subsidy_status` (`order_id`, `subsidy_status`),
    KEY `idx_external_subsidy_apply_no` (`external_apply_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='补贴申请表';

-- 交付预约表
CREATE TABLE IF NOT EXISTS `vso_delivery_appointment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `delivery_appointment_id` VARCHAR(64) NOT NULL COMMENT '预约业务 ID',
    `delivery_appointment_no` VARCHAR(64) NOT NULL COMMENT '预约单号',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `appointment_status` VARCHAR(32) NOT NULL COMMENT '预约状态',
    `delivery_mode` VARCHAR(32) NOT NULL COMMENT '交付方式',
    `appointment_time` TIMESTAMP NULL DEFAULT NULL COMMENT '预约时间',
    `appointment_place` VARCHAR(255) DEFAULT NULL COMMENT '预约地点',
    `appointment_store_code` VARCHAR(64) DEFAULT NULL COMMENT '预约门店/交付中心编码',
    `contact_name` VARCHAR(64) DEFAULT NULL COMMENT '预约联系人',
    `contact_mobile_encrypted` VARCHAR(255) DEFAULT NULL COMMENT '联系人手机号密文',
    `reschedule_reason` VARCHAR(255) DEFAULT NULL COMMENT '改期原因',
    `cancel_reason` VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
    `operator_id` VARCHAR(64) DEFAULT NULL COMMENT '操作人 ID',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_delivery_appointment_id` (`delivery_appointment_id`),
    UNIQUE KEY `uk_delivery_appointment_no_valid` (`delivery_appointment_no`, `row_valid`),
    KEY `idx_order_appointment_status` (`order_id`, `appointment_status`),
    KEY `idx_appointment_time` (`appointment_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交付预约表';

-- 交付完成记录表
CREATE TABLE IF NOT EXISTS `vso_delivery_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `delivery_record_id` VARCHAR(64) NOT NULL COMMENT '交付记录业务 ID',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `delivery_appointment_id` VARCHAR(64) DEFAULT NULL COMMENT '关联预约业务 ID',
    `delivery_status` VARCHAR(32) NOT NULL COMMENT '交付状态',
    `delivery_mode` VARCHAR(32) DEFAULT NULL COMMENT '交付方式',
    `actual_delivery_time` TIMESTAMP NULL DEFAULT NULL COMMENT '实际交付时间',
    `delivery_place` VARCHAR(255) DEFAULT NULL COMMENT '交付地点',
    `delivery_store_code` VARCHAR(64) DEFAULT NULL COMMENT '交付门店编码',
    `receiver_name` VARCHAR(64) DEFAULT NULL COMMENT '实际接收人',
    `receiver_mobile_encrypted` VARCHAR(255) DEFAULT NULL COMMENT '接收人手机号密文',
    `receiver_is_buyer` TINYINT NOT NULL DEFAULT 1 COMMENT '是否本人签收',
    `authorized_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否存在授权依据',
    `authorized_proof_type` VARCHAR(64) DEFAULT NULL COMMENT '授权依据类型',
    `vehicle_vin` VARCHAR(32) DEFAULT NULL COMMENT '交付 VIN',
    `vehicle_mileage` INT DEFAULT NULL COMMENT '交付里程 (公里)',
    `delivery_remark` VARCHAR(255) DEFAULT NULL COMMENT '交付备注',
    `exception_remark` VARCHAR(255) DEFAULT NULL COMMENT '交付异常说明',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_delivery_record_id` (`delivery_record_id`),
    KEY `idx_order_delivery_status` (`order_id`, `delivery_status`),
    KEY `idx_actual_delivery_time` (`actual_delivery_time`),
    KEY `idx_vehicle_vin_delivery` (`vehicle_vin`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交付完成记录表';

-- 上牌跟踪表
CREATE TABLE IF NOT EXISTS `vso_registration` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `registration_id` VARCHAR(64) NOT NULL COMMENT '上牌业务 ID',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `registration_status` VARCHAR(32) NOT NULL COMMENT '上牌状态',
    `plate_no` VARCHAR(32) DEFAULT NULL COMMENT '车牌号',
    `apply_time` TIMESTAMP NULL DEFAULT NULL COMMENT '申请时间',
    `material_submit_time` TIMESTAMP NULL DEFAULT NULL COMMENT '资料提交时间',
    `finish_time` TIMESTAMP NULL DEFAULT NULL COMMENT '上牌完成时间',
    `fail_reason` VARCHAR(255) DEFAULT NULL COMMENT '失败原因',
    `external_apply_no` VARCHAR(64) DEFAULT NULL COMMENT '外部上牌申请单号',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_registration_id` (`registration_id`),
    KEY `idx_order_registration_status` (`order_id`, `registration_status`),
    KEY `idx_plate_no` (`plate_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='上牌跟踪表';

-- 发票表
CREATE TABLE IF NOT EXISTS `vso_invoice` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `invoice_id` VARCHAR(64) NOT NULL COMMENT '发票业务 ID',
    `invoice_no` VARCHAR(64) DEFAULT NULL COMMENT '发票号',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `invoice_status` VARCHAR(32) NOT NULL COMMENT '发票状态',
    `invoice_type` VARCHAR(32) NOT NULL DEFAULT 'paper' COMMENT '发票类型',
    `invoice_title` VARCHAR(255) DEFAULT NULL COMMENT '发票抬头',
    `invoice_tax_no` VARCHAR(64) DEFAULT NULL COMMENT '税号',
    `invoice_amount` DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '发票金额',
    `invoice_subject_code` VARCHAR(64) DEFAULT NULL COMMENT '开票主体编码',
    `issue_time` TIMESTAMP NULL DEFAULT NULL COMMENT '开票时间',
    `void_time` TIMESTAMP NULL DEFAULT NULL COMMENT '作废时间',
    `reissue_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否重开',
    `void_reason` VARCHAR(255) DEFAULT NULL COMMENT '作废原因',
    `external_invoice_no` VARCHAR(64) DEFAULT NULL COMMENT '外部发票单号',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_invoice_id` (`invoice_id`),
    KEY `idx_order_invoice_status` (`order_id`, `invoice_status`),
    KEY `idx_invoice_no` (`invoice_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发票表';

SET FOREIGN_KEY_CHECKS = 1;
