# Vehicle Sale Order (VSO) - Requirements

## 1. Overview

车辆销售订单服务（VSO）为车联网平台提供从心愿单到交付激活的完整车辆销售订单全生命周期管理能力。

## 2. Background & Goals

- 背景：车联网 OTD（Order-To-Delivery）链路中，需要一个独立的车辆销售订单微服务，承载 C 端用户下单、支付、改配，以及 B 端运营管理订单、配车、发运、交付等业务流程。
- 目标：
  - 支持 C 端用户通过移动端完成心愿单管理、意向金/定金下单、支付、改配、取消等操作
  - 支持 B 端运营通过管理平台完成订单审核、锁单、配车、发运、交付、关闭等全流程管理
  - 支持销售车型及配置的管理与同步
  - 支持多支付渠道接入与回调处理
  - 支持服务间调用完成发运与交付状态流转
- 非目标（明确不做）：
  - 不负责车辆模型数据（VMD）的维护，仅消费
  - 不负责支付网关的实现，仅对接回调
  - 不负责电子签章、金融、补贴、发票的具体业务逻辑（仅预留回调接口）

## 3. User Stories

### US-001: 浏览销售车型

**As a** C 端用户, **I want** 查看当前在售车型列表及详情, **so that** 我能了解可购买的车型信息并选择配置。

**Acceptance Criteria** (EARS 语法):
- WHEN 用户请求销售车型列表 THE SYSTEM SHALL 返回所有已上架的销售车型基本信息（编码、名称、图片、价格）
- WHEN 用户请求指定车型的配置列表 THE SYSTEM SHALL 返回该车型下所有可选配置项（按配置族分组）
- WHEN 用户请求指定车型的可选特征码范围 THE SYSTEM SHALL 调用 VMD 服务获取动态配置范围并返回
- WHEN 用户提交已选特征码组合 THE SYSTEM SHALL 解析匹配的 buildConfigCode 并返回对应的车型配置快照
- WHEN 用户请求购车权益 THE SYSTEM SHALL 返回该车型当前有效的购车权益信息
- WHEN 用户请求上牌地区列表 THE SYSTEM SHALL 返回可选的省市区域列表

### US-002: 心愿单管理

**As a** C 端用户, **I want** 创建、修改和删除心愿单, **so that** 我能保存感兴趣的车型配置以便后续下单。

**Acceptance Criteria** (EARS 语法):
- WHEN 用户提交创建心愿单请求（含车型编码和特征码） THE SYSTEM SHALL 校验 buildConfigCode 有效性后创建心愿单并返回心愿单 ID
- WHEN 用户提交修改心愿单请求 THE SYSTEM SHALL 更新心愿单的配置信息
- WHEN 用户提交删除心愿单请求 THE SYSTEM SHALL 逻辑删除指定心愿单
- WHEN 用户请求心愿单详情 THE SYSTEM SHALL 返回心愿单信息并标注配置是否仍然有效（buildConfig 是否仍启用）
- WHEN 用户请求"我的车辆"列表 THE SYSTEM SHALL 合并返回用户的心愿单和订单列表

### US-003: 意向金下单（小定）

**As a** C 端用户, **I want** 支付意向金创建小定订单, **so that** 我能以较低成本锁定购车意向。

**Acceptance Criteria** (EARS 语法):
- WHEN 用户提交意向金下单请求（含车型编码、特征码、客户信息） THE SYSTEM SHALL 生成唯一订单号、创建 SMALL 类型订单、状态设为 EARNEST_MONEY_UNPAID、创建车辆配置快照（版本 1）
- WHEN 意向金订单创建成功 THE SYSTEM SHALL 自动删除该用户的所有心愿单
- WHEN 意向金订单创建成功 THE SYSTEM SHALL 返回支付渠道信息和支付过期时间
- IF buildConfigCode 无法从特征码解析 THEN THE SYSTEM SHALL 拒绝下单并返回 BuildConfigNotMatchedException
- IF brandCode 不存在 THEN THE SYSTEM SHALL 拒绝下单并返回 BrandCodeNotExistException

### US-004: 定金下单（大定）

**As a** C 端用户, **I want** 支付定金创建大定订单, **so that** 我能正式确认购车并进入生产排期。

**Acceptance Criteria** (EARS 语法):
- WHEN 用户提交定金下单请求 THE SYSTEM SHALL 生成唯一订单号、创建 FORMAL 类型订单、状态设为 DOWN_PAYMENT_UNPAID、创建车辆配置快照（版本 1）
- WHEN 定金订单创建成功 THE SYSTEM SHALL 自动删除该用户的所有心愿单
- WHEN 定金订单创建成功 THE SYSTEM SHALL 返回支付渠道信息和支付过期时间
- IF buildConfigCode 无法从特征码解析 THEN THE SYSTEM SHALL 拒绝下单并返回 BuildConfigNotMatchedException

### US-005: 订单支付

**As a** C 端用户, **I want** 通过多种支付渠道完成订单支付, **so that** 我能完成购车付款流程。

**Acceptance Criteria** (EARS 语法):
- WHEN 用户发起支付请求（含订单号和支付渠道） THE SYSTEM SHALL 创建支付记录并返回支付凭证信息
- WHEN 支付网关回调通知支付成功 THE SYSTEM SHALL 校验支付记录存在、状态为 PENDING_PAYMENT、金额匹配后更新支付状态为 PAID
- WHEN 支付成功且订单状态为 EARNEST_MONEY_UNPAID THE SYSTEM SHALL 将订单状态流转为 EARNEST_MONEY_PAID
- WHEN 支付成功且订单状态为 DOWN_PAYMENT_UNPAID THE SYSTEM SHALL 将订单状态流转为 DOWN_PAYMENT_PAID
- WHEN 支付回调到达 THE SYSTEM SHALL 使用幂等键防止重复处理
- IF 订单状态不是 EARNEST_MONEY_UNPAID 或 DOWN_PAYMENT_UNPAID THEN THE SYSTEM SHALL 拒绝发起支付
- WHILE 支付处理中 THE SYSTEM SHALL 通过分布式锁保证并发安全

### US-006: 意向金转定金

**As a** C 端用户, **I want** 将已支付意向金的小定订单升级为大定订单, **so that** 我能正式确认购车。

**Acceptance Criteria** (EARS 语法):
- WHEN 用户提交意向金转定金请求 THE SYSTEM SHALL 将订单类型从 SMALL 变更为 FORMAL、状态从 EARNEST_MONEY_PAID 流转为 DOWN_PAYMENT_PAID
- IF 订单当前状态不是 EARNEST_MONEY_PAID THEN THE SYSTEM SHALL 拒绝操作

### US-007: 订单改配

**As a** C 端用户, **I want** 在锁单前修改订单的车辆配置, **so that** 我能调整车辆选装方案。

**Acceptance Criteria** (EARS 语法):
- WHEN 用户提交改配请求（含新的特征码组合） THE SYSTEM SHALL 解析新的 buildConfigCode、创建新版本车辆配置快照、记录时间线事件
- IF 订单状态不是 EARNEST_MONEY_PAID、DOWN_PAYMENT_UNPAID 或 DOWN_PAYMENT_PAID THEN THE SYSTEM SHALL 拒绝改配
- IF 订单已锁单（buildConfigLock=true） THEN THE SYSTEM SHALL 拒绝改配并返回 SaleModelConfigHasLockedException

### US-008: 订单取消与退款

**As a** C 端用户, **I want** 取消订单并申请退款, **so that** 我能在不需要时终止购车流程。

**Acceptance Criteria** (EARS 语法):
- WHEN 用户提交取消订单请求 THE SYSTEM SHALL 将订单状态设为 CANCEL
- WHEN 用户提交退款申请 THE SYSTEM SHALL 将订单状态设为 REFUND_APPLY
- WHILE 取消/退款操作执行中 THE SYSTEM SHALL 通过分布式锁保证并发安全

### US-009: 订单锁单

**As a** B 端运营人员, **I want** 锁定已支付定金的订单, **so that** 冻结车辆配置并进入生产排期。

**Acceptance Criteria** (EARS 语法):
- WHEN 运营人员对 DOWN_PAYMENT_PAID 状态的订单执行锁单 THE SYSTEM SHALL 将订单状态流转为 ARRANGE_PRODUCTION 并设置 buildConfigLock=true
- WHEN 锁单成功 THE SYSTEM SHALL 记录锁单时间
- IF 订单状态不是 DOWN_PAYMENT_PAID THEN THE SYSTEM SHALL 拒绝锁单操作

### US-010: 订单审核

**As a** B 端运营人员, **I want** 审核订单通过或驳回, **so that** 确保订单信息合规后进入后续流程。

**Acceptance Criteria** (EARS 语法):
- WHEN 运营人员审核通过订单 THE SYSTEM SHALL 记录操作人并更新审核状态
- WHEN 运营人员审核驳回订单 THE SYSTEM SHALL 记录驳回原因和操作人并更新审核状态
- WHEN 审核操作执行 THE SYSTEM SHALL 记录操作人 ID（通过 X-Operator-Id 请求头传递）

### US-011: 配车（分配 VIN）

**As a** B 端运营人员, **I want** 为订单分配具体车辆（VIN）, **so that** 将生产出的车辆与订单绑定。

**Acceptance Criteria** (EARS 语法):
- WHEN 运营人员提交配车请求（含订单号和 VIN） THE SYSTEM SHALL 将 VIN 绑定到订单并更新订单状态为 ALLOCATION_VEHICLE
- WHEN 配车成功 THE SYSTEM SHALL 创建车辆分配记录

### US-012: 发运管理

**As a** B 端运营人员, **I want** 管理订单的发运流程, **so that** 跟踪车辆从工厂到交付中心的物流状态。

**Acceptance Criteria** (EARS 语法):
- WHEN 运营人员提交发运申请 THE SYSTEM SHALL 将订单状态流转为 APPLY_TRANSPORT 并记录发运申请时间
- WHEN 服务间调用通知准备发运 THE SYSTEM SHALL 将订单状态流转为 PREPARE_TRANSPORT
- WHEN 服务间调用通知发运中 THE SYSTEM SHALL 将订单状态流转为 TRANSPORTING

### US-013: 交付管理

**As a** B 端运营人员, **I want** 管理订单的交付流程, **so that** 完成车辆从交付中心到客户手中的最后环节。

**Acceptance Criteria** (EARS 语法):
- WHEN 运营人员分配交付人员 THE SYSTEM SHALL 记录交付人员信息到订单
- WHEN 服务间调用通知准备交付 THE SYSTEM SHALL 将订单状态流转为 PREPARE_DELIVER
- WHEN 服务间调用通知已交付 THE SYSTEM SHALL 将订单状态流转为 DELIVERED
- WHEN 服务间调用通知已激活 THE SYSTEM SHALL 将订单状态流转为 ACTIVATED

### US-014: 订单关闭

**As a** B 端运营人员, **I want** 关闭异常或无效订单, **so that** 终止不再需要的订单流程。

**Acceptance Criteria** (EARS 语法):
- WHEN 运营人员提交关闭订单请求（含关闭原因） THE SYSTEM SHALL 将订单状态设为 CLOSED 并记录关闭原因和操作人

### US-015: 订单物理删除

**As a** B 端运营人员, **I want** 物理删除已取消的订单及其所有关联数据, **so that** 清理测试或无效数据。

**Acceptance Criteria** (EARS 语法):
- WHEN 运营人员提交物理删除请求 THE SYSTEM SHALL 删除订单主表及所有关联数据（参与方、快照、金额、分配、支付、退款等）并创建影子审计记录
- IF 订单状态不是 CANCEL THEN THE SYSTEM SHALL 拒绝物理删除
- WHEN 物理删除执行 THE SYSTEM SHALL 校验操作人具有 `completeVehicle:order:info:physicalDelete` 权限

### US-016: 销售车型管理（MPT）

**As a** B 端运营人员, **I want** 管理销售车型的基本信息和配置, **so that** 维护 C 端用户可见的在售车型数据。

**Acceptance Criteria** (EARS 语法):
- WHEN 运营人员创建销售车型 THE SYSTEM SHALL 保存车型编码、名称、意向金价格、定金价格、图片等信息并返回 ID
- WHEN 运营人员更新销售车型 THE SYSTEM SHALL 更新对应字段
- WHEN 运营人员删除销售车型 THE SYSTEM SHALL 逻辑删除指定车型
- WHEN 运营人员查询销售车型列表 THE SYSTEM SHALL 支持按编码、名称、时间范围分页查询
- WHEN 运营人员管理车型配置 THE SYSTEM SHALL 支持配置项的查看、更新、排序操作
- WHEN 运营人员执行同步配置 THE SYSTEM SHALL 从 VMD 的 buildConfig 同步配置数据到本地
- WHEN 运营人员管理基础车型 THE SYSTEM SHALL 支持基础车型的查看、更新、同步操作

### US-017: 订单列表与详情查询（MPT）

**As a** B 端运营人员, **I want** 查询和查看订单列表及详情, **so that** 了解订单状态并进行后续操作。

**Acceptance Criteria** (EARS 语法):
- WHEN 运营人员查询订单列表 THE SYSTEM SHALL 支持分页查询并返回订单基本信息
- WHEN 运营人员查询可改配订单列表 THE SYSTEM SHALL 仅返回状态允许改配的订单
- WHEN 运营人员查询无交付人员订单列表 THE SYSTEM SHALL 仅返回未分配交付人员的订单
- WHEN 运营人员查询可配车订单列表 THE SYSTEM SHALL 仅返回状态允许配车的订单
- WHEN 运营人员查询发运相关订单列表 THE SYSTEM SHALL 返回发运阶段的订单信息
- WHEN 运营人员查询订单详情 THE SYSTEM SHALL 返回订单完整信息（含参与方、金额、配置快照、状态维度等）

### US-018: 支付回调处理

**As a** 外部支付系统, **I want** 通过回调接口通知支付结果, **so that** VSO 服务能及时更新订单支付状态。

**Acceptance Criteria** (EARS 语法):
- WHEN 支付回调到达 THE SYSTEM SHALL 验证 X-Signature 请求头签名
- WHEN 支付回调数据有效 THE SYSTEM SHALL 更新支付记录状态并触发订单状态流转
- WHEN 支付回调重复到达 THE SYSTEM SHALL 通过幂等机制保证不重复处理
- WHEN 回调处理完成 THE SYSTEM SHALL 记录回调日志

### US-019: 超时自动处理

**As a** 系统, **I want** 自动处理超时订单, **so that** 未及时支付或处理的订单能被自动关闭或提醒。

**Acceptance Criteria** (EARS 语法):
- WHILE 小定订单处于 EARNEST_MONEY_UNPAID 状态超过 30 分钟 THE SYSTEM SHALL 自动将订单状态设为 EXPIRED
- WHILE 大定订单处于审核阶段超过 1440 分钟 THE SYSTEM SHALL 发送提醒通知
- WHILE 订单处于待签约状态超过 2880 分钟 THE SYSTEM SHALL 发送提醒通知

### US-020: 订单并发控制

**As a** 系统, **I want** 对订单关键操作进行并发控制, **so that** 防止并发操作导致数据不一致。

**Acceptance Criteria** (EARS 语法):
- WHILE 订单支付操作执行中 THE SYSTEM SHALL 持有该订单的分布式锁
- WHILE 订单锁单操作执行中 THE SYSTEM SHALL 持有该订单的分布式锁
- WHILE 订单取消操作执行中 THE SYSTEM SHALL 持有该订单的分布式锁
- WHILE 订单退款申请操作执行中 THE SYSTEM SHALL 持有该订单的分布式锁
- WHILE 车辆绑定操作执行中 THE SYSTEM SHALL 持有该订单的分布式锁
- IF 获取锁失败 THEN THE SYSTEM SHALL 拒绝操作并返回并发冲突错误

## 4. Constraints & Assumptions

- 技术约束：
  - JDK 17、Spring Boot、MyBatis-Plus
  - DDD 分层架构（Adapter → Application → Domain → Infrastructure）
  - 分布式锁用于订单关键操作的并发控制
  - 领域事件驱动状态流转（如支付成功事件触发订单状态变更）
- 依赖：
  - VMD 服务：车辆模型数据、buildConfig 解析
  - 字典服务：省市区域数据
  - 组织经销商服务：门店/区域信息
  - 支付网关：通过回调机制对接
- 前置条件：
  - VMD 服务中已维护好车型和 buildConfig 数据
  - 支付网关已配置回调地址

## 5. Out of Scope

- 车辆模型数据（VMD）的 CRUD 管理
- 支付网关的具体实现（仅对接回调）
- 电子签章合同的生成与签署逻辑
- 金融贷款审批流程
- 政府补贴申请流程
- 发票开具流程
- 消息推送的具体实现（仅创建通知任务）
- 用户认证与鉴权（由网关层处理）

## 6. Changelog

| Date | Change ID | Type | Description |
|------|-----------|------|-------------|
| 2026-05-23 | CR-001 | Added | 基于现有代码逆向生成初始需求文档 |
