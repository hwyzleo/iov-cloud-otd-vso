-- OTD-VSO-DSN-CR-012：出站消息发件箱
-- 与 tb_wms_outbox / tb_tms_outbox 同构，便于跨域一致运维。
CREATE TABLE IF NOT EXISTS `vso_outbox`
(
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `event_id`          VARCHAR(64)  NOT NULL COMMENT '事件唯一 ID',
    `event_type`        VARCHAR(128) NOT NULL COMMENT '事件类型',
    `aggregate_type`    VARCHAR(64)  NOT NULL COMMENT '聚合类型',
    `aggregate_id`      VARCHAR(255) NOT NULL COMMENT '聚合 ID',
    `aggregate_version` BIGINT       NOT NULL COMMENT '聚合版本',
    `topic`             VARCHAR(255) NOT NULL COMMENT '注册的目标 Topic',
    `message_key`       VARCHAR(255) NOT NULL COMMENT 'Kafka Key',
    `payload`           TEXT         NOT NULL COMMENT '不可变 JSON 载荷',
    `publish_state`     VARCHAR(32)  NOT NULL COMMENT '发布状态：PENDING/FAILED_RETRYABLE/PUBLISHED/DEAD',
    `retry_count`       INT          NOT NULL DEFAULT 0 COMMENT '已重试次数',
    `next_retry_time`   DATETIME              DEFAULT NULL COMMENT '下次重试时间',
    `published_at`      DATETIME              DEFAULT NULL COMMENT 'Broker 确认时间',
    `last_error`        VARCHAR(2000)         DEFAULT NULL COMMENT '脱敏的最后错误',
    `description`       VARCHAR(255)          DEFAULT NULL COMMENT '备注',
    `create_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`         VARCHAR(64)           DEFAULT NULL COMMENT '创建者',
    `modify_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by`         VARCHAR(64)           DEFAULT NULL COMMENT '修改者',
    `row_version`       INT                   DEFAULT NULL COMMENT '记录版本',
    `row_valid`         TINYINT               DEFAULT NULL COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_vso_outbox_event_id` (`event_id`),
    KEY `idx_vso_outbox_publish_scan` (`publish_state`, `next_retry_time`),
    KEY `idx_vso_outbox_aggregate` (`aggregate_type`, `aggregate_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='VSO 事务性事件发布记录';

-- 待发运补偿查询索引（order_state + transport_apply_time）
CREATE INDEX `idx_vso_order_state_transport_time` ON `vso_order` (`order_state`, `transport_apply_time`);
