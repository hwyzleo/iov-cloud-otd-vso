### 设计文档：支付回调接口实现

#### 简介
本文档定义 `/api/open/callback/v1/payment` 支付回调接口的详细设计。该接口用于接收外部支付系统的支付结果回调，实现支付状态更新、订单状态推进、回调日志记录等核心功能。

首期实现范围：
- 支付阶段：意向金（EARNEST_MONEY）
- 支付渠道：模拟调用（调用后即认为支付成功）
- 签名验证：预留但首期跳过

#### 设计目标
- 实现支付回调幂等处理，避免重复回调重复落库
- 支付成功后自动推进订单状态
- 完整记录回调原始报文与处理日志
- 为后续扩展其他支付阶段（定金、首付款、尾款）预留框架

#### 设计原则
- **幂等优先**：所有回调必须幂等处理，使用外部交易单号作为幂等键
- **日志留痕**：回调原始报文全量记录，处理结果完整记录
- **事件解耦**：使用领域事件解耦支付处理与订单状态推进
- **扩展预留**：支付阶段、签名验证等预留扩展能力

#### 架构设计

##### 整体架构
```
┌─────────────────────────────────────────────────────────────────┐
│  OpenCallbackController                                          │
│  - 接收回调请求                                                   │
│  - 验证请求格式                                                   │
│  - 调用 PaymentCallbackService                                   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  PaymentCallbackService                                          │
│  - 幂等处理（基于 idempotentKey）                                  │
│  - 记录回调日志（CallbackLogRepository）                          │
│  - 更新 Payment 状态                                              │
│  - 发布 PaymentSuccessDomainEvent                                │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼ (Spring ApplicationEvent)
┌─────────────────────────────────────────────────────────────────┐
│  PaymentSuccessEventListener                                     │
│  - 监听 PaymentSuccessDomainEvent                                │
│  - 推进 Order 状态                                                │
│  - 记录时间线                                                     │
│  - 清除超时任务                                                   │
└─────────────────────────────────────────────────────────────────┘
```

##### 事件驱动设计
采用 Spring ApplicationEvent 同步事件机制：
- PaymentCallbackService 发布 PaymentSuccessDomainEvent
- PaymentSuccessEventListener 监听事件并处理订单状态推进
- 后续可切换为异步事件（@Async）或消息队列（Kafka）

#### 数据结构设计

##### 回调请求结构（PaymentCallbackRequest）
```json
{
    "paymentNo": "Pabc123def456",
    "externalTradeNo": "WX202401...",
    "paymentStage": "EARNEST_MONEY",
    "paymentAmount": 5000.00,
    "paymentStatus": "SUCCESS",
    "payTime": "2024-01-15T10:30:00",
    "idempotentKey": "uuid-xxx",
    "signature": "sha256..."
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| paymentNo | String | 是 | 内部支付单号 |
| externalTradeNo | String | 是 | 外部交易单号，用于幂等 |
| paymentStage | String | 是 | 支付阶段，首期仅支持 EARNEST_MONEY |
| paymentAmount | BigDecimal | 是 | 支付金额 |
| paymentStatus | String | 是 | 支付状态，SUCCESS |
| payTime | LocalDateTime | 是 | 支付成功时间 |
| idempotentKey | String | 否 | 幂等键，无则使用 externalTradeNo |
| signature | String | 否 | 签名，首期跳过验证 |

##### 回调响应结构
成功：
```json
{
    "code": "SUCCESS",
    "message": "回调处理成功"
}
```

幂等重复：
```json
{
    "code": "DUPLICATE",
    "message": "该回调已处理"
}
```

失败：
```json
{
    "code": "FAIL",
    "message": "支付单不存在"
}
```

##### 回调日志存储（vso_callback_log）
| 字段 | 取值 |
|------|------|
| callback_log_id | 生成的业务ID |
| order_id | 关联的订单ID |
| business_type | PAYMENT |
| external_system_name | MOCK_PAY（模拟） |
| external_business_no | externalTradeNo |
| idempotent_key | PAY:{externalTradeNo} |
| callback_status_value | SUCCESS |
| callback_result_code | PAID |
| event_time | 回调携带的 payTime |
| request_body | JSON序列化的原始请求 |
| response_body | JSON序列化的响应 |
| process_result | SUCCESS / FAIL / DUPLICATE |
| manual_override_flag | 0 |
| process_time | 处理完成时间 |

#### 核心处理流程

##### PaymentCallbackService 处理流程
```
1. 参数校验
   ├─ paymentNo、externalTradeNo、paymentStage、paymentAmount 必填校验
   └─ paymentStage 必须为 EARNEST_MONEY（首期范围）

2. 幂等处理
   ├─ 构造幂等键：PAY:{externalTradeNo} 或使用 idempotentKey
   ├─ 用幂等键查询 CallbackLog 是否已存在
   ├─ 若存在且 processResult=SUCCESS → 返回 DUPLICATE
   └─ 若存在但 processResult=FAIL → 可重试处理

3. 获取 Payment 记录
   ├─ 按 paymentNo 查询 Payment
   └─ 若不存在 → 记录失败回调日志，返回 FAIL
   ├─ 若 paymentStatus 非 PENDING_PAYMENT → 记录冲突，返回 FAIL
   └─ 校验金额是否一致，不一致 → 记录差异，返回 FAIL

4. 记录回调日志
   ├─ 创建 CallbackLogPo
   ├─ 保存原始请求 requestBody
   └─ 设置 processResult=PROCESSING

5. 更新 Payment 状态
   ├─ 更新 PaymentPo.paymentStatus = PAID
   ├─ 更新 PaymentPo.payTime
   └─ 更新 PaymentPo.externalTradeNo

6. 发布领域事件
   ├─ 创建 PaymentSuccessDomainEvent
   └─ 包含 orderId、paymentId、paymentStage、paymentAmount、payTime
   └─ ApplicationEventPublisher.publishEvent(event)

7. 更新回调日志
   ├─ 设置 processResult=SUCCESS
   └─ 设置 processTime

8. 返回成功响应
```

##### PaymentSuccessEventListener 处理流程
```
PaymentSuccessEventListener.handlePaymentSuccess(event)
│
├─ 1. 获取 Order
│     └─ orderRepository.findById(event.orderId)
│
├─ 2. 推进 Order 状态（根据 paymentStage）
│     ├─ 若 paymentStage = EARNEST_MONEY
│     │   ├─ order.pay(event.paymentAmount)
│     │   ├─ 订单状态：EARNEST_MONEY_UNPAID → EARNEST_MONEY_PAID
│     │   ├─ payState：EARNEST_MONEY_PAID
│     │   └─ 记录 earnestMoneyTime、earnestMoneyAmount
│     │
│     └─ 后续扩展：定金、首付款、尾款处理
│
├─ 3. 记录时间线
│     ├─ 创建 OrderTimelinePo
│     ├─ eventType = PAYMENT_SUCCESS
│     └─ 记录支付阶段、金额、时间
│
├─ 4. 清除超时任务
│     └─ timeoutNotifyService.cancelTimeoutTask(orderId, "SMALL_ORDER_PAY_TIMEOUT")
│
└─ 5. 保存 Order
      └─ orderRepository.save(order)
```

#### 幂等处理设计

##### 幂等键构造规则
- 优先使用请求中的 `idempotentKey`
- 若无则使用 `externalTradeNo` 构造：`PAY:{externalTradeNo}`
- 幂等键存储在 `vso_callback_log.idempotent_key` 字段

##### 幂等查询策略
1. 按 idempotentKey 查询 CallbackLog
2. 若存在且 processResult=SUCCESS → 幂等返回 DUPLICATE
3. 若存在且 processResult=FAIL → 允许重试
4. 若不存在 → 正常处理

##### 幂等存储时机
- 回调开始处理时：先创建 CallbackLog，processResult=PROCESSING
- 回调处理成功时：更新 processResult=SUCCESS
- 回调处理失败时：更新 processResult=FAIL

#### 异常处理设计

| 场景 | 处理方式 | 响应码 | 响应消息 |
|------|----------|--------|----------|
| paymentNo不存在 | 记录失败日志 | FAIL | 支付单不存在 |
| paymentStatus不匹配 | 记录冲突日志 | FAIL | 支付单状态不匹配，当前状态：{status} |
| 金额不一致 | 记录差异日志 | FAIL | 金额不一致，期望：{expected}，实际：{actual} |
| paymentStage不支持 | 记录失败日志 | FAIL | 不支持的支付阶段：{stage} |
| 订单状态不允许支付 | 事件监听器中处理 | FAIL | 订单状态不允许支付 |
| 重复回调 | 幂等返回 | DUPLICATE | 该回调已处理 |
| 处理过程中异常 | 记录异常日志，回滚 | FAIL | 回调处理异常 |

#### 类与文件结构

```
otd-vso-service/src/main/java/net/hwyz/iov/cloud/otd/vso/service/
│
├─ adapter/web/
│   ├─ controller/open/
│   │   └─ OpenCallbackController.java          [修改] 实现payment回调逻辑
│   └─ vo/request/
│       └─ PaymentCallbackRequest.java          [新增] 回调请求VO
│
├─ application/
│   ├─ service/
│   │   └─ PaymentCallbackService.java          [新增] 回调处理服务
│   └─ dto/
│       ├─ cmd/
│       │   └─ PaymentCallbackCmd.java          [新增] 回调命令DTO
│       └─ result/
│           └─ PaymentCallbackResult.java       [新增] 回调结果DTO
│
├─ domain/
│   ├─ model/event/
│   │   └─ PaymentSuccessDomainEvent.java       [新增] 支付成功领域事件
│   ├─ repository/
│   │   ├─ PaymentRepository.java               [修改] 补充updateStatus方法
│   │   └─ CallbackLogRepository.java           [新增] 回调日志仓储接口
│   ├─ service/
│   │   └─ PaymentSuccessEventListener.java     [新增] 事件监听器
│   └─ gateway/
│       └─ IdempotentKeyGateway.java            [新增] 幂等键管理（可选）
│
├─ infrastructure/
│   ├─ persistence/repository/
│   │   ├─ PaymentRepositoryImpl.java           [修改] 补充updateStatus方法
│   │   ├─ CallbackLogRepositoryImpl.java       [新增]
│   │   └─ OrderTimelineRepositoryImpl.java     [补充] 时间线仓储实现
│   └─ config/
│       └─ PaymentCallbackConfig.java           [新增] 回调配置（可选）
│
└─ common/exception/
    ├─ PaymentNotExistException.java             [新增]
    ├─ PaymentStatusMismatchException.java       [新增]
    └─ DuplicateCallbackException.java           [新增]
```

#### 文件清单

| 文件 | 状态 | 说明 |
|------|------|------|
| OpenCallbackController.java | 修改 | 实现payment方法，调用PaymentCallbackService |
| PaymentCallbackRequest.java | 新增 | 回调请求VO，包含验证注解 |
| PaymentCallbackService.java | 新增 | 回调核心处理服务 |
| PaymentCallbackCmd.java | 新增 | 回调命令DTO |
| PaymentCallbackResult.java | 新增 | 回调结果DTO |
| PaymentSuccessDomainEvent.java | 新增 | 支付成功领域事件 |
| PaymentRepository.java | 修改 | 补充findByPaymentNo、updateStatus方法 |
| CallbackLogRepository.java | 新增 | 回调日志仓储接口 |
| PaymentSuccessEventListener.java | 新增 | 事件监听器 |
| PaymentRepositoryImpl.java | 修改 | 实现新增方法 |
| CallbackLogRepositoryImpl.java | 新增 | 回调日志仓储实现 |
| PaymentNotExistException.java | 新增 | 支付单不存在异常 |
| PaymentStatusMismatchException.java | 新增 | 支付状态不匹配异常 |

#### 扩展预留

##### 签名验证
- Controller 中预留 signature 参数
- PaymentCallbackConfig 中配置签名验证开关
- 后续实现签名验证逻辑时，只需在 PaymentCallbackService 中添加验证步骤

##### 多支付阶段
- PaymentSuccessDomainEvent 包含 paymentStage 字段
- PaymentSuccessEventListener 根据 paymentStage 分支处理
- 后续添加定金、首付款、尾款处理逻辑时，只需在监听器中添加分支

##### 异步事件
- 当前采用 Spring ApplicationEvent 同步机制
- 后续可切换为 @Async 异步或 Kafka 消息队列
- 只需修改事件发布和监听方式，核心处理逻辑不变

#### 验收标准

1. 调用 `/api/open/callback/v1/payment` 接口，传入有效 paymentNo 和 externalTradeNo，返回 SUCCESS
2. 相同 externalTradeNo 重复回调，返回 DUPLICATE
3. paymentNo 不存在，返回 FAIL，记录回调日志
4. paymentStatus 不匹配，返回 FAIL，记录冲突日志
5. 支付成功后，订单状态从 EARNEST_MONEY_UNPAID 变为 EARNEST_MONEY_PAID
6. 支付成功后，payState 变为 EARNEST_MONEY_PAID
7. 支付成功后，earnestMoneyTime、earnestMoneyAmount 正确记录
8. 支付成功后，Payment 表 paymentStatus、payTime、externalTradeNo 正确更新
9. 回调日志完整记录 request_body、response_body、process_result
10. 支付成功后，超时任务 SMALL_ORDER_PAY_TIMEOUT 被清除

#### 依赖关系

本设计依赖以下已存在的组件：
- PaymentPo、PaymentMapper、PaymentRepository
- CallbackLogPo、CallbackLogMapper
- Order、OrderState、OrderRepository
- TimeoutNotifyService
- OrderDomainService

需要补充的组件：
- OrderTimelinePo、OrderTimelineMapper、OrderTimelineRepository（时间线）

#### 测试策略

1. **单元测试**：PaymentCallbackService 各方法测试
2. **集成测试**：完整回调流程测试，包括事件监听
3. **幂等测试**：重复回调场景测试
4. **异常场景测试**：各种异常场景测试
5. **并发测试**：并发回调幂等测试