DROP TABLE IF EXISTS `db_vso`.`tb_sale_model`;
CREATE TABLE `db_vso`.`tb_sale_model`
(
    `id`                   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `sale_code`            VARCHAR(50)  NOT NULL COMMENT '销售代码',
    `sale_model_type`      VARCHAR(255) NOT NULL COMMENT '销售车型类型',
    `sale_model_type_code` VARCHAR(50)  NOT NULL COMMENT '销售车型类型代码',
    `sale_name`            VARCHAR(255)          DEFAULT NULL COMMENT '销售名称',
    `sale_price`           VARCHAR(255)          DEFAULT NULL COMMENT '销售价格',
    `sale_image`           VARCHAR(255)          DEFAULT NULL COMMENT '销售图片',
    `sale_desc`            TEXT                  DEFAULT NULL COMMENT '销售描述',
    `sale_param`           TEXT                  DEFAULT NULL COMMENT '销售参数',
    `enable`               TINYINT      NOT NULL COMMENT '是否启用',
    `sort`                 INT          NOT NULL COMMENT '排序',
    `description`          VARCHAR(255)          DEFAULT NULL COMMENT '备注',
    `create_time`          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`            BIGINT                DEFAULT NULL COMMENT '创建者',
    `modify_time`          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by`            BIGINT                DEFAULT NULL COMMENT '修改者',
    `row_version`          INT                   DEFAULT NULL COMMENT '记录版本',
    `row_valid`            TINYINT               DEFAULT NULL COMMENT '是否有效',
    PRIMARY KEY (`id`),
    KEY `idx_sale_code` (`sale_code`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='销售车型';

DROP TABLE IF EXISTS `db_vso`.`tb_wishlist`;
CREATE TABLE `db_vso`.`tb_wishlist`
(
    `id`                BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `account_id`        VARCHAR(64) NOT NULL COMMENT '账号唯一ID',
    `series_code`       VARCHAR(50)          DEFAULT NULL COMMENT '车系代码',
    `model_code`        VARCHAR(50)          DEFAULT NULL COMMENT '车型代码',
    `model_config_code` VARCHAR(50)          DEFAULT NULL COMMENT '车型配置代码',
    `price`             DECIMAL(10, 2)       DEFAULT NULL COMMENT '车辆价格',
    `intention_money`   DECIMAL(10, 2)       DEFAULT NULL COMMENT '意向金',
    `is_order`          TINYINT              DEFAULT NULL COMMENT '是否转订单',
    `description`       VARCHAR(255)         DEFAULT NULL COMMENT '备注',
    `create_time`       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`         BIGINT               DEFAULT NULL COMMENT '创建者',
    `modify_time`       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by`         BIGINT               DEFAULT NULL COMMENT '修改者',
    `row_version`       INT                  DEFAULT NULL COMMENT '记录版本',
    `row_valid`         TINYINT              DEFAULT NULL COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY (`account_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='心愿单';