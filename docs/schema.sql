-- ============================================================
-- VSO 车辆销售订单系统 - 完整数据库脚本
-- 基于 Flyway V1.0.0 ~ V1.26.0 合并生成
-- 生成时间：2026-05-23
-- 说明：此脚本为所有迁移执行后的最终表结构
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 一、销售车型相关表
-- ============================================================

-- 销售车型表
CREATE TABLE IF NOT EXISTS `vso_sale_model` (
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
CREATE TABLE IF NOT EXISTS `vso_sale_model_config` (
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

-- 销售车型生产配置关联表
CREATE TABLE IF NOT EXISTS `vso_sale_model_build_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `sale_code` VARCHAR(50) NOT NULL COMMENT '销售代码',
    `build_config_code` VARCHAR(50) NOT NULL COMMENT '生产配置代码',
    `enable` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `modify_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` VARCHAR(64) DEFAULT '' COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '行版本',
    `row_valid` TINYINT(1) DEFAULT 1 COMMENT '行有效',
    `description` VARCHAR(500) DEFAULT '' COMMENT '描述',
    PRIMARY KEY (`id`),
    KEY `idx_sale_code` (`sale_code`),
    KEY `idx_build_config_code` (`build_config_code`),
    UNIQUE KEY `uk_sale_build_config` (`sale_code`, `build_config_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售车型生产配置关联表';

-- 销售车型基础车型关联表
CREATE TABLE IF NOT EXISTS `vso_sale_model_base_model` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `sale_code` VARCHAR(50) NOT NULL COMMENT '销售代码',
    `base_model_code` VARCHAR(50) NOT NULL COMMENT '基础车型代码',
    `base_model_name` VARCHAR(255) DEFAULT NULL COMMENT '基础车型名称',
    `base_model_image` JSON DEFAULT NULL COMMENT '基础车型图片',
    `base_model_price` DECIMAL(10,2) DEFAULT NULL COMMENT '基础车型价格',
    `base_model_desc` TEXT DEFAULT NULL COMMENT '基础车型描述',
    `base_model_param` TEXT DEFAULT NULL COMMENT '基础车型参数',
    `enable` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `modify_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` VARCHAR(64) DEFAULT '' COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '行版本',
    `row_valid` TINYINT(1) DEFAULT 1 COMMENT '行有效',
    `description` VARCHAR(500) DEFAULT '' COMMENT '描述',
    PRIMARY KEY (`id`),
    KEY `idx_sale_code` (`sale_code`),
    KEY `idx_base_model_code` (`base_model_code`),
    UNIQUE KEY `uk_sale_base_model` (`sale_code`, `base_model_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售车型基础车型关联表';

-- 购车权益表
CREATE TABLE IF NOT EXISTS `vso_purchase_benefits` (
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

-- 心愿单表
CREATE TABLE IF NOT EXISTS `vso_wishlist` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `wishlist_id` VARCHAR(64) NOT NULL COMMENT '心愿单业务ID',
    `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
    `sale_model` VARCHAR(64) NOT NULL COMMENT '销售车型编码',
    `build_config_code` VARCHAR(64) DEFAULT NULL COMMENT '生产配置编码',
    `wishlist_name` VARCHAR(128) DEFAULT NULL COMMENT '心愿单名称',
    `status` VARCHAR(32) NOT NULL DEFAULT 'active' COMMENT '状态：active-有效，deleted-已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `modify_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` VARCHAR(64) DEFAULT '' COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '行版本',
    `row_valid` TINYINT(1) DEFAULT 1 COMMENT '行有效',
    `description` VARCHAR(500) DEFAULT '' COMMENT '描述',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_wishlist_id` (`wishlist_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_sale_model` (`sale_model`),
    KEY `idx_user_status` (`user_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='心愿单表';

-- 心愿单唯一性约束说明：
-- MySQL 不支持条件唯一索引（Partial Index），无法直接创建 (user_id, build_config_code) WHERE status='active' 的唯一索引
-- 实际实现采用应用层校验（WishlistAppService.validateDuplicateWishlist）保证同一用户同一配置最多一个有效心愿单
-- 若需 DB 层兜底防并发，可考虑使用分布式锁或应用层唯一性校验 + 事务控制

-- ============================================================
-- 二、订单核心表
-- ============================================================

-- 订单主表
CREATE TABLE IF NOT EXISTS `vso_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务唯一标识',
    `order_no` VARCHAR(64) DEFAULT NULL COMMENT '订单号',
    `order_type` VARCHAR(32) NOT NULL COMMENT '订单类型：small-小订单，formal-正式订单，manual-手工订单，repair-补单，change-变更单，refund_apply-退订申请，void-作废单，closed-关闭单',
    `order_source` VARCHAR(32) NOT NULL COMMENT '订单来源：capp-C端自主下单，sales-销售代客下单，store-门店代客下单，operation-运营补录，import-外部导入，activity-活动订单，small_to_formal-小订单转正式',
    `source_remark` VARCHAR(255) DEFAULT NULL COMMENT '来源补充说明',
    `customer_type` VARCHAR(32) NOT NULL DEFAULT 'personal' COMMENT '客户类型：personal-个人客户',
    `payment_method` VARCHAR(32) DEFAULT NULL COMMENT '付款方式：full_payment-全款，loan-贷款',
    `license_city` VARCHAR(64) DEFAULT NULL COMMENT '上牌城市编码',
    `order_state` INT DEFAULT NULL COMMENT '订单状态数值',
    `previous_order_state` INT DEFAULT NULL COMMENT '关闭前上一有效状态数值',
    `end_type` VARCHAR(32) DEFAULT NULL COMMENT '结束语义：cancel-取消，close-关闭，void-作废',
    `brand_code` VARCHAR(32) NOT NULL DEFAULT 'OPENIOV' COMMENT '品牌编码',
    `sale_model` VARCHAR(64) DEFAULT NULL COMMENT '销售车型编码',
    `owner_region_code` VARCHAR(64) DEFAULT NULL COMMENT '归属区域编码',
    `order_store_code` VARCHAR(64) DEFAULT NULL COMMENT '下单门店编码',
    `owner_store_code` VARCHAR(64) DEFAULT NULL COMMENT '归属门店编码',
    `delivery_store_code` VARCHAR(64) DEFAULT NULL COMMENT '交付门店编码',
    `delivery_region_code` VARCHAR(64) DEFAULT NULL COMMENT '交付区域编码',
    `sales_code` VARCHAR(64) DEFAULT NULL COMMENT '当前销售顾问编码',
    `vehicle_vin` VARCHAR(32) DEFAULT NULL COMMENT '当前绑定 VIN',
    `build_config_code` VARCHAR(64) DEFAULT NULL COMMENT '生产配置编码',
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
    KEY `idx_store_sales_status` (`order_store_code`, `sales_code`, `order_state`),
    KEY `idx_lock_time` (`lock_time`),
    KEY `idx_create_time_business` (`created_at_business`),
    KEY `idx_build_config_code` (`build_config_code`),
    KEY `idx_sale_model` (`sale_model`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';

-- 订单客户与购车人信息表
CREATE TABLE IF NOT EXISTS `vso_order_party` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `party_id` VARCHAR(64) NOT NULL COMMENT '主体关系业务 ID',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `party_role` VARCHAR(32) NOT NULL COMMENT '角色：order_user-下单客户，buyer-购车人，invoice_contact-发票联系人，delivery_contact-交付联系人，plate_contact-上牌联系人，emergency_contact-紧急联系人',
    `user_id` VARCHAR(64) DEFAULT NULL COMMENT '平台用户 ID',
    `name` VARCHAR(64) DEFAULT NULL COMMENT '姓名',
    `person_type` INT DEFAULT NULL COMMENT '订购人类型：1-本人、2-代理人等',
    `id_type` INT DEFAULT NULL COMMENT '证件类型：1-身份证、2-护照、3-营业执照、4-组织机构代码等',
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
    UNIQUE KEY `uk_order_snapshot_version` (`order_id`, `snapshot_version`),
    KEY `idx_sale_model` (`sale_model_code`),
    KEY `idx_build_config` (`build_config_code`)
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

-- ============================================================
-- 三、订单业务流程表
-- ============================================================

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

-- ============================================================
-- 四、审计与支撑表
-- ============================================================

-- 异常单表
CREATE TABLE IF NOT EXISTS `vso_exception_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `exception_order_id` VARCHAR(64) NOT NULL COMMENT '异常单业务 ID',
    `exception_no` VARCHAR(64) NOT NULL COMMENT '异常单号',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `exception_type` VARCHAR(32) NOT NULL COMMENT '异常类型',
    `exception_source` VARCHAR(32) NOT NULL COMMENT '异常来源',
    `exception_status` VARCHAR(32) NOT NULL COMMENT '异常状态',
    `responsible_user_id` VARCHAR(64) DEFAULT NULL COMMENT '责任人 ID',
    `upgrade_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否升级',
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
CREATE TABLE IF NOT EXISTS `vso_callback_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `callback_log_id` VARCHAR(64) NOT NULL COMMENT '回调日志业务 ID',
    `order_id` VARCHAR(64) DEFAULT NULL COMMENT '订单业务 ID',
    `business_type` VARCHAR(32) NOT NULL COMMENT '业务类型',
    `external_system_name` VARCHAR(64) NOT NULL COMMENT '外部系统名称',
    `external_business_no` VARCHAR(64) DEFAULT NULL COMMENT '外部业务单号',
    `idempotent_key` VARCHAR(128) DEFAULT NULL COMMENT '幂等键',
    `callback_status_value` VARCHAR(64) DEFAULT NULL COMMENT '回调状态值',
    `callback_result_code` VARCHAR(64) DEFAULT NULL COMMENT '回调结果码',
    `event_time` TIMESTAMP NULL DEFAULT NULL COMMENT '事件时间',
    `request_body` LONGTEXT DEFAULT NULL COMMENT '原始请求报文',
    `response_body` LONGTEXT DEFAULT NULL COMMENT '处理响应报文',
    `process_result` VARCHAR(32) NOT NULL DEFAULT 'success' COMMENT '处理结果',
    `manual_override_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否因人工覆盖被忽略',
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
CREATE TABLE IF NOT EXISTS `vso_order_version` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_version_id` VARCHAR(64) NOT NULL COMMENT '版本业务 ID',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `version_no` INT NOT NULL COMMENT '版本号',
    `change_type` VARCHAR(32) NOT NULL COMMENT '变更类型',
    `change_reason` VARCHAR(255) DEFAULT NULL COMMENT '变更原因',
    `approval_id` VARCHAR(64) DEFAULT NULL COMMENT '关联审批业务 ID',
    `trigger_source` VARCHAR(32) DEFAULT NULL COMMENT '触发来源',
    `trigger_user_id` VARCHAR(64) DEFAULT NULL COMMENT '触发人 ID',
    `effective_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已生效',
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
CREATE TABLE IF NOT EXISTS `vso_order_version_diff` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_version_diff_id` VARCHAR(64) NOT NULL COMMENT '差异业务 ID',
    `order_version_id` VARCHAR(64) NOT NULL COMMENT '版本业务 ID',
    `field_name` VARCHAR(128) NOT NULL COMMENT '字段名',
    `field_label` VARCHAR(128) DEFAULT NULL COMMENT '字段展示名',
    `before_value` TEXT DEFAULT NULL COMMENT '变更前值',
    `after_value` TEXT DEFAULT NULL COMMENT '变更后值',
    `change_category` VARCHAR(32) DEFAULT NULL COMMENT '差异类别',
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
CREATE TABLE IF NOT EXISTS `vso_order_timeline` (
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
    `result` VARCHAR(32) DEFAULT NULL COMMENT '处理结果',
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
CREATE TABLE IF NOT EXISTS `vso_audit_log` (
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
    `operation_result` VARCHAR(32) NOT NULL DEFAULT 'success' COMMENT '操作结果',
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
CREATE TABLE IF NOT EXISTS `vso_notify_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `notify_task_id` VARCHAR(64) NOT NULL COMMENT '通知任务业务 ID',
    `order_id` VARCHAR(64) DEFAULT NULL COMMENT '订单业务 ID',
    `notify_type` VARCHAR(32) NOT NULL COMMENT '通知类型',
    `receiver_type` VARCHAR(32) NOT NULL DEFAULT 'customer' COMMENT '接收人类型',
    `receiver_id` VARCHAR(64) DEFAULT NULL COMMENT '接收人 ID',
    `receiver_address` VARCHAR(255) DEFAULT NULL COMMENT '接收地址',
    `content_template_code` VARCHAR(64) DEFAULT NULL COMMENT '模板编码',
    `send_status` VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '发送状态',
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
CREATE TABLE IF NOT EXISTS `vso_timeout_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `timeout_task_id` VARCHAR(64) NOT NULL COMMENT '超时任务业务 ID',
    `order_id` VARCHAR(64) DEFAULT NULL COMMENT '订单业务 ID',
    `task_type` VARCHAR(32) NOT NULL COMMENT '任务类型',
    `task_status` VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '任务状态',
    `threshold_minutes` INT DEFAULT NULL COMMENT '阈值分钟数',
    `trigger_strategy` VARCHAR(32) NOT NULL COMMENT '触发策略',
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
CREATE TABLE IF NOT EXISTS `vso_order_shadow_delete` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `shadow_delete_id` VARCHAR(64) NOT NULL COMMENT '影子记录业务 ID',
    `origin_order_no` VARCHAR(64) DEFAULT NULL COMMENT '原订单号',
    `delete_approval_id` VARCHAR(64) DEFAULT NULL COMMENT '删除审批业务 ID',
    `delete_reason` VARCHAR(255) DEFAULT NULL COMMENT '删除原因',
    `before_order_state` INT DEFAULT NULL COMMENT '删除前订单状态数值',
    `compliance_delete_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否因合规要求删除',
    `delete_user_id` VARCHAR(64) DEFAULT NULL COMMENT '删除人 ID',
    `owner_region_code` VARCHAR(64) DEFAULT NULL COMMENT '归属区域编码',
    `order_store_code` VARCHAR(64) DEFAULT NULL COMMENT '下单门店编码',
    `owner_store_code` VARCHAR(64) DEFAULT NULL COMMENT '归属门店编码',
    `delivery_store_code` VARCHAR(64) DEFAULT NULL COMMENT '交付门店编码',
    `delivery_region_code` VARCHAR(64) DEFAULT NULL COMMENT '交付区域编码',
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
CREATE TABLE IF NOT EXISTS `vso_order_lock` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_lock_id` VARCHAR(64) NOT NULL COMMENT '订单锁业务 ID',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    `lock_scene` VARCHAR(32) NOT NULL COMMENT '锁定场景',
    `lock_holder_id` VARCHAR(64) NOT NULL COMMENT '持锁人 ID',
    `lock_holder_role` VARCHAR(32) NOT NULL COMMENT '持锁人角色',
    `lock_start_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '锁定开始时间',
    `lock_release_time` TIMESTAMP NULL DEFAULT NULL COMMENT '锁定释放时间',
    `lock_release_method` VARCHAR(32) DEFAULT NULL COMMENT '释放方式',
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
-- 五、配置表
-- ============================================================

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
-- 六、改配补款与退款相关表
-- ============================================================

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

-- ============================================================
-- 七、初始化数据
-- ============================================================

INSERT INTO `vso_config_timeout` (`config_key`, `config_name`, `threshold_minutes`, `trigger_strategy`, `remark`) VALUES
('small_order_pay_timeout', '小订单待支付意向金超时', 30, 'invalid', '小订单待支付意向金超时自动失效'),
('formal_order_audit_timeout', '正式订单待审核超时', 1440, 'remind', '正式订单待审核超时仅提醒'),
('deposit_timeout', '正式订单定金待支付超时', 60, 'close', '定金待支付超时自动关闭订单'),
('contract_sign_timeout', '合同待签署超时', 2880, 'remind', '合同待签署超时仅提醒')
ON DUPLICATE KEY UPDATE `config_name` = VALUES(`config_name`);

INSERT INTO `vso_config_vehicle_occupancy` (`occupancy_rule_key`, `occupancy_hours`, `enable_flag`, `priority`, `remark`) VALUES
('GENERAL_RULE', 24, 1, 0, '通用车源占用有效期规则：24 小时')
ON DUPLICATE KEY UPDATE `occupancy_hours` = VALUES(`occupancy_hours`);

SET FOREIGN_KEY_CHECKS = 1;
