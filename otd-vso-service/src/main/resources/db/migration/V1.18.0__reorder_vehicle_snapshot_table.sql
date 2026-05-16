-- ============================================================
-- VSO 车辆销售订单系统 - 数据库迁移脚本
-- Flyway 版本：V1.18.0
-- 描述：重构订单车型配置快照表，字段与业务概念对齐
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 备份旧表（如果存在数据）
DROP TABLE IF EXISTS vso_order_vehicle_snapshot_backup;
CREATE TABLE vso_order_vehicle_snapshot_backup LIKE vso_order_vehicle_snapshot;
INSERT INTO vso_order_vehicle_snapshot_backup SELECT * FROM vso_order_vehicle_snapshot;

-- 删除旧表
DROP TABLE IF EXISTS vso_order_vehicle_snapshot;

-- 创建新表
CREATE TABLE vso_order_vehicle_snapshot (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    snapshot_id VARCHAR(64) NOT NULL COMMENT '快照业务 ID',
    order_id VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
    
    -- 销售车型维度
    sale_model_code VARCHAR(64) NOT NULL COMMENT '销售车型代码',
    sale_model_name VARCHAR(128) NOT NULL COMMENT '销售车型名称',
    
    -- 生产配置维度
    build_config_code VARCHAR(64) NOT NULL COMMENT '生产配置代码',
    build_config_name VARCHAR(256) NOT NULL COMMENT '生产配置名称',
    
    -- 特征值快照（完整保留用户选择）
    feature_config_snapshot JSON NOT NULL COMMENT '特征值选择快照（VMD featureCodes数组）',
    
    -- 元数据
    snapshot_version INT NOT NULL DEFAULT 1 COMMENT '快照版本号',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建者',
    modify_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    modify_by BIGINT DEFAULT NULL COMMENT '修改者',
    row_version INT DEFAULT 0 COMMENT '记录版本',
    row_valid TINYINT DEFAULT 1 COMMENT '是否有效',
    
    PRIMARY KEY (id),
    UNIQUE KEY uk_snapshot_id (snapshot_id),
    UNIQUE KEY uk_order_snapshot_valid (order_id, row_valid),
    KEY idx_sale_model (sale_model_code),
    KEY idx_build_config (build_config_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单车型配置快照表';

SET FOREIGN_KEY_CHECKS = 1;