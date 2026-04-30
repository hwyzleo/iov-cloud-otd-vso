# VSO 数据库版本管理说明

## Flyway 版本化策略

本项目使用 Flyway 进行数据库版本管理，所有 DDL 变更通过版本化迁移脚本管理。

## 目录结构

```
vso-service/src/main/resources/db/migration/
├── V1.0.0__create_base_tables.sql              # 基础业务表（车型、配置、权益、协议）
├── V1.1.0__create_order_core_tables.sql        # 订单核心表（主档、客户、快照、金额、归属、维度状态）
├── V1.2.0__create_order_business_tables.sql    # 订单业务流程表（审批、配车、合同、支付、退款、金融、补贴、交付、上牌、发票）
├── V1.3.0__create_audit_support_tables.sql     # 审计与支撑表（异常、回调、版本、时间线、审计、通知、超时、锁、转化、影子记录）
└── V1.4.0__create_config_tables.sql            # 配置表（超时配置、车源占用配置）及初始化数据
```

## 版本号规则

采用语义化版本号：`V{主版本}.{次版本}.{补丁版本}__{描述}.sql`

- **主版本**：重大架构变更（如 1.x.x → 2.x.x）
- **次版本**：新增功能模块（如 1.0.x → 1.1.x）
- **补丁版本**：小功能或修复（如 1.1.0 → 1.1.1）

## 当前版本

**V1.4.0** - 首版完整数据库结构

### 表清单（35 张）

#### 基础业务表（4 张）
1. `tb_sale_model` - 销售车型
2. `tb_sale_model_config` - 销售车型配置
3. `tb_purchase_benefits` - 购车权益
4. `tb_purchase_agreement` - 购车协议

#### 订单核心表（6 张）
5. `vso_order` - 订单主表
6. `vso_order_party` - 订单客户与购车人信息
7. `vso_order_vehicle_snapshot` - 订单车型配置快照
8. `vso_order_amount` - 订单金额口径
9. `vso_order_assignment` - 订单归属与转派
10. `vso_order_status_dimension` - 订单维度状态

#### 订单业务流程表（14 张）
11. `vso_order_material` - 订单资料主表
12. `vso_order_material_version` - 订单资料版本
13. `vso_approval` - 审批单主表
14. `vso_approval_record` - 审批流转记录
15. `vso_vehicle_assignment` - 配车与车辆绑定
16. `vso_contract` - 合同/协议/授权文件
17. `vso_payment` - 支付记录
18. `vso_refund` - 退款记录
19. `vso_finance_application` - 金融申请
20. `vso_subsidy_application` - 补贴申请
21. `vso_delivery_appointment` - 交付预约
22. `vso_delivery_record` - 交付完成记录
23. `vso_registration` - 上牌跟踪
24. `vso_invoice` - 发票

#### 审计与支撑表（9 张）
25. `vso_exception_order` - 异常单
26. `vso_callback_log` - 外部回调日志
27. `vso_order_version` - 订单版本
28. `vso_order_version_diff` - 订单版本差异
29. `vso_order_timeline` - 订单业务时间线
30. `vso_audit_log` - 系统审计日志
31. `vso_notify_task` - 通知任务
32. `vso_timeout_task` - 超时任务
33. `vso_order_shadow_delete` - 物理删除审计影子记录
34. `vso_order_lock` - 订单锁记录
35. `vso_order_transform` - 小订单转正式订单转化关系

#### 配置表（2 张）
36. `vso_config_timeout` - 超时任务配置
37. `vso_config_vehicle_occupancy` - 车源占用有效期配置

## 执行顺序

Flyway 会按版本号顺序自动执行迁移脚本：

1. V1.0.0 → V1.1.0 → V1.2.0 → V1.3.0 → V1.4.0

## 新增迁移脚本

### 开发阶段新增表

按下一个版本号创建，例如：
- `V1.5.0__create_xxx_tables.sql`

### 修改现有表结构

使用 `ALTER TABLE` 语句，创建新补丁版本：
- `V1.1.1__alter_order_add_column.sql`
- `V1.2.1__add_index_xxx.sql`

### 数据修复/初始化

使用 `R` 前缀创建可重复执行的脚本：
- `R__init_xxx_data.sql`

## 配置说明

Flyway 配置在 `bootstrap.yml` 中：

```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    encoding: UTF-8
```

## 注意事项

1. **禁止修改历史版本**：已发布的版本脚本禁止修改，只能新增版本
2. **回滚策略**：Flyway 不支持自动回滚，需要手动编写向下兼容的迁移脚本
3. **数据备份**：执行生产环境迁移前务必备份数据
4. **幂等性**：尽量使用 `CREATE TABLE IF NOT EXISTS` 和 `ON DUPLICATE KEY UPDATE`
5. **字符集**：统一使用 `utf8mb4`
6. **存储引擎**：统一使用 `InnoDB`

## 相关文档

- 需求文档：`doc/spec/requirements.md`
- 设计文档：`doc/spec/design.md`
- 实施计划：`doc/spec/tasks.md`
