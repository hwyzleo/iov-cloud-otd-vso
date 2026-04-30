-- ============================================================
-- VSO 车辆销售订单系统 - 审计与支撑表
-- Flyway 版本：V1.3.0
-- 描述：创建异常单、回调日志、版本、时间线、审计日志、通知、超时任务、锁记录、转化关系、影子记录表
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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
    `origin_small_order_no` VARCHAR(64) DEFAULT NULL COMMENT '原小订单号',
    `delete_approval_id` VARCHAR(64) DEFAULT NULL COMMENT '删除审批业务 ID',
    `delete_reason` VARCHAR(255) DEFAULT NULL COMMENT '删除原因',
    `before_main_status` VARCHAR(32) DEFAULT NULL COMMENT '删除前主状态',
    `compliance_delete_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否因合规要求删除',
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

-- 小订单转正式订单转化关系表
CREATE TABLE IF NOT EXISTS `vso_order_transform` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `transform_id` VARCHAR(64) NOT NULL COMMENT '转化业务 ID',
    `small_order_id` VARCHAR(64) NOT NULL COMMENT '小订单业务 ID',
    `small_order_no` VARCHAR(64) NOT NULL COMMENT '小订单号',
    `formal_order_id` VARCHAR(64) NOT NULL COMMENT '正式订单业务 ID',
    `formal_order_no` VARCHAR(64) NOT NULL COMMENT '正式订单号',
    `transform_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '转化时间',
    `transform_user_id` VARCHAR(64) NOT NULL COMMENT '转化操作人 ID',
    `transform_user_role` VARCHAR(32) NOT NULL COMMENT '转化操作人角色',
    `transform_scene` VARCHAR(32) NOT NULL DEFAULT 'normal' COMMENT '转化场景',
    `transform_remark` VARCHAR(255) DEFAULT NULL COMMENT '转化备注',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by` BIGINT DEFAULT NULL COMMENT '修改者',
    `row_version` INT DEFAULT 0 COMMENT '记录版本',
    `row_valid` TINYINT DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_transform_id` (`transform_id`),
    UNIQUE KEY `uk_small_order_transform` (`small_order_id`),
    KEY `idx_formal_order_transform` (`formal_order_id`),
    KEY `idx_transform_time` (`transform_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='小订单转正式订单转化关系表';

SET FOREIGN_KEY_CHECKS = 1;
