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

DROP TABLE IF EXISTS `db_vso`.`tb_order`;
CREATE TABLE `db_vso`.`tb_order`
(
    `id`                          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_num`                   VARCHAR(50) NOT NULL COMMENT '订单编码',
    `order_state`                 INT         NOT NULL COMMENT '订单状态',
    `order_state_time`            TIMESTAMP   NOT NULL COMMENT '订单状态时间',
    `order_time`                  TIMESTAMP            DEFAULT NULL COMMENT '下单时间',
    `cancel_time`                 TIMESTAMP            DEFAULT NULL COMMENT '取消时间',
    `benefits_id`                 BIGINT               DEFAULT NULL COMMENT '购车权益ID',
    `agreement_id`                BIGINT               DEFAULT NULL COMMENT '购车协议ID',
    `contract_num`                VARCHAR(50)          DEFAULT NULL COMMENT '购车合同号',
    `contract_time`               TIMESTAMP            DEFAULT NULL COMMENT '购车合同签署时间',
    `sale_code`                   VARCHAR(50) NOT NULL COMMENT '销售代码',
    `model_config_code`           VARCHAR(50) NOT NULL COMMENT '车型配置代码',
    `model_config_lock`           TINYINT              DEFAULT 0 COMMENT '车型配置是否锁定',
    `pay_state`                   SMALLINT             DEFAULT NULL COMMENT '支付状态：10-意向金已支付，15-意向金已退，20-定金已支付，25-定金已退，30-尾款已支付，35-尾款已退',
    `earnest_money_time`          TIMESTAMP            DEFAULT NULL COMMENT '意向金支付时间',
    `earnest_money_amount`        DECIMAL(10, 2)       DEFAULT NULL COMMENT '意向金支付金额',
    `down_payment_time`           TIMESTAMP            DEFAULT NULL COMMENT '定金支付时间',
    `down_payment_amount`         DECIMAL(10, 2)       DEFAULT NULL COMMENT '定金支付金额',
    `lock_time`                   TIMESTAMP            DEFAULT NULL COMMENT '锁单时间',
    `assign_time`                 TIMESTAMP            DEFAULT NULL COMMENT '配车时间',
    `revoke_time`                 TIMESTAMP            DEFAULT NULL COMMENT '解配时间',
    `reassign_time`               TIMESTAMP            DEFAULT NULL COMMENT '改配时间',
    `transport_apply_time`        TIMESTAMP            DEFAULT NULL COMMENT '发运申请时间',
    `final_payment_time`          TIMESTAMP            DEFAULT NULL COMMENT '尾款支付时间',
    `order_type`                  SMALLINT             DEFAULT NULL COMMENT '订单类型：1-C端普通客户，2-C端大客户，3-B端大客户，4-员工内购，5-员工推荐，6-内部用车，7-媒体用车，8-展车用车',
    `order_person_id`             VARCHAR(64)          DEFAULT NULL COMMENT '下单人员ID',
    `order_person_type`           SMALLINT             DEFAULT NULL COMMENT '下单人员类型：1-个人，2-企业',
    `order_person_name`           VARCHAR(50)          DEFAULT NULL COMMENT '下单人员姓名',
    `order_person_phone`          VARCHAR(50)          DEFAULT NULL COMMENT '下单人员电话',
    `order_person_id_type`        SMALLINT             DEFAULT NULL COMMENT '下单人员证件类型：1-身份证',
    `order_person_id_num`         VARCHAR(50)          DEFAULT NULL COMMENT '下单人员证件号',
    `recommender_id`              VARCHAR(64)          DEFAULT NULL COMMENT '推荐用户ID',
    `contact_person_name`         VARCHAR(50)          DEFAULT NULL COMMENT '联系人姓名',
    `contact_person_phone`        VARCHAR(50)          DEFAULT NULL COMMENT '联系人电话',
    `sales_person_id`             VARCHAR(64)          DEFAULT NULL COMMENT '销售人员ID',
    `sales_person_name`           VARCHAR(50)          DEFAULT NULL COMMENT '销售人员姓名',
    `sales_person_phone`          VARCHAR(50)          DEFAULT NULL COMMENT '销售人员电话',
    `sales_person_order`          TINYINT              DEFAULT 0 COMMENT '销售人员是否代理下单',
    `sales_channel`               VARCHAR(50)          DEFAULT NULL COMMENT '销售渠道',
    `purchase_plan`               SMALLINT             DEFAULT NULL COMMENT '购车方案：1-全款购车，2-金融贷款',
    `financing_plan_id`           BIGINT               DEFAULT NULL COMMENT '购车金融方案ID',
    `delivery_vin`                VARCHAR(17)          DEFAULT NULL COMMENT '交付车辆',
    `delivery_city`               VARCHAR(50)          DEFAULT NULL COMMENT '交付城市',
    `delivery_method`             VARCHAR(50)          DEFAULT NULL COMMENT '交付方式：1-现场交付，2-远程交付，3-钣喷交付',
    `transport_apply_person_id`   VARCHAR(64)          DEFAULT NULL COMMENT '发运申请人员ID',
    `transport_apply_person_name` VARCHAR(50)          DEFAULT NULL COMMENT '发运申请人员姓名',
    `delivery_person_id`          VARCHAR(64)          DEFAULT NULL COMMENT '交付人员ID',
    `delivery_person_name`        VARCHAR(50)          DEFAULT NULL COMMENT '交付人员姓名',
    `delivery_remark`             VARCHAR(255)         DEFAULT NULL COMMENT '交付备注',
    `reception_person_id`         VARCHAR(64)          DEFAULT NULL COMMENT '接待人员ID',
    `reception_person_name`       VARCHAR(50)          DEFAULT NULL COMMENT '接待人员姓名',
    `invoicing_time`              TIMESTAMP            DEFAULT NULL COMMENT '开票时间',
    `estimated_delivery_time`     TIMESTAMP            DEFAULT NULL COMMENT '预计交付时间',
    `actual_delivery_time`        TIMESTAMP            DEFAULT NULL COMMENT '实际交付时间',
    `license_city`                VARCHAR(50)          DEFAULT NULL COMMENT '上牌城市',
    `dealership`                  VARCHAR(50)          DEFAULT NULL COMMENT '销售门店',
    `delivery_center`             VARCHAR(50)          DEFAULT NULL COMMENT '交付中心',
    `description`                 VARCHAR(255)         DEFAULT NULL COMMENT '备注',
    `create_time`                 TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`                   BIGINT               DEFAULT NULL COMMENT '创建者',
    `modify_time`                 TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by`                   BIGINT               DEFAULT NULL COMMENT '修改者',
    `row_version`                 INT                  DEFAULT NULL COMMENT '记录版本',
    `row_valid`                   TINYINT              DEFAULT NULL COMMENT '是否有效',
    PRIMARY KEY (`id`),
    UNIQUE KEY (`order_num`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='车辆销售订单';

DROP TABLE IF EXISTS `db_vso`.`tb_order_log`;
CREATE TABLE `db_vso`.`tb_order_log`
(
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_num`      VARCHAR(50)  NOT NULL COMMENT '订单编码',
    `operate_client` VARCHAR(255) NOT NULL COMMENT '操作终端',
    `account_id`     VARCHAR(64)  NOT NULL COMMENT '操作者ID',
    `operator`       VARCHAR(255)          DEFAULT NULL COMMENT '操作者',
    `operate_type`   VARCHAR(255) NOT NULL COMMENT '操作类型',
    `operate_desc`   VARCHAR(2000)         DEFAULT NULL COMMENT '操作描述',
    `operate_time`   TIMESTAMP    NOT NULL COMMENT '操作时间',
    `description`    VARCHAR(255)          DEFAULT NULL COMMENT '备注',
    `create_time`    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`      BIGINT                DEFAULT NULL COMMENT '创建者',
    `modify_time`    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by`      BIGINT                DEFAULT NULL COMMENT '修改者',
    `row_version`    INT                   DEFAULT NULL COMMENT '记录版本',
    `row_valid`      TINYINT               DEFAULT NULL COMMENT '是否有效',
    PRIMARY KEY (`id`),
    KEY `idx_order_num` (`order_num`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='车辆销售订单日志';

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

DROP TABLE IF EXISTS `db_vso`.`tb_order_payment`;
CREATE TABLE `db_vso`.`tb_order_payment`
(
    `id`                  BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_num`           VARCHAR(50)    NOT NULL COMMENT '订单编码',
    `order_payment_phase` SMALLINT       NOT NULL COMMENT '订单支付阶段：1-意向金，2-定金，3-尾款',
    `payment_merchant`    VARCHAR(50)    NOT NULL COMMENT '支付商户',
    `payment_order`       VARCHAR(50)    NOT NULL COMMENT '支付内部订单号',
    `payment_reference`   VARCHAR(50)    NOT NULL COMMENT '支付流水号',
    `payment_amount`      DECIMAL(10, 2) NOT NULL COMMENT '支付金额',
    `payment_channel`     VARCHAR(50)    NOT NULL COMMENT '支付渠道：UNION_PAY-银联，WECHAT-微信，ALIPAY-支付宝',
    `payment_data_type`   SMALLINT       NOT NULL COMMENT '支付数据类型：1-支付URL，2-URL二维码，3-BASE64图片，4-JSON数据，5-FORM表单数据，6-支付成功URL，7-预下单支付，8-JSAPI小程序数据',
    `state`               SMALLINT       NOT NULL COMMENT '支付状态',
    `description`         VARCHAR(255)            DEFAULT NULL COMMENT '备注',
    `create_time`         TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`           BIGINT                  DEFAULT NULL COMMENT '创建者',
    `modify_time`         TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `modify_by`           BIGINT                  DEFAULT NULL COMMENT '修改者',
    `row_version`         INT                     DEFAULT NULL COMMENT '记录版本',
    `row_valid`           TINYINT                 DEFAULT NULL COMMENT '是否有效',
    PRIMARY KEY (`id`),
    KEY `idx_order_num` (`order_num`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='订单支付记录';