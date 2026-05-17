DROP TABLE IF EXISTS `db_vso`.`tb_sale_model`;
CREATE TABLE `db_vso`.`tb_sale_model`
(
    `id`                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `sale_code`           VARCHAR(50)  NOT NULL COMMENT '销售代码',
    `model_name`          VARCHAR(255) NOT NULL COMMENT '销售车型名称',
    `parameters`          JSON                  DEFAULT NULL COMMENT '销售车型相关参数',
    `images`              JSON                  DEFAULT NULL COMMENT '销售车型图片集',
    `earnest_money`       TINYINT      NOT NULL COMMENT '是否允许意向金',
    `earnest_money_price` DECIMAL(10, 2)        DEFAULT NULL COMMENT '意向金价格',
    `down_payment`        TINYINT      NOT NULL COMMENT '是否允许定金',
    `down_payment_price`  DECIMAL(10, 2)        DEFAULT NULL COMMENT '定金价格',
    `enable`              TINYINT      NOT NULL COMMENT '是否启用',
    `sort`                INT          NOT NULL COMMENT '排序',
    `description`         VARCHAR(255)          DEFAULT NULL COMMENT '备注',
    `create_time`         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`           BIGINT                DEFAULT NULL COMMENT '创建者',
    `modify_time`         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by`           BIGINT                DEFAULT NULL COMMENT '修改者',
    `row_version`         INT                   DEFAULT NULL COMMENT '记录版本',
    `row_valid`           TINYINT               DEFAULT NULL COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY (`sale_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='销售车型';

DROP TABLE IF EXISTS `db_vso`.`tb_sale_model_config`;
CREATE TABLE `db_vso`.`tb_sale_model_config`
(
    `id`          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `sale_code`   VARCHAR(50)    NOT NULL COMMENT '销售代码',
    `type`        VARCHAR(50)    NOT NULL COMMENT '销售车型配置类型',
    `type_code`   VARCHAR(50)    NOT NULL COMMENT '销售车型配置类型代码',
    `type_name`   VARCHAR(255)   NOT NULL COMMENT '销售车型配置类型名称',
    `type_price`  DECIMAL(10, 2) NOT NULL COMMENT '销售车型配置类型价格',
    `type_image`  JSON                    DEFAULT NULL COMMENT '销售车型配置类型图片',
    `type_desc`   TEXT                    DEFAULT NULL COMMENT '销售车型配置类型描述',
    `type_param`  TEXT                    DEFAULT NULL COMMENT '销售车型配置类型参数',
    `enable`      TINYINT        NOT NULL COMMENT '是否启用',
    `sort`        INT            NOT NULL COMMENT '排序',
    `description` VARCHAR(255)            DEFAULT NULL COMMENT '备注',
    `create_time` TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`   BIGINT                  DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by`   BIGINT                  DEFAULT NULL COMMENT '修改者',
    `row_version` INT                     DEFAULT NULL COMMENT '记录版本',
    `row_valid`   TINYINT                 DEFAULT NULL COMMENT '是否有效',
    PRIMARY KEY (`id`),
    KEY `idx_sale_code` (`sale_code`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='销售车型配置';

DROP TABLE IF EXISTS `db_vso`.`tb_purchase_benefits`;
CREATE TABLE `db_vso`.`tb_purchase_benefits`
(
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `sale_code`   VARCHAR(50) NOT NULL COMMENT '销售代码',
    `start_time`  TIMESTAMP   NOT NULL COMMENT '权益开始时间',
    `end_time`    TIMESTAMP   NOT NULL COMMENT '权益结束时间',
    `intro`       TEXT                 DEFAULT NULL COMMENT '权益简介',
    `detail`      TEXT                 DEFAULT NULL COMMENT '权益详情',
    `enable`      TINYINT     NOT NULL COMMENT '是否启用',
    `description` VARCHAR(255)         DEFAULT NULL COMMENT '备注',
    `create_time` TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`   BIGINT               DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by`   BIGINT               DEFAULT NULL COMMENT '修改者',
    `row_version` INT                  DEFAULT NULL COMMENT '记录版本',
    `row_valid`   TINYINT              DEFAULT NULL COMMENT '是否有效',
    PRIMARY KEY (`id`),
    KEY `idx_sale_code` (`sale_code`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='购车权益';

DROP TABLE IF EXISTS `db_vso`.`tb_purchase_agreement`;
CREATE TABLE `db_vso`.`tb_purchase_agreement`
(
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `sale_code`   VARCHAR(50) NOT NULL COMMENT '销售代码',
    `type`        SMALLINT             DEFAULT NULL COMMENT '协议类型：1-意向金（小定），2-定金（大定）',
    `title`       VARCHAR(255)         DEFAULT NULL COMMENT '协议标题',
    `intro`       TEXT                 DEFAULT NULL COMMENT '协议简介',
    `detail`      TEXT                 DEFAULT NULL COMMENT '协议详情',
    `enable`      TINYINT     NOT NULL COMMENT '是否启用',
    `description` VARCHAR(255)         DEFAULT NULL COMMENT '备注',
    `create_time` TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`   BIGINT               DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by`   BIGINT               DEFAULT NULL COMMENT '修改者',
    `row_version` INT                  DEFAULT NULL COMMENT '记录版本',
    `row_valid`   TINYINT              DEFAULT NULL COMMENT '是否有效',
    PRIMARY KEY (`id`),
    KEY `idx_sale_code` (`sale_code`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='购车协议';