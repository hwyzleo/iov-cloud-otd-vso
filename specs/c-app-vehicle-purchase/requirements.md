# C App Vehicle Purchase - Requirements

## 1. Overview

补全 C 端用户从选车、心愿单、下单、支付、转定金、退款、锁单，到配车、发运、交付和激活的车辆购买闭环。

## 2. Background & Goals

- 背景：当前项目已提供部分选车、订单和支付接口，但直接定金下单、订单金额、支付异常、退款、自动锁单、配车发运、交付预约及订单进度等链路尚未闭环。
- G-001：用户能够基于有效车型配置完成意向金或直接定金下单。
- G-002：用户能够完成可查询、可幂等、可追踪的 Mock 支付和退款流程。
- G-003：系统能够保证订单金额、支付金额、补款金额和退款金额均来源于同一订单金额事实。
- G-004：系统能够按照既定状态机完成锁单、配车、发运、运输、交付和激活。
- G-005：用户只能查询和操作本人心愿单及订单，并能够查看本人订单的完整处理进度。
- G-006：运营人员能够在 MPT 只读核对单笔订单的全链路事实（时间线、支付与补款、退款与手续费、协议确认快照、交付预约与交付记录）。
- N-001：不得以 Mock 支付成功回调代替支付状态、金额校验、订单归属、异常处理或幂等逻辑。
- N-002：不得通过直接修改订单状态绕过支付、退款、配车、发运、交付或激活的前置条件。

## 3. User Stories

### US-001: 选车、报价与购车协议

**As a** C 端购车用户, **I want** 查看可售车型、配置、价格、权益和购车协议, **so that** 我能够在信息明确的情况下提交订单。

**Acceptance Criteria**:

- WHEN 用户查询车型、选配器、价格或购车权益时 THE SYSTEM SHALL 返回当前可售数据。
- WHEN 用户选择车型、版本和选装并请求报价时 THE SYSTEM SHALL 返回配置代码、版本价格、选装明细、选装合计和订单总价。
- WHEN 用户确认订单前查询购车协议时 THE SYSTEM SHALL 返回当前有效协议及其版本。
- WHEN 用户提交订单时 THE SYSTEM SHALL 保存本次确认的协议标识和版本快照。
- IF 车型、版本、配置或选装不可售或组合不合法 THEN THE SYSTEM SHALL 返回明确失败原因且不创建或修改订单。

### US-002: 心愿单与用户数据隔离

**As a** C 端购车用户, **I want** 保存和调整心愿单并只查看本人车辆与订单, **so that** 我可以持续管理购车意向且不会访问其他用户的数据。

**Acceptance Criteria**:

- WHEN 用户保存有效车型配置时 THE SYSTEM SHALL 为当前账号创建心愿单。
- WHEN 用户查看、修改或删除心愿单时 THE SYSTEM SHALL 校验心愿单属于当前账号。
- WHEN 心愿单配置已不可售时 THE SYSTEM SHALL 在查询结果中返回对应失效原因。
- WHEN 用户查询订单列表、订单详情或“我的车辆”时 THE SYSTEM SHALL 仅返回当前账号的订单和心愿单。
- WHEN 用户发起支付、取消、退款、转定金、锁单、改配或预约交付时 THE SYSTEM SHALL 校验订单属于当前账号。
- IF 心愿单或订单不属于当前账号 THEN THE SYSTEM SHALL 拒绝操作且不返回该对象的业务详情。

### US-003: 意向金与直接定金下单

**As a** C 端购车用户, **I want** 使用意向金下单或直接定金下单, **so that** 我可以选择不同方式锁定购车意向。

**Acceptance Criteria**:

- WHEN 用户提交有效意向金订单时 THE SYSTEM SHALL 创建 `EARNEST_MONEY_UNPAID` 订单并返回订单号、应付意向金、支付渠道和支付截止时间。
- WHEN 用户不经过意向金直接提交有效定金订单时 THE SYSTEM SHALL 创建 `DOWN_PAYMENT_UNPAID` 正式订单并保存所选配置、订购人、购买计划和门店信息。
- WHEN 任意订单创建成功时 THE SYSTEM SHALL 按订单选中的 Variant 和 Option 保存车型配置及金额快照。
- WHEN 系统计算意向金或定金应付金额时 THE SYSTEM SHALL 使用当前订单选中的 Variant 销售策略，不得使用销售车型下其他 Variant 的最低价格替代。
- WHEN 用户取消处于意向金待支付或定金待支付状态的本人订单时 THE SYSTEM SHALL 将订单更新为 `CANCEL`。
- IF 下单校验失败 THEN THE SYSTEM SHALL 返回失败原因且不产生部分订单、金额或支付数据。
- IF 用户存在规则不允许重复的未支付订单 THEN THE SYSTEM SHALL 拒绝重复创建并返回冲突原因。

### US-004: Mock 支付与支付结果闭环

**As a** C 端购车用户, **I want** 发起支付并查询成功、失败、取消或处理中的结果, **so that** 我可以安全完成付款且不会重复支付。

**Acceptance Criteria**:

- WHEN 用户对处于待支付状态的本人订单发起支付时 THE SYSTEM SHALL 创建与订单、支付阶段和订单金额一致的支付单并返回 Mock 收银台所需参数。
- WHILE 同一订单支付阶段存在待支付或处理中的有效支付单时 THE SYSTEM SHALL 返回已有支付单且不得创建第二个并发有效支付单。
- WHEN Mock 支付成功结果被确认时 THE SYSTEM SHALL 在同一业务事务中更新支付单、订单支付状态和订单已支付金额。
- WHEN Mock 支付失败或被取消时 THE SYSTEM SHALL 保存对应支付结果、保持订单为原待支付状态并允许用户重新发起支付。
- WHEN 支付结果仍为处理中或未知时 THE SYSTEM SHALL 返回处理中状态且不得将订单更新为已支付。
- WHEN 用户查询支付结果时 THE SYSTEM SHALL 返回支付单号、订单号、支付阶段、金额、渠道、状态及支付时间。
- IF 回调或结果确认的订单、支付单、阶段、金额或签名不匹配 THEN THE SYSTEM SHALL 拒绝处理且不改变支付单或订单状态。
- IF 系统收到已成功处理的相同回调 THEN THE SYSTEM SHALL 幂等返回且不得重复累计金额或重复推进订单状态。
- IF 同一支付单收到与首次结果冲突的回调 THEN THE SYSTEM SHALL 拒绝冲突结果并保留原支付事实。

### US-005: 意向金转定金与差额补款

**As a** 已支付意向金的用户, **I want** 将意向金订单转为定金订单, **so that** 我可以继续完成正式购车流程。

**Acceptance Criteria**:

- WHEN 用户对 `EARNEST_MONEY_PAID` 订单申请转定金时 THE SYSTEM SHALL 按订单金额快照计算“定金金额减意向金金额”的差额。
- IF 差额大于零 THEN THE SYSTEM SHALL 创建差额补款任务、返回补款单号和金额并保持订单为 `EARNEST_MONEY_PAID`。
- WHILE 同一订单存在未完成的转定金补款任务时 THE SYSTEM SHALL 返回已有任务且不得重复创建补款任务。
- WHEN 转定金差额补款成功时 THE SYSTEM SHALL 累计已支付金额并将订单转换为 `DOWN_PAYMENT_PAID` 正式订单。
- IF 差额小于或等于零 THEN THE SYSTEM SHALL 直接将订单转换为 `DOWN_PAYMENT_PAID` 正式订单。
- WHEN 用户重复提交已完成的转定金请求时 THE SYSTEM SHALL 返回当前转换结果且不得重复补款或累计金额。
- IF 当前订单状态不允许转定金 THEN THE SYSTEM SHALL 拒绝操作且不改变订单或支付数据。

### US-006: 退款申请、处理与进度

**As a** 已付款用户, **I want** 申请退款并查看处理进度, **so that** 我可以了解退款金额、手续费和最终结果。

**Acceptance Criteria**:

- WHEN `EARNEST_MONEY_PAID` 或 `DOWN_PAYMENT_PAID` 订单符合退款条件时 THE SYSTEM SHALL 按已支付金额计算退款金额。
- WHEN `ARRANGE_PRODUCTION` 订单申请退款时 THE SYSTEM SHALL 按 `max(已支付金额 × 5%, 500 元)` 计算手续费并从已支付金额中扣除。
- WHEN 意向金转定金订单满足 `upgradedFromSmall` 且已支付金额高于定金金额时 THE SYSTEM SHALL 按现有超额退款规则计算可退金额。
- WHEN 退款申请被接受时 THE SYSTEM SHALL 创建唯一退款单、保存退款金额和手续费并将订单更新为 `REFUND_APPLY`。
- WHILE 同一订单存在待处理或处理中的退款单时 THE SYSTEM SHALL 返回已有退款单且不得重复申请。
- WHEN Mock 退款网关受理退款时 THE SYSTEM SHALL 将退款单更新为处理中并记录外部退款标识。
- WHEN Mock 退款成功时 THE SYSTEM SHALL 将退款单更新为成功、订单更新为 `REFUND_COMPLETE` 并累计已退款金额。
- IF Mock 退款失败 THEN THE SYSTEM SHALL 保存失败状态和原因、保持订单为 `REFUND_APPLY` 并允许后续重试。
- WHEN 用户查询本人退款进度时 THE SYSTEM SHALL 返回退款单号、金额、手续费、状态、申请时间、完成时间和失败原因。

### US-007: 手动与自动锁单

**As a** 已支付定金的用户, **I want** 提前锁单或由系统到期锁单, **so that** 我的车辆配置可以进入排产阶段。

**Acceptance Criteria**:

- WHEN `DOWN_PAYMENT_PAID` 订单的本人用户主动锁单时 THE SYSTEM SHALL 锁定生产配置并将订单更新为 `ARRANGE_PRODUCTION`。
- WHEN `DOWN_PAYMENT_PAID` 订单在定金支付成功后达到 48 小时时 THE SYSTEM SHALL 在下一次到期任务执行时自动锁单。
- IF 手动锁单与自动锁单并发执行 THEN THE SYSTEM SHALL 保证订单仅完成一次有效锁单且不得重复记录锁单事实。
- IF 订单已取消、已退款、已锁单或处于其他不允许锁单的状态 THEN THE SYSTEM SHALL 不执行自动锁单。
- WHEN 锁单成功时 THE SYSTEM SHALL 保存锁单时间、操作来源和状态变更时间线。

### US-008: 配车、发运与运输

**As an** 订单运营人员, **I want** 为已锁单订单分配车辆并推进发运和运输, **so that** 订单能够进入交付准备阶段。

**Acceptance Criteria**:

- WHEN 运营人员为 `ARRANGE_PRODUCTION` 订单分配可用 VIN 时 THE SYSTEM SHALL 创建车辆占用和配车记录并将订单更新为 `ALLOCATION_VEHICLE`。
- IF VIN 不可用或已被其他有效订单占用 THEN THE SYSTEM SHALL 拒绝配车且不改变原订单或车辆占用事实。
- WHEN 运营人员为 `ALLOCATION_VEHICLE` 订单申请发运时 THE SYSTEM SHALL 保存申请人和申请时间并将订单更新为 `APPLY_TRANSPORT`。
- WHEN 外部物流服务依次通知发运车辆、物流出发和到达交付中心时 THE SYSTEM SHALL 依次将订单更新为 `PREPARE_TRANSPORT`、`TRANSPORTING` 和 `PREPARE_DELIVER`。
- WHEN 配车订单被取消或关闭时 THE SYSTEM SHALL 释放对应 VIN 占用并记录释放原因。
- IF 配车、发运或运输通知不符合当前订单状态 THEN THE SYSTEM SHALL 拒绝本次操作且不改变订单状态。
- WHILE 车辆库存网关使用 Stub 时 THE SYSTEM SHALL 仍执行 VSO 内部的订单状态、VIN 占用、冲突和释放规则。

### US-009: 交付预约、交付与激活

**As a** 待收车用户, **I want** 预约交付并在收车后激活车辆, **so that** 我可以完成购车流程。

**Acceptance Criteria**:

- WHEN `PREPARE_DELIVER` 订单的本人用户提交交付中心和预约时间时 THE SYSTEM SHALL 创建交付预约。
- WHILE 同一订单存在有效交付预约时 THE SYSTEM SHALL 保证该订单仅存在一个有效预约。
- WHEN 用户修改或取消本人有效预约时 THE SYSTEM SHALL 保存最新预约状态并保留操作记录。
- WHEN 用户查询本人交付信息时 THE SYSTEM SHALL 返回交付中心、交付人员、车辆和有效预约信息。
- WHEN 外部交付服务确认车辆已交付时 THE SYSTEM SHALL 将 `PREPARE_DELIVER` 订单更新为 `DELIVERED` 并完成对应有效预约。
- WHEN 外部车辆服务确认 `DELIVERED` 订单对应车辆已激活时 THE SYSTEM SHALL 将订单更新为 `ACTIVATED`。
- IF 预约、交付或激活操作不满足当前订单状态或不属于当前用户 THEN THE SYSTEM SHALL 拒绝操作且不改变订单状态。

### US-010: 订单详情与全流程时间线

**As a** C 端购车用户, **I want** 查看订单配置、金额和各环节进度, **so that** 我可以追踪当前状态和历史处理结果。

**Acceptance Criteria**:

- WHEN 用户查询本人订单详情时 THE SYSTEM SHALL 返回车型配置、订单金额、支付、补款、退款、锁单、配车、运输、交付和激活的已有信息。
- WHEN 下单、支付、退款、锁单、配车、发运、运输、预约、交付或激活状态发生变化时 THE SYSTEM SHALL 追加时间线记录。
- WHEN 系统记录时间线时 THE SYSTEM SHALL 保存事件类型、事件时间、变更前状态、变更后状态、操作来源和关联业务单号。
- WHEN 用户查询本人订单时间线时 THE SYSTEM SHALL 按事件发生时间正序返回记录。
- IF 某个后续环节尚未发生 THEN THE SYSTEM SHALL 返回该环节未开始的明确状态且不得伪造完成时间或业务单号。

### US-011: 运营订单只读视图（MPT）

**As an** 订单运营人员, **I want** 在管理后台按订单查看时间线、支付与补款、退款进度与手续费、协议确认快照和交付预约, **so that** 我可以核对订单事实并定位异常，而不需要代用户操作或直接改状态。

**Acceptance Criteria**:

- WHEN 运营人员按业务订单 ID 查询订单详情、时间线、支付记录、补款任务、退款记录、协议确认快照或交付预约时 THE SYSTEM SHALL 返回该订单已存在的全部事实，不按 C 端账号过滤。
- WHEN 运营人员查询时间线时 THE SYSTEM SHALL 返回事件时间正序，且每条事件包含事件类型、变更前后状态、操作人、操作角色、操作来源、关联业务单号和失败原因。
- WHEN 运营人员查询退款记录时 THE SYSTEM SHALL 返回退款场景、退款金额、手续费和明细进度，使 `max(已支付金额 × 5%, 500)`、未锁单免手续费和超额退款规则可被独立核对。
- WHEN 订单尚未发生某环节时 THE SYSTEM SHALL 返回空集合或未开始状态，不得合成业务单号、时间或手续费。
- WHEN 运营人员查询订单列表或交付人员分配列表时 THE SYSTEM SHALL 在数据库层分页，并在交付人员分配列表一次性带出该订单的有效预约信息。
- IF 订单不存在 THEN THE SYSTEM SHALL 返回 ORDER_NOT_EXIST。
- MPT 只读查询 SHALL NOT 触发支付/退款网关调用、状态推进或时间线追加；本期不得新增可修改订单金额、支付或退款状态的 MPT 端点。
- MPT 订单域路径参数 SHALL 使用业务订单 ID，不得使用数据库主键或与语义不符的参数名。

### US-012: 运营改配提交（MPT）

**As an** 订单运营人员, **I want** 在管理后台为允许改配的订单提交新配置, **so that** 我可以在客户电话/门店沟通后直接完成改配，而不需要客户重新在 App 操作。

**Acceptance Criteria**:

- WHEN `EARNEST_MONEY_PAID`、`DOWN_PAYMENT_UNPAID` 或 `DOWN_PAYMENT_PAID` 订单的运营人员提交有效 Variant/Option 组合时 THE SYSTEM SHALL 复用 C 端改配的配置解析、车型快照与价格重算逻辑，更新订单配置与金额。
- WHEN 改配产生正差额时 THE SYSTEM SHALL 创建补款任务；产生负差额时 THE SYSTEM SHALL 创建退款任务；无差额时不创建单据。
- WHEN 运营改配成功时 THE SYSTEM SHALL 追加时间线事件，`operateSource=mpt` 且操作人取自安全上下文，不得记录为 C 端账户。
- WHEN 同一订单并发改配时 THE SYSTEM SHALL 通过订单锁保证串行执行，仅一次生效。
- IF 订单不存在 THEN THE SYSTEM SHALL 返回 ORDER_NOT_EXIST。
- IF 订单状态不允许改配、配置组合非法或金额快照缺失 THEN THE SYSTEM SHALL 拒绝操作且不产生部分配置/金额/补退款数据。
- MPT 改配 SHALL NOT 校验 C 端账号归属（运营代客操作），但 MUST 使用 MPT 改配权限点。
- WHEN 运营查询可改配订单列表时 THE SYSTEM SHALL 使用与改配校验完全一致的订单状态范围（意向金已付/定金待付/定金已付），不得出现“列表有但提交被拒”或“可改配却漏单”。
- IF 保存的协议在同一车型+协议代码+版本号已存在 THEN THE SYSTEM SHALL 返回明确的业务错误（协议版本已存在），不得向前端暴露数据库唯一键异常。

## 4. Constraints & Assumptions

- 支付及退款外部网关使用可替换的 Mock 实现，VSO 内部支付、退款、金额、状态和幂等逻辑必须完整执行。
- 车辆库存网关本期继续使用 Stub，VSO 内部 VIN 占用与释放规则不得省略。
- 外部物流和车辆激活系统不在本服务内实现，本服务提供并处理其内部服务契约。
- 自动锁单时间为定金支付成功后 48 小时。
- 订单状态名称沿用现有 `OrderState` 定义。
- 接口时间格式必须继续遵守现有时间契约，不得改变支付签名中的时间戳语义。
- 查询必须在数据库层完成用户过滤和必要分页，不得通过全量查询后内存过滤实现用户隔离或分页。
- US-011 的 MPT 只读查询不受 C 端账号隔离约束，但必须使用 MPT 权限点校验；C 端账号隔离（INV-001）继续适用于全部 Mobile 端点。
- 运营核对所需事实必须已持久化：时间线保存操作人与操作来源，退款保存退款场景、退款金额和手续费，订单协议确认保存完整内容快照。
- 验收以 Notion 页面 `C-1250ec7a4b92808db3f7f74081ff2f6e` 的订单状态图和手机端用户订购车辆流程为业务基线。

## 5. Out of Scope

- O-001：真实支付渠道、真实退款渠道及对账平台接入。
- O-002：真实车辆库存服务接入。
- O-003：外部物流平台、交付中心业务系统或车端激活系统自身功能。
- O-004：手机 App 页面、交互和客户端本地数据保留。
- O-005：尾款、发票、金融贷款、补贴和电子签完整业务流程。
- O-006：与本次购车链路无关的销售策略管理、订单物理删除和全新后台业务功能；MPT 订单只读查询（US-011）属于本次范围，不在本排除项内。
- O-007：生产部署、跨服务真实环境 E2E 和灰度发布。
- O-008：MPT 端业务写操作扩展（补款/退款重试、强制改状态、代下单、协议重新确认），本期仅提供只读查询、既有审核/配车/发运/交付人员分配动作，以及 US-012 的运营改配提交。
- O-009：订单联系人手机号/证件号明文展示与解密服务接入。数据源为 `vso_order_party` 加密字段，本期 MPT 不输出明文（字段保持空）；如需展示须另立 CR 定义解密服务、脱敏规则与授权范围。

## 6. Changelog

| Date | Change ID | Type | Description |
|------|-----------|------|-------------|
| 2026-11-10 | OTD-VSO-REQ-CR-002 | Added | 建立 C 端用户购车全流程需求及可测试验收标准。 |
| 2026-11-10 | OTD-VSO-REQ-CR-002 | Clarified | 补充业务不变量、固定金额样例和异常验收场景，不改变原需求范围。 |
| 2026-11-10 | OTD-VSO-REQ-CR-002 | Extended | 原地新增 US-012（MPT 运营改配提交）；O-008 口径同步调整。 |
| 2026-11-10 | OTD-VSO-REQ-CR-002 | Extended | 原地新增 US-011（MPT 运营订单只读视图）、G-006 及 MPT 只读约束；收窄 O-006 排除口径并新增 O-008（MPT 写操作扩展仍排除）。未新增 CR 编号。 |

## 7. Business Invariants

以下不变量是 US-001～US-010 的共同验收约束：

### INV-001: 用户与订单归属

- accountId MUST 只来自 SecurityContext，MUST NOT 使用请求体或请求参数中的 accountId。
- C 端订单归属 MUST 通过 `vso_order_party` 中 `party_role=order_user` 且 `user_id=accountId` 的有效记录判断。
- 订单列表 MUST 在 SQL 中使用 `EXISTS` 进行账号过滤，禁止全量查询后内存过滤。
- 非本人订单 MUST 统一表现为 `ORDER_NOT_EXIST`，不得泄露订单、支付、退款或预约详情。

### INV-002: 金额事实

- 下单后 MUST 使用订单金额快照，不得重新读取其他 Variant 或销售车型最低价。
- 支付成功 MUST 按 paymentId 幂等累计 paidTotal。
- 退款完成 MUST 按 refundId 幂等累计 refundTotal。
- `paidTotal` MUST 等于该订单所有已确认成功且未重复计算的支付金额之和。
- 退款主单金额 MUST 等于其全部退款明细金额之和。
- 缺少金额快照的历史订单 MUST NOT 按当前价格自动补值。

### INV-003: 状态事实

- 状态推进 MUST 经过 OrderStateMachine 或明确的领域状态检查。
- 支付、退款、配车、交付或激活回调不得绕过前置状态。
- 相同终态的重复通知 MUST 幂等；冲突通知 MUST NOT 覆盖原终态。
- 自动锁单与手动锁单 MUST 进入同一订单锁和同一领域方法。

### INV-004: 有效单据唯一性

同一订单在同一时间 MUST 满足：

- 同一支付阶段最多一个有效 `PENDING_PAYMENT/PROCESSING` 支付单。
- 同一 `earnest_to_down` 场景最多一个 pending 补款任务。
- 最多一个 pending/processing 退款主单。
- 最多一个 booked 交付预约。

唯一性 MUST 由订单锁、数据库条件更新和唯一业务单号共同保证；锁获取失败时 MUST 拒绝操作，不得降级为无锁执行。

### INV-005: 事务与外部调用

- 订单、金额、支付/退款事实和时间线的状态变更 MUST 在同一数据库事务中提交。
- 外部 Mock 网关调用 MUST 在本地初始事务提交后执行。
- 网关调用失败 MUST 记录失败状态，不得造成本地订单与单据状态不一致。
- 本地事务提交前发生异常时 MUST 回滚本次全部数据库写入。

### INV-006: Mock 边界

- Mock MUST NOT 固定返回支付成功。
- 支付/退款终态 MUST 由经过签名、金额、状态和幂等校验的 Mock 回调驱动。
- 支付查询在本地已有终态时返回本地事实；本地处于活动状态时查询 Mock 网关，Mock 默认返回 PROCESSING。
- Mock 实现 MUST 继续通过 PaymentAdapter 隔离，不得在 Controller 或 Domain 中写死。

## 8. Fixed Acceptance Datasets

### 8.1 基础车型与协议数据

| 数据项 | 值 |
|---|---|
| saleModelCode | SM001 |
| modelCode | M001 |
| variantCode | V001 |
| variantPrice | 200000.00 |
| earnestMoneyPrice | 5000.00 |
| downPaymentPrice | 20000.00 |
| option A / B | 1000.00 / 2000.00 |
| 选择 A+B 后 dealPriceTotal | 203000.00 |
| 当前协议 | VEHICLE_PURCHASE，versionNo=1 |

### 8.2 意向金补差主链

1. 意向金下单后：depositAmount=5000.00、downPaymentAmount=20000.00、paidTotal=0，应付支付单金额=5000.00。
2. 意向金支付成功后：订单为 EARNEST_MONEY_PAID，paidTotal=5000.00。
3. 第一次转定金：difference=15000.00，只创建一个 supplementaryNo，订单仍为 EARNEST_MONEY_PAID。
4. 重复转定金请求：返回同一个 supplementaryNo，不新增补款任务。
5. 补款成功后：paidTotal=20000.00，订单为 FORMAL/DOWN_PAYMENT_PAID，downPaymentTime 不为空。
6. 相同补款回调再次到达：paidTotal 仍为 20000.00，不重复推进状态或追加成功时间线。

### 8.3 直接定金主链

- 直接定金下单时 depositAmount=0、downPaymentAmount=20000.00。
- 支付金额 MUST 为 20000.00，而不是其他 Variant 的更低定金价格。
- 支付成功后 paidTotal=20000.00 且 downPaymentTime 不为空。
- downPaymentTime 后 47 小时 59 分不得自动锁单；达到 48 小时后下一次扫描必须锁单。

### 8.4 意向金高于定金

当意向金为 20000.00、定金为 10000.00 时：

- 意向金支付成功后 paidTotal=20000.00。
- 转定金不创建补款任务。
- 转换后 upgradedFromSmall=true。
- 未锁单退款时 refundAmount=10000.00，fee=10000.00。

### 8.5 锁单退款

| paidTotal | fee 计算 | 预期 fee | 预期 refundAmount |
|---|---|---|---|
| 20000.00 | max(1000, 500) | 1000.00 | 19000.00 |
| 5000.00 | max(250, 500) | 500.00 | 4500.00 |

### 8.6 多支付退款分摊

订单存在两笔成功支付：

| paymentNo | 金额 | payTime |
|---|---|---|
| P-EARNEST | 5000.00 | 较早 |
| P-SUPPLEMENT | 15000.00 | 较晚 |

未锁单退款 20000.00 时必须生成两个退款明细：P-EARNEST 5000.00、P-SUPPLEMENT 15000.00。
一个明细 completed、另一个 processing 时，主单仍为 processing；全部 completed 后主单和订单才能完成。

### 8.7 支付异常

- PAYMENT_FAILED：订单保持原待支付状态，允许创建新支付单。
- CANCELLED：订单保持原待支付状态，允许创建新支付单。
- PROCESSING：订单不得变为已支付，重复发起返回原支付单。
- 相同幂等键重复 PAID：返回已处理，金额和状态不重复变化。
- 已 PAID 后收到 PAYMENT_FAILED：返回 PAYMENT_CONFLICT，保留 PAID。
- 已 CANCELLED 的支付单收到 PAID：记录冲突和人工核查日志，不恢复支付单或订单状态。

## 9. Requirements Completion Checklist

实施审查时必须能够提供：

- 每个 US 的自动化测试证据；
- 每个 INV 的至少一个正向和一个反向测试；
- 本文件全部固定金额样例的准确断言；
- 双账号隔离测试；
- 支付/退款重复与冲突回调测试；
- 服务重启后仍能依据数据库时间执行自动锁单的测试。
