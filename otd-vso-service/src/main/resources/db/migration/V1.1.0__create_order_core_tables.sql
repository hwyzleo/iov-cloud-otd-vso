-- ============================================================
-- VSO 车辆销售订单系统 - 订单核心表
-- Flyway 版本：V1.1.0
-- 描述：创建订单主档、客户、商品快照、金额、归属、维度状态表
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 订单核心表
-- ============================================================

-- 订单主表
CREATE TABLE IF NOT EXISTS `vso_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务唯一标识',
    `order_no` VARCHAR(64) DEFAULT NULL COMMENT '正式订单号',
    `small_order_no` VARCHAR(64) DEFAULT NULL COMMENT '小订单号',
    `order_type` VARCHAR(32) NOT NULL COMMENT '订单类型：small-小订单，formal-正式订单，manual-手工订单，repair-补单，change-变更单，refund_apply-退订申请，void-作废单，closed-关闭单',
    `order_source` VARCHAR(32) NOT NULL COMMENT '订单来源：capp-C 端自主下单，sales-销售代客下单，store-门店代客下单，operation-运营补录，import-外部导入，activity-活动订单，small_to_formal-小订单转正式',
    `source_remark` VARCHAR(255) DEFAULT NULL COMMENT '来源补充说明',
    `customer_type` VARCHAR(32) NOT NULL DEFAULT 'personal' COMMENT '客户类型：personal-个人客户',
    `main_status` VARCHAR(32) NOT NULL COMMENT '主状态：待创建，待提交，待审核，待锁单，已锁单，待配车，已配车，待签约，待付款，待交付，已交付，已完成，已取消，已关闭',
    `end_type` VARCHAR(32) DEFAULT NULL COMMENT '结束语义：cancel-取消，close-关闭，void-作废',
    `previous_main_status` VARCHAR(32) DEFAULT NULL COMMENT '关闭前上一有效状态',
    `brand_code` VARCHAR(32) NOT NULL DEFAULT 'OPENIOV' COMMENT '品牌编码',
    `region_code` VARCHAR(64) DEFAULT NULL COMMENT '当前归属区域编码',
    `store_code` VARCHAR(64) DEFAULT NULL COMMENT '当前归属门店编码',
    `sales_code` VARCHAR(64) DEFAULT NULL COMMENT '当前销售顾问编码',
    `vehicle_vin` VARCHAR(32) DEFAULT NULL COMMENT '当前绑定 VIN',
    `has_exception` TINYINT NOT NULL DEFAULT 0 COMMENT '是否存在未关闭异常单：0-否，1-是',
    `current_version_no` INT NOT NULL DEFAULT 1 COMMENT '当前版本号',
    `locked_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否处于订单锁中：0-否，1-是',
    `reopen_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否发生过重开：0-否，1-是',
    `cancel_reason` VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
    `close_reason` VARCHAR(255) DEFAULT NULL COMMENT '关闭原因',
    `void_reason` VARCHAR(255) DEFAULT NULL COMMENT '作废原因',
    `created_at_business` TIMESTAMP NULL DEFAULT NULL COMMENT '业务创建时间',
    `audit_submit_time` TIMESTAMP NULL DEFAULT NULL COMMENT '提交审核时间',
    `audit_pass_time` TIMESTAMP NULL DEFAULT NULL COMMENT '审核通过时间',
    `lock_time` TIMESTAMP NULL DEFAULT NULL COMMENT '锁单时间',
    `delivery_finish_time` TIMESTAMP NULL DEFAULT NULL COMMENT '交付完成时间',
    `finish_time` TIMESTAMP NULL DEFAULT NULL COMMENT '订单完成时间',
    `cancel_time` TIMESTAMP NULL DEFAULT NULL COMMENT '订单取消时间',
    `close_time` TIMESTAMP NULL DEFAULT NULL COMMENT '订单关闭时间',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_id` (`order_id`),
    UNIQUE KEY `uk_order_no_valid` (`order_no`, `row_valid`),
    UNIQUE KEY `uk_small_order_no_valid` (`small_order_no`, `row_valid`),
    KEY `idx_status_time` (`main_status`, `created_at_business`),
    KEY `idx_store_sales_status` (`store_code`, `sales_code`, `main_status`),
    KEY `idx_lock_time` (`lock_time`),
    KEY `idx_create_time_business` (`created_at_business`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';

-- 订单客户与购车人信息表
CREATE TABLE IF NOT EXISTS `vso_order_party` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `party_id` VARCHAR(64) NOT NULL COMMENT '主体关系业务 ID',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `party_role` VARCHAR(32) NOT NULL COMMENT '角色：order_user-下单客户，buyer-购车人，invoice_contact-发票联系人，delivery_contact-交付联系人，plate_contact-上牌联系人，emergency_contact-紧急联系人',
    `user_id` VARCHAR(64) DEFAULT NULL COMMENT '平台用户 ID',
    `name` VARCHAR(64) NOT NULL COMMENT '姓名',
    `mobile_encrypted` VARCHAR(255) DEFAULT NULL COMMENT '手机号密文',
    `mobile_hash` CHAR(64) DEFAULT NULL COMMENT '手机号哈希',
    `id_no_encrypted` VARCHAR(255) DEFAULT NULL COMMENT '身份证号密文',
    `id_no_hash` CHAR(64) DEFAULT NULL COMMENT '身份证号哈希',
    `relation_to_buyer` VARCHAR(32) DEFAULT NULL COMMENT '与购车人关系',
    `address_encrypted` VARCHAR(512) DEFAULT NULL COMMENT '地址密文',
    `authorized_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否具备授权：0-否，1-是',
    `authorized_proof_type` VARCHAR(64) DEFAULT NULL COMMENT '授权依据类型',
    `authorized_proof_url` VARCHAR(255) DEFAULT NULL COMMENT '授权材料地址',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_party_id` (`party_id`),
    KEY `idx_order_role` (`order_id`, `party_role`),
    KEY `idx_mobile_hash` (`mobile_hash`),
    KEY `idx_id_no_hash` (`id_no_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单客户与购车人信息表';

-- 订单车型配置快照表
CREATE TABLE IF NOT EXISTS `vso_order_vehicle_snapshot` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `snapshot_id` VARCHAR(64) NOT NULL COMMENT '快照业务 ID',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `model_code` VARCHAR(64) NOT NULL COMMENT '车型编码',
    `model_name` VARCHAR(128) NOT NULL COMMENT '车型名称',
    `config_code` VARCHAR(64) NOT NULL COMMENT '配置编码',
    `config_name` VARCHAR(128) NOT NULL COMMENT '配置名称',
    `color_code` VARCHAR(64) NOT NULL COMMENT '颜色编码',
    `color_name` VARCHAR(128) NOT NULL COMMENT '颜色名称',
    `option_snapshot` TEXT DEFAULT NULL COMMENT '选装项快照',
    `sale_scope_code` VARCHAR(64) DEFAULT NULL COMMENT '可售口径编码',
    `display_snapshot` TEXT DEFAULT NULL COMMENT '展示文案快照',
    `snapshot_version` INT NOT NULL DEFAULT 1 COMMENT '快照版本号',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_snapshot_id` (`snapshot_id`),
    UNIQUE KEY `uk_order_snapshot_valid` (`order_id`, `row_valid`),
    KEY `idx_model_config_color` (`model_code`, `config_code`, `color_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单车型配置快照表';

-- 订单金额口径表
CREATE TABLE IF NOT EXISTS `vso_order_amount` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `amount_id` VARCHAR(64) NOT NULL COMMENT '金额业务 ID',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `guide_price` DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '指导价',
    `vehicle_price` DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '裸车价/车款',
    `option_price` DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '选装价',
    `color_markup` DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '颜色加价',
    `service_fee` DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '服务费',
    `plate_service_fee` DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '上牌服务费',
    `insurance_fee` DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '保险费用',
    `discount_total` DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '优惠合计',
    `subsidy_total` DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '补贴合计',
    `finance_discount_total` DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '金融贴息合计',
    `deal_price_total` DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '最终成交总价',
    `deposit_amount` DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '定金金额',
    `down_payment_amount` DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '首付款金额',
    `tail_payment_amount` DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '尾款金额',
    `paid_total` DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '已支付金额',
    `refund_total` DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '已退款金额',
    `receivable_total` DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '订单应收金额',
    `net_receivable_total` DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '净应收金额',
    `unpaid_total` DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '待支付金额',
    `invoice_amount` DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '开票金额',
    `calculation_version` INT NOT NULL DEFAULT 1 COMMENT '金额计算版本',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_amount_id` (`amount_id`),
    UNIQUE KEY `uk_order_amount_valid` (`order_id`, `row_valid`),
    KEY `idx_receivable_unpaid` (`receivable_total`, `unpaid_total`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单金额口径表';

-- 订单归属与转派信息表
CREATE TABLE IF NOT EXISTS `vso_order_assignment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `assignment_id` VARCHAR(64) NOT NULL COMMENT '归属业务 ID',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `order_store_code` VARCHAR(64) DEFAULT NULL COMMENT '下单门店编码',
    `owner_store_code` VARCHAR(64) DEFAULT NULL COMMENT '归属门店编码',
    `delivery_store_code` VARCHAR(64) DEFAULT NULL COMMENT '交付门店编码',
    `origin_region_code` VARCHAR(64) DEFAULT NULL COMMENT '原归属区域编码',
    `owner_region_code` VARCHAR(64) DEFAULT NULL COMMENT '当前归属区域编码',
    `delivery_region_code` VARCHAR(64) DEFAULT NULL COMMENT '实际交付区域编码',
    `sales_code` VARCHAR(64) DEFAULT NULL COMMENT '当前销售顾问编码',
    `assign_type` VARCHAR(32) NOT NULL DEFAULT 'initial' COMMENT '归属动作：initial-首次指派，transfer-转派',
    `assign_reason` VARCHAR(255) DEFAULT NULL COMMENT '转派原因',
    `assign_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '归属生效时间',
    `operator_code` VARCHAR(64) DEFAULT NULL COMMENT '操作人员编码',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_assignment_id` (`assignment_id`),
    KEY `idx_owner_store_sales` (`owner_store_code`, `sales_code`),
    KEY `idx_owner_region` (`owner_region_code`),
    KEY `idx_order_assign_time` (`order_id`, `assign_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单归属与转派信息表';

-- 订单维度状态表
CREATE TABLE IF NOT EXISTS `vso_order_status_dimension` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `status_dimension_id` VARCHAR(64) NOT NULL COMMENT '维度状态业务 ID',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `audit_status` VARCHAR(32) DEFAULT NULL COMMENT '审批状态',
    `vehicle_status` VARCHAR(32) DEFAULT NULL COMMENT '配车状态',
    `contract_status` VARCHAR(32) DEFAULT NULL COMMENT '合同状态',
    `payment_status` VARCHAR(32) DEFAULT NULL COMMENT '支付状态',
    `finance_status` VARCHAR(32) DEFAULT NULL COMMENT '金融状态',
    `subsidy_status` VARCHAR(32) DEFAULT NULL COMMENT '补贴状态',
    `delivery_status` VARCHAR(32) DEFAULT NULL COMMENT '交付预约/执行状态',
    `registration_status` VARCHAR(32) DEFAULT NULL COMMENT '上牌状态',
    `invoice_status` VARCHAR(32) DEFAULT NULL COMMENT '发票状态',
    `material_status` VARCHAR(32) DEFAULT NULL COMMENT '资料状态',
    `exception_status` VARCHAR(32) DEFAULT NULL COMMENT '异常状态',
    `last_sync_source` VARCHAR(64) DEFAULT NULL COMMENT '最近一次状态变更来源',
    `last_sync_time` TIMESTAMP NULL DEFAULT NULL COMMENT '最近一次状态变更时间',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_status_dimension_id` (`status_dimension_id`),
    UNIQUE KEY `uk_order_status_dimension_valid` (`order_id`, `row_valid`),
    KEY `idx_payment_status` (`payment_status`),
    KEY `idx_vehicle_status` (`vehicle_status`),
    KEY `idx_exception_status` (`exception_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单维度状态表';

SET FOREIGN_KEY_CHECKS = 1;
