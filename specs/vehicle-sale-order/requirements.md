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

### US-001: 浏览销售车型与选配器入口

**As a** C 端用户, **I want** 查看当前在售车型卡片列表, **so that** 我能挑选一款进入选配页。

**修订点说明（CR-011）**：SaleModel 卡片粒度由 Variant 级上提至 Carline 级（一个车系一张卡片）。返回字段移除 variantName、basePrice、downPaymentPrice，新增 carlineCode、carlineName、startingPrice（起售价）；earnestMoneyPrice 改为派生取值。startingPrice 为派生字段，实时基于该 SaleModel 下"当前可售 Variant 销售策略"（`tb_sale_model_variant_policy`）重算。CR-010 起以卡片级信息为准、可选项树由 US-021 选配器返回的设计保持不变。废弃项：variantName / basePrice / downPaymentPrice 不再出现在卡片返回中。

**Acceptance Criteria** (EARS 语法):
- WHERE 销售车型存在、listingStatus = active、当前时间在 effectiveFrom 与 effectiveTo 之间、未逻辑删除 WHEN 用户请求销售车型列表 THE SYSTEM SHALL 在 300ms 内分页返回所有符合条件的销售车型卡片信息（saleModelCode、name、icon、carlineCode、carlineName、startingPrice、earnestMoneyPrice、marketingCopy、images、sortWeight），按 sortWeight 升序、createdTime 倒序
- WHERE 销售车型在售判定通过 WHEN 系统组装卡片 startingPrice THE SYSTEM SHALL 取该 SaleModel 下当前可售 Variant（`tb_sale_model_variant_policy` 中 saleStatus = active、variantPrice 非空、availableRegions 命中用户 regionCode 或为空、时间窗有效）的 min(variantPrice) 作为起售价；startingPrice 为派生字段，MUST 实时基于 `tb_sale_model_variant_policy` 重算，MAY 加缓存，缓存以销售策略变更为失效触发
- WHERE 销售车型下无任何当前可售 Variant WHEN 系统组装销售车型列表 THE SYSTEM SHALL 不返回该 SaleModel（视为暂不可售）
- WHERE 卡片需展示 earnestMoneyPrice WHEN 系统组装卡片 THE SYSTEM SHALL 取该 SaleModel 下当前可售 Variant 销售策略行 earnestMoneyPrice 的最低值作为展示意向金（统一/最低取值口径详见 US-016）
- WHERE availableRegions 非空 WHEN 用户请求销售车型列表 THE SYSTEM SHALL 仅返回 availableRegions 包含用户 regionCode 的车型；WHERE availableRegions 为空 THE SYSTEM SHALL 返回该车型（全国可售）
- WHERE 销售车型存在、listingStatus = active、未逻辑删除 WHEN 用户请求指定车型详情 THE SYSTEM SHALL 在 300ms 内返回该车型的卡片信息和起售价；若车型不存在或已下架返回 404
- WHEN 用户请求销售车型列表且无符合条件的车型 THE SYSTEM SHALL 返回 200 + 空数组，不返回 404
- WHERE 车型存在且购车权益已配置 WHEN 用户请求购车权益 THE SYSTEM SHALL 返回该车型当前有效的购车权益信息；若无权益配置返回空列表
- WHEN 用户请求上牌地区列表 THE SYSTEM SHALL 返回可选的省市区域列表（数据来自字典服务缓存，响应时间 <200ms）

### US-002: 心愿单管理

**As a** C 端用户, **I want** 创建、修改和删除心愿单, **so that** 我能保存感兴趣的车型配置以便后续下单。

**修订点说明（CR-011）**：心愿单实体新增 `modelCode`、`variantCode` 字段；入参从 `{ saleModelCode, optionCodes[] }` 扩展为 `{ saleModelCode, modelCode, variantCode, optionCodes[] }`。唯一性键由"(userId, configurationCode, optionCodes 排序哈希)"扩展为 `(userId, saleModelCode, modelCode, variantCode, configurationCode, optionCodes 排序哈希)`。查询时实时校验项由三项扩展为五项（新增 Model 在售、Variant 在售），invalidReason 枚举新增 `MODEL_OFF_SHELF`、`VARIANT_OFF_SHELF`（原有保持）。resolveConfiguration 所用 variantCode 来自入参而非 SaleModel 直接绑定。CR-010 已确立的数量上限（5 个）、Configuration 白名单空表语义、Option 销售策略校验保持不变。

**Acceptance Criteria** (EARS 语法):
- WHERE 用户已登录、saleModelCode/modelCode/variantCode 有效、optionCodes 非空 WHEN 用户提交创建心愿单请求 THE SYSTEM SHALL 在 500ms 内依次校验：① Model 销售策略（`tb_sale_model_model_policy` 中 saleStatus = active，空表视为全开）② Variant 销售策略（`tb_sale_model_variant_policy` 中 saleStatus = active、variantPrice 非空，空表视为全开但要求 variantPrice 非空）③ MDM `resolveConfiguration(variantCode, optionCodes)` 组合合法性 ④ configurationCode 在 Configuration 销售白名单内 ⑤ 各 optionCode 处于销售 active，全部通过后创建心愿单并返回心愿单 ID；Model 失败返回 MODEL_NOT_FOR_SALE（301040），Variant 失败返回 VARIANT_NOT_FOR_SALE（301041），resolveConfiguration 失败返回 CONFIGURATION_NOT_MATCHED（301010），configurationCode 不在白名单返回 CONFIGURATION_NOT_FOR_SALE（301035），optionCode 不在销售允许范围返回 OPTION_NOT_FOR_SALE（301036）
- WHERE 用户已登录 WHEN 用户提交创建心愿单请求 THE SYSTEM SHALL 校验用户当前有效心愿单数量，若已达上限（5 个）则拒绝并返回 WISHLIST_LIMIT_EXCEEDED（错误码 301026），提示"心愿单已达上限（5个），请删除后再创建"
- WHERE 用户已存在相同 `(saleModelCode, modelCode, variantCode, configurationCode, optionCodes 排序后哈希)` 的有效心愿单 WHEN 用户提交创建心愿单请求 THE SYSTEM SHALL 拒绝并返回 DUPLICATE_WISHLIST（错误码 301027），提示"该配置已存在心愿单，请勿重复添加"
- WHERE 心愿单存在且属于当前用户 WHEN 用户提交修改心愿单请求 THE SYSTEM SHALL 更新心愿单的 modelCode/variantCode/optionCodes 信息，重新执行五项校验（Model 在售 → Variant 在售 → resolveConfiguration → Configuration 白名单 → Option 在售）；若心愿单不存在返回 404
- WHERE 心愿单存在且属于当前用户 WHEN 用户提交修改心愿单请求（`(saleModelCode, modelCode, variantCode, configurationCode, optionCodes 排序后哈希)` 与其他心愿单重复） THE SYSTEM SHALL 拒绝并返回 DUPLICATE_WISHLIST（错误码 301027）
- WHERE 心愿单存在且属于当前用户 WHEN 用户提交删除心愿单请求 THE SYSTEM SHALL 逻辑删除指定心愿单；若心愿单不存在返回 404
- WHERE 心愿单存在 WHEN 用户请求心愿单详情 THE SYSTEM SHALL 返回心愿单信息并实时校验五项：① SaleModel 仍在售（listingStatus = active、时间窗有效）② Model 仍在售（`tb_sale_model_model_policy` saleStatus = active，空表全开）③ Variant 仍在售（`tb_sale_model_variant_policy` saleStatus = active、variantPrice 非空、区域/渠道命中）④ configurationCode 仍在销售白名单（status = active）⑤ 每个 optionCode 仍在销售策略允许范围（saleStatus = active、区域/渠道命中）；任一项失效则在响应中标注 invalidReason 枚举值（SALE_MODEL_OFF_SHELF / MODEL_OFF_SHELF / VARIANT_OFF_SHELF / CONFIGURATION_OFF_SHELF / OPTION_OFF_SHELF / REGION_RESTRICTED）
- WHERE 用户已登录 WHEN 用户请求"我的车辆"列表 THE SYSTEM SHALL 合并返回用户的心愿单和订单列表，按创建时间倒序分页

### US-003: 意向金下单（小定）

**As a** C 端用户, **I want** 支付意向金创建小定订单, **so that** 我能以较低成本锁定购车意向。

**修订点说明（CR-011）**：入参增加 `modelCode`、`variantCode`，由 `{ saleModelCode, optionCodes[], customerInfo, paymentChannel, regionCode }` 扩展为 `{ saleModelCode, modelCode, variantCode, optionCodes[], customerInfo, paymentChannel, regionCode }`。防刷单校验保持不变。下单校验由 CR-010 的四步扩展为六步顺序校验（新增 Model 销售策略、Variant 销售策略两步置于 OptionCode 校验之前）。订单总价改为 variantPrice + Σ(optionPrice)，basePrice 字段废弃（或仅作起售价展示用）；意向金/定金金额来源由 SaleModel 改为命中的 Variant 销售策略行。车辆配置快照新增固化字段 carlineCode、modelCode、variantCode、modelPolicySnapshot、variantPolicySnapshot。

**Acceptance Criteria** (EARS 语法):
- WHERE 用户已登录、saleModelCode/modelCode/variantCode 有效、optionCodes 非空、regionCode 有效 WHEN 用户提交意向金下单请求 THE SYSTEM SHALL 在 1s 内生成唯一订单号、创建 SMALL 类型订单、状态设为 EARNEST_MONEY_UNPAID、创建车辆配置快照（版本 1），并通过分布式锁保证同一用户并发下单互斥
- WHERE 用户已存在未完成订单（订单状态为 EARNEST_MONEY_UNPAID 或 DOWN_PAYMENT_UNPAID） WHEN 用户提交意向金下单请求 THE SYSTEM SHALL 拒绝下单并返回 DUPLICATE_UNPAID_ORDER（错误码 301025），提示"您有未完成的订单，请先完成支付或取消后再下单"
- WHERE 手机号已关联其他用户的未完成订单（订单状态为 EARNEST_MONEY_UNPAID 或 DOWN_PAYMENT_UNPAID） WHEN 用户提交意向金下单请求 THE SYSTEM SHALL 拒绝下单并返回 DUPLICATE_UNPAID_ORDER（错误码 301025），提示"该手机号存在未完成的订单，请先完成支付或取消后再下单"
- WHERE 防刷单校验通过 WHEN 用户提交意向金下单请求 THE SYSTEM SHALL 按以下六步顺序校验，每一步失败返回对应错误码并终止：
  1. SaleModel 在售校验：Carline 级 listingStatus = active、当前时间在 effectiveFrom 与 effectiveTo 之间、availableRegions 包含 regionCode 或为空；失败返回 SALE_MODEL_NOT_EXIST（301003）或 SALE_MODEL_OFF_SHELF（沿用 301003 + 描述）
  2. Model 销售策略校验：modelCode 在 `tb_sale_model_model_policy` 中 saleStatus = active、availableRegions 包含 regionCode 或为空、channels 包含当前渠道或为空（空表视为该层全开）；失败返回 MODEL_NOT_FOR_SALE（301040）
  3. Variant 销售策略校验：variantCode 在 `tb_sale_model_variant_policy` 中 saleStatus = active、variantPrice 非空、availableRegions 包含 regionCode 或为空、channels 包含当前渠道或为空、时间窗有效（空表视为该层全开但仍要求 variantPrice 非空，由 Variant 层兜底）；失败返回 VARIANT_NOT_FOR_SALE（301041）
  4. OptionCode 销售策略校验：每个 optionCode 在 `tb_sale_model_option_policy`（归属键 `(saleModelCode, variantCode, optionCode)`）中 saleStatus = active、optionPrice 非空、availableRegions 包含 regionCode 或为空、channels 包含当前渠道或为空；失败返回 OPTION_NOT_FOR_SALE（301036）或 OPTION_REGION_RESTRICTED（301037）
  5. MDM 调用 `resolveConfiguration(variantCode, optionCodes)`：无法匹配返回 CONFIGURATION_NOT_MATCHED（301010）
  6. Configuration 销售白名单校验：configurationCode 在 `tb_sale_model_config_policy`（归属键 `(saleModelCode, variantCode, configurationCode)`）中 status = active（空白名单视为 ALL 全开）；失败返回 CONFIGURATION_NOT_FOR_SALE（301035）
- WHEN 订单创建成功 THE SYSTEM SHALL 在车辆配置快照中固化：saleModelCode、carlineCode、modelCode、variantCode、configurationCode、optionCodes[]、optionPriceBreakdown[]（含 optionFamilyCode/optionCode/optionPrice 明细）、modelPolicySnapshot、variantPolicySnapshot、configPolicySnapshot、salePolicySnapshot（命中各层销售策略行的 JSON 快照）
- WHEN 订单创建成功 THE SYSTEM SHALL 以"订单总价 = variantPolicySnapshot.variantPrice + Σ(optionPrice)"计算总价，意向金金额取命中的 Variant 销售策略行 earnestMoneyPrice，而非 SaleModel
- WHEN 意向金订单创建成功 THE SYSTEM SHALL 自动删除该用户的所有心愿单（在同一事务内）
- WHEN 意向金订单创建成功 THE SYSTEM SHALL 返回支付渠道信息和支付过期时间（基于 paymentChannelConfig.smallOrderTimeoutMinutes 配置，默认 30 分钟）

### US-004: 定金下单（大定）

**As a** C 端用户, **I want** 支付定金创建大定订单, **so that** 我能正式确认购车并进入生产排期。

**修订点说明（CR-011）**：同 US-003 的入参变更（新增 modelCode、variantCode）与六步校验顺序、快照固化、价格来源要求。其他保持原 CR-005、CR-009 等已确立的逻辑（防刷单、支付过期等）。订单总价改为 variantPrice + Σ(optionPrice)，定金金额来源由 SaleModel 改为命中的 Variant 销售策略行 downPaymentPrice。

**Acceptance Criteria** (EARS 语法):
- WHERE 用户已登录、saleModelCode/modelCode/variantCode 有效、optionCodes 非空、regionCode 有效 WHEN 用户提交定金下单请求 THE SYSTEM SHALL 在 1s 内生成唯一订单号、创建 FORMAL 类型订单、状态设为 DOWN_PAYMENT_UNPAID、创建车辆配置快照（版本 1），并通过分布式锁保证同一用户并发下单互斥
- WHERE 用户已存在未完成订单（订单状态为 EARNEST_MONEY_UNPAID 或 DOWN_PAYMENT_UNPAID） WHEN 用户提交定金下单请求 THE SYSTEM SHALL 拒绝下单并返回 DUPLICATE_UNPAID_ORDER（错误码 301025），提示"您有未完成的订单，请先完成支付或取消后再下单"
- WHERE 手机号已关联其他用户的未完成订单（订单状态为 EARNEST_MONEY_UNPAID 或 DOWN_PAYMENT_UNPAID） WHEN 用户提交定金下单请求 THE SYSTEM SHALL 拒绝下单并返回 DUPLICATE_UNPAID_ORDER（错误码 301025），提示"该手机号存在未完成的订单，请先完成支付或取消后再下单"
- WHERE 防刷单校验通过 WHEN 用户提交定金下单请求 THE SYSTEM SHALL 按以下六步顺序校验，每一步失败返回对应错误码并终止：
  1. SaleModel 在售校验：Carline 级 listingStatus = active、当前时间在 effectiveFrom 与 effectiveTo 之间、availableRegions 包含 regionCode 或为空；失败返回 SALE_MODEL_NOT_EXIST（301003）或 SALE_MODEL_OFF_SHELF（沿用 301003 + 描述）
  2. Model 销售策略校验：modelCode 在 `tb_sale_model_model_policy` 中 saleStatus = active、availableRegions 包含 regionCode 或为空、channels 包含当前渠道或为空（空表视为该层全开）；失败返回 MODEL_NOT_FOR_SALE（301040）
  3. Variant 销售策略校验：variantCode 在 `tb_sale_model_variant_policy` 中 saleStatus = active、variantPrice 非空、availableRegions 包含 regionCode 或为空、channels 包含当前渠道或为空、时间窗有效（空表视为该层全开但仍要求 variantPrice 非空，由 Variant 层兜底）；失败返回 VARIANT_NOT_FOR_SALE（301041）
  4. OptionCode 销售策略校验：每个 optionCode 在 `tb_sale_model_option_policy`（归属键 `(saleModelCode, variantCode, optionCode)`）中 saleStatus = active、optionPrice 非空、availableRegions 包含 regionCode 或为空、channels 包含当前渠道或为空；失败返回 OPTION_NOT_FOR_SALE（301036）或 OPTION_REGION_RESTRICTED（301037）
  5. MDM 调用 `resolveConfiguration(variantCode, optionCodes)`：无法匹配返回 CONFIGURATION_NOT_MATCHED（301010）
  6. Configuration 销售白名单校验：configurationCode 在 `tb_sale_model_config_policy`（归属键 `(saleModelCode, variantCode, configurationCode)`）中 status = active（空白名单视为 ALL 全开）；失败返回 CONFIGURATION_NOT_FOR_SALE（301035）
- WHEN 订单创建成功 THE SYSTEM SHALL 在车辆配置快照中固化：saleModelCode、carlineCode、modelCode、variantCode、configurationCode、optionCodes[]、optionPriceBreakdown[]（含 optionFamilyCode/optionCode/optionPrice 明细）、modelPolicySnapshot、variantPolicySnapshot、configPolicySnapshot、salePolicySnapshot（命中各层销售策略行的 JSON 快照）
- WHEN 订单创建成功 THE SYSTEM SHALL 以"订单总价 = variantPolicySnapshot.variantPrice + Σ(optionPrice)"计算总价，定金金额取命中的 Variant 销售策略行 downPaymentPrice，而非 SaleModel
- WHEN 定金订单创建成功 THE SYSTEM SHALL 自动删除该用户的所有心愿单（在同一事务内）
- WHEN 定金订单创建成功 THE SYSTEM SHALL 返回支付渠道信息和支付过期时间（基于 paymentChannelConfig.downPaymentTimeoutMinutes 配置）

### US-005: 订单支付

**As a** C 端用户, **I want** 通过多种支付渠道完成订单支付, **so that** 我能完成购车付款流程。

**Acceptance Criteria** (EARS 语法):
- WHERE 订单状态为 EARNEST_MONEY_UNPAID 或 DOWN_PAYMENT_UNPAID、未超过支付过期时间、获取分布式锁成功 WHEN 用户发起支付请求（含订单号和支付渠道） THE SYSTEM SHALL 在 2s 内创建支付记录（状态 PENDING_PAYMENT）并返回支付凭证信息；若获取锁失败返回并发冲突错误（错误码 301032）
- WHERE 支付记录存在、状态为 PENDING_PAYMENT、回调金额与订单金额一致（精度到分） WHEN 支付网关回调通知支付成功 THE SYSTEM SHALL 更新支付状态为 PAID 并在同一事务内触发订单状态流转
- WHERE 支付成功且订单状态为 EARNEST_MONEY_UNPAID WHEN 支付状态更新为 PAID THE SYSTEM SHALL 将订单状态流转为 EARNEST_MONEY_PAID
- WHERE 支付成功且订单状态为 DOWN_PAYMENT_UNPAID WHEN 支付状态更新为 PAID THE SYSTEM SHALL 将订单状态流转为 DOWN_PAYMENT_PAID
- WHEN 支付回调到达 THE SYSTEM SHALL 使用支付流水号作为幂等键防止重复处理，重复回调直接返回成功
- IF 订单状态不是 EARNEST_MONEY_UNPAID 或 DOWN_PAYMENT_UNPAID THEN THE SYSTEM SHALL 拒绝发起支付
- WHILE 支付处理中 THE SYSTEM SHALL 持有该订单的分布式锁（TTL 30s，操作完成后自动释放）

### US-006: 意向金转定金

**As a** C 端用户, **I want** 将已支付意向金的小定订单升级为大定订单, **so that** 我能正式确认购车。

**Acceptance Criteria** (EARS 语法):

#### 状态转换
- WHERE 订单当前状态为 EARNEST_MONEY_PAID、获取分布式锁成功 WHEN 用户提交意向金转定金请求 THE SYSTEM SHALL 在 1s 内将订单类型从 SMALL 变更为 FORMAL、状态从 EARNEST_MONEY_PAID 流转为 DOWN_PAYMENT_PAID，并记录时间线事件
- IF 订单当前状态不是 EARNEST_MONEY_PAID THEN THE SYSTEM SHALL 拒绝操作并返回状态不合法错误
- IF 获取分布式锁失败 THEN THE SYSTEM SHALL 返回并发冲突错误（错误码 301033）

#### 补充信息采集
- WHEN 用户提交意向金转定金请求 THE SYSTEM SHALL 支持采集以下可选信息：客户类型（customerType）、支付方式（paymentMethod）、订购人类型（orderPersonType）、订购人姓名（orderPersonName）、订购人证件类型（orderPersonIdType）、订购人证件号码（orderPersonIdNum）、购买计划（purchasePlan）、上牌城市代码（licenseCityCode）、下单门店编码（orderStoreCode）、交付门店编码（deliveryStoreCode）
- IF customerType 不为空且不在枚举范围内（personal） THEN THE SYSTEM SHALL 拒绝操作并返回参数错误
- IF paymentMethod 不为空且不在枚举范围内（full_payment、loan） THEN THE SYSTEM SHALL 拒绝操作并返回参数错误
- WHERE orderPersonName 和 orderPersonIdNum 均非空 WHEN 保存订购人信息 THE SYSTEM SHALL 同步更新订单参与方表（vso_order_party）

#### 差额支付
- WHERE 意向金金额 < 定金金额（根据销售车型配置） WHEN 用户提交意向金转定金请求 THE SYSTEM SHALL 计算差额（定金金额 - 意向金金额），创建差额支付任务（状态 PENDING），并在 1s 内返回差额支付信息（含差额金额、支付渠道、支付过期时间）
- WHERE 差额支付任务存在且状态为 PENDING、未超过支付过期时间（默认 30 分钟，基于 paymentChannelConfig.downPaymentTimeoutMinutes 配置） WHEN 用户发起差额支付 THE SYSTEM SHALL 调用支付网关创建支付订单并返回支付凭证
- WHERE 差额支付成功（支付网关回调通知） WHEN 支付状态更新为 PAID THE SYSTEM SHALL 在同一事务内更新订单类型为 FORMAL、状态流转为 DOWN_PAYMENT_PAID、更新订单已支付金额（paidTotal += 差额金额），并记录时间线事件
- IF 差额支付超过 30 分钟未完成 THEN THE SYSTEM SHALL 自动取消差额支付任务，订单状态保持 EARNEST_MONEY_PAID 不变，并记录超时事件
- WHERE 意向金金额 >= 定金金额 WHEN 用户提交意向金转定金请求 THE SYSTEM SHALL 直接完成状态转换，不创建差额支付任务；若意向金金额 > 定金金额，差额部分在后续退款流程中处理（关联 US-008）

#### 支付记录
- WHERE 差额 > 0 且差额支付成功 WHEN 创建支付记录 THE SYSTEM SHALL 记录支付流水号、支付金额（差额金额）、支付渠道、支付时间，并关联到原订单
- WHEN 差额支付回调到达 THE SYSTEM SHALL 使用支付流水号作为幂等键防止重复处理，重复回调直接返回成功

#### 失败回滚
- WHEN 转定金操作执行过程中发生异常 THE SYSTEM SHALL 回滚所有数据库变更至操作前状态（EARNEST_MONEY_PAID），释放分布式锁，并记录失败原因和时间线事件
- WHERE 差额支付任务已创建但支付失败 WHEN 支付网关回调通知支付失败 THE SYSTEM SHALL 更新差额支付任务状态为 FAILED，订单状态保持 EARNEST_MONEY_PAID 不变，并通知用户支付失败
- WHEN 差额支付任务因超时自动取消 THE SYSTEM SHALL 记录超时事件，订单状态保持 EARNEST_MONEY_PAID 不变，用户可重新发起转定金操作

#### 幂等性
- WHEN 用户重复提交相同订单的意向金转定金请求（订单号相同） THE SYSTEM SHALL 通过订单号作为幂等键保证不重复处理，直接返回上次操作结果；若上次操作未完成（存在进行中的差额支付任务），返回当前差额支付任务状态
- WHERE 订单已成功转为 FORMAL 状态 WHEN 用户再次提交转定金请求 THE SYSTEM SHALL 直接返回成功，不重复执行转换操作

#### 并发控制
- WHILE 意向金转定金操作执行中 THE SYSTEM SHALL 持有该订单的分布式锁（场景 `convert`，TTL 30s，操作完成后自动释放）
- WHILE 差额支付操作执行中 THE SYSTEM SHALL 持有该订单的分布式锁（场景 `payment`，TTL 30s，操作完成后自动释放）

### US-007: 订单改配

**As a** C 端用户, **I want** 在锁单前修改订单的车辆配置, **so that** 我能调整车辆选装方案并处理价格差异。

**修订点说明（CR-011）**：入参由 `{ orderNo, optionCodes[] }` 扩展为 `{ orderNo, variantCode?, optionCodes[] }`。saleModelCode 与 modelCode 均不可变更（固定为下单时的取值）：跨 SaleModel 变更返回 SALE_MODEL_CHANGE_NOT_ALLOWED（301044），跨 Model（动力总成切换，本质是换车）返回 MODEL_CHANGE_NOT_ALLOWED（301043），业务上要求取消重下。允许同 Model 下的 variantCode 变更，切换 Variant 时同样执行 Variant 销售策略校验。价格重算基于订单快照固化的 `variantPolicySnapshot.variantPrice` + `salePolicySnapshot` 各 optionPrice 计算原价；新 Variant / 新 Option 若不在快照中则按当前 `tb_sale_model_variant_policy` / `tb_sale_model_option_policy` 实时价计入。

**Acceptance Criteria** (EARS 语法):

#### 配置变更
- WHERE 订单状态为 EARNEST_MONEY_PAID、DOWN_PAYMENT_UNPAID 或 DOWN_PAYMENT_PAID、buildConfigLock=false、获取分布式锁成功 WHEN 用户提交改配请求（含 orderNo、variantCode?、optionCodes[]） THE SYSTEM SHALL 在 2s 内按以下顺序校验并执行：
  1. 改配粒度边界校验：IF 请求中 saleModelCode 与订单不一致 THEN 返回 SALE_MODEL_CHANGE_NOT_ALLOWED（301044）；IF 请求中 modelCode 与订单不一致 THEN 返回 MODEL_CHANGE_NOT_ALLOWED（301043）
  2. Variant 销售策略校验（仅当 variantCode 发生变更时执行）：新 variantCode 必须属于订单 modelCode 下的合法 Variant，且在 `tb_sale_model_variant_policy` 中 saleStatus = active、variantPrice 非空、区域/渠道命中、时间窗有效；失败返回 VARIANT_NOT_FOR_SALE（301041）
  3. OptionCode 销售策略校验：每个 optionCode 在 `tb_sale_model_option_policy`（归属键 `(saleModelCode, variantCode, optionCode)`）中 saleStatus = active、optionPrice 非空；失败返回 OPTION_NOT_FOR_SALE（301036）
  4. MDM 调用 `resolveConfiguration(variantCode, optionCodes)`：无法匹配返回 CONFIGURATION_NOT_MATCHED（301010）
  5. Configuration 销售白名单校验：configurationCode 在 `tb_sale_model_config_policy` 中 status = active；失败返回 CONFIGURATION_NOT_FOR_SALE（301035）
  6. 校验通过后创建新版本车辆配置快照、记录时间线事件
- IF 订单状态不在允许改配的状态范围内 THEN THE SYSTEM SHALL 拒绝改配并返回状态不合法错误
- IF 订单已锁单（buildConfigLock=true） THEN THE SYSTEM SHALL 拒绝改配并返回 CONFIGURATION_HAS_LOCKED（301008，原 SALE_MODEL_CONFIG_HAS_LOCKED 改名）
- IF 获取分布式锁失败 THEN THE SYSTEM SHALL 返回并发冲突错误（错误码 301033）

#### 价格重算与差额处理
- WHERE 改配成功 WHEN 计算价格差额 THE SYSTEM SHALL 基于订单快照固化的 `variantPolicySnapshot.variantPrice` + `salePolicySnapshot` 中各 optionPrice 计算原配置总价，与新组合价格对比；新 Variant 若不在原快照中则按当前 `tb_sale_model_variant_policy.variantPrice` 计入，新 OptionCode 若不在原快照中则按当前 `tb_sale_model_option_policy.optionPrice` 计入
- WHERE 改配成功、新版本快照生成 THE SYSTEM SHALL 同时刷新快照中新增 variantCode / OptionCode 对应的策略行（variantPolicySnapshot、salePolicySnapshot；已有的不动）
- IF 差额 > 0 THEN THE SYSTEM SHALL 创建补款任务，状态为 PENDING，并通过领域事件通知用户补款
- IF 差额 < 0 THEN THE SYSTEM SHALL 创建退款任务，状态为 PENDING，退款金额 = |差额|，并通过领域事件通知用户退款
- IF 差额 = 0 THEN THE SYSTEM SHALL 仅更新配置快照，不创建补款/退款任务

#### 补款流程
- WHERE 存在待支付的补款任务、订单状态允许支付 WHEN 用户发起补款支付 THE SYSTEM SHALL 调用支付网关创建支付订单，支付成功后更新补款任务状态为 COMPLETED
- IF 补款任务超过 30 分钟未支付 THEN THE SYSTEM SHALL 自动取消补款任务并记录超时事件

#### 退款流程
- WHERE 存在待处理的退款任务 WHEN 退款任务创建成功 THE SYSTEM SHALL 在 1s 内调用退款接口发起退款，退款金额 = |差额|
- WHERE 退款处理中 WHEN 支付网关回调退款成功 THE SYSTEM SHALL 更新退款任务状态为 COMPLETED 并记录退款流水号
- IF 退款失败 THEN THE SYSTEM SHALL 标记退款任务为 FAILED 并触发人工审核流程

#### 幂等性
- WHEN 用户重复提交相同配置的改配请求 THE SYSTEM SHALL 通过幂等键（订单号+配置版本号）保证不重复处理，直接返回上次改配结果

### US-008: 订单取消与退款

**As a** C 端用户, **I want** 取消订单并申请退款, **so that** 我能在不需要时终止购车流程。

**Acceptance Criteria** (EARS 语法):
- WHERE 订单存在且属于当前用户、订单状态允许取消（EARNEST_MONEY_UNPAID、EARNEST_MONEY_PAID、DOWN_PAYMENT_UNPAID、DOWN_PAYMENT_PAID）、获取分布式锁成功 WHEN 用户提交取消订单请求 THE SYSTEM SHALL 在 1s 内将订单状态设为 CANCEL 并记录取消原因和时间线事件
- WHERE 订单状态为 EARNEST_MONEY_PAID 或 DOWN_PAYMENT_PAID、获取分布式锁成功 WHEN 用户提交退款申请 THE SYSTEM SHALL 将订单状态设为 REFUND_APPLY 并创建退款任务，退款金额等于已支付金额（全额退款，手续费为 0）
- WHERE 订单状态为 ARRANGE_PRODUCTION、获取分布式锁成功 WHEN 用户提交退款申请 THE SYSTEM SHALL 将订单状态设为 REFUND_APPLY 并创建退款任务，退款金额 = 已支付金额 - max(已支付金额 × 5%, 500)，其中 5% 为手续费比例，500 为最低手续费金额（单位：元）
- WHERE 订单状态为 ALLOCATION_VEHICLE 或之后状态（APPLY_TRANSPORT、PREPARE_TRANSPORT、TRANSPORTING、PREPARE_DELIVER、DELIVERED、ACTIVATED） WHEN 用户提交退款申请 THE SYSTEM SHALL 拒绝退款并返回状态不合法错误（订单已进入生产/发运阶段，不支持退款）
- WHERE 订单类型为 FORMAL（由 SMALL 升级而来）、订单状态为 EARNEST_MONEY_PAID 或 DOWN_PAYMENT_PAID、已支付金额 > 定金金额（存在超额支付）、获取分布式锁成功 WHEN 用户提交退款申请 THE SYSTEM SHALL 将订单状态设为 REFUND_APPLY 并创建退款任务，退款金额 = 已支付金额 - 定金金额（超额部分退款，定金不退还），手续费为 0，并记录时间线事件
- WHILE 取消/退款操作执行中 THE SYSTEM SHALL 持有该订单的分布式锁（TTL 30s，操作完成后自动释放）
- IF 获取分布式锁失败 THEN THE SYSTEM SHALL 返回并发冲突错误（错误码 301033）
- WHEN 退款申请提交成功 THE SYSTEM SHALL 记录退款记录（含退款金额、手续费金额、退款原因、申请时间）

### US-009: 订单锁单

**As a** B 端运营人员, **I want** 锁定已支付定金的订单, **so that** 冻结车辆配置并进入生产排期。

**Acceptance Criteria** (EARS 语法):
- WHERE 订单状态为 DOWN_PAYMENT_PAID、获取分布式锁成功 WHEN 运营人员对订单执行锁单 THE SYSTEM SHALL 在 1s 内将订单状态流转为 ARRANGE_PRODUCTION 并设置 buildConfigLock=true
- WHEN 锁单成功 THE SYSTEM SHALL 记录锁单时间并创建超时提醒任务（LOCK_TIMEOUT，阈值 2880 分钟，策略 remind）
- IF 订单状态不是 DOWN_PAYMENT_PAID THEN THE SYSTEM SHALL 拒绝锁单操作并返回状态不合法错误
- IF 获取分布式锁失败 THEN THE SYSTEM SHALL 返回并发冲突错误（错误码 301033）

### US-010: 订单审核

**As a** B 端运营人员, **I want** 审核订单通过或驳回, **so that** 确保订单信息合规后进入后续流程。

**Acceptance Criteria** (EARS 语法):
- WHERE 订单处于待审核状态、X-Operator-Id 请求头存在且有效 WHEN 运营人员审核通过订单 THE SYSTEM SHALL 在 1s 内记录操作人并更新审核状态为通过，创建超时提醒任务（FORMAL_ORDER_AUDIT_TIMEOUT，阈值 1440 分钟，策略 remind）
- WHERE 订单处于待审核状态、驳回原因分类和详情非空、X-Operator-Id 请求头存在且有效 WHEN 运营人员审核驳回订单 THE SYSTEM SHALL 在 1s 内记录驳回原因分类、驳回详情和操作人并更新审核状态为驳回，累加驳回次数，创建驳回提醒任务（AUDIT_REJECT_REMIND，阈值 72 小时，策略 remind）和驳回超时关闭任务（AUDIT_REJECT_TIMEOUT，阈值 168 小时，策略 auto_close）
- IF 驳回原因分类或详情为空 THEN THE SYSTEM SHALL 拒绝审核驳回操作并返回驳回原因必填错误（错误码 301029）
- WHERE 订单处于审核驳回状态、累计驳回次数 < 3 WHEN 用户修正信息后重新提交审核 THE SYSTEM SHALL 在 1s 内更新审核状态为待审核、清空驳回原因分类、记录审批记录（RESUBMIT 类型，关联上次驳回记录）、取消驳回阶段超时任务、创建审核超时提醒任务
- IF 累计驳回次数 >= 3 THEN THE SYSTEM SHALL 拒绝重提并返回重提次数超限错误（错误码 301028）
- WHERE 订单处于审核驳回状态 WHEN 用户主动取消订单 THE SYSTEM SHALL 在 1s 内将订单状态流转为已取消并记录时间线事件
- WHERE 订单处于审核驳回状态且超过 168 小时未重提 WHEN 超时任务触发 THE SYSTEM SHALL 自动将订单状态流转为已取消并记录时间线事件
- WHEN 审核操作执行 THE SYSTEM SHALL 从 X-Operator-Id 请求头提取操作人 ID 并记录到审核记录中；若请求头缺失返回 401

**驳回原因分类枚举**：

| 枚举值 | 含义 |
|--------|------|
| INCOMPLETE_INFO | 资料不全 |
| INCORRECT_INFO | 信息有误 |
| RISK_BLOCK | 风险拦截 |
| DUPLICATE_ORDER | 重复订单 |
| OTHER | 其他 |

### US-011: 配车（分配 VIN）

**As a** B 端运营人员, **I want** 为订单分配具体车辆（VIN）, **so that** 将生产出的车辆与订单绑定。

**Acceptance Criteria** (EARS 语法):

**配车绑定**
- WHERE 订单状态允许配车（ARRANGE_PRODUCTION）、VIN 未被其他订单占用、获取分布式锁成功 WHEN 运营人员提交配车请求（含订单号和 VIN） THE SYSTEM SHALL 在 2s 内将 VIN 绑定到订单并更新订单状态为 ALLOCATION_VEHICLE
- WHEN 配车成功 THE SYSTEM SHALL 创建车辆分配记录（含占用过期时间，从 vso_config_vehicle_occupancy 读取配置，默认 72 小时）
- IF VIN 已被其他订单占用 THEN THE SYSTEM SHALL 拒绝配车并返回 VIN 冲突错误（错误码 301030）
- IF 获取分布式锁失败 THEN THE SYSTEM SHALL 返回并发冲突错误（错误码 301034）
- WHERE 订单已绑定 VIN、订单状态为 ALLOCATION_VEHICLE、获取分布式锁成功 WHEN 运营人员提交换绑请求（含订单号和新 VIN） THE SYSTEM SHALL 解绑旧 VIN 后绑定新 VIN，并更新占用过期时间
- IF 换绑时新 VIN 已被其他订单占用 THEN THE SYSTEM SHALL 拒绝换绑并返回 VIN 冲突错误，旧 VIN 保持绑定状态

**VIN 冲突检测**
- WHEN 配车或换绑请求处理 THE SYSTEM SHALL 通过数据库唯一索引（VIN + assign_status IN ('ASSIGNED', 'BOUND')）+ 分布式锁双重保障 VIN 唯一性
- IF 同一 VIN 被并发请求分配 THEN THE SYSTEM SHALL 保证只有一个请求成功，其他请求返回 VIN 冲突错误

**VIN 超时释放**
- IF VIN 占用超过配置的过期时间（从 vso_config_vehicle_occupancy 读取，默认 72 小时）THEN THE SYSTEM SHALL 自动释放 VIN 并更新配车状态为 EXPIRED，订单状态回退至 ARRANGE_PRODUCTION
- WHEN VIN 超时释放 THE SYSTEM SHALL 记录时间线事件（类型：VEHICLE_ASSIGNMENT_EXPIRED）并发送通知给订单归属运营人员
- WHERE 订单状态为 ALLOCATION_VEHICLE 且配车状态为 ASSIGNED/BOUND WHEN VIN 超时释放任务执行 THE SYSTEM SHALL 先获取分布式锁再执行释放操作

**订单取消/退款时 VIN 释放**
- WHERE 订单已绑定 VIN WHEN 订单取消或退款完成 THE SYSTEM SHALL 释放已绑定的 VIN 并更新配车状态为 RELEASED，记录释放时间为当前时间
- WHEN VIN 因订单取消释放 THE SYSTEM SHALL 记录时间线事件（类型：VEHICLE_ASSIGNMENT_RELEASED，原因：ORDER_CANCELLED/REFUND_COMPLETED）

**主动解绑 VIN**
- WHERE 订单状态为 ALLOCATION_VEHICLE、解绑原因非空、获取分布式锁成功 WHEN 运营人员提交解绑请求 THE SYSTEM SHALL 释放 VIN 并更新配车状态为 UNBOUND，订单状态回退至 ARRANGE_PRODUCTION
- WHEN 主动解绑成功 THE SYSTEM SHALL 记录时间线事件（类型：VEHICLE_ASSIGNMENT_UNBOUND，含解绑原因）
- IF 订单状态不是 ALLOCATION_VEHICLE THEN THE SYSTEM SHALL 拒绝解绑并返回状态不合法错误

**配车状态机**
- 配车记录状态（assign_status）状态机：ASSIGNED（已分配）→ BOUND（已绑定）→ RELEASED/EXPIRED/UNBOUND（已释放/过期/解绑）
- WHERE 配车状态为 ASSIGNED WHEN 订单进入发运流程 THE SYSTEM SHALL 更新配车状态为 BOUND
- IF 配车状态为 RELEASED/EXPIRED/UNBOUND THEN THE SYSTEM SHALL 不再占用该 VIN

**VIN 来源校验**
- WHEN 配车请求处理 THE SYSTEM SHALL 调用车辆库存服务校验 VIN 是否存在且状态可用（状态为 IN_STOCK 或 ALLOCATED）
- IF VIN 不存在或状态不可用 THEN THE SYSTEM SHALL 拒绝配车并返回 VIN 无效错误（错误码 301031）
- WHEN 配车成功 THE SYSTEM SHALL 调用车辆库存服务更新车辆状态为 ALLOCATED

**并发控制**
- WHILE 配车/换绑/解绑操作执行中 THE SYSTEM SHALL 持有该订单的分布式锁（场景 `bindVehicle`，TTL 30s）

**幂等性**
- WHEN 运营人员重复提交相同 VIN 的配车请求 THE SYSTEM SHALL 通过幂等键（订单号 + VIN）保证不重复处理，直接返回上次配车结果

**查询可配车订单**
- WHEN 运营人员查询可配车订单列表 THE SYSTEM SHALL 仅返回状态为 ARRANGE_PRODUCTION 的订单

### US-012: 发运管理

**As a** B 端运营人员, **I want** 管理订单的发运流程, **so that** 跟踪车辆从工厂到交付中心的物流状态。

**Acceptance Criteria** (EARS 语法):
- WHERE 订单状态为 ALLOCATION_VEHICLE、获取分布式锁成功 WHEN 运营人员提交发运申请 THE SYSTEM SHALL 在 1s 内将订单状态流转为 APPLY_TRANSPORT 并记录发运申请时间
- WHERE 订单状态为 APPLY_TRANSPORT WHEN 服务间调用通知准备发运 THE SYSTEM SHALL 将订单状态流转为 PREPARE_TRANSPORT 并记录时间线事件
- WHERE 订单状态为 PREPARE_TRANSPORT WHEN 服务间调用通知发运中 THE SYSTEM SHALL 将订单状态流转为 TRANSPORTING 并记录时间线事件
- IF 服务间调用携带的订单状态与当前状态不匹配 THEN THE SYSTEM SHALL 拒绝状态流转并返回状态不合法错误，不记录时间线

#### 倒计时补偿机制
- WHERE 订单状态流转为 PREPARE_TRANSPORT THE SYSTEM SHALL 创建倒计时任务（期望状态 TRANSPORTING，阈值 24 小时），倒计时触发时发送告警通知给运营人员
- WHERE 订单状态流转为 TRANSPORTING THE SYSTEM SHALL 取消 PREPARE_TRANSPORT 倒计时任务，创建新倒计时任务（期望状态 PREPARE_DELIVER，阈值 48 小时），倒计时触发时发送告警通知
- WHERE 服务间回调成功推进状态 THE SYSTEM SHALL 取消对应倒计时任务，订单状态不受影响

### US-013: 交付管理

**As a** B 端运营人员, **I want** 管理订单的交付流程, **so that** 完成车辆从交付中心到客户手中的最后环节。

**Acceptance Criteria** (EARS 语法):
- WHERE 订单状态为 TRANSPORTING WHEN 运营人员分配交付人员 THE SYSTEM SHALL 在 1s 内记录交付人员信息到订单并记录时间线事件
- WHERE 订单状态为 TRANSPORTING 且已分配交付人员 WHEN 服务间调用通知准备交付 THE SYSTEM SHALL 将订单状态流转为 PREPARE_DELIVER 并记录时间线事件
- WHERE 订单状态为 PREPARE_DELIVER WHEN 服务间调用通知已交付 THE SYSTEM SHALL 将订单状态流转为 DELIVERED 并记录时间线事件
- WHERE 订单状态为 DELIVERED WHEN 服务间调用通知已激活 THE SYSTEM SHALL 将订单状态流转为 ACTIVATED 并记录时间线事件

#### 倒计时补偿机制
- WHERE 订单状态流转为 PREPARE_DELIVER THE SYSTEM SHALL 创建倒计时任务（期望状态 DELIVERED，阈值 24 小时），倒计时触发时发送告警通知给运营人员
- WHERE 订单状态流转为 DELIVERED THE SYSTEM SHALL 取消 PREPARE_DELIVER 倒计时任务，创建新倒计时任务（期望状态 ACTIVATED，阈值 72 小时），倒计时触发时发送告警通知
- WHERE 服务间回调成功推进状态 THE SYSTEM SHALL 取消对应倒计时任务，订单状态不受影响

### US-014: 订单关闭

**As a** B 端运营人员, **I want** 关闭异常或无效订单, **so that** 终止不再需要的订单流程。

**Acceptance Criteria** (EARS 语法):
- WHERE 订单存在且状态非终态（非 CANCEL、EXPIRED、ACTIVATED）、X-Operator-Id 请求头存在、关闭原因非空、获取分布式锁成功 WHEN 运营人员提交关闭订单请求（含关闭原因） THE SYSTEM SHALL 在 1s 内将订单状态设为 CLOSED 并记录关闭原因和操作人、时间线事件
- IF 获取分布式锁失败 THEN THE SYSTEM SHALL 返回并发冲突错误（错误码 301033）

### US-015: 订单物理删除

**As a** B 端运营人员, **I want** 物理删除已取消的订单及其所有关联数据, **so that** 清理测试或无效数据。

**Acceptance Criteria** (EARS 语法):
- WHERE 订单状态为 CANCEL、操作人具有 `completeVehicle:order:info:physicalDelete` 权限 WHEN 运营人员提交物理删除请求 THE SYSTEM SHALL 在 5s 内删除订单主表及所有关联数据（参与方、快照、金额、分配、支付、退款等）并创建影子审计记录（含操作人、时间、删除数据快照）
- IF 订单状态不是 CANCEL THEN THE SYSTEM SHALL 拒绝物理删除并返回状态不合法错误
- WHEN 物理删除执行 THE SYSTEM SHALL 校验操作人具有 `completeVehicle:order:info:physicalDelete` 权限；若无权限返回 403

### US-016: 销售车型管理（MPT）

**As a** B 端运营人员, **I want** 维护销售车型（SaleModel）与上游 MDM Carline 的绑定及销售域属性, **so that** 控制车系上架节奏与商业策略。

**修订点说明（CR-011）**：SaleModel 由"1:1 Variant"重构为"1:1 Carline"。移除字段 `variantCode`、`basePrice`、`earnestMoneyPrice`、`downPaymentPrice`，新增字段 `carlineCode`；起售价、意向金、定金均下沉到 Variant 销售策略层（见 US-022）。修改锁定校验由 variantCode 上提至 carlineCode：存在未完成订单或活跃心愿单时返回 SALE_MODEL_CARLINE_LOCKED（301042）。同步 MDM 数据接口范围由单 Variant 扩展为整条 Carline 链路（Carline 元数据 + 该 Carline 下全部 Model 元数据 + Model 与 Variant 的归属关系 + Variant/Configuration/Option 投影）。废弃项：CR-010 的 variantCode/basePrice/earnestMoneyPrice/downPaymentPrice 字段与 301038 锁定语义（301038 作为兼容保留，描述更新见 §6）。

**Acceptance Criteria** (EARS 语法):
- WHERE saleModelCode 唯一、必填字段（saleModelCode、name、carlineCode、effectiveFrom）非空 WHEN 运营人员创建销售车型 THE SYSTEM SHALL 在 1s 内保存车型信息并返回 ID；若 saleModelCode 重复返回唯一性冲突错误
- WHERE carlineCode 在 MDM 投影中存在且未停用 WHEN 运营人员创建销售车型 THE SYSTEM SHALL 校验通过；若 carlineCode 不存在或已停用返回参数错误
- WHERE 同 carlineCode 已存在 SaleModel WHEN 运营人员创建销售车型 THE SYSTEM SHALL 拒绝创建并返回唯一性冲突错误（1:1 约束）
- WHERE 车型存在且未逻辑删除 WHEN 运营人员更新销售车型 THE SYSTEM SHALL 更新对应字段并记录更新时间；WHERE 更新字段包含 listingStatus THE SYSTEM SHALL 允许 active ↔ off_shelf 切换，不影响存量订单
- WHERE 车型存在、更新字段包含 carlineCode WHEN 运营人员更新销售车型 THE SYSTEM SHALL 校验"无未完成订单（非终态）+ 无活跃心愿单"，否则返回 SALE_MODEL_CARLINE_LOCKED（301042）
- WHERE 车型存在且无关联的活跃订单（state ∈ {EARNEST_MONEY_PAID 之后非终态}） WHEN 运营人员删除销售车型 THE SYSTEM SHALL 逻辑删除指定车型；若存在关联活跃订单返回约束冲突错误
- WHEN 运营人员删除销售车型 THE SYSTEM SHALL 在同一事务内级联将该 saleModelCode 关联的四层销售策略标记为失效（off_shelf）：① Model 销售策略（`tb_sale_model_model_policy`）② Variant 销售策略（`tb_sale_model_variant_policy`）③ Configuration 销售白名单（`tb_sale_model_config_policy`）④ OptionCode 销售策略（`tb_sale_model_option_policy`）；级联操作不影响历史订单快照数据
- WHEN 运营人员查询销售车型列表 THE SYSTEM SHALL 支持按 saleModelCode、name、carlineCode、listingStatus、时间范围分页查询，默认按 sortWeight 升序 + createTime 倒序，单页最大 100 条，响应时间 <500ms
- WHERE 车型存在 WHEN 运营人员触发同步 MDM 数据 THE SYSTEM SHALL 强制刷新该 carlineCode 整条链路的本地 MDM 投影（Carline 元数据、该 Carline 下全部 Model 元数据、Model→Variant 归属关系、各 Variant 标配 options/所属 Configuration 列表/OptionFamily 树/各 OptionCode 元数据），并返回同步统计（新增/更新/删除数量），响应时间 <5s
- WHERE 卡片展示需要统一意向金/起售价 WHEN 运营查询车型卡片派生值 THE SYSTEM SHALL 以"该 SaleModel 下当前可售 Variant 销售策略行的 min(variantPrice) 为 startingPrice、min(earnestMoneyPrice) 为展示意向金"口径计算（与 US-001 一致）

### US-021: 销售车型选配器（C 端）

**As a** C 端用户, **I want** 在选配页按"车系 → Model → Variant"三段式逐层选择并完成配置, **so that** 我能得到一台具体可下单的车辆配置和实时报价。

**修订点说明（CR-011）**：选配器返回结构由"单 Variant 的 selectableFamilies"重构为三层（carline → models[] → variants[]）。Model 层、Variant 层均叠加销售策略过滤与营销元数据；价格（variantPrice/earnestMoneyPrice/downPaymentPrice）下沉至 Variant 层。报价入参增加 modelCode、variantCode，校验顺序与 US-003 六步对齐（去掉防刷单步，即 SaleModel → Model → Variant → Option → resolveConfiguration → Configuration 白名单）。Option / Configuration 销售策略沿用 CR-010 逻辑，归属键调整为 `(saleModelCode, variantCode, ...)`。MDM 超时兜底策略保持：选配器只读可降级到本地投影，报价不允许降级。

**Acceptance Criteria** (EARS 语法):

#### 配置器数据组装（三段式）
- WHERE SaleModel 存在、listingStatus = active、当前时间在 effectiveFrom 与 effectiveTo 之间、availableRegions 包含 regionCode 或为空 WHEN 用户请求选配器 THE SYSTEM SHALL 在 800ms 内返回如下三层结构：
  ```
  {
    carlineCode, carlineName,
    models: [
      { modelCode, modelName, marketingName?, marketingImage?, marketingCopy?, saleStatus, sortWeight,
        variants: [
          { variantCode, variantName, marketingName?, marketingImage?, marketingCopy?, sortWeight,
            variantPrice, earnestMoneyPrice, downPaymentPrice,
            standardOptions: [...],            // 来自 MDM 投影（只读展示）
            selectableFamilies: [...]          // 来自 Option 销售策略 + MDM optionTree（保留 CR-010 逻辑）
          }
        ]
      }
    ]
  }
  ```
- WHERE Model 在 `tb_sale_model_model_policy` 中 saleStatus = off_shelf、或区域/渠道未命中 WHEN 组装 models THE SYSTEM SHALL 过滤掉该 Model；WHERE Model 销售策略表对该 SaleModel 无任何行 THE SYSTEM SHALL 视为 Model 层 ALL 全开（返回该 Carline 下全部 MDM 合法 Model）
- WHERE Variant 在 `tb_sale_model_variant_policy` 中 saleStatus = off_shelf、或 variantPrice 为 null、或区域/渠道未命中、或时间窗失效 WHEN 组装 variants THE SYSTEM SHALL 过滤掉该 Variant；WHERE Variant 销售策略表对该 SaleModel 无任何行 THE SYSTEM SHALL 视为 Variant 层 ALL 全开（但 variantPrice 为 null 的 Variant 仍不可展示购买）
- WHERE 某 Model 经 Variant 过滤后已无任何可售 Variant WHEN 组装 models THE SYSTEM SHALL 从结果中移除该 Model
- WHERE OptionCode 在销售策略中 saleStatus = off_shelf、或 optionPrice 为 null、或 availableRegions 不含用户 regionCode WHEN 组装 selectableFamilies THE SYSTEM SHALL 过滤掉该 OptionCode；WHERE OptionFamily 过滤后无任何 OptionCode 可选 THE SYSTEM SHALL 从树中移除该 family
- WHERE OptionCode 在 `tb_sale_model_option_policy`（归属键 `(saleModelCode, variantCode, optionCode)`）中无对应记录 WHEN 组装 selectableFamilies THE SYSTEM SHALL 视为未配置销售策略，直接过滤不展示

#### 实时报价
- WHEN 用户提交报价请求 `{ saleModelCode, modelCode, variantCode, optionCodes[], regionCode }` THE SYSTEM SHALL 在 500ms 内按以下步骤返回（顺序与 US-003 六步对齐，去防刷单步）：
  1. SaleModel 在售校验：listingStatus = active、时间窗有效、区域命中；失败返回 SALE_MODEL_NOT_EXIST（301003）
  2. Model 销售策略校验：`tb_sale_model_model_policy` saleStatus = active、区域/渠道命中（空表全开）；失败返回 MODEL_NOT_FOR_SALE（301040）
  3. Variant 销售策略校验：`tb_sale_model_variant_policy` saleStatus = active、variantPrice 非空、区域/渠道命中、时间窗有效（空表全开但要求 variantPrice 非空）；失败返回 VARIANT_NOT_FOR_SALE（301041）
  4. OptionCode 销售策略校验：每个 optionCode saleStatus = active、optionPrice 非空、区域命中；失败返回 OPTION_NOT_FOR_SALE（301036）或 OPTION_REGION_RESTRICTED（301037）
  5. 调 MDM `resolveConfiguration(variantCode, optionCodes)`：失败返回 CONFIGURATION_NOT_MATCHED（301010）
  6. Configuration 销售白名单校验：configurationCode 在 `tb_sale_model_config_policy` 中 status = active（空白名单视为 ALL）；失败返回 CONFIGURATION_NOT_FOR_SALE（301035）
- WHEN 六步校验全部通过 THE SYSTEM SHALL 计算总价 = variantPrice + Σ(optionPrice)，返回 `{ configurationCode, totalPrice, variantPrice, optionPriceBreakdown[] }`

#### 兜底
- WHEN MDM 服务超时（>2s）或不可用 THE SYSTEM SHALL 返回服务暂不可用提示，不允许用本地投影兜底完成报价（避免脏数据下单）
- WHERE MDM 服务不可用 WHEN 用户请求选配器只读展示（三段式数据组装） THE SYSTEM SHALL 可基于本地投影返回 models/variants/standardOptions/selectableFamilies（标注数据可能延迟）

### US-022: 销售策略管理（B 端 MPT）

**As a** B 端运营人员, **I want** 在 SaleModel 下维护 Model / Variant / Configuration / Option 四层销售策略, **so that** 精细化控制每个 SaleModel 下哪些 Model、哪些 Variant、哪些 Configuration、哪些 OptionCode 在售，以及价格、区域、渠道、营销元数据。

**修订点说明（CR-011）**：销售策略 CRUD 范围由 Configuration / Option 两层扩展为 Model / Variant / Configuration / Option 四层，四层语义一致（白名单/策略入表、saleStatus、自定义销售元数据、区域/渠道/有效时间、必选层兜底警告、CSV 批量导入）。新增 Model 层（不直接定价）与 Variant 层（承载 variantPrice/earnestMoneyPrice/downPaymentPrice）。Configuration / Option 销售策略沿用 CR-010 规则，但归属键由 `(saleModelCode, ...)` 调整为 `(saleModelCode, variantCode, ...)`，确保跨 Variant 隔离。空表语义：Model / Variant 层无任何行时视为该层 ALL 全开；Option 层保持"未在策略表中即过滤不展示"。

**Acceptance Criteria** (EARS 语法):

#### Model 销售策略
- WHEN 运营进入 SaleModel 销售策略页 THE SYSTEM SHALL 展示该 carlineCode 下 MDM 全部 Model，并标注每个 Model 是否已有销售策略及当前状态（saleStatus）
- WHERE modelCode 属于 SaleModel.carlineCode 下的 MDM 合法 Model WHEN 运营创建/更新 Model 策略 THE SYSTEM SHALL 写入 `tb_sale_model_model_policy`，字段含：modelCode、saleStatus（active / off_shelf / coming_soon）、availableRegions、channels、marketingName、marketingImage、marketingCopy、sortWeight、effectiveFrom、effectiveTo（Model 层不直接定价）
- **空表语义约定**：WHERE `tb_sale_model_model_policy` 中该 SaleModel 无任何行 WHEN 下单/改配/选配器校验 Model THE SYSTEM SHALL 视为 ALL 全开（该 Carline 下全部 Model 都可售）；WHERE 表中存在至少 1 行 THE SYSTEM SHALL 严格按表中且 saleStatus = active 的 Model 可售
- WHERE 运营把某 Model 标记 off_shelf 且该 SaleModel 下已无其它 active 的 Model WHEN 运营保存 THE SYSTEM SHALL 弹出强警告（"该 SaleModel 将无可售 Model，用户无法下单"），由运营确认后方可保存（不强制阻止）

#### Variant 销售策略
- WHEN 运营进入某 Model 下 Variant 策略页 THE SYSTEM SHALL 展示该 Model 下 MDM 全部 Variant，并标注是否已有销售策略及当前状态
- WHERE variantCode 属于已加入策略的某个 modelCode 下的 MDM 合法 Variant WHEN 运营创建/更新 Variant 策略 THE SYSTEM SHALL 写入 `tb_sale_model_variant_policy`，字段含：variantCode、saleStatus、availableRegions、channels、variantPrice、earnestMoneyPrice、downPaymentPrice、marketingName、marketingImage、marketingCopy、sortWeight、effectiveFrom、effectiveTo
- WHERE variantPrice 为空且 saleStatus = active WHEN 运营保存 Variant 策略 THE SYSTEM SHALL 拒绝保存并返回参数错误（价格必填才能上架）
- **空表语义约定**：WHERE `tb_sale_model_variant_policy` 中该 SaleModel 无任何行 WHEN 校验 Variant THE SYSTEM SHALL 视为 ALL 全开（但 variantPrice 为空的 Variant 仍不可售）；WHERE 存在至少 1 行 THE SYSTEM SHALL 严格按表中且 saleStatus = active、variantPrice 非空的 Variant 可售
- WHERE 运营把某 Variant 标记 off_shelf 且其所属 Model 下已无其它 active 的 Variant WHEN 运营保存 THE SYSTEM SHALL 弹出强警告（"该 Model 将无可售 Variant"），由运营确认后方可保存（不强制阻止）

#### 可售 Configuration 白名单
- WHEN 运营进入某 Variant 下 Configuration 策略页 THE SYSTEM SHALL 展示该 variantCode 下 MDM 全部 Configuration，并标注是否已在白名单
- WHERE configurationCode 属于该 variantCode 下的合法 Configuration WHEN 运营添加到白名单 THE SYSTEM SHALL 写入 `tb_sale_model_config_policy`（归属键 `(saleModelCode, variantCode, configurationCode)`），status 默认 active
- **白名单语义约定**：WHERE `tb_sale_model_config_policy` 中该 `(saleModelCode, variantCode)` 无任何行 WHEN 校验 Configuration THE SYSTEM SHALL 视为 ALL 全开；WHERE 存在至少 1 行 THE SYSTEM SHALL 严格白名单语义，仅在表中且 status = active 的可售
- WHERE 删除/下架某 Configuration 时存在关联活跃订单（非终态） WHEN 运营执行操作 THE SYSTEM SHALL 不阻止操作，但在响应中返回 affectedOrderCount 提示影响的订单数（订单基于快照运行不受影响）

#### OptionCode 销售策略
- WHEN 运营进入某 Variant 下 Option 策略页 THE SYSTEM SHALL 展示该 variantCode 下 MDM 全部 OptionCode（按 OptionFamily 分组），并标注每个 OptionCode 是否已有销售策略及当前状态（saleStatus）
- WHERE optionCode 属于该 variantCode 的 optionTree WHEN 运营创建/更新策略 THE SYSTEM SHALL 写入 `tb_sale_model_option_policy`（归属键 `(saleModelCode, variantCode, optionCode)`），字段含：saleStatus、availableRegions、channels、optionPrice、bundleWith、mutexWith、marketingTitle、marketingImage、sortWeight、effectiveFrom、effectiveTo；WHERE optionPrice 为空且 saleStatus = active THE SYSTEM SHALL 拒绝保存并返回参数错误（价格必填才能上架）
- WHEN 运营把某 OptionCode 标记 off_shelf 且该 OptionCode 属于 required = true 的 family 且 family 内已无其它 saleStatus = active 的 OptionCode THE SYSTEM SHALL 弹出强警告（"该 family 将无可选项，用户无法下单"），由运营确认后方可保存（不强制阻止）
- WHEN 同 family 内同时存在 bundleWith 和 mutexWith 配置导致逻辑矛盾（如 A.bundleWith = B 且 A.mutexWith = B） THE SYSTEM SHALL 保存前校验并返回参数错误，禁止矛盾配置入库

#### 批量导入
- WHEN 运营批量导入策略 THE SYSTEM SHALL 对 Model / Variant / Configuration / Option 四类策略各自支持 CSV 上传，单次最多 500 行，全量校验（归属合法性、价格必填等）通过才落库，响应时间 <5s

#### 响应时间
- 所有 CRUD 操作响应时间 <1s
- 批量导入（500 行内）响应时间 <5s

### US-023: MDM 主数据本地投影（系统）

**As a** 系统, **I want** 维护 MDM 主数据的本地只读投影, **so that** 配置器与下单链路无需对每个请求都跨服务调用 MDM。

**修订点说明（CR-011）**：投影对象由 3 类（Variant / Configuration / OptionCode）扩展为 5 类，新增 Carline 与 Model，并含归属关系链 Carline→Model[]→Variant[]。Kafka 订阅主题新增 `mdm.product.carline.changed`、`mdm.product.model.changed`。强制同步接口范围由单 Variant 扩展为整条 Carline 链路。

**Acceptance Criteria** (EARS 语法):

#### 初始化与订阅
- WHEN VSO 启动 THE SYSTEM SHALL 拉取所有 listingStatus = active 的 SaleModel 关联 carlineCode 对应的整条 MDM 主数据投影链路（Carline 元数据、该 Carline 下全部 Model 元数据、Model→Variant 归属关系、Variant 含标配 options、Configuration 列表含归属 variantCode/optionCodes/指导价、OptionFamily 树、OptionCode 元数据含 optionFamilyCode/optionName/互斥关系）
- WHEN VSO 订阅的 Kafka topic `mdm.product.carline.changed` / `mdm.product.model.changed` / `mdm.product.variant.changed` / `mdm.product.configuration.changed` / `mdm.product.option.changed` 收到事件 THE SYSTEM SHALL 在 30s 内更新本地投影并失效相关 Redis 缓存

#### 读路径
- WHEN 选配器/心愿单/下单/改配链路需要读 MDM 主数据 THE SYSTEM SHALL 优先读本地投影 + Redis 缓存（TTL 10 分钟）
- WHERE 本地投影中无某 carlineCode / modelCode / variantCode / configurationCode / optionCode 记录 WHEN 读路径访问 THE SYSTEM SHALL 实时回源 MDM 拉取并写入投影
- **投影仅读，不可直接写**：所有变更必须经由 Kafka 事件、初始化任务、运营触发的"强制同步"（见 US-016 / US-022）三条路径之一

#### 一致性兜底
- WHERE 检测到本地投影与 MDM 不一致（例如下单 resolveConfiguration 返回的 configurationCode 在本地投影中不存在） WHEN 异常发生 THE SYSTEM SHALL 记录告警日志、回源 MDM 补齐投影，并返回 MDM_PROJECTION_STALE（301039），由运营或下次重试解决
- WHEN 运营触发"强制同步"接口（见 US-016 / US-022） THE SYSTEM SHALL 全量重拉指定 carlineCode 整条链路的投影，忽略本地缓存

#### 投影范围
- 投影 5 类对象：
  - Carline：含 carlineCode、carlineName、下属 modelCodes[]
  - Model：含 modelCode、modelName、carlineCode、下属 variantCodes[]
  - Variant：含 variantCode、variantName、modelCode、modelName、标配 options[]
  - Configuration：含 configurationCode、variantCode、optionCodes[]、指导价
  - OptionCode：含 optionCode、optionFamilyCode、optionName、互斥关系（mutexWith[]、bundleWith[]）

### US-017: 订单列表与详情查询（MPT）

**As a** B 端运营人员, **I want** 查询和查看订单列表及详情, **so that** 了解订单状态并进行后续操作。

**Acceptance Criteria** (EARS 语法):
- WHEN 运营人员查询订单列表 THE SYSTEM SHALL 支持分页查询并返回订单基本信息，默认按创建时间倒序，单页最大 100 条，响应时间 <1s
- WHEN 运营人员查询可改配订单列表 THE SYSTEM SHALL 仅返回状态为 EARNEST_MONEY_PAID、DOWN_PAYMENT_UNPAID 或 DOWN_PAYMENT_PAID 且 buildConfigLock=false 的订单
- WHEN 运营人员查询无交付人员订单列表 THE SYSTEM SHALL 仅返回状态为 TRANSPORTING 且未分配交付人员的订单
- WHEN 运营人员查询可配车订单列表 THE SYSTEM SHALL 仅返回状态允许配车的订单
- WHEN 运营人员查询发运相关订单列表 THE SYSTEM SHALL 返回 APPLY_TRANSPORT、PREPARE_TRANSPORT、TRANSPORTING 阶段的订单信息
- WHERE 订单存在 WHEN 运营人员查询订单详情 THE SYSTEM SHALL 在 500ms 内返回订单完整信息（含参与方、金额、配置快照、状态维度、时间线事件）

### US-018: 支付回调处理

**As a** 外部支付系统, **I want** 通过回调接口通知支付结果, **so that** VSO 服务能及时更新订单支付状态。

**Acceptance Criteria** (EARS 语法):
- WHEN 支付回调到达 THE SYSTEM SHALL 验证 X-Signature 请求头签名；签名无效返回 403 并记录安全日志
- WHERE 签名验证通过、支付记录存在、金额匹配（精度到分） WHEN 支付回调数据有效 THE SYSTEM SHALL 在 2s 内更新支付记录状态并触发订单状态流转
- WHEN 支付回调重复到达（支付流水号已处理） THE SYSTEM SHALL 通过幂等机制保证不重复处理，直接返回成功
- WHEN 回调处理完成（无论成功或失败） THE SYSTEM SHALL 记录回调日志（含回调时间、支付流水号、处理结果、耗时）

#### 签名规范
- **签名算法**：HMAC-SHA256
- **签名内容**（按字段字典序排列，用 `&` 连接）：
  ```
  amount={金额}&nonce={随机串}&orderId={订单号}&paySeq={支付流水号}&status={支付状态}&timestamp={Unix秒}
  ```
- **防重放机制**：
  - `timestamp`：与服务器时间差 < 5 分钟
  - `nonce`：存 Redis SET，TTL 5 分钟，防止重放
- **响应规范**：
  - 验签失败 → 返回 403，**不记录支付流水号**（防信息泄露）
  - 验签通过 → 返回 200

### US-019: 超时自动处理

**As a** 系统, **I want** 自动处理超时订单, **so that** 未及时支付或处理的订单能被自动关闭或提醒。

**Acceptance Criteria** (EARS 语法):
- **调度方式**：采用 Spring `@Scheduled(fixedRate = 60000)` 定时扫描模式，每 60 秒（1 分钟）扫描一次过期任务队列
- **时间精度**：分钟级精度，超时阈值以分钟为单位配置，实际触发时间 = 任务创建时间 + 阈值分钟数，扫描周期内的最大延迟为 1 分钟
- WHERE 小定订单处于 EARNEST_MONEY_UNPAID 状态超过 30 分钟（阈值可配置，来源 paymentChannelConfig.smallOrderTimeoutMinutes） THE SYSTEM SHALL 在下一个扫描周期内（最大延迟 1 分钟）自动将订单状态设为 EXPIRED，触发策略为 `invalid`，由 `OrderDomainService.invalidateSmallOrder()` 执行（含事务保证和时间线记录）
- WHERE 大定订单处于待审核状态超过 1440 分钟（24 小时，阈值可配置） THE SYSTEM SHALL 在下一个扫描周期内发送提醒通知，触发策略为 `remind`
- WHERE 订单处于待签约状态超过 2880 分钟（48 小时，阈值可配置） THE SYSTEM SHALL 在下一个扫描周期内发送提醒通知，触发策略为 `remind`
- **补偿机制**：若超时任务处理失败（抛出异常），系统标记任务为 FAILED 并递增重试计数器；最多重试 3 次（`retryCount < 3`），超过 3 次后标记为 DONE 并发送告警通知（策略 `retry_and_alert`）
- **任务状态机**：PENDING → TRIGGERED → DONE/FAILED/CANCELLED，支付成功时自动取消对应订单的 SMALL_ORDER_PAY_TIMEOUT 任务
- **时钟漂移**：系统使用单节点 `LocalDateTime.now()` 进行时间比较，不跨节点同步时钟；若未来部署多实例，需改用 Redis 分布式时间或数据库行级锁保证一致性
- **⚠️ 多实例部署警告**：当前任务存储在 ConcurrentHashMap（单实例内存），多实例部署会导致任务被重复执行或漏执行。**生产部署前**必须迁移至：
  - **方案A（推荐）**：Redis 存储任务 + 分布式锁保证单节点执行
  - **方案B**：数据库任务表 + 行级锁
  迁移完成前，禁止部署多副本。

### US-020: 订单并发控制

**As a** 系统, **I want** 对订单关键操作进行并发控制, **so that** 防止并发操作导致数据不一致。

**Acceptance Criteria** (EARS 语法):
- **锁实现**：基于 Redis 的分布式锁（`OrderLockService`），锁键格式 `order:lock:{orderId}`，锁值格式 `{operatorId}:{lockScene}:{timestamp}`
- **锁 TTL**：默认 30 秒（`DEFAULT_EXPIRE_SECONDS = 30`），操作完成后自动释放
- WHILE 订单支付操作执行中 THE SYSTEM SHALL 持有该订单的分布式锁（场景 `payment`，TTL 30s）
- WHILE 订单锁单操作执行中 THE SYSTEM SHALL 持有该订单的分布式锁（场景 `lockOrder`，TTL 30s）
- WHILE 订单取消操作执行中 THE SYSTEM SHALL 持有该订单的分布式锁（场景 `cancel`，TTL 30s）
- WHILE 订单退款申请操作执行中 THE SYSTEM SHALL 持有该订单的分布式锁（场景 `refund`，TTL 30s）
- WHILE 车辆绑定操作执行中 THE SYSTEM SHALL 持有该订单的分布式锁（场景 `bindVehicle`，TTL 30s）
- IF 获取锁失败 THEN THE SYSTEM SHALL 拒绝操作并返回并发冲突错误（`IllegalStateException: 订单正在处理中，请稍后再试`）
- **锁续期**：操作完成后自动续期 5 秒再释放（`renewLock(orderId, operatorId, 5)`），防止操作接近 TTL 边界时锁提前过期
- **锁安全**：只有持锁人（operatorId 匹配）才能释放或续期锁，防止误释放其他操作的锁

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

## 6. 错误码表

| Code | Name | Description | HTTP |
|---|---|---|---|
| 301002 | CONFIGURATION_CODE_NOT_EXIST | 配置编码不存在（CR-010 重命名自 BUILD_CONFIG_CODE_NOT_EXIST） | 400 |
| 301008 | CONFIGURATION_HAS_LOCKED | 订单配置已锁定，不可修改（CR-010 重命名自 SALE_MODEL_CONFIG_HAS_LOCKED） | 409 |
| 301010 | CONFIGURATION_NOT_MATCHED | OptionCode 组合无法匹配到合法 Configuration（CR-010 重命名自 BUILD_CONFIG_NOT_MATCHED）；CR-011 起所引用的 variantCode 来自下单入参而非 SaleModel 直接绑定 | 400 |
| 301035 | CONFIGURATION_NOT_FOR_SALE | Configuration 可生产但未列入销售白名单 | 409 |
| 301036 | OPTION_NOT_FOR_SALE | OptionCode 在销售策略中处于 off_shelf 状态或未配置价格 | 409 |
| 301037 | OPTION_REGION_RESTRICTED | OptionCode 当前用户区域不可售 | 409 |
| 301038 | SALE_MODEL_VARIANT_LOCKED | 用于 Variant 销售策略层锁定校验（活跃订单 / 心愿单已引用该 Variant，不可下架或删除）；CR-011 起 SaleModel 的 carlineCode 锁定改用 301042 | 409 |
| 301039 | MDM_PROJECTION_STALE | MDM 本地投影过期或不一致，需触发强制同步 | 503 |
| 301040 | MODEL_NOT_FOR_SALE | Model 在销售策略中处于 off_shelf 或未配置 | 409 |
| 301041 | VARIANT_NOT_FOR_SALE | Variant 在销售策略中处于 off_shelf、未配置价格或区域/渠道未命中 | 409 |
| 301042 | SALE_MODEL_CARLINE_LOCKED | SaleModel 已有活跃订单或心愿单，不可修改 carlineCode | 409 |
| 301043 | MODEL_CHANGE_NOT_ALLOWED | 改配不允许跨 Model（动力切换）变更 | 409 |
| 301044 | SALE_MODEL_CHANGE_NOT_ALLOWED | 改配不允许跨 SaleModel 变更 | 409 |

## 7. Changelog

| Date | Change ID | Type | Description |
|------|-----------|------|-------------|
| 2026-05-23 | CR-001 | Added | 基于现有代码逆向生成初始需求文档 |
| 2026-05-23 | CR-002 | Fixed | 修复 EARS 语法不严谨问题：为所有 AC 添加 WHERE 前置条件、量化响应时间指标、明确分布式锁 TTL/重试策略/补偿机制；US-019 补充精度（分钟级）、调度方式（定时扫描 60s）、补偿（最多重试 3 次）、时钟漂移说明；US-020 补充锁实现细节、场景标识、续期策略 |
| 2026-05-23 | CR-003 | Fixed | 代码实现与需求对齐：接入状态机校验、补充分布式锁覆盖、修复超时任务重试逻辑、性能优化（N+1 查询+缓存）、功能补全（退款任务+心愿单删除+锁单超时） |
| 2026-05-25 | CR-004 | Added | US-006 意向金转定金需求补齐：新增补充信息采集（客户类型/支付方式/订购人信息等 10 个可选字段）、差额支付流程（意向金<定金时创建差额支付任务，30 分钟超时自动取消）、支付记录与回调处理、失败回滚机制（异常回滚至 EARNEST_MONEY_PAID）、幂等性控制（订单号作幂等键）、并发控制（分布式锁场景 convert/payment） |
| 2026-05-25 | CR-005 | Added | US-003/US-004 防刷单规则补齐：新增用户维度和手机号维度的未完成订单唯一性校验（订单状态为 EARNEST_MONEY_UNPAID 或 DOWN_PAYMENT_UNPAID 时禁止重复下单），错误码 301025 DUPLICATE_UNPAID_ORDER |
| 2026-05-25 | CR-006 | Added | US-002 心愿单数量上限与唯一性约束：新增数量上限（5个）校验、buildConfigCode 维度唯一性校验（创建/修改时均校验），错误码 301026 WISHLIST_LIMIT_EXCEEDED、301027 DUPLICATE_WISHLIST |
| 2026-05-25 | CR-007 | Added | US-011 配车业务规则补全：补全配车绑定、换绑 VIN、VIN 冲突检测、VIN 超时释放（72小时自动释放）、订单取消/退款时 VIN 释放、主动解绑 VIN、配车状态机、VIN 来源校验、并发控制、幂等性等业务规则；新增错误码 301030 VIN_CONFLICT、301031 VIN_INVALID |
| 2026-05-25 | CR-008 | Added | US-010 审核驳回可恢复路径：新增重提审核规则（最多 3 次，超限自动关闭）、驳回超时策略（72h 提醒+168h 自动关闭）、结构化驳回原因枚举（INCOMPLETE_INFO/INCORRECT_INFO/RISK_BLOCK/DUPLICATE_ORDER/OTHER）、驳回后可取消、审批记录扩展（action_type/reject_category/reject_reason/parent_record_id）；新增错误码 301028 AUDIT_RESUBMIT_LIMIT_EXCEEDED、301029 AUDIT_REJECT_REASON_REQUIRED |
| 2026-05-25 | CR-009 | Fixed | 需求文档问题修复与优化：①错误码优化：301015 CONCURRENT_CONFLICT 拆分为 301032 PAYMENT_CONFLICT（支付）、301033 LOCK_CONFLICT（锁单/退款/改配/关单）、301034 BIND_CONFLICT（配车/换绑）；②VIN 占用过期时间统一为 72 小时（修正 US-011 正文）；③US-008 补充意向金>定金超额退款规则；④US-019 增加多实例部署警告；⑤US-012/US-013 增加倒计时补偿机制（PREPARE_TRANSPORT→24h、TRANSPORTING→48h、PREPARE_DELIVER→24h、DELIVERED→72h）；⑥US-018 补充支付回调签名规范（HMAC-SHA256 + timestamp + nonce 防重放） |
| 2026-05-29 | CR-010 | Changed/Added | 销售车型 MDM 对齐重构：① 术语全量迁移（buildConfig→configuration、feature→option、baseModel→variant）；② SaleModel 由"1:N buildConfig"重构为"1:1 Variant + 销售策略"；③ 重写 US-001/US-016；④ 修订 US-002/US-003/US-004/US-007（入参改 optionCodes[]、新增 OptionCode/Configuration 销售策略校验、订单快照固化销售策略）；⑤ 新增 US-021 选配器、US-022 销售策略管理、US-023 MDM 本地投影；⑥ 错误码 301002/301008/301010 重命名，新增 301035~301039 |
| 2026-06-01 | CR-011 | Changed/Added | 销售车型粒度上提至 Carline 重构：① SaleModel 由"1:1 Variant"重构为"1:1 Carline"，下层新增 Model / Variant 两层可自定义销售策略（语义与 Configuration/Option 对齐：白名单/策略入表、saleStatus、营销元数据、区域/渠道/有效时间）；② 重写 US-001 / US-016 / US-021 / US-022 / US-023；③ 修订 US-002 / US-003 / US-004 / US-007（入参增加 modelCode + variantCode，下单校验由四步扩为六步，改配增加跨 Model / 跨 SaleModel 禁止 AC）；④ Option / Configuration 销售策略归属键由 saleModelCode 扩为 (saleModelCode, variantCode)；⑤ 错误码新增 301040~301044，301038 描述更新（改为 Variant 锁定语义）、301010 说明 variantCode 来自下单入参；⑥ 价格字段（startingPrice/earnestMoneyPrice/downPaymentPrice）从 SaleModel 下沉至 Variant 销售策略层，basePrice 废弃；⑦ 订单快照固化 carline/model/variant/config/option 五层销售策略行；⑧ Model/Variant 销售策略空表语义统一为 ALL 全开，Option 保持未配置即过滤 |
