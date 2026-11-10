# C App Vehicle Purchase - Tasks

## Conventions

- [ ] = TODO，[x] = Done，~~删除线~~ = Cancelled（不删除条目）
- 任务粒度：单人 0.5-2 天可完成
- 每个任务完成后先更新勾选状态，再进入下一个任务
- 修改代码前必须完成并声明 Pre-Flight Checklist
- 任务追踪：Task → US-ID → Design Section

## Task List

- [x] **T-001** 建立 V1.39.0 数据模型及持久化基础 · 关联 US-001, US-003, US-006, US-007 · 设计 §3.2、§3.3、§10 · 依赖：无
  - 子项：
    - [x] 新增协议模板、协议确认、退款明细、订单字段、支付过期时间和用户关系索引迁移
    - [x] 新增或修改对应 PO、Mapper、Converter、Domain 和 Repository
    - [x] 补充迁移及持久化单元/集成测试
    - [x] 记录数据库文档待更新项

- [x] **T-002** 修复心愿单与订单的当前用户隔离 · 关联 US-002 · 设计 §2、§3.2-E、§3.3、§10.6 · 依赖：T-001
  - 子项：
    - [x] 修复 orderNo + accountId EXISTS 查询和 findByAccountId
    - [x] 为订单 search 增加可空 accountId，并保证 C 端必传、MPT 不传
    - [x] 统一订单详情、支付、退款、锁单、改配和预约的归属校验
    - [x] 添加双账号隔离及跨账号操作拒绝测试

- [x] **T-003** 实现购车协议查询和订单版本确认 · 关联 US-001 · 设计 §3.2-C、§10.2、§10.3、§11.1 · 依赖：T-001
  - 子项：
    - [x] 实现车型专属及 GLOBAL 协议查询和有效期过滤
    - [x] 实现 agreementCode + versionNo 确认校验及独立确认快照
    - [x] 增加 Mobile 协议查询接口和下单协议参数
    - [x] 添加协议缺失、版本不匹配及多协议确认测试

- [x] **T-004** 修复意向金和直接定金下单及金额快照 · 关联 US-003 · 设计 §3.1、§4.1、§11.2、§11.3 · 依赖：T-002, T-003
  - 子项：
    - [x] 统一下单配置解析、销售策略校验和订单信息保存
    - [x] 修复直接定金 VO/Assembler，移除 saleModelConfigType 断链
    - [x] 按选中 Variant/Option 初始化 OrderAmount 和配置快照
    - [x] 实现待支付取消及下单时间线
    - [x] 添加两类下单、金额一致性、失败回滚和重复订单测试

- [x] **T-005** 实现 Mock 支付发起、并发防重和结果查询 · 关联 US-004 · 设计 §2、§4.1、§11.4、§12、§13 · 依赖：T-004
  - 子项：
    - [x] 完善 Payment Domain、Repository 和 MockPaymentAdapter
    - [x] 按订单金额快照创建支付单并返回 Mock 收银台参数
    - [x] 在订单锁内返回已有有效支付单，禁止并发重复支付
    - [x] 提供支付单及订单支付记录查询接口
    - [x] 添加金额、归属、重复发起和 Mock 不固定成功测试

- [x] **T-006** 完成支付回调、异常状态和幂等处理 · 关联 US-004 · 设计 §4.1、§11.5、§12 · 依赖：T-005
  - 子项：
    - [x] 启用支付 HMAC、timestamp、nonce 和金额/阶段校验
    - [x] 实现 PAID、PROCESSING、PAYMENT_FAILED、CANCELLED 分支
    - [x] 在同一事务内更新支付、订单金额、订单状态和时间线
    - [x] 处理重复回调、终态冲突和失败后的重新支付
    - [x] 移除公开 /order/action/pay 和重复取消路由
    - [x] 添加回调签名、重放、冲突和事务测试

- [x] **T-007** 完成意向金转定金及差额补款 · 关联 US-005 · 设计 §3.1、§4.2、§12 · 依赖：T-006
  - 子项：
    - [x] 按订单快照计算差额并复用已有 pending 补款任务
    - [x] 补款发起复用统一 Payment 用例并关联 supplementaryNo
    - [x] 补款成功后累计金额、转换订单并写 downPaymentTime
    - [x] 持久化 upgradedFromSmall 并处理零/负差额及重复请求
    - [x] 添加正/零/负差额、重复申请和重复回调测试

- [x] **T-008** 实现退款主从单、进度、回调和失败重试 · 关联 US-006 · 设计 §3.2-D、§4.3、§10.4、§11.6、§12 · 依赖：T-007
  - 子项：
    - [x] 实现退款金额、手续费和超额退款规则
    - [x] 按原支付单拆分退款明细并调用 Mock 退款网关
    - [x] 实现签名退款回调、主单状态聚合和支付退款状态
    - [x] 全部成功后更新 REFUND_COMPLETE、refundTotal 和时间线
    - [x] 提供退款申请、进度查询及 failed/pending 明细重试接口
    - [x] 添加多支付分摊、失败重试、重复申请和重复回调测试

- [x] **T-009** 实现手动锁单和数据库补偿自动锁单 · 关联 US-007 · 设计 §3.2-A、§4.4、§12 · 依赖：T-006
  - 子项：
    - [x] 持久化定金支付及转定金完成时间
    - [x] 实现每分钟到期订单查询和统一锁单用例
    - [x] 使用订单锁保证手动/自动并发幂等
    - [x] 记录手动和自动锁单来源及时间线
    - [x] 添加 48 小时边界、非法状态、并发和补偿扫描测试

- [x] **T-010** 修复配车、申请发运及运输状态链 · 关联 US-008 · 设计 §4.5、§5.4、§12 · 依赖：T-009
  - 子项：
    - [x] MPT 配车统一调用 VehicleAssignmentAppService
    - [x] 实现 applyTransport 操作人、时间和状态持久化
    - [x] 校验运输通知状态并追加时间线
    - [x] 统一取消/关闭时 VIN 释放逻辑
    - [x] 对齐 VsoOrderService Feign 契约
    - [x] 添加配车冲突、发运、运输顺序和 VIN 释放测试

- [x] **T-011** 实现交付预约、交付记录和激活前置校验 · 关联 US-009 · 设计 §4.5、§5.3、§11.7、§12 · 依赖：T-010
  - 子项：
    - [x] 实现预约创建、修改、取消和当前有效预约查询
    - [x] 使用订单锁保证一个订单仅一个有效预约
    - [x] 交付时创建交付记录并完成有效预约
    - [x] 激活仅允许 DELIVERED → ACTIVATED
    - [x] 添加预约归属、重复预约、交付完成及非法激活测试

- [x] **T-012** 完成订单详情聚合和全流程时间线 · 关联 US-010 · 设计 §4.6、§5.3、§13.2 · 依赖：T-005, T-006, T-007, T-008, T-009, T-010, T-011
  - 子项：
    - [x] 建立 OrderTimeline Domain、Repository 和统一记录入口
    - [x] 聚合订单金额、支付、退款、配车、运输和交付信息
    - [x] 提供本人订单时间线正序查询
    - [x] 对未发生环节返回 NOT_STARTED 或空对象
    - [x] 添加详情组装、事件顺序及未发生环节测试

- [x] **T-013** 执行完整购车链路集成测试并修订数据库文档 · 关联 US-001～US-010 · 设计 §7.2、§7.3、§14 · 依赖：T-012
  - 子项：
    - [x] 验证意向金补差至激活的完整链路
    - [x] 验证直接定金、自动锁单、支付异常和退款重试
    - [x] 验证双账号订单、支付、退款及预约隔离
    - [x] 执行相关模块及全量 Maven 测试并记录结果
    - [x] 更新 Notion 数据库表目录、字段和版本记录

- [x] **T-014** 汇总 CR 实现证据并完成交付检查 · 关联 US-001～US-010 · 设计 §6、§7、§14 · 依赖：T-013
  - 子项：
    - [x] 检查 Coverage Mapping 和全部验收标准实现证据
    - [x] 检查 git diff，确认无越界修改和分层违规
    - [x] 执行 graphify update 更新项目知识图谱
    - [x] 更新 CR 实现状态、测试数量、限制及未执行事项
    - [x] 汇总变更文件、接口、数据库迁移和测试结果

## Task Deliverable Contracts

### T-001 数据与持久化

- 修改：V1.39.0 迁移、OrderPo、OrderAmountPo、PaymentPo、OrderPartyMapper、OrderMapper、Payment/Refund/Delivery/Supplement Mapper XML。
- 新增：PurchaseAgreementPo、OrderAgreementConfirmationPo、RefundItemPo 及对应 Domain、Converter、Mapper、Repository。
- 测试：迁移可执行、两个同版本协议可确认、refundFee 可读写、paymentNo/refundNo/appointmentNo 查询条件真实生效。
- 完成门禁：禁止只建表而未完成 Domain ↔ PO ↔ DB 双向映射。

### T-002 用户隔离

- 修改：OrderRepository、OrderRepositoryImpl、OrderMapper.xml、OrderAppService、MobileVsoController。
- 测试：账号 A 不可查看或操作账号 B 的订单、支付、退款和预约；MPT 无 accountId 时仍可管理查询。
- 完成门禁：C 端 SQL 必含 accountId；无 UnsupportedOperationException。

### T-003 协议

- 新增：PurchaseAgreementAppService、协议 VO/Assembler、协议确认 Repository。
- 修改：MobileSaleModelController、两类下单 Cmd 和 OrderAppService。
- 测试：GLOBAL 回退、车型优先、有效期边界、多协议、缺失协议整单回滚。

### T-004 下单与金额

- 修改：两类下单 VO/Assembler/Cmd、OrderAppService、OrderPoConverter。
- 测试：Controller VO → Cmd 全字段映射；SM001 固定金额；直接定金不再 BuildConfigNotMatched；交付人员和发运字段可持久化。

### T-005 支付发起与查询

- 新增：PaymentAppService、PaymentGatewayResult、PaymentQuery/Result VO。
- 修改：PaymentAdapter、MockPaymentAdapter、PaymentRepository、PaymentMapper.xml。
- 测试：PENDING/PROCESSING 返回原单、过期 PENDING 关闭、失败允许新建、锁失败不执行。

### T-006 支付回调

- 修改：PaymentCallbackService、OpenVsoCallbackController、支付成功调用链、OrderAmount、Order、VsoErrorCode。
- 删除公开路由：MobileVsoController.pay。
- 测试：四状态、签名、重放、金额/阶段错误、已 PAID 后失败冲突、已取消后成功冲突、重复成功只累计一次。

### T-007 转定金

- 修改：OrderAppService、SupplementaryPaymentRepository、PaymentAppService、OrderAmountPoConverter。
- 查询必须增加 scene 条件，不能复用只按 orderId 查 pending 的旧方法。
- 测试：三种差额、已有补款、支付成功、重复转换、upgradedFromSmall、downPaymentTime。

### T-008 退款

- 新增：Refund、RefundItem、RefundAppService、RefundCallbackService、RefundGatewayResult、退款 VO。
- 修改：RefundRepository、RefundMapper.xml、PaymentRepository、OrderStateMachine。
- 测试：两笔支付分摊、一项完成一项处理中、一项失败重试、全部完成、重复完成不累计 refundTotal。

### T-009 自动锁单

- 修改：TimeoutTaskScheduler、OrderRepository、OrderAppService、OrderLockService。
- 锁释放改为随机 token + compare-and-delete；Redis 异常不得无锁执行。
- 测试：47:59 不锁、48:00 可锁、非 DOWN_PAYMENT_PAID 不锁、手动/自动只产生一次成功事件。

### T-010 配车发运

- 修改：MptVsoController、OrderAppService、VehicleAssignmentAppService、Order、OrderPoConverter、VsoOrderService。
- applyTransport 必须设置 operator、applyTime 和 APPLY_TRANSPORT。
- 测试：完整状态顺序、非法状态、VIN 冲突、取消释放、Feign 路径一致。

### T-011 交付与激活

- 新增：DeliveryAppointment Domain、DeliveryAppService、预约 VO/Assembler。
- 修改：DeliveryRepository、DeliveryAppointmentMapper.xml、ServiceOrderController、Order。
- 测试：唯一 booked、修改时 storeCode 可更新、交付完成预约、非 DELIVERED 激活失败。

### T-012 详情与时间线

- 新增：OrderTimeline Domain/Repository/AppService、OrderTimelineVo、订单详情聚合 Result。
- 所有业务写入口只能调用统一时间线服务，不能在 Controller 拼接。
- 测试：eventTime/id 排序、失败原因、重复事件、未发生环节 NOT_STARTED。

### T-013 集成与数据库文档

- 新增 PurchaseFlowIntegrationTest。
- 至少执行：相关测试、otd-vso-service 全量测试、根 Reactor 测试。
- Notion 数据库文档必须列出三个新表、全部新列/索引和 1.39 版本。
- 测试失败不得勾选完成。

### T-014 交付检查

- 检查 INV-001～INV-006、US-001～US-010 和设计 §14.1。
- 输出变更文件清单、接口清单、SQL 清单、测试命令及真实结果。
- 执行 graphify update。
- 未执行的真实跨服务 E2E、生产迁移和发布必须明确标记“未执行”。

### T-015 MPT 运营只读视图与 MPT 契约缺陷修复（US-011，设计 §5.4.2、§7.2）

- [x] 关联：US-011、设计 §4.6、§5.1、§5.4.2、§7.2、§14、§15.4；原地修订 CR-002，未新增 CR 编号。
- [x] 修改：MptVsoController、OrderTimelineResult/OrderTimelineAppService、PaymentAppService、RefundAppService、DeliveryAppService、OrderAppService（时间线与补款的无 accountId 变体）、交付人员列表批量预约查询。
- [x] 只读端点：`GET /{orderId}`、`/{orderId}/timeline`、`/{orderId}/payments`、`/{orderId}/supplements`、`/{orderId}/refunds`、`/{orderId}/agreements`、`/{orderId}/delivery`；统一 `completeVehicle:order:info:query`。
- [x] 路径参数：业务 `orderId`（String），删除 `Long vehicleSaleOrderId`；`VehicleSaleOrderMpt.id` 仅展示，不得作参数。
- [x] 缺陷修复：删除根路径 `GET /api/mpt/vso/v1` 与 `/vehicleAssignment/list` 空实现；MPT 订单列表补 `startPage()` 数据库分页；`listWithoutDeliveryPerson` 批量带 `appointmentNo/appointmentTime/appointmentStatus`，禁止 N+1。
- [x] 额外缺陷（同批修复）：`OrderRepositoryImpl.searchByAccount` 保留 `Page`（修复 PageResult.total 退化为页大小）；`VehicleSaleOrderMpt`/`TransportOrderMpt` 继承 `BaseRequest`（修复 `params[beginTime]` 用 `instanceof Map` 取参的死代码）；`OrderTimelineMapper.selectByOrderId` 排序修为 `event_time ASC, id ASC`（对齐 §4.6，修复时间线倒序）；`RefundResult` 补 `refundScene/applyTime/refundTime/failReason`（US-006 验收要求）。
- [x] 写动作：去掉 `X-Operator-Id` 请求头与 `catch (Exception)` 吞异常，操作人取安全上下文；细粒度权限码待权限中心对齐（本期不新增猜测码）。
- [x] 不新增数据库表或字段；退款手续费核对以 `refundAmount + refundFee` 为申请时 `paidTotal`。
- [x] 测试：新增 `MptOrderQueryControllerTest`（12）、`MptOrderWriteControllerTest`（8）、`OrderRepositoryPaginationTest`（2），并补充既有 Payment/Refund/Delivery/OrderSearch 测试；覆盖不存在订单、时间线 operatorId、退款场景与手续费、预约未发生返回空、列表分页与无 N+1、权限点、只读端点不调网关不写时间线、写动作异常不被吞。
- [x] 完成门禁：无空实现端点；无 MPT 改金额/支付/退款状态的端点；`mvn -o -pl otd-vso-service -am test` 373 项、`mvn -o test` 根 Reactor 全部通过。

### T-016 MPT 运营字段补齐、交付人员列表与写动作权限（US-011 补充）

- [x] 字段：`OrderListResult`/`OrderDetailResult` 补齐 MPT VO 全部运营字段（saleCode、modelConfigCode、buildConfigCode、orderStateTime、orderTime、earnestMoneyTime、earnestMoneyAmount、downPaymentTime、downPaymentAmount、lockTime、deliveryVin、deliveryPersonId/Name、createTime；详情另含 orderType/来源名称、brand、saleModel、licenseCity、appointment*）。
- [x] 派生与批量：`orderStateTime`←时间线最新事件、`earnestMoneyTime`←意向金成功支付单；新增 `OrderAmountMapper.selectByOrderIds`、`PaymentMapper.selectSuccessfulByOrderIds`、`OrderTimelineMapper.selectLatestEventTimeByOrderIds` 及对应仓储方法，列表全程批量查询。
- [x] 不输出：`id`（`@Deprecated`，恒为 null，不作路由参数）、`orderPersonPhone`/`orderPersonId`（加密隐私字段，归入 O-009 另行设计解密服务）。新增契约测试禁止 Result 承载主键与加密字段。
- [x] 只读容错：`getById` 金额快照缺失时金额留空并告警，不对前端报 500。
- [x] 交付人员列表：`searchDeliveryStaff` 接组织服务 `OrgDealershipStaffService.searchPage` 远程分页，返回真实人员与未交付订单数；组织服务无返回时返回空分页。
- [x] 写动作权限：10 个写接口补 `@RequiresPermissions`（权限码见设计 §5.4.3，需权限中心登记）。
- [x] 订单不存在：`OrderDomainService.loadOrder/loadOrderByOrderNo` 改抛 `OrderNotExistException`（ORDER_NOT_EXIST），不再冒泡 `IllegalArgumentException` 导致 500。
- [x] 测试：`MptOrderQueryControllerTest`（+2：交付人员列表、VO 字段承载）、`MptOrderWriteControllerTest`（+1：写动作权限点）、`OrderAppServiceDeliveryStaffTest`（3）、`OrderAppServiceQueryTest`（+2：ORDER_NOT_EXIST、金额/类型补齐）；`mvn -o -pl otd-vso-service -am test` 382 项通过。

### T-017 MPT 协议后台权限/分页、VO 去主键、批量富化与运营改配提交（US-012）

- [x] 协议后台：`MptPurchaseAgreementController` 补 `completeVehicle:order:agreement:list|edit|remove`；list 改 `startPage()` + `PageResult`（`PurchaseAgreementRepositoryImpl` 保留 Page 元信息，`PurchaseAgreementAppService.search` 用 `PageUtil.convert` 保 Page）。
- [x] MPT VO 去主键：删除 `VehicleSaleOrderMpt`/`TransportOrderMpt`/`DeliveryCenterStaffMpt` 中恒为 null 的 `id`，前端统一 `orderId`；契约测试断言 VO 不含 `id`。
- [x] 交付门店名称：`listWithoutDeliveryPerson`/`listTransport` 按 `storeCode` 去重后单次获取（组织服务仅有 `getByCode`），新增去重测试。
- [x] 交付人员未交付数：`OrderMapper.selectUndeliveredCountByDeliveryPersonIds`（GROUP BY + `delivery_finish_time IS NULL`）+ `OrderRepository.countUndeliveredByDeliveryPersonIds`，替换逐个 count；顺带让 `countPoByMap` 支持 `delivered` 过滤。
- [x] 运营改配提交：`POST /api/mpt/vso/v1/{orderId}/modifyConfig`（权限 `completeVehicle:order:changeModel:edit`），`OrderAppService.modifyConfigByOperator` 与 C 端共用 `doModifyConfig`，时间线 `mpt_operator/mpt`，事件 accountId 取订单 order_user，锁内重载、订单不存在抛 ORDER_NOT_EXIST。
- [x] 测试：新增 `MptPurchaseAgreementControllerTest`（4）、`OrderAppServiceModifyConfigTest` +2（MPT 成功/不存在）、`MptOrderWriteControllerTest` +3（改配路由/权限/委托/必填）、`MptOrderQueryControllerTest` +1（门店名称去重）；`mvn -o -pl otd-vso-service -am test` 392 项、根 Reactor 通过。

### T-018 可改配状态一致性、协议版本冲突业务化、删除端点清理与批量统计确认

- [x] 可改配列表：`OrderState#configModifiableStateValues()`（210/300/310）作为唯一事实源，`listModelConfigChangeable` 与 `validateOrderStateForModifyConfig` 共用；新增 `OrderStateConfigModifiableTest` 与控制器筛选断言。
- [x] 协议版本冲突：新增错误码 `201054 AGREEMENT_VERSION_CONFLICT` + `AgreementVersionConflictException`；保存前预校验（排除同 agreementId）+ `DuplicateKeyException` 兜底；新增 3 个用例（预校验/并发兜底/同条更新）。
- [x] 删除端点清理：移除 `DELETE /api/mpt/vso/v1/{orderNo}`、`OrderAppService.remove`、`Order.manageDelete`（原实现只校验后原样保存，并非真正删除）；保留物理删除并加断言。
- [x] 批量统计确认：交付人员未交付数已走 `countUndeliveredByDeliveryPersonIds` 单次 group by；新增“2 人只查 1 次且不调逐个 count”断言。
- [x] 测试：`mvn -o -pl otd-vso-service -am test` 400 项、根 Reactor 通过；错误码总数断言更新为 53。

## Change Requests

### OTD-VSO-REQ-CR-002 / OTD-VSO-DSN-CR-002 (2026-11-10): C 端用户购车全流程补全

- 初始任务即为本配套 CR 的建立内容，无其他历史任务需要修改。
- 2026-11-10 原地细化 CR-002，未新增 CR 编号；补充 Task Deliverable Contracts 和完成门禁。
- 后续需求或设计新增项必须追加到本区块，并关联稳定 US-ID 和设计锚点。

### OTD-VSO-REQ-CR-002 / OTD-VSO-DSN-CR-002 原地修订（2026-11-10，US-011）

- 变更内容：新增 US-011（MPT 运营订单只读视图）与设计 §5.4.2，修正 MPT 订单域路径参数语义，清理空实现端点与 MPT 列表分页缺失，补 `OrderTimelineResult.operatorId`。
- 不新增 CR 编号；不新增数据库表/字段/迁移。
- 新增任务：T-015；需同步 Notion 需求/设计正文与 CR 记录页 §7/§8。
- 业务动因：运营无法核对订单时间线、支付/退款进度、退款手续费、协议确认快照和交付预约，且部分 MPT 端点为空实现或参数语义错误。

## Changelog

| Date | Change ID | Type | Description |
|------|-----------|------|-------------|
| 2026-11-10 | OTD-VSO-DSN-CR-002 | Added | 建立 C 端购车全流程开发与测试任务及追踪关系。 |
| 2026-11-10 | OTD-VSO-DSN-CR-002 | Clarified | 补充每任务交付文件、测试场景及完成门禁。 |
| 2026-11-10 | OTD-VSO-DSN-CR-002 | Implemented | T-001～T-014 全部实现并通过本地测试（343 项，0 失败）。新增范围扩展：购车协议 MPT 后台 CRUD（用户明确要求，超出原 §7.2「本期不新增协议运营 CRUD」）。缺陷修复：V1.40.0 清理 V1.16.0 遗留孤儿索引 `uk_small_order_no_valid`（曾导致全库仅允许一条有效订单）。未执行项：真实跨服务 E2E、生产库迁移、灰度与发布。 |
| 2026-11-10 | OTD-VSO-DSN-CR-002 | Extended | 原地修订新增 US-011 与设计 §5.4.2（MPT 运营只读视图），修正 MPT 路径参数语义、清理空实现端点、补 MPT 列表分页与时间线 operatorId；新增 T-015，未新增 CR 编号。 |
| 2026-11-10 | OTD-VSO-DSN-CR-002 | Implemented | T-018 实现完成：可改配状态唯一事实源、协议版本冲突转 201054、移除逻辑删除端点、批量计数确认；本地 400 项测试通过。 |
| 2026-11-10 | OTD-VSO-DSN-CR-002 | Implemented | T-017 实现完成：协议后台权限与分页、MPT VO 去主键、门店名称去重、未交付数批量统计、运营改配提交端点；本地 392 项测试通过。 |
| 2026-11-10 | OTD-VSO-DSN-CR-002 | Implemented | T-016 实现完成：MPT 运营字段补齐（含派生与批量查询）、交付人员列表接组织服务、写动作权限点、订单不存在改抛 ORDER_NOT_EXIST；本地 382 项测试通过。 |
| 2026-11-10 | OTD-VSO-DSN-CR-002 | Implemented | T-015 实现完成：7 个 MPT 只读端点、路径参数语义修正、占位端点删除、列表分页与预约批量查询、时间线 operatorId 与排序修复、写动作操作人/异常链修复；本地 373 项测试与根 Reactor 全部通过。 |

## 实现状态（2026-11-10）

- 状态：T-001～T-015 全部完成；本地 `mvn -o -pl otd-vso-service -am test` 通过 373 项（0 失败 0 错误），根 Reactor `mvn -o test` 构建成功。
- US-011 / T-015 交付：`MptVsoController`（7 个只读端点 + 路径参数改业务 orderId + 删除 2 个空实现 + 5 个列表端点补分页）、`OrderAppService.getTimelineByOrderId|getSupplementaryPaymentsByOrderId`、`PaymentAppService.listPaymentsByOrderId`、`RefundAppService.listByOrderId`、`DeliveryAppService.getByOrderId`、`DeliveryRepository.findBookedByOrderIds`（一次批量查询）、`OrderTimelineResult.operatorId`、`RefundResult.refundScene|applyTime|refundTime|failReason`、`OrderListResult`/MPT VO 预约字段。
- US-011 / T-015 缺陷修复：`OrderRepositoryImpl` 保留 PageHelper `Page`（total 不再退化为页大小）；MPT 列表时间范围改用 `BaseRequest.params[beginTime]`（原 `instanceof Map` 为死代码）；`OrderTimelineMapper` 排序修为 `event_time ASC, id ASC`；写动作操作人取安全上下文且不再吞异常。
- T-016 交付：`OrderListResult`/`OrderDetailResult` 补齐 MPT 运营字段并批量派生状态时间/意向金支付时间/金额；`searchDeliveryStaff` 接组织服务远程分页；10 个写接口补权限点；`OrderDomainService.loadOrder*` 改抛 `OrderNotExistException`；只读详情金额快照缺失降级为留空 + 告警。
- T-017 交付：协议后台权限点与分页、MPT VO 删除主键字段、门店名称去重获取、未交付订单数批量统计、`POST /{orderId}/modifyConfig` 运营改配提交（与 C 端共用改配实现）。
- T-018 交付：可改配状态唯一事实源与筛选对齐、协议版本冲突业务错误（201054）、移除逻辑删除端点与 `info:remove` 权限、批量计数测试确认。
- 不新增数据库迁移与数据库文档版本（US-011 / T-016 / T-017 / T-018 均无表/字段变更）。
- 数据库：`V1.39.0__complete_c_app_purchase_flow.sql`（3 新表 + 9 新字段 + 3 索引）、`V1.40.0__drop_orphan_uk_small_order_no_valid.sql`（缺陷修复）。
- 范围扩展（用户明确要求）：购车协议 MPT 后台管理 `POST/GET /api/mpt/vso/v1/purchaseAgreement/*`（列表、保存、下架），超出原设计 §7.2「本期不新增协议运营 CRUD」，已在 Notion 数据库文档与本文件登记。
- 缺陷修复：`uk_small_order_no_valid` 在 V1.16.0 删除字段后仍残留在部分环境并退化为 `(row_valid)` 唯一约束，导致全库仅允许一条有效订单；由 V1.40.0 清理。
- 测试环境修复（仅测试配置，不改生产语义）：测试上下文 `spring.main.allow-bean-definition-overriding=true`、`iov.kafka.topic-provisioning.enabled=false`（本地 Nacos 服务级配置同步关闭 Topic Provisioning）、surefire `-Duser.timezone=UTC` 以满足 FW-MYSQL 时间治理启动探针。
- 未执行：真实跨服务 E2E、生产库迁移、灰度与发布。
