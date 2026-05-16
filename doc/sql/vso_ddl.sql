-- ============================================================
-- VSO 车辆销售订单系统 - 数据库初始化脚本
-- 版本：1.0.0
-- 数据库：MySQL 8.0+
-- 字符集：utf8mb4
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 基础业务表
-- ============================================================

-- 销售车型表
DROP TABLE IF EXISTS `tb_sale_model`;
CREATE TABLE `tb_sale_model` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `sale_code` VARCHAR(50) NOT NULL COMMENT '销售代码',
    `model_name` VARCHAR(255) NOT NULL COMMENT '销售车型名称',
    `parameters` JSON DEFAULT NULL COMMENT '销售车型相关参数',
    `images` JSON DEFAULT NULL COMMENT '销售车型图片集',
    `earnest_money` TINYINT NOT NULL COMMENT '是否允许意向金',
    `earnest_money_price` DECIMAL(10,2) DEFAULT NULL COMMENT '意向金价格',
    `down_payment` TINYINT NOT NULL COMMENT '是否允许定金',
    `down_payment_price` DECIMAL(10,2) DEFAULT NULL COMMENT '定金价格',
    `enable` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sale_code` (`sale_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售车型';

-- 销售车型配置表
DROP TABLE IF EXISTS `tb_sale_model_config`;
CREATE TABLE `tb_sale_model_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `sale_code` VARCHAR(50) NOT NULL COMMENT '销售代码',
    `type` VARCHAR(50) NOT NULL COMMENT '销售车型配置类型',
    `type_code` VARCHAR(50) NOT NULL COMMENT '销售车型配置类型代码',
    `type_name` VARCHAR(255) NOT NULL COMMENT '销售车型配置类型名称',
    `type_price` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '销售车型配置类型价格',
    `type_image` JSON DEFAULT NULL COMMENT '销售车型配置类型图片',
    `type_param` TEXT DEFAULT NULL COMMENT '销售车型配置类型参数',
    `enable` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    KEY `idx_sale_code` (`sale_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售车型配置';

-- 购车权益表
DROP TABLE IF EXISTS `tb_purchase_benefits`;
CREATE TABLE `tb_purchase_benefits` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `sale_code` VARCHAR(50) NOT NULL COMMENT '销售代码',
    `start_time` TIMESTAMP NOT NULL COMMENT '权益开始时间',
    `end_time` TIMESTAMP NOT NULL COMMENT '权益结束时间',
    `intro` TEXT DEFAULT NULL COMMENT '权益简介',
    `detail` TEXT DEFAULT NULL COMMENT '权益详情',
    `enable` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    KEY `idx_sale_code` (`sale_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购车权益';

-- 购车协议表
DROP TABLE IF EXISTS `tb_purchase_agreement`;
CREATE TABLE `tb_purchase_agreement` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `sale_code` VARCHAR(50) NOT NULL COMMENT '销售代码',
    `type` SMALLINT DEFAULT NULL COMMENT '协议类型：1-意向金（小定），2-定金（大定）',
    `title` VARCHAR(255) DEFAULT NULL COMMENT '协议标题',
    `intro` TEXT DEFAULT NULL COMMENT '协议简介',
    `detail` TEXT DEFAULT NULL COMMENT '协议详情',
    `enable` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    KEY `idx_sale_code` (`sale_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购车协议';

-- ============================================================
-- 订单核心表
-- ============================================================

-- 订单主表
DROP TABLE IF EXISTS `vso_order`;
CREATE TABLE `vso_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务唯一标识',
    `order_no` VARCHAR(64) DEFAULT NULL COMMENT '订单号',
    `order_type` VARCHAR(32) NOT NULL COMMENT '订单类型：small-小订单，formal-正式订单，manual-手工订单，repair-补单，change-变更单，refund_apply-退订申请，void-作废单，closed-关闭单',
    `order_source` VARCHAR(32) NOT NULL COMMENT '订单来源：capp-C 端自主下单，sales-销售代客下单，store-门店代客下单，operation-运营补录，import-外部导入，activity-活动订单，small_to_formal-小订单转正式',
    `source_remark` VARCHAR(255) DEFAULT NULL COMMENT '来源补充说明',
    `customer_type` VARCHAR(32) NOT NULL DEFAULT 'personal' COMMENT '客户类型：personal-个人客户',
    `payment_method` VARCHAR(32) DEFAULT NULL COMMENT '付款方式：full_payment-全款，loan-贷款',
    `license_city` VARCHAR(64) DEFAULT NULL COMMENT '上牌城市编码',
    `order_state` INT NOT NULL COMMENT '订单状态数值',
    `end_type` VARCHAR(32) DEFAULT NULL COMMENT '结束语义：cancel-取消，close-关闭，void-作废',
    `previous_order_state` INT DEFAULT NULL COMMENT '关闭前上一有效状态数值',
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
    KEY `idx_status_time` (`order_state`, `created_at_business`),
    KEY `idx_store_sales_status` (`store_code`, `sales_code`, `order_state`),
    KEY `idx_lock_time` (`lock_time`),
    KEY `idx_create_time_business` (`created_at_business`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';

-- 订单客户与购车人信息表
DROP TABLE IF EXISTS `vso_order_party`;
CREATE TABLE `vso_order_party` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `party_id` VARCHAR(64) NOT NULL COMMENT '主体关系业务 ID',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `party_role` VARCHAR(32) NOT NULL COMMENT '角色：order_user-下单客户，buyer-购车人，invoice_contact-发票联系人，delivery_contact-交付联系人，plate_contact-上牌联系人，emergency_contact-紧急联系人',
    `user_id` VARCHAR(64) DEFAULT NULL COMMENT '平台用户 ID',
    `name` VARCHAR(64) DEFAULT NULL COMMENT '姓名',
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
DROP TABLE IF EXISTS `vso_order_vehicle_snapshot`;
CREATE TABLE `vso_order_vehicle_snapshot` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `snapshot_id` VARCHAR(64) NOT NULL COMMENT '快照业务 ID',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `sale_model_code` VARCHAR(64) NOT NULL COMMENT '销售车型代码',
    `sale_model_name` VARCHAR(128) NOT NULL COMMENT '销售车型名称',
    `build_config_code` VARCHAR(64) NOT NULL COMMENT '生产配置代码',
    `build_config_name` VARCHAR(256) NOT NULL COMMENT '生产配置名称',
    `feature_config_snapshot` JSON NOT NULL COMMENT '特征值选择快照（VMD featureCodes数组）',
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
    KEY `idx_sale_model` (`sale_model_code`),
    KEY `idx_build_config` (`build_config_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单车型配置快照表';

-- 订单金额口径表
DROP TABLE IF EXISTS `vso_order_amount`;
CREATE TABLE `vso_order_amount` (
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
DROP TABLE IF EXISTS `vso_order_assignment`;
CREATE TABLE `vso_order_assignment` (
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
DROP TABLE IF EXISTS `vso_order_status_dimension`;
CREATE TABLE `vso_order_status_dimension` (
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

-- 订单资料主表
DROP TABLE IF EXISTS `vso_order_material`;
CREATE TABLE `vso_order_material` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `material_id` VARCHAR(64) NOT NULL COMMENT '资料业务 ID',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `material_type` VARCHAR(32) NOT NULL COMMENT '资料类型：id_card-身份证件，contact-联系方式，buyer_info-购车人信息，invoice_title-发票抬头，contract_attachment-合同附件，finance_info-金融资料，subsidy_info-补贴资料，plate_info-上牌资料，delivery_info-交付资料，authorization-授权委托，insurance-保险资料，other-其他补充',
    `material_name` VARCHAR(128) NOT NULL COMMENT '资料名称',
    `material_status` VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '资料状态：pending-待补充，submitted-已提交，reviewing-审核中，passed-审核通过，rejected-审核驳回，expired-已过期',
    `current_version_no` INT NOT NULL DEFAULT 1 COMMENT '当前资料版本号',
    `submitter_role` VARCHAR(32) DEFAULT NULL COMMENT '提交角色',
    `submitter_id` VARCHAR(64) DEFAULT NULL COMMENT '提交人 ID',
    `review_comment` VARCHAR(255) DEFAULT NULL COMMENT '最近一次审核意见',
    `timeout_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否超时：0-否，1-是',
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
DROP TABLE IF EXISTS `vso_order_material_version`;
CREATE TABLE `vso_order_material_version` (
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
DROP TABLE IF EXISTS `vso_approval`;
CREATE TABLE `vso_approval` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `approval_id` VARCHAR(64) NOT NULL COMMENT '审批业务 ID',
    `approval_no` VARCHAR(64) NOT NULL COMMENT '审批单号',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `approval_type` VARCHAR(32) NOT NULL COMMENT '审批类型：change-变更审批，transfer-转派审批，cancel-取消审批，close-关闭审批，reopen-重开审批，refund-退款审批，unlock-解锁审批，delete-删除审批',
    `approval_status` VARCHAR(32) NOT NULL COMMENT '审批状态：待创建，待提交，审批中，审批通过，审批驳回，已撤回，已取消，执行中，执行完成，执行失败，已关闭',
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
DROP TABLE IF EXISTS `vso_approval_record`;
CREATE TABLE `vso_approval_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `approval_record_id` VARCHAR(64) NOT NULL COMMENT '审批记录业务 ID',
    `approval_id` VARCHAR(64) NOT NULL COMMENT '审批业务 ID',
    `node_no` INT NOT NULL COMMENT '节点序号',
    `approve_type` VARCHAR(32) NOT NULL COMMENT '处理动作：approve-同意，reject-驳回，transfer-转审，add_sign-加签，countersign-会签，withdraw-撤回，remind-催办',
    `approver_id` VARCHAR(64) DEFAULT NULL COMMENT '审批人 ID',
    `approver_role` VARCHAR(32) DEFAULT NULL COMMENT '审批人角色',
    `approve_comment` VARCHAR(255) DEFAULT NULL COMMENT '审批意见',
    `attachment_url` VARCHAR(512) DEFAULT NULL COMMENT '审批附件地址',
    `operate_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    `timeout_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否超时处理：0-否，1-是',
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
DROP TABLE IF EXISTS `vso_vehicle_assignment`;
CREATE TABLE `vso_vehicle_assignment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `vehicle_assignment_id` VARCHAR(64) NOT NULL COMMENT '配车业务 ID',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `assignment_type` VARCHAR(32) NOT NULL COMMENT '动作类型：assign-配车，bind-绑车，change-改配，unbind-解绑，release-释放',
    `vehicle_source_type` VARCHAR(32) DEFAULT NULL COMMENT '车源类型',
    `vin` VARCHAR(32) DEFAULT NULL COMMENT 'VIN',
    `vehicle_id` VARCHAR(64) DEFAULT NULL COMMENT '车辆业务 ID',
    `assign_status` VARCHAR(32) NOT NULL COMMENT '配车状态：待配车，配车中，已配车，待绑定车辆，已绑定车辆，改配中，已释放车源，配车失败，已关闭',
    `manual_assign_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否人工指定：0-否，1-是',
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
DROP TABLE IF EXISTS `vso_contract`;
CREATE TABLE `vso_contract` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `contract_id` VARCHAR(64) NOT NULL COMMENT '合同业务 ID',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `contract_no` VARCHAR(64) NOT NULL COMMENT '合同号',
    `contract_type` VARCHAR(32) NOT NULL COMMENT '合同类型：main_contract-主购车合同，supplement_agreement-补充协议，price_change_agreement-价格变更协议，config_change_agreement-配置变更协议，delivery_agreement-交付补充协议，refund_agreement-退款/退订协议，finance_agreement-金融协议，subsidy_agreement-补贴协议，authorization-授权委托书，privacy_agreement-隐私/告知确认书，other_agreement-其他附件协议',
    `version_no` INT NOT NULL DEFAULT 1 COMMENT '版本号',
    `contract_status` VARCHAR(32) NOT NULL DEFAULT 'draft' COMMENT '合同状态：draft-草稿，generated-已生成，signing-签署中，signed-已签署，effective-已生效，invalid-已失效',
    `sign_mode` VARCHAR(32) DEFAULT NULL COMMENT '签署方式',
    `sign_effective_rule` VARCHAR(32) DEFAULT NULL COMMENT '生效规则：signed_immediately-签完即生效，confirm_after_sign-额外确认后生效',
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
DROP TABLE IF EXISTS `vso_payment`;
CREATE TABLE `vso_payment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `payment_id` VARCHAR(64) NOT NULL COMMENT '支付业务 ID',
    `payment_no` VARCHAR(64) NOT NULL COMMENT '支付单号',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `payment_stage` VARCHAR(32) NOT NULL COMMENT '支付阶段：deposit-定金，down_payment-首付款，tail_payment-尾款，finance_disbursement-金融放款',
    `payment_channel` VARCHAR(32) NOT NULL COMMENT '支付渠道：wechat-微信，alipay-支付宝，bank_card-银行卡，company_transfer-对公转账，offline_register-线下收款登记，finance_loan-金融放款',
    `payment_amount` DECIMAL(18,2) NOT NULL COMMENT '支付金额',
    `payment_status` VARCHAR(32) NOT NULL COMMENT '支付状态：待支付，部分支付，已支付，支付处理中，支付失败，已退款，部分退款，退款处理中，退款失败，已关闭',
    `initiator_role` VARCHAR(32) DEFAULT NULL COMMENT '发起角色',
    `initiator_id` VARCHAR(64) DEFAULT NULL COMMENT '发起人 ID',
    `authorized_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否存在授权依据：0-否，1-是',
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
DROP TABLE IF EXISTS `vso_refund`;
CREATE TABLE `vso_refund` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `refund_id` VARCHAR(64) NOT NULL COMMENT '退款业务 ID',
    `refund_no` VARCHAR(64) NOT NULL COMMENT '退款单号',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `payment_id` VARCHAR(64) DEFAULT NULL COMMENT '关联支付业务 ID',
    `refund_scene` VARCHAR(32) NOT NULL COMMENT '退款场景：cancel-取消退款，close-关闭退款，price_adjust-价格调整退款',
    `refund_amount` DECIMAL(18,2) NOT NULL COMMENT '退款金额',
    `refund_status` VARCHAR(32) NOT NULL COMMENT '退款状态：待审批，审批通过，审批驳回，退款处理中，退款成功，退款失败，已关闭',
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
DROP TABLE IF EXISTS `vso_finance_application`;
CREATE TABLE `vso_finance_application` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `finance_application_id` VARCHAR(64) NOT NULL COMMENT '金融申请业务 ID',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `finance_type` VARCHAR(32) NOT NULL COMMENT '金融方案类型：full_payment-全款购车，loan_installment-贷款分期，finance_leasing-融资租赁，balloon_payment-尾款型方案/气球贷，low_down_payment-低首付方案，interest_free-免息/贴息方案，manufacturer_finance-厂商金融，bank_finance-银行金融，other_finance-其他金融机构方案，special_finance-特殊活动金融方案',
    `finance_provider` VARCHAR(64) DEFAULT NULL COMMENT '金融机构',
    `finance_status` VARCHAR(32) NOT NULL COMMENT '金融状态：待申请，申请中，待提交资料，资料审核中，审批中，审批通过，审批驳回，放款中，已放款，放款失败，已关闭',
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
DROP TABLE IF EXISTS `vso_subsidy_application`;
CREATE TABLE `vso_subsidy_application` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `subsidy_application_id` VARCHAR(64) NOT NULL COMMENT '补贴申请业务 ID',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `subsidy_type` VARCHAR(32) NOT NULL COMMENT '补贴类型：manufacturer_subsidy-置换补贴，national_subsidy-国家补贴，local_subsidy-地方补贴，other_subsidy-其他补贴',
    `subsidy_amount` DECIMAL(18,2) DEFAULT NULL COMMENT '补贴金额',
    `subsidy_status` VARCHAR(32) NOT NULL COMMENT '补贴状态：待申请，申请中，待提交资料，审核中，审核通过，审核驳回，补贴发放中，已发放，发放失败，已关闭',
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
DROP TABLE IF EXISTS `vso_delivery_appointment`;
CREATE TABLE `vso_delivery_appointment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `delivery_appointment_id` VARCHAR(64) NOT NULL COMMENT '预约业务 ID',
    `delivery_appointment_no` VARCHAR(64) NOT NULL COMMENT '预约单号',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `appointment_status` VARCHAR(32) NOT NULL COMMENT '预约状态：待预约，已预约，已改期，已取消，待执行，执行中，已完成，已失约，已关闭',
    `delivery_mode` VARCHAR(32) NOT NULL COMMENT '交付方式：store_pickup-门店自提，delivery_center-交付中心交付，door_to_door-上门交付，third_party-第三方代交付，remote_delivery-异地交付',
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
DROP TABLE IF EXISTS `vso_delivery_record`;
CREATE TABLE `vso_delivery_record` (
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
    `receiver_is_buyer` TINYINT NOT NULL DEFAULT 1 COMMENT '是否本人签收：1-是，0-否',
    `authorized_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否存在授权依据：0-否，1-是',
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
DROP TABLE IF EXISTS `vso_registration`;
CREATE TABLE `vso_registration` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `registration_id` VARCHAR(64) NOT NULL COMMENT '上牌业务 ID',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `registration_status` VARCHAR(32) NOT NULL COMMENT '上牌状态：待上牌，待提交资料，资料审核中，待办理，办理中，已上牌，上牌失败，已关闭',
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
DROP TABLE IF EXISTS `vso_invoice`;
CREATE TABLE `vso_invoice` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `invoice_id` VARCHAR(64) NOT NULL COMMENT '发票业务 ID',
    `invoice_no` VARCHAR(64) DEFAULT NULL COMMENT '发票号',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `invoice_status` VARCHAR(32) NOT NULL COMMENT '发票状态：待开票，开票中，已开票，开票失败，已作废，已关闭',
    `invoice_type` VARCHAR(32) NOT NULL DEFAULT 'paper' COMMENT '发票类型：paper-纸质发票，electronic-电子发票',
    `invoice_title` VARCHAR(255) DEFAULT NULL COMMENT '发票抬头',
    `invoice_tax_no` VARCHAR(64) DEFAULT NULL COMMENT '税号',
    `invoice_amount` DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '发票金额',
    `invoice_subject_code` VARCHAR(64) DEFAULT NULL COMMENT '开票主体编码',
    `issue_time` TIMESTAMP NULL DEFAULT NULL COMMENT '开票时间',
    `void_time` TIMESTAMP NULL DEFAULT NULL COMMENT '作废时间',
    `reissue_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否重开：0-否，1-是',
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

-- 异常单表
DROP TABLE IF EXISTS `vso_exception_order`;
CREATE TABLE `vso_exception_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `exception_order_id` VARCHAR(64) NOT NULL COMMENT '异常单业务 ID',
    `exception_no` VARCHAR(64) NOT NULL COMMENT '异常单号',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `exception_type` VARCHAR(32) NOT NULL COMMENT '异常类型：payment_exception-支付异常，contract_exception-合同异常，vehicle_exception-配车异常，vehicle_bind_exception-车辆绑定异常，finance_exception-金融异常，subsidy_exception-补贴异常，registration_exception-上牌异常，delivery_exception-交付异常，approval_exception-审批异常，material_exception-资料异常，status_sync_exception-状态同步异常，external_interface_exception-外部系统接口异常，timeout_exception-超时异常，manual_review_exception-人工修正复核异常',
    `exception_source` VARCHAR(32) NOT NULL COMMENT '异常来源：system-系统自动识别，manual-人工上报，external_callback-外部回调冲突',
    `exception_status` VARCHAR(32) NOT NULL COMMENT '异常状态：待创建，待受理，处理中，待外部系统反馈，待人工复核，已解决，已关闭，已升级，已取消',
    `responsible_user_id` VARCHAR(64) DEFAULT NULL COMMENT '责任人 ID',
    `upgrade_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否升级：0-否，1-是',
    `exception_desc` VARCHAR(255) DEFAULT NULL COMMENT '异常描述',
    `external_system_name` VARCHAR(64) DEFAULT NULL COMMENT '外部系统名称',
    `external_error_code` VARCHAR(64) DEFAULT NULL COMMENT '外部错误码',
    `root_cause` VARCHAR(255) DEFAULT NULL COMMENT '根因分析',
    `solution_desc` VARCHAR(255) DEFAULT NULL COMMENT '处理方案',
    `prevention_desc` VARCHAR(255) DEFAULT NULL COMMENT '预防措施',
    `discover_time` TIMESTAMP NULL DEFAULT NULL COMMENT '发现时间',
    `close_time` TIMESTAMP NULL DEFAULT NULL COMMENT '关闭时间',
    `close_user_id` VARCHAR(64) DEFAULT NULL COMMENT '关闭人 ID',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_exception_order_id` (`exception_order_id`),
    UNIQUE KEY `uk_exception_no_valid` (`exception_no`, `row_valid`),
    KEY `idx_order_exception_status` (`order_id`, `exception_status`),
    KEY `idx_responsible_status` (`responsible_user_id`, `exception_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='异常单表';

-- 外部回调日志表
DROP TABLE IF EXISTS `vso_callback_log`;
CREATE TABLE `vso_callback_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `callback_log_id` VARCHAR(64) NOT NULL COMMENT '回调日志业务 ID',
    `order_id` VARCHAR(64) DEFAULT NULL COMMENT '订单业务 ID',
    `business_type` VARCHAR(32) NOT NULL COMMENT '业务类型：payment-支付，contract-合同，finance-金融，subsidy-补贴，invoice-发票，registration-上牌，vehicle-配车',
    `external_system_name` VARCHAR(64) NOT NULL COMMENT '外部系统名称',
    `external_business_no` VARCHAR(64) DEFAULT NULL COMMENT '外部业务单号',
    `idempotent_key` VARCHAR(128) DEFAULT NULL COMMENT '幂等键',
    `callback_status_value` VARCHAR(64) DEFAULT NULL COMMENT '回调状态值',
    `callback_result_code` VARCHAR(64) DEFAULT NULL COMMENT '回调结果码',
    `event_time` TIMESTAMP NULL DEFAULT NULL COMMENT '事件时间',
    `request_body` LONGTEXT DEFAULT NULL COMMENT '原始请求报文',
    `response_body` LONGTEXT DEFAULT NULL COMMENT '处理响应报文',
    `process_result` VARCHAR(32) NOT NULL DEFAULT 'success' COMMENT '处理结果：success-成功，ignored-忽略，conflict-冲突，failed-失败',
    `manual_override_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否因人工覆盖被忽略：0-否，1-是',
    `process_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '处理时间',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_callback_log_id` (`callback_log_id`),
    KEY `idx_order_business_type` (`order_id`, `business_type`),
    KEY `idx_external_business_no` (`external_system_name`, `external_business_no`),
    KEY `idx_idempotent_key` (`idempotent_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外部回调日志表';

-- 订单版本表
DROP TABLE IF EXISTS `vso_order_version`;
CREATE TABLE `vso_order_version` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_version_id` VARCHAR(64) NOT NULL COMMENT '版本业务 ID',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `version_no` INT NOT NULL COMMENT '版本号',
    `change_type` VARCHAR(32) NOT NULL COMMENT '变更类型：model_change-车型变更，config_change-配置变更，color_change-颜色变更，price_change-价格/权益变更，assignment_change-归属变更，post_lock_change-锁单后变更，vehicle_assign_change-配车结果变更，vehicle_bind_change-车辆绑定/换车变更，contract_change-合同关键信息变更，finance_change-金融方案变更，subsidy_change-补贴方案变更，delivery_change-交付关键信息变更，manual_modify-人工修正关键状态，approval_effective-审批通过后生效变更',
    `change_reason` VARCHAR(255) DEFAULT NULL COMMENT '变更原因',
    `approval_id` VARCHAR(64) DEFAULT NULL COMMENT '关联审批业务 ID',
    `trigger_source` VARCHAR(32) DEFAULT NULL COMMENT '触发来源',
    `trigger_user_id` VARCHAR(64) DEFAULT NULL COMMENT '触发人 ID',
    `effective_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已生效：0-否，1-是',
    `effective_time` TIMESTAMP NULL DEFAULT NULL COMMENT '生效时间',
    `snapshot_json` LONGTEXT DEFAULT NULL COMMENT '版本全量快照',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_version_id` (`order_version_id`),
    UNIQUE KEY `uk_order_version_no_valid` (`order_id`, `version_no`, `row_valid`),
    KEY `idx_approval_version` (`approval_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单版本表';

-- 订单版本差异表
DROP TABLE IF EXISTS `vso_order_version_diff`;
CREATE TABLE `vso_order_version_diff` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_version_diff_id` VARCHAR(64) NOT NULL COMMENT '差异业务 ID',
    `order_version_id` VARCHAR(64) NOT NULL COMMENT '版本业务 ID',
    `field_name` VARCHAR(128) NOT NULL COMMENT '字段名',
    `field_label` VARCHAR(128) DEFAULT NULL COMMENT '字段展示名',
    `before_value` TEXT DEFAULT NULL COMMENT '变更前值',
    `after_value` TEXT DEFAULT NULL COMMENT '变更后值',
    `change_category` VARCHAR(32) DEFAULT NULL COMMENT '差异类别：basic_info-基本信息，vehicle_info-车辆信息，amount_info-金额信息，assignment_info-归属信息，status_info-状态信息，other-其他',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_version_diff_id` (`order_version_diff_id`),
    KEY `idx_order_version_field` (`order_version_id`, `field_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单版本差异表';

-- 订单业务时间线表
DROP TABLE IF EXISTS `vso_order_timeline`;
CREATE TABLE `vso_order_timeline` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `timeline_id` VARCHAR(64) NOT NULL COMMENT '时间线业务 ID',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `event_type` VARCHAR(32) NOT NULL COMMENT '事件类型',
    `event_name` VARCHAR(64) NOT NULL COMMENT '事件名称',
    `before_status` VARCHAR(64) DEFAULT NULL COMMENT '变更前状态',
    `after_status` VARCHAR(64) DEFAULT NULL COMMENT '变更后状态',
    `operator_id` VARCHAR(64) DEFAULT NULL COMMENT '操作人 ID',
    `operator_role` VARCHAR(32) DEFAULT NULL COMMENT '操作人角色',
    `operate_source` VARCHAR(32) DEFAULT NULL COMMENT '操作来源',
    `related_doc_no` VARCHAR(64) DEFAULT NULL COMMENT '关联单据号',
    `external_system_name` VARCHAR(64) DEFAULT NULL COMMENT '外部系统名称',
    `result` VARCHAR(32) DEFAULT NULL COMMENT '处理结果：success-成功，fail-失败，pending-处理中',
    `fail_reason` VARCHAR(255) DEFAULT NULL COMMENT '失败原因',
    `event_remark` VARCHAR(255) DEFAULT NULL COMMENT '备注说明',
    `event_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '事件时间',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_timeline_id` (`timeline_id`),
    KEY `idx_order_event_time` (`order_id`, `event_time`),
    KEY `idx_event_type_time` (`event_type`, `event_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单业务时间线表';

-- 系统审计日志表
DROP TABLE IF EXISTS `vso_audit_log`;
CREATE TABLE `vso_audit_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `audit_id` VARCHAR(64) NOT NULL COMMENT '审计业务 ID',
    `order_id` VARCHAR(64) DEFAULT NULL COMMENT '订单业务 ID',
    `event_type` VARCHAR(32) NOT NULL COMMENT '事件类型',
    `event_name` VARCHAR(64) NOT NULL COMMENT '事件名称',
    `operator_id` VARCHAR(64) DEFAULT NULL COMMENT '操作人 ID',
    `operator_role` VARCHAR(32) DEFAULT NULL COMMENT '操作人角色',
    `request_uri` VARCHAR(255) DEFAULT NULL COMMENT '请求 URI',
    `request_method` VARCHAR(16) DEFAULT NULL COMMENT '请求方法',
    `trace_id` VARCHAR(64) DEFAULT NULL COMMENT '追踪标识',
    `operation_result` VARCHAR(32) NOT NULL DEFAULT 'success' COMMENT '操作结果：success-成功，fail-失败',
    `request_snapshot` LONGTEXT DEFAULT NULL COMMENT '请求快照',
    `response_code` VARCHAR(32) DEFAULT NULL COMMENT '响应码',
    `ip_address` VARCHAR(64) DEFAULT NULL COMMENT '请求 IP',
    `event_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '事件时间',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_audit_id` (`audit_id`),
    KEY `idx_order_event_time` (`order_id`, `event_time`),
    KEY `idx_operator_time` (`operator_id`, `event_time`),
    KEY `idx_trace_id` (`trace_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统审计日志表';

-- 通知任务表
DROP TABLE IF EXISTS `vso_notify_task`;
CREATE TABLE `vso_notify_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `notify_task_id` VARCHAR(64) NOT NULL COMMENT '通知任务业务 ID',
    `order_id` VARCHAR(64) DEFAULT NULL COMMENT '订单业务 ID',
    `notify_type` VARCHAR(32) NOT NULL COMMENT '通知类型：order_created-订单创建成功，status_changed-订单状态变更，lock_remind-锁单提醒，contract_remind-合同待签署提醒，deposit_remind-定金待支付提醒，payment_remind-首付款/尾款待支付提醒，vehicle_result-配车结果通知，approval_remind-审批待处理提醒，approval_result-审批结果通知，material_remind-资料待补充提醒，subsidy_remind-补贴资料待提交提醒，plate_remind-上牌资料待提交提醒，delivery_remind-交付预约提醒，delivery_complete-交付完成通知，timeout_warning-超时预警通知，exception_alert-异常订单告警，external_fail_alert-外部系统处理失败告警',
    `receiver_type` VARCHAR(32) NOT NULL DEFAULT 'customer' COMMENT '接收人类型：customer-C 端客户，internal-内部运营人员',
    `receiver_id` VARCHAR(64) DEFAULT NULL COMMENT '接收人 ID',
    `receiver_address` VARCHAR(255) DEFAULT NULL COMMENT '接收地址 (手机号/邮箱/推送 token)',
    `content_template_code` VARCHAR(64) DEFAULT NULL COMMENT '模板编码',
    `send_status` VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '发送状态：pending-待发送，sending-发送中，sent-已发送，failed-发送失败',
    `retry_count` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    `plan_send_time` TIMESTAMP NULL DEFAULT NULL COMMENT '计划发送时间',
    `send_time` TIMESTAMP NULL DEFAULT NULL COMMENT '发送时间',
    `fail_reason` VARCHAR(255) DEFAULT NULL COMMENT '失败原因',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_notify_task_id` (`notify_task_id`),
    KEY `idx_order_notify_type` (`order_id`, `notify_type`),
    KEY `idx_send_status_time` (`send_status`, `plan_send_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知任务表';

-- 超时任务表
DROP TABLE IF EXISTS `vso_timeout_task`;
CREATE TABLE `vso_timeout_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `timeout_task_id` VARCHAR(64) NOT NULL COMMENT '超时任务业务 ID',
    `order_id` VARCHAR(64) DEFAULT NULL COMMENT '订单业务 ID',
    `task_type` VARCHAR(32) NOT NULL COMMENT '任务类型：small_order_pay_timeout-小订单待支付超时，formal_order_audit_timeout-正式订单待审核超时，deposit_timeout-定金待支付超时，contract_sign_timeout-合同待签署超时，down_payment_timeout-首付款待支付超时，tail_payment_timeout-尾款待支付超时，vehicle_assign_timeout-配车长时间未完成，delivery_appointment_timeout-交付长时间未预约，plate_material_timeout-上牌资料长时间未提交，subsidy_material_timeout-补贴资料长时间未提交，approval_timeout-审批长时间未处理，external_timeout-外部系统处理超时',
    `task_status` VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '任务状态：pending-待触发，triggered-已触发，done-已完成，cancelled-已取消，failed-失败',
    `threshold_minutes` INT DEFAULT NULL COMMENT '阈值分钟数',
    `trigger_strategy` VARCHAR(32) NOT NULL COMMENT '触发策略：remind-提醒，close-关闭，invalid-失效，retry_and_alert-重试并告警',
    `plan_trigger_time` TIMESTAMP NOT NULL COMMENT '计划触发时间',
    `actual_trigger_time` TIMESTAMP NULL DEFAULT NULL COMMENT '实际触发时间',
    `retry_count` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    `last_fail_reason` VARCHAR(255) DEFAULT NULL COMMENT '最近失败原因',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_timeout_task_id` (`timeout_task_id`),
    KEY `idx_task_status_time` (`task_status`, `plan_trigger_time`),
    KEY `idx_order_task_type` (`order_id`, `task_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='超时任务表';

-- 物理删除审计影子记录表
DROP TABLE IF EXISTS `vso_order_shadow_delete`;
CREATE TABLE `vso_order_shadow_delete` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `shadow_delete_id` VARCHAR(64) NOT NULL COMMENT '影子记录业务 ID',
    `origin_order_no` VARCHAR(64) DEFAULT NULL COMMENT '原订单号',
    `delete_approval_id` VARCHAR(64) DEFAULT NULL COMMENT '删除审批业务 ID',
    `delete_reason` VARCHAR(255) DEFAULT NULL COMMENT '删除原因',
    `before_order_state` INT DEFAULT NULL COMMENT '删除前订单状态数值',
    `compliance_delete_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否因合规要求删除：0-否，1-是',
    `delete_user_id` VARCHAR(64) DEFAULT NULL COMMENT '删除人 ID',
    `delete_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '删除时间',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_shadow_delete_id` (`shadow_delete_id`),
    KEY `idx_origin_order_no` (`origin_order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物理删除审计影子记录表';

-- 订单锁记录表
DROP TABLE IF EXISTS `vso_order_lock`;
CREATE TABLE `vso_order_lock` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_lock_id` VARCHAR(64) NOT NULL COMMENT '订单锁业务 ID',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `lock_scene` VARCHAR(32) NOT NULL COMMENT '锁定场景：payment-支付发起，audit_submit-提交审核，modify_key_fields-修改关键字段，lock_order-锁单，cancel-发起取消，refund_apply-发起退订申请，refund-发起退款，vehicle_bind-配车绑定，vehicle_unbind-配车解绑，change_assign-改配，delivery_appointment-提交交付预约，delivery_confirm-确认交付完成，invoice_issue-开票，invoice_void-发票作废重开',
    `lock_holder_id` VARCHAR(64) NOT NULL COMMENT '持锁人 ID',
    `lock_holder_role` VARCHAR(32) NOT NULL COMMENT '持锁人角色',
    `lock_start_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '锁定开始时间',
    `lock_release_time` TIMESTAMP NULL DEFAULT NULL COMMENT '锁定释放时间',
    `lock_release_method` VARCHAR(32) DEFAULT NULL COMMENT '释放方式：normal-正常释放，timeout-超时释放，manual-人工解锁，inspection-巡检兜底解锁',
    `lock_release_reason` VARCHAR(255) DEFAULT NULL COMMENT '释放原因',
    `unlock_user_id` VARCHAR(64) DEFAULT NULL COMMENT '解锁人 ID',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_lock_id` (`order_lock_id`),
    KEY `idx_order_lock_scene` (`order_id`, `lock_scene`),
    KEY `idx_lock_start_time` (`lock_start_time`),
    KEY `idx_lock_holder_time` (`lock_holder_id`, `lock_start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单锁记录表';

-- ============================================================
-- 配置表
-- ============================================================

-- 超时任务配置表
DROP TABLE IF EXISTS `vso_config_timeout`;
CREATE TABLE `vso_config_timeout` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `config_key` VARCHAR(64) NOT NULL COMMENT '配置键：small_order_pay_timeout-小订单待支付超时，formal_order_audit_timeout-正式订单待审核超时，deposit_timeout-定金待支付超时，contract_sign_timeout-合同待签署超时',
    `config_name` VARCHAR(128) NOT NULL COMMENT '配置名称',
    `threshold_minutes` INT NOT NULL COMMENT '超时阈值分钟数',
    `trigger_strategy` VARCHAR(32) NOT NULL COMMENT '触发策略：remind-提醒，close-关闭，invalid-失效，retry_and_alert-重试并告警',
    `enable_flag` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：0-否，1-是',
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
DROP TABLE IF EXISTS `vso_config_vehicle_occupancy`;
CREATE TABLE `vso_config_vehicle_occupancy` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `occupancy_rule_key` VARCHAR(64) NOT NULL COMMENT '规则键',
    `model_code` VARCHAR(64) DEFAULT NULL COMMENT '车型编码：NULL 表示通用',
    `order_type` VARCHAR(32) DEFAULT NULL COMMENT '订单类型：NULL 表示通用',
    `store_code` VARCHAR(64) DEFAULT NULL COMMENT '门店编码：NULL 表示通用',
    `region_code` VARCHAR(64) DEFAULT NULL COMMENT '区域编码：NULL 表示通用',
    `activity_type` VARCHAR(32) DEFAULT NULL COMMENT '活动类型：NULL 表示通用',
    `customer_type` VARCHAR(32) DEFAULT NULL COMMENT '客户类型：NULL 表示通用',
    `finance_order_flag` TINYINT DEFAULT NULL COMMENT '是否金融订单：NULL-不区分，0-否，1-是',
    `special_approval_flag` TINYINT DEFAULT NULL COMMENT '是否特殊审批订单：NULL-不区分，0-否，1-是',
    `occupancy_hours` INT NOT NULL COMMENT '占用时长小时数',
    `enable_flag` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：0-否，1-是',
    `priority` INT NOT NULL DEFAULT 0 COMMENT '优先级：数字越大优先级越高',
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
('contract_sign_timeout', '合同待签署超时', 2880, 'remind', '合同待签署超时仅提醒');

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 数据库初始化完成
-- ============================================================
