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

DROP TABLE IF EXISTS `db_vso`.`tb_order`;
CREATE TABLE `db_vso`.`tb_order`
(
    `id`                      BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_num`               VARCHAR(50) NOT NULL COMMENT '订单编码',
    `order_state`             INT         NOT NULL COMMENT '订单状态：100-心愿单',
    `sale_code`               VARCHAR(50) NOT NULL COMMENT '销售代码',
    `model_config_code`       VARCHAR(50) NOT NULL COMMENT '车型配置代码',
    `model_config_lock`       TINYINT              DEFAULT 0 COMMENT '车型配置是否锁定',
    `pay_state`               INT                  DEFAULT NULL COMMENT '支付状态',
    `order_type`              INT                  DEFAULT NULL COMMENT '订单类型：1-C端普通客户，2-C端大客户，3-B端大客户，4-员工内购，5-员工推荐，6-内部用车，7-媒体用车，8-展车用车',
    `order_person_id`         VARCHAR(64)          DEFAULT NULL COMMENT '下单人员ID',
    `order_person_name`       VARCHAR(50)          DEFAULT NULL COMMENT '下单人员姓名',
    `order_person_phone`      VARCHAR(50)          DEFAULT NULL COMMENT '下单人员电话',
    `recommender_id`          VARCHAR(64)          DEFAULT NULL COMMENT '推荐用户ID',
    `contact_person_name`     VARCHAR(50)          DEFAULT NULL COMMENT '联系人姓名',
    `contact_person_phone`    VARCHAR(50)          DEFAULT NULL COMMENT '联系人电话',
    `sales_person_id`         VARCHAR(64)          DEFAULT NULL COMMENT '销售人员ID',
    `sales_person_name`       VARCHAR(50)          DEFAULT NULL COMMENT '销售人员姓名',
    `sales_person_phone`      VARCHAR(50)          DEFAULT NULL COMMENT '销售人员电话',
    `sales_person_order`      TINYINT              DEFAULT 0 COMMENT '销售人员是否代理下单',
    `sales_channel`           VARCHAR(50)          DEFAULT NULL COMMENT '销售渠道',
    `buy_plan`                VARCHAR(50)          DEFAULT NULL COMMENT '购车方案：1-全款购车，2-金融贷款',
    `delivery_city`           VARCHAR(50)          DEFAULT NULL COMMENT '交付城市',
    `delivery_method`         VARCHAR(50)          DEFAULT NULL COMMENT '交付方式：1-现场交付，2-远程交付，3-钣喷交付',
    `delivery_person_id`      VARCHAR(64)          DEFAULT NULL COMMENT '交付人员ID',
    `delivery_person_name`    VARCHAR(50)          DEFAULT NULL COMMENT '交付人员姓名',
    `delivery_remark`         VARCHAR(255)         DEFAULT NULL COMMENT '交付备注',
    `reception_person_id`     VARCHAR(64)          DEFAULT NULL COMMENT '接待人员ID',
    `reception_person_name`   VARCHAR(50)          DEFAULT NULL COMMENT '接待人员姓名',
    `invoicing_time`          TIMESTAMP            DEFAULT NULL COMMENT '开票时间',
    `estimated_delivery_time` TIMESTAMP            DEFAULT NULL COMMENT '预计交付时间',
    `actual_delivery_time`    TIMESTAMP            DEFAULT NULL COMMENT '实际交付时间',
    `license_city`            VARCHAR(50)          DEFAULT NULL COMMENT '上牌城市',
    `description`             VARCHAR(255)         DEFAULT NULL COMMENT '备注',
    `create_time`             TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`               BIGINT               DEFAULT NULL COMMENT '创建者',
    `modify_time`             TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by`               BIGINT               DEFAULT NULL COMMENT '修改者',
    `row_version`             INT                  DEFAULT NULL COMMENT '记录版本',
    `row_valid`               TINYINT              DEFAULT NULL COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY (`order_num`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='车辆销售订单';

DROP TABLE IF EXISTS `db_vso`.`tb_order_model_config`;
CREATE TABLE `db_vso`.`tb_order_model_config`
(
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_num`   VARCHAR(50) NOT NULL COMMENT '订单编码',
    `type`        VARCHAR(50) NOT NULL COMMENT '销售车型配置类型',
    `type_code`   VARCHAR(50) NOT NULL COMMENT '销售车型配置类型代码',
    `type_name`   VARCHAR(255)         DEFAULT NULL COMMENT '销售车型配置类型名称',
    `type_price`  DECIMAL(10, 2)       DEFAULT NULL COMMENT '销售车型配置类型价格',
    `description` VARCHAR(255)         DEFAULT NULL COMMENT '备注',
    `create_time` TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`   BIGINT               DEFAULT NULL COMMENT '创建者',
    `modify_time` TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by`   BIGINT               DEFAULT NULL COMMENT '修改者',
    `row_version` INT                  DEFAULT NULL COMMENT '记录版本',
    `row_valid`   TINYINT              DEFAULT NULL COMMENT '是否有效',
    PRIMARY KEY (`id`),
    KEY `idx_order_num` (`order_num`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='订单车型配置';